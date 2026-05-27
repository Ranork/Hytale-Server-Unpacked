package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WorldInteraction {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 20;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 20;
   public static final int MAX_SIZE = 20;
   public int entityId;
   @Nullable
   public BlockPosition blockPosition;
   @Nullable
   public BlockRotation blockRotation;

   public WorldInteraction() {
   }

   public WorldInteraction(int entityId, @Nullable BlockPosition blockPosition, @Nullable BlockRotation blockRotation) {
      this.entityId = entityId;
      this.blockPosition = blockPosition;
      this.blockRotation = blockRotation;
   }

   public WorldInteraction(@Nonnull WorldInteraction other) {
      this.entityId = other.entityId;
      this.blockPosition = other.blockPosition;
      this.blockRotation = other.blockRotation;
   }

   @Nonnull
   public static WorldInteraction deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 20) {
         throw ProtocolException.bufferTooSmall("WorldInteraction", 20, buf.readableBytes() - offset);
      } else {
         WorldInteraction obj = new WorldInteraction();
         byte nullBits = buf.getByte(offset);
         obj.entityId = buf.getIntLE(offset + 1);
         if ((nullBits & 1) != 0) {
            obj.blockPosition = BlockPosition.deserialize(buf, offset + 5);
         }

         if ((nullBits & 2) != 0) {
            obj.blockRotation = BlockRotation.deserialize(buf, offset + 17);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 20;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 20L;
   }

   public static int getEntityId(MemorySegment mem) {
      return getEntityId(mem, 0);
   }

   public static int getEntityId(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   @Nullable
   public static BlockPosition getBlockPosition(MemorySegment mem) {
      return getBlockPosition(mem, 0);
   }

   @Nullable
   public static BlockPosition getBlockPosition(MemorySegment mem, int offset) {
      return hasBlockPosition(mem, offset) ? BlockPosition.toObject(mem, offset + 5) : null;
   }

   @Nullable
   public static BlockRotation getBlockRotation(MemorySegment mem) {
      return getBlockRotation(mem, 0);
   }

   @Nullable
   public static BlockRotation getBlockRotation(MemorySegment mem, int offset) {
      return hasBlockRotation(mem, offset) ? BlockRotation.toObject(mem, offset + 17) : null;
   }

   public static boolean hasBlockPosition(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasBlockRotation(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static WorldInteraction toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static WorldInteraction toObject(MemorySegment mem, int offset) {
      if (offset + 20 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("WorldInteraction", offset + 20, (int)mem.byteSize());
      } else {
         return new WorldInteraction(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            hasBlockPosition(mem, offset) ? BlockPosition.toObject(mem, offset + 5) : null,
            hasBlockRotation(mem, offset) ? BlockRotation.toObject(mem, offset + 17) : null
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.blockPosition != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.blockRotation != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.entityId);
      if (this.blockPosition != null) {
         this.blockPosition.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      if (this.blockRotation != null) {
         this.blockRotation.serialize(buf);
      } else {
         buf.writeZero(3);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.blockPosition != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.blockRotation != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.entityId);
      if (this.blockPosition != null) {
         this.blockPosition.serialize(mem, offset + 5);
      } else {
         mem.asSlice(offset + 5, 12L).fill((byte)0);
      }

      if (this.blockRotation != null) {
         this.blockRotation.serialize(mem, offset + 17);
      } else {
         mem.asSlice(offset + 17, 3L).fill((byte)0);
      }

      return 20;
   }

   public int computeSize() {
      return 20;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 20) {
         return ValidationResult.error("Buffer too small: expected at least 20 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         return ValidationResult.OK;
      }
   }

   public WorldInteraction clone() {
      WorldInteraction copy = new WorldInteraction();
      copy.entityId = this.entityId;
      copy.blockPosition = this.blockPosition != null ? this.blockPosition.clone() : null;
      copy.blockRotation = this.blockRotation != null ? this.blockRotation.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof WorldInteraction other)
            ? false
            : this.entityId == other.entityId
               && Objects.equals(this.blockPosition, other.blockPosition)
               && Objects.equals(this.blockRotation, other.blockRotation);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.entityId, this.blockPosition, this.blockRotation);
   }
}
