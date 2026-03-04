package com.hypixel.hytale.builtin.hytalegenerator.props;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.conveyor.stagedconveyor.ContextDependency;
import com.hypixel.hytale.builtin.hytalegenerator.datastructures.voxelspace.VoxelSpace;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.threadindexer.WorkerIndexer;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class UnionProp extends Prop {
   @Nonnull
   private final List<Prop> props;
   @Nonnull
   private final ContextDependency contextDependency;
   @Nonnull
   private final Bounds3i readBounds_voxelGrid;
   @Nonnull
   private final Bounds3i writeBounds_voxelGrid;

   public UnionProp(@Nonnull List<Prop> propChain) {
      this.props = new ArrayList<>(propChain);
      this.readBounds_voxelGrid = new Bounds3i();
      this.writeBounds_voxelGrid = new Bounds3i();
      Vector3i writeRange = new Vector3i();
      Vector3i readRange = new Vector3i();

      for (Prop prop : propChain) {
         writeRange = Vector3i.max(writeRange, prop.getContextDependency().getWriteRange());
         readRange = Vector3i.max(readRange, prop.getContextDependency().getReadRange());
         this.readBounds_voxelGrid.encompass(prop.getReadBounds_voxelGrid());
         this.writeBounds_voxelGrid.encompass(prop.getWriteBounds_voxelGrid());
      }

      this.contextDependency = new ContextDependency(readRange, writeRange);
   }

   @Nonnull
   @Override
   public ScanResult scan(@Nonnull Vector3i position, @Nonnull VoxelSpace<Material> materialSpace, @Nonnull WorkerIndexer.Id id) {
      UnionProp.ChainedScanResult scanResult = new UnionProp.ChainedScanResult();
      scanResult.scanResults = new ArrayList<>(this.props.size());

      for (Prop prop : this.props) {
         scanResult.scanResults.add(prop.scan(position, materialSpace, id));
      }

      return scanResult;
   }

   @Override
   public void place(@Nonnull Prop.Context context) {
      List<ScanResult> scanResults = UnionProp.ChainedScanResult.cast(context.scanResult).scanResults;

      for (int i = 0; i < this.props.size(); i++) {
         Prop prop = this.props.get(i);
         Prop.Context childContext = new Prop.Context(context);
         childContext.scanResult = scanResults.get(i);
         prop.place(childContext);
      }
   }

   @Nonnull
   @Override
   public ContextDependency getContextDependency() {
      return this.contextDependency.clone();
   }

   @NonNullDecl
   @Override
   public Bounds3i getReadBounds_voxelGrid() {
      return this.readBounds_voxelGrid;
   }

   @Nonnull
   @Override
   public Bounds3i getWriteBounds_voxelGrid() {
      return this.writeBounds_voxelGrid;
   }

   private static class ChainedScanResult implements ScanResult {
      List<ScanResult> scanResults;

      @Nonnull
      public static UnionProp.ChainedScanResult cast(ScanResult scanResult) {
         if (!(scanResult instanceof UnionProp.ChainedScanResult)) {
            throw new IllegalArgumentException("The provided ScanResult isn't compatible with this prop.");
         } else {
            return (UnionProp.ChainedScanResult)scanResult;
         }
      }

      @Override
      public boolean isNegative() {
         return this.scanResults == null || this.scanResults.isEmpty();
      }
   }
}
