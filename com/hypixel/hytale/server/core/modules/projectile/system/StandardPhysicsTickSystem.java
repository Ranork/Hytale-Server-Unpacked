package com.hypixel.hytale.server.core.modules.projectile.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.OrderPriority;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.collision.BlockCollisionProvider;
import com.hypixel.hytale.server.core.modules.collision.BlockTracker;
import com.hypixel.hytale.server.core.modules.collision.EntityContactData;
import com.hypixel.hytale.server.core.modules.collision.EntityRefCollisionProvider;
import com.hypixel.hytale.server.core.modules.collision.TangiableEntitySpatialSystem;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.system.TransformSystems;
import com.hypixel.hytale.server.core.modules.physics.RestingSupport;
import com.hypixel.hytale.server.core.modules.physics.SimplePhysicsProvider;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.physics.util.ForceProvider;
import com.hypixel.hytale.server.core.modules.physics.util.ForceProviderEntity;
import com.hypixel.hytale.server.core.modules.physics.util.ForceProviderStandardState;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsBodyState;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsBodyStateUpdater;
import com.hypixel.hytale.server.core.modules.projectile.config.StandardPhysicsConfig;
import com.hypixel.hytale.server.core.modules.projectile.config.StandardPhysicsProvider;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Set;
import javax.annotation.Nonnull;

public class StandardPhysicsTickSystem extends EntityTickingSystem<EntityStore> {
   @Nonnull
   private final Query<EntityStore> query = Query.and(
      StandardPhysicsProvider.getComponentType(),
      TransformComponent.getComponentType(),
      HeadRotation.getComponentType(),
      Velocity.getComponentType(),
      BoundingBox.getComponentType()
   );
   @Nonnull
   private final Set<Dependency<EntityStore>> dependencies = Set.of(
      new SystemDependency<>(Order.AFTER, TangiableEntitySpatialSystem.class, OrderPriority.CLOSEST),
      new SystemDependency<>(Order.BEFORE, TransformSystems.EntityTrackerUpdate.class)
   );

   @Nonnull
   @Override
   public Set<Dependency<EntityStore>> getDependencies() {
      return this.dependencies;
   }

   @Nonnull
   @Override
   public Query<EntityStore> getQuery() {
      return this.query;
   }

