package com.hypixel.hytale.protocol.io;

import com.github.luben.zstd.Zstd;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.PacketRegistry;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.foreign.ValueLayout.OfBoolean;
import java.lang.foreign.ValueLayout.OfByte;
import java.lang.foreign.ValueLayout.OfDouble;
import java.lang.foreign.ValueLayout.OfFloat;
import java.lang.foreign.ValueLayout.OfInt;
import java.lang.foreign.ValueLayout.OfLong;
import java.lang.foreign.ValueLayout.OfShort;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;

public final class PacketIO {
   public static final int FRAME_HEADER_SIZE = 4;
   public static final int PACKET_HEADER_SIZE = 8;
   public static final Charset UTF8 = StandardCharsets.UTF_8;
   public static final Charset ASCII = StandardCharsets.US_ASCII;
   public static final OfBoolean PROTO_BOOL = ValueLayout.JAVA_BOOLEAN.withOrder(ByteOrder.LITTLE_ENDIAN);
   public static final OfByte PROTO_BYTE = ValueLayout.JAVA_BYTE.withOrder(ByteOrder.LITTLE_ENDIAN);
   public static final OfShort PROTO_SHORT = ValueLayout.JAVA_SHORT_UNALIGNED.withOrder(ByteOrder.LITTLE_ENDIAN);
   public static final OfInt PROTO_INT = ValueLayout.JAVA_INT_UNALIGNED.withOrder(ByteOrder.LITTLE_ENDIAN);
   public static final OfLong PROTO_LONG = ValueLayout.JAVA_LONG_UNALIGNED.withOrder(ByteOrder.LITTLE_ENDIAN);
   public static final OfFloat PROTO_FLOAT = ValueLayout.JAVA_FLOAT_UNALIGNED.withOrder(ByteOrder.LITTLE_ENDIAN);
   public static final OfDouble PROTO_DOUBLE = ValueLayout.JAVA_DOUBLE_UNALIGNED.withOrder(ByteOrder.LITTLE_ENDIAN);
   public static final OfLong UUID_LONG = ValueLayout.JAVA_LONG_UNALIGNED.withOrder(ByteOrder.BIG_ENDIAN);
   public static final Vector2fc ZERO_VECTOR2 = new Vector2f();
   public static final Vector3fc ZERO_VECTOR3 = new Vector3f();
   public static final Vector4fc ZERO_VECTOR4 = new Vector4f();
   public static final Quaternionfc ZERO_QUATERNION = new Quaternionf(0.0F, 0.0F, 0.0F, 0.0F);
   public static final Matrix4fc ZERO_MATRIX = new Matrix4f().zero();
   private static final int COMPRESSION_LEVEL = Integer.getInteger("hytale.protocol.compressionLevel", Zstd.defaultCompressionLevel());

   private PacketIO() {
   }

   public static float readHalfLE(@Nonnull ByteBuf buf, int index) {
      short bits = buf.getShortLE(index);
      return halfToFloat(bits);
   }

   public static void writeHalfLE(@Nonnull ByteBuf buf, float value) {
      buf.writeShortLE(floatToHalf(value));
   }

   @Nonnull
   public static byte[] readBytes(@Nonnull ByteBuf buf, int offset, int length) {
      byte[] bytes = new byte[length];
      buf.getBytes(offset, bytes);
      return bytes;
   }

   @Nonnull
   public static byte[] readByteArray(@Nonnull ByteBuf buf, int offset, int length) {
      byte[] result = new byte[length];
      buf.getBytes(offset, result);
      return result;
   }

   @Nonnull
   public static short[] readShortArrayLE(@Nonnull ByteBuf buf, int offset, int length) {
      short[] result = new short[length];

      for (int i = 0; i < length; i++) {
         result[i] = buf.getShortLE(offset + i * 2);
      }

      return result;
   }

   @Nonnull
   public static float[] readFloatArrayLE(@Nonnull ByteBuf buf, int offset, int length) {
      float[] result = new float[length];

      for (int i = 0; i < length; i++) {
         result[i] = buf.getFloatLE(offset + i * 4);
      }

      return result;
   }

   @Nonnull
   public static String readFixedAsciiString(@Nonnull ByteBuf buf, int offset, int length) {
      byte[] bytes = new byte[length];
      buf.getBytes(offset, bytes);
      int end = 0;

      while (end < length && bytes[end] != 0) {
         end++;
      }

      return new String(bytes, 0, end, StandardCharsets.US_ASCII);
   }

   @Nonnull
   public static String readFixedString(@Nonnull ByteBuf buf, int offset, int length) {
      byte[] bytes = new byte[length];
      buf.getBytes(offset, bytes);
      int end = 0;

      while (end < length && bytes[end] != 0) {
         end++;
      }

      return new String(bytes, 0, end, StandardCharsets.UTF_8);
   }

