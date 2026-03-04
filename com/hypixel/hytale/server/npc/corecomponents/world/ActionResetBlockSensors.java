package com.hypixel.hytale.server.npc.corecomponents.world;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.corecomponents.world.builders.BuilderActionResetBlockSensors;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.support.WorldSupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;

public class ActionResetBlockSensors extends ActionBase {
   protected final int[] blockSets;

   public ActionResetBlockSensors(@Nonnull BuilderActionResetBlockSensors builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.blockSets = builder.getBlockSets(support);

      for (int blockSet : this.blockSets) {
         support.registerBlockSensorResetAction(blockSet);
      }
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);
      WorldSupport worldSupport = role.getWorldSupport();
      if (this.blockSets.length == 0) {
         worldSupport.resetAllBlockSensors();
         return true;
      } else {
         for (int blockSet : this.blockSets) {
            worldSupport.resetBlockSensorFoundBlock(blockSet);
         }

         return true;
      }
   }
}
