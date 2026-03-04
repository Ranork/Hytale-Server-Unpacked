package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum BlockMaterial {
   Empty(0),
   Solid(1);

   public static final BlockMaterial[] VALUES = values();
   private final int value;

   private BlockMaterial(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static BlockMaterial fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("BlockMaterial", value);
      }
   }
}
