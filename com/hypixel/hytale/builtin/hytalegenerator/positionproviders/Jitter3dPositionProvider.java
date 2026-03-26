package com.hypixel.hytale.builtin.hytalegenerator.positionproviders;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3d;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Control;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Pipe;
import com.hypixel.hytale.builtin.hytalegenerator.rng.RngField;
import com.hypixel.hytale.math.util.FastRandom;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class Jitter3dPositionProvider extends PositionProvider {
   private static final float PI = (float) Math.PI;
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
         Jitter3dPositionProvider.this.random.setSeed(Jitter3dPositionProvider.this.rngField.get(position.x, position.y, position.z));
         double radius = Jitter3dPositionProvider.this.magnitude * Math.sqrt(Jitter3dPositionProvider.this.random.nextDouble());
         float rotationX = Jitter3dPositionProvider.this.random.nextFloat() * 2.0F * (float) Math.PI;
         float rotationY = Jitter3dPositionProvider.this.random.nextFloat() * 2.0F * (float) Math.PI;
         float rotationZ = Jitter3dPositionProvider.this.random.nextFloat() * 2.0F * (float) Math.PI;
         Jitter3dPositionProvider.this.rVector.assign(radius, 0.0, 0.0);
         Jitter3dPositionProvider.this.rVector.rotateX(rotationX);
         Jitter3dPositionProvider.this.rVector.rotateY(rotationY);
         Jitter3dPositionProvider.this.rVector.rotateZ(rotationZ);
         position.add(Jitter3dPositionProvider.this.rVector);
         if (Jitter3dPositionProvider.this.rContext.bounds.contains(position)) {
            Jitter3dPositionProvider.this.rContext.pipe.accept(position, control);
         }
      }
   };

   public Jitter3dPositionProvider(double magnitude, int seed, @Nonnull PositionProvider positionProvider) {
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
      this.rBounds.min.add(-this.magnitude);
      this.rBounds.max.add(this.magnitude);
      this.rChildContext.assign(context);
      this.rChildContext.bounds = this.rBounds;
      this.rChildContext.pipe = this.rChildPipe;
      this.positionProvider.generate(this.rChildContext);
   }
}
