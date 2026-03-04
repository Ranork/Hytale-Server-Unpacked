package com.hypixel.hytale.server.npc.corecomponents.entity.prioritisers;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.asset.type.attitude.Attitude;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.IEntityFilter;
import com.hypixel.hytale.server.npc.corecomponents.ISensorEntityPrioritiser;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.EntityFilterAttitude;
import com.hypixel.hytale.server.npc.corecomponents.entity.prioritisers.builders.BuilderSensorEntityPrioritiserAttitude;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.support.WorldSupport;
import com.hypixel.hytale.server.npc.util.IEntityByPriorityFilter;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SensorEntityPrioritiserAttitude implements ISensorEntityPrioritiser {
   private static final ComponentType<EntityStore, TransformComponent> TRANSFORM_COMPONENT_TYPE = TransformComponent.getComponentType();
   private final Attitude[] attitudeByPriority;

   public SensorEntityPrioritiserAttitude(@Nonnull BuilderSensorEntityPrioritiserAttitude builder, @Nonnull BuilderSupport support) {
      this.attitudeByPriority = builder.getPrioritisedAttitudes(support);
   }

   @Override
   public void registerWithSupport(@Nonnull Role role) {
      role.getWorldSupport().requireAttitudeCache();
   }

   @Nonnull
   @Override
   public IEntityByPriorityFilter getNPCPrioritiser() {
      return new SensorEntityPrioritiserAttitude.AttitudePrioritiser(this.attitudeByPriority);
   }

   @Nonnull
   @Override
   public IEntityByPriorityFilter getPlayerPrioritiser() {
      return new SensorEntityPrioritiserAttitude.AttitudePrioritiser(this.attitudeByPriority);
   }

   @Nonnull
   @Override
   public Ref<EntityStore> pickTarget(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Role role,
      @Nonnull Vector3d position,
      @Nonnull Ref<EntityStore> playerRef,
      @Nonnull Ref<EntityStore> npcRef,
      boolean useProjectedDistance,
      @Nonnull Store<EntityStore> store
   ) {
      WorldSupport worldSupport = role.getWorldSupport();
      int playerPriority = this.getPriority(ref, worldSupport, playerRef, store);
      int npcPriority = this.getPriority(ref, worldSupport, npcRef, store);
      if (playerPriority != npcPriority) {
         return playerPriority <= npcPriority ? playerRef : npcRef;
      } else {
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
   }

   @Override
   public boolean providesFilters() {
      return true;
   }

   @Override
   public void buildProvidedFilters(@Nonnull List<IEntityFilter> filters) {
      filters.add(new EntityFilterAttitude(this.attitudeByPriority));
   }

   protected int getPriority(
      @Nonnull Ref<EntityStore> ref, @Nonnull WorldSupport support, @Nonnull Ref<EntityStore> targetRef, @Nonnull Store<EntityStore> store
   ) {
      Attitude attitude = support.getAttitude(ref, targetRef, store);

      for (int i = 0; i < this.attitudeByPriority.length; i++) {
         if (attitude == this.attitudeByPriority[i]) {
            return i;
         }
      }

      throw new IllegalStateException(String.format("Attitude %s was not specified in the priority list but an NPC with that attitude was picked", attitude));
   }

   public static class AttitudePrioritiser implements IEntityByPriorityFilter {
      private final Attitude[] attitudeByPriority;
      private final Ref<EntityStore>[] targetsByAttitude = new Ref[Attitude.VALUES.length];
      @Nullable
      private WorldSupport support;

      public AttitudePrioritiser(Attitude[] attitudeByPriority) {
         this.attitudeByPriority = attitudeByPriority;
      }

      @Override
      public void init(@Nonnull Role role) {
         this.support = role.getWorldSupport();
      }

      public boolean test(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
         Attitude attitude = this.support.getAttitude(ref, targetRef, componentAccessor);
         if (this.targetsByAttitude[attitude.ordinal()] == null) {
            this.targetsByAttitude[attitude.ordinal()] = targetRef;
         }

         return attitude == this.attitudeByPriority[0];
      }

      @Nullable
      @Override
      public Ref<EntityStore> getHighestPriorityTarget() {
         for (int i = 0; i < this.attitudeByPriority.length; i++) {
            int attitudeIdx = this.attitudeByPriority[i].ordinal();
            Ref<EntityStore> target = this.targetsByAttitude[attitudeIdx];
            if (target != null) {
               return target;
            }
         }

         return null;
      }

      @Override
      public void cleanup() {
         this.support = null;
         Arrays.fill(this.targetsByAttitude, null);
      }
   }
}
