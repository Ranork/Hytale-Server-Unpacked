package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum PositionDistanceOffsetType {
   DistanceOffset(0),
   DistanceOffsetRaycast(1),
   None(2);

   public static final PositionDistanceOffsetType[] VALUES = values();
   private final int value;

   private PositionDistanceOffsetType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static PositionDistanceOffsetType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("PositionDistanceOffsetType", value);
      }
   }
}
