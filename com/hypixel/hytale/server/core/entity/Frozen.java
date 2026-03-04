package com.hypixel.hytale.server.core.entity;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class Frozen implements Component<EntityStore> {
   @Nonnull
   public static final BuilderCodec<Frozen> CODEC = BuilderCodec.builder(Frozen.class, Frozen::get).build();
   private static final Frozen INSTANCE = new Frozen();

   public static ComponentType<EntityStore, Frozen> getComponentType() {
      return EntityModule.get().getFrozenComponentType();
   }

   public static Frozen get() {
      return INSTANCE;
   }

   private Frozen() {
   }

   @Override
   public Component<EntityStore> clone() {
      return get();
   }
}
