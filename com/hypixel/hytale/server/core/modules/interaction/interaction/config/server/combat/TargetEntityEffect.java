package com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.combat;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.Object2DoubleMapCodec;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import javax.annotation.Nonnull;

public class TargetEntityEffect {
   public static final BuilderCodec<TargetEntityEffect> CODEC = BuilderCodec.builder(TargetEntityEffect.class, TargetEntityEffect::new)
      .addField(new KeyedCodec<>("Duration", Codec.DOUBLE), (target, value) -> target.duration = value.floatValue(), target -> (double)target.duration)
      .addField(new KeyedCodec<>("Chance", Codec.DOUBLE), (target, value) -> target.chance = value, target -> target.chance)
      .addField(
         new KeyedCodec<>("EntityTypeDurationModifiers", new Object2DoubleMapCodec<>(Codec.STRING, Object2DoubleOpenHashMap::new)),
         (target, map) -> target.entityTypeDurationModifiers = map,
         target -> target.entityTypeDurationModifiers
      )
      .addField(new KeyedCodec<>("OverlapBehavior", OverlapBehavior.CODEC), (target, value) -> target.overlapBehavior = value, target -> target.overlapBehavior)
      .build();
   protected float duration;
   protected double chance = 1.0;
   protected Object2DoubleMap<String> entityTypeDurationModifiers;
   protected OverlapBehavior overlapBehavior = OverlapBehavior.IGNORE;

   public TargetEntityEffect(float duration, double chance, Object2DoubleMap<String> entityTypeDurationModifiers, OverlapBehavior overlapBehavior) {
      this.duration = duration;
      this.chance = chance;
      this.entityTypeDurationModifiers = entityTypeDurationModifiers;
      this.overlapBehavior = overlapBehavior;
   }

   protected TargetEntityEffect() {
   }

   public float getDuration() {
      return this.duration;
   }

   public double getChance() {
      return this.chance;
   }

   public Object2DoubleMap<String> getEntityTypeDurationModifiers() {
      return this.entityTypeDurationModifiers;
   }

   public OverlapBehavior getOverlapBehavior() {
      return this.overlapBehavior;
   }

   @Nonnull
   @Override
   public String toString() {
      return "TargetEntityEffect{duration="
         + this.duration
         + ", chance="
         + this.chance
         + ", entityTypeDurationModifiers="
         + this.entityTypeDurationModifiers
         + ", overlapBehavior="
         + this.overlapBehavior
         + "}";
   }
}
