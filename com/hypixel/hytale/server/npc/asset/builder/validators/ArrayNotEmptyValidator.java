package com.hypixel.hytale.server.npc.asset.builder.validators;

import com.hypixel.hytale.server.npc.asset.builder.BuilderObjectArrayHelper;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ArrayNotEmptyValidator extends ArrayValidator {
   private static final ArrayNotEmptyValidator INSTANCE = new ArrayNotEmptyValidator();

   private ArrayNotEmptyValidator() {
   }

   @Override
   public boolean test(@Nonnull BuilderObjectArrayHelper<?, ?> builderObjectArrayHelper) {
      return builderObjectArrayHelper.isPresent();
   }

   @Nonnull
   @Override
   public String errorMessage(String name, BuilderObjectArrayHelper<?, ?> builderObjectArrayHelper) {
      return errorMessage(name);
   }

   @Nonnull
   @Override
   public String errorMessage(BuilderObjectArrayHelper<?, ?> builderObjectArrayHelper) {
      return errorMessage((String)null);
   }

   @Nonnull
   public static String errorMessage(@Nullable String name) {
      if (name == null) {
         name = "Array";
      } else {
         name = "'" + name + "'";
      }

      return name + " must not be empty";
   }

   public static ArrayNotEmptyValidator get() {
      return INSTANCE;
   }
}
