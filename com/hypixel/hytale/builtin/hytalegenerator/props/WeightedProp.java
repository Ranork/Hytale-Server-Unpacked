package com.hypixel.hytale.builtin.hytalegenerator.props;

import com.hypixel.hytale.builtin.hytalegenerator.WeightedMap;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.props.deprecated.ScanResult;
import com.hypixel.hytale.builtin.hytalegenerator.rng.RngField;
import com.hypixel.hytale.math.util.FastRandom;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class WeightedProp extends Prop {
   @Nonnull
   private final WeightedMap<Prop> props;
   @Nonnull
   private final Bounds3i readBounds_voxelGrid;
   @Nonnull
   private final Bounds3i writeBounds_voxelGrid;
   @Nonnull
   private final RngField rngField;
   @Nonnull
   private final FastRandom random;

   public WeightedProp(@Nonnull WeightedMap<Prop> props, int seed) {
      this.props = new WeightedMap<>(props);
      this.readBounds_voxelGrid = new Bounds3i();
      this.writeBounds_voxelGrid = new Bounds3i();
      this.rngField = new RngField(seed);
      this.random = new FastRandom();

      for (Prop prop : this.props.allElements()) {
         this.readBounds_voxelGrid.encompass(prop.getReadBounds_voxelGrid());
         this.writeBounds_voxelGrid.encompass(prop.getWriteBounds_voxelGrid());
      }
   }

   @Override
   public boolean generate(@NonNullDecl Prop.Context context) {
      if (this.props.size() == 0) {
         return false;
      } else {
         int localSeed = this.rngField.get(context.position.x, context.position.y, context.position.z);
         this.random.setSeed(localSeed);
         Prop pickedProp = this.props.pick(this.random);
         return pickedProp.generate(context);
      }
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

   private static class PickedScanResult implements ScanResult {
      ScanResult scanResult;
      Prop prop;

      @Nonnull
      public static WeightedProp.PickedScanResult cast(ScanResult scanResult) {
         if (!(scanResult instanceof WeightedProp.PickedScanResult)) {
            throw new IllegalArgumentException("The provided ScanResult isn't compatible with this prop.");
         } else {
            return (WeightedProp.PickedScanResult)scanResult;
         }
      }

      @Override
      public boolean isNegative() {
         return this.scanResult == null || this.scanResult.isNegative();
      }
   }
}
