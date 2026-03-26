package com.hypixel.hytale.builtin.hytalegenerator.patterns;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class OffsetPattern extends Pattern {
   @Nonnull
   private final Pattern pattern;
   @Nonnull
   private final Vector3i offset;
   @Nonnull
   private final Bounds3i bounds_voxelGrid;
   @Nonnull
   private final Vector3i rChildPosition;
   @Nonnull
   private final Pattern.Context rChildContext;

   public OffsetPattern(@Nonnull Pattern pattern, @Nonnull Vector3i offset) {
      this.pattern = pattern;
      this.offset = offset;
      this.bounds_voxelGrid = pattern.getBounds_voxelGrid().clone().offset(offset);
      this.rChildPosition = new Vector3i();
      this.rChildContext = new Pattern.Context();
   }

   @Override
   public boolean matches(@Nonnull Pattern.Context context) {
      this.rChildPosition.assign(context.position).add(this.offset);
      this.rChildContext.assign(context);
      this.rChildContext.position = this.rChildPosition;
      return this.pattern.matches(this.rChildContext);
   }

   @NonNullDecl
   @Override
   public Bounds3i getBounds_voxelGrid() {
      return this.bounds_voxelGrid;
   }
}
