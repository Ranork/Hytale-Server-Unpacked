package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum EasingType {
   Linear(0),
   QuadIn(1),
   QuadOut(2),
   QuadInOut(3),
   CubicIn(4),
   CubicOut(5),
   CubicInOut(6),
   QuartIn(7),
   QuartOut(8),
   QuartInOut(9),
   QuintIn(10),
   QuintOut(11),
   QuintInOut(12),
   SineIn(13),
   SineOut(14),
   SineInOut(15),
   ExpoIn(16),
   ExpoOut(17),
   ExpoInOut(18),
   CircIn(19),
   CircOut(20),
   CircInOut(21),
   ElasticIn(22),
   ElasticOut(23),
   ElasticInOut(24),
   BackIn(25),
   BackOut(26),
   BackInOut(27),
   BounceIn(28),
   BounceOut(29),
   BounceInOut(30);

   public static final EasingType[] VALUES = values();
   private final int value;

   private EasingType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static EasingType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("EasingType", value);
      }
   }
}
