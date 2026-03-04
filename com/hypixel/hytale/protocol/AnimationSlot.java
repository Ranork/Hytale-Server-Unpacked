package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum AnimationSlot {
   Movement(0),
   Status(1),
   Action(2),
   Face(3),
   Emote(4);

   public static final AnimationSlot[] VALUES = values();
   private final int value;

   private AnimationSlot(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static AnimationSlot fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("AnimationSlot", value);
      }
   }
}
