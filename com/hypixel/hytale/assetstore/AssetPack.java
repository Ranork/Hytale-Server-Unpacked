package com.hypixel.hytale.assetstore;

import com.hypixel.hytale.common.plugin.PluginManifest;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AssetPack {
   @Nonnull
   private final String name;
   @Nonnull
   private final Path root;
   @Nullable
   private final FileSystem fileSystem;
   private final boolean isImmutable;
   private final PluginManifest manifest;
   private final Path packLocation;

   public AssetPack(Path packLocation, @Nonnull String name, @Nonnull Path root, @Nullable FileSystem fileSystem, boolean isImmutable, PluginManifest manifest) {
      this.name = name;
      this.root = root;
      this.fileSystem = fileSystem;
      this.isImmutable = isImmutable;
      this.manifest = manifest;
      this.packLocation = packLocation;
   }

   @Nonnull
   public String getName() {
      return this.name;
   }

   @Nonnull
   public Path getRoot() {
      return this.root;
   }

   @Nullable
   public FileSystem getFileSystem() {
      return this.fileSystem;
   }

   public PluginManifest getManifest() {
      return this.manifest;
   }

   public boolean isImmutable() {
      return this.isImmutable;
   }

   public Path getPackLocation() {
      return this.packLocation;
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         AssetPack assetPack = (AssetPack)o;
         return !this.name.equals(assetPack.name) ? false : this.root.equals(assetPack.root);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.name.hashCode();
      return 31 * result + this.root.hashCode();
   }

   @Nonnull
   @Override
   public String toString() {
      return "AssetPack{name='" + this.name + "', root=" + this.root + ", fileSystem=" + this.fileSystem + "}";
   }
}
