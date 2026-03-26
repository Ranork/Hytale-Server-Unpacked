package com.hypixel.hytale.builtin.hytalegenerator.scanners;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Control;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Pipe;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class DirectScanner extends Scanner {
   @Nonnull
   private static final Bounds3i BOUNDS = new Bounds3i(Vector3i.ZERO, Vector3i.ALL_ONES);
   @Nonnull
   private final Control rControl = new Control();

   @Override
   public void scan(@Nonnull Scanner.Context context) {
      Pattern.Context patternContext = new Pattern.Context(context.position, context.materialSpace);
      if (context.pattern.matches(patternContext)) {
         context.validPositions_out.add(context.position.clone());
      }
   }

   @Override
   public void scan(@NonNullDecl Vector3i position, @NonNullDecl Pipe.One<Vector3i> pipe) {
      this.rControl.stop = false;
      pipe.accept(position, this.rControl);
   }

   @Override
   public Bounds3i getBounds_voxelGrid() {
      return BOUNDS;
   }
}
