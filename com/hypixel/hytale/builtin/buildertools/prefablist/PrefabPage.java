package com.hypixel.hytale.builtin.buildertools.prefablist;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.PrefabEditSessionManager;
import com.hypixel.hytale.builtin.buildertools.utils.PasteToolUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolPrefabPreview;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.EditorBlocksChange;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.core.asset.util.ColorParseUtil;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.prefab.PrefabStore;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.browser.FileBrowserConfig;
import com.hypixel.hytale.server.core.ui.browser.FileBrowserEventData;
import com.hypixel.hytale.server.core.ui.browser.FileListProvider;
import com.hypixel.hytale.server.core.ui.browser.ServerFileBrowser;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;

public class PrefabPage extends InteractiveCustomUIPage<FileBrowserEventData> {
   private static final String BASE_PACK_KEY = "HytaleAssets";
   @Nonnull
   private final ServerFileBrowser browser;
   @Nonnull
   private final BuilderToolsPlugin.BuilderState builderState;
   @Nonnull
   private final AssetPrefabFileProvider assetProvider;
   @Nonnull
   private Path assetsCurrentDir;
   @Nullable
   private Path previewedPath;
   @Nullable
   private String previewedFileName;
   private static final int PREVIEW_TILT = 23;
   private static final int PREVIEW_SPIN_SPEED = 27;
   private static final int PREVIEW_MAX_SIZE = 100;
   private static final int DEFAULT_BIOME_TINT = ColorParseUtil.colorToARGBInt(PrefabEditSessionManager.DEFAULT_TINT) & 16777215;
   private static final int DEFAULT_WATER_TINT = ColorParseUtil.colorToARGBInt(Environment.getUnknownFor("").getWaterTint()) & 16777215;

   public PrefabPage(@Nonnull PlayerRef playerRef, @Nonnull BuilderToolsPlugin.BuilderState builderState) {
      super(playerRef, CustomPageLifetime.CanDismiss, FileBrowserEventData.CODEC);
      this.builderState = builderState;
      this.assetProvider = new AssetPrefabFileProvider();
      boolean multiplePacks = PrefabStore.get().getAllBrowsablePrefabPaths().size() > 1;
      this.assetsCurrentDir = multiplePacks ? Paths.get("") : Paths.get("HytaleAssets");
      FileBrowserConfig config = FileBrowserConfig.builder()
         .listElementId("#FileList")
         .searchInputId("#SearchInput")
         .roots(List.of())
         .allowedExtensions(".prefab.json")
         .enableRootSelector(false)
         .enableSearch(true)
         .enableDirectoryNav(true)
         .maxResults(50)
         .build();
      String savedSearchQuery = builderState.getPrefabListSearchQuery();
      this.browser = new ServerFileBrowser(config, Paths.get("HytaleAssets"), null);
      if (savedSearchQuery != null && !savedSearchQuery.isEmpty()) {
         this.browser.setSearchQuery(savedSearchQuery);
      }
   }

   @Override
   public void build(
      @Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store
   ) {
      commandBuilder.append("Pages/PrefabListPage.ui");
      this.browser.buildSearchInput(commandBuilder, eventBuilder);
      this.buildPersistentBindings(eventBuilder);
      this.buildCurrentPath(commandBuilder);
      this.buildFileList(commandBuilder, eventBuilder);
      this.buildLoadButton(commandBuilder, eventBuilder);
   }

   private void buildPersistentBindings(@Nonnull UIEventBuilder eventBuilder) {
      eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#HomeButton", EventData.of("File", "~"));
   }

