package com.hypixel.hytale.server.core.modules.entity.item;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class PreventItemMerging implements Component<EntityStore> {
   @Nonnull
   public static final PreventItemMerging INSTANCE = new PreventItemMerging();
   @Nonnull
   public static final BuilderCodec<PreventItemMerging> CODEC = BuilderCodec.builder(PreventItemMerging.class, () -> INSTANCE).build();

   @Nonnull
   public static ComponentType<EntityStore, PreventItemMerging> getComponentType() {
      return EntityModule.get().getPreventItemMergingType();
   }

   private PreventItemMerging() {
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      return INSTANCE;
   }
}
