package com.hypixel.hytale.server.core.util.backup;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.config.BackupConfig;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BackupTask {
   private static final DateTimeFormatter BACKUP_FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
   private static final Duration BACKUP_ARCHIVE_FREQUENCY = Duration.of(12L, ChronoUnit.HOURS);
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   @Nonnull
   private final CompletableFuture<Void> completion = new CompletableFuture<>();

   public static CompletableFuture<Void> start(@Nonnull Path universeDir, @Nonnull Path backupDir) {
      BackupTask task = new BackupTask(universeDir, backupDir);
      return task.completion;
   }

   private BackupTask(@Nonnull final Path universeDir, @Nonnull final Path backupDir) {
      (new Thread("Backup Runner") {
         {
            this.setDaemon(false);
         }

         @Override
         public void run() {
            BackupUtil.broadcastBackupStatus(true);

            try {
               Path archiveDir = backupDir.resolve("archive");
               Files.createDirectories(backupDir);
               Files.createDirectories(archiveDir);
               BackupTask.cleanOrArchiveOldBackups(backupDir, archiveDir);
               BackupTask.cleanOldArchives(archiveDir);
               String backupName = BackupTask.BACKUP_FILE_DATE_FORMATTER.format(LocalDateTime.now()) + ".zip";
               Path tempZip = backupDir.resolve(backupName + ".tmp");
               BackupUtil.walkFileTreeAndZip(universeDir, tempZip);
               Path backupZip = backupDir.resolve(backupName);
               Files.move(tempZip, backupZip, StandardCopyOption.REPLACE_EXISTING);
               BackupTask.LOGGER.at(Level.INFO).log("Successfully created backup %s", backupZip);
               BackupTask.this.completion.complete(null);
            } catch (Throwable var8) {
               ((HytaleLogger.Api)BackupTask.LOGGER.at(Level.SEVERE).withCause(var8)).log("Backup failed with exception");
               BackupUtil.broadcastBackupError(var8);
               BackupTask.this.completion.completeExceptionally(var8);
            } finally {
               BackupUtil.broadcastBackupStatus(false);
            }
         }
      }).start();
   }

   private static void cleanOrArchiveOldBackups(@Nonnull Path sourceDir, @Nonnull Path archiveDir) throws IOException {
      BackupConfig backupConfig = HytaleServer.get().getConfig().getBackupConfig();
      int maxCount = backupConfig.getMaxCount();
      if (maxCount >= 1) {
         List<Path> oldBackups = BackupUtil.findOldBackups(sourceDir, maxCount);
         if (oldBackups != null && !oldBackups.isEmpty()) {
            Path oldestBackup = oldBackups.getFirst();
            FileTime oldestBackupTime = Files.getLastModifiedTime(oldestBackup);
            FileTime lastArchive = getMostRecentArchive(archiveDir);
            boolean doArchive = lastArchive == null
               || Duration.between(lastArchive.toInstant(), oldestBackupTime.toInstant()).compareTo(BACKUP_ARCHIVE_FREQUENCY) > 0;
            if (doArchive) {
               oldBackups = oldBackups.subList(1, oldBackups.size());
               Files.move(oldestBackup, archiveDir.resolve(oldestBackup.getFileName()), StandardCopyOption.REPLACE_EXISTING);
               LOGGER.at(Level.INFO).log("Archived old backup: %s", oldestBackup);
            }

            for (Path path : oldBackups) {
               LOGGER.at(Level.INFO).log("Clearing old backup: %s", path);
               Files.deleteIfExists(path);
            }
         }
      }
   }

   private static void cleanOldArchives(@Nonnull Path dir) throws IOException {
      BackupConfig backupConfig = HytaleServer.get().getConfig().getBackupConfig();
      int maxCount = backupConfig.getArchiveMaxCount();
      if (maxCount >= 1) {
         List<Path> oldBackups = BackupUtil.findOldBackups(dir, maxCount);
         if (oldBackups != null && !oldBackups.isEmpty()) {
            for (Path path : oldBackups) {
               LOGGER.at(Level.INFO).log("Clearing old archive backup: %s", path);
               Files.deleteIfExists(path);
            }
         }
      }
   }

   @Nullable
   private static FileTime getMostRecentArchive(@Nonnull Path dir) throws IOException {
      FileTime mostRecent = null;

      try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
         for (Path path : stream) {
            if (Files.isRegularFile(path)) {
               FileTime modificationTime = Files.getLastModifiedTime(path);
               if (mostRecent == null || modificationTime.compareTo(mostRecent) > 0) {
                  mostRecent = modificationTime;
               }
            }
         }
      }

      return mostRecent;
   }
}
