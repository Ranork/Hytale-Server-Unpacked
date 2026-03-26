package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.ui;

import com.hypixel.hytale.builtin.buildertools.PrototypePlayerBuilderToolSettings;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.ScriptedBrushAsset;
import com.hypixel.hytale.builtin.buildertools.tooloperations.ToolOperation;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.ui.browser.FileBrowserConfig;
import com.hypixel.hytale.server.core.ui.browser.FileBrowserEventData;
import com.hypixel.hytale.server.core.ui.browser.ServerFileBrowser;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import javax.annotation.Nonnull;

public class ScriptedBrushPage extends InteractiveCustomUIPage<FileBrowserEventData> {
   @Nonnull
   private final ServerFileBrowser browser;

   public ScriptedBrushPage(@Nonnull PlayerRef playerRef) {
      super(playerRef, CustomPageLifetime.CanDismiss, FileBrowserEventData.CODEC);
      FileBrowserConfig config = FileBrowserConfig.builder()
         .listElementId("#FileList")
         .searchInputId("#SearchInput")
         .currentPathId("#CurrentPath")
         .enableRootSelector(false)
         .enableSearch(true)
         .enableDirectoryNav(true)
         .assetPackMode(true, "Server/ScriptedBrushes")
         .allowedExtensions(".json")
         .maxResults(50)
         .build();
      this.browser = new ServerFileBrowser(config);
   }

   @Override
   public void build(
      @Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store
   ) {
      commandBuilder.append("Pages/ScriptedBrushListPage.ui");
      this.browser.buildUI(commandBuilder, eventBuilder);
   }

   public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull FileBrowserEventData data) {
      if (this.browser.handleEvent(data)) {
         UICommandBuilder commandBuilder = new UICommandBuilder();
         UIEventBuilder eventBuilder = new UIEventBuilder();
         this.browser.buildUI(commandBuilder, eventBuilder);
         this.sendUpdate(commandBuilder, eventBuilder, false);
      } else {
         String selectedPath = data.getSearchResult() != null ? data.getSearchResult() : data.getFile();
         if (selectedPath != null) {
            this.handleBrushSelection(ref, store, selectedPath, data.getSearchResult() != null);
         }
      }
   }

   private void handleBrushSelection(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, String selectedPath, boolean isSearchResult) {
      String virtualPath;
      if (isSearchResult) {
         virtualPath = selectedPath;
      } else {
         String currentPath = this.browser.getAssetPackCurrentPath();
         virtualPath = currentPath.isEmpty() ? selectedPath : currentPath + "/" + selectedPath;
      }

      Path resolvedPath = this.browser.resolveAssetPackPath(virtualPath);
      if (resolvedPath != null && !Files.isDirectory(resolvedPath)) {
         String fileName = resolvedPath.getFileName().toString();
         String brushId = fileName.endsWith(".json") ? fileName.substring(0, fileName.length() - ".json".length()) : fileName;
         ScriptedBrushAsset scriptedBrushAsset = ScriptedBrushAsset.get(brushId);
         Player playerComponent = store.getComponent(ref, Player.getComponentType());

         assert playerComponent != null;

         PlayerRef playerRefComponent = store.getComponent(ref, PlayerRef.getComponentType());

         assert playerRefComponent != null;

         if (scriptedBrushAsset == null) {
            playerRefComponent.sendMessage(Message.translation("server.commands.brushConfig.load.error.notFound").param("name", brushId));
            this.sendUpdate();
         } else {
            UUID playerUUID = playerRefComponent.getUuid();
            PrototypePlayerBuilderToolSettings prototypeSettings = ToolOperation.getOrCreatePrototypeSettings(playerUUID);
            BrushConfigCommandExecutor brushConfigCommandExecutor = prototypeSettings.getBrushConfigCommandExecutor();

            try {
               scriptedBrushAsset.loadIntoExecutor(brushConfigCommandExecutor);
               Inventory inventory = playerComponent.getInventory();
               ItemContainer hotbar = inventory.getHotbar();
               String editorToolItemId = ScriptedBrushAsset.getEditorToolItemId(brushId);
               if (editorToolItemId == null) {
                  editorToolItemId = "EditorTool_ScriptedBrushTemplate";
               }

               hotbar.setItemStackForSlot(inventory.getActiveHotbarSlot(), new ItemStack(editorToolItemId));
               prototypeSettings.setPrototypeItemId(editorToolItemId);
               prototypeSettings.setCurrentlyLoadedBrushConfigName(scriptedBrushAsset.getId());
               prototypeSettings.setUsePrototypeBrushConfigurations(true);
               playerComponent.getPageManager().setPage(ref, store, Page.None);
               playerRefComponent.sendMessage(Message.translation("server.commands.brushConfig.loaded").param("name", scriptedBrushAsset.getId()));
            } catch (Exception var18) {
               playerRefComponent.sendMessage(
                  Message.translation("server.commands.brushConfig.load.error.loadFailed")
                     .param("name", brushId)
                     .param("error", var18.getMessage() != null ? var18.getMessage() : "Unknown error")
               );
               this.sendUpdate();
            }
         }
      } else {
         this.sendUpdate();
      }
   }
}
