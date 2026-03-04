package com.hypixel.hytale.builtin.hytalegenerator.assets.props;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.conveyor.stagedconveyor.ContextDependency;
import com.hypixel.hytale.builtin.hytalegenerator.datastructures.voxelspace.VoxelSpace;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.props.ScanResult;
import com.hypixel.hytale.builtin.hytalegenerator.threadindexer.WorkerIndexer;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class NoPropAsset extends PropAsset {
   @Nonnull
   public static final BuilderCodec<NoPropAsset> CODEC = BuilderCodec.builder(NoPropAsset.class, NoPropAsset::new, PropAsset.ABSTRACT_CODEC).build();

   @Nonnull
   @Override
   public Prop build(@Nonnull PropAsset.Argument argument) {
      return new Prop() {
         @Nonnull
         final Bounds3i emptyBounds_voxelGrid = new Bounds3i();

         @Nonnull
         @Override
         public ScanResult scan(@Nonnull Vector3i position, @Nonnull VoxelSpace<Material> materialSpace, @Nonnull WorkerIndexer.Id id) {
            return ScanResult.noScanResult();
         }

         @Override
         public void place(@Nonnull Prop.Context context) {
         }

         @Nonnull
         @Override
         public ContextDependency getContextDependency() {
            return ContextDependency.EMPTY;
         }

         @NonNullDecl
         @Override
         public Bounds3i getReadBounds_voxelGrid() {
            return this.emptyBounds_voxelGrid;
         }

         @Nonnull
         @Override
         public Bounds3i getWriteBounds_voxelGrid() {
            return this.emptyBounds_voxelGrid;
         }
      };
   }

   @Override
   public void cleanUp() {
   }
}
