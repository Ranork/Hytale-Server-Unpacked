package com.hypixel.hytale.server.npc.corecomponents.entity;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.attitude.Attitude;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.corecomponents.entity.builders.BuilderActionOverrideAttitude;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ActionOverrideAttitude extends ActionBase {
   protected final Attitude attitude;
   protected final double duration;

   public ActionOverrideAttitude(@Nonnull BuilderActionOverrideAttitude builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.attitude = builder.getAttitude(support);
      this.duration = builder.getDuration(support);
      support.requireAttitudeOverrideMemory();
   }

   @Override
   public boolean canExecute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      return super.canExecute(ref, role, sensorInfo, dt, store) && sensorInfo != null && sensorInfo.hasPosition();
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nonnull InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);
      Ref<EntityStore> target = sensorInfo.getPositionProvider().getTarget();
      if (target == null) {
         return true;
      } else {
         role.getWorldSupport().overrideAttitude(target, this.attitude, this.duration);
         return true;
      }
   }
}
