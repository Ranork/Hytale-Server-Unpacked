package com.hypixel.hytale.server.npc.movement.steeringforces;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.movement.Steering;
import javax.annotation.Nonnull;

public abstract class SteeringForceWithGroup implements SteeringForce {
   @Nonnull
   protected final Vector3d selfPosition = new Vector3d();
   protected Vector3d componentSelector;

   public void setSelf(@Nonnull Ref<EntityStore> ref, @Nonnull Vector3d position, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      this.selfPosition.assign(position.getX(), position.getY(), position.getZ());
   }

   public void setComponentSelector(Vector3d componentSelector) {
      this.componentSelector = componentSelector;
   }

   public abstract void reset();

   public abstract void add(@Nonnull Ref<EntityStore> var1, @Nonnull CommandBuffer<EntityStore> var2);

   @Override
   public boolean compute(Steering output) {
      return true;
   }
}
