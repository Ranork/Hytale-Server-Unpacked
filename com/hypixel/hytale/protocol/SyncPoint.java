package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum SyncPoint {
   Immediate(0),
   NextBeat(1),
   NextBar(2),
   ExitMarker(3);

   public static final SyncPoint[] VALUES = values();
   private final int value;

   private SyncPoint(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static SyncPoint fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("SyncPoint", value);
      }
   }
}
