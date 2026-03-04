package com.hypixel.hytale.server.npc.corecomponents.utility;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.MotionBase;
import com.hypixel.hytale.server.npc.corecomponents.utility.builders.BuilderMotionSequence;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.instructions.Motion;
import com.hypixel.hytale.server.npc.movement.Steering;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.util.IAnnotatedComponent;
import com.hypixel.hytale.server.npc.util.IAnnotatedComponentCollection;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class MotionSequence<T extends Motion> extends MotionBase implements IAnnotatedComponentCollection {
   protected final boolean looped;
   protected final boolean restartOnActivate;
   protected final T[] steps;
   protected boolean finished;
   protected int index;
   @Nullable
   protected T activeMotion;

   public MotionSequence(@Nonnull BuilderMotionSequence<T> builder, T[] steps) {
      this.restart();
      this.looped = builder.isLooped();
      this.restartOnActivate = builder.isRestartOnActivate();
      this.steps = steps;
   }

   @Override
   public void activate(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (this.restartOnActivate) {
         this.deactivate(ref, role, componentAccessor);
         this.restart();
      }

      if (!this.finished) {
         this.doActivate(ref, role, componentAccessor);
      }
   }

   @Override
   public void deactivate(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (this.activeMotion != null) {
         this.activeMotion.deactivate(ref, role, componentAccessor);
         this.activeMotion = null;
      }
   }

   @Override
   public boolean computeSteering(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Role role,
      @Nullable InfoProvider sensorInfo,
      double dt,
      @Nonnull Steering desiredSteering,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (this.finished) {
         desiredSteering.clear();
         return false;
      } else {
         T currentActiveMotion = this.activeMotion;

         do {
            Objects.requireNonNull(this.activeMotion, "Active motion not set");
            if (this.activeMotion.computeSteering(ref, role, sensorInfo, dt, desiredSteering, componentAccessor)) {
               return true;
            }

            if (this.index + 1 < this.steps.length) {
               this.activateNext(ref, this.index + 1, role, componentAccessor);
            } else {
               if (!this.looped) {
                  break;
               }

               this.activateNext(ref, 0, role, componentAccessor);
            }
         } while (this.activeMotion != currentActiveMotion);

         this.deactivate(ref, role, componentAccessor);
         this.finished = true;
         return false;
      }
   }

   @Override
   public void registerWithSupport(Role role) {
      for (T step : this.steps) {
         step.registerWithSupport(role);
      }
   }

   @Override
   public void motionControllerChanged(
      @Nullable Ref<EntityStore> ref,
      @Nonnull NPCEntity npcComponent,
      MotionController motionController,
      @Nullable ComponentAccessor<EntityStore> componentAccessor
   ) {
      for (T step : this.steps) {
         step.motionControllerChanged(ref, npcComponent, motionController, componentAccessor);
      }
   }

   @Override
   public void loaded(Role role) {
      for (T step : this.steps) {
         step.loaded(role);
      }
   }

   @Override
   public void spawned(Role role) {
      for (T step : this.steps) {
         step.spawned(role);
      }
   }

   @Override
   public void unloaded(Role role) {
      for (T step : this.steps) {
         step.unloaded(role);
      }
   }

   @Override
   public void removed(Role role) {
      for (T step : this.steps) {
         step.removed(role);
      }
   }

   @Override
   public void teleported(Role role, World from, World to) {
      for (T step : this.steps) {
         step.teleported(role, from, to);
      }
   }

   @Override
   public int componentCount() {
      return this.steps.length;
   }

   @Override
   public IAnnotatedComponent getComponent(int index) {
      return this.steps[index];
   }

   @Override
   public void setContext(IAnnotatedComponent parent, int index) {
      for (int i = 0; i < this.steps.length; i++) {
         this.steps[i].setContext(parent, i);
      }
   }

   public void restart() {
      this.index = 0;
      this.finished = false;
   }

   protected void doActivate(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (this.steps.length == 0) {
         throw new IllegalArgumentException("Motion sequence must have steps!");
      } else if (this.index >= 0 && this.index < this.steps.length) {
         this.activeMotion = this.steps[this.index];
         Objects.requireNonNull(this.activeMotion, "Active motion must not be null");
         this.activeMotion.activate(ref, role, componentAccessor);
      } else {
         throw new IndexOutOfBoundsException(
            String.format("Motion sequence index out of range (%s) must be less than size (%s)", this.index, this.steps.length)
         );
      }
   }

   protected void activateNext(@Nonnull Ref<EntityStore> ref, int newIndex, @Nonnull Role role, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      this.activeMotion.deactivate(ref, role, componentAccessor);
      this.index = newIndex;
      this.doActivate(ref, role, componentAccessor);
   }
}
