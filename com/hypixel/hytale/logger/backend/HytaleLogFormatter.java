package com.hypixel.hytale.logger.backend;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.function.BooleanSupplier;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

public class HytaleLogFormatter extends Formatter {
   private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
   private static final Pattern ANSI_CONTROL_CODES = Pattern.compile("\u001b\\[[;\\d]*m");
   private BooleanSupplier ansi;
   public int maxModuleName;
   private int shorterCount;

   public HytaleLogFormatter(BooleanSupplier ansi) {
      this.ansi = ansi;
   }

   @Nonnull
   @Override
   public String format(@Nonnull LogRecord record) {
      String message = record.getMessage();
      if (message == null) {
         message = "null";
      }

      if (record.getParameters() != null && record.getParameters().length > 0) {
         try {
            message = String.format(message, record.getParameters());
         } catch (RuntimeException var9) {
            throw new IllegalArgumentException("Error logging using format string: " + record.getMessage(), var9);
         }
      }

      if (record.getThrown() != null) {
         StringWriter writer = new StringWriter();
         record.getThrown().printStackTrace(new PrintWriter(writer));
         message = message + "\n" + writer.toString();
      }

      boolean ansi = this.ansi.getAsBoolean();
      if (ansi) {
         message = message + "\u001b[m";
      }

      if (record instanceof HytaleLoggerBackend.RawLogRecord) {
         return !ansi ? stripAnsi(message) + "\n" : message + "\n";
      } else {
         String loggerName = record.getLoggerName();
         int moduleNameTextSize = loggerName.length() + 3;
         if ((moduleNameTextSize <= this.maxModuleName || moduleNameTextSize >= 35) && this.shorterCount <= 500) {
            if (moduleNameTextSize < this.maxModuleName) {
               this.shorterCount++;
            }
         } else {
            this.maxModuleName = moduleNameTextSize;
            this.shorterCount = 0;
         }

         StringBuilder sb = new StringBuilder(33 + this.maxModuleName + message.length());
         if (ansi) {
            String color = null;
            int level = record.getLevel().intValue();
            if (level <= Level.ALL.intValue()) {
               color = "\u001b[37m";
            } else if (level <= Level.FINEST.intValue()) {
               color = "\u001b[36m";
            } else if (level <= Level.FINER.intValue()) {
               color = "\u001b[34m";
            } else if (level <= Level.FINE.intValue()) {
               color = "\u001b[35m";
            } else if (level <= Level.CONFIG.intValue()) {
               color = "\u001b[32m";
            } else if (level <= Level.INFO.intValue()) {
               color = "\u001b[m";
            } else if (level <= Level.WARNING.intValue()) {
               color = "\u001b[33m";
            } else if (level <= Level.SEVERE.intValue()) {
               color = "\u001b[31m";
            }

            if (color != null) {
               sb.append(color);
            }
         }

         sb.append('[');
         DATE_FORMATTER.formatTo(LocalDateTime.ofInstant(record.getInstant(), ZoneOffset.UTC), sb);
         sb.append(' ');
         String levelx;
         if (Level.WARNING.equals(record.getLevel())) {
            levelx = "WARN";
         } else {
            levelx = record.getLevel().getName();
         }

         int levelLength = levelx.length();
         if (levelLength < 6) {
            sb.append(" ".repeat(6 - levelLength));
            sb.append(levelx);
         } else {
            sb.append(levelx, 0, 6);
         }

         sb.append("] ");
         sb.append(" ".repeat(Math.max(0, this.maxModuleName - moduleNameTextSize)));
         sb.append('[').append(loggerName).append("] ").append(ansi ? message : stripAnsi(message)).append('\n');
         return sb.toString();
      }
   }

   public static String stripAnsi(@Nonnull String message) {
      return ANSI_CONTROL_CODES.matcher(message).replaceAll("");
   }
}
