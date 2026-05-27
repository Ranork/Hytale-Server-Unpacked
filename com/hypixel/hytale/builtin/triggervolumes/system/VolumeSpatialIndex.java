package com.hypixel.hytale.builtin.triggervolumes.system;

import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import com.hypixel.hytale.component.spatial.KDTree;
import com.hypixel.hytale.component.spatial.SpatialData;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import org.joml.Vector3d;

public class VolumeSpatialIndex {
   @Nonnull
   private final KDTree<VolumeEntry> tree = new KDTree<>(VolumeEntry::isEnabled);
   @Nonnull
   private final SpatialData<VolumeEntry> data = new SpatialData<>();
   private double maxBoundingRadius;
   private volatile boolean dirty;

   public void markDirty() {
      this.dirty = true;
   }

   public void rebuildIfDirty(@Nonnull Collection<VolumeEntry> volumes) {
      if (this.dirty) {
         this.dirty = false;
         this.data.clear();
         this.maxBoundingRadius = 0.0;
         int enabledCount = 0;

         for (VolumeEntry vol : volumes) {
            if (vol.isEnabled()) {
               enabledCount++;
            }
         }

         this.data.addCapacity(enabledCount);

         for (VolumeEntry volx : volumes) {
            if (volx.isEnabled()) {
               this.data.append(volx.getPosition(), volx);
               double dist = volx.getShape().getMaxDistanceFromOrigin();
               if (dist > this.maxBoundingRadius) {
                  this.maxBoundingRadius = dist;
               }
            }
         }

         this.tree.rebuild(this.data);
      }
   }

   public void collectCandidates(@Nonnull Vector3d entityPos, @Nonnull List<VolumeEntry> results) {
      if (this.tree.size() != 0) {
         this.tree.collect(entityPos, this.maxBoundingRadius, results);
         results.removeIf(volume -> {
            double maxDist = volume.getShape().getMaxDistanceFromOrigin();
            return entityPos.distanceSquared(volume.getPosition()) > maxDist * maxDist;
         });
      }
   }
}
