package com.hypixel.hytale.builtin.hytalegenerator.scanners;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Pipe;
import com.hypixel.hytale.math.vector.Vector3i;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class EmptyScanner extends Scanner {
   public static final EmptyScanner INSTANCE = new EmptyScanner();

   private EmptyScanner() {
   }

   @Override
   public void scan(@NonNullDecl Scanner.Context context) {
   }

   @Override
   public void scan(@NonNullDecl Vector3i position, @NonNullDecl Pipe.One<Vector3i> pipe) {
   }

   @Override
   public Bounds3i getBounds_voxelGrid() {
      return Bounds3i.ZERO;
   }
}
