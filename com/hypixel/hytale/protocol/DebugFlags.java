package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum DebugFlags {
   Fade(0),
   NoWireframe(1),
   NoSolid(2);

   public static final DebugFlags[] VALUES = values();
   private final int value;

   private DebugFlags(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static DebugFlags fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("DebugFlags", value);
      }
   }
}
