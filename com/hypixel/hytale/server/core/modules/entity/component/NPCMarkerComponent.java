package com.hypixel.hytale.server.core.modules.entity.component;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

@Deprecated(forRemoval = true)
public class NPCMarkerComponent implements Component<EntityStore> {
   private static final NPCMarkerComponent INSTANCE = new NPCMarkerComponent();

   public static ComponentType<EntityStore, NPCMarkerComponent> getComponentType() {
      return EntityModule.get().getNPCMarkerComponentType();
   }

   public static NPCMarkerComponent get() {
      return INSTANCE;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      return this;
   }
}
