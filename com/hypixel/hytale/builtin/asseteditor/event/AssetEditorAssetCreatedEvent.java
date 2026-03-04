package com.hypixel.hytale.builtin.asseteditor.event;

import com.hypixel.hytale.builtin.asseteditor.EditorClient;
import java.nio.file.Path;

public class AssetEditorAssetCreatedEvent extends EditorClientEvent<String> {
   private final String assetType;
   private final Path assetPath;
   private final byte[] data;
   private final String buttonId;

   public AssetEditorAssetCreatedEvent(EditorClient editorClient, String assetType, Path assetPath, byte[] data, String buttonId) {
      super(editorClient);
      this.assetType = assetType;
      this.assetPath = assetPath;
      this.data = data;
      this.buttonId = buttonId;
   }

   public String getAssetType() {
      return this.assetType;
   }

   public Path getAssetPath() {
      return this.assetPath;
   }

   public byte[] getData() {
      return this.data;
   }

   public String getButtonId() {
      return this.buttonId;
   }
}
