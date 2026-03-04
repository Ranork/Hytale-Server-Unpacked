package com.hypixel.hytale.server.core.prefab.selection.buffer;

import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.logger.sentry.SkipSentryException;
import com.hypixel.hytale.server.core.Options;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.IPrefabBuffer;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.PrefabBuffer;
import com.hypixel.hytale.server.core.util.BsonUtil;
import com.hypixel.hytale.server.core.util.io.FileUtil;
import com.hypixel.hytale.sneakythrow.SneakyThrow;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.StampedLock;
import java.util.logging.Level;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PrefabBufferUtil {
   public static final Path CACHE_PATH = Options.getOrDefault(Options.PREFAB_CACHE_DIRECTORY, Options.getOptionSet(), Path.of(".cache/prefabs"));
   public static final String LPF_FILE_SUFFIX = ".lpf";
   public static final String JSON_FILE_SUFFIX = ".prefab.json";
   public static final String JSON_LPF_FILE_SUFFIX = ".prefab.json.lpf";
   public static final String FILE_SUFFIX_REGEX = "((!\\.prefab\\.json)\\.lpf|\\.prefab\\.json)$";
   public static final Pattern FILE_SUFFIX_PATTERN = Pattern.compile("((!\\.prefab\\.json)\\.lpf|\\.prefab\\.json)$");
   public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private static final Map<Path, WeakReference<PrefabBufferUtil.CachedEntry>> CACHE = new ConcurrentHashMap<>();

   @Nonnull
   public static IPrefabBuffer getCached(@Nonnull Path path) {
      WeakReference<PrefabBufferUtil.CachedEntry> reference = CACHE.get(path);
      PrefabBufferUtil.CachedEntry cachedPrefab = reference != null ? reference.get() : null;
      if (cachedPrefab != null) {
         long stamp = cachedPrefab.lock.readLock();

         try {
            if (cachedPrefab.buffer != null) {
               return cachedPrefab.buffer.newAccess();
            }
         } finally {
            cachedPrefab.lock.unlockRead(stamp);
         }
      }

      cachedPrefab = getOrCreateCacheEntry(path);
      long stamp = cachedPrefab.lock.writeLock();

      PrefabBuffer.PrefabBufferAccessor var5;
      try {
         if (cachedPrefab.buffer == null) {
            cachedPrefab.buffer = loadBuffer(path);
            return cachedPrefab.buffer.newAccess();
         }

         var5 = cachedPrefab.buffer.newAccess();
      } finally {
         cachedPrefab.lock.unlockWrite(stamp);
      }

      return var5;
   }

   @Nonnull
   public static PrefabBuffer loadBuffer(@Nonnull Path path) {
      String fileNameStr = path.getFileName().toString();
      String fileName = fileNameStr.replace(".prefab.json.lpf", "").replace(".prefab.json", "");
      Path lpfPath = path.resolveSibling(fileName + ".lpf");
      if (Files.exists(lpfPath)) {
         return loadFromLPF(path, lpfPath);
      } else {
         Path cachedLpfPath;
         AssetPack pack;
         if (AssetModule.get().isAssetPathImmutable(path)) {
            Path lpfConvertedPath = path.resolveSibling(fileName + ".prefab.json.lpf");
            if (Files.exists(lpfConvertedPath)) {
               return loadFromLPF(path, lpfConvertedPath);
            }

            pack = AssetModule.get().findAssetPackForPath(path);
            if (pack != null) {
               String safePackName = FileUtil.INVALID_FILENAME_CHARACTERS.matcher(pack.getName()).replaceAll("_");
               cachedLpfPath = CACHE_PATH.resolve(safePackName).resolve(pack.getRoot().relativize(lpfConvertedPath).toString());
            } else if (lpfConvertedPath.getRoot() != null) {
               cachedLpfPath = CACHE_PATH.resolve(lpfConvertedPath.subpath(1, lpfConvertedPath.getNameCount()).toString());
            } else {
               cachedLpfPath = CACHE_PATH.resolve(lpfConvertedPath.toString());
            }
         } else {
            cachedLpfPath = path.resolveSibling(fileName + ".prefab.json.lpf");
            pack = null;
         }

         Path jsonPath = path.resolveSibling(fileName + ".prefab.json");
         if (!Files.exists(jsonPath)) {
            try {
               Files.deleteIfExists(cachedLpfPath);
            } catch (IOException var8) {
            }

            throw new Error("Error loading Prefab from " + jsonPath.toAbsolutePath() + " (.lpf and .prefab.json) File NOT found!");
         } else {
            try {
               return loadFromJson(pack, path, cachedLpfPath, jsonPath);
            } catch (IOException var9) {
               throw SneakyThrow.sneakyThrow(var9);
            }
         }
      }
   }

   @Nonnull
   public static CompletableFuture<Void> writeToFileAsync(@Nonnull PrefabBuffer prefab, @Nonnull Path path) {
      return CompletableFuture.runAsync(SneakyThrow.sneakyRunnable(() -> {
         try (SeekableByteChannel channel = Files.newByteChannel(path, FileUtil.DEFAULT_WRITE_OPTIONS)) {
            channel.write(BinaryPrefabBufferCodec.INSTANCE.serialize(prefab).nioBuffer());
         }
      }));
   }

   public static PrefabBuffer readFromFile(@Nonnull Path path) {
      return readFromFileAsync(path).join();
   }

   @Nonnull
   public static CompletableFuture<PrefabBuffer> readFromFileAsync(@Nonnull Path path) {
      return CompletableFuture.supplyAsync(SneakyThrow.sneakySupplier(() -> {
         PrefabBuffer var4;
         try (SeekableByteChannel channel = Files.newByteChannel(path)) {
            int size = (int)channel.size();
            ByteBuf buf = Unpooled.buffer(size);
            buf.writerIndex(size);
            if (channel.read(buf.internalNioBuffer(0, size)) != size) {
               throw new IOException("Didn't read full file!");
            }

            var4 = BinaryPrefabBufferCodec.INSTANCE.deserialize(path, buf);
         }

         return var4;
      }));
   }

   @Nonnull
   public static PrefabBuffer loadFromLPF(@Nonnull Path path, @Nonnull Path realPath) {
      try {
         return readFromFile(realPath);
      } catch (Exception var3) {
         throw new Error("Error while loading prefab " + path.toAbsolutePath() + " from " + realPath.toAbsolutePath(), var3);
      }
   }

   @Nonnull
   public static PrefabBuffer loadFromJson(@Nullable AssetPack pack, Path path, @Nonnull Path cachedLpfPath, @Nonnull Path jsonPath) throws IOException {
      BasicFileAttributes cachedAttr = null;

      try {
         cachedAttr = Files.readAttributes(cachedLpfPath, BasicFileAttributes.class);
      } catch (IOException var10) {
      }

      FileTime targetModifiedTime;
      if (pack != null && pack.isImmutable()) {
         targetModifiedTime = Files.readAttributes(pack.getPackLocation(), BasicFileAttributes.class).lastModifiedTime();
      } else {
         targetModifiedTime = Files.readAttributes(jsonPath, BasicFileAttributes.class).lastModifiedTime();
      }

      if (cachedAttr != null && targetModifiedTime.compareTo(cachedAttr.lastModifiedTime()) <= 0) {
         try {
            return readFromFile(cachedLpfPath);
         } catch (CompletionException var11) {
            if (!Options.getOptionSet().has(Options.VALIDATE_PREFABS)) {
               if (var11.getCause() instanceof UpdateBinaryPrefabException) {
                  LOGGER.at(Level.FINE).log("Ignoring LPF %s due to: %s", path, var11.getMessage());
               } else {
                  ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(new SkipSentryException(var11))).log("Failed to load %s", cachedLpfPath);
               }
            }
         }
      }

      try {
         PrefabBuffer buffer = BsonPrefabBufferDeserializer.INSTANCE.deserialize(jsonPath, BsonUtil.readDocument(jsonPath, false).join());
         if (!Options.getOptionSet().has(Options.DISABLE_CPB_BUILD)) {
            try {
               Files.createDirectories(cachedLpfPath.getParent());
               writeToFileAsync(buffer, cachedLpfPath)
                  .thenRun(() -> {
                     try {
                        Files.setLastModifiedTime(cachedLpfPath, targetModifiedTime);
                     } catch (IOException var3x) {
                     }
                  })
                  .exceptionally(
                     throwable -> {
                        ((HytaleLogger.Api)HytaleLogger.getLogger().at(Level.FINE).withCause(new SkipSentryException(throwable)))
                           .log("Failed to save prefab cache %s", cachedLpfPath);
                        return null;
                     }
                  );
            } catch (IOException var8) {
               LOGGER.at(Level.FINE).log("Cannot create cache directory for %s: %s", cachedLpfPath, var8.getMessage());
            }
         }

         return buffer;
      } catch (Exception var9) {
         throw new Error("Error while loading Prefab from " + jsonPath.toAbsolutePath(), var9);
      }
   }

   @Nonnull
   private static PrefabBufferUtil.CachedEntry getOrCreateCacheEntry(Path path) {
      PrefabBufferUtil.CachedEntry[] temp = new PrefabBufferUtil.CachedEntry[1];
      CACHE.compute(path, (p, ref) -> {
         if (ref != null) {
            PrefabBufferUtil.CachedEntry cached = ref.get();
            temp[0] = cached;
            if (cached != null) {
               return ref;
            }
         }

         return new WeakReference<>(temp[0] = new PrefabBufferUtil.CachedEntry());
      });
      return temp[0];
   }

   private static class CachedEntry {
      private final StampedLock lock = new StampedLock();
      private PrefabBuffer buffer;
   }
}
