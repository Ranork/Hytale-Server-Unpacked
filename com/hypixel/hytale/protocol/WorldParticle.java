package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3fc;

public class WorldParticle {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 32;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 32;
   public static final int MAX_SIZE = 16384037;
   @Nullable
   public String systemId;
   public float scale;
   @Nullable
   public Color color;
   @Nullable
   public Vector3fc positionOffset;
   @Nullable
   public Direction rotationOffset;

   public WorldParticle() {
   }

   public WorldParticle(@Nullable String systemId, float scale, @Nullable Color color, @Nullable Vector3fc positionOffset, @Nullable Direction rotationOffset) {
      this.systemId = systemId;
      this.scale = scale;
      this.color = color;
      this.positionOffset = positionOffset;
      this.rotationOffset = rotationOffset;
   }

   public WorldParticle(@Nonnull WorldParticle other) {
      this.systemId = other.systemId;
      this.scale = other.scale;
      this.color = other.color;
      this.positionOffset = other.positionOffset;
      this.rotationOffset = other.rotationOffset;
   }

   @Nonnull
   public static WorldParticle deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 32) {
         throw ProtocolException.bufferTooSmall("WorldParticle", 32, buf.readableBytes() - offset);
      } else {
         WorldParticle obj = new WorldParticle();
         byte nullBits = buf.getByte(offset);
         obj.scale = buf.getFloatLE(offset + 1);
         if ((nullBits & 1) != 0) {
            obj.color = Color.deserialize(buf, offset + 5);
         }

         if ((nullBits & 2) != 0) {
            obj.positionOffset = PacketIO.readVector3f(buf, offset + 8);
         }

         if ((nullBits & 4) != 0) {
            obj.rotationOffset = Direction.deserialize(buf, offset + 20);
         }

         int pos = offset + 32;
         if ((nullBits & 8) != 0) {
            int systemIdLen = VarInt.peek(buf, pos);
            if (systemIdLen < 0) {
               throw ProtocolException.invalidVarInt("SystemId");
            }

            int systemIdVarLen = VarInt.size(systemIdLen);
            if (systemIdLen > 4096000) {
               throw ProtocolException.stringTooLong("SystemId", systemIdLen, 4096000);
            }

            if (pos + systemIdVarLen + systemIdLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("SystemId", pos + systemIdVarLen + systemIdLen, buf.readableBytes());
            }

            obj.systemId = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
            pos += systemIdVarLen + systemIdLen;
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 32;
      if ((nullBits & 8) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.size(sl) + sl;
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 32L;
   }

   @Nullable
   public static String getSystemId(MemorySegment mem) {
      return getSystemId(mem, 0);
   }

   @Nullable
   public static String getSystemId(MemorySegment mem, int offset) {
      return hasSystemId(mem, offset) ? PacketIO.readVarString("SystemId", mem, offset + 32, 4096000, PacketIO.UTF8) : null;
   }

   public static float getScale(MemorySegment mem) {
      return getScale(mem, 0);
   }

   public static float getScale(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1));
   }

   @Nullable
   public static Color getColor(MemorySegment mem) {
      return getColor(mem, 0);
   }

   @Nullable
   public static Color getColor(MemorySegment mem, int offset) {
      return hasColor(mem, offset) ? Color.toObject(mem, offset + 5) : null;
   }

   @Nullable
   public static Vector3fc getPositionOffset(MemorySegment mem) {
      return getPositionOffset(mem, 0);
   }

   @Nullable
   public static Vector3fc getPositionOffset(MemorySegment mem, int offset) {
      return hasPositionOffset(mem, offset) ? PacketIO.readVector3f(mem, offset + 8) : null;
   }

   @Nullable
   public static Direction getRotationOffset(MemorySegment mem) {
      return getRotationOffset(mem, 0);
   }

   @Nullable
   public static Direction getRotationOffset(MemorySegment mem, int offset) {
      return hasRotationOffset(mem, offset) ? Direction.toObject(mem, offset + 20) : null;
   }

   public static boolean hasColor(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasPositionOffset(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasRotationOffset(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasSystemId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   public static WorldParticle toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static WorldParticle toObject(MemorySegment mem, int offset) {
      if (offset + 32 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("WorldParticle", offset + 32, (int)mem.byteSize());
      } else {
         return new WorldParticle(
            hasSystemId(mem, offset) ? PacketIO.readVarString("SystemId", mem, offset + 32, 4096000, PacketIO.UTF8) : null,
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1)),
            hasColor(mem, offset) ? Color.toObject(mem, offset + 5) : null,
            hasPositionOffset(mem, offset) ? PacketIO.readVector3f(mem, offset + 8) : null,
            hasRotationOffset(mem, offset) ? Direction.toObject(mem, offset + 20) : null
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.color != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.positionOffset != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.rotationOffset != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.systemId != null) {
         nullBits = (byte)(nullBits | 8);
      }

      buf.writeByte(nullBits);
      buf.writeFloatLE(this.scale);
      if (this.color != null) {
         this.color.serialize(buf);
      } else {
         buf.writeZero(3);
      }

      if (this.positionOffset != null) {
         PacketIO.writeVector3f(buf, this.positionOffset);
      } else {
         buf.writeZero(12);
      }

      if (this.rotationOffset != null) {
         this.rotationOffset.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      if (this.systemId != null) {
         PacketIO.writeVarString(buf, this.systemId, 4096000);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.color != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.positionOffset != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.rotationOffset != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.systemId != null) {
         nullBits = (byte)(nullBits | 8);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 1), this.scale);
      if (this.color != null) {
         this.color.serialize(mem, offset + 5);
      } else {
         mem.asSlice(offset + 5, 3L).fill((byte)0);
      }

      if (this.positionOffset != null) {
         PacketIO.writeVector3f(mem, offset + 8, this.positionOffset);
      } else {
         mem.asSlice(offset + 8, 12L).fill((byte)0);
      }

      if (this.rotationOffset != null) {
         this.rotationOffset.serialize(mem, offset + 20);
      } else {
         mem.asSlice(offset + 20, 12L).fill((byte)0);
      }

      int varOffset = offset + 32;
      if (this.systemId != null) {
         varOffset += PacketIO.writeVarString(mem, varOffset, this.systemId, 4096000);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 32;
      if (this.systemId != null) {
         size += PacketIO.stringSize(this.systemId);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 32) {
         return ValidationResult.error("Buffer too small: expected at least 32 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 32;
         if ((nullBits & 8) != 0) {
            int systemIdLen = VarInt.peek(buffer, pos);
            if (systemIdLen < 0) {
               return ValidationResult.error("Invalid string length for SystemId");
            }

            if (systemIdLen > 4096000) {
               return ValidationResult.error("SystemId exceeds max length 4096000");
            }

            pos += VarInt.size(systemIdLen);
            pos += systemIdLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading SystemId");
            }
         }

         return ValidationResult.OK;
      }
   }

   public WorldParticle clone() {
      WorldParticle copy = new WorldParticle();
      copy.systemId = this.systemId;
      copy.scale = this.scale;
      copy.color = this.color != null ? this.color.clone() : null;
      copy.positionOffset = this.positionOffset;
      copy.rotationOffset = this.rotationOffset != null ? this.rotationOffset.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof WorldParticle other)
            ? false
            : Objects.equals(this.systemId, other.systemId)
               && this.scale == other.scale
               && Objects.equals(this.color, other.color)
               && Objects.equals(this.positionOffset, other.positionOffset)
               && Objects.equals(this.rotationOffset, other.rotationOffset);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.systemId, this.scale, this.color, this.positionOffset, this.rotationOffset);
   }
}
