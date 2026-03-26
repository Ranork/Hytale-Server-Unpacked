package com.hypixel.hytale.builtin.hytalegenerator.positionproviders;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3d;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Pipe;
import com.hypixel.hytale.builtin.hytalegenerator.propdistributions.PropDistribution;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class PositionProvider {
   public abstract void generate(@Nonnull PositionProvider.Context var1);

   public static class Context {
      public Bounds3d bounds;
      public Pipe.One<Vector3d> pipe;
      @Nullable
      public Vector3d anchor;

      public Context() {
         this.bounds = new Bounds3d();
         this.pipe = Pipe.getEmptyOne();
         this.anchor = null;
      }

      public Context(@Nonnull Bounds3d bounds, @Nonnull Pipe.One<Vector3d> pipe, @Nullable Vector3d anchor) {
         this.bounds = bounds;
         this.pipe = pipe;
         this.anchor = anchor;
      }

      public Context(@Nonnull PositionProvider.Context other) {
         this.bounds = other.bounds;
         this.pipe = other.pipe;
         this.anchor = other.anchor;
      }

      public void assign(@Nonnull PositionProvider.Context other) {
         this.bounds = other.bounds;
         this.pipe = other.pipe;
         this.anchor = other.anchor;
      }

      public void assign(@Nonnull PropDistribution.Context other) {
         this.bounds.assign(other.bounds);
         this.pipe = Pipe.getEmptyOne();
         this.anchor = null;
      }
   }
}
