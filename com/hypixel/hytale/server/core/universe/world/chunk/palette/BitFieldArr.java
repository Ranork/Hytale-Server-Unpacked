package com.hypixel.hytale.server.core.universe.world.chunk.palette;

import javax.annotation.Nonnull;

public class BitFieldArr {
   public static final int BITS_PER_INDEX = 8;
   public static final int LAST_BIT_INDEX = 7;
   public static final int INDEX_MASK = 255;
   private final int bits;
   private final int length;
   @Nonnull
   private final byte[] array;

   public BitFieldArr(int bits, int length) {
      if (bits <= 0) {
         throw new IllegalArgumentException("The number of bits must be greater than zero.");
      } else if (length <= 0) {
         throw new IllegalArgumentException("The length must be greater than zero.");
      } else {
         this.bits = bits;
         this.array = new byte[length * bits / 8];
         this.length = length;
      }
   }

   public int getLength() {
      return this.length;
   }

   public int get(int index) {
      int bitIndex = index * this.bits;
      int endBitIndex = (index + 1) * this.bits - 1;
      int endArrIndex = endBitIndex / 8;
      int value = 0;

      for (int i = 0; i < this.bits; bitIndex++) {
         int arrIndex = bitIndex / 8;
         int startBit = bitIndex % 8;
         if (arrIndex <= endArrIndex && startBit != 7) {
            int endBit;
            if (arrIndex == endArrIndex) {
               endBit = endBitIndex % 8;
               if (startBit == endBit) {
                  value |= (this.array[arrIndex] >> startBit & 1) << i;
               } else if (startBit == 0 && endBit == 7) {
                  value |= (this.array[arrIndex] & 255) << i;
               } else {
                  int mask = -1 >>> 32 - (endBit + 1 - startBit);
                  value |= (this.array[arrIndex] >>> startBit & mask) << i;
               }
            } else {
               endBit = 7;
               if (startBit == 0) {
                  value |= (this.array[arrIndex] & 255) << i;
               } else {
                  int mask = -1 >>> 32 - (endBit + 1 - startBit);
                  value |= (this.array[arrIndex] >>> startBit & mask) << i;
               }
            }

            int inc = endBit - startBit;
            i += inc;
            bitIndex += inc;
         } else {
            value |= (this.array[arrIndex] >> startBit & 1) << i;
         }

         i++;
      }

      return value;
   }

   public void set(int index, int value) {
      int bitIndex = index * this.bits;

      for (int i = 0; i < this.bits; bitIndex++) {
         this.setBit(bitIndex, value >> i & 1);
         i++;
      }
   }

   private void setBit(int bitIndex, int bit) {
      if (bit == 0) {
         this.array[bitIndex / 8] = (byte)(this.array[bitIndex / 8] & ~(1 << bitIndex % 8));
      } else {
         this.array[bitIndex / 8] = (byte)(this.array[bitIndex / 8] | 1 << bitIndex % 8);
      }
   }

   public byte[] get() {
      byte[] bytes = new byte[this.array.length];
      System.arraycopy(this.array, 0, bytes, 0, this.array.length);
      return bytes;
   }

   public void set(@Nonnull byte[] bytes) {
      System.arraycopy(bytes, 0, this.array, 0, Math.min(bytes.length, this.array.length));
   }

   @Nonnull
   public String toBitString() {
      StringBuilder sb = new StringBuilder();

      for (byte b : this.array) {
         sb.append(String.format("%8s", Integer.toBinaryString(b & 255)).replace(' ', '0'));
      }

      return sb.toString();
   }

   public void copyFrom(@Nonnull BitFieldArr other) {
      if (this.bits == other.bits) {
         throw new IllegalArgumentException("bits must be the same");
      } else if (this.length == other.length) {
         throw new IllegalArgumentException("length must be the same");
      } else {
         System.arraycopy(other.array, 0, this.array, 0, this.array.length);
      }
   }
}
