package com.hypixel.hytale.server.npc.asset.builder.validators;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StringArrayNotEmptyValidator extends StringArrayValidator {
   private static final StringArrayNotEmptyValidator INSTANCE = new StringArrayNotEmptyValidator();

   private StringArrayNotEmptyValidator() {
   }

   @Override
   public boolean test(@Nullable String[] list) {
      if (list != null && list.length != 0) {
         for (String s : list) {
            if (s == null || s.isEmpty()) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   @Nonnull
   @Override
   public String errorMessage(@Nullable String name, String[] list) {
      if (name == null) {
         name = "StringList";
      } else {
         name = "'" + name + "'";
      }

      return name + " must not be empty or contain empty strings";
   }

   @Nonnull
   @Override
   public String errorMessage(String[] list) {
      return this.errorMessage(null, list);
   }

   public static StringArrayNotEmptyValidator get() {
      return INSTANCE;
   }
}
