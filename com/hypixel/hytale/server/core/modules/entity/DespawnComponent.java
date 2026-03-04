package com.hypixel.hytale.server.core.modules.entity;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Duration;
import java.time.Instant;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DespawnComponent implements Component<EntityStore> {
   public static final BuilderCodec<DespawnComponent> CODEC = BuilderCodec.builder(DespawnComponent.class, DespawnComponent::new)
      .append(
         new KeyedCodec<>("Despawn", Codec.INSTANT),
         (despawnComponent, instant) -> despawnComponent.timeToDespawnAt = instant,
         despawnComponent -> despawnComponent.timeToDespawnAt
      )
      .add()
      .build();
   @Nullable
   private Instant timeToDespawnAt;

   public static ComponentType<EntityStore, DespawnComponent> getComponentType() {
      return EntityModule.get().getDespawnComponentType();
   }

   public DespawnComponent() {
      this(null);
   }

   public DespawnComponent(@Nullable Instant timeToDespawnAt) {
      this.timeToDespawnAt = timeToDespawnAt;
   }

   public void setDespawn(Instant timeToDespawnAt) {
      this.timeToDespawnAt = timeToDespawnAt;
   }

   public void setDespawnTo(@Nonnull Instant from, float additionalSeconds) {
      this.timeToDespawnAt = from.plusNanos((long)(additionalSeconds * 1.0E9F));
   }

   @Nullable
   public Instant getDespawn() {
      return this.timeToDespawnAt;
   }

   @Nonnull
   public static DespawnComponent despawnInSeconds(@Nonnull TimeResource time, int seconds) {
      return new DespawnComponent(time.getNow().plus(Duration.ofSeconds(seconds)));
   }

   @Nonnull
   public static DespawnComponent despawnInSeconds(@Nonnull TimeResource time, float seconds) {
      return new DespawnComponent(time.getNow().plusNanos((long)(seconds * 1.0E9F)));
   }

   @Nonnull
   public static DespawnComponent despawnInMilliseconds(@Nonnull TimeResource time, long milliseconds) {
      return new DespawnComponent(time.getNow().plus(Duration.ofMillis(milliseconds)));
   }

   public static void trySetDespawn(
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull TimeResource timeResource,
      @Nonnull Ref<EntityStore> ref,
      @Nullable DespawnComponent despawnComponent,
      @Nullable Float newLifetime
   ) {
      if (despawnComponent != null) {
         if (newLifetime != null) {
            despawnComponent.setDespawnTo(timeResource.getNow(), newLifetime);
         } else {
            commandBuffer.removeComponent(ref, getComponentType());
         }
      } else {
         commandBuffer.putComponent(ref, getComponentType(), despawnInSeconds(timeResource, newLifetime));
      }
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      return new DespawnComponent(this.timeToDespawnAt);
   }
}
