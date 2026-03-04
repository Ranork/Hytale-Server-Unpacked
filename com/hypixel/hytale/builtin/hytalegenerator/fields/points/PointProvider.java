package com.hypixel.hytale.builtin.hytalegenerator.fields.points;

import com.hypixel.hytale.math.vector.Vector2d;
import com.hypixel.hytale.math.vector.Vector2i;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

public interface PointProvider {
   List<Vector3i> points3i(@Nonnull Vector3i var1, @Nonnull Vector3i var2);

   List<Vector2i> points2i(@Nonnull Vector2i var1, @Nonnull Vector2i var2);

   List<Integer> points1i(int var1, int var2);

   void points3i(@Nonnull Vector3i var1, @Nonnull Vector3i var2, @Nonnull Consumer<Vector3i> var3);

   void points2i(@Nonnull Vector2i var1, @Nonnull Vector2i var2, @Nonnull Consumer<Vector2i> var3);

   void points1i(int var1, int var2, @Nonnull Consumer<Integer> var3);

   List<Vector3d> points3d(@Nonnull Vector3d var1, @Nonnull Vector3d var2);

   List<Vector2d> points2d(@Nonnull Vector2d var1, @Nonnull Vector2d var2);

   List<Double> points1d(double var1, double var3);

   void points3d(@Nonnull Vector3d var1, @Nonnull Vector3d var2, @Nonnull Consumer<Vector3d> var3);

   void points2d(@Nonnull Vector2d var1, @Nonnull Vector2d var2, @Nonnull Consumer<Vector2d> var3);

   void points1d(double var1, double var3, @Nonnull Consumer<Double> var5);
}
