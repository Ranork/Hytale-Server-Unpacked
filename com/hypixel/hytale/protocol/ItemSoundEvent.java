package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum ItemSoundEvent {
   Drag(0),
   Drop(1);

   public static final ItemSoundEvent[] VALUES = values();
   private final int value;

   private ItemSoundEvent(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static ItemSoundEvent fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("ItemSoundEvent", value);
      }
   }
}
