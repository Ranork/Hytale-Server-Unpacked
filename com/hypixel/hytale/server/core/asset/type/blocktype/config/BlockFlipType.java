package com.hypixel.hytale.server.core.asset.type.blocktype.config;

import com.hypixel.hytale.math.Axis;
import javax.annotation.Nonnull;

public enum BlockFlipType {
   ORTHOGONAL,
   ORTHOGONAL_INVERSE,
   SYMMETRIC;

   public Rotation flipYaw(@Nonnull Rotation rotation, Axis axis) {
      return this.flipComponent(rotation, axis, Axis.Y, Axis.Z, rotation.getAxisOfAlignment());
   }

   private Rotation flipComponent(@Nonnull Rotation rotation, Axis axis, Axis ownAxis, Axis negateAxis, Axis alignment) {
      switch (this) {
         case ORTHOGONAL:
            int multiplier = rotation.getDegrees() % 180 == 90 ? 1 : -1;
            if (axis == negateAxis) {
               multiplier = -multiplier;
            }

            int index = rotation.ordinal() + multiplier + Rotation.VALUES.length;
            if (axis == ownAxis && rotation.getDegrees() % 180 == 90) {
               index += 2;
            }

            index %= Rotation.VALUES.length;
            return Rotation.VALUES[index];
         case ORTHOGONAL_INVERSE:
            int multiplierInv = rotation.getDegrees() % 180 == 90 ? -1 : 1;
            if (axis == negateAxis) {
               multiplierInv = -multiplierInv;
            }

            int indexInv = rotation.ordinal() + multiplierInv + Rotation.VALUES.length;
            if (axis == ownAxis && rotation.getDegrees() % 180 == 90) {
               indexInv += 2;
            }

            indexInv %= Rotation.VALUES.length;
            return Rotation.VALUES[indexInv];
         case SYMMETRIC:
            if (axis != ownAxis && alignment != axis) {
               return rotation;
            }

            return rotation.add(Rotation.OneEighty);
         default:
            throw new UnsupportedOperationException();
      }
   }
}
