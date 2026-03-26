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
   private final int[] attitudeToPriority;

   public SensorEntityPrioritiserAttitude(@Nonnull BuilderSensorEntityPrioritiserAttitude builder, @Nonnull BuilderSupport support) {
      this.attitudeByPriority = builder.getPrioritisedAttitudes(support);
      this.attitudeToPriority = new int[Attitude.VALUES.length];
      Arrays.fill(this.attitudeToPriority, -1);
      int itr = 0;
      int len = this.attitudeByPriority.length;

      while (itr < len) {
         this.attitudeToPriority[this.attitudeByPriority[itr].ordinal()] = itr++;
      }
   }

   @Override
   public void registerWithSupport(@Nonnull Role role) {
      role.getWorldSupport().requireAttitudeCache();
   }

   @Nonnull
   @Override
   public IEntityByPriorityFilter getNPCPrioritiser() {
      return new SensorEntityPrioritiserAttitude.AttitudePrioritiser(this.attitudeToPriority);
   }

   @Nonnull
   @Override
   public IEntityByPriorityFilter getPlayerPrioritiser() {
      return new SensorEntityPrioritiserAttitude.AttitudePrioritiser(this.attitudeToPriority);
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
      int priority = this.attitudeToPriority[attitude.ordinal()];
      if (priority == -1) {
         throw new IllegalStateException(String.format("Attitude %s was not specified in the priority list but an NPC with that attitude was picked", attitude));
      } else {
         return priority;
      }
   }

   public static class AttitudePrioritiser implements IEntityByPriorityFilter {
      private final int[] attitudeToPriority;
      @Nullable
      private Ref<EntityStore> highestPriorityTarget;
      private int highestPriorityIndex = Integer.MAX_VALUE;
      @Nullable
      private WorldSupport support;

      public AttitudePrioritiser(int[] attitudeToPriority) {
         this.attitudeToPriority = attitudeToPriority;
      }

      @Override
      public void init(@Nonnull Role role) {
         this.support = role.getWorldSupport();
      }

      public boolean test(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
         Attitude attitude = this.support.getAttitude(ref, targetRef, componentAccessor);
         int attitudeIdx = attitude.ordinal();
         int priority = this.attitudeToPriority[attitudeIdx];
         if (priority != -1 && priority < this.highestPriorityIndex) {
            this.highestPriorityIndex = priority;
            this.highestPriorityTarget = targetRef;
         }

         return this.highestPriorityIndex == 0;
      }

      @Nullable
      @Override
      public Ref<EntityStore> getHighestPriorityTarget() {
         return this.highestPriorityTarget;
      }

      @Override
      public void cleanup() {
         this.support = null;
         this.highestPriorityTarget = null;
         this.highestPriorityIndex = Integer.MAX_VALUE;
      }
   }
}
