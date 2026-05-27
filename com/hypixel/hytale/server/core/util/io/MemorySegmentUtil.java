package com.hypixel.hytale.server.core.util.io;

import com.hypixel.hytale.protocol.io.PacketIO;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.foreign.ValueLayout.OfFloat;
import java.lang.foreign.ValueLayout.OfInt;
import java.lang.foreign.ValueLayout.OfLong;
import java.lang.foreign.ValueLayout.OfShort;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nonnull;

public class MemorySegmentUtil {
   public static final OfShort SHORT_BE = ValueLayout.JAVA_SHORT_UNALIGNED.withOrder(ByteOrder.BIG_ENDIAN);
   public static final OfShort SHORT_LE = ValueLayout.JAVA_SHORT_UNALIGNED.withOrder(ByteOrder.LITTLE_ENDIAN);
   public static final OfInt INT_BE = ValueLayout.JAVA_INT_UNALIGNED.withOrder(ByteOrder.BIG_ENDIAN);
   public static final OfInt INT_LE = ValueLayout.JAVA_INT_UNALIGNED.withOrder(ByteOrder.LITTLE_ENDIAN);
   public static final OfLong LONG_BE = ValueLayout.JAVA_LONG_UNALIGNED.withOrder(ByteOrder.BIG_ENDIAN);
   public static final OfLong LONG_LE = ValueLayout.JAVA_LONG_UNALIGNED.withOrder(ByteOrder.LITTLE_ENDIAN);
   public static final OfFloat FLOAT_BE = ValueLayout.JAVA_FLOAT_UNALIGNED.withOrder(ByteOrder.BIG_ENDIAN);
   public static final OfFloat FLOAT_LE = ValueLayout.JAVA_FLOAT_UNALIGNED.withOrder(ByteOrder.LITTLE_ENDIAN);
   public static final int MAX_UNSIGNED_SHORT_VALUE = 65535;

   public static int utf8Size(@Nonnull String string) {
      return 2 + PacketIO.utf8ByteLength(string);
   }

   public static int utf8Size(@Nonnull MemorySegment segment, long offset) {
      return 2 + Short.toUnsignedInt(segment.get(SHORT_BE, offset));
   }

   public static int writeUTF(@Nonnull MemorySegment segment, long offset, @Nonnull String string) {
      byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
      if (bytes.length >= 65535) {
         throw new IllegalArgumentException("String is too large");
      } else {
         segment.set(SHORT_BE, offset, (short)bytes.length);
         MemorySegment.copy(bytes, 0, segment, ValueLayout.JAVA_BYTE, offset + 2L, bytes.length);
         return 2 + bytes.length;
      }
   }

   @Nonnull
   public static String readUTF(@Nonnull MemorySegment segment, long offset) {
      int length = Short.toUnsignedInt(segment.get(SHORT_BE, offset));
      byte[] bytes = segment.asSlice(offset + 2L, length).toArray(ValueLayout.JAVA_BYTE);
      return new String(bytes, StandardCharsets.UTF_8);
   }

   public static void writeNumber(@Nonnull MemorySegment data, int offset, int bytes, int value) {
      switch (bytes) {
         case 1:
            data.set(ValueLayout.JAVA_BYTE, (long)offset, (byte)value);
            break;
         case 2:
            data.set(ValueLayout.JAVA_SHORT_UNALIGNED, (long)offset, (short)value);
         case 3:
         default:
            break;
         case 4:
            data.set(ValueLayout.JAVA_INT_UNALIGNED, (long)offset, value);
      }
   }

   public static int readNumber(@Nonnull MemorySegment data, int offset, int bytes) {
      return switch (bytes) {
         case 1 -> data.get(ValueLayout.JAVA_BYTE, (long)offset) & 255;
         case 2 -> data.get(ValueLayout.JAVA_SHORT_UNALIGNED, (long)offset) & '\uffff';
         default -> 0;
         case 4 -> data.get(ValueLayout.JAVA_INT_UNALIGNED, (long)offset);
      };
   }
}
