package com.hypixel.hytale.builtin.teleport.commands.event;

import com.hypixel.hytale.builtin.teleport.Warp;
import javax.annotation.Nonnull;

public class ReplaceWarpEvent extends ModifyWarpEvent {
   public ReplaceWarpEvent(@Nonnull Warp warp) {
      super(warp);
   }

   @Override
   public String toString() {
      return "ReplaceWarpEvent{warp=" + this.warp + ", cancelReason=" + this.cancelReason + ", cancel=" + this.cancel + "}";
   }
}
