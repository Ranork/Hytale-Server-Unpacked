package com.hypixel.hytale.builtin.hytalegenerator;

import com.hypixel.hytale.common.util.ExceptionUtil;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

public class LoggerUtil {
   @Nonnull
   public static final String HYTALE_GENERATOR_NAME = "HytaleGenerator";

   public static Logger getLogger() {
      return Logger.getLogger("HytaleGenerator");
   }

   public static void logException(@Nonnull String contextDescription, @Nonnull Throwable e) {
      logException(contextDescription, e, getLogger());
   }

   public static void logException(@Nonnull String contextDescription, @Nonnull Throwable e, @Nonnull Logger logger) {
      String msg = "Exception occurred during ";
      msg = msg + contextDescription;
      msg = msg + " \n";
      msg = msg + ExceptionUtil.toStringWithStack(e);
      logger.severe(msg);
   }

   @Nonnull
   public static String nsToMsDecimal(long ns) {
      long ms = ns / 1000000L;
      long decimal = (ns - ms * 1000000L) / 1000L;
      return ms + "." + decimal;
   }
}