   @Nonnull
   public static String readFixedAsciiString(@Nonnull MemorySegment mem, int offset, int length) {
      byte[] bytes = new byte[length];
      MemorySegment.copy(mem, PROTO_BYTE, offset, bytes, 0, length);
      int end = 0;

      while (end < length && bytes[end] != 0) {
         end++;
      }

      return new String(bytes, 0, end, StandardCharsets.US_ASCII);
   }

   @Nonnull
   public static String readFixedString(@Nonnull MemorySegment mem, int offset, int length) {
      byte[] bytes = new byte[length];
      MemorySegment.copy(mem, PROTO_BYTE, offset, bytes, 0, length);
      int end = 0;

      while (end < length && bytes[end] != 0) {
         end++;
      }

      return new String(bytes, 0, end, StandardCharsets.UTF_8);
   }

   @Nonnull
   public static String readVarString(@Nonnull ByteBuf buf, int offset) {
      return readVarString(buf, offset, StandardCharsets.UTF_8);
   }

   @Nonnull
   public static String readVarString(@Nonnull ByteBuf buf, int offset, Charset charset) {
      int len = VarInt.peek(buf, offset);
      int varIntLen = VarInt.length(buf, offset);
      byte[] bytes = new byte[len];
      buf.getBytes(offset + varIntLen, bytes);
      return new String(bytes, charset);
   }

   @Nonnull
   public static String readValidatedAsciiString(@Nonnull ByteBuf buf, int offset, int length, @Nonnull String fieldName) {
      byte[] bytes = new byte[length];
      buf.getBytes(offset, bytes);

      for (int i = 0; i < length; i++) {
         if ((bytes[i] & 255) > 127) {
            throw ProtocolException.invalidAsciiString(fieldName);
         }
      }

      return new String(bytes, StandardCharsets.US_ASCII);
   }

   public static boolean isValidAscii(@Nonnull ByteBuf buf, int offset, int length) {
      for (int i = 0; i < length; i++) {
         if ((buf.getByte(offset + i) & 255) > 127) {
            return false;
         }
      }

      return true;
   }

   @Nonnull
   public static String readVarString(String fieldName, @Nonnull MemorySegment mem, int offset, int minLength, int maxLength, Charset charset) {
      long packed = VarInt.getWithLength(mem, offset);
      int len = (int)packed;
      if (len == -1) {
         throw ProtocolException.invalidVarInt(fieldName);
      } else if (len < 0) {
         throw ProtocolException.negativeLength(fieldName, len);
      } else if (len > maxLength) {
         throw ProtocolException.stringTooLong(fieldName, len, maxLength);
      } else if (len < minLength) {
         throw ProtocolException.stringTooShort(fieldName, len, minLength);
      } else {
         int varIntLen = (int)(packed >>> 32);
         if (offset + len + varIntLen > mem.byteSize()) {
            throw ProtocolException.bufferTooSmall(fieldName, len + varIntLen, (int)(mem.byteSize() - offset));
         } else {
            byte[] bytes = new byte[len];
            MemorySegment.copy(mem, PROTO_BYTE, offset + varIntLen, bytes, 0, len);
            return new String(bytes, charset);
         }
      }
   }

   @Nonnull
   public static String readVarString(String fieldName, @Nonnull MemorySegment mem, int offset, int maxLength, Charset charset) {
      return readVarString(fieldName, mem, offset, 0, maxLength, charset);
   }

   @Nonnull
   public static String readValidatedAsciiString(String fieldName, @Nonnull MemorySegment mem, int offset, int minLength, int maxLength) {
      long packed = VarInt.getWithLength(mem, offset);
      int len = (int)packed;
      if (len == -1) {
         throw ProtocolException.invalidVarInt(fieldName);
      } else if (len < 0) {
         throw ProtocolException.negativeLength(fieldName, len);
      } else if (len > maxLength) {
         throw ProtocolException.stringTooLong(fieldName, len, maxLength);
      } else if (len < minLength) {
         throw ProtocolException.stringTooShort(fieldName, len, minLength);
      } else {
         int varIntLen = (int)(packed >>> 32);
         if (offset + len + varIntLen > mem.byteSize()) {
            throw ProtocolException.bufferTooSmall(fieldName, len + varIntLen, (int)(mem.byteSize() - offset));
         } else {
            byte[] bytes = new byte[len];
            MemorySegment.copy(mem, PROTO_BYTE, offset + varIntLen, bytes, 0, len);

            for (int i = 0; i < len; i++) {
               if ((bytes[i] & 255) > 127) {
                  throw ProtocolException.invalidAsciiString(fieldName);
               }
            }

            return new String(bytes, StandardCharsets.US_ASCII);
         }
      }
   }

