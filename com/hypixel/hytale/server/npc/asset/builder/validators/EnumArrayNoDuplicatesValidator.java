package com.hypixel.hytale.server.npc.asset.builder.validators;

import java.util.Arrays;
import java.util.EnumSet;
import javax.annotation.Nonnull;

public class EnumArrayNoDuplicatesValidator extends EnumArrayValidator {
   private static final EnumArrayNoDuplicatesValidator INSTANCE = new EnumArrayNoDuplicatesValidator();

   private EnumArrayNoDuplicatesValidator() {
   }

   @Override
   public <T extends Enum<T>> boolean test(@Nonnull T[] array, Class<T> clazz) {
      EnumSet<T> set = EnumSet.noneOf(clazz);

      for (T item : array) {
         if (!set.add(item)) {
            return false;
         }
      }

      return true;
   }

   @Nonnull
   @Override
   public <T extends Enum<T>> String errorMessage(String name, T[] array) {
      return String.format("%s must not contain duplicates: %s", name, Arrays.toString((Object[])array));
   }

   public static EnumArrayNoDuplicatesValidator get() {
      return INSTANCE;
   }
}
