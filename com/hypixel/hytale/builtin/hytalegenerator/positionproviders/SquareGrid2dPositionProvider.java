package com.hypixel.hytale.builtin.hytalegenerator.positionproviders;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3d;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Control;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class SquareGrid2dPositionProvider extends PositionProvider {
   private static final double Y = 0.0;
   @Nonnull
   private final Vector3d rPosition = new Vector3d();
   @Nonnull
   private final Bounds3d rGridBounds = new Bounds3d();
   @Nonnull
   private final Control rControl = new Control();

   @Override
   public void generate(@NonNullDecl PositionProvider.Context context) {
      if (!(context.bounds.min.y > 0.0) && !(context.bounds.max.y <= 0.0)) {
         this.rGridBounds.min.assign(Math.floor(context.bounds.min.x), 0.0, Math.floor(context.bounds.min.z));
         this.rGridBounds.max.assign(Math.ceil(context.bounds.max.x), 1.0, Math.ceil(context.bounds.max.z));
         if (this.rGridBounds.min.x < context.bounds.min.x) {
            this.rGridBounds.min.x++;
         }

         if (this.rGridBounds.min.z < context.bounds.min.z) {
            this.rGridBounds.min.z++;
         }

         this.rControl.reset();

         for (double x = this.rGridBounds.min.x; x < this.rGridBounds.max.x; x++) {
            for (double z = this.rGridBounds.min.z; z < this.rGridBounds.max.z; z++) {
               assert context.bounds.contains(x, 0.0, z);

               if (this.rControl.stop) {
                  return;
               }

               this.rPosition.assign(x, 0.0, z);
               context.pipe.accept(this.rPosition, this.rControl);
            }
         }
      }
   }
}
