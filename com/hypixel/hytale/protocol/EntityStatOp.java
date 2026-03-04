package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum EntityStatOp {
   Init(0),
   Remove(1),
   PutModifier(2),
   RemoveModifier(3),
   Add(4),
   Set(5),
   Minimize(6),
   Maximize(7),
   Reset(8);

   public static final EntityStatOp[] VALUES = values();
   private final int value;

   private EntityStatOp(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static EntityStatOp fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("EntityStatOp", value);
      }
   }
}
