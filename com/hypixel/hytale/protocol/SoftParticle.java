package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum SoftParticle {
   Enable(0),
   Disable(1),
   Require(2);

   public static final SoftParticle[] VALUES = values();
   private final int value;

   private SoftParticle(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static SoftParticle fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("SoftParticle", value);
      }
   }
}
