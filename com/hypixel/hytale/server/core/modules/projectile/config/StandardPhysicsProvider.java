package com.hypixel.hytale.server.core.modules.projectile.config;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.NearestBlockUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.math.vector.Vector4d;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.meta.DynamicMetaStore;
import com.hypixel.hytale.server.core.modules.collision.BlockCollisionProvider;
import com.hypixel.hytale.server.core.modules.collision.BlockContactData;
import com.hypixel.hytale.server.core.modules.collision.BlockData;
import com.hypixel.hytale.server.core.modules.collision.BlockTracker;
import com.hypixel.hytale.server.core.modules.collision.EntityRefCollisionProvider;
import com.hypixel.hytale.server.core.modules.collision.IBlockCollisionConsumer;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.physics.RestingSupport;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.physics.util.ForceProvider;
import com.hypixel.hytale.server.core.modules.physics.util.ForceProviderEntity;
import com.hypixel.hytale.server.core.modules.physics.util.ForceProviderStandardState;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsBodyState;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsBodyStateUpdater;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsBodyStateUpdaterSymplecticEuler;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.modules.projectile.ProjectileModule;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StandardPhysicsProvider implements IBlockCollisionConsumer, Component<EntityStore> {
   public static final int WATER_DETECTION_EXTREMA_COUNT = 2;
   public static final double MIN_BOUNCE_EPSILON = 0.4;
   public static final double MIN_BOUNCE_EPSILON_SQUARED = 0.16000000000000003;
   @Nonnull
   protected static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   @Nonnull
   protected final BlockCollisionProvider blockCollisionProvider;
   @Nonnull
   protected final EntityRefCollisionProvider entityCollisionProvider;
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
   protected final Vector3d nextMovement = new Vector3d();
   protected boolean bounced;
   protected int bounces = 0;
   protected boolean onGround;
   protected boolean provideCharacterCollisions = true;
   @Nullable
   protected final UUID creatorUuid;
   @Nonnull
   protected final StandardPhysicsConfig physicsConfig;
   protected final Vector3d tempVector = new Vector3d();
   @Nullable
   protected BounceConsumer bounceConsumer;
   @Nullable
   protected ImpactConsumer impactConsumer;
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
   protected StandardPhysicsProvider.STATE state = StandardPhysicsProvider.STATE.ACTIVE;
   protected ForceProviderEntity forceProviderEntity;
   protected ForceProvider[] forceProviders;
   protected final ForceProviderStandardState forceProviderStandardState = new ForceProviderStandardState();
   protected double dragMultiplier;
   protected double dragOffset;
   protected final BlockTracker fluidTracker = new BlockTracker();
   protected boolean isSliding;
   @Deprecated(forRemoval = true)
   protected BoundingBox boundingBox;

   @Nonnull
   public static ComponentType<EntityStore, StandardPhysicsProvider> getComponentType() {
      return ProjectileModule.get().getStandardPhysicsProviderComponentType();
   }

   public StandardPhysicsProvider(
      @Nonnull BoundingBox boundingBox,
      @Nullable UUID creatorUuid,
      @Nonnull StandardPhysicsConfig physicsConfig,
      @Nonnull Vector3d initialForce,
      boolean predicted
   ) {
      this.creatorUuid = creatorUuid;
      this.physicsConfig = physicsConfig;
      this.blockCollisionProvider = new BlockCollisionProvider();
      this.blockCollisionProvider.setRequestedCollisionMaterials(6);
      this.blockCollisionProvider.setReportOverlaps(true);
      this.entityCollisionProvider = new EntityRefCollisionProvider();
      this.triggerTracker = new BlockTracker();
      this.restingSupport = new RestingSupport();
      this.velocity = new Vector3d();
      this.position = new Vector3d();
      this.movement = new Vector3d();
      this.boundingBox = boundingBox;
      this.forceProviderEntity = new ForceProviderEntity(boundingBox);
      this.forceProviderEntity.setDensity(physicsConfig.density);
      this.forceProviders = new ForceProvider[]{this.forceProviderEntity};
      this.forceProviderStandardState.nextTickVelocity.assign(initialForce);
      this.recomputeDragFactors(boundingBox);
      if (!predicted) {
         this.impactConsumer = (ref, position, targetRef, collisionDetailName, commandBuffer) -> {
            if (creatorUuid != null) {
               Ref<EntityStore> creatorRef = commandBuffer.getExternalData().getRefFromUUID(creatorUuid);
               if (creatorRef != null && creatorRef.isValid() && EntityUtils.getEntity(creatorRef, commandBuffer) instanceof LivingEntity livingEntity) {
                  InteractionManager interactionManagerComponent = commandBuffer.getComponent(
                     creatorRef, InteractionModule.get().getInteractionManagerComponent()
                  );
                  if (interactionManagerComponent == null) {
                     commandBuffer.removeEntity(ref, RemoveReason.REMOVE);
                  } else {
                     InteractionContext context = InteractionContext.forProxyEntity(interactionManagerComponent, livingEntity, ref);
                     DynamicMetaStore<InteractionContext> metaStore = context.getMetaStore();
                     metaStore.putMetaObject(Interaction.TARGET_ENTITY, targetRef);
                     metaStore.putMetaObject(Interaction.HIT_LOCATION, new Vector4d(position.x, position.y, position.z, 1.0));
                     metaStore.putMetaObject(Interaction.HIT_DETAIL, collisionDetailName);
                     InteractionType interactionType = targetRef != null ? InteractionType.ProjectileHit : InteractionType.ProjectileMiss;
                     String rootInteractionId = context.getRootInteractionId(interactionType);
                     if (rootInteractionId != null) {
                        RootInteraction rootInteraction = RootInteraction.getRootInteractionOrUnknown(rootInteractionId);
                        if (rootInteraction != null) {
                           InteractionChain chain = interactionManagerComponent.initChain(interactionType, context, rootInteraction, true);
                           interactionManagerComponent.queueExecuteChain(chain);
                        }
                     }
                  }
               } else {
                  commandBuffer.removeEntity(ref, RemoveReason.REMOVE);
               }
            }
         };
         this.bounceConsumer = (ref, position, commandBuffer) -> {
            if (creatorUuid != null) {
               Ref<EntityStore> creatorRef = commandBuffer.getExternalData().getRefFromUUID(creatorUuid);
               if (creatorRef != null && creatorRef.isValid() && EntityUtils.getEntity(creatorRef, commandBuffer) instanceof LivingEntity livingEntity) {
                  InteractionManager interactionManagerComponent = commandBuffer.getComponent(
                     creatorRef, InteractionModule.get().getInteractionManagerComponent()
                  );
                  if (interactionManagerComponent == null) {
                     commandBuffer.removeEntity(ref, RemoveReason.REMOVE);
                  } else {
                     InteractionContext context = InteractionContext.forProxyEntity(interactionManagerComponent, livingEntity, ref);
                     context.getMetaStore().putMetaObject(Interaction.HIT_LOCATION, new Vector4d(position.x, position.y, position.z, 1.0));
                     InteractionType interactionType = InteractionType.ProjectileBounce;
                     String rootInteractionId = context.getRootInteractionId(interactionType);
                     if (rootInteractionId != null) {
                        RootInteraction rootInteraction = RootInteraction.getRootInteractionOrUnknown(rootInteractionId);
                        if (rootInteraction != null) {
                           InteractionChain chain = interactionManagerComponent.initChain(interactionType, context, rootInteraction, true);
                           interactionManagerComponent.queueExecuteChain(chain);
                        }
                     }
                  }
               } else {
                  commandBuffer.removeEntity(ref, RemoveReason.REMOVE);
               }
            }
         };
      }
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
      if (this.physicsConfig.moveOutOfSolidSpeed > 0.0 && contactData.isOverlapping() && blockMaterial == BlockMaterial.Solid) {
         Vector3i nearestBlock = NearestBlockUtil.findNearestBlock(this.position, (block, w) -> {
            WorldChunk worldChunk = w.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(block.getX(), block.getZ()));
            return worldChunk != null && worldChunk.getBlockType(block).getMaterial() != BlockMaterial.Solid;
         }, this.world);
         if (nearestBlock != null) {
            this.tempVector.assign(nearestBlock.x, nearestBlock.y, nearestBlock.z);
            this.tempVector.add(0.5, 0.5, 0.5);
            this.tempVector.subtract(this.position);
            this.tempVector.setLength(this.physicsConfig.moveOutOfSolidSpeed);
         } else {
            this.tempVector.assign(0.0, this.physicsConfig.moveOutOfSolidSpeed, 0.0);
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
            if (this.physicsConfig.allowRolling) {
               Vector3d remaining = this.stateBefore.position.clone().add(this.movement).subtract(this.contactPosition);
               if (!remaining.equals(Vector3d.ZERO)) {
                  double t = remaining.dot(this.contactNormal);
                  this.nextMovement.assign(remaining);
                  this.nextMovement.addScaled(this.contactNormal, -t);
                  this.isSliding = true;
               }
            }

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

   public void finishTick(@Nonnull TransformComponent position, @Nonnull Velocity velocity) {
      position.setPosition(this.position);
      velocity.set(this.velocity);
      this.world = null;
      this.entityCollisionProvider.clear();
   }

   public void rotateBody(double dt, @Nonnull Vector3f bodyRotation) {
      if (this.physicsConfig.computeYaw || this.physicsConfig.computePitch) {
         double vx = this.stateAfter.velocity.x;
         double vz = this.stateAfter.velocity.z;
         if (!(vx * vx + vz * vz <= 1.0000000000000002E-10)) {
            switch (this.physicsConfig.rotationMode) {
               case None:
               default:
                  break;
               case Velocity:
                  if (this.physicsConfig.computeYaw) {
                     bodyRotation.setYaw(PhysicsMath.normalizeTurnAngle(PhysicsMath.headingFromDirection(vx, vz)));
                  }

                  if (this.physicsConfig.computePitch) {
                     bodyRotation.setPitch(PhysicsMath.pitchFromDirection(vx, this.stateAfter.velocity.y, vz));
                  }
                  break;
               case VelocityDamped:
                  if (this.physicsConfig.computeYaw) {
                     bodyRotation.setYaw(PhysicsMath.normalizeTurnAngle(PhysicsMath.headingFromDirection(vx, vz)));
                  }

                  if (this.physicsConfig.computePitch) {
                     float pitch = bodyRotation.getPitch();
                     float targetPitch = PhysicsMath.pitchFromDirection(vx, this.velocity.y, vz);
                     float delta = PhysicsMath.normalizeTurnAngle(targetPitch - pitch);
                     float maxDelta = (float)(this.velocity.squaredLength() * dt * this.physicsConfig.speedRotationFactor);
                     if (delta > maxDelta) {
                        targetPitch = pitch + maxDelta;
                        delta = maxDelta;
                     } else if (delta < -maxDelta) {
                        targetPitch = pitch - maxDelta;
                        delta = maxDelta;
                     }

                     bodyRotation.setPitch(targetPitch);
                     this.forceProviderStandardState.externalForce.addScaled(this.stateAfter.velocity, delta * -this.physicsConfig.rotationForce);
                  }
                  break;
               case VelocityRoll:
                  bodyRotation.setYaw(PhysicsMath.normalizeTurnAngle(PhysicsMath.headingFromDirection(vx, vz)));
                  bodyRotation.setPitch(bodyRotation.getPitch() - (float)this.stateBefore.velocity.length() * this.physicsConfig.rollingSpeed);
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

   public double getDragCoefficient(double density) {
      return this.dragMultiplier * density + this.dragOffset;
   }

   protected void recomputeDragFactors(@Nonnull BoundingBox boundingBoxComponent) {
      Box boundingBox = boundingBoxComponent.getBoundingBox();
      double area = boundingBox.width() * boundingBox.depth();
      double mass = this.forceProviderEntity.getMass(boundingBox.getVolume());
      double drag1 = PhysicsMath.computeDragCoefficient(this.physicsConfig.terminalVelocityAir, area, mass, this.physicsConfig.gravity);
      double drag2 = PhysicsMath.computeDragCoefficient(this.physicsConfig.terminalVelocityWater, area, mass, this.physicsConfig.gravity);
      this.dragMultiplier = (drag2 - drag1) / (this.physicsConfig.densityWater - this.physicsConfig.densityAir);
      this.dragOffset = drag1 - this.dragMultiplier * this.physicsConfig.densityAir;
   }

   @Nonnull
   public StandardPhysicsProvider.STATE getState() {
      return this.state;
   }

   public void setState(@Nonnull StandardPhysicsProvider.STATE state) {
      this.state = state;
   }

   @Nonnull
   public StandardPhysicsConfig getPhysicsConfig() {
      return this.physicsConfig;
   }

   @Nonnull
   public ForceProviderStandardState getForceProviderStandardState() {
      return this.forceProviderStandardState;
   }

   @Nonnull
   public RestingSupport getRestingSupport() {
      return this.restingSupport;
   }

   public void setWorld(@Nullable World world) {
      this.world = world;
   }

   @Nonnull
   public Vector3d getPosition() {
      return this.position;
   }

   @Nonnull
   public Vector3d getVelocity() {
      return this.velocity;
   }

   @Nonnull
   public Vector3d getMovement() {
      return this.movement;
   }

   @Nonnull
   public Vector3d getNextMovement() {
      return this.nextMovement;
   }

   @Nonnull
   public ForceProviderEntity getForceProviderEntity() {
      return this.forceProviderEntity;
   }

   @Nonnull
   public ForceProvider[] getForceProviders() {
      return this.forceProviders;
   }

   @Nonnull
   public PhysicsBodyStateUpdater getStateUpdater() {
      return this.stateUpdater;
   }

   @Nonnull
   public PhysicsBodyState getStateBefore() {
      return this.stateBefore;
   }

   @Nonnull
   public PhysicsBodyState getStateAfter() {
      return this.stateAfter;
   }

   public boolean isProvidesCharacterCollisions() {
      return this.provideCharacterCollisions;
   }

   @Nullable
   public UUID getCreatorUuid() {
      return this.creatorUuid;
   }

   @Nonnull
   public EntityRefCollisionProvider getEntityCollisionProvider() {
      return this.entityCollisionProvider;
   }

   public boolean isBounced() {
      return this.bounced;
   }

   public void setBounced(boolean bounced) {
      this.bounced = bounced;
   }

   public int getBounces() {
      return this.bounces;
   }

   public void incrementBounces() {
      this.bounces++;
   }

   @Nonnull
   public Vector3d getMoveOutOfSolidVelocity() {
      return this.moveOutOfSolidVelocity;
   }

   public boolean isMovedInsideSolid() {
      return this.movedInsideSolid;
   }

   public void setMovedInsideSolid(boolean movedInsideSolid) {
      this.movedInsideSolid = movedInsideSolid;
   }

   public double getDisplacedMass() {
      return this.displacedMass;
   }

   public void setDisplacedMass(double displacedMass) {
      this.displacedMass = displacedMass;
   }

   public double getSubSurfaceVolume() {
      return this.subSurfaceVolume;
   }

   public void setSubSurfaceVolume(double subSurfaceVolume) {
      this.subSurfaceVolume = subSurfaceVolume;
   }

   public double getEnterFluid() {
      return this.enterFluid;
   }

   public void setEnterFluid(double enterFluid) {
      this.enterFluid = enterFluid;
   }

   public double getLeaveFluid() {
      return this.leaveFluid;
   }

   public void setLeaveFluid(double leaveFluid) {
      this.leaveFluid = leaveFluid;
   }

   public double getCollisionStart() {
      return this.collisionStart;
   }

   public void setCollisionStart(double collisionStart) {
      this.collisionStart = collisionStart;
   }

   @Nonnull
   public Vector3d getContactPosition() {
      return this.contactPosition;
   }

   @Nonnull
   public Vector3d getContactNormal() {
      return this.contactNormal;
   }

   public boolean isSliding() {
      return this.isSliding;
   }

   public void setSliding(boolean sliding) {
      this.isSliding = sliding;
   }

   @Nonnull
   public BlockCollisionProvider getBlockCollisionProvider() {
      return this.blockCollisionProvider;
   }

   @Nonnull
   public BlockTracker getTriggerTracker() {
      return this.triggerTracker;
   }

   @Nonnull
   public BlockTracker getFluidTracker() {
      return this.fluidTracker;
   }

   public boolean isInFluid() {
      return this.inFluid;
   }

   public void setInFluid(boolean inFluid) {
      this.inFluid = inFluid;
   }

   public int getVelocityExtremaCount() {
      return this.velocityExtremaCount;
   }

   public void setVelocityExtremaCount(int velocityExtremaCount) {
      this.velocityExtremaCount = velocityExtremaCount;
   }

   public void decrementVelocityExtremaCount() {
      this.velocityExtremaCount--;
   }

   public void setOnGround(boolean onGround) {
      this.onGround = onGround;
   }

   @Nullable
   public ImpactConsumer getImpactConsumer() {
      return this.impactConsumer;
   }

   @Nullable
   public BounceConsumer getBounceConsumer() {
      return this.bounceConsumer;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      return this;
   }

   public static enum STATE {
      ACTIVE,
      RESTING,
      INACTIVE;
   }
}
