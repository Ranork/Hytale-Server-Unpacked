package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum EmitShape {
   Sphere(0),
   Cube(1);

   public static final EmitShape[] VALUES = values();
   private final int value;

   private EmitShape(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static EmitShape fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("EmitShape", value);
      }
   }
}
