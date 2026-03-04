package com.hypixel.hytale.server.core.universe.playerdata;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.common.util.PathUtil;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.server.core.Constants;
import com.hypixel.hytale.server.core.Options;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.BsonUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.bson.BsonDocument;

public class DiskPlayerStorageProvider implements PlayerStorageProvider {
   public static final String ID = "Disk";
   public static final BuilderCodec<DiskPlayerStorageProvider> CODEC = BuilderCodec.builder(DiskPlayerStorageProvider.class, DiskPlayerStorageProvider::new)
      .append(new KeyedCodec<>("Path", Codec.STRING), (o, s) -> o.path = Path.of(s), o -> o.path.toString())
      .add()
      .build();
   @Nonnull
   private Path path = Constants.UNIVERSE_PATH.resolve("players");

   @Nonnull
   public Path getPath() {
      return this.path;
   }

   @Nonnull
   @Override
   public PlayerStorage getPlayerStorage() {
      if (!PathUtil.isInTrustedRoot(this.path)) {
         throw new IllegalStateException("Player storage path must be within a trusted directory: " + this.path);
      } else {
         return new DiskPlayerStorageProvider.DiskPlayerStorage(this.path);
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "DiskPlayerStorageProvider{path=" + this.path + "}";
   }

   public static class DiskPlayerStorage implements PlayerStorage {
      public static final String FILE_EXTENSION = ".json";
      @Nonnull
      private final Path path;

      public DiskPlayerStorage(@Nonnull Path path) {
         this.path = path;
         if (!Options.getOptionSet().has(Options.BARE)) {
            try {
               Files.createDirectories(path);
            } catch (IOException var3) {
               throw new RuntimeException("Failed to create players directory", var3);
            }
         }
      }

      @Nonnull
      @Override
      public CompletableFuture<Holder<EntityStore>> load(@Nonnull UUID uuid) {
         Path file = this.path.resolve(uuid + ".json");
         return BsonUtil.readDocument(file).thenApply(bsonDocument -> {
            if (bsonDocument == null) {
               bsonDocument = new BsonDocument();
            }

            return EntityStore.REGISTRY.deserialize(bsonDocument);
         });
      }

      @Nonnull
      @Override
      public CompletableFuture<Void> save(@Nonnull UUID uuid, @Nonnull Holder<EntityStore> holder) {
         Path file = this.path.resolve(uuid + ".json");
         BsonDocument document = EntityStore.REGISTRY.serialize(holder);
         return BsonUtil.writeDocument(file, document);
      }

      @Nonnull
      @Override
      public CompletableFuture<Void> remove(@Nonnull UUID uuid) {
         Path file = this.path.resolve(uuid + ".json");

         try {
            Files.deleteIfExists(file);
            return CompletableFuture.completedFuture(null);
         } catch (IOException var4) {
            return CompletableFuture.failedFuture(var4);
         }
      }

      @Nonnull
      @Override
      public Set<UUID> getPlayers() throws IOException {
         Set var2;
         try (Stream<Path> stream = Files.list(this.path)) {
            var2 = stream.<UUID>map(p -> {
               String fileName = p.getFileName().toString();
               if (!fileName.endsWith(".json")) {
                  return null;
               } else {
                  try {
                     return UUID.fromString(fileName.substring(0, fileName.length() - ".json".length()));
                  } catch (IllegalArgumentException var3) {
                     return null;
                  }
               }
            }).filter(Objects::nonNull).collect(Collectors.toSet());
         }

         return var2;
      }
   }
}
