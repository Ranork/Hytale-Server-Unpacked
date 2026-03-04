package com.hypixel.hytale.logger.backend;

import com.google.common.flogger.backend.LogData;
import com.google.common.flogger.backend.LoggerBackend;
import com.google.common.flogger.backend.system.SimpleLogRecord;
import com.hypixel.hytale.logger.sentry.HytaleSentryHandler;
import com.hypixel.hytale.logger.sentry.SkipSentryException;
import io.sentry.IScopes;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HytaleLoggerBackend extends LoggerBackend {
   public static Function<String, Level> LOG_LEVEL_LOADER;
   public static final PrintStream REAL_SOUT = System.out;
   public static final PrintStream REAL_SERR = System.err;
   private static final Map<String, HytaleLoggerBackend> CACHE = new ConcurrentHashMap<>();
   private static final HytaleLoggerBackend ROOT_LOGGER = new HytaleLoggerBackend("Hytale", null);
   private static final int OFF_VALUE = Level.OFF.intValue();
   private final String name;
   private final HytaleLoggerBackend parent;
   @Nonnull
   private Level level = Level.INFO;
   private BiConsumer<Level, Level> onLevelChange;
   @Nullable
   private HytaleSentryHandler sentryHandler;
   private boolean propagateSentryToParent = true;
   @Nonnull
   private CopyOnWriteArrayList<CopyOnWriteArrayList<LogRecord>> subscribers = new CopyOnWriteArrayList<>();

   protected HytaleLoggerBackend(String name) {
      this.name = name;
      this.parent = ROOT_LOGGER;
   }

   protected HytaleLoggerBackend(String name, HytaleLoggerBackend parent) {
      this.name = name;
      this.parent = parent;
   }

   public String getLoggerName() {
      return this.name;
   }

   @Nonnull
   public Level getLevel() {
      return this.level;
   }

   public boolean isLoggable(@Nonnull Level lvl) {
      int levelValue = this.level.intValue();
      return lvl.intValue() >= levelValue && levelValue != OFF_VALUE;
   }

   public void log(@Nonnull LogData data) {
      this.log(SimpleLogRecord.create(data));
   }

   public void handleError(@Nonnull RuntimeException error, @Nonnull LogData badData) {
      this.log(SimpleLogRecord.error(error, badData));
   }

   public void log(@Nonnull LogRecord logRecord) {
      this.log(logRecord, false);
   }

   public void log(@Nonnull LogRecord logRecord, boolean sentryHandled) {
      if (this.sentryHandler != null && !sentryHandled && logRecord.getThrown() != null && !SkipSentryException.hasSkipSentry(logRecord.getThrown())) {
         this.sentryHandler.publish(logRecord);
         sentryHandled = true;
      }

      if (!this.propagateSentryToParent && !sentryHandled && logRecord.getThrown() != null) {
         sentryHandled = true;
      }

      if (this.parent != null) {
         this.parent.log(logRecord, sentryHandled);
      } else {
         HytaleFileHandler.INSTANCE.log(logRecord);
         HytaleConsole.INSTANCE.publish(logRecord);

         for (int i = 0; i < this.subscribers.size(); i++) {
            this.subscribers.get(i).add(logRecord);
         }
      }
   }

   public static void subscribe(CopyOnWriteArrayList<LogRecord> subscriber) {
      if (!ROOT_LOGGER.subscribers.contains(subscriber)) {
         ROOT_LOGGER.subscribers.add(subscriber);
      }
   }

   public static void unsubscribe(CopyOnWriteArrayList<LogRecord> subscriber) {
      if (ROOT_LOGGER.subscribers.contains(subscriber)) {
         ROOT_LOGGER.subscribers.remove(subscriber);
      }
   }

   @Nonnull
   public HytaleLoggerBackend getSubLogger(String name) {
      HytaleLoggerBackend hytaleLoggerBackend = new HytaleLoggerBackend(this.name + "][" + name, this);
      hytaleLoggerBackend.loadLogLevel();
      return hytaleLoggerBackend;
   }

   public void setSentryClient(@Nullable IScopes scope) {
      if (scope != null) {
         this.sentryHandler = new HytaleSentryHandler(scope);
         this.sentryHandler.setLevel(Level.ALL);
      } else {
         this.sentryHandler = null;
      }
   }

   public void setPropagatesSentryToParent(boolean propagate) {
      this.propagateSentryToParent = propagate;
   }

   public void setOnLevelChange(BiConsumer<Level, Level> onLevelChange) {
      this.onLevelChange = onLevelChange;
   }

   public void setLevel(@Nonnull Level newLevel) {
      Level old = this.level;
      this.level = newLevel;
      if (this.onLevelChange != null && !Objects.equals(old, newLevel)) {
         this.onLevelChange.accept(old, newLevel);
      }
   }

   public void loadLogLevel() {
      if (this.name != null && LOG_LEVEL_LOADER != null) {
         Level level = LOG_LEVEL_LOADER.apply(this.name);
         if (level != null) {
            this.setLevel(level);
         }
      }
   }

   public static void loadLevels(@Nonnull List<Entry<String, Level>> list) {
      for (Entry<String, Level> e : list) {
         getLogger(e.getKey()).setLevel(e.getValue());
      }
   }

   public static void reloadLogLevels() {
      CACHE.values().forEach(HytaleLoggerBackend::loadLogLevel);
   }

   public static HytaleLoggerBackend getLogger() {
      return ROOT_LOGGER;
   }

   public static HytaleLoggerBackend getLogger(@Nonnull String name) {
      if (name.isEmpty()) {
         return getLogger();
      } else {
         HytaleLoggerBackend logger = CACHE.computeIfAbsent(name, HytaleLoggerBackend::new);
         logger.loadLogLevel();
         return logger;
      }
   }

   @Nonnull
   public static HytaleLoggerBackend getLogger(String name, BiConsumer<Level, Level> onLevelChange) {
      HytaleLoggerBackend logger = CACHE.computeIfAbsent(name, HytaleLoggerBackend::new);
      logger.setOnLevelChange(onLevelChange);
      logger.loadLogLevel();
      return logger;
   }

   public static void setIndent(int indent) {
      HytaleConsole.INSTANCE.getFormatter().maxModuleName = indent;
      FileHandler fileHandler = HytaleFileHandler.INSTANCE.getFileHandler();
      if (fileHandler != null) {
         ((HytaleLogFormatter)fileHandler.getFormatter()).maxModuleName = indent;
      }
   }

   public static boolean isJunitTest() {
      for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
         if (element.getClassName().startsWith("org.junit.")) {
            return true;
         }
      }

      return false;
   }

   public static void rawLog(String message) {
      ROOT_LOGGER.log(new HytaleLoggerBackend.RawLogRecord(Level.ALL, message));
   }

   public static class RawLogRecord extends LogRecord {
      public RawLogRecord(@Nonnull Level level, String msg) {
         super(level, msg);
      }
   }
}
