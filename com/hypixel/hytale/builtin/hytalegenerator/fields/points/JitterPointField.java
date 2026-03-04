package com.hypixel.hytale.builtin.hytalegenerator.fields.points;

import com.hypixel.hytale.builtin.hytalegenerator.VectorUtil;
import com.hypixel.hytale.builtin.hytalegenerator.fields.FastNoiseLite;
import com.hypixel.hytale.math.vector.Vector2d;
import com.hypixel.hytale.math.vector.Vector2i;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

public class JitterPointField extends PointField {
   @Nonnull
   private final FastNoiseLite noise;
   private final int seed;
   private final double jitter;
   @Nonnull
   private final Vector3d scaleDown3d;
   @Nonnull
   private final Vector3d scaleUp3d;
   @Nonnull
   private final Vector2d scaleDown2d;
   @Nonnull
   private final Vector2d scaleUp2d;

   public JitterPointField(int seed, double jitter) {
      this.jitter = jitter;
      this.seed = seed;
      this.noise = new FastNoiseLite();
      this.scaleDown3d = new Vector3d(1.0, 1.0, 1.0);
      this.scaleUp3d = new Vector3d(1.0, 1.0, 1.0);
      this.scaleDown2d = new Vector2d(1.0, 1.0);
      this.scaleUp2d = new Vector2d(1.0, 1.0);
   }

   @Override
   public PointField setScale(double scaleX, double scaleY, double scaleZ, double scaleW) {
      this.scaleDown3d.x = 1.0 / scaleX;
      this.scaleDown3d.y = 1.0 / scaleY;
      this.scaleDown3d.z = 1.0 / scaleZ;
      this.scaleUp3d.x = scaleX;
      this.scaleUp3d.y = scaleY;
      this.scaleUp3d.z = scaleZ;
      this.scaleDown2d.x = 1.0 / scaleX;
      this.scaleDown2d.y = 1.0 / scaleZ;
      this.scaleUp2d.x = scaleX;
      this.scaleUp2d.y = scaleZ;
      return super.setScale(scaleX, scaleY, scaleZ, scaleW);
   }

   @Override
   public void points3i(@Nonnull Vector3i min, @Nonnull Vector3i max, @Nonnull Consumer<Vector3i> pointsOut) {
      this.points3d(min.toVector3d(), max.toVector3d(), p -> pointsOut.accept(p.toVector3i()));
   }

   @Override
   public void points2i(@Nonnull Vector2i min, @Nonnull Vector2i max, @Nonnull Consumer<Vector2i> pointsOut) {
      this.points2d(new Vector2d(min.x, min.y), new Vector2d(max.x, max.y), p -> pointsOut.accept(new Vector2i((int)p.x, (int)p.y)));
   }

   @Override
   public void points1i(int min, int max, @Nonnull Consumer<Integer> pointsOut) {
      this.points1d(min, max, p -> pointsOut.accept(FastNoiseLite.fastRound(p)));
   }

   @Override
   public void points3d(@Nonnull Vector3d min, @Nonnull Vector3d max, @Nonnull Consumer<Vector3d> pointsOut) {
      Vector3d cellMin = min.clone().scale(this.scaleDown3d);
      Vector3d cellMax = max.clone().scale(this.scaleDown3d);
      cellMin.x = FastNoiseLite.fastRound(cellMin.x);
      cellMin.y = FastNoiseLite.fastRound(cellMin.y);
      cellMin.z = FastNoiseLite.fastRound(cellMin.z);
      cellMax.x = FastNoiseLite.fastRound(cellMax.x);
      cellMax.y = FastNoiseLite.fastRound(cellMax.y);
      cellMax.z = FastNoiseLite.fastRound(cellMax.z);

      for (double x = cellMin.x; x <= cellMax.x + 0.25; x++) {
         for (double y = cellMin.y; y <= cellMax.y + 0.25; y++) {
            for (double z = cellMin.z; z <= cellMax.z + 0.25; z++) {
               Vector3d point = this.noise.pointFor(this.seed, this.jitter, x, y, z);
               point.scale(this.scaleUp3d);
               if (VectorUtil.isInside(point, min, max)) {
                  pointsOut.accept(point);
               }
            }
         }
      }
   }

   @Override
   public void points2d(@Nonnull Vector2d min, @Nonnull Vector2d max, @Nonnull Consumer<Vector2d> pointsOut) {
      Vector2d cellMin = min.clone().scale(this.scaleDown2d);
      Vector2d cellMax = max.clone().scale(this.scaleDown2d);
      cellMin.x = FastNoiseLite.fastRound(cellMin.x);
      cellMin.y = FastNoiseLite.fastRound(cellMin.y);
      cellMax.x = FastNoiseLite.fastRound(cellMax.x);
      cellMax.y = FastNoiseLite.fastRound(cellMax.y);

      for (double x = cellMin.x; x <= cellMax.x + 0.25; x++) {
         for (double z = cellMin.y; z <= cellMax.y + 0.25; z++) {
            Vector2d point = this.noise.pointFor(this.seed, this.jitter, x, z);
            point.scale(this.scaleUp2d);
            if (VectorUtil.isInside(point, min, max)) {
               pointsOut.accept(point);
            }
         }
      }
   }

   @Override
   public void points1d(double min, double max, @Nonnull Consumer<Double> pointsOut) {
      for (double x = min - this.scaleX; x < max + this.scaleX; x += this.scaleX) {
         double point = this.noise.pointFor(this.seed, this.jitter, x);
         if (!(point < min) || point < max) {
            pointsOut.accept(point);
         }
      }
   }
}
