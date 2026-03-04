package com.hypixel.hytale.server.npc.corecomponents.world;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.corecomponents.world.builders.BuilderActionResetSearchRays;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.support.WorldSupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;

public class ActionResetSearchRays extends ActionBase {
   protected final int[] searchRayIds;

   public ActionResetSearchRays(@Nonnull BuilderActionResetSearchRays builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.searchRayIds = builder.getIds(support);
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);
      WorldSupport worldSupport = role.getWorldSupport();
      if (this.searchRayIds.length == 0) {
         worldSupport.resetAllCachedSearchRayPositions();
         return true;
      } else {
         for (int id : this.searchRayIds) {
            worldSupport.resetCachedSearchRayPosition(id);
         }

         return true;
      }
   }
}
