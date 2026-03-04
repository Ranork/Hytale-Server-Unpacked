package com.hypixel.hytale.builtin.asseteditor.assettypehandler;

import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.AssetUpdateQuery;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.asseteditor.AssetEditorPlugin;
import com.hypixel.hytale.builtin.asseteditor.AssetPath;
import com.hypixel.hytale.builtin.asseteditor.EditorClient;
import com.hypixel.hytale.builtin.asseteditor.util.AssetPathUtil;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.metadata.ui.UIRebuildCaches;
import com.hypixel.hytale.common.util.PathUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.logger.sentry.SkipSentryException;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorAssetType;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorEditorType;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorPopupNotificationType;
import com.hypixel.hytale.server.core.Message;
import java.nio.file.Path;
import java.util.Collections;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import org.bson.BsonDocument;

public class AssetStoreTypeHandler extends JsonTypeHandler {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   @Nonnull
   private final AssetStore assetStore;

   public AssetStoreTypeHandler(@Nonnull AssetStore assetStore) {
      super(
         new AssetEditorAssetType(
            assetStore.getAssetClass().getSimpleName(),
            null,
            false,
            PathUtil.toUnixPathString(AssetPathUtil.PATH_DIR_SERVER.resolve(assetStore.getPath())),
            assetStore.getExtension(),
            AssetEditorEditorType.JsonConfig
         )
      );
      this.assetStore = assetStore;
   }

   @Nonnull
   public AssetStore getAssetStore() {
      return this.assetStore;
   }

   @Nonnull
   @Override
   public AssetTypeHandler.AssetLoadResult loadAssetFromDocument(
      AssetPath path, Path dataPath, BsonDocument document, AssetUpdateQuery updateQuery, EditorClient editorClient
   ) {
      try {
         Object key = this.assetStore.decodeFilePathKey(path.path());
         JsonAssetWithMap decodedAsset = this.assetStore.decode(path.packId(), key, document.clone());
         this.assetStore.loadAssets(path.packId(), Collections.singletonList(decodedAsset), updateQuery, true);
      } catch (Exception var8) {
         ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(new SkipSentryException(var8))).log("Failed to load asset", path);
         if (editorClient != null) {
            editorClient.sendPopupNotification(
               AssetEditorPopupNotificationType.Error,
               Message.translation("server.assetEditor.messages.failedToDecodeAsset").param("message", var8.getMessage())
            );
         }

         return AssetTypeHandler.AssetLoadResult.ASSETS_UNCHANGED;
      }

      return AssetTypeHandler.AssetLoadResult.ASSETS_CHANGED;
   }

   @Nonnull
   @Override
   public AssetTypeHandler.AssetLoadResult unloadAsset(@Nonnull AssetPath path, @Nonnull AssetUpdateQuery updateQuery) {
      this.assetStore.removeAssets(path.packId(), true, Collections.singleton(this.assetStore.decodeFilePathKey(path.path())), updateQuery);
      return AssetTypeHandler.AssetLoadResult.ASSETS_CHANGED;
   }

   @Nonnull
   @Override
   public AssetTypeHandler.AssetLoadResult restoreOriginalAsset(@Nonnull AssetPath originalAssetPath, @Nonnull AssetUpdateQuery updateQuery) {
      try {
         this.assetStore.loadAssetsFromPaths(originalAssetPath.packId(), Collections.singletonList(originalAssetPath.path()), updateQuery, true);
      } catch (Exception var4) {
         ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(new SkipSentryException(var4))).log("Failed to restore asset", originalAssetPath);
         return AssetTypeHandler.AssetLoadResult.ASSETS_UNCHANGED;
      }

      return AssetTypeHandler.AssetLoadResult.ASSETS_CHANGED;
   }

   @Nonnull
   @Override
   public AssetUpdateQuery getDefaultUpdateQuery() {
      if (this.cachedDefaultUpdateQuery == null) {
         Schema schema = AssetEditorPlugin.get().getSchema(this.config.id + ".json");
         if (schema == null) {
            return AssetUpdateQuery.DEFAULT;
         }

         AssetUpdateQuery.RebuildCacheBuilder rebuildCacheBuilder = AssetUpdateQuery.RebuildCache.builder();
         if (schema.getHytale().getUiRebuildCaches() != null) {
            for (UIRebuildCaches.ClientCache cache : schema.getHytale().getUiRebuildCaches()) {
               switch (cache) {
                  case MODELS:
                     rebuildCacheBuilder.setModels(true);
                     break;
                  case MODEL_TEXTURES:
                     rebuildCacheBuilder.setModelTextures(true);
                     break;
                  case ITEM_ICONS:
                     rebuildCacheBuilder.setItemIcons(true);
                     break;
                  case BLOCK_TEXTURES:
                     rebuildCacheBuilder.setBlockTextures(true);
                     break;
                  case MAP_GEOMETRY:
                     rebuildCacheBuilder.setMapGeometry(true);
               }
            }
         }

         this.cachedDefaultUpdateQuery = new AssetUpdateQuery(rebuildCacheBuilder.build());
      }

      return this.cachedDefaultUpdateQuery;
   }
}
