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

public class ActionToggleStateEvaluator extends ActionBase {
   protected final boolean enable;

   public ActionToggleStateEvaluator(@Nonnull BuilderActionToggleStateEvaluator builder) {
      super(builder);
      this.enable = builder.isEnable();
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);
      StateEvaluator stateEvaluatorComponent = store.getComponent(ref, StateEvaluator.getComponentType());

      assert stateEvaluatorComponent != null;

      stateEvaluatorComponent.setActive(this.enable);
      return true;
   }
}
