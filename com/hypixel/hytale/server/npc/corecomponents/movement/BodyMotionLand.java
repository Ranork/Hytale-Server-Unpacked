package com.hypixel.hytale.server.npc.corecomponents.movement;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.movement.builders.BuilderBodyMotionLand;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.movement.Steering;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.movement.controllers.MotionControllerFly;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BodyMotionLand extends BodyMotionFind {
   protected final double goalLenience;
   protected final double goalLenienceSquared;

   public BodyMotionLand(@Nonnull BuilderBodyMotionLand builderMotionLand, @Nonnull BuilderSupport support) {
      super(builderMotionLand, support);
      this.goalLenience = builderMotionLand.getGoalLenience(support);
      this.goalLenienceSquared = this.goalLenience * this.goalLenience;
   }

   @Override
   public boolean computeSteering(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Role role,
      @Nullable InfoProvider infoProvider,
      double dt,
      @Nonnull Steering desiredSteering,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      boolean result = super.computeSteering(ref, role, infoProvider, dt, desiredSteering, componentAccessor);
      TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      if (this.isGoalReached(ref, role.getActiveMotionController(), transformComponent.getPosition(), componentAccessor)) {
         NPCEntity npcComponent = componentAccessor.getComponent(ref, NPCEntity.getComponentType());

         assert npcComponent != null;

         role.setActiveMotionController(ref, npcComponent, "Walk", componentAccessor);
         return false;
      } else {
         return result;
      }
   }

   @Override
   public boolean canComputeMotion(
      @Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nullable InfoProvider positionProvider, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      MotionController activeMotionController = role.getActiveMotionController();
      return activeMotionController.matchesType(MotionControllerFly.class)
         && !activeMotionController.isObstructed()
         && super.canComputeMotion(ref, role, positionProvider, componentAccessor);
   }

   @Override
   protected boolean isGoalReached(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull MotionController motionController,
      @Nonnull Vector3d position,
      @Nonnull Vector3d targetPosition,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      double differenceY = targetPosition.y - position.y;
      if (!(differenceY < this.heightDifferenceMin) && !(differenceY > this.heightDifferenceMax)) {
         double waypointDistanceSquared = motionController.waypointDistanceSquared(position, targetPosition);
         return waypointDistanceSquared > this.effectiveDistanceSquared && waypointDistanceSquared > this.goalLenienceSquared
            ? false
            : !this.reachable || this.canReachTarget(ref, motionController, position, targetPosition, componentAccessor);
      } else {
         return false;
      }
   }
}
