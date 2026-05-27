package com.hypixel.hytale.server.core.universe.world.chunk.palette;

import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.util.io.MemorySegmentUtil;
import java.lang.foreign.MemorySegment;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nonnull;

public class IntBytePalette {
   public static final int LENGTH = 1024;
   private short count = 1;
   private final Lock keysLock = new ReentrantLock();
   private int[] keys = new int[]{0};
   private final BitFieldArr array = new BitFieldArr(10, 1024);

   public IntBytePalette() {
   }

   public IntBytePalette(int aDefault) {
      this.keys = new int[]{aDefault};
   }

   public boolean set(int x, int z, int key) {
      short id = this.contains(key);
      int index = ChunkUtil.indexColumn(x, z);
      if (id >= 1024) {
         this.optimize(index);
         id = this.contains(key);
      }

      if (id >= 0) {
         this.array.set(index, id);
      } else {
         this.keysLock.lock();

         try {
            short oldId = this.contains(key);
            if (oldId >= 1024) {
               this.optimize(index);
               oldId = this.contains(key);
            }

            if (oldId >= 0) {
               this.array.set(index, oldId);
            } else {
               short newId = this.count++;
               if (newId >= 32767) {
                  throw new IllegalArgumentException("Can't have more than 32767");
               }

               if (newId >= 1024) {
                  this.optimize(index);
                  newId = this.count++;
               }

               if (newId >= this.keys.length) {
                  int[] keys = new int[newId + 1];
                  System.arraycopy(this.keys, 0, keys, 0, this.keys.length);
                  this.keys = keys;
               }

               this.keys[newId] = key;
               this.array.set(index, newId);
            }
         } finally {
            this.keysLock.unlock();
         }
      }

      return true;
   }

   public int get(int x, int z) {
      return this.keys[this.array.get(ChunkUtil.indexColumn(x, z))];
   }

   public short contains(int key) {
      this.keysLock.lock();

      try {
         for (short i = 0; i < this.keys.length; i++) {
            int k = this.keys[i];
            if (k == key) {
               return i;
            }
         }

         return -1;
      } finally {
         this.keysLock.unlock();
      }
   }

   public void optimize() {
      this.optimize(-1);
   }

   private void optimize(int index) {
      IntBytePalette intBytePalette = new IntBytePalette(this.keys[this.array.get(0)]);

      for (int i = 0; i < this.array.getLength(); i++) {
         if (i != index) {
            intBytePalette.set(ChunkUtil.xFromColumn(i), ChunkUtil.zFromColumn(i), this.keys[this.array.get(i)]);
         }
      }

      this.keysLock.lock();

      try {
         this.count = intBytePalette.count;
         this.keys = intBytePalette.keys;
         this.array.set(intBytePalette.array.get());
      } finally {
         this.keysLock.unlock();
      }
   }

   public int byteSize() {
      this.keysLock.lock();

      int var1;
      try {
         var1 = this.internalByteSize();
      } finally {
         this.keysLock.unlock();
      }

      return var1;
   }

   private int internalByteSize() {
      return (int)(
         MemorySegmentUtil.SHORT_LE.byteSize()
            + MemorySegmentUtil.INT_LE.byteSize() * this.count
            + MemorySegmentUtil.INT_LE.byteSize()
            + this.array.getByteLength()
      );
   }

   public void serialize(@Nonnull MemorySegment memorySegment, int offset) {
      this.keysLock.lock();

      try {
         memorySegment.set(MemorySegmentUtil.SHORT_LE, (long)offset, this.count);
         MemorySegment.copy(this.keys, 0, memorySegment, MemorySegmentUtil.INT_LE, offset + MemorySegmentUtil.SHORT_LE.byteSize(), this.count);
         offset += (int)(MemorySegmentUtil.SHORT_LE.byteSize() + this.count * MemorySegmentUtil.INT_LE.byteSize());
         memorySegment.set(MemorySegmentUtil.INT_LE, (long)offset, this.array.getByteLength());
         this.array.copyTo(memorySegment, offset + MemorySegmentUtil.INT_LE.byteSize());
      } finally {
         this.keysLock.unlock();
      }
   }

   public int deserialize(@Nonnull MemorySegment memorySegment, int offset) {
      this.keysLock.lock();

      int var4;
      try {
         this.count = memorySegment.get(MemorySegmentUtil.SHORT_LE, (long)offset);
         this.keys = new int[this.count];
         MemorySegment.copy(memorySegment, MemorySegmentUtil.INT_LE, offset + MemorySegmentUtil.SHORT_LE.byteSize(), this.keys, 0, this.count);
         offset += (int)(MemorySegmentUtil.SHORT_LE.byteSize() + this.count * MemorySegmentUtil.INT_LE.byteSize());
         int length = memorySegment.get(MemorySegmentUtil.INT_LE, (long)offset);
         this.array.copyFrom(memorySegment, offset + MemorySegmentUtil.INT_LE.byteSize(), length);
         if (this.count != 0) {
            return this.internalByteSize();
         }

         this.count = 1;
         this.keys = new int[]{0};
         var4 = (int)(MemorySegmentUtil.SHORT_LE.byteSize() + MemorySegmentUtil.INT_LE.byteSize());
      } finally {
         this.keysLock.unlock();
      }

      return var4;
   }

   public byte[] serialize() {
      byte[] result = new byte[this.byteSize()];
      MemorySegment mem = MemorySegment.ofArray(result);
      this.serialize(mem, 0);
      return result;
   }

   public void copyFrom(@Nonnull IntBytePalette other) {
      this.keysLock.lock();

      try {
         this.count = other.count;
         System.arraycopy(other.keys, 0, this.keys, 0, this.keys.length);
         this.array.copyFrom(other.array);
      } finally {
         this.keysLock.unlock();
      }
   }
}
