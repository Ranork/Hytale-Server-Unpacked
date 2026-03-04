package com.hypixel.hytale.server.npc.movement.controllers;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.util.TrigMathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.collision.BlockCollisionData;
import com.hypixel.hytale.server.core.modules.collision.CollisionModule;
import com.hypixel.hytale.server.core.modules.collision.WorldUtil;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkColumn;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.movement.MotionKind;
import com.hypixel.hytale.server.npc.movement.Steering;
import com.hypixel.hytale.server.npc.movement.controllers.builders.BuilderMotionControllerFly;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.util.NPCPhysicsMath;
import com.hypixel.hytale.server.npc.util.PositionProbeAir;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MotionControllerFly extends MotionControllerBase {
   public static final String TYPE = "Fly";
   public static final double DAMPING_FACTOR = 20.0;
   public static final int COLLISION_MATERIALS_PASSIVE = 4;
   public static final int COLLISION_MATERIALS_ACTIVE = 6;
   protected final double minAirSpeed;
   protected final double maxClimbSpeed;
   protected final double maxSinkSpeed;
   protected final double maxFallSpeed;
   protected final double maxSinkSpeedFluid;
   protected final float maxClimbAngle;
   protected final float maxSinkAngle;
   protected final double acceleration;
   protected final double deceleration;
   protected final double sinkRatio = 0.5;
   protected final double desiredAltitudeWeight;
   protected final float maxTurnSpeed;
   protected final float maxRollAngle;
   protected final float maxRollSpeed;
   protected final float rollDamping;
   protected final double fastFlyThreshold;
   protected final double minHeightOverGround;
   protected final double maxHeightOverGround;
   protected final boolean autoLevel;
   protected final double sinMaxClimbAngle;
   protected final double sinMaxSinkAngle;
   protected final MotionController.VerticalRange verticalRange = new MotionController.VerticalRange();
   protected final PositionProbeAir moveProbe = new PositionProbeAir();
   protected final PositionProbeAir probeMoveProbe = new PositionProbeAir();
   protected int lastVerticalPositionX = Integer.MIN_VALUE;
   protected int lastVerticalPositionZ = Integer.MIN_VALUE;
   protected final Vector3d lastVelocity = new Vector3d();
   protected double lastSpeed;
   protected float lastRoll;
   protected double currentRelativeSpeed;
   protected double minSpeedAfterForceSquared;
   @Nullable
   protected double[] desiredAltitudeOverride;

   public MotionControllerFly(@Nonnull BuilderSupport builderSupport, @Nonnull BuilderMotionControllerFly builder) {
      super(builderSupport, builder);
      this.setGravity(builder.getGravity());
      this.componentSelector.assign(1.0, 1.0, 1.0);
      this.minAirSpeed = builder.getMinAirSpeed();
      this.maxClimbSpeed = builder.getMaxClimbSpeed();
      this.maxSinkSpeed = builder.getMaxSinkSpeed();
      this.maxFallSpeed = builder.getMaxFallSpeed();
      this.maxSinkSpeedFluid = builder.getMaxSinkSpeedFluid();
      this.maxClimbAngle = builder.getMaxClimbAngle();
      this.sinMaxClimbAngle = TrigMathUtil.sin(this.maxClimbAngle);
      this.maxSinkAngle = builder.getMaxSinkAngle();
      this.sinMaxSinkAngle = -TrigMathUtil.sin(this.maxSinkAngle);
      this.acceleration = builder.getAcceleration();
      this.deceleration = builder.getDeceleration();
      this.maxTurnSpeed = builder.getMaxTurnSpeed();
      this.maxRollAngle = builder.getMaxRollAngle();
      this.minHeightOverGround = builder.getMinHeightOverGround(builderSupport);
      this.maxHeightOverGround = builder.getMaxHeightOverGround(builderSupport);
      this.maxRollSpeed = builder.getMaxRollSpeed();
      this.rollDamping = builder.getRollDamping();
      this.fastFlyThreshold = builder.getFastFlyThreshold();
      this.autoLevel = builder.isAutoLevel();
      this.desiredAltitudeWeight = builder.getDesiredAltitudeWeight();
      this.minSpeedAfterForceSquared = MathUtil.minValue(this.maxHorizontalSpeed, this.maxSinkSpeed, this.maxClimbSpeed);
      this.minSpeedAfterForceSquared = this.minSpeedAfterForceSquared * this.minSpeedAfterForceSquared;
   }

   @Nonnull
   @Override
   public String getType() {
      return "Fly";
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
      this.setMotionKind(this.inWater() ? MotionKind.MOVING : MotionKind.FLYING);
      this.moveProbe.probePosition(ref, this.collisionBoundingBox, this.position, this.collisionResult, componentAccessor);
      this.currentRelativeSpeed = steering.getSpeed();
      if (!this.isAlive(ref, componentAccessor)) {
         this.forceVelocity.assign(Vector3d.ZERO);
         this.appliedVelocities.clear();
      }

      double maxFallSpeed = this.moveProbe.isInWater() ? this.maxSinkSpeedFluid : this.maxFallSpeed;
      boolean onGround = this.onGround();
      if (this.forceVelocity.equals(Vector3d.ZERO) && this.appliedVelocities.isEmpty()) {
         if (NPCPhysicsMath.near(this.lastVelocity, Vector3d.ZERO)) {
            PhysicsMath.vectorFromAngles(this.getYaw(), this.getPitch(), this.lastVelocity);
            this.lastSpeed = 0.0;
         }

         if (this.canAct(ref, componentAccessor)) {
            translation.assign(steering.getTranslation());
            double steeringSpeed = steering.hasTranslation() ? translation.length() : 0.0;
            float yaw = PhysicsMath.normalizeAngle(this.getYaw());
            float pitch = PhysicsMath.normalizeTurnAngle(this.getPitch());
            double dirX = translation.x;
            double dirZ = translation.z;
            double dotXZ = dirX * dirX + dirZ * dirZ;
            float expYaw;
            float expPitch;
            if (dotXZ >= 1.0E-12) {
               expYaw = PhysicsMath.headingFromDirection(dirX, dirZ);
               expPitch = TrigMathUtil.atan2(translation.y, Math.sqrt(dotXZ));
            } else {
               expYaw = steering.hasYaw() ? steering.getYaw() : yaw;
               expPitch = steering.hasPitch() ? steering.getPitch() : (this.autoLevel ? 0.0F : pitch);
            }

            steering.clearYaw();
            steering.clearPitch();
            expPitch = MathUtil.clamp(expPitch, -this.maxSinkAngle, this.maxClimbAngle);
            float turnYaw = NPCPhysicsMath.turnAngle(yaw, expYaw);
            float turnPitch = NPCPhysicsMath.turnAngle(pitch, expPitch);
            float maxRotationAngle = (float)(this.getCurrentMaxBodyRotationSpeed() * dt);
            turnYaw = NPCPhysicsMath.clampRotation(turnYaw, maxRotationAngle);
            turnPitch = NPCPhysicsMath.clampRotation(turnPitch, maxRotationAngle);
            float newYaw = PhysicsMath.normalizeAngle(yaw + turnYaw);
            float newPitch = PhysicsMath.normalizeTurnAngle(pitch + turnPitch);
            double speedLimit = this.computeMaxSpeedFromPitch(pitch);
            steeringSpeed *= speedLimit;
            double minSpeed = Math.max(this.minAirSpeed, this.lastSpeed - this.deceleration * dt);
            double maxSpeed = this.lastSpeed + this.acceleration * dt;
            steeringSpeed = maxSpeed < minSpeed ? minSpeed : MathUtil.clamp(steeringSpeed, minSpeed, maxSpeed);
            PhysicsMath.vectorFromAngles(newYaw, newPitch, translation);
            translation.normalize();
            double mX = this.lastVelocity.z;
            double mZ = -this.lastVelocity.x;
            double mL = Math.sqrt(mX * mX + mZ * mZ);
            float rollTurnCosine = (float)(NPCPhysicsMath.dotProduct(mX, 0.0, mZ, translation.x, translation.y, translation.z) / mL);
            float maxRollTurnAngle = (float)(this.maxTurnSpeed * dt);
            float maxRollTurnCosine = TrigMathUtil.sin(maxRollTurnAngle);
            float rollTurnStrength = rollTurnCosine / maxRollTurnCosine;
            double speedFactor = steeringSpeed / speedLimit;
            float desiredRoll = this.maxRollAngle * MathUtil.clamp(rollTurnStrength, -1.0F, 1.0F) * MathUtil.clamp((float)speedFactor, 0.0F, 1.0F);
            float dampedRoll = MathUtil.clamp(this.rollDamping * this.lastRoll + (1.0F - this.rollDamping) * desiredRoll, -this.maxRollAngle, this.maxRollAngle);
            float deltaRoll = (float)(this.maxRollSpeed * dt);
            float constrainedRoll = MathUtil.clamp(dampedRoll, this.lastRoll - deltaRoll, this.lastRoll + deltaRoll);
            this.lastRoll = constrainedRoll;
            steering.setYaw(newYaw);
            steering.setPitch(newPitch);
            steering.setRoll(constrainedRoll);
            if (steeringSpeed == 0.0) {
               translation.assign(Vector3d.ZERO);
            } else {
               translation.scale(steeringSpeed * this.effectHorizontalSpeedMultiplier);
            }

            this.lastVelocity.assign(translation);
            this.lastSpeed = steeringSpeed;
            translation.scale(dt);
            if (this.debugModeValidateMath && !NPCPhysicsMath.isValid(translation)) {
               throw new IllegalArgumentException(String.valueOf(translation));
            }
         } else {
            steering.setYaw(this.getYaw());
            steering.setPitch(this.getPitch());
            steering.setRoll(this.getRoll());
            if (onGround) {
               this.setMotionKind(MotionKind.STANDING);
               this.lastVelocity.assign(Vector3d.ZERO);
               this.lastSpeed = 0.0;
               return dt;
            }

            this.setMotionKind(MotionKind.DROPPING);
            translation.y = NPCPhysicsMath.gravityDrag(this.lastVelocity.y, this.gravity, dt, maxFallSpeed);
            double diffSpeed = maxFallSpeed - translation.y;
            if (!(diffSpeed <= 0.0) && !this.isObstructed()) {
               double scale = translation.x * translation.x + translation.z * translation.z;
               if (diffSpeed * diffSpeed < scale) {
                  scale = Math.sqrt(scale / diffSpeed);
                  translation.x = this.lastVelocity.x * scale;
                  translation.z = this.lastVelocity.z * scale;
               } else {
                  translation.x = this.lastVelocity.x;
                  translation.z = this.lastVelocity.z;
               }
            } else {
               translation.x = 0.0;
               translation.z = 0.0;
            }

            this.lastVelocity.assign(translation);
            this.lastSpeed = this.lastVelocity.length();
            translation.scale(dt);
            if (this.debugModeValidateMath && !NPCPhysicsMath.isValid(translation)) {
               throw new IllegalArgumentException(String.valueOf(translation));
            }
         }

         if (this.lastSpeed > 1.0E-6 && this.isAlive(ref, componentAccessor)) {
            this.setDirectionFromTranslation(steering, translation);
         }

         return dt;
      } else {
         steering.setYaw(this.getYaw());
         steering.setPitch(this.getPitch());
         steering.setRoll(this.getRoll());
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

         if (!onGround) {
            translation.y = NPCPhysicsMath.accelerateDrag(translation.y, -this.gravity, dt, maxFallSpeed);
         }

         this.lastVelocity.assign(translation);
         this.lastSpeed = this.lastVelocity.length();
         translation.scale(dt);
         if (this.debugModeValidateMath && !NPCPhysicsMath.isValid(translation)) {
            throw new IllegalArgumentException(String.valueOf(translation));
         } else {
            return dt;
         }
      }
   }

   private void setDirectionFromTranslation(@Nonnull Steering steering, @Nonnull Vector3d translation) {
      if (!steering.hasYaw()) {
         if (translation.x * translation.x + translation.z * translation.z > 1.0E-6) {
            steering.setYaw(PhysicsMath.headingFromDirection(translation.x, translation.z));
         } else {
            steering.setYaw(this.getYaw());
         }
      }

      if (!steering.hasPitch()) {
         steering.setPitch(PhysicsMath.pitchFromDirection(translation.x, translation.y, translation.z));
      }
   }

   @Override
   public double probeMove(@Nonnull Ref<EntityStore> ref, @Nonnull ProbeMoveData probeMoveData, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      return probeMoveData.probeDirection.length()
         * this.doMove(ref, probeMoveData.probePosition, probeMoveData.probeDirection, this.probeMoveProbe, probeMoveData, componentAccessor);
   }

   @Override
   public boolean isFastMotionKind(double speed) {
      return this.lastVelocity.y < -1.0E-6 || this.currentRelativeSpeed > this.fastFlyThreshold && this.lastVelocity.y <= 1.0E-6;
   }

   @Override
   public MotionController.VerticalRange getDesiredVerticalRange(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      Vector3d position = transformComponent.getPosition();
      int ix = MathUtil.floor(position.getX());
      int iz = MathUtil.floor(position.getZ());
      if (ix == this.lastVerticalPositionX && iz == this.lastVerticalPositionZ) {
         return this.verticalRange;
      } else {
         this.lastVerticalPositionX = ix;
         this.lastVerticalPositionZ = iz;
         double y = position.getY();
         World world = componentAccessor.getExternalData().getWorld();
         ChunkStore chunkStore = world.getChunkStore();
         long chunkIndex = ChunkUtil.indexChunkFromBlock(ix, iz);
         Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(chunkIndex);
         if (chunkRef != null && chunkRef.isValid()) {
            Store<ChunkStore> chunkStoreAccessor = chunkStore.getStore();
            ChunkColumn chunkColumnComponent = chunkStoreAccessor.getComponent(chunkRef, ChunkColumn.getComponentType());
            BlockChunk blockChunkComponent = chunkStoreAccessor.getComponent(chunkRef, BlockChunk.getComponentType());
            if (chunkColumnComponent != null && blockChunkComponent != null) {
               double minHeightOverGround;
               double maxHeightOverGround;
               if (this.desiredAltitudeOverride != null) {
                  minHeightOverGround = this.desiredAltitudeOverride[0];
                  maxHeightOverGround = this.desiredAltitudeOverride[1];
               } else {
                  minHeightOverGround = this.minHeightOverGround;
                  maxHeightOverGround = this.maxHeightOverGround;
               }

               int iy = MathUtil.floor(y);
               double below = WorldUtil.findFarthestEmptySpaceBelow(chunkStoreAccessor, chunkColumnComponent, blockChunkComponent, ix, iy, iz, iy)
                  + this.collisionBoundingBox.min.y;
               double above = WorldUtil.findFarthestEmptySpaceAbove(chunkStoreAccessor, chunkColumnComponent, blockChunkComponent, ix, iy, iz, iy)
                  - this.collisionBoundingBox.max.y
                  + 1.0;
               double minY = below + minHeightOverGround;
               double maxY = Math.min(below + maxHeightOverGround, above);
               if (minY > maxY) {
                  minY = y;
                  maxY = y;
               }

               this.verticalRange.assign(y, minY, maxY);
               return this.verticalRange;
            } else {
               this.verticalRange.assign(y, y, y);
               return this.verticalRange;
            }
         } else {
            this.verticalRange.assign(y, y, y);
            return this.verticalRange;
         }
      }
   }

   @Override
   public double getWanderVerticalMovementRatio() {
      return 0.5;
   }

   protected double doMove(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Vector3d position,
      @Nonnull Vector3d translation,
      @Nonnull PositionProbeAir moveProbe,
      @Nullable ProbeMoveData probeMoveData,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      boolean probeOnly = probeMoveData != null;
      boolean saveSegments = probeOnly && probeMoveData.startProbing();
      boolean canAct = probeOnly || this.canAct(ref, componentAccessor);
      String debugPrefix = probeOnly ? "Probe" : "Move";
      if (this.debugModeMove) {
         LOGGER.at(Level.INFO)
            .log(
               "%s - Fly: Execute pos=%s vel=%s onGround=%s blocked=%s ",
               debugPrefix,
               Vector3d.formatShortString(position),
               Vector3d.formatShortString(translation),
               this.onGround(),
               this.isObstructed
            );
      }

      if (this.debugModeValidatePositions && !this.isValidPosition(position, this.collisionResult, componentAccessor)) {
         throw new IllegalStateException("Invalid position");
      } else {
         if (saveSegments) {
            probeMoveData.addStartSegment(position, false);
         }

         if (!probeOnly) {
            this.isObstructed = false;
            if (this.debugModeBlockCollisions) {
               this.collisionResult.setLogger(LOGGER);
            }
         }

         this.collisionResult.setCollisionByMaterial(canAct ? 6 : 4);
         CollisionModule.get();
         CollisionModule.findCollisions(this.collisionBoundingBox, position, translation, this.collisionResult, componentAccessor);
         if (this.debugModeBlockCollisions) {
            this.collisionResult.setLogger(null);
         }

         if (this.debugModeCollisions) {
            this.dumpCollisionResults();
         }

         BlockCollisionData collision = this.collisionResult.getFirstBlockCollision();
         this.lastValidPosition.assign(position);
         double distanceFactor;
         if (collision == null) {
            position.add(translation);
            distanceFactor = 1.0;
            if (this.debugModeMove) {
               LOGGER.at(Level.INFO)
                  .log(
                     "%s - Fly: No collision pos=%s vel=%s onGround=%s blocked=%s ",
                     debugPrefix,
                     Vector3d.formatShortString(position),
                     Vector3d.formatShortString(translation),
                     this.onGround(),
                     this.isObstructed
                  );
            }

            if (this.debugModeValidatePositions && !this.isValidPosition(position, this.collisionResult, componentAccessor)) {
               throw new IllegalStateException("Invalid position");
            }
         } else {
            position.assign(collision.collisionPoint);
            distanceFactor = collision.collisionStart;
            if (!probeOnly) {
               this.isObstructed = true;
            }

            if (this.debugModeMove) {
               LOGGER.at(Level.INFO)
                  .log(
                     "%s - Fly: Collision pos=%s collStart=%s vel=%s onGround=%s blocked=%s ",
                     debugPrefix,
                     Vector3d.formatShortString(position),
                     distanceFactor,
                     Vector3d.formatShortString(translation),
                     this.onGround(),
                     this.isObstructed
                  );
            }

            if (this.debugModeValidatePositions && !this.isValidPosition(position, this.collisionResult, componentAccessor)) {
               throw new IllegalStateException("Invalid position");
            }
         }

         if (!moveProbe.probePosition(ref, this.collisionBoundingBox, position, this.collisionResult, componentAccessor)) {
            double adjust = this.bisect(
               this.lastValidPosition,
               position,
               this,
               (_this, newPosition) -> _this.moveProbe.probePosition(ref, _this.collisionBoundingBox, newPosition, _this.collisionResult, componentAccessor),
               position
            );
            distanceFactor *= adjust;
            if (this.debugModeMove) {
               LOGGER.at(Level.INFO)
                  .log("%s - Fly: Bisect step pos=%s distanceFactor=%s adjust=%s", debugPrefix, Vector3d.formatShortString(position), distanceFactor, adjust);
            }
         }

         if (!probeOnly) {
            this.processTriggers(ref, this.collisionResult, distanceFactor, componentAccessor);
         } else if (saveSegments) {
            double distance = this.waypointDistance(probeMoveData.initialPosition, position);
            if (collision == null) {
               probeMoveData.addMoveSegment(position, false, distance);
            } else if (this.getWorldNormal().equals(collision.collisionNormal)) {
               probeMoveData.addHitGroundSegment(position, distance, collision.collisionNormal, collision.blockId);
            } else {
               probeMoveData.addHitWallSegment(position, false, distance, collision.collisionNormal, collision.blockId);
            }

            probeMoveData.addEndSegment(position, true, distance);
         }

         return distanceFactor;
      }
   }

   @Override
   protected double executeMove(
      @Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Vector3d translation, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      double scale = this.doMove(ref, this.position, translation, this.moveProbe, null, componentAccessor);
      if (scale < 1.0) {
         dt *= scale;
         this.lastSpeed *= scale;
         this.lastVelocity.scale(scale);
      }

      return dt;
   }

   @Override
   public void constrainRotations(Role role, TransformComponent transform) {
   }

   @Override
   public double getCurrentMaxBodyRotationSpeed() {
      return this.maxTurnSpeed * this.effectHorizontalSpeedMultiplier;
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

   @Override
   protected boolean shouldDampenAppliedVelocitiesY() {
      return true;
   }

   @Override
   protected boolean shouldAlwaysUseGroundResistance() {
      return true;
   }

   @Override
   public void spawned() {
   }

   @Override
   public boolean canAct(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      return super.canAct(ref, componentAccessor) && this.moveProbe.isInAir() && this.effectHorizontalSpeedMultiplier != 0.0;
   }

   @Override
   public boolean inAir() {
      return !this.onGround();
   }

   @Override
   public boolean onGround() {
      return this.moveProbe.isOnGround();
   }

   @Override
   public boolean inWater() {
      return this.moveProbe.isInWater();
   }

   @Override
   public double getCurrentSpeed() {
      return 0.0;
   }

   @Override
   public double getCurrentTurnRadius() {
      return this.lastSpeed / this.maxTurnSpeed;
   }

   @Override
   public float getMaxClimbAngle() {
      return this.maxClimbAngle;
   }

   @Override
   public float getMaxSinkAngle() {
      return this.maxSinkAngle;
   }

   @Override
   public double getMaximumSpeed() {
      return MathUtil.maxValue(this.maxClimbSpeed, this.maxHorizontalSpeed, this.maxSinkSpeed) * this.effectHorizontalSpeedMultiplier;
   }

   @Override
   public boolean is2D() {
      return false;
   }

   @Override
   public boolean canRestAtPlace() {
      return false;
   }

   @Override
   public double getDesiredAltitudeWeight() {
      return this.desiredAltitudeWeight;
   }

   @Override
   public double getHeightOverGround() {
      return this.probeMoveProbe.getHeightOverGround();
   }

   @Override
   public boolean isHorizontalIdle(double speed) {
      return false;
   }

   @Override
   public boolean estimateVelocity(Steering steering, @Nonnull Vector3d velocityOut) {
      velocityOut.assign(Vector3d.ZERO);
      return false;
   }

   @Override
   public void clearOverrides() {
      this.desiredAltitudeOverride = null;
   }

   public void setDesiredAltitudeOverride(double[] desiredAltitudeOverride) {
      this.desiredAltitudeOverride = desiredAltitudeOverride;
   }

   public void takeOff(@Nonnull Ref<EntityStore> ref, double speed, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      PhysicsMath.vectorFromAngles(transformComponent.getRotation().getYaw(), (float) (Math.PI / 4), this.lastVelocity);
      this.lastSpeed = speed;
   }

   public double getMinSpeedAfterForceSquared() {
      return this.minSpeedAfterForceSquared;
   }

   public double getDampingDeceleration() {
      return this.forceVelocityDamping * 20.0;
   }

   protected double computeMaxSpeedFromPitch(double pitch) {
      double sinePitch = TrigMathUtil.sin(pitch);
      double cosinePitch = Math.sqrt(1.0 - sinePitch * sinePitch);
      double c = cosinePitch * this.maxHorizontalSpeed;
      double s = sinePitch * (sinePitch > 0.0 ? this.maxClimbSpeed : this.maxSinkSpeed);
      return Math.sqrt(c * c + s * s);
   }
}
