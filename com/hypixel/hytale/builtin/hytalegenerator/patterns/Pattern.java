package com.hypixel.hytale.builtin.hytalegenerator.patterns;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.voxelspace.NullSpace;
import com.hypixel.hytale.builtin.hytalegenerator.voxelspace.VoxelSpace;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3i;

public abstract class Pattern {
   public abstract boolean matches(@Nonnull Pattern.Context var1);

   @Nonnull
   public abstract Bounds3i getBounds_voxelGrid();

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

      public void assign(@Nonnull Prop.Context other) {
         this.position = other.position;
         this.materialSpace = other.materialReadSpace;
      }
   }
}
