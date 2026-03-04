package com.hypixel.hytale.server.core.command.system.arguments.types;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.ParseResult;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RelativeFloat {
   @Nonnull
   public static final BuilderCodec<RelativeFloat> CODEC = BuilderCodec.builder(RelativeFloat.class, RelativeFloat::new)
      .append(new KeyedCodec<>("Value", Codec.FLOAT), (o, i) -> o.value = i, RelativeFloat::getRawValue)
      .addValidator(Validators.nonNull())
      .add()
      .<Boolean>append(new KeyedCodec<>("Relative", Codec.BOOLEAN), (o, i) -> o.isRelative = i, RelativeFloat::isRelative)
      .addValidator(Validators.nonNull())
      .add()
      .build();
   private float value;
   private boolean isRelative;

   public RelativeFloat(float value, boolean isRelative) {
      this.value = value;
      this.isRelative = isRelative;
   }

   protected RelativeFloat() {
   }

   @Nullable
   public static RelativeFloat parse(@Nonnull String input, @Nonnull ParseResult parseResult) {
      boolean relative = input.contains("~");
      input = input.replaceAll(Pattern.quote("~"), "");

      try {
         float value;
         if (input.isBlank()) {
            value = 0.0F;
         } else {
            value = Float.parseFloat(input);
         }

         return new RelativeFloat(value, relative);
      } catch (Exception var4) {
         parseResult.fail(Message.raw("Invalid float: " + input));
         return null;
      }
   }

   public float getRawValue() {
      return this.value;
   }

   public boolean isRelative() {
      return this.isRelative;
   }

   public float resolve(float baseValue) {
      return this.isRelative ? baseValue + this.value : this.value;
   }

   @Nonnull
   @Override
   public String toString() {
      return (this.isRelative ? "~" : "") + this.value;
   }
}
