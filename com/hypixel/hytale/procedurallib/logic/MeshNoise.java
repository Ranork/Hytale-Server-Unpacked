package com.hypixel.hytale.procedurallib.logic;

import com.hypixel.hytale.math.vector.Vector2i;
import com.hypixel.hytale.procedurallib.NoiseFunction;
import com.hypixel.hytale.procedurallib.condition.IIntCondition;
import com.hypixel.hytale.procedurallib.logic.cell.GridCellDistanceFunction;

public class MeshNoise implements NoiseFunction {
   public static final Vector2i[] ADJACENT_CELLS = new Vector2i[]{new Vector2i(-1, 0), new Vector2i(0, -1), new Vector2i(1, 0), new Vector2i(0, 1)};
   private final IIntCondition density;
   private final double thickness;
   private final double thicknessSquared;
   private final double jitterX;
   private final double jitterY;

   public MeshNoise(IIntCondition density, double thickness, double jitterX, double jitterY) {
      this.density = density;
      this.thickness = thickness;
      this.thicknessSquared = thickness * thickness;
      this.jitterX = jitterX;
      this.jitterY = jitterY;
   }

   @Override
   public double get(int seed, int offsetSeed, double x, double y) {
      double thickness = this.thickness;
      double lowest = this.thicknessSquared;
      int _x = GeneralNoise.fastFloor(x);
      int _y = GeneralNoise.fastFloor(y);
      double rx = x - _x;
      double ry = y - _y;

      for (int c = 0; c < 8; c++) {
         switch (c) {
            case 0:
               if (rx >= thickness || ry >= thickness) {
                  continue;
               }
               break;
            case 1:
               if (rx >= thickness) {
                  continue;
               }
               break;
            case 2:
               if (rx >= thickness || ry < 1.0 - thickness) {
                  continue;
               }
               break;
            case 3:
               if (ry >= thickness) {
                  continue;
               }
            case 4:
            default:
               break;
            case 5:
               if (ry < 1.0 - thickness) {
                  continue;
               }
               break;
            case 6:
               if (rx < 1.0 - thickness || ry >= thickness) {
                  continue;
               }
               break;
            case 7:
               if (rx < 1.0 - thickness) {
                  continue;
               }
         }

         int cx = c / 3;
         int cy = c % 3;
         int xr = _x + cx - 1;
         int yr = _y + cy - 1;
         int cellHash = GridCellDistanceFunction.getHash(offsetSeed, xr, yr);
         if (this.density.eval(cellHash)) {
            DoubleArray.Double2 cell = CellularNoise.CELL_2D[cellHash & 0xFF];
            double cellX = cell.x * this.jitterX;
            double cellY = cell.y * this.jitterY;
            double centerX = xr + cellX;
            double centerY = yr + cellY;
            double qX = x - centerX;
            double qY = y - centerY;

            for (Vector2i v : ADJACENT_CELLS) {
               int xi = xr + v.x;
               int yi = yr + v.y;
               int vecHash = GridCellDistanceFunction.getHash(offsetSeed, xi, yi);
               if (this.density.eval(vecHash)) {
                  DoubleArray.Double2 vec = CellularNoise.CELL_2D[vecHash & 0xFF];
                  double vecX = vec.x * this.jitterX;
                  double vecY = vec.y * this.jitterY;
                  double vx = v.x + vecX - cellX;
                  double vy = v.y + vecY - cellY;
                  double t = (qX * vx + qY * vy) / (vx * vx + vy * vy);
                  if (t < 0.0) {
                     t = 0.0;
                  } else if (t > 1.0) {
                     t = 1.0;
                  }

                  double lx = centerX + vx * t;
                  double ly = centerY + vy * t;
                  double dx = x - lx;
                  double dy = y - ly;
                  double distance = dx * dx + dy * dy;
                  if (distance < lowest) {
                     lowest = distance;
                  }
               }
            }
         }
      }

      if (lowest != this.thicknessSquared) {
         double distance = Math.sqrt(lowest);
         double d = distance / thickness;
         return d * 2.0 - 1.0;
      } else {
         return 1.0;
      }
   }

   @Override
   public double get(int seed, int offsetSeed, double x, double y, double z) {
      throw new UnsupportedOperationException("3d not supported");
   }
}
