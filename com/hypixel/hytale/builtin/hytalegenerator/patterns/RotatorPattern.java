package com.hypixel.hytale.builtin.hytalegenerator.patterns;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.material.MaterialCache;
import com.hypixel.hytale.builtin.hytalegenerator.voxelspace.RotationVoxelSpace;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class RotatorPattern extends Pattern {
   @Nonnull
   private final Pattern pattern;
   @Nonnull
   private final Bounds3i bounds;
   @Nonnull
   private final RotationVoxelSpace readRotationVoxelSpace;
   @Nonnull
   private final Pattern.Context rChildContext;

   public RotatorPattern(@Nonnull Pattern pattern, @Nonnull RotationTuple rotation, @Nonnull MaterialCache materialCache) {
      this.pattern = pattern;
      this.bounds = pattern.getBounds_voxelGrid().clone().applyRotationAroundVoxel(rotation, Vector3i.ZERO);
      this.readRotationVoxelSpace = new RotationVoxelSpace(rotation, materialCache);
      this.rChildContext = new Pattern.Context();
   }

   @Override
   public boolean matches(@NonNullDecl Pattern.Context context) {
      this.readRotationVoxelSpace.setSource(context.materialSpace, context.position);
      this.rChildContext.assign(context);
      this.rChildContext.materialSpace = this.readRotationVoxelSpace;
      return this.pattern.matches(this.rChildContext);
   }

   @NonNullDecl
   @Override
   public Bounds3i getBounds_voxelGrid() {
      return this.bounds;
   }
}
