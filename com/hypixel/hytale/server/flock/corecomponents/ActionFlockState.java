package com.hypixel.hytale.server.flock.corecomponents;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.flock.corecomponents.builders.BuilderActionFlockState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ActionFlockState extends ActionBase {
   protected final String state;
   @Nullable
   protected final String subState;

   public ActionFlockState(@Nonnull BuilderActionFlockState builderActionFlockState, @Nonnull BuilderSupport support) {
      super(builderActionFlockState);
      String[] split = builderActionFlockState.getState(support).split("\\.");
      this.state = split[0];
      this.subState = split.length > 1 && split[1] != null && !split[1].isEmpty() ? split[1] : null;
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);
      role.getStateSupport().flockSetState(ref, this.state, this.subState, store);
      return true;
   }
}
