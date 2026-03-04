package com.hypixel.hytale.server.npc.corecomponents.utility;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.corecomponents.utility.builders.BuilderActionResetInstructions;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;

public class ActionResetInstructions extends ActionBase {
   protected final int[] instructions;

   public ActionResetInstructions(@Nonnull BuilderActionResetInstructions builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.instructions = builder.getInstructions(support);
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);
      role.addDeferredAction((_ref, _role, _dt, _store) -> this.resetInstructions(_role, _dt));
      return true;
   }

   protected boolean resetInstructions(@Nonnull Role role, double dt) {
      if (this.instructions.length == 0) {
         role.resetAllInstructions();
         return true;
      } else {
         for (int instruction : this.instructions) {
            role.resetInstruction(instruction);
         }

         return true;
      }
   }
}
