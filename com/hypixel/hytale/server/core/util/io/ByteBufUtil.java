package com.hypixel.hytale.server.core.util.io;

import com.hypixel.hytale.common.util.BitSetUtil;
import com.hypixel.hytale.unsafe.UnsafeUtil;
import io.netty.buffer.ByteBuf;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;
import javax.annotation.Nonnull;

public class ByteBufUtil {
   private static int MAX_UNSIGNED_SHORT_VALUE = 65535;

   public static void writeUTF(@Nonnull ByteBuf buf, @Nonnull String string) {
      if (io.netty.buffer.ByteBufUtil.utf8MaxBytes(string) >= MAX_UNSIGNED_SHORT_VALUE) {
         throw new IllegalArgumentException("String is too large");
      } else {
         int before = buf.writerIndex();
         buf.writeShort(-1);
         int length = buf.writeCharSequence(string, StandardCharsets.UTF_8);
         if (length >= 0 && length < MAX_UNSIGNED_SHORT_VALUE) {
            int after = buf.writerIndex();
            buf.writerIndex(before);
            buf.writeShort(length);
            buf.writerIndex(after);
         } else {
            throw new IllegalArgumentException("Serialized string is too large");
         }
      }
   }

   @Nonnull
   public static String readUTF(@Nonnull ByteBuf buf) {
      int length = buf.readUnsignedShort();
      return buf.readCharSequence(length, StandardCharsets.UTF_8).toString();
   }

   public static void writeByteArray(@Nonnull ByteBuf buf, @Nonnull byte[] arr) {
      writeByteArray(buf, arr, 0, arr.length);
   }

   public static void writeByteArray(@Nonnull ByteBuf buf, byte[] arr, int src, int length) {
      if (length > 0 && length < MAX_UNSIGNED_SHORT_VALUE) {
         buf.writeShort(length);
         buf.writeBytes(arr, src, length);
      } else {
         throw new IllegalArgumentException("length is too large");
      }
   }

   public static byte[] readByteArray(@Nonnull ByteBuf buf) {
      int length = buf.readUnsignedShort();
      byte[] bytes = new byte[length];
      buf.readBytes(bytes);
      return bytes;
   }

   public static byte[] getBytesRelease(@Nonnull ByteBuf buf) {
      byte[] var1;
      try {
         var1 = io.netty.buffer.ByteBufUtil.getBytes(buf, 0, buf.writerIndex(), false);
      } finally {
         buf.release();
      }

      return var1;
   }

   public static void writeNumber(@Nonnull ByteBuf buf, int bytes, int value) {
      switch (bytes) {
         case 1:
            buf.writeByte(value);
            break;
         case 2:
            buf.writeShort(value);
         case 3:
         default:
            break;
         case 4:
            buf.writeInt(value);
      }
   }

   public static int readNumber(@Nonnull ByteBuf buf, int bytes) {
      return switch (bytes) {
         case 1 -> buf.readByte() & 255;
         case 2 -> buf.readShort() & '\uffff';
         default -> 0;
         case 4 -> buf.readInt();
      };
   }

   public static void writeBitSet(@Nonnull ByteBuf buf, @Nonnull BitSet bitset) {
      int wordsInUse;
      long[] words;
      if (UnsafeUtil.UNSAFE == null) {
         words = bitset.toLongArray();
         wordsInUse = words.length;
      } else {
         wordsInUse = UnsafeUtil.UNSAFE.getInt(bitset, BitSetUtil.WORDS_IN_USE_OFFSET);
         words = (long[])UnsafeUtil.UNSAFE.getObject(bitset, BitSetUtil.WORDS_OFFSET);
      }

      buf.writeInt(wordsInUse);

      for (int i = 0; i < wordsInUse; i++) {
         buf.writeLong(words[i]);
      }
   }

   public static void readBitSet(@Nonnull ByteBuf buf, @Nonnull BitSet bitset) {
      int wordsInUse = buf.readInt();
      if (UnsafeUtil.UNSAFE == null) {
         long[] words = new long[wordsInUse];

         for (int i = 0; i < wordsInUse; i++) {
            words[i] = buf.readLong();
         }

         bitset.clear();
         bitset.or(BitSet.valueOf(words));
      } else {
         UnsafeUtil.UNSAFE.putInt(bitset, BitSetUtil.WORDS_IN_USE_OFFSET, wordsInUse);
         long[] words = (long[])UnsafeUtil.UNSAFE.getObject(bitset, BitSetUtil.WORDS_OFFSET);
         if (wordsInUse > words.length) {
            words = new long[wordsInUse];
            UnsafeUtil.UNSAFE.putObject(bitset, BitSetUtil.WORDS_OFFSET, words);
         }

         for (int i = 0; i < wordsInUse; i++) {
            words[i] = buf.readLong();
         }
      }
   }
}
