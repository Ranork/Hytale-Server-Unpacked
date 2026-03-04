package com.hypixel.hytale.builtin.hytalegenerator.framework.math;

import javax.annotation.Nonnull;

public class BitConverter {
   public static void main(String[] args) {
      System.out.println("LONG TEST:");

      for (int i = -4; i < 10; i++) {
         System.out.println();
         System.out.print("INPUT [" + i + "] -> BINARY -> [");
         boolean[] output = toBitArray((long)i);

         for (boolean bit : output) {
            System.out.print(bit ? "1" : "0");
         }

         System.out.print("] -> DECIMAL -> [" + toLong(output) + "]");
      }

      System.out.println();
      System.out.println("INT TEST:");

      for (int i = -4; i < 10; i++) {
         System.out.println();
         System.out.print("INPUT [" + i + "] -> BINARY -> [");
         boolean[] output = toBitArray(i);

         for (boolean bit : output) {
            System.out.print(bit ? "1" : "0");
         }

         System.out.print("] -> DECIMAL -> [" + toInt(output) + "]");
      }

      System.out.println();
      System.out.println("BYTE TEST:");

      for (int i = -4; i < 10; i++) {
         System.out.println();
         System.out.print("INPUT [" + i + "] -> BINARY -> [");
         boolean[] output = toBitArray((byte)i);

         for (boolean bit : output) {
            System.out.print(bit ? "1" : "0");
         }

         System.out.print("] -> DECIMAL -> [" + toByte(output) + "]");
      }

      System.out.println();
   }

   public static boolean[] toBitArray(long number) {
      byte PRECISION = 64;
      boolean[] bits = new boolean[64];
      long position = 1L;

      for (byte i = 63; i >= 0; i--) {
         bits[i] = (number & position) != 0L;
         position <<= 1;
      }

      return bits;
   }

   public static boolean[] toBitArray(int number) {
      byte PRECISION = 32;
      boolean[] bits = new boolean[32];
      int position = 1;

      for (byte i = 31; i >= 0; i--) {
         bits[i] = (number & position) != 0;
         position <<= 1;
      }

      return bits;
   }

   public static boolean[] toBitArray(byte number) {
      byte PRECISION = 8;
      boolean[] bits = new boolean[8];
      byte position = 1;

      for (byte i = 7; i >= 0; i--) {
         bits[i] = (number & position) != 0;
         position = (byte)(position << 1);
      }

      return bits;
   }

   public static long toLong(@Nonnull boolean[] bits) {
      byte PRECISION = 64;
      if (bits.length != 64) {
         throw new IllegalArgumentException("array must have length 64");
      } else {
         long position = 1L;
         long number = 0L;

         for (byte i = 63; i >= 0; i--) {
            if (bits[i]) {
               number += position;
            }

            position <<= 1;
         }

         return number;
      }
   }

   public static int toInt(@Nonnull boolean[] bits) {
      byte PRECISION = 32;
      if (bits.length != 32) {
         throw new IllegalArgumentException("array must have length 32");
      } else {
         int position = 1;
         int number = 0;

         for (byte i = 31; i >= 0; i--) {
            if (bits[i]) {
               number += position;
            }

            position <<= 1;
         }

         return number;
      }
   }

   public static int toByte(@Nonnull boolean[] bits) {
      byte PRECISION = 8;
      if (bits.length != 8) {
         throw new IllegalArgumentException("array must have length 8");
      } else {
         byte position = 1;
         byte number = 0;

         for (byte i = 7; i >= 0; i--) {
            if (bits[i]) {
               number += position;
            }

            position = (byte)(position << 1);
         }

         return number;
      }
   }
}
