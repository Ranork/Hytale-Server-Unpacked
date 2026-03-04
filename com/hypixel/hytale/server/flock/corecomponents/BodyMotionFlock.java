package com.hypixel.hytale.server.flock.corecomponents;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.group.EntityGroup;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.flock.FlockMembership;
import com.hypixel.hytale.server.flock.corecomponents.builders.BuilderBodyMotionFlock;
import com.hypixel.hytale.server.npc.corecomponents.BodyMotionBase;
import com.hypixel.hytale.server.npc.movement.GroupSteeringAccumulator;
import com.hypixel.hytale.server.npc.movement.Steering;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BodyMotionFlock extends BodyMotionBase {
   private static final ComponentType<EntityStore, FlockMembership> FLOCK_MEMBERSHIP_COMPONENT_TYPE = FlockMembership.getComponentType();
   private static final ComponentType<EntityStore, TransformComponent> TRANSFORM_COMPONENT_TYPE = TransformComponent.getComponentType();
   private static final ComponentType<EntityStore, EntityGroup> ENTITY_GROUP_COMPONENT_TYPE = EntityGroup.getComponentType();
   protected final GroupSteeringAccumulator groupSteeringAccumulator = new GroupSteeringAccumulator();

   public BodyMotionFlock(@Nonnull BuilderBodyMotionFlock builderBodyMotionFlock) {
      super(builderBodyMotionFlock);
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
      FlockMembership flockMembership = componentAccessor.getComponent(ref, FLOCK_MEMBERSHIP_COMPONENT_TYPE);
      Ref<EntityStore> flockReference = flockMembership != null ? flockMembership.getFlockRef() : null;
      if (flockReference != null && flockReference.isValid()) {
         EntityGroup entityGroup = componentAccessor.getComponent(flockReference, ENTITY_GROUP_COMPONENT_TYPE);
         Vector3d componentSelector = role.getActiveMotionController().getComponentSelector();
         this.groupSteeringAccumulator.setComponentSelector(componentSelector);
         this.groupSteeringAccumulator.setMaxRange(role.getFlockInfluenceRange());
         this.groupSteeringAccumulator.begin(ref, componentAccessor);
         entityGroup.forEachMemberExcludingSelf(
            (iGroupEntity, _entity, _groupSteeringAccumulator, _store) -> _groupSteeringAccumulator.processEntity(iGroupEntity, _store),
            ref,
            this.groupSteeringAccumulator,
            componentAccessor
         );
         this.groupSteeringAccumulator.end();
         double weightCohesion = 1.0;
         double weightSeparation = 1.0;
         Ref<EntityStore> leaderRef = entityGroup.getLeaderRef();
         if (!leaderRef.isValid()) {
            return false;
         } else {
            Vector3d sumOfPositions = this.groupSteeringAccumulator.getSumOfPositions();
            Vector3d sumOfVelocities = this.groupSteeringAccumulator.getSumOfVelocities();
            Vector3d sumOfDistances = this.groupSteeringAccumulator.getSumOfDistances();
            TransformComponent leaderTransformComponent = componentAccessor.getComponent(leaderRef, TRANSFORM_COMPONENT_TYPE);

            assert leaderTransformComponent != null;

            Vector3d position = leaderTransformComponent.getPosition();
            Vector3d toLeader = new Vector3d(position.getX(), position.getY(), position.getZ());
            if (sumOfVelocities.squaredLength() > 1.0E-4) {
               desiredSteering.setYaw(PhysicsMath.headingFromDirection(sumOfVelocities.getX(), sumOfVelocities.getZ()));
            } else {
               TransformComponent parentEntityTransformComponent = componentAccessor.getComponent(ref, TRANSFORM_COMPONENT_TYPE);

               assert parentEntityTransformComponent != null;

               desiredSteering.setYaw(parentEntityTransformComponent.getRotation().getYaw());
            }

            sumOfPositions.subtract(position.getX(), position.getY(), position.getZ()).scale(componentSelector);
            if (sumOfPositions.squaredLength() > 1.0E-4) {
               sumOfPositions.normalize().scale(weightCohesion);
            } else {
               sumOfPositions.assign(0.0);
            }

            if (sumOfDistances.squaredLength() > 1.0E-4) {
               sumOfDistances.normalize().scale(-weightSeparation);
            } else {
               sumOfDistances.assign(0.0);
            }

            toLeader.subtract(position.getX(), position.getY(), position.getZ()).scale(componentSelector);
            toLeader.normalize().scale(0.5);
            sumOfPositions.add(sumOfDistances).add(toLeader);
            if (sumOfPositions.squaredLength() > 1.0E-4) {
               sumOfPositions.normalize();
            } else {
               sumOfPositions.assign(0.0);
            }

            desiredSteering.setTranslation(sumOfPositions);
            return true;
         }
      } else {
         return false;
      }
   }
}
