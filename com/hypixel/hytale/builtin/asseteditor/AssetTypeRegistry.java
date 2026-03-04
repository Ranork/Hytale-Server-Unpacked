package com.hypixel.hytale.builtin.asseteditor;

import com.hypixel.hytale.builtin.asseteditor.assettypehandler.AssetTypeHandler;
import com.hypixel.hytale.common.util.PathUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorAssetType;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorPopupNotificationType;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorSetupAssetTypes;
import com.hypixel.hytale.server.core.Message;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AssetTypeRegistry {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private final ConcurrentHashMap<String, AssetTypeHandler> assetTypeHandlers = new ConcurrentHashMap<>();
   private AssetEditorSetupAssetTypes setupPacket;

   @Nonnull
   public Map<String, AssetTypeHandler> getRegisteredAssetTypeHandlers() {
      return this.assetTypeHandlers;
   }

   public void registerAssetType(@Nonnull AssetTypeHandler assetType) {
      if (this.assetTypeHandlers.putIfAbsent(assetType.getConfig().id, assetType) != null) {
         throw new IllegalArgumentException("An asset type with id '" + assetType.getConfig().id + "' is already registered");
      }
   }

   public void unregisterAssetType(@Nonnull AssetTypeHandler assetType) {
      this.assetTypeHandlers.remove(assetType.getConfig().id);
   }

   public AssetTypeHandler getAssetTypeHandler(String id) {
      return this.assetTypeHandlers.get(id);
   }

   @Nullable
   public AssetTypeHandler getAssetTypeHandlerForPath(@Nonnull Path path) {
      String extension = PathUtil.getFileExtension(path);
      if (extension.isEmpty()) {
         return null;
      } else {
         for (AssetTypeHandler handler : this.assetTypeHandlers.values()) {
            if (handler.getConfig().fileExtension.equalsIgnoreCase(extension) && path.startsWith(handler.getConfig().path)) {
               return handler;
            }
         }

         return null;
      }
   }

   public boolean isPathInAssetTypeFolder(@Nonnull Path path) {
      for (AssetTypeHandler assetTypeHandler : this.assetTypeHandlers.values()) {
         if (path.startsWith(assetTypeHandler.getRootPath()) && !path.equals(assetTypeHandler.getRootPath())) {
            return true;
         }
      }

      return false;
   }

   @Nullable
   public AssetTypeHandler tryGetAssetTypeHandler(@Nonnull Path assetPath, @Nonnull EditorClient editorClient, int requestToken) {
      AssetTypeHandler assetTypeHandler = this.getAssetTypeHandlerForPath(assetPath);
      if (assetTypeHandler == null) {
         LOGGER.at(Level.WARNING).log("Invalid asset type for %s", assetPath);
         if (requestToken != -1) {
            editorClient.sendFailureReply(requestToken, Message.translation("server.assetEditor.messages.invalidAssetType"));
         } else {
            editorClient.sendPopupNotification(AssetEditorPopupNotificationType.Error, Message.translation("server.assetEditor.messages.invalidAssetType"));
         }

         return null;
      } else {
         return assetTypeHandler;
      }
   }

   public void sendPacket(@Nonnull EditorClient editorClient) {
      editorClient.getPacketHandler().write(this.setupPacket);
   }

   public void setupPacket() {
      List<AssetEditorAssetType> types = new ObjectArrayList();

      for (AssetTypeHandler assetTypeHandler : this.assetTypeHandlers.values()) {
         types.add(assetTypeHandler.getConfig());
      }

      this.setupPacket = new AssetEditorSetupAssetTypes(types.toArray(AssetEditorAssetType[]::new));
   }
}
