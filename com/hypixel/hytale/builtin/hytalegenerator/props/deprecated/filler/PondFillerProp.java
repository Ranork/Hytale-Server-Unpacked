package com.hypixel.hytale.builtin.hytalegenerator.props.deprecated.filler;

import com.hypixel.hytale.builtin.hytalegenerator.MaterialSet;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.MaterialProvider;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.props.deprecated.ScanResult;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.Scanner;
import com.hypixel.hytale.builtin.hytalegenerator.voxelspace.ArrayVoxelSpace;
import com.hypixel.hytale.builtin.hytalegenerator.voxelspace.VoxelSpace;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class PondFillerProp extends Prop {
   private static final int TRAVERSED = 1;
   private static final int LEAKS = 16;
   private static final int SOLID = 256;
   private static final int STACKED = 4096;
   @Nonnull
   private final Vector3i boundingMin;
   @Nonnull
   private final Vector3i boundingMax;
   @Nonnull
   private final MaterialProvider<Material> fillerMaterialProvider;
   @Nonnull
   private final MaterialSet solidSet;
   @Nonnull
   private final Scanner scanner;
   @Nonnull
   private final Pattern pattern;
   @Nonnull
   private final Bounds3i readBounds_voxelGrid;
   @Nonnull
   private final Bounds3i writeBounds_voxelGrid;

   public PondFillerProp(
      @Nonnull Vector3i boundingMin,
      @Nonnull Vector3i boundingMax,
      @Nonnull MaterialSet solidSet,
      @Nonnull MaterialProvider<Material> fillerMaterialProvider,
      @Nonnull Scanner scanner,
      @Nonnull Pattern pattern
   ) {
      this.boundingMin = boundingMin.clone();
      this.boundingMax = boundingMax.clone();
      this.solidSet = solidSet;
      this.fillerMaterialProvider = fillerMaterialProvider;
      this.scanner = scanner;
      this.pattern = pattern;
      this.readBounds_voxelGrid = this.scanner.getBoundsWithPattern_voxelGrid(pattern);
      this.writeBounds_voxelGrid = new Bounds3i(boundingMin, boundingMax);
      this.writeBounds_voxelGrid.stack(this.readBounds_voxelGrid);
   }

   @Nonnull
   public FillerPropScanResult scan_deprecated(@Nonnull Vector3i position, @Nonnull VoxelSpace<Material> materialSpace) {
      Scanner.Context scannerContext = new Scanner.Context(position, this.pattern, materialSpace, new ArrayList<>());
      this.scanner.scan(scannerContext);
      if (scannerContext.validPositions_out.size() == 1) {
         List<Vector3i> resultList = this.renderFluidBlocks(scannerContext.validPositions_out.getFirst(), materialSpace);
         return new FillerPropScanResult(resultList);
      } else {
         ArrayList<Vector3i> resultList = new ArrayList<>();

         for (Vector3i scanPosition : scannerContext.validPositions_out) {
            List<Vector3i> renderResult = this.renderFluidBlocks(scanPosition, materialSpace);
            resultList.addAll(renderResult);
         }

         return new FillerPropScanResult(resultList);
      }
   }

   @Nonnull
   private List<Vector3i> renderFluidBlocks(@Nonnull Vector3i origin, @Nonnull VoxelSpace<Material> materialSpace) {
      Vector3i min = this.boundingMin.clone().add(origin);
      Vector3i max = this.boundingMax.clone().add(origin);
      min = Vector3i.max(min, materialSpace.getBounds().min);
      max = Vector3i.min(max, materialSpace.getBounds().max);
      Bounds3i maskBounds = new Bounds3i(min, max);
      ArrayVoxelSpace<Integer> mask = new ArrayVoxelSpace<>(new Bounds3i(min, max));
      mask.setAll(0);
      int y = min.y;

      for (int x = min.x; x < max.x; x++) {
         for (int z = min.z; z < max.z; z++) {
            Material material = materialSpace.get(x, y, z);
            int contextMaterialHash = material.hashMaterialIds();
            int maskValue = 1;
            if (this.solidSet.test(contextMaterialHash)) {
               maskValue |= 256;
               mask.set(maskValue, x, y, z);
            } else {
               maskValue |= 16;
               mask.set(maskValue, x, y, z);
            }
         }
      }

      for (int var30 = min.y + 1; var30 < max.y; var30++) {
         int underY = var30 - 1;

         for (int x = min.x; x < max.x; x++) {
            for (int zx = min.z; zx < max.z; zx++) {
               if (!isTraversed(mask.get(x, var30, zx))) {
                  int maskValueUnder = mask.get(x, underY, zx);
                  Material material = materialSpace.get(x, var30, zx);
                  int contextMaterialHash = material.hashMaterialIds();
                  if (this.solidSet.test(contextMaterialHash)) {
                     int maskValue = 0;
                     maskValue |= 1;
                     maskValue |= 256;
                     mask.set(maskValue, x, var30, zx);
                  } else if (isLeaks(maskValueUnder) || x == min.x || x == max.x - 1 || zx == min.z || zx == max.z - 1) {
                     ArrayDeque<Vector3i> stack = new ArrayDeque<>();
                     stack.push(new Vector3i(x, var30, zx));
                     mask.set(4096, x, var30, zx);

                     while (!stack.isEmpty()) {
                        Vector3i poppedPos = stack.pop();
                        int maskValue = mask.get(poppedPos.x, poppedPos.y, poppedPos.z);
                        maskValue |= 16;
                        mask.set(maskValue, poppedPos.x, poppedPos.y, poppedPos.z);
                        poppedPos.x--;
                        if (mask.getBounds().contains(poppedPos.x, poppedPos.y, poppedPos.z)) {
                           int poppedMaskValue = mask.get(poppedPos.x, poppedPos.y, poppedPos.z);
                           if (!isStacked(poppedMaskValue)) {
                              material = materialSpace.get(poppedPos.x, poppedPos.y, poppedPos.z);
                              contextMaterialHash = material.hashMaterialIds();
                              if (!this.solidSet.test(contextMaterialHash)) {
                                 stack.push(poppedPos.clone());
                                 mask.set(4096 | poppedMaskValue, poppedPos.x, poppedPos.y, poppedPos.z);
                              }
                           }
                        }

                        poppedPos.x += 2;
                        if (mask.getBounds().contains(poppedPos.x, poppedPos.y, poppedPos.z)) {
                           int poppedMaskValue = mask.get(poppedPos.x, poppedPos.y, poppedPos.z);
                           if (!isStacked(poppedMaskValue)) {
                              material = materialSpace.get(poppedPos.x, poppedPos.y, poppedPos.z);
                              contextMaterialHash = material.hashMaterialIds();
                              if (!this.solidSet.test(contextMaterialHash)) {
                                 stack.push(poppedPos.clone());
                                 mask.set(4096 | poppedMaskValue, poppedPos.x, poppedPos.y, poppedPos.z);
                              }
                           }
                        }

                        poppedPos.x--;
                        poppedPos.z--;
                        if (mask.getBounds().contains(poppedPos.x, poppedPos.y, poppedPos.z)) {
                           int poppedMaskValue = mask.get(poppedPos.x, poppedPos.y, poppedPos.z);
                           if (!isStacked(poppedMaskValue)) {
                              material = materialSpace.get(poppedPos.x, var30, poppedPos.z);
                              contextMaterialHash = material.hashMaterialIds();
                              if (!this.solidSet.test(contextMaterialHash)) {
                                 stack.push(poppedPos.clone());
                                 mask.set(4096 | poppedMaskValue, poppedPos.x, poppedPos.y, poppedPos.z);
                              }
                           }
                        }

                        poppedPos.z += 2;
                        if (mask.getBounds().contains(poppedPos.x, poppedPos.y, poppedPos.z)) {
                           int poppedMaskValue = mask.get(poppedPos.x, poppedPos.y, poppedPos.z);
                           if (!isStacked(poppedMaskValue)) {
                              material = materialSpace.get(poppedPos.x, poppedPos.y, poppedPos.z);
                              contextMaterialHash = material.hashMaterialIds();
                              if (!this.solidSet.test(contextMaterialHash)) {
                                 stack.push(poppedPos.clone());
                                 mask.set(4096 | poppedMaskValue, poppedPos.x, poppedPos.y, poppedPos.z);
                              }
                           }
                        }

                        poppedPos.z--;
                     }
                  }
               }
            }
         }
      }

      ArrayList<Vector3i> fluidBlocks = new ArrayList<>();

      for (int var31 = maskBounds.min.y + 1; var31 < maskBounds.max.y; var31++) {
         for (int x = maskBounds.min.x + 1; x < maskBounds.max.x - 1; x++) {
            for (int zxx = maskBounds.min.z + 1; zxx < maskBounds.max.z - 1; zxx++) {
               int maskValuex = mask.get(x, var31, zxx);
               if (!isSolid(maskValuex) && !isLeaks(maskValuex)) {
                  fluidBlocks.add(new Vector3i(x, var31, zxx));
               }
            }
         }
      }

      return fluidBlocks;
   }

   public void place_deprecated(@Nonnull Prop.Context context, @Nonnull ScanResult scanResult) {
      List<Vector3i> fluidBlocks = FillerPropScanResult.cast(scanResult).getFluidBlocks();
      if (fluidBlocks != null) {
         for (Vector3i position : fluidBlocks) {
            if (context.materialWriteSpace.getBounds().contains(position.x, position.y, position.z)) {
               MaterialProvider.Context materialsContext = new MaterialProvider.Context(position, 0.0, 0, 0, 0, 0, null, context.distanceToBiomeEdge);
               Material material = this.fillerMaterialProvider.getVoxelTypeAt(materialsContext);
               if (material != null) {
                  context.materialWriteSpace.set(material, position.x, position.y, position.z);
               }
            }
         }
      }
   }

   @Override
   public boolean generate(@NonNullDecl Prop.Context context) {
      ScanResult scanResult = this.scan_deprecated(context.position, context.materialReadSpace);
      this.place_deprecated(context, scanResult);
      return !scanResult.isNegative();
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

   private static boolean isTraversed(int maskValue) {
      return (maskValue & 1) == 1;
   }

   private static boolean isLeaks(int maskValue) {
      return (maskValue & 16) == 16;
   }

   private static boolean isSolid(int maskValue) {
      return (maskValue & 256) == 256;
   }

   private static boolean isStacked(int maskValue) {
      return (maskValue & 4096) == 4096;
   }
}
