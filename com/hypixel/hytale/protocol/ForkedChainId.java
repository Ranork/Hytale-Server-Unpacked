package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ForkedChainId {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 9;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 1033;
   public int entryIndex;
   public int subIndex;
   @Nullable
   public ForkedChainId forkedId;

   public ForkedChainId() {
   }

   public ForkedChainId(int entryIndex, int subIndex, @Nullable ForkedChainId forkedId) {
      this.entryIndex = entryIndex;
      this.subIndex = subIndex;
      this.forkedId = forkedId;
   }

   public ForkedChainId(@Nonnull ForkedChainId other) {
      this.entryIndex = other.entryIndex;
      this.subIndex = other.subIndex;
      this.forkedId = other.forkedId;
   }

   @Nonnull
   public static ForkedChainId deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 9) {
         throw ProtocolException.bufferTooSmall("ForkedChainId", 9, buf.readableBytes() - offset);
      } else {
         ForkedChainId obj = new ForkedChainId();
         byte nullBits = buf.getByte(offset);
         obj.entryIndex = buf.getIntLE(offset + 1);
         obj.subIndex = buf.getIntLE(offset + 5);
         int pos = offset + 9;
         if ((nullBits & 1) != 0) {
            obj.forkedId = deserialize(buf, pos);
            pos += computeBytesConsumed(buf, pos);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 9;
      if ((nullBits & 1) != 0) {
         pos += computeBytesConsumed(buf, pos);
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 9L;
   }

   public static int getEntryIndex(MemorySegment mem) {
      return getEntryIndex(mem, 0);
   }

   public static int getEntryIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   public static int getSubIndex(MemorySegment mem) {
      return getSubIndex(mem, 0);
   }

   public static int getSubIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 5));
   }

   @Nullable
   public static ForkedChainId getForkedId(MemorySegment mem) {
      return getForkedId(mem, 0);
   }

   @Nullable
   public static ForkedChainId getForkedId(MemorySegment mem, int offset) {
      return hasForkedId(mem, offset) ? toObject(mem, offset + 9) : null;
   }

   public static boolean hasForkedId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static ForkedChainId toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ForkedChainId toObject(MemorySegment mem, int offset) {
      if (offset + 9 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ForkedChainId", offset + 9, (int)mem.byteSize());
      } else {
         return new ForkedChainId(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 5)),
            hasForkedId(mem, offset) ? toObject(mem, offset + 9) : null
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.forkedId != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.entryIndex);
      buf.writeIntLE(this.subIndex);
      if (this.forkedId != null) {
         this.forkedId.serialize(buf);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.forkedId != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.entryIndex);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 5), this.subIndex);
      int varOffset = offset + 9;
      if (this.forkedId != null) {
         varOffset += this.forkedId.serialize(mem, varOffset);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 9;
      if (this.forkedId != null) {
         size += this.forkedId.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 9) {
         return ValidationResult.error("Buffer too small: expected at least 9 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 9;
         if ((nullBits & 1) != 0) {
            ValidationResult forkedIdResult = validateStructure(buffer, pos);
            if (!forkedIdResult.isValid()) {
               return ValidationResult.error("Invalid ForkedId: " + forkedIdResult.error());
            }

            pos += computeBytesConsumed(buffer, pos);
         }

         return ValidationResult.OK;
      }
   }

   public ForkedChainId clone() {
      ForkedChainId copy = new ForkedChainId();
      copy.entryIndex = this.entryIndex;
      copy.subIndex = this.subIndex;
      copy.forkedId = this.forkedId != null ? this.forkedId.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ForkedChainId other)
            ? false
            : this.entryIndex == other.entryIndex && this.subIndex == other.subIndex && Objects.equals(this.forkedId, other.forkedId);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.entryIndex, this.subIndex, this.forkedId);
   }
}
