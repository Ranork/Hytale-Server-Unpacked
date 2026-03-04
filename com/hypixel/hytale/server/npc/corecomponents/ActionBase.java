package com.hypixel.hytale.server.npc.corecomponents;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;

public abstract class ActionBase extends AnnotatedComponentBase implements Action {
   protected boolean once;
   protected boolean triggered;
   protected boolean active;

   public ActionBase(@Nonnull BuilderActionBase builderActionBase) {
      this.once = builderActionBase.isOnce();
   }

   @Override
   public boolean canExecute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      return !this.once || !this.triggered;
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      this.setOnce();
      return true;
   }

   @Override
   public void activate(Role role, InfoProvider infoProvider) {
      this.active = true;
   }

   @Override
   public void deactivate(Role role, InfoProvider infoProvider) {
      this.active = false;
   }

   @Override
   public boolean isActivated() {
      return this.active;
   }

   @Override
   public boolean isTriggered() {
      return this.triggered;
   }

   @Override
   public void clearOnce() {
      this.triggered = false;
   }

   @Override
   public void setOnce() {
      this.triggered = true;
   }

   @Override
   public boolean processDelay(float dt) {
      return true;
   }
}
