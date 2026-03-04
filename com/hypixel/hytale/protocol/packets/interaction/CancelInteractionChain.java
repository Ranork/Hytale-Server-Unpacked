package com.hypixel.hytale.protocol.packets.interaction;

import com.hypixel.hytale.protocol.ForkedChainId;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CancelInteractionChain implements Packet, ToClientPacket {
   public static final int PACKET_ID = 291;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 5;
   public static final int MAX_SIZE = 1038;
   public int chainId;
   @Nullable
   public ForkedChainId forkedId;

   @Override
   public int getId() {
      return 291;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public CancelInteractionChain() {
   }

   public CancelInteractionChain(int chainId, @Nullable ForkedChainId forkedId) {
      this.chainId = chainId;
      this.forkedId = forkedId;
   }

   public CancelInteractionChain(@Nonnull CancelInteractionChain other) {
      this.chainId = other.chainId;
      this.forkedId = other.forkedId;
   }

   @Nonnull
   public static CancelInteractionChain deserialize(@Nonnull ByteBuf buf, int offset) {
      CancelInteractionChain obj = new CancelInteractionChain();
      byte nullBits = buf.getByte(offset);
      obj.chainId = buf.getIntLE(offset + 1);
      int pos = offset + 5;
      if ((nullBits & 1) != 0) {
         obj.forkedId = ForkedChainId.deserialize(buf, pos);
         pos += ForkedChainId.computeBytesConsumed(buf, pos);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 5;
      if ((nullBits & 1) != 0) {
         pos += ForkedChainId.computeBytesConsumed(buf, pos);
      }

      return pos - offset;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.forkedId != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.chainId);
      if (this.forkedId != null) {
         this.forkedId.serialize(buf);
      }
   }

   @Override
   public int computeSize() {
      int size = 5;
      if (this.forkedId != null) {
         size += this.forkedId.computeSize();
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
            ValidationResult forkedIdResult = ForkedChainId.validateStructure(buffer, pos);
            if (!forkedIdResult.isValid()) {
               return ValidationResult.error("Invalid ForkedId: " + forkedIdResult.error());
            }

            pos += ForkedChainId.computeBytesConsumed(buffer, pos);
         }

         return ValidationResult.OK;
      }
   }

   public CancelInteractionChain clone() {
      CancelInteractionChain copy = new CancelInteractionChain();
      copy.chainId = this.chainId;
      copy.forkedId = this.forkedId != null ? this.forkedId.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof CancelInteractionChain other) ? false : this.chainId == other.chainId && Objects.equals(this.forkedId, other.forkedId);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.chainId, this.forkedId);
   }
}
