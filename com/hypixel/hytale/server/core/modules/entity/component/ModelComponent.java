package com.hypixel.hytale.server.core.modules.entity.component;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class ModelComponent implements Component<EntityStore> {
   private final Model model;
   private boolean isNetworkOutdated = true;

   public static ComponentType<EntityStore, ModelComponent> getComponentType() {
      return EntityModule.get().getModelComponentType();
   }

   public ModelComponent(Model model) {
      this.model = model;
   }

   public Model getModel() {
      return this.model;
   }

   public boolean consumeNetworkOutdated() {
      boolean temp = this.isNetworkOutdated;
      this.isNetworkOutdated = false;
      return temp;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      return new ModelComponent(this.model);
   }
}
