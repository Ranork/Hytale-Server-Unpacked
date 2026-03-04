package com.hypixel.hytale.server.core.asset.common;

import com.hypixel.hytale.sneakythrow.SneakyThrow;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ResourceCommonAsset extends CommonAsset {
   private final Class<?> clazz;
   private final String path;

   public ResourceCommonAsset(Class<?> clazz, String path, @Nonnull String name, byte[] bytes) {
      super(name, bytes);
      this.clazz = clazz;
      this.path = path;
   }

   public ResourceCommonAsset(Class<?> clazz, String path, @Nonnull String name, @Nonnull String hash, byte[] bytes) {
      super(name, hash, bytes);
      this.clazz = clazz;
      this.path = path;
   }

   public String getPath() {
      return this.path;
   }

   @Nonnull
   @Override
   public CompletableFuture<byte[]> getBlob0() {
      try {
         CompletableFuture var2;
         try (InputStream stream = this.clazz.getResourceAsStream(this.path)) {
            var2 = CompletableFuture.completedFuture(stream.readAllBytes());
         }

         return var2;
      } catch (IOException var6) {
         return CompletableFuture.failedFuture(var6);
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "ResourceCommonAsset{" + super.toString() + "}";
   }

   @Nullable
   public static ResourceCommonAsset of(@Nonnull Class<?> clazz, @Nonnull String path, @Nonnull String name) {
      try {
         ResourceCommonAsset var5;
         try (InputStream stream = clazz.getResourceAsStream(path)) {
            if (stream == null) {
               return null;
            }

            byte[] bytes = stream.readAllBytes();
            var5 = new ResourceCommonAsset(clazz, path, name, bytes);
         }

         return var5;
      } catch (IOException var8) {
         throw SneakyThrow.sneakyThrow(var8);
      }
   }
}
