package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum BlockNeighbor {
   Up(0),
   Down(1),
   North(2),
   East(3),
   South(4),
   West(5),
   UpNorth(6),
   UpSouth(7),
   UpEast(8),
   UpWest(9),
   DownNorth(10),
   DownSouth(11),
   DownEast(12),
   DownWest(13),
   NorthEast(14),
   SouthEast(15),
   SouthWest(16),
   NorthWest(17),
   UpNorthEast(18),
   UpSouthEast(19),
   UpSouthWest(20),
   UpNorthWest(21),
   DownNorthEast(22),
   DownSouthEast(23),
   DownSouthWest(24),
   DownNorthWest(25);

   public static final BlockNeighbor[] VALUES = values();
   private final int value;

   private BlockNeighbor(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static BlockNeighbor fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("BlockNeighbor", value);
      }
   }
}
