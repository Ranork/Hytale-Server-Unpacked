package com.hypixel.hytale.server.core.util.backup;

import com.hypixel.hytale.common.util.java.ManifestUtil;
import com.hypixel.hytale.protocol.packets.interface_.WorldSavingStatus;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.Universe;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class BackupUtil {
   static void walkFileTreeAndZip(@Nonnull Path sourceDir, @Nonnull Path zipPath) throws IOException {
      try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(zipPath))) {
         zipOutputStream.setMethod(0);
         zipOutputStream.setLevel(0);
         zipOutputStream.setComment(
            "Automated backup by HytaleServer - Version: "
               + ManifestUtil.getImplementationVersion()
               + ", Revision: "
               + ManifestUtil.getImplementationRevisionId()
         );

         try (Stream<Path> stream = Files.walk(sourceDir)) {
            for (Path path : stream.filter(x$0 -> Files.isRegularFile(x$0)).toList()) {
               long size = Files.size(path);
               CRC32 crc = new CRC32();

               try (InputStream inputStream = Files.newInputStream(path)) {
                  byte[] buffer = new byte[16384];

                  int len;
                  while ((len = inputStream.read(buffer)) != -1) {
                     crc.update(buffer, 0, len);
                  }
               }

               ZipEntry zipEntry = new ZipEntry(sourceDir.relativize(path).toString());
               zipEntry.setSize(size);
               zipEntry.setCompressedSize(size);
               zipEntry.setCrc(crc.getValue());
               zipOutputStream.putNextEntry(zipEntry);
               Files.copy(path, zipOutputStream);
               zipOutputStream.closeEntry();
            }
         }
      }
   }

   static void broadcastBackupStatus(boolean isWorldSaving) {
      Universe.get().broadcastPacket(new WorldSavingStatus(isWorldSaving));
   }

   static void broadcastBackupError(Throwable cause) {
      Message message = Message.translation("server.universe.backup.error").param("message", cause.getLocalizedMessage());
      Universe.get().getPlayers().forEach(player -> {
         boolean hasPermission = PermissionsModule.get().hasPermission(player.getUuid(), "hytale.status.backup.error");
         if (hasPermission) {
            player.sendMessage(message);
         }
      });
   }

   @Nullable
   static List<Path> findOldBackups(@Nonnull Path backupDirectory, int maxBackupCount) throws IOException {
      if (!backupDirectory.toFile().isDirectory()) {
         return null;
      } else {
         try (Stream<Path> files = Files.list(backupDirectory)) {
            List<Path> zipFiles = files.filter(p -> p.getFileName().toString().endsWith(".zip")).sorted(Comparator.comparing(p -> {
               try {
                  return Files.readAttributes(p, BasicFileAttributes.class).creationTime();
               } catch (IOException var2x) {
                  return FileTime.fromMillis(0L);
               }
            })).toList();
            if (zipFiles.size() > maxBackupCount) {
               return zipFiles.subList(0, zipFiles.size() - maxBackupCount);
            }
         }

         return null;
      }
   }
}
