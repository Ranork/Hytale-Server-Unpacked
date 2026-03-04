package com.hypixel.hytale.server.npc.corecomponents.statemachine;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.corecomponents.statemachine.builders.BuilderSensorState;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.support.StateSupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.util.ComponentInfo;
import javax.annotation.Nonnull;

public class SensorState extends SensorBase {
   protected final int state;
   protected final boolean defaultSubState;
   protected final int subState;
   protected final boolean componentLocal;
   protected final int componentIndex;

   public SensorState(@Nonnull BuilderSensorState builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.state = builder.getState();
      this.defaultSubState = builder.isDefaultSubState();
      this.subState = builder.getSubStateIndex();
      this.componentLocal = builder.isComponentLocal();
      this.componentIndex = support.getComponentIndex();
   }

   @Override
   public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
      StateSupport stateSupport = role.getStateSupport();
      return this.componentLocal
         ? super.matches(ref, role, dt, store) && stateSupport.isComponentInState(this.componentIndex, this.state)
         : super.matches(ref, role, dt, store) && stateSupport.inState(this.state) && (this.defaultSubState || stateSupport.inSubState(this.subState));
   }

   @Override
   public InfoProvider getSensorInfo() {
      return null;
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
