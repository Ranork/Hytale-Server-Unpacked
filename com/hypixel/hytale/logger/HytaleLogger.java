package com.hypixel.hytale.logger;

import com.google.common.flogger.AbstractLogger;
import com.google.common.flogger.LogContext;
import com.google.common.flogger.LoggingApi;
import com.google.common.flogger.backend.Platform;
import com.google.common.flogger.parser.DefaultPrintfMessageParser;
import com.google.common.flogger.parser.MessageParser;
import com.hypixel.hytale.logger.backend.HytaleConsole;
import com.hypixel.hytale.logger.backend.HytaleFileHandler;
import com.hypixel.hytale.logger.backend.HytaleLogManager;
import com.hypixel.hytale.logger.backend.HytaleLoggerBackend;
import com.hypixel.hytale.logger.backend.HytaleUncaughtExceptionHandler;
import com.hypixel.hytale.logger.util.LoggerPrintStream;
import io.sentry.IScopes;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.LogManager;
import javax.annotation.Nonnull;

public class HytaleLogger extends AbstractLogger<HytaleLogger.Api> {
   private static final Map<String, HytaleLogger> CACHE;
   private static final HytaleLogger LOGGER;
   static final HytaleLogger.NoOp NO_OP;
   @Nonnull
   private final HytaleLoggerBackend backend;

   public static void init() {
      HytaleFileHandler fileHandler = HytaleFileHandler.INSTANCE;
      HytaleConsole console = HytaleConsole.INSTANCE;
      LOGGER.at(Level.INFO).log("Logger Initialized");
   }

   public static void replaceStd() {
      if (!HytaleLoggerBackend.isJunitTest()) {
         System.setOut(new LoggerPrintStream(get("SOUT"), Level.INFO));
         System.setErr(new LoggerPrintStream(get("SERR"), Level.SEVERE));
      }
   }

   public static HytaleLogger getLogger() {
      return LOGGER;
   }

   @Nonnull
   public static HytaleLogger forEnclosingClass() {
      String className = Platform.getCallerFinder().findLoggingClass(HytaleLogger.class);
      String loggerName = classToLoggerName(className);
      return get(loggerName);
   }

   @Nonnull
   public static HytaleLogger forEnclosingClassFull() {
      String loggingClass = Platform.getCallerFinder().findLoggingClass(HytaleLogger.class);
      return get(loggingClass);
   }

   @Nonnull
   public static HytaleLogger get(String loggerName) {
      return CACHE.computeIfAbsent(loggerName, key -> new HytaleLogger(HytaleLoggerBackend.getLogger(key)));
   }

   private HytaleLogger(@Nonnull HytaleLoggerBackend backend) {
      super(backend);
      this.backend = backend;
   }

   public HytaleLogger.Api at(@Nonnull Level level) {
      return (HytaleLogger.Api)(this.isLoggable(level) ? new HytaleLogger.Context(level) : NO_OP);
   }

   public String getName() {
      return super.getName();
   }

   @Nonnull
   public Level getLevel() {
      return this.backend.getLevel();
   }

   public void setLevel(@Nonnull Level level) {
      this.backend.setLevel(level);
   }

   @Nonnull
   public HytaleLogger getSubLogger(String name) {
      return new HytaleLogger(this.backend.getSubLogger(name));
   }

   public void setSentryClient(@Nonnull IScopes scope) {
      this.backend.setSentryClient(scope);
   }

   public void setPropagatesSentryToParent(boolean propagate) {
      this.backend.setPropagatesSentryToParent(propagate);
   }

   @Nonnull
   private static String classToLoggerName(@Nonnull String className) {
      int lastIndexOf = className.lastIndexOf(46);
      String loggerName;
      if (lastIndexOf >= 0 && className.length() > lastIndexOf + 1) {
         loggerName = className.substring(lastIndexOf + 1);
      } else {
         loggerName = className;
      }

      return loggerName;
   }

   static {
      System.setProperty("java.util.logging.manager", HytaleLogManager.class.getName());
      HytaleUncaughtExceptionHandler.setup();
      LogManager logManager = LogManager.getLogManager();
      if (!logManager.getClass().getName().equals(HytaleLogManager.class.getName())) {
         throw new IllegalStateException(
            "Log manager wasn't set! Please ensure HytaleLogger is the first logger to be initialized or\nuse `System.setProperty(\"java.util.logging.manager\", HytaleLogManager.class.getName());` at the start of your application.\nLog manager is: "
               + logManager
         );
      } else {
         CACHE = new ConcurrentHashMap<>();
         LOGGER = new HytaleLogger(HytaleLoggerBackend.getLogger());
         NO_OP = new HytaleLogger.NoOp();
      }
   }

   public interface Api extends LoggingApi<HytaleLogger.Api> {
   }

   final class Context extends LogContext<HytaleLogger, HytaleLogger.Api> implements HytaleLogger.Api {
      private Context(@Nonnull Level level) {
         super(level, false);
      }

      @Nonnull
      protected HytaleLogger getLogger() {
         return HytaleLogger.this;
      }

      @Nonnull
      protected HytaleLogger.Api api() {
         return this;
      }

      @Nonnull
      protected HytaleLogger.Api noOp() {
         return HytaleLogger.NO_OP;
      }

      protected MessageParser getMessageParser() {
         return DefaultPrintfMessageParser.getInstance();
      }
   }

   private static final class NoOp extends com.google.common.flogger.LoggingApi.NoOp<HytaleLogger.Api> implements HytaleLogger.Api {
   }
}
