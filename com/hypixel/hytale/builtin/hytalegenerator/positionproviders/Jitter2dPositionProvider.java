package com.hypixel.hytale.builtin.hytalegenerator.positionproviders;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3d;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Control;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Pipe;
import com.hypixel.hytale.builtin.hytalegenerator.rng.RngField;
import com.hypixel.hytale.math.util.FastRandom;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class Jitter2dPositionProvider extends PositionProvider {
   private static final double SEED_GENERATOR_RESOLUTION = 10.0;
   private final double magnitude;
   @Nonnull
   private final PositionProvider positionProvider;
   @Nonnull
   private final RngField rngField;
   @Nonnull
   private final FastRandom random;
   @Nonnull
   private final Vector3d rVector;
   @Nonnull
   private final Bounds3d rBounds;
   @Nonnull
   private final PositionProvider.Context rChildContext;
   @Nonnull
   private PositionProvider.Context rContext;
   @Nonnull
   private final Pipe.One<Vector3d> rChildPipe = new Pipe.One<Vector3d>() {
      public void accept(@NonNullDecl Vector3d position, @NonNullDecl Control control) {
         int localSeed = Jitter2dPositionProvider.this.rngField.get(position.x, position.y, position.z);
         Jitter2dPositionProvider.this.random.setSeed(localSeed);
         double radius = Jitter2dPositionProvider.this.magnitude * Math.sqrt(Jitter2dPositionProvider.this.random.nextDouble());
         double theta = Jitter2dPositionProvider.this.random.nextDouble() * 2.0 * Math.PI;
         Jitter2dPositionProvider.this.rVector.assign(radius * Math.cos(theta), 0.0, radius * Math.sin(theta));
         position.add(Jitter2dPositionProvider.this.rVector);
         if (Jitter2dPositionProvider.this.rContext.bounds.contains(position)) {
            Jitter2dPositionProvider.this.rContext.pipe.accept(position, control);
         }
      }
   };

   public Jitter2dPositionProvider(double magnitude, int seed, @Nonnull PositionProvider positionProvider) {
      this.magnitude = Math.abs(magnitude);
      this.positionProvider = positionProvider;
      this.rngField = new RngField(seed);
      this.random = new FastRandom();
      this.rVector = new Vector3d();
      this.rBounds = new Bounds3d();
      this.rChildContext = new PositionProvider.Context();
      this.rContext = new PositionProvider.Context();
   }

   @Override
   public void generate(@Nonnull PositionProvider.Context context) {
      this.rContext = context;
      this.rBounds.assign(context.bounds);
      this.rBounds.min.add(-this.magnitude, 0.0, -this.magnitude);
      this.rBounds.max.add(this.magnitude, 0.0, this.magnitude);
      this.rChildContext.assign(context);
      this.rChildContext.bounds = this.rBounds;
      this.rChildContext.pipe = this.rChildPipe;
      this.positionProvider.generate(this.rChildContext);
   }
}
