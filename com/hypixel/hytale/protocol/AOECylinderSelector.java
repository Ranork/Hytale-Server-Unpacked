package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3fc;

public class AOECylinderSelector extends Selector {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 21;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 21;
   public static final int MAX_SIZE = 21;
   public float range;
   public float height;
   @Nullable
   public Vector3fc offset;

   public AOECylinderSelector() {
   }

   public AOECylinderSelector(float range, float height, @Nullable Vector3fc offset) {
      this.range = range;
      this.height = height;
      this.offset = offset;
   }

   public AOECylinderSelector(@Nonnull AOECylinderSelector other) {
      this.range = other.range;
      this.height = other.height;
      this.offset = other.offset;
   }

   @Nonnull
   public static AOECylinderSelector deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 21) {
         throw ProtocolException.bufferTooSmall("AOECylinderSelector", 21, buf.readableBytes() - offset);
      } else {
         AOECylinderSelector obj = new AOECylinderSelector();
         byte nullBits = buf.getByte(offset);
         obj.range = buf.getFloatLE(offset + 1);
         obj.height = buf.getFloatLE(offset + 5);
         if ((nullBits & 1) != 0) {
            obj.offset = PacketIO.readVector3f(buf, offset + 9);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 21;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 21L;
   }

   public static float getRange(MemorySegment mem) {
      return getRange(mem, 0);
   }

   public static float getRange(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1));
   }

   public static float getHeight(MemorySegment mem) {
      return getHeight(mem, 0);
   }

   public static float getHeight(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 5));
   }

   @Nullable
   public static Vector3fc getOffset(MemorySegment mem) {
      return getOffset(mem, 0);
   }

   @Nullable
   public static Vector3fc getOffset(MemorySegment mem, int offset) {
      return hasOffset(mem, offset) ? PacketIO.readVector3f(mem, offset + 9) : null;
   }

   public static boolean hasOffset(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static AOECylinderSelector toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AOECylinderSelector toObject(MemorySegment mem, int offset) {
      if (offset + 21 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AOECylinderSelector", offset + 21, (int)mem.byteSize());
      } else {
         return new AOECylinderSelector(
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 5)),
            hasOffset(mem, offset) ? PacketIO.readVector3f(mem, offset + 9) : null
         );
      }
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.offset != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeFloatLE(this.range);
      buf.writeFloatLE(this.height);
      if (this.offset != null) {
         PacketIO.writeVector3f(buf, this.offset);
      } else {
         buf.writeZero(12);
      }

      return buf.writerIndex() - startPos;
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.offset != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 1), this.range);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 5), this.height);
      if (this.offset != null) {
         PacketIO.writeVector3f(mem, offset + 9, this.offset);
      } else {
         mem.asSlice(offset + 9, 12L).fill((byte)0);
      }

      return 21;
   }

   @Override
   public int computeSize() {
      return 21;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 21) {
         return ValidationResult.error("Buffer too small: expected at least 21 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         return ValidationResult.OK;
      }
   }

   public AOECylinderSelector clone() {
      AOECylinderSelector copy = new AOECylinderSelector();
      copy.range = this.range;
      copy.height = this.height;
      copy.offset = this.offset;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AOECylinderSelector other)
            ? false
            : this.range == other.range && this.height == other.height && Objects.equals(this.offset, other.offset);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.range, this.height, this.offset);
   }
}
