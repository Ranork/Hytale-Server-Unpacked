package com.hypixel.hytale.server.npc.asset.builder.validators;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StringsOneSetValidator extends Validator {
   private final String[] attributes;

   private StringsOneSetValidator(String[] attributes) {
      this.attributes = attributes;
   }

   public static boolean test(@Nullable String string1, @Nullable String string2) {
      boolean str1IsEmpty = string1 == null || string1.isEmpty();
      boolean str2IsEmpty = string2 == null || string2.isEmpty();
      return str1IsEmpty != str2IsEmpty;
   }

   @Nonnull
   public static String errorMessage(String string1, String string2, String context) {
      return errorMessage(string1, "Value1", string2, "Value2", context);
   }

   @Nonnull
   public static String errorMessage(String string1, String attribute1, String string2, String attribute2, String context) {
      return formatErrorMessage(string1, attribute1, string2, attribute2, context);
   }

   @Nonnull
   public static String formatErrorMessage(String string1, String attribute1, String string2, String attribute2, String context) {
      return String.format("Only %s or %s must be set to some value in %s.", attribute1, attribute2, context);
   }

   @Nonnull
   public static StringsOneSetValidator withAttributes(String attribute1, String attribute2) {
      return new StringsOneSetValidator(new String[]{attribute1, attribute2});
   }

   @Nonnull
   public static StringsOneSetValidator withAttributes(String[] attributes) {
      return new StringsOneSetValidator(attributes);
   }
}
