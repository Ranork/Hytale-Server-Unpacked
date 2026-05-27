package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum AudioStateAuthority {
   Server(0),
   Client(1);

   public static final AudioStateAuthority[] VALUES = values();
   private final int value;

   private AudioStateAuthority(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static AudioStateAuthority fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("AudioStateAuthority", value);
      }
   }
}
