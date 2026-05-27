package com.hypixel.hytale.builtin.triggervolumes.effect.builtin;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEventType;
import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class DeleteVolumeEffect extends VolumeStateEffect {
   @Nonnull
   public static final BuilderCodec<DeleteVolumeEffect> CODEC = createCodec(DeleteVolumeEffect.class, DeleteVolumeEffect::new);

   @Nonnull
   public static DeleteVolumeEffect create(@Nonnull TriggerEventType eventType) {
      DeleteVolumeEffect effect = new DeleteVolumeEffect();
      effect.setEventType(eventType);
      return effect;
   }

   @Override
   protected boolean applyTo(@Nonnull VolumeEntry volume) {
      if (volume.isPendingDestroy()) {
         return false;
      } else {
         volume.markPendingDestroy();
         return true;
      }
   }

   @Override
   protected boolean shouldNotifyViewers() {
      return false;
   }
}
