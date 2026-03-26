package com.hypixel.hytale.builtin.hytalegenerator.scanners;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.ConstantPattern;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Pipe;
import com.hypixel.hytale.builtin.hytalegenerator.voxelspace.NullSpace;
import com.hypixel.hytale.builtin.hytalegenerator.voxelspace.VoxelSpace;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public abstract class Scanner {
   @Deprecated
   public abstract void scan(@Nonnull Scanner.Context var1);

   public abstract void scan(@Nonnull Vector3i var1, @Nonnull Pipe.One<Vector3i> var2);

   public abstract Bounds3i getBounds_voxelGrid();

   public Bounds3i getBoundsWithPattern_voxelGrid(@Nonnull Pattern pattern) {
      return this.getBounds_voxelGrid().clone().stack(pattern.getBounds_voxelGrid());
   }

   public static class Context {
      @Nonnull
      public Vector3i position;
      @Nonnull
      public Pattern pattern;
      @Nonnull
      public VoxelSpace<Material> materialSpace;
      @Nonnull
      public List<Vector3i> validPositions_out;

      public Context() {
         this.position = new Vector3i();
         this.pattern = ConstantPattern.INSTANCE_FALSE;
         this.materialSpace = NullSpace.instance();
         this.validPositions_out = new ArrayList<>();
      }

      public Context(
         @Nonnull Vector3i position, @Nonnull Pattern pattern, @Nonnull VoxelSpace<Material> materialSpace, @Nonnull List<Vector3i> validPositions_out
      ) {
         this.position = position;
         this.pattern = pattern;
         this.materialSpace = materialSpace;
         this.validPositions_out = validPositions_out;
      }

      public Context(@Nonnull Scanner.Context other) {
         this.position = other.position;
         this.pattern = other.pattern;
         this.materialSpace = other.materialSpace;
         this.validPositions_out = other.validPositions_out;
      }
   }
}
