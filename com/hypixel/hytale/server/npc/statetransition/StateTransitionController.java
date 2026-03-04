package com.hypixel.hytale.server.npc.statetransition;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderFactory;
import com.hypixel.hytale.server.npc.asset.builder.BuilderManager;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.StateMappingHelper;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.instructions.ActionList;
import com.hypixel.hytale.server.npc.instructions.RoleStateChange;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.statetransition.builders.BuilderStateTransition;
import com.hypixel.hytale.server.npc.statetransition.builders.BuilderStateTransitionController;
import com.hypixel.hytale.server.npc.statetransition.builders.BuilderStateTransitionEdges;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StateTransitionController {
   private final Int2ObjectOpenHashMap<StateTransitionController.IActionListHolder> stateTransitionActions = new Int2ObjectOpenHashMap();
   @Nullable
   private StateTransitionController.IActionListHolder runningActions;

   public StateTransitionController(@Nonnull BuilderStateTransitionController builder, @Nonnull BuilderSupport support) {
      StateMappingHelper stateHelper = support.getStateHelper();

      for (BuilderStateTransition.StateTransition stateTransitionEntry : builder.getStateTransitionEntries(support)) {
         ActionList actions = stateTransitionEntry.getActions();

         for (BuilderStateTransitionEdges.StateTransitionEdges stateTransition : stateTransitionEntry.getStateTransitionEdges()) {
            int priority = stateTransition.getPriority();
            int[] fromStateIndices = stateTransition.getFromStateIndices() != null ? stateTransition.getFromStateIndices() : stateHelper.getAllMainStates();
            int[] toStateIndices = stateTransition.getToStateIndices() != null ? stateTransition.getToStateIndices() : stateHelper.getAllMainStates();

            for (int fromIndex : fromStateIndices) {
               for (int toIndex : toStateIndices) {
                  if (toIndex != fromIndex) {
                     int combinedValue = indexStateTransitionEdge(fromIndex, toIndex);
                     StateTransitionController.IActionListHolder currentList = (StateTransitionController.IActionListHolder)this.stateTransitionActions
                        .get(combinedValue);
                     if (currentList == null) {
                        this.stateTransitionActions.put(combinedValue, new StateTransitionController.PrioritisedActionList(priority, actions));
                     } else {
                        StateTransitionController.CompositeActionList compositeActionList;
                        if (currentList instanceof StateTransitionController.CompositeActionList) {
                           compositeActionList = (StateTransitionController.CompositeActionList)currentList;
                        } else {
                           compositeActionList = new StateTransitionController.CompositeActionList((StateTransitionController.PrioritisedActionList)currentList);
                           this.stateTransitionActions.put(combinedValue, compositeActionList);
                        }

                        compositeActionList.addActionList(priority, actions);
                     }
                  }
               }
            }
         }
      }

      this.stateTransitionActions.trim();
   }

   public void registerWithSupport(Role role) {
      ObjectIterator var2 = this.stateTransitionActions.values().iterator();

      while (var2.hasNext()) {
         StateTransitionController.IActionListHolder actions = (StateTransitionController.IActionListHolder)var2.next();
         actions.registerWithSupport(role);
      }
   }

   public void motionControllerChanged(
      @Nullable Ref<EntityStore> ref,
      @Nonnull NPCEntity npcComponent,
      @Nullable MotionController motionController,
      @Nullable ComponentAccessor<EntityStore> componentAccessor
   ) {
      ObjectIterator var5 = this.stateTransitionActions.values().iterator();

      while (var5.hasNext()) {
         StateTransitionController.IActionListHolder actions = (StateTransitionController.IActionListHolder)var5.next();
         actions.motionControllerChanged(ref, npcComponent, motionController, componentAccessor);
      }
   }

   public void loaded(Role role) {
      ObjectIterator var2 = this.stateTransitionActions.values().iterator();

      while (var2.hasNext()) {
         StateTransitionController.IActionListHolder actions = (StateTransitionController.IActionListHolder)var2.next();
         actions.loaded(role);
      }
   }

   public void spawned(Role role) {
      ObjectIterator var2 = this.stateTransitionActions.values().iterator();

      while (var2.hasNext()) {
         StateTransitionController.IActionListHolder actions = (StateTransitionController.IActionListHolder)var2.next();
         actions.spawned(role);
      }
   }

   public void unloaded(Role role) {
      ObjectIterator var2 = this.stateTransitionActions.values().iterator();

      while (var2.hasNext()) {
         StateTransitionController.IActionListHolder actions = (StateTransitionController.IActionListHolder)var2.next();
         actions.unloaded(role);
      }
   }

   public void removed(Role role) {
      ObjectIterator var2 = this.stateTransitionActions.values().iterator();

      while (var2.hasNext()) {
         StateTransitionController.IActionListHolder actions = (StateTransitionController.IActionListHolder)var2.next();
         actions.removed(role);
      }
   }

   public void teleported(Role role, World from, World to) {
      ObjectIterator var4 = this.stateTransitionActions.values().iterator();

      while (var4.hasNext()) {
         StateTransitionController.IActionListHolder actions = (StateTransitionController.IActionListHolder)var4.next();
         actions.teleported(role, from, to);
      }
   }

   public void clearOnce() {
      ObjectIterator var1 = this.stateTransitionActions.values().iterator();

      while (var1.hasNext()) {
         StateTransitionController.IActionListHolder actions = (StateTransitionController.IActionListHolder)var1.next();
         actions.clearOnce();
      }
   }

   public void initiateStateTransition(int fromState, int toState) {
      this.runningActions = (StateTransitionController.IActionListHolder)this.stateTransitionActions.get(indexStateTransitionEdge(fromState, toState));
   }

   public boolean isRunningTransitionActions() {
      return this.runningActions != null;
   }

   public boolean runTransitionActions(Ref<EntityStore> ref, Role role, double dt, Store<EntityStore> store) {
      if (this.runningActions == null) {
         return false;
      } else if (this.runningActions.canExecute(ref, role, null, dt, store)
         && this.runningActions.execute(ref, role, null, dt, store)
         && this.runningActions.hasCompletedRun()) {
         this.runningActions.clearOnce();
         this.runningActions = null;
         return false;
      } else {
         return true;
      }
   }

   public static void registerFactories(@Nonnull BuilderManager builderManager) {
      BuilderFactory<StateTransitionController> transitionControllerFactory = new BuilderFactory<>(
         StateTransitionController.class, "Type", BuilderStateTransitionController::new
      );
      builderManager.registerFactory(transitionControllerFactory);
      BuilderFactory<BuilderStateTransition.StateTransition> transitionEntryFactory = new BuilderFactory<>(
         BuilderStateTransition.StateTransition.class, "Type", BuilderStateTransition::new
      );
      builderManager.registerFactory(transitionEntryFactory);
      BuilderFactory<BuilderStateTransitionEdges.StateTransitionEdges> transitionFactory = new BuilderFactory<>(
         BuilderStateTransitionEdges.StateTransitionEdges.class, "Type", BuilderStateTransitionEdges::new
      );
      builderManager.registerFactory(transitionFactory);
   }

   public static int indexStateTransitionEdge(int from, int to) {
      return (from << 16) + to;
   }

   private static class CompositeActionList implements StateTransitionController.IActionListHolder {
      private final List<StateTransitionController.PrioritisedActionList> actionLists = new ObjectArrayList();
      private int currentIndex;

      private CompositeActionList(StateTransitionController.PrioritisedActionList initialActionList) {
         this.actionLists.add(initialActionList);
      }

      private void addActionList(int priority, ActionList actionList) {
         for (int i = 0; i < this.actionLists.size(); i++) {
            if (priority > this.actionLists.get(i).priority) {
               this.actionLists.add(i, new StateTransitionController.PrioritisedActionList(priority, actionList));
               return;
            }
         }

         this.actionLists.add(new StateTransitionController.PrioritisedActionList(priority, actionList));
      }

      @Override
      public boolean canExecute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
         if (this.currentIndex >= this.actionLists.size()) {
            this.currentIndex = 0;
         }

         return this.actionLists.get(this.currentIndex).actionList.canExecute(ref, role, sensorInfo, dt, store);
      }

      @Override
      public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
         StateTransitionController.PrioritisedActionList actionList = this.actionLists.get(this.currentIndex);
         if (!actionList.actionList.canExecute(ref, role, sensorInfo, dt, store)) {
            return false;
         } else if (actionList.actionList.execute(ref, role, sensorInfo, dt, store) && actionList.actionList.hasCompletedRun()) {
            this.currentIndex++;
            return true;
         } else {
            return false;
         }
      }

      @Override
      public boolean hasCompletedRun() {
         if (this.currentIndex >= this.actionLists.size()) {
            this.currentIndex = 0;
            return true;
         } else {
            return false;
         }
      }

      @Override
      public void registerWithSupport(Role role) {
         for (StateTransitionController.PrioritisedActionList actionList : this.actionLists) {
            actionList.actionList.registerWithSupport(role);
         }
      }

      @Override
      public void motionControllerChanged(
         @Nullable Ref<EntityStore> ref,
         @Nonnull NPCEntity npcComponent,
         MotionController motionController,
         @Nullable ComponentAccessor<EntityStore> componentAccessor
      ) {
         for (StateTransitionController.PrioritisedActionList actionList : this.actionLists) {
            actionList.actionList.motionControllerChanged(ref, npcComponent, motionController, componentAccessor);
         }
      }

      @Override
      public void loaded(Role role) {
         for (StateTransitionController.PrioritisedActionList actionList : this.actionLists) {
            actionList.actionList.loaded(role);
         }
      }

      @Override
      public void spawned(Role role) {
         for (StateTransitionController.PrioritisedActionList actionList : this.actionLists) {
            actionList.actionList.spawned(role);
         }
      }

      @Override
      public void unloaded(Role role) {
         for (StateTransitionController.PrioritisedActionList actionList : this.actionLists) {
            actionList.actionList.unloaded(role);
         }
      }

      @Override
      public void removed(Role role) {
         for (StateTransitionController.PrioritisedActionList actionList : this.actionLists) {
            actionList.actionList.removed(role);
         }
      }

      @Override
      public void teleported(Role role, World from, World to) {
         for (StateTransitionController.PrioritisedActionList actionList : this.actionLists) {
            actionList.actionList.teleported(role, from, to);
         }
      }

      @Override
      public void clearOnce() {
         for (StateTransitionController.PrioritisedActionList actionList : this.actionLists) {
            actionList.actionList.clearOnce();
         }
      }
   }

   private interface IActionListHolder extends RoleStateChange {
      boolean canExecute(Ref<EntityStore> var1, Role var2, InfoProvider var3, double var4, Store<EntityStore> var6);

      boolean execute(Ref<EntityStore> var1, Role var2, InfoProvider var3, double var4, Store<EntityStore> var6);

      boolean hasCompletedRun();

      void clearOnce();
   }

   private record PrioritisedActionList(int priority, ActionList actionList) implements StateTransitionController.IActionListHolder {
      @Override
      public boolean canExecute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
         return this.actionList.canExecute(ref, role, sensorInfo, dt, store);
      }

      @Override
      public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
         return this.actionList.execute(ref, role, sensorInfo, dt, store);
      }

      @Override
      public boolean hasCompletedRun() {
         return this.actionList.hasCompletedRun();
      }

      @Override
      public void registerWithSupport(Role role) {
         this.actionList.registerWithSupport(role);
      }

      @Override
      public void motionControllerChanged(
         @Nullable Ref<EntityStore> ref,
         @Nonnull NPCEntity npcComponent,
         MotionController motionController,
         @Nullable ComponentAccessor<EntityStore> componentAccessor
      ) {
         this.actionList.motionControllerChanged(ref, npcComponent, motionController, componentAccessor);
      }

      @Override
      public void loaded(Role role) {
         this.actionList.loaded(role);
      }

      @Override
      public void spawned(Role role) {
         this.actionList.spawned(role);
      }

      @Override
      public void unloaded(Role role) {
         this.actionList.unloaded(role);
      }

      @Override
      public void removed(Role role) {
         this.actionList.removed(role);
      }

      @Override
      public void teleported(Role role, World from, World to) {
         this.actionList.teleported(role, from, to);
      }

      @Override
      public void clearOnce() {
         this.actionList.clearOnce();
      }
   }
}
