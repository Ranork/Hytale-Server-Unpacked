package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum MusicTransitionType {
   Crossfade(0),
   FadeOutFadeIn(1),
   Immediate(2);

   public static final MusicTransitionType[] VALUES = values();
   private final int value;

   private MusicTransitionType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static MusicTransitionType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("MusicTransitionType", value);
      }
   }
}
