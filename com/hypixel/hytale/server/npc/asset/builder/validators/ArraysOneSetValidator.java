package com.hypixel.hytale.server.npc.asset.builder.validators;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ArraysOneSetValidator extends Validator {
   private final String[] attributes;

   private ArraysOneSetValidator(String[] attributes) {
      this.attributes = attributes;
   }

   public static boolean validate(String[] value1, String[] value2) {
      return arrayContainsNonEmptyString(value1) || arrayContainsNonEmptyString(value2);
   }

   private static boolean arrayContainsNonEmptyString(@Nullable String[] array) {
      if (array != null) {
         for (String value : array) {
            if (value != null && !value.isEmpty()) {
               return true;
            }
         }
      }

      return false;
   }

   @Nonnull
   public static String formatErrorMessage(String attr1, String attr2, String context) {
      return String.format("%s or %s must be provided in %s!", attr1, attr2, context);
   }

   @Nonnull
   public static ArraysOneSetValidator withAttributes(String attribute1, String attribute2) {
      return new ArraysOneSetValidator(new String[]{attribute1, attribute2});
   }

   @Nonnull
   public static ArraysOneSetValidator withAttributes(String[] attributes) {
      return new ArraysOneSetValidator(attributes);
   }
}
