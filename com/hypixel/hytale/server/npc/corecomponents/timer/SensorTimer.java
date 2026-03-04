package com.hypixel.hytale.server.npc.corecomponents.timer;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.corecomponents.timer.builders.BuilderSensorTimer;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.util.Timer;
import javax.annotation.Nonnull;

public class SensorTimer extends SensorBase {
   protected final double minTimeRemaining;
   protected final double maxTimeRemaining;
   protected final Timer timer;
   protected final Timer.TimerState timerState;

   public SensorTimer(@Nonnull BuilderSensorTimer builderSensorTimer, @Nonnull BuilderSupport builderSupport) {
      super(builderSensorTimer);
      this.timer = builderSensorTimer.getTimer(builderSupport);
      double[] timerThresholds = builderSensorTimer.getRemainingTimeRange(builderSupport);
      this.minTimeRemaining = timerThresholds[0];
      this.maxTimeRemaining = timerThresholds[1];
      this.timerState = builderSensorTimer.getTimerState();
   }

   @Override
   public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
      if (!super.matches(ref, role, dt, store)) {
         return false;
      } else {
         return !this.timer.isInitialised()
            ? (this.timerState == Timer.TimerState.ANY || this.timerState == Timer.TimerState.STOPPED) && this.isBetween(0.0)
            : this.timer.isInState(this.timerState) && this.isBetween(this.timer.getValue());
      }
   }

   @Override
   public InfoProvider getSensorInfo() {
      return null;
   }

   protected boolean isBetween(double value) {
      return value >= this.minTimeRemaining && value <= this.maxTimeRemaining;
   }
}
