package com.hypixel.hytale.server.core.modules.entitystats.asset.condition;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.function.predicate.BiFloatPredicate;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Instant;
import javax.annotation.Nonnull;

public class StatCondition extends EntityStatBoundCondition {
   @Nonnull
   public static final BuilderCodec<StatCondition> CODEC = BuilderCodec.builder(StatCondition.class, StatCondition::new, EntityStatBoundCondition.CODEC)
      .append(new KeyedCodec<>("Amount", Codec.FLOAT), (condition, value) -> condition.amount = value, condition -> condition.amount)
      .documentation("The amount to compare the entity's stat against.")
      .addValidator(Validators.nonNull())
      .add()
      .<StatCondition.StatComparisonType>appendInherited(
         new KeyedCodec<>("Comparison", new EnumCodec<>(StatCondition.StatComparisonType.class)),
         (condition, o) -> condition.comparison = o,
         condition -> condition.comparison,
         (condition, parent) -> condition.comparison = parent.comparison
      )
      .documentation("The comparison type used for evaluating the stat condition.")
      .add()
      .build();
   protected StatCondition.StatComparisonType comparison = StatCondition.StatComparisonType.GTE;
   protected float amount;

   protected StatCondition() {
   }

   public StatCondition(boolean inverse, int stat, float amount) {
      super(inverse, stat);
      this.amount = amount;
   }

   @Override
   public boolean eval0(@Nonnull Ref<EntityStore> ref, @Nonnull Instant currentTime, @Nonnull EntityStatValue statValue) {
      return this.comparison.satisfies(statValue.get(), this.amount);
   }

   @Nonnull
   @Override
   public String toString() {
      return "StatCondition{amount=" + this.amount + "comparison=" + this.comparison.name() + "} " + super.toString();
   }

   public static enum StatComparisonType {
      GTE(">=", (v1, v2) -> v1 >= v2),
      GT(">", (v1, v2) -> v1 > v2),
      LTE("<=", (v1, v2) -> v1 <= v2),
      LT("<", (v1, v2) -> v1 < v2),
      EQUAL("=", (v1, v2) -> v1 == v2);

      private final String prefix;
      private final BiFloatPredicate satisfies;

      private StatComparisonType(String prefix, BiFloatPredicate satisfies) {
         this.prefix = prefix;
         this.satisfies = satisfies;
      }

      public String getPrefix() {
         return this.prefix;
      }

      public boolean satisfies(float compareTo, float f) {
         return this.satisfies.test(compareTo, f);
      }
   }
}
