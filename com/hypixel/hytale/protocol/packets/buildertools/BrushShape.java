package com.hypixel.hytale.protocol.packets.buildertools;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum BrushShape {
   Cube(0),
   Sphere(1),
   Cylinder(2),
   Cone(3),
   InvertedCone(4),
   Pyramid(5),
   InvertedPyramid(6),
   Dome(7),
   InvertedDome(8),
   Diamond(9),
   Torus(10);

   public static final BrushShape[] VALUES = values();
   private final int value;

   private BrushShape(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   public static BrushShape fromValue(int value) {
      if (value >= 0 && value < VALUES.length) {
         return VALUES[value];
      } else {
         throw ProtocolException.invalidEnumValue("BrushShape", value);
      }
   }
}
