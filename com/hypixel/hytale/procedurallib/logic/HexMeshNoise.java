package com.hypixel.hytale.procedurallib.logic;

import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.procedurallib.NoiseFunction;
import com.hypixel.hytale.procedurallib.condition.IIntCondition;
import com.hypixel.hytale.procedurallib.logic.cell.HexCellDistanceFunction;
import com.hypixel.hytale.procedurallib.logic.cell.jitter.CellJitter;

public class HexMeshNoise implements NoiseFunction {
   protected final IIntCondition density;
   protected final double thickness;
   protected final double thicknessSquared;
   protected final CellJitter jitter;
   protected final boolean linesX;
   protected final boolean linesY;
   protected final boolean linesZ;

   public HexMeshNoise(IIntCondition density, double thickness, CellJitter jitter, boolean linesX, boolean linesY, boolean linesZ) {
      double domainLocalThickness = HexCellDistanceFunction.DISTANCE_FUNCTION.scale(thickness);
      this.density = density;
      this.thickness = domainLocalThickness;
      this.thicknessSquared = domainLocalThickness * domainLocalThickness;
      this.jitter = jitter;
      this.linesX = linesX;
      this.linesY = linesY;
      this.linesZ = linesZ;
   }

   @Override
   public double get(int seed, int offsetSeed, double x, double y) {
      x = HexCellDistanceFunction.DISTANCE_FUNCTION.scale(x);
      y = HexCellDistanceFunction.DISTANCE_FUNCTION.scale(y);
      int cx = HexCellDistanceFunction.toGridX(x, y);
      int cy = HexCellDistanceFunction.toGridY(x, y);
      double nearest = this.thicknessSquared;
      nearest = this.checkConnections(offsetSeed, x, y, cx - 1, cy - 1, nearest);
      nearest = this.checkConnections(offsetSeed, x, y, cx - 1, cy + 0, nearest);
      nearest = this.checkConnections(offsetSeed, x, y, cx + 1, cy + 0, nearest);
      nearest = this.checkConnections(offsetSeed, x, y, cx + 0, cy - 1, nearest);
      nearest = this.checkConnections(offsetSeed, x, y, cx + 0, cy + 1, nearest);
      if (this.linesZ) {
         nearest = this.checkDiagonalConnections(offsetSeed, x, y, cx + 0, cy + 0, nearest);
         nearest = this.checkDiagonalConnections(offsetSeed, x, y, cx + 0, cy - 1, nearest);
         nearest = this.checkDiagonalConnections(offsetSeed, x, y, cx + 0, cy + 1, nearest);
         nearest = this.checkDiagonalConnections(offsetSeed, x, y, cx - 1, cy + 0, nearest);
         nearest = this.checkDiagonalConnections(offsetSeed, x, y, cx - 1, cy - 1, nearest);
      }

      if (nearest < this.thicknessSquared) {
         double distance = Math.sqrt(nearest);
         double d = distance / this.thickness;
         return d * 2.0 - 1.0;
      } else {
         return 1.0;
      }
   }

   @Override
   public double get(int seed, int offsetSeed, double x, double y, double z) {
      throw new UnsupportedOperationException("3d not supported");
   }

   protected double checkConnections(int offsetSeed, double x, double y, int cx, int cy, double nearest) {
      int hash = HexCellDistanceFunction.getHash(offsetSeed, cx, cy);
      if (!this.density.eval(hash)) {
         return nearest;
      } else {
         DoubleArray.Double2 vec = HexCellDistanceFunction.HEX_CELL_2D[hash & 0xFF];
         double px = this.jitter.getPointX(cx, vec);
         double py = this.jitter.getPointY(cy, vec);
         double ax = HexCellDistanceFunction.toHexX(px, py);
         double ay = HexCellDistanceFunction.toHexY(px, py);
         double adx = x - ax;
         double ady = y - ay;
         if (this.linesX) {
            nearest = Math.min(nearest, this.dist2Cell(offsetSeed, x, y, adx, ady, ax, ay, cx - 1, cy));
            nearest = Math.min(nearest, this.dist2Cell(offsetSeed, x, y, adx, ady, ax, ay, cx + 1, cy));
         }

         if (this.linesY) {
            nearest = Math.min(nearest, this.dist2Cell(offsetSeed, x, y, adx, ady, ax, ay, cx, cy - 1));
            nearest = Math.min(nearest, this.dist2Cell(offsetSeed, x, y, adx, ady, ax, ay, cx, cy + 1));
         }

         return nearest;
      }
   }

   protected double checkDiagonalConnections(int offsetSeed, double x, double y, int cx, int cy, double nearest) {
      int hash = HexCellDistanceFunction.getHash(offsetSeed, cx, cy);
      if (!this.density.eval(hash)) {
         return nearest;
      } else {
         DoubleArray.Double2 vec = HexCellDistanceFunction.HEX_CELL_2D[hash & 0xFF];
         double px = this.jitter.getPointX(cx, vec);
         double py = this.jitter.getPointY(cy, vec);
         double ax = HexCellDistanceFunction.toHexX(px, py);
         double ay = HexCellDistanceFunction.toHexY(px, py);
         double adx = x - ax;
         double ady = y - ay;
         nearest = Math.min(nearest, this.dist2Cell(offsetSeed, x, y, adx, ady, ax, ay, cx - 1, cy + 1));
         return Math.min(nearest, this.dist2Cell(offsetSeed, x, y, adx, ady, ax, ay, cx + 1, cy - 1));
      }
   }

   protected double dist2Cell(int offsetSeed, double x, double y, double adx, double ady, double ax, double ay, int cx, int cy) {
      int hash = HexCellDistanceFunction.getHash(offsetSeed, cx, cy);
      if (!this.density.eval(hash)) {
         return Double.MAX_VALUE;
      } else {
         DoubleArray.Double2 vec = HexCellDistanceFunction.HEX_CELL_2D[hash & 0xFF];
         double px = this.jitter.getPointX(cx, vec);
         double py = this.jitter.getPointY(cy, vec);
         double bx = HexCellDistanceFunction.toHexX(px, py);
         double by = HexCellDistanceFunction.toHexY(px, py);
         return MathUtil.distanceToLineSq(x, y, ax, ay, bx, by, adx, ady, bx - ax, by - ay);
      }
   }
}
