package com.hypixel.hytale.builtin.triggervolumes.effect.builtin;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEventType;
import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class DisableVolumeEffect extends VolumeStateEffect {
   @Nonnull
   public static final BuilderCodec<DisableVolumeEffect> CODEC = createCodec(DisableVolumeEffect.class, DisableVolumeEffect::new);

   @Nonnull
   public static DisableVolumeEffect create(@Nonnull TriggerEventType eventType) {
      DisableVolumeEffect effect = new DisableVolumeEffect();
      effect.setEventType(eventType);
      return effect;
   }

   @Override
   protected boolean applyTo(@Nonnull VolumeEntry volume) {
      if (!volume.isEnabled()) {
         return false;
      } else {
         volume.setEnabled(false);
         return true;
      }
   }
}
