package com.hypixel.hytale.builtin.buildertools.prefabeditor;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class PrefabAnchor implements Component<EntityStore> {
   public static final PrefabAnchor INSTANCE = new PrefabAnchor();
   public static final BuilderCodec<PrefabAnchor> CODEC = BuilderCodec.builder(PrefabAnchor.class, () -> INSTANCE).build();

   public static ComponentType<EntityStore, PrefabAnchor> getComponentType() {
      return BuilderToolsPlugin.get().getPrefabAnchorComponentType();
   }

   private PrefabAnchor() {
   }

   @Override
   public Component<EntityStore> clone() {
      return INSTANCE;
   }
}
