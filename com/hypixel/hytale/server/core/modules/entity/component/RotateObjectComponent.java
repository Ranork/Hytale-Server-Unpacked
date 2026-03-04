package com.hypixel.hytale.server.core.modules.entity.component;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class RotateObjectComponent implements Component<EntityStore> {
   @Nonnull
   public static final BuilderCodec<RotateObjectComponent> CODEC = BuilderCodec.builder(RotateObjectComponent.class, RotateObjectComponent::new)
      .append(new KeyedCodec<>("RotationSpeed", Codec.FLOAT), (c, f) -> c.rotationSpeed = f, c -> c.rotationSpeed)
      .add()
      .build();
   private float rotationSpeed;

   @Nonnull
   public static ComponentType<EntityStore, RotateObjectComponent> getComponentType() {
      return EntityModule.get().getRotateObjectComponentType();
   }

   public RotateObjectComponent() {
   }

   public RotateObjectComponent(float rotationSpeed) {
      this.rotationSpeed = rotationSpeed;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      return new RotateObjectComponent(this.rotationSpeed);
   }

   public void setRotationSpeed(float rotationSpeed) {
      this.rotationSpeed = rotationSpeed;
   }

   public float getRotationSpeed() {
      return this.rotationSpeed;
   }
}
