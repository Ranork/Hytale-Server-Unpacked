package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum DrawType {
   Empty(0),
   GizmoCube(1),
   Cube(2),
   Model(3),
   CubeWithModel(4);

   public static final DrawType[] VALUES = values();
   private final int value;

   private DrawType(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static DrawType fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("DrawType", value);
      }
   }
}
