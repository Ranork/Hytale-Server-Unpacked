package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum AmbienceFXSoundPlay3D {
   Random(0),
   LocationName(1),
   No(2);

   public static final AmbienceFXSoundPlay3D[] VALUES = values();
   private final int value;

   private AmbienceFXSoundPlay3D(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static AmbienceFXSoundPlay3D fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("AmbienceFXSoundPlay3D", value);
      }
   }
}
