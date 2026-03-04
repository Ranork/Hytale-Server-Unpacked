package com.hypixel.hytale.builtin.hytalegenerator.conveyor.stagedconveyor;

import com.hypixel.hytale.builtin.hytalegenerator.VectorUtil;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

public class ContextDependency {
   @Nonnull
   public static ContextDependency EMPTY = new ContextDependency(new Vector3i(), new Vector3i());
   @Nonnull
   private final Vector3i readRange;
   @Nonnull
   private final Vector3i writeRange;
   private Vector3i trashRange;
   private Vector3i externalDependencyRange;
   private Vector3i positioningRange;

   public ContextDependency(@Nonnull Vector3i readRange, @Nonnull Vector3i writeRange) {
      this.readRange = readRange.clone();
      this.writeRange = writeRange.clone();
      this.update();
   }

   public ContextDependency() {
      this(new Vector3i(), new Vector3i());
   }

   @Nonnull
   public Bounds3i getReadBounds_voxelGrid() {
      return new Bounds3i(this.getReadRange().clone().scale(-1), this.getReadRange().clone().add(Vector3i.ALL_ONES));
   }

   @Nonnull
   public Bounds3i getWriteBounds_voxelGrid() {
      Vector3i readMin_voxelGrid = this.getReadRange().scale(-1);
      Vector3i readMax_voxelGrid = this.getReadRange().add(Vector3i.ALL_ONES);
      Vector3i writeMin_voxelGrid = this.getWriteRange().scale(-1);
      Vector3i writeMax_voxelGrid = this.getWriteRange().add(Vector3i.ALL_ONES);
      Bounds3i readBounds_voxelGrid = new Bounds3i(readMin_voxelGrid, readMax_voxelGrid);
      Bounds3i writeBounds_voxelGrid = new Bounds3i(writeMin_voxelGrid, writeMax_voxelGrid);
      writeBounds_voxelGrid.stack(readBounds_voxelGrid);
      return writeBounds_voxelGrid;
   }

   private void update() {
      this.trashRange = VectorUtil.fromOperation(this.readRange, this.writeRange, (r, w, retriever) -> r >= 0 && w >= 0 ? r + w : 0);
      this.externalDependencyRange = VectorUtil.fromOperation(
         this.readRange, this.writeRange, (r, w, retriever) -> r < 0 ? -w : Math.max(r, retriever.from(this.trashRange))
      );
      this.positioningRange = VectorUtil.fromOperation(this.readRange, this.writeRange, (r, w, retriever) -> r < 0 ? -w : r);
      this.trashRange.y = 0;
      this.externalDependencyRange.y = 0;
      this.positioningRange.y = 0;
      this.readRange.y = 0;
      this.writeRange.y = 0;
   }

   @Nonnull
   public ContextDependency stackOver(@Nonnull ContextDependency other) {
      new Vector3i();
      new Vector3i();
      Vector3i r1 = this.getReadRange();
      Vector3i w1 = this.getWriteRange();
      Vector3i r2 = other.getReadRange();
      Vector3i w2 = other.getWriteRange();
      Vector3i totalRead = VectorUtil.fromOperation(value -> {
         if (value.from(r1) < 0 && value.from(r2) < 0) {
            return -1;
         } else if (value.from(r1) < 0) {
            return value.from(r2);
         } else {
            return value.from(r2) < 0 ? value.from(r1) : value.from(r1) + value.from(w1) + value.from(r2);
         }
      });
      Vector3i totalWrite = VectorUtil.fromOperation(value -> {
         if (value.from(r1) < 0 && value.from(r2) < 0) {
            return -Math.min(value.from(w1), value.from(w2));
         } else if (value.from(r1) < 0) {
            return value.from(w2);
         } else {
            return value.from(r2) < 0 ? value.from(w1) : value.from(w2);
         }
      });
      return new ContextDependency(totalRead, totalWrite);
   }

   @Nonnull
   public Vector3i getReadRange() {
      return this.readRange.clone();
   }

   @Nonnull
   public Vector3i getWriteRange() {
      return this.writeRange.clone();
   }

   @Nonnull
   public Vector3i getTrashRange() {
      return this.trashRange.clone();
   }

   @Nonnull
   public Vector3i getExternalDependencyRange() {
      return this.externalDependencyRange.clone();
   }

   @Nonnull
   public Vector3i getPositioningRange() {
      return this.positioningRange.clone();
   }

   @Nonnull
   public static Vector3i getRequiredPadOf(@Nonnull List<ContextDependency> dependencies) {
      Vector3i pad = new Vector3i();

      for (ContextDependency dependency : dependencies) {
         pad.add(dependency.getExternalDependencyRange());
      }

      return pad;
   }

   @Nonnull
   public static Map<Integer, ContextDependency> cloneMap(@Nonnull Map<Integer, ContextDependency> map) {
      HashMap<Integer, ContextDependency> out = new HashMap<>(map.size());
      map.forEach((k, v) -> out.put(k, v.clone()));
      return out;
   }

   @Nonnull
   public static Map<Integer, ContextDependency> stackMaps(@Nonnull Map<Integer, ContextDependency> under, @Nonnull Map<Integer, ContextDependency> over) {
      Map<Integer, ContextDependency> out = new HashMap<>();

      for (Entry<Integer, ContextDependency> entry : over.entrySet()) {
         if (!under.containsKey(entry.getKey())) {
            out.put(entry.getKey(), entry.getValue());
         } else {
            out.put(entry.getKey(), entry.getValue().stackOver(under.get(entry.getKey())));
         }
      }

      for (Entry<Integer, ContextDependency> entryx : under.entrySet()) {
         if (!over.containsKey(entryx.getKey())) {
            out.put(entryx.getKey(), entryx.getValue());
         }
      }

      return out;
   }

   @Nonnull
   public static ContextDependency mostOf(@Nonnull List<ContextDependency> dependencies) {
      ContextDependency out = EMPTY;

      for (ContextDependency d : dependencies) {
         out = mostOf(out, d);
      }

      return out;
   }

   @Nonnull
   public static ContextDependency mostOf(@Nonnull ContextDependency a, @Nonnull ContextDependency b) {
      Vector3i read = Vector3i.max(a.readRange, b.readRange);
      Vector3i write = Vector3i.max(a.writeRange, b.writeRange);
      return new ContextDependency(read, write);
   }

   @Nonnull
   public static ContextDependency from(@Nonnull Bounds3i readBounds, @Nonnull Bounds3i writeBounds) {
      return new ContextDependency(rangeFromBounds(readBounds), rangeFromBounds(writeBounds));
   }

   @Nonnull
   private static Vector3i rangeFromBounds(@Nonnull Bounds3i readBounds) {
      Vector3i readRange = new Vector3i();
      readRange.x = Math.max(Math.abs(readBounds.min.x), Math.abs(readBounds.max.x - 1));
      readRange.y = Math.max(Math.abs(readBounds.min.y), Math.abs(readBounds.max.y - 1));
      readRange.z = Math.max(Math.abs(readBounds.min.z), Math.abs(readBounds.max.z - 1));
      return readRange;
   }

   @Nonnull
   public ContextDependency clone() {
      return new ContextDependency(this.readRange, this.writeRange);
   }
}