   public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull FileBrowserEventData data) {
      if (data.getSearchQuery() != null) {
         this.browser.handleEvent(data);
         this.sendListingUpdate();
      } else if (data.isBrowseRequested()) {
         Path targetPath = this.previewedPath;
         if (data.getPreview() != null) {
            targetPath = this.resolvePreviewPath(data.getPreview());
         }

         if (targetPath != null) {
            this.handlePrefabSelection(ref, store, targetPath, targetPath.getFileName().toString());
         }
      } else if (data.getPreview() != null) {
         this.handlePreviewRequest(data.getPreview());
      } else {
         String selectedPath = data.getSearchResult() != null ? data.getSearchResult() : data.getFile();
         if (selectedPath != null) {
            this.handleAssetsNavigation(ref, store, selectedPath, data.getSearchResult() != null);
         }
      }
   }

   private void handleAssetsNavigation(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull String selectedPath, boolean isSearchResult) {
      if ("~".equals(selectedPath)) {
         boolean multiplePacks = PrefabStore.get().getAllBrowsablePrefabPaths().size() > 1;
         this.assetsCurrentDir = multiplePacks ? Paths.get("") : Paths.get("HytaleAssets");
         this.sendListingUpdate();
      } else if ("..".equals(selectedPath)) {
         if (this.assetsCurrentDir.getNameCount() > 1) {
            this.assetsCurrentDir = this.assetsCurrentDir.getParent();
         } else if (this.assetsCurrentDir.getNameCount() == 1) {
            this.assetsCurrentDir = Paths.get("");
         }

         this.sendListingUpdate();
      } else {
         String targetVirtualPath;
         if (isSearchResult) {
            targetVirtualPath = selectedPath;
         } else {
            targetVirtualPath = this.assetsCurrentDir.toString().isEmpty()
               ? selectedPath
               : this.assetsCurrentDir.toString().replace('\\', '/') + "/" + selectedPath;
         }

         Path resolvedPath = this.assetProvider.resolveVirtualPath(targetVirtualPath);
         if (resolvedPath == null) {
            this.sendUpdate();
         } else {
            if (Files.isDirectory(resolvedPath)) {
               this.assetsCurrentDir = Paths.get(targetVirtualPath);
               this.sendListingUpdate();
            } else {
               this.handlePreviewRequest(selectedPath);
            }
         }
      }
   }

   private void buildLoadButton(@Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder) {
      boolean hasPreview = this.previewedPath != null;
      commandBuilder.set("#LoadButton.Visible", hasPreview);
      eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#LoadButton", EventData.of("Browse", "true"));
   }

   @Nullable
   private Path resolvePreviewPath(@Nonnull String selectedPath) {
      String virtualPath = selectedPath.contains("/") ? selectedPath : this.assetsCurrentDir.toString().replace('\\', '/') + "/" + selectedPath;
      Path resolved = this.assetProvider.resolveVirtualPath(virtualPath);
      return resolved != null && Files.isRegularFile(resolved) ? resolved : null;
   }

   private void handlePreviewRequest(@Nonnull String selectedPath) {
      Path resolvedFile = this.resolvePreviewPath(selectedPath);
      if (resolvedFile == null) {
         this.sendUpdate();
      } else if (resolvedFile.equals(this.previewedPath)) {
         this.sendUpdate();
      } else {
         this.previewedPath = resolvedFile;
         this.previewedFileName = selectedPath;
         BlockSelection selection = PrefabStore.get().getPrefab(resolvedFile);
         if (selection != null) {
            this.sendPreviewPacket(selection);
            UICommandBuilder commandBuilder = new UICommandBuilder();
            UIEventBuilder eventBuilder = new UIEventBuilder();
            this.buildPersistentBindings(eventBuilder);
            this.buildFileList(commandBuilder, eventBuilder);
            this.buildLoadButton(commandBuilder, eventBuilder);
            this.sendUpdate(commandBuilder, eventBuilder, false);
         }
      }
   }

   private void sendPreviewPacket(@Nullable BlockSelection selection) {
      BuilderToolPrefabPreview packet = new BuilderToolPrefabPreview();
      if (selection != null) {
         packet.tilt = 23;
         packet.spinSpeed = 27;
         packet.previewScale = 100;
         EditorBlocksChange editorPacket = selection.toPacket();
         packet.blocksChange = editorPacket.blocksChange;
         packet.fluidsChange = editorPacket.fluidsChange;
         packet.entityChanges = editorPacket.entityChanges;
         this.applyTintFromPlayerPosition(packet);
      }

      this.playerRef.getPacketHandler().write(packet);
   }

   private void applyTintFromPlayerPosition(BuilderToolPrefabPreview packet) {
      Ref<EntityStore> ref = this.playerRef.getReference();
      if (ref == null) {
         packet.biomeTint = DEFAULT_BIOME_TINT;
         packet.waterTint = DEFAULT_WATER_TINT;
      } else {
         Store<EntityStore> store = ref.getStore();
         World world = store.getExternalData().getWorld();
         Vector3d pos = this.playerRef.getTransform().getPosition();
         int x = MathUtil.floor(pos.x);
         int y = MathUtil.floor(pos.y);
         int z = MathUtil.floor(pos.z);
         long chunkIndex = ChunkUtil.indexChunkFromBlock(x, z);
         WorldChunk chunk = world.getNonTickingChunk(chunkIndex);
         if (chunk != null && chunk.getBlockChunk() != null) {
            BlockChunk blockChunk = chunk.getBlockChunk();
            packet.biomeTint = blockChunk.getTint(x, z);
            int envId = blockChunk.getEnvironment(x, y, z);
            Environment environment = Environment.getAssetMap().getAsset(envId);
            if (environment != null) {
               Color waterColor = environment.getWaterTint();
               if (waterColor != null) {
                  packet.waterTint = (waterColor.red & 255) << 16 | (waterColor.green & 255) << 8 | waterColor.blue & 255;
                  return;
               }
            }

            packet.waterTint = DEFAULT_WATER_TINT;
         } else {
            packet.biomeTint = DEFAULT_BIOME_TINT;
            packet.waterTint = DEFAULT_WATER_TINT;
         }
      }
   }

   private void clearPreview() {
      this.previewedPath = null;
      this.previewedFileName = null;
      this.sendPreviewPacket(null);
   }

   private void handlePrefabSelection(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull Path file, @Nonnull String displayPath) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      if (playerComponent.getGameMode() != GameMode.Creative) {
         playerComponent.getPageManager().setPage(ref, store, Page.None);
      } else {
         PlayerRef playerRefComponent = store.getComponent(ref, PlayerRef.getComponentType());

         assert playerRefComponent != null;

         this.clearPreview();
         playerComponent.getPageManager().setPage(ref, store, Page.None);
         BlockSelection prefab = PrefabStore.get().getPrefab(file);
         if (prefab != null) {
            BuilderToolsPlugin.addToQueue(playerComponent, playerRefComponent, (r, s, componentAccessor) -> s.load(displayPath, prefab, componentAccessor));
            PasteToolUtil.switchToPasteTool(ref, playerRefComponent, store);
         }
      }
   }

   private void sendListingUpdate() {
      UICommandBuilder commandBuilder = new UICommandBuilder();
      UIEventBuilder eventBuilder = new UIEventBuilder();
      this.buildPersistentBindings(eventBuilder);
      this.buildCurrentPath(commandBuilder);
      this.buildFileList(commandBuilder, eventBuilder);
      this.sendUpdate(commandBuilder, eventBuilder, false);
   }

   private void buildCurrentPath(@Nonnull UICommandBuilder commandBuilder) {
      String currentDirStr = this.assetsCurrentDir.toString().replace('\\', '/');
      String[] parts = currentDirStr.split("/", 2);
      String displayPath = parts.length > 1 ? parts[1] : "/";
      commandBuilder.set("#CurrentPath.Text", displayPath);
   }

   private void buildFileList(@Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder) {
      commandBuilder.clear("#FileList");
      List<FileListProvider.FileEntry> entries = this.assetProvider.getFiles(this.assetsCurrentDir, this.browser.getSearchQuery());
      int buttonIndex = 0;
      boolean canGoUp = this.assetsCurrentDir.getNameCount() >= 1 && !this.assetsCurrentDir.toString().isEmpty();
      if (canGoUp && this.browser.getSearchQuery().isEmpty()) {
         commandBuilder.append("#FileList", "Pages/BasicTextButton.ui");
         commandBuilder.set("#FileList[0].Text", "../");
         eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#FileList[0]", EventData.of("File", ".."));
         buttonIndex++;
      }

      for (FileListProvider.FileEntry entry : entries) {
         String displayText = entry.isDirectory() ? entry.displayName() + "/" : entry.displayName();
         String selector = "#FileList[" + buttonIndex + "]";
         commandBuilder.append("#FileList", "Pages/BasicTextButton.ui");
         commandBuilder.set(selector + ".Text", displayText);
         boolean isFile = !entry.isDirectory();
         if (isFile) {
            boolean isActive = entry.name().equals(this.previewedFileName);
            commandBuilder.set(selector + ".Style", Value.ref("Pages/BasicTextButton.ui", isActive ? "ActiveLabelStyle" : "SelectedLabelStyle"));
         }

         String fileEventKey = !this.browser.getSearchQuery().isEmpty() && isFile ? "SearchResult" : "File";
         if (isFile) {
            eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, selector, EventData.of("Preview", entry.name()), false);
            eventBuilder.addEventBinding(CustomUIEventBindingType.DoubleClicking, selector, EventData.of("Browse", "true").append("Preview", entry.name()));
         } else {
            eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, selector, EventData.of(fileEventKey, entry.name()));
         }

         buttonIndex++;
      }
   }

   @Override
   public void onDismiss(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
      this.clearPreview();
   }
}