   @Override
   public void tick(
      float dt,
      int index,
      @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      TimeResource timeResource = store.getResource(TimeResource.getResourceType());
      float timeDilationModifier = timeResource.getTimeDilationModifier();
      World world = store.getExternalData().getWorld();
      dt = 1.0F / world.getTps();
      dt *= timeDilationModifier;
      StandardPhysicsProvider physicsComponent = archetypeChunk.getComponent(index, StandardPhysicsProvider.getComponentType());

      assert physicsComponent != null;

      Velocity velocityComponent = archetypeChunk.getComponent(index, Velocity.getComponentType());

      assert velocityComponent != null;

      TransformComponent transformComponent = archetypeChunk.getComponent(index, TransformComponent.getComponentType());

      assert transformComponent != null;

      BoundingBox boundingBoxComponent = archetypeChunk.getComponent(index, BoundingBox.getComponentType());

      assert boundingBoxComponent != null;

      StandardPhysicsConfig physicsConfig = physicsComponent.getPhysicsConfig();
      Ref<EntityStore> selfRef = archetypeChunk.getReferenceTo(index);
      if (physicsComponent.getState() == StandardPhysicsProvider.STATE.INACTIVE) {
         velocityComponent.setZero();
      } else {
         ForceProviderStandardState forceState = physicsComponent.getForceProviderStandardState();
         RestingSupport restingSupport = physicsComponent.getRestingSupport();
         if (physicsComponent.getState() == StandardPhysicsProvider.STATE.RESTING) {
            if (forceState.externalForce.squaredLength() == 0.0 && !restingSupport.hasChanged(world)) {
               return;
            }

            physicsComponent.setState(StandardPhysicsProvider.STATE.ACTIVE);
         }

         Vector3d position = physicsComponent.getPosition();
         Vector3d velocity = physicsComponent.getVelocity();
         Vector3d movement = physicsComponent.getMovement();
         Box boundingBox = boundingBoxComponent.getBoundingBox();
         PhysicsBodyState stateBefore = physicsComponent.getStateBefore();
         PhysicsBodyState stateAfter = physicsComponent.getStateAfter();
         ForceProviderEntity forceProviderEntity = physicsComponent.getForceProviderEntity();
         PhysicsBodyStateUpdater stateUpdater = physicsComponent.getStateUpdater();
         ForceProvider[] forceProviders = physicsComponent.getForceProviders();
         Vector3d moveOutOfSolidVelocity = physicsComponent.getMoveOutOfSolidVelocity();
         double gravity = physicsConfig.getGravity();
         int bounceCount = physicsConfig.getBounceCount();
         boolean allowRolling = physicsConfig.isAllowRolling();
         physicsComponent.setWorld(world);
         position.assign(transformComponent.getPosition());
         velocityComponent.assignVelocityTo(velocity);
         double mass = forceProviderEntity.getMass(boundingBox.getVolume());
         forceState.convertToForces(dt, mass);
         forceState.updateVelocity(velocity);
         if (!(velocity.squaredLength() * dt * dt >= 1.0000000000000002E-10) && !(forceState.externalForce.squaredLength() >= 0.0)) {
            velocity.assign(Vector3d.ZERO);
         } else {
            physicsComponent.setState(StandardPhysicsProvider.STATE.ACTIVE);
         }

         if (physicsComponent.getState() == StandardPhysicsProvider.STATE.RESTING && restingSupport.hasChanged(world)) {
            physicsComponent.setState(StandardPhysicsProvider.STATE.ACTIVE);
         }

         stateBefore.position.assign(position);
         stateBefore.velocity.assign(velocity);
         forceProviderEntity.setForceProviderStandardState(forceState);
         stateUpdater.update(stateBefore, stateAfter, mass, dt, physicsComponent.isOnGround(), forceProviders);
         velocity.assign(stateAfter.velocity);
         movement.assign(velocity).scale(dt);
         forceState.clear();
         if (velocity.squaredLength() * dt * dt >= 1.0000000000000002E-10) {
            physicsComponent.setState(StandardPhysicsProvider.STATE.ACTIVE);
         } else {
            velocity.assign(Vector3d.ZERO);
         }

         EntityRefCollisionProvider entityCollisionProvider = physicsComponent.getEntityCollisionProvider();
         BlockCollisionProvider blockCollisionProvider = physicsComponent.getBlockCollisionProvider();
         BlockTracker triggerTracker = physicsComponent.getTriggerTracker();
         Vector3d contactPosition = physicsComponent.getContactPosition();
         Vector3d contactNormal = physicsComponent.getContactNormal();
         Vector3d nextMovement = physicsComponent.getNextMovement();
         double maxRelativeDistance = 1.0;
         if (physicsComponent.isProvidesCharacterCollisions()) {
            Ref<EntityStore> creatorReference = null;
            if (physicsComponent.getCreatorUuid() != null) {
               creatorReference = store.getExternalData().getRefFromUUID(physicsComponent.getCreatorUuid());
            }

            maxRelativeDistance = entityCollisionProvider.computeNearest(commandBuffer, boundingBox, position, movement, selfRef, creatorReference);
            if (maxRelativeDistance < 0.0 || maxRelativeDistance > 1.0) {
               maxRelativeDistance = 1.0;
            }
         }

         physicsComponent.setBounced(false);
         physicsComponent.setOnGround(false);
         moveOutOfSolidVelocity.assign(Vector3d.ZERO);
         physicsComponent.setMovedInsideSolid(false);
         physicsComponent.setDisplacedMass(0.0);
         physicsComponent.setSubSurfaceVolume(0.0);
         physicsComponent.setEnterFluid(Double.MAX_VALUE);
         physicsComponent.setLeaveFluid(-Double.MAX_VALUE);
         physicsComponent.setCollisionStart(maxRelativeDistance);
         contactPosition.assign(position).addScaled(movement, physicsComponent.getCollisionStart());
         contactNormal.assign(Vector3d.ZERO);
         physicsComponent.setSliding(true);
         Vector3d tmpPosition = position.clone();
         nextMovement.assign(Vector3d.ZERO);

         while (physicsComponent.isSliding() && !movement.equals(Vector3d.ZERO)) {
            contactPosition.assign(tmpPosition).addScaled(movement, physicsComponent.getCollisionStart());
            physicsComponent.setSliding(false);
            blockCollisionProvider.cast(world, boundingBox, tmpPosition, movement, physicsComponent, triggerTracker, maxRelativeDistance);
            movement.assign(nextMovement);
            tmpPosition.assign(contactPosition);
         }

         movement.assign(tmpPosition).add(nextMovement).subtract(position);
         physicsComponent.getFluidTracker().reset();
         double density = physicsComponent.getDisplacedMass() > 0.0 ? physicsComponent.getDisplacedMass() / physicsComponent.getSubSurfaceVolume() : 1.2;
         if (physicsComponent.isMovedInsideSolid()) {
            position.addScaled(moveOutOfSolidVelocity, dt);
            velocity.assign(moveOutOfSolidVelocity);
            forceState.dragCoefficient = physicsComponent.getDragCoefficient(density);
            forceState.displacedMass = physicsComponent.getDisplacedMass();
            forceState.gravity = gravity;
            physicsComponent.finishTick(transformComponent, velocityComponent);
         } else {
            double velocityClip = physicsComponent.isBounced() ? physicsComponent.getCollisionStart() : 1.0;
            boolean enteringWater = false;
            if (!physicsComponent.isInFluid() && physicsComponent.getEnterFluid() < physicsComponent.getCollisionStart()) {
               physicsComponent.setInFluid(true);
               velocityClip = physicsComponent.getEnterFluid();
               physicsComponent.setVelocityExtremaCount(2);
               enteringWater = true;
            } else if (physicsComponent.isInFluid() && physicsComponent.getLeaveFluid() < physicsComponent.getCollisionStart()) {
               physicsComponent.setInFluid(false);
               velocityClip = physicsComponent.getLeaveFluid();
               physicsComponent.setVelocityExtremaCount(2);
            }

            if (velocityClip > 0.0 && velocityClip < 1.0) {
               stateUpdater.update(stateBefore, stateAfter, mass, dt * velocityClip, physicsComponent.isOnGround(), forceProviders);
               velocity.assign(stateAfter.velocity);
            }

            if (physicsComponent.isInFluid()
               && physicsComponent.getSubSurfaceVolume() < boundingBox.getVolume()
               && physicsComponent.getVelocityExtremaCount() > 0) {
               double speedBefore = stateBefore.velocity.y;
               double speedAfter = stateAfter.velocity.y;
               if (speedBefore * speedAfter <= 0.0) {
                  physicsComponent.decrementVelocityExtremaCount();
               }
            }

            if (physicsComponent.isSwimming()) {
               forceState.externalForce.y = forceState.externalForce.y - stateAfter.velocity.y * (physicsConfig.getSwimmingDampingFactor() / mass);
            }

            if (enteringWater) {
               forceState.externalImpulse.addScaled(stateAfter.velocity, -physicsConfig.getHitWaterImpulseLoss() * mass);
            }

            forceState.displacedMass = physicsComponent.getDisplacedMass();
            forceState.dragCoefficient = physicsComponent.getDragCoefficient(density);
            forceState.gravity = gravity;
            if (entityCollisionProvider.getCount() > 0) {
               EntityContactData contact = entityCollisionProvider.getContact(0);
               Ref<EntityStore> contactRef = contact.getEntityReference();
               position.assign(contact.getCollisionPoint());
               physicsComponent.setState(StandardPhysicsProvider.STATE.INACTIVE);
               if (physicsComponent.getImpactConsumer() != null) {
                  physicsComponent.getImpactConsumer().onImpact(selfRef, position, contactRef, contact.getCollisionDetailName(), commandBuffer);
               }

               physicsComponent.rotateBody(dt, transformComponent.getRotation());
               physicsComponent.finishTick(transformComponent, velocityComponent);
            } else if (!physicsComponent.isBounced()) {
               position.add(movement);
               physicsComponent.rotateBody(dt, transformComponent.getRotation());
               physicsComponent.finishTick(transformComponent, velocityComponent);
            } else {
               position.assign(contactPosition);
               physicsComponent.incrementBounces();
               SimplePhysicsProvider.computeReflectedVector(velocity, contactNormal, velocity);
               if (bounceCount == -1 || physicsComponent.getBounces() <= bounceCount) {
                  velocity.scale(physicsConfig.getBounciness());
               }

               if ((bounceCount == -1 || physicsComponent.getBounces() <= bounceCount)
                  && !(velocity.squaredLength() * dt * dt < physicsConfig.getBounceLimit() * physicsConfig.getBounceLimit())) {
                  if (physicsComponent.getBounceConsumer() != null) {
                     physicsComponent.getBounceConsumer().onBounce(selfRef, position, commandBuffer);
                  }
               } else {
                  boolean hitGround = contactNormal.equals(Vector3d.UP);
                  if (!allowRolling && (physicsConfig.isSticksVertically() || hitGround)) {
                     physicsComponent.setState(StandardPhysicsProvider.STATE.RESTING);
                     restingSupport.rest(world, boundingBox, position);
                     physicsComponent.setOnGround(hitGround);
                     if (physicsComponent.getImpactConsumer() != null) {
                        physicsComponent.getImpactConsumer().onImpact(selfRef, position, null, null, commandBuffer);
                     }
                  }

                  if (allowRolling) {
                     velocity.y = 0.0;
                     velocity.scale(physicsConfig.getRollingFrictionFactor());
                     physicsComponent.setOnGround(hitGround);
                  } else {
                     velocity.assign(Vector3d.ZERO);
                  }
               }

               physicsComponent.rotateBody(dt, transformComponent.getRotation());
               physicsComponent.finishTick(transformComponent, velocityComponent);
            }
         }
      }
   }
}
