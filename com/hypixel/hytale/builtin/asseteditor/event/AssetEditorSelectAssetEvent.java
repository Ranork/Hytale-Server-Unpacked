package com.hypixel.hytale.builtin.asseteditor.event;

import com.hypixel.hytale.builtin.asseteditor.AssetPath;
import com.hypixel.hytale.builtin.asseteditor.EditorClient;

public class AssetEditorSelectAssetEvent extends EditorClientEvent<Void> {
   private final String assetType;
   private final AssetPath assetFilePath;
   private final String previousAssetType;
   private final AssetPath previousAssetFilePath;

   public AssetEditorSelectAssetEvent(
      EditorClient editorClient, String assetType, AssetPath assetFilePath, String previousAssetType, AssetPath previousAssetFilePath
   ) {
      super(editorClient);
      this.assetType = assetType;
      this.assetFilePath = assetFilePath;
      this.previousAssetType = previousAssetType;
      this.previousAssetFilePath = previousAssetFilePath;
   }

   public String getAssetType() {
      return this.assetType;
   }

   public AssetPath getAssetFilePath() {
      return this.assetFilePath;
   }

   public String getPreviousAssetType() {
      return this.previousAssetType;
   }

   public AssetPath getPreviousAssetFilePath() {
      return this.previousAssetFilePath;
   }
}
