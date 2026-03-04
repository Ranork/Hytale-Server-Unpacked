package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum MountController {
   Minecart(0),
   BlockMount(1);

   public static final MountController[] VALUES = values();
   private final int value;

   private MountController(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static MountController fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("MountController", value);
      }
   }
}
