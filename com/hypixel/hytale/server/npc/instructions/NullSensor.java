package com.hypixel.hytale.server.npc.instructions;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.util.ComponentInfo;
import com.hypixel.hytale.server.npc.util.IAnnotatedComponent;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NullSensor implements Sensor {
   @Override
   public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
      return true;
   }

   @Override
   public InfoProvider getSensorInfo() {
      return null;
   }

   @Override
   public boolean processDelay(float dt) {
      return true;
   }

   @Override
   public void clearOnce() {
   }

   @Override
   public void setOnce() {
   }

   @Override
   public boolean isTriggered() {
      return false;
   }

   @Override
   public void getInfo(Role role, ComponentInfo holder) {
   }

   @Override
   public void setContext(IAnnotatedComponent parent, int index) {
   }

   @Nullable
   @Override
   public IAnnotatedComponent getParent() {
      return null;
   }

   @Override
   public int getIndex() {
      return 0;
   }
}
