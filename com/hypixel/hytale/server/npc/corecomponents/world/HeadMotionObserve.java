package com.hypixel.hytale.server.npc.corecomponents.world;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.random.RandomExtra;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.Rangef;
import com.hypixel.hytale.server.core.asset.type.model.config.camera.CameraSettings;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.HeadMotionBase;
import com.hypixel.hytale.server.npc.corecomponents.world.builders.BuilderHeadMotionObserve;
import com.hypixel.hytale.server.npc.movement.Steering;
import com.hypixel.hytale.server.npc.movement.steeringforces.SteeringForceRotate;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HeadMotionObserve extends HeadMotionBase {
   protected final float[] angleRange;
   protected final double[] pauseTimeRange;
   protected final boolean pickRandomAngle;
   protected final int viewSegments;
   protected final double relativeTurnSpeed;
   protected double preDelay;
   protected double delay;
   protected int currentViewSegment;
   protected boolean invertedDirection;
   protected float targetBodyOffsetYaw;
   protected final SteeringForceRotate steeringForceRotate = new SteeringForceRotate();

   public HeadMotionObserve(@Nonnull BuilderHeadMotionObserve builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.angleRange = builder.getAngleRange(support);
      this.pauseTimeRange = builder.getPauseTimeRange(support);
      this.pickRandomAngle = builder.isPickRandomAngle(support);
      this.viewSegments = builder.getViewSegments(support);
      this.relativeTurnSpeed = builder.getRelativeTurnSpeed(support);
   }

   @Override
   public void activate(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      this.preDelay = RandomExtra.randomRange(this.pauseTimeRange);
      this.currentViewSegment = 0;
      this.pickNextAngle(ref, componentAccessor);
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
      if (this.tickPreDelay(dt)) {
         return true;
      } else {
         TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

         assert transformComponent != null;

         HeadRotation headRotationComponent = componentAccessor.getComponent(ref, HeadRotation.getComponentType());

         assert headRotationComponent != null;

         Vector3f headRotation = headRotationComponent.getRotation();
         this.steeringForceRotate.setHeading(headRotation.getYaw());
         this.steeringForceRotate.setDesiredHeading(transformComponent.getRotation().getYaw() + this.targetBodyOffsetYaw);
         if (this.steeringForceRotate.compute(desiredSteering)) {
            desiredSteering.setRelativeTurnSpeed(this.relativeTurnSpeed);
            return true;
         } else {
            desiredSteering.setYaw(transformComponent.getRotation().getYaw() + this.targetBodyOffsetYaw);
            if (this.tickDelay(dt)) {
               return true;
            } else {
               this.pickNextAngle(ref, componentAccessor);
               return true;
            }
         }
      }
   }

   protected boolean tickPreDelay(double dt) {
      if (this.preDelay > 0.0) {
         this.preDelay -= dt;
         return true;
      } else {
         return false;
      }
   }

   protected boolean tickDelay(double dt) {
      if (this.delay > 0.0) {
         this.delay -= dt;
         return true;
      } else {
         return false;
      }
   }

   protected void pickNextAngle(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      ModelComponent modelComponent = componentAccessor.getComponent(ref, ModelComponent.getComponentType());

      assert modelComponent != null;

      CameraSettings headRotationRestrictions = modelComponent.getModel().getCamera();
      float limitMin;
      float limitMax;
      if (headRotationRestrictions != null && headRotationRestrictions.getYaw() != null && headRotationRestrictions.getYaw().getAngleRange() != null) {
         Rangef yawRange = headRotationRestrictions.getYaw().getAngleRange();
         limitMin = yawRange.min;
         limitMax = yawRange.max;
      } else {
         limitMin = (float) (-Math.PI / 4);
         limitMax = (float) (Math.PI / 4);
      }

      float min = Math.max(this.angleRange[0], limitMin);
      float max = Math.min(this.angleRange[1], limitMax);
      if (this.pickRandomAngle) {
         this.targetBodyOffsetYaw = RandomExtra.randomRange(min, max);
      } else if (this.viewSegments > 1) {
         float fullSector = MathUtil.wrapAngle(max - min);
         float segment = fullSector / (this.viewSegments - 1);
         int thisSegment = this.currentViewSegment++;
         this.currentViewSegment = this.currentViewSegment % this.viewSegments;
         this.targetBodyOffsetYaw = min + thisSegment * segment;
      } else if (!this.invertedDirection) {
         this.targetBodyOffsetYaw = min;
         this.invertedDirection = true;
      } else {
         this.targetBodyOffsetYaw = max;
         this.invertedDirection = false;
      }

      this.delay = RandomExtra.randomRange(this.pauseTimeRange);
   }
}
