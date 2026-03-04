package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum OverlapBehavior {
   Extend(0),
   Overwrite(1),
   Ignore(2);

   public static final OverlapBehavior[] VALUES = values();
   private final int value;

   private OverlapBehavior(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static OverlapBehavior fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("OverlapBehavior", value);
      }
   }
}
