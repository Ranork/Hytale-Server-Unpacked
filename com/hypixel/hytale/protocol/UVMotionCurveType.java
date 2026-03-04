package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum UVMotionCurveType {
   Constant(0),
   IncreaseLinear(1),
   IncreaseQuartIn(2),
   IncreaseQuartInOut(3),
   IncreaseQuartOut(4),
   DecreaseLinear(5),
   DecreaseQuartIn(6),
   DecreaseQuartInOut(7),
   DecreaseQuartOut(8);

   public static final UVMotionCurveType[] VALUES = values();
   private final int value;

   private UVMotionCurveType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static UVMotionCurveType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("UVMotionCurveType", value);
      }
   }
}
