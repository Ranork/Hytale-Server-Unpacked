package com.hypixel.hytale.builtin.hytalegenerator.patterns;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class ConstantPattern extends Pattern {
   public static final ConstantPattern INSTANCE_TRUE = new ConstantPattern(true);
   public static final ConstantPattern INSTANCE_FALSE = new ConstantPattern(false);
   private final boolean value;

   private ConstantPattern(boolean value) {
      this.value = value;
   }

   @Override
   public boolean matches(@NonNullDecl Pattern.Context context) {
      return this.value;
   }

   @NonNullDecl
   @Override
   public Bounds3i getBounds_voxelGrid() {
      return Bounds3i.ZERO;
   }
}
