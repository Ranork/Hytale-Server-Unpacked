package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum BlockMountType {
   Seat(0),
   Bed(1);

   public static final BlockMountType[] VALUES = values();
   private final int value;

   private BlockMountType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static BlockMountType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("BlockMountType", value);
      }
   }
}
