package com.hypixel.hytale.server.core.modules.physics;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.function.consumer.QuadConsumer;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.NearestBlockUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.asset.type.projectile.config.Projectile;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.modules.collision.BlockCollisionProvider;
import com.hypixel.hytale.server.core.modules.collision.BlockContactData;
import com.hypixel.hytale.server.core.modules.collision.BlockData;
import com.hypixel.hytale.server.core.modules.collision.BlockTracker;
import com.hypixel.hytale.server.core.modules.collision.EntityCollisionProvider;
import com.hypixel.hytale.server.core.modules.collision.EntityContactData;
import com.hypixel.hytale.server.core.modules.collision.IBlockCollisionConsumer;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.physics.util.ForceProvider;
import com.hypixel.hytale.server.core.modules.physics.util.ForceProviderEntity;
import com.hypixel.hytale.server.core.modules.physics.util.ForceProviderStandardState;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsBodyState;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsBodyStateUpdater;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsBodyStateUpdaterSymplecticEuler;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import java.util.function.BiConsumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Deprecated
public class SimplePhysicsProvider implements IBlockCollisionConsumer {
   protected static final double HIT_WATER_IMPULSE_LOSS = 0.2;
   protected static final double ROTATION_FORCE = 3.0;
   protected static final float SPEED_ROTATION_FACTOR = 2.0F;
   protected static final double SWIMMING_DAMPING_FACTOR = 1.0;
   protected static final double DEFAULT_MOVE_OUT_OF_SOLID_SPEED = 5.0;
   protected static final int WATER_DETECTION_EXTREMA_COUNT = 2;
   protected static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   @Nonnull
   protected final BlockCollisionProvider blockCollisionProvider;
   @Nonnull
   protected final EntityCollisionProvider entityCollisionProvider;
   @Nonnull
   protected final BlockTracker triggerTracker;
   @Nonnull
   protected final RestingSupport restingSupport;
   @Nullable
   protected World world;
   @Nonnull
   protected final Vector3d velocity;
   @Nonnull
   protected final Vector3d position;
   @Nonnull
   protected final Vector3d movement;
   protected boolean bounced;
   protected boolean onGround;
   protected boolean provideCharacterCollisions;
   protected double gravity;
   protected double bounciness;
   protected boolean sticksVertically;
   protected boolean computeYaw = true;
   protected boolean computePitch = true;
   protected SimplePhysicsProvider.ROTATION_MODE rotationMode = SimplePhysicsProvider.ROTATION_MODE.VelocityDamped;
   protected UUID creatorUuid;
   protected static final double minBounceEpsilon = 0.4;
   protected static final double minBounceEpsilonSquared = 0.16000000000000003;
   protected final Vector3d tempVector = new Vector3d();
   protected BiConsumer<Vector3d, ComponentAccessor<EntityStore>> bounceConsumer;
   protected QuadConsumer<Ref<EntityStore>, Vector3d, Ref<EntityStore>, ComponentAccessor<EntityStore>> impactConsumer;
   protected double moveOutOfSolidSpeed;
   protected boolean movedInsideSolid;
   protected final Vector3d moveOutOfSolidVelocity = new Vector3d();
   protected final Vector3d contactPosition = new Vector3d();
   protected final Vector3d contactNormal = new Vector3d();
   protected double collisionStart;
   protected final PhysicsBodyStateUpdater stateUpdater = new PhysicsBodyStateUpdaterSymplecticEuler();
   protected final PhysicsBodyState stateBefore = new PhysicsBodyState();
   protected final PhysicsBodyState stateAfter = new PhysicsBodyState();
   protected double displacedMass;
   protected double subSurfaceVolume;
   protected double enterFluid;
   protected double leaveFluid;
   protected boolean inFluid;
   protected int velocityExtremaCount = Integer.MAX_VALUE;
   @Nonnull
   protected SimplePhysicsProvider.STATE state = SimplePhysicsProvider.STATE.Active;
   protected ForceProviderEntity forceProviderEntity;
   protected ForceProvider[] forceProviders;
   protected final ForceProviderStandardState forceProviderStandardState = new ForceProviderStandardState();
   protected double terminalVelocity1;
   protected double density1;
   protected double terminalVelocity2;
   protected double density2;
   protected double dragMultiplier;
   protected double dragOffset;
   protected final BlockTracker fluidTracker = new BlockTracker();
   protected double hitWaterImpulseLoss = 0.2;
   protected double rotationForce = 3.0;
   protected float speedRotationFactor = 2.0F;
   protected double swimmingDampingFactor = 1.0;
   @Deprecated(forRemoval = true)
   protected BoundingBox boundingBox;

