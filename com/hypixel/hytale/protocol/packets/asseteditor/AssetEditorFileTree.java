package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum AssetEditorFileTree {
   Server(0),
   Common(1);

   public static final AssetEditorFileTree[] VALUES = values();
   private final int value;

   private AssetEditorFileTree(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static AssetEditorFileTree fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("AssetEditorFileTree", value);
      }
   }
}
