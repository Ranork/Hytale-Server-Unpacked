package com.hypixel.hytale.procedurallib.file;

import java.nio.file.Path;
import java.util.Comparator;
import javax.annotation.Nonnull;

public final class AssetPath {
   private final Path path;
   private final Path filepath;
   private final transient int hash;
   public static final Comparator<AssetPath> COMPARATOR = (a, b) -> {
      int max = Math.min(a.filepath.getNameCount(), b.filepath.getNameCount());

      for (int i = 0; i < max; i++) {
         int comp = a.filepath.getName(i).toString().compareTo(b.filepath.getName(i).toString());
         if (comp != 0) {
            return comp;
         }
      }

      return Integer.compare(a.filepath.getNameCount(), b.filepath.getNameCount());
   };

   private AssetPath(@Nonnull Path path, @Nonnull Path filepath) {
      this.path = path;
      this.filepath = filepath;
      this.hash = FileIO.hashCode(path);
   }

   @Nonnull
   public AssetPath rename(@Nonnull String filename) {
      Path rel = this.path.getParent().resolve(filename);
      Path path = this.filepath.getParent().resolve(filename);
      return new AssetPath(rel, path);
   }

   @Nonnull
   public Path path() {
      return this.path;
   }

   @Nonnull
   public Path filepath() {
      return this.filepath;
   }

   @Nonnull
   public String getFileName() {
      return this.filepath.getFileName().toString();
   }

   @Override
   public String toString() {
      return "AssetPath{path=" + this.path + ", filepath=" + this.filepath + "}";
   }

   @Override
   public int hashCode() {
      return this.hash;
   }

   @Override
   public boolean equals(Object obj) {
      return this == obj || obj instanceof AssetPath other && this.hash == other.hash && FileIO.equals(this.path, other.path);
   }

   public static AssetPath fromAbsolute(@Nonnull Path root, @Nonnull Path filepath) {
      assert root.getNameCount() == 0 || FileIO.startsWith(filepath, root);

      Path relPath = FileIO.relativize(filepath, root);
      return new AssetPath(relPath, filepath);
   }

   public static AssetPath fromRelative(@Nonnull Path root, @Nonnull Path assetPath) {
      assert root.getNameCount() == 0 || !FileIO.startsWith(assetPath, root);

      Path filepath = FileIO.append(root, assetPath);
      return new AssetPath(assetPath, filepath);
   }
}
