package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum BlockPlacementRotationMode {
   FacingPlayer(0),
   StairFacingPlayer(1),
   BlockNormal(2),
   Default(3);

   public static final BlockPlacementRotationMode[] VALUES = values();
   private final int value;

   private BlockPlacementRotationMode(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static BlockPlacementRotationMode fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("BlockPlacementRotationMode", value);
      }
   }
}
