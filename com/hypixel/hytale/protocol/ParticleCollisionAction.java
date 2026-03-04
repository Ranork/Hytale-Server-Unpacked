package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum ParticleCollisionAction {
   Expire(0),
   LastFrame(1),
   Linger(2);

   public static final ParticleCollisionAction[] VALUES = values();
   private final int value;

   private ParticleCollisionAction(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static ParticleCollisionAction fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("ParticleCollisionAction", value);
      }
   }
}
