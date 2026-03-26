package com.hypixel.hytale.builtin.hytalegenerator.props;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class QueueProp extends Prop {
   @Nonnull
   private final List<Prop> props;
   @Nonnull
   private final Bounds3i readBounds_voxelGrid;
   @Nonnull
   private final Bounds3i writeBounds_voxelGrid;

   public QueueProp(@Nonnull List<Prop> propChain) {
      if (propChain.isEmpty()) {
         this.props = List.of();
         this.readBounds_voxelGrid = new Bounds3i();
         this.writeBounds_voxelGrid = new Bounds3i();
      } else {
         this.props = new ArrayList<>(propChain);
         this.readBounds_voxelGrid = new Bounds3i();
         this.writeBounds_voxelGrid = new Bounds3i();

         for (Prop prop : propChain) {
            this.readBounds_voxelGrid.encompass(prop.getReadBounds_voxelGrid());
            this.writeBounds_voxelGrid.encompass(prop.getWriteBounds_voxelGrid());
         }
      }
   }

   @Override
   public boolean generate(@Nonnull Prop.Context context) {
      for (Prop prop : this.props) {
         boolean hasGenerated = prop.generate(context);
         if (hasGenerated) {
            return true;
         }
      }

      return false;
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
}
