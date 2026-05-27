package com.hypixel.hytale.protocol.packets.buildertools;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum ExtrudeMode {
   Extrude(0),
   Shrink(1),
   Fill(2);

   public static final ExtrudeMode[] VALUES = values();
   private final int value;

   private ExtrudeMode(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static ExtrudeMode fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("ExtrudeMode", value);
      }
   }
}
