package com.hypixel.hytale.builtin.teleport.commands.event;

import com.hypixel.hytale.builtin.teleport.Warp;
import javax.annotation.Nonnull;

public class RemoveWarpEvent extends ModifyWarpEvent {
   public RemoveWarpEvent(@Nonnull Warp warp) {
      super(warp);
   }

   @Override
   public String toString() {
      return "RemoveWarpEvent{warp=" + this.warp + ", cancelReason=" + this.cancelReason + ", cancel=" + this.cancel + "}";
   }
}
