package com.hypixel.hytale.server.npc.asset.builder.validators;

import java.util.Objects;
import javax.annotation.Nonnull;

public class AtMostOneBooleanValidator extends Validator {
   @Nonnull
   private final String[] attributes;

   private AtMostOneBooleanValidator(@Nonnull String[] attributes) {
      Objects.requireNonNull(attributes);
      this.attributes = attributes;
   }

   public static boolean test(@Nonnull boolean[] values) {
      int count = 0;

      for (boolean value : values) {
         if (value) {
            count++;
         }
      }

      return count <= 1;
   }

   @Nonnull
   public static String errorMessage(String[] attributes) {
      return "At most one of " + String.join(" ", attributes) + " can be true";
   }

   @Nonnull
   public String errorMessage() {
      return errorMessage(this.attributes);
   }

   @Nonnull
   public static AtMostOneBooleanValidator withAttributes(String attribute1, String attribute2) {
      return new AtMostOneBooleanValidator(new String[]{attribute1, attribute2});
   }

   @Nonnull
   public static AtMostOneBooleanValidator withAttributes(@Nonnull String[] attributes) {
      return new AtMostOneBooleanValidator(attributes);
   }
}
