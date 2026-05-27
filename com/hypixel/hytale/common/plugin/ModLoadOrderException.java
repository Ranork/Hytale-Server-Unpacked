package com.hypixel.hytale.common.plugin;

import javax.annotation.Nonnull;

public class ModLoadOrderException extends Exception {
   @Nonnull
   private final ModLoadOrderException.Type type;

   public ModLoadOrderException(@Nonnull ModLoadOrderException.Type type, @Nonnull String message) {
      super(message);
      this.type = type;
   }

   @Nonnull
   public ModLoadOrderException.Type getType() {
      return this.type;
   }

   public static enum Type {
      MISSING_DEPENDENCY,
      CYCLIC_DEPENDENCY,
      MANUAL_ORDER_CONFLICT;
   }
}
