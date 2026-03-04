package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum UpdateType {
   Init(0),
   AddOrUpdate(1),
   Remove(2);

   public static final UpdateType[] VALUES = values();
   private final int value;

   private UpdateType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static UpdateType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("UpdateType", value);
      }
   }
}
