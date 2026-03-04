package com.hypixel.hytale.server.core.modules.entity.component;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class PropComponent implements Component<EntityStore> {
   public static final BuilderCodec<PropComponent> CODEC = BuilderCodec.builder(PropComponent.class, PropComponent::new).build();
   private static final PropComponent INSTANCE = new PropComponent();

   public static ComponentType<EntityStore, PropComponent> getComponentType() {
      return EntityModule.get().getPropComponentType();
   }

   public static PropComponent get() {
      return INSTANCE;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      return this;
   }
}
