package com.hypixel.hytale.server.core.modules.collision;

import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CollisionTracker extends BlockTracker {
   @Nonnull
   protected BlockData[] blockData = new BlockData[4];
   @Nonnull
   protected BlockContactData[] contactData = new BlockContactData[4];

   public CollisionTracker() {
      for (int i = 0; i < 4; i++) {
         this.blockData[i] = new BlockData();
         this.contactData[i] = new BlockContactData();
      }
   }

   public BlockData getBlockData(int index) {
      return this.blockData[index];
   }

   public BlockContactData getContactData(int index) {
      return this.contactData[index];
   }

   @Override
   public void reset() {
      super.reset();

      for (int i = 0; i < this.count; i++) {
         this.blockData[i].clear();
         this.contactData[i].clear();
      }
   }

   public boolean track(int x, int y, int z, @Nonnull BlockContactData contactData, @Nonnull BlockData blockData) {
      if (this.isTracked(x, y, z)) {
         return true;
      } else {
         this.trackNew(x, y, z, contactData, blockData);
         return false;
      }
   }

   @Nonnull
   public BlockContactData trackNew(int x, int y, int z, @Nonnull BlockContactData contactData, @Nonnull BlockData blockData) {
      super.trackNew(x, y, z);
      this.blockData[this.count - 1].assign(blockData);
      BlockContactData data = this.contactData[this.count - 1];
      data.assign(contactData);
      return data;
   }

   @Override
   public void untrack(int index) {
      super.untrack(index);
      if (this.count == 0) {
         this.blockData[0].clear();
         this.contactData[0].clear();
      } else {
         int length = this.count - index;
         BlockData block = this.blockData[index];
         block.clear();
         System.arraycopy(this.blockData, index + 1, this.blockData, index, length);
         this.blockData[this.count] = block;
         BlockContactData coll = this.contactData[index];
         coll.clear();
         System.arraycopy(this.contactData, index + 1, this.contactData, index, length);
         this.contactData[this.count] = coll;
      }
   }

   @Nullable
   public BlockContactData getContactData(int x, int y, int z) {
      int index = this.getIndex(x, y, z);
      return index == -1 ? null : this.contactData[index];
   }

   @Override
   protected void alloc() {
      super.alloc();
      int newLength = this.blockData.length + 4;
      this.blockData = Arrays.copyOf(this.blockData, newLength);
      this.contactData = Arrays.copyOf(this.contactData, newLength);

      for (int i = this.count; i < newLength; i++) {
         this.blockData[i] = new BlockData();
         this.contactData[i] = new BlockContactData();
      }
   }
}