   @Nonnull
   public static String readValidatedAsciiString(String fieldName, @Nonnull MemorySegment mem, int offset, int maxLength) {
      return readValidatedAsciiString(fieldName, mem, offset, 0, maxLength);
   }

   public static int utf8ByteLength(@Nonnull String s) {
      int len = 0;

      for (int i = 0; i < s.length(); i++) {
         char c = s.charAt(i);
         if (c < 128) {
            len++;
         } else if (c < 2048) {
            len += 2;
         } else if (Character.isHighSurrogate(c)) {
            len += 4;
            i++;
         } else {
            len += 3;
         }
      }

      return len;
   }

   public static int stringSize(@Nonnull String s) {
      int len = utf8ByteLength(s);
      return VarInt.size(len) + len;
   }

   public static void writeFixedBytes(@Nonnull ByteBuf buf, @Nonnull byte[] data, int length) {
      buf.writeBytes(data, 0, Math.min(data.length, length));

      for (int i = data.length; i < length; i++) {
         buf.writeByte(0);
      }
   }

   public static void writeFixedAsciiString(@Nonnull ByteBuf buf, @Nullable String value, int length) {
      if (value != null) {
         byte[] bytes = value.getBytes(StandardCharsets.US_ASCII);
         if (bytes.length > length) {
            throw new ProtocolException("Fixed ASCII string exceeds length: " + bytes.length + " > " + length);
         }

         buf.writeBytes(bytes);
         buf.writeZero(length - bytes.length);
      } else {
         buf.writeZero(length);
      }
   }

   public static void writeFixedString(@Nonnull ByteBuf buf, @Nullable String value, int length) {
      if (value != null) {
         byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
         if (bytes.length > length) {
            throw new ProtocolException("Fixed UTF-8 string exceeds length: " + bytes.length + " > " + length);
         }

         buf.writeBytes(bytes);
         buf.writeZero(length - bytes.length);
      } else {
         buf.writeZero(length);
      }
   }

   public static void writeVarString(@Nonnull ByteBuf buf, @Nonnull String value, int maxLength) {
      byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
      if (bytes.length > maxLength) {
         throw new ProtocolException("String exceeds max bytes: " + bytes.length + " > " + maxLength);
      } else {
         VarInt.write(buf, bytes.length);
         buf.writeBytes(bytes);
      }
   }

   public static void writeVarAsciiString(@Nonnull ByteBuf buf, @Nonnull String value, int maxLength) {
      byte[] bytes = value.getBytes(StandardCharsets.US_ASCII);
      if (bytes.length > maxLength) {
         throw new ProtocolException("String exceeds max bytes: " + bytes.length + " > " + maxLength);
      } else {
         VarInt.write(buf, bytes.length);
         buf.writeBytes(bytes);
      }
   }

   public static int writeVarString(@Nonnull MemorySegment mem, int offset, @Nonnull String value, int maxLength) {
      byte[] bytes = value.getBytes(UTF8);
      if (bytes.length > maxLength) {
         throw new ProtocolException("String exceeds max bytes: " + bytes.length + " > " + maxLength);
      } else {
         int len = VarInt.set(mem, offset, bytes.length);
         MemorySegment.copy(bytes, 0, mem, ValueLayout.JAVA_BYTE, offset + len, bytes.length);
         return len + bytes.length;
      }
   }

   public static int writeVarAsciiString(@Nonnull MemorySegment mem, int offset, @Nonnull String value, int maxLength) {
      byte[] bytes = value.getBytes(ASCII);
      if (bytes.length > maxLength) {
         throw new ProtocolException("String exceeds max bytes: " + bytes.length + " > " + maxLength);
      } else {
         int len = VarInt.set(mem, offset, bytes.length);
         MemorySegment.copy(bytes, 0, mem, ValueLayout.JAVA_BYTE, offset + len, bytes.length);
         return len + bytes.length;
      }
   }

   @Nonnull
   public static Vector2f readVector2f(@Nonnull ByteBuf buf, int offset) {
      return new Vector2f(buf.getFloatLE(offset), buf.getFloatLE(offset + 4));
   }

   @Nonnull
   public static Vector3f readVector3f(@Nonnull ByteBuf buf, int offset) {
      return new Vector3f(buf.getFloatLE(offset), buf.getFloatLE(offset + 4), buf.getFloatLE(offset + 8));
   }

   @Nonnull
   public static Vector4f readVector4f(@Nonnull ByteBuf buf, int offset) {
      return new Vector4f(buf.getFloatLE(offset), buf.getFloatLE(offset + 4), buf.getFloatLE(offset + 8), buf.getFloatLE(offset + 12));
   }

   @Nonnull
   public static Quaternionf readQuaternionf(@Nonnull ByteBuf buf, int offset) {
      return new Quaternionf(buf.getFloatLE(offset), buf.getFloatLE(offset + 4), buf.getFloatLE(offset + 8), buf.getFloatLE(offset + 12));
   }

