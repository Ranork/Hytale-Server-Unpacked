package com.hypixel.hytale.builtin.triggervolumes.effect.builtin;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class VolumeStateEffect extends TriggerEffect {
   @Nonnull
   protected String matchKey = "";
   @Nullable
   protected String matchValue;
   protected double radius = 50.0;
   @Nonnull
   protected TaggedVolumeEffectUtil.Center center = TaggedVolumeEffectUtil.Center.VOLUME;

   @Nonnull
   protected static <T extends VolumeStateEffect> BuilderCodec<T> createCodec(@Nonnull Class<T> type, @Nonnull Supplier<T> supplier) {
      return BuilderCodec.builder(type, supplier, BASE_CODEC)
         .append(new KeyedCodec<>("MatchKey", Codec.STRING, false), (effect, matchKey) -> effect.matchKey = matchKey, effect -> effect.matchKey)
         .add()
         .append(new KeyedCodec<>("MatchValue", Codec.STRING, false), (effect, matchValue) -> effect.matchValue = matchValue, effect -> effect.matchValue)
         .add()
         .append(new KeyedCodec<>("Radius", Codec.DOUBLE, false), (effect, radius) -> effect.radius = radius, effect -> effect.radius)
         .add()
         .append(
            new KeyedCodec<>("Center", new EnumCodec<>(TaggedVolumeEffectUtil.Center.class), false),
            (effect, center) -> effect.center = center,
            effect -> effect.center
         )
         .add()
         .build();
   }

   @Override
   public final void execute(@Nonnull TriggerContext context) {
      TriggerVolumeManager manager = TaggedVolumeEffectUtil.manager(context);
      if (manager != null) {
         boolean changed = false;
         String tagFilter = TaggedVolumeEffectUtil.composeTagFilter(this.matchKey, this.matchValue);

         for (VolumeEntry volume : TaggedVolumeEffectUtil.collectTargets(context, tagFilter, this.radius, this.center)) {
            changed |= this.applyTo(volume);
         }

         if (changed) {
            manager.markSpatialDirty();
            if (this.shouldNotifyViewers()) {
               manager.notifyViewers();
            }
         }
      }
   }

   protected abstract boolean applyTo(@Nonnull VolumeEntry var1);

   protected boolean shouldNotifyViewers() {
      return true;
   }
}
