package com.hypixel.hytale.builtin.hytalegenerator.props.filler;

import com.hypixel.hytale.builtin.hytalegenerator.MaterialSet;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.SpaceSize;
import com.hypixel.hytale.builtin.hytalegenerator.conveyor.stagedconveyor.ContextDependency;
import com.hypixel.hytale.builtin.hytalegenerator.datastructures.voxelspace.ArrayVoxelSpace;
import com.hypixel.hytale.builtin.hytalegenerator.datastructures.voxelspace.VoxelSpace;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.MaterialProvider;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.Scanner;
import com.hypixel.hytale.builtin.hytalegenerator.threadindexer.WorkerIndexer;
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
   private final MaterialProvider<Material> filledMaterialProvider;
   @Nonnull
   private final MaterialSet solidSet;
   @Nonnull
   private final Scanner scanner;
   @Nonnull
   private final Pattern pattern;
   @Nonnull
   private final ContextDependency contextDependency;
   @Nonnull
   private final Bounds3i readBounds_voxelGrid;
   @Nonnull
   private final Bounds3i writeBounds_voxelGrid;

   public PondFillerProp(
      @Nonnull Vector3i boundingMin,
      @Nonnull Vector3i boundingMax,
      @Nonnull MaterialSet solidSet,
      @Nonnull MaterialProvider<Material> filledMaterialProvider,
      @Nonnull Scanner scanner,
      @Nonnull Pattern pattern
   ) {
      this.boundingMin = boundingMin.clone();
      this.boundingMax = boundingMax.clone();
      this.solidSet = solidSet;
      this.filledMaterialProvider = filledMaterialProvider;
      this.scanner = scanner;
      this.pattern = pattern;
      SpaceSize boundingSpace = new SpaceSize(boundingMin, boundingMax);
      boundingSpace = SpaceSize.stack(boundingSpace, scanner.readSpaceWith(pattern));
      SpaceSize.stack(scanner.readSpaceWith(pattern), boundingSpace);
      Vector3i range = boundingSpace.getRange();
      this.contextDependency = new ContextDependency(range, range);
      this.readBounds_voxelGrid = this.contextDependency.getReadBounds_voxelGrid();
      this.writeBounds_voxelGrid = this.contextDependency.getWriteBounds_voxelGrid();
   }

   @Nonnull
   public FillerPropScanResult scan(@Nonnull Vector3i position, @Nonnull VoxelSpace<Material> materialSpace, @Nonnull WorkerIndexer.Id id) {
      Scanner.Context scannerContext = new Scanner.Context(position, this.pattern, materialSpace, id);
      List<Vector3i> scanResults = this.scanner.scan(scannerContext);
      if (scanResults.size() == 1) {
         List<Vector3i> resultList = this.renderFluidBlocks(scanResults.getFirst(), materialSpace);
         return new FillerPropScanResult(resultList);
      } else {
         ArrayList<Vector3i> resultList = new ArrayList<>();

         for (Vector3i scanPosition : scanResults) {
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
      min = Vector3i.max(min, new Vector3i(materialSpace.minX(), materialSpace.minY(), materialSpace.minZ()));
      max = Vector3i.min(max, new Vector3i(materialSpace.maxX(), materialSpace.maxY(), materialSpace.maxZ()));
      ArrayVoxelSpace<Integer> mask = new ArrayVoxelSpace<>(max.x - min.x, max.y - min.y, max.z - min.z);
      mask.setOrigin(-min.x, -min.y, -min.z);
      mask.set(0);
      int y = min.y;

      for (int x = min.x; x < max.x; x++) {
         for (int z = min.z; z < max.z; z++) {
            Material material = materialSpace.getContent(x, y, z);
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

      for (int var29 = min.y + 1; var29 < max.y; var29++) {
         int underY = var29 - 1;

         for (int x = min.x; x < max.x; x++) {
            for (int zx = min.z; zx < max.z; zx++) {
               if (!isTraversed(mask.getContent(x, var29, zx))) {
                  int maskValueUnder = mask.getContent(x, underY, zx);
                  Material material = materialSpace.getContent(x, var29, zx);
                  int contextMaterialHash = material.hashMaterialIds();
                  if (this.solidSet.test(contextMaterialHash)) {
                     int maskValue = 0;
                     maskValue |= 1;
                     maskValue |= 256;
                     mask.set(maskValue, x, var29, zx);
                  } else if (isLeaks(maskValueUnder) || x == min.x || x == max.x - 1 || zx == min.z || zx == max.z - 1) {
                     ArrayDeque<Vector3i> stack = new ArrayDeque<>();
                     stack.push(new Vector3i(x, var29, zx));
                     mask.set(4096, x, var29, zx);

                     while (!stack.isEmpty()) {
                        Vector3i poppedPos = stack.pop();
                        int maskValue = mask.getContent(poppedPos.x, poppedPos.y, poppedPos.z);
                        maskValue |= 16;
                        mask.set(maskValue, poppedPos.x, poppedPos.y, poppedPos.z);
                        poppedPos.x--;
                        if (mask.isInsideSpace(poppedPos.x, poppedPos.y, poppedPos.z)) {
                           int poppedMaskValue = mask.getContent(poppedPos.x, poppedPos.y, poppedPos.z);
                           if (!isStacked(poppedMaskValue)) {
                              material = materialSpace.getContent(poppedPos.x, poppedPos.y, poppedPos.z);
                              contextMaterialHash = material.hashMaterialIds();
                              if (!this.solidSet.test(contextMaterialHash)) {
                                 stack.push(poppedPos.clone());
                                 mask.set(4096 | poppedMaskValue, poppedPos.x, poppedPos.y, poppedPos.z);
                              }
                           }
                        }

                        poppedPos.x += 2;
                        if (mask.isInsideSpace(poppedPos.x, poppedPos.y, poppedPos.z)) {
                           int poppedMaskValue = mask.getContent(poppedPos.x, poppedPos.y, poppedPos.z);
                           if (!isStacked(poppedMaskValue)) {
                              material = materialSpace.getContent(poppedPos.x, poppedPos.y, poppedPos.z);
                              contextMaterialHash = material.hashMaterialIds();
                              if (!this.solidSet.test(contextMaterialHash)) {
                                 stack.push(poppedPos.clone());
                                 mask.set(4096 | poppedMaskValue, poppedPos.x, poppedPos.y, poppedPos.z);
                              }
                           }
                        }

                        poppedPos.x--;
                        poppedPos.z--;
                        if (mask.isInsideSpace(poppedPos.x, poppedPos.y, poppedPos.z)) {
                           int poppedMaskValue = mask.getContent(poppedPos.x, poppedPos.y, poppedPos.z);
                           if (!isStacked(poppedMaskValue)) {
                              material = materialSpace.getContent(poppedPos.x, var29, poppedPos.z);
                              contextMaterialHash = material.hashMaterialIds();
                              if (!this.solidSet.test(contextMaterialHash)) {
                                 stack.push(poppedPos.clone());
                                 mask.set(4096 | poppedMaskValue, poppedPos.x, poppedPos.y, poppedPos.z);
                              }
                           }
                        }

                        poppedPos.z += 2;
                        if (mask.isInsideSpace(poppedPos.x, poppedPos.y, poppedPos.z)) {
                           int poppedMaskValue = mask.getContent(poppedPos.x, poppedPos.y, poppedPos.z);
                           if (!isStacked(poppedMaskValue)) {
                              material = materialSpace.getContent(poppedPos.x, poppedPos.y, poppedPos.z);
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

      for (int var30 = mask.minY() + 1; var30 < mask.maxY(); var30++) {
         for (int x = mask.minX() + 1; x < mask.maxX() - 1; x++) {
            for (int zxx = mask.minZ() + 1; zxx < mask.maxZ() - 1; zxx++) {
               int maskValuex = mask.getContent(x, var30, zxx);
               if (!isSolid(maskValuex) && !isLeaks(maskValuex)) {
                  fluidBlocks.add(new Vector3i(x, var30, zxx));
               }
            }
         }
      }

      return fluidBlocks;
   }

   @Override
   public void place(@Nonnull Prop.Context context) {
      List<Vector3i> fluidBlocks = FillerPropScanResult.cast(context.scanResult).getFluidBlocks();
      if (fluidBlocks != null) {
         for (Vector3i position : fluidBlocks) {
            if (context.materialSpace.isInsideSpace(position.x, position.y, position.z)) {
               MaterialProvider.Context materialsContext = new MaterialProvider.Context(position, 0.0, 0, 0, 0, 0, null, context.distanceFromBiomeEdge);
               Material material = this.filledMaterialProvider.getVoxelTypeAt(materialsContext);
               if (material != null) {
                  context.materialSpace.set(material, position.x, position.y, position.z);
               }
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
