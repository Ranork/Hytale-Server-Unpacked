package com.hypixel.hytale.common.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.annotation.Nonnull;

public class ExceptionUtil {
   @Nonnull
   public static String combineMessages(Throwable thrown, @Nonnull String joiner) {
      StringBuilder sb = new StringBuilder();

      for (Throwable throwable = thrown; throwable != null; throwable = throwable.getCause()) {
         if (throwable.getCause() == throwable) {
            return sb.toString();
         }

         if (throwable.getMessage() != null) {
            sb.append(throwable.getMessage()).append(joiner);
         }
      }

      sb.setLength(sb.length() - joiner.length());
      return sb.toString();
   }

   public static String toStringWithStack(@Nonnull Throwable t) {
      try {
         String var2;
         try (StringWriter out = new StringWriter()) {
            t.printStackTrace(new PrintWriter(out));
            var2 = out.toString();
         }

         return var2;
      } catch (IOException var6) {
         return t.toString();
      }
   }
}
