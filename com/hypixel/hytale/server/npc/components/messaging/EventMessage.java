package com.hypixel.hytale.server.npc.components.messaging;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class EventMessage extends NPCMessage {
   private final Vector3d position = new Vector3d();
   private final double maxRangeSquared;
   private boolean sameFlock;

   public EventMessage(double maxRange) {
      this.maxRangeSquared = maxRange * maxRange;
   }

   private EventMessage(@Nonnull Vector3d position, double maxRangeSquared, boolean sameFlock) {
      this.position.assign(position);
      this.maxRangeSquared = maxRangeSquared;
      this.sameFlock = sameFlock;
   }

   @Nonnull
   public Vector3d getPosition() {
      return this.position;
   }

   public double getMaxRangeSquared() {
      return this.maxRangeSquared;
   }

   public boolean isSameFlock() {
      return this.sameFlock;
   }

   public void setSameFlock(boolean sameFlock) {
      this.sameFlock = sameFlock;
   }

   public void activate(double x, double y, double z, Ref<EntityStore> target, double age) {
      super.activate(target, age);
      this.position.assign(x, y, z);
   }

   @Nonnull
   public EventMessage clone() {
      return new EventMessage(this.position, this.maxRangeSquared, this.sameFlock);
   }
}
