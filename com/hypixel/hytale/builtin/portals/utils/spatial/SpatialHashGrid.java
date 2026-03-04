package com.hypixel.hytale.builtin.portals.utils.spatial;

import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;

public class SpatialHashGrid<T> {
   private final double cellSize;
   private final Map<Vector3i, List<SpatialHashGrid.Entry<T>>> grid = new Object2ObjectOpenHashMap();
   private final Map<T, SpatialHashGrid.Entry<T>> index = new Object2ObjectOpenHashMap();

   public SpatialHashGrid(double cellSize) {
      this.cellSize = cellSize;
   }

   private Vector3i cellFor(Vector3d p) {
      return new Vector3i(MathUtil.floor(p.x / this.cellSize), MathUtil.floor(p.y / this.cellSize), MathUtil.floor(p.z / this.cellSize));
   }

   public Collection<? extends T> getAll() {
      return Collections.unmodifiableSet(this.index.keySet());
   }

   public int size() {
      return this.index.size();
   }

   public boolean isEmpty() {
      return this.index.isEmpty();
   }

   public void add(Vector3d pos, T value) {
      Vector3i cell = this.cellFor(pos);
      SpatialHashGrid.Entry<T> entry = new SpatialHashGrid.Entry<>(pos.clone(), cell, value);
      this.index.put(value, entry);
      this.grid.computeIfAbsent(cell, x -> new ObjectArrayList()).add(entry);
   }

   public boolean remove(T value) {
      SpatialHashGrid.Entry<T> entry = this.index.remove(value);
      if (entry == null) {
         return false;
      } else {
         List<SpatialHashGrid.Entry<T>> bucket = this.grid.get(entry.cell);
         if (bucket != null) {
            bucket.remove(entry);
            if (bucket.isEmpty()) {
               this.grid.remove(entry.cell);
            }
         }

         return true;
      }
   }

   public void removeIf(Predicate<T> predicate) {
      Set<T> toRemove = new HashSet<>();

      for (T elem : this.index.keySet()) {
         if (predicate.test(elem)) {
            toRemove.add(elem);
         }
      }

      for (T elemx : toRemove) {
         this.remove(elemx);
      }
   }

   public void move(T value, Vector3d newPos) {
      SpatialHashGrid.Entry<T> entry = this.index.get(value);
      if (entry != null) {
         Vector3i oldCell = entry.cell;
         Vector3i newCell = this.cellFor(newPos);
         entry.pos.assign(newPos);
         if (!oldCell.equals(newCell)) {
            List<SpatialHashGrid.Entry<T>> oldBucket = this.grid.get(oldCell);
            oldBucket.remove(entry);
            if (oldBucket.isEmpty()) {
               this.grid.remove(oldCell);
            }

            entry.cell = newCell;
            this.grid.computeIfAbsent(newCell, x -> new ObjectArrayList()).add(entry);
         }
      }
   }

   public Map<T, Vector3d> queryRange(Vector3d center, double radius) {
      Map<T, Vector3d> out = new Object2ObjectOpenHashMap();
      double radiusSq = radius * radius;
      this.query(center, radius, bucket -> {
         for (SpatialHashGrid.Entry<T> entry : bucket) {
            if (entry.pos.distanceSquaredTo(center) <= radiusSq) {
               out.put(entry.value, entry.pos);
            }
         }

         return true;
      });
      return out;
   }

   @Nullable
   public T findClosest(final Vector3d center, double searchRadius) {
      var closestVisitor = new SpatialHashGrid.CellVisitor<T>() {
         double closestDist = Double.MAX_VALUE;
         T closest = (T)null;

         @Override
         public boolean visit(List<SpatialHashGrid.Entry<T>> bucket) {
            for (SpatialHashGrid.Entry<T> entry : bucket) {
               double dist = entry.pos.distanceSquaredTo(center);
               if (dist <= this.closestDist) {
                  this.closestDist = dist;
                  this.closest = entry.value;
               }
            }

            return true;
         }
      };
      this.query(center, searchRadius, closestVisitor);
      return closestVisitor.closest;
   }

   public boolean hasAnyWithin(final Vector3d center, final double radius) {
      var withinVisitor = new SpatialHashGrid.CellVisitor<T>() {
         final double radiusSq = radius * radius;
         boolean hasWithin = false;

         @Override
         public boolean visit(List<SpatialHashGrid.Entry<T>> bucket) {
            for (SpatialHashGrid.Entry<T> entry : bucket) {
               double dist = entry.pos.distanceSquaredTo(center);
               if (dist <= this.radiusSq) {
                  this.hasWithin = true;
                  return false;
               }
            }

            return true;
         }
      };
      this.query(center, radius, withinVisitor);
      return withinVisitor.hasWithin;
   }

   private void query(Vector3d center, double radius, SpatialHashGrid.CellVisitor<T> visitor) {
      int minX = MathUtil.floor((center.x - radius) / this.cellSize);
      int minY = MathUtil.floor((center.y - radius) / this.cellSize);
      int minZ = MathUtil.floor((center.z - radius) / this.cellSize);
      int maxX = MathUtil.floor((center.x + radius) / this.cellSize);
      int maxY = MathUtil.floor((center.y + radius) / this.cellSize);
      int maxZ = MathUtil.floor((center.z + radius) / this.cellSize);
      Vector3i lookup = new Vector3i();

      for (int x = minX; x <= maxX; x++) {
         for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
               lookup.assign(x, y, z);
               List<SpatialHashGrid.Entry<T>> bucket = this.grid.get(lookup);
               if (bucket != null) {
                  boolean keepGoing = visitor.visit(bucket);
                  if (!keepGoing) {
                     return;
                  }
               }
            }
         }
      }
   }

   private interface CellVisitor<T> {
      boolean visit(List<SpatialHashGrid.Entry<T>> var1);
   }

   private static final class Entry<T> {
      private final Vector3d pos;
      private Vector3i cell;
      private final T value;

      private Entry(Vector3d pos, Vector3i cell, T value) {
         this.pos = pos;
         this.cell = cell;
         this.value = value;
      }
   }
}
