package com.hypixel.hytale.builtin.hytalegenerator.patterns;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.SpaceSize;
import com.hypixel.hytale.builtin.hytalegenerator.datastructures.voxelspace.NullSpace;
import com.hypixel.hytale.builtin.hytalegenerator.datastructures.voxelspace.VoxelSpace;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class Pattern {
   public abstract boolean matches(@Nonnull Pattern.Context var1);

   public abstract SpaceSize readSpace();

   @Nonnull
   public static Pattern noPattern() {
      final SpaceSize space = new SpaceSize(new Vector3i(0, 0, 0), new Vector3i(0, 0, 0));
      return new Pattern() {
         @Override
         public boolean matches(@Nonnull Pattern.Context context) {
            return false;
         }

         @Nonnull
         @Override
         public SpaceSize readSpace() {
            return space;
         }
      };
   }

   @Nonnull
   public static Pattern yesPattern() {
      final SpaceSize space = new SpaceSize(new Vector3i(0, 0, 0), new Vector3i(0, 0, 0));
      return new Pattern() {
         @Override
         public boolean matches(@Nonnull Pattern.Context context) {
            return true;
         }

         @Nonnull
         @Override
         public SpaceSize readSpace() {
            return space;
         }
      };
   }

   public static class Context {
      @Nonnull
      public Vector3i position;
      @Nonnull
      public VoxelSpace<Material> materialSpace;

      public Context() {
         this.position = new Vector3i();
         this.materialSpace = NullSpace.instance();
      }

      public Context(@Nonnull Vector3i position, @Nullable VoxelSpace<Material> materialSpace) {
         this.position = position;
         this.materialSpace = materialSpace;
      }

      public Context(@Nonnull Pattern.Context other) {
         this.position = other.position;
         this.materialSpace = other.materialSpace;
      }

      public void assign(@Nonnull Pattern.Context other) {
         this.position = other.position;
         this.materialSpace = other.materialSpace;
      }
   }
}
