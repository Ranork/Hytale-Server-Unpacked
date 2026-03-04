package com.hypixel.hytale.server.npc.asset.builder.validators;

import java.util.Objects;
import javax.annotation.Nonnull;

public class AnyBooleanValidator extends Validator {
   @Nonnull
   private final String[] attributes;

   private AnyBooleanValidator(@Nonnull String[] attributes) {
      Objects.requireNonNull(attributes);
      this.attributes = attributes;
   }

   public static boolean test(@Nonnull boolean[] values) {
      for (boolean value : values) {
         if (value) {
            return true;
         }
      }

      return false;
   }

   @Nonnull
   public static String errorMessage(String[] attributes) {
      return "At least one of " + String.join(" ", attributes) + " must be true";
   }

   @Nonnull
   public String errorMessage() {
      return errorMessage(this.attributes);
   }

   @Nonnull
   public static AnyBooleanValidator withAttributes(String attribute1, String attribute2) {
      return new AnyBooleanValidator(new String[]{attribute1, attribute2});
   }

   @Nonnull
   public static AnyBooleanValidator withAttributes(@Nonnull String[] attributes) {
      return new AnyBooleanValidator(attributes);
   }
}
