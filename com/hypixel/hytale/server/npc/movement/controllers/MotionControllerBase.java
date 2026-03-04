package com.hypixel.hytale.server.npc.movement.controllers;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.MovementSettings;
import com.hypixel.hytale.protocol.MovementStates;
import com.hypixel.hytale.protocol.Rangef;
import com.hypixel.hytale.server.core.asset.modifiers.MovementEffects;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.camera.CameraSettings;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.modules.collision.BlockCollisionData;
import com.hypixel.hytale.server.core.modules.collision.CollisionModule;
import com.hypixel.hytale.server.core.modules.collision.CollisionModuleConfig;
import com.hypixel.hytale.server.core.modules.collision.CollisionResult;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.physics.component.PhysicsValues;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.modules.splitvelocity.SplitVelocity;
import com.hypixel.hytale.server.core.modules.splitvelocity.VelocityConfig;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.movement.MotionKind;
import com.hypixel.hytale.server.npc.movement.NavState;
import com.hypixel.hytale.server.npc.movement.Steering;
import com.hypixel.hytale.server.npc.movement.controllers.builders.BuilderMotionControllerBase;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.RoleDebugFlags;
import com.hypixel.hytale.server.npc.util.NPCPhysicsMath;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class MotionControllerBase implements MotionController {
   public static final double FORCE_SCALE = 5.0;
   public static final double BISECT_DIST = 0.05;
   public static final double FILTER_COEFFICIENT = 0.7;
   public static final double DOT_PRODUCT_EPSILON = 0.001;
   public static final double DEFAULT_BLOCK_DRAG = 0.82;
   protected static final HytaleLogger LOGGER = NPCPlugin.get().getLogger();
   public static final boolean DEBUG_APPLIED_FORCES = false;
   @Nonnull
   protected final NPCEntity entity;
   protected final String type;
   protected final double epsilonSpeed;
   protected final float epsilonAngle;
   protected final double forceVelocityDamping;
   protected final double maxHorizontalSpeed;
   protected final double fastMotionThreshold;
   protected final double fastMotionThresholdRange;
   protected final float maxHeadRotationSpeed;
   protected Role role;
   protected double inertia;
   protected double knockbackScale;
   protected double gravity;
   @Nullable
   protected float[] headPitchAngleRange;
   protected boolean debugModeSteer;
   protected boolean debugModeMove;
   protected boolean debugModeCollisions;
   protected boolean debugModeBlockCollisions;
   protected boolean debugModeProbeBlockCollisions;
   protected boolean debugModeValidatePositions;
   protected boolean debugModeOverlaps;
   protected boolean debugModeValidateMath;
   protected final Vector3d position = new Vector3d();
   protected final Box collisionBoundingBox = new Box();
   protected final CollisionResult collisionResult = new CollisionResult();
   protected final Vector3d translation = new Vector3d();
   protected final Vector3d bisectValidPosition = new Vector3d();
   protected final Vector3d bisectInvalidPosition = new Vector3d();
   protected final Vector3d lastValidPosition = new Vector3d();
   protected final Vector3d forceVelocity = new Vector3d();
   protected final Vector3d appliedForce = new Vector3d();
   protected boolean ignoreDamping;
   protected final List<MotionControllerBase.AppliedVelocity> appliedVelocities = new ObjectArrayList();
   protected boolean isObstructed;
   protected NavState navState;
   protected double throttleDuration;
   protected double targetDeltaSquared;
   protected boolean recomputePath;
   protected final Vector3d worldNormal = Vector3d.UP;
   protected final Vector3d worldAntiNormal = Vector3d.DOWN;
   protected final Vector3d componentSelector = new Vector3d(1.0, 0.0, 1.0);
   protected final Vector3d planarComponentSelector = new Vector3d(1.0, 0.0, 1.0);
   protected boolean enableTriggers = true;
   protected boolean enableBlockDamage = true;
   protected boolean isReceivingBlockDamage;
   protected boolean isAvoidingBlockDamage = true;
   protected boolean requiresPreciseMovement;
   protected boolean requiresDepthProbing;
   protected boolean havePreciseMovementTarget;
   @Nonnull
   protected Vector3d preciseMovementTarget = new Vector3d();
   protected boolean isRelaxedMoveConstraints;
   protected boolean isBlendingHeading;
   protected double blendHeading;
   protected boolean haveBlendHeadingPosition;
   @Nonnull
   protected Vector3d blendHeadingPosition = new Vector3d();
   protected double blendLevelAtTargetPosition = 0.5;
   protected boolean fastMotionKind;
   protected boolean idleMotionKind;
   protected boolean horizontalIdleKind;
   protected double moveSpeed;
   protected double previousSpeed;
   protected MotionKind motionKind;
   protected MotionKind lastMovementStateUpdatedMotionKind;
   protected MotionKind previousMotionKind;
   protected double effectHorizontalSpeedMultiplier;
   protected boolean cachedMovementBlocked;
   private float yaw;
   private float pitch;
   private float roll;
   private final Vector3d beforeTriggerForce = new Vector3d();
   private final Vector3d beforeTriggerPosition = new Vector3d();
   private boolean processTriggersHasMoved;
   protected MovementSettings movementSettings;

   public MotionControllerBase(@Nonnull BuilderSupport builderSupport, @Nonnull BuilderMotionControllerBase builder) {
      this.entity = builderSupport.getEntity();
      this.type = builder.getType();
      this.epsilonSpeed = builder.getEpsilonSpeed();
      this.epsilonAngle = builder.getEpsilonAngle();
      this.forceVelocityDamping = builder.getForceVelocityDamping();
      this.maxHorizontalSpeed = builder.getMaxHorizontalSpeed(builderSupport);
      this.fastMotionThreshold = builder.getFastHorizontalThreshold(builderSupport);
      this.fastMotionThresholdRange = builder.getFastHorizontalThresholdRange();
      this.maxHeadRotationSpeed = builder.getMaxHeadRotationSpeed(builderSupport);
      this.setInertia(1.0);
      this.setKnockbackScale(1.0);
      this.setGravity(10.0);
   }

   @Override
   public Role getRole() {
      return this.role;
   }

   @Override
   public void setRole(Role role) {
      this.role = role;
   }

   @Override
   public void setInertia(double inertia) {
      this.inertia = Math.max(inertia, 1.0E-4);
   }

   @Override
   public void setKnockbackScale(double knockbackScale) {
      this.knockbackScale = Math.max(0.0, knockbackScale);
   }

   @Override
   public void updateModelParameters(Ref<EntityStore> ref, Model model, @Nonnull Box boundingBox, ComponentAccessor<EntityStore> componentAccessor) {
      Objects.requireNonNull(boundingBox, "updateModelParameters: MotionController needs a bounding box");
      this.collisionBoundingBox.assign(boundingBox);
   }

   @Override
   public void setHeadPitchAngleRange(float[] headPitchAngleRange) {
      if (headPitchAngleRange == null) {
         this.headPitchAngleRange = null;
      } else {
         assert headPitchAngleRange.length == 2;

         this.headPitchAngleRange = (float[])headPitchAngleRange.clone();
      }
   }

   protected void readEntityPosition(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      Vector3f bodyRotation = transformComponent.getRotation();
      this.position.assign(transformComponent.getPosition());
      this.yaw = bodyRotation.getY();
      this.pitch = bodyRotation.getPitch();
      this.roll = bodyRotation.getRoll();
      this.adjustReadPosition(ref, componentAccessor);
      this.postReadPosition(ref, componentAccessor);
   }

   public void postReadPosition(Ref<EntityStore> ref, ComponentAccessor<EntityStore> componentAccessor) {
   }

   public void moveEntity(@Nonnull Ref<EntityStore> ref, double dt, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      this.adjustWritePosition(ref, dt, componentAccessor);
      Vector3f bodyRotation = transformComponent.getRotation();
      bodyRotation.setYaw(this.yaw);
      bodyRotation.setPitch(this.pitch);
      bodyRotation.setRoll(this.roll);
      this.entity.moveTo(ref, this.position.x, this.position.y, this.position.z, componentAccessor);
   }

   public float getYaw() {
      return this.yaw;
   }

   public float getPitch() {
      return this.pitch;
   }

   public float getRoll() {
      return this.roll;
   }

   public boolean touchesWater(boolean defaultValue, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      World world = componentAccessor.getExternalData().getWorld();
      ChunkStore chunkStore = world.getChunkStore();
      long chunkIndex = ChunkUtil.indexChunkFromBlock(this.position.getX(), this.position.getZ());
      Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(chunkIndex);
      if (chunkRef != null && chunkRef.isValid()) {
         WorldChunk worldChunkComponent = chunkStore.getStore().getComponent(chunkRef, WorldChunk.getComponentType());

         assert worldChunkComponent != null;

         int blockX = MathUtil.floor(this.position.getX());
         int blockY = MathUtil.floor(this.position.getY() + this.collisionBoundingBox.min.y);
         int blockZ = MathUtil.floor(this.position.getZ());
         int fluidId = worldChunkComponent.getFluidId(blockX, blockY, blockZ);
         return fluidId != 0;
      } else {
         return defaultValue;
      }
   }

   @Override
   public void updateMovementState(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull MovementStates movementStates,
      @Nonnull Steering steering,
      @Nonnull Vector3d velocity,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      boolean lastFastMotion = movementStates.running;
      movementStates.climbing = false;
      movementStates.swimJumping = false;
      movementStates.inFluid = this.touchesWater(movementStates.inFluid, componentAccessor);
      movementStates.onGround = this.role.isOnGround();
      double speed = this.waypointDistance(Vector3d.ZERO, velocity);
      speed = 0.7 * this.previousSpeed + 0.30000000000000004 * speed;
      this.previousSpeed = speed;
      this.fastMotionKind = this.isFastMotionKind(speed);
      this.idleMotionKind = steering.getTranslation().equals(Vector3d.ZERO);
      this.horizontalIdleKind = this.isHorizontalIdle(speed);
      if (this.motionKind != this.lastMovementStateUpdatedMotionKind
         || lastFastMotion != this.fastMotionKind
         || movementStates.idle != this.idleMotionKind
         || movementStates.horizontalIdle != this.horizontalIdleKind) {
         switch (this.motionKind) {
            case FLYING:
               this.updateFlyingStates(movementStates, this.idleMotionKind, this.fastMotionKind);
               break;
            case SWIMMING:
               this.updateSwimmingStates(movementStates, this.idleMotionKind, this.fastMotionKind, this.horizontalIdleKind);
               break;
            case SWIMMING_TURNING:
               this.updateSwimmingStates(movementStates, false, true, false);
               break;
            case ASCENDING:
               this.updateAscendingStates(ref, movementStates, this.fastMotionKind, this.horizontalIdleKind, componentAccessor);
               break;
            case MOVING:
               updateMovingStates(ref, movementStates, this.fastMotionKind, componentAccessor);
               break;
            case DESCENDING:
               NPCEntity npcComponent = componentAccessor.getComponent(ref, NPCEntity.getComponentType());

               assert npcComponent != null;

               this.updateDescendingStates(ref, movementStates, this.fastMotionKind, npcComponent.getHoverHeight() > 0.0, componentAccessor);
               break;
            case DROPPING:
               this.updateDroppingStates(movementStates);
               break;
            case STANDING:
            default:
               NPCEntity npcComponent = componentAccessor.getComponent(ref, NPCEntity.getComponentType());

               assert npcComponent != null;

               this.updateStandingStates(movementStates, this.motionKind, npcComponent.getHoverHeight() > 0.0);
         }
      }

      this.lastMovementStateUpdatedMotionKind = this.motionKind;
   }

   protected abstract boolean isFastMotionKind(double var1);

   protected void updateFlyingStates(@Nonnull MovementStates movementStates, boolean idle, boolean fastMotionKind) {
      movementStates.flying = true;
      movementStates.idle = idle;
      movementStates.horizontalIdle = false;
      movementStates.walking = !fastMotionKind;
      movementStates.running = fastMotionKind;
      movementStates.falling = false;
      movementStates.swimming = false;
      movementStates.jumping = false;
   }

   protected void updateSwimmingStates(@Nonnull MovementStates movementStates, boolean idle, boolean fastMotionKind, boolean horizontalIdleKind) {
      movementStates.flying = false;
      movementStates.idle = idle;
      movementStates.horizontalIdle = horizontalIdleKind;
      movementStates.walking = !fastMotionKind;
      movementStates.running = fastMotionKind;
      movementStates.falling = false;
      movementStates.swimming = true;
      movementStates.jumping = false;
   }

   protected static void updateMovingStates(
      @Nonnull Ref<EntityStore> ref, @Nonnull MovementStates movementStates, boolean fastMotionKind, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      NPCEntity npcComponent = componentAccessor.getComponent(ref, NPCEntity.getComponentType());

      assert npcComponent != null;

      movementStates.flying = npcComponent.getHoverHeight() > 0.0;
      movementStates.idle = false;
      movementStates.horizontalIdle = false;
      movementStates.falling = false;
      movementStates.walking = !fastMotionKind;
      movementStates.running = fastMotionKind;
      movementStates.swimming = false;
      movementStates.jumping = false;
   }

   protected void updateAscendingStates(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull MovementStates movementStates,
      boolean fastMotionKind,
      boolean horizontalIdleKind,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      updateMovingStates(ref, movementStates, fastMotionKind, componentAccessor);
   }

   protected void updateDescendingStates(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull MovementStates movementStates,
      boolean fastMotionKind,
      boolean hovering,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      updateMovingStates(ref, movementStates, fastMotionKind, componentAccessor);
   }

   protected void updateDroppingStates(@Nonnull MovementStates movementStates) {
      movementStates.falling = true;
   }

   protected void updateStandingStates(@Nonnull MovementStates movementStates, @Nonnull MotionKind motionKind, boolean hovering) {
      movementStates.flying = hovering;
      movementStates.idle = true;
      movementStates.horizontalIdle = true;
      movementStates.walking = false;
      movementStates.running = false;
      movementStates.falling = false;
      movementStates.swimming = false;
      movementStates.jumping = false;
   }

   @Override
   public double steer(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Role role,
      @Nonnull Steering bodySteering,
      @Nonnull Steering headSteering,
      double interval,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      this.readEntityPosition(ref, componentAccessor);
      if (this.debugModeSteer) {
         double dx = this.position.x;
         double dz = this.position.z;
         double st = this.steer0(ref, role, bodySteering, headSteering, interval, componentAccessor);
         double t = interval - st;
         dx = this.position.x - dx;
         dz = this.position.z - dz;
         double l = Math.sqrt(dx * dx + dz * dz);
         double v = t > 0.0 ? l / t : 0.0;
         LOGGER.at(Level.INFO)
            .log(
               "==  Steer %s  = t =%.4f dt=%.4f h =%.4f l =%.4f v =%.4f motion=%s",
               this.getType(),
               interval,
               t,
               (180.0F / (float)Math.PI) * this.yaw,
               l,
               v,
               role.getSteeringMotionName()
            );
         return st;
      } else {
         return this.steer0(ref, role, bodySteering, headSteering, interval, componentAccessor);
      }
   }

   public double steer0(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Role role,
      @Nonnull Steering bodySteering,
      @Nonnull Steering headSteering,
      double interval,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      World world = componentAccessor.getExternalData().getWorld();
      NPCEntity npcComponent = componentAccessor.getComponent(ref, NPCEntity.getComponentType());

      assert npcComponent != null;

      this.effectHorizontalSpeedMultiplier = npcComponent.getCurrentHorizontalSpeedMultiplier(ref, componentAccessor);
      this.setAvoidingBlockDamage(this.isAvoidingBlockDamage && !this.isReceivingBlockDamage);
      this.translation.assign(0.0);
      this.cachedMovementBlocked = this.isMovementBlocked(ref, componentAccessor);
      this.computeMove(ref, role, bodySteering, interval, this.translation, componentAccessor);
      if (this.debugModeValidateMath && !NPCPhysicsMath.isValid(this.translation)) {
         throw new IllegalArgumentException(String.valueOf(this.translation));
      } else {
         if (this.translation.squaredLength() > 1000000.0) {
            if (this.debugModeValidateMath) {
               LOGGER.at(Level.WARNING)
                  .log("NPC with role %s has abnormal high speed! (Distance=%s, MotionController=%s)", role.getRoleName(), this.translation.length(), this.type);
            }

            this.translation.assign(Vector3d.ZERO);
         }

         this.executeMove(ref, role, interval, this.translation, componentAccessor);
         this.postExecuteMove();
         this.clearRequirePreciseMovement();
         this.clearRequireDepthProbing();
         this.clearBlendHeading();
         this.setAvoidingBlockDamage(!this.isReceivingBlockDamage);
         this.setRelaxedMoveConstraints(false);
         float maxBodyRotation = (float)(interval * this.getCurrentMaxBodyRotationSpeed() * bodySteering.getRelativeTurnSpeed());
         float maxHeadRotation = (float)(interval * this.maxHeadRotationSpeed * headSteering.getRelativeTurnSpeed() * this.effectHorizontalSpeedMultiplier);
         this.calculateYaw(ref, bodySteering, headSteering, maxHeadRotation, maxBodyRotation, componentAccessor);
         this.calculatePitch(ref, bodySteering, headSteering, maxHeadRotation, componentAccessor);
         this.calculateRoll(bodySteering, headSteering);
         this.moveEntity(ref, interval, componentAccessor);
         HeadRotation headRotationComponent = componentAccessor.getComponent(ref, HeadRotation.getComponentType());

         assert headRotationComponent != null;

         Vector3f headRotation = headRotationComponent.getRotation();
         headRotation.setYaw(headSteering.getYaw());
         headRotation.setPitch(headSteering.getPitch());
         headRotation.setRoll(headSteering.getRoll());
         if (!this.forceVelocity.equals(Vector3d.ZERO) && !this.ignoreDamping) {
            double movementThresholdSquared = 1.0000000000000002E-10;
            if (this.forceVelocity.squaredLength() >= movementThresholdSquared) {
               this.dampForceVelocity(this.forceVelocity, this.forceVelocityDamping, interval, componentAccessor);
            } else {
               this.forceVelocity.assign(Vector3d.ZERO);
            }
         }

         double clientTps = 60.0;
         int serverTps = world.getTps();
         double rate = clientTps / serverTps;
         boolean dampenY = this.shouldDampenAppliedVelocitiesY();
         boolean useGroundResistance = this.shouldAlwaysUseGroundResistance() || this.onGround();

         for (int i = 0; i < this.appliedVelocities.size(); i++) {
            MotionControllerBase.AppliedVelocity entry = this.appliedVelocities.get(i);
            float min;
            float max;
            if (useGroundResistance) {
               min = entry.config.getGroundResistance();
               max = entry.config.getGroundResistanceMax();
            } else {
               min = entry.config.getAirResistance();
               max = entry.config.getAirResistanceMax();
            }

            float resistance = min;
            if (max >= 0.0F) {
               resistance = switch (entry.config.getStyle()) {
                  case Linear -> {
                     float len = (float)entry.velocity.length();
                     if (len < entry.config.getThreshold()) {
                        float mul = len / entry.config.getThreshold();
                        yield min * mul + max * (1.0F - mul);
                     } else {
                        yield min;
                     }
                  }
                  case Exp -> {
                     float len = (float)entry.velocity.squaredLength();
                     if (len < entry.config.getThreshold() * entry.config.getThreshold()) {
                        float mul = len / (entry.config.getThreshold() * entry.config.getThreshold());
                        yield min * mul + max * (1.0F - mul);
                     } else {
                        yield min;
                     }
                  }
               };
            }

            double resistanceScale = Math.pow(resistance, rate);
            entry.velocity.x *= resistanceScale;
            entry.velocity.z *= resistanceScale;
            if (dampenY) {
               entry.velocity.y *= resistanceScale;
            }
         }

         this.appliedVelocities.removeIf(v -> v.velocity.squaredLength() < 0.001);
         return interval;
      }
   }

   protected boolean shouldDampenAppliedVelocitiesY() {
      return false;
   }

   protected boolean shouldAlwaysUseGroundResistance() {
      return false;
   }

   protected void calculateYaw(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Steering bodySteering,
      @Nonnull Steering headSteering,
      float maxHeadRotation,
      float maxBodyRotation,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (bodySteering.hasYaw()) {
         this.yaw = bodySteering.getYaw();
      } else if (NPCPhysicsMath.dotProduct(this.translation.x, 0.0, this.translation.z) > 0.001) {
         this.yaw = PhysicsMath.headingFromDirection(this.translation.x, this.translation.z);
      }

      boolean hasHeadSteering = headSteering.hasYaw();
      if (!hasHeadSteering) {
         headSteering.setYaw(this.yaw);
      }

      HeadRotation headRotationComponent = componentAccessor.getComponent(ref, HeadRotation.getComponentType());

      assert headRotationComponent != null;

      ModelComponent modelComponent = componentAccessor.getComponent(ref, ModelComponent.getComponentType());

      assert modelComponent != null;

      Vector3f headRotation = headRotationComponent.getRotation();
      float currentYaw = headRotation.getYaw();
      float targetYaw = headSteering.getYaw();
      float turnAngle = MathUtil.clamp(NPCPhysicsMath.turnAngle(currentYaw, targetYaw), -maxHeadRotation, maxHeadRotation);
      headSteering.setYaw(PhysicsMath.normalizeTurnAngle(currentYaw + turnAngle));
      if (hasHeadSteering) {
         float yawOffset = MathUtil.wrapAngle(headSteering.getYaw() - this.yaw);
         CameraSettings headRotationRestrictions = modelComponent.getModel().getCamera();
         float yawMin;
         float yawMax;
         if (headRotationRestrictions != null && headRotationRestrictions.getYaw() != null && headRotationRestrictions.getYaw().getAngleRange() != null) {
            Rangef yawRange = headRotationRestrictions.getYaw().getAngleRange();
            yawMin = yawRange.min * (float) (Math.PI / 180.0);
            yawMax = yawRange.max * (float) (Math.PI / 180.0);
         } else {
            yawMin = (float) (-Math.PI / 4);
            yawMax = (float) (Math.PI / 4);
         }

         if (yawOffset > yawMax) {
            float initialBodyYaw = this.yaw;
            if (!bodySteering.hasYaw()) {
               this.yaw = this.blendBodyYaw(ref, yawOffset, maxBodyRotation, componentAccessor);
            }

            headSteering.setYaw(MathUtil.wrapAngle(initialBodyYaw + yawMax));
         } else if (yawOffset < yawMin) {
            float initialBodyYaw = this.yaw;
            if (!bodySteering.hasYaw()) {
               this.yaw = this.blendBodyYaw(ref, yawOffset, maxBodyRotation, componentAccessor);
            }

            headSteering.setYaw(MathUtil.wrapAngle(initialBodyYaw + yawMin));
         }
      }
   }

   protected float blendBodyYaw(
      @Nonnull Ref<EntityStore> ref, float yawOffset, float maxBodyRotation, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      Vector3f bodyRotation = transformComponent.getRotation();
      float currentBodyYaw = bodyRotation.getYaw();
      float targetBodyYaw = MathUtil.wrapAngle(this.yaw + yawOffset);
      float bodyTurnAngle = MathUtil.clamp(NPCPhysicsMath.turnAngle(currentBodyYaw, targetBodyYaw), -maxBodyRotation, maxBodyRotation);
      return MathUtil.wrapAngle(this.yaw + bodyTurnAngle);
   }

   protected void calculatePitch(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Steering bodySteering,
      @Nonnull Steering headSteering,
      float maxHeadRotation,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (bodySteering.hasPitch()) {
         this.pitch = bodySteering.getPitch();
      } else if (NPCPhysicsMath.dotProduct(this.translation.x, this.translation.y, this.translation.z) > 0.001) {
         this.pitch = PhysicsMath.pitchFromDirection(this.translation.x, this.translation.y, this.translation.z);
      }

      boolean hasHeadSteering = headSteering.hasPitch();
      if (!hasHeadSteering) {
         headSteering.setPitch(this.pitch);
      }

      HeadRotation headRotationComponent = componentAccessor.getComponent(ref, HeadRotation.getComponentType());

      assert headRotationComponent != null;

      Vector3f headRotation = headRotationComponent.getRotation();
      float currentPitch = headRotation.getPitch();
      float targetPitch = headSteering.getPitch();
      float turnAngle = MathUtil.clamp(NPCPhysicsMath.turnAngle(currentPitch, targetPitch), -maxHeadRotation, maxHeadRotation);
      headSteering.setPitch(PhysicsMath.normalizeTurnAngle(currentPitch + turnAngle));
      if (hasHeadSteering) {
         ModelComponent modelComponent = componentAccessor.getComponent(ref, ModelComponent.getComponentType());

         assert modelComponent != null;

         float bodyPitch = this.pitch;
         float pitchOffset = MathUtil.wrapAngle(headSteering.getPitch() - bodyPitch);
         CameraSettings headRotationRestrictions = modelComponent.getModel().getCamera();
         float pitchMin;
         float pitchMax;
         if (this.headPitchAngleRange != null) {
            pitchMin = this.headPitchAngleRange[0];
            pitchMax = this.headPitchAngleRange[1];
         } else if (headRotationRestrictions != null
            && headRotationRestrictions.getPitch() != null
            && headRotationRestrictions.getPitch().getAngleRange() != null) {
            Rangef pitchRange = headRotationRestrictions.getPitch().getAngleRange();
            pitchMin = pitchRange.min * (float) (Math.PI / 180.0);
            pitchMax = pitchRange.max * (float) (Math.PI / 180.0);
         } else {
            pitchMin = (float) (-Math.PI / 4);
            pitchMax = (float) (Math.PI / 4);
         }

         if (pitchOffset > pitchMax) {
            headSteering.setPitch(MathUtil.wrapAngle(bodyPitch + pitchMax));
         } else if (pitchOffset < pitchMin) {
            headSteering.setPitch(MathUtil.wrapAngle(bodyPitch + pitchMin));
         }
      }
   }

   protected void calculateRoll(@Nonnull Steering bodySteering, @Nonnull Steering headSteering) {
      if (bodySteering.hasRoll()) {
         this.roll = bodySteering.getRoll();
      }

      if (!headSteering.hasRoll()) {
         headSteering.setRoll(this.roll);
      }
   }

   protected void dampForceVelocity(
      @Nonnull Vector3d forceVelocity, double forceVelocityDamping, double interval, ComponentAccessor<EntityStore> componentAccessor
   ) {
      World world = componentAccessor.getExternalData().getWorld();
      double drag = 0.0;
      if (this.motionKind != MotionKind.FLYING) {
         if (!this.onGround() && this.motionKind != MotionKind.SWIMMING && this.motionKind != MotionKind.SWIMMING_TURNING) {
            double horizontalSpeed = Math.sqrt(forceVelocity.x * forceVelocity.x + forceVelocity.z * forceVelocity.z);
            drag = convertToNewRange(
               horizontalSpeed,
               this.movementSettings.airDragMinSpeed,
               this.movementSettings.airDragMaxSpeed,
               this.movementSettings.airDragMin,
               this.movementSettings.airDragMax
            );
         } else {
            drag = 0.82;
         }
      }

      double clientTps = 60.0;
      int serverTps = world.getTps();
      double rate = 60.0 / serverTps;
      drag = Math.pow(drag, rate);
      forceVelocity.x *= drag;
      forceVelocity.z *= drag;
      float velocityEpsilon = 0.1F;
      if (Math.abs(forceVelocity.x) <= velocityEpsilon) {
         forceVelocity.x = 0.0;
      }

      if (Math.abs(forceVelocity.y) <= velocityEpsilon) {
         forceVelocity.y = 0.0;
      }

      if (Math.abs(forceVelocity.z) <= velocityEpsilon) {
         forceVelocity.z = 0.0;
      }
   }

   private static double convertToNewRange(double value, double oldMinRange, double oldMaxRange, double newMinRange, double newMaxRange) {
      if (newMinRange != newMaxRange && oldMinRange != oldMaxRange) {
         double newValue = (value - oldMinRange) * (newMaxRange - newMinRange) / (oldMaxRange - oldMinRange) + newMinRange;
         return MathUtil.clamp(newValue, Math.min(newMinRange, newMaxRange), Math.max(newMinRange, newMaxRange));
      } else {
         return newMinRange;
      }
   }

   @Override
   public double probeMove(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Vector3d position,
      @Nonnull Vector3d direction,
      @Nonnull ProbeMoveData probeMoveData,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      probeMoveData.setPosition(position).setDirection(direction);
      return this.probeMove(ref, probeMoveData, componentAccessor);
   }

   protected void postExecuteMove() {
   }

   protected void adjustReadPosition(Ref<EntityStore> ref, ComponentAccessor<EntityStore> componentAccessor) {
   }

   protected void adjustWritePosition(Ref<EntityStore> ref, double dt, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
   }

   @Override
   public boolean isInProgress() {
      return false;
   }

   @Override
   public boolean isObstructed() {
      return this.isObstructed;
   }

   @Override
   public NavState getNavState() {
      return this.navState;
   }

   @Override
   public double getThrottleDuration() {
      return this.throttleDuration;
   }

   @Override
   public double getTargetDeltaSquared() {
      return this.targetDeltaSquared;
   }

   @Override
   public void setNavState(NavState navState, double throttleDuration, double targetDeltaSquared) {
      this.navState = navState;
      this.throttleDuration = throttleDuration;
      this.targetDeltaSquared = targetDeltaSquared;
   }

   @Override
   public boolean isForceRecomputePath() {
      return this.recomputePath;
   }

   @Override
   public void setForceRecomputePath(boolean recomputePath) {
      this.recomputePath = recomputePath;
   }

   @Override
   public void beforeInstructionSensorsAndActions(double physicsTickDuration) {
      this.recomputePath = false;
   }

   @Override
   public void beforeInstructionMotion(double physicsTickDuration) {
      this.resetNavState();
   }

   public boolean isHorizontalIdle(double speed) {
      return speed == 0.0;
   }

   @Override
   public boolean canAct(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      return this.isAlive(ref, componentAccessor)
         && this.role.couldBreatheCached()
         && this.forceVelocity.equals(Vector3d.ZERO)
         && this.appliedVelocities.isEmpty();
   }

   public boolean isMovementBlocked(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      InteractionManager interactionManager = componentAccessor.getComponent(ref, InteractionModule.get().getInteractionManagerComponent());
      if (interactionManager != null) {
         Boolean movementBlocked = interactionManager.forEachInteraction((chain, interaction, val) -> {
            if (val) {
               return Boolean.TRUE;
            } else {
               MovementEffects movementEffects = interaction.getEffects().getMovementEffects();
               return movementEffects != null ? movementEffects.isDisableAll() : Boolean.FALSE;
            }
         }, Boolean.FALSE);
         return movementBlocked;
      } else {
         return false;
      }
   }

   protected abstract double computeMove(
      @Nonnull Ref<EntityStore> var1, @Nonnull Role var2, Steering var3, double var4, Vector3d var6, @Nonnull ComponentAccessor<EntityStore> var7
   );

   protected abstract double executeMove(
      @Nonnull Ref<EntityStore> var1, @Nonnull Role var2, double var3, Vector3d var5, @Nonnull ComponentAccessor<EntityStore> var6
   );

   public <T> double bisect(
      @Nonnull Vector3d validPosition, @Nonnull Vector3d invalidPosition, @Nonnull T t, @Nonnull BiPredicate<T, Vector3d> validate, @Nonnull Vector3d result
   ) {
      return this.bisect(validPosition, invalidPosition, t, validate, 0.05, result);
   }

   public <T> double bisect(
      @Nonnull Vector3d validPosition,
      @Nonnull Vector3d invalidPosition,
      @Nonnull T t,
      @Nonnull BiPredicate<T, Vector3d> validate,
      double maxDistance,
      @Nonnull Vector3d result
   ) {
      double validDistance = 0.0;
      double invalidDistance = 1.0;
      this.bisectValidPosition.assign(validPosition);
      this.bisectInvalidPosition.assign(invalidPosition);
      maxDistance *= maxDistance;
      double validWeight = 0.1;
      double invalidWeight = 0.9;

      while (this.bisectValidPosition.distanceSquaredTo(this.bisectInvalidPosition) > maxDistance) {
         double distance = validWeight * validDistance + invalidWeight * invalidDistance;
         result.x = validWeight * this.bisectValidPosition.x + invalidWeight * this.bisectInvalidPosition.x;
         result.y = validWeight * this.bisectValidPosition.y + invalidWeight * this.bisectInvalidPosition.y;
         result.z = validWeight * this.bisectValidPosition.z + invalidWeight * this.bisectInvalidPosition.z;
         if (validate.test(t, result)) {
            validDistance = distance;
            this.bisectValidPosition.assign(result);
         } else {
            invalidDistance = distance;
            this.bisectInvalidPosition.assign(result);
            validWeight = 0.5;
            invalidWeight = 0.5;
         }
      }

      result.assign(this.bisectValidPosition);
      return validDistance;
   }

   @Nonnull
   @Override
   public Vector3d getForce() {
      return this.forceVelocity;
   }

   @Override
   public void addForce(@Nonnull Vector3d force, VelocityConfig velocityConfig) {
      double scale = this.knockbackScale;
      if (!SplitVelocity.SHOULD_MODIFY_VELOCITY && velocityConfig != null) {
         this.appliedVelocities.add(new MotionControllerBase.AppliedVelocity(new Vector3d(force.x * scale, force.y * scale, force.z * scale), velocityConfig));
      } else {
         double horzMul = 0.18000000000000005 * this.movementSettings.velocityResistance;
         this.forceVelocity.add(force.x * scale * horzMul, force.y * scale, force.z * scale * horzMul);
         this.appliedForce.assign(this.forceVelocity);
         this.ignoreDamping = false;
      }
   }

   @Override
   public void forceVelocity(@Nonnull Vector3d velocity, @Nullable VelocityConfig velocityConfig, boolean ignoreDamping) {
      if (!SplitVelocity.SHOULD_MODIFY_VELOCITY && velocityConfig != null) {
         this.appliedVelocities.clear();
         this.appliedVelocities.add(new MotionControllerBase.AppliedVelocity(velocity.clone(), velocityConfig));
      } else {
         this.forceVelocity.assign(velocity);
         this.ignoreDamping = ignoreDamping;
      }
   }

   public void clearForce() {
      this.forceVelocity.assign(Vector3d.ZERO);
   }

   protected void dumpCollisionResults() {
      String slideString = "";
      if (this.collisionResult.isSliding) {
         slideString = String.format("SLIDE: start/end=%f/%f", this.collisionResult.slideStart, this.collisionResult.slideEnd);
      }

      LOGGER.at(Level.INFO)
         .log(
            "CollRes: pos=%s yaw=%f count=%d %s",
            Vector3d.formatShortString(this.position),
            (180.0F / (float)Math.PI) * this.yaw,
            this.collisionResult.getBlockCollisionCount(),
            slideString
         );
      if (this.collisionResult.getBlockCollisionCount() > 0) {
         for (int i = 0; i < this.collisionResult.getBlockCollisionCount(); i++) {
            BlockCollisionData cd = this.collisionResult.getBlockCollision(i);
            String materialName = cd.blockMaterial != null ? cd.blockMaterial.name() : "none";
            String typeName = cd.blockType != null ? cd.blockType.getId() : "none";
            String hitboxName = cd.blockType != null ? cd.blockType.getHitboxType() : "none";
            String rotation;
            if (cd.blockType != null) {
               RotationTuple blockRotation = RotationTuple.get(cd.rotation);
               rotation = blockRotation.yaw() + " " + blockRotation.pitch();
            } else {
               rotation = "none";
            }

            LOGGER.at(Level.INFO)
               .log(
                  "   COLL: blk=%s/%s/%s start=%f norm=%s pos=%s mat=%s block=%s hitbox=%s rot=%s",
                  cd.x,
                  cd.y,
                  cd.z,
                  cd.collisionStart,
                  Vector3d.formatShortString(cd.collisionNormal),
                  Vector3d.formatShortString(cd.collisionPoint),
                  materialName,
                  typeName,
                  hitboxName,
                  rotation
               );
         }
      }
   }

   public void setEnableTriggers(boolean enableTriggers) {
      this.enableTriggers = enableTriggers;
   }

   public void setEnableBlockDamage(boolean enableBlockDamage) {
      this.enableBlockDamage = enableBlockDamage;
   }

   @Override
   public boolean willReceiveBlockDamage() {
      return this.isReceivingBlockDamage;
   }

   @Override
   public void setAvoidingBlockDamage(boolean avoid) {
      this.isAvoidingBlockDamage = avoid;
   }

   @Override
   public boolean isAvoidingBlockDamage() {
      return this.isAvoidingBlockDamage;
   }

   public void processTriggers(
      @Nonnull Ref<EntityStore> ref, @Nonnull CollisionResult collisionResult, double t, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      this.processTriggersHasMoved = false;
      this.isReceivingBlockDamage = false;
      if (this.enableTriggers || this.enableBlockDamage) {
         collisionResult.pruneTriggerBlocks(t);
         int count = collisionResult.getTriggerBlocks().size();
         if (count != 0) {
            if (this.enableTriggers) {
               this.beforeTriggerForce.assign(this.getForce());
               this.beforeTriggerPosition.assign(this.position);
            }

            this.moveEntity(ref, 0.0, componentAccessor);
            InteractionManager interactionManagerComponent = componentAccessor.getComponent(ref, InteractionModule.get().getInteractionManagerComponent());

            assert interactionManagerComponent != null;

            int damageToEntity = collisionResult.defaultTriggerBlocksProcessing(
               interactionManagerComponent, this.entity, ref, this.enableTriggers, componentAccessor
            );
            if (this.enableBlockDamage && damageToEntity > 0) {
               Damage damage = new Damage(Damage.NULL_SOURCE, DamageCause.ENVIRONMENT, damageToEntity);
               DamageSystems.executeDamage(ref, componentAccessor, damage);
               this.isReceivingBlockDamage = true;
            }

            this.readEntityPosition(ref, componentAccessor);
            if (this.enableTriggers) {
               this.processTriggersHasMoved = !this.beforeTriggerForce.equals(this.getForce()) || !this.beforeTriggerPosition.equals(this.position);
            }
         }
      }
   }

   public boolean isProcessTriggersHasMoved() {
      return this.processTriggersHasMoved;
   }

   protected boolean isAlive(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      return !componentAccessor.getArchetype(ref).contains(DeathComponent.getComponentType());
   }

   @Override
   public void onDebugFlagsChanged(EnumSet<RoleDebugFlags> newFlags) {
      MotionController.super.onDebugFlagsChanged(newFlags);
      this.debugModeSteer = newFlags.contains(RoleDebugFlags.MotionControllerSteer);
      this.debugModeMove = newFlags.contains(RoleDebugFlags.MotionControllerMove);
      this.debugModeCollisions = newFlags.contains(RoleDebugFlags.Collisions);
      this.debugModeBlockCollisions = newFlags.contains(RoleDebugFlags.BlockCollisions);
      this.debugModeProbeBlockCollisions = newFlags.contains(RoleDebugFlags.ProbeBlockCollisions);
      this.debugModeValidatePositions = newFlags.contains(RoleDebugFlags.ValidatePositions);
      this.debugModeOverlaps = newFlags.contains(RoleDebugFlags.Overlaps);
      this.debugModeValidateMath = newFlags.contains(RoleDebugFlags.ValidateMath);
   }

   @Override
   public void activate() {
      this.resetObstructedFlags();
      this.resetNavState();
   }

   public void resetNavState() {
      this.navState = NavState.AT_GOAL;
      this.throttleDuration = 0.0;
      this.targetDeltaSquared = 0.0;
   }

   public void resetObstructedFlags() {
      this.isObstructed = false;
   }

   @Override
   public void deactivate() {
   }

   public double getEpsilonSpeed() {
      return this.epsilonSpeed;
   }

   public float getEpsilonAngle() {
      return this.epsilonAngle;
   }

   @Nonnull
   @Override
   public Vector3d getComponentSelector() {
      return this.componentSelector;
   }

   @Nonnull
   @Override
   public Vector3d getPlanarComponentSelector() {
      return this.planarComponentSelector;
   }

   @Override
   public void setComponentSelector(@Nonnull Vector3d componentSelector) {
      this.componentSelector.assign(componentSelector);
   }

   @Override
   public Vector3d getWorldNormal() {
      return this.worldNormal;
   }

   @Override
   public Vector3d getWorldAntiNormal() {
      return this.worldAntiNormal;
   }

   @Override
   public double waypointDistance(@Nonnull Vector3d p, @Nonnull Vector3d q) {
      return Math.sqrt(this.waypointDistanceSquared(p, q));
   }

   @Override
   public double waypointDistanceSquared(@Nonnull Vector3d p, @Nonnull Vector3d q) {
      double dx = (p.x - q.x) * this.getComponentSelector().x;
      double dy = (p.y - q.y) * this.getComponentSelector().y;
      double dz = (p.z - q.z) * this.getComponentSelector().z;
      return dx * dx + dy * dy + dz * dz;
   }

   @Override
   public double waypointDistance(@Nonnull Ref<EntityStore> ref, @Nonnull Vector3d p, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      return Math.sqrt(this.waypointDistanceSquared(ref, p, componentAccessor));
   }

   @Override
   public double waypointDistanceSquared(@Nonnull Ref<EntityStore> ref, @Nonnull Vector3d p, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      Vector3d position = transformComponent.getPosition();
      double dx = (p.x - position.getX()) * this.getComponentSelector().x;
      double dy = (p.y - position.getY()) * this.getComponentSelector().y;
      double dz = (p.z - position.getZ()) * this.getComponentSelector().z;
      return dx * dx + dy * dy + dz * dz;
   }

   @Override
   public boolean isValidPosition(@Nonnull Vector3d position, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      return this.isValidPosition(position, this.collisionResult, componentAccessor);
   }

   public boolean isValidPosition(
      @Nonnull Vector3d position, @Nonnull CollisionResult collisionResult, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      World world = componentAccessor.getExternalData().getWorld();
      CollisionModule module = CollisionModule.get();
      CollisionModuleConfig config = module.getConfig();
      boolean saveDebugModeOverlaps = config.isDumpInvalidBlocks();
      config.setDumpInvalidBlocks(this.debugModeOverlaps);
      boolean isValid = module.validatePosition(
            world,
            this.collisionBoundingBox,
            position,
            this.getInvalidOverlapMaterials(),
            null,
            (_this, collisionCode, collision, collisionConfig) -> collisionConfig.blockId != Integer.MIN_VALUE,
            collisionResult
         )
         != -1;
      config.setDumpInvalidBlocks(saveDebugModeOverlaps);
      return isValid;
   }

   public int getInvalidOverlapMaterials() {
      return 4;
   }

   protected void saveMotionKind() {
      this.previousMotionKind = this.getMotionKind();
   }

   protected boolean switchedToMotionKind(MotionKind motionKind) {
      return this.getMotionKind() == motionKind && this.previousMotionKind != motionKind;
   }

   public MotionKind getMotionKind() {
      return this.motionKind;
   }

   public void setMotionKind(MotionKind motionKind) {
      this.motionKind = motionKind;
   }

   @Override
   public double getGravity() {
      return this.gravity;
   }

   public void setGravity(double gravity) {
      this.gravity = gravity;
   }

   @Override
   public boolean translateToAccessiblePosition(
      Vector3d position, Box boundingBox, double minYValue, double maxYValue, ComponentAccessor<EntityStore> componentAccessor
   ) {
      return true;
   }

   @Override
   public boolean standingOnBlockOfType(int blockSet) {
      return false;
   }

   @Override
   public void requirePreciseMovement(@Nullable Vector3d positionHint) {
      this.requiresPreciseMovement = true;
      this.havePreciseMovementTarget = positionHint != null;
      if (this.havePreciseMovementTarget) {
         this.preciseMovementTarget.assign(positionHint);
      }
   }

   public void clearRequirePreciseMovement() {
      this.requiresPreciseMovement = false;
      this.havePreciseMovementTarget = false;
   }

   public boolean isRequiresPreciseMovement() {
      return this.requiresPreciseMovement;
   }

   @Override
   public void requireDepthProbing() {
      this.requiresDepthProbing = true;
   }

   public void clearRequireDepthProbing() {
      this.requiresDepthProbing = false;
   }

   public boolean isRequiresDepthProbing() {
      return this.requiresDepthProbing;
   }

   @Override
   public void enableHeadingBlending(double heading, @Nullable Vector3d targetPosition, double blendLevel) {
      this.isBlendingHeading = true;
      this.blendHeading = heading;
      this.haveBlendHeadingPosition = targetPosition != null;
      if (this.haveBlendHeadingPosition) {
         this.blendHeadingPosition.assign(targetPosition);
      }

      this.blendLevelAtTargetPosition = blendLevel;
   }

   @Override
   public void enableHeadingBlending() {
      this.enableHeadingBlending(Double.NaN, null, 0.0);
   }

   public void clearBlendHeading() {
      this.isBlendingHeading = false;
      this.haveBlendHeadingPosition = false;
   }

   @Override
   public void setRelaxedMoveConstraints(boolean relax) {
      this.isRelaxedMoveConstraints = relax;
   }

   @Override
   public boolean isRelaxedMoveConstraints() {
      return this.isRelaxedMoveConstraints;
   }

   @Override
   public void updatePhysicsValues(PhysicsValues values) {
      this.movementSettings = MovementManager.MASTER_DEFAULT.apply(values, GameMode.Adventure);
   }

   protected static class AppliedVelocity {
      protected final Vector3d velocity;
      protected final VelocityConfig config;
      protected boolean canClear;

      public AppliedVelocity(Vector3d velocity, VelocityConfig config) {
         this.velocity = velocity;
         this.config = config;
      }
   }
}
