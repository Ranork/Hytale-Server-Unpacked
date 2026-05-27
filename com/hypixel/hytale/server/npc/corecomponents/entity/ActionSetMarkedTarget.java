package com.hypixel.hytale.server.npc.corecomponents.entity;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.corecomponents.entity.builders.BuilderActionSetMarkedTarget;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.IPositionProvider;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ActionSetMarkedTarget extends ActionBase {
   protected final int targetSlot;

   public ActionSetMarkedTarget(@Nonnull BuilderActionSetMarkedTarget builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.targetSlot = builder.getTargetSlot(support);
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);
      IPositionProvider positionProvider = sensorInfo != null ? sensorInfo.getPositionProvider() : null;
      Ref<EntityStore> target = positionProvider != null ? positionProvider.getTarget() : null;
      role.getMarkedEntitySupport().setMarkedEntity(this.targetSlot, target);
      return true;
   }
}
