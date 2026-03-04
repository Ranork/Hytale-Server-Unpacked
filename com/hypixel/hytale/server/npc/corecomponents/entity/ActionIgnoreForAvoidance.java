package com.hypixel.hytale.server.npc.corecomponents.entity;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.corecomponents.entity.builders.BuilderActionIgnoreForAvoidance;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;

public class ActionIgnoreForAvoidance extends ActionBase {
   private final int targetSlot;

   public ActionIgnoreForAvoidance(@Nonnull BuilderActionIgnoreForAvoidance builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.targetSlot = builder.getTargetSlot(support);
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      role.getMarkedEntitySupport().setTargetSlotToIgnoreForAvoidance(this.targetSlot);
      return true;
   }
}
