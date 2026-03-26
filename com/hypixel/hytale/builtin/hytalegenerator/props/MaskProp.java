package com.hypixel.hytale.builtin.hytalegenerator.props;

import com.hypixel.hytale.builtin.hytalegenerator.BlockMask;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.voxelspace.MaskVoxelSpace;
import com.hypixel.hytale.builtin.hytalegenerator.voxelspace.NullSpace;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class MaskProp extends Prop {
   @Nonnull
   private final Prop prop;
   @Nonnull
   private final MaskVoxelSpace maskVoxelSpace;
   @Nonnull
   private final Prop.Context rChildContext;

   public MaskProp(@Nonnull Prop prop, @Nonnull BlockMask mask) {
      this.prop = prop;
      this.maskVoxelSpace = new MaskVoxelSpace(mask, NullSpace.instance());
      this.rChildContext = new Prop.Context();
   }

   @Override
   public boolean generate(@NonNullDecl Prop.Context context) {
      this.maskVoxelSpace.setSource(context.materialWriteSpace);
      this.rChildContext.assign(context);
      this.rChildContext.materialWriteSpace = this.maskVoxelSpace;
      return this.prop.generate(this.rChildContext);
   }

   @NonNullDecl
   @Override
   public Bounds3i getReadBounds_voxelGrid() {
      return this.prop.getReadBounds_voxelGrid();
   }

   @NonNullDecl
   @Override
   public Bounds3i getWriteBounds_voxelGrid() {
      return this.prop.getWriteBounds_voxelGrid();
   }
}
