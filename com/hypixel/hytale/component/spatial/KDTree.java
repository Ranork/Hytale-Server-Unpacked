package com.hypixel.hytale.component.spatial;

import com.hypixel.hytale.math.vector.Vector3d;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class KDTree<T> implements SpatialStructure<T> {
   @Nonnull
   private final List<KDTree.Node<T>> nodePool = new ObjectArrayList();
   private int nodePoolIndex = 0;
   @Nonnull
   private final List<List<T>> dataListPool = new ObjectArrayList();
   private int dataListPoolIndex = 0;
   private int size;
   @Nonnull
   private final Predicate<T> collectionFilter;
   @Nullable
   private KDTree.Node<T> root;

   public KDTree(@Nonnull Predicate<T> collectionFilter) {
      this.collectionFilter = collectionFilter;
   }

   @Override
   public int size() {
      return this.size;
   }

   @Override
   public void rebuild(@Nonnull SpatialData<T> spatialData) {
      this.root = null;
      this.size = 0;
      int spatialDataSize = spatialData.size();
      if (spatialDataSize != 0) {
         for (int i = 0; i < this.dataListPoolIndex; i++) {
            this.dataListPool.get(i).clear();
         }

         this.nodePoolIndex = 0;
         this.dataListPoolIndex = 0;
         spatialData.sortMorton();
         int mid = spatialDataSize / 2;
         int sortedIndex = spatialData.getSortedIndex(mid);
         Vector3d vector = spatialData.getVector(sortedIndex);
         T data = spatialData.getData(sortedIndex);
         List<T> list = this.getPooledDataList();
         list.add(data);

         int left;
         for (left = mid - 1; left >= 0; left--) {
            int leftSortedIndex = spatialData.getSortedIndex(left);
            Vector3d leftVector = spatialData.getVector(leftSortedIndex);
            if (!leftVector.equals(vector)) {
               break;
            }

            T leftData = spatialData.getData(leftSortedIndex);
            list.add(leftData);
         }

         int right;
         for (right = mid + 1; right < spatialDataSize; right++) {
            int rightSortedIndex = spatialData.getSortedIndex(right);
            Vector3d rightVector = spatialData.getVector(rightSortedIndex);
            if (!rightVector.equals(vector)) {
               break;
            }

            T rightData = spatialData.getData(rightSortedIndex);
            list.add(rightData);
         }

         this.root = this.getPooledNode(vector, list);
         if (0 < left + 1) {
            this.build0(spatialData, 0, left + 1);
         }

         if (right < spatialDataSize) {
            this.build0(spatialData, right, spatialDataSize);
         }

         this.size = spatialDataSize;
      }
   }

   @Nullable
   @Override
   public T closest(@Nonnull Vector3d point) {
      KDTree.ClosestState<T> closestState = new KDTree.ClosestState<>(null, Double.MAX_VALUE);
      this.closest0(closestState, this.root, point, 0);
      return closestState.node == null ? null : closestState.node.data.getFirst();
   }

   @Override
   public void collect(@Nonnull Vector3d center, double radius, @Nonnull List<T> results) {
      double distanceSq = radius * radius;
      this.collect0(results, this.root, center, distanceSq, 0);
   }

   @Override
   public void collectCylinder(@Nonnull Vector3d center, double radius, double height, @Nonnull List<T> results) {
      double radiusSq = radius * radius;
      double halfHeight = height / 2.0;
      this.collectCylinder0(results, this.root, center, radiusSq, halfHeight, radius, 0);
   }

   @Override
   public void collectBox(@Nonnull Vector3d min, @Nonnull Vector3d max, @Nonnull List<T> results) {
      this.collectBox0(results, this.root, min, max, 0);
   }

   @Override
   public void ordered(@Nonnull Vector3d center, double radius, @Nonnull List<T> results) {
      double distanceSq = radius * radius;
      ObjectArrayList<KDTree.OrderedEntry<T>> entryResults = new ObjectArrayList();
      this.ordered0(entryResults, this.root, center, distanceSq, 0);
      entryResults.sort(Comparator.comparingDouble(o -> o.distanceSq));
      ObjectListIterator var8 = entryResults.iterator();

      while (var8.hasNext()) {
         KDTree.OrderedEntry<T> entry = (KDTree.OrderedEntry<T>)var8.next();
         int i = 0;

         for (int bound = entry.values.size(); i < bound; i++) {
            T data = entry.values.get(i);
            if (this.collectionFilter.test(data)) {
               results.add(data);
            }
         }
      }
   }

   @Override
   public void ordered3DAxis(@Nonnull Vector3d center, double xSearchRadius, double YSearchRadius, double zSearchRadius, @Nonnull List<T> results) {
      ObjectArrayList<KDTree.OrderedEntry<T>> entryResults = new ObjectArrayList();
      this._internal_ordered3DAxis(entryResults, this.root, center, xSearchRadius, YSearchRadius, zSearchRadius, 0);
      entryResults.sort(Comparator.comparingDouble(o -> o.distanceSq));
      ObjectListIterator var10 = entryResults.iterator();

      while (var10.hasNext()) {
         KDTree.OrderedEntry<T> entry = (KDTree.OrderedEntry<T>)var10.next();
         int i = 0;

         for (int bound = entry.values.size(); i < bound; i++) {
            T data = entry.values.get(i);
            if (this.collectionFilter.test(data)) {
               results.add(data);
            }
         }
      }
   }

   @Nonnull
   @Override
   public String dump() {
      return "KDTree(size=" + this.size + ")\n" + (this.root == null ? null : this.root.dump(0));
   }

   @Nonnull
   private KDTree.Node<T> getPooledNode(Vector3d vector, List<T> data) {
      if (this.nodePoolIndex < this.nodePool.size()) {
         KDTree.Node<T> node = this.nodePool.get(this.nodePoolIndex++);
         node.reset(vector, data);
         return node;
      } else {
         KDTree.Node<T> node = new KDTree.Node<>(vector, data);
         this.nodePool.add(node);
         this.nodePoolIndex++;
         return node;
      }
   }

   private List<T> getPooledDataList() {
      if (this.dataListPoolIndex < this.dataListPool.size()) {
         return this.dataListPool.get(this.dataListPoolIndex++);
      } else {
         ObjectArrayList<T> set = new ObjectArrayList(1);
         this.dataListPool.add(set);
         this.dataListPoolIndex++;
         return set;
      }
   }

   private void build0(@Nonnull SpatialData<T> spatialData, int start, int end) {
      int mid = (start + end) / 2;
      int sortedIndex = spatialData.getSortedIndex(mid);
      Vector3d vector = spatialData.getVector(sortedIndex);
      T data = spatialData.getData(sortedIndex);
      List<T> list = this.getPooledDataList();
      list.add(data);

      int left;
      for (left = mid - 1; left >= start; left--) {
         int leftSortedIndex = spatialData.getSortedIndex(left);
         Vector3d leftVector = spatialData.getVector(leftSortedIndex);
         if (!leftVector.equals(vector)) {
            break;
         }

         T leftData = spatialData.getData(leftSortedIndex);
         list.add(leftData);
      }

      int right;
      for (right = mid + 1; right < end; right++) {
         int rightSortedIndex = spatialData.getSortedIndex(right);
         Vector3d rightVector = spatialData.getVector(rightSortedIndex);
         if (!rightVector.equals(vector)) {
            break;
         }

         T rightData = spatialData.getData(rightSortedIndex);
         list.add(rightData);
      }

      this.put0(this.root, vector, list, 0);
      if (start < left + 1) {
         this.build0(spatialData, start, left + 1);
      }

      if (right < end) {
         this.build0(spatialData, right, end);
      }
   }

   private void put0(@Nonnull KDTree.Node<T> node, @Nonnull Vector3d vector, @Nonnull List<T> list, int axis) {
      if (compare(node.vector, vector, axis) < 0) {
         if (node.one == null) {
            node.one = this.getPooledNode(vector, list);
         } else {
            this.put0(node.one, vector, list, (axis + 1) % 3);
         }
      } else if (node.two == null) {
         node.two = this.getPooledNode(vector, list);
      } else {
         this.put0(node.two, vector, list, (axis + 1) % 3);
      }
   }

   private void closest0(@Nonnull KDTree.ClosestState<T> closestState, @Nullable KDTree.Node<T> node, @Nonnull Vector3d vector, int depth) {
      if (node != null) {
         if (vector.equals(node.vector)) {
            closestState.distanceSq = 0.0;
            closestState.node = node;
         } else {
            int axis = depth % 3;
            int compare = compare(node.vector, vector, axis);
            double distanceSq = node.vector.distanceSquaredTo(vector);
            if (distanceSq < closestState.distanceSq) {
               closestState.node = node;
               closestState.distanceSq = distanceSq;
            }

            int newDepth = depth + 1;
            if (compare < 0) {
               this.closest0(closestState, node.one, vector, newDepth);
            } else {
               this.closest0(closestState, node.two, vector, newDepth);
            }

            double plane = get(node.vector, axis);
            double component = get(closestState.node.vector, axis);
            double planeDistance = Math.abs(component - plane);
            if (planeDistance * planeDistance < closestState.distanceSq) {
               if (compare < 0) {
                  this.closest0(closestState, node.two, vector, newDepth);
               } else {
                  this.closest0(closestState, node.one, vector, newDepth);
               }
            }
         }
      }
   }

   private void collect0(@Nonnull List<T> results, @Nullable KDTree.Node<T> node, @Nonnull Vector3d vector, double distanceSq, int depth) {
      if (node != null) {
         int axis = depth % 3;
         int compare = compare(node.vector, vector, axis);
         double nodeDistanceSq = node.vector.distanceSquaredTo(vector);
         if (nodeDistanceSq < distanceSq) {
            int i = 0;

            for (int bound = node.data.size(); i < bound; i++) {
               T data = node.data.get(i);
               if (this.collectionFilter.test(data)) {
                  results.add(data);
               }
            }
         }

         int newDepth = depth + 1;
         if (compare < 0) {
            this.collect0(results, node.one, vector, distanceSq, newDepth);
         } else {
            this.collect0(results, node.two, vector, distanceSq, newDepth);
         }

         double plane = get(node.vector, axis);
         double component = get(vector, axis);
         double planeDistance = Math.abs(component - plane);
         if (planeDistance * planeDistance < distanceSq) {
            if (compare < 0) {
               this.collect0(results, node.two, vector, distanceSq, newDepth);
            } else {
               this.collect0(results, node.one, vector, distanceSq, newDepth);
            }
         }
      }
   }

   private void collectCylinder0(
      @Nonnull List<T> results, @Nullable KDTree.Node<T> node, @Nonnull Vector3d center, double radiusSq, double halfHeight, double radius, int depth
   ) {
      if (node != null) {
         int axis = depth % 3;
         int compare = compare(node.vector, center, axis);
         double dy = node.vector.y - center.y;
         if (Math.abs(dy) <= halfHeight) {
            double dx = node.vector.x - center.x;
            double dz = node.vector.z - center.z;
            double xzDistanceSq = dx * dx + dz * dz;
            if (xzDistanceSq <= radiusSq) {
               int i = 0;

               for (int bound = node.data.size(); i < bound; i++) {
                  T data = node.data.get(i);
                  if (this.collectionFilter.test(data)) {
                     results.add(data);
                  }
               }
            }
         }

         int newDepth = depth + 1;
         if (compare < 0) {
            this.collectCylinder0(results, node.one, center, radiusSq, halfHeight, radius, newDepth);
         } else {
            this.collectCylinder0(results, node.two, center, radiusSq, halfHeight, radius, newDepth);
         }

         double plane = get(node.vector, axis);
         double component = get(center, axis);
         double axisRadius = axis == 2 ? halfHeight : radius;
         if (Math.abs(component - plane) <= axisRadius) {
            if (compare < 0) {
               this.collectCylinder0(results, node.two, center, radiusSq, halfHeight, radius, newDepth);
            } else {
               this.collectCylinder0(results, node.one, center, radiusSq, halfHeight, radius, newDepth);
            }
         }
      }
   }

   private void collectBox0(@Nonnull List<T> results, @Nullable KDTree.Node<T> node, @Nonnull Vector3d min, @Nonnull Vector3d max, int depth) {
      if (node != null) {
         int axis = depth % 3;
         if (node.vector.x >= min.x
            && node.vector.x <= max.x
            && node.vector.y >= min.y
            && node.vector.y <= max.y
            && node.vector.z >= min.z
            && node.vector.z <= max.z) {
            int i = 0;

            for (int bound = node.data.size(); i < bound; i++) {
               T data = node.data.get(i);
               if (this.collectionFilter.test(data)) {
                  results.add(data);
               }
            }
         }

         int newDepth = depth + 1;
         double plane = get(node.vector, axis);
         double minComponent = get(min, axis);
         double maxComponent = get(max, axis);
         if (maxComponent >= plane) {
            this.collectBox0(results, node.one, min, max, newDepth);
         }

         if (minComponent <= plane) {
            this.collectBox0(results, node.two, min, max, newDepth);
         }
      }
   }

   private void ordered0(@Nonnull List<KDTree.OrderedEntry<T>> results, @Nullable KDTree.Node<T> node, @Nonnull Vector3d vector, double distanceSq, int depth) {
      if (node != null) {
         int axis = depth % 3;
         int compare = compare(node.vector, vector, axis);
         double nodeDistanceSq = node.vector.distanceSquaredTo(vector);
         if (nodeDistanceSq < distanceSq) {
            results.add(new KDTree.OrderedEntry<>(nodeDistanceSq, node.data));
         }

         int newDepth = depth + 1;
         if (compare < 0) {
            this.ordered0(results, node.one, vector, distanceSq, newDepth);
         } else {
            this.ordered0(results, node.two, vector, distanceSq, newDepth);
         }

         double plane = get(node.vector, axis);
         double component = get(vector, axis);
         double planeDistance = Math.abs(component - plane);
         if (planeDistance * planeDistance < distanceSq) {
            if (compare < 0) {
               this.ordered0(results, node.two, vector, distanceSq, newDepth);
            } else {
               this.ordered0(results, node.one, vector, distanceSq, newDepth);
            }
         }
      }
   }

   private void _internal_ordered3DAxis(
      @Nonnull List<KDTree.OrderedEntry<T>> results,
      @Nullable KDTree.Node<T> node,
      @Nonnull Vector3d center,
      double xSearchRadius,
      double ySearchRadius,
      double zSearchRadius,
      int depth
   ) {
      if (node != null) {
         int axis = depth % 3;
         boolean inCuboid = node.vector.x >= center.x - xSearchRadius
            && node.vector.x <= center.x + xSearchRadius
            && node.vector.y >= center.y - ySearchRadius
            && node.vector.y <= center.y + ySearchRadius
            && node.vector.z >= center.z - zSearchRadius
            && node.vector.z <= center.z + zSearchRadius;
         if (inCuboid) {
            double nodeDistanceSq = node.vector.distanceSquaredTo(center);
            results.add(new KDTree.OrderedEntry<>(nodeDistanceSq, node.data));
         }

         int newDepth = depth + 1;
         int compare = compare(node.vector, center, axis);
         KDTree.Node<T> primary = compare < 0 ? node.one : node.two;
         KDTree.Node<T> secondary = compare < 0 ? node.two : node.one;
         this._internal_ordered3DAxis(results, primary, center, xSearchRadius, ySearchRadius, zSearchRadius, newDepth);
         double plane = get(node.vector, axis);
         double component = get(center, axis);
         double radius = axis == 0 ? xSearchRadius : (axis == 1 ? zSearchRadius : ySearchRadius);
         if (Math.abs(component - plane) <= radius) {
            this._internal_ordered3DAxis(results, secondary, center, xSearchRadius, ySearchRadius, zSearchRadius, newDepth);
         }
      }
   }

   private static int compare(@Nonnull Vector3d v1, @Nonnull Vector3d v2, int axis) {
      return switch (axis) {
         case 0 -> Double.compare(v1.x, v2.x);
         case 1 -> Double.compare(v1.z, v2.z);
         case 2 -> Double.compare(v1.y, v2.y);
         default -> throw new IllegalArgumentException("Invalid axis: " + axis);
      };
   }

   private static double get(@Nonnull Vector3d v, int axis) {
      return switch (axis) {
         case 0 -> v.x;
         case 1 -> v.z;
         case 2 -> v.y;
         default -> throw new IllegalArgumentException("Invalid axis: " + axis);
      };
   }

   private static class ClosestState<T> {
      private KDTree.Node<T> node;
      private double distanceSq;

      public ClosestState(KDTree.Node<T> node, double distanceSq) {
         this.node = node;
         this.distanceSq = distanceSq;
      }
   }

   private static class Node<T> {
      private Vector3d vector;
      private List<T> data;
      @Nullable
      private KDTree.Node<T> one;
      @Nullable
      private KDTree.Node<T> two;

      public Node(Vector3d vector, List<T> data) {
         this.vector = vector;
         this.data = data;
      }

      public void reset(Vector3d vector, List<T> data) {
         this.vector = vector;
         this.data = data;
         this.one = null;
         this.two = null;
      }

      @Nonnull
      public String dump(int depth) {
         int nextDepth = depth + 1;
         return "vector="
            + this.vector
            + ", data="
            + this.data
            + ",\n"
            + " ".repeat(depth)
            + "one="
            + (this.one == null ? null : this.one.dump(nextDepth))
            + ",\n"
            + " ".repeat(depth)
            + "two="
            + (this.two == null ? null : this.two.dump(nextDepth));
      }
   }

   private static class OrderedEntry<T> {
      private final double distanceSq;
      private final List<T> values;

      public OrderedEntry(double distanceSq, List<T> values) {
         this.distanceSq = distanceSq;
         this.values = values;
      }
   }
}
