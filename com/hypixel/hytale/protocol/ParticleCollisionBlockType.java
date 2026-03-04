package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum ParticleCollisionBlockType {
   None(0),
   Air(1),
   Solid(2),
   All(3);

   public static final ParticleCollisionBlockType[] VALUES = values();
   private final int value;

   private ParticleCollisionBlockType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static ParticleCollisionBlockType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("ParticleCollisionBlockType", value);
      }
   }
}
