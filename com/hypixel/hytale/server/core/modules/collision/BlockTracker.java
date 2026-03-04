package com.hypixel.hytale.server.core.modules.collision;

import com.hypixel.hytale.math.vector.Vector3i;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class BlockTracker implements IBlockTracker {
   public static final int NOT_FOUND = -1;
   protected static final int ALLOC_SIZE = 4;
   @Nonnull
   protected Vector3i[] positions = new Vector3i[4];
   protected int count;

   public BlockTracker() {
      for (int i = 0; i < this.positions.length; i++) {
         this.positions[i] = new Vector3i();
      }
   }

   @Override
   public Vector3i getPosition(int index) {
      return this.positions[index];
   }

   @Override
   public int getCount() {
      return this.count;
   }

   public void reset() {
      this.count = 0;
   }

   @Override
   public boolean track(int x, int y, int z) {
      if (this.isTracked(x, y, z)) {
         return true;
      } else {
         this.trackNew(x, y, z);
         return false;
      }
   }

   @Override
   public void trackNew(int x, int y, int z) {
      if (this.count >= this.positions.length) {
         this.alloc();
      }

      Vector3i v = this.positions[this.count++];
      v.x = x;
      v.y = y;
      v.z = z;
   }

   @Override
   public boolean isTracked(int x, int y, int z) {
      return this.getIndex(x, y, z) >= 0;
   }

   @Override
   public void untrack(int x, int y, int z) {
      int index = this.getIndex(x, y, z);
      if (index >= 0) {
         this.untrack(index);
      }
   }

   public void untrack(int index) {
      if (this.count <= 0) {
         throw new IllegalStateException("Calling untrack on empty tracker");
      } else {
         this.count--;
         if (this.count != 0) {
            Vector3i v = this.positions[index];
            System.arraycopy(this.positions, index + 1, this.positions, index, this.count - index);
            this.positions[this.count] = v;
         }
      }
   }

   public int getIndex(int x, int y, int z) {
      for (int i = this.count - 1; i >= 0; i--) {
         Vector3i v = this.positions[i];
         if (v.x == x && v.y == y && v.z == z) {
            return i;
         }
      }

      return -1;
   }

   protected void alloc() {
      this.positions = Arrays.copyOf(this.positions, this.positions.length + 4);

      for (int i = this.count; i < this.positions.length; i++) {
         this.positions[i] = new Vector3i();
      }
   }
}
