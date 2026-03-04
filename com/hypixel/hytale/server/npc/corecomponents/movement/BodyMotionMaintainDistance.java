package com.hypixel.hytale.server.npc.corecomponents.movement;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.random.RandomExtra;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.BodyMotionBase;
import com.hypixel.hytale.server.npc.corecomponents.movement.builders.BuilderBodyMotionMaintainDistance;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.movement.Steering;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.movement.controllers.MotionControllerWalk;
import com.hypixel.hytale.server.npc.movement.steeringforces.SteeringForceEvade;
import com.hypixel.hytale.server.npc.movement.steeringforces.SteeringForcePursue;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.IPositionProvider;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.sensorinfo.parameterproviders.DoubleParameterProvider;
import com.hypixel.hytale.server.npc.sensorinfo.parameterproviders.ParameterProvider;
import com.hypixel.hytale.server.npc.util.NPCPhysicsMath;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BodyMotionMaintainDistance extends BodyMotionBase {
   protected static final ComponentType<EntityStore, TransformComponent> TRANSFORM_COMPONENT_TYPE = TransformComponent.getComponentType();
   protected static final float POSITIONING_ANGLE_THRESHOLD = 0.08726646F;
   protected final double[] initialDesiredDistanceRange;
   protected final double moveThreshold;
   @Nonnull
   protected final double[] thresholdDistanceRangeSquared;
   protected final double targetDistanceFactor;
   protected final double relativeForwardsSpeed;
   protected final double relativeBackwardsSpeed;
   protected final double moveTowardsSlowdownThreshold;
   protected final double[] strafingDurationRange;
   protected final double[] strafingFrequencyRange;
   protected final int minRangeProviderSlot;
   protected final int maxRangeProviderSlot;
   protected final int positioningAngleProviderSlot;
   protected final double[] desiredDistanceRange = new double[2];
   protected double targetDistanceSquared;
   protected boolean approaching;
   protected boolean movingAway;
   protected int strafingDirection = 1;
   protected double strafingDelay;
   protected boolean pauseStrafing;
   protected final SteeringForceEvade flee = new SteeringForceEvade();
   protected final SteeringForcePursue seek = new SteeringForcePursue();
   protected final Vector3d targetPosition = new Vector3d();
   protected final Vector3d toTarget = new Vector3d();
   protected DoubleParameterProvider cachedMinRangeProvider;
   protected DoubleParameterProvider cachedMaxRangeProvider;
   protected DoubleParameterProvider cachedPositioningAngleProvider;
   protected boolean initialised;

   public BodyMotionMaintainDistance(@Nonnull BuilderBodyMotionMaintainDistance builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.initialDesiredDistanceRange = builder.getDesiredDistanceRange(support);
      this.desiredDistanceRange[0] = this.initialDesiredDistanceRange[0];
      this.desiredDistanceRange[1] = this.initialDesiredDistanceRange[1];
      this.targetDistanceFactor = builder.getTargetDistanceFactor(support);
      this.moveThreshold = builder.getMoveThreshold(support);
      double min = Math.max(0.0, this.initialDesiredDistanceRange[0] - this.moveThreshold);
      double max = this.initialDesiredDistanceRange[1] + this.moveThreshold;
      this.thresholdDistanceRangeSquared = new double[2];
      this.thresholdDistanceRangeSquared[0] = min * min;
      this.thresholdDistanceRangeSquared[1] = max * max;
      this.relativeForwardsSpeed = builder.getRelativeForwardsSpeed(support);
      this.relativeBackwardsSpeed = builder.getRelativeBackwardsSpeed(support);
      this.moveTowardsSlowdownThreshold = builder.getMoveTowardsSlowdownThreshold(support);
      this.strafingDurationRange = builder.getStrafingDurationRange(support);
      this.strafingFrequencyRange = builder.getStrafingFrequencyRange(support);
      this.minRangeProviderSlot = support.getParameterSlot("MinRange");
      this.maxRangeProviderSlot = support.getParameterSlot("MaxRange");
      this.positioningAngleProviderSlot = support.getParameterSlot("PositioningAngle");
   }

   @Override
   public boolean computeSteering(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Role support,
      @Nullable InfoProvider sensorInfo,
      double dt,
      @Nonnull Steering desiredSteering,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      desiredSteering.clear();
      if (!support.getActiveMotionController().matchesType(MotionControllerWalk.class)) {
         support.setBackingAway(false);
         return false;
      } else {
         if (!this.initialised) {
            if (sensorInfo != null) {
               ParameterProvider parameterProvider = sensorInfo.getParameterProvider(this.minRangeProviderSlot);
               if (parameterProvider instanceof DoubleParameterProvider) {
                  this.cachedMinRangeProvider = (DoubleParameterProvider)parameterProvider;
               }

               parameterProvider = sensorInfo.getParameterProvider(this.maxRangeProviderSlot);
               if (parameterProvider instanceof DoubleParameterProvider) {
                  this.cachedMaxRangeProvider = (DoubleParameterProvider)parameterProvider;
               }

               parameterProvider = sensorInfo.getParameterProvider(this.positioningAngleProviderSlot);
               if (parameterProvider instanceof DoubleParameterProvider) {
                  this.cachedPositioningAngleProvider = (DoubleParameterProvider)parameterProvider;
               }
            }

            this.initialised = true;
         }

         boolean recalculateMinThreshold = false;
         boolean forceNewTargetRange = false;
         if (this.cachedMinRangeProvider != null) {
            double value = this.cachedMinRangeProvider.getDoubleParameter();
            double before = this.desiredDistanceRange[0];
            if (value != -Double.MAX_VALUE) {
               this.desiredDistanceRange[0] = this.cachedMinRangeProvider.getDoubleParameter();
            } else {
               this.desiredDistanceRange[0] = this.initialDesiredDistanceRange[0];
            }

            recalculateMinThreshold = true;
            if (before != this.desiredDistanceRange[0]) {
               forceNewTargetRange = true;
            }
         }

         if (this.cachedMaxRangeProvider != null) {
            double valuex = this.cachedMaxRangeProvider.getDoubleParameter();
            double beforex = this.desiredDistanceRange[1];
            if (valuex != -Double.MAX_VALUE) {
               this.desiredDistanceRange[1] = this.cachedMaxRangeProvider.getDoubleParameter();
            } else {
               this.desiredDistanceRange[1] = this.initialDesiredDistanceRange[1];
            }

            double max = this.desiredDistanceRange[1] + this.moveThreshold;
            this.thresholdDistanceRangeSquared[1] = max * max;
            if (beforex != this.desiredDistanceRange[1]) {
               forceNewTargetRange = true;
            }
         }

         double positioningAngle = Double.MAX_VALUE;
         if (this.cachedPositioningAngleProvider != null) {
            positioningAngle = this.cachedPositioningAngleProvider.getDoubleParameter();
         }

         NPCEntity npcComponent = componentAccessor.getComponent(ref, NPCEntity.getComponentType());

         assert npcComponent != null;

         if (this.desiredDistanceRange[0] > this.desiredDistanceRange[1]) {
            ((HytaleLogger.Api)NPCPlugin.get().getLogger().at(Level.WARNING).atMostEvery(1, TimeUnit.MINUTES))
               .log(
                  "Attempting to set min distance for %s to a value higher than its max distance [min=%d max=%s]",
                  npcComponent.getRoleName(),
                  this.desiredDistanceRange[0],
                  this.desiredDistanceRange[1]
               );
            this.desiredDistanceRange[0] = this.desiredDistanceRange[1];
            recalculateMinThreshold = true;
         }

         if (recalculateMinThreshold) {
            double min = Math.max(0.0, this.desiredDistanceRange[0] - this.moveThreshold);
            this.thresholdDistanceRangeSquared[0] = min * min;
         }

         if (sensorInfo != null && sensorInfo.hasPosition()) {
            TransformComponent transformComponent = componentAccessor.getComponent(ref, TRANSFORM_COMPONENT_TYPE);

            assert transformComponent != null;

            IPositionProvider positionProvider = sensorInfo.getPositionProvider();
            positionProvider.providePosition(this.targetPosition);
            Vector3d selfPosition = transformComponent.getPosition();
            double distanceSquared = selfPosition.distanceSquaredTo(this.targetPosition);
            if (forceNewTargetRange) {
               double targetDistance;
               if (distanceSquared > this.thresholdDistanceRangeSquared[1]) {
                  targetDistance = MathUtil.lerp(this.desiredDistanceRange[0], this.desiredDistanceRange[1], 1.0 - this.targetDistanceFactor);
               } else {
                  targetDistance = MathUtil.lerp(this.desiredDistanceRange[0], this.desiredDistanceRange[1], this.targetDistanceFactor);
               }

               this.targetDistanceSquared = targetDistance * targetDistance;
            }

            if (!(distanceSquared > this.thresholdDistanceRangeSquared[1]) && (!this.approaching || !(distanceSquared > this.targetDistanceSquared))) {
               if (!(distanceSquared < this.thresholdDistanceRangeSquared[0]) && (!this.movingAway || !(distanceSquared < this.targetDistanceSquared))) {
                  if (this.approaching || this.movingAway) {
                     this.approaching = false;
                     this.movingAway = false;
                  }
               } else {
                  if (!this.movingAway) {
                     double targetDistance = MathUtil.lerp(this.desiredDistanceRange[0], this.desiredDistanceRange[1], this.targetDistanceFactor);
                     this.targetDistanceSquared = targetDistance * targetDistance;
                     this.movingAway = true;
                     this.approaching = false;
                     support.setBackingAway(true);
                  }

                  this.flee.setPositions(selfPosition, this.targetPosition);
                  MotionController activeMotionController = support.getActiveMotionController();
                  this.flee.setComponentSelector(activeMotionController.getComponentSelector());
                  this.flee.compute(desiredSteering);
                  desiredSteering.scaleTranslation(this.relativeBackwardsSpeed);
               }
            } else {
               if (!this.approaching) {
                  double targetDistance = MathUtil.lerp(this.desiredDistanceRange[0], this.desiredDistanceRange[1], 1.0 - this.targetDistanceFactor);
                  this.targetDistanceSquared = targetDistance * targetDistance;
                  this.seek.setDistances(targetDistance + this.moveTowardsSlowdownThreshold, targetDistance);
                  this.approaching = true;
                  this.movingAway = false;
                  support.setBackingAway(false);
               }

               this.seek.setPositions(selfPosition, this.targetPosition);
               MotionController activeMotionController = support.getActiveMotionController();
               this.seek.setComponentSelector(activeMotionController.getComponentSelector());
               this.seek.compute(desiredSteering);
               desiredSteering.scaleTranslation(this.relativeForwardsSpeed);
            }

            double x = this.targetPosition.getX() - selfPosition.getX();
            double z = this.targetPosition.getZ() - selfPosition.getZ();
            float targetYaw = PhysicsMath.normalizeTurnAngle(PhysicsMath.headingFromDirection(x, z));
            MotionController motionController = support.getActiveMotionController();
            if (this.strafingDurationRange[1] > 0.0 || positioningAngle != Double.MAX_VALUE) {
               if (positioningAngle == Double.MAX_VALUE) {
                  if (!this.tickStrafingDelay(dt)) {
                     if (this.pauseStrafing) {
                        this.strafingDelay = RandomExtra.randomRange(this.strafingDurationRange);
                        this.strafingDirection = RandomExtra.randomBoolean() ? 1 : -1;
                        this.pauseStrafing = false;
                     } else {
                        this.strafingDelay = RandomExtra.randomRange(this.strafingFrequencyRange);
                        this.pauseStrafing = true;
                     }
                  }
               } else {
                  Ref<EntityStore> targetRef = positionProvider.getTarget();
                  if (targetRef != null) {
                     TransformComponent targetTransformComponent = componentAccessor.getComponent(targetRef, TRANSFORM_COMPONENT_TYPE);

                     assert targetTransformComponent != null;

                     float selfYaw = NPCPhysicsMath.lookatHeading(selfPosition, this.targetPosition, transformComponent.getRotation().getYaw());
                     float difference = PhysicsMath.normalizeTurnAngle(targetTransformComponent.getRotation().getYaw() - selfYaw - (float)positioningAngle);
                     if (Math.abs(difference) > 0.08726646F) {
                        this.strafingDirection = difference > 0.0F ? -1 : 1;
                        this.pauseStrafing = false;
                     } else {
                        this.pauseStrafing = true;
                     }
                  } else {
                     this.pauseStrafing = true;
                  }
               }

               if (!this.pauseStrafing) {
                  float angle;
                  if (!desiredSteering.hasTranslation()) {
                     this.toTarget.add(this.targetPosition).subtract(selfPosition).setY(0.0);
                     this.toTarget.normalize();
                     desiredSteering.setTranslation(this.toTarget);
                     Vector3d translation = desiredSteering.getTranslation();
                     double newX = translation.getZ() * this.strafingDirection;
                     double newZ = translation.getX() * -this.strafingDirection;
                     translation.setX(newX);
                     translation.setZ(newZ);
                     desiredSteering.scaleTranslation(this.relativeForwardsSpeed);
                     angle = this.strafingDirection * (float) (Math.PI / 4);
                  } else {
                     angle = this.strafingDirection * (this.movingAway ? (float) (-Math.PI / 4) : (float) (Math.PI / 4));
                     desiredSteering.getTranslation().rotateY(angle);
                  }

                  support.setBackingAway(true);
                  if (!motionController.isObstructed()) {
                     targetYaw += angle;
                  }
               }
            }

            motionController.requireDepthProbing();
            desiredSteering.setYaw(targetYaw);
            return false;
         } else {
            return false;
         }
      }
   }

   protected boolean tickStrafingDelay(double dt) {
      if (this.strafingDelay > 0.0) {
         this.strafingDelay -= dt;
         return true;
      } else {
         return false;
      }
   }

   @Override
   public void deactivate(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      super.deactivate(ref, role, componentAccessor);
      role.setBackingAway(false);
   }
}
