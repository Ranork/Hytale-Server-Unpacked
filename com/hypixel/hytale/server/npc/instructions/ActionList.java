package com.hypixel.hytale.server.npc.instructions;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.util.IAnnotatedComponent;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ActionList {
   public static final ActionList EMPTY_ACTION_LIST = new ActionList(Action.EMPTY_ARRAY);
   @Nonnull
   protected final Action[] actions;
   protected boolean blocking;
   protected boolean atomic;
   protected int actionIndex;

   public ActionList(@Nonnull Action[] actions) {
      this.actions = actions;
      Objects.requireNonNull(actions, "Action array in sequence must not be null");

      for (Action action : actions) {
         Objects.requireNonNull(action, "Action in sequence can't be null");
      }
   }

   public void setBlocking(boolean blocking) {
      this.blocking = blocking;
   }

   public void setAtomic(boolean atomic) {
      this.atomic = atomic;
   }

   public boolean canExecute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      if (this.actions.length == 0) {
         return false;
      } else if (this.blocking) {
         if (this.actionIndex >= this.actions.length) {
            this.actionIndex = 0;
         }

         return this.actions[this.actionIndex].canExecute(ref, role, sensorInfo, dt, store);
      } else {
         for (Action action : this.actions) {
            if (action.canExecute(ref, role, sensorInfo, dt, store)) {
               if (!this.atomic) {
                  return true;
               }
            } else if (this.atomic) {
               return false;
            }
         }

         return this.atomic;
      }
   }

   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      if (this.blocking) {
         Action action = this.actions[this.actionIndex];
         if (!action.canExecute(ref, role, sensorInfo, dt, store)) {
            return false;
         } else {
            if (!action.isActivated()) {
               action.activate(role, sensorInfo);
            }

            if (!action.execute(ref, role, sensorInfo, dt, store)) {
               return false;
            } else {
               action.deactivate(role, sensorInfo);
               this.actionIndex++;
               return this.actionIndex >= this.actions.length;
            }
         }
      } else {
         for (Action action : this.actions) {
            if (action.canExecute(ref, role, sensorInfo, dt, store)) {
               if (!action.isActivated()) {
                  action.activate(role, sensorInfo);
               }

               action.execute(ref, role, sensorInfo, dt, store);
            } else if (action.isActivated()) {
               action.deactivate(role, sensorInfo);
            }
         }

         return true;
      }
   }

   public boolean hasCompletedRun() {
      if (this.actionIndex >= this.actions.length) {
         this.actionIndex = 0;
         return true;
      } else {
         return false;
      }
   }

   public void setContext(IAnnotatedComponent parent) {
      for (int i = 0; i < this.actions.length; i++) {
         this.actions[i].setContext(parent, i);
      }
   }

   public void registerWithSupport(Role role) {
      for (Action action : this.actions) {
         action.registerWithSupport(role);
      }
   }

   public void motionControllerChanged(
      @Nullable Ref<EntityStore> ref,
      @Nonnull NPCEntity npcComponent,
      MotionController motionController,
      @Nullable ComponentAccessor<EntityStore> componentAccessor
   ) {
      for (Action action : this.actions) {
         action.motionControllerChanged(ref, npcComponent, motionController, componentAccessor);
      }
   }

   public void loaded(Role role) {
      for (Action action : this.actions) {
         action.loaded(role);
      }
   }

   public void spawned(Role role) {
      for (Action action : this.actions) {
         action.spawned(role);
      }
   }

   public void unloaded(Role role) {
      for (Action action : this.actions) {
         action.unloaded(role);
      }
   }

   public void removed(Role role) {
      for (Action action : this.actions) {
         action.removed(role);
      }
   }

   public void teleported(Role role, World from, World to) {
      for (Action action : this.actions) {
         action.teleported(role, from, to);
      }
   }

   public void clearOnce() {
      for (Action action : this.actions) {
         action.clearOnce();
      }

      this.actionIndex = 0;
   }

   public void onEndMotion() {
      if (!this.blocking) {
         this.clearOnce();
      }
   }

   public void setOnce() {
      for (Action action : this.actions) {
         action.setOnce();
      }
   }

   public int actionCount() {
      return this.actions.length;
   }

   public IAnnotatedComponent getComponent(int index) {
      return this.actions[index];
   }
}
