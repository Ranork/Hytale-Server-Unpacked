package com.hypixel.hytale.server.npc.corecomponents.statemachine;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.corecomponents.statemachine.builders.BuilderActionState;
import com.hypixel.hytale.server.npc.decisionmaker.stateevaluator.StateEvaluator;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.util.ComponentInfo;
import javax.annotation.Nonnull;

public class ActionState extends ActionBase {
   protected final int state;
   protected final int subState;
   protected final boolean clearOnce;
   protected final boolean componentLocal;
   protected final int componentIndex;

   public ActionState(@Nonnull BuilderActionState builderActionState, @Nonnull BuilderSupport support) {
      super(builderActionState);
      this.state = builderActionState.getStateIndex();
      this.subState = builderActionState.getSubStateIndex();
      this.clearOnce = builderActionState.isClearState();
      this.componentLocal = builderActionState.isComponentLocal();
      this.componentIndex = support.getComponentIndex();
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);
      if (this.componentLocal) {
         role.getStateSupport().setComponentState(this.componentIndex, this.state);
         return true;
      } else {
         StateEvaluator stateEvaluatorComponent = store.getComponent(ref, StateEvaluator.getComponentType());
         if (stateEvaluatorComponent == null || !stateEvaluatorComponent.isActive()) {
            role.getStateSupport().setState(this.state, this.subState, this.clearOnce, false);
         }

         return true;
      }
   }

   @Override
   public void getInfo(@Nonnull Role role, @Nonnull ComponentInfo holder) {
      if (this.componentLocal) {
         holder.addField("Component local state: " + this.state);
      } else {
         holder.addField("State: " + role.getStateSupport().getStateName(this.state, this.subState));
      }
   }
}
