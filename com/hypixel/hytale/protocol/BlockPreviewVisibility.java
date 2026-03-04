package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum BlockPreviewVisibility {
   AlwaysVisible(0),
   AlwaysHidden(1),
   Default(2);

   public static final BlockPreviewVisibility[] VALUES = values();
   private final int value;

   private BlockPreviewVisibility(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static BlockPreviewVisibility fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("BlockPreviewVisibility", value);
      }
   }
}
