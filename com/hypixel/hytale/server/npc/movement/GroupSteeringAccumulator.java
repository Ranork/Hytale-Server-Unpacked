package com.hypixel.hytale.server.npc.movement;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.util.NPCPhysicsMath;
import javax.annotation.Nonnull;

public class GroupSteeringAccumulator {
   @Nonnull
   private static final ComponentType<EntityStore, TransformComponent> TRANSFORM_COMPONENT_TYPE = TransformComponent.getComponentType();
   private final Vector3d sumOfVelocities = new Vector3d();
   private final Vector3d sumOfDistances = new Vector3d();
   private final Vector3d sumOfPositions = new Vector3d();
   private final Vector3d temp = new Vector3d();
   private int count;
   private double x;
   private double y;
   private double z;
   private double xViewDirection;
   private double yViewDirection;
   private double zViewDirection;
   private Vector3d componentSelector = Vector3d.ALL_ONES;
   private double maxRangeSquared = Double.MAX_VALUE;
   private double maxDistance = Double.MAX_VALUE;
   private float collisionViewHalfAngleCosine = 1.0F;

   public void begin(double x, double y, double z, double xViewDirection, double yViewDirection, double zViewDirection) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.xViewDirection = xViewDirection;
      this.yViewDirection = yViewDirection;
      this.zViewDirection = zViewDirection;
      this.sumOfDistances.assign(0.0);
      this.sumOfPositions.assign(0.0);
      this.sumOfVelocities.assign(0.0);
      this.count = 0;
   }

   public void begin(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      HeadRotation headRotationComponent = componentAccessor.getComponent(ref, HeadRotation.getComponentType());

      assert headRotationComponent != null;

      Vector3f headRotation = headRotationComponent.getRotation();
      NPCPhysicsMath.getViewDirection(headRotation, this.temp);
      this.temp.normalize();
      TransformComponent transformComponent = componentAccessor.getComponent(ref, TRANSFORM_COMPONENT_TYPE);

      assert transformComponent != null;

      Vector3d position = transformComponent.getPosition();
      this.begin(position.getX(), position.getY(), position.getZ(), this.temp.x, this.temp.y, this.temp.z);
   }

   public void processEntity(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      Velocity velocityComponent = componentAccessor.getComponent(ref, Velocity.getComponentType());

      assert velocityComponent != null;

      Vector3d velocity = velocityComponent.getVelocity();
      TransformComponent transformComponent = componentAccessor.getComponent(ref, TRANSFORM_COMPONENT_TYPE);

      assert transformComponent != null;

      Vector3d position = transformComponent.getPosition();
      double xPosition = position.getX();
      double yPosition = position.getY();
      double zPosition = position.getZ();
      double dx = xPosition - this.x;
      double dy = yPosition - this.y;
      double dz = zPosition - this.z;
      if (NPCPhysicsMath.dotProduct(dx, dy, dz, this.componentSelector) < this.maxRangeSquared
         && NPCPhysicsMath.isInViewCone(this.xViewDirection, this.yViewDirection, this.zViewDirection, this.collisionViewHalfAngleCosine, dx, dy, dz)) {
         this.sumOfDistances.add(dx, dy, dz);
         this.sumOfPositions.add(xPosition, yPosition, zPosition);
         this.sumOfVelocities.add(velocity);
         this.count++;
      }
   }

   public void processEntity(
      @Nonnull Ref<EntityStore> ref,
      double distanceWeight,
      double positionWeight,
      double velocityWeight,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      TransformComponent transformComponent = componentAccessor.getComponent(ref, TRANSFORM_COMPONENT_TYPE);

      assert transformComponent != null;

      Vector3d position = transformComponent.getPosition();
      Velocity velocityComponent = componentAccessor.getComponent(ref, Velocity.getComponentType());

      assert velocityComponent != null;

      Vector3d velocity = velocityComponent.getVelocity();
      double dx = position.getX() - this.x;
      double dy = position.getY() - this.y;
      double dz = position.getZ() - this.z;
      double d = NPCPhysicsMath.dotProduct(dx, dy, dz, this.componentSelector);
      if (d < this.maxRangeSquared
         && NPCPhysicsMath.isInViewCone(this.xViewDirection, this.yViewDirection, this.zViewDirection, this.collisionViewHalfAngleCosine, dx, dy, dz)) {
         d = 1.0 - Math.sqrt(d) / this.maxDistance;
         double w = Math.pow(d, distanceWeight);
         this.sumOfDistances.add(dx * w, dy * w, dz * w);
         w = Math.pow(d, positionWeight);
         this.sumOfPositions.addScaled(position, w);
         w = Math.pow(d, velocityWeight);
         this.sumOfVelocities.addScaled(velocity, w);
         this.count++;
      }
   }

   public void end() {
      if (this.count > 0) {
         double scale = 1.0 / this.count;
         this.sumOfDistances.scale(scale).scale(this.componentSelector);
         this.sumOfPositions.scale(scale).scale(this.componentSelector);
         this.sumOfVelocities.scale(scale).scale(this.componentSelector);
      }
   }

   public void setComponentSelector(Vector3d componentSelector) {
      this.componentSelector = componentSelector;
   }

   public void setMaxRange(double maxRange) {
      this.maxRangeSquared = maxRange * maxRange;
      this.maxDistance = maxRange;
   }

   public void setViewConeHalfAngleCosine(float collisionViewHalfAngleCosine) {
      this.collisionViewHalfAngleCosine = collisionViewHalfAngleCosine;
   }

   @Nonnull
   public Vector3d getSumOfVelocities() {
      return this.sumOfVelocities;
   }

   @Nonnull
   public Vector3d getSumOfDistances() {
      return this.sumOfDistances;
   }

   @Nonnull
   public Vector3d getSumOfPositions() {
      return this.sumOfPositions;
   }

   public int getCount() {
      return this.count;
   }
}
