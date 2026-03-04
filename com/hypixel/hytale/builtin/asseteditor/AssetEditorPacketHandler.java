package com.hypixel.hytale.builtin.asseteditor;

import com.hypixel.hytale.builtin.asseteditor.event.AssetEditorActivateButtonEvent;
import com.hypixel.hytale.builtin.asseteditor.event.AssetEditorFetchAutoCompleteDataEvent;
import com.hypixel.hytale.builtin.asseteditor.event.AssetEditorRequestDataSetEvent;
import com.hypixel.hytale.builtin.asseteditor.event.AssetEditorUpdateWeatherPreviewLockEvent;
import com.hypixel.hytale.common.util.CompletableFutureUtil;
import com.hypixel.hytale.event.IEventDispatcher;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.HostAddress;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorActivateButton;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorCreateAsset;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorCreateAssetPack;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorCreateDirectory;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorDeleteAsset;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorDeleteAssetPack;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorDeleteDirectory;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorExportAssets;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorFetchAsset;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorFetchAutoCompleteData;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorFetchAutoCompleteDataReply;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorFetchJsonAssetWithParents;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorFetchLastModifiedAssets;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorPopupNotificationType;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorRedoChanges;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorRenameAsset;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorRenameDirectory;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorRequestChildrenList;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorRequestDataset;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorRequestDatasetReply;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorSelectAsset;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorSetGameTime;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorSubscribeModifiedAssetsChanges;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorUndoChanges;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorUpdateAsset;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorUpdateAssetPack;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorUpdateJsonAsset;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorUpdateWeatherPreviewLock;
import com.hypixel.hytale.protocol.packets.connection.Disconnect;
import com.hypixel.hytale.protocol.packets.connection.DisconnectType;
import com.hypixel.hytale.protocol.packets.connection.Pong;
import com.hypixel.hytale.protocol.packets.interface_.UpdateLanguage;
import com.hypixel.hytale.protocol.packets.world.UpdateEditorTimeOverride;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.auth.PlayerAuthentication;
import com.hypixel.hytale.server.core.io.ProtocolVersion;
import com.hypixel.hytale.server.core.io.handlers.GenericPacketHandler;
import com.hypixel.hytale.server.core.io.netty.NettyUtil;
import com.hypixel.hytale.server.core.modules.i18n.I18nModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class AssetEditorPacketHandler extends GenericPacketHandler {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   @Nonnull
   private final EditorClient editorClient;

   public AssetEditorPacketHandler(@Nonnull Channel channel, @Nonnull ProtocolVersion protocolVersion, String language, @Nonnull PlayerAuthentication auth) {
      super(channel, protocolVersion);
      this.auth = auth;
      this.editorClient = new EditorClient(language, auth, this);
      this.init();
   }

   public AssetEditorPacketHandler(@Nonnull Channel channel, @Nonnull ProtocolVersion protocolVersion, String language, UUID uuid, String username) {
      this(channel, protocolVersion, language, uuid, username, null, null);
   }

   public AssetEditorPacketHandler(
      @Nonnull Channel channel,
      @Nonnull ProtocolVersion protocolVersion,
      String language,
      UUID uuid,
      String username,
      byte[] referralData,
      HostAddress referralSource
   ) {
      super(channel, protocolVersion);
      this.auth = null;
      this.editorClient = new EditorClient(language, uuid, username, this);
      this.init();
   }

   private void init() {
      this.registerHandlers();
      AssetEditorPlugin.get().handleInitializeClient(this.editorClient);
   }

   @Nonnull
   public EditorClient getEditorClient() {
      return this.editorClient;
   }

   @Nonnull
   @Override
   public String getIdentifier() {
      return "{Editor(" + NettyUtil.formatRemoteAddress(this.getChannel()) + "), " + this.editorClient.getUuid() + ", " + this.editorClient.getUsername() + "}";
   }

   @Override
   public void closed(ChannelHandlerContext ctx) {
      AssetEditorPlugin.get().handleEditorClientDisconnected(this.editorClient, this.disconnectReason);
   }

   public void registerHandlers() {
      this.registerHandler(1, p -> this.handle((Disconnect)p));
      this.registerHandler(3, p -> this.handlePong((Pong)p));
      this.registerHandler(321, p -> this.handle((AssetEditorRequestChildrenList)p));
      this.registerHandler(324, p -> this.handle((AssetEditorUpdateAsset)p));
      this.registerHandler(323, p -> this.handle((AssetEditorUpdateJsonAsset)p));
      this.registerHandler(336, p -> this.handle((AssetEditorSelectAsset)p));
      this.registerHandler(310, p -> this.handle((AssetEditorFetchAsset)p));
      this.registerHandler(311, p -> this.handle((AssetEditorFetchJsonAssetWithParents)p));
      this.registerHandler(327, p -> this.handle((AssetEditorCreateAsset)p));
      this.registerHandler(307, p -> this.handle((AssetEditorCreateDirectory)p));
      this.registerHandler(333, p -> this.handle((AssetEditorRequestDataset)p));
      this.registerHandler(331, p -> this.handle((AssetEditorFetchAutoCompleteData)p));
      this.registerHandler(335, p -> this.handle((AssetEditorActivateButton)p));
      this.registerHandler(329, p -> this.handle((AssetEditorDeleteAsset)p));
      this.registerHandler(328, p -> this.handle((AssetEditorRenameAsset)p));
      this.registerHandler(308, p -> this.handle((AssetEditorDeleteDirectory)p));
      this.registerHandler(309, p -> this.handle((AssetEditorRenameDirectory)p));
      this.registerHandler(342, p -> this.handle((AssetEditorExportAssets)p));
      this.registerHandler(338, p -> this.handle((AssetEditorFetchLastModifiedAssets)p));
      this.registerHandler(349, p -> this.handle((AssetEditorUndoChanges)p));
      this.registerHandler(350, p -> this.handle((AssetEditorRedoChanges)p));
      this.registerHandler(341, p -> this.handle((AssetEditorSubscribeModifiedAssetsChanges)p));
      this.registerHandler(352, p -> this.handle((AssetEditorSetGameTime)p));
      this.registerHandler(354, p -> this.handle((AssetEditorUpdateWeatherPreviewLock)p));
      this.registerHandler(316, p -> this.handle((AssetEditorCreateAssetPack)p));
      this.registerHandler(315, p -> this.handle((AssetEditorUpdateAssetPack)p));
      this.registerHandler(317, p -> this.handle((AssetEditorDeleteAssetPack)p));
      this.registerHandler(232, p -> this.handle((UpdateLanguage)p));
   }

   public void handle(@Nonnull AssetEditorSubscribeModifiedAssetsChanges packet) {
      if (!this.lacksPermission()) {
         if (packet.subscribe) {
            AssetEditorPlugin.get().handleSubscribeToModifiedAssetsChanges(this.editorClient);
         } else {
            AssetEditorPlugin.get().handleUnsubscribeFromModifiedAssetsChanges(this.editorClient);
         }
      }
   }

   public void handle(@Nonnull AssetEditorUndoChanges packet) {
      if (!this.lacksPermission(packet.token)) {
         LOGGER.at(Level.INFO).log("%s undoing last change", this.editorClient.getUsername());
         AssetEditorPlugin.get().handleUndo(this.editorClient, new AssetPath(packet.path.pack, Path.of(packet.path.path)), packet.token);
      }
   }

   public void handle(@Nonnull AssetEditorRedoChanges packet) {
      if (!this.lacksPermission(packet.token)) {
         LOGGER.at(Level.INFO).log("%s redoing last change", this.editorClient.getUsername());
         AssetEditorPlugin.get().handleRedo(this.editorClient, new AssetPath(packet.path.pack, Path.of(packet.path.path)), packet.token);
      }
   }

   public void handle(AssetEditorFetchLastModifiedAssets packet) {
      if (!this.lacksPermission()) {
         AssetEditorPlugin.get().handleFetchLastModifiedAssets(this.editorClient);
      }
   }

   public void handle(@Nonnull AssetEditorExportAssets packet) {
      if (!this.lacksPermission()) {
         StringBuilder assets = new StringBuilder();

         for (com.hypixel.hytale.protocol.packets.asseteditor.AssetPath assetPath : packet.paths) {
            if (!assets.isEmpty()) {
               assets.append(", ");
            }

            assets.append(assetPath.toString());
         }

         LOGGER.at(Level.INFO).log("%s is exporting: %s", this.editorClient.getUsername(), assets.toString());
         List<AssetPath> paths = new ObjectArrayList();

         for (com.hypixel.hytale.protocol.packets.asseteditor.AssetPath assetPath : packet.paths) {
            paths.add(new AssetPath(assetPath.pack, Path.of(assetPath.path)));
         }

         AssetEditorPlugin.get().handleExportAssets(this.editorClient, paths);
      }
   }

   public void handle(@Nonnull AssetEditorCreateAsset packet) {
      if (!this.lacksPermission(packet.token)) {
         LOGGER.at(Level.INFO).log("%s is creating asset %s", this.editorClient.getUsername(), packet.path);
         AssetEditorPlugin.get()
            .handleCreateAsset(
               this.editorClient, new AssetPath(packet.path.pack, Path.of(packet.path.path)), packet.data, packet.rebuildCaches, packet.buttonId, packet.token
            );
      }
   }

   public void handle(@Nonnull AssetEditorFetchAsset packet) {
      if (!this.lacksPermission(packet.token)) {
         LOGGER.at(Level.INFO).log("%s is fetching asset %s, from opened tab: %s", this.editorClient.getUsername(), packet.path, packet.isFromOpenedTab);
         AssetEditorPlugin.get().handleFetchAsset(this.editorClient, new AssetPath(packet.path.pack, Path.of(packet.path.path)), packet.token);
      }
   }

   public void handle(@Nonnull AssetEditorFetchJsonAssetWithParents packet) {
      if (!this.lacksPermission(packet.token)) {
         LOGGER.at(Level.INFO).log("%s is fetching json asset %s, from opened tab: %s", this.editorClient.getUsername(), packet.path, packet.isFromOpenedTab);
         AssetEditorPlugin.get()
            .handleFetchJsonAssetWithParents(
               this.editorClient, new AssetPath(packet.path.pack, Path.of(packet.path.path)), packet.isFromOpenedTab, packet.token
            );
      }
   }

   public void handle(@Nonnull AssetEditorRequestChildrenList packet) {
      if (!this.lacksPermission()) {
         LOGGER.at(Level.INFO).log("%s is requesting child ids for %s", this.editorClient.getUsername(), packet.path);
         AssetEditorPlugin.get().handleRequestChildIds(this.editorClient, new AssetPath(packet.path.pack, Path.of(packet.path.path)));
      }
   }

   public void handle(@Nonnull AssetEditorUpdateAsset packet) {
      if (!this.lacksPermission(packet.token)) {
         LOGGER.at(Level.INFO).log("%s updating asset at %s", this.editorClient.getUsername(), packet.path);
         AssetEditorPlugin.get().handleAssetUpdate(this.editorClient, new AssetPath(packet.path.pack, Path.of(packet.path.path)), packet.data, packet.token);
      }
   }

   public void handle(@Nonnull AssetEditorUpdateJsonAsset packet) {
      if (!this.lacksPermission(packet.token)) {
         LOGGER.at(Level.INFO).log("%s updating json asset at %s", this.editorClient.getUsername(), packet.path);
         AssetEditorPlugin.get()
            .handleJsonAssetUpdate(
               this.editorClient,
               new AssetPath(packet.path.pack, Path.of(packet.path.path)),
               packet.assetType,
               packet.assetIndex,
               packet.commands,
               packet.token
            );
      }
   }

   public void handle(@Nonnull AssetEditorFetchAutoCompleteData packet) {
      if (!this.lacksPermission(packet.token)) {
         CompletableFutureUtil._catch(
            HytaleServer.get()
               .getEventBus()
               .dispatchForAsync(AssetEditorFetchAutoCompleteDataEvent.class, packet.dataset)
               .dispatch(new AssetEditorFetchAutoCompleteDataEvent(this.editorClient, packet.dataset, packet.query))
               .thenAccept(event -> {
                  if (event.getResults() == null) {
                     HytaleLogger.getLogger().at(Level.WARNING).log("Tried to request unknown autocomplete dataset for asset editor: %s", packet.dataset);
                  } else {
                     this.editorClient.getPacketHandler().write(new AssetEditorFetchAutoCompleteDataReply(packet.token, event.getResults()));
                  }
               })
         );
      }
   }

   public void handle(@Nonnull AssetEditorRenameAsset packet) {
      if (!this.lacksPermission(packet.token)) {
         LOGGER.at(Level.WARNING).log("%s is renaming %s to %s", this.editorClient.getUsername(), packet.path, packet.newPath);
         AssetEditorPlugin.get().handleRenameAsset(this.editorClient, new AssetPath(packet.path), new AssetPath(packet.newPath), packet.token);
      }
   }

   public void handle(@Nonnull AssetEditorDeleteAsset packet) {
      if (!this.lacksPermission(packet.token)) {
         LOGGER.at(Level.INFO).log("%s is deleting asset %s", this.editorClient.getUsername(), packet.path);
         AssetEditorPlugin.get().handleDeleteAsset(this.editorClient, new AssetPath(packet.path), packet.token);
      }
   }

   public void handle(@Nonnull AssetEditorActivateButton packet) {
      if (!this.lacksPermission()) {
         AssetEditorPlugin.get().getLogger().at(Level.INFO).log("%s is activating button %s", this.editorClient.getUsername(), packet.buttonId);
         IEventDispatcher<AssetEditorActivateButtonEvent, AssetEditorActivateButtonEvent> dispatch = HytaleServer.get()
            .getEventBus()
            .dispatchFor(AssetEditorActivateButtonEvent.class, packet.buttonId);
         if (dispatch.hasListener()) {
            dispatch.dispatch(new AssetEditorActivateButtonEvent(this.editorClient, packet.buttonId));
         }
      }
   }

   public void handle(@Nonnull AssetEditorRequestDataset packet) {
      if (!this.lacksPermission()) {
         CompletableFutureUtil._catch(
            HytaleServer.get()
               .getEventBus()
               .dispatchForAsync(AssetEditorRequestDataSetEvent.class, packet.name)
               .dispatch(new AssetEditorRequestDataSetEvent(this.editorClient, packet.name, null))
               .thenAccept(event -> {
                  if (event.getResults() == null) {
                     HytaleLogger.getLogger().at(Level.WARNING).log("Tried to request unknown dataset list for asset editor: %s", packet.name);
                  } else {
                     this.editorClient.getPacketHandler().write(new AssetEditorRequestDatasetReply(packet.name, event.getResults()));
                  }
               })
         );
      }
   }

   public void handle(@Nonnull AssetEditorSelectAsset packet) {
      if (!this.lacksPermission()) {
         LOGGER.at(Level.INFO).log("%s selecting %s", this.editorClient.getUsername(), packet.path);
         AssetEditorPlugin.get().handleSelectAsset(this.editorClient, packet.path != null ? new AssetPath(packet.path) : null);
      }
   }

   public void handle(@Nonnull AssetEditorCreateDirectory packet) {
      if (!this.lacksPermission(packet.token)) {
         LOGGER.at(Level.INFO).log("%s is creating directory %s", this.editorClient.getUsername(), packet.path);
         AssetEditorPlugin.get().handleCreateDirectory(this.editorClient, new AssetPath(packet.path), packet.token);
      }
   }

   public void handle(@Nonnull AssetEditorDeleteDirectory packet) {
      if (!this.lacksPermission(packet.token)) {
         LOGGER.at(Level.INFO).log("%s is deleting directory %s", this.editorClient.getUsername(), packet.path);
         AssetEditorPlugin.get().handleDeleteDirectory(this.editorClient, new AssetPath(packet.path), packet.token);
      }
   }

   public void handle(@Nonnull AssetEditorRenameDirectory packet) {
      if (!this.lacksPermission(packet.token)) {
         LOGGER.at(Level.INFO).log("%s is renaming directory %s to $s", this.editorClient.getUsername(), packet.path, packet.newPath);
         AssetEditorPlugin.get().handleRenameDirectory(this.editorClient, new AssetPath(packet.path), new AssetPath(packet.newPath), packet.token);
      }
   }

   public void handle(@Nonnull UpdateLanguage packet) {
      if (!this.lacksPermission()) {
         this.editorClient.setLanguage(packet.language);
         I18nModule.get().sendTranslations(this.editorClient.getPacketHandler(), this.editorClient.getLanguage());
      }
   }

   public void handle(@Nonnull AssetEditorSetGameTime packet) {
      if (!this.lacksPermission()) {
         PlayerRef player = this.editorClient.tryGetPlayer();
         if (player != null) {
            player.getPacketHandler().write(new UpdateEditorTimeOverride(packet.gameTime, packet.paused));
         }
      }
   }

   public void handle(@Nonnull AssetEditorUpdateWeatherPreviewLock packet) {
      if (!this.lacksPermission()) {
         IEventDispatcher<AssetEditorUpdateWeatherPreviewLockEvent, AssetEditorUpdateWeatherPreviewLockEvent> dispatch = HytaleServer.get()
            .getEventBus()
            .dispatchFor(AssetEditorUpdateWeatherPreviewLockEvent.class);
         if (dispatch.hasListener()) {
            dispatch.dispatch(new AssetEditorUpdateWeatherPreviewLockEvent(this.editorClient, packet.locked));
         }
      }
   }

   public void handle(@Nonnull AssetEditorUpdateAssetPack packet) {
      if (!this.lacksPermission("hytale.editor.packs.edit")) {
         LOGGER.at(Level.INFO).log("%s is updating the asset pack manifest for %s", this.editorClient.getUsername(), packet.id);
         AssetEditorPlugin.get().handleUpdateAssetPack(this.editorClient, packet.id, packet.manifest);
      }
   }

   public void handle(@Nonnull AssetEditorDeleteAssetPack packet) {
      if (!this.lacksPermission("hytale.editor.packs.delete")) {
         LOGGER.at(Level.INFO).log("%s is deleting the asset pack %s", this.editorClient.getUsername(), packet.id);
         AssetEditorPlugin.get().handleDeleteAssetPack(this.editorClient, packet.id);
      }
   }

   public void handle(@Nonnull AssetEditorCreateAssetPack packet) {
      if (!this.lacksPermission(packet.token, "hytale.editor.packs.create")) {
         LOGGER.at(Level.INFO).log("%s is creating a new asset pack: %s:%s", this.editorClient.getUsername(), packet.manifest.group, packet.manifest.name);
         AssetEditorPlugin.get().handleCreateAssetPack(this.editorClient, packet.manifest, packet.token);
      }
   }

   public void handle(@Nonnull Disconnect packet) {
      switch (packet.type) {
         case Disconnect:
            this.disconnectReason.setClientDisconnectType(DisconnectType.Disconnect);
            break;
         case Crash:
            this.disconnectReason.setClientDisconnectType(DisconnectType.Crash);
      }

      LOGGER.at(Level.INFO)
         .log(
            "%s - %s at %s left with reason: %s - %s",
            this.editorClient.getUuid(),
            this.editorClient.getUsername(),
            NettyUtil.formatRemoteAddress(this.getChannel()),
            packet.type.name(),
            packet.reason
         );
      this.getChannel().close();
   }

   private boolean lacksPermission(int token) {
      if (!this.editorClient.hasPermission("hytale.editor.asset")) {
         this.editorClient.sendFailureReply(token, Messages.USAGE_DENIED);
         return true;
      } else {
         return false;
      }
   }

   private boolean lacksPermission() {
      return this.lacksPermission("hytale.editor.asset");
   }

   private boolean lacksPermission(String permissionId) {
      if (!this.editorClient.hasPermission(permissionId)) {
         this.editorClient.sendPopupNotification(AssetEditorPopupNotificationType.Error, Messages.USAGE_DENIED);
         return true;
      } else {
         return false;
      }
   }

   private boolean lacksPermission(int token, String permissionId) {
      if (!this.editorClient.hasPermission(permissionId)) {
         this.editorClient.sendFailureReply(token, Messages.USAGE_DENIED);
         return true;
      } else {
         return false;
      }
   }
}
