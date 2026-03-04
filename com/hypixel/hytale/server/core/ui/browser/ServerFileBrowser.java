package com.hypixel.hytale.server.core.ui.browser;

import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.common.util.PathUtil;
import com.hypixel.hytale.common.util.StringCompareUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ServerFileBrowser {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private static final Value<String> BUTTON_HIGHLIGHTED = Value.ref("Pages/BasicTextButton.ui", "SelectedLabelStyle");
   private static final String BASE_ASSET_PACK_DISPLAY_NAME = "HytaleAssets";
   @Nonnull
   private final FileBrowserConfig config;
   @Nonnull
   private Path root;
   @Nonnull
   private Path currentDir;
   @Nonnull
   private String searchQuery;
   @Nonnull
   private final Set<String> selectedItems;

   public ServerFileBrowser(@Nonnull FileBrowserConfig config) {
      this.config = config;
      this.selectedItems = new LinkedHashSet<>();
      this.searchQuery = "";
      if (!config.roots().isEmpty()) {
         this.root = config.roots().get(0).path();
      } else {
         this.root = Paths.get("");
      }

      this.currentDir = this.root.getFileSystem().getPath("");
   }

   public ServerFileBrowser(@Nonnull FileBrowserConfig config, @Nullable Path initialRoot, @Nullable Path initialDir) {
      this(config);
      if (initialRoot != null && Files.isDirectory(initialRoot)) {
         this.root = initialRoot;
         this.currentDir = this.root.getFileSystem().getPath("");
      }

      if (initialDir != null) {
         this.currentDir = initialDir;
      }
   }

   public void buildRootSelector(@Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder) {
      if (this.config.enableRootSelector() && this.config.rootSelectorId() != null) {
         ObjectArrayList<DropdownEntryInfo> entries = new ObjectArrayList();

         for (FileBrowserConfig.RootEntry rootEntry : this.config.roots()) {
            entries.add(new DropdownEntryInfo(rootEntry.displayName(), rootEntry.path().toString()));
         }

         commandBuilder.set(this.config.rootSelectorId() + ".Entries", entries);
         commandBuilder.set(this.config.rootSelectorId() + ".Value", this.root.toString());
         eventBuilder.addEventBinding(
            CustomUIEventBindingType.ValueChanged,
            this.config.rootSelectorId(),
            new EventData().append("@Root", this.config.rootSelectorId() + ".Value"),
            false
         );
      } else {
         if (this.config.rootSelectorId() != null) {
            commandBuilder.set(this.config.rootSelectorId() + ".Visible", false);
         }
      }
   }

   public void buildSearchInput(@Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder) {
      if (this.config.enableSearch() && this.config.searchInputId() != null) {
         if (!this.searchQuery.isEmpty()) {
            commandBuilder.set(this.config.searchInputId() + ".Value", this.searchQuery);
         }

         eventBuilder.addEventBinding(
            CustomUIEventBindingType.ValueChanged, this.config.searchInputId(), EventData.of("@SearchQuery", this.config.searchInputId() + ".Value"), false
         );
      }
   }

   public void buildCurrentPath(@Nonnull UICommandBuilder commandBuilder) {
      if (this.config.currentPathId() != null) {
         String displayPath;
         if (this.config.assetPackMode()) {
            String currentDirStr = this.currentDir.toString().replace('\\', '/');
            if (currentDirStr.isEmpty()) {
               displayPath = "Assets";
            } else {
               String[] parts = currentDirStr.split("/", 2);
               String packName = parts[0];
               String subPath = parts.length > 1 ? "/" + parts[1] : "";
               if ("HytaleAssets".equals(packName)) {
                  displayPath = packName + subPath;
               } else {
                  displayPath = "Mods/" + packName + subPath;
               }
            }
         } else {
            String rootDisplay = this.root.toString().replace("\\", "/");
            String relativeDisplay = this.currentDir.toString().isEmpty() ? "" : "/" + this.currentDir.toString().replace("\\", "/");
            displayPath = rootDisplay + relativeDisplay;
         }

         commandBuilder.set(this.config.currentPathId() + ".Text", displayPath);
      }
   }

   public void buildFileList(@Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder) {
      commandBuilder.clear(this.config.listElementId());
      List<FileListProvider.FileEntry> entries;
      if (this.config.customProvider() != null) {
         entries = this.config.customProvider().getFiles(this.currentDir, this.searchQuery);
      } else if (this.config.assetPackMode()) {
         if (!this.searchQuery.isEmpty() && this.config.enableSearch()) {
            entries = this.buildAssetPackSearchResults();
         } else {
            entries = this.buildAssetPackListing();
         }
      } else if (!this.searchQuery.isEmpty() && this.config.enableSearch()) {
         entries = this.buildSearchResults();
      } else {
         entries = this.buildDirectoryListing();
      }

      int buttonIndex = 0;
      if (this.config.enableDirectoryNav() && !this.currentDir.toString().isEmpty() && this.searchQuery.isEmpty()) {
         commandBuilder.append(this.config.listElementId(), "Pages/BasicTextButton.ui");
         commandBuilder.set(this.config.listElementId() + "[0].Text", "../");
         eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, this.config.listElementId() + "[0]", EventData.of("File", ".."));
         buttonIndex++;
      }

      for (FileListProvider.FileEntry entry : entries) {
         boolean isNavigableDir = entry.isDirectory() && !entry.isTerminal();
         String displayText = isNavigableDir ? entry.displayName() + "/" : entry.displayName();
         commandBuilder.append(this.config.listElementId(), "Pages/BasicTextButton.ui");
         commandBuilder.set(this.config.listElementId() + "[" + buttonIndex + "].Text", displayText);
         if (!entry.isDirectory() || entry.isTerminal()) {
            commandBuilder.set(this.config.listElementId() + "[" + buttonIndex + "].Style", BUTTON_HIGHLIGHTED);
         }

         String eventKey = !this.searchQuery.isEmpty() && !isNavigableDir ? "SearchResult" : "File";
         eventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating, this.config.listElementId() + "[" + buttonIndex + "]", EventData.of(eventKey, entry.name())
         );
         buttonIndex++;
      }
   }

   public void buildUI(@Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder) {
      this.buildRootSelector(commandBuilder, eventBuilder);
      this.buildSearchInput(commandBuilder, eventBuilder);
      this.buildCurrentPath(commandBuilder);
      this.buildFileList(commandBuilder, eventBuilder);
   }

   public boolean handleEvent(@Nonnull FileBrowserEventData data) {
      if (data.getSearchQuery() != null) {
         this.searchQuery = data.getSearchQuery().trim().toLowerCase();
         return true;
      } else if (data.getRoot() != null) {
         Path newRoot = this.findConfigRoot(data.getRoot());
         if (newRoot == null) {
            newRoot = Path.of(data.getRoot());
         }

         this.setRoot(newRoot);
         this.currentDir = this.root.getFileSystem().getPath("");
         this.searchQuery = "";
         return true;
      } else if (data.getFile() != null) {
         String fileName = data.getFile();
         if ("..".equals(fileName)) {
            this.navigateUp();
            return true;
         } else if (this.config.assetPackMode()) {
            return this.handleAssetPackNavigation(fileName);
         } else {
            if (this.config.enableDirectoryNav()) {
               Path currentPath = this.root.resolve(this.currentDir.toString());
               Path targetPath = PathUtil.resolvePathWithinDir(currentPath, fileName);
               if (targetPath != null && Files.isDirectory(targetPath) && PathUtil.isChildOf(this.root, targetPath)) {
                  this.currentDir = PathUtil.relativize(this.root, targetPath);
                  return true;
               }
            }

            return false;
         }
      } else {
         return data.getSearchResult() != null ? false : false;
      }
   }

   private List<FileListProvider.FileEntry> buildDirectoryListing() {
      List<FileListProvider.FileEntry> entries = new ObjectArrayList();
      Path path = this.root.resolve(this.currentDir.toString());
      if (!Files.isDirectory(path)) {
         return entries;
      } else {
         try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path file : stream) {
               String fileName = file.getFileName().toString();
               if (!fileName.startsWith(".")) {
                  boolean isDirectory = Files.isDirectory(file);
                  if (isDirectory || this.matchesExtension(fileName)) {
                     entries.add(new FileListProvider.FileEntry(fileName, isDirectory));
                  }
               }
            }
         } catch (IOException var10) {
            ((HytaleLogger.Api)((HytaleLogger.Api)LOGGER.atSevere()).withCause(var10)).log("Error listing directory: %s", path);
         }

         entries.sort((a, b) -> {
            if (a.isDirectory() == b.isDirectory()) {
               return a.name().compareToIgnoreCase(b.name());
            } else {
               return a.isDirectory() ? -1 : 1;
            }
         });
         return entries;
      }
   }

   private List<FileListProvider.FileEntry> buildSearchResults() {
      final List<Path> allFiles = new ObjectArrayList();
      if (!Files.isDirectory(this.root)) {
         return List.of();
      } else {
         try {
            Files.walkFileTree(this.root, new SimpleFileVisitor<Path>() {
               @Nonnull
               public FileVisitResult visitFile(@Nonnull Path file, @Nonnull BasicFileAttributes attrs) {
                  String fileName = file.getFileName().toString();
                  if (ServerFileBrowser.this.matchesExtension(fileName)) {
                     allFiles.add(file);
                  }

                  return FileVisitResult.CONTINUE;
               }
            });
         } catch (IOException var8) {
            ((HytaleLogger.Api)((HytaleLogger.Api)LOGGER.atSevere()).withCause(var8)).log("Error walking directory: %s", this.root);
         }

         Object2IntMap<Path> matchScores = new Object2IntOpenHashMap(allFiles.size());

         for (Path file : allFiles) {
            String fileName = file.getFileName().toString();
            String baseName = this.removeExtensions(fileName);
            int score = StringCompareUtil.getFuzzyDistance(baseName, this.searchQuery, Locale.ENGLISH);
            if (score > 0) {
               matchScores.put(file, score);
            }
         }

         return matchScores.keySet().stream().sorted(Comparator.comparingInt(matchScores::getInt).reversed()).limit(this.config.maxResults()).map(filex -> {
            Path relativePath = PathUtil.relativize(this.root, filex);
            String fileNamex = filex.getFileName().toString();
            String displayName = this.removeExtensions(fileNamex);
            return new FileListProvider.FileEntry(relativePath.toString(), displayName, false, false, matchScores.getInt(filex));
         }).collect(Collectors.toList());
      }
   }

   private boolean matchesExtension(@Nonnull String fileName) {
      if (this.config.allowedExtensions().isEmpty()) {
         return true;
      } else {
         for (String ext : this.config.allowedExtensions()) {
            if (fileName.endsWith(ext)) {
               return true;
            }
         }

         return false;
      }
   }

   private List<FileListProvider.FileEntry> buildAssetPackListing() {
      List<FileListProvider.FileEntry> entries = new ObjectArrayList();
      String currentDirStr = this.currentDir.toString().replace('\\', '/');
      if (currentDirStr.isEmpty()) {
         for (AssetPack pack : AssetModule.get().getAssetPacks()) {
            Path subPath = this.getAssetPackSubPath(pack);
            if (subPath != null && Files.isDirectory(subPath)) {
               String displayName = this.getAssetPackDisplayName(pack);
               entries.add(new FileListProvider.FileEntry(displayName, displayName, true));
            }
         }
      } else {
         String[] parts = currentDirStr.split("/", 2);
         String packName = parts[0];
         String subDir = parts.length > 1 ? parts[1] : "";
         AssetPack packx = this.findAssetPackByDisplayName(packName);
         if (packx != null) {
            Path packSubPath = this.getAssetPackSubPath(packx);
            if (packSubPath != null) {
               Path targetDir = subDir.isEmpty() ? packSubPath : PathUtil.resolvePathWithinDir(packSubPath, subDir);
               if (targetDir != null && Files.isDirectory(targetDir)) {
                  try (DirectoryStream<Path> stream = Files.newDirectoryStream(targetDir)) {
                     for (Path file : stream) {
                        String fileName = file.getFileName().toString();
                        if (!fileName.startsWith(".")) {
                           boolean isDirectory = Files.isDirectory(file);
                           if (isDirectory || this.matchesExtension(fileName)) {
                              String displayName = isDirectory ? fileName : this.removeExtensions(fileName);
                              boolean isTerminal = isDirectory && this.isTerminalDirectory(file);
                              entries.add(new FileListProvider.FileEntry(fileName, displayName, isDirectory, isTerminal));
                           }
                        }
                     }
                  } catch (IOException var18) {
                     ((HytaleLogger.Api)((HytaleLogger.Api)LOGGER.atSevere()).withCause(var18)).log("Error listing asset pack directory: %s", targetDir);
                  }
               }
            }
         }
      }

      entries.sort((a, b) -> {
         boolean aIsBase = "HytaleAssets".equals(a.name());
         boolean bIsBase = "HytaleAssets".equals(b.name());
         if (aIsBase != bIsBase) {
            return aIsBase ? -1 : 1;
         } else if (a.isDirectory() == b.isDirectory()) {
            return a.displayName().compareToIgnoreCase(b.displayName());
         } else {
            return a.isDirectory() ? -1 : 1;
         }
      });
      return entries;
   }

   private List<FileListProvider.FileEntry> buildAssetPackSearchResults() {
      List<ServerFileBrowser.AssetPackSearchResult> allResults = new ObjectArrayList();
      String currentDirStr = this.currentDir.toString().replace('\\', '/');
      if (currentDirStr.isEmpty()) {
         for (AssetPack pack : AssetModule.get().getAssetPacks()) {
            Path subPath = this.getAssetPackSubPath(pack);
            if (subPath != null && Files.isDirectory(subPath)) {
               String packDisplayName = this.getAssetPackDisplayName(pack);
               this.searchInAssetPackDirectory(subPath, packDisplayName, "", allResults);
            }
         }
      } else {
         String[] parts = currentDirStr.split("/", 2);
         String packName = parts[0];
         String subDir = parts.length > 1 ? parts[1] : "";
         AssetPack packx = this.findAssetPackByDisplayName(packName);
         if (packx != null) {
            Path packSubPath = this.getAssetPackSubPath(packx);
            if (packSubPath != null) {
               Path searchRoot = subDir.isEmpty() ? packSubPath : PathUtil.resolvePathWithinDir(packSubPath, subDir);
               if (searchRoot != null && Files.isDirectory(searchRoot)) {
                  this.searchInAssetPackDirectory(searchRoot, packName, subDir, allResults);
               }
            }
         }
      }

      allResults.sort(Comparator.comparingInt(ServerFileBrowser.AssetPackSearchResult::score).reversed());
      return allResults.stream()
         .limit(this.config.maxResults())
         .map(r -> new FileListProvider.FileEntry(r.virtualPath(), r.displayName(), r.isTerminal(), r.isTerminal(), r.score()))
         .collect(Collectors.toList());
   }

   private void searchInAssetPackDirectory(
      @Nonnull final Path searchRoot,
      @Nonnull final String packName,
      @Nonnull final String basePath,
      @Nonnull final List<ServerFileBrowser.AssetPackSearchResult> results
   ) {
      try {
         Files.walkFileTree(searchRoot, new SimpleFileVisitor<Path>() {
            @Nonnull
            public FileVisitResult preVisitDirectory(@Nonnull Path dir, @Nonnull BasicFileAttributes attrs) {
               if (dir.equals(searchRoot)) {
                  return FileVisitResult.CONTINUE;
               } else if (ServerFileBrowser.this.isTerminalDirectory(dir)) {
                  String dirName = dir.getFileName().toString();
                  int score = StringCompareUtil.getFuzzyDistance(dirName.toLowerCase(), ServerFileBrowser.this.searchQuery, Locale.ENGLISH);
                  if (score > 0) {
                     Path relativePath = searchRoot.relativize(dir);
                     String relativeStr = relativePath.toString().replace('\\', '/');
                     String virtualPath = basePath.isEmpty() ? packName + "/" + relativeStr : packName + "/" + basePath + "/" + relativeStr;
                     results.add(new ServerFileBrowser.AssetPackSearchResult(virtualPath, dirName, score, true));
                  }

                  return FileVisitResult.SKIP_SUBTREE;
               } else {
                  return FileVisitResult.CONTINUE;
               }
            }

            @Nonnull
            public FileVisitResult visitFile(@Nonnull Path file, @Nonnull BasicFileAttributes attrs) {
               String fileName = file.getFileName().toString();
               if (ServerFileBrowser.this.matchesExtension(fileName)) {
                  String baseName = ServerFileBrowser.this.removeExtensions(fileName);
                  int score = StringCompareUtil.getFuzzyDistance(baseName.toLowerCase(), ServerFileBrowser.this.searchQuery, Locale.ENGLISH);
                  if (score > 0) {
                     Path relativePath = searchRoot.relativize(file);
                     String relativeStr = relativePath.toString().replace('\\', '/');
                     String virtualPath = basePath.isEmpty() ? packName + "/" + relativeStr : packName + "/" + basePath + "/" + relativeStr;
                     results.add(new ServerFileBrowser.AssetPackSearchResult(virtualPath, baseName, score, false));
                  }
               }

               return FileVisitResult.CONTINUE;
            }
         });
      } catch (IOException var6) {
         ((HytaleLogger.Api)((HytaleLogger.Api)LOGGER.atSevere()).withCause(var6)).log("Error searching asset pack directory: %s", searchRoot);
      }
   }

   private boolean handleAssetPackNavigation(@Nonnull String fileName) {
      String currentDirStr = this.currentDir.toString().replace('\\', '/');
      if (currentDirStr.isEmpty()) {
         AssetPack pack = this.findAssetPackByDisplayName(fileName);
         if (pack != null) {
            Path subPath = this.getAssetPackSubPath(pack);
            if (subPath != null && Files.isDirectory(subPath)) {
               this.currentDir = Paths.get(fileName);
               return true;
            }
         }

         return false;
      } else {
         String[] parts = currentDirStr.split("/", 2);
         String packName = parts[0];
         String subDir = parts.length > 1 ? parts[1] : "";
         AssetPack pack = this.findAssetPackByDisplayName(packName);
         if (pack == null) {
            return false;
         } else {
            Path packSubPath = this.getAssetPackSubPath(pack);
            if (packSubPath == null) {
               return false;
            } else {
               Path targetDir = subDir.isEmpty() ? packSubPath : PathUtil.resolvePathWithinDir(packSubPath, subDir);
               if (targetDir == null) {
                  return false;
               } else {
                  Path targetPath = PathUtil.resolvePathWithinDir(targetDir, fileName);
                  if (targetPath == null) {
                     return false;
                  } else if (Files.isDirectory(targetPath)) {
                     if (this.isTerminalDirectory(targetPath)) {
                        return false;
                     } else {
                        String newPath = subDir.isEmpty() ? packName + "/" + fileName : packName + "/" + subDir + "/" + fileName;
                        this.currentDir = Paths.get(newPath);
                        return true;
                     }
                  } else {
                     return false;
                  }
               }
            }
         }
      }
   }

   @Nullable
   private Path getAssetPackSubPath(@Nonnull AssetPack pack) {
      return this.config.assetPackSubPath() == null ? null : pack.getRoot().resolve(this.config.assetPackSubPath());
   }

   @Nonnull
   private String getAssetPackDisplayName(@Nonnull AssetPack pack) {
      if (pack.equals(AssetModule.get().getBaseAssetPack())) {
         return "HytaleAssets";
      } else {
         PluginManifest manifest = pack.getManifest();
         return manifest != null ? manifest.getName() : pack.getName();
      }
   }

   @Nullable
   private AssetPack findAssetPackByDisplayName(@Nonnull String displayName) {
      for (AssetPack pack : AssetModule.get().getAssetPacks()) {
         if (this.getAssetPackDisplayName(pack).equals(displayName)) {
            return pack;
         }
      }

      return null;
   }

   private boolean isTerminalDirectory(@Nonnull Path path) {
      Predicate<Path> predicate = this.config.terminalDirectoryPredicate();
      return predicate != null && predicate.test(path);
   }

   @Nullable
   public Path resolveAssetPackPath(@Nonnull String virtualPath) {
      if (this.config.assetPackMode() && !virtualPath.isEmpty()) {
         String[] parts = virtualPath.replace('\\', '/').split("/", 2);
         String packName = parts[0];
         String subPath = parts.length > 1 ? parts[1] : "";
         AssetPack pack = this.findAssetPackByDisplayName(packName);
         if (pack == null) {
            return null;
         } else {
            Path packSubPath = this.getAssetPackSubPath(pack);
            if (packSubPath == null) {
               return null;
            } else {
               return subPath.isEmpty() ? packSubPath : PathUtil.resolvePathWithinDir(packSubPath, subPath);
            }
         }
      } else {
         return null;
      }
   }

   @Nonnull
   public String getAssetPackCurrentPath() {
      return this.currentDir.toString().replace('\\', '/');
   }

   private String removeExtensions(@Nonnull String fileName) {
      for (String ext : this.config.allowedExtensions()) {
         if (fileName.endsWith(ext)) {
            return fileName.substring(0, fileName.length() - ext.length());
         }
      }

      return fileName;
   }

   @Nonnull
   public Path getRoot() {
      return this.root;
   }

   public void setRoot(@Nonnull Path root) {
      this.root = root;
   }

   @Nonnull
   public Path getCurrentDir() {
      return this.currentDir;
   }

   public void setCurrentDir(@Nonnull Path currentDir) {
      Path resolved = this.root.resolve(currentDir.toString());
      if (!PathUtil.isChildOf(this.root, resolved)) {
         throw new IllegalArgumentException("Invalid path");
      } else {
         this.currentDir = currentDir;
      }
   }

   @Nonnull
   public String getSearchQuery() {
      return this.searchQuery;
   }

   public void setSearchQuery(@Nonnull String searchQuery) {
      this.searchQuery = searchQuery;
   }

   public void navigateUp() {
      if (!this.currentDir.toString().isEmpty()) {
         Path parent = this.currentDir.getParent();
         this.currentDir = parent != null ? parent : Paths.get("");
      }
   }

   public void navigateTo(@Nonnull Path relativePath) {
      Path targetPath = this.root.resolve(this.currentDir.toString()).resolve(relativePath.toString());
      if (PathUtil.isChildOf(this.root, targetPath)) {
         if (Files.isDirectory(targetPath)) {
            this.currentDir = PathUtil.relativize(this.root, targetPath);
         }
      }
   }

   @Nonnull
   public Set<String> getSelectedItems() {
      return Collections.unmodifiableSet(this.selectedItems);
   }

   public void addSelection(@Nonnull String item) {
      if (this.config.enableMultiSelect()) {
         this.selectedItems.add(item);
      } else {
         this.selectedItems.clear();
         this.selectedItems.add(item);
      }
   }

   public void clearSelection() {
      this.selectedItems.clear();
   }

   @Nonnull
   public FileBrowserConfig getConfig() {
      return this.config;
   }

   @Nullable
   private Path findConfigRoot(@Nonnull String pathStr) {
      for (FileBrowserConfig.RootEntry rootEntry : this.config.roots()) {
         if (rootEntry.path().toString().equals(pathStr)) {
            return rootEntry.path();
         }
      }

      return null;
   }

   private record AssetPackSearchResult(@Nonnull String virtualPath, @Nonnull String displayName, int score, boolean isTerminal) {
   }
}
