package com.hypixel.hytale.builtin.hytalegenerator.positionproviders;

import com.hypixel.hytale.math.vector.Vector3d;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class PositionProvider {
   public abstract void positionsIn(@Nonnull PositionProvider.Context var1);

   @Nonnull
   public static PositionProvider noPositionProvider() {
      return new PositionProvider() {
         @Override
         public void positionsIn(@Nonnull PositionProvider.Context context) {
         }
      };
   }

   public static class Context {
      @Nonnull
      public static final Consumer<Vector3d> EMPTY_CONSUMER = p -> {};
      public Vector3d minInclusive;
      public Vector3d maxExclusive;
      public Consumer<Vector3d> consumer;
      @Nullable
      public Vector3d anchor;

      public Context() {
         this.minInclusive = Vector3d.ZERO;
         this.maxExclusive = Vector3d.ZERO;
         this.consumer = EMPTY_CONSUMER;
         this.anchor = null;
      }

      public Context(@Nonnull Vector3d minInclusive, @Nonnull Vector3d maxExclusive, @Nonnull Consumer<Vector3d> consumer, @Nullable Vector3d anchor) {
         this.minInclusive = minInclusive;
         this.maxExclusive = maxExclusive;
         this.consumer = consumer;
         this.anchor = anchor;
      }

      public Context(@Nonnull PositionProvider.Context other) {
         this.minInclusive = other.minInclusive;
         this.maxExclusive = other.maxExclusive;
         this.consumer = other.consumer;
         this.anchor = other.anchor;
      }

      public void assign(@Nonnull PositionProvider.Context other) {
         this.minInclusive = other.minInclusive;
         this.maxExclusive = other.maxExclusive;
         this.consumer = other.consumer;
         this.anchor = other.anchor;
      }
   }
}
