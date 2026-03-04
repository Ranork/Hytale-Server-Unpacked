package com.hypixel.hytale.server.core.asset.monitor;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.logger.sentry.SkipSentryException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class FileChangeTask implements Runnable {
   public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private static final long FILE_SIZE_CHECK_DELAY_MILLIS = 200L;
   private final AssetMonitor assetMonitor;
   @Nonnull
   private final Path path;
   @Nonnull
   private final PathEvent pathEvent;
   private final boolean createdOrModified;
   @Nonnull
   private final ScheduledFuture<?> task;
   private long lastSize;

   public FileChangeTask(AssetMonitor assetMonitor, @Nonnull Path path, @Nonnull PathEvent pathEvent) throws IOException {
      this.assetMonitor = assetMonitor;
      this.path = path;
      this.pathEvent = pathEvent;
      this.createdOrModified = pathEvent.getEventKind() == EventKind.ENTRY_CREATE || pathEvent.getEventKind() == EventKind.ENTRY_MODIFY;
      long size = 0L;
      if (this.createdOrModified) {
         BasicFileAttributes fileAttributes = Files.readAttributes(path, BasicFileAttributes.class);
         if (!fileAttributes.isDirectory()) {
            size = fileAttributes.size();
         }
      }

      this.lastSize = size;
      this.task = AssetMonitor.runTask(this, 200L);
   }

   public AssetMonitor getAssetMonitor() {
      return this.assetMonitor;
   }

   @Nonnull
   public Path getPath() {
      return this.path;
   }

   @Nonnull
   public PathEvent getPathEvent() {
      return this.pathEvent;
   }

   @Override
   public void run() {
      try {
         if (this.createdOrModified) {
            if (!Files.exists(this.path)) {
               LOGGER.at(Level.WARNING).log("The asset file '%s' was deleted before we could load/update it!", this.path);
               this.cancelSchedule();
               return;
            }

            BasicFileAttributes fileAttributes = Files.readAttributes(this.path, BasicFileAttributes.class);
            if (!fileAttributes.isDirectory()) {
               long size = fileAttributes.size();
               if (size > this.lastSize) {
                  LOGGER.at(Level.FINEST).log("File increased in size: %s, %s, %d > %d", this.path, this.pathEvent, size, this.lastSize);
                  this.lastSize = size;
                  this.assetMonitor.markChanged(this.path);
                  return;
               }
            }
         }

         this.cancelSchedule();
         this.assetMonitor.onDelayedChange(this.path, this.pathEvent);
      } catch (FileNotFoundException | NoSuchFileException var4) {
         ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(new SkipSentryException(var4)))
            .log("The asset file '%s' was deleted before we could load/update it!", this.path);
      } catch (Throwable var5) {
         ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(var5)).log("Failed to handle file change %s", this.path);
      }
   }

   public void cancelSchedule() {
      LOGGER.at(Level.FINEST).log("cancelSchedule(): %s", this);
      this.assetMonitor.removeFileChangeTask(this);
      if (this.task != null && !this.task.isDone()) {
         this.task.cancel(false);
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "FileChangeTask{path=" + this.path + ", eventKind=" + this.pathEvent + ", lastSize=" + this.lastSize + "}";
   }
}
