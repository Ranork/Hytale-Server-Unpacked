package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InteractionChainData {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 61;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 61;
   public static final int MAX_SIZE = 16384066;
   public int entityId = -1;
   @Nonnull
   public UUID proxyId = new UUID(0L, 0L);
   @Nullable
   public Vector3f hitLocation;
   @Nullable
   public String hitDetail;
   @Nullable
   public BlockPosition blockPosition;
   public int targetSlot = Integer.MIN_VALUE;
   @Nullable
   public Vector3f hitNormal;

   public InteractionChainData() {
   }

   public InteractionChainData(
      int entityId,
      @Nonnull UUID proxyId,
      @Nullable Vector3f hitLocation,
      @Nullable String hitDetail,
      @Nullable BlockPosition blockPosition,
      int targetSlot,
      @Nullable Vector3f hitNormal
   ) {
      this.entityId = entityId;
      this.proxyId = proxyId;
      this.hitLocation = hitLocation;
      this.hitDetail = hitDetail;
      this.blockPosition = blockPosition;
      this.targetSlot = targetSlot;
      this.hitNormal = hitNormal;
   }

   public InteractionChainData(@Nonnull InteractionChainData other) {
      this.entityId = other.entityId;
      this.proxyId = other.proxyId;
      this.hitLocation = other.hitLocation;
      this.hitDetail = other.hitDetail;
      this.blockPosition = other.blockPosition;
      this.targetSlot = other.targetSlot;
      this.hitNormal = other.hitNormal;
   }

   @Nonnull
   public static InteractionChainData deserialize(@Nonnull ByteBuf buf, int offset) {
      InteractionChainData obj = new InteractionChainData();
      byte nullBits = buf.getByte(offset);
      obj.entityId = buf.getIntLE(offset + 1);
      obj.proxyId = PacketIO.readUUID(buf, offset + 5);
      if ((nullBits & 1) != 0) {
         obj.hitLocation = Vector3f.deserialize(buf, offset + 21);
      }

      if ((nullBits & 2) != 0) {
         obj.blockPosition = BlockPosition.deserialize(buf, offset + 33);
      }

      obj.targetSlot = buf.getIntLE(offset + 45);
      if ((nullBits & 4) != 0) {
         obj.hitNormal = Vector3f.deserialize(buf, offset + 49);
      }

      int pos = offset + 61;
      if ((nullBits & 8) != 0) {
         int hitDetailLen = VarInt.peek(buf, pos);
         if (hitDetailLen < 0) {
            throw ProtocolException.negativeLength("HitDetail", hitDetailLen);
         }

         if (hitDetailLen > 4096000) {
            throw ProtocolException.stringTooLong("HitDetail", hitDetailLen, 4096000);
         }

         int hitDetailVarLen = VarInt.length(buf, pos);
         obj.hitDetail = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
         pos += hitDetailVarLen + hitDetailLen;
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 61;
      if ((nullBits & 8) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos) + sl;
      }

      return pos - offset;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.hitLocation != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.blockPosition != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.hitNormal != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.hitDetail != null) {
         nullBits = (byte)(nullBits | 8);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.entityId);
      PacketIO.writeUUID(buf, this.proxyId);
      if (this.hitLocation != null) {
         this.hitLocation.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      if (this.blockPosition != null) {
         this.blockPosition.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      buf.writeIntLE(this.targetSlot);
      if (this.hitNormal != null) {
         this.hitNormal.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      if (this.hitDetail != null) {
         PacketIO.writeVarString(buf, this.hitDetail, 4096000);
      }
   }

   public int computeSize() {
      int size = 61;
      if (this.hitDetail != null) {
         size += PacketIO.stringSize(this.hitDetail);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 61) {
         return ValidationResult.error("Buffer too small: expected at least 61 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 61;
         if ((nullBits & 8) != 0) {
            int hitDetailLen = VarInt.peek(buffer, pos);
            if (hitDetailLen < 0) {
               return ValidationResult.error("Invalid string length for HitDetail");
            }

            if (hitDetailLen > 4096000) {
               return ValidationResult.error("HitDetail exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += hitDetailLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading HitDetail");
            }
         }

         return ValidationResult.OK;
      }
   }

   public InteractionChainData clone() {
      InteractionChainData copy = new InteractionChainData();
      copy.entityId = this.entityId;
      copy.proxyId = this.proxyId;
      copy.hitLocation = this.hitLocation != null ? this.hitLocation.clone() : null;
      copy.hitDetail = this.hitDetail;
      copy.blockPosition = this.blockPosition != null ? this.blockPosition.clone() : null;
      copy.targetSlot = this.targetSlot;
      copy.hitNormal = this.hitNormal != null ? this.hitNormal.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof InteractionChainData other)
            ? false
            : this.entityId == other.entityId
               && Objects.equals(this.proxyId, other.proxyId)
               && Objects.equals(this.hitLocation, other.hitLocation)
               && Objects.equals(this.hitDetail, other.hitDetail)
               && Objects.equals(this.blockPosition, other.blockPosition)
               && this.targetSlot == other.targetSlot
               && Objects.equals(this.hitNormal, other.hitNormal);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.entityId, this.proxyId, this.hitLocation, this.hitDetail, this.blockPosition, this.targetSlot, this.hitNormal);
   }
}
