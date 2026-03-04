package com.hypixel.hytale.builtin.asseteditor.event;

import com.hypixel.hytale.builtin.asseteditor.EditorClient;

public class AssetEditorUpdateWeatherPreviewLockEvent extends EditorClientEvent<Void> {
   private final boolean locked;

   public AssetEditorUpdateWeatherPreviewLockEvent(EditorClient editorClient, boolean locked) {
      super(editorClient);
      this.locked = locked;
   }

   public boolean isLocked() {
      return this.locked;
   }
}
