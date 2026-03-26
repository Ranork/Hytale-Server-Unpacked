package com.hypixel.hytale.builtin.hytalegenerator.props;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class OffsetProp extends Prop {
   @Nonnull
   private final Vector3i offset;
   @Nonnull
   private final Prop childProp;
   @Nonnull
   private final Bounds3i readBounds_voxelGrid;
   @Nonnull
   private final Bounds3i writeBounds_voxelGrid;
   private final Vector3i rChildPosition;
   private final Prop.Context rChildContext;

   public OffsetProp(@Nonnull Vector3i offset, @Nonnull Prop childProp) {
      this.offset = offset.clone();
      this.childProp = childProp;
      this.readBounds_voxelGrid = childProp.getReadBounds_voxelGrid().clone().offset(offset);
      this.writeBounds_voxelGrid = childProp.getWriteBounds_voxelGrid().clone().offset(offset);
      this.rChildPosition = new Vector3i();
      this.rChildContext = new Prop.Context();
   }

   @Override
   public boolean generate(@NonNullDecl Prop.Context context) {
      this.rChildPosition.assign(context.position).add(this.offset);
      this.rChildContext.assign(context);
      this.rChildContext.position = this.rChildPosition;
      return this.childProp.generate(this.rChildContext);
   }

   @NonNullDecl
   @Override
   public Bounds3i getReadBounds_voxelGrid() {
      return this.readBounds_voxelGrid;
   }

   @NonNullDecl
   @Override
   public Bounds3i getWriteBounds_voxelGrid() {
      return this.writeBounds_voxelGrid;
   }
}
