package com.hypixel.hytale.server.core.universe.world.chunk.palette;

import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.util.io.ByteBufUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
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

   public void serialize(@Nonnull ByteBuf dos) {
      this.keysLock.lock();

      try {
         dos.writeShortLE(this.count);

         for (int i = 0; i < this.count; i++) {
            dos.writeIntLE(this.keys[i]);
         }

         byte[] bytes = this.array.get();
         dos.writeIntLE(bytes.length);
         dos.writeBytes(bytes);
      } finally {
         this.keysLock.unlock();
      }
   }

   public void deserialize(@Nonnull ByteBuf dis) {
      this.keysLock.lock();

      try {
         this.count = dis.readShortLE();
         this.keys = new int[this.count];

         for (int i = 0; i < this.count; i++) {
            this.keys[i] = dis.readIntLE();
         }

         int length = dis.readIntLE();
         byte[] bytes = new byte[length];
         dis.readBytes(bytes);
         this.array.set(bytes);
         if (this.count == 0) {
            this.count = 1;
            this.keys = new int[]{0};
         }
      } finally {
         this.keysLock.unlock();
      }
   }

   public byte[] serialize() {
      ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
      this.serialize(buf);
      return ByteBufUtil.getBytesRelease(buf);
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
