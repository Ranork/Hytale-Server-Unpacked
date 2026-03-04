package com.hypixel.hytale.server.npc.asset.builder.validators;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StringsAtMostOneValidator extends Validator {
   private final String[] attributes;

   private StringsAtMostOneValidator(String[] attributes) {
      this.attributes = attributes;
   }

   public static boolean test(@Nullable String string1, @Nullable String string2) {
      return string1 == null || string1.isEmpty() || string2 == null || string2.isEmpty();
   }

   @Nonnull
   public static String errorMessage(String string1, String string2, String context) {
      return errorMessage(string1, "Value1", string2, "Value2", context);
   }

   @Nonnull
   public static String errorMessage(String string1, String attribute1, String string2, String attribute2, String context) {
      return String.format("Both %s and %s are set to values. At most only 1 of the variables should be set in %s.", attribute1, attribute2, context);
   }

   @Nonnull
   public static StringsAtMostOneValidator withAttributes(String attribute1, String attribute2) {
      return new StringsAtMostOneValidator(new String[]{attribute1, attribute2});
   }

   @Nonnull
   public static StringsAtMostOneValidator withAttributes(String[] attributes) {
      return new StringsAtMostOneValidator(attributes);
   }
}
