package com.hypixel.hytale.protocol.packets.buildertools;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
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
      BuilderToolSetEntityCollision obj = new BuilderToolSetEntityCollision();
      byte nullBits = buf.getByte(offset);
      obj.entityId = buf.getIntLE(offset + 1);
      int pos = offset + 5;
      if ((nullBits & 1) != 0) {
         int collisionTypeLen = VarInt.peek(buf, pos);
         if (collisionTypeLen < 0) {
            throw ProtocolException.negativeLength("CollisionType", collisionTypeLen);
         }

         if (collisionTypeLen > 4096000) {
            throw ProtocolException.stringTooLong("CollisionType", collisionTypeLen, 4096000);
         }

         int collisionTypeVarLen = VarInt.length(buf, pos);
         obj.collisionType = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
         pos += collisionTypeVarLen + collisionTypeLen;
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 5;
      if ((nullBits & 1) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos) + sl;
      }

      return pos - offset;
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

            pos += VarInt.length(buffer, pos);
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
