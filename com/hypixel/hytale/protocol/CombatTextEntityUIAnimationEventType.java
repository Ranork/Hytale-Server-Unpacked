package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum CombatTextEntityUIAnimationEventType {
   Scale(0),
   Position(1),
   Opacity(2);

   public static final CombatTextEntityUIAnimationEventType[] VALUES = values();
   private final int value;

   private CombatTextEntityUIAnimationEventType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static CombatTextEntityUIAnimationEventType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("CombatTextEntityUIAnimationEventType", value);
      }
   }
}
