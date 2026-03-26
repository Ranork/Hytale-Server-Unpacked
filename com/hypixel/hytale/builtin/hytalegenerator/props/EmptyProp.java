package com.hypixel.hytale.builtin.hytalegenerator.props;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class EmptyProp extends Prop {
   public static final EmptyProp INSTANCE = new EmptyProp();

   @Override
   public boolean generate(@NonNullDecl Prop.Context context) {
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
      return Bounds3i.ZERO;
   }
}
