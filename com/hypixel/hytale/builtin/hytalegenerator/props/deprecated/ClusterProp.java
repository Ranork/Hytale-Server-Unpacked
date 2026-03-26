package com.hypixel.hytale.builtin.hytalegenerator.props.deprecated;

import com.hypixel.hytale.builtin.hytalegenerator.WeightedMap;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.engine.entityfunnel.EntityFunnel;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.math.Calculator;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.rng.RngField;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.Scanner;
import com.hypixel.hytale.builtin.hytalegenerator.voxelspace.VoxelSpace;
import com.hypixel.hytale.builtin.hytalegenerator.voxelspace.WindowVoxelSpace;
import com.hypixel.hytale.math.util.FastRandom;
import com.hypixel.hytale.math.vector.Vector3i;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class ClusterProp extends Prop {
   @Nonnull
   private final Double2DoubleFunction weightCurve;
   @Nonnull
   private final RngField rngField;
   @Nonnull
   private final WeightedMap<Prop> propWeightedMap;
   private final int range;
   @Nonnull
   private final Pattern pattern;
   @Nonnull
   private final Scanner scanner;
   @Nonnull
   private final Bounds3i readBounds_voxelGrid;
   @Nonnull
   private final Bounds3i writeBounds_voxelGrid;
   @Nonnull
   private final Prop.Context rChildContext;

   public ClusterProp(
      int range,
      @Nonnull Double2DoubleFunction weightCurve,
      int seed,
      @Nonnull WeightedMap<Prop> propWeightedMap,
      @Nonnull Pattern pattern,
      @Nonnull Scanner scanner
   ) {
      if (range < 0) {
         throw new IllegalArgumentException("negative range");
      } else {
         this.range = range;
         this.rngField = new RngField(seed);
         this.weightCurve = weightCurve;
         this.pattern = pattern;
         this.scanner = scanner;
         this.readBounds_voxelGrid = scanner.getBoundsWithPattern_voxelGrid(pattern);
         this.writeBounds_voxelGrid = new Bounds3i(new Vector3i(-range, -10000, -range), new Vector3i(range, 10000, range));
         this.writeBounds_voxelGrid.stack(scanner.getBounds_voxelGrid());
         this.propWeightedMap = new WeightedMap<>();
         propWeightedMap.forEach((prop, weight) -> {
            if (this.isColumnBounded(prop)) {
               this.propWeightedMap.add(prop, propWeightedMap.get(prop));
            }
         });
         this.rChildContext = new Prop.Context();
      }
   }

   @Override
   public boolean generate(@NonNullDecl Prop.Context context) {
      if (this.propWeightedMap.size() == 0) {
         return false;
      } else {
         PositionListScanResult scanResult = this.scan_deprecated(context.position, context.materialReadSpace);
         this.place_deprecated(context, scanResult);
         return !scanResult.isNegative();
      }
   }

   @Nonnull
   public PositionListScanResult scan_deprecated(@Nonnull Vector3i position, @Nonnull VoxelSpace<Material> materialSpace) {
      Scanner.Context scannerContext = new Scanner.Context(position, this.pattern, materialSpace, new ArrayList<>());
      this.scanner.scan(scannerContext);
      return new PositionListScanResult(scannerContext.validPositions_out);
   }

   public void place_deprecated(@Nonnull Prop.Context context, @Nonnull PositionListScanResult scanResult) {
      List<Vector3i> positions = scanResult.getPositions();
      if (positions != null) {
         for (Vector3i position : positions) {
            this.place(position, context.materialReadSpace, context.materialWriteSpace, context.entityWriteBuffer, context.distanceToBiomeEdge);
         }
      }
   }

   private boolean isColumnBounded(@Nonnull Prop prop) {
      Bounds3i readBounds_voxelGrid = prop.getReadBounds_voxelGrid();
      Bounds3i writeBounds_voxelGrid = prop.getWriteBounds_voxelGrid();
      return readBounds_voxelGrid.min.x == 0
         && readBounds_voxelGrid.min.z == 0
         && readBounds_voxelGrid.max.x == 1
         && readBounds_voxelGrid.max.z == 1
         && writeBounds_voxelGrid.min.x == 0
         && writeBounds_voxelGrid.min.z == 0
         && writeBounds_voxelGrid.max.x == 1
         && writeBounds_voxelGrid.max.z == 1;
   }

   private void place(
      @Nonnull Vector3i position,
      @Nonnull VoxelSpace<Material> materialReadSpace,
      @Nonnull VoxelSpace<Material> materialWriteSpace,
      @Nonnull EntityFunnel entityBuffer,
      double distanceFromBiomeEdge
   ) {
      WindowVoxelSpace<Material> columnReadSpace = new WindowVoxelSpace<>(materialWriteSpace);
      WindowVoxelSpace<Material> columnWriteSpace = new WindowVoxelSpace<>(materialWriteSpace);
      Bounds3i writeBounds_voxelGrid = columnWriteSpace.getBounds();
      this.rChildContext.materialReadSpace = columnReadSpace;
      this.rChildContext.materialWriteSpace = columnWriteSpace;
      this.rChildContext.entityWriteBuffer = entityBuffer;
      this.rChildContext.distanceToBiomeEdge = distanceFromBiomeEdge;
      FastRandom random = new FastRandom(this.rngField.get(position.x, position.z));

      for (int x = position.x - this.range; x < position.x + this.range; x++) {
         for (int z = position.z - this.range; z < position.z + this.range; z++) {
            double distance = Calculator.distance(x, z, position.x, position.z);
            double density = this.weightCurve.get(distance);
            if (!(random.nextDouble() > density)) {
               Prop pickedProp = this.propWeightedMap.pick(random);
               if (materialWriteSpace.getBounds().contains(x, writeBounds_voxelGrid.min.y, z)) {
                  columnWriteSpace.setBounds(x, writeBounds_voxelGrid.min.y, z, x + 1, writeBounds_voxelGrid.max.y, z + 1);
                  this.rChildContext.position.assign(x, position.y, z);
                  pickedProp.generate(this.rChildContext);
               }
            }
         }
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
}
