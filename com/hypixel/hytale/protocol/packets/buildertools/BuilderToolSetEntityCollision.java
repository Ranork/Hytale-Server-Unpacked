package com.hypixel.hytale.protocol.packets.buildertools;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderToolSetEntityCollision implements Packet, ToServerPacket {
   public static final int PACKET_ID = 425;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 5;
   public static final int MAX_SIZE = 16384010;
   public int entityId;
   @Nullable
   public String collisionType;

   @Override
   public int getId() {
      return 425;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public BuilderToolSetEntityCollision() {
   }

   public BuilderToolSetEntityCollision(int entityId, @Nullable String collisionType) {
      this.entityId = entityId;
      this.collisionType = collisionType;
   }

   public BuilderToolSetEntityCollision(@Nonnull BuilderToolSetEntityCollision other) {
      this.entityId = other.entityId;
      this.collisionType = other.collisionType;
   }

   @Nonnull
   public static BuilderToolSetEntityCollision deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 5) {
         throw ProtocolException.bufferTooSmall("BuilderToolSetEntityCollision", 5, buf.readableBytes() - offset);
      } else {
         BuilderToolSetEntityCollision obj = new BuilderToolSetEntityCollision();
         byte nullBits = buf.getByte(offset);
         obj.entityId = buf.getIntLE(offset + 1);
         int pos = offset + 5;
         if ((nullBits & 1) != 0) {
            int collisionTypeLen = VarInt.peek(buf, pos);
            if (collisionTypeLen < 0) {
               throw ProtocolException.invalidVarInt("CollisionType");
            }

            int collisionTypeVarLen = VarInt.size(collisionTypeLen);
            if (collisionTypeLen > 4096000) {
               throw ProtocolException.stringTooLong("CollisionType", collisionTypeLen, 4096000);
            }

            if (pos + collisionTypeVarLen + collisionTypeLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("CollisionType", pos + collisionTypeVarLen + collisionTypeLen, buf.readableBytes());
            }

            obj.collisionType = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
            pos += collisionTypeVarLen + collisionTypeLen;
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 5;
      if ((nullBits & 1) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.size(sl) + sl;
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 5L;
   }

   public static int getEntityId(MemorySegment mem) {
      return getEntityId(mem, 0);
   }

   public static int getEntityId(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   @Nullable
   public static String getCollisionType(MemorySegment mem) {
      return getCollisionType(mem, 0);
   }

   @Nullable
   public static String getCollisionType(MemorySegment mem, int offset) {
      return hasCollisionType(mem, offset) ? PacketIO.readVarString("CollisionType", mem, offset + 5, 4096000, PacketIO.UTF8) : null;
   }

   public static boolean hasCollisionType(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static BuilderToolSetEntityCollision toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static BuilderToolSetEntityCollision toObject(MemorySegment mem, int offset) {
      if (offset + 5 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("BuilderToolSetEntityCollision", offset + 5, (int)mem.byteSize());
      } else {
         return new BuilderToolSetEntityCollision(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            hasCollisionType(mem, offset) ? PacketIO.readVarString("CollisionType", mem, offset + 5, 4096000, PacketIO.UTF8) : null
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.collisionType != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.entityId);
      if (this.collisionType != null) {
         PacketIO.writeVarString(buf, this.collisionType, 4096000);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.collisionType != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.entityId);
      int varOffset = offset + 5;
      if (this.collisionType != null) {
         varOffset += PacketIO.writeVarString(mem, varOffset, this.collisionType, 4096000);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 5;
      if (this.collisionType != null) {
         size += PacketIO.stringSize(this.collisionType);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 5) {
         return ValidationResult.error("Buffer too small: expected at least 5 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 5;
         if ((nullBits & 1) != 0) {
            int collisionTypeLen = VarInt.peek(buffer, pos);
            if (collisionTypeLen < 0) {
               return ValidationResult.error("Invalid string length for CollisionType");
            }

            if (collisionTypeLen > 4096000) {
               return ValidationResult.error("CollisionType exceeds max length 4096000");
            }

            pos += VarInt.size(collisionTypeLen);
            pos += collisionTypeLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading CollisionType");
            }
         }

         return ValidationResult.OK;
      }
   }

   public BuilderToolSetEntityCollision clone() {
      BuilderToolSetEntityCollision copy = new BuilderToolSetEntityCollision();
      copy.entityId = this.entityId;
      copy.collisionType = this.collisionType;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BuilderToolSetEntityCollision other)
            ? false
            : this.entityId == other.entityId && Objects.equals(this.collisionType, other.collisionType);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.entityId, this.collisionType);
   }
}
