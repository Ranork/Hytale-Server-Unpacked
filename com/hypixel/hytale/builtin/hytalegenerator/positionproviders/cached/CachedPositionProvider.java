package com.hypixel.hytale.builtin.hytalegenerator.positionproviders.cached;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3d;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Control;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Pipe;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.math.util.HashUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.ArrayList;
import javax.annotation.Nonnull;

public class CachedPositionProvider extends PositionProvider {
   @Nonnull
   private final PositionProvider positionProvider;
   private final int sectionSize;
   private CacheThreadMemory cache;

   public CachedPositionProvider(@Nonnull PositionProvider positionProvider, int sectionSize, int cacheSize) {
      if (sectionSize > 0 && cacheSize >= 0) {
         this.positionProvider = positionProvider;
         this.sectionSize = sectionSize;
         this.cache = new CacheThreadMemory(cacheSize);
      } else {
         throw new IllegalArgumentException();
      }
   }

   @Override
   public void generate(@Nonnull PositionProvider.Context context) {
      this.get(context);
   }

   public void get(@Nonnull PositionProvider.Context context) {
      Vector3i minSection = this.sectionAddress(context.bounds.min);
      Vector3i maxSection = this.sectionAddress(context.bounds.max);
      Vector3i sectionAddress = minSection.clone();

      for (sectionAddress.x = minSection.x; sectionAddress.x <= maxSection.x; sectionAddress.x++) {
         for (sectionAddress.z = minSection.z; sectionAddress.z <= maxSection.z; sectionAddress.z++) {
            for (sectionAddress.y = minSection.y; sectionAddress.y <= maxSection.y; sectionAddress.y++) {
               long key = HashUtil.hash(sectionAddress.x, sectionAddress.y, sectionAddress.z);
               Vector3d[] section = this.cache.sections.get(key);
               if (section == null) {
                  Vector3d sectionMin = this.sectionMin(sectionAddress);
                  Bounds3d sectionBounds = new Bounds3d(sectionMin, sectionMin.clone().add(this.sectionSize, this.sectionSize, this.sectionSize));
                  ArrayList<Vector3d> generatedPositions = new ArrayList<>();
                  Pipe.One<Vector3d> pipe = (positionx, controlx) -> generatedPositions.add(positionx);
                  PositionProvider.Context childContext = new PositionProvider.Context(sectionBounds, pipe, null);
                  this.positionProvider.generate(childContext);
                  section = new Vector3d[generatedPositions.size()];
                  generatedPositions.toArray(section);
                  this.cache.sections.put(key, section);
                  this.cache.expirationList.addFirst(key);
                  if (this.cache.expirationList.size() > this.cache.size) {
                     long removedKey = this.cache.expirationList.removeLast();
                     this.cache.sections.remove(removedKey);
                  }
               }

               Control control = new Control();

               for (Vector3d position : section) {
                  if (context.bounds.contains(position)) {
                     if (control.stop) {
                        return;
                     }

                     context.pipe.accept(position.clone(), control);
                  }
               }
            }
         }
      }
   }

   @Nonnull
   private Vector3i sectionAddress(@Nonnull Vector3d pointer) {
      Vector3i address = pointer.toVector3i();
      address.x = this.sectionFloor(address.x) / this.sectionSize;
      address.y = this.sectionFloor(address.y) / this.sectionSize;
      address.z = this.sectionFloor(address.z) / this.sectionSize;
      return address;
   }

   @Nonnull
   private Vector3d sectionMin(@Nonnull Vector3i sectionAddress) {
      Vector3d min = sectionAddress.toVector3d();
      min.x = min.x * this.sectionSize;
      min.y = min.y * this.sectionSize;
      min.z = min.z * this.sectionSize;
      return min;
   }

   private int toSectionAddress(double position) {
      int positionAddress = (int)position;
      positionAddress = this.sectionFloor(positionAddress);
      return positionAddress / this.sectionSize;
   }

   public int sectionFloor(int voxelAddress) {
      return voxelAddress < 0 ? voxelAddress - voxelAddress % this.sectionSize - this.sectionSize : voxelAddress - voxelAddress % this.sectionSize;
   }
}
