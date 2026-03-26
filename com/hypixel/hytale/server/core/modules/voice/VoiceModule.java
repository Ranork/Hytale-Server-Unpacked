package com.hypixel.hytale.server.core.modules.voice;

import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.packets.serveraccess.Access;
import com.hypixel.hytale.protocol.packets.stream.StreamType;
import com.hypixel.hytale.server.core.Constants;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.io.ServerManager;
import com.hypixel.hytale.server.core.io.stream.StreamManager;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.singleplayer.SingleplayerRequestAccessEvent;
import com.hypixel.hytale.server.core.modules.voice.commands.VoiceCommand;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import com.hypixel.hytale.server.core.util.concurrent.ThreadUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class VoiceModule extends JavaPlugin {
   private static final long POSITION_CACHE_UPDATE_INTERVAL_MS = 100L;
   public static final PluginManifest MANIFEST = PluginManifest.corePlugin(VoiceModule.class).depends(Universe.class).depends(EntityModule.class).build();
   private static VoiceModule instance;
   @Nonnull
   private final Config<VoiceModuleConfig> config = this.withConfig("VoiceModule", VoiceModuleConfig.CODEC);
   private static final int MAX_PACKETS_PER_SECOND = 60;
   private static final int BURST_CAPACITY = 25;
   private static final int MAX_PACKET_SIZE = 1024;
   private static final double PLAYER_EYE_HEIGHT_OFFSET = 1.62;
   private final Map<UUID, VoicePlayerState> playerStates = new ConcurrentHashMap<>();
   private VoiceRouter voiceRouter;
   private static final int VOICE_THREAD_POOL_SIZE = 4;
   private final ExecutorService[] voiceExecutors;
   private volatile boolean isShutdown = false;
   private final ConcurrentHashMap<UUID, VoiceModule.PositionSnapshot> positionCache = new ConcurrentHashMap<>();
   private ScheduledFuture<?> positionUpdateTask;

   public static VoiceModule get() {
      return instance;
   }

   public VoiceModule(@Nonnull JavaPluginInit init) {
      super(init);
      instance = this;
      this.voiceExecutors = new ExecutorService[4];

      for (int i = 0; i < 4; i++) {
         this.voiceExecutors[i] = Executors.newSingleThreadExecutor(ThreadUtil.daemon("VoiceRouter-" + i));
      }
   }

   @Override
   protected void setup() {
      if (Constants.SINGLEPLAYER) {
         this.config.get().setVoiceEnabled(false);
      }

      this.voiceRouter = new VoiceRouter(this);
      this.getCommandRegistry().registerCommand(new VoiceCommand());
      ServerManager.get().registerSubPacketHandlers(VoicePacketHandler::new);
      StreamManager.getInstance().registerHandler(StreamType.Voice, VoiceStreamHandler::new);
      this.getLogger().at(Level.INFO).log("[Voice] Registered voice stream handler");
      this.getEventRegistry().register(PlayerConnectEvent.class, this::onPlayerConnect);
      this.getEventRegistry().register(PlayerDisconnectEvent.class, this::onPlayerDisconnect);
      if (Constants.SINGLEPLAYER) {
         this.getEventRegistry().register(SingleplayerRequestAccessEvent.class, this::onServerAccessChanged);
      }

      this.getLogger()
         .at(Level.INFO)
         .log(
            "[Voice] VoiceModule initialized (maxDistance=%.1f, refDistance=%.1f, enabled=%s, voiceStreamSupported=true)",
            this.getMaxHearingDistance(),
            this.getReferenceDistance(),
            this.isVoiceEnabled()
         );
   }

   @Override
   protected void start() {
      this.positionUpdateTask = HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(this::updateAllPlayerPositions, 100L, 100L, TimeUnit.MILLISECONDS);
      this.getLogger().at(Level.INFO).log("[Voice] Started position cache update task (%dms interval)", 100L);
   }

   private void updateAllPlayerPositions() {
      if (!this.isShutdown) {
         HashMap<World, List<UUID>> playersByWorld = new HashMap<>();

         for (Entry<UUID, VoicePlayerState> entry : this.playerStates.entrySet()) {
            UUID playerId = entry.getKey();
            PlayerRef playerRef = Universe.get().getPlayer(playerId);
            if (playerRef != null) {
               Ref<EntityStore> ref = playerRef.getReference();
               if (ref != null) {
                  Store<EntityStore> store = ref.getStore();
                  if (store != null) {
                     EntityStore externalData = store.getExternalData();
                     if (externalData != null) {
                        World world = externalData.getWorld();
                        if (world != null) {
                           playersByWorld.computeIfAbsent(world, k -> new ArrayList<>()).add(playerId);
                        }
                     }
                  }
               }
            }
         }

         for (Entry<World, List<UUID>> worldEntry : playersByWorld.entrySet()) {
            World world = worldEntry.getKey();
            List<UUID> playerIds = worldEntry.getValue();
            world.execute(
               () -> {
                  for (UUID playerId : playerIds) {
                     PlayerRef freshPlayerRef = Universe.get().getPlayer(playerId);
                     if (freshPlayerRef != null) {
                        Ref<EntityStore> freshRef = freshPlayerRef.getReference();
                        if (freshRef != null && freshRef.isValid()) {
                           Store<EntityStore> freshStore = freshRef.getStore();
                           if (freshStore != null) {
                              EntityStore freshExternalData = freshStore.getExternalData();
                              if (freshExternalData != null) {
                                 World freshWorld = freshExternalData.getWorld();
                                 if (freshWorld != null) {
                                    long currentWorldId = freshWorld.getWorldConfig().getUuid().getMostSignificantBits();
                                    TransformComponent transform = freshStore.getComponent(freshRef, EntityModule.get().getTransformComponentType());
                                    if (transform != null) {
                                       boolean isUnderwater = this.isEyeInFluid(transform.getPosition(), freshWorld);
                                       int networkId = 0;
                                       NetworkId networkIdComp = freshStore.getComponent(freshRef, NetworkId.getComponentType());
                                       if (networkIdComp != null) {
                                          networkId = networkIdComp.getId();
                                       }

                                       boolean isDead = freshStore.getComponent(freshRef, DeathComponent.getComponentType()) != null;
                                       this.voiceRouter
                                          .updateSpeakerPositionCache(freshPlayerRef, transform.getPosition(), isUnderwater, currentWorldId, networkId, isDead);
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            );
         }
      }
   }

   @Override
   protected void shutdown() {
      this.isShutdown = true;
      if (this.positionUpdateTask != null) {
         this.positionUpdateTask.cancel(false);
      }

      for (ExecutorService executor : this.voiceExecutors) {
         executor.shutdown();
      }

      try {
         for (ExecutorService executor : this.voiceExecutors) {
            if (!executor.awaitTermination(5L, TimeUnit.SECONDS)) {
               this.getLogger().at(Level.WARNING).log("[Voice] VoiceExecutor did not terminate in time, forcing shutdown");
               executor.shutdownNow();
            }
         }
      } catch (InterruptedException var6) {
         this.getLogger().at(Level.WARNING).log("[Voice] VoiceExecutor shutdown interrupted");

         for (ExecutorService executorx : this.voiceExecutors) {
            executorx.shutdownNow();
         }

         Thread.currentThread().interrupt();
      }

      this.playerStates.clear();
      this.positionCache.clear();
      this.getLogger().at(Level.INFO).log("[Voice] VoiceModule shutting down");
   }

   private void onServerAccessChanged(@Nonnull SingleplayerRequestAccessEvent event) {
      if (event.getAccess() != Access.Private && !this.isVoiceEnabled()) {
         this.setVoiceEnabled(true);
         this.getLogger().at(Level.INFO).log("[Voice] Auto-enabled voice for %s play", event.getAccess());
      } else if (event.getAccess() == Access.Private && this.isVoiceEnabled()) {
         this.setVoiceEnabled(false);
         this.getLogger().at(Level.INFO).log("[Voice] Auto-disabled voice — returned to singleplayer");
      }
   }

   private void onPlayerConnect(@Nonnull PlayerConnectEvent event) {
      PlayerRef playerRef = event.getPlayerRef();
      VoicePlayerState state = new VoicePlayerState(playerRef.getUuid(), this.getLogger(), 25);
      this.playerStates.put(playerRef.getUuid(), state);
      this.getLogger()
         .at(Level.FINE)
         .log("[Voice] Player connected: %s (%s), totalPlayers=%d", playerRef.getUsername(), playerRef.getUuid(), this.playerStates.size());
      this.voiceRouter.sendVoiceConfig(playerRef);
      this.scheduleImmediatePositionUpdate(playerRef);
   }

   public void scheduleImmediatePositionUpdate(@Nonnull PlayerRef playerRef) {
      UUID playerId = playerRef.getUuid();
      Ref<EntityStore> ref = playerRef.getReference();
      if (ref != null) {
         Store<EntityStore> store = ref.getStore();
         if (store != null) {
            EntityStore externalData = store.getExternalData();
            if (externalData != null) {
               World world = externalData.getWorld();
               if (world != null) {
                  world.execute(() -> {
                     PlayerRef freshPlayerRef = Universe.get().getPlayer(playerId);
                     if (freshPlayerRef != null) {
                        Ref<EntityStore> freshRef = freshPlayerRef.getReference();
                        if (freshRef != null && freshRef.isValid()) {
                           Store<EntityStore> freshStore = freshRef.getStore();
                           if (freshStore != null) {
                              EntityStore freshExternalData = freshStore.getExternalData();
                              if (freshExternalData != null) {
                                 World freshWorld = freshExternalData.getWorld();
                                 if (freshWorld != null) {
                                    long worldIdHash = freshWorld.getWorldConfig().getUuid().getMostSignificantBits();
                                    TransformComponent transform = freshStore.getComponent(freshRef, EntityModule.get().getTransformComponentType());
                                    if (transform != null) {
                                       boolean isUnderwater = this.isEyeInFluid(transform.getPosition(), freshWorld);
                                       int networkId = 0;
                                       NetworkId networkIdComp = freshStore.getComponent(freshRef, NetworkId.getComponentType());
                                       if (networkIdComp != null) {
                                          networkId = networkIdComp.getId();
                                       }

                                       boolean isDead = freshStore.getComponent(freshRef, DeathComponent.getComponentType()) != null;
                                       this.updatePositionCache(playerId, transform.getPosition(), isUnderwater, worldIdHash, networkId, isDead);
                                       this.getLogger().at(Level.FINE).log("[Voice] Immediate position cache populated for %s", freshPlayerRef.getUsername());
                                    }
                                 }
                              }
                           }
                        }
                     }
                  });
               }
            }
         }
      }
   }

   private void onPlayerDisconnect(@Nonnull PlayerDisconnectEvent event) {
      PlayerRef playerRef = event.getPlayerRef();
      VoicePlayerState state = this.playerStates.remove(playerRef.getUuid());
      this.positionCache.remove(playerRef.getUuid());
      this.voiceRouter.removePlayerFromWorldSets(playerRef.getUuid());
      String stats = state != null ? state.getStatsString() : "no state";
      this.getLogger()
         .at(Level.FINE)
         .log(
            "[Voice] Player disconnected: %s (%s), stats=[%s], remainingPlayers=%d",
            playerRef.getUsername(),
            playerRef.getUuid(),
            stats,
            this.playerStates.size()
         );
   }

   public VoicePlayerState getPlayerState(@Nonnull UUID playerId) {
      return this.playerStates.get(playerId);
   }

   public VoiceRouter getVoiceRouter() {
      return this.voiceRouter;
   }

   public boolean isVoiceEnabled() {
      return this.config.get().isVoiceEnabled();
   }

   public void setVoiceEnabled(boolean enabled) {
      boolean wasEnabled = this.config.get().isVoiceEnabled();
      this.config.get().setVoiceEnabled(enabled);
      if (wasEnabled != enabled) {
         this.getLogger().at(Level.INFO).log("[Voice] Voice enabled changed: %s -> %s", wasEnabled, enabled);
         this.broadcastConfigToAllPlayers();
         this.config.save();
      }
   }

   public boolean isDeadPlayersCanHear() {
      return this.config.get().isDeadPlayersCanHear();
   }

   public float getMaxHearingDistance() {
      return this.config.get().getMaxHearingDistance();
   }

   public void setMaxHearingDistance(float distance) {
      float oldDistance = this.config.get().getMaxHearingDistance();
      this.config.get().setMaxHearingDistance(distance);
      if (oldDistance != distance) {
         this.getLogger().at(Level.INFO).log("[Voice] Max hearing distance changed: %.1f -> %.1f", oldDistance, distance);
         this.broadcastConfigToAllPlayers();
         this.config.save();
      }
   }

   public float getReferenceDistance() {
      return this.config.get().getFullVolumeDistance();
   }

   public void setReferenceDistance(float distance) {
      float oldDistance = this.config.get().getFullVolumeDistance();
      this.config.get().setFullVolumeDistance(distance);
      if (oldDistance != distance) {
         this.getLogger().at(Level.INFO).log("[Voice] Full volume distance changed: %.1f -> %.1f", oldDistance, distance);
         this.broadcastConfigToAllPlayers();
         this.config.save();
      }
   }

   private void broadcastConfigToAllPlayers() {
      for (Entry<UUID, VoicePlayerState> entry : this.playerStates.entrySet()) {
         PlayerRef playerRef = Universe.get().getPlayer(entry.getKey());
         if (playerRef != null) {
            this.voiceRouter.sendVoiceConfig(playerRef);
         }
      }
   }

   private void broadcastMuteUpdate(@Nonnull UUID playerId, boolean isMuted) {
      this.broadcastConfigToAllPlayers();
      this.getLogger().at(Level.INFO).log("[Voice] Broadcast config update for mute change: playerId=%s, isMuted=%s", playerId, isMuted);
   }

   public boolean isPlayerMuted(@Nonnull UUID playerId) {
      return this.config.get().isPlayerMuted(playerId);
   }

   public boolean mutePlayer(@Nonnull UUID playerId) {
      boolean added = this.config.get().mutePlayer(playerId);
      if (added) {
         this.getLogger().at(Level.INFO).log("[Voice] Player globally muted: %s", playerId);
         this.broadcastMuteUpdate(playerId, true);
         this.config.save();
      }

      return added;
   }

   public boolean unmutePlayer(@Nonnull UUID playerId) {
      boolean removed = this.config.get().unmutePlayer(playerId);
      if (removed) {
         this.getLogger().at(Level.INFO).log("[Voice] Player globally unmuted: %s", playerId);
         this.broadcastMuteUpdate(playerId, false);
         this.config.save();
      }

      return removed;
   }

   public Set<UUID> getGloballyMutedPlayers() {
      return this.config.get().getMutedPlayers();
   }

   public int getMaxPacketsPerSecond() {
      return 60;
   }

   public int getBurstCapacity() {
      return 25;
   }

   public int getMaxPacketSize() {
      return 1024;
   }

   private boolean isEyeInFluid(@Nonnull Vector3d position, @Nonnull World world) {
      int blockX = MathUtil.floor(position.getX());
      int blockY = MathUtil.floor(position.getY() + 1.62);
      int blockZ = MathUtil.floor(position.getZ());
      ChunkStore chunkStore = world.getChunkStore();
      long chunkIndex = ChunkUtil.indexChunkFromBlock(blockX, blockZ);
      Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(chunkIndex);
      if (chunkRef != null && chunkRef.isValid()) {
         WorldChunk worldChunk = chunkStore.getStore().getComponent(chunkRef, WorldChunk.getComponentType());
         return worldChunk == null ? false : worldChunk.getFluidId(blockX, blockY, blockZ) != 0;
      } else {
         return false;
      }
   }

   @Deprecated
   public void updatePositionCache(@Nonnull UUID playerId, @Nonnull Vector3d position) {
      this.updatePositionCache(playerId, position, false, 0L, 0, false);
   }

   @Deprecated
   public void updatePositionCache(@Nonnull UUID playerId, @Nonnull Vector3d position, boolean isUnderwater) {
      this.updatePositionCache(playerId, position, isUnderwater, 0L, 0, false);
   }

   @Deprecated
   public void updatePositionCache(@Nonnull UUID playerId, @Nonnull Vector3d position, boolean isUnderwater, long worldId, int networkId) {
      this.updatePositionCache(playerId, position, isUnderwater, worldId, networkId, false);
   }

   public void updatePositionCache(@Nonnull UUID playerId, @Nonnull Vector3d position, boolean isUnderwater, long worldId, int networkId, boolean isDead) {
      this.positionCache
         .put(
            playerId,
            new VoiceModule.PositionSnapshot(
               position.getX(), position.getY() + 1.62, position.getZ(), isUnderwater, worldId, networkId, isDead, System.currentTimeMillis()
            )
         );
   }

   public VoiceModule.PositionSnapshot getCachedPosition(@Nonnull UUID playerId) {
      return this.positionCache.get(playerId);
   }

   public ExecutorService getVoiceExecutor(@Nonnull UUID speakerId) {
      int index = Math.floorMod(speakerId.hashCode(), 4);
      return this.voiceExecutors[index];
   }

   public boolean isShutdown() {
      return this.isShutdown;
   }

   public Map<UUID, VoicePlayerState> getPlayerStates() {
      return this.playerStates;
   }

   public record PositionSnapshot(double x, double y, double z, boolean isUnderwater, long worldId, int networkId, boolean isDead, long timestamp) {
   }
}
