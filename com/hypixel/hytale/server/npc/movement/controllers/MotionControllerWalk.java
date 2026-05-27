package com.hypixel.hytale.server.npc.movement.controllers;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.util.TrigMathUtil;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.protocol.MovementStates;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.asset.type.fluidfx.config.FluidFX;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.modules.blockset.BlockSetModule;
import com.hypixel.hytale.server.core.modules.collision.BlockCollisionData;
import com.hypixel.hytale.server.core.modules.collision.BoxBlockIntersectionEvaluator;
import com.hypixel.hytale.server.core.modules.collision.CollisionConfig;
import com.hypixel.hytale.server.core.modules.collision.CollisionMath;
import com.hypixel.hytale.server.core.modules.collision.CollisionModule;
import com.hypixel.hytale.server.core.modules.collision.CollisionResult;
import com.hypixel.hytale.server.core.modules.collision.WorldUtil;
import com.hypixel.hytale.server.core.modules.entity.component.PositionDataComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.modules.splitvelocity.VelocityConfig;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.movement.MotionKind;
import com.hypixel.hytale.server.npc.movement.MovementMode;
import com.hypixel.hytale.server.npc.movement.Steering;
import com.hypixel.hytale.server.npc.movement.constraints.RelaxedConstraint;
import com.hypixel.hytale.server.npc.movement.controllers.builders.BuilderMotionControllerWalk;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.util.NPCPhysicsMath;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector2d;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class MotionControllerWalk extends MotionControllerBase {
   public static final String TYPE = "Walk";
   public static final Set<MovementMode> SUPPORTED_MOVEMENT_MODES = Set.of(MovementMode.WALK, MovementMode.WADE, MovementMode.UNDERWATER_WALK);
   public static final double CLIMB_FORWARD_DISTANCE = 0.1;
   public static final double CLIMB_FORWARD_DISTANCE_SQUARED = 0.010000000000000002;
   public static final double JUMP_FORWARD_DISTANCE = 0.5;
   public static final double ONE_PLUS_THRESHOLD = 1.00001;
   public static final double MIN_SAFETY_DISTANCE = 0.05;
   public static final double EDGE_MARGIN_BBOX_FACTOR = 0.1;
   public static final double MIN_SEPARATION_STEERING = 1.0E-5;
   protected static final EnumSet<MotionKind> STATE_CAN_HOVER = EnumSet.of(MotionKind.MOVING, MotionKind.STANDING);
   protected static final EnumSet<MotionKind> VALID_MOTIONS = EnumSet.of(
      MotionKind.ASCENDING, MotionKind.DESCENDING, MotionKind.DROPPING, MotionKind.STANDING, MotionKind.MOVING
   );
   protected static final int COLLISION_MATERIALS = 4;
   protected static final int WALKING_MATERIALS = 5;
   protected static final int WALKING_MATERIALS_RELAXED = 13;
   protected final double minHorizontalSpeed;
   protected final double maxVerticalSpeed;
   protected final double maxVerticalSpeedFluid;
   protected final double acceleration;
   protected final double maxRotationSpeed;
   protected final float maxMoveTurnAngle;
   protected final float blendRestTurnAngle;
   protected final double blendRestRelativeSpeed;
   protected final double maxClimbHeight;
   protected final double jumpHeight;
   protected final double minJumpHeight;
   protected final double minJumpDistance;
   protected final double jumpForce;
   protected final double jumpDescentSteepness;
   protected final double jumpBlending;
   protected final double jumpDescentBlending;
   protected final double climbSpeedMult;
   protected final double climbSpeedPow;
   protected final double climbSpeedConst;
   protected final double maxDropHeight;
   protected final double maxDropHeightRelaxed;
   protected final double minDescentAnimationHeight;
   protected final double descendFlatness;
   protected final double descendSpeedCompensation;
   protected final double descentSteepness;
   protected final double descentBlending;
   protected final MotionControllerWalk.DescentAnimationType descentAnimationType;
   protected final MotionControllerWalk.AscentAnimationType ascentAnimationType;
   protected final double maxWalkSpeedAfterHitMultiplier;
   protected final int fenceBlockSet;
   protected final double minHover;
   protected final double maxHover;
   protected final double hoverFreq;
   protected final float hoverCycle;
   protected final double minHoverClimb;
   protected final double minHoverDrop;
   protected final boolean floatsDown;
   protected boolean onGround;
   protected boolean inWater;
   protected double horizontalSpeedMultiplier;
   protected double fallStartHeight;
   protected double fallSpeed;
   protected double currentRelativeSpeed;
   protected boolean isFullyRotated = true;
   @Nullable
   protected BlockType belowBlockType;
   protected int belowBlockTypeId = 0;
   protected int[] footingBlocks;
   protected short[] footingFillers;
   protected byte[] footingRotations;
   protected final Vector3d footingPosition = new Vector3d();
   protected boolean footingBlocksValid;
   protected double breathingDepth;
   protected double constraintDepth;
   protected double climbUpDistance;
   protected double currentJumpHeight;
   protected double jumpDropHeight;
   protected double jumpBlockHeight;
   protected double predictedFallHeight;
   protected final Vector3d jumpDropDirection = new Vector3d();
   protected final Vector3d climbUpDirection = new Vector3d();
   protected double currentClimbForwardDistance;
   protected double maxClimbForwardDistance;
   protected double totalDropDistance;
   protected final Vector3d climbForwardDirection = new Vector3d();
   protected double climbSpeed;
   protected boolean jumping;
   protected final MotionController.VerticalRange verticalRange = new MotionController.VerticalRange();
   protected final Vector3d tmpClimbPosition = new Vector3d();
   protected final Vector3d tmpClimbMovement = new Vector3d();
   protected final Vector3d tmpMovePosition = new Vector3d();
   protected final CollisionResult tmpResults = new CollisionResult();
   protected final Vector2d tmpClimbHeightResults = new Vector2d();
   private final EnumSet<RelaxedConstraint> effectiveMoveConstraints = EnumSet.noneOf(RelaxedConstraint.class);

   public MotionControllerWalk(@Nonnull BuilderMotionControllerWalk builder, @Nonnull BuilderSupport builderSupport) {
      super(builderSupport, builder);
      this.setGravity(builder.getGravity());
      this.minHorizontalSpeed = builder.getMinHorizontalSpeed();
      this.maxVerticalSpeed = builder.getMaxVerticalSpeed();
      this.maxVerticalSpeedFluid = builder.getMaxVerticalSpeedFluid();
      this.acceleration = builder.getAcceleration(builderSupport);
      this.maxRotationSpeed = builder.getMaxRotationSpeed(builderSupport);
      this.maxMoveTurnAngle = builder.getMaxMoveTurnAngle(builderSupport);
      this.blendRestTurnAngle = builder.getBlendRestTurnAngle(builderSupport);
      this.blendRestRelativeSpeed = builder.getBlendRestRelativeSpeed(builderSupport);
      this.maxClimbHeight = builder.getMaxClimbHeight(builderSupport);
      this.jumpHeight = builder.getJumpHeight(builderSupport);
      this.minJumpHeight = builder.getMinJumpHeight(builderSupport);
      this.minJumpDistance = builder.getMinJumpDistance(builderSupport);
      this.jumpForce = builder.getJumpForce(builderSupport);
      this.jumpDescentSteepness = builder.getJumpDescentSteepness(builderSupport);
      this.jumpBlending = builder.getJumpBlending(builderSupport);
      this.jumpDescentBlending = builder.getJumpDescentBlending(builderSupport);
      this.ascentAnimationType = builder.getAscentAnimationType(builderSupport);
      this.climbSpeedMult = builder.getClimbSpeedMult(builderSupport);
      this.climbSpeedPow = builder.getClimbSpeedPow(builderSupport);
      this.climbSpeedConst = builder.getClimbSpeedConst(builderSupport);
      this.minDescentAnimationHeight = builder.getMinDescentAnimationHeight(builderSupport);
      this.descendFlatness = builder.getDescendForwardAmount(builderSupport);
      this.descendSpeedCompensation = builder.getDescendSpeedCompensation(builderSupport);
      this.maxDropHeight = builder.getMaxDropHeight(builderSupport);
      this.maxDropHeightRelaxed = this.maxDropHeight + builder.getMaxDropHeightRelaxedAdjustment(builderSupport);
      this.fenceBlockSet = builder.getFenceBlockSet();
      this.minHover = builder.getMinHover();
      this.maxHover = builder.getMaxHover();
      this.minHoverClimb = builder.getMinHoverClimb();
      this.minHoverDrop = builder.getMinHoverDrop();
      this.floatsDown = builder.isFloatsDown();
      this.hoverFreq = builder.getHoverFreq();
      this.hoverCycle = this.hoverFreq > 0.0 ? 1.0F / (float)this.hoverFreq : 0.0F;
      this.maxWalkSpeedAfterHitMultiplier = builder.getMaxWalkSpeedAfterHitMultiplier();
      this.descentAnimationType = builder.getDescentAnimationType(builderSupport);
      this.descentSteepness = builder.getDescentSteepness(builderSupport);
      this.descentBlending = builder.getDescentBlending(builderSupport);
   }

   @Nonnull
   @Override
   public String getType() {
      return "Walk";
   }

   @Nonnull
   @Override
   public Set<MovementMode> getSupportedMovementModes() {
      return SUPPORTED_MOVEMENT_MODES;
   }

   @Nonnull
   @Override
   public Set<MovementMode> getDefaultSpawnMovementModes() {
      assert this.role != null;

      if (this.role.isBreathesInAir()) {
         return Set.of(MovementMode.WALK);
      } else {
         assert this.role.isBreathesInWater();

         return Set.of(MovementMode.UNDERWATER_WALK);
      }
   }

   @Override
   public void spawned() {
      this.position.y = this.position.y + this.minHover;
   }

   @Override
   public double getWanderVerticalMovementRatio() {
      return 0.0;
   }

   @Nonnull
   @Override
   public MotionController.VerticalRange getDesiredVerticalRange(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      this.verticalRange.set(transformComponent.getPosition().y(), 0.0, 320.0);
      return this.verticalRange;
   }

   @Override
   protected void adjustReadPosition(Ref<EntityStore> ref, ComponentAccessor<EntityStore> componentAccessor) {
      NPCEntity npcComponent = componentAccessor.getComponent(ref, NPCEntity.getComponentType());

      assert npcComponent != null;

      this.position.y = this.position.y - npcComponent.getHoverHeight();
   }

   @Override
   protected void adjustWritePosition(Ref<EntityStore> ref, double dt, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      NPCEntity npcComponent = componentAccessor.getComponent(ref, NPCEntity.getComponentType());

      assert npcComponent != null;

      if (this.maxHover > 0.0 && this.hoverCycle > 0.0F && dt > 0.0) {
         double hoverHeight = npcComponent.getHoverHeight();
         float hoverPhase = npcComponent.getHoverPhase();
         if (this.switchedToMotionKind(MotionKind.DROPPING) || this.switchedToMotionKind(MotionKind.DESCENDING)) {
            double hoverDrop = this.maxHover - hoverHeight;
            double dropDist = this.dropDistance(this.position, hoverDrop, componentAccessor);
            if (dropDist < hoverDrop) {
               hoverDrop = dropDist;
            } else {
               hoverPhase = this.hoverCycle / 2.0F;
            }

            hoverHeight += hoverDrop;
            this.position.y -= hoverDrop;
         } else if (this.switchedToMotionKind(MotionKind.ASCENDING)) {
            double hoverClimb = hoverHeight - this.minHoverClimb;
            if (hoverClimb > this.climbUpDistance) {
               hoverClimb = this.climbUpDistance;
            } else {
               hoverPhase = 0.0F;
            }

            hoverHeight -= hoverClimb;
            this.position.y += hoverClimb;
            this.climbUpDistance -= hoverClimb;
         } else if (hoverHeight < this.minHover) {
            hoverHeight += dt * this.computeClimbSpeed(this.moveSpeed);
            if (hoverHeight >= this.minHover) {
               hoverHeight = this.minHover;
               hoverPhase = 0.0F;
            }
         } else if (STATE_CAN_HOVER.contains(this.getMotionKind())) {
            hoverPhase = (hoverPhase + (float)dt) % this.hoverCycle;
            double scale = (float) (Math.PI * 2) / this.hoverCycle;
            double derivate = TrigMathUtil.cos(scale * hoverPhase - (float) (Math.PI / 2)) * scale;
            hoverHeight += dt * derivate * (this.maxHover - this.minHover) / 2.0;
            if (hoverHeight <= this.minHover) {
               hoverHeight = this.minHover;
               hoverPhase = 0.0F;
            } else if (hoverHeight >= this.maxHover) {
               hoverHeight = this.maxHover;
               hoverPhase = this.hoverCycle / 2.0F;
            }
         }

         npcComponent.setHoverPhase(hoverPhase);
         npcComponent.setHoverHeight(hoverHeight);
      }

      this.position.y = this.position.y + npcComponent.getHoverHeight();
   }

   @Override
   protected void updateAscendingStates(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull MovementStates movementStates,
      boolean fastMotionKind,
      boolean horizontalIdleKind,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (!this.jumping) {
         switch (this.ascentAnimationType) {
            case Walk:
               movementStates.jumping = false;
               movementStates.idle = false;
               movementStates.flying = false;
               break;
            case Jump:
               movementStates.jumping = true;
               movementStates.idle = false;
               movementStates.flying = false;
               break;
            case Climb:
               movementStates.jumping = false;
               movementStates.idle = false;
               movementStates.flying = false;
               movementStates.climbing = true;
               break;
            case Fly:
               movementStates.jumping = false;
               movementStates.idle = false;
               movementStates.flying = true;
               break;
            case Idle:
               movementStates.jumping = false;
               movementStates.idle = true;
               movementStates.flying = false;
         }
      } else {
         NPCEntity npcComponent = componentAccessor.getComponent(ref, NPCEntity.getComponentType());

         assert npcComponent != null;

         movementStates.jumping = this.jumping;
         movementStates.idle = false;
         movementStates.flying = npcComponent.getHoverHeight() > 0.0;
      }

      movementStates.horizontalIdle = horizontalIdleKind;
      movementStates.falling = false;
      movementStates.running = fastMotionKind;
      movementStates.walking = !fastMotionKind;
      movementStates.sprinting = false;
      movementStates.swimming = false;
   }

   @Override
   protected void updateDescendingStates(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull MovementStates movementStates,
      boolean fastMotionKind,
      boolean hovering,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (this.predictedFallHeight >= this.minDescentAnimationHeight - 1.0E-5) {
         switch (this.descentAnimationType) {
            case Walk:
               movementStates.falling = false;
               movementStates.idle = false;
               movementStates.running = fastMotionKind;
               movementStates.walking = !fastMotionKind;
               movementStates.flying = hovering;
               break;
            case Fall:
               movementStates.falling = true;
               movementStates.idle = false;
               movementStates.running = false;
               movementStates.walking = false;
               movementStates.flying = false;
               break;
            case Idle:
               movementStates.falling = false;
               movementStates.idle = true;
               movementStates.running = false;
               movementStates.walking = false;
               movementStates.flying = hovering;
         }
      } else {
         movementStates.falling = false;
         movementStates.idle = false;
         movementStates.running = fastMotionKind;
         movementStates.walking = !fastMotionKind;
         movementStates.flying = hovering;
      }

      movementStates.horizontalIdle = false;
      movementStates.swimming = false;
      movementStates.jumping = false;
      movementStates.sprinting = false;
   }

   @Override
   public boolean isFastMotionKind(double speed) {
      boolean isRunning = this.fastMotionKind;
      if (this.jumping && this.getMotionKind() == MotionKind.ASCENDING) {
         return isRunning;
      } else {
         double threshold;
         if (isRunning) {
            threshold = this.fastMotionThreshold - this.fastMotionThresholdRange;
         } else {
            threshold = this.fastMotionThreshold + this.fastMotionThresholdRange;
         }

         threshold *= this.maxHorizontalSpeed;
         return speed > threshold;
      }
   }

   @Override
   public boolean isInProgress() {
      MotionKind motionKind = this.getMotionKind();
      return motionKind == MotionKind.ASCENDING || motionKind == MotionKind.DESCENDING || motionKind == MotionKind.DROPPING;
   }

   private boolean isOnSolidGround() {
      return this.onGround && this.belowBlockType != null && this.belowBlockType.getMaterial() == BlockMaterial.Solid;
   }

   @Override
   public boolean canSteer(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      return super.canSteer(ref, componentAccessor) && this.isOnSolidGround();
   }

   @Nullable
   @Override
   public String canSteerFailReason(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      String reason = super.canSteerFailReason(ref, componentAccessor);
      if (reason != null) {
         return reason;
      } else if (!this.onGround) {
         return "OFF_GROUND";
      } else if (this.belowBlockType == null) {
         return "NO_BLOCK_BELOW";
      } else {
         return this.belowBlockType.getMaterial() != BlockMaterial.Solid ? "NO_SOLID_BLOCK_BELOW" : null;
      }
   }

   @Override
   public void updateModelParameters(Ref<EntityStore> ref, Model model, @Nonnull Box boundingBox, ComponentAccessor<EntityStore> componentAccessor) {
      super.updateModelParameters(ref, model, boundingBox, componentAccessor);
      float eyeHeight = model != null ? model.getEyeHeight(ref, componentAccessor) : 0.0F;
      this.collisionBoundingBox.max.y = this.collisionBoundingBox.max.y + this.maxHover;
      this.breathingDepth = 1.0E-5 + eyeHeight;
      if (this.role.isBreathesInAir() && this.role.isBreathesInWater()) {
         this.constraintDepth = this.breathingDepth;
      } else if (this.role.isBreathesInAir()) {
         this.constraintDepth = Math.min(0.25, this.breathingDepth / 2.0);
      } else {
         this.constraintDepth = 1.0E-5;
      }

      int size = (int)(MathUtil.fastCeil(this.collisionBoundingBox.width() + 1.0) * MathUtil.fastCeil(this.collisionBoundingBox.depth() + 1.0));
      this.footingBlocks = new int[size];
      this.footingFillers = new short[size];
      this.footingRotations = new byte[size];
      this.footingBlocksValid = false;
   }

   @Override
   public void constrainRotations(Role role, @Nonnull TransformComponent transform) {
      Rotation3f rotation = transform.getRotation();
      rotation.setPitch(0.0F);
      rotation.setRoll(0.0F);
   }

   @Override
   public void setVelocity(@Nonnull Vector3dc velocity, VelocityConfig velocityConfig, boolean ignoreDamping) {
      super.setVelocity(velocity, velocityConfig, ignoreDamping);
      this.onGround = false;
   }

   @Override
   public boolean inAir() {
      return !this.onGround && !this.inWater;
   }

   @Override
   public boolean onGround() {
      return this.onGround;
   }

   @Override
   public boolean standingOnBlockOfType(int blockSet) {
      return BlockSetModule.getInstance().blockInSet(blockSet, this.belowBlockType);
   }

   @Override
   public boolean inWater() {
      return this.inWater;
   }

   @Override
   public boolean touchesWater(boolean defaultValue, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      return this.inWater;
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
      return 0.0F;
   }

   @Override
   public float getMaxSinkAngle() {
      return 0.0F;
   }

   @Override
   public double getMaxClimbHeight() {
      return this.maxClimbHeight;
   }

   @Override
   public double getMaxDropHeight() {
      return this.maxDropHeight;
   }

   @Override
   public double getMaximumSpeed() {
      return this.maxHorizontalSpeed * this.horizontalSpeedMultiplier * this.effectHorizontalSpeedMultiplier;
   }

   @Override
   public boolean is2D() {
      return true;
   }

   @Override
   public boolean canRestAtPlace() {
      return true;
   }

   @Override
   public double getDesiredAltitudeWeight() {
      return 0.0;
   }

   @Override
   public double getHeightOverGround() {
      return 0.0;
   }

   @Override
   public boolean estimateVelocity(@Nonnull Steering steering, @Nonnull Vector3d velocityOut) {
      if (steering.hasTranslation()) {
         velocityOut.set(steering.getTranslation()).mul(this.getCurrentSpeed());
         return true;
      } else {
         velocityOut.zero();
         return false;
      }
   }

   @Override
   public void setMotionKind(MotionKind motionKind) {
      if (!VALID_MOTIONS.contains(motionKind)) {
         motionKind = MotionKind.STANDING;
      }

      super.setMotionKind(motionKind);
   }

   @Override
   public void postReadPosition(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (!this.footingPosition.equals(this.position)) {
         this.footingBlocksValid = false;
         this.footingPosition.set(this.position);
      }

      World world = componentAccessor.getExternalData().getWorld();
      boolean wasOnGround = this.onGround;
      double bottomY = this.position.y + this.collisionBoundingBox.min.y;
      int blockY = MathUtil.floor(bottomY);
      if (bottomY - blockY < 1.0E-5) {
         blockY--;
      }

      if (blockY >= 0 && blockY <= 319) {
         double xPos = this.position.x > 0.0 ? this.position.x + MathUtil.EPSILON_DOUBLE : this.position.x - MathUtil.EPSILON_DOUBLE;
         double zPos = this.position.z > 0.0 ? this.position.z + MathUtil.EPSILON_DOUBLE : this.position.z - MathUtil.EPSILON_DOUBLE;
         long minBlockX = MathUtil.fastFloor(xPos + this.collisionBoundingBox.min.x);
         long maxBlockX = MathUtil.fastFloor(xPos + this.collisionBoundingBox.max.x);
         long minBlockZ = MathUtil.fastFloor(zPos + this.collisionBoundingBox.min.z);
         long maxBlockZ = MathUtil.fastFloor(zPos + this.collisionBoundingBox.max.z);
         int minChunkX = ChunkUtil.chunkCoordinate(minBlockX);
         int maxChunkX = ChunkUtil.chunkCoordinate(maxBlockX);
         int minChunkZ = ChunkUtil.chunkCoordinate(minBlockZ);
         int maxChunkZ = ChunkUtil.chunkCoordinate(maxBlockZ);
         boolean different = !this.footingBlocksValid;
         int blockIndex = 0;
         this.footingBlocksValid = true;

         label270:
         for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
               WorldChunk chunk = world.getChunkIfInMemory(ChunkUtil.indexChunk(chunkX, chunkZ));
               if (chunk == null) {
                  this.footingBlocksValid = false;
                  this.belowBlockType = null;
                  this.belowBlockTypeId = 0;
                  this.onGround = false;
                  break label270;
               }

               int minX = chunkX == minChunkX ? ChunkUtil.localCoordinate(minBlockX) : 0;
               int maxX = chunkX == maxChunkX ? ChunkUtil.localCoordinate(maxBlockX) : 31;
               int minZ = chunkZ == minChunkZ ? ChunkUtil.localCoordinate(minBlockZ) : 0;
               int maxZ = chunkZ == maxChunkZ ? ChunkUtil.localCoordinate(maxBlockZ) : 31;
               BlockSection chunkSection = chunk.getBlockChunk().getSectionAtBlockY(blockY);

               for (int x = minX; x <= maxX; x++) {
                  for (int z = minZ; z <= maxZ; z++) {
                     int block = chunkSection.get(x, blockY, z);
                     if (different || block != this.footingBlocks[blockIndex]) {
                        this.footingBlocks[blockIndex] = block;
                        this.footingFillers[blockIndex] = (short)chunkSection.getFiller(x, blockY, z);
                        this.footingRotations[blockIndex] = (byte)chunkSection.getRotationIndex(x, blockY, z);
                        different = true;
                     }

                     blockIndex++;
                  }
               }
            }
         }

         if (different && this.footingBlocksValid) {
            this.belowBlockType = null;
            this.onGround = false;
            this.belowBlockTypeId = 0;
            BoxBlockIntersectionEvaluator boxBlockIntersectionEvaluator = this.collisionResult.getBoxBlockIntersection();
            boxBlockIntersectionEvaluator.setBox(this.collisionBoundingBox, this.footingPosition);
            Vector3dc worldUp = boxBlockIntersectionEvaluator.getWorldUp();
            int horizontalOverlapMask;
            if (worldUp.y() != 0.0) {
               horizontalOverlapMask = 40;
            } else if (worldUp.x() != 0.0) {
               horizontalOverlapMask = 48;
            } else {
               horizontalOverlapMask = 24;
            }

            blockIndex = 0;

            label237:
            for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
               int chunkMinBlockX = ChunkUtil.minBlock(chunkX);

               for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                  int chunkMinBlockZ = ChunkUtil.minBlock(chunkZ);
                  int minX = chunkX == minChunkX ? ChunkUtil.localCoordinate(minBlockX) : 0;
                  int maxX = chunkX == maxChunkX ? ChunkUtil.localCoordinate(maxBlockX) : 31;
                  int minZ = chunkZ == minChunkZ ? ChunkUtil.localCoordinate(minBlockZ) : 0;
                  int maxZ = chunkZ == maxChunkZ ? ChunkUtil.localCoordinate(maxBlockZ) : 31;

                  for (int localX = minX; localX <= maxX; localX++) {
                     for (int localZ = minZ; localZ <= maxZ; localZ++) {
                        int rotation = this.footingRotations[blockIndex];
                        int filler = this.footingFillers[blockIndex];
                        int blockId = this.footingBlocks[blockIndex++];
                        if (blockId != 0) {
                           BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
                           int x = chunkMinBlockX + localX;
                           int z = chunkMinBlockZ + localZ;
                           int y = blockY;
                           if (filler != 0) {
                              x -= FillerBlockUtil.unpackX(filler);
                              y = blockY - FillerBlockUtil.unpackY(filler);
                              z -= FillerBlockUtil.unpackZ(filler);
                           }

                           BlockMaterial material = blockType.getMaterial();
                           if (material == BlockMaterial.Solid) {
                              BlockBoundingBoxes boundingBoxes = BlockBoundingBoxes.getAssetMap().getAsset(blockType.getHitboxTypeIndex());
                              BlockBoundingBoxes.RotatedVariantBoxes rotatedBoxes = boundingBoxes.get(rotation);
                              int code = boxBlockIntersectionEvaluator.intersectBoxComputeOnGround(rotatedBoxes.getBoundingBox(), x, y, z);
                              if (!CollisionMath.isDisjoint(code)) {
                                 if (rotatedBoxes.hasDetailBoxes()) {
                                    for (Box detailBox : rotatedBoxes.getDetailBoxes()) {
                                       code = boxBlockIntersectionEvaluator.intersectBoxComputeOnGround(detailBox, x, y, z);
                                       if (boxBlockIntersectionEvaluator.isOnGround() && (code & horizontalOverlapMask) == horizontalOverlapMask) {
                                          this.belowBlockType = blockType;
                                          this.belowBlockTypeId = blockId;
                                          this.onGround = true;
                                          break label237;
                                       }
                                    }
                                 } else if (boxBlockIntersectionEvaluator.isOnGround() && (code & horizontalOverlapMask) == horizontalOverlapMask) {
                                    this.belowBlockType = blockType;
                                    this.belowBlockTypeId = blockId;
                                    this.onGround = true;
                                    break label237;
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      } else {
         this.footingBlocksValid = false;
         this.belowBlockType = null;
         this.belowBlockTypeId = 0;
         this.onGround = false;
      }

      if (this.debugModeMove && wasOnGround != this.onGround) {
         LOGGER.at(Level.INFO).log("PostReadPosition: OnGround was changed from %s to %s", wasOnGround, this.onGround);
      }

      int xx = MathUtil.floor(this.position.x);
      int yx = MathUtil.floor(this.position.y + this.collisionBoundingBox.min.y);
      int zx = MathUtil.floor(this.position.z);
      TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      Ref<ChunkStore> chunkRef = transformComponent.getChunkRef();
      Fluid fluidType;
      if (chunkRef != null && chunkRef.isValid()) {
         Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
         WorldChunk worldChunkComponent = chunkStore.getComponent(chunkRef, WorldChunk.getComponentType());

         assert worldChunkComponent != null;

         fluidType = Fluid.getAssetMap().getAsset(worldChunkComponent.getFluidId(xx, yx, zx));
      } else {
         fluidType = null;
      }

      this.inWater = fluidType != null && !fluidType.equals(Fluid.EMPTY);
      this.horizontalSpeedMultiplier = 1.0;
      if (this.inWater) {
         int fxIndex = fluidType.getFluidFXIndex();
         if (fxIndex != 0) {
            FluidFX fx = FluidFX.getAssetMap().getAsset(fxIndex);
            this.horizontalSpeedMultiplier = fx != null && fx.getMovementSettings() != null ? fx.getMovementSettings().horizontalSpeedMultiplier : 1.0;
         }
      }

      if (chunkRef != null && chunkRef.isValid()) {
         PositionDataComponent positionDataComponent = componentAccessor.getComponent(ref, PositionDataComponent.getComponentType());

         assert positionDataComponent != null;

         Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
         BlockChunk blockChunkComponent = chunkStore.getComponent(chunkRef, BlockChunk.getComponentType());

         assert blockChunkComponent != null;

         positionDataComponent.setInsideBlockTypeId(blockChunkComponent.getBlock(xx, yx, zx));
         positionDataComponent.setStandingOnBlockTypeId(this.belowBlockTypeId);
      }
   }

   @Override
   public boolean translateToAccessiblePosition(
      @Nonnull Vector3d position, @Nullable Box boundingBox, double minYValue, double maxYValue, ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (maxYValue < minYValue) {
         return false;
      } else {
         if (minYValue < 0.0) {
            minYValue = 0.0;
         }

         if (maxYValue > 320.0) {
            maxYValue = 320.0;
         }

         if (boundingBox == null) {
            return this.translateToAccessiblePosition(position, minYValue, maxYValue, componentAccessor) > 0;
         } else {
            int minX = MathUtil.floor(boundingBox.min.x + position.x);
            int maxX = MathUtil.floor(boundingBox.max.x + position.x);
            int minZ = MathUtil.floor(boundingBox.min.z + position.z);
            int maxZ = MathUtil.floor(boundingBox.max.z + position.z);
            double originalY = position.y;
            double y = position.y + boundingBox.min.y;
            double resultY = -1.0;
            if (maxX - minX > 2 || maxZ - minZ > 2) {
               position.y = y;
               int retCode = this.translateToAccessiblePosition(position, minYValue, maxYValue, componentAccessor);
               if (retCode < 0) {
                  position.y = originalY;
                  return false;
               }

               if (retCode > 0 && position.y > resultY) {
                  resultY = position.y;
                  minYValue = resultY;
                  if (resultY > y) {
                     y = resultY;
                  }
               }
            }

            for (int x = minX; x <= maxX; x++) {
               for (int z = minZ; z <= maxZ; z++) {
                  position.y = y;
                  int retCodex = this.translateToAccessiblePosition(position, minYValue, maxYValue, componentAccessor);
                  if (retCodex < 0) {
                     position.y = originalY;
                     return false;
                  }

                  if (retCodex > 0 && position.y > resultY) {
                     resultY = position.y;
                     minYValue = resultY;
                     if (resultY > y) {
                        y = resultY;
                     }
                  }
               }
            }

            if (resultY < 0.0) {
               position.y = originalY;
               return false;
            } else {
               position.y = resultY - boundingBox.min.y;
               return true;
            }
         }
      }
   }

   public int translateToAccessiblePosition(
      @Nonnull Vector3d position, double minYValue, double maxYValue, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (position.y() < 0.0) {
         return -1;
      } else {
         World world = componentAccessor.getExternalData().getWorld();
         long chunkIndex = ChunkUtil.indexChunkFromBlock(position.x(), position.z());
         WorldChunk chunk = world.getChunkIfInMemory(chunkIndex);
         if (chunk == null) {
            return -1;
         } else {
            BlockChunk blockChunk = chunk.getBlockChunk();
            BlockTypeAssetMap<String, BlockType> assetMap = BlockType.getAssetMap();
            int x = MathUtil.floor(position.x());
            int y = MathUtil.floor(position.y());
            int z = MathUtil.floor(position.z());
            if (y < 320) {
               int blockId = chunk.getBlock(x, y, z);
               if (blockId != 0) {
                  BlockType blockType = assetMap.getAsset(blockId);
                  int filler = chunk.getFiller(x, y, z);
                  boolean isFiller = filler != 0;
                  if (isFiller || blockType.getMaterial() == BlockMaterial.Solid) {
                     int maxY;
                     for (maxY = MathUtil.ceil(maxYValue); y < maxY; y++) {
                        blockId = chunk.getBlock(x, y, z);
                        if (blockId == 0) {
                           break;
                        }

                        blockType = assetMap.getAsset(blockId);
                        filler = chunk.getFiller(x, y, z);
                        if (blockType.getMaterial() != BlockMaterial.Solid) {
                           break;
                        }
                     }

                     if (y == maxY) {
                        return 0;
                     } else {
                        blockType = chunk.getBlockType(x, --y, z);
                        int rotation = chunk.getRotationIndex(x, y, z);
                        BlockBoundingBoxes boxesAsset = BlockBoundingBoxes.getAssetMap().getAsset(blockType.getHitboxTypeIndex());
                        BlockBoundingBoxes.RotatedVariantBoxes rotatedBoxes = boxesAsset.get(rotation);
                        double top;
                        if (filler != 0) {
                           top = y - FillerBlockUtil.unpackY(filler) + rotatedBoxes.getBoundingBox().max.y;
                        } else {
                           top = y + rotatedBoxes.getBoundingBox().max.y;
                        }

                        if (top > maxYValue) {
                           return 0;
                        } else {
                           position.y = top;
                           return 1;
                        }
                     }
                  }
               }
            } else {
               y = 319;
            }

            int indexSection = ChunkUtil.indexSection(y);

            while (indexSection >= 0) {
               BlockSection chunkSection = blockChunk.getSectionAtIndex(indexSection);
               if (chunkSection.isSolidAir()) {
                  y = 32 * indexSection - 1;
                  if (y < minYValue) {
                     return 0;
                  }

                  indexSection--;
               } else {
                  int yBottom = 32 * indexSection--;

                  while (y >= yBottom) {
                     if (y < minYValue) {
                        return 0;
                     }

                     int rotationx = chunkSection.getRotationIndex(x, y, z);
                     int filler = chunkSection.getFiller(x, y, z);
                     int blockId = chunkSection.get(x, y--, z);
                     if (blockId != 0) {
                        BlockType blockType = assetMap.getAsset(blockId);
                        if (filler != 0) {
                           if (blockType.getMaterial() == BlockMaterial.Solid) {
                              BlockBoundingBoxes boundingBoxes = BlockBoundingBoxes.getAssetMap().getAsset(blockType.getHitboxTypeIndex());
                              BlockBoundingBoxes.RotatedVariantBoxes rotatedBoxesx = boundingBoxes.get(rotationx);
                              double topx = y + 1 - FillerBlockUtil.unpackY(filler) + rotatedBoxesx.getBoundingBox().max.y;
                              if (topx < minYValue) {
                                 return 0;
                              }

                              position.y = topx;
                              return 1;
                           }
                        } else if (blockType.getMaterial() == BlockMaterial.Solid) {
                           BlockBoundingBoxes boundingBoxes = BlockBoundingBoxes.getAssetMap().getAsset(blockType.getHitboxTypeIndex());
                           BlockBoundingBoxes.RotatedVariantBoxes rotatedBoxesx = boundingBoxes.get(rotationx);
                           double topx = y + 1 + rotatedBoxesx.getBoundingBox().max.y;
                           if (topx < minYValue) {
                              return 0;
                           }

                           position.y = topx;
                           return 1;
                        }
                     }
                  }
               }
            }

            return 0;
         }
      }
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
      NPCEntity npcComponent = componentAccessor.getComponent(ref, NPCEntity.getComponentType());

      assert npcComponent != null;

      this.saveMotionKind();
      this.isFullyRotated = true;
      Vector3d direction = steering.getTranslation();
      direction.y = 0.0;
      this.currentRelativeSpeed = direction.length();
      if (!steering.hasPitch()) {
         steering.setPitch(0.0F);
      }

      double maxVerticalSpeed = this.inWater() ? this.maxVerticalSpeedFluid : this.maxVerticalSpeed;
      double maxHorizontalSpeed = this.getMaximumSpeed();
      boolean isDead = !this.isAlive(ref, componentAccessor);
      if (isDead && !this.isForcePushed()) {
         this.climbUpDistance = 0.0;
         this.currentClimbForwardDistance = 0.0;
         this.maxClimbForwardDistance = 0.0;
         steering.setYaw(this.getYaw());
         if (this.onGround) {
            translation.zero();
            steering.setPitch(0.0F);
         } else {
            steering.setPitch(this.getPitch());
            Velocity velocityComponent = componentAccessor.getComponent(ref, Velocity.getComponentType());
            Vector3d velocity = velocityComponent.getVelocity();
            translation.set(velocity);
         }

         translation.y = this.computeNewFallSpeed(dt, translation.y);
         translation.mul(dt);
         this.validateTranslation(translation, "Death");
         return dt;
      } else if (this.isForcePushed()) {
         this.climbUpDistance = 0.0;
         this.currentClimbForwardDistance = 0.0;
         this.maxClimbForwardDistance = 0.0;
         translation.set(this.externalVelocity);

         for (int i = 0; i < this.appliedVelocities.size(); i++) {
            MotionControllerBase.AppliedVelocity entry = this.appliedVelocities.get(i);
            if (entry.velocity.y + this.externalVelocity.y <= 0.0 || entry.velocity.y < 0.0) {
               entry.canClear = true;
            }

            if (this.onGround && entry.canClear) {
               entry.velocity.y = 0.0;
            }

            translation.add(entry.velocity);
         }

         translation.mul(dt);
         if (!this.onGround || !(this.externalVelocity.y < 0.0)) {
            this.externalVelocity.y = this.computeNewFallSpeed(dt, this.externalVelocity.y);
         } else if (translation.y < 0.0) {
            translation.y = 0.0;
            this.externalVelocity.y = 0.0;
         }

         if (!this.appliedVelocity.equals(Vector3dUtil.ZERO)) {
            if (this.moveSpeed > 0.0) {
               float headingX = PhysicsMath.headingX(this.getYaw());
               float headingZ = PhysicsMath.headingZ(this.getYaw());
               double length2 = this.appliedVelocity.x * this.appliedVelocity.x + this.appliedVelocity.z * this.appliedVelocity.z;
               double multiplier = length2 > 0.0 ? (headingX * this.appliedVelocity.x + headingZ * this.appliedVelocity.z) / Math.sqrt(length2) : 0.0;
               multiplier = Math.min((multiplier + 1.0) / 2.0, this.maxWalkSpeedAfterHitMultiplier);
               this.moveSpeed *= multiplier;
               if (this.moveSpeed > this.minHorizontalSpeed) {
                  translation.add(this.moveSpeed * dt * headingX, 0.0, this.moveSpeed * dt * headingZ);
                  this.currentRelativeSpeed = this.moveSpeed;
               } else {
                  this.moveSpeed = 0.0;
               }
            }

            if (this.maxHover > 0.0) {
               npcComponent.setHoverHeight(translation.y <= 0.0 ? this.minHoverDrop : this.maxHover);
            }

            this.appliedVelocity.zero();
         }

         if (this.onGround && this.ignoreDamping) {
            double speed = this.externalVelocity.length() - dt * this.inertia * this.acceleration * 5.0;
            if (speed > 0.0) {
               this.externalVelocity.normalize(speed);
            } else {
               this.externalVelocity.zero();
            }
         }

         steering.setYaw(this.getYaw());
         this.validateTranslation(translation, "ExtForce");
         return dt;
      } else if (this.cachedMovementBlocked) {
         return dt;
      } else {
         float heading = this.getYaw();
         if (this.getMotionKind() == MotionKind.ASCENDING) {
            if (this.isRequiresPreciseMovement() && this.havePreciseMovementTarget) {
               double maxDistance = this.waypointDistance(this.preciseMovementTarget, this.position) + this.currentClimbForwardDistance;
               if (maxDistance < this.maxClimbForwardDistance) {
                  this.maxClimbForwardDistance = maxDistance;
               }
            }

            double distance = dt * this.climbSpeed;
            if (this.jumping) {
               if (this.climbUpDistance > 0.0) {
                  distance *= Math.max(Math.pow(this.climbUpDistance * this.climbUpDistance * this.jumpForce, this.jumpBlending), 1.0);
                  this.climbUpDistance = this.computeClimbMove(this.climbUpDirection, this.climbUpDistance, distance, translation);
               } else if (this.jumpDropHeight > 0.0) {
                  double jumpDiff = this.currentJumpHeight - this.jumpDropHeight;
                  distance *= Math.max(Math.pow(jumpDiff * jumpDiff * this.jumpDescentSteepness, this.jumpDescentBlending), 1.0);
                  this.jumpDropHeight = this.computeClimbMove(this.jumpDropDirection, this.jumpDropHeight, distance, translation);
               } else {
                  this.setMotionKind(MotionKind.DROPPING);
               }

               double heightAboveBlock = this.currentJumpHeight - this.climbUpDistance - translation.y;
               double moveDistance = 0.0;
               if (heightAboveBlock > this.jumpBlockHeight) {
                  double percentage;
                  if (this.climbUpDistance > 0.0) {
                     percentage = (heightAboveBlock - this.jumpBlockHeight) / heightAboveBlock / 2.0;
                  } else {
                     percentage = 0.5 + (1.0 - this.jumpDropHeight / (this.currentJumpHeight - this.jumpBlockHeight)) / 2.0;
                  }

                  double expectedDistance = this.maxClimbForwardDistance * percentage;
                  if (expectedDistance > this.maxClimbForwardDistance) {
                     expectedDistance = this.maxClimbForwardDistance;
                  }

                  moveDistance = expectedDistance - this.currentClimbForwardDistance;
                  if (moveDistance < 0.0) {
                     moveDistance = 0.0;
                  }

                  this.currentClimbForwardDistance = expectedDistance;
                  translation.add(this.climbForwardDirection.x * moveDistance, 0.0, this.climbForwardDirection.z * moveDistance);
               }

               if (this.isBlendingHeading) {
                  heading = this.computeBlendHeading(
                     heading,
                     NPCPhysicsMath.headingFromDirection(this.climbForwardDirection.x, this.climbForwardDirection.z, heading),
                     dt,
                     moveDistance / dt,
                     steering.getRelativeTurnSpeed()
                  );
               }

               this.lockOrientation(steering, translation, heading);
               this.fallSpeed = 0.0;
               this.onGround = false;
               this.validateTranslation(translation, "AscendingJump");
               return dt;
            } else {
               double moveDistancex;
               if (this.climbUpDistance > 0.0) {
                  double prevDistance = this.climbUpDistance;
                  this.climbUpDistance = this.computeClimbMove(this.climbUpDirection, prevDistance, distance, translation);
                  moveDistancex = prevDistance - this.climbUpDistance;
               } else if (this.currentClimbForwardDistance < this.maxClimbForwardDistance) {
                  double remaining = this.maxClimbForwardDistance - this.currentClimbForwardDistance;
                  if (distance < remaining) {
                     double newRemaining = remaining - distance;
                     moveDistancex = newRemaining <= 1.0E-5 ? remaining : distance;
                  } else {
                     moveDistancex = remaining;
                  }

                  this.currentClimbForwardDistance += moveDistancex;
                  translation.set(this.climbForwardDirection).mul(moveDistancex);
                  this.onGround = false;
               } else {
                  this.setMotionKind(MotionKind.DROPPING);
                  moveDistancex = 0.0;
               }

               if (this.isBlendingHeading) {
                  heading = this.computeBlendHeading(
                     heading,
                     NPCPhysicsMath.headingFromDirection(this.climbForwardDirection.x, this.climbForwardDirection.z, heading),
                     dt,
                     moveDistancex / dt,
                     steering.getRelativeTurnSpeed()
                  );
               }

               this.lockOrientation(steering, translation, heading);
               this.fallSpeed = 0.0;
               this.onGround = false;
               this.validateSpeeds(ref, "Ascending", componentAccessor);
               this.validateTranslation(translation, "Ascending");
               return dt;
            }
         } else if (this.getMotionKind() == MotionKind.DESCENDING) {
            double maxForwardSpeed = this.currentRelativeSpeed * maxHorizontalSpeed;
            if (this.isRequiresPreciseMovement() && this.havePreciseMovementTarget) {
               double maxForwardDistance = this.waypointDistance(this.position, this.preciseMovementTarget);
               if (maxForwardDistance < maxForwardSpeed * dt) {
                  maxForwardSpeed = maxForwardDistance / dt;
               }
            }

            this.fallSpeed = MathUtil.maxValue(0.0, NPCPhysicsMath.accelerateDragCapped(this.fallSpeed, 5.0 * this.gravity, dt, maxVerticalSpeed));
            this.moveSpeed = MathUtil.clamp(maxForwardSpeed, 0.0, this.moveSpeed + dt * this.acceleration);
            if (this.moveSpeed > maxHorizontalSpeed) {
               this.moveSpeed = maxHorizontalSpeed;
            }

            if (this.moveSpeed <= 0.3 * this.maxHorizontalSpeed) {
               this.setMotionKind(MotionKind.DROPPING);
            }

            double vertical = this.climbForwardDirection.y;
            if (this.predictedFallHeight > 0.0) {
               this.totalDropDistance = this.totalDropDistance + Math.abs(this.climbForwardDirection.y * this.moveSpeed * dt);
               double scaledDiff = Math.min(this.totalDropDistance / this.predictedFallHeight, 1.0);
               vertical *= Math.pow(this.descentSteepness, this.descentBlending) * Math.pow(scaledDiff, this.descentBlending);
            }

            translation.set(this.climbForwardDirection.x, vertical, this.climbForwardDirection.z);
            translation.mul(this.moveSpeed * dt);
            Vector3dUtil.clipToZero(translation, this.getEpsilonSpeed());
            if (this.isBlendingHeading) {
               heading = this.computeBlendHeading(
                  heading,
                  NPCPhysicsMath.headingFromDirection(this.climbForwardDirection.x, this.climbForwardDirection.z, heading),
                  dt,
                  this.moveSpeed,
                  steering.getRelativeTurnSpeed()
               );
            }

            this.lockOrientation(steering, translation, heading);
            this.validateSpeeds(ref, "Descending", componentAccessor);
            this.validateTranslation(translation, "Descending");
            return dt;
         } else {
            this.validateSpeeds(ref, "Enter Walk/Drop", componentAccessor);
            boolean canSteer = this.canSteer(ref, componentAccessor);
            boolean isBlendResting = false;
            float moveHeading;
            if (canSteer) {
               moveHeading = NPCPhysicsMath.headingFromDirection(direction.x, direction.z, heading);
               if (this.isRequiresPreciseMovement() && !this.isBlendingHeading) {
                  float turnAngle = NPCPhysicsMath.turnAngle(heading, moveHeading);
                  float epsilon = this.getEpsilonAngle();
                  if (turnAngle < -epsilon || turnAngle > epsilon) {
                     float maxRotation = (float)MathUtil.clamp(
                        dt * this.getCurrentMaxBodyRotationSpeed() * steering.getRelativeTurnSpeed(), 0.0, (float) (Math.PI / 2)
                     );
                     turnAngle = MathUtil.clamp(turnAngle, -maxRotation, maxRotation);
                     heading = PhysicsMath.normalizeTurnAngle(heading + turnAngle);
                     translation.set(0.0, 0.0, 0.0);
                     steering.setYaw(heading);
                     this.isFullyRotated = false;
                     return dt;
                  }

                  heading = moveHeading;
               }

               this.moveSpeed = MathUtil.clamp(this.currentRelativeSpeed * maxHorizontalSpeed, 0.0, this.moveSpeed + dt * this.acceleration);
               if (this.fallSpeed > 0.0) {
                  this.fallSpeed = 0.0;
               }

               if (this.moveSpeed < this.getEpsilonSpeed()) {
                  this.moveSpeed = 0.0;
               } else if (this.moveSpeed < this.minHorizontalSpeed) {
                  this.moveSpeed = this.minHorizontalSpeed;
               }

               if (this.isBlendingHeading) {
                  if (this.blendRestTurnAngle > 0.0F) {
                     float turnAngle = this.computeBlendTurnAngle(heading, moveHeading);
                     if (Math.abs(turnAngle) > this.blendRestTurnAngle) {
                        isBlendResting = true;
                     }

                     heading = this.computeBlendHeading(heading, moveHeading, dt, this.moveSpeed, turnAngle, steering.getRelativeTurnSpeed());
                  } else {
                     heading = this.computeBlendHeading(heading, moveHeading, dt, this.moveSpeed, steering.getRelativeTurnSpeed());
                  }

                  steering.setYaw(heading);
                  this.isFullyRotated = true;
               } else if (steering.hasYawOrDirection()) {
                  float yaw = steering.getYawOrDirection();
                  heading = this.computeHeading(yaw, steering.getRelativeTurnSpeed(), heading, dt, false, false);
                  steering.setYaw(heading);
               } else if (this.moveSpeed != 0.0) {
                  heading = this.computeHeading(moveHeading, steering.getRelativeTurnSpeed(), heading, dt, true, true);
                  moveHeading = heading;
                  steering.setYaw(heading);
               }

               if (this.debugModeSteer) {
                  LOGGER.at(Level.INFO)
                     .log(
                        "=== Compute = t =%.4f v =%.4f h =%.4f mh=%.4f",
                        dt,
                        this.moveSpeed,
                        (180.0F / (float)Math.PI) * heading,
                        (180.0F / (float)Math.PI) * moveHeading
                     );
               }
            } else {
               Velocity velocityComponent = componentAccessor.getComponent(ref, Velocity.getComponentType());
               Vector3d velocity = velocityComponent.getVelocity();
               moveHeading = NPCPhysicsMath.headingFromDirection(velocity.x, velocity.z, heading);
               if (!steering.hasYawOrDirection()) {
                  steering.setYaw(heading);
               }

               if (this.maxHover > 0.0 && this.floatsDown) {
                  this.fallSpeed = this.climbSpeedConst;
                  if (this.fallSpeed != 0.0 && this.climbSpeedMult != 0.0) {
                     double prevFallSpeed = this.fallSpeed;
                     double deltaFallSpeed = this.climbSpeedMult * Math.pow(prevFallSpeed, this.climbSpeedPow);
                     this.fallSpeed += deltaFallSpeed;
                  }
               } else {
                  double prevFallSpeed = this.fallSpeed;
                  this.fallSpeed = -NPCPhysicsMath.gravityDrag(-prevFallSpeed, 5.0 * this.gravity, dt, maxVerticalSpeed);
               }
            }

            if (this.moveSpeed > maxHorizontalSpeed) {
               this.moveSpeed = maxHorizontalSpeed;
            }

            if (this.fallSpeed > maxVerticalSpeed) {
               this.fallSpeed = maxVerticalSpeed;
            } else if (this.fallSpeed < -maxVerticalSpeed) {
               this.fallSpeed = -maxVerticalSpeed;
            }

            double appliedSpeed = this.moveSpeed;
            if (isBlendResting) {
               double maxSpeed = this.blendRestRelativeSpeed * this.getMaximumSpeed();
               if (appliedSpeed > maxSpeed) {
                  appliedSpeed = maxSpeed;
               }
            }

            translation.x = appliedSpeed * dt * PhysicsMath.headingX(moveHeading);
            translation.z = appliedSpeed * dt * PhysicsMath.headingZ(moveHeading);
            translation.y = -this.fallSpeed * dt;
            double maxDistance = steering.getMaxDistance();
            if (this.canSteer(ref, componentAccessor) && maxDistance < Double.MAX_VALUE && maxDistance > 0.0) {
               double lenSquared = NPCPhysicsMath.dotProduct(translation.x, translation.y, translation.z, this.getComponentSelector());
               double len = Math.sqrt(lenSquared);
               if (len > maxDistance) {
                  translation.mul(maxDistance / len);
               }
            }

            this.validateSpeeds(ref, canSteer ? "Moving" : "Falling", componentAccessor);
            this.validateTranslation(translation, canSteer ? "Moving" : "Falling");
            return dt;
         }
      }
   }

   private double computeNewFallSpeed(double dt, double fallSpeed) {
      Box hitbox = this.collisionBoundingBox;
      int invertedGravityModifier = this.movementSettings.invertedGravity ? 1 : -1;
      double terminalVelocity = invertedGravityModifier
         * PhysicsMath.getTerminalVelocity(
            this.movementSettings.mass,
            0.001225,
            Math.abs((hitbox.max.x - hitbox.min.x) * (hitbox.max.z - hitbox.min.z)),
            this.movementSettings.dragCoefficient
         );
      double gravityStep = invertedGravityModifier * PhysicsMath.getAcceleration(fallSpeed, terminalVelocity) * dt;
      if (fallSpeed < terminalVelocity && gravityStep > 0.0) {
         fallSpeed = Math.min(fallSpeed + gravityStep, terminalVelocity);
      } else if (fallSpeed > terminalVelocity && gravityStep < 0.0) {
         fallSpeed = Math.max(fallSpeed + gravityStep, terminalVelocity);
      }

      return fallSpeed;
   }

   private boolean findMoveCollisions(
      int walkingMaterials, boolean avoidDamage, @Nonnull Vector3d translation, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      this.collisionResult.setCollisionByMaterial(4, walkingMaterials);
      this.resetObstructedFlags();
      if (this.debugModeBlockCollisions) {
         this.collisionResult.setLogger(LOGGER);
      }

      boolean oldState = this.collisionResult.setDamageBlocking(avoidDamage);
      boolean shortMove = !CollisionModule.findCollisions(this.collisionBoundingBox, this.position, translation, false, this.collisionResult, componentAccessor);
      this.collisionResult.setDamageBlocking(oldState);
      if (this.debugModeBlockCollisions) {
         this.collisionResult.setLogger(null);
      }

      if (this.debugModeCollisions) {
         this.dumpCollisionResults();
      }

      return shortMove;
   }

   private double finishPhysicsCollision(
      @Nonnull Ref<EntityStore> ref, double dt, double triggerScale, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      this.processTriggers(ref, this.collisionResult, triggerScale, componentAccessor);
      dt *= triggerScale;
      if (this.debugModeValidatePositions && !this.isValidPosition(this.position, this.collisionResult, componentAccessor)) {
         LOGGER.at(Level.WARNING).log("Move: Physics moved to invalid position pos=%s/%s/%s", this.position.x, this.position.y, this.position.z);
      }

      return dt;
   }

   private double executePhysicsMove(
      @Nonnull Ref<EntityStore> ref, double dt, @Nonnull Vector3d translation, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      this.findMoveCollisions(13, false, translation, componentAccessor);
      BlockCollisionData collision = this.collisionResult.getFirstBlockCollision();
      this.lastValidPosition.set(this.position);
      boolean wasOnGround = this.onGround;
      if (collision == null) {
         double startSlide;
         double endSlide;
         if (this.collisionResult.isSliding) {
            startSlide = this.collisionResult.slideStart;
            endSlide = this.collisionResult.slideEnd;
         } else {
            startSlide = Double.MAX_VALUE;
            endSlide = Double.MAX_VALUE;
         }

         this.position.add(translation);
         this.onGround = startSlide <= 1.0 && endSlide >= 1.0;
         double triggerScale = 1.0;
         if (this.onGround) {
            this.setMotionKind(MotionKind.STANDING);
         } else {
            this.setMotionKind(MotionKind.DROPPING);
            if (wasOnGround) {
               this.fallStartHeight = this.position.y;
               this.fallSpeed = 0.0;
            }
         }

         if (!this.isValidPosition(this.position, this.collisionResult, componentAccessor)) {
            double scale = this.bisect(this.lastValidPosition, this.position, this.position, componentAccessor);
            triggerScale *= scale;
            if (this.debugModeMove) {
               LOGGER.at(Level.INFO)
                  .log(
                     "Move: Physics no-collision bisect scale=%s newpos=%s state=%s",
                     scale,
                     Vector3dUtil.formatShortString(this.position),
                     this.getMotionKind()
                  );
            }

            if (scale == 0.0) {
               this.isObstructed = true;
            }
         } else if (this.debugModeMove) {
            LOGGER.at(Level.INFO)
               .log(
                  "Move: Physics no-collision onGround=%s newpos=%s state=%s",
                  this.onGround,
                  Vector3dUtil.formatShortString(this.position),
                  this.getMotionKind()
               );
         }

         this.processTriggers(ref, this.collisionResult, triggerScale, componentAccessor);
         if (this.debugModeValidatePositions && !this.isValidPosition(this.position, this.collisionResult, componentAccessor)) {
            LOGGER.at(Level.WARNING).log("Move: Physics moved to invalid position pos=%s/%s/%s", this.position.x, this.position.y, this.position.z);
         }

         return dt;
      } else {
         double triggerScalex = collision.collisionStart;
         this.position.set(collision.collisionPoint);
         this.tmpMovePosition.set(this.lastValidPosition).add(translation).sub(collision.collisionPoint);
         if (!this.tmpMovePosition.equals(Vector3dUtil.ZERO)) {
            double t = this.tmpMovePosition.dot(collision.collisionNormal);
            this.tmpMovePosition.fma(-t, collision.collisionNormal);
            this.position.add(this.tmpMovePosition);
         }

         if (!this.isValidPosition(this.position, this.collisionResult, componentAccessor)) {
            double scalex = this.bisect(this.lastValidPosition, this.position, this.position, componentAccessor);
            triggerScalex *= scalex;
            if (this.debugModeMove) {
               LOGGER.at(Level.INFO).log("Move: Physics collision bisect=%s triggerScale=%s", scalex, triggerScalex);
            }
         }

         if (!this.isForcePushed()) {
            if (collision.collisionNormal.equals(this.getWorldNormal())) {
               this.setMotionKind(MotionKind.STANDING);
               if (!this.onGround) {
                  this.onGround = true;
                  this.postReadPosition(ref, componentAccessor);
               }

               this.fallSpeed = 0.0;
               if (this.debugModeMove) {
                  LOGGER.at(Level.INFO)
                     .log(
                        "Move: Physics ground collision onGround=%s newpos=%s state=%s",
                        this.onGround,
                        Vector3dUtil.formatShortString(this.position),
                        this.getMotionKind()
                     );
               }

               return this.finishPhysicsCollision(ref, dt, triggerScalex, componentAccessor);
            } else if (collision.collisionNormal.equals(this.getWorldAntiNormal())) {
               this.fallSpeed = 0.0;
               this.setMotionKind(MotionKind.DROPPING);
               if (this.debugModeMove) {
                  LOGGER.at(Level.INFO)
                     .log("Move: Physics ceiling collision newpos=%s state=%s", Vector3dUtil.formatShortString(this.position), this.getMotionKind());
               }

               return this.finishPhysicsCollision(ref, dt, triggerScalex, componentAccessor);
            } else {
               this.isObstructed = true;
               this.setMotionKind(MotionKind.STANDING);
               if (this.debugModeMove) {
                  LOGGER.at(Level.INFO)
                     .log("Move: Physics wall collision newpos=%s state=%s", Vector3dUtil.formatShortString(this.position), this.getMotionKind());
               }

               return this.finishPhysicsCollision(ref, dt, triggerScalex, componentAccessor);
            }
         } else {
            if (this.ignoreDamping) {
               this.ignoreDamping = false;
               this.clearExternalVelocity();
            }

            int count = this.collisionResult.getBlockCollisionCount();

            for (int i = 0; i < count; i++) {
               BlockCollisionData c = this.collisionResult.getBlockCollision(i);
               if (c.collisionNormal.equals(this.getWorldNormal())) {
                  this.onGround = true;
                  this.fallSpeed = 0.0;
                  break;
               }
            }

            this.setMotionKind(MotionKind.DROPPING);
            if (collision.collisionNormal.equals(this.getWorldAntiNormal())) {
               this.fallSpeed = 0.0;
            }

            if (this.debugModeMove) {
               LOGGER.at(Level.INFO)
                  .log(
                     "Move: Physics force-push collision normal=%s onGround=%s newpos=%s state=%s",
                     collision.collisionNormal,
                     this.onGround,
                     Vector3dUtil.formatShortString(this.position),
                     this.getMotionKind()
                  );
            }

            return this.finishPhysicsCollision(ref, dt, triggerScalex, componentAccessor);
         }
      }
   }

   @Override
   protected double executeMove(
      @Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Vector3d translation, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (this.debugModeMove) {
         LOGGER.at(Level.INFO)
            .log(
               "Move: Execute pos=%s vel=%s onGround=%s blocked=%s solidGround=%s avdDmg=%s relax=%s",
               Vector3dUtil.formatShortString(this.position),
               Vector3dUtil.formatShortString(translation),
               this.onGround,
               this.isObstructed,
               this.isOnSolidGround(),
               this.isAvoidingBlockDamage(),
               !this.getRelaxedConstraints().isEmpty()
            );
      }

      if (this.isAlive(ref, componentAccessor) && !this.isForcePushed()) {
         this.effectiveMoveConstraints.clear();
         this.effectiveMoveConstraints.addAll(this.relaxedConstraints);
         if (!this.isOnSolidGround()) {
            this.effectiveMoveConstraints.add(RelaxedConstraint.DAMAGE);
            this.effectiveMoveConstraints.add(RelaxedConstraint.WADE);
         }

         if (this.inWater) {
            this.effectiveMoveConstraints.add(RelaxedConstraint.WADE);
         }

         if (!role.couldBreatheCached()) {
            this.effectiveMoveConstraints.add(RelaxedConstraint.BREATHE);
         }

         boolean avoidDamage = !this.effectiveMoveConstraints.contains(RelaxedConstraint.DAMAGE);
         boolean allowWade = this.effectiveMoveConstraints.contains(RelaxedConstraint.WADE);
         double effectiveDropHeight = this.effectiveMaxDropHeight(this.effectiveMoveConstraints);
         boolean shortMove = this.findMoveCollisions(allowWade ? 13 : 5, avoidDamage, translation, componentAccessor);
         BlockCollisionData collision = this.collisionResult.getFirstBlockCollision();
         double startSlide;
         double endSlide;
         if (this.collisionResult.isSliding) {
            startSlide = this.collisionResult.slideStart;
            endSlide = this.collisionResult.slideEnd;
            if (this.onGround) {
               collision = this.discardIgnorableSlideCollisions(this.collisionResult, collision, this.effectiveMoveConstraints);
            }
         } else {
            startSlide = Double.MAX_VALUE;
            endSlide = Double.MAX_VALUE;
         }

         boolean needsRotation = this.isRequiresPreciseMovement() && !this.isFullyRotated;
         this.lastValidPosition.set(this.position);
         boolean wasOnGround = this.onGround;
         if (collision == null) {
            double triggerScale;
            if (wasOnGround && !(endSlide >= 1.0)) {
               if (this.isOnSolidGround() && needsRotation) {
                  this.isObstructed = false;
                  endSlide = this.shortenSlide(translation, endSlide);
               } else {
                  this.onGround = false;
                  this.tmpMovePosition.set(this.position).fma(endSlide, translation);
                  if (this.isDropBlocked(this.tmpMovePosition, effectiveDropHeight, false, this.effectiveMoveConstraints, componentAccessor)) {
                     ChunkStore chunkStore = componentAccessor.getExternalData().getWorld().getChunkStore();
                     if (this.isValidWalkPosition(
                        chunkStore, this.lastValidPosition.x, this.lastValidPosition.y, this.lastValidPosition.z, this.effectiveMoveConstraints
                     )) {
                        endSlide = this.shortenSlide(translation, endSlide);
                        this.isObstructed = true;
                        this.onGround = true;
                     }
                  }
               }

               this.position.fma(endSlide, translation);
               triggerScale = endSlide;
               dt *= endSlide;
               if (this.onGround && this.validateGroundPosition(effectiveDropHeight, this.effectiveMoveConstraints, componentAccessor)) {
                  triggerScale = 0.0;
               }
            } else {
               this.position.add(translation);
               this.onGround = startSlide <= 1.0 && endSlide >= 1.0;
               triggerScale = 1.0;
               if (wasOnGround && this.onGround) {
                  ChunkStore chunkStore = componentAccessor.getExternalData().getWorld().getChunkStore();
                  if (!this.isValidWalkPosition(chunkStore, this.position.x, this.position.y, this.position.z, this.effectiveMoveConstraints)
                     && this.isValidWalkPosition(
                        chunkStore, this.lastValidPosition.x, this.lastValidPosition.y, this.lastValidPosition.z, this.effectiveMoveConstraints
                     )) {
                     double scale = this.bisect(
                        this.lastValidPosition,
                        this.position,
                        this,
                        (_this, pos) -> _this.isValidWalkPosition(chunkStore, pos.x, pos.y, pos.z, this.effectiveMoveConstraints),
                        this.position
                     );
                     triggerScale *= scale;
                     if (scale == 0.0) {
                        this.isObstructed = true;
                     }
                  }
               }
            }

            if (this.getMotionKind() != MotionKind.ASCENDING) {
               if (this.onGround) {
                  this.setMotionKind(
                     !this.isObstructed && NPCPhysicsMath.projectedLengthSquared(translation, this.getComponentSelector()) > 0.0
                        ? MotionKind.MOVING
                        : MotionKind.STANDING
                  );
               } else if (this.initiateDescend(translation, wasOnGround, "No collision", componentAccessor)) {
                  this.position.set(this.lastValidPosition);
                  this.moveSpeed = 0.0;
                  triggerScale = 0.0;
               }
            }

            if (!this.isValidPosition(this.position, this.collisionResult, componentAccessor)) {
               if (this.getMotionKind() == MotionKind.DESCENDING) {
                  this.setMotionKind(MotionKind.DROPPING);
               }

               double scale = this.bisect(this.lastValidPosition, this.position, this.position, componentAccessor);
               triggerScale *= scale;
               if (this.debugModeMove) {
                  LOGGER.at(Level.INFO)
                     .log(
                        "Move: No collision, bisect onGround was/is=%s/%s slide=%s/%s scale=%s newpos=%s state=%s blocked=%s",
                        wasOnGround,
                        this.onGround,
                        startSlide,
                        endSlide,
                        scale,
                        Vector3dUtil.formatShortString(this.position),
                        this.getMotionKind(),
                        this.isObstructed
                     );
               }

               if (scale == 0.0) {
                  this.isObstructed = true;
               }
            } else if (this.debugModeMove) {
               LOGGER.at(Level.INFO)
                  .log(
                     "Move: No collision onGround was/is=%s/%s slide=%s/%s newpos=%s state=%s",
                     wasOnGround,
                     this.onGround,
                     startSlide,
                     endSlide,
                     Vector3dUtil.formatShortString(this.position),
                     this.getMotionKind()
                  );
            }

            this.processTriggers(ref, this.collisionResult, triggerScale, componentAccessor);
            if (this.debugModeValidatePositions && !this.isValidPosition(this.position, this.collisionResult, componentAccessor)) {
               LOGGER.at(Level.WARNING)
                  .log(
                     "Move: Walked on invalid position pos=%s/%s/%s overlaps=%s",
                     this.position.x,
                     this.position.y,
                     this.position.z,
                     this.collisionResult.getBlockCollisionCount()
                  );
            }

            return dt;
         } else if (this.debugModeValidatePositions && !this.isValidPosition(this.position, this.collisionResult, componentAccessor)) {
            throw new IllegalStateException("Invalid position");
         } else {
            double triggerScalex = collision.collisionStart;
            this.position.set(collision.collisionPoint);
            if (!this.isValidPosition(this.position, this.collisionResult, componentAccessor)) {
               double scalex = this.bisect(this.lastValidPosition, this.position, this.position, componentAccessor);
               triggerScalex *= scalex;
               if (this.debugModeMove) {
                  LOGGER.at(Level.INFO).log("Move: Collision bisect=%s triggerScale=%s", scalex, triggerScalex);
               }
            }

            if (collision.collisionNormal.equals(this.getWorldNormal())) {
               if (this.onGround && !this.isDropBlocked(this.position, effectiveDropHeight, false, this.effectiveMoveConstraints, componentAccessor)) {
                  this.initiateDescend(translation, true, "Collision", componentAccessor);
                  this.onGround = false;
               } else {
                  if (this.onGround) {
                     this.isObstructed = true;
                     if (avoidDamage && collision.willDamage) {
                        triggerScalex = this.shortenMovement(triggerScalex);
                     }
                  } else if (this.getMotionKind() == MotionKind.DROPPING) {
                     double fallHeight = this.fallStartHeight - this.position.y;
                     if (fallHeight >= this.maxDropHeight) {
                        this.moveSpeed = 0.0;
                     } else if (fallHeight > this.maxClimbHeight) {
                        this.moveSpeed = this.moveSpeed * ((fallHeight - this.maxClimbHeight) / (this.maxDropHeight - this.maxClimbHeight));
                        this.validateSpeeds(ref, "Collision on Ground", componentAccessor);
                     }
                  }

                  this.setMotionKind(MotionKind.STANDING);
                  if (this.debugModeMove) {
                     LOGGER.at(Level.INFO)
                        .log(
                           "Move: Collision Up onGround is/was=%s/%s blocked=%s newpos=%s state=%s",
                           this.onGround,
                           wasOnGround,
                           this.isObstructed,
                           Vector3dUtil.formatShortString(this.position),
                           this.getMotionKind()
                        );
                  }

                  if (!this.onGround) {
                     this.onGround = true;
                     this.postReadPosition(ref, componentAccessor);
                  }
               }

               if (this.onGround && this.validateGroundPosition(effectiveDropHeight, this.effectiveMoveConstraints, componentAccessor)) {
                  triggerScalex = 0.0;
               }

               this.processTriggers(ref, this.collisionResult, triggerScalex, componentAccessor);
               dt *= triggerScalex;
               if (this.debugModeValidatePositions && !this.isValidPosition(this.position, this.collisionResult, componentAccessor)) {
                  LOGGER.at(Level.WARNING)
                     .log(
                        "Move: Walked on invalid position pos=%s/%s/%s overlaps=%s",
                        this.position.x,
                        this.position.y,
                        this.position.z,
                        this.collisionResult.getBlockCollisionCount()
                     );
               }

               return dt;
            } else if (collision.collisionNormal.equals(this.getWorldAntiNormal())) {
               this.fallSpeed = 0.0;
               this.setMotionKind(MotionKind.DROPPING);
               if (this.debugModeMove) {
                  LOGGER.at(Level.INFO)
                     .log(
                        "Move: No ext force, collision down, clear vert speed newpos=%s state=%s",
                        Vector3dUtil.formatShortString(this.position),
                        this.getMotionKind()
                     );
               }

               if (this.onGround && this.validateGroundPosition(effectiveDropHeight, this.effectiveMoveConstraints, componentAccessor)) {
                  triggerScalex = 0.0;
               }

               this.processTriggers(ref, this.collisionResult, triggerScalex, componentAccessor);
               dt *= triggerScalex;
               if (this.debugModeValidatePositions && !this.isValidPosition(this.position, this.collisionResult, componentAccessor)) {
                  LOGGER.at(Level.WARNING)
                     .log(
                        "Move: Walked on invalid position pos=%s/%s/%s overlaps=%s",
                        this.position.x,
                        this.position.y,
                        this.position.z,
                        this.collisionResult.getBlockCollisionCount()
                     );
               }

               return dt;
            } else {
               boolean tryClimb;
               if (!shortMove
                  && !needsRotation
                  && this.isOnSolidGround()
                  && collision.blockType != null
                  && this.isClimbable(collision.blockType, collision.fluid, this.effectiveMoveConstraints)) {
                  tryClimb = this.tryClimb(translation, this.effectiveMoveConstraints, componentAccessor);
                  if (this.debugModeMove) {
                     LOGGER.at(Level.INFO)
                        .log(
                           "Move: No ext force, collision horz, try climb h=%s succ=%s newpos(succ|fail)=%s|%s state=%s",
                           this.climbUpDistance,
                           tryClimb,
                           Vector3dUtil.formatShortString(this.tmpMovePosition),
                           Vector3dUtil.formatShortString(this.position),
                           this.getMotionKind()
                        );
                  }
               } else {
                  tryClimb = false;
                  if (avoidDamage && collision.willDamage) {
                     triggerScalex = this.shortenMovement(triggerScalex);
                  }

                  if (this.debugModeMove) {
                     LOGGER.at(Level.INFO)
                        .log(
                           "Move: No ext force, collision horz, don't try climb onGround %s, block %s newpos=%s state=%s",
                           this.onGround,
                           collision.blockType != null,
                           Vector3dUtil.formatShortString(this.position),
                           this.getMotionKind()
                        );
                  }
               }

               this.isObstructed = this.isFullyRotated && !tryClimb && !shortMove;
               if (this.onGround && this.validateGroundPosition(effectiveDropHeight, this.effectiveMoveConstraints, componentAccessor)) {
                  tryClimb = false;
                  triggerScalex = 0.0;
               }

               this.processTriggers(ref, this.collisionResult, triggerScalex, componentAccessor);
               dt *= triggerScalex;
               if (tryClimb && !this.isProcessTriggersHasMoved()) {
                  this.setMotionKind(MotionKind.ASCENDING);
                  this.climbUpDirection.set(this.getWorldNormal());
                  this.climbForwardDirection.set(translation).normalize();
                  this.climbSpeed = this.computeClimbSpeed(this.moveSpeed);
                  this.onGround = false;
                  if (this.debugModeMove) {
                     LOGGER.at(Level.INFO)
                        .log(
                           "Move: No ext force, collision horz, start climbing h=%s forw=%s state=%s",
                           this.climbUpDistance,
                           this.maxClimbForwardDistance,
                           this.getMotionKind()
                        );
                  }

                  if (this.debugModeValidatePositions && !this.isValidPosition(this.position, this.collisionResult, componentAccessor)) {
                     throw new IllegalStateException("Invalid position");
                  }
               } else if (role.getLastAvoidanceSteering().lengthSquared() < 1.0E-5 && role.getLastSeparationSteering().lengthSquared() < 1.0E-5) {
                  this.tmpMovePosition.set(this.lastValidPosition).add(translation).sub(collision.collisionPoint);
                  if (!this.tmpMovePosition.equals(Vector3dUtil.ZERO)) {
                     double t = this.tmpMovePosition.dot(collision.collisionNormal);
                     this.tmpMovePosition.fma(-t, collision.collisionNormal);
                     this.position.add(this.tmpMovePosition);
                  }

                  if (this.debugModeMove) {
                     LOGGER.at(Level.INFO)
                        .log(
                           "Move: No ext force, collision horz, sliding at wall h=%s forw=%s state=%s",
                           Vector3dUtil.formatShortString(this.tmpMovePosition),
                           this.getMotionKind()
                        );
                  }

                  if (!this.isValidPosition(this.position, this.collisionResult, componentAccessor)) {
                     double scalex = this.bisect(this.lastValidPosition, this.position, this.position, componentAccessor);
                     if (this.debugModeMove) {
                        LOGGER.at(Level.INFO).log("Move: Collision slide bisect=%s dt=%s", scalex, dt);
                     }
                  }

                  if (this.lastValidPosition.distanceSquared(this.position) > this.getEpsilonSpeed()) {
                     this.setMotionKind(MotionKind.MOVING);
                  } else {
                     this.setMotionKind(MotionKind.STANDING);
                  }
               } else {
                  if (this.debugModeMove) {
                     LOGGER.at(Level.INFO)
                        .log(
                           "Move: No ext force, collision horz, no sliding at wall, avoidance/separation, state=%s",
                           Vector3dUtil.formatShortString(this.tmpMovePosition),
                           this.getMotionKind()
                        );
                  }

                  this.setMotionKind(MotionKind.STANDING);
               }

               if (this.debugModeValidatePositions && !this.isValidPosition(this.position, this.collisionResult, componentAccessor)) {
                  LOGGER.at(Level.WARNING)
                     .log(
                        "Move: Walked on invalid position pos=%s/%s/%s overlaps=%s",
                        this.position.x,
                        this.position.y,
                        this.position.z,
                        this.collisionResult.getBlockCollisionCount()
                     );
               }

               return dt;
            }
         }
      } else {
         return this.executePhysicsMove(ref, dt, translation, componentAccessor);
      }
   }

   @Override
   public double probeMove(@Nonnull Ref<EntityStore> ref, @Nonnull ProbeMoveData probeMoveData, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      World world = componentAccessor.getExternalData().getWorld();
      boolean saveSegments = probeMoveData.startProbing();
      Vector3d probeMovement = probeMoveData.probeDirection;
      double distanceLeftSquared = NPCPhysicsMath.projectedLengthSquared(probeMovement, this.getComponentSelector());
      if (distanceLeftSquared == 0.0) {
         return 0.0;
      } else {
         EnumSet<RelaxedConstraint> probeConstraints = probeMoveData.getRelaxedConstraints();
         boolean avoidDamage = !probeConstraints.contains(RelaxedConstraint.DAMAGE);
         boolean allowWade = probeConstraints.contains(RelaxedConstraint.WADE);
         double effectiveDropHeight = this.effectiveMaxDropHeight(probeConstraints);
         this.collisionResult.setCollisionByMaterial(4, allowWade ? 13 : 5);
         Predicate<CollisionConfig> previousBlockCollisionFilter = this.collisionResult.setBlockCollisionFilter(probeMoveData.getBlockCollisionFilter());

         try {
            Vector3d probePosition = probeMoveData.probePosition;
            Vector3d initialPosition = probeMoveData.initialPosition;
            Vector3d targetPosition = probeMoveData.targetPosition;
            Vector3d directionComponentSelector = probeMoveData.directionComponentSelector;
            CollisionModule collisionModule = CollisionModule.get();
            boolean onGround;
            if (initialPosition.equals(this.position)) {
               onGround = this.onGround;
            } else {
               onGround = collisionModule.validatePosition(world, this.collisionBoundingBox, probePosition, this.collisionResult) == 1;
            }

            directionComponentSelector.set(this.getComponentSelector());
            probeMovement.mul(directionComponentSelector);
            if (saveSegments) {
               probeMoveData.addStartSegment(initialPosition, true);
            }

            if (!onGround) {
               if (this.isDropBlocked(probePosition, effectiveDropHeight, true, probeConstraints, componentAccessor)) {
                  if (!saveSegments) {
                     probeMoveData.edgeBlocked = true;
                     return this.waypointDistance(initialPosition, probePosition);
                  }

                  probeMoveData.addBlockedDropSegment(probePosition, this.waypointDistance(initialPosition, probePosition));
                  return probeMoveData.getLastDistance();
               }

               if (saveSegments) {
                  probeMoveData.addDropSegment(probePosition, this.waypointDistance(initialPosition, probePosition));
               }
            }

            while (distanceLeftSquared > 0.0) {
               if (this.debugModeProbeBlockCollisions || probeMoveData.debugCollision) {
                  this.collisionResult.setLogger(LOGGER);
               }

               boolean oldState = this.collisionResult.setDamageBlocking(avoidDamage);
               boolean shortMove = !CollisionModule.findCollisions(
                  this.collisionBoundingBox, probePosition, probeMovement, this.collisionResult, componentAccessor
               );
               this.collisionResult.setDamageBlocking(oldState);
               if (this.debugModeProbeBlockCollisions || probeMoveData.debugCollision) {
                  this.collisionResult.setLogger(null);
               }

               if (this.debugModeMove) {
                  LOGGER.at(Level.INFO)
                     .log(
                        "Probe Step: pos=%s mov=%s left=%s",
                        Vector3dUtil.formatShortString(probePosition),
                        Vector3dUtil.formatShortString(probeMovement),
                        Math.sqrt(distanceLeftSquared)
                     );
               }

               if (this.debugModeCollisions) {
                  this.dumpCollisionResults();
               }

               BlockCollisionData collision = this.collisionResult.getFirstBlockCollision();
               double endSlide = this.collisionResult.isSliding ? this.collisionResult.slideEnd : Double.MAX_VALUE;
               if (this.collisionResult.isSliding) {
                  collision = this.discardIgnorableSlideCollisions(this.collisionResult, collision, probeConstraints);
               }

               if (collision == null || !(collision.collisionStart <= endSlide)) {
                  if (endSlide >= 1.0) {
                     probePosition.add(probeMovement);
                     if (!allowWade) {
                        ChunkStore chunkStore = componentAccessor.getExternalData().getWorld().getChunkStore();
                        if (!this.isValidWalkPosition(chunkStore, probePosition.x, probePosition.y, probePosition.z, probeConstraints)
                           && this.isValidWalkPosition(chunkStore, initialPosition.x, initialPosition.y, initialPosition.z, probeConstraints)) {
                           probePosition.sub(probeMovement);
                           if (saveSegments) {
                              probeMoveData.addBlockedGroundSegment(
                                 probePosition, this.waypointDistance(initialPosition, probePosition), this.getWorldNormal(), -1
                              );
                           }

                           return this.waypointDistance(initialPosition, probePosition);
                        }
                     }

                     probeMovement.set(Vector3dUtil.ZERO);
                     double distance = this.waypointDistance(initialPosition, probePosition);
                     if (saveSegments) {
                        probeMoveData.addMoveSegment(probePosition, true, distance);
                     }

                     return distance;
                  }

                  probePosition.fma(endSlide, probeMovement);
                  if (saveSegments) {
                     probeMoveData.addHitEdgeSegment(probePosition, this.waypointDistance(initialPosition, probePosition));
                  }

                  if (this.isDropBlocked(probePosition, effectiveDropHeight, true, probeConstraints, componentAccessor)) {
                     ChunkStore chunkStore = componentAccessor.getExternalData().getWorld().getChunkStore();
                     if (allowWade || this.isValidWalkPosition(chunkStore, initialPosition.x, initialPosition.y, initialPosition.z, probeConstraints)) {
                        probeMovement.set(targetPosition).sub(probePosition).mul(directionComponentSelector);
                        if (!saveSegments) {
                           probeMoveData.edgeBlocked = true;
                           return this.waypointDistance(initialPosition, probePosition);
                        }

                        probeMoveData.changeSegmentToBlockedEdge();
                        return probeMoveData.getLastDistance();
                     }
                  }

                  if (saveSegments) {
                     probeMoveData.addDropSegment(probePosition, this.waypointDistance(initialPosition, probePosition));
                  }

                  distanceLeftSquared = this.updateMovementVector(probePosition, probeMovement, targetPosition, directionComponentSelector);
               } else {
                  if (avoidDamage && collision.willDamage) {
                     this.shortenMovement(probePosition, collision.collisionPoint, probePosition);
                     distanceLeftSquared = 0.0;
                  } else {
                     probePosition.set(collision.collisionPoint);
                     distanceLeftSquared = this.updateMovementVector(probePosition, probeMovement, targetPosition, directionComponentSelector);
                  }

                  if (collision.collisionNormal.equals(this.getWorldNormal())) {
                     double distance = this.waypointDistance(initialPosition, probePosition);
                     if (saveSegments) {
                        probeMoveData.addBlockedGroundSegment(probePosition, distance, collision.collisionNormal, collision.blockId);
                     }

                     return distance;
                  }

                  if (saveSegments) {
                     probeMoveData.addHitWallSegment(
                        probePosition, true, this.waypointDistance(initialPosition, probePosition), collision.collisionNormal, collision.blockId
                     );
                  }

                  int blockId = collision.blockId;
                  if (collision.blockType == null
                     || distanceLeftSquared < 0.010000000000000002
                     || !this.isClimbable(collision.blockType, collision.fluid, probeConstraints)) {
                     if (saveSegments) {
                        probeMoveData.changeSegmentToBlockedWall();
                     }

                     return this.waypointDistance(initialPosition, probePosition);
                  }

                  double climbHeight = shortMove
                     ? 0.0
                     : this.computeClimbHeight(
                        probePosition, probeMovement, this.maxClimbHeight, 0.1, null, this.tmpClimbHeightResults, probeConstraints, componentAccessor
                     );
                  if (climbHeight <= 0.0) {
                     if (saveSegments) {
                        probeMoveData.changeSegmentToBlockedWall();
                     }

                     return this.waypointDistance(initialPosition, probePosition);
                  }

                  probePosition.fma(climbHeight, this.getWorldNormal());
                  if (saveSegments) {
                     probeMoveData.addClimbSegment(probePosition, this.waypointDistance(initialPosition, probePosition), blockId);
                  }

                  distanceLeftSquared = this.updateMovementVector(probePosition, probeMovement, targetPosition, directionComponentSelector);
               }
            }

            double distance = this.waypointDistance(initialPosition, probePosition);
            if (saveSegments) {
               probeMoveData.addEndSegment(probePosition, true, distance);
            }

            return distance;
         } finally {
            this.collisionResult.setBlockCollisionFilter(previousBlockCollisionFilter);
         }
      }
   }

   @Override
   protected void postExecuteMove() {
      if (this.isObstructed && !this.onGround) {
         this.moveSpeed = 0.0;
      }
   }

   @Override
   public double getCurrentMaxBodyRotationSpeed() {
      return this.maxRotationSpeed * this.effectHorizontalSpeedMultiplier;
   }

   protected float computeHeading(
      float desiredAngle, double relativeTurnSpeed, float heading, double dt, boolean updateFullyRotated, boolean stopIfTurnedTooFar
   ) {
      double turnAngle = NPCPhysicsMath.turnAngle(heading, desiredAngle);
      double epsilonAngle = this.getEpsilonAngle();
      if (turnAngle >= -epsilonAngle && turnAngle <= epsilonAngle) {
         heading = desiredAngle;
         if (updateFullyRotated) {
            this.isFullyRotated = true;
         }
      } else {
         double maxRotation = dt * this.getCurrentMaxBodyRotationSpeed() * relativeTurnSpeed;
         turnAngle = MathUtil.clamp(turnAngle, -maxRotation, maxRotation);
         heading = PhysicsMath.normalizeTurnAngle((float)(heading + turnAngle));
         if (updateFullyRotated) {
            this.isFullyRotated = false;
         }
      }

      if (stopIfTurnedTooFar && Math.abs(turnAngle) > this.maxMoveTurnAngle) {
         this.moveSpeed = 0.0;
      }

      return heading;
   }

   protected boolean initiateDescend(
      @Nonnull Vector3d translation, boolean wasOnGround, String logName, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (wasOnGround && this.getMotionKind() != MotionKind.DESCENDING && this.getMotionKind() != MotionKind.DROPPING) {
         this.fallStartHeight = this.position.y;
         this.fallSpeed = 0.0;
         this.predictedFallHeight = this.dropDistance(this.position, this.maxDropHeight, componentAccessor);
         this.totalDropDistance = 0.0;
         this.computeDescendDirection(translation);
         if (this.debugModeMove) {
            LOGGER.at(Level.INFO).log("Move: %s, descend init %s %s", logName, this.moveSpeed, this.climbForwardDirection.toString());
         }
      }

      if (this.getMotionKind() != MotionKind.DROPPING) {
         if (this.fallStartHeight - this.position.y > this.maxClimbHeight
            || this.dropDistance(this.position, this.maxDropHeight, componentAccessor) > this.maxClimbHeight + 1.0E-5) {
            if (this.debugModeMove) {
               LOGGER.at(Level.INFO)
                  .log(
                     "Move: %s, dropping %s %s %s %s",
                     logName,
                     this.moveSpeed,
                     this.fallStartHeight - this.position.y,
                     this.dropDistance(this.position, this.maxDropHeight, componentAccessor),
                     this.climbForwardDirection.toString()
                  );
            }

            boolean descendToDrop = this.getMotionKind() == MotionKind.DESCENDING;
            this.setMotionKind(MotionKind.DROPPING);
            return descendToDrop;
         }

         if (this.getMotionKind() != MotionKind.DESCENDING) {
            if (this.debugModeMove) {
               LOGGER.at(Level.INFO).log("Move: %s, descending %s %s", logName, this.moveSpeed, this.climbForwardDirection.toString());
            }

            this.setMotionKind(MotionKind.DESCENDING);
         }
      }

      return false;
   }

   protected double updateMovementVector(
      @Nonnull Vector3d probePosition, @Nonnull Vector3d probeMovement, @Nonnull Vector3d targetPosition, @Nonnull Vector3d directionComponentSelector
   ) {
      probeMovement.set(targetPosition).sub(probePosition).mul(directionComponentSelector);
      return this.waypointDistanceSquared(probePosition, targetPosition);
   }

   @Nullable
   private BlockCollisionData discardIgnorableSlideCollisions(
      @Nonnull CollisionResult collisionResult, @Nullable BlockCollisionData startCollision, @Nonnull EnumSet<RelaxedConstraint> constraints
   ) {
      boolean avoidDamage = !constraints.contains(RelaxedConstraint.DAMAGE);
      double endSlide = collisionResult.slideEnd;

      while (startCollision != null) {
         if (avoidDamage && startCollision.willDamage) {
            return startCollision;
         }

         BlockType blockType = startCollision.blockType;
         if (blockType == null || !startCollision.collisionNormal.equals(this.getWorldNormal()) || startCollision.collisionStart > endSlide) {
            return startCollision;
         }

         startCollision = collisionResult.forgetFirstBlockCollision();
      }

      return null;
   }

   private double bisect(
      @Nonnull Vector3d validPosition, @Nonnull Vector3d invalidPosition, @Nonnull Vector3d result, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (!this.isValidPosition(validPosition, this.collisionResult, componentAccessor)) {
         result.set(invalidPosition);
         return 1.0;
      } else {
         return this.bisect(validPosition, invalidPosition, this, (_this, pos) -> _this.isValidPosition(pos, _this.collisionResult, componentAccessor), result);
      }
   }

   private boolean validateGroundPosition(
      double effectiveDropHeight, @Nonnull EnumSet<RelaxedConstraint> effectiveMoveConstraints, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (this.isDropBlocked(this.position, effectiveDropHeight, false, effectiveMoveConstraints, componentAccessor)) {
         this.position.set(this.lastValidPosition);
         this.isObstructed = true;
         return true;
      } else {
         ChunkStore chunkStore = componentAccessor.getExternalData().getWorld().getChunkStore();
         if (!this.isValidWalkPosition(chunkStore, this.position.x, this.position.y, this.position.z, effectiveMoveConstraints)
            && this.isValidWalkPosition(chunkStore, this.lastValidPosition.x, this.lastValidPosition.y, this.lastValidPosition.z, effectiveMoveConstraints)) {
            this.position.set(this.lastValidPosition);
            this.isObstructed = true;
            return true;
         } else {
            return false;
         }
      }
   }

   private double shortenSlide(@Nonnull Vector3d translation, double endSlide) {
      Vector3d cs = this.getComponentSelector();
      double hwX = (this.collisionBoundingBox.max.x - this.collisionBoundingBox.min.x) * 0.1 * cs.x;
      double hwY = (this.collisionBoundingBox.max.y - this.collisionBoundingBox.min.y) * 0.1 * cs.y;
      double hwZ = (this.collisionBoundingBox.max.z - this.collisionBoundingBox.min.z) * 0.1 * cs.z;
      double edgeMargin = Math.max(Math.max(hwX, Math.max(hwY, hwZ)), 0.05);
      double moveLength = translation.length() * endSlide;
      if (moveLength > edgeMargin) {
         endSlide = endSlide * (moveLength - edgeMargin) / moveLength;
      } else {
         endSlide = 0.0;
      }

      return endSlide;
   }

   private double shortenMovement(@Nonnull Vector3d start, @Nonnull Vector3d end, @Nonnull Vector3d result) {
      double moveLength = end.distance(start);
      if (moveLength <= 0.05) {
         return 0.0;
      } else {
         moveLength = (moveLength - 0.05) / moveLength;
         NPCPhysicsMath.lerp(start, end, moveLength, result);
         return moveLength;
      }
   }

   private double shortenMovement(double triggerScale) {
      double reduction = this.shortenMovement(this.lastValidPosition, this.position, this.position);
      triggerScale *= reduction;
      if (this.debugModeMove) {
         LOGGER.at(Level.INFO).log("Move: Collision reduction=%s triggerScale=%s", reduction, triggerScale);
      }

      return triggerScale;
   }

   private void validateTranslation(@Nonnull Vector3d translation, String kind) {
      if (this.debugModeValidateMath) {
         boolean b = NPCPhysicsMath.isValid(translation) && translation.lengthSquared() < 1000000.0;
         if (!b) {
            throw new IllegalStateException(
               String.format(
                  "Walk - Translation invalid path=%s %s, moveSpeed=%s, fallSpeed=%s, pos=%s",
                  kind,
                  translation.toString(),
                  this.moveSpeed,
                  this.fallSpeed,
                  Vector3dUtil.formatShortString(this.position)
               )
            );
         }
      } else if (!NPCPhysicsMath.isValid(translation) || translation.lengthSquared() > 1000000.0) {
         translation.zero();
      }
   }

   private void validateSpeeds(@Nonnull Ref<EntityStore> ref, @Nonnull String kind, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (this.debugModeValidateMath) {
         if (!Double.isFinite(this.moveSpeed) || !(Math.abs(this.moveSpeed) < 200.0) || !Double.isFinite(this.fallSpeed) || !(Math.abs(this.fallSpeed) < 200.0)
            )
          {
            throw new IllegalStateException(
               String.format(
                  "Walk - Invalid speed path=%s, moveSpeed=%s, fallSpeed=%s, onGround=%s canSteer=%s, pos=%s, motionKind=%s",
                  kind,
                  this.moveSpeed,
                  this.fallSpeed,
                  this.onGround,
                  this.canSteer(ref, componentAccessor),
                  Vector3dUtil.formatShortString(this.position),
                  this.getMotionKind()
               )
            );
         }
      } else {
         this.moveSpeed = MathUtil.clamp(this.moveSpeed, -200.0, 200.0);
         this.fallSpeed = MathUtil.clamp(this.fallSpeed, -200.0, 200.0);
      }
   }

   private void lockOrientation(@Nonnull Steering steering, @Nonnull Vector3d translation, float heading) {
      steering.setYaw(heading);
      steering.setPitch(this.getPitch());
      steering.setRoll(this.getRoll());
      if (this.debugModeValidateMath && !NPCPhysicsMath.isValid(translation)) {
         throw new IllegalArgumentException("Translation invalid");
      }
   }

   private float computeBlendHeading(float heading, float moveHeading, double dt, double speedEstimate, double relativeTurnSpeed) {
      float turnAngle = this.computeBlendTurnAngle(heading, moveHeading);
      return this.computeBlendHeading(heading, moveHeading, dt, speedEstimate, turnAngle, relativeTurnSpeed);
   }

   private float computeBlendHeading(float heading, float moveHeading, double dt, double speedEstimate, float turnAngle, double relativeTurnSpeed) {
      double maxRotationSpeed = this.getCurrentMaxBodyRotationSpeed();
      float maxRotation = (float)MathUtil.clamp(dt * maxRotationSpeed * relativeTurnSpeed, 0.0, (float) (Math.PI / 2));
      if (this.haveBlendHeadingPosition && speedEstimate > 0.0) {
         turnAngle *= (float)this.blendLevelAtTargetPosition;
         double arrivalTime = this.waypointDistance(this.position, this.blendHeadingPosition) / speedEstimate;
         if (arrivalTime * maxRotationSpeed * relativeTurnSpeed > turnAngle) {
            turnAngle = NPCPhysicsMath.turnAngle(heading, moveHeading);
         }
      }

      turnAngle = MathUtil.clamp(turnAngle, -maxRotation, maxRotation);
      return PhysicsMath.normalizeTurnAngle(heading + turnAngle);
   }

   private float computeBlendTurnAngle(float heading, float moveHeading) {
      float desiredHeading = Double.isNaN(this.blendHeading) ? moveHeading : (float)this.blendHeading;
      return NPCPhysicsMath.turnAngle(heading, desiredHeading);
   }

   private double computeClimbMove(@Nonnull Vector3d climbDirection, double climbDistance, double distance, @Nonnull Vector3d translation) {
      if (distance >= climbDistance) {
         distance = climbDistance;
         climbDistance = 0.0;
      } else {
         double newDistance = climbDistance - distance;
         if (newDistance <= 1.0E-5) {
            distance = climbDistance;
            climbDistance = 0.0;
         } else {
            climbDistance = newDistance;
         }
      }

      translation.set(climbDirection).mul(distance);
      return climbDistance;
   }

   private void computeDescendDirection(@Nonnull Vector3d translation) {
      this.climbForwardDirection.set(this.getWorldAntiNormal());
      if (!(this.descendFlatness <= 0.0)) {
         double forwardDistance = NPCPhysicsMath.dotProduct(translation.x, 0.0, translation.z);
         if (!(forwardDistance <= 1.0E-12)) {
            forwardDistance = Math.sqrt(forwardDistance);
            double forwardScale = this.descendFlatness / forwardDistance;
            this.climbForwardDirection.x = forwardScale * translation.x;
            this.climbForwardDirection.z = forwardScale * translation.z;
            this.climbForwardDirection.normalize();
            double newForwardDistance = Math.sqrt(NPCPhysicsMath.dotProduct(this.climbForwardDirection.x, 0.0, this.climbForwardDirection.z));
            if (newForwardDistance > 1.0E-6) {
               double compensation = 1.0 + (1.0 / newForwardDistance - 1.0) * this.descendSpeedCompensation;
               this.climbForwardDirection.mul(compensation);
            }
         }
      }
   }

   private double computeClimbSpeed(double walkSpeed) {
      double climbSpeed = this.climbSpeedConst;
      if (walkSpeed != 0.0 && this.climbSpeedMult != 0.0) {
         climbSpeed += this.climbSpeedMult * Math.pow(walkSpeed, this.climbSpeedPow);
      }

      return climbSpeed;
   }

   private boolean tryClimb(
      @Nonnull Vector3d translation, @Nonnull EnumSet<RelaxedConstraint> constraints, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      boolean canJump = this.jumpHeight > 0.0;
      this.currentClimbForwardDistance = 0.0;
      this.maxClimbForwardDistance = 0.1;
      this.climbUpDistance = this.computeClimbHeight(
         this.position,
         translation,
         this.maxClimbHeight + this.jumpHeight,
         this.maxClimbForwardDistance,
         null,
         this.tmpClimbHeightResults,
         constraints,
         componentAccessor
      );
      double targetJumpHeight = this.climbUpDistance + this.jumpHeight;
      double high = this.tmpClimbHeightResults.y;
      if (this.climbUpDistance > this.maxClimbHeight + 1.0E-5) {
         this.climbUpDistance = 0.0;
      } else if (canJump && this.climbUpDistance >= this.minJumpHeight && high >= targetJumpHeight) {
         this.currentJumpHeight = targetJumpHeight;
         this.jumpDropHeight = this.currentJumpHeight - this.climbUpDistance;
         this.jumpBlockHeight = this.climbUpDistance;
         this.jumpDropDirection.set(this.getWorldAntiNormal());
         double baseClimbUpDistance = this.climbUpDistance;
         this.climbUpDistance = this.currentJumpHeight;
         this.tmpClimbMovement.set(translation).normalize(0.4);
         this.tmpMovePosition.set(this.position).add(0.0, baseClimbUpDistance, 0.0);
         double forwardMax = this.maxMoveFactor(this.tmpMovePosition, this.tmpClimbMovement, constraints, componentAccessor);
         this.maxClimbForwardDistance += forwardMax * 0.4;
         this.tmpMovePosition.fma(forwardMax, this.tmpClimbMovement);
         this.jumping = this.maxClimbForwardDistance >= this.minJumpDistance;
      } else {
         this.jumping = false;
      }

      return this.climbUpDistance > 0.0;
   }

   private double computeClimbHeight(
      @Nonnull Vector3d position,
      @Nonnull Vector3d direction,
      double height,
      double forward,
      @Nullable Vector3d targetPosition,
      @Nonnull Vector2d results,
      @Nonnull EnumSet<RelaxedConstraint> constraints,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      World world = componentAccessor.getExternalData().getWorld();
      ChunkStore chunkStore = world.getChunkStore();
      boolean allowWade = constraints.contains(RelaxedConstraint.WADE);
      this.tmpResults.setCollisionByMaterial(4, allowWade ? 13 : 5);
      results.zero();
      Vector3dc worldNormal = this.getWorldNormal();
      this.tmpClimbMovement.set(worldNormal).mul(height);
      double scale = this.maxMoveFactor(position, this.tmpClimbMovement, constraints, componentAccessor);
      height *= scale;
      if (height == 0.0) {
         return 0.0;
      } else {
         this.tmpClimbMovement.set(direction).normalize(forward);
         this.tmpClimbPosition.set(position).add(this.tmpClimbMovement);
         this.tmpClimbMovement.set(worldNormal).mul(height);
         boolean saveComputeOverlaps = this.tmpResults.isComputeOverlaps();
         this.tmpResults.setComputeOverlaps(true);
         CollisionModule.get();
         CollisionModule.findCollisions(this.collisionBoundingBox, this.tmpClimbPosition, this.tmpClimbMovement, false, this.tmpResults, componentAccessor);
         this.tmpResults.setComputeOverlaps(saveComputeOverlaps);
         BlockCollisionData collisionData = this.tmpResults.getFirstBlockCollision();
         Vector3dc worldAntiNormal = this.getWorldAntiNormal();
         double top = 0.0;

         double high;
         for (high = 1.0; collisionData != null; collisionData = this.tmpResults.forgetFirstBlockCollision()) {
            BlockType blockType = collisionData.blockType;
            if (blockType == null || !this.isClimbable(blockType, collisionData.fluid, constraints)) {
               break;
            }

            if (collisionData.collisionNormal.equals(worldAntiNormal)) {
               if (collisionData.collisionStart > top) {
                  high = collisionData.collisionStart;
                  break;
               }

               if (collisionData.collisionEnd > top) {
                  top = collisionData.collisionEnd;
                  if (top > 1.00001) {
                     return 0.0;
                  }
               }
            }
         }

         if (top == 0.0) {
            return 0.0;
         } else {
            this.tmpClimbPosition.fma(top, this.tmpClimbMovement);
            if (!this.isValidWalkPosition(chunkStore, this.tmpClimbPosition.x, this.tmpClimbPosition.y, this.tmpClimbPosition.z, constraints)) {
               return 0.0;
            } else {
               double bottom = height * top;
               results.set(bottom, height * high);
               if (targetPosition != null) {
                  targetPosition.set(this.tmpClimbPosition);
               }

               return bottom;
            }
         }
      }
   }

   private double effectiveMaxDropHeight(@Nonnull EnumSet<RelaxedConstraint> constraints) {
      return constraints.contains(RelaxedConstraint.DROP) ? this.maxDropHeightRelaxed : this.maxDropHeight;
   }

   private boolean isDropBlocked(
      @Nonnull Vector3d position,
      double maxDropHeight,
      boolean updatePosition,
      @Nonnull EnumSet<RelaxedConstraint> constraints,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      World world = componentAccessor.getExternalData().getWorld();
      ChunkStore chunkStore = world.getChunkStore();
      boolean avoidDamage = !constraints.contains(RelaxedConstraint.DAMAGE);
      if (this.debugModeValidatePositions && !this.isValidPosition(position, this.tmpResults, componentAccessor)) {
         throw new IllegalStateException("Invalid position");
      } else {
         BlockCollisionData collision = this.findDropBlockCollision(position, maxDropHeight, componentAccessor);
         if (collision == null) {
            return true;
         } else {
            this.tmpClimbPosition.set(collision.collisionPoint);
            if (avoidDamage) {
               double collisionStart = collision.collisionStart;

               do {
                  if (collision.willDamage) {
                     if (this.debugModeMove) {
                        LOGGER.at(Level.FINE).log("Drop DMG  %.2f/%.2f/%.2f", this.tmpClimbPosition.x, this.tmpClimbPosition.y, this.tmpClimbPosition.z);
                     }

                     return true;
                  }

                  collision = this.tmpResults.forgetFirstBlockCollision();
               } while (collision != null && collision.collisionStart <= collisionStart);
            }

            if (!this.isValidWalkPosition(chunkStore, this.tmpClimbPosition.x, this.tmpClimbPosition.y, this.tmpClimbPosition.z, constraints)) {
               if (this.debugModeMove) {
                  LOGGER.at(Level.FINE).log("Drop INV  %.2f/%.2f/%.2f", this.tmpClimbPosition.x, this.tmpClimbPosition.y, this.tmpClimbPosition.z);
               }

               return true;
            } else {
               if (this.debugModeMove) {
                  LOGGER.at(Level.FINE).log("Drop END  %.2f/%.2f/%.2f", this.tmpClimbPosition.x, this.tmpClimbPosition.y, this.tmpClimbPosition.z);
               }

               if (updatePosition) {
                  position.set(this.tmpClimbPosition);
               }

               return false;
            }
         }
      }
   }

   private double dropDistance(@Nonnull Vector3d position, double maxTestDistance, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      BlockCollisionData collision = this.findDropBlockCollision(position, maxTestDistance, componentAccessor);
      return collision != null ? collision.collisionStart * maxTestDistance : maxTestDistance;
   }

   @Nullable
   private BlockCollisionData findDropBlockCollision(
      @Nonnull Vector3d position, double maxTestDistance, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      this.tmpResults.setCollisionByMaterial(4);
      this.tmpClimbMovement.set(this.getWorldAntiNormal()).mul(maxTestDistance);
      CollisionModule.get();
      CollisionModule.findCollisions(this.collisionBoundingBox, position, this.tmpClimbMovement, this.tmpResults, componentAccessor);
      BlockCollisionData collision = this.tmpResults.getFirstBlockCollision();
      if (this.debugModeMove) {
         LOGGER.at(Level.FINE)
            .log(
               "Test Drop BOX  %.2f/%.2f/%.2f %.2f/%.2f/%.2f",
               this.collisionBoundingBox.min.x,
               this.collisionBoundingBox.min.y,
               this.collisionBoundingBox.min.z,
               this.collisionBoundingBox.max.x,
               this.collisionBoundingBox.max.y,
               this.collisionBoundingBox.max.z
            );
         if (collision == null) {
            LOGGER.at(Level.FINE)
               .log(
                  "Test Drop NONE %.2f/%.2f/%.2f %.2f/%.2f/%.2f",
                  position.x,
                  position.y,
                  position.z,
                  this.tmpClimbMovement.x,
                  this.tmpClimbMovement.y,
                  this.tmpClimbMovement.z
               );
         } else {
            LOGGER.at(Level.FINE)
               .log(
                  "Test Drop COLL %.2f/%.2f/%.2f %.2f/%.2f/%.2f dist=%s",
                  position.x,
                  position.y,
                  position.z,
                  this.tmpClimbMovement.x,
                  this.tmpClimbMovement.y,
                  this.tmpClimbMovement.z,
                  collision.collisionStart * maxTestDistance
               );
         }
      }

      return collision;
   }

   private boolean isClimbable(@Nonnull BlockType blockType, @Nonnull Fluid fluid, @Nonnull EnumSet<RelaxedConstraint> constraints) {
      boolean allowDamage = constraints.contains(RelaxedConstraint.DAMAGE);
      boolean allowFence = constraints.contains(RelaxedConstraint.FENCE);
      return (allowDamage || blockType.getDamageToEntities() <= 0 && fluid.getDamageToEntities() <= 0)
         && (allowFence || this.fenceBlockSet == Integer.MIN_VALUE || !BlockSetModule.getInstance().blockInSet(this.fenceBlockSet, blockType));
   }

   private boolean isValidWalkPosition(
      @Nonnull Ref<ChunkStore> chunkRef,
      @Nonnull ComponentAccessor<ChunkStore> chunkStore,
      double x,
      double y,
      double z,
      @Nonnull EnumSet<RelaxedConstraint> constraints
   ) {
      boolean checkBreathing = !constraints.contains(RelaxedConstraint.BREATHE);
      boolean allowWade = constraints.contains(RelaxedConstraint.WADE);
      if (checkBreathing) {
         long packed = WorldUtil.getPackedMaterialAndFluidAtPosition(chunkRef, chunkStore, x, y + this.breathingDepth, z);
         BlockMaterial material = BlockMaterial.VALUES[MathUtil.unpackLeft(packed)];
         int fluidId = MathUtil.unpackRight(packed);
         if (!this.role.couldBreathe(material, fluidId)) {
            return false;
         }

         if (this.breathingDepth == this.constraintDepth) {
            return true;
         }
      }

      if (!allowWade) {
         long packedx = WorldUtil.getPackedMaterialAndFluidAtPosition(chunkRef, chunkStore, x, y + this.constraintDepth, z);
         BlockMaterial materialx = BlockMaterial.VALUES[MathUtil.unpackLeft(packedx)];
         int fluidIdx = MathUtil.unpackRight(packedx);
         if (!this.role.couldBreathe(materialx, fluidIdx)) {
            return false;
         }
      }

      return true;
   }

   private boolean isValidWalkPosition(@Nonnull ChunkStore chunkStore, double x, double y, double z, @Nonnull EnumSet<RelaxedConstraint> constraints) {
      long chunkIndex = ChunkUtil.indexChunkFromBlock(x, z);
      Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(chunkIndex);
      return chunkRef != null && chunkRef.isValid() ? this.isValidWalkPosition(chunkRef, chunkStore.getStore(), x, y, z, constraints) : false;
   }

   private double maxMoveFactor(
      @Nonnull Vector3d position,
      @Nonnull Vector3d velocity,
      @Nonnull EnumSet<RelaxedConstraint> constraints,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      CollisionModule.get();
      CollisionModule.findCollisions(this.collisionBoundingBox, position, velocity, this.tmpResults, componentAccessor);
      BlockCollisionData collision;
      if (velocity.y != 0.0) {
         collision = this.tmpResults.getFirstBlockCollision();
      } else {
         collision = this.discardIgnorableSlideCollisions(this.tmpResults, this.tmpResults.getFirstBlockCollision(), constraints);
      }

      return collision == null ? 1.0 : MathUtil.clamp(collision.collisionStart, 0.0, 1.0);
   }

   public static enum AscentAnimationType implements Supplier<String> {
      Walk("Play walk animation"),
      Jump("Play jump animation"),
      Climb("Play climb animation"),
      Fly("Play fly animation"),
      Idle("Play idle animation");

      private final String description;

      private AscentAnimationType(String description) {
         this.description = description;
      }

      public String get() {
         return this.description;
      }
   }

   public static enum DescentAnimationType implements Supplier<String> {
      Walk("Play walk animation"),
      Fall("Play fall animation"),
      Idle("Play idle animation");

      private final String description;

      private DescentAnimationType(String description) {
         this.description = description;
      }

      public String get() {
         return this.description;
      }
   }
}
