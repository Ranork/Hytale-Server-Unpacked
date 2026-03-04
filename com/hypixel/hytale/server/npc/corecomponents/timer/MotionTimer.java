package com.hypixel.hytale.server.npc.corecomponents.timer;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.random.RandomExtra;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.MotionBase;
import com.hypixel.hytale.server.npc.corecomponents.timer.builders.BuilderMotionTimer;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.instructions.Motion;
import com.hypixel.hytale.server.npc.movement.Steering;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class MotionTimer<T extends Motion> extends MotionBase {
   protected final T motion;
   protected final double atLeastSeconds;
   protected final double atMostSeconds;
   protected double activeTime;
   protected double timeToLive;

   public MotionTimer(@Nonnull BuilderMotionTimer<T> builder, @Nonnull BuilderSupport builderSupport, T motion) {
      double[] timerRange = builder.getTimerRange(builderSupport);
      this.atLeastSeconds = timerRange[0];
      this.atMostSeconds = timerRange[1];
      this.motion = motion;
   }

   @Override
   public void activate(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      this.activeTime = 0.0;
      this.timeToLive = RandomExtra.randomRange(this.atLeastSeconds, this.atMostSeconds);
      this.motion.activate(ref, role, componentAccessor);
   }

   @Override
   public void deactivate(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      this.motion.deactivate(ref, role, componentAccessor);
   }

   @Override
   public boolean computeSteering(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Role support,
      @Nullable InfoProvider sensorInfo,
      double dt,
      @Nonnull Steering desiredSteering,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (this.activeTime >= this.timeToLive) {
         return false;
      } else {
         this.activeTime += dt;
         if (!this.motion.computeSteering(ref, support, sensorInfo, dt, desiredSteering, componentAccessor)) {
            this.activeTime = this.timeToLive;
            return false;
         } else {
            return true;
         }
      }
   }

   @Override
   public void registerWithSupport(Role role) {
      this.motion.registerWithSupport(role);
   }

   @Override
   public void motionControllerChanged(
      @Nullable Ref<EntityStore> ref,
      @Nonnull NPCEntity npcComponent,
      MotionController motionController,
      @Nullable ComponentAccessor<EntityStore> componentAccessor
   ) {
      this.motion.motionControllerChanged(ref, npcComponent, motionController, componentAccessor);
   }

   @Override
   public void loaded(Role role) {
      this.motion.loaded(role);
   }

   @Override
   public void spawned(Role role) {
      this.motion.spawned(role);
   }

   @Override
   public void unloaded(Role role) {
      this.motion.unloaded(role);
   }

   @Override
   public void removed(Role role) {
      this.motion.removed(role);
   }

   @Override
   public void teleported(Role role, World from, World to) {
      this.motion.teleported(role, from, to);
   }
}
