package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class Hitbox {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 24;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 24;
   public static final int MAX_SIZE = 24;
   public float minX;
   public float minY;
   public float minZ;
   public float maxX;
   public float maxY;
   public float maxZ;

   public Hitbox() {
   }

   public Hitbox(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
      this.minX = minX;
      this.minY = minY;
      this.minZ = minZ;
      this.maxX = maxX;
      this.maxY = maxY;
      this.maxZ = maxZ;
   }

   public Hitbox(@Nonnull Hitbox other) {
      this.minX = other.minX;
      this.minY = other.minY;
      this.minZ = other.minZ;
      this.maxX = other.maxX;
      this.maxY = other.maxY;
      this.maxZ = other.maxZ;
   }

   @Nonnull
   public static Hitbox deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 24) {
         throw ProtocolException.bufferTooSmall("Hitbox", 24, buf.readableBytes() - offset);
      } else {
         Hitbox obj = new Hitbox();
         obj.minX = buf.getFloatLE(offset + 0);
         obj.minY = buf.getFloatLE(offset + 4);
         obj.minZ = buf.getFloatLE(offset + 8);
         obj.maxX = buf.getFloatLE(offset + 12);
         obj.maxY = buf.getFloatLE(offset + 16);
         obj.maxZ = buf.getFloatLE(offset + 20);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 24;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 24L;
   }

   public static float getMinX(MemorySegment mem) {
      return getMinX(mem, 0);
   }

   public static float getMinX(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 0));
   }

   public static float getMinY(MemorySegment mem) {
      return getMinY(mem, 0);
   }

   public static float getMinY(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 4));
   }

   public static float getMinZ(MemorySegment mem) {
      return getMinZ(mem, 0);
   }

   public static float getMinZ(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 8));
   }

   public static float getMaxX(MemorySegment mem) {
      return getMaxX(mem, 0);
   }

   public static float getMaxX(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 12));
   }

   public static float getMaxY(MemorySegment mem) {
      return getMaxY(mem, 0);
   }

   public static float getMaxY(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 16));
   }

   public static float getMaxZ(MemorySegment mem) {
      return getMaxZ(mem, 0);
   }

   public static float getMaxZ(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 20));
   }

   public static Hitbox toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static Hitbox toObject(MemorySegment mem, int offset) {
      if (offset + 24 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("Hitbox", offset + 24, (int)mem.byteSize());
      } else {
         return new Hitbox(
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 0)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 4)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 8)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 12)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 16)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 20))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeFloatLE(this.minX);
      buf.writeFloatLE(this.minY);
      buf.writeFloatLE(this.minZ);
      buf.writeFloatLE(this.maxX);
      buf.writeFloatLE(this.maxY);
      buf.writeFloatLE(this.maxZ);
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 0), this.minX);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 4), this.minY);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 8), this.minZ);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 12), this.maxX);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 16), this.maxY);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 20), this.maxZ);
      return 24;
   }

   public int computeSize() {
      return 24;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 24 ? ValidationResult.error("Buffer too small: expected at least 24 bytes") : ValidationResult.OK;
   }

   public Hitbox clone() {
      Hitbox copy = new Hitbox();
      copy.minX = this.minX;
      copy.minY = this.minY;
      copy.minZ = this.minZ;
      copy.maxX = this.maxX;
      copy.maxY = this.maxY;
      copy.maxZ = this.maxZ;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof Hitbox other)
            ? false
            : this.minX == other.minX
               && this.minY == other.minY
               && this.minZ == other.minZ
               && this.maxX == other.maxX
               && this.maxY == other.maxY
               && this.maxZ == other.maxZ;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
   }
}
