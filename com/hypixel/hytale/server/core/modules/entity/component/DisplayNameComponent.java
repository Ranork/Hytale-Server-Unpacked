package com.hypixel.hytale.server.core.modules.entity.component;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DisplayNameComponent implements Component<EntityStore> {
   @Nullable
   private Message displayName;

   @Nonnull
   public static ComponentType<EntityStore, DisplayNameComponent> getComponentType() {
      return EntityModule.get().getDisplayNameComponentType();
   }

   public DisplayNameComponent() {
   }

   public DisplayNameComponent(@Nullable Message displayName) {
      this.displayName = displayName;
   }

   @Nullable
   public Message getDisplayName() {
      return this.displayName;
   }

   @Nullable
   @Override
   public Component<EntityStore> clone() {
      return new DisplayNameComponent(this.displayName);
   }
}
