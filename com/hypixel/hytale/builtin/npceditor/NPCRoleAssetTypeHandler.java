package com.hypixel.hytale.builtin.npceditor;

import com.hypixel.hytale.assetstore.AssetUpdateQuery;
import com.hypixel.hytale.builtin.asseteditor.AssetPath;
import com.hypixel.hytale.builtin.asseteditor.EditorClient;
import com.hypixel.hytale.builtin.asseteditor.assettypehandler.AssetTypeHandler;
import com.hypixel.hytale.builtin.asseteditor.assettypehandler.JsonTypeHandler;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorAssetType;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorEditorType;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.npc.NPCPlugin;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import org.bson.BsonDocument;

public class NPCRoleAssetTypeHandler extends JsonTypeHandler {
   public static final String TYPE_ID = "NPCRole";

   public NPCRoleAssetTypeHandler() {
      super(new AssetEditorAssetType("NPCRole", null, false, NPCPlugin.ROLE_ASSETS_PATH, ".json", AssetEditorEditorType.JsonSource));
   }

   @Nonnull
   @Override
   public AssetTypeHandler.AssetLoadResult loadAssetFromDocument(
      AssetPath assetPath, Path dataPath, BsonDocument document, AssetUpdateQuery updateQuery, EditorClient editorClient
   ) {
      NPCPlugin.get().getBuilderManager().assetEditorLoadFile(dataPath);
      return AssetTypeHandler.AssetLoadResult.ASSETS_CHANGED;
   }

   @Nonnull
   @Override
   public AssetTypeHandler.AssetLoadResult unloadAsset(@Nonnull AssetPath path, AssetUpdateQuery updateQuery) {
      Path rootPath = AssetModule.get().getAssetPack(path.packId()).getRoot();
      NPCPlugin.get().getBuilderManager().assetEditorRemoveFile(rootPath.resolve(path.path()).toAbsolutePath());
      return AssetTypeHandler.AssetLoadResult.ASSETS_CHANGED;
   }

   @Nonnull
   @Override
   public AssetTypeHandler.AssetLoadResult restoreOriginalAsset(@Nonnull AssetPath originalAssetPath, AssetUpdateQuery updateQuery) {
      Path rootPath = AssetModule.get().getAssetPack(originalAssetPath.packId()).getRoot();
      NPCPlugin.get().getBuilderManager().assetEditorLoadFile(rootPath.resolve(originalAssetPath.path()).toAbsolutePath());
      return AssetTypeHandler.AssetLoadResult.ASSETS_CHANGED;
   }

   @Nonnull
   @Override
   public AssetUpdateQuery getDefaultUpdateQuery() {
      return AssetUpdateQuery.DEFAULT_NO_REBUILD;
   }
}
