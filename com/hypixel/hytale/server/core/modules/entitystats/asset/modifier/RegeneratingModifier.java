package com.hypixel.hytale.server.core.modules.entitystats.asset.modifier;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.entitystats.asset.condition.Condition;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Instant;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class RegeneratingModifier {
   public static final BuilderCodec<RegeneratingModifier> CODEC = BuilderCodec.builder(RegeneratingModifier.class, RegeneratingModifier::new)
      .append(
         new KeyedCodec<>("Conditions", new ArrayCodec<>(Condition.CODEC, Condition[]::new)),
         (condition, value) -> condition.conditions = value,
         condition -> condition.conditions
      )
      .add()
      .append(new KeyedCodec<>("Amount", Codec.FLOAT), (condition, value) -> condition.amount = value, condition -> condition.amount)
      .add()
      .build();
   protected Condition[] conditions;
   protected float amount;

   protected RegeneratingModifier() {
   }

   public RegeneratingModifier(Condition[] conditions, float amount) {
      this.conditions = conditions;
      this.amount = amount;
   }

   public float getModifier(ComponentAccessor<EntityStore> store, Ref<EntityStore> ref, Instant currentTime) {
      return Condition.allConditionsMet(store, ref, currentTime, this.conditions) ? this.amount : 1.0F;
   }

   @Nonnull
   @Override
   public String toString() {
      return "RegeneratingModifier{conditions=" + Arrays.toString((Object[])this.conditions) + ", amount=" + this.amount + "}";
   }
}
