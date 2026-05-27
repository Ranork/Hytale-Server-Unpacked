package com.hypixel.hytale.builtin.triggervolumes.effect.builtin.conditions;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerCondition;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEventType;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nonnull;

public class RandomChanceCondition extends TriggerCondition {
   @Nonnull
   public static final BuilderCodec<RandomChanceCondition> CODEC = BuilderCodec.builder(RandomChanceCondition.class, RandomChanceCondition::new, BASE_CODEC)
      .append(new KeyedCodec<>("Chance", Codec.FLOAT, false), (c, v) -> c.chance = v, c -> c.chance)
      .add()
      .build();
   private float chance = 1.0F;

   @Nonnull
   public static RandomChanceCondition create(@Nonnull TriggerEventType eventType, float chance) {
      RandomChanceCondition condition = new RandomChanceCondition();
      condition.setEventType(eventType);
      condition.chance = chance;
      return condition;
   }

   @Override
   public boolean test(@Nonnull TriggerContext context) {
      if (this.chance <= 0.0F) {
         return false;
      } else {
         return this.chance >= 1.0F ? true : ThreadLocalRandom.current().nextFloat() < this.chance;
      }
   }
}
