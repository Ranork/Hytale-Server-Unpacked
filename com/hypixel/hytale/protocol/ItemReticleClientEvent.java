package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum ItemReticleClientEvent {
   OnHit(0),
   Wielding(1),
   OnMovementLeft(2),
   OnMovementRight(3),
   OnMovementBack(4);

   public static final ItemReticleClientEvent[] VALUES = values();
   private final int value;

   private ItemReticleClientEvent(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static ItemReticleClientEvent fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("ItemReticleClientEvent", value);
      }
   }
}
