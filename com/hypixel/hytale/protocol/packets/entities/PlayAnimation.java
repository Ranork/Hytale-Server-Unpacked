package com.hypixel.hytale.protocol.packets.entities;

import com.hypixel.hytale.protocol.AnimationSlot;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayAnimation implements Packet, ToClientPacket {
   public static final int PACKET_ID = 162;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 6;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 14;
   public static final int MAX_SIZE = 32768024;
   public int entityId;
   @Nullable
   public String itemAnimationsId;
   @Nullable
   public String animationId;
   @Nonnull
   public AnimationSlot slot = AnimationSlot.Movement;

   @Override
   public int getId() {
      return 162;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public PlayAnimation() {
   }

   public PlayAnimation(int entityId, @Nullable String itemAnimationsId, @Nullable String animationId, @Nonnull AnimationSlot slot) {
      this.entityId = entityId;
      this.itemAnimationsId = itemAnimationsId;
      this.animationId = animationId;
      this.slot = slot;
   }

   public PlayAnimation(@Nonnull PlayAnimation other) {
      this.entityId = other.entityId;
      this.itemAnimationsId = other.itemAnimationsId;
      this.animationId = other.animationId;
      this.slot = other.slot;
   }

   @Nonnull
   public static PlayAnimation deserialize(@Nonnull ByteBuf buf, int offset) {
      PlayAnimation obj = new PlayAnimation();
      byte nullBits = buf.getByte(offset);
      obj.entityId = buf.getIntLE(offset + 1);
      obj.slot = AnimationSlot.fromValue(buf.getByte(offset + 5));
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 14 + buf.getIntLE(offset + 6);
         int itemAnimationsIdLen = VarInt.peek(buf, varPos0);
         if (itemAnimationsIdLen < 0) {
            throw ProtocolException.negativeLength("ItemAnimationsId", itemAnimationsIdLen);
         }

         if (itemAnimationsIdLen > 4096000) {
            throw ProtocolException.stringTooLong("ItemAnimationsId", itemAnimationsIdLen, 4096000);
         }

         obj.itemAnimationsId = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 14 + buf.getIntLE(offset + 10);
         int animationIdLen = VarInt.peek(buf, varPos1);
         if (animationIdLen < 0) {
            throw ProtocolException.negativeLength("AnimationId", animationIdLen);
         }

         if (animationIdLen > 4096000) {
            throw ProtocolException.stringTooLong("AnimationId", animationIdLen, 4096000);
         }

         obj.animationId = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 14;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 6);
         int pos0 = offset + 14 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 10);
         int pos1 = offset + 14 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.itemAnimationsId != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.animationId != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.entityId);
      buf.writeByte(this.slot.getValue());
      int itemAnimationsIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int animationIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.itemAnimationsId != null) {
         buf.setIntLE(itemAnimationsIdOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.itemAnimationsId, 4096000);
      } else {
         buf.setIntLE(itemAnimationsIdOffsetSlot, -1);
      }

      if (this.animationId != null) {
         buf.setIntLE(animationIdOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.animationId, 4096000);
      } else {
         buf.setIntLE(animationIdOffsetSlot, -1);
      }
   }

   @Override
   public int computeSize() {
      int size = 14;
      if (this.itemAnimationsId != null) {
         size += PacketIO.stringSize(this.itemAnimationsId);
      }

      if (this.animationId != null) {
         size += PacketIO.stringSize(this.animationId);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 14) {
         return ValidationResult.error("Buffer too small: expected at least 14 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int itemAnimationsIdOffset = buffer.getIntLE(offset + 6);
            if (itemAnimationsIdOffset < 0) {
               return ValidationResult.error("Invalid offset for ItemAnimationsId");
            }

            int pos = offset + 14 + itemAnimationsIdOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ItemAnimationsId");
            }

            int itemAnimationsIdLen = VarInt.peek(buffer, pos);
            if (itemAnimationsIdLen < 0) {
               return ValidationResult.error("Invalid string length for ItemAnimationsId");
            }

            if (itemAnimationsIdLen > 4096000) {
               return ValidationResult.error("ItemAnimationsId exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += itemAnimationsIdLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading ItemAnimationsId");
            }
         }

         if ((nullBits & 2) != 0) {
            int animationIdOffset = buffer.getIntLE(offset + 10);
            if (animationIdOffset < 0) {
               return ValidationResult.error("Invalid offset for AnimationId");
            }

            int posx = offset + 14 + animationIdOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for AnimationId");
            }

            int animationIdLen = VarInt.peek(buffer, posx);
            if (animationIdLen < 0) {
               return ValidationResult.error("Invalid string length for AnimationId");
            }

            if (animationIdLen > 4096000) {
               return ValidationResult.error("AnimationId exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
            posx += animationIdLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading AnimationId");
            }
         }

         return ValidationResult.OK;
      }
   }

   public PlayAnimation clone() {
      PlayAnimation copy = new PlayAnimation();
      copy.entityId = this.entityId;
      copy.itemAnimationsId = this.itemAnimationsId;
      copy.animationId = this.animationId;
      copy.slot = this.slot;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof PlayAnimation other)
            ? false
            : this.entityId == other.entityId
               && Objects.equals(this.itemAnimationsId, other.itemAnimationsId)
               && Objects.equals(this.animationId, other.animationId)
               && Objects.equals(this.slot, other.slot);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.entityId, this.itemAnimationsId, this.animationId, this.slot);
   }
}
