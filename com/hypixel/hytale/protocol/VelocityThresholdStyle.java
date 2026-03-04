package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum VelocityThresholdStyle {
   Linear(0),
   Exp(1);

   public static final VelocityThresholdStyle[] VALUES = values();
   private final int value;

   private VelocityThresholdStyle(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static VelocityThresholdStyle fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("VelocityThresholdStyle", value);
      }
   }
}