   @Nonnull
   public static Vector2f readVector2f(@Nonnull MemorySegment mem, int offset) {
      return new Vector2f(mem.get(PROTO_FLOAT, (long)offset), mem.get(PROTO_FLOAT, (long)(offset + 4)));
   }

   @Nonnull
   public static Vector3f readVector3f(@Nonnull MemorySegment mem, int offset) {
      return new Vector3f(mem.get(PROTO_FLOAT, (long)offset), mem.get(PROTO_FLOAT, (long)(offset + 4)), mem.get(PROTO_FLOAT, (long)(offset + 8)));
   }

   @Nonnull
   public static Vector4f readVector4f(@Nonnull MemorySegment mem, int offset) {
      return new Vector4f(
         mem.get(PROTO_FLOAT, (long)offset),
         mem.get(PROTO_FLOAT, (long)(offset + 4)),
         mem.get(PROTO_FLOAT, (long)(offset + 8)),
         mem.get(PROTO_FLOAT, (long)(offset + 12))
      );
   }

   @Nonnull
   public static Quaternionf readQuaternionf(@Nonnull MemorySegment mem, int offset) {
      return new Quaternionf(
         mem.get(PROTO_FLOAT, (long)offset),
         mem.get(PROTO_FLOAT, (long)(offset + 4)),
         mem.get(PROTO_FLOAT, (long)(offset + 8)),
         mem.get(PROTO_FLOAT, (long)(offset + 12))
      );
   }

   @Nonnull
   public static Matrix4f readMatrix4f(@Nonnull ByteBuf buf, int offset) {
      return new Matrix4f(
         buf.getFloatLE(offset),
         buf.getFloatLE(offset + 4),
         buf.getFloatLE(offset + 8),
         buf.getFloatLE(offset + 12),
         buf.getFloatLE(offset + 16),
         buf.getFloatLE(offset + 20),
         buf.getFloatLE(offset + 24),
         buf.getFloatLE(offset + 28),
         buf.getFloatLE(offset + 32),
         buf.getFloatLE(offset + 36),
         buf.getFloatLE(offset + 40),
         buf.getFloatLE(offset + 44),
         buf.getFloatLE(offset + 48),
         buf.getFloatLE(offset + 52),
         buf.getFloatLE(offset + 56),
         buf.getFloatLE(offset + 60)
      );
   }

   public static void writeVector2f(@Nonnull ByteBuf buf, @Nonnull Vector2fc v) {
      buf.writeFloatLE(v.x());
      buf.writeFloatLE(v.y());
   }

   public static void writeVector3f(@Nonnull ByteBuf buf, @Nonnull Vector3fc v) {
      buf.writeFloatLE(v.x());
      buf.writeFloatLE(v.y());
      buf.writeFloatLE(v.z());
   }

   public static void writeVector4f(@Nonnull ByteBuf buf, @Nonnull Vector4fc v) {
      buf.writeFloatLE(v.x());
      buf.writeFloatLE(v.y());
      buf.writeFloatLE(v.z());
      buf.writeFloatLE(v.w());
   }

   public static void writeQuaternionf(@Nonnull ByteBuf buf, @Nonnull Quaternionfc q) {
      buf.writeFloatLE(q.x());
      buf.writeFloatLE(q.y());
      buf.writeFloatLE(q.z());
      buf.writeFloatLE(q.w());
   }

   public static void writeVector2f(@Nonnull MemorySegment mem, int offset, @Nonnull Vector2fc v) {
      mem.set(PROTO_FLOAT, (long)offset, v.x());
      mem.set(PROTO_FLOAT, (long)(offset + 4), v.y());
   }

   public static void writeVector3f(@Nonnull MemorySegment mem, int offset, @Nonnull Vector3fc v) {
      mem.set(PROTO_FLOAT, (long)offset, v.x());
      mem.set(PROTO_FLOAT, (long)(offset + 4), v.y());
      mem.set(PROTO_FLOAT, (long)(offset + 8), v.z());
   }

   public static void writeVector4f(@Nonnull MemorySegment mem, int offset, @Nonnull Vector4fc v) {
      mem.set(PROTO_FLOAT, (long)offset, v.x());
      mem.set(PROTO_FLOAT, (long)(offset + 4), v.y());
      mem.set(PROTO_FLOAT, (long)(offset + 8), v.z());
      mem.set(PROTO_FLOAT, (long)(offset + 12), v.w());
   }

   public static void writeQuaternionf(@Nonnull MemorySegment mem, int offset, @Nonnull Quaternionfc q) {
      mem.set(PROTO_FLOAT, (long)offset, q.x());
      mem.set(PROTO_FLOAT, (long)(offset + 4), q.y());
      mem.set(PROTO_FLOAT, (long)(offset + 8), q.z());
      mem.set(PROTO_FLOAT, (long)(offset + 12), q.w());
   }

