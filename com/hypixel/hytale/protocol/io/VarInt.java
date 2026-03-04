package com.hypixel.hytale.protocol.io;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;

public final class VarInt {
   private VarInt() {
   }

   public static void write(@Nonnull ByteBuf buf, int value) {
      if (value < 0) {
         throw new IllegalArgumentException("VarInt cannot encode negative values: " + value);
      } else {
         while ((value & -128) != 0) {
            buf.writeByte(value & 127 | 128);
            value >>>= 7;
         }

         buf.writeByte(value);
      }
   }

   public static int read(@Nonnull ByteBuf buf) {
      int value = 0;
      int shift = 0;

      do {
         byte b = buf.readByte();
         value |= (b & 127) << shift;
         if ((b & 128) == 0) {
            return value;
         }

         shift += 7;
      } while (shift <= 28);

      throw new ProtocolException("VarInt exceeds maximum length (5 bytes)");
   }

   public static int peek(@Nonnull ByteBuf buf, int index) {
      int value = 0;
      int shift = 0;
      int pos = index;

      while (pos < buf.writerIndex()) {
         byte b = buf.getByte(pos++);
         value |= (b & 127) << shift;
         if ((b & 128) == 0) {
            return value;
         }

         shift += 7;
         if (shift > 28) {
            return -1;
         }
      }

      return -1;
   }

   public static int length(@Nonnull ByteBuf buf, int index) {
      int pos = index;

      while (pos < buf.writerIndex()) {
         if ((buf.getByte(pos++) & 128) == 0) {
            return pos - index;
         }

         if (pos - index > 5) {
            return -1;
         }
      }

      return -1;
   }

   public static int size(int value) {
      if ((value & -128) == 0) {
         return 1;
      } else if ((value & -16384) == 0) {
         return 2;
      } else if ((value & -2097152) == 0) {
         return 3;
      } else {
         return (value & -268435456) == 0 ? 4 : 5;
      }
   }
}
