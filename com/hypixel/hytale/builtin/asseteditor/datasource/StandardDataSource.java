package com.hypixel.hytale.builtin.asseteditor.datasource;

import com.hypixel.hytale.builtin.asseteditor.AssetTree;
import com.hypixel.hytale.builtin.asseteditor.EditorClient;
import com.hypixel.hytale.builtin.asseteditor.assettypehandler.AssetTypeHandler;
import com.hypixel.hytale.builtin.asseteditor.data.AssetState;
import com.hypixel.hytale.builtin.asseteditor.data.ModifiedAsset;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.common.util.PathUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Options;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.server.core.util.BsonUtil;
import com.hypixel.hytale.server.core.util.HashUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonValue;

public class StandardDataSource implements DataSource {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private final Path rootPath;
   private final ConcurrentHashMap<Path, Deque<StandardDataSource.EditorFileSaveInfo>> editorSaves;
   private final AssetTree assetTree;
   private final String packKey;
   private final PluginManifest manifest;
   private final boolean isImmutable;
   private final Path recentModificationsFilePath;
   private final AtomicBoolean indexNeedsSaving = new AtomicBoolean();
   private final Map<Path, ModifiedAsset> modifiedAssets = new ConcurrentHashMap<>();
   private ScheduledFuture<?> saveSchedule;
   private boolean isAssetPackBeDeleteable;

   public StandardDataSource(String packKey, Path rootPath, boolean isImmutable, PluginManifest manifest) {
      this.rootPath = rootPath;
      this.editorSaves = new ConcurrentHashMap<>();
      this.packKey = packKey;
      this.isImmutable = isImmutable;
      this.manifest = manifest;
      this.isAssetPackBeDeleteable = !isImmutable && isInModsDirectory(rootPath);
      this.assetTree = new AssetTree(rootPath, packKey, isImmutable, this.isAssetPackBeDeleteable);
      this.recentModificationsFilePath = Path.of("assetEditor", "recentAssetEdits_" + packKey.replace(':', '-') + ".json");
   }

   private static boolean isInModsDirectory(Path path) {
      if (path.startsWith(PluginManager.MODS_PATH)) {
         return true;
      } else {
         for (Path modsPath : Options.getOptionSet().valuesOf(Options.MODS_DIRECTORIES)) {
            if (path.startsWith(modsPath)) {
               return true;
            }
         }

         return false;
      }
   }