   public static void writeFixedAsciiString(@Nonnull MemorySegment mem, int offset, @Nullable String value, int length) {
      if (value != null) {
         byte[] bytes = value.getBytes(StandardCharsets.US_ASCII);
         if (bytes.length > length) {
            throw new ProtocolException("Fixed ASCII string exceeds length: " + bytes.length + " > " + length);
         }

         MemorySegment.copy(bytes, 0, mem, PROTO_BYTE, offset, bytes.length);
         mem.asSlice(offset + bytes.length, length - bytes.length).fill((byte)0);
      } else {
         mem.asSlice(offset, length).fill((byte)0);
      }
   }

   public static void writeFixedString(@Nonnull MemorySegment mem, int offset, @Nullable String value, int length) {
      if (value != null) {
         byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
         if (bytes.length > length) {
            throw new ProtocolException("Fixed UTF-8 string exceeds length: " + bytes.length + " > " + length);
         }

         MemorySegment.copy(bytes, 0, mem, PROTO_BYTE, offset, bytes.length);
         mem.asSlice(offset + bytes.length, length - bytes.length).fill((byte)0);
      } else {
         mem.asSlice(offset, length).fill((byte)0);
      }
   }

   public static void writeMatrix4f(@Nonnull ByteBuf buf, @Nonnull Matrix4fc m) {
      buf.writeFloatLE(m.m00());
      buf.writeFloatLE(m.m10());
      buf.writeFloatLE(m.m20());
      buf.writeFloatLE(m.m30());
      buf.writeFloatLE(m.m01());
      buf.writeFloatLE(m.m11());
      buf.writeFloatLE(m.m21());
      buf.writeFloatLE(m.m31());
      buf.writeFloatLE(m.m02());
      buf.writeFloatLE(m.m12());
      buf.writeFloatLE(m.m22());
      buf.writeFloatLE(m.m32());
      buf.writeFloatLE(m.m03());
      buf.writeFloatLE(m.m13());
      buf.writeFloatLE(m.m23());
      buf.writeFloatLE(m.m33());
   }

   @Nonnull
   public static UUID readUUID(@Nonnull ByteBuf buf, int offset) {
      long mostSig = buf.getLong(offset);
      long leastSig = buf.getLong(offset + 8);
      return new UUID(mostSig, leastSig);
   }

   public static void writeUUID(@Nonnull ByteBuf buf, @Nonnull UUID value) {
      buf.writeLong(value.getMostSignificantBits());
      buf.writeLong(value.getLeastSignificantBits());
   }

   @Nonnull
   public static UUID readUUID(@Nonnull MemorySegment mem, int offset) {
      long mostSig = mem.get(UUID_LONG, (long)offset);
      long leastSig = mem.get(UUID_LONG, (long)(offset + 8));
      return new UUID(mostSig, leastSig);
   }

   public static void writeUUID(@Nonnull MemorySegment mem, int offset, @Nonnull UUID value) {
      mem.set(UUID_LONG, (long)offset, value.getMostSignificantBits());
      mem.set(UUID_LONG, (long)(offset + 8), value.getLeastSignificantBits());
   }

   public static float halfToFloat(short half) {
      int h = half & '\uffff';
      int sign = h >>> 15 & 1;
      int exp = h >>> 10 & 31;
      int mant = h & 1023;
      if (exp == 0) {
         if (mant == 0) {
            return sign == 0 ? 0.0F : -0.0F;
         }

         for (exp = 1; (mant & 1024) == 0; exp--) {
            mant <<= 1;
         }

         mant &= 1023;
      } else if (exp == 31) {
         return mant == 0 ? (sign == 0 ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY) : Float.NaN;
      }

      int floatBits = sign << 31 | exp + 112 << 23 | mant << 13;
      return Float.intBitsToFloat(floatBits);
   }

   public static short floatToHalf(float f) {
      int bits = Float.floatToRawIntBits(f);
      int sign = bits >>> 16 & 32768;
      int val = (bits & 2147483647) + 4096;
      if (val >= 1199570944) {
         if ((bits & 2147483647) >= 1199570944) {
            return val < 2139095040 ? (short)(sign | 31744) : (short)(sign | 31744 | (bits & 8388607) >>> 13);
         } else {
            return (short)(sign | 31743);
         }
      } else if (val >= 947912704) {
         return (short)(sign | val - 939524096 >>> 13);
      } else if (val < 855638016) {
         return (short)sign;
      } else {
         val = (bits & 2147483647) >>> 23;
         return (short)(sign | (bits & 8388607 | 8388608) + (8388608 >>> val - 102) >>> 126 - val);
      }
   }

