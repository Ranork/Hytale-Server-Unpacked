package com.hypixel.hytale.server.core.modules.entity.component;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class FromPrefabInstance implements Component<EntityStore> {
   public static final BuilderCodec<FromPrefabInstance> CODEC = BuilderCodec.builder(FromPrefabInstance.class, FromPrefabInstance::new)
      .append(new KeyedCodec<>("PrefabInstanceId", Codec.INTEGER), (component, i) -> component.prefabInstanceId = i, component -> component.prefabInstanceId)
      .add()
      .build();
   private int prefabInstanceId;

   public static ComponentType<EntityStore, FromPrefabInstance> getComponentType() {
      return EntityModule.get().getFromPrefabInstanceComponentType();
   }

   private FromPrefabInstance() {
   }

   public FromPrefabInstance(int prefabInstanceId) {
      this.prefabInstanceId = prefabInstanceId;
   }

   public int getPrefabInstanceId() {
      return this.prefabInstanceId;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      return new FromPrefabInstance(this.prefabInstanceId);
   }
}
