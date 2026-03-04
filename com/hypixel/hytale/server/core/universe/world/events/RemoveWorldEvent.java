package com.hypixel.hytale.server.core.universe.world.events;

import com.hypixel.hytale.event.ICancellable;
import com.hypixel.hytale.server.core.universe.world.World;
import javax.annotation.Nonnull;

public class RemoveWorldEvent extends WorldEvent implements ICancellable {
   private boolean cancelled;
   @Nonnull
   private final RemoveWorldEvent.RemovalReason removalReason;

   public RemoveWorldEvent(@Nonnull World world, @Nonnull RemoveWorldEvent.RemovalReason removalReason) {
      super(world);
      this.removalReason = removalReason;
   }

   @Nonnull
   public RemoveWorldEvent.RemovalReason getRemovalReason() {
      return this.removalReason;
   }

   @Override
   public boolean isCancelled() {
      return this.removalReason == RemoveWorldEvent.RemovalReason.EXCEPTIONAL ? false : this.cancelled;
   }

   @Override
   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }

   @Nonnull
   @Override
   public String toString() {
      return "RemoveWorldEvent{cancelled=" + this.cancelled + "} " + super.toString();
   }

   public static enum RemovalReason {
      GENERAL,
      EXCEPTIONAL;
   }
}