   public SimplePhysicsProvider() {
      this.blockCollisionProvider = new BlockCollisionProvider();
      this.blockCollisionProvider.setRequestedCollisionMaterials(6);
      this.blockCollisionProvider.setReportOverlaps(true);
      this.entityCollisionProvider = new EntityCollisionProvider();
      this.triggerTracker = new BlockTracker();
      this.restingSupport = new RestingSupport();
      this.velocity = new Vector3d();
      this.position = new Vector3d();
      this.movement = new Vector3d();
   }

   public SimplePhysicsProvider(
      @Nonnull BiConsumer<Vector3d, ComponentAccessor<EntityStore>> bounceConsumer,
      @Nonnull QuadConsumer<Ref<EntityStore>, Vector3d, Ref<EntityStore>, ComponentAccessor<EntityStore>> impactConsumer
   ) {
      this();
      this.bounceConsumer = bounceConsumer;
      this.impactConsumer = impactConsumer;
   }

   public void setImpacted(boolean impacted) {
      this.state = impacted ? SimplePhysicsProvider.STATE.Inactive : SimplePhysicsProvider.STATE.Active;
   }

   public boolean isImpacted() {
      return this.state == SimplePhysicsProvider.STATE.Inactive;
   }

   public void setResting(boolean resting) {
      if (this.state != SimplePhysicsProvider.STATE.Inactive) {
         this.state = resting ? SimplePhysicsProvider.STATE.Resting : SimplePhysicsProvider.STATE.Active;
      }
   }

   public boolean isResting() {
      return this.state == SimplePhysicsProvider.STATE.Resting;
   }

