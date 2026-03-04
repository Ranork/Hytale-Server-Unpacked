package com.hypixel.hytale.server.npc.asset.builder.validators;

import java.util.function.Supplier;
import javax.annotation.Nonnull;

public class ValidateIfEnumIsValidator<E extends Enum<E> & Supplier<String>> extends Validator {
   private final String parameter1;
   private final Validator validator;
   private final String parameter2;
   private final E enumValue;

   private ValidateIfEnumIsValidator(String p1, Validator validator, String p2, E value) {
      this.parameter1 = p1;
      this.validator = validator;
      this.parameter2 = p2;
      this.enumValue = value;
   }

   @Nonnull
   public static <E extends Enum<E> & Supplier<String>> ValidateIfEnumIsValidator<E> withAttributes(String p1, Validator validator, String p2, E value) {
      return new ValidateIfEnumIsValidator<>(p1, validator, p2, value);
   }
}
