package com.hypixel.hytale.server.npc.corecomponents.timer;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.corecomponents.timer.builders.BuilderActionTimer;
import com.hypixel.hytale.server.npc.corecomponents.timer.builders.BuilderActionTimerContinue;
import com.hypixel.hytale.server.npc.corecomponents.timer.builders.BuilderActionTimerModify;
import com.hypixel.hytale.server.npc.corecomponents.timer.builders.BuilderActionTimerPause;
import com.hypixel.hytale.server.npc.corecomponents.timer.builders.BuilderActionTimerRestart;
import com.hypixel.hytale.server.npc.corecomponents.timer.builders.BuilderActionTimerStart;
import com.hypixel.hytale.server.npc.corecomponents.timer.builders.BuilderActionTimerStop;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.util.Timer;
import javax.annotation.Nonnull;

public class ActionTimer extends ActionBase {
   protected final Timer timer;
   protected final Timer.TimerAction action;
   protected double minStartValue;
   protected double maxStartValue;
   protected double minRestartValue;
   protected double maxValue;
   protected double rate;
   protected double increaseValue;
   protected boolean modifyRepeating;
   protected boolean repeating;

   public ActionTimer(@Nonnull BuilderActionTimer builderActionTimer, @Nonnull BuilderSupport builderSupport) {
      super(builderActionTimer);
      this.timer = builderActionTimer.getTimer(builderSupport);
      this.action = builderActionTimer.getTimerAction();
   }

   public ActionTimer(@Nonnull BuilderActionTimerStart builderActionTimerStart, @Nonnull BuilderSupport builderSupport) {
      this((BuilderActionTimer)builderActionTimerStart, builderSupport);
      double[] startValueRange = builderActionTimerStart.getStartValueRange(builderSupport);
      this.minStartValue = startValueRange[0];
      this.maxStartValue = startValueRange[1];
      double[] restartValueRange = builderActionTimerStart.getRestartValueRange(builderSupport);
      this.minRestartValue = restartValueRange[0];
      this.maxValue = restartValueRange[1];
      this.rate = builderActionTimerStart.getRate(builderSupport);
      this.repeating = builderActionTimerStart.isRepeating(builderSupport);
   }

   public ActionTimer(@Nonnull BuilderActionTimerModify builderActionTimerModify, @Nonnull BuilderSupport builderSupport) {
      this((BuilderActionTimer)builderActionTimerModify, builderSupport);
      this.increaseValue = builderActionTimerModify.getIncreaseValue(builderSupport);
      double[] restartValueRange = builderActionTimerModify.getRestartValueRange(builderSupport);
      this.minRestartValue = restartValueRange[0];
      this.maxValue = restartValueRange[1];
      this.rate = builderActionTimerModify.getRate(builderSupport);
      this.minStartValue = builderActionTimerModify.getSetValue(builderSupport);
      this.repeating = builderActionTimerModify.isRepeating(builderSupport);
      this.modifyRepeating = builderActionTimerModify.isModifyRepeating();
   }

   public ActionTimer(BuilderActionTimerPause builderActionTimerPause, @Nonnull BuilderSupport builderSupport) {
      this((BuilderActionTimer)builderActionTimerPause, builderSupport);
   }

   public ActionTimer(BuilderActionTimerStop builderActionTimerStop, @Nonnull BuilderSupport builderSupport) {
      this((BuilderActionTimer)builderActionTimerStop, builderSupport);
   }

   public ActionTimer(BuilderActionTimerContinue builderActionTimerContinue, @Nonnull BuilderSupport builderSupport) {
      this((BuilderActionTimer)builderActionTimerContinue, builderSupport);
   }

   public ActionTimer(BuilderActionTimerRestart builderActionTimerRestart, @Nonnull BuilderSupport builderSupport) {
      this((BuilderActionTimer)builderActionTimerRestart, builderSupport);
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);
      switch (this.action) {
         case START:
            this.executeStartAction();
            break;
         case PAUSE:
            this.executePauseAction();
            break;
         case STOP:
            this.executeStopAction();
            break;
         case MODIFY:
            this.executeModifyAction();
            break;
         case CONTINUE:
            this.executeContinueAction();
            break;
         case RESTART:
            this.executeRestartAction();
      }

      return true;
   }

   protected void executeRestartAction() {
      if (this.timer.isInitialised()) {
         this.timer.restart();
      }
   }

   protected void executeModifyAction() {
      if (this.timer.isInitialised() && !this.timer.isStopped()) {
         if (this.minRestartValue > 0.0) {
            this.timer.setMinRestartValue(this.minRestartValue);
         }

         if (this.maxValue > 0.0) {
            this.timer.setMaxValue(this.maxValue);
         }

         if (this.minStartValue > 0.0) {
            this.timer.setValue(this.minStartValue);
         }

         if (this.increaseValue > 0.0) {
            this.timer.addValue(this.increaseValue);
         }

         if (this.rate > 0.0) {
            this.timer.setRate(this.rate);
         }

         if (this.modifyRepeating) {
            this.timer.setRepeating(this.repeating);
         }
      }
   }

   protected void executeContinueAction() {
      if (this.timer.isInitialised()) {
         this.timer.resume();
      }
   }

   protected void executePauseAction() {
      if (this.timer.isInitialised()) {
         this.timer.pause();
      }
   }

   protected void executeStopAction() {
      if (this.timer.isInitialised()) {
         this.timer.stop();
      }
   }

   protected void executeStartAction() {
      if (!this.timer.isInitialised()) {
         this.timer.start(this.minStartValue, this.maxStartValue, this.minRestartValue, this.maxValue, this.rate, this.repeating);
      }
   }
}
