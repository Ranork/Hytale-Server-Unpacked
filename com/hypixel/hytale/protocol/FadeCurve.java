package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum FadeCurve {
   Linear(0),
   Logarithmic(1),
   Exponential(2),
   SCurve(3),
   EqualPowerSine(4);

   public static final FadeCurve[] VALUES = values();
   private final int value;

   private FadeCurve(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static FadeCurve fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("FadeCurve", value);
      }
   }
}
