package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum AssetEditorEditorType {
   None(0),
   Text(1),
   JsonSource(2),
   JsonConfig(3),
   Model(4),
   Texture(5),
   Animation(6);

   public static final AssetEditorEditorType[] VALUES = values();
   private final int value;

   private AssetEditorEditorType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static AssetEditorEditorType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("AssetEditorEditorType", value);
      }
   }
}
