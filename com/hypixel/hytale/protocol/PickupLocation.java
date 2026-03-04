package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum PickupLocation {
   Hotbar(0),
   Storage(1),
   Backpack(2);

   public static final PickupLocation[] VALUES = values();
   private final int value;

   private PickupLocation(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static PickupLocation fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("PickupLocation", value);
      }
   }
}
