package com.hypixel.hytale.builtin.hytalegenerator.scanners;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Control;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Pipe;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class RadialScanner extends Scanner {
   @Nonnull
   private final Bounds3i bounds;
   @Nonnull
   private final Scanner childScanner;
   @Nonnull
   private final byte[] sortedPositions;
   private final int positionsCount;
   @Nonnull
   private final Control rControl;
   @Nonnull
   private final Vector3i rPosition;
   @Nonnull
   private Pipe.One<Vector3i> rContextPipe;
   @Nonnull
   private final Pipe.One<Vector3i> rChildPipe = new Pipe.One<Vector3i>() {
      public void accept(@NonNullDecl Vector3i position, @NonNullDecl Control control) {
         RadialScanner.this.rContextPipe.accept(position, control);
         if (control.stop) {
            RadialScanner.this.rControl.stop = true;
         }
      }
   };

   public RadialScanner(@Nonnull Bounds3i bounds, @Nonnull Scanner childScanner) {
      this.bounds = bounds.clone();
      this.childScanner = childScanner;
      Vector3i size = bounds.getSize();
      this.positionsCount = size.x * size.y * size.z;
      List<Vector3i> sortedPositions = new ArrayList<>(this.positionsCount);

      for (Vector3i position = bounds.min.clone(); position.x < bounds.max.x; position.x++) {
         for (position.y = bounds.min.y; position.y < bounds.max.y; position.y++) {
            for (position.z = bounds.min.z; position.z < bounds.max.z; position.z++) {
               sortedPositions.add(position.clone());
            }
         }
      }

      sortedPositions.sort(Comparator.comparingDouble(Vector3i::length));
      this.sortedPositions = new byte[this.positionsCount * 3];

      for (int i = 0; i < this.positionsCount; i++) {
         Vector3i position = sortedPositions.get(i);
         this.sortedPositions[indexX(i)] = (byte)position.x;
         this.sortedPositions[indexY(i)] = (byte)position.y;
         this.sortedPositions[indexZ(i)] = (byte)position.z;
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
      this.rControl.reset();

      for (int i = 0; i < this.positionsCount; i++) {
         if (this.rControl.stop) {
            return;
         }

         int x = this.sortedPositions[indexX(i)];
         int y = this.sortedPositions[indexY(i)];
         int z = this.sortedPositions[indexZ(i)];
         this.rPosition.assign(x, y, z);
         this.rPosition.add(anchor);
         this.childScanner.scan(this.rPosition, this.rChildPipe);
      }
   }

   @Override
   public Bounds3i getBounds_voxelGrid() {
      return this.bounds;
   }

   private static int indexX(int i) {
      return i * 3;
   }

   private static int indexY(int i) {
      return i * 3 + 1;
   }

   private static int indexZ(int i) {
      return i * 3 + 2;
   }
}
