package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum ShelterType {
   Any(0),
   Open(1),
   Partial(2),
   Sheltered(3),
   Enclosed(4);

   public static final ShelterType[] VALUES = values();
   private final int value;

   private ShelterType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static ShelterType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("ShelterType", value);
      }
   }
}
