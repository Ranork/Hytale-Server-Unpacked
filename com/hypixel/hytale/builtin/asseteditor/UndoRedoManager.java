package com.hypixel.hytale.builtin.asseteditor;

import com.hypixel.hytale.builtin.asseteditor.data.AssetUndoRedoInfo;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;

public class UndoRedoManager {
   private final Map<AssetPath, AssetUndoRedoInfo> assetUndoRedoInfo = new Object2ObjectOpenHashMap();

   public AssetUndoRedoInfo getOrCreateUndoRedoStack(AssetPath path) {
      return this.assetUndoRedoInfo.computeIfAbsent(path, k -> new AssetUndoRedoInfo());
   }

   public AssetUndoRedoInfo getUndoRedoStack(AssetPath path) {
      return this.assetUndoRedoInfo.get(path);
   }

   public void putUndoRedoStack(AssetPath path, AssetUndoRedoInfo undoRedoInfo) {
      this.assetUndoRedoInfo.put(path, undoRedoInfo);
   }

   public AssetUndoRedoInfo clearUndoRedoStack(AssetPath path) {
      return this.assetUndoRedoInfo.remove(path);
   }
}
