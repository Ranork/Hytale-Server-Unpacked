package com.hypixel.hytale.builtin.hytalegenerator.props;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.engine.entityfunnel.RotationEntityFunnel;
import com.hypixel.hytale.builtin.hytalegenerator.material.MaterialCache;
import com.hypixel.hytale.builtin.hytalegenerator.voxelspace.RotationVoxelSpace;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class StaticRotatorProp extends Prop {
   @Nonnull
   private final Prop prop;
   @Nonnull
   private final RotationVoxelSpace readRotationVoxelSpace;
   @Nonnull
   private final RotationVoxelSpace writeRotationVoxelSpace;
   @Nonnull
   private final RotationEntityFunnel rotationEntityFunnel;
   @Nonnull
   private final Bounds3i readBounds;
   @Nonnull
   private final Bounds3i writeBounds;
   @Nonnull
   private final Prop.Context rChildContext;

   public StaticRotatorProp(@Nonnull Prop prop, @Nonnull RotationTuple rotation, @Nonnull MaterialCache materialCache) {
      this.prop = prop;
      this.readRotationVoxelSpace = new RotationVoxelSpace(rotation, materialCache);
      this.writeRotationVoxelSpace = new RotationVoxelSpace(rotation, materialCache);
      this.rotationEntityFunnel = new RotationEntityFunnel(rotation);
      this.readBounds = prop.getReadBounds_voxelGrid().clone();
      this.writeBounds = prop.getWriteBounds_voxelGrid().clone();
      this.readBounds.applyRotationAroundVoxel(rotation, Vector3i.ZERO);
      this.writeBounds.applyRotationAroundVoxel(rotation, Vector3i.ZERO);
      this.rChildContext = new Prop.Context();
   }

   @Override
   public boolean generate(@NonNullDecl Prop.Context context) {
      this.readRotationVoxelSpace.setSource(context.materialReadSpace, context.position);
      this.writeRotationVoxelSpace.setSource(context.materialWriteSpace, context.position);
      this.rotationEntityFunnel.setSource(context.entityWriteBuffer, context.position);
      this.rChildContext.assign(context);
      this.rChildContext.materialReadSpace = this.readRotationVoxelSpace;
      this.rChildContext.materialWriteSpace = this.writeRotationVoxelSpace;
      this.rChildContext.entityWriteBuffer = this.rotationEntityFunnel;
      return this.prop.generate(this.rChildContext);
   }

   @NonNullDecl
   @Override
   public Bounds3i getReadBounds_voxelGrid() {
      return this.readBounds;
   }

   @NonNullDecl
   @Override
   public Bounds3i getWriteBounds_voxelGrid() {
      return this.writeBounds;
   }
}
