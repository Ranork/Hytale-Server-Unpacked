package com.hypixel.hytale.server.core.modules.entity.component;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class Invulnerable implements Component<EntityStore> {
   public static final Invulnerable INSTANCE = new Invulnerable();
   public static final BuilderCodec<Invulnerable> CODEC = BuilderCodec.builder(Invulnerable.class, () -> INSTANCE).build();

   public static ComponentType<EntityStore, Invulnerable> getComponentType() {
      return EntityModule.get().getInvulnerableComponentType();
   }

   private Invulnerable() {
   }

   @Override
   public Component<EntityStore> clone() {
      return INSTANCE;
   }
}
