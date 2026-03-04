package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum EffectOp {
   Add(0),
   Remove(1);

   public static final EffectOp[] VALUES = values();
   private final int value;

   private EffectOp(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static EffectOp fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("EffectOp", value);
      }
   }
}
