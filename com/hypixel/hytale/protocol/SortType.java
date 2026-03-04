package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum SortType {
   Name(0),
   Type(1),
   Rarity(2);

   public static final SortType[] VALUES = values();
   private final int value;

   private SortType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static SortType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("SortType", value);
      }
   }
}
