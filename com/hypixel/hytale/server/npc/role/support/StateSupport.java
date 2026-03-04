package com.hypixel.hytale.server.npc.role.support;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.InteractableUpdate;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.group.EntityGroup;
import com.hypixel.hytale.server.core.modules.entity.component.Interactable;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSettings;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.flock.FlockPlugin;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.StateMappingHelper;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.builders.BuilderRole;
import com.hypixel.hytale.server.npc.statetransition.StateTransitionController;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StateSupport {
   public static final int NO_STATE = Integer.MIN_VALUE;
   @Nullable
   protected static final ComponentType<EntityStore, NPCEntity> NPC_COMPONENT_TYPE = NPCEntity.getComponentType();
   protected final StateMappingHelper stateHelper;
   protected final int startState;
   protected final int startSubState;
   protected int state;
   protected int subState;
   protected Int2IntMap componentLocalStateMachines;
   protected BitSet localStateMachineAutoResetStates;
   protected final Int2ObjectMap<IntSet> busyStates;
   protected final HashSet<String> missingStates = new HashSet<>();
   protected boolean needClearOnce;
   protected Set<Ref<EntityStore>> interactablePlayers;
   protected Set<Ref<EntityStore>> interactedPlayers;
   protected Map<Ref<EntityStore>, String> contextualInteractions;
   protected String lastHint;
   @Nullable
   protected Ref<EntityStore> interactionIterationTarget;
   @Nullable
   protected final StateTransitionController stateTransitionController;

   public StateSupport(@Nonnull BuilderRole builder, @Nonnull BuilderSupport support) {
      this.stateHelper = builder.getStateMappingHelper();
      this.busyStates = builder.getBusyStates();
      this.stateTransitionController = builder.getStateTransitionController(support);
      this.startState = builder.getStartStateIndex();
      this.startSubState = builder.getStartSubStateIndex();
   }

   @Nullable
   public StateTransitionController getStateTransitionController() {
      return this.stateTransitionController;
   }

   public StateMappingHelper getStateHelper() {
      return this.stateHelper;
   }

   public void postRoleBuilt(@Nonnull BuilderSupport builderSupport) {
      if (builderSupport.hasComponentLocalStateMachines()) {
         this.componentLocalStateMachines = builderSupport.getComponentLocalStateMachines();
         this.localStateMachineAutoResetStates = builderSupport.getLocalStateMachineAutoResetStates();
      }

      if (builderSupport.isTrackInteractions()) {
         this.interactedPlayers = new HashSet<>();
         this.interactablePlayers = new HashSet<>();
         this.contextualInteractions = new HashMap<>();
      }

      if (this.busyStates != null) {
         String defaultSubState = this.stateHelper.getDefaultSubState();
         this.busyStates.forEach((key, value) -> {
            int defaultSubStateIndex = this.stateHelper.getSubStateIndex(key, defaultSubState);
            if (value.contains(defaultSubStateIndex) && value.size() == 1) {
               int maxIndex = this.stateHelper.getHighestSubStateIndex(key);

               for (int i = 0; i <= maxIndex; i++) {
                  value.add(i);
               }
            }
         });
      }
   }

   public void update(@Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (this.contextualInteractions != null) {
         this.contextualInteractions.clear();
      }

      if (this.interactablePlayers != null) {
         Iterator<Ref<EntityStore>> it = this.interactablePlayers.iterator();

         while (it.hasNext()) {
            Ref<EntityStore> ref = it.next();
            if (ref.isValid()) {
               Player playerComponent = componentAccessor.getComponent(ref, Player.getComponentType());
               if (playerComponent == null) {
                  it.remove();
               } else if (playerComponent.getGameMode() == GameMode.Creative) {
                  PlayerSettings playerSettingsComponent = componentAccessor.getComponent(ref, PlayerSettings.getComponentType());
                  if (playerSettingsComponent == null || !playerSettingsComponent.creativeSettings().allowNPCDetection()) {
                     it.remove();
                  }
               }
            }
         }
      }
   }

   public boolean pollNeedClearOnce() {
      boolean ret = this.needClearOnce;
      this.needClearOnce = false;
      return ret;
   }

   public boolean inState(int state) {
      return this.state == state;
   }

   public boolean inSubState(int subState) {
      return this.subState == subState;
   }

   public boolean inState(int state, int subState) {
      return this.inState(state) && (subState == Integer.MIN_VALUE || this.inSubState(subState));
   }

   public boolean inState(String state, String subState) {
      int stateIndex = this.stateHelper.getStateIndex(state);
      if (stateIndex < 0) {
         return false;
      } else {
         int subStateIndex = this.stateHelper.getSubStateIndex(stateIndex, subState);
         return subStateIndex >= 0 && this.inState(stateIndex, subStateIndex);
      }
   }

   @Nonnull
   public String getStateName() {
      return this.getStateName(this.state, this.subState);
   }

   @Nonnull
   public String getStateName(int state, int subState) {
      return this.stateHelper.getStateName(state) + "." + this.stateHelper.getSubStateName(state, subState);
   }

   public int getStateIndex() {
      return this.state;
   }

   public int getSubStateIndex() {
      return this.subState;
   }

   public void appendStateName(@Nonnull StringBuilder builder) {
      builder.append(this.stateHelper.getStateName(this.state)).append('.').append(this.stateHelper.getSubStateName(this.state, this.subState));
   }

   public void setState(int state, int subState, boolean clearOnce, boolean skipTransition) {
      int oldState = this.state;
      this.state = state;
      this.subState = subState;
      if (clearOnce) {
         this.needClearOnce = true;
      }

      if (!skipTransition && oldState != state && this.stateTransitionController != null) {
         this.stateTransitionController.initiateStateTransition(oldState, state);
      }
   }

   public void setState(
      @Nonnull Ref<EntityStore> ref, @Nonnull String state, @Nullable String subState, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      int index = this.stateHelper.getStateIndex(state);
      if (index >= 0) {
         if (subState == null) {
            subState = this.stateHelper.getDefaultSubState();
         }

         int subStateIndex = this.stateHelper.getSubStateIndex(index, subState);
         if (subStateIndex >= 0) {
            this.setState(index, subStateIndex, true, false);
            return;
         }
      }

      if (!this.missingStates.add(state)) {
         NPCEntity npcComponent = componentAccessor.getComponent(ref, NPCEntity.getComponentType());

         assert npcComponent != null;

         NPCPlugin.get()
            .getLogger()
            .at(Level.WARNING)
            .log("State '%s.%s' in '%s' does not exist and was set by an external call", state, subState, npcComponent.getRoleName());
      }
   }

   public void setSubState(String subState) {
      int subStateIndex = this.stateHelper.getSubStateIndex(this.state, subState);
      if (subStateIndex >= 0) {
         this.setState(this.state, subStateIndex, true, true);
      }
   }

   public boolean isComponentInState(int componentIndex, int targetState) {
      int state = this.componentLocalStateMachines.get(componentIndex);
      if (state == Integer.MIN_VALUE) {
         throw new IllegalArgumentException("Querying for a component index that doesn't exist");
      } else {
         return state == targetState;
      }
   }

   public void setComponentState(int componentIndex, int targetState) {
      this.componentLocalStateMachines.put(componentIndex, targetState);
   }

   public void resetLocalStateMachines() {
      if (this.localStateMachineAutoResetStates != null) {
         for (int i = this.localStateMachineAutoResetStates.nextSetBit(0); i >= 0; i = this.localStateMachineAutoResetStates.nextSetBit(i + 1)) {
            this.componentLocalStateMachines.put(i, 0);
            if (i == Integer.MAX_VALUE) {
               break;
            }
         }
      }
   }

   public void flockSetState(Ref<EntityStore> ref, @Nonnull String state, @Nullable String subState, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      Ref<EntityStore> flockReference = FlockPlugin.getFlockReference(ref, componentAccessor);
      if (flockReference != null) {
         EntityGroup entityGroupComponent = componentAccessor.getComponent(flockReference, EntityGroup.getComponentType());

         assert entityGroupComponent != null;

         entityGroupComponent.forEachMemberExcludingSelf((member, sender, _state, _substate) -> {
            Store<EntityStore> memberStore = member.getStore();
            NPCEntity npcComponent = memberStore.getComponent(member, NPC_COMPONENT_TYPE);
            if (npcComponent != null) {
               npcComponent.onFlockSetState(member, _state, _substate, memberStore);
            }
         }, ref, state, subState);
      }
   }

   public boolean isInBusyState() {
      if (this.busyStates == null) {
         return false;
      } else {
         IntSet busySubStates = (IntSet)this.busyStates.get(this.state);
         return busySubStates != null && busySubStates.contains(this.subState);
      }
   }

   public void addContextualInteraction(@Nonnull Ref<EntityStore> playerRef, @Nonnull String context) {
      if (this.contextualInteractions != null) {
         this.contextualInteractions.put(playerRef, context);
      }
   }

   public boolean hasContextualInteraction(@Nonnull Ref<EntityStore> playerReference, @Nonnull String context) {
      String contextualInteraction = this.contextualInteractions.get(playerReference);
      return contextualInteraction == null ? false : contextualInteraction.equals(context);
   }

   public void addInteraction(@Nonnull Player player) {
      this.interactedPlayers.add(player.getReference());
   }

   public boolean consumeInteraction(@Nonnull Ref<EntityStore> playerReference) {
      return this.interactedPlayers.remove(playerReference);
   }

   public void setInteractable(@Nonnull Ref<EntityStore> playerReference, boolean interactable) {
      if (interactable) {
         this.interactablePlayers.add(playerReference);
      } else {
         this.interactablePlayers.remove(playerReference);
      }
   }

   public void setInteractable(
      @Nonnull Ref<EntityStore> entityRef,
      @Nonnull Ref<EntityStore> playerReference,
      boolean interactable,
      @Nullable String hint,
      boolean showPrompt,
      @Nonnull Store<EntityStore> store
   ) {
      boolean wasInteractable = this.interactablePlayers.contains(playerReference);
      if (interactable) {
         this.interactablePlayers.add(playerReference);
      } else {
         this.interactablePlayers.remove(playerReference);
      }

      boolean hasComponent = store.getArchetype(entityRef).contains(Interactable.getComponentType());
      if (interactable) {
         if (!showPrompt) {
            hint = "";
         }

         boolean needsHint = hint != null && !hint.equals(this.lastHint);
         if (!hasComponent) {
            store.ensureComponent(entityRef, Interactable.getComponentType());
            needsHint = hint != null && !hint.isEmpty();
         }

         if (needsHint) {
            this.sendInteractionHintToPlayer(entityRef, playerReference, hint, store);
            this.lastHint = hint;
         }
      } else if (hasComponent && this.interactablePlayers.isEmpty()) {
         store.removeComponent(entityRef, Interactable.getComponentType());
      }
   }

   private void sendInteractionHintToPlayer(
      @Nonnull Ref<EntityStore> entityRef, @Nonnull Ref<EntityStore> playerReference, @Nonnull String hint, @Nonnull Store<EntityStore> store
   ) {
      EntityTrackerSystems.EntityViewer viewerComponent = store.getComponent(playerReference, EntityTrackerSystems.EntityViewer.getComponentType());
      if (viewerComponent != null && viewerComponent.visible.contains(entityRef)) {
         InteractableUpdate update = new InteractableUpdate(hint);
         viewerComponent.queueUpdate(entityRef, update);
      }
   }

   public void setInteractionIterationTarget(@Nullable Ref<EntityStore> playerReference) {
      this.interactionIterationTarget = playerReference;
   }

   @Nullable
   public Ref<EntityStore> getInteractionIterationTarget() {
      return this.interactionIterationTarget;
   }

   public boolean willInteractWith(@Nonnull Ref<EntityStore> playerReference) {
      return this.interactablePlayers != null && this.interactablePlayers.contains(playerReference) && !this.isInBusyState();
   }

   public boolean runTransitionActions(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
      return this.stateTransitionController == null ? false : this.stateTransitionController.runTransitionActions(ref, role, dt, store);
   }

   public boolean isRunningTransitionActions() {
      return this.stateTransitionController != null && this.stateTransitionController.isRunningTransitionActions();
   }

   public void activate() {
      this.setState(this.startState, this.startSubState, true, true);
   }
}
