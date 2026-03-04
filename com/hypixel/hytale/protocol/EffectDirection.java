package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum EffectDirection {
   None(0),
   BottomUp(1),
   TopDown(2),
   ToCenter(3),
   FromCenter(4);

   public static final EffectDirection[] VALUES = values();
   private final int value;

   private EffectDirection(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static EffectDirection fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("EffectDirection", value);
      }
   }
}
