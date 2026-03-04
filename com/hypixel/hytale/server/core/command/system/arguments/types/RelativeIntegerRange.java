package com.hypixel.hytale.server.core.command.system.arguments.types;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nonnull;

public class RelativeIntegerRange {
   @Nonnull
   public static final BuilderCodec<RelativeIntegerRange> CODEC = BuilderCodec.builder(RelativeIntegerRange.class, RelativeIntegerRange::new)
      .append(new KeyedCodec<>("Min", RelativeInteger.CODEC), (o, i) -> o.min = i, o -> o.min)
      .addValidator(Validators.nonNull())
      .add()
      .<RelativeInteger>append(new KeyedCodec<>("Max", RelativeInteger.CODEC), (o, i) -> o.max = i, o -> o.max)
      .addValidator(Validators.nonNull())
      .add()
      .build();
   private RelativeInteger min;
   private RelativeInteger max;

   public RelativeIntegerRange(@Nonnull RelativeInteger min, @Nonnull RelativeInteger max) {
      this.min = min;
      this.max = max;
   }

   protected RelativeIntegerRange() {
   }

   public RelativeIntegerRange(int min, int max) {
      this.min = new RelativeInteger(min, false);
      this.max = new RelativeInteger(max, false);
   }

   public int getNumberInRange(int base) {
      return this.min.getRawValue() == this.max.getRawValue()
         ? this.min.resolve(base)
         : ThreadLocalRandom.current().nextInt(this.min.resolve(base), this.max.resolve(base) + 1);
   }

   @Nonnull
   @Override
   public String toString() {
      return "{ Minimum: " + this.min + ", Maximum: " + this.max + " }";
   }
}
