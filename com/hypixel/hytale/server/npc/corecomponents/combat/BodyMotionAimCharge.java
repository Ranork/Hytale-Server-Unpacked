package com.hypixel.hytale.server.npc.corecomponents.combat;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.BodyMotionBase;
import com.hypixel.hytale.server.npc.corecomponents.combat.builders.BuilderBodyMotionAimCharge;
import com.hypixel.hytale.server.npc.movement.Steering;
import com.hypixel.hytale.server.npc.movement.controllers.ProbeMoveData;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.util.AimingData;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BodyMotionAimCharge extends BodyMotionBase {
   protected static final ComponentType<EntityStore, TransformComponent> TRANSFORM_COMPONENT_TYPE = TransformComponent.getComponentType();
   protected final double relativeTurnSpeed;
   protected final AimingData aimingData = new AimingData();
   protected final Vector3d direction = new Vector3d();
   protected final ProbeMoveData probeMoveData = new ProbeMoveData();

   public BodyMotionAimCharge(@Nonnull BuilderBodyMotionAimCharge builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.relativeTurnSpeed = builder.getRelativeTurnSpeed(support);
   }

   @Override
   public void preComputeSteering(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nullable InfoProvider sensorInfo, @Nonnull Store<EntityStore> store) {
      if (sensorInfo != null) {
         sensorInfo.passExtraInfo(this.aimingData);
      }
   }

   @Override
   public boolean computeSteering(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Role role,
      @Nullable InfoProvider sensorInfo,
      double dt,
      @Nonnull Steering desiredSteering,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (sensorInfo != null && sensorInfo.getPositionProvider().providePosition(this.direction)) {
         if (this.aimingData.isHaveAttacked()) {
            if (role.getCombatSupport().isExecutingAttack()) {
               desiredSteering.clear();
               return true;
            }

            this.aimingData.setHaveAttacked(false);
         }

         Vector3d selfPosition = componentAccessor.getComponent(ref, TRANSFORM_COMPONENT_TYPE).getPosition();
         double distanceToTarget = role.getActiveMotionController().waypointDistance(selfPosition, this.direction);
         if (distanceToTarget > this.aimingData.getChargeDistance()) {
            this.aimingData.clearSolution();
            return true;
         } else {
            this.direction.subtract(selfPosition);
            this.direction.setLength(this.aimingData.getChargeDistance());
            double x = this.direction.getX();
            double y = this.direction.getY();
            double z = this.direction.getZ();
            float yaw = PhysicsMath.normalizeTurnAngle(PhysicsMath.headingFromDirection(x, z));
            float pitch = PhysicsMath.pitchFromDirection(x, y, z);
            desiredSteering.setYaw(yaw);
            desiredSteering.setPitch(pitch);
            desiredSteering.setRelativeTurnSpeed(this.relativeTurnSpeed);
            TransformComponent transformComponent = componentAccessor.getComponent(ref, TRANSFORM_COMPONENT_TYPE);
            Vector3f bodyRotation = transformComponent.getRotation();
            this.aimingData.setOrientation(yaw, pitch);
            if (!this.aimingData.isOnTarget(bodyRotation.getYaw(), bodyRotation.getPitch(), this.aimingData.getDesiredHitAngle())) {
               this.aimingData.clearSolution();
               return true;
            } else {
               double distance = role.getActiveMotionController().probeMove(ref, selfPosition, this.direction, this.probeMoveData, componentAccessor);
               if (distance < distanceToTarget - 1.0E-6) {
                  this.aimingData.clearSolution();
                  return true;
               } else {
                  Ref<EntityStore> target = sensorInfo.getPositionProvider().getTarget();
                  this.aimingData.setTarget(target);
                  return true;
               }
            }
         }
      } else {
         desiredSteering.clear();
         return true;
      }
   }
}
