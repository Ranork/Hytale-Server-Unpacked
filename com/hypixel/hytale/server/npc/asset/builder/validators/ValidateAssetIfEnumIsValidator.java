package com.hypixel.hytale.server.npc.asset.builder.validators;

import java.util.function.Supplier;
import javax.annotation.Nonnull;

public class ValidateAssetIfEnumIsValidator<E extends Enum<E> & Supplier<String>> extends Validator {
   private final String parameter1;
   private final transient AssetValidator validator;
   private final String parameter2;
   private final E enumValue;

   private ValidateAssetIfEnumIsValidator(String p1, AssetValidator validator, String p2, E value) {
      this.parameter1 = p1;
      this.validator = validator;
      this.parameter2 = p2;
      this.enumValue = value;
   }

   @Nonnull
   public static <E extends Enum<E> & Supplier<String>> ValidateAssetIfEnumIsValidator<E> withAttributes(
      String p1, AssetValidator validator, String p2, E value
   ) {
      return new ValidateAssetIfEnumIsValidator<>(p1, validator, p2, value);
   }
}
