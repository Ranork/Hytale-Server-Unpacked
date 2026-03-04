package com.hypixel.hytale.component.system;

public abstract class CancellableEcsEvent extends EcsEvent implements ICancellableEcsEvent {
   private boolean cancelled = false;

   @Override
   public final boolean isCancelled() {
      return this.cancelled;
   }

   @Override
   public final void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }
}