   @Override
   public void start() {
      this.loadRecentModifications();
      this.saveSchedule = HytaleServer.SCHEDULED_EXECUTOR.scheduleWithFixedDelay(() -> {
         try {
            this.saveRecentModifications();
         } catch (Exception var2) {
            ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(var2)).log("Failed to save assets index");
         }
      }, 1L, 1L, TimeUnit.MINUTES);
   }

   @Override
   public void shutdown() {
      if (this.saveSchedule != null) {
         this.saveSchedule.cancel(false);
      }

      this.saveRecentModifications();
   }

   private void loadRecentModifications() {
      Path path = this.recentModificationsFilePath;
      if (!Files.exists(path)) {
         path = path.resolveSibling(path.getFileName() + ".bak");
         if (!Files.exists(path)) {
            return;
         }
      }

      BsonDocument doc = BsonUtil.readDocument(path).join();

      for (BsonValue asset : doc.getArray("Assets")) {
         ModifiedAsset modifiedAsset = ModifiedAsset.CODEC.decode(asset, new ExtraInfo());
         if (modifiedAsset != null) {
            this.modifiedAssets.put(modifiedAsset.path, modifiedAsset);
         }
      }
   }

   public void saveRecentModifications() {
      if (this.indexNeedsSaving.getAndSet(false)) {
         LOGGER.at(Level.INFO).log("Saving recent asset modification index...");
         BsonDocument doc = new BsonDocument();
         BsonArray assetsArray = new BsonArray();

         for (Entry<Path, ModifiedAsset> modifiedAsset : this.modifiedAssets.entrySet()) {
            assetsArray.add(ModifiedAsset.CODEC.encode(modifiedAsset.getValue(), new ExtraInfo()));
         }

         doc.append("Assets", assetsArray);

         try {
            BsonUtil.writeDocument(this.recentModificationsFilePath, doc);
         } catch (Exception var5) {
            ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(var5)).log("Failed to save recent asset modification index...");
            this.indexNeedsSaving.set(true);
         }
      }
   }

   public boolean canAssetPackBeDeleted() {
      return this.isAssetPackBeDeleteable;
   }

   public Path resolveAbsolutePath(Path path) {
      Path resolved = this.rootPath.resolve(path.toString()).toAbsolutePath();
      if (!PathUtil.isChildOf(this.rootPath, resolved)) {
         throw new IllegalArgumentException("Invalid path: " + path);
      } else {
         return resolved;
      }
   }

   @Override
   public Path getFullPathToAssetData(Path assetPath) {
      return this.resolveAbsolutePath(assetPath);
   }

   @Override
   public AssetTree getAssetTree() {
      return this.assetTree;
   }

   @Override
   public boolean isImmutable() {
      return this.isImmutable;
   }

   @Override
   public Path getRootPath() {
      return this.rootPath;
   }

   @Override
   public PluginManifest getManifest() {
      return this.manifest;
   }

   @Override
   public boolean doesDirectoryExist(Path folderPath) {
      return Files.isDirectory(this.resolveAbsolutePath(folderPath));
   }

   @Override
   public boolean createDirectory(Path dirPath, EditorClient editorClient) {
      try {
         Files.createDirectory(this.resolveAbsolutePath(dirPath));
         return true;
      } catch (IOException var4) {
         ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var4)).log("Failed to create directory %s", dirPath);
         return false;
      }
   }

   @Override
   public boolean deleteDirectory(Path dirPath) {
      try {
         Files.deleteIfExists(this.resolveAbsolutePath(dirPath));
         return true;
      } catch (IOException var3) {
         ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var3)).log("Failed to delete directory %s", dirPath);
         return false;
      }
   }

   @Override
   public boolean moveDirectory(Path oldDirPath, Path newDirPath) {
      try {
         Files.move(this.resolveAbsolutePath(oldDirPath), this.resolveAbsolutePath(newDirPath));
         return true;
      } catch (IOException var4) {
         ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var4)).log("Failed to move directory %s to %s", oldDirPath, newDirPath);
         return false;
      }
   }

   @Override
   public boolean doesAssetExist(Path assetPath) {
      return Files.isRegularFile(this.resolveAbsolutePath(assetPath));
   }

   @Override
   public byte[] getAssetBytes(Path assetPath) {
      try {
         return Files.readAllBytes(this.resolveAbsolutePath(assetPath));
      } catch (IOException var3) {
         ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var3)).log("Failed to read asset %s", assetPath);
         return null;
      }
   }

   @Override
   public boolean updateAsset(Path assetPath, byte[] bytes, EditorClient editorClient) {
      Path path = this.resolveAbsolutePath(assetPath);

      try {
         String hash = HashUtil.sha256(bytes);
         this.trackEditorFileSave(assetPath, hash);
         Files.write(path, bytes, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
         ModifiedAsset modifiedAsset = new ModifiedAsset();
         modifiedAsset.path = assetPath;
         modifiedAsset.state = AssetState.CHANGED;
         modifiedAsset.markEditedBy(editorClient);
         this.putModifiedAsset(modifiedAsset);
         return true;
      } catch (IOException var7) {
         ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var7)).log("Failed to update asset %s", assetPath);
         return false;
      }
   }

   @Override
   public boolean createAsset(Path assetPath, byte[] bytes, EditorClient editorClient) {
      Path path = this.resolveAbsolutePath(assetPath);

      try {
         String hash = HashUtil.sha256(bytes);
         this.trackEditorFileSave(assetPath, hash);
         Files.createDirectories(path.getParent());
         Files.write(path, bytes, StandardOpenOption.CREATE);
         ModifiedAsset modifiedAsset = new ModifiedAsset();
         modifiedAsset.path = assetPath;
         modifiedAsset.state = AssetState.NEW;
         modifiedAsset.markEditedBy(editorClient);
         this.putModifiedAsset(modifiedAsset);
         return true;
      } catch (IOException var7) {
         ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var7)).log("Failed to create asset %s", assetPath);
         return false;
      }
   }

   @Override
   public boolean deleteAsset(Path assetPath, EditorClient editorClient) {
      try {
         Files.deleteIfExists(this.resolveAbsolutePath(assetPath));
         ModifiedAsset modifiedAsset = new ModifiedAsset();
         modifiedAsset.path = assetPath;
         modifiedAsset.state = AssetState.DELETED;
         modifiedAsset.markEditedBy(editorClient);
         this.putModifiedAsset(modifiedAsset);
         return true;
      } catch (IOException var4) {
         ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var4)).log("Failed to delete asset %s", assetPath);
         return false;
      }
   }

   @Override
   public boolean shouldReloadAssetFromDisk(Path assetPath) {
      Deque<StandardDataSource.EditorFileSaveInfo> fileSaveInfos = this.editorSaves.get(assetPath);
      if (fileSaveInfos != null && !fileSaveInfos.isEmpty()) {
         byte[] bytes = this.getAssetBytes(assetPath);
         if (bytes == null) {
            return true;
         } else {
            String hash = HashUtil.sha256(bytes);
            long now = System.currentTimeMillis();
            synchronized (fileSaveInfos) {
               fileSaveInfos.removeIf(mx -> mx.expiryMs <= now);

               for (StandardDataSource.EditorFileSaveInfo m : fileSaveInfos) {
                  if (m.hash.equals(hash)) {
                     return false;
                  }
               }

               return true;
            }
         }
      } else {
         return true;
      }
   }

   @Override
   public Instant getLastModificationTimestamp(Path assetPath) {
      return null;
   }

   @Override
   public boolean moveAsset(Path oldAssetPath, Path newAssetPath, EditorClient editorClient) {
      try {
         Files.move(this.resolveAbsolutePath(oldAssetPath), this.resolveAbsolutePath(newAssetPath));
         ModifiedAsset modifiedAsset = new ModifiedAsset();
         modifiedAsset.path = newAssetPath;
         modifiedAsset.oldPath = oldAssetPath;
         modifiedAsset.state = AssetState.CHANGED;
         modifiedAsset.markEditedBy(editorClient);
         this.putModifiedAsset(modifiedAsset);
         return true;
      } catch (IOException var5) {
         ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var5)).log("Failed to move asset %s to %s", oldAssetPath, newAssetPath);
         return false;
      }
   }

   @Override
   public AssetTree loadAssetTree(Collection<AssetTypeHandler> assetTypes) {
      return new AssetTree(this.rootPath, this.packKey, this.isImmutable, this.isAssetPackBeDeleteable, assetTypes);
   }

   public void putModifiedAsset(ModifiedAsset modifiedAsset) {
      this.modifiedAssets.put(modifiedAsset.path, modifiedAsset);
      if (this.modifiedAssets.size() > 50) {
         ModifiedAsset oldestAsset = null;

         for (ModifiedAsset asset : this.modifiedAssets.values()) {
            if (oldestAsset == null) {
               oldestAsset = asset;
            } else if (asset.lastModificationTimestamp.isBefore(oldestAsset.lastModificationTimestamp)) {
               oldestAsset = asset;
            }
         }

         this.modifiedAssets.remove(oldestAsset.path);
      }

      this.indexNeedsSaving.set(true);
   }

   public Map<Path, ModifiedAsset> getRecentlyModifiedAssets() {
      return this.modifiedAssets;
   }

   private void trackEditorFileSave(Path path, String hash) {
      Deque<StandardDataSource.EditorFileSaveInfo> fileSaves = this.editorSaves.computeIfAbsent(path, p -> new ArrayDeque<>());
      synchronized (fileSaves) {
         fileSaves.addLast(new StandardDataSource.EditorFileSaveInfo(hash, System.currentTimeMillis() + 30000L));

         while (fileSaves.size() > 20) {
            fileSaves.removeFirst();
         }
      }
   }

   record EditorFileSaveInfo(String hash, long expiryMs) {
   }
}
