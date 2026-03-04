package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum TagPatternType {
   Equals(0),
   And(1),
   Or(2),
   Not(3);

   public static final TagPatternType[] VALUES = values();
   private final int value;

   private TagPatternType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static TagPatternType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("TagPatternType", value);
      }
   }
}
