package com.hypixel.hytale.protocol.packets.interaction;

import com.hypixel.hytale.protocol.ForkedChainId;
import com.hypixel.hytale.protocol.InteractionType;
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

public class PlayInteractionFor implements Packet, ToClientPacket {
   public static final int PACKET_ID = 292;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 19;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 27;
   public static final int MAX_SIZE = 16385065;
   public int entityId;
   public int chainId;
   @Nullable
   public ForkedChainId forkedId;
   public int operationIndex;
   public int interactionId;
   @Nullable
   public String interactedItemId;
   @Nonnull
   public InteractionType interactionType = InteractionType.Primary;
   public boolean cancel;

   @Override
   public int getId() {
      return 292;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public PlayInteractionFor() {
   }

   public PlayInteractionFor(
      int entityId,
      int chainId,
      @Nullable ForkedChainId forkedId,
      int operationIndex,
      int interactionId,
      @Nullable String interactedItemId,
      @Nonnull InteractionType interactionType,
      boolean cancel
   ) {
      this.entityId = entityId;
      this.chainId = chainId;
      this.forkedId = forkedId;
      this.operationIndex = operationIndex;
      this.interactionId = interactionId;
      this.interactedItemId = interactedItemId;
      this.interactionType = interactionType;
      this.cancel = cancel;
   }

   public PlayInteractionFor(@Nonnull PlayInteractionFor other) {
      this.entityId = other.entityId;
      this.chainId = other.chainId;
      this.forkedId = other.forkedId;
      this.operationIndex = other.operationIndex;
      this.interactionId = other.interactionId;
      this.interactedItemId = other.interactedItemId;
      this.interactionType = other.interactionType;
      this.cancel = other.cancel;
   }

   @Nonnull
   public static PlayInteractionFor deserialize(@Nonnull ByteBuf buf, int offset) {
      PlayInteractionFor obj = new PlayInteractionFor();
      byte nullBits = buf.getByte(offset);
      obj.entityId = buf.getIntLE(offset + 1);
      obj.chainId = buf.getIntLE(offset + 5);
      obj.operationIndex = buf.getIntLE(offset + 9);
      obj.interactionId = buf.getIntLE(offset + 13);
      obj.interactionType = InteractionType.fromValue(buf.getByte(offset + 17));
      obj.cancel = buf.getByte(offset + 18) != 0;
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 27 + buf.getIntLE(offset + 19);
         obj.forkedId = ForkedChainId.deserialize(buf, varPos0);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 27 + buf.getIntLE(offset + 23);
         int interactedItemIdLen = VarInt.peek(buf, varPos1);
         if (interactedItemIdLen < 0) {
            throw ProtocolException.negativeLength("InteractedItemId", interactedItemIdLen);
         }

         if (interactedItemIdLen > 4096000) {
            throw ProtocolException.stringTooLong("InteractedItemId", interactedItemIdLen, 4096000);
         }

         obj.interactedItemId = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 27;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 19);
         int pos0 = offset + 27 + fieldOffset0;
         pos0 += ForkedChainId.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 23);
         int pos1 = offset + 27 + fieldOffset1;
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
      if (this.forkedId != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.interactedItemId != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.entityId);
      buf.writeIntLE(this.chainId);
      buf.writeIntLE(this.operationIndex);
      buf.writeIntLE(this.interactionId);
      buf.writeByte(this.interactionType.getValue());
      buf.writeByte(this.cancel ? 1 : 0);
      int forkedIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int interactedItemIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.forkedId != null) {
         buf.setIntLE(forkedIdOffsetSlot, buf.writerIndex() - varBlockStart);
         this.forkedId.serialize(buf);
      } else {
         buf.setIntLE(forkedIdOffsetSlot, -1);
      }

      if (this.interactedItemId != null) {
         buf.setIntLE(interactedItemIdOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.interactedItemId, 4096000);
      } else {
         buf.setIntLE(interactedItemIdOffsetSlot, -1);
      }
   }

   @Override
   public int computeSize() {
      int size = 27;
      if (this.forkedId != null) {
         size += this.forkedId.computeSize();
      }

      if (this.interactedItemId != null) {
         size += PacketIO.stringSize(this.interactedItemId);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 27) {
         return ValidationResult.error("Buffer too small: expected at least 27 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int forkedIdOffset = buffer.getIntLE(offset + 19);
            if (forkedIdOffset < 0) {
               return ValidationResult.error("Invalid offset for ForkedId");
            }

            int pos = offset + 27 + forkedIdOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ForkedId");
            }

            ValidationResult forkedIdResult = ForkedChainId.validateStructure(buffer, pos);
            if (!forkedIdResult.isValid()) {
               return ValidationResult.error("Invalid ForkedId: " + forkedIdResult.error());
            }

            pos += ForkedChainId.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits & 2) != 0) {
            int interactedItemIdOffset = buffer.getIntLE(offset + 23);
            if (interactedItemIdOffset < 0) {
               return ValidationResult.error("Invalid offset for InteractedItemId");
            }

            int posx = offset + 27 + interactedItemIdOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for InteractedItemId");
            }

            int interactedItemIdLen = VarInt.peek(buffer, posx);
            if (interactedItemIdLen < 0) {
               return ValidationResult.error("Invalid string length for InteractedItemId");
            }

            if (interactedItemIdLen > 4096000) {
               return ValidationResult.error("InteractedItemId exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
            posx += interactedItemIdLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading InteractedItemId");
            }
         }

         return ValidationResult.OK;
      }
   }

   public PlayInteractionFor clone() {
      PlayInteractionFor copy = new PlayInteractionFor();
      copy.entityId = this.entityId;
      copy.chainId = this.chainId;
      copy.forkedId = this.forkedId != null ? this.forkedId.clone() : null;
      copy.operationIndex = this.operationIndex;
      copy.interactionId = this.interactionId;
      copy.interactedItemId = this.interactedItemId;
      copy.interactionType = this.interactionType;
      copy.cancel = this.cancel;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof PlayInteractionFor other)
            ? false
            : this.entityId == other.entityId
               && this.chainId == other.chainId
               && Objects.equals(this.forkedId, other.forkedId)
               && this.operationIndex == other.operationIndex
               && this.interactionId == other.interactionId
               && Objects.equals(this.interactedItemId, other.interactedItemId)
               && Objects.equals(this.interactionType, other.interactionType)
               && this.cancel == other.cancel;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(
         this.entityId, this.chainId, this.forkedId, this.operationIndex, this.interactionId, this.interactedItemId, this.interactionType, this.cancel
      );
   }
}
