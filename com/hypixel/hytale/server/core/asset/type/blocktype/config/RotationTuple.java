package com.hypixel.hytale.server.core.asset.type.blocktype.config;

import com.hypixel.hytale.math.Axis;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public record RotationTuple(int index, Rotation yaw, Rotation pitch, Rotation roll) {
   public static final RotationTuple[] EMPTY_ARRAY = new RotationTuple[0];
   public static final RotationTuple NONE = new RotationTuple(0, Rotation.None, Rotation.None, Rotation.None);
   public static final int NONE_INDEX = 0;
   @Nonnull
   public static final RotationTuple[] VALUES;

   public static RotationTuple of(@Nonnull Rotation yaw, @Nonnull Rotation pitch, @Nonnull Rotation roll) {
      return VALUES[index(yaw, pitch, roll)];
   }

   public static RotationTuple of(@Nonnull Rotation yaw, @Nonnull Rotation pitch) {
      return VALUES[index(yaw, pitch, Rotation.None)];
   }

   public static int index(@Nonnull Rotation yaw, @Nonnull Rotation pitch, @Nonnull Rotation roll) {
      return roll.ordinal() * Rotation.VALUES.length * Rotation.VALUES.length + pitch.ordinal() * Rotation.VALUES.length + yaw.ordinal();
   }

   public static RotationTuple get(int index) {
      return VALUES[index];
   }

   public static RotationTuple getRotation(@Nonnull RotationTuple[] rotations, @Nonnull RotationTuple pair, @Nonnull Rotation rotation) {
      int index = 0;

      for (int i = 0; i < rotations.length; i++) {
         RotationTuple rotationPair = rotations[i];
         if (pair.equals(rotationPair)) {
            index = i;
            break;
         }
      }

      return rotations[(index + rotation.ordinal()) % Rotation.VALUES.length];
   }

   public static RotationTuple flip(@Nonnull RotationTuple blockRotation, @Nullable BlockFlipType flipType, @Nonnull Axis axis, int[][][] flipCorrections) {
      int[][] matrix = eulerToMatrix(blockRotation.yaw, blockRotation.pitch, blockRotation.roll);

      int flipRow = switch (axis) {
         case X -> 0;
         case Y -> 1;
         case Z -> 2;
      };

      for (int i = 0; i < 3; i++) {
         matrix[flipRow][i] = -matrix[flipRow][i];
      }

      int[][] correction = flipCorrections[flipType.ordinal()];
      int[][] result = multiply3x3(matrix, correction);
      return matrixToRotationTuple(result);
   }

   @Nonnull
   public RotationTuple composeOnAxis(@Nonnull Axis axis, @Nonnull Rotation rotation) {
      int[][] current = eulerToMatrix(this.yaw, this.pitch, this.roll);
      int[][] axisRot = axisRotationMatrix(axis, rotation);
      int[][] result = multiply3x3(axisRot, current);
      return matrixToRotationTuple(result);
   }

   private static int[][] eulerToMatrix(@Nonnull Rotation yaw, @Nonnull Rotation pitch, @Nonnull Rotation roll) {
      int cy = cos90(yaw);
      int sy = sin90(yaw);
      int cp = cos90(pitch);
      int sp = sin90(pitch);
      int cr = cos90(roll);
      int sr = sin90(roll);
      return new int[][]{
         {cy * cr + sy * sp * sr, -cy * sr + sy * sp * cr, sy * cp}, {cp * sr, cp * cr, -sp}, {-sy * cr + cy * sp * sr, sy * sr + cy * sp * cr, cy * cp}
      };
   }

   private static int[][] axisRotationMatrix(@Nonnull Axis axis, @Nonnull Rotation rotation) {
      int c = cos90(rotation);
      int s = sin90(rotation);

      return switch (axis) {
         case X -> new int[][]{{1, 0, 0}, {0, c, -s}, {0, s, c}};
         case Y -> new int[][]{{c, 0, s}, {0, 1, 0}, {-s, 0, c}};
         case Z -> new int[][]{{c, -s, 0}, {s, c, 0}, {0, 0, 1}};
      };
   }

   private static int[][] multiply3x3(int[][] a, int[][] b) {
      int[][] r = new int[3][3];

      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 3; j++) {
            r[i][j] = a[i][0] * b[0][j] + a[i][1] * b[1][j] + a[i][2] * b[2][j];
         }
      }

      return r;
   }

   private static RotationTuple matrixToRotationTuple(int[][] m) {
      int sp = -m[1][2];
      Rotation newPitch = sinToRotation(sp);
      Rotation newYaw;
      Rotation newRoll;
      if (sp != 1 && sp != -1) {
         newYaw = atan2_90(m[0][2], m[2][2]);
         newRoll = atan2_90(m[1][0], m[1][1]);
      } else {
         newYaw = atan2_90(-m[2][0], m[0][0]);
         newRoll = Rotation.None;
      }

      return of(newYaw, newPitch, newRoll);
   }

   private static int cos90(@Nonnull Rotation r) {
      return switch (r) {
         case None -> 1;
         case Ninety -> 0;
         case OneEighty -> -1;
         case TwoSeventy -> 0;
      };
   }

   private static int sin90(@Nonnull Rotation r) {
      return switch (r) {
         case None -> 0;
         case Ninety -> 1;
         case OneEighty -> 0;
         case TwoSeventy -> -1;
      };
   }

   private static Rotation sinToRotation(int s) {
      return switch (s) {
         case -1 -> Rotation.TwoSeventy;
         case 0 -> Rotation.None;
         case 1 -> Rotation.Ninety;
         default -> throw new IllegalArgumentException("Invalid sin value for 90-degree rotation: " + s);
      };
   }

   private static Rotation atan2_90(int sinVal, int cosVal) {
      if (sinVal == 0 && cosVal == 1) {
         return Rotation.None;
      } else if (sinVal == 1 && cosVal == 0) {
         return Rotation.Ninety;
      } else if (sinVal == 0 && cosVal == -1) {
         return Rotation.OneEighty;
      } else if (sinVal == -1 && cosVal == 0) {
         return Rotation.TwoSeventy;
      } else {
         throw new IllegalArgumentException("Invalid atan2 values for 90-degree rotation: sin=" + sinVal + " cos=" + cosVal);
      }
   }

   @Nonnull
   public RotationTuple add(@Nonnull RotationTuple rotation) {
      return of(rotation.yaw.add(this.yaw), rotation.pitch.add(this.pitch), rotation.roll.add(this.roll));
   }

   @Nonnull
   public Vector3d rotatedVector(@Nonnull Vector3d vector) {
      return Rotation.rotate(vector, this.yaw, this.pitch, this.roll);
   }

   public void applyRotationTo(@Nonnull Vector3i vector) {
      Rotation.applyRotationTo(vector, this.yaw, this.pitch, this.roll);
   }

   public void applyRotationTo(@Nonnull Vector3f vector) {
      Rotation.applyRotationTo(vector, this.yaw, this.pitch, this.roll);
   }

   public void applyRotationTo(@Nonnull Vector3d vector) {
      Rotation.applyRotationTo(vector, this.yaw, this.pitch, this.roll);
   }

   public void undoRotationTo(@Nonnull Vector3i vector) {
      Rotation.undoRotationTo(vector, this.yaw, this.pitch, this.roll);
   }

   public void undoRotationTo(@Nonnull Vector3f vector) {
      Rotation.undoRotationTo(vector, this.yaw, this.pitch, this.roll);
   }

   public void undoRotationTo(@Nonnull Vector3d vector) {
      Rotation.undoRotationTo(vector, this.yaw, this.pitch, this.roll);
   }

   static {
      RotationTuple[] arr = new RotationTuple[Rotation.VALUES.length * Rotation.VALUES.length * Rotation.VALUES.length];
      arr[0] = NONE;

      for (Rotation roll : Rotation.VALUES) {
         for (Rotation pitch : Rotation.VALUES) {
            for (Rotation yaw : Rotation.VALUES) {
               if (yaw != Rotation.None || pitch != Rotation.None || roll != Rotation.None) {
                  int index = index(yaw, pitch, roll);
                  arr[index] = new RotationTuple(index, yaw, pitch, roll);
               }
            }
         }
      }

      VALUES = arr;
   }
}