   @Nonnull
   @Override
   public IBlockCollisionConsumer.Result onCollision(
      int blockX,
      int blockY,
      int blockZ,
      @Nonnull Vector3d direction,
      @Nonnull BlockContactData contactData,
      @Nonnull BlockData blockData,
      @Nonnull Box collider
   ) {
      BlockMaterial blockMaterial = blockData.getBlockType().getMaterial();
      if (this.moveOutOfSolidSpeed > 0.0 && contactData.isOverlapping() && blockMaterial == BlockMaterial.Solid) {
         Vector3i nearestBlock = NearestBlockUtil.findNearestBlock(this.position, (block, w) -> {
            WorldChunk worldChunk = w.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(block.getX(), block.getZ()));
            return worldChunk != null && worldChunk.getBlockType(block).getMaterial() != BlockMaterial.Solid;
         }, this.world);
         if (nearestBlock != null) {
            this.tempVector.assign(nearestBlock.x, nearestBlock.y, nearestBlock.z);
            this.tempVector.add(0.5, 0.5, 0.5);
            this.tempVector.subtract(this.position);
            this.tempVector.setLength(this.moveOutOfSolidSpeed);
         } else {
            this.tempVector.assign(0.0, this.moveOutOfSolidSpeed, 0.0);
         }

         this.moveOutOfSolidVelocity.add(this.tempVector);
         this.movedInsideSolid = true;
         return IBlockCollisionConsumer.Result.CONTINUE;
      } else if (blockData.getFluidId() != 0 && !this.fluidTracker.isTracked(blockX, blockY, blockZ)) {
         double collisionStart = contactData.getCollisionStart();
         double collisionEnd = contactData.getCollisionEnd();
         if (collisionStart < this.enterFluid) {
            this.enterFluid = collisionStart;
         }

         if (collisionEnd > this.leaveFluid) {
            this.leaveFluid = collisionEnd;
         }

         if (collisionEnd <= collisionStart) {
            return IBlockCollisionConsumer.Result.CONTINUE;
         } else {
            double density = 1000.0;
            double volume = PhysicsMath.volumeOfIntersection(this.boundingBox.getBoundingBox(), this.contactPosition, collider, blockX, blockY, blockZ);
            this.subSurfaceVolume += volume;
            this.displacedMass += volume * density;
            this.fluidTracker.trackNew(blockX, blockY, blockZ);
            return IBlockCollisionConsumer.Result.CONTINUE;
         }
      } else if (contactData.isOverlapping()) {
         return IBlockCollisionConsumer.Result.CONTINUE;
      } else {
         double surfaceAlignment = direction.dot(contactData.getCollisionNormal());
         if (blockMaterial == BlockMaterial.Solid && surfaceAlignment == 0.0) {
         }

         if (surfaceAlignment >= 0.0) {
            return IBlockCollisionConsumer.Result.CONTINUE;
         } else {
            this.contactPosition.assign(contactData.getCollisionPoint());
            this.contactNormal.assign(contactData.getCollisionNormal());
            this.collisionStart = contactData.getCollisionStart();
            this.bounced = true;
            return IBlockCollisionConsumer.Result.STOP;
         }
      }
   }

   @Nonnull
   @Override
   public IBlockCollisionConsumer.Result probeCollisionDamage(
      int blockX, int blockY, int blockZ, Vector3d direction, BlockContactData collisionData, BlockData blockData
   ) {
      return IBlockCollisionConsumer.Result.CONTINUE;
   }

   @Override
   public void onCollisionDamage(int blockX, int blockY, int blockZ, Vector3d direction, BlockContactData collisionData, BlockData blockData) {
   }

   @Nonnull
   @Override
   public IBlockCollisionConsumer.Result onCollisionSliceFinished() {
      return IBlockCollisionConsumer.Result.CONTINUE;
   }

   @Override
   public void onCollisionFinished() {
   }

   @Nullable
   public Entity tick(
      double dt,
      @Nonnull Velocity entityVelocity,
      @Nonnull World entityWorld,
      @Nonnull TransformComponent entityTransform,
      Ref<EntityStore> selfRef,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (this.state == SimplePhysicsProvider.STATE.Inactive) {
         entityVelocity.setZero();
         return null;
      } else {
         if (this.state == SimplePhysicsProvider.STATE.Resting) {
            if (this.forceProviderStandardState.externalForce.squaredLength() == 0.0 && !this.restingSupport.hasChanged(entityWorld)) {
               return null;
            }

            this.state = SimplePhysicsProvider.STATE.Active;
         }

         this.world = entityWorld;
         this.position.assign(entityTransform.getPosition());
         entityVelocity.assignVelocityTo(this.velocity);
         double mass = this.forceProviderEntity.getMass(this.boundingBox.getBoundingBox().getVolume());
         this.forceProviderStandardState.convertToForces(dt, mass);
         this.forceProviderStandardState.updateVelocity(this.velocity);
         if (!(this.velocity.squaredLength() * dt * dt >= 1.0000000000000002E-10) && !(this.forceProviderStandardState.externalForce.squaredLength() >= 0.0)) {
            this.velocity.assign(Vector3d.ZERO);
         } else {
            this.state = SimplePhysicsProvider.STATE.Active;
         }

         if (this.state == SimplePhysicsProvider.STATE.Resting && this.restingSupport.hasChanged(entityWorld)) {
            this.state = SimplePhysicsProvider.STATE.Active;
         }

         this.stateBefore.position.assign(this.position);
         this.stateBefore.velocity.assign(this.velocity);
         this.forceProviderEntity.setForceProviderStandardState(this.forceProviderStandardState);
         this.stateUpdater.update(this.stateBefore, this.stateAfter, mass, dt, this.onGround, this.forceProviders);
         this.velocity.assign(this.stateAfter.velocity);
         this.movement.assign(this.velocity).scale(dt);
         this.forceProviderStandardState.clear();
         if (this.velocity.squaredLength() * dt * dt >= 1.0000000000000002E-10) {
            this.state = SimplePhysicsProvider.STATE.Active;
         } else {
            this.velocity.assign(Vector3d.ZERO);
         }

         double maxRelativeDistance = 1.0;
         if (this.provideCharacterCollisions) {
            Ref<EntityStore> creatorReference = null;
            if (this.creatorUuid != null) {
               creatorReference = entityWorld.getEntityRef(this.creatorUuid);
            }

            maxRelativeDistance = this.entityCollisionProvider
               .computeNearest(this.boundingBox.getBoundingBox(), this.position, this.movement, selfRef, creatorReference, componentAccessor);
            if (maxRelativeDistance < 0.0 || maxRelativeDistance > 1.0) {
               maxRelativeDistance = 1.0;
            }
         }

         this.bounced = false;
         this.onGround = false;
         this.moveOutOfSolidVelocity.assign(Vector3d.ZERO);
         this.movedInsideSolid = false;
         this.displacedMass = 0.0;
         this.subSurfaceVolume = 0.0;
         this.enterFluid = Double.MAX_VALUE;
         this.leaveFluid = -Double.MAX_VALUE;
         this.collisionStart = maxRelativeDistance;
         this.contactPosition.assign(this.position).addScaled(this.movement, this.collisionStart);
         this.contactNormal.assign(Vector3d.ZERO);
         this.blockCollisionProvider
            .cast(entityWorld, this.boundingBox.getBoundingBox(), this.position, this.movement, this, this.triggerTracker, maxRelativeDistance);
         this.fluidTracker.reset();
         double density = this.displacedMass > 0.0 ? this.displacedMass / this.subSurfaceVolume : 1.2;
         if (this.movedInsideSolid) {
            this.position.addScaled(this.moveOutOfSolidVelocity, dt);
            this.velocity.assign(this.moveOutOfSolidVelocity);
            this.forceProviderStandardState.dragCoefficient = this.getDragCoefficient(density);
            this.forceProviderStandardState.displacedMass = this.displacedMass;
            this.forceProviderStandardState.gravity = this.gravity;
            this.finishTick(entityTransform, entityVelocity);
            return null;
         } else {
            double velocityClip = this.bounced ? this.collisionStart : 1.0;
            boolean enteringWater = false;
            if (!this.inFluid && this.enterFluid < this.collisionStart) {
               this.inFluid = true;
               velocityClip = this.enterFluid;
               this.velocityExtremaCount = 2;
               enteringWater = true;
            } else if (this.inFluid && this.leaveFluid < this.collisionStart) {
               this.inFluid = false;
               velocityClip = this.leaveFluid;
               this.velocityExtremaCount = 2;
            }

            if (velocityClip > 0.0 && velocityClip < 1.0) {
               this.stateUpdater.update(this.stateBefore, this.stateAfter, mass, dt * velocityClip, this.onGround, this.forceProviders);
               this.velocity.assign(this.stateAfter.velocity);
            }

            if (this.inFluid && this.subSurfaceVolume < this.boundingBox.getBoundingBox().getVolume() && this.velocityExtremaCount > 0) {
               double speedBefore = this.stateBefore.velocity.y;
               double speedAfter = this.stateAfter.velocity.y;
               if (speedBefore * speedAfter <= 0.0) {
                  this.velocityExtremaCount--;
               }
            }

            if (this.isSwimming()) {
               this.forceProviderStandardState.externalForce.y = this.forceProviderStandardState.externalForce.y
                  - this.stateAfter.velocity.y * (this.swimmingDampingFactor / mass);
            }

            if (enteringWater) {
               this.forceProviderStandardState.externalImpulse.addScaled(this.stateAfter.velocity, -this.hitWaterImpulseLoss * mass);
            }

            this.forceProviderStandardState.displacedMass = this.displacedMass;
            this.forceProviderStandardState.dragCoefficient = this.getDragCoefficient(density);
            this.forceProviderStandardState.gravity = this.gravity;
            if (!this.bounced) {
               if (this.entityCollisionProvider.getCount() > 0) {
                  EntityContactData contact = this.entityCollisionProvider.getContact(0);
                  Ref<EntityStore> contactRef = contact.getEntityReference();
                  Entity target = EntityUtils.getEntity(contactRef, componentAccessor);
                  this.position.assign(contact.getCollisionPoint());
                  this.state = SimplePhysicsProvider.STATE.Inactive;
                  if (this.impactConsumer != null) {
                     this.impactConsumer.accept(selfRef, this.position, contactRef, componentAccessor);
                  }

                  this.rotateBody(dt, entityTransform.getRotation());
                  this.finishTick(entityTransform, entityVelocity);
                  return target;
               } else {
                  this.position.add(this.movement);
                  this.rotateBody(dt, entityTransform.getRotation());
                  this.finishTick(entityTransform, entityVelocity);
                  return null;
               }
            } else {
               this.position.assign(this.contactPosition);
               computeReflectedVector(this.velocity, this.contactNormal, this.velocity);
               this.velocity.scale(this.bounciness);
               if (this.velocity.squaredLength() * dt * dt < 0.16000000000000003) {
                  boolean hitGround = this.contactNormal.equals(Vector3d.UP);
                  if (this.sticksVertically || hitGround) {
                     this.state = SimplePhysicsProvider.STATE.Resting;
                     this.restingSupport.rest(entityWorld, this.boundingBox.getBoundingBox(), this.position);
                     this.onGround = hitGround;
                     if (this.impactConsumer != null) {
                        this.impactConsumer.accept(selfRef, this.position, null, componentAccessor);
                     }
                  }

                  this.velocity.assign(Vector3d.ZERO);
               } else if (this.bounceConsumer != null) {
                  this.bounceConsumer.accept(this.position, componentAccessor);
               }

               this.rotateBody(dt, entityTransform.getRotation());
               this.finishTick(entityTransform, entityVelocity);
               return null;
            }
         }
      }
   }

   protected void finishTick(@Nonnull TransformComponent position, @Nonnull Velocity velocity) {
      position.setPosition(this.position);
      velocity.set(this.velocity);
      this.world = null;
      this.entityCollisionProvider.clear();
   }

   protected void rotateBody(double dt, @Nonnull Vector3f bodyRotation) {
      if (this.isComputeYaw() || this.isComputePitch()) {
         double vx = this.stateAfter.velocity.x;
         double vz = this.stateAfter.velocity.z;
         if (!(vx * vx + vz * vz <= 1.0000000000000002E-10)) {
            switch (this.rotationMode) {
               case None:
               default:
                  break;
               case Velocity:
                  if (this.isComputeYaw()) {
                     bodyRotation.setYaw(PhysicsMath.normalizeTurnAngle(PhysicsMath.headingFromDirection(vx, vz)));
                  }

                  if (this.isComputePitch()) {
                     bodyRotation.setPitch(PhysicsMath.pitchFromDirection(vx, this.stateAfter.velocity.y, vz));
                  }
                  break;
               case VelocityDamped:
                  if (this.isComputeYaw()) {
                     bodyRotation.setYaw(PhysicsMath.normalizeTurnAngle(PhysicsMath.headingFromDirection(vx, vz)));
                  }

                  if (this.isComputePitch()) {
                     float pitch = bodyRotation.getPitch();
                     float targetPitch = PhysicsMath.pitchFromDirection(vx, this.velocity.y, vz);
                     float delta = PhysicsMath.normalizeTurnAngle(targetPitch - pitch);
                     float maxDelta = (float)(this.velocity.squaredLength() * dt * this.speedRotationFactor);
                     if (delta > maxDelta) {
                        targetPitch = pitch + maxDelta;
                        delta = maxDelta;
                     } else if (delta < -maxDelta) {
                        targetPitch = pitch - maxDelta;
                        delta = maxDelta;
                     }

                     bodyRotation.setPitch(targetPitch);
                     this.forceProviderStandardState.externalForce.addScaled(this.stateAfter.velocity, delta * -this.rotationForce);
                  }
            }
         }
      }
   }

   public boolean isOnGround() {
      return this.onGround;
   }

   public boolean isSwimming() {
      return this.velocityExtremaCount <= 0;
   }

   public static void computeReflectedVector(@Nonnull Vector3d vec, @Nonnull Vector3d normal, @Nonnull Vector3d result) {
      result.assign(vec);
      double squaredLength = normal.squaredLength();
      if (squaredLength != 0.0) {
         double proj = vec.dot(normal) / squaredLength;
         result.addScaled(normal, -2.0 * proj);
      }
   }

   public boolean isProvidingCharacterCollisions() {
      return this.provideCharacterCollisions;
   }

   public void setProvideCharacterCollisions(boolean provideCharacterCollisions) {
      this.provideCharacterCollisions = provideCharacterCollisions;
   }

   public void setGravity(double gravity, @Nonnull BoundingBox boundingBox) {
      this.gravity = gravity;
      this.recomputeDragFactors(boundingBox);
   }

   public void setBounciness(double bounciness) {
      this.bounciness = bounciness;
   }

   public void setTerminalVelocities(double terminalVelocityAir, double terminalVelocityWater, @Nonnull BoundingBox boundingBox) {
      this.setTerminalVelocities(terminalVelocityAir, 1.2, terminalVelocityWater, 998.0, boundingBox);
   }

   public void setTerminalVelocities(double terminalVelocity1, double density1, double terminalVelocity2, double density2, @Nonnull BoundingBox boundingBox) {
      this.terminalVelocity1 = terminalVelocity1;
      this.density1 = density1;
      this.terminalVelocity2 = terminalVelocity2;
      this.density2 = density2;
      this.recomputeDragFactors(boundingBox);
   }

   @Nonnull
   @Deprecated
   public SimplePhysicsProvider setImpactSlowdown(double impactSlowdown) {
      return this;
   }

   public void setSticksVertically(boolean sticksVertically) {
      this.sticksVertically = sticksVertically;
   }

   public boolean isComputeYaw() {
      return this.computeYaw;
   }

   public void setComputeYaw(boolean computeYaw) {
      this.computeYaw = computeYaw;
   }

   public boolean isComputePitch() {
      return this.computePitch;
   }

   public void setComputePitch(boolean computePitch) {
      this.computePitch = computePitch;
   }

   public void setCreatorId(UUID creatorUuid) {
      this.creatorUuid = creatorUuid;
   }

   public void initialize(@Nullable Projectile projectile, @Nonnull BoundingBox boundingBox) {
      if (projectile != null) {
         this.boundingBox = boundingBox;
         this.forceProviderEntity = new ForceProviderEntity(boundingBox);
         this.forceProviders = new ForceProvider[]{this.forceProviderEntity};
         this.setGravity(projectile.getGravity(), boundingBox);
         double terminalVelocity = projectile.getTerminalVelocity();
         this.setTerminalVelocities(terminalVelocity, terminalVelocity * projectile.getWaterTerminalVelocityMultiplier(), boundingBox);
         this.hitWaterImpulseLoss = projectile.getWaterHitImpulseLoss();
         this.rotationForce = projectile.getDampingRotation();
         this.speedRotationFactor = (float)projectile.getRotationSpeedVelocityRatio();
         this.swimmingDampingFactor = projectile.getSwimmingDampingFactor();
         this.rotationMode = projectile.getRotationMode();
         this.setBounciness(projectile.getBounciness() * (1.0 - projectile.getImpactSlowdown()));
         this.setImpactSlowdown(projectile.getImpactSlowdown());
         this.setSticksVertically(projectile.isSticksVertically());
         this.setComputeYaw(projectile.isComputeYaw());
         this.setComputePitch(projectile.isComputePitch());
         this.forceProviderEntity.setDensity(projectile.getDensity());
      }
   }

   @Nonnull
   public Vector3d getVelocity() {
      return this.velocity;
   }

   public void addVelocity(float x, float y, float z) {
      this.forceProviderStandardState.externalVelocity.add(x, y, z);
   }

   public void setVelocity(@Nonnull Vector3d velocity) {
      this.forceProviderStandardState.nextTickVelocity.assign(velocity);
   }

   public void setMoveOutOfSolid(boolean moveOutOfSolid) {
      this.setMoveOutOfSolid(moveOutOfSolid ? 5.0 : 0.0);
   }

   public void setMoveOutOfSolid(double speed) {
      this.moveOutOfSolidSpeed = Math.max(0.0, speed);
   }

   protected double getDragCoefficient(double density) {
      return this.dragMultiplier * density + this.dragOffset;
   }

   protected void recomputeDragFactors(@Nonnull BoundingBox boundingBoxComponent) {
      Box boundingBox = boundingBoxComponent.getBoundingBox();
      double area = boundingBox.width() * boundingBox.depth();
      double mass = this.forceProviderEntity.getMass(boundingBox.getVolume());
      double drag1 = PhysicsMath.computeDragCoefficient(this.terminalVelocity1, area, mass, this.gravity);
      double drag2 = PhysicsMath.computeDragCoefficient(this.terminalVelocity2, area, mass, this.gravity);
      this.dragMultiplier = (drag2 - drag1) / (this.density2 - this.density1);
      this.dragOffset = drag1 - this.dragMultiplier * this.density1;
   }

   public static enum ROTATION_MODE {
      None,
      Velocity,
      VelocityDamped;
   }

   public static enum STATE {
      Active,
      Resting,
      Inactive;
   }
}
