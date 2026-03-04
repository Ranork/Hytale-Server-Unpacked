package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum ParticleUVOption {
   None(0),
   RandomFlipU(1),
   RandomFlipV(2),
   RandomFlipUV(3),
   FlipU(4),
   FlipV(5),
   FlipUV(6);

   public static final ParticleUVOption[] VALUES = values();
   private final int value;

   private ParticleUVOption(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static ParticleUVOption fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("ParticleUVOption", value);
      }
   }
}
