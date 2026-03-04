package com.hypixel.hytale.server.core.prefab.event;

import com.hypixel.hytale.component.system.CancellableEcsEvent;

public class PrefabPasteEvent extends CancellableEcsEvent {
   private final int prefabId;
   private final boolean pasteStart;

   public PrefabPasteEvent(int prefabId, boolean pasteStart) {
      this.prefabId = prefabId;
      this.pasteStart = pasteStart;
   }

   public int getPrefabId() {
      return this.prefabId;
   }

   public boolean isPasteStart() {
      return this.pasteStart;
   }
}
