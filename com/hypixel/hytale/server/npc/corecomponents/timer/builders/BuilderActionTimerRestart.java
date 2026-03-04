package com.hypixel.hytale.server.npc.corecomponents.timer.builders;

import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.timer.ActionTimer;
import com.hypixel.hytale.server.npc.util.Timer;
import javax.annotation.Nonnull;

public class BuilderActionTimerRestart extends BuilderActionTimer {
   @Nonnull
   public ActionTimer build(@Nonnull BuilderSupport builderSupport) {
      return new ActionTimer(this, builderSupport);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Restart a timer";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Restart a timer. Will be set to the original initial values.";
   }

   @Nonnull
   @Override
   public Timer.TimerAction getTimerAction() {
      return Timer.TimerAction.RESTART;
   }
}
