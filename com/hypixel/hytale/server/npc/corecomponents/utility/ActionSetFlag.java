package com.hypixel.hytale.server.npc.corecomponents.utility;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.corecomponents.utility.builders.BuilderActionSetFlag;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;

public class ActionSetFlag extends ActionBase {
   protected final int flagIndex;
   protected final boolean value;

   public ActionSetFlag(@Nonnull BuilderActionSetFlag builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.flagIndex = builder.getFlagSlot(support);
      this.value = builder.getValue(support);
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);
      role.setFlag(this.flagIndex, this.value);
      return true;
   }
}
