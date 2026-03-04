package com.hypixel.hytale.codec.validation;

import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.exception.CodecValidationException;
import com.hypixel.hytale.logger.HytaleLogger;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class ThrowingValidationResults extends ValidationResults {
   public ThrowingValidationResults(ExtraInfo extraInfo) {
      super(extraInfo);
   }

   @Override
   public void add(@Nonnull ValidationResults.ValidationResult result) {
      StringBuilder sb = new StringBuilder("Failed to validate asset!\n");
      this.extraInfo.appendDetailsTo(sb);
      sb.append("Key: ").append(this.extraInfo.peekKey()).append("\n");
      sb.append("Results:\n");
      boolean failed = result.appendResult(sb);
      if (failed) {
         throw new CodecValidationException(sb.toString());
      } else {
         HytaleLogger.getLogger().at(Level.WARNING).log(sb.toString());
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "ThrowingValidationResults{} " + super.toString();
   }
}
