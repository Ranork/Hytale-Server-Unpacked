package com.hypixel.hytale.builtin.ambience.components;

import com.hypixel.hytale.builtin.ambience.AmbiencePlugin;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AmbientEmitterComponent implements Component<EntityStore> {
   @Nonnull
   public static final BuilderCodec<AmbientEmitterComponent> CODEC = BuilderCodec.builder(AmbientEmitterComponent.class, AmbientEmitterComponent::new)
      .append(new KeyedCodec<>("SoundEventId", Codec.STRING), (emitter, s) -> emitter.soundEventId = s, emitter -> emitter.soundEventId)
      .add()
      .build();
   private String soundEventId;
   private Ref<EntityStore> spawnedEmitter;

   public static ComponentType<EntityStore, AmbientEmitterComponent> getComponentType() {
      return AmbiencePlugin.get().getAmbientEmitterComponentType();
   }

   public String getSoundEventId() {
      return this.soundEventId;
   }

   public void setSoundEventId(String soundEventId) {
      this.soundEventId = soundEventId;
   }

   public Ref<EntityStore> getSpawnedEmitter() {
      return this.spawnedEmitter;
   }

   public void setSpawnedEmitter(Ref<EntityStore> spawnedEmitter) {
      this.spawnedEmitter = spawnedEmitter;
   }

   @Nullable
   @Override
   public Component<EntityStore> clone() {
      AmbientEmitterComponent clone = new AmbientEmitterComponent();
      clone.soundEventId = this.soundEventId;
      return clone;
   }
}
