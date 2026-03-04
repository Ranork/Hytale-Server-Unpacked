package com.hypixel.hytale.server.npc.corecomponents.movement;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.group.EntityGroup;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.flock.FlockPlugin;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.movement.builders.BuilderBodyMotionWanderInCircle;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.util.NPCPhysicsMath;
import javax.annotation.Nonnull;

public class BodyMotionWanderInCircle extends BodyMotionWanderBase {
   protected final double radius;
   protected final boolean flock;
   protected final boolean useSphere;
   protected final Vector3d referencePoint = new Vector3d();

   public BodyMotionWanderInCircle(@Nonnull BuilderBodyMotionWanderInCircle builder, @Nonnull BuilderSupport builderSupport) {
      super(builder, builderSupport);
      this.radius = builder.getRadius(builderSupport);
      this.flock = builder.isFlock();
      this.useSphere = builder.isUseSphere();
   }

   @Override
   protected double constrainMove(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Role role,
      @Nonnull Vector3d probePosition,
      @Nonnull Vector3d targetPosition,
      double moveDist,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      Vector3d referencePoint = this.getReferencePoint(ref, componentAccessor);
      double r2 = this.radius * this.radius;
      MotionController activeMotionController = role.getActiveMotionController();
      if (this.useSphere) {
         double endDist2 = activeMotionController.waypointDistanceSquared(targetPosition, referencePoint);
         if (endDist2 <= r2) {
            return moveDist;
         } else {
            double startDist2 = activeMotionController.waypointDistanceSquared(probePosition, referencePoint);
            if (startDist2 >= r2) {
               return endDist2 <= startDist2 ? moveDist : 0.0;
            } else {
               return NPCPhysicsMath.intersectLineSphereLerp(
                     referencePoint, this.radius, probePosition, targetPosition, activeMotionController.getComponentSelector()
                  )
                  * moveDist;
            }
         }
      } else {
         Vector3d n = activeMotionController.getWorldNormal();
         double endDist2 = NPCPhysicsMath.squaredDistProjected(targetPosition.getX(), targetPosition.getY(), targetPosition.getZ(), referencePoint, n);
         if (endDist2 <= r2) {
            return moveDist;
         } else {
            double startDist2 = NPCPhysicsMath.squaredDistProjected(probePosition.getX(), probePosition.getY(), probePosition.getZ(), referencePoint, n);
            if (startDist2 >= r2) {
               return endDist2 <= startDist2 ? moveDist : 0.0;
            } else {
               return moveDist * Math.max(0.0, NPCPhysicsMath.rayCircleIntersect(probePosition, targetPosition, referencePoint, this.radius, n));
            }
         }
      }
   }

   protected Vector3d getReferencePoint(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (this.flock) {
         World world = componentAccessor.getExternalData().getWorld();
         TransformComponent entityTransformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

         assert entityTransformComponent != null;

         Vector3d entityPosition = entityTransformComponent.getPosition();
         Ref<EntityStore> flockReference = FlockPlugin.getFlockReference(ref, componentAccessor);
         if (flockReference != null) {
            EntityGroup entityGroupComponent = componentAccessor.getComponent(flockReference, EntityGroup.getComponentType());

            assert entityGroupComponent != null;

            Ref<EntityStore> leaderRef = entityGroupComponent.getLeaderRef();
            if (leaderRef.isValid()) {
               TransformComponent leaderTransformComponent = componentAccessor.getComponent(leaderRef, TransformComponent.getComponentType());

               assert leaderTransformComponent != null;

               Vector3d leaderPosition = leaderTransformComponent.getPosition();
               this.referencePoint.assign(leaderPosition.getX(), leaderPosition.getY(), leaderPosition.getZ());
               return this.referencePoint;
            }
         }

         this.referencePoint.assign(entityPosition);
         return this.referencePoint;
      } else {
         NPCEntity npcComponent = componentAccessor.getComponent(ref, NPCEntity.getComponentType());

         assert npcComponent != null;

         return npcComponent.getLeashPoint();
      }
   }
}
