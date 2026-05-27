package com.hypixel.hytale.builtin.triggervolumes.effect.builtin;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEventType;
import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class EnableVolumeEffect extends VolumeStateEffect {
   @Nonnull
   public static final BuilderCodec<EnableVolumeEffect> CODEC = createCodec(EnableVolumeEffect.class, EnableVolumeEffect::new);

   @Nonnull
   public static EnableVolumeEffect create(@Nonnull TriggerEventType eventType) {
      EnableVolumeEffect effect = new EnableVolumeEffect();
      effect.setEventType(eventType);
      return effect;
   }

   @Override
   protected boolean applyTo(@Nonnull VolumeEntry volume) {
      if (volume.isEnabled()) {
         return false;
      } else {
         volume.setEnabled(true);
         return true;
      }
   }
}
