package com.hypixel.hytale.server.core.command.system;

import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.server.core.Message;
import javax.annotation.Nonnull;

public class CommandValidationResults extends ValidationResults {
   public CommandValidationResults(@Nonnull ExtraInfo extraInfo) {
      super(extraInfo);
   }

   public void processResults(@Nonnull ParseResult parseResult) {
      this._processValidationResults();
      if (this.validatorExceptions != null && !this.validatorExceptions.isEmpty()) {
         StringBuilder sb = new StringBuilder();
         boolean failed = false;

         for (ValidationResults.ValidatorResultsHolder holder : this.validatorExceptions) {
            for (ValidationResults.ValidationResult result : holder.results()) {
               failed |= result.appendResult(sb);
            }
         }

         if (failed) {
            parseResult.fail(Message.raw(sb.toString()));
         } else {
            this.validatorExceptions.clear();
         }
      }
   }
}
