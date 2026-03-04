package com.hypixel.hytale.server.core.asset.common.asset;

import com.hypixel.hytale.server.core.asset.common.CommonAsset;
import com.hypixel.hytale.sneakythrow.SneakyThrow;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public class FileCommonAsset extends CommonAsset {
   private final Path file;

   public FileCommonAsset(Path file, @Nonnull String name, byte[] bytes) {
      super(name, bytes);
      this.file = file;
   }

   public FileCommonAsset(Path file, @Nonnull String name, @Nonnull String hash, byte[] bytes) {
      super(name, hash, bytes);
      this.file = file;
   }

   public Path getFile() {
      return this.file;
   }

   @Nonnull
   @Override
   public CompletableFuture<byte[]> getBlob0() {
      return CompletableFuture.supplyAsync(SneakyThrow.sneakySupplier(() -> Files.readAllBytes(this.file)));
   }

   @Nonnull
   @Override
   public String toString() {
      return "FileCommonAsset{file=" + this.file + ", " + super.toString() + "}";
   }
}
