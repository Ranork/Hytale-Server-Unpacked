package com.hypixel.hytale.server.core.telemetry;

import com.hypixel.hytale.logger.HytaleLogger;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class TelemetryStorage {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private static final String TELEMETRY_DIRECTORY_NAME = "telemetry";
   private static final String SESSION_FILE_EXTENSION = ".jsonl";
   private static final String COMPRESSED_FILE_EXTENSION = ".jsonl.gz";
   private static final int RETENTION_DAYS = 7;
   private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss").withZone(ZoneOffset.UTC);
   @Nonnull
   private final Path telemetryDirectory;
   @Nonnull
   private final Path sessionFilePath;
   @Nonnull
   private final Object writerLock = new Object();
   @Nullable
   private BufferedWriter sessionWriter;

   public TelemetryStorage(@Nonnull String sessionId) {
      this.telemetryDirectory = Path.of("telemetry");

      try {
         if (!Files.exists(this.telemetryDirectory)) {
            Files.createDirectories(this.telemetryDirectory);
            LOGGER.at(Level.INFO).log("Created telemetry storage directory: %s", this.telemetryDirectory.toAbsolutePath());
         }
      } catch (IOException var7) {
         ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var7)).log("Failed to create telemetry directory");
      }

      String timestamp = TIMESTAMP_FORMAT.format(Instant.now());
      String sessionIdPrefix = sessionId.length() >= 8 ? sessionId.substring(0, 8) : sessionId;
      String sessionFileName = timestamp + "_" + sessionIdPrefix + ".jsonl";
      this.sessionFilePath = this.telemetryDirectory.resolve(sessionFileName);
      CompletableFuture.runAsync(this::performMaintenance);

      try {
         this.sessionWriter = Files.newBufferedWriter(this.sessionFilePath);
         LOGGER.at(Level.INFO).log("Opened telemetry session file: %s", this.sessionFilePath);
      } catch (IOException var6) {
         ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var6)).log("Failed to create telemetry session file");
      }
   }

   public void writeEntry(@Nonnull String json) {
      synchronized (this.writerLock) {
         if (this.sessionWriter != null) {
            try {
               this.sessionWriter.write(json);
               this.sessionWriter.newLine();
               this.sessionWriter.flush();
            } catch (IOException var5) {
               ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var5)).log("Failed to write telemetry packet to local storage");
            }
         }
      }
   }

   private void performMaintenance() {
      try {
         if (!Files.exists(this.telemetryDirectory)) {
            return;
         }

         Instant cutoffTime = Instant.now().minus(7L, ChronoUnit.DAYS);
         String currentSessionFileName = this.sessionFilePath.getFileName().toString();

         try (Stream<Path> stream = Files.list(this.telemetryDirectory)) {
            stream.forEach(file -> processFile(file, cutoffTime, currentSessionFileName));
         }
      } catch (IOException var8) {
         ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var8)).log("Failed to perform telemetry storage maintenance");
      }
   }

   private static void processFile(@Nonnull Path file, @Nonnull Instant cutoffTime, @Nonnull String currentSessionFileName) {
      try {
         String fileName = file.getFileName().toString();
         boolean isJsonl = fileName.endsWith(".jsonl");
         boolean isCompressed = fileName.endsWith(".jsonl.gz");
         if (!isJsonl && !isCompressed) {
            return;
         }

         Instant lastModified = Files.getLastModifiedTime(file).toInstant();
         if (lastModified.isBefore(cutoffTime)) {
            try {
               Files.delete(file);
               LOGGER.at(Level.FINE).log("Deleted expired telemetry file: %s", fileName);
            } catch (IOException var8) {
               ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var8)).log("Failed to delete expired telemetry file: %s", fileName);
            }

            return;
         }

         if (isJsonl && !fileName.equals(currentSessionFileName)) {
            compressFile(file);
         }
      } catch (IOException var9) {
         ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var9)).log("Failed to process telemetry file during maintenance");
      }
   }

   private static void compressFile(@Nonnull Path sourcePath) {
      Path compressedPath = Path.of(sourcePath + ".gz");
      if (!Files.exists(compressedPath)) {
         try {
            FileTime modifiedTime = Files.getLastModifiedTime(sourcePath);

            try (
               InputStream inputStream = Files.newInputStream(sourcePath);
               OutputStream outputStream = Files.newOutputStream(compressedPath);
               GZIPOutputStream gzipStream = new GZIPOutputStream(outputStream);
            ) {
               inputStream.transferTo(gzipStream);
            }

            Files.setLastModifiedTime(compressedPath, modifiedTime);
            Files.delete(sourcePath);
            LOGGER.at(Level.FINE).log("Compressed telemetry file: %s", sourcePath.getFileName());
         } catch (IOException var14) {
            ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var14)).log("Failed to compress telemetry file: %s", sourcePath.getFileName());
         }
      }
   }

   public void closeAndCompress() {
      synchronized (this.writerLock) {
         try {
            if (this.sessionWriter != null) {
               this.sessionWriter.close();
               this.sessionWriter = null;
            }

            if (Files.exists(this.sessionFilePath)) {
               compressFile(this.sessionFilePath);
            }
         } catch (IOException var4) {
            ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var4)).log("Failed to close and compress telemetry session file");
         }
      }
   }
}
