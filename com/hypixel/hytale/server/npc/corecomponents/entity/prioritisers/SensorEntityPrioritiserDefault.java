package com.hypixel.hytale.server.npc.corecomponents.entity.prioritisers;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.IEntityFilter;
import com.hypixel.hytale.server.npc.corecomponents.ISensorEntityPrioritiser;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.util.IEntityByPriorityFilter;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SensorEntityPrioritiserDefault implements ISensorEntityPrioritiser {
   private static final ComponentType<EntityStore, TransformComponent> TRANSFORM_COMPONENT_TYPE = TransformComponent.getComponentType();
   private final SensorEntityPrioritiserDefault.DefaultPrioritiser playerPrioritiser = new SensorEntityPrioritiserDefault.DefaultPrioritiser();
   private final SensorEntityPrioritiserDefault.DefaultPrioritiser npcPrioritiser = new SensorEntityPrioritiserDefault.DefaultPrioritiser();

   @Nonnull
   @Override
   public IEntityByPriorityFilter getNPCPrioritiser() {
      return this.npcPrioritiser;
   }

   @Nonnull
   @Override
   public IEntityByPriorityFilter getPlayerPrioritiser() {
      return this.playerPrioritiser;
   }

   @Nonnull
   @Override
   public Ref<EntityStore> pickTarget(
      Ref<EntityStore> ref,
      @Nonnull Role role,
      @Nonnull Vector3d position,
      @Nonnull Ref<EntityStore> playerRef,
      @Nonnull Ref<EntityStore> npcRef,
      boolean useProjectedDistance,
      @Nonnull Store<EntityStore> store
   ) {
      MotionController motionController = role.getActiveMotionController();
      TransformComponent playerTransformComponent = store.getComponent(playerRef, TRANSFORM_COMPONENT_TYPE);

      assert playerTransformComponent != null;

      TransformComponent npcTransformComponent = store.getComponent(npcRef, TRANSFORM_COMPONENT_TYPE);

      assert npcTransformComponent != null;

      return motionController.getSquaredDistance(position, playerTransformComponent.getPosition(), useProjectedDistance)
            <= motionController.getSquaredDistance(position, npcTransformComponent.getPosition(), useProjectedDistance)
         ? playerRef
         : npcRef;
   }

   @Override
   public boolean providesFilters() {
      return false;
   }

   @Override
   public void buildProvidedFilters(List<IEntityFilter> filters) {
   }

   public static class DefaultPrioritiser implements IEntityByPriorityFilter {
      @Nullable
      private Ref<EntityStore> target;

      private DefaultPrioritiser() {
      }

      @Override
      public void init(Role role) {
      }

      @Nullable
      @Override
      public Ref<EntityStore> getHighestPriorityTarget() {
         return this.target;
      }

      @Override
      public void cleanup() {
         this.target = null;
      }

      public boolean test(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
         if (this.target == null) {
            this.target = targetRef;
         }

         return true;
      }
   }
}
