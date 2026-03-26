package com.hypixel.hytale.server.core.modules.entity.component;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class CachedStatsComponent implements Component<EntityStore> {
   private boolean canBreathe;

   public static ComponentType<EntityStore, CachedStatsComponent> getComponentType() {
      return EntityModule.get().getCachedStatsComponentType();
   }

   public boolean isCanBreathe() {
      return this.canBreathe;
   }

   public void setCanBreathe(boolean canBreathe) {
      this.canBreathe = canBreathe;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      CachedStatsComponent component = new CachedStatsComponent();
      component.canBreathe = this.canBreathe;
      return component;
   }
}
