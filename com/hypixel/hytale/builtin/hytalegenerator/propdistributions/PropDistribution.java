package com.hypixel.hytale.builtin.hytalegenerator.propdistributions;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3d;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Pipe;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.math.vector.Vector3d;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

public abstract class PropDistribution {
   public abstract void distribute(@Nonnull PropDistribution.Context var1);

   public abstract void forEachPossibleProp(@Nonnull Consumer<Prop> var1);

   public static class Context {
      @Nonnull
      public Bounds3d bounds;
      @Nonnull
      public Pipe.Two<Vector3d, Prop> pipe;
      public double distanceFromBiomeEdge;

      public Context() {
         this.bounds = new Bounds3d();
         this.pipe = (position, prop, control) -> {};
         this.distanceFromBiomeEdge = Double.MAX_VALUE;
      }

      public Context(@Nonnull Bounds3d bounds, @Nonnull Pipe.Two<Vector3d, Prop> pipe, double distanceFromBiomeEdge) {
         this.bounds = bounds;
         this.pipe = pipe;
         this.distanceFromBiomeEdge = distanceFromBiomeEdge;
      }

      public void assign(@Nonnull PropDistribution.Context context) {
         this.bounds = context.bounds;
         this.pipe = context.pipe;
         this.distanceFromBiomeEdge = context.distanceFromBiomeEdge;
      }
   }
}
