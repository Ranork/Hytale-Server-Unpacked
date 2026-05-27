package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.joml.Vector3fc;

public class RailPoint {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 24;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 24;
   public static final int MAX_SIZE = 24;
   @Nonnull
   public Vector3fc point = PacketIO.ZERO_VECTOR3;
   @Nonnull
   public Vector3fc normal = PacketIO.ZERO_VECTOR3;

   public RailPoint() {
   }

   public RailPoint(@Nonnull Vector3fc point, @Nonnull Vector3fc normal) {
      this.point = point;
      this.normal = normal;
   }

   public RailPoint(@Nonnull RailPoint other) {
      this.point = other.point;
      this.normal = other.normal;
   }

   @Nonnull
   public static RailPoint deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 24) {
         throw ProtocolException.bufferTooSmall("RailPoint", 24, buf.readableBytes() - offset);
      } else {
         RailPoint obj = new RailPoint();
         obj.point = PacketIO.readVector3f(buf, offset + 0);
         obj.normal = PacketIO.readVector3f(buf, offset + 12);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 24;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 24L;
   }

   public static Vector3fc getPoint(MemorySegment mem) {
      return getPoint(mem, 0);
   }

   public static Vector3fc getPoint(MemorySegment mem, int offset) {
      return PacketIO.readVector3f(mem, offset + 0);
   }

   public static Vector3fc getNormal(MemorySegment mem) {
      return getNormal(mem, 0);
   }

   public static Vector3fc getNormal(MemorySegment mem, int offset) {
      return PacketIO.readVector3f(mem, offset + 12);
   }

   public static RailPoint toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static RailPoint toObject(MemorySegment mem, int offset) {
      if (offset + 24 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("RailPoint", offset + 24, (int)mem.byteSize());
      } else {
         return new RailPoint(PacketIO.readVector3f(mem, offset + 0), PacketIO.readVector3f(mem, offset + 12));
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      PacketIO.writeVector3f(buf, this.point);
      PacketIO.writeVector3f(buf, this.normal);
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      PacketIO.writeVector3f(mem, offset + 0, this.point);
      PacketIO.writeVector3f(mem, offset + 12, this.normal);
      return 24;
   }

   public int computeSize() {
      return 24;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 24 ? ValidationResult.error("Buffer too small: expected at least 24 bytes") : ValidationResult.OK;
   }

   public RailPoint clone() {
      RailPoint copy = new RailPoint();
      copy.point = this.point;
      copy.normal = this.normal;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof RailPoint other) ? false : Objects.equals(this.point, other.point) && Objects.equals(this.normal, other.normal);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.point, this.normal);
   }
}
