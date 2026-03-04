package com.hypixel.hytale.server.core.util.io;

import com.hypixel.hytale.sneakythrow.SneakyThrow;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import javax.annotation.Nonnull;

public class FileUtil {
   public static final Set<OpenOption> DEFAULT_WRITE_OPTIONS = Set.of(StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
   public static final Set<FileVisitOption> DEFAULT_WALK_TREE_OPTIONS_SET = Set.of();
   public static final FileVisitOption[] DEFAULT_WALK_TREE_OPTIONS_ARRAY = new FileVisitOption[0];
   public static final Pattern INVALID_FILENAME_CHARACTERS = Pattern.compile("[<>:\"|?*/\\\\]");

   public static void unzipFile(@Nonnull Path path, @Nonnull byte[] buffer, @Nonnull ZipInputStream zipStream, @Nonnull ZipEntry zipEntry, @Nonnull String name) throws IOException {
      Path filePath = path.resolve(name);
      if (!filePath.toAbsolutePath().startsWith(path)) {
         throw new ZipException("Entry is outside of the target dir: " + zipEntry.getName());
      } else {
         if (zipEntry.isDirectory()) {
            Files.createDirectory(filePath);
         } else {
            int len;
            try (OutputStream stream = Files.newOutputStream(filePath)) {
               while ((len = zipStream.read(buffer)) > 0) {
                  stream.write(buffer, 0, len);
               }
            }
         }

         zipStream.closeEntry();
      }
   }

   public static void copyDirectory(@Nonnull Path origin, @Nonnull Path destination) throws IOException {
      try (Stream<Path> paths = Files.walk(origin)) {
         paths.forEach(originSubPath -> {
            try {
               Path relative = origin.relativize(originSubPath);
               Path destinationSubPath = destination.resolve(relative);
               Files.copy(originSubPath, destinationSubPath);
            } catch (Throwable var5) {
               throw new RuntimeException("Error copying path", var5);
            }
         });
      }
   }

   public static void moveDirectoryContents(@Nonnull Path origin, @Nonnull Path destination, CopyOption... options) throws IOException {
      try (Stream<Path> paths = Files.walk(origin)) {
         paths.forEach(originSubPath -> {
            if (!originSubPath.equals(origin)) {
               try {
                  Path relative = origin.relativize(originSubPath);
                  Path destinationSubPath = destination.resolve(relative);
                  Files.move(originSubPath, destinationSubPath, options);
               } catch (Throwable var6) {
                  throw new RuntimeException("Error moving path", var6);
               }
            }
         });
      }
   }

   public static void deleteDirectory(@Nonnull Path path) throws IOException {
      try (Stream<Path> stream = Files.walk(path)) {
         stream.sorted(Comparator.reverseOrder()).forEach(SneakyThrow.sneakyConsumer(Files::delete));
      }
   }

   public static void extractZip(@Nonnull Path zipFile, @Nonnull Path destDir) throws IOException {
      extractZip(Files.newInputStream(zipFile), destDir);
   }

   public static void extractZip(@Nonnull InputStream inputStream, @Nonnull Path destDir) throws IOException {
      ZipEntry entry;
      try (ZipInputStream zis = new ZipInputStream(inputStream)) {
         for (; (entry = zis.getNextEntry()) != null; zis.closeEntry()) {
            Path destPath = destDir.resolve(entry.getName()).normalize();
            if (!destPath.startsWith(destDir)) {
               throw new ZipException("Zip entry outside target directory: " + entry.getName());
            }

            if (entry.isDirectory()) {
               Files.createDirectories(destPath);
            } else {
               Files.createDirectories(destPath.getParent());
               Files.copy(zis, destPath, StandardCopyOption.REPLACE_EXISTING);
            }
         }
      }
   }

   public static void writeStringAtomic(@Nonnull Path file, @Nonnull String content, boolean backup) throws IOException {
      Path tmpPath = file.resolveSibling(file.getFileName() + ".tmp");
      Path bakPath = file.resolveSibling(file.getFileName() + ".bak");
      Files.writeString(tmpPath, content);
      if (backup && Files.isRegularFile(file)) {
         atomicMove(file, bakPath);
      }

      atomicMove(tmpPath, file);
   }

   public static void atomicMove(@Nonnull Path source, @Nonnull Path target) throws IOException {
      try {
         Files.move(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
      } catch (AtomicMoveNotSupportedException var3) {
         Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
      }
   }

   public static void writeStringAtomic(@Nonnull Path file, @Nonnull String content) throws IOException {
      writeStringAtomic(file, content, true);
   }
}
