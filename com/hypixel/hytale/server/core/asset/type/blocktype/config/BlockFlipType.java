package com.hypixel.hytale.server.core.asset.type.blocktype.config;

import com.hypixel.hytale.math.Axis;
import javax.annotation.Nonnull;

public enum BlockFlipType {
   ORTHOGONAL,
   SYMMETRIC;

   public Rotation flipYaw(@Nonnull Rotation rotation, Axis axis) {
      if (axis == Axis.Y) {
         return rotation;
      } else {
         switch (this) {
            case ORTHOGONAL:
               int multiplier = axis == rotation.getAxisOfAlignment() ? -1 : 1;
               int index = rotation.ordinal() + multiplier + Rotation.VALUES.length;
               index %= Rotation.VALUES.length;
               return Rotation.VALUES[index];
            case SYMMETRIC:
               if (rotation.getAxisOfAlignment() == axis) {
                  return rotation.add(Rotation.OneEighty);
               }

               return rotation;
            default:
               throw new UnsupportedOperationException();
         }
      }
   }
}
