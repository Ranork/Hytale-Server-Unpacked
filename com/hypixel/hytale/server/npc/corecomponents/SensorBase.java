package com.hypixel.hytale.server.npc.corecomponents;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import com.hypixel.hytale.server.npc.role.Role;
import javax.annotation.Nonnull;

public abstract class SensorBase extends AnnotatedComponentBase implements Sensor {
   protected final boolean once;
   protected boolean triggered;

   public SensorBase(@Nonnull BuilderSensorBase builderSensorBase) {
      this.once = builderSensorBase.getOnce();
   }

   @Override
   public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
      return !this.once || !this.triggered;
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
   public boolean isTriggered() {
      return this.triggered;
   }

   @Override
   public boolean processDelay(float dt) {
      return true;
   }
}
