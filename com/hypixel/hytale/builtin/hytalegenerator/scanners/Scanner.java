package com.hypixel.hytale.builtin.hytalegenerator.scanners;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.SpaceSize;
import com.hypixel.hytale.builtin.hytalegenerator.datastructures.voxelspace.VoxelSpace;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.builtin.hytalegenerator.threadindexer.WorkerIndexer;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

public abstract class Scanner {
   public abstract List<Vector3i> scan(@Nonnull Scanner.Context var1);

   public abstract SpaceSize scanSpace();

   @Nonnull
   public SpaceSize readSpaceWith(@Nonnull Pattern pattern) {
      return SpaceSize.stack(pattern.readSpace(), this.scanSpace());
   }

   @Nonnull
   public static Scanner noScanner() {
      final SpaceSize space = new SpaceSize(new Vector3i(0, 0, 0), new Vector3i(0, 0, 0));
      return new Scanner() {
         @Nonnull
         @Override
         public List<Vector3i> scan(@Nonnull Scanner.Context context) {
            return Collections.emptyList();
         }

         @Nonnull
         @Override
         public SpaceSize scanSpace() {
            return space;
         }
      };
   }

   public static class Context {
      public Vector3i position;
      public Pattern pattern;
      public VoxelSpace<Material> materialSpace;
      public WorkerIndexer.Id workerId;

      public Context(@Nonnull Vector3i position, @Nonnull Pattern pattern, @Nonnull VoxelSpace<Material> materialSpace, @Nonnull WorkerIndexer.Id workerId) {
         this.position = position;
         this.pattern = pattern;
         this.materialSpace = materialSpace;
         this.workerId = workerId;
      }

      public Context(@Nonnull Scanner.Context other) {
         this.position = other.position;
         this.pattern = other.pattern;
         this.materialSpace = other.materialSpace;
         this.workerId = other.workerId;
      }
   }
}
