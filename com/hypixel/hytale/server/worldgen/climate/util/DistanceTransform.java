package com.hypixel.hytale.server.worldgen.climate.util;

import com.hypixel.hytale.math.util.MathUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Arrays;
import java.util.PriorityQueue;
import javax.annotation.Nonnull;

public class DistanceTransform {
   private static final IntArrayList EMPTY_LIST = new IntArrayList();
   private static final int[] DX = new int[]{-1, 1, 0, 0, -1, -1, 1, 1};
   private static final int[] DY = new int[]{0, 0, -1, 1, -1, 1, -1, 1};
   private static final double[] COST = new double[]{1.0, 1.0, 1.0, 1.0, Math.sqrt(2.0), Math.sqrt(2.0), Math.sqrt(2.0), Math.sqrt(2.0)};

   public static void apply(@Nonnull IntMap source, @Nonnull DoubleMap dest, double radius) {
      if (radius <= 0.0) {
         throw new IllegalArgumentException("radius must be > 0");
      } else {
         int width = source.width;
         int height = source.height;
         int size = width * height;
         Int2ObjectOpenHashMap<IntArrayList> regions = new Int2ObjectOpenHashMap();
         Int2ObjectOpenHashMap<IntArrayList> boundaries = new Int2ObjectOpenHashMap();

         for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
               int index = source.index(x, y);
               int value = source.at(index);
               ((IntArrayList)regions.computeIfAbsent(value, k -> new IntArrayList())).add(index);

               for (int i = 0; i < 4; i++) {
                  int nx = x + DX[i];
                  int ny = y + DY[i];
                  if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                     int neighborIndex = source.index(nx, ny);
                     if (source.at(neighborIndex) != value) {
                        ((IntArrayList)boundaries.computeIfAbsent(value, k -> new IntArrayList())).add(index);
                        break;
                     }
                  }
               }
            }
         }

         double[] dist = new double[size];
         PriorityQueue<DistanceTransform.Node> queue = new PriorityQueue<>(DistanceTransform.Node::sort);
         ObjectIterator var29 = regions.int2ObjectEntrySet().iterator();

         while (var29.hasNext()) {
            Entry<IntArrayList> entry = (Entry<IntArrayList>)var29.next();
            int id = entry.getIntKey();
            IntArrayList region = (IntArrayList)entry.getValue();
            IntArrayList boundary = (IntArrayList)boundaries.getOrDefault(id, EMPTY_LIST);
            if (boundary.isEmpty()) {
               for (int ix = 0; ix < region.size(); ix++) {
                  dest.set(region.getInt(ix), 1.0);
               }
            } else {
               Arrays.fill(dist, radius);

               for (int ix = 0; ix < boundary.size(); ix++) {
                  int index = boundary.getInt(ix);
                  dist[index] = 0.0;
                  queue.offer(new DistanceTransform.Node(index, 0.0));
               }

               while (!queue.isEmpty()) {
                  DistanceTransform.Node node = queue.poll();
                  int index = node.index;
                  if (!(node.distance > dist[index])) {
                     int cx = index % width;
                     int cy = index / width;

                     for (int ix = 0; ix < DX.length; ix++) {
                        int nx = cx + DX[ix];
                        int ny = cy + DY[ix];
                        if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                           int neighborIndex = source.index(nx, ny);
                           int neighborId = source.at(neighborIndex);
                           if (neighborId == id) {
                              double distance = node.distance + COST[ix];
                              if (distance < dist[neighborIndex]) {
                                 dist[neighborIndex] = distance;
                                 queue.offer(new DistanceTransform.Node(neighborIndex, distance));
                              }
                           }
                        }
                     }
                  }
               }

               for (int ixx = 0; ixx < region.size(); ixx++) {
                  int index = region.getInt(ixx);
                  double value = MathUtil.clamp(dist[index], 0.0, radius);
                  dest.set(index, value / radius);
               }
            }
         }
      }
   }

   private record Node(int index, double distance) {
      public static int sort(DistanceTransform.Node a, DistanceTransform.Node b) {
         return Double.compare(a.distance, b.distance);
      }
   }
}
