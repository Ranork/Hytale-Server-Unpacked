package com.hypixel.hytale.builtin.asseteditor.event;

import com.hypixel.hytale.builtin.asseteditor.EditorClient;

public class AssetEditorActivateButtonEvent extends EditorClientEvent<String> {
   private final String buttonId;

   public AssetEditorActivateButtonEvent(EditorClient editorClient, String buttonId) {
      super(editorClient);
      this.buttonId = buttonId;
   }

   public String getButtonId() {
      return this.buttonId;
   }
}
