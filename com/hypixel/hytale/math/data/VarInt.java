package com.hypixel.hytale.math.data;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import javax.annotation.Nonnull;

public final class VarInt {
   private VarInt() {
      throw new UnsupportedOperationException("Do not instantiate.");
   }

   public static void writeSignedVarLong(long value, @Nonnull DataOutput out) throws IOException {
      writeUnsignedVarLong(value << 1 ^ value >> 63, out);
   }

   public static void writeUnsignedVarLong(long value, @Nonnull DataOutput out) throws IOException {
      while ((value & -128L) != 0L) {
         out.writeByte((int)value & 127 | 128);
         value >>>= 7;
      }

      out.writeByte((int)value & 127);
   }

   public static void writeSignedVarInt(int value, @Nonnull DataOutput out) throws IOException {
      writeUnsignedVarInt(value << 1 ^ value >> 31, out);
   }

   public static void writeUnsignedVarInt(int value, @Nonnull DataOutput out) throws IOException {
      while ((value & -128) != 0L) {
         out.writeByte(value & 127 | 128);
         value >>>= 7;
      }

      out.writeByte(value & 127);
   }

   public static byte[] writeSignedVarInt(int value) {
      return writeUnsignedVarInt(value << 1 ^ value >> 31);
   }

   public static byte[] writeUnsignedVarInt(int value) {
      byte[] byteArrayList = new byte[10];
      int i = 0;

      while ((value & -128) != 0L) {
         byteArrayList[i++] = (byte)(value & 127 | 128);
         value >>>= 7;
      }

      byteArrayList[i] = (byte)(value & 127);

      byte[] out;
      for (out = new byte[i + 1]; i >= 0; i--) {
         out[i] = byteArrayList[i];
      }

      return out;
   }

   public static long readSignedVarLong(@Nonnull DataInput in) throws IOException {
      long raw = readUnsignedVarLong(in);
      long temp = (raw << 63 >> 63 ^ raw) >> 1;
      return temp ^ raw & Long.MIN_VALUE;
   }

   public static long readUnsignedVarLong(@Nonnull DataInput in) throws IOException {
      long value = 0L;
      int i = 0;

      long b;
      while (((b = in.readByte()) & 128L) != 0L) {
         value |= (b & 127L) << i;
         i += 7;
         if (i > 63) {
            throw new IllegalArgumentException("Variable length quantity is too long");
         }
      }

      return value | b << i;
   }

   public static int readSignedVarInt(@Nonnull DataInput in) throws IOException {
      int raw = readUnsignedVarInt(in);
      int temp = (raw << 31 >> 31 ^ raw) >> 1;
      return temp ^ raw & -2147483648;
   }

   public static int readUnsignedVarInt(@Nonnull DataInput in) throws IOException {
      int value = 0;
      int i = 0;

      int b;
      while (((b = in.readByte()) & 128) != 0) {
         value |= (b & 127) << i;
         i += 7;
         if (i > 35) {
            throw new IllegalArgumentException("Variable length quantity is too long");
         }
      }

      return value | b << i;
   }

   public static int readSignedVarInt(@Nonnull byte[] bytes) {
      int raw = readUnsignedVarInt(bytes);
      int temp = (raw << 31 >> 31 ^ raw) >> 1;
      return temp ^ raw & -2147483648;
   }

   public static int readUnsignedVarInt(@Nonnull byte[] bytes) {
      int value = 0;
      int i = 0;
      byte rb = -128;

      for (byte b : bytes) {
         rb = b;
         if ((b & 128) == 0) {
            break;
         }

         value |= (b & 127) << i;
         i += 7;
         if (i > 35) {
            throw new IllegalArgumentException("Variable length quantity is too long");
         }
      }

      return value | rb << i;
   }
}
