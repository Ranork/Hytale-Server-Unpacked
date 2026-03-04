package com.hypixel.hytale.server.core.universe.world.worldmap;

import com.hypixel.fastutil.longs.Long2ObjectConcurrentHashMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.common.util.CompletableFutureUtil;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.protocol.packets.player.RemoveMapMarker;
import com.hypixel.hytale.protocol.packets.worldmap.CreateUserMarker;
import com.hypixel.hytale.protocol.packets.worldmap.MapImage;
import com.hypixel.hytale.protocol.packets.worldmap.MapMarker;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerConfigData;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerWorldData;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.MarkersCollector;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.providers.DeathMarkerProvider;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.providers.OtherPlayersMarkerProvider;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.providers.POIMarkerProvider;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.providers.PersonalMarkersProvider;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.providers.RespawnMarkerProvider;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.providers.SharedMarkersProvider;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.providers.SpawnMarkerProvider;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMapMarker;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMapMarkersStore;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMarkerValidator;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.worldstore.WorldMarkersResource;
import com.hypixel.hytale.server.core.util.thread.TickingThread;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WorldMapManager extends TickingThread {
   private static final int IMAGE_KEEP_ALIVE = 60;
   private static final float DEFAULT_UNLOAD_DELAY = 1.0F;
   @Nonnull
   private final HytaleLogger logger;
   @Nonnull
   private final World world;
   private final Long2ObjectConcurrentHashMap<WorldMapManager.ImageEntry> images = new Long2ObjectConcurrentHashMap<>(
      true, ChunkUtil.indexChunk(Integer.MIN_VALUE, Integer.MIN_VALUE)
   );
   private final Long2ObjectConcurrentHashMap<CompletableFuture<MapImage>> generating = new Long2ObjectConcurrentHashMap<>(
      true, ChunkUtil.indexChunk(Integer.MIN_VALUE, Integer.MIN_VALUE)
   );
   private final Map<String, WorldMapManager.MarkerProvider> markerProviders = new ConcurrentHashMap<>();
   private final Map<String, MapMarker> pointsOfInterest = new ConcurrentHashMap<>();
   @Nonnull
   private WorldMapSettings worldMapSettings = WorldMapSettings.DISABLED;
   @Nullable
   private IWorldMap generator;
   @Nonnull
   private CompletableFuture<Void> generatorLoaded = new CompletableFuture<>();
   private float unloadDelay = 1.0F;

   public WorldMapManager(@Nonnull World world) {
      super("WorldMap - " + world.getName(), 10, true);
      this.logger = HytaleLogger.get("World|" + world.getName() + "|M");
      this.world = world;
      this.addMarkerProvider("spawn", SpawnMarkerProvider.INSTANCE);
      this.addMarkerProvider("playerIcons", OtherPlayersMarkerProvider.INSTANCE);
      this.addMarkerProvider("death", DeathMarkerProvider.INSTANCE);
      this.addMarkerProvider("respawn", RespawnMarkerProvider.INSTANCE);
      this.addMarkerProvider("personal", PersonalMarkersProvider.INSTANCE);
      this.addMarkerProvider("shared", SharedMarkersProvider.INSTANCE);
      this.addMarkerProvider("poi", POIMarkerProvider.INSTANCE);
   }

   @Nullable
   public IWorldMap getGenerator() {
      return this.generator;
   }

   public void setGenerator(@Nullable IWorldMap generator) {
      boolean before = this.shouldTick();
      if (this.generator != null) {
         this.generator.shutdown();
      }

      this.generator = generator;
      if (generator != null) {
         this.logger.at(Level.INFO).log("Initializing world map generator: %s", generator.toString());
         this.generatorLoaded.complete(null);
         this.generatorLoaded = new CompletableFuture<>();
         this.worldMapSettings = generator.getWorldMapSettings();
         this.images.clear();
         this.generating.clear();

         for (Player worldPlayer : this.world.getPlayers()) {
            worldPlayer.getWorldMapTracker().clear();
         }

         this.updateTickingState(before);
         this.sendSettings();
         this.logger.at(Level.INFO).log("Generating Points of Interest...");
         CompletableFutureUtil._catch(generator.generatePointsOfInterest(this.world).thenAcceptAsync(pointsOfInterest -> {
            this.pointsOfInterest.putAll((Map<? extends String, ? extends MapMarker>)pointsOfInterest);
            this.logger.at(Level.INFO).log("Finished Generating Points of Interest!");
         }));
      } else {
         this.logger.at(Level.INFO).log("World map disabled!");
         this.worldMapSettings = WorldMapSettings.DISABLED;
         this.sendSettings();
      }
   }

   @Override
   protected boolean isIdle() {
      return this.world.getPlayerCount() == 0;
   }

   @Override
   protected void tick(float dt) {
      for (Player player : this.world.getPlayers()) {
         player.getWorldMapTracker().tick(dt);
      }

      this.unloadDelay -= dt;
      if (this.unloadDelay <= 0.0F) {
         this.unloadDelay = 1.0F;
         this.unloadImages();
      }
   }

   @Override
   protected void onShutdown() {
   }

   public void unloadImages() {
      int imagesCount = this.images.size();
      if (imagesCount != 0) {
         List<Player> players = this.world.getPlayers();
         LongSet toRemove = new LongOpenHashSet();
         this.images.forEach((index, chunk) -> {
            if (this.isWorldMapEnabled() && isWorldMapImageVisibleToAnyPlayer(players, index, this.worldMapSettings)) {
               chunk.keepAlive.set(60);
            } else {
               if (chunk.keepAlive.decrementAndGet() <= 0) {
                  toRemove.add(index);
               }
            }
         });
         if (!toRemove.isEmpty()) {
            toRemove.forEach(value -> {
               this.logger.at(Level.FINE).log("Unloading world map image: %s", value);
               this.images.remove(value);
            });
         }

         int toRemoveSize = toRemove.size();
         if (toRemoveSize > 0) {
            this.logger
               .at(Level.FINE)
               .log("Cleaned %s world map images from memory, with %s images remaining in memory.", toRemoveSize, imagesCount - toRemoveSize);
         }
      }
   }

   public boolean isWorldMapEnabled() {
      return this.worldMapSettings.getSettingsPacket().enabled;
   }

   public static boolean isWorldMapImageVisibleToAnyPlayer(@Nonnull List<Player> players, long imageIndex, @Nonnull WorldMapSettings settings) {
      for (Player player : players) {
         int viewRadius = settings.getViewRadius(player.getViewRadius());
         if (player.getWorldMapTracker().shouldBeVisible(viewRadius, imageIndex)) {
            return true;
         }
      }

      return false;
   }

   @Nonnull
   public World getWorld() {
      return this.world;
   }

   @Nonnull
   public WorldMapSettings getWorldMapSettings() {
      return this.worldMapSettings;
   }

   public Map<String, WorldMapManager.MarkerProvider> getMarkerProviders() {
      return this.markerProviders;
   }

   public void addMarkerProvider(@Nonnull String key, @Nonnull WorldMapManager.MarkerProvider provider) {
      this.markerProviders.put(key, provider);
   }

   public Map<String, MapMarker> getPointsOfInterest() {
      return this.pointsOfInterest;
   }

   @Nullable
   public MapImage getImageIfInMemory(int x, int z) {
      return this.getImageIfInMemory(ChunkUtil.indexChunk(x, z));
   }

   @Nullable
   public MapImage getImageIfInMemory(long index) {
      WorldMapManager.ImageEntry pair = this.images.get(index);
      return pair != null ? pair.image : null;
   }

   @Nonnull
   public CompletableFuture<MapImage> getImageAsync(int x, int z) {
      return this.getImageAsync(ChunkUtil.indexChunk(x, z));
   }

   @Nonnull
   public CompletableFuture<MapImage> getImageAsync(long index) {
      WorldMapManager.ImageEntry pair = this.images.get(index);
      MapImage image = pair != null ? pair.image : null;
      if (image != null) {
         return CompletableFuture.completedFuture(image);
      } else {
         CompletableFuture<MapImage> gen = this.generating.get(index);
         if (gen != null) {
            return gen;
         } else {
            int imageSize = MathUtil.fastFloor(32.0F * this.worldMapSettings.getImageScale());
            LongSet chunksToGenerate = new LongOpenHashSet();
            chunksToGenerate.add(index);
            CompletableFuture<MapImage> future = CompletableFutureUtil._catch(
               this.generator.generate(this.world, imageSize, imageSize, chunksToGenerate).thenApplyAsync(worldMap -> {
                  MapImage newImage = (MapImage)worldMap.getChunks().get(index);
                  if (this.generating.remove(index) != null) {
                     this.images.put(index, new WorldMapManager.ImageEntry(newImage));
                  }

                  return newImage;
               })
            );
            this.generating.put(index, future);
            return future;
         }
      }
   }

   public void generate() {
   }

   public void sendSettings() {
      for (Player player : this.world.getPlayers()) {
         player.getWorldMapTracker().sendSettings(this.world);
      }
   }

   public boolean shouldTick() {
      return this.world.getWorldConfig().isCompassUpdating() || this.isWorldMapEnabled();
   }

   public void updateTickingState(boolean before) {
      boolean after = this.shouldTick();
      if (before != after) {
         if (after) {
            this.start();
         } else {
            this.stop();
         }
      }
   }

   public void handleUserCreateMarker(PlayerRef playerRef, CreateUserMarker packet) {
      Ref<EntityStore> ref = playerRef.getReference();
      if (ref != null) {
         UserMarkerValidator.PlaceResult validation = UserMarkerValidator.validatePlacing(ref, packet);
         if (validation instanceof UserMarkerValidator.CanSpawn canSpawn) {
            Store var13 = ref.getStore();
            UserMapMarkersStore markersStore = canSpawn.markersStore();
            DisplayNameComponent displayNameComponent = var13.getComponent(ref, DisplayNameComponent.getComponentType());
            String createdByName = displayNameComponent == null ? playerRef.getUsername() : displayNameComponent.getDisplayName().getRawText();
            UserMapMarker userMapMarker = new UserMapMarker();
            userMapMarker.setId("user_" + (packet.shared ? "shared" : "personal") + "_" + UUID.randomUUID());
            userMapMarker.setPosition(packet.x, packet.z);
            userMapMarker.setName(packet.name);
            userMapMarker.setIcon(packet.markerImage == null ? "User1.png" : packet.markerImage);
            userMapMarker.setColorTint(packet.tintColor == null ? new Color((byte)0, (byte)0, (byte)0) : packet.tintColor);
            userMapMarker.withCreatedByName(createdByName);
            userMapMarker.withCreatedByUuid(playerRef.getUuid());
            markersStore.addUserMapMarker(userMapMarker);
         } else {
            if (validation instanceof UserMarkerValidator.Fail(Message displayNameComponent)) {
               playerRef.sendMessage(displayNameComponent.color("#ffc800"));
            }
         }
      }
   }

   public void handleUserRemoveMarker(PlayerRef playerRef, RemoveMapMarker packet) {
      Ref<EntityStore> ref = playerRef.getReference();
      if (ref != null) {
         Store<EntityStore> store = ref.getStore();
         World world = store.getExternalData().getWorld();
         Player player = store.getComponent(ref, Player.getComponentType());
         PlayerWorldData perWorldData = player.getPlayerConfigData().getPerWorldData(world.getName());
         boolean removedDeathMarker = perWorldData.removeLastDeath(packet.markerId);
         if (!removedDeathMarker) {
            WorldMapManager.MarkerAndItsStore userMarkerAndStore = this.findUserMapMarker(packet.markerId, player, world);
            if (userMarkerAndStore == null) {
               playerRef.sendMessage(Message.translation("server.worldmap.markers.edit.notFound").color("#ffc800"));
               HytaleLogger.getLogger()
                  .at(Level.WARNING)
                  .log("Couldn't find marker to remove '" + packet.markerId + "' from " + playerRef.getUsername() + " " + playerRef.getUuid());
            } else {
               UserMarkerValidator.RemoveResult validation = UserMarkerValidator.validateRemove(ref, userMarkerAndStore.marker);
               if (!(validation instanceof UserMarkerValidator.CanRemove)) {
                  if (validation instanceof UserMarkerValidator.Fail(Message var13)) {
                     playerRef.sendMessage(var13.color("#ffc800"));
                  }
               } else {
                  userMarkerAndStore.store.removeUserMapMarker(userMarkerAndStore.marker.getId());
               }
            }
         }
      }
   }

   @Nullable
   private WorldMapManager.MarkerAndItsStore findUserMapMarker(String markerId, Player player, World world) {
      PlayerWorldData perWorldData = player.getPlayerConfigData().getPerWorldData(world.getName());
      UserMapMarker personalMarker = perWorldData.getUserMapMarker(markerId);
      if (personalMarker != null) {
         return new WorldMapManager.MarkerAndItsStore(personalMarker, perWorldData);
      } else {
         WorldMarkersResource sharedMarkers = world.getChunkStore().getStore().getResource(WorldMarkersResource.getResourceType());
         UserMapMarker sharedMarker = sharedMarkers.getUserMapMarker(markerId);
         return sharedMarker != null ? new WorldMapManager.MarkerAndItsStore(sharedMarker, sharedMarkers) : null;
      }
   }

   public void clearImages() {
      this.images.clear();
      this.generating.clear();
   }

   public void clearImagesInChunks(@Nonnull LongSet chunkIndices) {
      chunkIndices.forEach(index -> {
         this.images.remove(index);
         this.generating.remove(index);
      });
   }

   public static void sendSettingsToAllWorlds() {
      for (World world : Universe.get().getWorlds().values()) {
         world.execute(() -> world.getWorldMapManager().sendSettings());
      }
   }

   static {
      WorldMapManager.MarkerReference.CODEC.register("Player", WorldMapManager.PlayerMarkerReference.class, WorldMapManager.PlayerMarkerReference.CODEC);
   }

   public static class ImageEntry {
      private final AtomicInteger keepAlive = new AtomicInteger();
      private final MapImage image;

      public ImageEntry(MapImage image) {
         this.image = image;
      }
   }

   private record MarkerAndItsStore(UserMapMarker marker, UserMapMarkersStore store) {
   }

   public interface MarkerProvider {
      void update(@Nonnull World var1, @Nonnull Player var2, @Nonnull MarkersCollector var3);
   }

   public interface MarkerReference {
      CodecMapCodec<WorldMapManager.MarkerReference> CODEC = new CodecMapCodec<>();

      String getMarkerId();

      void remove();
   }

   public static class PlayerMarkerReference implements WorldMapManager.MarkerReference {
      public static final BuilderCodec<WorldMapManager.PlayerMarkerReference> CODEC = BuilderCodec.builder(
            WorldMapManager.PlayerMarkerReference.class, WorldMapManager.PlayerMarkerReference::new
         )
         .addField(
            new KeyedCodec<>("Player", Codec.UUID_BINARY),
            (playerMarkerReference, uuid) -> playerMarkerReference.player = uuid,
            playerMarkerReference -> playerMarkerReference.player
         )
         .addField(
            new KeyedCodec<>("World", Codec.STRING),
            (playerMarkerReference, s) -> playerMarkerReference.world = s,
            playerMarkerReference -> playerMarkerReference.world
         )
         .addField(
            new KeyedCodec<>("MarkerId", Codec.STRING),
            (playerMarkerReference, s) -> playerMarkerReference.markerId = s,
            playerMarkerReference -> playerMarkerReference.markerId
         )
         .build();
      private UUID player;
      private String world;
      private String markerId;

      private PlayerMarkerReference() {
      }

      public PlayerMarkerReference(@Nonnull UUID player, @Nonnull String world, @Nonnull String markerId) {
         this.player = player;
         this.world = world;
         this.markerId = markerId;
      }

      public UUID getPlayer() {
         return this.player;
      }

      @Override
      public String getMarkerId() {
         return this.markerId;
      }

      @Override
      public void remove() {
         PlayerRef playerRef = Universe.get().getPlayer(this.player);
         if (playerRef != null) {
            Player playerComponent = playerRef.getComponent(Player.getComponentType());
            this.removeMarkerFromOnlinePlayer(playerComponent);
         } else {
            this.removeMarkerFromOfflinePlayer();
         }
      }

      private void removeMarkerFromOnlinePlayer(@Nonnull Player player) {
         PlayerConfigData data = player.getPlayerConfigData();
         String world = this.world;
         if (world == null) {
            world = player.getWorld().getName();
         }

         removeMarkerFromData(data, world, this.markerId);
      }

      private void removeMarkerFromOfflinePlayer() {
         Universe.get().getPlayerStorage().load(this.player).thenApply(holder -> {
            Player player = holder.getComponent(Player.getComponentType());
            PlayerConfigData data = player.getPlayerConfigData();
            String world = this.world;
            if (world == null) {
               world = data.getWorld();
            }

            removeMarkerFromData(data, world, this.markerId);
            return holder;
         }).thenCompose(holder -> Universe.get().getPlayerStorage().save(this.player, (Holder<EntityStore>)holder));
      }

      @Nullable
      private static void removeMarkerFromData(@Nonnull PlayerConfigData data, @Nonnull String worldName, @Nonnull String markerId) {
         PlayerWorldData perWorldData = data.getPerWorldData(worldName);
         ArrayList<? extends UserMapMarker> playerMarkers = new ArrayList<>(perWorldData.getUserMapMarkers());
         playerMarkers.removeIf(marker -> markerId.equals(marker.getId()));
         perWorldData.setUserMapMarkers(playerMarkers);
      }
   }
}
