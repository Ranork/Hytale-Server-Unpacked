package com.hypixel.hytale.common.util;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PathUtil {
   private static final Pattern PATH_PATTERN = Pattern.compile("[\\\\/]");
   private static final Set<Path> TRUSTED_PATH_ROOTS = ConcurrentHashMap.newKeySet();

   public static void addTrustedRoot(@Nonnull Path root) {
      TRUSTED_PATH_ROOTS.add(root.toAbsolutePath().normalize());
   }

   public static boolean isInTrustedRoot(@Nonnull Path path) {
      Path normalized = path.toAbsolutePath().normalize();

      for (Path trusted : TRUSTED_PATH_ROOTS) {
         if (isChildOf(trusted, normalized)) {
            return true;
         }
      }

      return false;
   }

   @Nonnull
   public static Path getParent(@Nonnull Path path) {
      if (path.isAbsolute()) {
         return path.getParent().normalize();
      } else {
         Path parentAbsolute = path.toAbsolutePath().getParent();
         Path parent = path.resolve(relativize(path, parentAbsolute));
         return parent.normalize();
      }
   }

   @Nonnull
   public static Path relativize(@Nonnull Path pathA, @Nonnull Path pathB) {
      Path absolutePathA = pathA.toAbsolutePath();
      Path absolutePathB = pathB.toAbsolutePath();
      return Objects.equals(absolutePathA.getRoot(), absolutePathB.getRoot())
         ? absolutePathA.normalize().relativize(absolutePathB.normalize()).normalize()
         : absolutePathB.normalize();
   }

   @Nonnull
   public static Path relativizePretty(@Nonnull Path pathA, @Nonnull Path pathB) {
      Path absolutePathA = pathA.toAbsolutePath().normalize();
      Path absolutePathB = pathB.toAbsolutePath().normalize();
      Path absoluteUserHome = getUserHome().toAbsolutePath();
      if (Objects.equals(absoluteUserHome.getRoot(), absolutePathB.getRoot())) {
         Path relativizedHome = absoluteUserHome.relativize(absolutePathB).normalize();
         if (Objects.equals(absolutePathA.getRoot(), absolutePathB.getRoot())) {
            Path relativized = absolutePathA.relativize(absolutePathB).normalize();
            return relativizedHome.getNameCount() < relativized.getNameCount() ? Paths.get("~").resolve(relativizedHome) : relativized;
         } else {
            return relativizedHome.getNameCount() < absolutePathB.getNameCount() ? Paths.get("~").resolve(relativizedHome) : absolutePathB;
         }
      } else {
         return Objects.equals(absolutePathA.getRoot(), absolutePathB.getRoot()) ? absolutePathA.relativize(absolutePathB).normalize() : absolutePathB;
      }
   }

   public static boolean isValidName(@Nonnull String name) {
      return !name.isBlank() && name.indexOf(47) < 0 && name.indexOf(92) < 0 && !".".equals(name) && !"..".equals(name);
   }

   @Nullable
   public static Path resolvePathWithinDir(@Nonnull Path directory, @Nonnull String relativePath) {
      Path resolved = directory.resolve(relativePath);
      return !isChildOf(directory, resolved) ? null : resolved;
   }

   @Nullable
   public static Path resolveName(@Nonnull Path directory, @Nonnull String name) {
      return !isValidName(name) ? null : directory.resolve(name);
   }

   @Nonnull
   public static Path get(@Nonnull String path) {
      return get(Paths.get(path));
   }

   @Nonnull
   public static Path get(@Nonnull Path path) {
      return path.toString().charAt(0) == '~' ? getUserHome().resolve(path.subpath(1, path.getNameCount())).normalize() : path.normalize();
   }

   @Nonnull
   public static Path getUserHome() {
      return Paths.get(System.getProperty("user.home"));
   }

   public static String getFileName(@Nonnull URL extUrl) {
      String[] pathContents = PATH_PATTERN.split(extUrl.getPath());
      String fileName = pathContents[pathContents.length - 1];
      return fileName.isEmpty() && pathContents.length > 1 ? pathContents[pathContents.length - 2] : fileName;
   }

   public static boolean isChildOf(@Nonnull Path parent, @Nonnull Path child) {
      return child.toAbsolutePath().normalize().startsWith(parent.toAbsolutePath().normalize());
   }

   public static void forEachParent(@Nonnull Path path, @Nullable Path limit, @Nonnull Consumer<Path> consumer) {
      Path parent = path.toAbsolutePath();
      if (Files.isRegularFile(parent)) {
         parent = parent.getParent();
      }

      if (parent != null) {
         do {
            consumer.accept(parent);
         } while ((parent = parent.getParent()) != null && (limit == null || isChildOf(limit, parent)));
      }
   }

   @Nonnull
   public static String getFileExtension(@Nonnull Path path) {
      String fileName = path.getFileName().toString();
      int index = fileName.lastIndexOf(46);
      return index == -1 ? "" : fileName.substring(index);
   }

   @Nonnull
   public static String toUnixPathString(@Nonnull Path path) {
      return "\\".equals(path.getFileSystem().getSeparator()) ? path.toString().replace("\\", "/") : path.toString();
   }

   static {
      TRUSTED_PATH_ROOTS.add(Path.of("").toAbsolutePath().normalize());
      TRUSTED_PATH_ROOTS.add(Path.of(System.getProperty("java.io.tmpdir")).toAbsolutePath().normalize());
   }
}
