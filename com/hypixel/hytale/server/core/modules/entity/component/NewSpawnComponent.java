package com.hypixel.hytale.server.core.modules.entity.component;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class NewSpawnComponent implements Component<EntityStore> {
   private float newSpawnWindow;

   public static ComponentType<EntityStore, NewSpawnComponent> getComponentType() {
      return EntityModule.get().getNewSpawnComponentType();
   }

   public NewSpawnComponent(float newSpawnWindow) {
      this.newSpawnWindow = newSpawnWindow;
   }

   public boolean newSpawnWindowPassed(float dt) {
      return (this.newSpawnWindow -= dt) <= 0.0F;
   }

   @Override
   public Component<EntityStore> clone() {
      return new NewSpawnComponent(this.newSpawnWindow);
   }
}
