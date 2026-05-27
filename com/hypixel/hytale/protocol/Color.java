package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class Color {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 3;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 3;
   public static final int MAX_SIZE = 3;
   public byte red;
   public byte green;
   public byte blue;

   public Color() {
   }

   public Color(byte red, byte green, byte blue) {
      this.red = red;
      this.green = green;
      this.blue = blue;
   }

   public Color(@Nonnull Color other) {
      this.red = other.red;
      this.green = other.green;
      this.blue = other.blue;
   }

   @Nonnull
   public static Color deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 3) {
         throw ProtocolException.bufferTooSmall("Color", 3, buf.readableBytes() - offset);
      } else {
         Color obj = new Color();
         obj.red = buf.getByte(offset + 0);
         obj.green = buf.getByte(offset + 1);
         obj.blue = buf.getByte(offset + 2);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 3;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 3L;
   }

   public static byte getRed(MemorySegment mem) {
      return getRed(mem, 0);
   }

   public static byte getRed(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
   }

   public static byte getGreen(MemorySegment mem) {
      return getGreen(mem, 0);
   }

   public static byte getGreen(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
   }

   public static byte getBlue(MemorySegment mem) {
      return getBlue(mem, 0);
   }

   public static byte getBlue(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2));
   }

   public static Color toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static Color toObject(MemorySegment mem, int offset) {
      if (offset + 3 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("Color", offset + 3, (int)mem.byteSize());
      } else {
         return new Color(
            mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0)),
            mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.red);
      buf.writeByte(this.green);
      buf.writeByte(this.blue);
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), this.red);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), this.green);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 2), this.blue);
      return 3;
   }

   public int computeSize() {
      return 3;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 3 ? ValidationResult.error("Buffer too small: expected at least 3 bytes") : ValidationResult.OK;
   }

   public Color clone() {
      Color copy = new Color();
      copy.red = this.red;
      copy.green = this.green;
      copy.blue = this.blue;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof Color other) ? false : this.red == other.red && this.green == other.green && this.blue == other.blue;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.red, this.green, this.blue);
   }
}
