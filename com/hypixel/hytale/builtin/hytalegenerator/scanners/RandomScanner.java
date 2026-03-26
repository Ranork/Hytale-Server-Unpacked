package com.hypixel.hytale.builtin.hytalegenerator.scanners;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.delimiters.RangeInt;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Control;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Pipe;
import com.hypixel.hytale.builtin.hytalegenerator.rng.RngField;
import com.hypixel.hytale.math.Axis;
import com.hypixel.hytale.math.util.FastRandom;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class RandomScanner extends Scanner {
   @Nonnull
   private final Axis axis;
   @Nonnull
   private final RangeInt range;
   @Nonnull
   private final Scanner childScanner;
   @Nonnull
   private final Bounds3i bounds;
   @Nonnull
   private final RngField rngField;
   @Nonnull
   private final FastRandom random;
   private final int attempts;
   @Nonnull
   private final Control rControl;
   @Nonnull
   private final Vector3i rPosition;
   @Nonnull
   private Pipe.One<Vector3i> rContextPipe;
   @Nonnull
   private final Pipe.One<Vector3i> rChildPipe = new Pipe.One<Vector3i>() {
      public void accept(@NonNullDecl Vector3i position, @NonNullDecl Control control) {
         RandomScanner.this.rContextPipe.accept(position, control);
         if (control.stop) {
            RandomScanner.this.rControl.stop = true;
         }
      }
   };

   public RandomScanner(@Nonnull Axis axis, @Nonnull RangeInt range, @Nonnull Scanner childScanner, int seed) {
      this.axis = axis;
      this.range = range;
      this.childScanner = childScanner;
      this.rngField = new RngField(seed);
      this.attempts = range.getMaxExclusive() - range.getMinInclusive();
      this.random = new FastRandom();
      switch (axis) {
         case X:
            this.bounds = new Bounds3i(new Vector3i(range.getMinInclusive(), 0, 0), new Vector3i(range.getMaxExclusive(), 1, 1));
            break;
         case Y:
            this.bounds = new Bounds3i(new Vector3i(0, range.getMinInclusive(), 0), new Vector3i(1, range.getMaxExclusive(), 1));
            break;
         case Z:
            this.bounds = new Bounds3i(new Vector3i(0, 0, range.getMinInclusive()), new Vector3i(1, 1, range.getMaxExclusive()));
            break;
         default:
            this.bounds = new Bounds3i();
      }

      this.bounds.stack(childScanner.getBounds_voxelGrid());
      this.rControl = new Control();
      this.rPosition = new Vector3i();
      this.rContextPipe = Pipe.getEmptyOne();
   }

   @Override
   public void scan(@NonNullDecl Scanner.Context context) {
   }

   @Override
   public void scan(@NonNullDecl Vector3i anchor, @NonNullDecl Pipe.One<Vector3i> pipe) {
      this.rContextPipe = pipe;
      this.rPosition.assign(anchor);
      this.rControl.reset();
      this.random.setSeed(this.rngField.get(anchor.x, anchor.y, anchor.z));

      for (int i = 0; i < this.attempts; i++) {
         if (this.rControl.stop) {
            return;
         }

         int rollResult = this.random.nextInt(this.attempts) + this.range.getMinInclusive();
         switch (this.axis) {
            case X:
               this.rPosition.x = rollResult + anchor.x;
               break;
            case Y:
               this.rPosition.y = rollResult + anchor.y;
               break;
            case Z:
               this.rPosition.z = rollResult + anchor.z;
         }

         this.childScanner.scan(this.rPosition, this.rChildPipe);
      }
   }

   @Override
   public Bounds3i getBounds_voxelGrid() {
      return this.bounds;
   }
}
