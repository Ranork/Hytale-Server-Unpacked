package com.hypixel.hytale;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.logger.backend.HytaleFileHandler;
import com.hypixel.hytale.logger.backend.HytaleLoggerBackend;
import com.hypixel.hytale.logger.sentry.SkipSentryException;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.HytaleServerConfig;
import com.hypixel.hytale.server.core.Options;
import com.hypixel.hytale.server.core.console.ConsoleModule;
import io.sentry.Sentry;
import java.util.Map.Entry;
import java.util.logging.Level;

public class LateMain {
   public static void lateMain(String[] args) {
      try {
         if (!Options.parse(args)) {
            HytaleLogger.init();
            ConsoleModule.initializeTerminal();
            HytaleFileHandler.INSTANCE.enable();
            HytaleLogger.replaceStd();
            HytaleLoggerBackend.LOG_LEVEL_LOADER = name -> {
               for (Entry<String, Level> e : Options.getOptionSet().valuesOf(Options.LOG_LEVELS)) {
                  if (name.equals(e.getKey())) {
                     return e.getValue();
                  }
               }

               HytaleServer hytaleServer = HytaleServer.get();
               if (hytaleServer != null) {
                  HytaleServerConfig config = hytaleServer.getConfig();
                  if (config != null) {
                     Level configLevel = config.getLogLevels().get(name);
                     if (configLevel != null) {
                        return configLevel;
                     }
                  }
               }

               return Options.getOptionSet().has(Options.SHUTDOWN_AFTER_VALIDATE) ? Level.WARNING : null;
            };
            if (Options.getOptionSet().has(Options.SHUTDOWN_AFTER_VALIDATE)) {
               HytaleLoggerBackend.reloadLogLevels();
            }

            new HytaleServer();
         }
      } catch (Throwable var2) {
         if (!SkipSentryException.hasSkipSentry(var2)) {
            Sentry.captureException(var2);
         }

         var2.printStackTrace();
         throw new RuntimeException("Failed to create HytaleServer", var2);
      }
   }
}
