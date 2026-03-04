package com.hypixel.hytale.server.npc.corecomponents.interaction;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.corecomponents.interaction.builders.BuilderActionLockOnInteractionTarget;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;

public class ActionLockOnInteractionTarget extends ActionBase {
   protected final int targetSlot;

   public ActionLockOnInteractionTarget(@Nonnull BuilderActionLockOnInteractionTarget builderActionBase, @Nonnull BuilderSupport support) {
      super(builderActionBase);
      this.targetSlot = builderActionBase.getTargetSlot(support);
   }

   @Override
   public boolean canExecute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      return super.canExecute(ref, role, sensorInfo, dt, store) && role.getStateSupport().getInteractionIterationTarget() != null;
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);
      Ref<EntityStore> target = role.getStateSupport().getInteractionIterationTarget();
      role.getMarkedEntitySupport().setMarkedEntity(this.targetSlot, target);
      return true;
   }
}
