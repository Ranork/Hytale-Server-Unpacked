package com.hypixel.hytale.builtin.hytalegenerator.props;

import com.hypixel.hytale.builtin.hytalegenerator.BlockMask;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.SpaceSize;
import com.hypixel.hytale.builtin.hytalegenerator.conveyor.stagedconveyor.ContextDependency;
import com.hypixel.hytale.builtin.hytalegenerator.datastructures.voxelspace.VoxelSpace;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.material.MaterialCache;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.builtin.hytalegenerator.props.directionality.Directionality;
import com.hypixel.hytale.builtin.hytalegenerator.props.directionality.RotatedPosition;
import com.hypixel.hytale.builtin.hytalegenerator.props.directionality.RotatedPositionsScanResult;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.Scanner;
import com.hypixel.hytale.builtin.hytalegenerator.threadindexer.WorkerIndexer;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.prefab.PrefabRotation;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class ColumnProp extends Prop {
   @Nonnull
   private final int[] yPositions;
   @Nonnull
   private final Material[] blocks0;
   @Nonnull
   private final Material[] blocks90;
   @Nonnull
   private final Material[] blocks180;
   @Nonnull
   private final Material[] blocks270;
   @Nonnull
   private final BlockMask blockMask;
   @Nonnull
   private final Scanner scanner;
   @Nonnull
   private final ContextDependency contextDependency;
   @Nonnull
   private final Directionality directionality;
   @Nonnull
   private final Bounds3i readBounds_voxelGrid;
   @Nonnull
   private final Bounds3i writeBounds_voxelGrid;

   public ColumnProp(
      @Nonnull List<Integer> propYPositions,
      @Nonnull List<Material> blocks,
      @Nonnull BlockMask blockMask,
      @Nonnull Scanner scanner,
      @Nonnull Directionality directionality,
      @Nonnull MaterialCache materialCache
   ) {
      if (propYPositions.size() != blocks.size()) {
         throw new IllegalArgumentException("blocks and positions sizes don't match");
      } else {
         this.blockMask = blockMask;
         this.yPositions = new int[propYPositions.size()];
         this.blocks0 = new Material[blocks.size()];
         this.blocks90 = new Material[blocks.size()];
         this.blocks180 = new Material[blocks.size()];
         this.blocks270 = new Material[blocks.size()];

         for (int i = 0; i < this.yPositions.length; i++) {
            this.yPositions[i] = propYPositions.get(i);
            this.blocks0[i] = blocks.get(i);
            this.blocks90[i] = new Material(materialCache.getSolidMaterialRotatedY(blocks.get(i).solid(), Rotation.Ninety), blocks.get(i).fluid());
            this.blocks180[i] = new Material(materialCache.getSolidMaterialRotatedY(blocks.get(i).solid(), Rotation.OneEighty), blocks.get(i).fluid());
            this.blocks270[i] = new Material(materialCache.getSolidMaterialRotatedY(blocks.get(i).solid(), Rotation.TwoSeventy), blocks.get(i).fluid());
         }

         this.scanner = scanner;
         this.directionality = directionality;
         SpaceSize writeSpace = new SpaceSize(new Vector3i(0, 0, 0), new Vector3i(1, 0, 1));
         writeSpace = SpaceSize.stack(writeSpace, scanner.readSpaceWith(directionality.getGeneralPattern()));
         Vector3i writeRange = writeSpace.getRange();
         Vector3i readRange = directionality.getReadRangeWith(scanner);
         this.contextDependency = new ContextDependency(readRange, writeRange);
         this.readBounds_voxelGrid = this.contextDependency.getReadBounds_voxelGrid();
         this.writeBounds_voxelGrid = this.contextDependency.getWriteBounds_voxelGrid();
      }
   }

   @Nonnull
   @Override
   public ScanResult scan(@Nonnull Vector3i position, @Nonnull VoxelSpace<Material> materialSpace, @Nonnull WorkerIndexer.Id id) {
      Scanner.Context scannerContext = new Scanner.Context(position, this.directionality.getGeneralPattern(), materialSpace, id);
      List<Vector3i> validPositions = this.scanner.scan(scannerContext);
      Vector3i patternPosition = new Vector3i();
      Pattern.Context patternContext = new Pattern.Context(patternPosition, materialSpace);
      RotatedPositionsScanResult scanResult = new RotatedPositionsScanResult(new ArrayList<>());

      for (Vector3i validPosition : validPositions) {
         patternPosition.assign(validPosition);
         PrefabRotation rotation = this.directionality.getRotationAt(patternContext);
         if (rotation != null) {
            scanResult.positions.add(new RotatedPosition(validPosition.x, validPosition.y, validPosition.z, rotation));
         }
      }

      return scanResult;
   }

   @Override
   public void place(@Nonnull Prop.Context context) {
      for (RotatedPosition position : RotatedPositionsScanResult.cast(context.scanResult).positions) {
         this.place(position, context.materialSpace);
      }
   }

   private void place(@Nonnull RotatedPosition position, @Nonnull VoxelSpace<Material> materialSpace) {
      PrefabRotation rotation = position.rotation;

      Material[] blocks = switch (rotation) {
         case ROTATION_0 -> this.blocks0;
         case ROTATION_90 -> this.blocks90;
         case ROTATION_180 -> this.blocks180;
         case ROTATION_270 -> this.blocks270;
      };

      for (int i = 0; i < this.yPositions.length; i++) {
         int y = this.yPositions[i] + position.y;
         Material propBlock = blocks[i];
         if (materialSpace.isInsideSpace(position.x, y, position.z) && this.blockMask.canPlace(propBlock)) {
            Material worldMaterial = materialSpace.getContent(position.x, y, position.z);

            assert worldMaterial != null;

            int worldMaterialHash = worldMaterial.hashMaterialIds();
            if (this.blockMask.canReplace(propBlock.hashMaterialIds(), worldMaterialHash)) {
               materialSpace.set(propBlock, position.x, y, position.z);
            }
         }
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
}
