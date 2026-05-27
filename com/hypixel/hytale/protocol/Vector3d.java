package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class Vector3d {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 24;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 24;
   public static final int MAX_SIZE = 24;
   public double x;
   public double y;
   public double z;

   public Vector3d() {
   }

   public Vector3d(double x, double y, double z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public Vector3d(@Nonnull Vector3d other) {
      this.x = other.x;
      this.y = other.y;
      this.z = other.z;
   }

   @Nonnull
   public static Vector3d deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 24) {
         throw ProtocolException.bufferTooSmall("Vector3d", 24, buf.readableBytes() - offset);
      } else {
         Vector3d obj = new Vector3d();
         obj.x = buf.getDoubleLE(offset + 0);
         obj.y = buf.getDoubleLE(offset + 8);
         obj.z = buf.getDoubleLE(offset + 16);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 24;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 24L;
   }

   public static double getX(MemorySegment mem) {
      return getX(mem, 0);
   }

   public static double getX(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 0));
   }

   public static double getY(MemorySegment mem) {
      return getY(mem, 0);
   }

   public static double getY(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 8));
   }

   public static double getZ(MemorySegment mem) {
      return getZ(mem, 0);
   }

   public static double getZ(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 16));
   }

   public static Vector3d toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static Vector3d toObject(MemorySegment mem, int offset) {
      if (offset + 24 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("Vector3d", offset + 24, (int)mem.byteSize());
      } else {
         return new Vector3d(
            mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 0)),
            mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 8)),
            mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 16))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeDoubleLE(this.x);
      buf.writeDoubleLE(this.y);
      buf.writeDoubleLE(this.z);
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_DOUBLE, (long)(offset + 0), this.x);
      mem.set(PacketIO.PROTO_DOUBLE, (long)(offset + 8), this.y);
      mem.set(PacketIO.PROTO_DOUBLE, (long)(offset + 16), this.z);
      return 24;
   }

   public int computeSize() {
      return 24;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 24 ? ValidationResult.error("Buffer too small: expected at least 24 bytes") : ValidationResult.OK;
   }

   public Vector3d clone() {
      Vector3d copy = new Vector3d();
      copy.x = this.x;
      copy.y = this.y;
      copy.z = this.z;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof Vector3d other) ? false : this.x == other.x && this.y == other.y && this.z == other.z;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.x, this.y, this.z);
   }
}
