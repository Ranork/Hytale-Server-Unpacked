package com.hypixel.hytale.server.npc.asset.builder.validators;

import com.hypixel.hytale.server.npc.asset.builder.BuilderObjectHelper;
import java.util.Objects;
import java.util.function.IntPredicate;
import javax.annotation.Nonnull;

public class OneOrNonePresentValidator extends Validator {
   @Nonnull
   private final String[] attributes;

   private OneOrNonePresentValidator(String... attributes) {
      this.attributes = Objects.requireNonNull(attributes, "Attributes in OneOrNonePresentValidator must not be null");
   }

   public static boolean test(@Nonnull BuilderObjectHelper<?>[] objects) {
      return OnePresentValidator.countPresent(objects.length, i -> objects[i].isPresent()) <= 1;
   }

   public static boolean test(@Nonnull boolean[] readStatus) {
      return OnePresentValidator.countPresent(readStatus.length, i -> readStatus[i]) <= 1;
   }

   public static boolean test(@Nonnull BuilderObjectHelper<?> objectHelper1, @Nonnull BuilderObjectHelper<?> objectHelper2) {
      return OnePresentValidator.countPresent(objectHelper1, objectHelper2) <= 1;
   }

   public static boolean test(
      @Nonnull BuilderObjectHelper<?> objectHelper1, @Nonnull BuilderObjectHelper<?> objectHelper2, @Nonnull BuilderObjectHelper<?> objectHelper3
   ) {
      return OnePresentValidator.countPresent(objectHelper1, objectHelper2, objectHelper3) <= 1;
   }

   @Nonnull
   public static String errorMessage(@Nonnull String[] attributes, BuilderObjectHelper<?>[] objectHelpers) {
      return errorMessage(attributes, i -> objectHelpers[i].isPresent());
   }

   @Nonnull
   public static String errorMessage(@Nonnull String[] attributes, boolean[] readStatus) {
      return errorMessage(attributes, i -> readStatus[i]);
   }

   @Nonnull
   public static String errorMessage(@Nonnull String[] attributes, @Nonnull IntPredicate presentPredicate) {
      StringBuilder result = new StringBuilder("Exactly one or none of ");
      String sep = ", ";

      for (int i = 0; i < attributes.length; i++) {
         if (i == attributes.length - 1) {
            sep = "";
         }

         result.append(String.format("'%s'%s%s", attributes[i], presentPredicate.test(i) ? "(Present)" : "", sep));
      }

      return result + " must be present";
   }

   @Nonnull
   public static OneOrNonePresentValidator withAttributes(String... attributes) {
      return new OneOrNonePresentValidator(attributes);
   }
}
