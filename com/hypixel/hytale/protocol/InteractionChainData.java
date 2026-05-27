package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3fc;

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
   public Vector3fc hitLocation;
   @Nullable
   public String hitDetail;
   @Nullable
   public BlockPosition blockPosition;
   public int targetSlot = Integer.MIN_VALUE;
   @Nullable
   public Vector3fc hitNormal;

   public InteractionChainData() {
   }

   public InteractionChainData(
      int entityId,
      @Nonnull UUID proxyId,
      @Nullable Vector3fc hitLocation,
      @Nullable String hitDetail,
      @Nullable BlockPosition blockPosition,
      int targetSlot,
      @Nullable Vector3fc hitNormal
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
      if (buf.readableBytes() - offset < 61) {
         throw ProtocolException.bufferTooSmall("InteractionChainData", 61, buf.readableBytes() - offset);
      } else {
         InteractionChainData obj = new InteractionChainData();
         byte nullBits = buf.getByte(offset);
         obj.entityId = buf.getIntLE(offset + 1);
         obj.proxyId = PacketIO.readUUID(buf, offset + 5);
         if ((nullBits & 1) != 0) {
            obj.hitLocation = PacketIO.readVector3f(buf, offset + 21);
         }

         if ((nullBits & 2) != 0) {
            obj.blockPosition = BlockPosition.deserialize(buf, offset + 33);
         }

         obj.targetSlot = buf.getIntLE(offset + 45);
         if ((nullBits & 4) != 0) {
            obj.hitNormal = PacketIO.readVector3f(buf, offset + 49);
         }

         int pos = offset + 61;
         if ((nullBits & 8) != 0) {
            int hitDetailLen = VarInt.peek(buf, pos);
            if (hitDetailLen < 0) {
               throw ProtocolException.invalidVarInt("HitDetail");
            }

            int hitDetailVarLen = VarInt.size(hitDetailLen);
            if (hitDetailLen > 4096000) {
               throw ProtocolException.stringTooLong("HitDetail", hitDetailLen, 4096000);
            }

            if (pos + hitDetailVarLen + hitDetailLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("HitDetail", pos + hitDetailVarLen + hitDetailLen, buf.readableBytes());
            }

            obj.hitDetail = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
            pos += hitDetailVarLen + hitDetailLen;
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 61;
      if ((nullBits & 8) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.size(sl) + sl;
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 61L;
   }

   public static int getEntityId(MemorySegment mem) {
      return getEntityId(mem, 0);
   }

   public static int getEntityId(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   public static UUID getProxyId(MemorySegment mem) {
      return getProxyId(mem, 0);
   }

   public static UUID getProxyId(MemorySegment mem, int offset) {
      return PacketIO.readUUID(mem, offset + 5);
   }

   @Nullable
   public static Vector3fc getHitLocation(MemorySegment mem) {
      return getHitLocation(mem, 0);
   }

   @Nullable
   public static Vector3fc getHitLocation(MemorySegment mem, int offset) {
      return hasHitLocation(mem, offset) ? PacketIO.readVector3f(mem, offset + 21) : null;
   }

   @Nullable
   public static String getHitDetail(MemorySegment mem) {
      return getHitDetail(mem, 0);
   }

   @Nullable
   public static String getHitDetail(MemorySegment mem, int offset) {
      return hasHitDetail(mem, offset) ? PacketIO.readVarString("HitDetail", mem, offset + 61, 4096000, PacketIO.UTF8) : null;
   }

   @Nullable
   public static BlockPosition getBlockPosition(MemorySegment mem) {
      return getBlockPosition(mem, 0);
   }

   @Nullable
   public static BlockPosition getBlockPosition(MemorySegment mem, int offset) {
      return hasBlockPosition(mem, offset) ? BlockPosition.toObject(mem, offset + 33) : null;
   }

   public static int getTargetSlot(MemorySegment mem) {
      return getTargetSlot(mem, 0);
   }

   public static int getTargetSlot(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 45));
   }

   @Nullable
   public static Vector3fc getHitNormal(MemorySegment mem) {
      return getHitNormal(mem, 0);
   }

   @Nullable
   public static Vector3fc getHitNormal(MemorySegment mem, int offset) {
      return hasHitNormal(mem, offset) ? PacketIO.readVector3f(mem, offset + 49) : null;
   }

   public static boolean hasHitLocation(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasBlockPosition(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasHitNormal(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasHitDetail(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   public static InteractionChainData toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static InteractionChainData toObject(MemorySegment mem, int offset) {
      if (offset + 61 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("InteractionChainData", offset + 61, (int)mem.byteSize());
      } else {
         return new InteractionChainData(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            PacketIO.readUUID(mem, offset + 5),
            hasHitLocation(mem, offset) ? PacketIO.readVector3f(mem, offset + 21) : null,
            hasHitDetail(mem, offset) ? PacketIO.readVarString("HitDetail", mem, offset + 61, 4096000, PacketIO.UTF8) : null,
            hasBlockPosition(mem, offset) ? BlockPosition.toObject(mem, offset + 33) : null,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 45)),
            hasHitNormal(mem, offset) ? PacketIO.readVector3f(mem, offset + 49) : null
         );
      }
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
         PacketIO.writeVector3f(buf, this.hitLocation);
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
         PacketIO.writeVector3f(buf, this.hitNormal);
      } else {
         buf.writeZero(12);
      }

      if (this.hitDetail != null) {
         PacketIO.writeVarString(buf, this.hitDetail, 4096000);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
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

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.entityId);
      PacketIO.writeUUID(mem, offset + 5, this.proxyId);
      if (this.hitLocation != null) {
         PacketIO.writeVector3f(mem, offset + 21, this.hitLocation);
      } else {
         mem.asSlice(offset + 21, 12L).fill((byte)0);
      }

      if (this.blockPosition != null) {
         this.blockPosition.serialize(mem, offset + 33);
      } else {
         mem.asSlice(offset + 33, 12L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_INT, (long)(offset + 45), this.targetSlot);
      if (this.hitNormal != null) {
         PacketIO.writeVector3f(mem, offset + 49, this.hitNormal);
      } else {
         mem.asSlice(offset + 49, 12L).fill((byte)0);
      }

      int varOffset = offset + 61;
      if (this.hitDetail != null) {
         varOffset += PacketIO.writeVarString(mem, varOffset, this.hitDetail, 4096000);
      }

      return varOffset - offset;
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

            pos += VarInt.size(hitDetailLen);
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
      copy.hitLocation = this.hitLocation;
      copy.hitDetail = this.hitDetail;
      copy.blockPosition = this.blockPosition != null ? this.blockPosition.clone() : null;
      copy.targetSlot = this.targetSlot;
      copy.hitNormal = this.hitNormal;
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
