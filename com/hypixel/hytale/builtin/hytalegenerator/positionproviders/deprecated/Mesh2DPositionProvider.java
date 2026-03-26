package com.hypixel.hytale.builtin.hytalegenerator.positionproviders.deprecated;

import com.hypixel.hytale.builtin.hytalegenerator.noise.pointprovider.PointProvider;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Control;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.math.vector.Vector2d;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;

public class Mesh2DPositionProvider extends PositionProvider {
   @Nonnull
   private final PointProvider pointGenerator;
   private final int y;

   public Mesh2DPositionProvider(@Nonnull PointProvider positionProvider, int y) {
      this.pointGenerator = positionProvider;
      this.y = y;
   }

   @Override
   public void generate(@Nonnull PositionProvider.Context context) {
      if (!(context.bounds.min.y > this.y) && !(context.bounds.max.y <= this.y)) {
         Vector2d min2d = new Vector2d(context.bounds.min.x, context.bounds.min.z);
         Vector2d max2d = new Vector2d(context.bounds.max.x, context.bounds.max.z);
         Control control = new Control();
         this.pointGenerator.points2d(min2d, max2d, point -> context.pipe.accept(new Vector3d(point.x, this.y, point.y), control));
      }
   }
}
