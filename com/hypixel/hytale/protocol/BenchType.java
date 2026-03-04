package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum BenchType {
   Crafting(0),
   Processing(1),
   DiagramCrafting(2),
   StructuralCrafting(3);

   public static final BenchType[] VALUES = values();
   private final int value;

   private BenchType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static BenchType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("BenchType", value);
      }
   }
}
