package com.hypixel.hytale.server.npc.corecomponents.lifecycle;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.corecomponents.lifecycle.builders.BuilderActionRole;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.systems.RoleChangeSystem;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ActionRole extends ActionBase {
   protected final int roleIndex;
   protected final String kind;
   protected final boolean changeAppearance;
   @Nullable
   protected final String state;
   @Nullable
   protected final String subState;

   public ActionRole(@Nonnull BuilderActionRole builder, @Nonnull BuilderSupport builderSupport) {
      super(builder);
      this.kind = builder.getRole(builderSupport);
      this.roleIndex = NPCPlugin.get().getIndex(this.kind);
      this.changeAppearance = builder.getChangeAppearance(builderSupport);
      String stateString = builder.getState(builderSupport);
      if (stateString != null) {
         String[] split = stateString.split("\\.");
         this.state = split[0];
         this.subState = split.length > 1 && split[1] != null && !split[1].isEmpty() ? split[1] : null;
      } else {
         this.state = null;
         this.subState = null;
      }
   }

   @Override
   public boolean canExecute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      return super.canExecute(ref, role, sensorInfo, dt, store) && this.roleIndex >= 0;
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);
      if (role.isRoleChangeRequested()) {
         return false;
      } else {
         RoleChangeSystem.requestRoleChange(ref, role, this.roleIndex, this.changeAppearance, this.state, this.subState, store);
         role.setReachedTerminalAction(true);
         return true;
      }
   }
}
