package com.hypixel.hytale.builtin.hytalegenerator.assets.pointgenerators;

import com.hypixel.hytale.builtin.hytalegenerator.fields.points.PointProvider;
import com.hypixel.hytale.builtin.hytalegenerator.seed.SeedBox;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector2d;
import com.hypixel.hytale.math.vector.Vector2i;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

public class NoPointGeneratorAsset extends PointGeneratorAsset {
   @Nonnull
   public static final BuilderCodec<NoPointGeneratorAsset> CODEC = BuilderCodec.builder(
         NoPointGeneratorAsset.class, NoPointGeneratorAsset::new, PointGeneratorAsset.ABSTRACT_CODEC
      )
      .build();

   @Nonnull
   @Override
   public PointProvider build(@Nonnull SeedBox parentSeed) {
      return new PointProvider() {
         @Nonnull
         @Override
         public List<Vector3i> points3i(@Nonnull Vector3i min, @Nonnull Vector3i max) {
            return List.of();
         }

         @Nonnull
         @Override
         public List<Vector2i> points2i(@Nonnull Vector2i min, @Nonnull Vector2i max) {
            return List.of();
         }

         @Nonnull
         @Override
         public List<Integer> points1i(int min, int max) {
            return List.of();
         }

         @Override
         public void points3i(@Nonnull Vector3i min, @Nonnull Vector3i max, @Nonnull Consumer<Vector3i> pointsOut) {
         }

         @Override
         public void points2i(@Nonnull Vector2i min, @Nonnull Vector2i max, @Nonnull Consumer<Vector2i> pointsOut) {
         }

         @Override
         public void points1i(int min, int max, @Nonnull Consumer<Integer> pointsOut) {
         }

         @Nonnull
         @Override
         public List<Vector3d> points3d(@Nonnull Vector3d min, @Nonnull Vector3d max) {
            return List.of();
         }

         @Nonnull
         @Override
         public List<Vector2d> points2d(@Nonnull Vector2d min, @Nonnull Vector2d max) {
            return List.of();
         }

         @Nonnull
         @Override
         public List<Double> points1d(double min, double max) {
            return List.of();
         }

         @Override
         public void points3d(@Nonnull Vector3d min, @Nonnull Vector3d max, @Nonnull Consumer<Vector3d> pointsOut) {
         }

         @Override
         public void points2d(@Nonnull Vector2d min, @Nonnull Vector2d max, @Nonnull Consumer<Vector2d> pointsOut) {
         }

         @Override
         public void points1d(double min, double max, @Nonnull Consumer<Double> pointsOut) {
         }
      };
   }
}
