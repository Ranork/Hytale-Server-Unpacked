package com.hypixel.hytale.server.core.prefab.selection.buffer;

import com.hypixel.hytale.common.util.PathUtil;
import com.hypixel.hytale.server.core.util.io.FileUtil;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import javax.annotation.Nonnull;

public class PrefabLoader {
   private static final char JSON_FILEPATH_SEPARATOR = '.';
   private final Path rootFolder;

   public PrefabLoader(Path rootFolder) {
      this.rootFolder = rootFolder;
   }

   public Path getRootFolder() {
      return this.rootFolder;
   }

   public void resolvePrefabs(@Nonnull String prefabName, @Nonnull Consumer<Path> pathConsumer) throws IOException {
      resolvePrefabs(this.rootFolder, prefabName, pathConsumer);
   }

   public static void resolvePrefabs(@Nonnull Path rootFolder, @Nonnull String prefabName, @Nonnull Consumer<Path> pathConsumer) throws IOException {
      if (prefabName.endsWith(".*")) {
         resolvePrefabFolder(rootFolder, prefabName, pathConsumer);
      } else {
         Path prefabPath = rootFolder.resolve(prefabName.replace('.', File.separatorChar) + ".prefab.json");
         if (!PathUtil.isChildOf(rootFolder, prefabPath)) {
            throw new IllegalArgumentException("Invalid prefab name: " + prefabName);
         }

         if (!Files.exists(prefabPath)) {
            return;
         }

         pathConsumer.accept(prefabPath);
      }
   }

   public static void resolvePrefabFolder(@Nonnull Path rootFolder, @Nonnull String prefabName, @Nonnull final Consumer<Path> pathConsumer) throws IOException {
      String prefabDirectory = prefabName.substring(0, prefabName.length() - 2);
      Path directoryPath = rootFolder.resolve(prefabDirectory.replace('.', File.separatorChar));
      if (!PathUtil.isChildOf(rootFolder, directoryPath)) {
         throw new IllegalArgumentException("Invalid prefab name: " + prefabName);
      } else if (Files.exists(directoryPath)) {
         if (!Files.isDirectory(directoryPath)) {
            throw new NotDirectoryException(directoryPath.toString());
         } else {
            Files.walkFileTree(directoryPath, FileUtil.DEFAULT_WALK_TREE_OPTIONS_SET, Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
               @Nonnull
               public FileVisitResult visitFile(@Nonnull Path file, BasicFileAttributes attrs) {
                  String fileName = file.getFileName().toString();
                  Matcher matcher = PrefabBufferUtil.FILE_SUFFIX_PATTERN.matcher(fileName);
                  if (matcher.find()) {
                     String fileNameNoExtension = matcher.replaceAll("");
                     pathConsumer.accept(file.resolveSibling(fileNameNoExtension));
                  }

                  return FileVisitResult.CONTINUE;
               }
            });
         }
      }
   }

   @Nonnull
   public static String resolveRelativeJsonPath(@Nonnull String prefabName, @Nonnull Path prefabPath, @Nonnull Path rootPrefabDir) {
      if (!prefabName.endsWith(".*")) {
         return prefabName;
      } else {
         String filepath = rootPrefabDir.relativize(prefabPath).toString();
         int start = prefabName.equals(".*") ? 0 : prefabName.length() - 1;
         int length = getFilepathLengthNoExtension(filepath);
         if (length < start) {
            throw new IllegalArgumentException(String.format("Prefab key '%s' is longer than its filepath '%s'", prefabName, filepath));
         } else {
            char[] chars = new char[length - start];
            filepath.getChars(start, length, chars, 0);

            for (int i = 0; i < chars.length; i++) {
               if (chars[i] == File.separatorChar) {
                  chars[i] = '.';
               }
            }

            return new String(chars);
         }
      }
   }

   private static int getFilepathLengthNoExtension(@Nonnull String filepath) {
      int extensionSize = 0;
      if (filepath.endsWith(".prefab.json")) {
         extensionSize = ".prefab.json".length();
      } else if (filepath.endsWith(".prefab.json.lpf")) {
         extensionSize = ".prefab.json.lpf".length();
      }

      return filepath.length() - extensionSize;
   }
}
