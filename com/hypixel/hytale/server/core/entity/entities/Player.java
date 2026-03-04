package com.hypixel.hytale.server.core.entity.entities;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.event.IEventDispatcher;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.metrics.MetricProvider;
import com.hypixel.hytale.metrics.MetricResults;
import com.hypixel.hytale.metrics.MetricsRegistry;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.MovementStates;
import com.hypixel.hytale.protocol.SavedMovementStates;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.protocol.packets.player.SetBlockPlacementOverride;
import com.hypixel.hytale.protocol.packets.player.SetGameMode;
import com.hypixel.hytale.protocol.packets.player.SetMovementStates;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.gamemode.GameModeType;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.player.CameraManager;
import com.hypixel.hytale.server.core.entity.entities.player.HotbarManager;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerConfigData;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerRespawnPointData;
import com.hypixel.hytale.server.core.entity.entities.player.hud.HudManager;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.entity.entities.player.pages.PageManager;
import com.hypixel.hytale.server.core.entity.entities.player.windows.WindowManager;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.event.events.ecs.ChangeGameModeEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackSlotTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.modules.collision.CollisionModule;
import com.hypixel.hytale.server.core.modules.collision.CollisionResult;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.CollisionResultComponent;
import com.hypixel.hytale.server.core.modules.entity.component.Invulnerable;
import com.hypixel.hytale.server.core.modules.entity.component.RespondToHit;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.player.ChunkTracker;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSettings;
import com.hypixel.hytale.server.core.modules.entity.tracker.LegacyEntityTrackerSystems;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.permissions.PermissionHolder;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldMapTracker;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.hypixel.hytale.server.core.util.TempAssetIdUtil;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Player extends LivingEntity implements CommandSender, PermissionHolder, MetricProvider {
   @Nonnull
   public static final MetricsRegistry<Player> METRICS_REGISTRY = new MetricsRegistry<Player>()
      .register("Uuid", Entity::getUuid, Codec.UUID_STRING)
      .register("ClientViewRadius", Player::getClientViewRadius, Codec.INTEGER);
   @Nonnull
   public static final KeyedCodec<PlayerConfigData> PLAYER_CONFIG_DATA = new KeyedCodec<>("PlayerData", PlayerConfigData.CODEC);
   @Nonnull
   public static final BuilderCodec<Player> CODEC = BuilderCodec.builder(Player.class, Player::new, LivingEntity.CODEC)
      .append(PLAYER_CONFIG_DATA, (player, data) -> player.data = data, player -> player.data)
      .add()
      .append(
         new KeyedCodec<>("BlockPlacementOverride", Codec.BOOLEAN),
         (player, blockPlacementOverride) -> player.overrideBlockPlacementRestrictions = blockPlacementOverride,
         player -> player.overrideBlockPlacementRestrictions
      )
      .add()
      .append(
         new KeyedCodec<>("HotbarManager", HotbarManager.CODEC),
         (player, hotbarManager) -> player.hotbarManager = hotbarManager,
         player -> player.hotbarManager
      )
      .add()
      .<GameMode>appendInherited(
         new KeyedCodec<>("GameMode", ProtocolCodecs.GAMEMODE_LEGACY),
         (player, s) -> player.gameMode = s,
         player -> player.gameMode,
         (player, parent) -> player.gameMode = parent.gameMode
      )
      .documentation("The last known game-mode of the entity.")
      .add()
      .build();
   public static final int DEFAULT_VIEW_RADIUS_CHUNKS = 6;
   public static final long RESPAWN_INVULNERABILITY_TIME_NANOS = TimeUnit.MILLISECONDS.toNanos(3000L);
   public static final long MAX_TELEPORT_INVULNERABILITY_MILLIS = 10000L;
   @Deprecated(forRemoval = true)
   private PlayerRef playerRef;
   @Nonnull
   private PlayerConfigData data = new PlayerConfigData();
   @Nonnull
   private final WorldMapTracker worldMapTracker = new WorldMapTracker(this);
   @Nonnull
   private final WindowManager windowManager = new WindowManager();
   @Nonnull
   private final PageManager pageManager = new PageManager();
   @Nonnull
   private final HudManager hudManager = new HudManager();
   @Nonnull
   private HotbarManager hotbarManager = new HotbarManager();
   private GameMode gameMode;
   private int clientViewRadius = 6;
   protected long lastSpawnTimeNanos;
   private static final int MAX_VELOCITY_SAMPLE_COUNT = 2;
   private static final int VELOCITY_SAMPLE_LENGTH = 12;
   private static final double[][] velocitySampleWeights = new double[][]{{1.0}, {0.9, 0.1}};
   private final double[] velocitySamples = new double[12];
   private int velocitySampleCount;
   private int velocitySampleIndex = 4;
   private boolean overrideBlockPlacementRestrictions;
   private final AtomicInteger readyId = new AtomicInteger();
   private final AtomicReference<ScheduledFuture<?>> waitingForClientReady = new AtomicReference<>();
   public boolean executeTriggers;
   public boolean executeBlockDamage;
   private boolean firstSpawn;
   private int mountEntityId;

   @Nonnull
   public static ComponentType<EntityStore, Player> getComponentType() {
      return EntityModule.get().getPlayerComponentType();
   }

   public void copyFrom(@Nonnull Player oldPlayerComponent) {
      this.init(this.legacyUuid, this.playerRef);
      this.worldMapTracker.copyFrom(oldPlayerComponent.worldMapTracker);
      this.clientViewRadius = oldPlayerComponent.clientViewRadius;
      this.readyId.set(oldPlayerComponent.readyId.get());
   }

   public void init(@Nonnull UUID uuid, @Nonnull PlayerRef playerRef) {
      this.legacyUuid = uuid;
      this.playerRef = playerRef;
      this.windowManager.init(playerRef);
      this.pageManager.init(playerRef, this.windowManager);
   }

   public void setNetworkId(int id) {
      this.networkId = id;
   }

   @Nonnull
   @Override
   protected Inventory createDefaultInventory() {
      return new Inventory();
   }

   @Nonnull
   @Override
   public Inventory setInventory(Inventory inventory) {
      return super.setInventory(inventory, true);
   }

   @Override
   public boolean remove() {
      if (this.wasRemoved.getAndSet(true)) {
         return false;
      } else {
         this.removedBy = new Throwable();
         if (this.world != null && this.world.isAlive()) {
            if (this.world.isInThread()) {
               Ref<EntityStore> ref = this.playerRef.getReference();
               if (ref != null) {
                  Store<EntityStore> store = ref.getStore();
                  ChunkTracker tracker = store.getComponent(ref, ChunkTracker.getComponentType());
                  if (tracker != null) {
                     tracker.unloadAll(this.playerRef);
                  }

                  this.playerRef.removeFromStore();
               }
            } else {
               this.world.execute(() -> {
                  Ref<EntityStore> ref = this.playerRef.getReference();
                  if (ref != null) {
                     Store<EntityStore> storex = ref.getStore();
                     ChunkTracker trackerx = storex.getComponent(ref, ChunkTracker.getComponentType());
                     if (trackerx != null) {
                        trackerx.unloadAll(this.playerRef);
                     }

                     this.playerRef.removeFromStore();
                  }
               });
            }
         }

         if (this.playerRef.getPacketHandler().getChannel().isActive()) {
            this.playerRef.getPacketHandler().disconnect("Player removed from world!");
            ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(this.removedBy)).log("Player removed from world! %s", this);
         }

         ScheduledFuture<?> task;
         if ((task = this.waitingForClientReady.getAndSet(null)) != null) {
            task.cancel(false);
         }

         return true;
      }
   }

   @Override
   public void moveTo(@Nonnull Ref<EntityStore> ref, double locX, double locY, double locZ, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      Vector3d position = transformComponent.getPosition();
      this.addLocationChange(ref, locX - position.getX(), locY - position.getY(), locZ - position.getZ(), componentAccessor);
      super.moveTo(ref, locX, locY, locZ, componentAccessor);
      this.windowManager.validateWindows(ref, componentAccessor);
   }

   @Nonnull
   public PlayerConfigData getPlayerConfigData() {
      return this.data;
   }

   @Override
   public void markNeedsSave() {
      this.data.markChanged();
   }

   @Override
   public void unloadFromWorld() {
      super.unloadFromWorld();
   }

   public void applyMovementStates(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull SavedMovementStates savedMovementStates,
      @Nonnull MovementStates movementStates,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      movementStates.flying = savedMovementStates.flying;
      PlayerRef playerRefComponent = componentAccessor.getComponent(ref, PlayerRef.getComponentType());

      assert playerRefComponent != null;

      playerRefComponent.getPacketHandler().writeNoCache(new SetMovementStates(new SavedMovementStates(movementStates.flying)));
   }

   public void startClientReadyTimeout() {
      ScheduledFuture<?> task = HytaleServer.SCHEDULED_EXECUTOR.schedule(() -> this.handleClientReady(true), 10000L, TimeUnit.MILLISECONDS);
      ScheduledFuture<?> oldTask = this.waitingForClientReady.getAndSet(task);
      if (oldTask != null) {
         oldTask.cancel(false);
      }
   }

   public void handleClientReady(boolean forced) {
      ScheduledFuture<?> task;
      if ((task = this.waitingForClientReady.getAndSet(null)) != null) {
         task.cancel(false);
         if (this.world == null) {
            return;
         }

         IEventDispatcher<PlayerReadyEvent, PlayerReadyEvent> dispatcher = HytaleServer.get()
            .getEventBus()
            .dispatchFor(PlayerReadyEvent.class, this.world.getName());
         if (dispatcher.hasListener()) {
            dispatcher.dispatch(new PlayerReadyEvent(this.reference, this, this.readyId.getAndIncrement()));
         }
      }
   }

   public void sendInventory() {
      this.getInventory().consumeIsDirty();
      this.playerRef.getPacketHandler().write(this.getInventory().toPacket());
   }

   @Nonnull
   public CompletableFuture<Void> saveConfig(@Nonnull World world, @Nonnull Holder<EntityStore> holder) {
      MovementStatesComponent movementStatesComponent = holder.getComponent(MovementStatesComponent.getComponentType());

      assert movementStatesComponent != null;

      UUIDComponent uuidComponent = holder.getComponent(UUIDComponent.getComponentType());

      assert uuidComponent != null;

      this.data.getPerWorldData(world.getName()).setLastMovementStates(movementStatesComponent.getMovementStates(), false);
      return Universe.get().getPlayerStorage().save(uuidComponent.getUuid(), holder);
   }

   @Deprecated(forRemoval = true)
   public PacketHandler getPlayerConnection() {
      return this.playerRef.getPacketHandler();
   }

   @Nonnull
   public WorldMapTracker getWorldMapTracker() {
      return this.worldMapTracker;
   }

   @Nonnull
   public WindowManager getWindowManager() {
      return this.windowManager;
   }

   @Nonnull
   public PageManager getPageManager() {
      return this.pageManager;
   }

   @Nonnull
   public HudManager getHudManager() {
      return this.hudManager;
   }

   @Nonnull
   public HotbarManager getHotbarManager() {
      return this.hotbarManager;
   }

   public boolean isFirstSpawn() {
      return this.firstSpawn;
   }

   public void setFirstSpawn(boolean firstSpawn) {
      this.firstSpawn = firstSpawn;
   }

   public void resetManagers(@Nonnull Holder<EntityStore> holder) {
      PlayerRef playerRef = this.playerRef;
      LegacyEntityTrackerSystems.clear(this, holder);
      this.worldMapTracker.clear();
      this.hudManager.resetUserInterface(this.playerRef);
      this.hudManager.resetHud(this.playerRef);
      CameraManager cameraManagerComponent = playerRef.getComponent(CameraManager.getComponentType());

      assert cameraManagerComponent != null;

      cameraManagerComponent.resetCamera(playerRef);
      MovementManager movementManagerComponent = playerRef.getComponent(MovementManager.getComponentType());

      assert movementManagerComponent != null;

      movementManagerComponent.applyDefaultSettings();
      movementManagerComponent.update(playerRef.getPacketHandler());
   }

   public void notifyPickupItem(
      @Nonnull Ref<EntityStore> ref, @Nonnull ItemStack itemStack, @Nullable Vector3d position, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      World world = componentAccessor.getExternalData().getWorld();
      if (world.getGameplayConfig().getShowItemPickupNotifications()) {
         PlayerRef playerRefComponent = componentAccessor.getComponent(ref, PlayerRef.getComponentType());

         assert playerRefComponent != null;

         Message itemNameMessage = Message.translation(itemStack.getItem().getTranslationKey());
         NotificationUtil.sendNotification(
            playerRefComponent.getPacketHandler(),
            Message.translation("server.general.pickedUpItem").param("item", itemNameMessage),
            null,
            itemStack.toPacket()
         );
      }

      if (position != null) {
         SoundUtil.playSoundEvent3dToPlayer(ref, TempAssetIdUtil.getSoundEventIndex("SFX_Player_Pickup_Item"), SoundCategory.UI, position, componentAccessor);
      } else {
         SoundUtil.playSoundEvent2d(ref, TempAssetIdUtil.getSoundEventIndex("SFX_Player_Pickup_Item"), SoundCategory.UI, componentAccessor);
      }
   }

   public boolean isOverrideBlockPlacementRestrictions() {
      return this.overrideBlockPlacementRestrictions;
   }

   public void setOverrideBlockPlacementRestrictions(
      @Nonnull Ref<EntityStore> ref, boolean overrideBlockPlacementRestrictions, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      this.overrideBlockPlacementRestrictions = overrideBlockPlacementRestrictions;
      PlayerRef playerRefComponent = componentAccessor.getComponent(ref, PlayerRef.getComponentType());

      assert playerRefComponent != null;

      playerRefComponent.getPacketHandler().writeNoCache(new SetBlockPlacementOverride(overrideBlockPlacementRestrictions));
   }

   @Override
   public void sendMessage(@Nonnull Message message) {
      this.playerRef.sendMessage(message);
   }

   @Override
   public boolean hasPermission(@Nonnull String id) {
      return PermissionsModule.get().hasPermission(this.getUuid(), id);
   }

   @Override
   public boolean hasPermission(@Nonnull String id, boolean def) {
      return PermissionsModule.get().hasPermission(this.getUuid(), id, def);
   }

   public void addLocationChange(
      @Nonnull Ref<EntityStore> ref, double deltaX, double deltaY, double deltaZ, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      CollisionResultComponent collisionResultComponent = componentAccessor.getComponent(ref, CollisionResultComponent.getComponentType());

      assert collisionResultComponent != null;

      collisionResultComponent.getCollisionPositionOffset().add(deltaX, deltaY, deltaZ);
      if (!collisionResultComponent.isPendingCollisionCheck()) {
         TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

         assert transformComponent != null;

         Vector3d position = transformComponent.getPosition();
         collisionResultComponent.getCollisionStartPosition().assign(position);
         collisionResultComponent.markPendingCollisionCheck();
      }
   }

   public void configTriggerBlockProcessing(boolean triggers, boolean blockDamage, @Nonnull CollisionResultComponent collisionResultComponent) {
      this.executeTriggers = triggers;
      this.executeBlockDamage = blockDamage;
      if (!triggers && !blockDamage) {
         collisionResultComponent.getCollisionResult().disableTriggerBlocks();
      } else {
         collisionResultComponent.getCollisionResult().enableTriggerBlocks();
      }
   }

   public void resetVelocity(@Nonnull Velocity velocity) {
      Arrays.fill(this.velocitySamples, 0.0);
      this.velocitySampleIndex = 4;
      this.velocitySampleCount = 0;
      velocity.setZero();
   }

   public void processVelocitySample(double dt, @Nonnull Vector3d position, @Nonnull Velocity velocity) {
      double x = position.x;
      double y = position.y;
      double z = position.z;
      if (dt != 0.0) {
         this.velocitySamples[this.velocitySampleIndex] = x;
         this.velocitySamples[this.velocitySampleIndex + 1] = y;
         this.velocitySamples[this.velocitySampleIndex + 2] = z;
         this.velocitySamples[this.velocitySampleIndex + 3] = dt;
         int index = this.velocitySampleIndex;
         this.velocitySampleIndex += 4;
         if (this.velocitySampleIndex >= 12) {
            this.velocitySampleIndex = 4;
         }

         if (this.velocitySampleCount < 2) {
            this.velocitySampleCount++;
         }

         if (this.velocitySampleCount < 2) {
            velocity.setZero();
         } else {
            for (int i = 0; i < 4; i++) {
               this.velocitySamples[i] = 0.0;
            }

            double[] weights = velocitySampleWeights[this.velocitySampleCount - 2];

            for (int i = 0; i < this.velocitySampleCount - 1; i++) {
               int previousIndex = index - 4;
               if (previousIndex < 4) {
                  previousIndex = 8;
               }

               double k = weights[i] / this.velocitySamples[index + 3];
               this.velocitySamples[0] = this.velocitySamples[0] + k * (this.velocitySamples[index] - this.velocitySamples[previousIndex]);
               this.velocitySamples[1] = this.velocitySamples[1] + k * (this.velocitySamples[index + 1] - this.velocitySamples[previousIndex + 1]);
               this.velocitySamples[2] = this.velocitySamples[2] + k * (this.velocitySamples[index + 2] - this.velocitySamples[previousIndex + 2]);
               index = previousIndex;
            }

            velocity.set(this.velocitySamples[0], this.velocitySamples[1], this.velocitySamples[2]);
         }
      }
   }

   @Nonnull
   public static CompletableFuture<Transform> getRespawnPosition(
      @Nonnull Ref<EntityStore> ref, @Nonnull String worldName, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      Player playerComponent = componentAccessor.getComponent(ref, getComponentType());

      assert playerComponent != null;

      World world = componentAccessor.getExternalData().getWorld();
      PlayerConfigData playerConfigData = playerComponent.data;
      PlayerRespawnPointData[] respawnPoints = playerConfigData.getPerWorldData(worldName).getRespawnPoints();
      if (respawnPoints != null && respawnPoints.length != 0) {
         TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

         assert transformComponent != null;

         Vector3d playerPosition = transformComponent.getPosition();
         List<PlayerRespawnPointData> sortedRespawnPoints = Arrays.stream(respawnPoints).sorted((a, b) -> {
            Vector3d posA = a.getRespawnPosition();
            Vector3d posB = b.getRespawnPosition();
            double distA = playerPosition.distanceSquaredTo(posA.x, playerPosition.y, posA.z);
            double distB = playerPosition.distanceSquaredTo(posB.x, playerPosition.y, posB.z);
            return Double.compare(distA, distB);
         }).toList();
         BoundingBox playerBoundingBoxComponent = componentAccessor.getComponent(ref, BoundingBox.getComponentType());
         return playerBoundingBoxComponent == null
            ? CompletableFuture.completedFuture(new Transform(sortedRespawnPoints.getFirst().getRespawnPosition()))
            : tryUseSpawnPoint(world, sortedRespawnPoints, 0, ref, playerComponent, playerBoundingBoxComponent.getBoundingBox());
      } else {
         Transform worldSpawnPoint = world.getWorldConfig().getSpawnProvider().getSpawnPoint(ref, componentAccessor);
         worldSpawnPoint.setRotation(Vector3f.ZERO);
         return CompletableFuture.completedFuture(worldSpawnPoint);
      }
   }

   @Nonnull
   private static CompletableFuture<Transform> tryUseSpawnPoint(
      World world, List<PlayerRespawnPointData> sortedRespawnPoints, int index, Ref<EntityStore> ref, Player playerComponent, Box boundingBox
   ) {
      if (sortedRespawnPoints != null && index < sortedRespawnPoints.size()) {
         PlayerRespawnPointData respawnPoint = sortedRespawnPoints.get(index);
         LongOpenHashSet requiredChunks = new LongOpenHashSet();
         if (respawnPoint.getRespawnPosition() != null) {
            boundingBox.forEachBlock(respawnPoint.getRespawnPosition(), 2.0, requiredChunks, (x, y, z, chunks) -> {
               chunks.add(ChunkUtil.indexChunkFromBlock(x, z));
               return true;
            });
         }

         if (respawnPoint.getBlockPosition() != null) {
            boundingBox.forEachBlock(respawnPoint.getBlockPosition().toVector3d(), 2.0, requiredChunks, (x, y, z, chunks) -> {
               chunks.add(ChunkUtil.indexChunkFromBlock(x, z));
               return true;
            });
         }

         CompletableFuture<WorldChunk>[] chunkFutures = new CompletableFuture[requiredChunks.size()];
         int i = 0;
         LongIterator iterator = requiredChunks.iterator();

         while (iterator.hasNext()) {
            long chunkIndex = iterator.nextLong();
            chunkFutures[i++] = world.getChunkStore().getChunkReferenceAsync(chunkIndex).thenApplyAsync(v -> {
               if (v != null && v.isValid()) {
                  WorldChunk wc = v.getStore().getComponent((Ref<ChunkStore>)v, WorldChunk.getComponentType());

                  assert wc != null;

                  wc.addKeepLoaded();
                  return wc;
               } else {
                  return null;
               }
            }, world);
         }

         return CompletableFuture.allOf(chunkFutures)
            .thenApplyAsync(v -> {
               Vector3d pos = ensureNoCollisionAtRespawnPosition(respawnPoint, boundingBox, world);
               if (pos != null) {
                  return new Transform(pos, Vector3f.ZERO);
               } else {
                  playerComponent.sendMessage(Message.translation("server.general.respawnPointObstructed").param("respawnPointName", respawnPoint.getName()));
                  return null;
               }
            }, world)
            .whenComplete((unused, throwable) -> {
               for (CompletableFuture<WorldChunk> future : chunkFutures) {
                  future.thenAccept(WorldChunk::removeKeepLoaded);
               }
            })
            .thenCompose(
               v -> v != null
                  ? CompletableFuture.completedFuture(v)
                  : tryUseSpawnPoint(world, sortedRespawnPoints, index + 1, ref, playerComponent, boundingBox)
            );
      } else {
         playerComponent.sendMessage(Message.translation("server.general.allRespawnPointsObstructed"));
         return CompletableFuture.supplyAsync(() -> {
            if (!ref.isValid()) {
               return new Transform();
            } else {
               Transform worldSpawnPoint = world.getWorldConfig().getSpawnProvider().getSpawnPoint(ref, ref.getStore());
               worldSpawnPoint.setRotation(Vector3f.ZERO);
               return worldSpawnPoint;
            }
         }, world);
      }
   }

   @Nullable
   private static Vector3d ensureNoCollisionAtRespawnPosition(PlayerRespawnPointData playerRespawnPointData, Box playerHitbox, World world) {
      Vector3d respawnPosition = new Vector3d(playerRespawnPointData.getRespawnPosition());
      if (CollisionModule.get().validatePosition(world, playerHitbox, respawnPosition, new CollisionResult()) != -1) {
         return respawnPosition;
      } else {
         respawnPosition.x = playerRespawnPointData.getBlockPosition().x + 0.5F;
         respawnPosition.y = playerRespawnPointData.getBlockPosition().y;
         respawnPosition.z = playerRespawnPointData.getBlockPosition().z + 0.5F;

         for (int distance = 1; distance <= 2; distance++) {
            for (int offset = -distance; offset <= distance; offset++) {
               Vector3d newPosition = new Vector3d(respawnPosition.x + offset, respawnPosition.y, respawnPosition.z - distance);
               if (CollisionModule.get().validatePosition(world, playerHitbox, newPosition, new CollisionResult()) != -1) {
                  return newPosition;
               }

               newPosition = new Vector3d(respawnPosition.x + offset, respawnPosition.y, respawnPosition.z + distance);
               if (CollisionModule.get().validatePosition(world, playerHitbox, newPosition, new CollisionResult()) != -1) {
                  return newPosition;
               }
            }

            for (int offset = -distance + 1; offset < distance; offset++) {
               Vector3d newPositionx = new Vector3d(respawnPosition.x - distance, respawnPosition.y, respawnPosition.z + offset);
               if (CollisionModule.get().validatePosition(world, playerHitbox, newPositionx, new CollisionResult()) != -1) {
                  return newPositionx;
               }

               newPositionx = new Vector3d(respawnPosition.x + distance, respawnPosition.y, respawnPosition.z + offset);
               if (CollisionModule.get().validatePosition(world, playerHitbox, newPositionx, new CollisionResult()) != -1) {
                  return newPositionx;
               }
            }
         }

         return null;
      }
   }

   public boolean hasSpawnProtection() {
      return System.nanoTime() - this.lastSpawnTimeNanos <= RESPAWN_INVULNERABILITY_TIME_NANOS || this.waitingForClientReady.get() != null;
   }

   public boolean isWaitingForClientReady() {
      return this.waitingForClientReady.get() != null;
   }

   @Override
   public boolean isHiddenFromLivingEntity(
      @Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      UUIDComponent uuidComponent = componentAccessor.getComponent(ref, UUIDComponent.getComponentType());

      assert uuidComponent != null;

      PlayerRef targetPlayerComponent = componentAccessor.getComponent(targetRef, PlayerRef.getComponentType());
      return targetPlayerComponent != null && targetPlayerComponent.getHiddenPlayersManager().isPlayerHidden(uuidComponent.getUuid());
   }

   public void setClientViewRadius(int clientViewRadius) {
      this.clientViewRadius = clientViewRadius;
   }

   public int getClientViewRadius() {
      return this.clientViewRadius;
   }

   public int getViewRadius() {
      return Math.min(this.clientViewRadius, HytaleServer.get().getConfig().getMaxViewRadius());
   }

   @Override
   public boolean canDecreaseItemStackDurability(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      Player playerComponent = componentAccessor.getComponent(ref, getComponentType());

      assert playerComponent != null;

      return playerComponent.gameMode != GameMode.Creative;
   }

   @Override
   public boolean canApplyItemStackPenalties(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      Player playerComponent = componentAccessor.getComponent(ref, getComponentType());

      assert playerComponent != null;

      return playerComponent.gameMode != GameMode.Creative;
   }

   @Nullable
   @Override
   public ItemStackSlotTransaction updateItemStackDurability(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull ItemStack itemStack,
      ItemContainer container,
      int slotId,
      double durabilityChange,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      ItemStackSlotTransaction transaction = super.updateItemStackDurability(ref, itemStack, container, slotId, durabilityChange, componentAccessor);
      if (transaction != null && transaction.getSlotAfter().isBroken() && !itemStack.isBroken()) {
         Message itemNameMessage = Message.translation(itemStack.getItem().getTranslationKey());
         this.sendMessage(Message.translation("server.general.repair.itemBroken").param("itemName", itemNameMessage).color("#ff5555"));
         PlayerRef playerRefComponent = componentAccessor.getComponent(ref, PlayerRef.getComponentType());

         assert playerRefComponent != null;

         int soundEventIndex = TempAssetIdUtil.getSoundEventIndex("SFX_Item_Break");
         SoundUtil.playSoundEvent2dToPlayer(playerRefComponent, soundEventIndex, SoundCategory.SFX);
      }

      return transaction;
   }

   @Nonnull
   @Override
   public MetricResults toMetricResults() {
      return METRICS_REGISTRY.toMetricResults(this);
   }

   public void setLastSpawnTimeNanos(long lastSpawnTimeNanos) {
      this.lastSpawnTimeNanos = lastSpawnTimeNanos;
   }

   public long getSinceLastSpawnNanos() {
      return System.nanoTime() - this.lastSpawnTimeNanos;
   }

   @Deprecated(forRemoval = true)
   public PlayerRef getPlayerRef() {
      return this.playerRef;
   }

   public int getMountEntityId() {
      return this.mountEntityId;
   }

   public void setMountEntityId(int mountEntityId) {
      this.mountEntityId = mountEntityId;
   }

   public GameMode getGameMode() {
      return this.gameMode;
   }

   public static void setGameMode(@Nonnull Ref<EntityStore> playerRef, @Nonnull GameMode gameMode, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      MovementManager movementManagerComponent = componentAccessor.getComponent(playerRef, MovementManager.getComponentType());

      assert movementManagerComponent != null;

      Player playerComponent = componentAccessor.getComponent(playerRef, getComponentType());

      assert playerComponent != null;

      GameMode oldGameMode = playerComponent.gameMode;
      if (oldGameMode != gameMode) {
         ChangeGameModeEvent event = new ChangeGameModeEvent(gameMode);
         componentAccessor.invoke(playerRef, event);
         if (event.isCancelled()) {
            return;
         }

         setGameModeInternal(playerRef, event.getGameMode(), movementManagerComponent, componentAccessor);
         runOnSwitchToGameMode(playerRef, gameMode);
      }
   }

   public static void initGameMode(@Nonnull Ref<EntityStore> playerRef, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      MovementManager movementManagerComponent = componentAccessor.getComponent(playerRef, MovementManager.getComponentType());

      assert movementManagerComponent != null;

      Player playerComponent = componentAccessor.getComponent(playerRef, getComponentType());

      assert playerComponent != null;

      GameMode gameMode = playerComponent.gameMode;
      if (gameMode == null) {
         World world = componentAccessor.getExternalData().getWorld();
         gameMode = world.getWorldConfig().getGameMode();
         LOGGER.at(Level.INFO).log("Assigning default gamemode %s to player!", gameMode);
      }

      setGameModeInternal(playerRef, gameMode, movementManagerComponent, componentAccessor);
   }

   private static void setGameModeInternal(
      @Nonnull Ref<EntityStore> playerRef,
      @Nonnull GameMode gameMode,
      @Nonnull MovementManager movementManager,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      Player playerComponent = componentAccessor.getComponent(playerRef, getComponentType());

      assert playerComponent != null;

      PlayerRef playerRefComponent = componentAccessor.getComponent(playerRef, PlayerRef.getComponentType());

      assert playerRefComponent != null;

      GameMode oldGameMode = playerComponent.gameMode;
      playerComponent.gameMode = gameMode;
      playerRefComponent.getPacketHandler().writeNoCache(new SetGameMode(gameMode));
      if (movementManager.getDefaultSettings() != null) {
         movementManager.getDefaultSettings().canFly = gameMode == GameMode.Creative;
         movementManager.getSettings().canFly = gameMode == GameMode.Creative;
         movementManager.update(playerRefComponent.getPacketHandler());
      }

      PermissionsModule permissionsModule = PermissionsModule.get();
      if (oldGameMode != null) {
         GameModeType oldGameModeType = GameModeType.fromGameMode(oldGameMode);

         for (String group : oldGameModeType.getPermissionGroups()) {
            permissionsModule.removeUserFromGroup(playerRefComponent.getUuid(), group);
         }
      }

      GameModeType gameModeType = GameModeType.fromGameMode(gameMode);

      for (String group : gameModeType.getPermissionGroups()) {
         permissionsModule.addUserToGroup(playerRefComponent.getUuid(), group);
      }

      if (gameMode == GameMode.Creative) {
         componentAccessor.putComponent(playerRef, Invulnerable.getComponentType(), Invulnerable.INSTANCE);
      } else {
         componentAccessor.tryRemoveComponent(playerRef, Invulnerable.getComponentType());
      }

      if (gameMode == GameMode.Creative) {
         PlayerSettings settings = componentAccessor.getComponent(playerRef, PlayerSettings.getComponentType());
         if (settings == null) {
            settings = PlayerSettings.defaults();
         }

         if (settings.creativeSettings().respondToHit()) {
            componentAccessor.putComponent(playerRef, RespondToHit.getComponentType(), RespondToHit.INSTANCE);
         } else {
            componentAccessor.tryRemoveComponent(playerRef, RespondToHit.getComponentType());
         }
      } else {
         componentAccessor.tryRemoveComponent(playerRef, RespondToHit.getComponentType());
      }

      World world = componentAccessor.getExternalData().getWorld();
      playerComponent.worldMapTracker.sendSettings(world);
   }

   private static void runOnSwitchToGameMode(@Nonnull Ref<EntityStore> ref, @Nonnull GameMode gameMode) {
      Store<EntityStore> store = ref.getStore();
      GameModeType gameModeType = GameModeType.fromGameMode(gameMode);
      InteractionManager interactionManagerComponent = store.getComponent(ref, InteractionModule.get().getInteractionManagerComponent());
      if (interactionManagerComponent != null) {
         String interactions = gameModeType.getInteractionsOnEnter();
         if (interactions != null) {
            InteractionContext context = InteractionContext.forInteraction(interactionManagerComponent, ref, InteractionType.GameModeSwap, store);
            RootInteraction rootInteraction = RootInteraction.getRootInteractionOrUnknown(interactions);
            if (rootInteraction != null) {
               InteractionChain chain = interactionManagerComponent.initChain(InteractionType.EntityStatEffect, context, rootInteraction, true);
               interactionManagerComponent.queueExecuteChain(chain);
            }
         }
      }
   }

   @Nonnull
   public ItemStackTransaction giveItem(@Nonnull ItemStack stack, @Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      PlayerSettings playerSettings = componentAccessor.getComponent(ref, PlayerSettings.getComponentType());
      if (playerSettings == null) {
         playerSettings = PlayerSettings.defaults();
      }

      return this.getInventory().getContainerForItemPickup(stack.getItem(), playerSettings).addItemStack(stack);
   }

   @Override
   public int hashCode() {
      int result = super.hashCode();
      return 31 * result + (this.getUuid() != null ? this.getUuid().hashCode() : 0);
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o == null || this.getClass() != o.getClass()) {
         return false;
      } else if (!super.equals(o)) {
         return false;
      } else {
         Player player = (Player)o;
         return this.getUuid() != null ? this.getUuid().equals(player.getUuid()) : player.getUuid() == null;
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "Player{uuid=" + this.getUuid() + ", clientViewRadius='" + this.clientViewRadius + "', " + super.toString() + "}";
   }

   @Override
   public String getDisplayName() {
      return this.playerRef.getUsername();
   }
}
