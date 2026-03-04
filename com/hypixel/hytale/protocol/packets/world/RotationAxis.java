package com.hypixel.hytale.protocol.packets.world;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum RotationAxis {
   X(0),
   Y(1),
   Z(2);

   public static final RotationAxis[] VALUES = values();
   private final int value;

   private RotationAxis(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static RotationAxis fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("RotationAxis", value);
      }
   }
}
