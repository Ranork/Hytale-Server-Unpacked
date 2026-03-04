package com.hypixel.hytale.server.core.util;

import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.sneakythrow.SneakyThrow;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Config<T> {
   @Nonnull
   private final Path path;
   private final String name;
   private final BuilderCodec<T> codec;
   @Nullable
   private T config;
   @Nullable
   private CompletableFuture<T> loadingConfig;

   public Config(@Nonnull Path path, String name, BuilderCodec<T> codec) {
      this.path = path.resolve(name + ".json");
      this.name = name;
      this.codec = codec;
   }

   @Nonnull
   @Deprecated(forRemoval = true)
   public static <T> Config<T> preloadedConfig(@Nonnull Path path, String name, BuilderCodec<T> codec, T config) {
      Config<T> c = new Config<>(path, name, codec);
      c.config = config;
      return c;
   }

   @Nonnull
   public CompletableFuture<T> load() {
      if (this.loadingConfig != null) {
         return this.loadingConfig;
      } else if (!Files.exists(this.path)) {
         this.config = this.codec.getDefaultValue();
         return CompletableFuture.completedFuture(this.config);
      } else {
         return this.loadingConfig = CompletableFuture.supplyAsync(SneakyThrow.sneakySupplier(() -> {
            this.config = RawJsonReader.readSync(this.path, this.codec, HytaleLogger.getLogger());
            this.loadingConfig = null;
            return this.config;
         }));
      }
   }

   public T get() {
      if (this.config == null && this.loadingConfig == null) {
         throw new IllegalStateException("Config is not loaded");
      } else {
         return this.loadingConfig != null ? this.loadingConfig.join() : this.config;
      }
   }

   @Nonnull
   public CompletableFuture<Void> save() {
      if (this.config == null && this.loadingConfig == null) {
         throw new IllegalStateException("Config is not loaded");
      } else {
         return this.loadingConfig != null
            ? CompletableFuture.completedFuture(null)
            : BsonUtil.writeDocument(this.path, this.codec.encode(this.config, new ExtraInfo()));
      }
   }
}
