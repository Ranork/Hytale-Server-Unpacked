package com.hypixel.hytale.builtin.buildertools.prefablist;

import com.hypixel.hytale.common.util.PathUtil;
import com.hypixel.hytale.common.util.StringCompareUtil;
import com.hypixel.hytale.server.core.prefab.PrefabStore;
import com.hypixel.hytale.server.core.ui.browser.FileListProvider;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AssetPrefabFileProvider implements FileListProvider {
   private static final String PREFAB_EXTENSION = ".prefab.json";
   private static final int MAX_SEARCH_RESULTS = 50;
   private static final String BASE_ASSET_PACK_DISPLAY_NAME = "HytaleAssets";

   @Nonnull
   @Override
   public List<FileListProvider.FileEntry> getFiles(@Nonnull Path currentDir, @Nonnull String searchQuery) {
      String currentDirStr = currentDir.toString().replace('\\', '/');
      if (!searchQuery.isEmpty()) {
         return this.buildSearchResults(currentDirStr, searchQuery);
      } else {
         return currentDirStr.isEmpty() ? this.buildPackListings() : this.buildPackDirectoryListing(currentDirStr);
      }
   }

   @Nonnull
   private List<FileListProvider.FileEntry> buildPackListings() {
      List<FileListProvider.FileEntry> entries = new ObjectArrayList();

      for (PrefabStore.AssetPackPrefabPath packPath : PrefabStore.get().getAllAssetPrefabPaths()) {
         String displayName = packPath.getDisplayName();
         String packKey = this.getPackKey(packPath);
         entries.add(new FileListProvider.FileEntry(packKey, displayName, true));
      }

      entries.sort((a, b) -> {
         boolean aIsBase = "HytaleAssets".equals(a.displayName());
         boolean bIsBase = "HytaleAssets".equals(b.displayName());
         if (aIsBase != bIsBase) {
            return aIsBase ? -1 : 1;
         } else {
            return a.displayName().compareToIgnoreCase(b.displayName());
         }
      });
      return entries;
   }

   @Nonnull
   private List<FileListProvider.FileEntry> buildPackDirectoryListing(@Nonnull String currentDirStr) {
      List<FileListProvider.FileEntry> entries = new ObjectArrayList();
      String[] parts = currentDirStr.split("/", 2);
      String packKey = parts[0];
      String subPath = parts.length > 1 ? parts[1] : "";
      PrefabStore.AssetPackPrefabPath packPath = this.findPackByKey(packKey);
      if (packPath == null) {
         return entries;
      } else {
         Path targetPath = packPath.prefabsPath();
         if (!subPath.isEmpty()) {
            targetPath = PathUtil.resolvePathWithinDir(targetPath, subPath);
            if (targetPath == null) {
               return entries;
            }
         }

         if (!Files.isDirectory(targetPath)) {
            return entries;
         } else {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(targetPath)) {
               for (Path file : stream) {
                  String fileName = file.getFileName().toString();
                  if (!fileName.startsWith(".")) {
                     boolean isDirectory = Files.isDirectory(file);
                     if (isDirectory || fileName.endsWith(".prefab.json")) {
                        String displayName = isDirectory ? fileName : this.removeExtension(fileName);
                        entries.add(new FileListProvider.FileEntry(fileName, displayName, isDirectory));
                     }
                  }
               }
            } catch (IOException var16) {
            }

            entries.sort((a, b) -> {
               if (a.isDirectory() == b.isDirectory()) {
                  return a.displayName().compareToIgnoreCase(b.displayName());
               } else {
                  return a.isDirectory() ? -1 : 1;
               }
            });
            return entries;
         }
      }
   }

   @Nonnull
   private List<FileListProvider.FileEntry> buildSearchResults(@Nonnull String currentDirStr, @Nonnull String searchQuery) {
      List<AssetPrefabFileProvider.SearchResult> allResults = new ObjectArrayList();
      String lowerQuery = searchQuery.toLowerCase();
      if (currentDirStr.isEmpty()) {
         for (PrefabStore.AssetPackPrefabPath packPath : PrefabStore.get().getAllAssetPrefabPaths()) {
            String packKey = this.getPackKey(packPath);
            this.searchInDirectory(packPath.prefabsPath(), packKey, "", lowerQuery, allResults);
         }
      } else {
         String[] parts = currentDirStr.split("/", 2);
         String packKey = parts[0];
         String subPath = parts.length > 1 ? parts[1] : "";
         PrefabStore.AssetPackPrefabPath packPath = this.findPackByKey(packKey);
         if (packPath != null) {
            Path searchRoot = packPath.prefabsPath();
            if (!subPath.isEmpty()) {
               searchRoot = PathUtil.resolvePathWithinDir(searchRoot, subPath);
               if (searchRoot == null) {
                  return List.of();
               }
            }

            this.searchInDirectory(searchRoot, packKey, subPath, lowerQuery, allResults);
         }
      }

      allResults.sort(Comparator.comparingInt(AssetPrefabFileProvider.SearchResult::score).reversed());
      List<FileListProvider.FileEntry> entries = new ObjectArrayList();

      for (int i = 0; i < Math.min(allResults.size(), 50); i++) {
         AssetPrefabFileProvider.SearchResult result = allResults.get(i);
         entries.add(new FileListProvider.FileEntry(result.relativePath(), result.displayName(), false, false, result.score()));
      }

      return entries;
   }

   private void searchInDirectory(
      @Nonnull final Path root,
      @Nonnull final String packKey,
      @Nonnull final String basePath,
      @Nonnull final String searchQuery,
      @Nonnull final List<AssetPrefabFileProvider.SearchResult> results
   ) {
      if (Files.isDirectory(root)) {
         try {
            Files.walkFileTree(
               root,
               new SimpleFileVisitor<Path>() {
                  @Nonnull
                  public FileVisitResult visitFile(@Nonnull Path file, @Nonnull BasicFileAttributes attrs) {
                     String fileName = file.getFileName().toString();
                     if (fileName.endsWith(".prefab.json")) {
                        String baseName = AssetPrefabFileProvider.this.removeExtension(fileName);
                        int score = StringCompareUtil.getFuzzyDistance(baseName.toLowerCase(), searchQuery, Locale.ENGLISH);
                        if (score > 0) {
                           Path relativePath = root.relativize(file);
                           String fullRelativePath = basePath.isEmpty()
                              ? packKey + "/" + relativePath.toString().replace('\\', '/')
                              : packKey + "/" + basePath + "/" + relativePath.toString().replace('\\', '/');
                           results.add(new AssetPrefabFileProvider.SearchResult(fullRelativePath, baseName, score));
                        }
                     }

                     return FileVisitResult.CONTINUE;
                  }
               }
            );
         } catch (IOException var7) {
         }
      }
   }

   @Nonnull
   private String getPackKey(@Nonnull PrefabStore.AssetPackPrefabPath packPath) {
      return packPath.getDisplayName();
   }

   @Nullable
   private PrefabStore.AssetPackPrefabPath findPackByKey(@Nonnull String packKey) {
      for (PrefabStore.AssetPackPrefabPath packPath : PrefabStore.get().getAllAssetPrefabPaths()) {
         if (this.getPackKey(packPath).equals(packKey)) {
            return packPath;
         }
      }

      return null;
   }

   @Nonnull
   private String removeExtension(@Nonnull String fileName) {
      return fileName.endsWith(".prefab.json") ? fileName.substring(0, fileName.length() - ".prefab.json".length()) : fileName;
   }

   @Nullable
   public Path resolveVirtualPath(@Nonnull String virtualPath) {
      if (virtualPath.isEmpty()) {
         return null;
      } else {
         String[] parts = virtualPath.split("/", 2);
         String packKey = parts[0];
         String subPath = parts.length > 1 ? parts[1] : "";
         PrefabStore.AssetPackPrefabPath packPath = this.findPackByKey(packKey);
         if (packPath == null) {
            return null;
         } else {
            return subPath.isEmpty() ? packPath.prefabsPath() : PathUtil.resolvePathWithinDir(packPath.prefabsPath(), subPath);
         }
      }
   }

   @Nonnull
   public String getPackDisplayName(@Nonnull String packKey) {
      PrefabStore.AssetPackPrefabPath packPath = this.findPackByKey(packKey);
      return packPath != null ? packPath.getDisplayName() : packKey;
   }

   private record SearchResult(@Nonnull String relativePath, @Nonnull String displayName, int score) {
   }
}
