package com.hypixel.hytale.protocol.io;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public record ValidationResult(boolean isValid, @Nullable String error) {
   public static final ValidationResult OK = new ValidationResult(true, null);

   @Nonnull
   public static ValidationResult error(@Nonnull String message) {
      return new ValidationResult(false, message);
   }

   public void throwIfInvalid() {
      if (!this.isValid) {
         throw new ProtocolException(this.error != null ? this.error : "Validation failed");
      }
   }
}
