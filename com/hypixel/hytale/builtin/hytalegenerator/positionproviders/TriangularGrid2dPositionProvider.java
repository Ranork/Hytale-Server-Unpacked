package com.hypixel.hytale.builtin.hytalegenerator.positionproviders;

import com.hypixel.hytale.builtin.hytalegenerator.pipe.Control;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.joml.Vector3d;

public class TriangularGrid2dPositionProvider extends PositionProvider {
   private static final double Y = 0.0;
   private static final double SPACING = 1.0;
   private static final double HALF_SPACING = 0.5;
   private static final double X_HEIGHT = Math.sqrt(0.75);
   private static final double X_HEIGHT_INVERSE = 1.0 / X_HEIGHT;
   @Nonnull
   private final Vector3d rPosition = new Vector3d();
   @Nonnull
   private final Control rControl = new Control();

   @Override
   public void generate(@NonNullDecl PositionProvider.Context context) {
      if (!(context.bounds.min.y > 0.0) && !(context.bounds.max.y <= 0.0)) {
         int minGridX = toCellGridInclusive(context.bounds.min.x);
         int maxGridX = toCellGridExclusive(context.bounds.max.x);
         this.rControl.reset();

         for (int x = minGridX; x <= maxGridX; x++) {
            double zStart = firstGridZ(x, context.bounds.min.z);
            double zEnd = lastGridZ(x, context.bounds.max.z);

            for (double z = zStart; z <= zEnd; z++) {
               this.rPosition.set(x * X_HEIGHT, 0.0, z);
               if (this.rControl.stop) {
                  return;
               }

               assert context.bounds.contains(this.rPosition);

               context.pipe.accept(this.rPosition, this.rControl);
            }
         }
      }
   }

   private static int toCellGridInclusive(double minInclusive) {
      int gridX = (int)Math.floor(Math.nextUp(minInclusive) * X_HEIGHT_INVERSE);
      if (gridX * X_HEIGHT < minInclusive) {
         gridX++;
      }

      return gridX;
   }

   private static int toCellGridExclusive(double maxExclusive) {
      return (int)Math.floor(Math.nextDown(maxExclusive) * X_HEIGHT_INVERSE);
   }

   private static double firstGridZ(int gridX, double minInclusive) {
      if (gridX % 2 == 0) {
         int k = (int)Math.floor(Math.nextUp(minInclusive - 0.5));
         if (k + 0.5 < minInclusive) {
            k++;
         }

         return k + 0.5;
      } else {
         int z = (int)Math.floor(Math.nextUp(minInclusive));
         if (z < minInclusive) {
            z++;
         }

         return z;
      }
   }

   private static double lastGridZ(int gridX, double maxExclusive) {
      double maxZ = Math.nextDown(maxExclusive);
      return gridX % 2 == 0 ? Math.floor(maxZ - 0.5) + 0.5 : Math.floor(maxZ);
   }
}
