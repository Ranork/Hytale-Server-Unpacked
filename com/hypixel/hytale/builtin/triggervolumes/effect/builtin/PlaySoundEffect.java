package com.hypixel.hytale.builtin.triggervolumes.effect.builtin;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEventType;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import org.joml.Vector3d;

public class PlaySoundEffect extends TriggerEffect {
   @Nonnull
   public static final BuilderCodec<PlaySoundEffect> CODEC = BuilderCodec.builder(PlaySoundEffect.class, PlaySoundEffect::new, BASE_CODEC)
      .append(new KeyedCodec<>("SoundEvent", Codec.STRING), (e, v) -> e.soundEventId = v, e -> e.soundEventId)
      .add()
      .append(new KeyedCodec<>("Volume", Codec.FLOAT, false), (e, v) -> e.volumeModifier = v, e -> e.volumeModifier)
      .add()
      .append(new KeyedCodec<>("Pitch", Codec.FLOAT, false), (e, v) -> e.pitchModifier = v, e -> e.pitchModifier)
      .add()
      .build();
   private String soundEventId;
   private float volumeModifier = 1.0F;
   private float pitchModifier = 1.0F;

   @Nonnull
   public static PlaySoundEffect create(@Nonnull TriggerEventType eventType, @Nonnull String soundEventId) {
      PlaySoundEffect effect = new PlaySoundEffect();
      effect.setEventType(eventType);
      effect.soundEventId = soundEventId;
      return effect;
   }

   @Override
   public void execute(@Nonnull TriggerContext context) {
      if (this.soundEventId != null) {
         int soundEventIndex = SoundEvent.getAssetMap().getIndex(this.soundEventId);
         if (soundEventIndex != Integer.MIN_VALUE && soundEventIndex != 0) {
            Ref<EntityStore> entityRef = context.getEntityRef();
            Store<EntityStore> store = context.getStore();
            TransformComponent transform = store.getComponent(entityRef, TransformComponent.getComponentType());
            if (transform != null) {
               Vector3d pos = transform.getPosition();
               SoundUtil.playSoundEvent3d(soundEventIndex, SoundCategory.SFX, pos.x(), pos.y(), pos.z(), this.volumeModifier, this.pitchModifier, store);
            }
         }
      }
   }
}
