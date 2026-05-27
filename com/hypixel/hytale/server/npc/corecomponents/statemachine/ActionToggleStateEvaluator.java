package com.hypixel.hytale.server.npc.corecomponents.statemachine;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.corecomponents.statemachine.builders.BuilderActionToggleStateEvaluator;
import com.hypixel.hytale.server.npc.decisionmaker.stateevaluator.StateEvaluator;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ActionToggleStateEvaluator extends ActionBase {
   protected final boolean on;

   public ActionToggleStateEvaluator(@Nonnull BuilderActionToggleStateEvaluator builder) {
      super(builder);
      this.on = builder.isOn();
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);
      StateEvaluator stateEvaluatorComponent = store.getComponent(ref, StateEvaluator.getComponentType());
      if (stateEvaluatorComponent != null) {
         stateEvaluatorComponent.setActive(this.on);
      }

      return true;
   }
}
