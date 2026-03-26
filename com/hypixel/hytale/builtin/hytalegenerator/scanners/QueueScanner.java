package com.hypixel.hytale.builtin.hytalegenerator.scanners;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Pipe;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class QueueScanner extends Scanner {
   @Nonnull
   private final List<Scanner> scanners;
   @Nonnull
   private final Bounds3i bounds;

   public QueueScanner(@Nonnull List<Scanner> scanners) {
      this.scanners = new ArrayList<>(scanners);
      this.bounds = new Bounds3i();

      for (Scanner scanner : scanners) {
         this.bounds.encompass(scanner.getBounds_voxelGrid());
      }
   }

   @Override
   public void scan(@NonNullDecl Scanner.Context context) {
   }

   @Override
   public void scan(@NonNullDecl Vector3i anchor, @NonNullDecl Pipe.One<Vector3i> pipe) {
      for (Scanner scanner : this.scanners) {
         scanner.scan(anchor, pipe);
      }
   }

   @Override
   public Bounds3i getBounds_voxelGrid() {
      return this.bounds;
   }
}
