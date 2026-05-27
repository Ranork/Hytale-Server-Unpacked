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
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Map;
import java.util.Set;
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
   private static final Set<Path> SAVING_PREFABS = ConcurrentHashMap.newKeySet();

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
         Path tmp = path.resolveSibling(path.getFileName() + ".tmp");

         try (DataOutputStream channel = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(tmp)))) {
            BinaryPrefabBufferCodec.INSTANCE.serialize(prefab, channel);
         }

         FileUtil.atomicMove(tmp, path);
      }));
   }

   public static PrefabBuffer readFromFile(@Nonnull Path path) {
      return readFromFileAsync(path).join();
   }

   @Nonnull
   public static CompletableFuture<PrefabBuffer> readFromFileAsync(@Nonnull Path path) {
      return CompletableFuture.supplyAsync(SneakyThrow.sneakySupplier(() -> {
         byte[] bytes = Files.readAllBytes(path);
         return BinaryPrefabBufferCodec.INSTANCE.deserialize(ByteBuffer.wrap(bytes));
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
      } catch (IOException var12) {
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
         } catch (CompletionException var13) {
            if (!Options.getOptionSet().has(Options.VALIDATE_PREFABS)) {
               if (var13.getCause() instanceof UpdateBinaryPrefabException) {
                  LOGGER.at(Level.FINE).log("Ignoring LPF %s due to: %s", path, var13.getMessage());
               } else {
                  ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(new SkipSentryException(var13))).log("Failed to load %s", cachedLpfPath);
               }
            }
         }
      }

      try {
         PrefabBuffer buffer = BsonPrefabBufferDeserializer.INSTANCE.deserialize(jsonPath, BsonUtil.readDocument(jsonPath, false).join());
         Path fullPath = path.normalize();
         if (!Options.getOptionSet().has(Options.DISABLE_CPB_BUILD) && SAVING_PREFABS.add(fullPath)) {
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
                  )
                  .whenComplete((unused, throwable) -> SAVING_PREFABS.remove(fullPath));
            } catch (IOException var9) {
               LOGGER.at(Level.FINE).log("Cannot create cache directory for %s: %s", cachedLpfPath, var9.getMessage());
               SAVING_PREFABS.remove(fullPath);
            } catch (Throwable var10) {
               SAVING_PREFABS.remove(fullPath);
               throw var10;
            }
         }

         return buffer;
      } catch (Exception var11) {
         throw new Error("Error while loading Prefab from " + jsonPath.toAbsolutePath(), var11);
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