   private static int compressToBuffer(@Nonnull ByteBuf src, @Nonnull ByteBuf dst, int dstOffset, int maxDstSize) {
      if (src.isDirect() && dst.isDirect()) {
         return Zstd.compress(dst.nioBuffer(dstOffset, maxDstSize), src.nioBuffer(), COMPRESSION_LEVEL);
      } else {
         int srcSize = src.readableBytes();
         byte[] srcBytes = new byte[srcSize];
         src.getBytes(src.readerIndex(), srcBytes);
         byte[] compressed = Zstd.compress(srcBytes, COMPRESSION_LEVEL);
         dst.setBytes(dstOffset, compressed);
         return compressed.length;
      }
   }

   @Nonnull
   private static ByteBuf decompressFromBuffer(@Nonnull ByteBufAllocator allocator, @Nonnull ByteBuf src, int srcOffset, int srcLength, int maxDecompressedSize) {
      if (srcLength > maxDecompressedSize) {
         throw new ProtocolException("Compressed size " + srcLength + " exceeds max decompressed size " + maxDecompressedSize);
      } else if (src.isDirect()) {
         ByteBuffer srcNio = src.nioBuffer(srcOffset, srcLength);
         long decompressedSize = Zstd.getFrameContentSize(srcNio);
         if (decompressedSize < 0L) {
            throw new ProtocolException("Invalid Zstd frame or unknown content size");
         } else if (decompressedSize > maxDecompressedSize) {
            throw new ProtocolException("Decompressed size " + decompressedSize + " exceeds maximum " + maxDecompressedSize);
         } else {
            ByteBuf dst = allocator.directBuffer((int)decompressedSize);

            try {
               ByteBuffer dstNio = dst.nioBuffer(0, (int)decompressedSize);
               int result = Zstd.decompress(dstNio, srcNio);
               if (Zstd.isError(result)) {
                  throw new ProtocolException("Zstd decompression failed: " + Zstd.getErrorName(result));
               } else {
                  dst.writerIndex(result);
                  return dst;
               }
            } catch (Exception var11) {
               dst.release();
               throw var11;
            }
         }
      } else {
         byte[] srcBytes = new byte[srcLength];
         src.getBytes(srcOffset, srcBytes);
         long decompressedSize = Zstd.getFrameContentSize(srcBytes);
         if (decompressedSize < 0L) {
            throw new ProtocolException("Invalid Zstd frame or unknown content size");
         } else if (decompressedSize > maxDecompressedSize) {
            throw new ProtocolException("Decompressed size " + decompressedSize + " exceeds maximum " + maxDecompressedSize);
         } else {
            byte[] decompressed = Zstd.decompress(srcBytes, (int)decompressedSize);
            return Unpooled.wrappedBuffer(decompressed);
         }
      }
   }

   private static MemorySegment decompressFromBuffer(
      @Nonnull MemorySegment decompressionContext, @Nonnull Arena arena, @Nonnull MemorySegment src, int srcOffset, int srcLength, int maxDecompressedSize
   ) {
      if (srcLength > maxDecompressedSize) {
         throw new ProtocolException("Compressed size " + srcLength + " exceeds max decompressed size " + maxDecompressedSize);
      } else {
         MemorySegment srcBuf = src.asSlice(srcOffset, srcLength);
         long decompressedSize = ZstdNative.getFrameContentSize(srcBuf, srcLength);
         if (decompressedSize < 0L) {
            throw new ProtocolException("Invalid Zstd frame or unknown content size");
         } else if (decompressedSize > maxDecompressedSize) {
            throw new ProtocolException("Decompressed size " + decompressedSize + " exceeds maximum " + maxDecompressedSize);
         } else {
            MemorySegment dst = arena.allocate((int)decompressedSize);
            long result = ZstdNative.decompressDCtx(decompressionContext, dst, dst.byteSize(), srcBuf, srcLength);
            if (ZstdNative.isError(result)) {
               throw new ProtocolException("Decompression failed with error code: " + ZstdNative.getErrorName(result));
            } else {
               return dst;
            }
         }
      }
   }

   public static void writeFramedPacket(
      @Nonnull Packet packet,
      @Nonnull Class<? extends Packet> packetClass,
      @Nonnull ByteBuf out,
      @Nonnull ByteBufAllocator allocator,
      @Nonnull PacketStatsRecorder statsRecorder
   ) {
      Integer id = PacketRegistry.getId(packetClass);
      if (id == null) {
         throw new ProtocolException("Unknown packet type: " + packetClass.getName());
      } else {
         PacketRegistry.PacketInfo info = PacketRegistry.getToClientPacketById(id);
         writeFramedPacketWithInfo(packet, info, out, allocator, statsRecorder);
      }
   }

