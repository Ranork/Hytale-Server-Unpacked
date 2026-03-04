package com.hypixel.hytale.server.npc.asset.builder.validators;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;

public class NoDuplicatesValidator<T> extends Validator {
   private final Iterable<T> iterable;
   private final String variableName;

   private NoDuplicatesValidator(Iterable<T> iterable, String variableName) {
      this.iterable = iterable;
      this.variableName = variableName;
   }

   public boolean test() {
      Set<T> set = new HashSet<>();

      for (T each : this.iterable) {
         if (!set.add(each)) {
            return false;
         }
      }

      return true;
   }

   @Nonnull
   public String errorMessage() {
      return "There are not allowed to be duplicate entries in the \"" + this.variableName + "\" list.";
   }

   @Nonnull
   public static <T> NoDuplicatesValidator<T> withAttributes(Iterable<T> iterable, String variableName) {
      return new NoDuplicatesValidator<>(iterable, variableName);
   }
}
