package com.hypixel.hytale.procedurallib.file;

import it.unimi.dsi.fastutil.Hash.Strategy;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface FileIO {
   Strategy<Path> PATH_STRATEGY = new Strategy<Path>() {
      public int hashCode(Path o) {
         return FileIO.hashCode(o);
      }

      public boolean equals(Path a, Path b) {
         return FileIO.equals(a, b);
      }
   };

   static void setDefaultRoot(@Nonnull Path path) {
      FileIOSystem.Provider.setRoot(path);
   }

   @Nonnull
   static <FS extends FileIOSystem> FS openFileIOSystem(@Nonnull FS fs) {
      FileIOSystem.Provider.set(fs);
      return fs;
   }

   static void closeFileIOSystem(@Nonnull FileIOSystem fs) {
      FileIOSystem.Provider.unset();
   }

   static boolean exists(@Nonnull AssetPath path) {
      return Files.exists(path.filepath());
   }

   static boolean exists(@Nonnull Path root, @Nonnull Path path) {
      return Files.exists(append(root, path));
   }

   @Nonnull
   static AssetPath resolve(@Nonnull Path path) {
      FileIOSystem fs = FileIOSystem.Provider.get();
      return fs.resolve(path);
   }

   @Nonnull
   static <T> T load(@Nonnull AssetPath assetPath, @Nonnull AssetLoader<T> loader) throws IOException {
      FileIOSystem fs = FileIOSystem.Provider.get();
      return fs.load(assetPath, loader);
   }

   @Nonnull
   static <T> T load(@Nonnull Path path, @Nonnull AssetLoader<T> loader) throws IOException {
      FileIOSystem fs = FileIOSystem.Provider.get();
      AssetPath assetPath = fs.resolve(path);
      return fs.load(assetPath, loader);
   }

   @Nonnull
   static List<AssetPath> list(@Nonnull Path path, @Nonnull Predicate<AssetPath> matcher, @Nonnull UnaryOperator<AssetPath> disableOp) throws IOException {
      FileIOSystem fs = FileIOSystem.Provider.get();
      Path assetDirPath = relativize(path, fs.baseRoot());
      ObjectArrayList<AssetPath> paths = new ObjectArrayList();
      ObjectOpenHashSet<AssetPath> visited = new ObjectOpenHashSet();
      ObjectOpenHashSet<AssetPath> disabled = new ObjectOpenHashSet();

      for (Path root : fs.roots().paths) {
         Path rootAssetDirPath = append(root, assetDirPath);
         if (Files.exists(rootAssetDirPath) && Files.isDirectory(rootAssetDirPath)) {
            try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(rootAssetDirPath)) {
               visited.addAll(disabled);
               disabled.clear();

               for (Path filepath : dirStream) {
                  AssetPath assetPath = AssetPath.fromAbsolute(root, filepath);
                  AssetPath disabledPath = disableOp.apply(assetPath);
                  if (disabledPath != assetPath) {
                     disabled.add(disabledPath);
                  } else if (matcher.test(assetPath) && visited.add(assetPath)) {
                     paths.add(assetPath);
                  }
               }
            }
         }
      }

      return paths;
   }

   static boolean startsWith(Path path, Path prefix) {
      if (prefix.getNameCount() > path.getNameCount()) {
         return false;
      } else {
         boolean match = true;

         for (int i = 0; match && i < prefix.getNameCount(); i++) {
            match = path.getName(i).toString().equals(prefix.getName(i).toString());
         }

         return match;
      }
   }

   static Path relativize(Path child, Path parent) {
      if (child.getNameCount() < parent.getNameCount()) {
         return child;
      } else {
         return !startsWith(child, parent) ? child : child.subpath(parent.getNameCount(), child.getNameCount());
      }
   }

   static Path append(Path root, Path path) {
      if (path.getFileSystem() == root.getFileSystem()) {
         return root.resolve(path);
      } else {
         Path out = root;

         for (int i = 0; i < path.getNameCount(); i++) {
            out = out.resolve(path.getName(i).toString());
         }

         return out;
      }
   }

   static boolean equals(@Nullable Path a, @Nullable Path b) {
      return a == b || a != null && b != null && a.getNameCount() == b.getNameCount() && startsWith(a, b);
   }

   static int hashCode(@Nullable Path path) {
      if (path == null) {
         return 0;
      } else {
         int hashcode = 1;

         for (int i = 0; i < path.getNameCount(); i++) {
            hashcode = hashcode * 31 + path.getName(i).toString().hashCode();
         }

         return hashcode;
      }
   }
}
