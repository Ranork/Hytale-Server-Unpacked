package com.hypixel.hytale.server.core.prefab;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class PrefabCopyableComponent implements Component<EntityStore> {
   public static final PrefabCopyableComponent INSTANCE = new PrefabCopyableComponent();
   public static final BuilderCodec<PrefabCopyableComponent> CODEC = BuilderCodec.builder(PrefabCopyableComponent.class, () -> INSTANCE).build();

   public static ComponentType<EntityStore, PrefabCopyableComponent> getComponentType() {
      return EntityModule.get().getPrefabCopyableComponentType();
   }

   public static PrefabCopyableComponent get() {
      return INSTANCE;
   }

   @Override
   public Component<EntityStore> clone() {
      return INSTANCE;
   }
}
