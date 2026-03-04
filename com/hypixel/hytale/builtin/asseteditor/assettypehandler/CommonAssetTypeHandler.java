package com.hypixel.hytale.builtin.asseteditor.assettypehandler;

import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.assetstore.AssetUpdateQuery;
import com.hypixel.hytale.builtin.asseteditor.AssetPath;
import com.hypixel.hytale.builtin.asseteditor.EditorClient;
import com.hypixel.hytale.builtin.asseteditor.util.AssetPathUtil;
import com.hypixel.hytale.common.util.PathUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorAssetType;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorEditorType;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.asset.common.CommonAssetModule;
import com.hypixel.hytale.server.core.asset.common.CommonAssetRegistry;
import com.hypixel.hytale.server.core.asset.common.asset.FileCommonAsset;
import com.hypixel.hytale.server.core.universe.Universe;
import it.unimi.dsi.fastutil.booleans.BooleanObjectPair;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CommonAssetTypeHandler extends AssetTypeHandler {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

   public CommonAssetTypeHandler(String id, @Nullable String icon, String fileExtension, AssetEditorEditorType editorType) {
      super(new AssetEditorAssetType(id, icon, true, "Common", fileExtension, editorType));
   }

   @Nonnull
   @Override
   public AssetTypeHandler.AssetLoadResult loadAsset(AssetPath path, Path dataPath, byte[] data, AssetUpdateQuery updateQuery, EditorClient editorClient) {
      String relativePath = PathUtil.toUnixPathString(AssetPathUtil.PATH_DIR_COMMON.relativize(path.path()));
      FileCommonAsset newAsset = new FileCommonAsset(dataPath, relativePath, data);
      CommonAssetRegistry.AddCommonAssetResult result = CommonAssetRegistry.addCommonAsset(path.packId(), newAsset);
      CommonAssetRegistry.PackAsset asset = result.getNewPackAsset();
      CommonAssetRegistry.PackAsset oldAsset = result.getPreviousNameAsset();
      return oldAsset != null && oldAsset.asset().getHash().equals(asset.asset().getHash())
         ? AssetTypeHandler.AssetLoadResult.ASSETS_UNCHANGED
         : AssetTypeHandler.AssetLoadResult.COMMON_ASSETS_CHANGED;
   }

   @Nonnull
   @Override
   public AssetTypeHandler.AssetLoadResult unloadAsset(@Nonnull AssetPath path, @Nonnull AssetUpdateQuery updateQuery) {
      BooleanObjectPair<CommonAssetRegistry.PackAsset> removedCommonAsset = CommonAssetRegistry.removeCommonAssetByName(
         path.packId(), PathUtil.toUnixPathString(AssetPathUtil.PATH_DIR_COMMON.relativize(path.path()))
      );
      if (removedCommonAsset != null) {
         if (Universe.get().getPlayerCount() > 0) {
            if (removedCommonAsset.firstBoolean()) {
               CommonAssetModule.get()
                  .sendAsset(((CommonAssetRegistry.PackAsset)removedCommonAsset.second()).asset(), updateQuery.getRebuildCache().isCommonAssetsRebuild());
            } else {
               CommonAssetModule.get()
                  .sendRemoveAssets(
                     Collections.singletonList((CommonAssetRegistry.PackAsset)removedCommonAsset.second()),
                     updateQuery.getRebuildCache().isCommonAssetsRebuild()
                  );
            }
         }

         return AssetTypeHandler.AssetLoadResult.COMMON_ASSETS_CHANGED;
      } else {
         return AssetTypeHandler.AssetLoadResult.ASSETS_UNCHANGED;
      }
   }

   @Nonnull
   @Override
   public AssetTypeHandler.AssetLoadResult restoreOriginalAsset(@Nonnull AssetPath originalAssetPath, AssetUpdateQuery updateQuery) {
      AssetPack pack = AssetModule.get().getAssetPack(originalAssetPath.packId());
      Path absolutePath = pack.getRoot().resolve(originalAssetPath.path()).toAbsolutePath();
      byte[] bytes = null;

      try {
         bytes = Files.readAllBytes(absolutePath);
      } catch (IOException var11) {
         ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var11)).log("Failed to load file %s", absolutePath);
      }

      if (bytes == null) {
         return AssetTypeHandler.AssetLoadResult.ASSETS_UNCHANGED;
      } else {
         String relativePath = PathUtil.toUnixPathString(AssetPathUtil.PATH_DIR_COMMON.relativize(originalAssetPath.path()));
         FileCommonAsset commonAsset = new FileCommonAsset(absolutePath, relativePath, bytes);
         CommonAssetRegistry.AddCommonAssetResult result = CommonAssetRegistry.addCommonAsset(originalAssetPath.packId(), commonAsset);
         CommonAssetRegistry.PackAsset oldAsset = result.getPreviousNameAsset();
         CommonAssetRegistry.PackAsset newAsset = result.getNewPackAsset();
         return oldAsset != null && oldAsset.asset().getHash().equals(newAsset.asset().getHash())
            ? AssetTypeHandler.AssetLoadResult.ASSETS_UNCHANGED
            : AssetTypeHandler.AssetLoadResult.COMMON_ASSETS_CHANGED;
      }
   }

   @Nonnull
   @Override
   public AssetUpdateQuery getDefaultUpdateQuery() {
      if (this.cachedDefaultUpdateQuery == null) {
         this.cachedDefaultUpdateQuery = new AssetUpdateQuery(new AssetUpdateQuery.RebuildCache(false, false, false, false, false, true));
      }

      return this.cachedDefaultUpdateQuery;
   }
}
