package com.hypixel.hytale.builtin.hytalegenerator.props;

import com.hypixel.hytale.builtin.hytalegenerator.MaterialSet;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.MaterialProvider;
import com.hypixel.hytale.builtin.hytalegenerator.voxelspace.ArrayVoxelSpace;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.ArrayDeque;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class PondFillerProp extends Prop {
   private static final int TRAVERSED = 1;
   private static final int LEAKS = 16;
   private static final int SOLID = 256;
   private static final int STACKED = 4096;
   @Nonnull
   private final Bounds3i bounds;
   @Nonnull
   private final MaterialProvider<Material> fillerMaterialProvider;
   @Nonnull
   private final MaterialSet solidSet;
   @Nonnull
   private final Bounds3i rLocalBounds;
   @Nonnull
   private final Bounds3i rLocalWriteBounds;
   @Nonnull
   private final ArrayVoxelSpace<Integer> rMask;
   @Nonnull
   private final MaterialProvider.Context rMaterialProviderContext;

   public PondFillerProp(@Nonnull Bounds3i bounds, @Nonnull MaterialProvider<Material> fillerMaterialProvider, @Nonnull MaterialSet solidSet) {
      this.bounds = bounds.clone();
      this.fillerMaterialProvider = fillerMaterialProvider;
      this.solidSet = solidSet;
      this.rLocalBounds = new Bounds3i();
      this.rLocalWriteBounds = new Bounds3i();
      this.rMask = new ArrayVoxelSpace<>(bounds);
      this.rMaterialProviderContext = new MaterialProvider.Context(new Vector3i(), 0.0, 0, 0, 0, 0, null, Double.MAX_VALUE);
   }

   @Override
   public boolean generate(@NonNullDecl Prop.Context context) {
      this.rLocalBounds.assign(this.bounds).offset(context.position);
      this.rLocalWriteBounds.assign(this.rLocalBounds).intersect(context.materialWriteSpace.getBounds());
      if (!context.materialReadSpace.getBounds().contains(this.rLocalBounds)) {
         return true;
      } else {
         Bounds3i localMaskBounds = this.rMask.getBounds();
         localMaskBounds.assign(this.bounds);
         localMaskBounds.offset(context.position);
         this.rMask.setAll(0);
         int y = this.rLocalBounds.min.y;

         for (int x = this.rLocalBounds.min.x; x < this.rLocalBounds.max.x; x++) {
            for (int z = this.rLocalBounds.min.z; z < this.rLocalBounds.max.z; z++) {
               Material material = context.materialReadSpace.get(x, y, z);
               int contextMaterialHash = material.hashMaterialIds();
               int maskValue = 1;
               if (this.solidSet.test(contextMaterialHash)) {
                  maskValue |= 256;
                  this.rMask.set(maskValue, x, y, z);
               } else {
                  maskValue |= 16;
                  this.rMask.set(maskValue, x, y, z);
               }
            }
         }

         for (int var14 = this.rLocalBounds.min.y + 1; var14 < this.rLocalBounds.max.y; var14++) {
            int underY = var14 - 1;

            for (int x = this.rLocalBounds.min.x; x < this.rLocalBounds.max.x; x++) {
               for (int zx = this.rLocalBounds.min.z; zx < this.rLocalBounds.max.z; zx++) {
                  if (!isTraversed(this.rMask.get(x, var14, zx))) {
                     int maskValueUnder = this.rMask.get(x, underY, zx);
                     Material material = context.materialReadSpace.get(x, var14, zx);
                     int contextMaterialHash = material.hashMaterialIds();
                     if (this.solidSet.test(contextMaterialHash)) {
                        int maskValue = 0;
                        maskValue |= 1;
                        maskValue |= 256;
                        this.rMask.set(maskValue, x, var14, zx);
                     } else if (isLeaks(maskValueUnder)
                        || x == this.rLocalBounds.min.x
                        || x == this.rLocalBounds.max.x - 1
                        || zx == this.rLocalBounds.min.z
                        || zx == this.rLocalBounds.max.z - 1) {
                        ArrayDeque<Vector3i> stack = new ArrayDeque<>();
                        stack.push(new Vector3i(x, var14, zx));
                        this.rMask.set(4096, x, var14, zx);

                        while (!stack.isEmpty()) {
                           Vector3i poppedPos = stack.pop();
                           int maskValue = this.rMask.get(poppedPos.x, poppedPos.y, poppedPos.z);
                           maskValue |= 16;
                           this.rMask.set(maskValue, poppedPos.x, poppedPos.y, poppedPos.z);
                           poppedPos.x--;
                           if (this.rMask.getBounds().contains(poppedPos.x, poppedPos.y, poppedPos.z)) {
                              int poppedMaskValue = this.rMask.get(poppedPos.x, poppedPos.y, poppedPos.z);
                              if (!isStacked(poppedMaskValue)) {
                                 material = context.materialReadSpace.get(poppedPos.x, poppedPos.y, poppedPos.z);
                                 contextMaterialHash = material.hashMaterialIds();
                                 if (!this.solidSet.test(contextMaterialHash)) {
                                    stack.push(poppedPos.clone());
                                    this.rMask.set(4096 | poppedMaskValue, poppedPos.x, poppedPos.y, poppedPos.z);
                                 }
                              }
                           }

                           poppedPos.x += 2;
                           if (this.rMask.getBounds().contains(poppedPos.x, poppedPos.y, poppedPos.z)) {
                              int poppedMaskValue = this.rMask.get(poppedPos.x, poppedPos.y, poppedPos.z);
                              if (!isStacked(poppedMaskValue)) {
                                 material = context.materialReadSpace.get(poppedPos.x, poppedPos.y, poppedPos.z);
                                 contextMaterialHash = material.hashMaterialIds();
                                 if (!this.solidSet.test(contextMaterialHash)) {
                                    stack.push(poppedPos.clone());
                                    this.rMask.set(4096 | poppedMaskValue, poppedPos.x, poppedPos.y, poppedPos.z);
                                 }
                              }
                           }

                           poppedPos.x--;
                           poppedPos.z--;
                           if (this.rMask.getBounds().contains(poppedPos.x, poppedPos.y, poppedPos.z)) {
                              int poppedMaskValue = this.rMask.get(poppedPos.x, poppedPos.y, poppedPos.z);
                              if (!isStacked(poppedMaskValue)) {
                                 material = context.materialReadSpace.get(poppedPos.x, var14, poppedPos.z);
                                 contextMaterialHash = material.hashMaterialIds();
                                 if (!this.solidSet.test(contextMaterialHash)) {
                                    stack.push(poppedPos.clone());
                                    this.rMask.set(4096 | poppedMaskValue, poppedPos.x, poppedPos.y, poppedPos.z);
                                 }
                              }
                           }

                           poppedPos.z += 2;
                           if (this.rMask.getBounds().contains(poppedPos.x, poppedPos.y, poppedPos.z)) {
                              int poppedMaskValue = this.rMask.get(poppedPos.x, poppedPos.y, poppedPos.z);
                              if (!isStacked(poppedMaskValue)) {
                                 material = context.materialReadSpace.get(poppedPos.x, poppedPos.y, poppedPos.z);
                                 contextMaterialHash = material.hashMaterialIds();
                                 if (!this.solidSet.test(contextMaterialHash)) {
                                    stack.push(poppedPos.clone());
                                    this.rMask.set(4096 | poppedMaskValue, poppedPos.x, poppedPos.y, poppedPos.z);
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

         this.rMaterialProviderContext.distanceToBiomeEdge = context.distanceToBiomeEdge;

         for (int var15 = this.rLocalWriteBounds.min.y; var15 < this.rLocalWriteBounds.max.y; var15++) {
            for (int x = this.rLocalWriteBounds.min.x; x < this.rLocalWriteBounds.max.x; x++) {
               for (int zxx = this.rLocalWriteBounds.min.z; zxx < this.rLocalWriteBounds.max.z; zxx++) {
                  int maskValuex = this.rMask.get(x, var15, zxx);
                  if (!isSolid(maskValuex) && !isLeaks(maskValuex)) {
                     this.rMaterialProviderContext.position.assign(x, var15, zxx);
                     Material material = this.fillerMaterialProvider.getVoxelTypeAt(this.rMaterialProviderContext);
                     context.materialWriteSpace.set(material, x, var15, zxx);
                  }
               }
            }
         }

         return true;
      }
   }

   @NonNullDecl
   @Override
   public Bounds3i getReadBounds_voxelGrid() {
      return this.bounds;
   }

   @NonNullDecl
   @Override
   public Bounds3i getWriteBounds_voxelGrid() {
      return this.bounds;
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
