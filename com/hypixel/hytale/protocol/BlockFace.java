package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum BlockFace {
   None(0),
   Up(1),
   Down(2),
   North(3),
   South(4),
   East(5),
   West(6);

   public static final BlockFace[] VALUES = values();
   private final int value;

   private BlockFace(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static BlockFace fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("BlockFace", value);
      }
   }
}
