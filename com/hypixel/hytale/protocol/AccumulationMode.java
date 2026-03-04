package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum AccumulationMode {
   Set(0),
   Sum(1),
   Average(2);

   public static final AccumulationMode[] VALUES = values();
   private final int value;

   private AccumulationMode(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static AccumulationMode fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("AccumulationMode", value);
      }
   }
}
