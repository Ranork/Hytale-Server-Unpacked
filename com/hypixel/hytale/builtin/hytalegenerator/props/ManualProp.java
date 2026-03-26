package com.hypixel.hytale.builtin.hytalegenerator.props;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class ManualProp extends Prop {
   @Nonnull
   private final List<ManualProp.Block> blocks;
   @Nonnull
   private final Bounds3i bounds = new Bounds3i();
   @Nonnull
   private final Vector3i rPosition;

   public ManualProp(@Nonnull List<ManualProp.Block> blocks) {
      this.blocks = new ArrayList<>(blocks.size());

      for (ManualProp.Block block : blocks) {
         this.blocks.add(block.clone());
         this.bounds.encompass(block.position);
      }

      this.rPosition = new Vector3i();
   }

   @Override
   public boolean generate(@Nonnull Prop.Context context) {
      Bounds3i bounds = context.materialWriteSpace.getBounds();

      for (ManualProp.Block block : this.blocks) {
         this.rPosition.assign(block.position);
         this.rPosition.add(context.position);
         if (bounds.contains(this.rPosition)) {
            context.materialWriteSpace.set(block.material, this.rPosition);
         }
      }

      return true;
   }

   @NonNullDecl
   @Override
   public Bounds3i getReadBounds_voxelGrid() {
      return Bounds3i.ZERO;
   }

   @NonNullDecl
   @Override
   public Bounds3i getWriteBounds_voxelGrid() {
      return this.bounds;
   }

   public static class Block {
      public final Material material;
      public final Vector3i position;

      public Block(@Nonnull Material material, @Nonnull Vector3i position) {
         this.material = material;
         this.position = position.clone();
      }

      @Nonnull
      public ManualProp.Block clone() {
         return new ManualProp.Block(this.material, this.position);
      }
   }
}
