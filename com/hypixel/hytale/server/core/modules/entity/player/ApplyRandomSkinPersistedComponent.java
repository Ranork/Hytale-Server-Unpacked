package com.hypixel.hytale.server.core.modules.entity.player;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class ApplyRandomSkinPersistedComponent implements Component<EntityStore> {
   public static final ApplyRandomSkinPersistedComponent INSTANCE = new ApplyRandomSkinPersistedComponent();
   public static final BuilderCodec<ApplyRandomSkinPersistedComponent> CODEC = BuilderCodec.builder(ApplyRandomSkinPersistedComponent.class, () -> INSTANCE)
      .build();

   public static ComponentType<EntityStore, ApplyRandomSkinPersistedComponent> getComponentType() {
      return EntityModule.get().getApplyRandomSkinPersistedComponent();
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      return this;
   }
}
