package com.hypixel.hytale.server.npc.asset.builder.validators;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StringNotEmptyValidator extends StringValidator {
   private static final StringNotEmptyValidator INSTANCE = new StringNotEmptyValidator();

   private StringNotEmptyValidator() {
   }

   @Override
   public boolean test(@Nullable String value) {
      return value != null && !value.isEmpty();
   }

   @Nonnull
   @Override
   public String errorMessage(String value) {
      return this.errorMessage0(value, "Value");
   }

   @Nonnull
   @Override
   public String errorMessage(String value, String name) {
      return this.errorMessage0(value, "\"" + name + "\"");
   }

   @Nonnull
   private String errorMessage0(String value, String name) {
      return name + " must not be an empty string";
   }

   public static StringNotEmptyValidator get() {
      return INSTANCE;
   }
}
