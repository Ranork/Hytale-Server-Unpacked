package com.hypixel.hytale.server.npc.corecomponents.utility;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.corecomponents.WeightedAction;
import com.hypixel.hytale.server.npc.corecomponents.utility.builders.BuilderActionRandom;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ActionRandom extends ActionBase {
   @Nonnull
   protected final WeightedAction[] actions;
   @Nonnull
   protected final WeightedAction[] availableActions;
   protected int availableActionsCount;
   protected double totalWeight;
   @Nullable
   protected WeightedAction current;

   public ActionRandom(@Nonnull BuilderActionRandom builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.actions = builder.getActions(support).toArray(WeightedAction[]::new);

      for (WeightedAction action : this.actions) {
         if (action == null) {
            throw new IllegalArgumentException("WeightedAction in Random actions list can't be null");
         }
      }

      this.availableActions = new WeightedAction[this.actions.length];
      this.availableActionsCount = 0;
   }

   @Override
   public boolean canExecute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      int length = this.actions.length;
      if (super.canExecute(ref, role, sensorInfo, dt, store) && length != 0) {
         if (this.current != null) {
            return this.current.canExecute(ref, role, sensorInfo, dt, store);
         } else {
            this.availableActionsCount = 0;
            this.totalWeight = 0.0;

            for (WeightedAction action : this.actions) {
               if (action.canExecute(ref, role, sensorInfo, dt, store)) {
                  this.availableActions[this.availableActionsCount++] = action;
                  this.totalWeight = this.totalWeight + action.getWeight();
               }
            }

            return this.availableActionsCount > 0;
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);
      if (this.availableActionsCount == 0) {
         return true;
      } else {
         if (this.current == null) {
            this.current = this.availableActions[0];
            this.totalWeight = this.totalWeight * ThreadLocalRandom.current().nextDouble();
            this.totalWeight = this.totalWeight - this.current.getWeight();

            for (int i = 1; i < this.availableActionsCount && this.totalWeight >= 0.0; i++) {
               this.current = this.availableActions[i];
               this.totalWeight = this.totalWeight - this.current.getWeight();
            }

            this.current.activate(role, sensorInfo);
         }

         boolean finished = this.current.execute(ref, role, sensorInfo, dt, store);
         if (finished) {
            this.current.clearOnce();
            this.current.deactivate(role, sensorInfo);
            this.current = null;
         }

         return finished;
      }
   }

   @Override
   public void registerWithSupport(Role role) {
      for (WeightedAction action : this.actions) {
         action.registerWithSupport(role);
      }
   }

   @Override
   public void motionControllerChanged(
      @Nullable Ref<EntityStore> ref,
      @Nonnull NPCEntity npcComponent,
      MotionController motionController,
      @Nullable ComponentAccessor<EntityStore> componentAccessor
   ) {
      for (WeightedAction action : this.actions) {
         action.motionControllerChanged(ref, npcComponent, motionController, componentAccessor);
      }
   }

   @Override
   public void loaded(Role role) {
      for (WeightedAction action : this.actions) {
         action.loaded(role);
      }
   }

   @Override
   public void spawned(Role role) {
      for (WeightedAction action : this.actions) {
         action.spawned(role);
      }
   }

   @Override
   public void unloaded(Role role) {
      for (WeightedAction action : this.actions) {
         action.unloaded(role);
      }
   }

   @Override
   public void removed(Role role) {
      for (WeightedAction action : this.actions) {
         action.removed(role);
      }
   }

   @Override
   public void teleported(Role role, World from, World to) {
      for (WeightedAction action : this.actions) {
         action.teleported(role, from, to);
      }
   }
}
