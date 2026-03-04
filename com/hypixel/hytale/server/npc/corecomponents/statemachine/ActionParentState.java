package com.hypixel.hytale.server.npc.corecomponents.statemachine;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.StatePair;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.corecomponents.statemachine.builders.BuilderActionParentState;
import com.hypixel.hytale.server.npc.decisionmaker.stateevaluator.StateEvaluator;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;

public class ActionParentState extends ActionBase {
   protected final int state;
   protected final int subState;

   public ActionParentState(@Nonnull BuilderActionParentState builderActionState, @Nonnull BuilderSupport support) {
      super(builderActionState);
      StatePair statePair = builderActionState.getStatePair(support);
      this.state = statePair.getState();
      this.subState = statePair.getSubState();
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);
      StateEvaluator stateEvaluatorComponent = store.getComponent(ref, StateEvaluator.getComponentType());
      if (stateEvaluatorComponent == null || !stateEvaluatorComponent.isActive()) {
         role.getStateSupport().setState(this.state, this.subState, true, false);
      }

      return true;
   }
}
