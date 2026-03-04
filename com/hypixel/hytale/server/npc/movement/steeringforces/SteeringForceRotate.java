package com.hypixel.hytale.server.npc.movement.steeringforces;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.movement.Steering;
import com.hypixel.hytale.server.npc.util.NPCPhysicsMath;
import javax.annotation.Nonnull;

public class SteeringForceRotate implements SteeringForce {
   private float desiredHeading;
   private float heading;
   private double tolerance = 0.05235988F;

   @Override
   public boolean compute(@Nonnull Steering output) {
      output.clear();
      float turnAngle = NPCPhysicsMath.turnAngle(this.desiredHeading, this.heading);
      if (Math.abs(turnAngle) >= this.tolerance) {
         output.setYaw(this.desiredHeading);
         return true;
      } else {
         return false;
      }
   }

   public void setDesiredHeading(float desiredHeading) {
      this.desiredHeading = desiredHeading;
   }

   public void setHeading(float heading) {
      this.heading = heading;
   }

   public void setHeading(Ref<EntityStore> ref, @Nonnull Entity entity, ComponentAccessor<EntityStore> componentAccessor) {
      TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      this.heading = transformComponent.getRotation().getYaw();
   }

   public void setTolerance(double tolerance) {
      this.tolerance = tolerance;
   }

   public double getDesiredHeading() {
      return this.desiredHeading;
   }

   public double getHeading() {
      return this.heading;
   }

   public double getTolerance() {
      return this.tolerance;
   }
}
