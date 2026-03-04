package com.hypixel.hytale.logger.backend;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HytaleFileHandler extends Thread {
   public static final DateTimeFormatter LOG_FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
   public static final HytaleFileHandler INSTANCE = new HytaleFileHandler();
   private final BlockingQueue<LogRecord> logRecords = new LinkedBlockingQueue<>();
   @Nullable
   private FileHandler fileHandler;

   public HytaleFileHandler() {
      super("HytaleLogger");
      this.setDaemon(true);
   }

   @Override
   public void run() {
      if (this.fileHandler == null) {
         throw new IllegalStateException("Thread should not be started when no file handler exists!");
      } else {
         try {
            while (!this.isInterrupted()) {
               this.fileHandler.publish(this.logRecords.take());
            }
         } catch (InterruptedException var2) {
            Thread.currentThread().interrupt();
         }
      }
   }

   @Nullable
   public FileHandler getFileHandler() {
      return this.fileHandler;
   }

   public void enable() {
      if (this.fileHandler != null) {
         throw new IllegalStateException("Already enabled!");
      } else {
         try {
            Path logsDirectory = Paths.get("logs/");
            if (!Files.isDirectory(logsDirectory)) {
               Files.createDirectory(logsDirectory);
            }

            String fileNamePart = "logs/" + LOG_FILE_DATE_FORMAT.format(LocalDateTime.now());
            String fileName = fileNamePart + "_server.log";
            if (Files.exists(Paths.get(fileName))) {
               fileName = fileNamePart + "%u_server.log";
            }

            this.fileHandler = new FileHandler(fileName);
            this.fileHandler.setEncoding("UTF-8");
            this.fileHandler.setLevel(Level.ALL);
            this.fileHandler.setFormatter(new HytaleLogFormatter(() -> false));
         } catch (IOException var4) {
            throw new RuntimeException("Failed to create file handler!", var4);
         }

         this.start();
      }
   }

   public void log(@Nonnull LogRecord logRecord) {
      if (!this.isAlive()) {
         if (this.fileHandler != null) {
            this.fileHandler.publish(logRecord);
         }
      } else {
         this.logRecords.add(logRecord);
      }
   }

   public void shutdown() {
      if (this.fileHandler != null) {
         this.interrupt();

         try {
            this.join();
         } catch (InterruptedException var2) {
            Thread.currentThread().interrupt();
         }

         List<LogRecord> list = new ObjectArrayList();
         this.logRecords.drainTo(list);
         list.forEach(this.fileHandler::publish);
         this.fileHandler.flush();
         this.fileHandler.close();
         this.fileHandler = null;
      }
   }
}
