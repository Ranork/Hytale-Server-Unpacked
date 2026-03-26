package com.hypixel.hytale.builtin.hytalegenerator.positionproviders;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3d;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Control;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Pipe;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class ClustersPositionProvider extends PositionProvider {
   @Nonnull
   private final PositionProvider clusterPositions;
   @Nonnull
   private final PositionProvider distributorPositions;
   @Nonnull
   private final Bounds3d clusterBounds;
   private final Bounds3d clusterBoundsFlipped;
   @Nonnull
   private final Bounds3d rDistributionBounds;
   @Nonnull
   private final PositionProvider.Context rDistributionContext;
   @Nonnull
   private final Bounds3d rClusterBounds;
   @Nonnull
   private final PositionProvider.Context rClusterContext;
   @Nonnull
   private PositionProvider.Context rContext;
   @Nonnull
   private Control rControl;
   @Nonnull
   private final Pipe.One<Vector3d> rDistributionPipe = new Pipe.One<Vector3d>() {
      public void accept(@NonNullDecl Vector3d clusterAnchor, @NonNullDecl Control control) {
         ClustersPositionProvider.this.rClusterBounds.assign(ClustersPositionProvider.this.clusterBounds);
         ClustersPositionProvider.this.rClusterBounds.offset(clusterAnchor);
         ClustersPositionProvider.this.rClusterBounds.intersect(ClustersPositionProvider.this.rContext.bounds);
         ClustersPositionProvider.this.rClusterContext.assign(ClustersPositionProvider.this.rContext);
         ClustersPositionProvider.this.rClusterContext.bounds = ClustersPositionProvider.this.rClusterBounds;
         ClustersPositionProvider.this.rClusterContext.anchor = clusterAnchor;
         ClustersPositionProvider.this.rControl = control;
         ClustersPositionProvider.this.rClusterContext.pipe = ClustersPositionProvider.this.rClusterPipe;
         ClustersPositionProvider.this.clusterPositions.generate(ClustersPositionProvider.this.rClusterContext);
      }
   };
   @Nonnull
   private final Pipe.One<Vector3d> rClusterPipe = new Pipe.One<Vector3d>() {
      public void accept(@NonNullDecl Vector3d position, @NonNullDecl Control control) {
         if (control.stop) {
            ClustersPositionProvider.this.rControl.stop = true;
         } else {
            ClustersPositionProvider.this.rContext.pipe.accept(position, control);
         }
      }
   };

   public ClustersPositionProvider(@Nonnull PositionProvider clusterPositions, @Nonnull PositionProvider distributorPositions, @Nonnull Bounds3d clusterBounds) {
      this.clusterPositions = clusterPositions;
      this.distributorPositions = distributorPositions;
      this.clusterBounds = clusterBounds.clone();
      this.clusterBoundsFlipped = this.clusterBounds.clone();
      this.clusterBoundsFlipped.flipOnOriginPoint();
      this.rDistributionBounds = new Bounds3d();
      this.rDistributionContext = new PositionProvider.Context();
      this.rClusterBounds = new Bounds3d();
      this.rClusterContext = new PositionProvider.Context();
      this.rContext = new PositionProvider.Context();
      this.rControl = new Control();
   }

   @Override
   public void generate(@NonNullDecl PositionProvider.Context context) {
      this.rContext = context;
      this.rDistributionBounds.assign(context.bounds);
      this.rDistributionBounds.stack(this.clusterBoundsFlipped);
      this.rDistributionContext.assign(context);
      this.rDistributionContext.bounds = this.rDistributionBounds;
      this.rDistributionContext.pipe = this.rDistributionPipe;
      this.distributorPositions.generate(this.rDistributionContext);
   }
}
