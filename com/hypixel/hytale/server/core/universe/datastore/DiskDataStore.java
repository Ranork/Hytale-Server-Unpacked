package com.hypixel.hytale.server.core.universe.datastore;

import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.common.util.PathUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.util.BsonUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonDocument;

public class DiskDataStore<T> implements DataStore<T> {
   private static final String EXTENSION = ".json";
   private static final int EXTENSION_LEN = ".json".length();
   private static final String EXTENSION_BACKUP = ".json.bak";
   private static final String GLOB = "*.json";
   private static final String GLOB_WITH_BACKUP = "*.{json,json.bak}";
   @Nonnull
   private final HytaleLogger logger;
   @Nonnull
   private final Path path;
   private final BuilderCodec<T> codec;

   public DiskDataStore(@Nonnull String path, BuilderCodec<T> codec) {
      this.logger = HytaleLogger.get("DataStore|" + path);
      Path universePath = Universe.get().getPath();
      Path resolved = PathUtil.resolvePathWithinDir(universePath, path);
      if (resolved == null) {
         throw new IllegalStateException("Data store path must be within universe directory: " + path);
      } else {
         this.path = resolved;
         this.codec = codec;
         if (Files.isDirectory(this.path)) {
            try (DirectoryStream<Path> paths = Files.newDirectoryStream(this.path, "*.bson")) {
               for (Path oldPath : paths) {
                  Path newPath = getPathFromId(this.path, getIdFromPath(oldPath));

                  try {
                     Files.move(oldPath, newPath);
                  } catch (IOException var11) {
                  }
               }
            } catch (IOException var13) {
               ((HytaleLogger.Api)this.logger.at(Level.SEVERE).withCause(var13)).log("Failed to migrate files form .bson to .json!");
            }
         }
      }
   }

   @Nonnull
   public Path getPath() {
      return this.path;
   }

   @Override
   public BuilderCodec<T> getCodec() {
      return this.codec;
   }

   @Nullable
   @Override
   public T load(String id) throws IOException {
      Path filePath = getPathFromId(this.path, id);
      return Files.exists(filePath) ? this.load0(filePath) : null;
   }

   @Override
   public void save(String id, T value) {
      ExtraInfo extraInfo = ExtraInfo.THREAD_LOCAL.get();
      BsonDocument bsonValue = this.codec.encode(value, extraInfo);
      extraInfo.getValidationResults().logOrThrowValidatorExceptions(this.logger);
      BsonUtil.writeDocument(getPathFromId(this.path, id), bsonValue.asDocument()).join();
   }

   @Override
   public void remove(String id) throws IOException {
      Files.deleteIfExists(getPathFromId(this.path, id));
      Files.deleteIfExists(getBackupPathFromId(this.path, id));
   }

   @Nonnull
   @Override
   public List<String> list() throws IOException {
      List<String> list = new ObjectArrayList();

      try (DirectoryStream<Path> paths = Files.newDirectoryStream(this.path, "*.json")) {
         for (Path path : paths) {
            list.add(getIdFromPath(path));
         }
      }

      return list;
   }

   @Nonnull
   @Override
   public Map<String, T> loadAll() throws IOException {
      Map<String, T> map = new Object2ObjectOpenHashMap();

      try (DirectoryStream<Path> paths = Files.newDirectoryStream(this.path, "*.json")) {
         for (Path path : paths) {
            map.put(getIdFromPath(path), this.load0(path));
         }
      }

      return map;
   }

   @Override
   public void removeAll() throws IOException {
      try (DirectoryStream<Path> paths = Files.newDirectoryStream(this.path, "*.{json,json.bak}")) {
         for (Path path : paths) {
            Files.delete(path);
         }
      }
   }

   @Nullable
   protected T load0(@Nonnull Path path) throws IOException {
      return RawJsonReader.readSync(path, this.codec, this.logger);
   }

   @Nonnull
   protected static Path getPathFromId(@Nonnull Path path, String id) {
      if (!PathUtil.isValidName(id)) {
         throw new IllegalArgumentException("Invalid ID: " + id);
      } else {
         return path.resolve(id + ".json");
      }
   }

   @Nonnull
   protected static Path getBackupPathFromId(@Nonnull Path path, String id) {
      if (!PathUtil.isValidName(id)) {
         throw new IllegalArgumentException("Invalid ID: " + id);
      } else {
         return path.resolve(id + ".json.bak");
      }
   }

   @Nonnull
   protected static String getIdFromPath(@Nonnull Path path) {
      String fileName = path.getFileName().toString();
      return fileName.substring(0, fileName.length() - EXTENSION_LEN);
   }
}
