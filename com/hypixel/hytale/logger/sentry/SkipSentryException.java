package com.hypixel.hytale.logger.sentry;

public class SkipSentryException extends RuntimeException {
   public SkipSentryException() {
   }

   public SkipSentryException(Throwable cause) {
      super(cause);
   }

   public SkipSentryException(String message, Throwable cause) {
      super(message, cause);
   }

   public static boolean hasSkipSentry(Throwable thrown) {
      for (Throwable throwable = thrown; throwable != null; throwable = throwable.getCause()) {
         if (throwable instanceof SkipSentryException) {
            return true;
         }

         if (throwable.getCause() == throwable) {
            return false;
         }
      }

      return false;
   }
}
