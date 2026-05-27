package com.hypixel.hytale.server.core.modules.entity.component;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PersistentDisplayName implements Component<EntityStore> {
   @Nonnull
   public static final BuilderCodec<PersistentDisplayName> CODEC = BuilderCodec.builder(PersistentDisplayName.class, PersistentDisplayName::new)
      .appendInherited(new KeyedCodec<>("DisplayName", Message.CODEC), (e, s) -> e.displayName = s, e -> e.displayName, (e, p) -> e.displayName = p.displayName)
      .documentation("The value of the display name.")
      .add()
      .build();
   @Nullable
   private Message displayName;

   @Nonnull
   public static ComponentType<EntityStore, PersistentDisplayName> getComponentType() {
      return EntityModule.get().getPersistentDisplayNameComponentType();
   }

   private PersistentDisplayName() {
   }

   public PersistentDisplayName(@Nullable Message displayName) {
      this.displayName = displayName;
   }

   @Nullable
   public Message getDisplayName() {
      return this.displayName;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      return new PersistentDisplayName(this.displayName);
   }
}
