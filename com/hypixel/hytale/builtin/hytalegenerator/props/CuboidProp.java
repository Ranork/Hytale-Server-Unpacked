package com.hypixel.hytale.builtin.hytalegenerator.props;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.MaterialProvider;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class CuboidProp extends Prop {
   @Nonnull
   private final Bounds3i bounds;
   @Nonnull
   private final MaterialProvider<Material> materialProvider;
   @Nonnull
   private final Bounds3i rIntersectingBounds;
   @Nonnull
   private final MaterialProvider.Context rContext;

   public CuboidProp(@Nonnull Bounds3i bounds, @Nonnull MaterialProvider<Material> materialProvider) {
      this.bounds = bounds.clone();
      this.materialProvider = materialProvider;
      this.rIntersectingBounds = new Bounds3i();
      this.rContext = new MaterialProvider.Context(new Vector3i(), 1.0, 0, 0, 0, 0, null, Double.MAX_VALUE);
   }

   @Override
   public boolean generate(@Nonnull Prop.Context context) {
      this.rIntersectingBounds.assign(this.bounds);
      this.rIntersectingBounds.offset(context.position);
      int minYInclusive = this.rIntersectingBounds.min.y;
      int maxYExclusive = this.rIntersectingBounds.max.y;
      this.rIntersectingBounds.intersect(context.materialWriteSpace.getBounds());
      Vector3i position = this.rContext.position;
      this.rContext.density = 1.0;

      for (position.x = this.rIntersectingBounds.min.x; position.x < this.rIntersectingBounds.max.x; position.x++) {
         for (position.z = this.rIntersectingBounds.min.z; position.z < this.rIntersectingBounds.max.z; position.z++) {
            for (position.y = this.rIntersectingBounds.min.y; position.y < this.rIntersectingBounds.max.y; position.y++) {
               this.rContext.depthIntoFloor = maxYExclusive - position.y;
               this.rContext.depthIntoCeiling = position.y - minYInclusive;
               this.rContext.spaceAboveFloor = Integer.MAX_VALUE;
               this.rContext.spaceBelowCeiling = Integer.MAX_VALUE;
               Material material = this.materialProvider.getVoxelTypeAt(this.rContext);
               context.materialWriteSpace.set(material, position);
            }
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
}
