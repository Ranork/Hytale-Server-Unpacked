package com.hypixel.hytale.builtin.teleport.commands.event;

import com.hypixel.hytale.builtin.teleport.Warp;
import com.hypixel.hytale.event.ICancellable;
import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.server.core.Message;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class ModifyWarpEvent implements IEvent<String>, ICancellable {
   @Nonnull
   protected final Warp warp;
   @Nullable
   protected Message cancelReason = null;
   protected boolean cancel = false;

   public ModifyWarpEvent(@Nonnull Warp warp) {
      this.warp = warp;
   }

   @Override
   public boolean isCancelled() {
      return this.cancel;
   }

   @Override
   public void setCancelled(boolean cancelled) {
      this.cancel = cancelled;
   }

   @Nonnull
   public Warp getWarp() {
      return this.warp;
   }

   @Nullable
   public Message getCancelReason() {
      return this.cancelReason;
   }

   public void cancel(@Nonnull Message reason) {
      this.cancelReason = reason;
      this.cancel = true;
   }

   @Override
   public String toString() {
      return "ModifyWarpEvent{warp=" + this.warp + ", cancelReason=" + this.cancelReason + ", cancel=" + this.cancel + "}";
   }
}
