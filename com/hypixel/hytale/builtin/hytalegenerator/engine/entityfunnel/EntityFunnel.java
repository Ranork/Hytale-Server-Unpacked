package com.hypixel.hytale.builtin.hytalegenerator.engine.entityfunnel;

import com.hypixel.hytale.builtin.hytalegenerator.EntityPlacementData;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public interface EntityFunnel {
   EntityFunnel NULL = new EntityFunnel() {
      @Override
      public void addEntity(@NonNullDecl EntityPlacementData entityPlacementData) {
      }

      @NonNullDecl
      @Override
      public Bounds3i getBounds() {
         return Bounds3i.ZERO;
      }
   };

   void addEntity(@Nonnull EntityPlacementData var1);

   @Nonnull
   Bounds3i getBounds();
}
