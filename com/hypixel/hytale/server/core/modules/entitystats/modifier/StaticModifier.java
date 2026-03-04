package com.hypixel.hytale.server.core.modules.entitystats.modifier;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.validation.Validators;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StaticModifier extends Modifier {
   public static final BuilderCodec<StaticModifier> CODEC = BuilderCodec.builder(StaticModifier.class, StaticModifier::new, BASE_CODEC)
      .append(
         new KeyedCodec<>("CalculationType", new EnumCodec<>(StaticModifier.CalculationType.class)),
         (modifier, value) -> modifier.calculationType = value,
         modifier -> modifier.calculationType
      )
      .addValidator(Validators.nonNull())
      .add()
      .append(new KeyedCodec<>("Amount", Codec.FLOAT), (modifier, value) -> modifier.amount = value, modifier -> modifier.amount)
      .add()
      .build();
   public static final BuilderCodec<StaticModifier> ENTITY_CODEC = BuilderCodec.builder(StaticModifier.class, StaticModifier::new)
      .append(
         new KeyedCodec<>("CalculationType", new EnumCodec<>(StaticModifier.CalculationType.class, EnumCodec.EnumStyle.LEGACY)),
         (modifier, value) -> modifier.calculationType = value,
         modifier -> modifier.calculationType
      )
      .setVersionRange(0, 3)
      .addValidator(Validators.nonNull())
      .add()
      .<StaticModifier.CalculationType>append(
         new KeyedCodec<>("CalculationType", new EnumCodec<>(StaticModifier.CalculationType.class)),
         (modifier, value) -> modifier.calculationType = value,
         modifier -> modifier.calculationType
      )
      .setVersionRange(4, 5)
      .addValidator(Validators.nonNull())
      .add()
      .addField(new KeyedCodec<>("Amount", Codec.FLOAT), (modifier, value) -> modifier.amount = value, modifier -> modifier.amount)
      .build();
   protected StaticModifier.CalculationType calculationType;
   protected float amount;

   protected StaticModifier() {
   }

   public StaticModifier(Modifier.ModifierTarget target, StaticModifier.CalculationType calculationType, float amount) {
      super(target);
      this.calculationType = calculationType;
      this.amount = amount;
   }

   public StaticModifier.CalculationType getCalculationType() {
      return this.calculationType;
   }

   public float getAmount() {
      return this.amount;
   }

   @Override
   public float apply(float statValue) {
      return this.calculationType.compute(statValue, this.amount);
   }

   @Nonnull
   @Override
   public com.hypixel.hytale.protocol.Modifier toPacket() {
      com.hypixel.hytale.protocol.Modifier packet = super.toPacket();

      packet.calculationType = switch (this.calculationType) {
         case ADDITIVE -> com.hypixel.hytale.protocol.CalculationType.Additive;
         case MULTIPLICATIVE -> com.hypixel.hytale.protocol.CalculationType.Multiplicative;
      };
      packet.amount = this.amount;
      return packet;
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o == null || this.getClass() != o.getClass()) {
         return false;
      } else if (!super.equals(o)) {
         return false;
      } else {
         StaticModifier that = (StaticModifier)o;
         return Float.compare(that.amount, this.amount) != 0 ? false : this.calculationType == that.calculationType;
      }
   }

   @Override
   public int hashCode() {
      int result = super.hashCode();
      result = 31 * result + (this.calculationType != null ? this.calculationType.hashCode() : 0);
      return 31 * result + (this.amount != 0.0F ? Float.floatToIntBits(this.amount) : 0);
   }

   @Nonnull
   @Override
   public String toString() {
      return "StaticModifier{calculationType=" + this.calculationType + ", amount=" + this.amount + "} " + super.toString();
   }

   public static enum CalculationType {
      ADDITIVE {
         @Override
         public float compute(float value, float amount) {
            return value + amount;
         }
      },
      MULTIPLICATIVE {
         @Override
         public float compute(float value, float amount) {
            return value * amount;
         }
      };

      public abstract float compute(float var1, float var2);

      @Nonnull
      public String createKey(String armor) {
         return armor + "_" + this;
      }
   }
}
