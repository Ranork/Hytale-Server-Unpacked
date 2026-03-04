package com.hypixel.hytale.codec.validation;

import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.exception.CodecValidationException;
import com.hypixel.hytale.logger.HytaleLogger;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ValidationResults {
   protected final ExtraInfo extraInfo;
   @Nullable
   protected List<ValidationResults.ValidatorResultsHolder> validatorExceptions;
   @Nullable
   protected List<ValidationResults.ValidationResult> results;

   public ValidationResults(ExtraInfo extraInfo) {
      this.extraInfo = extraInfo;
   }

   public ExtraInfo getExtraInfo() {
      return this.extraInfo;
   }

   public void fail(String reason) {
      this.add(ValidationResults.ValidationResult.fail(reason));
   }

   public void warn(String reason) {
      this.add(ValidationResults.ValidationResult.warn(reason));
   }

   public void add(ValidationResults.ValidationResult result) {
      if (this.results == null) {
         this.results = new ObjectArrayList();
      }

      this.results.add(result);
   }

   public void _processValidationResults() {
      if (this.results != null && !this.results.isEmpty()) {
         for (ValidationResults.ValidationResult validationResult : this.results) {
            ValidationResults.Result result = validationResult.result;
            if (result == ValidationResults.Result.WARNING || result == ValidationResults.Result.FAIL) {
               if (this.validatorExceptions == null) {
                  this.validatorExceptions = new ObjectArrayList();
               }

               this.validatorExceptions
                  .add(
                     new ValidationResults.ValidatorResultsHolder(
                        this.extraInfo.peekKey(), this.extraInfo.peekLine(), this.extraInfo.peekColumn(), new ObjectArrayList(this.results)
                     )
                  );
               break;
            }
         }

         this.results.clear();
      }
   }

   public void logOrThrowValidatorExceptions(@Nonnull HytaleLogger logger) {
      this.logOrThrowValidatorExceptions(logger, "Failed to validate asset!\n");
   }

   public void logOrThrowValidatorExceptions(@Nonnull HytaleLogger logger, @Nonnull String msg) {
      if (this.validatorExceptions != null && !this.validatorExceptions.isEmpty()) {
         StringBuilder sb = new StringBuilder(msg);
         this.extraInfo.appendDetailsTo(sb);
         boolean failed = false;

         for (ValidationResults.ValidatorResultsHolder holder : this.validatorExceptions) {
            if (holder.key != null && !holder.key.isEmpty()) {
               sb.append("Key: ").append(holder.key).append("\n");
            }

            sb.append("Results:\n");

            for (ValidationResults.ValidationResult result : holder.results) {
               failed |= result.appendResult(sb);
            }
         }

         if (failed) {
            throw new CodecValidationException(sb.toString());
         } else {
            logger.at(Level.WARNING).log(sb.toString());
            this.validatorExceptions.clear();
         }
      }
   }

   public boolean hasFailed() {
      if (this.results == null) {
         return false;
      } else {
         for (ValidationResults.ValidationResult res : this.results) {
            if (res.result() == ValidationResults.Result.FAIL) {
               return true;
            }
         }

         return false;
      }
   }

   @Nullable
   public List<ValidationResults.ValidationResult> getResults() {
      return this.results == null ? null : this.results;
   }

   public void setResults(@Nullable List<ValidationResults.ValidationResult> results) {
      this.results = results;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ValidationResults{results=" + this.results + "}";
   }

   public static enum Result {
      SUCCESS,
      WARNING,
      FAIL;
   }

   public record ValidationResult(ValidationResults.Result result, String reason) {
      public boolean appendResult(@Nonnull StringBuilder sb) {
         sb.append("\t").append(this.result).append(": ").append(this.reason).append("\n");
         return this.result == ValidationResults.Result.FAIL;
      }

      @Nonnull
      public static ValidationResults.ValidationResult fail(String reason) {
         return new ValidationResults.ValidationResult(ValidationResults.Result.FAIL, reason);
      }

      @Nonnull
      public static ValidationResults.ValidationResult warn(String reason) {
         return new ValidationResults.ValidationResult(ValidationResults.Result.WARNING, reason);
      }
   }

   protected record ValidatorResultsHolder(String key, int line, int column, List<ValidationResults.ValidationResult> results) {
   }
}
