package com.hypixel.hytale.server.core.universe.world.chunk.palette;

import javax.annotation.Nonnull;

public class BitFieldArr {
   public static final int BITS_PER_INDEX = 8;
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
         this.array = new byte[(length * bits + 8 - 1) / 8];
         this.length = length;
      }
   }

   public int getLength() {
      return this.length;
   }

   public int get(int index) {
      int bitIndex = index * this.bits;
      int byteIndex = bitIndex / 8;
      int bitOffset = bitIndex % 8;
      if (bitOffset + this.bits <= 8) {
         int mask = (1 << this.bits) - 1;
         return (this.array[byteIndex] & 0xFF) >>> bitOffset & mask;
      } else {
         int value = 0;
         int remainingBits = this.bits;

         for (int shift = 0; remainingBits > 0; bitOffset = 0) {
            int bitsInThisByte = Math.min(8 - bitOffset, remainingBits);
            int mask = (1 << bitsInThisByte) - 1;
            value |= ((this.array[byteIndex] & 255) >>> bitOffset & mask) << shift;
            shift += bitsInThisByte;
            remainingBits -= bitsInThisByte;
            byteIndex++;
         }

         return value;
      }
   }

   public void set(int index, int value) {
      int bitIndex = index * this.bits;
      int byteIndex = bitIndex / 8;
      int bitOffset = bitIndex % 8;
      if (bitOffset + this.bits <= 8) {
         int mask = (1 << this.bits) - 1;
         int clearMask = ~(mask << bitOffset);
         this.array[byteIndex] = (byte)(this.array[byteIndex] & clearMask | (value & mask) << bitOffset);
      } else {
         int remainingBits = this.bits;

         for (int currentValue = value; remainingBits > 0; bitOffset = 0) {
            int bitsInThisByte = Math.min(8 - bitOffset, remainingBits);
            int mask = (1 << bitsInThisByte) - 1;
            int clearMask = ~(mask << bitOffset);
            this.array[byteIndex] = (byte)(this.array[byteIndex] & clearMask | (currentValue & mask) << bitOffset);
            currentValue >>>= bitsInThisByte;
            remainingBits -= bitsInThisByte;
            byteIndex++;
         }
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
      if (this.bits != other.bits) {
         throw new IllegalArgumentException("bits must be the same");
      } else if (this.length != other.length) {
         throw new IllegalArgumentException("length must be the same");
      } else {
         System.arraycopy(other.array, 0, this.array, 0, this.array.length);
      }
   }
}
