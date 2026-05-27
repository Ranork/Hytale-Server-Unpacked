package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class EditorSelection {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 24;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 24;
   public static final int MAX_SIZE = 24;
   public int minX;
   public int minY;
   public int minZ;
   public int maxX;
   public int maxY;
   public int maxZ;

   public EditorSelection() {
   }

   public EditorSelection(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
      this.minX = minX;
      this.minY = minY;
      this.minZ = minZ;
      this.maxX = maxX;
      this.maxY = maxY;
      this.maxZ = maxZ;
   }

   public EditorSelection(@Nonnull EditorSelection other) {
      this.minX = other.minX;
      this.minY = other.minY;
      this.minZ = other.minZ;
      this.maxX = other.maxX;
      this.maxY = other.maxY;
      this.maxZ = other.maxZ;
   }

   @Nonnull
   public static EditorSelection deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 24) {
         throw ProtocolException.bufferTooSmall("EditorSelection", 24, buf.readableBytes() - offset);
      } else {
         EditorSelection obj = new EditorSelection();
         obj.minX = buf.getIntLE(offset + 0);
         obj.minY = buf.getIntLE(offset + 4);
         obj.minZ = buf.getIntLE(offset + 8);
         obj.maxX = buf.getIntLE(offset + 12);
         obj.maxY = buf.getIntLE(offset + 16);
         obj.maxZ = buf.getIntLE(offset + 20);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 24;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 24L;
   }

   public static int getMinX(MemorySegment mem) {
      return getMinX(mem, 0);
   }

   public static int getMinX(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 0));
   }

   public static int getMinY(MemorySegment mem) {
      return getMinY(mem, 0);
   }

   public static int getMinY(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 4));
   }

   public static int getMinZ(MemorySegment mem) {
      return getMinZ(mem, 0);
   }

   public static int getMinZ(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 8));
   }

   public static int getMaxX(MemorySegment mem) {
      return getMaxX(mem, 0);
   }

   public static int getMaxX(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 12));
   }

   public static int getMaxY(MemorySegment mem) {
      return getMaxY(mem, 0);
   }

   public static int getMaxY(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 16));
   }

   public static int getMaxZ(MemorySegment mem) {
      return getMaxZ(mem, 0);
   }

   public static int getMaxZ(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 20));
   }

   public static EditorSelection toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static EditorSelection toObject(MemorySegment mem, int offset) {
      if (offset + 24 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("EditorSelection", offset + 24, (int)mem.byteSize());
      } else {
         return new EditorSelection(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 0)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 4)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 8)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 12)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 16)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 20))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.minX);
      buf.writeIntLE(this.minY);
      buf.writeIntLE(this.minZ);
      buf.writeIntLE(this.maxX);
      buf.writeIntLE(this.maxY);
      buf.writeIntLE(this.maxZ);
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_INT, (long)(offset + 0), this.minX);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 4), this.minY);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 8), this.minZ);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 12), this.maxX);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 16), this.maxY);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 20), this.maxZ);
      return 24;
   }

   public int computeSize() {
      return 24;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 24 ? ValidationResult.error("Buffer too small: expected at least 24 bytes") : ValidationResult.OK;
   }

   public EditorSelection clone() {
      EditorSelection copy = new EditorSelection();
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
         return !(obj instanceof EditorSelection other)
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
