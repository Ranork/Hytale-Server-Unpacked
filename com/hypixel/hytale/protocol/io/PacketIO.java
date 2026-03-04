package com.hypixel.hytale.protocol.io;

import com.github.luben.zstd.Zstd;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.PacketRegistry;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class PacketIO {
   public static final int FRAME_HEADER_SIZE = 4;
   public static final Charset UTF8 = StandardCharsets.UTF_8;
   public static final Charset ASCII = StandardCharsets.US_ASCII;
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
   public static String readVarString(@Nonnull ByteBuf buf, int offset) {
      return readVarString(buf, offset, StandardCharsets.UTF_8);
   }

   @Nonnull
   public static String readVarAsciiString(@Nonnull ByteBuf buf, int offset) {
      return readVarString(buf, offset, StandardCharsets.US_ASCII);
   }

   @Nonnull
   public static String readVarString(@Nonnull ByteBuf buf, int offset, Charset charset) {
      int len = VarInt.peek(buf, offset);
      int varIntLen = VarInt.length(buf, offset);
      byte[] bytes = new byte[len];
      buf.getBytes(offset + varIntLen, bytes);
      return new String(bytes, charset);
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

   private static float halfToFloat(short half) {
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

   private static short floatToHalf(float f) {
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
   private static ByteBuf decompressFromBuffer(@Nonnull ByteBuf src, int srcOffset, int srcLength, int maxDecompressedSize) {
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
            ByteBuf dst = Unpooled.directBuffer((int)decompressedSize);
            ByteBuffer dstNio = dst.nioBuffer(0, (int)decompressedSize);
            int result = Zstd.decompress(dstNio, srcNio);
            if (Zstd.isError(result)) {
               dst.release();
               throw new ProtocolException("Zstd decompression failed: " + Zstd.getErrorName(result));
            } else {
               dst.writerIndex(result);
               return dst;
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

   public static void writeFramedPacket(
      @Nonnull Packet packet, @Nonnull Class<? extends Packet> packetClass, @Nonnull ByteBuf out, @Nonnull PacketStatsRecorder statsRecorder
   ) {
      Integer id = PacketRegistry.getId(packetClass);
      if (id == null) {
         throw new ProtocolException("Unknown packet type: " + packetClass.getName());
      } else {
         PacketRegistry.PacketInfo info = PacketRegistry.getToClientPacketById(id);
         int lengthIndex = out.writerIndex();
         out.writeIntLE(0);
         out.writeIntLE(id);
         ByteBuf payloadBuf = Unpooled.buffer(Math.min(info.maxSize(), 65536));

         try {
            packet.serialize(payloadBuf);
            int serializedSize = payloadBuf.readableBytes();
            if (serializedSize > info.maxSize()) {
               throw new ProtocolException("Packet " + info.name() + " serialized to " + serializedSize + " bytes, exceeds max size " + info.maxSize());
            }

            if (info.compressed() && serializedSize > 0) {
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
            } else {
               if (serializedSize > 1677721600) {
                  throw new ProtocolException("Packet " + info.name() + " payload size " + serializedSize + " exceeds protocol maximum");
               }

               out.writeBytes(payloadBuf);
               out.setIntLE(lengthIndex, serializedSize);
               statsRecorder.recordSend(id, serializedSize, 0);
            }
         } finally {
            payloadBuf.release();
         }
      }
   }

   @Nonnull
   public static Packet readFramedPacket(@Nonnull ByteBuf in, int payloadLength, @Nonnull PacketStatsRecorder statsRecorder) {
      int packetId = in.readIntLE();
      PacketRegistry.PacketInfo info = PacketRegistry.getToServerPacketById(packetId);
      if (info == null) {
         in.skipBytes(payloadLength);
         throw new ProtocolException("Unknown packet ID: " + packetId);
      } else {
         return readFramedPacketWithInfo(in, payloadLength, info, statsRecorder);
      }
   }

   @Nonnull
   public static Packet readFramedPacketWithInfo(
      @Nonnull ByteBuf in, int payloadLength, @Nonnull PacketRegistry.PacketInfo info, @Nonnull PacketStatsRecorder statsRecorder
   ) {
      int compressedSize = 0;
      ByteBuf payload;
      int uncompressedSize;
      if (info.compressed() && payloadLength > 0) {
         try {
            payload = decompressFromBuffer(in, in.readerIndex(), payloadLength, info.maxSize());
         } catch (ProtocolException var12) {
            in.skipBytes(payloadLength);
            throw var12;
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

      Packet var8;
      try {
         Packet packet = info.deserialize().apply(payload, 0);
         statsRecorder.recordReceive(info.id(), uncompressedSize, compressedSize);
         var8 = packet;
      } finally {
         if (payloadLength > 0) {
            payload.release();
         }
      }

      return var8;
   }
}
