package com.hypixel.hytale.server.npc.asset.builder.validators;

import javax.annotation.Nonnull;

public class ExactlyOneZeroValidator extends Validator {
   private final String attribute1;
   private final String attribute2;

   private ExactlyOneZeroValidator(String attribute1, String attribute2) {
      this.attribute1 = attribute1;
      this.attribute2 = attribute2;
   }

   public static boolean test(double value1, double value2) {
      return value1 == 0.0 != (value2 == 0.0);
   }

   @Nonnull
   public static String formatErrorMessage(String attribute1, double value1, String attribute2, double value2, String context) {
      return String.format("Exactly one of '%s'(=%s) and '%s'(=%s) must be 0 in %s", attribute1, value1, attribute2, value2, context);
   }

   @Nonnull
   public String errorMessage(double value1, double value2, String context) {
      return formatErrorMessage(this.attribute1, value1, this.attribute2, value2, context);
   }

   @Nonnull
   public static ExactlyOneZeroValidator withAttributes(String attribute1, String attribute2) {
      return new ExactlyOneZeroValidator(attribute1, attribute2);
   }
}
