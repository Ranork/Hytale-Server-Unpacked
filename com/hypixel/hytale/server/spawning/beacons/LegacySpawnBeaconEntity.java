package com.hypixel.hytale.server.spawning.beacons;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.random.RandomExtra;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.group.EntityGroup;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.hypixel.hytale.server.core.modules.entity.component.HiddenFromAdventurePlayers;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.flock.FlockMembership;
import com.hypixel.hytale.server.npc.components.SpawnBeaconReference;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.spawning.SpawningContext;
import com.hypixel.hytale.server.spawning.SpawningPlugin;
import com.hypixel.hytale.server.spawning.assets.spawns.config.BeaconNPCSpawn;
import com.hypixel.hytale.server.spawning.controllers.BeaconSpawnController;
import com.hypixel.hytale.server.spawning.suppression.component.SpawnSuppressionComponent;
import com.hypixel.hytale.server.spawning.util.FloodFillPositionSelector;
import com.hypixel.hytale.server.spawning.wrappers.BeaconSpawnWrapper;
import it.unimi.dsi.fastutil.Pair;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LegacySpawnBeaconEntity extends Entity {
   @Nonnull
   public static final BuilderCodec<LegacySpawnBeaconEntity> CODEC = BuilderCodec.builder(
         LegacySpawnBeaconEntity.class, LegacySpawnBeaconEntity::new, Entity.CODEC
      )
      .append(new KeyedCodec<>("SpawnConfiguration", Codec.STRING), (spawnBeacon, s) -> spawnBeacon.spawnConfigId = s, spawnBeacon -> spawnBeacon.spawnConfigId)
      .add()
      .append(
         new KeyedCodec<>("NextSpawnAfter", Codec.INSTANT),
         (spawnBeacon, instant) -> spawnBeacon.nextSpawnAfter = instant,
         spawnBeacon -> spawnBeacon.nextSpawnAfter
      )
      .add()
      .append(
         new KeyedCodec<>("NextSpawnAfterRealtime", Codec.BOOLEAN),
         (spawnBeacon, s) -> spawnBeacon.nextSpawnAfterRealtime = s,
         spawnBeacon -> spawnBeacon.nextSpawnAfterRealtime
      )
      .add()
      .append(
         new KeyedCodec<>("DespawnSelfAfter", Codec.INSTANT),
         (spawnBeacon, instant) -> spawnBeacon.despawnSelfAfter = instant,
         spawnBeacon -> spawnBeacon.despawnSelfAfter
      )
      .add()
      .append(
         new KeyedCodec<>("LastPlayerCount", Codec.INTEGER), (spawnBeacon, i) -> spawnBeacon.lastPlayerCount = i, spawnBeacon -> spawnBeacon.lastPlayerCount
      )
      .add()
      .append(new KeyedCodec<>("ObjectiveUUID", Codec.UUID_BINARY), (entity, uuid) -> entity.objectiveUUID = uuid, entity -> entity.objectiveUUID)
      .add()
      .build();
   private BeaconSpawnController spawnController;
   @Nullable
   protected UUID objectiveUUID;
   private BeaconSpawnWrapper spawnWrapper;
   private String spawnConfigId;
   private Instant nextSpawnAfter;
   private boolean nextSpawnAfterRealtime;
   @Nullable
   private Instant despawnSelfAfter;
   private int spawnAttempts;
   private int lastPlayerCount;

   @Nullable
   public static ComponentType<EntityStore, LegacySpawnBeaconEntity> getComponentType() {
      return EntityModule.get().getComponentType(LegacySpawnBeaconEntity.class);
   }

   public LegacySpawnBeaconEntity(@Nullable World world) {
      super(world);
   }

   private LegacySpawnBeaconEntity() {
   }

   public String getSpawnConfigId() {
      return this.spawnConfigId;
   }

   public BeaconSpawnController getSpawnController() {
      return this.spawnController;
   }

   public void setSpawnController(@Nonnull BeaconSpawnController spawnController) {
      this.spawnController = spawnController;
   }

   public Instant getNextSpawnAfter() {
      return this.nextSpawnAfter;
   }

   public boolean isNextSpawnAfterRealtime() {
      return this.nextSpawnAfterRealtime;
   }

   @Nullable
   public Instant getDespawnSelfAfter() {
      return this.despawnSelfAfter;
   }

   public void setSpawnAttempts(int spawnAttempts) {
      this.spawnAttempts = spawnAttempts;
   }

   public BeaconSpawnWrapper getSpawnWrapper() {
      return this.spawnWrapper;
   }

   public void setSpawnWrapper(BeaconSpawnWrapper spawnWrapper) {
      this.spawnWrapper = spawnWrapper;
   }

   public int getSpawnAttempts() {
      return this.spawnAttempts;
   }

   public int getLastPlayerCount() {
      return this.lastPlayerCount;
   }

   public void setLastPlayerCount(int lastPlayerCount) {
      this.lastPlayerCount = lastPlayerCount;
   }

   private void setSpawnConfiguration(BeaconSpawnWrapper spawn) {
      this.spawnWrapper = spawn;
   }

   private void setSpawnConfigId(String spawnConfigId) {
      this.spawnConfigId = spawnConfigId;
   }

   @Nullable
   public UUID getObjectiveUUID() {
      return this.objectiveUUID;
   }

   public void setObjectiveUUID(@Nullable UUID objectiveUUID) {
      this.objectiveUUID = objectiveUUID;
   }

   @Override
   public boolean isHiddenFromLivingEntity(
      @Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      Player targetPlayerComponent = componentAccessor.getComponent(targetRef, Player.getComponentType());
      return targetPlayerComponent == null || targetPlayerComponent.getGameMode() != GameMode.Creative;
   }

   @Override
   public boolean isCollidable() {
      return false;
   }

   @Override
   public void moveTo(@Nonnull Ref<EntityStore> ref, double locX, double locY, double locZ, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      super.moveTo(ref, locX, locY, locZ, componentAccessor);
      FloodFillPositionSelector floodFillPositionSelectorComponent = componentAccessor.getComponent(ref, FloodFillPositionSelector.getComponentType());

      assert floodFillPositionSelectorComponent != null;

      floodFillPositionSelectorComponent.setCalculatePositionsAfter(SpawnBeaconSystems.POSITION_CALCULATION_DELAY_RANGE[1]);
      floodFillPositionSelectorComponent.forceRebuildCache();
      this.spawnController.clearUnspawnableNPCs();
   }

   public void notifyFailedSpawn() {
      this.spawnAttempts++;
   }

   public void notifySpawn(@Nonnull Player target, @Nonnull Ref<EntityStore> spawnedEntity, @Nonnull Store<EntityStore> store) {
      this.processSpawn(spawnedEntity, target, store);
      FlockMembership flockMembershipComponent = store.getComponent(spawnedEntity, FlockMembership.getComponentType());
      Ref<EntityStore> flockReference = flockMembershipComponent != null ? flockMembershipComponent.getFlockRef() : null;
      if (flockReference != null && flockReference.isValid()) {
         EntityGroup entityGroup = store.getComponent(flockReference, EntityGroup.getComponentType());
         entityGroup.forEachMemberExcludingSelf((member, sender, beacon, player) -> {
            if (store.getArchetype(member).contains(NPCEntity.getComponentType())) {
               beacon.processSpawn(member, player, store);
            }
         }, spawnedEntity, this, target);
      }

      this.spawnController.onJobFinished(store);
   }

   public static void prepareNextSpawnTimer(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      LegacySpawnBeaconEntity legacySpawnBeaconComponent = componentAccessor.getComponent(ref, getComponentType());

      assert legacySpawnBeaconComponent != null;

      BeaconNPCSpawn beaconSpawn = legacySpawnBeaconComponent.spawnWrapper.getSpawn();
      boolean realtime = beaconSpawn.isRespawnRealtime();
      if (realtime) {
         Duration[] spawnAfterRange = beaconSpawn.getSpawnAfterRealTimeRange();
         Duration nextValue = RandomExtra.randomDuration(spawnAfterRange[0], spawnAfterRange[1]);
         legacySpawnBeaconComponent.nextSpawnAfter = Instant.now().plus(nextValue);
         legacySpawnBeaconComponent.nextSpawnAfterRealtime = true;
      } else {
         WorldTimeResource worldTimeResource = componentAccessor.getResource(WorldTimeResource.getResourceType());
         Duration[] spawnAfterRange = beaconSpawn.getSpawnAfterGameTimeRange();
         Duration nextValue = RandomExtra.randomDuration(spawnAfterRange[0], spawnAfterRange[1]);
         legacySpawnBeaconComponent.nextSpawnAfter = worldTimeResource.getGameTime().plus(nextValue);
         legacySpawnBeaconComponent.nextSpawnAfterRealtime = false;
      }

      TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      transformComponent.markChunkDirty(componentAccessor);
   }

   public static void clearDespawnTimer(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      LegacySpawnBeaconEntity legacySpawnBeaconComponent = componentAccessor.getComponent(ref, getComponentType());

      assert legacySpawnBeaconComponent != null;

      if (legacySpawnBeaconComponent.despawnSelfAfter != null) {
         legacySpawnBeaconComponent.despawnSelfAfter = null;
         TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

         assert transformComponent != null;

         transformComponent.markChunkDirty(componentAccessor);
      }
   }

   public static void setToDespawnAfter(@Nonnull Ref<EntityStore> ref, @Nullable Duration duration, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      LegacySpawnBeaconEntity legacySpawnBeaconComponent = componentAccessor.getComponent(ref, getComponentType());

      assert legacySpawnBeaconComponent != null;

      if (duration != null && legacySpawnBeaconComponent.despawnSelfAfter == null) {
         WorldTimeResource worldTimeResource = componentAccessor.getResource(WorldTimeResource.getResourceType());
         legacySpawnBeaconComponent.despawnSelfAfter = worldTimeResource.getGameTime().plus(duration);
         TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

         assert transformComponent != null;

         transformComponent.markChunkDirty(componentAccessor);
      }
   }

   public void markNPCUnspawnable(int roleIndex) {
      this.spawnController.markNPCUnspawnable(roleIndex);
   }

   public boolean prepareSpawnContext(
      @Nonnull Vector3d playerPosition,
      int spawnsThisRound,
      int roleIndex,
      @Nonnull SpawningContext spawningContext,
      @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      FloodFillPositionSelector floodFillPositionSelectorComponent = commandBuffer.getComponent(this.reference, FloodFillPositionSelector.getComponentType());

      assert floodFillPositionSelectorComponent != null;

      if (!floodFillPositionSelectorComponent.hasPositionsForRole(roleIndex)) {
         this.markNPCUnspawnable(roleIndex);
         return false;
      } else {
         return floodFillPositionSelectorComponent.prepareSpawnContext(playerPosition, spawnsThisRound, roleIndex, spawningContext, this.spawnWrapper);
      }
   }

   private void processSpawn(@Nonnull Ref<EntityStore> ref, @Nonnull Player target, @Nonnull Store<EntityStore> store) {
      SpawnBeaconReference spawnBeaconReference = store.ensureAndGetComponent(ref, SpawnBeaconReference.getComponentType());
      spawnBeaconReference.getReference().setEntity(this.reference, store);
      spawnBeaconReference.refreshTimeoutCounter();
      this.spawnController.notifySpawnedEntityExists(ref, store);
      NPCEntity npcComponent = store.getComponent(ref, NPCEntity.getComponentType());

      assert npcComponent != null;

      Role role = npcComponent.getRole();
      BeaconNPCSpawn spawn = this.spawnWrapper.getSpawn();
      role.getMarkedEntitySupport().setMarkedEntity(spawn.getTargetSlot(), target.getReference());
      String spawnState = spawn.getNpcSpawnState();
      if (spawnState != null) {
         role.getStateSupport().setState(ref, spawnState, spawn.getNpcSpawnSubState(), store);
      }
   }

   @Nonnull
   public static Pair<Ref<EntityStore>, LegacySpawnBeaconEntity> create(
      @Nonnull BeaconSpawnWrapper spawnWrapper,
      @Nonnull Vector3d position,
      @Nonnull Vector3f rotation,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      Holder<EntityStore> holder = createHolder(spawnWrapper, position, rotation);
      Ref<EntityStore> ref = componentAccessor.addEntity(holder, AddReason.SPAWN);
      LegacySpawnBeaconEntity legacySpawnBeaconComponent = holder.getComponent(getComponentType());
      return Pair.of(ref, legacySpawnBeaconComponent);
   }

   public static Holder<EntityStore> createHolder(@Nonnull BeaconSpawnWrapper spawnWrapper, @Nonnull Vector3d position, @Nonnull Vector3f rotation) {
      LegacySpawnBeaconEntity entity = new LegacySpawnBeaconEntity();
      entity.setSpawnConfiguration(spawnWrapper);
      BeaconNPCSpawn spawn = spawnWrapper.getSpawn();
      String spawnConfigId = spawn.getId();
      entity.setSpawnConfigId(spawnConfigId);
      String modelName = spawn.getModel();
      ModelAsset modelAsset = null;
      if (modelName != null && !modelName.isEmpty()) {
         modelAsset = ModelAsset.getAssetMap().getAsset(modelName);
      }

      Model model;
      if (modelAsset == null) {
         model = SpawningPlugin.get().getSpawnMarkerModel();
      } else {
         model = Model.createUnitScaleModel(modelAsset);
      }

      Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
      holder.addComponent(getComponentType(), entity);
      holder.addComponent(TransformComponent.getComponentType(), new TransformComponent(position, rotation));
      holder.ensureComponent(UUIDComponent.getComponentType());
      holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
      holder.addComponent(PersistentModel.getComponentType(), new PersistentModel(model.toReference()));
      DisplayNameComponent displayNameComponent = new DisplayNameComponent(Message.raw(spawnConfigId));
      holder.addComponent(DisplayNameComponent.getComponentType(), displayNameComponent);
      holder.addComponent(Nameplate.getComponentType(), new Nameplate(spawnConfigId));
      double[] initialSpawnDelay = spawn.getInitialSpawnDelay();
      if (initialSpawnDelay != null) {
         InitialBeaconDelay delay = holder.ensureAndGetComponent(InitialBeaconDelay.getComponentType());
         delay.setupInitialSpawnDelay(initialSpawnDelay);
      }

      String suppression = spawn.getSpawnSuppression();
      if (suppression != null && !suppression.isEmpty()) {
         holder.addComponent(SpawnSuppressionComponent.getComponentType(), new SpawnSuppressionComponent(suppression));
      }

      holder.ensureComponent(HiddenFromAdventurePlayers.getComponentType());
      return holder;
   }

   @Nonnull
   @Override
   public String toString() {
      return "LegacySpawnBeaconEntity{nextSpawnAfter="
         + this.nextSpawnAfter
         + ", nextSpawnAfterRealtime="
         + this.nextSpawnAfterRealtime
         + ", despawnSelfAfter="
         + this.despawnSelfAfter
         + ", spawnAttempts="
         + this.spawnAttempts
         + ", lastPlayerCount="
         + this.lastPlayerCount
         + "}";
   }
}
