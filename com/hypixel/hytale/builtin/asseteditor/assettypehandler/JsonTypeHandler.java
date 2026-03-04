package com.hypixel.hytale.builtin.asseteditor.assettypehandler;

import com.hypixel.hytale.assetstore.AssetUpdateQuery;
import com.hypixel.hytale.builtin.asseteditor.AssetPath;
import com.hypixel.hytale.builtin.asseteditor.EditorClient;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorAssetType;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import org.bson.BsonDocument;

public abstract class JsonTypeHandler extends AssetTypeHandler {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

   protected JsonTypeHandler(@Nonnull AssetEditorAssetType config) {
      super(config);
   }

   @Override
   public AssetTypeHandler.AssetLoadResult loadAsset(AssetPath path, Path dataPath, byte[] data, AssetUpdateQuery updateQuery, EditorClient editorClient) {
      BsonDocument doc;
      try {
         doc = BsonDocument.parse(new String(data, StandardCharsets.UTF_8));
      } catch (Exception var8) {
         ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var8)).log("Failed to parse JSON for " + path);
         return AssetTypeHandler.AssetLoadResult.ASSETS_UNCHANGED;
      }

      return this.loadAssetFromDocument(path, dataPath, doc, updateQuery, editorClient);
   }

   public abstract AssetTypeHandler.AssetLoadResult loadAssetFromDocument(
      AssetPath var1, Path var2, BsonDocument var3, AssetUpdateQuery var4, EditorClient var5
   );

   public AssetTypeHandler.AssetLoadResult loadAssetFromDocument(AssetPath path, Path dataPath, BsonDocument document, EditorClient editorClient) {
      return this.loadAssetFromDocument(path, dataPath, document, this.getDefaultUpdateQuery(), editorClient);
   }

   @Override
   public boolean isValidData(@Nonnull byte[] data) {
      try {
         String str = new String(data, StandardCharsets.UTF_8);
         char[] buffer = str.toCharArray();
         RawJsonReader.validateBsonDocument(new RawJsonReader(buffer));
         return true;
      } catch (Exception var4) {
         return false;
      }
   }
}
