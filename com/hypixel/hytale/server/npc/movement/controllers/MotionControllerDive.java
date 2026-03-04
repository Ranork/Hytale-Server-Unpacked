package com.hypixel.hytale.server.npc.movement.controllers;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.modules.collision.BlockCollisionData;
import com.hypixel.hytale.server.core.modules.collision.CollisionModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.movement.MotionKind;
import com.hypixel.hytale.server.npc.movement.Steering;
import com.hypixel.hytale.server.npc.movement.controllers.builders.BuilderMotionControllerDive;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.util.NPCPhysicsMath;
import com.hypixel.hytale.server.npc.util.PositionProbeWater;
import java.util.EnumSet;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MotionControllerDive extends MotionControllerBase {
   public static final String TYPE = "Dive";
   public static final int COLLISION_MATERIALS_ACTIVE = 5;
   public static final int COLLISION_MATERIALS_PASSIVE = 4;
   public static final double DEFAULT_SWIM_DEPTH = 0.5;
   protected static double DAMPING_FACTOR = 20.0;
   protected final double maxVerticalSpeed;
   protected final double acceleration;
   protected final double maxFallSpeed;
   protected final double maxSinkSpeed;
   protected final double maxRotationSpeed;
   protected final float maxMoveTurnAngle;
   protected final double minDiveDepth;
   protected final double maxDiveDepth;
   protected final double minWaterDepth;
   protected final double maxWaterDepth;
   protected final double minDepthAboveGround;
   protected final double minDepthBelowSurface;
   protected final double relativeSwimDepth;
   protected final double sinkRatio;
   protected final double fastDiveThreshold;
   protected final double minSpeedAfterForceSquared;
   protected final double desiredDepthWeight;
   protected double swimDepth;
   protected double climbSpeed;
   protected double currentRelativeSpeed;
   protected boolean collisionWithSolid;
   protected final MotionController.VerticalRange verticalRange = new MotionController.VerticalRange();
   protected final PositionProbeWater moveProbe = new PositionProbeWater();
   protected final PositionProbeWater probeMoveProbe = new PositionProbeWater();
   protected final Vector3d tempPosition = new Vector3d();
   protected final Vector3d tempDirection = new Vector3d();
   private static final EnumSet<MotionKind> VALID_MOTIONS = EnumSet.of(MotionKind.SWIMMING, MotionKind.SWIMMING_TURNING, MotionKind.MOVING);

   public MotionControllerDive(@Nonnull BuilderSupport builderSupport, @Nonnull BuilderMotionControllerDive builder) {
      super(builderSupport, builder);
      this.setGravity(builder.getGravity());
      this.componentSelector.assign(1.0, 1.0, 1.0);
      this.maxVerticalSpeed = builder.getMaxVerticalSpeed();
      this.acceleration = builder.getAcceleration();
      this.maxSinkSpeed = builder.getMaxSinkSpeed();
      this.maxFallSpeed = builder.getMaxFallSpeed();
      this.maxRotationSpeed = builder.getMaxRotationSpeed();
      this.maxMoveTurnAngle = builder.getMaxMoveTurnAngle();
      this.minDiveDepth = builder.getMinDiveDepth();
      this.maxDiveDepth = builder.getMaxDiveDepth();
      this.minWaterDepth = builder.getMinWaterDepth();
      this.maxWaterDepth = builder.getMaxWaterDepth();
      this.minDepthAboveGround = builder.getMinDepthAboveGround();
      this.minDepthBelowSurface = builder.getMinDepthBelowSurface();
      this.relativeSwimDepth = builder.getSwimDepth();
      this.sinkRatio = builder.getSinkRatio();
      this.fastDiveThreshold = builder.getFastDiveThreshold();
      this.desiredDepthWeight = builder.getDesiredDepthWeight();
      double minSpeedAfterForceSquared = MathUtil.minValue(this.maxVerticalSpeed, this.maxSinkSpeed, this.maxFallSpeed);
      this.minSpeedAfterForceSquared = minSpeedAfterForceSquared * minSpeedAfterForceSquared;
   }

   @Override
   public void activate() {
      super.activate();
      this.collisionResult.disableSlides();
   }

   @Override
   public double getWanderVerticalMovementRatio() {
      return this.sinkRatio;
   }

   @Nonnull
   @Override
   public MotionController.VerticalRange getDesiredVerticalRange(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      double waterLevel = this.moveProbe.getWaterLevel() + 1.0;
      double maxY = waterLevel - this.swimDepth - this.minDepthBelowSurface;
      double groundY = this.moveProbe.getGroundLevel() + this.minDepthAboveGround;
      double lowY = waterLevel - this.maxDiveDepth;
      double minY = Math.max(groundY, lowY);
      TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      double y = transformComponent.getPosition().y;
      if (this.onGround()) {
         minY = y;
      }

      if (this.moveProbe.isTouchCeil()) {
         maxY = y;
      }

      if (minY > maxY) {
         minY = y;
         maxY = y;
      }

      this.verticalRange.assign(y, minY, maxY);
      return this.verticalRange;
   }

   @Override
   protected double computeMove(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Role role,
      @Nonnull Steering steering,
      double dt,
      @Nonnull Vector3d translation,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      this.saveMotionKind();
      this.moveProbe.probePosition(ref, this.collisionBoundingBox, this.position, this.collisionResult, this.swimDepth, componentAccessor);
      this.setMotionKind(!this.moveProbe.isInWater() && this.moveProbe.isOnGround() ? MotionKind.MOVING : MotionKind.SWIMMING);
      this.currentRelativeSpeed = steering.getSpeed();
      Vector3d dir = steering.getTranslation();
      float heading = this.getYaw();
      float pitch = this.getPitch();
      if (this.collisionWithSolid) {
         this.moveSpeed = 0.0;
         this.climbSpeed = 0.0;
         this.forceVelocity.assign(Vector3d.ZERO);
         this.appliedVelocities.clear();
      }

      if (this.canAct(ref, componentAccessor)) {
         this.tempDirection.assign(dir.x, 0.0, dir.z);
         double maxVerticalSpeed = this.maxVerticalSpeed * this.effectHorizontalSpeedMultiplier;
         double hSpeed = this.tempDirection.length() * this.getMaximumSpeed();
         double vSpeed = dir.y * maxVerticalSpeed;
         this.moveSpeed = NPCPhysicsMath.accelerateToTargetSpeed(this.moveSpeed, hSpeed, dt, this.acceleration, this.getMaximumSpeed());
         this.climbSpeed = NPCPhysicsMath.accelerateToTargetSpeed(
            this.climbSpeed, vSpeed, dt, this.acceleration, this.acceleration, -maxVerticalSpeed, maxVerticalSpeed
         );
         float maxRotation = (float)(dt * this.getCurrentMaxBodyRotationSpeed());
         boolean isMoving = this.moveSpeed * this.moveSpeed + this.climbSpeed * this.climbSpeed > 1.0E-12 && steering.hasTranslation();
         float newHeading;
         float newPitch;
         if (this.moveSpeed * this.moveSpeed > 1.0000000000000002E-10 && steering.hasTranslation()) {
            newHeading = PhysicsMath.normalizeTurnAngle(PhysicsMath.headingFromDirection(dir.x, dir.z));
            newPitch = PhysicsMath.normalizeTurnAngle(PhysicsMath.pitchFromDirection(dir.x, dir.y, dir.z));
         } else {
            translation.assign(Vector3d.ZERO);
            if (steering.hasYaw()) {
               newHeading = steering.getYaw();
            } else {
               newHeading = heading;
            }

            steering.clearYaw();
            if (steering.hasPitch()) {
               newPitch = steering.getPitch();
            } else {
               newPitch = pitch;
            }

            steering.clearPitch();
         }

         float turnAngle = NPCPhysicsMath.turnAngle(heading, newHeading);
         float inclinationAngle = NPCPhysicsMath.turnAngle(pitch, newPitch);
         if (Math.abs(turnAngle) > this.maxMoveTurnAngle) {
            this.moveSpeed = 0.0;
         }

         if (this.isNearZero(turnAngle)) {
            heading = newHeading;
            turnAngle = 0.0F;
         } else {
            turnAngle = MathUtil.clamp(turnAngle, -maxRotation, maxRotation);
            heading += turnAngle;
            if (!isMoving) {
               this.setMotionKind(MotionKind.SWIMMING_TURNING);
            }
         }

         heading = PhysicsMath.normalizeTurnAngle(heading);
         if (this.isNearZero(inclinationAngle)) {
            pitch = newPitch;
         } else {
            inclinationAngle = MathUtil.clamp(inclinationAngle, -maxRotation, maxRotation);
            pitch += inclinationAngle;
         }

         pitch = PhysicsMath.normalizeTurnAngle(pitch);
         if (!steering.hasYaw()) {
            steering.setYaw(heading);
         }

         if (!steering.hasPitch()) {
            steering.setPitch(pitch);
         }

         if (this.debugModeSteer) {
            LOGGER.at(Level.INFO)
               .log(
                  "=== Compute = t =%.4f v =%.4f h =%.4f nh=%.4f dh=%.4f",
                  dt,
                  this.moveSpeed,
                  (180.0F / (float)Math.PI) * heading,
                  (180.0F / (float)Math.PI) * newHeading,
                  (180.0F / (float)Math.PI) * turnAngle
               );
         }

         this.computeTranslation(translation, dt, heading, this.moveSpeed, this.climbSpeed);
         return dt;
      } else {
         double maxSpeed = this.moveProbe.isInWater() ? this.maxSinkSpeed : this.maxFallSpeed;
         boolean onGround = this.onGround();
         if (!this.isAlive(ref, componentAccessor)) {
            steering.setYaw(this.getYaw());
            steering.setPitch(onGround ? 0.0F : this.getPitch());
            this.forceVelocity.assign(Vector3d.ZERO);
            this.appliedVelocities.clear();
            this.moveSpeed = 0.0;
            this.climbSpeed = 0.0;
            if (onGround) {
               translation.assign(Vector3d.ZERO);
            } else {
               Velocity velocityComponent = componentAccessor.getComponent(ref, Velocity.getComponentType());
               double sinkSpeed = velocityComponent.getVelocity().y;
               sinkSpeed = NPCPhysicsMath.gravityDrag(sinkSpeed, 5.0 * this.gravity, dt, maxSpeed);
               translation.assign(0.0, sinkSpeed, 0.0).scale(dt);
            }

            return dt;
         } else if (this.forceVelocity.equals(Vector3d.ZERO) && this.appliedVelocities.isEmpty()) {
            if (!steering.hasYaw()) {
               steering.setYaw(heading);
            }

            if (!steering.hasPitch()) {
               steering.setPitch(this.onGround() ? 0.0F : pitch);
            }

            this.climbSpeed = NPCPhysicsMath.gravityDrag(this.climbSpeed, 5.0 * this.gravity, dt, maxSpeed);
            this.computeTranslation(translation, dt, heading, this.moveSpeed, this.climbSpeed);
            return dt;
         } else {
            this.moveSpeed = 0.0;
            this.climbSpeed = 0.0;
            if (!this.isObstructed()) {
               translation.assign(this.forceVelocity);

               for (int i = 0; i < this.appliedVelocities.size(); i++) {
                  MotionControllerBase.AppliedVelocity entry = this.appliedVelocities.get(i);
                  if (entry.velocity.y + this.forceVelocity.y <= 0.0 || entry.velocity.y < 0.0) {
                     entry.canClear = true;
                  }

                  if (onGround && entry.canClear) {
                     entry.velocity.y = 0.0;
                  }

                  translation.add(entry.velocity);
               }
            } else {
               translation.assign(Vector3d.ZERO);
               this.appliedVelocities.clear();
               this.forceVelocity.assign(Vector3d.ZERO);
            }

            translation.y = NPCPhysicsMath.gravityDrag(this.forceVelocity.y, 5.0 * this.gravity, dt, maxSpeed);
            translation.scale(dt);
            steering.setYaw(this.getYaw());
            steering.setPitch(this.getPitch());
            return dt;
         }
      }
   }

   @Override
   protected boolean shouldDampenAppliedVelocitiesY() {
      return true;
   }

   @Override
   protected boolean shouldAlwaysUseGroundResistance() {
      return true;
   }

   private void computeTranslation(@Nonnull Vector3d translation, double dt, float heading, double moveSpeed, double climbSpeed) {
      translation.x = moveSpeed * dt * PhysicsMath.headingX(heading);
      translation.z = moveSpeed * dt * PhysicsMath.headingZ(heading);
      translation.y = climbSpeed * dt;
      translation.clipToZero(this.getEpsilonSpeed());
   }

   private boolean isNearZero(float angle) {
      float epsilonAngle = this.getEpsilonAngle();
      return angle >= -epsilonAngle && angle <= epsilonAngle;
   }

   @Override
   public void setMotionKind(MotionKind motionKind) {
      if (!VALID_MOTIONS.contains(motionKind)) {
         motionKind = MotionKind.SWIMMING;
      }

      super.setMotionKind(motionKind);
   }

   @Override
   protected double executeMove(
      @Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Vector3d translation, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (this.debugModeValidatePositions && !this.isValidPosition(this.position, this.collisionResult, componentAccessor)) {
         throw new IllegalStateException("Invalid position");
      } else {
         boolean canAct = this.canAct(ref, componentAccessor);
         this.collisionResult.setCollisionByMaterial(canAct ? 5 : 4);
         if (this.debugModeBlockCollisions) {
            this.collisionResult.setLogger(LOGGER);
         }

         CollisionModule.get();
         CollisionModule.findCollisions(this.collisionBoundingBox, this.position, translation, this.collisionResult, componentAccessor);
         if (this.debugModeBlockCollisions) {
            this.collisionResult.setLogger(null);
         }

         if (this.debugModeCollisions) {
            this.dumpCollisionResults();
         }

         this.lastValidPosition.assign(this.position);
         this.isObstructed = false;
         this.collisionWithSolid = false;
         BlockCollisionData collision = this.collisionResult.getFirstBlockCollision();
         if (collision == null) {
            double time = dt;
            this.position.add(translation);
            if (!this.moveProbe.probePosition(ref, this.collisionBoundingBox, this.position, this.collisionResult, this.swimDepth, componentAccessor)
               || canAct && !this.moveProbe.isInWater()) {
               time = this.bisect(ref, this.lastValidPosition, 0.0, this.position, dt, this.position, componentAccessor);
               this.isObstructed = true;
               if (this.debugModeMove) {
                  LOGGER.at(Level.INFO)
                     .log("Move - Dive: No Collision, Bisect pos=%s, blocked=%s", Vector3d.formatShortString(this.position), this.isObstructed);
               }
            } else if (this.debugModeMove) {
               LOGGER.at(Level.INFO).log("Move - Dive: No collision, pos=%s, blocked=%s", Vector3d.formatShortString(this.position), this.isObstructed);
            }

            if (this.debugModeValidatePositions && !this.isValidPosition(this.position, this.collisionResult, componentAccessor)) {
               throw new IllegalStateException("Invalid position");
            } else {
               this.processTriggers(ref, this.collisionResult, time / dt, componentAccessor);
               return time;
            }
         } else if (this.debugModeValidatePositions && !this.isValidPosition(collision.collisionPoint, this.collisionResult, componentAccessor)) {
            throw new IllegalStateException("Invalid position");
         } else {
            double collisionStart = collision.collisionStart;
            this.position.assign(collision.collisionPoint);
            this.isObstructed = true;
            this.collisionWithSolid = collision.blockMaterial == BlockMaterial.Solid;
            if (!this.moveProbe.probePosition(ref, this.collisionBoundingBox, this.position, this.collisionResult, this.swimDepth, componentAccessor)
               || canAct && !this.moveProbe.isInWater()) {
               collisionStart = this.bisect(ref, this.lastValidPosition, 0.0, this.position, collisionStart, this.position, componentAccessor);
               if (this.debugModeMove) {
                  LOGGER.at(Level.INFO)
                     .log(
                        "Move - Dive: Collision with solid=%s, Bisect pos=%s, blocked=%s",
                        this.collisionWithSolid,
                        Vector3d.formatShortString(this.position),
                        this.isObstructed
                     );
               }
            } else if (this.debugModeMove) {
               LOGGER.at(Level.INFO)
                  .log(
                     "Move - Dive: Collision with solid=%s, pos=%s, blocked=%s",
                     this.collisionWithSolid,
                     Vector3d.formatShortString(this.position),
                     this.isObstructed
                  );
            }

            if (this.debugModeValidatePositions && !this.isValidPosition(this.position, this.collisionResult, componentAccessor)) {
               throw new IllegalStateException("Invalid position");
            } else {
               this.processTriggers(ref, this.collisionResult, collisionStart, componentAccessor);
               return dt * collisionStart;
            }
         }
      }
   }

   @Override
   public void constrainRotations(Role role, @Nonnull TransformComponent transform) {
      transform.getRotation().setRoll(0.0F);
   }

   @Override
   public double getCurrentMaxBodyRotationSpeed() {
      return this.maxRotationSpeed * this.effectHorizontalSpeedMultiplier;
   }

   @Override
   public boolean canAct(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      return super.canAct(ref, componentAccessor) && this.moveProbe.isInWater();
   }

   @Override
   public boolean inAir() {
      return !this.moveProbe.isOnGround();
   }

   @Override
   public boolean inWater() {
      return this.moveProbe.isInWater();
   }

   @Override
   public boolean onGround() {
      return this.moveProbe.isOnGround();
   }

   @Nonnull
   @Override
   public String getType() {
      return "Dive";
   }

   public double bisect(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Vector3d validPosition,
      double validDistance,
      @Nonnull Vector3d invalidPosition,
      double invalidDistance,
      @Nonnull Vector3d result,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      return NPCPhysicsMath.lerp(validDistance, invalidDistance, this.bisect(ref, validPosition, invalidPosition, result, componentAccessor));
   }

   public double bisect(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Vector3d validPosition,
      @Nonnull Vector3d invalidPosition,
      @Nonnull Vector3d result,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      return this.bisect(
         validPosition,
         invalidPosition,
         this,
         (_this, position) -> _this.probeMoveProbe
            .probePosition(ref, _this.collisionBoundingBox, position, _this.collisionResult, _this.swimDepth, componentAccessor),
         result
      );
   }

   @Override
   public double probeMove(@Nonnull Ref<EntityStore> ref, @Nonnull ProbeMoveData probeMoveData, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      boolean saveSegments = probeMoveData.startProbing();
      this.collisionResult.setCollisionByMaterial(5);
      Vector3d probePosition = probeMoveData.probePosition;
      Vector3d probeMovement = probeMoveData.probeDirection;
      CollisionModule collisionModule = CollisionModule.get();
      if (saveSegments) {
         probeMoveData.addStartSegment(probePosition, false);
      }

      if (!this.probeMoveProbe.probePosition(ref, this.collisionBoundingBox, probePosition, this.collisionResult, this.swimDepth, componentAccessor)) {
         if (saveSegments) {
            probeMoveData.addEndSegment(probePosition, false, 0.0);
         }

         return 0.0;
      } else {
         double maxDistance = probeMovement.length();
         CollisionModule.findCollisions(this.collisionBoundingBox, probePosition, probeMovement, this.collisionResult, componentAccessor);
         if (this.debugModeMove) {
            LOGGER.at(Level.INFO)
               .log("Probe Step: pos=%s mov=%s left=%s", Vector3d.formatShortString(probePosition), Vector3d.formatShortString(probeMovement), maxDistance);
         }

         if (this.debugModeCollisions) {
            this.dumpCollisionResults();
         }

         BlockCollisionData collision = this.collisionResult.getFirstBlockCollision();
         this.tempPosition.assign(probePosition);
         if (collision == null) {
            probePosition.add(probeMovement);
            probeMovement.assign(Vector3d.ZERO);
            double distanceTravelled;
            if (this.probeMoveProbe.probePosition(ref, this.collisionBoundingBox, probePosition, this.collisionResult, this.swimDepth, componentAccessor)
               && this.probeMoveProbe.isInWater()) {
               distanceTravelled = maxDistance;
               if (this.debugModeMove) {
                  LOGGER.at(Level.INFO)
                     .log("Probe - Dive: No Collision, valid pos=%s, distanceLeft=%s", Vector3d.formatShortString(probePosition), maxDistance - maxDistance);
               }
            } else {
               distanceTravelled = this.bisect(ref, this.tempPosition, 0.0, probePosition, maxDistance, probePosition, componentAccessor);
               if (this.debugModeMove) {
                  LOGGER.at(Level.INFO)
                     .log(
                        "Probe - Dive: No Collision, Bisect pos=%s, distanceLeft=%s",
                        Vector3d.formatShortString(probePosition),
                        maxDistance - distanceTravelled
                     );
               }
            }

            if (this.debugModeValidatePositions && !this.isValidPosition(this.tempPosition, this.collisionResult, componentAccessor)) {
               throw new IllegalStateException("Invalid position");
            } else {
               if (saveSegments) {
                  probeMoveData.addMoveSegment(probePosition, false, distanceTravelled);
                  probeMoveData.addEndSegment(probePosition, false, distanceTravelled);
               }

               if (this.debugModeMove) {
                  LOGGER.at(Level.INFO).log("Probe Move done: No collision - maxDistance=%s distanceLeft=%s", maxDistance, maxDistance - distanceTravelled);
               }

               return distanceTravelled;
            }
         } else {
            double collisionStart = collision.collisionStart;
            double distanceTravelledx = maxDistance * collisionStart;
            probePosition.assign(collision.collisionPoint);
            if (!this.probeMoveProbe.probePosition(ref, this.collisionBoundingBox, probePosition, this.collisionResult, this.swimDepth, componentAccessor)
               || !this.probeMoveProbe.isInWater()) {
               distanceTravelledx = this.bisect(ref, this.tempPosition, 0.0, probePosition, distanceTravelledx, probePosition, componentAccessor);
               if (this.debugModeMove) {
                  LOGGER.at(Level.INFO)
                     .log(
                        "Probe - Dive: Collision, Bisect pos=%s, distanceLeft=%s, collision start=%s",
                        Vector3d.formatShortString(probePosition),
                        maxDistance - distanceTravelledx,
                        collisionStart
                     );
               }
            } else if (this.debugModeMove) {
               LOGGER.at(Level.INFO)
                  .log(
                     "Probe - Dive: Collision, valid pos=%s, distanceLeft=%s, collision start=%s",
                     Vector3d.formatShortString(probePosition),
                     maxDistance - distanceTravelledx,
                     collisionStart
                  );
            }

            if (this.debugModeValidatePositions && !this.isValidPosition(probePosition, this.collisionResult, componentAccessor)) {
               throw new IllegalStateException("Invalid position");
            } else {
               if (saveSegments) {
                  if (this.getWorldNormal().equals(collision.collisionNormal)) {
                     probeMoveData.addHitGroundSegment(probePosition, distanceTravelledx, collision.collisionNormal, collision.blockId);
                  } else {
                     probeMoveData.addHitWallSegment(probePosition, false, distanceTravelledx, collision.collisionNormal, collision.blockId);
                  }
               }

               if (saveSegments) {
                  probeMoveData.addEndSegment(probePosition, false, distanceTravelledx);
               }

               if (this.debugModeMove) {
                  LOGGER.at(Level.INFO).log("Probe Move done: maxDistance=%s distanceLeft=%s", maxDistance, maxDistance - distanceTravelledx);
               }

               return distanceTravelledx;
            }
         }
      }
   }

   @Override
   public void spawned() {
   }

   @Override
   public double getCurrentSpeed() {
      return this.moveSpeed;
   }

   @Override
   public double getCurrentTurnRadius() {
      return 0.0;
   }

   @Override
   public float getMaxClimbAngle() {
      return (float) (Math.PI / 2);
   }

   @Override
   public float getMaxSinkAngle() {
      return (float) (Math.PI / 2);
   }

   @Override
   public double getMaximumSpeed() {
      return this.maxHorizontalSpeed * this.effectHorizontalSpeedMultiplier;
   }

   @Override
   public boolean isFastMotionKind(double speed) {
      return this.currentRelativeSpeed > this.fastDiveThreshold;
   }

   @Override
   public boolean is2D() {
      return false;
   }

   @Override
   public boolean canRestAtPlace() {
      return true;
   }

   @Override
   public double getDesiredAltitudeWeight() {
      return this.desiredDepthWeight;
   }

   @Override
   public double getHeightOverGround() {
      return this.probeMoveProbe.getHeightOverGround();
   }

   @Override
   public boolean estimateVelocity(Steering steering, @Nonnull Vector3d velocityOut) {
      velocityOut.assign(Vector3d.ZERO);
      return false;
   }

   @Override
   public void updateModelParameters(Ref<EntityStore> ref, Model model, @Nonnull Box boundingBox, ComponentAccessor<EntityStore> componentAccessor) {
      super.updateModelParameters(ref, model, boundingBox, componentAccessor);
      this.swimDepth = relativeSwimDepthToHeight(ref, this.relativeSwimDepth, model, boundingBox, componentAccessor);
   }

   @Override
   protected void dampForceVelocity(
      @Nonnull Vector3d forceVelocity, double forceVelocityDamping, double interval, ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (forceVelocity.squaredLength() < this.minSpeedAfterForceSquared) {
         forceVelocity.assign(Vector3d.ZERO);
      } else {
         NPCPhysicsMath.deccelerateToStop(forceVelocity, this.getDampingDeceleration(), interval);
      }
   }

   public static double relativeSwimDepthToBoundingBox(double swimDepth, @Nullable Box boundingBox, float eyeHeight) {
      if (boundingBox == null) {
         return 0.5;
      } else {
         return swimDepth < 0.0
            ? NPCPhysicsMath.lerp(eyeHeight, boundingBox.getMin().getY(), -swimDepth)
            : NPCPhysicsMath.lerp(eyeHeight, boundingBox.getMax().getY(), swimDepth);
      }
   }

   public static double relativeSwimDepthToHeight(double swimDepth, @Nullable Box boundingBox, float eyeHeight) {
      return boundingBox == null ? 0.5 : relativeSwimDepthToBoundingBox(swimDepth, boundingBox, eyeHeight) - boundingBox.getMin().getY();
   }

   public static double relativeSwimDepthToHeight(
      @Nullable Ref<EntityStore> ref, double swimDepth, Model model, Box boundingBox, @Nullable ComponentAccessor<EntityStore> componentAccessor
   ) {
      return relativeSwimDepthToHeight(swimDepth, boundingBox, model != null ? model.getEyeHeight(ref, componentAccessor) : 0.0F);
   }

   public double getDampingDeceleration() {
      return this.forceVelocityDamping * DAMPING_FACTOR;
   }
}
