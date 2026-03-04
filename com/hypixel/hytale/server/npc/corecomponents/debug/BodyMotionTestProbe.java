package com.hypixel.hytale.server.npc.corecomponents.debug;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.BodyMotionBase;
import com.hypixel.hytale.server.npc.corecomponents.debug.builders.BuilderBodyMotionTestProbe;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.movement.Steering;
import com.hypixel.hytale.server.npc.movement.controllers.ProbeMoveData;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.RoleDebugFlags;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BodyMotionTestProbe extends BodyMotionBase {
   protected final double adjustX;
   protected final double adjustZ;
   protected final double adjustDistance;
   protected final float snapAngle;
   protected boolean displayText;
   protected final Vector3d direction = new Vector3d();
   protected final ProbeMoveData probeMoveData = new ProbeMoveData();

   public BodyMotionTestProbe(@Nonnull BuilderBodyMotionTestProbe builderBodyMotionTestProbe) {
      super(builderBodyMotionTestProbe);
      this.probeMoveData.setSaveSegments(true);
      this.adjustX = builderBodyMotionTestProbe.getAdjustX();
      this.adjustZ = builderBodyMotionTestProbe.getAdjustZ();
      this.adjustDistance = builderBodyMotionTestProbe.getAdjustDistance();
      this.snapAngle = builderBodyMotionTestProbe.getSnapAngle() * (float) (Math.PI / 180.0);
      this.probeMoveData.setAvoidingBlockDamage(builderBodyMotionTestProbe.isAvoidingBlockDamage());
      this.probeMoveData.setRelaxedMoveConstraints(builderBodyMotionTestProbe.isRelaxedMoveConstraints());
   }

   @Override
   public void activate(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      this.displayText = role.getDebugSupport().isDebugFlagSet(RoleDebugFlags.DisplayCustom);
      if (!(this.adjustX < 0.0) || !(this.adjustZ < 0.0)) {
         TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());
         if (transformComponent != null) {
            Vector3d position = transformComponent.getPosition();
            double x = position.x;
            double z = position.z;
            if (this.adjustX >= 0.0) {
               x = MathUtil.fastFloor(x) + this.adjustX;
            }

            if (this.adjustZ >= 0.0) {
               z = MathUtil.fastFloor(z) + this.adjustZ;
            }

            NPCEntity npcComponent = componentAccessor.getComponent(ref, NPCEntity.getComponentType());

            assert npcComponent != null;

            npcComponent.moveTo(ref, x, position.y, z, componentAccessor);
         }
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
      desiredSteering.clear();
      if (sensorInfo != null && sensorInfo.getPositionProvider().providePosition(this.direction)) {
         TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());
         if (transformComponent == null) {
            return false;
         } else {
            Vector3d position = transformComponent.getPosition();
            this.direction.subtract(position);
            if (!this.displayText) {
               return true;
            } else {
               double length = this.direction.length();
               if (length <= 1.0E-6) {
                  return true;
               } else {
                  if (this.adjustDistance > 0.0) {
                     length = this.adjustDistance;
                     this.direction.setLength(this.adjustDistance);
                  }

                  double x = this.direction.getX();
                  double y = this.direction.getY();
                  double z = this.direction.getZ();
                  float yaw = PhysicsMath.normalizeTurnAngle(PhysicsMath.headingFromDirection(x, z));
                  float pitch = PhysicsMath.pitchFromDirection(x, y, z);
                  if (this.snapAngle > 0.0F && this.snapAngle < (float) Math.PI) {
                     yaw = MathUtil.fastRound(yaw / this.snapAngle) * this.snapAngle;
                     PhysicsMath.vectorFromAngles(yaw, pitch, this.direction).setLength(length);
                  }

                  desiredSteering.setYaw(yaw);
                  desiredSteering.setPitch(pitch);
                  double distance = role.getActiveMotionController().probeMove(ref, position, this.direction, this.probeMoveData, componentAccessor);
                  role.getDebugSupport().setDisplayCustomString(String.format("PR: %.2f DX: %.2f DZ: %.2f", distance, this.direction.x, this.direction.z));
                  return true;
               }
            }
         }
      } else {
         return false;
      }
   }
}