   public static void writeFramedPacketWithInfo(
      @Nonnull Packet packet,
      @Nonnull PacketRegistry.PacketInfo info,
      @Nonnull ByteBuf out,
      @Nonnull ByteBufAllocator allocator,
      @Nonnull PacketStatsRecorder statsRecorder
   ) {
      int lengthIndex = out.writerIndex();
      out.writeIntLE(0);
      out.writeIntLE(info.id());
      if (info.compressed()) {
         writeCompressed(packet, info, info.id(), out, allocator, statsRecorder, lengthIndex);
      } else {
         writeUncompressed(packet, info, info.id(), out, statsRecorder, lengthIndex);
      }
   }

   private static void writeCompressed(
      @Nonnull Packet packet,
      @Nonnull PacketRegistry.PacketInfo info,
      int id,
      @Nonnull ByteBuf out,
      @Nonnull ByteBufAllocator allocator,
      @Nonnull PacketStatsRecorder statsRecorder,
      int lengthIndex
   ) {
      ByteBuf payloadBuf = allocator.buffer(Math.min(info.maxSize(), 65536));

      try {
         packet.serialize(payloadBuf);
         int serializedSize = payloadBuf.readableBytes();
         if (serializedSize > info.maxSize()) {
            throw new ProtocolException("Packet " + info.name() + " serialized to " + serializedSize + " bytes, exceeds max size " + info.maxSize());
         }

         if (serializedSize != 0) {
            int compressBound = (int)Zstd.compressBound(serializedSize);
            out.ensureWritable(compressBound);
            int compressedSize = compressToBuffer(payloadBuf, out, out.writerIndex(), compressBound);
            if (Zstd.isError(compressedSize)) {
               throw new ProtocolException("Zstd compression failed: " + Zstd.getErrorName(compressedSize));
            }

            if (compressedSize > 1677721600) {
               throw new ProtocolException("Packet " + info.name() + " compressed payload size " + compressedSize + " exceeds protocol maximum");
            }

            out.writerIndex(out.writerIndex() + compressedSize);
            out.setIntLE(lengthIndex, compressedSize);
            statsRecorder.recordSend(id, serializedSize, compressedSize);
            return;
         }

         out.setIntLE(lengthIndex, 0);
         statsRecorder.recordSend(id, 0, 0);
      } finally {
         payloadBuf.release();
      }
   }

   private static void writeUncompressed(
      @Nonnull Packet packet,
      @Nonnull PacketRegistry.PacketInfo info,
      int id,
      @Nonnull ByteBuf out,
      @Nonnull PacketStatsRecorder statsRecorder,
      int lengthIndex
   ) {
      int payloadStart = out.writerIndex();
      packet.serialize(out);
      int serializedSize = out.writerIndex() - payloadStart;
      if (serializedSize > info.maxSize()) {
         out.writerIndex(lengthIndex);
         throw new ProtocolException("Packet " + info.name() + " serialized to " + serializedSize + " bytes, exceeds max size " + info.maxSize());
      } else if (serializedSize > 1677721600) {
         out.writerIndex(lengthIndex);
         throw new ProtocolException("Packet " + info.name() + " payload size " + serializedSize + " exceeds protocol maximum");
      } else {
         out.setIntLE(lengthIndex, serializedSize);
         statsRecorder.recordSend(id, serializedSize, 0);
      }
   }

   @Nonnull
   public static Packet readFramedPacket(
      @Nonnull ByteBuf in, int payloadLength, @Nonnull ByteBufAllocator allocator, @Nonnull PacketStatsRecorder statsRecorder
   ) {
      int packetId = in.readIntLE();
      PacketRegistry.PacketInfo info = PacketRegistry.getToServerPacketById(packetId);
      if (info == null) {
         in.skipBytes(payloadLength);
         throw new ProtocolException("Unknown packet ID: " + packetId);
      } else {
         return readFramedPacketWithInfo(in, payloadLength, allocator, info, statsRecorder);
      }
   }

   @Nonnull
   public static Packet readFramedPacketWithInfo(
      @Nonnull ByteBuf in,
      int payloadLength,
      @Nonnull ByteBufAllocator allocator,
      @Nonnull PacketRegistry.PacketInfo info,
      @Nonnull PacketStatsRecorder statsRecorder
   ) {
      int compressedSize = 0;
      ByteBuf payload;
      int uncompressedSize;
      if (info.compressed() && payloadLength > 0) {
         try {
            payload = decompressFromBuffer(allocator, in, in.readerIndex(), payloadLength, info.maxSize());
         } catch (ProtocolException var13) {
            in.skipBytes(payloadLength);
            throw var13;
         }

         in.skipBytes(payloadLength);
         uncompressedSize = payload.readableBytes();
         compressedSize = payloadLength;
      } else if (payloadLength > 0) {
         payload = in.readRetainedSlice(payloadLength);
         uncompressedSize = payloadLength;
      } else {
         payload = Unpooled.EMPTY_BUFFER;
         uncompressedSize = 0;
      }

      Packet var9;
      try {
         Packet packet = info.deserialize().deserialize(payload, 0);
         statsRecorder.recordReceive(info.id(), uncompressedSize, compressedSize);
         var9 = packet;
      } finally {
         if (payloadLength > 0) {
            payload.release();
         }
      }

      return var9;
   }

