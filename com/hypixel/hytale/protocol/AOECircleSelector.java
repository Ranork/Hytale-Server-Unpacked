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

public class AOECircleSelector extends Selector {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 17;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 17;
   public static final int MAX_SIZE = 17;
   public float range;
   @Nullable
   public Vector3fc offset;

   public AOECircleSelector() {
   }

   public AOECircleSelector(float range, @Nullable Vector3fc offset) {
      this.range = range;
      this.offset = offset;
   }

   public AOECircleSelector(@Nonnull AOECircleSelector other) {
      this.range = other.range;
      this.offset = other.offset;
   }

   @Nonnull
   public static AOECircleSelector deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 17) {
         throw ProtocolException.bufferTooSmall("AOECircleSelector", 17, buf.readableBytes() - offset);
      } else {
         AOECircleSelector obj = new AOECircleSelector();
         byte nullBits = buf.getByte(offset);
         obj.range = buf.getFloatLE(offset + 1);
         if ((nullBits & 1) != 0) {
            obj.offset = PacketIO.readVector3f(buf, offset + 5);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 17;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 17L;
   }

   public static float getRange(MemorySegment mem) {
      return getRange(mem, 0);
   }

   public static float getRange(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1));
   }

   @Nullable
   public static Vector3fc getOffset(MemorySegment mem) {
      return getOffset(mem, 0);
   }

   @Nullable
   public static Vector3fc getOffset(MemorySegment mem, int offset) {
      return hasOffset(mem, offset) ? PacketIO.readVector3f(mem, offset + 5) : null;
   }

   public static boolean hasOffset(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static AOECircleSelector toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AOECircleSelector toObject(MemorySegment mem, int offset) {
      if (offset + 17 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AOECircleSelector", offset + 17, (int)mem.byteSize());
      } else {
         return new AOECircleSelector(mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1)), hasOffset(mem, offset) ? PacketIO.readVector3f(mem, offset + 5) : null);
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
      if (this.offset != null) {
         PacketIO.writeVector3f(mem, offset + 5, this.offset);
      } else {
         mem.asSlice(offset + 5, 12L).fill((byte)0);
      }

      return 17;
   }

   @Override
   public int computeSize() {
      return 17;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 17) {
         return ValidationResult.error("Buffer too small: expected at least 17 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         return ValidationResult.OK;
      }
   }

   public AOECircleSelector clone() {
      AOECircleSelector copy = new AOECircleSelector();
      copy.range = this.range;
      copy.offset = this.offset;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AOECircleSelector other) ? false : this.range == other.range && Objects.equals(this.offset, other.offset);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.range, this.offset);
   }
}
