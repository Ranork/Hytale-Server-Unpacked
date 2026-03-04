package com.hypixel.hytale.builtin.asseteditor;

import com.hypixel.hytale.builtin.asseteditor.assettypehandler.AssetTypeHandler;
import com.hypixel.hytale.builtin.asseteditor.data.AssetState;
import com.hypixel.hytale.builtin.asseteditor.data.ModifiedAsset;
import com.hypixel.hytale.builtin.asseteditor.util.AssetPathUtil;
import com.hypixel.hytale.common.util.FormatUtil;
import com.hypixel.hytale.common.util.ListUtil;
import com.hypixel.hytale.common.util.PathUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorAssetListSetup;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorFileEntry;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorFileTree;
import com.hypixel.hytale.server.core.asset.common.CommonAssetModule;
import com.hypixel.hytale.server.core.util.io.FileUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.StampedLock;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AssetTree {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private final StampedLock lock = new StampedLock();
   private final Path rootPath;
   private final String packKey;
   private final boolean isReadOnly;
   private final boolean canBeDeleted;
   List<AssetEditorFileEntry> serverAssets = new ObjectArrayList();
   List<AssetEditorFileEntry> commonAssets = new ObjectArrayList();

   public AssetTree(Path rootPath, String packKey, boolean isReadOnly, boolean canBeDeleted) {
      this.rootPath = rootPath;
      this.packKey = packKey;
      this.isReadOnly = isReadOnly;
      this.canBeDeleted = canBeDeleted;
   }

   public AssetTree(Path rootPath, String packKey, boolean isReadOnly, boolean canBeDeleted, @Nonnull Collection<AssetTypeHandler> assetTypes) {
      this.rootPath = rootPath;
      this.packKey = packKey;
      this.isReadOnly = isReadOnly;
      this.canBeDeleted = canBeDeleted;
      this.load(assetTypes);
   }

   public void replaceAssetTree(@Nonnull AssetTree assetTree) {
      long stamp = this.lock.writeLock();

      try {
         this.serverAssets = assetTree.serverAssets;
         this.commonAssets = assetTree.commonAssets;
      } finally {
         this.lock.unlockWrite(stamp);
      }
   }

   public void sendPackets(@Nonnull EditorClient editorClient) {
      long stamp = this.lock.readLock();

      try {
         editorClient.getPacketHandler()
            .write(
               new AssetEditorAssetListSetup(
                  this.packKey, this.isReadOnly, this.canBeDeleted, AssetEditorFileTree.Server, this.serverAssets.toArray(AssetEditorFileEntry[]::new)
               )
            );
         editorClient.getPacketHandler()
            .write(
               new AssetEditorAssetListSetup(
                  this.packKey, this.isReadOnly, this.canBeDeleted, AssetEditorFileTree.Common, this.commonAssets.toArray(AssetEditorFileEntry[]::new)
               )
            );
      } finally {
         this.lock.unlockRead(stamp);
      }
   }

   public boolean isDirectoryEmpty(@Nonnull Path path) {
      String pathString = PathUtil.toUnixPathString(path);
      long stamp = this.lock.readLock();

      boolean hasFile;
      try {
         List<AssetEditorFileEntry> assets = this.getAssetListForPath(path);
         int index = ListUtil.binarySearch(assets, o -> o.path, pathString, String::compareTo);
         if (index < 0) {
            return true;
         }

         if (!assets.get(index).isDirectory) {
            return false;
         }

         int fileIndex = index + 1;
         if (fileIndex < assets.size()) {
            hasFile = assets.get(fileIndex).path.startsWith(pathString + "/");
            return !hasFile;
         }

         hasFile = false;
      } finally {
         this.lock.unlockRead(stamp);
      }

      return hasFile;
   }

   @Nullable
   public AssetEditorFileEntry ensureAsset(@Nonnull Path path, boolean isDirectory) {
      String pathString = PathUtil.toUnixPathString(path);
      long stamp = this.lock.writeLock();

      Object insertionPoint;
      try {
         List<AssetEditorFileEntry> assets = this.getAssetListForPath(path);
         int index = ListUtil.binarySearch(assets, o -> o.path, pathString, String::compareTo);
         if (index < 0) {
            int insertionPointx = -(index + 1);
            if (path.getNameCount() > 1) {
               Path parentPath = path.getName(0);

               for (int i = 1; i < path.getNameCount() - 1; i++) {
                  parentPath = parentPath.resolve(path.getName(i));
                  String name = PathUtil.toUnixPathString(parentPath);
                  if (insertionPointx <= 0 || !assets.get(insertionPointx - 1).path.startsWith(name)) {
                     assets.add(insertionPointx++, new AssetEditorFileEntry(name, true));
                  }
               }
            }

            AssetEditorFileEntry entry = new AssetEditorFileEntry(pathString, isDirectory);
            assets.add(insertionPointx, entry);
            return entry;
         }

         insertionPoint = null;
      } finally {
         this.lock.unlockWrite(stamp);
      }

      return (AssetEditorFileEntry)insertionPoint;
   }

   @Nullable
   public AssetEditorFileEntry getAssetFile(@Nonnull Path path) {
      String pathString = PathUtil.toUnixPathString(path);
      long stamp = this.lock.readLock();

      AssetEditorFileEntry var7;
      try {
         List<AssetEditorFileEntry> assets = this.getAssetListForPath(path);
         int index = ListUtil.binarySearch(assets, o -> o.path, pathString, String::compareTo);
         var7 = index >= 0 ? assets.get(index) : null;
      } finally {
         this.lock.unlockRead(stamp);
      }

      return var7;
   }

   @Nullable
   public AssetEditorFileEntry removeAsset(@Nonnull Path path) {
      String pathString = PathUtil.toUnixPathString(path);
      long stamp = this.lock.writeLock();

      AssetEditorFileEntry entry;
      try {
         List<AssetEditorFileEntry> assets = this.getAssetListForPath(path);
         int index = ListUtil.binarySearch(assets, o -> o.path, pathString, String::compareTo);
         if (index >= 0) {
            entry = assets.remove(index);
            if (entry.isDirectory) {
               String pathPrefix = pathString + "/";
               int removeCount = 0;

               for (int i = index; i < assets.size(); i++) {
                  AssetEditorFileEntry asset = assets.get(i);
                  if (!asset.path.startsWith(pathPrefix)) {
                     break;
                  }

                  removeCount++;
               }

               for (int i = 0; i < removeCount; i++) {
                  assets.remove(index);
               }
            }

            return entry;
         }

         entry = null;
      } finally {
         this.lock.unlockWrite(stamp);
      }

      return entry;
   }

   public void applyAssetChanges(@Nonnull Map<Path, ModifiedAsset> createdDirectories, @Nonnull Map<Path, ModifiedAsset> modifiedAssets) {
      for (ModifiedAsset dir : createdDirectories.values()) {
         this.ensureAsset(dir.path, true);
      }

      for (ModifiedAsset file : modifiedAssets.values()) {
         if (file.state == AssetState.NEW) {
            this.ensureAsset(file.path, false);
         } else if (file.state == AssetState.DELETED) {
            this.removeAsset(file.oldPath != null ? file.oldPath : file.path);
         } else if (file.oldPath != null) {
            this.removeAsset(file.oldPath);
            this.ensureAsset(file.path, false);
         }
      }
   }

   private List<AssetEditorFileEntry> getAssetListForPath(@Nonnull Path path) {
      if (path.getNameCount() > 0) {
         String firstName = path.getName(0).toString();
         if (firstName.equals("..")) {
            try {
               firstName = path.getName(2).toString();
            } catch (IllegalArgumentException var4) {
            }
         }

         if ("Server".equals(firstName)) {
            return this.serverAssets;
         }

         if ("Common".equals(firstName)) {
            return this.commonAssets;
         }
      }

      throw new IllegalArgumentException("Invalid path " + path);
   }

   private void load(@Nonnull Collection<AssetTypeHandler> assetTypes) {
      try {
         long start = System.nanoTime();
         loadServerAssets(this.rootPath, assetTypes, this.serverAssets);
         LOGGER.at(Level.INFO).log("Loaded Server/ asset tree! Took: %s", FormatUtil.nanosToString(System.nanoTime() - start));
      } catch (IOException var5) {
         ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var5)).log("Failed to load server asset tree!");
      }

      try {
         long start = System.nanoTime();
         walkFileTree(this.rootPath, this.rootPath.resolve("Common"), this.commonAssets);
         LOGGER.at(Level.INFO).log("Loaded Common/ asset tree! Took: %s", FormatUtil.nanosToString(System.nanoTime() - start));
      } catch (IOException var4) {
         ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var4)).log("Failed to load common asset tree!");
      }

      long start = System.nanoTime();
      this.serverAssets.sort(Comparator.comparing(o -> o.path));
      this.commonAssets.sort(Comparator.comparing(o -> o.path));
      LOGGER.at(Level.INFO).log("Sorted asset tree! Took: %s", FormatUtil.nanosToString(System.nanoTime() - start));
   }

   private static void loadServerAssets(@Nonnull Path root, @Nonnull Collection<AssetTypeHandler> assetTypes, @Nonnull List<AssetEditorFileEntry> files) throws IOException {
      Set<String> assetTypePaths = new HashSet<>();
      Set<String> subPaths = new HashSet<>();

      for (AssetTypeHandler assetTypeHandler : assetTypes) {
         if (assetTypeHandler.getRootPath().startsWith(AssetPathUtil.PATH_DIR_SERVER)) {
            String assetTypePath = assetTypeHandler.getConfig().path;
            if (assetTypePaths.add(assetTypePath)) {
               Path path = Path.of(assetTypePath);
               Path subpath = AssetPathUtil.PATH_DIR_SERVER;

               for (int i = 1; i < path.getNameCount() - 1; i++) {
                  subpath = subpath.resolve(path.getName(i));
                  String name = PathUtil.toUnixPathString(subpath);
                  if (subPaths.add(name)) {
                     files.add(new AssetEditorFileEntry(name, true));
                  }
               }
            }
         }
      }

      for (String path : assetTypePaths) {
         Path dirPath = root.resolve(path);
         walkFileTree(root, dirPath, files);
         String name = PathUtil.toUnixPathString(root.relativize(dirPath));
         files.add(new AssetEditorFileEntry(name, true));
      }
   }

   private static void walkFileTree(@Nonnull final Path root, @Nonnull final Path dirPath, @Nonnull final List<AssetEditorFileEntry> files) throws IOException {
      if (Files.isDirectory(dirPath)) {
         Files.walkFileTree(dirPath, FileUtil.DEFAULT_WALK_TREE_OPTIONS_SET, Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
            public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs) throws IOException {
               if (path.equals(dirPath)) {
                  return FileVisitResult.CONTINUE;
               } else {
                  files.add(new AssetEditorFileEntry(PathUtil.toUnixPathString(root.relativize(path)), true));
                  return super.preVisitDirectory(path, attrs);
               }
            }

            @Nonnull
            public FileVisitResult visitFile(@Nonnull Path path, @Nonnull BasicFileAttributes attrs) {
               if (CommonAssetModule.IGNORED_FILES.contains(path.getFileName())) {
                  return FileVisitResult.CONTINUE;
               } else {
                  files.add(new AssetEditorFileEntry(PathUtil.toUnixPathString(root.relativize(path)), false));
                  return FileVisitResult.CONTINUE;
               }
            }
         });
      }
   }
}
