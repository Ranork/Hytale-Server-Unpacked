package com.hypixel.hytale.server.npc.corecomponents.entity;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSettings;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.ComponentContext;
import com.hypixel.hytale.server.npc.corecomponents.ISensorEntityCollector;
import com.hypixel.hytale.server.npc.corecomponents.ISensorEntityPrioritiser;
import com.hypixel.hytale.server.npc.corecomponents.SensorWithEntityFilters;
import com.hypixel.hytale.server.npc.corecomponents.entity.builders.BuilderSensorEntityBase;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.support.DebugSupport;
import com.hypixel.hytale.server.npc.sensorinfo.EntityPositionProvider;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.util.IEntityByPriorityFilter;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class SensorEntityBase extends SensorWithEntityFilters {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   protected static final ComponentType<EntityStore, TransformComponent> TRANSFORM_COMPONENT_TYPE = TransformComponent.getComponentType();
   @Nullable
   protected static final ComponentType<EntityStore, NPCEntity> NPC_COMPONENT_TYPE = NPCEntity.getComponentType();
   protected static final ComponentType<EntityStore, Player> PLAYER_COMPONENT_TYPE = Player.getComponentType();
   protected static final ComponentType<EntityStore, DeathComponent> DEATH_COMPONENT_TYPE = DeathComponent.getComponentType();
   protected final double range;
   protected final double minRange;
   protected final boolean useProjectedDistance;
   protected final boolean lockOnTarget;
   protected final boolean autoUnlockTarget;
   protected final boolean onlyLockedTarget;
   protected final int lockedTargetSlot;
   protected final int ignoredTargetSlot;
   protected final ISensorEntityPrioritiser prioritiser;
   protected IEntityByPriorityFilter npcPrioritiser;
   protected IEntityByPriorityFilter playerPrioritiser;
   @Nullable
   protected final ISensorEntityCollector collector;
   protected int ownRole;
   protected final EntityPositionProvider positionProvider = new EntityPositionProvider();
   protected int currentVisSensorColorIndex = -1;
   protected final float visViewAngle;

   public SensorEntityBase(@Nonnull BuilderSensorEntityBase builder, ISensorEntityPrioritiser prioritiser, @Nonnull BuilderSupport builderSupport) {
      super(builder, builder.getFilters(builderSupport, prioritiser, ComponentContext.SensorEntity));
      this.range = builder.getRange(builderSupport);
      this.minRange = builder.getMinRange(builderSupport);
      this.lockOnTarget = builder.isLockOnTarget(builderSupport);
      this.autoUnlockTarget = builder.isAutoUnlockTarget(builderSupport);
      this.onlyLockedTarget = builder.isOnlyLockedTarget(builderSupport);
      this.useProjectedDistance = builder.isUseProjectedDistance(builderSupport);
      this.lockedTargetSlot = builder.getLockedTargetSlot(builderSupport);
      this.ignoredTargetSlot = builder.getIgnoredTargetSlot(builderSupport);
      this.prioritiser = prioritiser;
      this.collector = builder.getCollector(builderSupport);
      this.visViewAngle = this.findViewAngleFromFilters();
   }

   @Override
   public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
      if (!super.matches(ref, role, dt, store)) {
         this.positionProvider.clear();
         this.currentVisSensorColorIndex = -1;
         return false;
      } else {
         TransformComponent transformComponent = store.getComponent(ref, TRANSFORM_COMPONENT_TYPE);

         assert transformComponent != null;

         Vector3d position = transformComponent.getPosition();
         this.ownRole = role.getRoleIndex();
         DebugSupport debugSupport = role.getDebugSupport();
         if (debugSupport.isVisSensorRanges()) {
            this.currentVisSensorColorIndex = debugSupport.recordSensorRange(this.range, this.minRange, this.visViewAngle);
         } else {
            this.currentVisSensorColorIndex = -1;
         }

         if (this.ignoredTargetSlot == Integer.MIN_VALUE || this.ignoredTargetSlot != this.lockedTargetSlot) {
            Ref<EntityStore> targetRef = this.filterLockedEntity(ref, position, role, store);
            if (targetRef != null) {
               this.collector.init(ref, role, store);
               if (!this.collector.terminateOnFirstMatch()) {
                  this.findPlayerOrEntity(ref, position, role, store);
               }

               this.collector.cleanup();
               return this.positionProvider.setTarget(targetRef, store) != null;
            }
         }

         if (this.onlyLockedTarget) {
            this.positionProvider.clear();
            return false;
         } else {
            this.collector.init(ref, role, store);
            Ref<EntityStore> targetRef = this.findPlayerOrEntity(ref, position, role, store);
            this.collector.cleanup();
            if (targetRef == null) {
               this.positionProvider.clear();
               return false;
            } else {
               this.positionProvider.setTarget(targetRef, store);
               if (this.lockOnTarget) {
                  role.getMarkedEntitySupport().setMarkedEntity(this.lockedTargetSlot, targetRef);
               }

               return true;
            }
         }
      }
   }

   @Override
   public void done() {
      this.positionProvider.clear();
   }

   @Override
   public InfoProvider getSensorInfo() {
      return this.positionProvider;
   }

   @Override
   public void registerWithSupport(@Nonnull Role role) {
      super.registerWithSupport(role);
      if (this.isGetPlayers()) {
         role.getPositionCache().requirePlayerDistanceSorted(this.range);
      }

      if (this.isGetNPCs()) {
         role.getPositionCache().requireEntityDistanceSorted(this.range);
      }

      this.prioritiser.registerWithSupport(role);
      this.collector.registerWithSupport(role);
   }

   @Override
   public void motionControllerChanged(
      @Nullable Ref<EntityStore> ref,
      @Nonnull NPCEntity npcComponent,
      MotionController motionController,
      @Nullable ComponentAccessor<EntityStore> componentAccessor
   ) {
      super.motionControllerChanged(ref, npcComponent, motionController, componentAccessor);
      this.prioritiser.motionControllerChanged(ref, npcComponent, motionController, componentAccessor);
      this.collector.motionControllerChanged(ref, npcComponent, motionController, componentAccessor);
   }

   @Override
   public void loaded(Role role) {
      super.loaded(role);
      this.prioritiser.loaded(role);
      this.collector.loaded(role);
   }

   @Override
   public void spawned(Role role) {
      super.spawned(role);
      this.prioritiser.spawned(role);
      this.collector.spawned(role);
   }

   @Override
   public void unloaded(Role role) {
      super.unloaded(role);
      this.prioritiser.unloaded(role);
      this.collector.unloaded(role);
   }

   @Override
   public void removed(Role role) {
      super.removed(role);
      this.prioritiser.removed(role);
      this.collector.removed(role);
   }

   @Override
   public void teleported(Role role, World from, World to) {
      super.teleported(role, from, to);
      this.prioritiser.teleported(role, from, to);
      this.collector.teleported(role, from, to);
   }

   protected void initialisePrioritiser() {
      this.npcPrioritiser = this.isGetNPCs() ? this.prioritiser.getNPCPrioritiser() : null;
      this.playerPrioritiser = this.isGetPlayers() ? this.prioritiser.getPlayerPrioritiser() : null;
   }

   protected abstract boolean isGetPlayers();

   protected abstract boolean isGetNPCs();

   protected boolean isExcludingOwnType() {
      return false;
   }

   @Nullable
   protected Ref<EntityStore> filterLockedEntity(
      @Nonnull Ref<EntityStore> ref, @Nonnull Vector3d position, @Nonnull Role role, @Nonnull Store<EntityStore> store
   ) {
      Ref<EntityStore> target = this.lockedTargetSlot >= 0 ? role.getMarkedEntitySupport().getMarkedEntityRef(this.lockedTargetSlot) : null;
      if (target == null) {
         return null;
      } else if (this.filterEntityWithRange(ref, target, position, role, store)) {
         return target;
      } else {
         if (this.autoUnlockTarget) {
            role.getMarkedEntitySupport().clearMarkedEntity(this.lockedTargetSlot);
         }

         return null;
      }
   }

   protected boolean filterEntityWithRange(
      @Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull Vector3d position, @Nonnull Role role, @Nonnull Store<EntityStore> store
   ) {
      Player playerComponent = store.getComponent(targetRef, Player.getComponentType());
      if (playerComponent != null) {
         if (!this.isGetPlayers()) {
            return false;
         }

         GameMode gameMode = playerComponent.getGameMode();
         if (gameMode == GameMode.Creative) {
            PlayerSettings playerSettingsComponent = store.getComponent(targetRef, PlayerSettings.getComponentType());
            boolean allowDetection = playerSettingsComponent != null && playerSettingsComponent.creativeSettings().allowNPCDetection();
            if (!allowDetection) {
               return false;
            }
         }
      } else {
         if (!store.getArchetype(targetRef).contains(NPC_COMPONENT_TYPE)) {
            return false;
         }

         if (!this.isGetNPCs()) {
            return false;
         }
      }

      TransformComponent targetTransformComponent = store.getComponent(targetRef, TRANSFORM_COMPONENT_TYPE);

      assert targetTransformComponent != null;

      Vector3d pos = targetTransformComponent.getPosition();
      double squaredDistance = role.getActiveMotionController().getSquaredDistance(position, pos, this.useProjectedDistance);
      return !(squaredDistance < this.minRange * this.minRange) && !(squaredDistance > this.range * this.range)
         ? this.filterEntity(ref, targetRef, role, store)
         : false;
   }

   protected boolean filterEntity(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull Role role, @Nonnull Store<EntityStore> store) {
      if (store.getArchetype(targetRef).contains(DEATH_COMPONENT_TYPE)) {
         return false;
      } else {
         NPCEntity npcComponent = store.getComponent(targetRef, NPC_COMPONENT_TYPE);
         return this.isExcludingOwnType() && npcComponent != null && this.ownRole == npcComponent.getRoleIndex()
            ? false
            : this.matchesFilters(ref, targetRef, role, store);
      }
   }

   protected boolean filterPrioritisedPlayer(
      @Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull Role role, @Nonnull Store<EntityStore> store
   ) {
      return this.filterPrioritisedEntity(ref, targetRef, role, store, this.playerPrioritiser);
   }

   protected boolean filterPrioritisedNPC(
      @Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull Role role, @Nonnull Store<EntityStore> store
   ) {
      return this.filterPrioritisedEntity(ref, targetRef, role, store, this.npcPrioritiser);
   }

   protected boolean filterPrioritisedEntity(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Ref<EntityStore> targetRef,
      @Nonnull Role role,
      @Nonnull Store<EntityStore> store,
      @Nonnull IEntityByPriorityFilter entityPrioritiser
   ) {
      boolean filterMatch = this.filterEntity(ref, targetRef, role, store);
      if (!filterMatch) {
         this.collector.collectNonMatching(targetRef, store);
         this.recordEntityVisData(targetRef, role, false);
         return false;
      } else {
         boolean match = entityPrioritiser.test(ref, targetRef, store);
         if (match) {
            this.collector.collectMatching(ref, targetRef, store);
         } else {
            this.collector.collectNonMatching(targetRef, store);
         }

         this.recordEntityVisData(targetRef, role, match);
         return this.collector.terminateOnFirstMatch() && match;
      }
   }

   private void recordEntityVisData(@Nonnull Ref<EntityStore> targetRef, @Nonnull Role role, boolean matched) {
      if (this.currentVisSensorColorIndex >= 0) {
         role.getDebugSupport().recordEntityCheck(targetRef, this.currentVisSensorColorIndex, matched);
      }
   }

   @Nullable
   protected Ref<EntityStore> findPlayerOrEntity(
      @Nonnull Ref<EntityStore> ref, @Nonnull Vector3d position, @Nonnull Role role, @Nonnull Store<EntityStore> store
   ) {
      Ref<EntityStore> player = null;
      Ref<EntityStore> npc = null;
      Ref<EntityStore> ignoredEntity = this.ignoredTargetSlot >= 0 ? role.getMarkedEntitySupport().getMarkedEntityRef(this.ignoredTargetSlot) : null;
      if (this.isGetPlayers()) {
         this.playerPrioritiser.init(role);
         role.getPositionCache()
            .processPlayersInRange(
               ref,
               this.minRange,
               this.range,
               this.useProjectedDistance,
               ignoredEntity,
               role,
               (sensorEntityBase, targetRef, role1, ref1) -> sensorEntityBase.filterPrioritisedPlayer(ref1, targetRef, role1, ref1.getStore()),
               this,
               ref,
               store
            );
         player = this.playerPrioritiser.getHighestPriorityTarget();
         this.playerPrioritiser.cleanup();
      }

      if (this.isGetNPCs()) {
         this.npcPrioritiser.init(role);
         role.getPositionCache()
            .processNPCsInRange(
               ref,
               this.minRange,
               this.range,
               this.useProjectedDistance,
               ignoredEntity,
               role,
               (sensorEntityBase, targetRef, role1, ref1) -> sensorEntityBase.filterPrioritisedNPC(ref1, targetRef, role1, ref1.getStore()),
               this,
               ref,
               store
            );
         npc = this.npcPrioritiser.getHighestPriorityTarget();
         this.npcPrioritiser.cleanup();
      }

      Ref<EntityStore> target;
      if (npc == null) {
         target = player;
      } else if (player == null) {
         target = npc;
      } else {
         target = this.prioritiser.pickTarget(ref, role, position, player, npc, this.useProjectedDistance, store);
      }

      return target;
   }
}
