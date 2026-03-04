package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum AssetEditorPopupNotificationType {
   Info(0),
   Success(1),
   Error(2),
   Warning(3);

   public static final AssetEditorPopupNotificationType[] VALUES = values();
   private final int value;

   private AssetEditorPopupNotificationType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static AssetEditorPopupNotificationType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("AssetEditorPopupNotificationType", value);
      }
   }
}
