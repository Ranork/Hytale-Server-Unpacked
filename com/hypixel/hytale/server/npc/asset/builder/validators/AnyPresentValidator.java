package com.hypixel.hytale.server.npc.asset.builder.validators;

import com.hypixel.hytale.server.npc.asset.builder.BuilderObjectHelper;
import java.util.Objects;
import javax.annotation.Nonnull;

public class AnyPresentValidator extends Validator {
   @Nonnull
   private final String[] attributes;

   private AnyPresentValidator(@Nonnull String[] attributes) {
      Objects.requireNonNull(attributes);
      this.attributes = attributes;
   }

   public static boolean test(@Nonnull BuilderObjectHelper<?>[] objects) {
      for (BuilderObjectHelper<?> object : objects) {
         if (object.isPresent()) {
            return true;
         }
      }

      return false;
   }

   @Nonnull
   public static String errorMessage(String[] attributes) {
      return "At least one of " + String.join(" ", attributes) + " must be present";
   }

   @Nonnull
   public String errorMessage() {
      return errorMessage(this.attributes);
   }

   @Nonnull
   public static AnyPresentValidator withAttributes(String attribute1, String attribute2) {
      return new AnyPresentValidator(new String[]{attribute1, attribute2});
   }

   @Nonnull
   public static AnyPresentValidator withAttributes(@Nonnull String[] attributes) {
      return new AnyPresentValidator(attributes);
   }
}
