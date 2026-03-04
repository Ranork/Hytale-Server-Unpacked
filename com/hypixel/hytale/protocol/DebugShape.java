package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum DebugShape {
   Sphere(0),
   Cylinder(1),
   Cone(2),
   Cube(3),
   Frustum(4),
   Sector(5),
   Disc(6);

   public static final DebugShape[] VALUES = values();
   private final int value;

   private DebugShape(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static DebugShape fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("DebugShape", value);
      }
   }
}