   public static int writeFramedPacket(
      @Nonnull MemorySegment compressionContext,
      @Nonnull Packet packet,
      @Nonnull PacketRegistry.PacketInfo packetInfo,
      @Nonnull MemorySegment out,
      int offset,
      int packetSize,
      @Nonnull PacketStatsRecorder statsRecorder
   ) {
      out.set(PROTO_INT, (long)(offset + 4), packetInfo.id());
      int dataStart = offset + 8;
      if (packetInfo.compressed()) {
         int var13;
         try (Arena arena = Arena.ofConfined()) {
            MemorySegment compressionTarget = out.asSlice(dataStart);
            MemorySegment src = arena.allocate(packetSize);
            packet.serialize(src, 0);
            if (packetSize > packetInfo.maxSize()) {
               throw new ProtocolException("Packet " + packetInfo.name() + " serialized to " + packetSize + " bytes, exceeds max size " + packetInfo.maxSize());
            }

            long result = ZstdNative.compressCCtx(compressionContext, compressionTarget, compressionTarget.byteSize(), src, src.byteSize(), COMPRESSION_LEVEL);
            if (ZstdNative.isError(result)) {
               throw new ProtocolException("Zstd compression failed: " + ZstdNative.getErrorName(result));
            }

            if (result > 1677721600L) {
               throw new ProtocolException("Packet " + packetInfo.name() + " compressed payload size " + result + " exceeds protocol maximum");
            }

            out.set(PROTO_INT, (long)offset, (int)result);
            statsRecorder.recordSend(packetInfo.id(), packetSize, (int)result);
            var13 = (int)(result + 8L);
         }

         return var13;
      } else {
         int size = packet.serialize(out, dataStart);
         if (size > packetInfo.maxSize()) {
            throw new ProtocolException("Packet " + packetInfo.name() + " serialized to " + size + " bytes, exceeds max size " + packetInfo.maxSize());
         } else if (size > 1677721600) {
            throw new ProtocolException("Packet " + packetInfo.name() + " payload size " + size + " exceeds protocol maximum");
         } else {
            out.set(PROTO_INT, (long)offset, size);
            statsRecorder.recordSend(packetInfo.id(), size, 0);
            return size + 8;
         }
      }
   }

   @Nonnull
   public static Packet readFramedPacket(
      @Nonnull MemorySegment decompressionContext, @Nonnull MemorySegment in, int payloadLength, @Nonnull PacketStatsRecorder statsRecorder
   ) {
      int packetId = in.get(PROTO_INT, 0L);
      PacketRegistry.PacketInfo info = PacketRegistry.getToServerPacketById(packetId);
      if (info == null) {
         throw new ProtocolException("Unknown packet ID: " + packetId);
      } else if (payloadLength > info.maxSize()) {
         throw new ProtocolException("Packet " + info.name() + " payload size " + payloadLength + " exceeds max size " + info.maxSize());
      } else {
         return readFramedPacketWithInfo(decompressionContext, in, (int)PROTO_INT.byteSize(), payloadLength, info, statsRecorder);
      }
   }

   @Nonnull
   public static Packet readFramedPacketWithInfo(
      @Nonnull MemorySegment decompressionContext,
      @Nonnull MemorySegment in,
      int offset,
      int payloadLength,
      @Nonnull PacketRegistry.PacketInfo info,
      @Nonnull PacketStatsRecorder statsRecorder
   ) {
      int compressedSize = 0;
      Arena arena = null;
      int uncompressedSize;
      MemorySegment payload;
      byte var17;
      if (info.compressed() && payloadLength > 0) {
         arena = Arena.ofConfined();

         try {
            payload = decompressFromBuffer(decompressionContext, arena, in, offset, payloadLength, info.maxSize());
         } catch (Throwable var15) {
            arena.close();
            throw var15;
         }

         uncompressedSize = (int)payload.byteSize();
         compressedSize = payloadLength;
         var17 = 0;
      } else {
         payload = in.asSlice(offset, payloadLength);
         var17 = 0;
         uncompressedSize = payloadLength;
      }

      Packet var11;
      try {
         Packet packet = info.toObject().deserialize(payload, var17);
         statsRecorder.recordReceive(info.id(), uncompressedSize, compressedSize);
         var11 = packet;
      } finally {
         if (arena != null) {
            arena.close();
         }
      }

      return var11;
   }
}
