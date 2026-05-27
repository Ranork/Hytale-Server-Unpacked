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

public class ItemPullbackConfiguration {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 49;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 49;
   public static final int MAX_SIZE = 49;
   @Nullable
   public Vector3fc leftOffsetOverride;
   @Nullable
   public Vector3fc leftRotationOverride;
   @Nullable
   public Vector3fc rightOffsetOverride;
   @Nullable
   public Vector3fc rightRotationOverride;

   public ItemPullbackConfiguration() {
   }

   public ItemPullbackConfiguration(
      @Nullable Vector3fc leftOffsetOverride,
      @Nullable Vector3fc leftRotationOverride,
      @Nullable Vector3fc rightOffsetOverride,
      @Nullable Vector3fc rightRotationOverride
   ) {
      this.leftOffsetOverride = leftOffsetOverride;
      this.leftRotationOverride = leftRotationOverride;
      this.rightOffsetOverride = rightOffsetOverride;
      this.rightRotationOverride = rightRotationOverride;
   }

   public ItemPullbackConfiguration(@Nonnull ItemPullbackConfiguration other) {
      this.leftOffsetOverride = other.leftOffsetOverride;
      this.leftRotationOverride = other.leftRotationOverride;
      this.rightOffsetOverride = other.rightOffsetOverride;
      this.rightRotationOverride = other.rightRotationOverride;
   }

   @Nonnull
   public static ItemPullbackConfiguration deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 49) {
         throw ProtocolException.bufferTooSmall("ItemPullbackConfiguration", 49, buf.readableBytes() - offset);
      } else {
         ItemPullbackConfiguration obj = new ItemPullbackConfiguration();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            obj.leftOffsetOverride = PacketIO.readVector3f(buf, offset + 1);
         }

         if ((nullBits & 2) != 0) {
            obj.leftRotationOverride = PacketIO.readVector3f(buf, offset + 13);
         }

         if ((nullBits & 4) != 0) {
            obj.rightOffsetOverride = PacketIO.readVector3f(buf, offset + 25);
         }

         if ((nullBits & 8) != 0) {
            obj.rightRotationOverride = PacketIO.readVector3f(buf, offset + 37);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 49;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 49L;
   }

   @Nullable
   public static Vector3fc getLeftOffsetOverride(MemorySegment mem) {
      return getLeftOffsetOverride(mem, 0);
   }

   @Nullable
   public static Vector3fc getLeftOffsetOverride(MemorySegment mem, int offset) {
      return hasLeftOffsetOverride(mem, offset) ? PacketIO.readVector3f(mem, offset + 1) : null;
   }

   @Nullable
   public static Vector3fc getLeftRotationOverride(MemorySegment mem) {
      return getLeftRotationOverride(mem, 0);
   }

   @Nullable
   public static Vector3fc getLeftRotationOverride(MemorySegment mem, int offset) {
      return hasLeftRotationOverride(mem, offset) ? PacketIO.readVector3f(mem, offset + 13) : null;
   }

   @Nullable
   public static Vector3fc getRightOffsetOverride(MemorySegment mem) {
      return getRightOffsetOverride(mem, 0);
   }

   @Nullable
   public static Vector3fc getRightOffsetOverride(MemorySegment mem, int offset) {
      return hasRightOffsetOverride(mem, offset) ? PacketIO.readVector3f(mem, offset + 25) : null;
   }

   @Nullable
   public static Vector3fc getRightRotationOverride(MemorySegment mem) {
      return getRightRotationOverride(mem, 0);
   }

   @Nullable
   public static Vector3fc getRightRotationOverride(MemorySegment mem, int offset) {
      return hasRightRotationOverride(mem, offset) ? PacketIO.readVector3f(mem, offset + 37) : null;
   }

   public static boolean hasLeftOffsetOverride(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasLeftRotationOverride(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasRightOffsetOverride(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasRightRotationOverride(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   public static ItemPullbackConfiguration toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ItemPullbackConfiguration toObject(MemorySegment mem, int offset) {
      if (offset + 49 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ItemPullbackConfiguration", offset + 49, (int)mem.byteSize());
      } else {
         return new ItemPullbackConfiguration(
            hasLeftOffsetOverride(mem, offset) ? PacketIO.readVector3f(mem, offset + 1) : null,
            hasLeftRotationOverride(mem, offset) ? PacketIO.readVector3f(mem, offset + 13) : null,
            hasRightOffsetOverride(mem, offset) ? PacketIO.readVector3f(mem, offset + 25) : null,
            hasRightRotationOverride(mem, offset) ? PacketIO.readVector3f(mem, offset + 37) : null
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.leftOffsetOverride != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.leftRotationOverride != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.rightOffsetOverride != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.rightRotationOverride != null) {
         nullBits = (byte)(nullBits | 8);
      }

      buf.writeByte(nullBits);
      if (this.leftOffsetOverride != null) {
         PacketIO.writeVector3f(buf, this.leftOffsetOverride);
      } else {
         buf.writeZero(12);
      }

      if (this.leftRotationOverride != null) {
         PacketIO.writeVector3f(buf, this.leftRotationOverride);
      } else {
         buf.writeZero(12);
      }

      if (this.rightOffsetOverride != null) {
         PacketIO.writeVector3f(buf, this.rightOffsetOverride);
      } else {
         buf.writeZero(12);
      }

      if (this.rightRotationOverride != null) {
         PacketIO.writeVector3f(buf, this.rightRotationOverride);
      } else {
         buf.writeZero(12);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.leftOffsetOverride != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.leftRotationOverride != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.rightOffsetOverride != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.rightRotationOverride != null) {
         nullBits = (byte)(nullBits | 8);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      if (this.leftOffsetOverride != null) {
         PacketIO.writeVector3f(mem, offset + 1, this.leftOffsetOverride);
      } else {
         mem.asSlice(offset + 1, 12L).fill((byte)0);
      }

      if (this.leftRotationOverride != null) {
         PacketIO.writeVector3f(mem, offset + 13, this.leftRotationOverride);
      } else {
         mem.asSlice(offset + 13, 12L).fill((byte)0);
      }

      if (this.rightOffsetOverride != null) {
         PacketIO.writeVector3f(mem, offset + 25, this.rightOffsetOverride);
      } else {
         mem.asSlice(offset + 25, 12L).fill((byte)0);
      }

      if (this.rightRotationOverride != null) {
         PacketIO.writeVector3f(mem, offset + 37, this.rightRotationOverride);
      } else {
         mem.asSlice(offset + 37, 12L).fill((byte)0);
      }

      return 49;
   }

   public int computeSize() {
      return 49;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 49) {
         return ValidationResult.error("Buffer too small: expected at least 49 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         return ValidationResult.OK;
      }
   }

   public ItemPullbackConfiguration clone() {
      ItemPullbackConfiguration copy = new ItemPullbackConfiguration();
      copy.leftOffsetOverride = this.leftOffsetOverride;
      copy.leftRotationOverride = this.leftRotationOverride;
      copy.rightOffsetOverride = this.rightOffsetOverride;
      copy.rightRotationOverride = this.rightRotationOverride;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ItemPullbackConfiguration other)
            ? false
            : Objects.equals(this.leftOffsetOverride, other.leftOffsetOverride)
               && Objects.equals(this.leftRotationOverride, other.leftRotationOverride)
               && Objects.equals(this.rightOffsetOverride, other.rightOffsetOverride)
               && Objects.equals(this.rightRotationOverride, other.rightRotationOverride);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.leftOffsetOverride, this.leftRotationOverride, this.rightOffsetOverride, this.rightRotationOverride);
   }
}
