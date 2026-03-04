package com.hypixel.hytale.server.core.modules.entity.component;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class EntityScaleComponent implements Component<EntityStore> {
   public static final BuilderCodec<EntityScaleComponent> CODEC = BuilderCodec.builder(EntityScaleComponent.class, EntityScaleComponent::new)
      .addField(new KeyedCodec<>("Scale", Codec.FLOAT), (o, scale) -> o.scale = scale, o -> o.scale)
      .build();
   private float scale = 1.0F;
   private boolean isNetworkOutdated = true;

   public static ComponentType<EntityStore, EntityScaleComponent> getComponentType() {
      return EntityModule.get().getEntityScaleComponentType();
   }

   private EntityScaleComponent() {
   }

   public EntityScaleComponent(float scale) {
      this.scale = scale;
   }

   public float getScale() {
      return this.scale;
   }

   public void setScale(float scale) {
      this.scale = scale;
      this.isNetworkOutdated = true;
   }

   public boolean consumeNetworkOutdated() {
      boolean temp = this.isNetworkOutdated;
      this.isNetworkOutdated = false;
      return temp;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      return new EntityScaleComponent(this.scale);
   }
}
