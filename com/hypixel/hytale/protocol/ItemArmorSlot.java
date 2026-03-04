package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum ItemArmorSlot {
   Head(0),
   Chest(1),
   Hands(2),
   Legs(3);

   public static final ItemArmorSlot[] VALUES = values();
   private final int value;

   private ItemArmorSlot(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static ItemArmorSlot fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("ItemArmorSlot", value);
      }
   }
}
