package com.hypixel.hytale.server.npc.corecomponents.movement;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.corecomponents.movement.builders.BuilderActionRecomputePath;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;

public class ActionRecomputePath extends ActionBase {
   public ActionRecomputePath(@Nonnull BuilderActionRecomputePath builder) {
      super(builder);
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider infoProvider, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, infoProvider, dt, store);
      role.getActiveMotionController().setForceRecomputePath(true);
      return true;
   }
}
