package com.hypixel.hytale.server.core.universe.world.chunk.environment;

import com.hypixel.hytale.server.core.util.io.MemorySegmentUtil;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.lang.foreign.MemorySegment;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EnvironmentColumn {
   public static final int MIN = Integer.MIN_VALUE;
   public static final int MAX = Integer.MAX_VALUE;
   @Nonnull
   private IntArrayList maxYs;
   @Nonnull
   private IntArrayList values;

   public EnvironmentColumn(@Nonnull int[] maxYs, @Nonnull int[] values) {
      this(new IntArrayList(maxYs), new IntArrayList(values));
   }

   public EnvironmentColumn(@Nonnull IntArrayList maxYs, @Nonnull IntArrayList values) {
      if (maxYs.size() + 1 != values.size()) {
         throw new IllegalStateException("maxY + 1 != values");
      } else {
         this.maxYs = maxYs;
         this.values = values;
      }
   }

   public EnvironmentColumn(int initialId) {
      this(new IntArrayList(0), new IntArrayList(new int[]{initialId}));
   }

   int maxys_size() {
      return this.maxYs.size();
   }

   public int size() {
      return this.values.size();
   }

   public int getValue(int index) {
      return this.values.getInt(index);
   }

   public int getValueMin(int index) {
      return index <= 0 ? Integer.MIN_VALUE : this.maxYs.getInt(index - 1) + 1;
   }

   public int getValueMax(int index) {
      return index >= this.maxYs.size() ? Integer.MAX_VALUE : this.maxYs.getInt(index);
   }

   public int indexOf(int y) {
      int n = this.maxYs.size();
      if (n == 0) {
         return 0;
      } else {
         int l = 0;
         int r = n - 1;
         int i = n;

         while (l <= r) {
            int mid = (l + r) / 2;
            if (this.maxYs.getInt(mid) < y) {
               l = mid + 1;
            } else {
               i = mid;
               r = mid - 1;
            }
         }

         return i;
      }
   }

   public void set(int value) {
      this.maxYs.clear();
      this.values.clear();
      this.values.add(value);
   }

   public int get(int y) {
      return this.values.getInt(this.indexOf(y));
   }

   public void set(int y, int value) {
      int idx = this.indexOf(y);
      int currentValue = this.values.getInt(idx);
      if (currentValue != value) {
         int keys = this.maxYs.size();
         int max = Integer.MAX_VALUE;
         if (idx < keys) {
            max = this.maxYs.getInt(idx);
         }

         int min = Integer.MIN_VALUE;
         if (idx > 0) {
            min = this.maxYs.getInt(idx - 1) + 1;
         }

         if (min == max) {
            if (idx < keys && this.values.getInt(idx + 1) == value) {
               this.maxYs.removeInt(idx);
               this.values.removeInt(idx);
            } else {
               this.values.set(idx, value);
            }

            if (idx != 0 && this.values.getInt(idx - 1) == value) {
               this.maxYs.removeInt(idx - 1);
               this.values.removeInt(idx - 1);
            }
         } else if (min == y) {
            if (idx != 0 && this.values.getInt(idx - 1) == value) {
               this.maxYs.set(idx - 1, y);
            } else {
               this.maxYs.add(idx, y);
               this.values.add(idx, value);
            }
         } else if (max == y) {
            if (idx == keys) {
               this.maxYs.add(idx, y - 1);
               this.values.add(idx + 1, value);
            } else {
               this.maxYs.set(idx, y - 1);
               if (this.values.getInt(idx + 1) != value) {
                  this.maxYs.add(idx + 1, y);
                  this.values.add(idx + 1, value);
               }
            }
         } else {
            this.maxYs.add(idx, y);
            this.values.add(idx, value);
            this.maxYs.add(idx, y - 1);
            this.values.add(idx, currentValue);
         }
      }
   }

   public int getMin(int y) {
      int idx = this.indexOf(y);
      int min = Integer.MIN_VALUE;
      if (idx > 0) {
         min = this.maxYs.getInt(idx - 1) + 1;
      }

      return min;
   }

   public int getMax(int y) {
      int idx = this.indexOf(y);
      int keys = this.maxYs.size();
      int max = Integer.MAX_VALUE;
      if (idx < keys) {
         max = this.maxYs.getInt(idx);
      }

      return max;
   }

   public void set(int fromY, int toY, int value) {
      for (int y = fromY; y <= toY; y++) {
         this.set(y, value);
      }
   }

   public void resetTo(@Nonnull int[] maxYs, @Nonnull int[] values) {
      assert maxYs.length == values.length - 1;

      this.maxYs = new IntArrayList(maxYs);
      this.values = new IntArrayList(values);
   }

   public int serialize(@Nonnull MemorySegment data, int baseOffset) {
      int n = this.maxYs.size();
      data.set(MemorySegmentUtil.INT_BE, (long)baseOffset, n);
      int offset = baseOffset + 4;
      MemorySegment.copy(this.maxYs.elements(), 0, data, MemorySegmentUtil.INT_BE, offset, n);
      offset += 4 * n;
      MemorySegment.copy(this.values.elements(), 0, data, MemorySegmentUtil.INT_BE, offset, n + 1);
      offset += 4 * (n + 1);
      return offset - baseOffset;
   }

   public int serializeProtocol(@Nonnull MemorySegment data, int baseOffset) {
      int n = this.maxYs.size();
      data.set(MemorySegmentUtil.SHORT_LE, (long)baseOffset, (short)(n + 1));
      int offset = baseOffset + 2;
      int min = Integer.MIN_VALUE;

      for (int i = 0; i < n; i++) {
         data.set(MemorySegmentUtil.SHORT_LE, (long)offset, (short)min);
         offset += 2;
         data.set(MemorySegmentUtil.SHORT_LE, (long)offset, (short)this.values.getInt(i));
         offset += 2;
         int max = this.maxYs.getInt(i);
         min = max + 1;
      }

      data.set(MemorySegmentUtil.SHORT_LE, (long)offset, (short)min);
      offset += 2;
      data.set(MemorySegmentUtil.SHORT_LE, (long)offset, (short)this.values.getInt(n));
      offset += 2;
      return offset - baseOffset;
   }

   public int deserialize(@Nonnull MemorySegment data, int baseOffset, @Nonnull Int2IntMap idMapping) {
      this.maxYs.clear();
      this.values.clear();
      int n = data.get(MemorySegmentUtil.INT_BE, (long)baseOffset);
      int offset = baseOffset + 4;
      this.maxYs.size(n);
      this.values.ensureCapacity(n + 1);
      MemorySegment.copy(data, MemorySegmentUtil.INT_BE, offset, this.maxYs.elements(), 0, n);
      offset += 4 * n;

      for (int i = 0; i <= n; i++) {
         this.values.add(idMapping.get(data.get(MemorySegmentUtil.INT_BE, (long)offset)));
         offset += 4;
      }

      return offset - baseOffset;
   }

   public void copyFrom(@Nonnull EnvironmentColumn other) {
      this.maxYs.clear();
      this.values.clear();
      this.maxYs.ensureCapacity(other.maxYs.size());
      this.values.ensureCapacity(other.values.size());
      this.maxYs.addAll(other.maxYs);
      this.values.addAll(other.values);
   }

   public void trim() {
      this.maxYs.trim();
      this.values.trim();
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         EnvironmentColumn that = (EnvironmentColumn)o;
         if (this.maxYs != null ? this.maxYs.equals(that.maxYs) : that.maxYs == null) {
            return this.values != null ? this.values.equals(that.values) : that.values == null;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.maxYs != null ? this.maxYs.hashCode() : 0;
      return 31 * result + (this.values != null ? this.values.hashCode() : 0);
   }

   @Nonnull
   @Override
   public String toString() {
      return "EnvironmentColumn{maxYs=" + this.maxYs + ", values=" + this.values + "}";
   }
}
