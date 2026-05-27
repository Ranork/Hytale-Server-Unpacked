package com.hypixel.hytale.server.core.modules.entity.damage;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ResistanceModifier implements NetworkSerializable<com.hypixel.hytale.protocol.ResistanceModifier> {
   public static final BuilderCodec<ResistanceModifier> CODEC = BuilderCodec.builder(ResistanceModifier.class, ResistanceModifier::new)
      .append(
         new KeyedCodec<>("CalculationType", new EnumCodec<>(ResistanceModifier.ResistanceCalculationType.class)),
         (modifier, value) -> modifier.calculationType = value,
         modifier -> modifier.calculationType
      )
      .addValidator(Validators.nonNull())
      .documentation("When using 'Percent', the amount represents a percentage reduction. For example, 0.25 equals 25% reduction.")
      .add()
      .append(new KeyedCodec<>("Amount", Codec.FLOAT), (modifier, value) -> modifier.amount = value, modifier -> modifier.amount)
      .add()
      .build();
   protected ResistanceModifier.ResistanceCalculationType calculationType;
   protected float amount;

   protected ResistanceModifier() {
   }

   public ResistanceModifier(ResistanceModifier.ResistanceCalculationType calculationType, float amount) {
      this.calculationType = calculationType;
      this.amount = amount;
   }

   public ResistanceModifier.ResistanceCalculationType getCalculationType() {
      return this.calculationType;
   }

   public float getAmount() {
      return this.amount;
   }

   @Nonnull
   public com.hypixel.hytale.protocol.ResistanceModifier toPacket() {
      com.hypixel.hytale.protocol.ResistanceModifier packet = new com.hypixel.hytale.protocol.ResistanceModifier();

      packet.calculationType = switch (this.calculationType) {
         case FLAT -> com.hypixel.hytale.protocol.ResistanceCalculationType.Flat;
         case PERCENT -> com.hypixel.hytale.protocol.ResistanceCalculationType.Percent;
      };
      packet.amount = this.amount;
      return packet;
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ResistanceModifier that = (ResistanceModifier)o;
         return Float.compare(that.amount, this.amount) != 0 ? false : this.calculationType == that.calculationType;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.calculationType != null ? this.calculationType.hashCode() : 0;
      return 31 * result + (this.amount != 0.0F ? Float.floatToIntBits(this.amount) : 0);
   }

   @Nonnull
   @Override
   public String toString() {
      return "ResistanceModifier{calculationType=" + this.calculationType + ", amount=" + this.amount + "}";
   }

   public static enum ResistanceCalculationType {
      FLAT,
      PERCENT;
   }
}
