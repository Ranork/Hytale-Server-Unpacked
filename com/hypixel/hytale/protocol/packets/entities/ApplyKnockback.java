package com.hypixel.hytale.protocol.packets.entities;

import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.Position;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ApplyKnockback implements Packet, ToClientPacket {
   public static final int PACKET_ID = 164;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 38;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 38;
   public static final int MAX_SIZE = 38;
   @Nullable
   public Position hitPosition;
   public float x;
   public float y;
   public float z;
   @Nonnull
   public ChangeVelocityType changeType = ChangeVelocityType.Add;

   @Override
   public int getId() {
      return 164;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public ApplyKnockback() {
   }

   public ApplyKnockback(@Nullable Position hitPosition, float x, float y, float z, @Nonnull ChangeVelocityType changeType) {
      this.hitPosition = hitPosition;
      this.x = x;
      this.y = y;
      this.z = z;
      this.changeType = changeType;
   }

   public ApplyKnockback(@Nonnull ApplyKnockback other) {
      this.hitPosition = other.hitPosition;
      this.x = other.x;
      this.y = other.y;
      this.z = other.z;
      this.changeType = other.changeType;
   }

   @Nonnull
   public static ApplyKnockback deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 38) {
         throw ProtocolException.bufferTooSmall("ApplyKnockback", 38, buf.readableBytes() - offset);
      } else {
         ApplyKnockback obj = new ApplyKnockback();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            obj.hitPosition = Position.deserialize(buf, offset + 1);
         }

         obj.x = buf.getFloatLE(offset + 25);
         obj.y = buf.getFloatLE(offset + 29);
         obj.z = buf.getFloatLE(offset + 33);
         obj.changeType = ChangeVelocityType.fromValue(buf.getByte(offset + 37));
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 38;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 38L;
   }

   @Nullable
   public static Position getHitPosition(MemorySegment mem) {
      return getHitPosition(mem, 0);
   }

   @Nullable
   public static Position getHitPosition(MemorySegment mem, int offset) {
      return hasHitPosition(mem, offset) ? Position.toObject(mem, offset + 1) : null;
   }

   public static float getX(MemorySegment mem) {
      return getX(mem, 0);
   }

   public static float getX(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 25));
   }

   public static float getY(MemorySegment mem) {
      return getY(mem, 0);
   }

   public static float getY(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 29));
   }

   public static float getZ(MemorySegment mem) {
      return getZ(mem, 0);
   }

   public static float getZ(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 33));
   }

   public static ChangeVelocityType getChangeType(MemorySegment mem) {
      return getChangeType(mem, 0);
   }

   public static ChangeVelocityType getChangeType(MemorySegment mem, int offset) {
      return ChangeVelocityType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 37)));
   }

   public static boolean hasHitPosition(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static ApplyKnockback toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ApplyKnockback toObject(MemorySegment mem, int offset) {
      if (offset + 38 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ApplyKnockback", offset + 38, (int)mem.byteSize());
      } else {
         return new ApplyKnockback(
            hasHitPosition(mem, offset) ? Position.toObject(mem, offset + 1) : null,
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 25)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 29)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 33)),
            ChangeVelocityType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 37)))
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.hitPosition != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.hitPosition != null) {
         this.hitPosition.serialize(buf);
      } else {
         buf.writeZero(24);
      }

      buf.writeFloatLE(this.x);
      buf.writeFloatLE(this.y);
      buf.writeFloatLE(this.z);
      buf.writeByte(this.changeType.getValue());
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.hitPosition != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      if (this.hitPosition != null) {
         this.hitPosition.serialize(mem, offset + 1);
      } else {
         mem.asSlice(offset + 1, 24L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 25), this.x);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 29), this.y);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 33), this.z);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 37), (byte)this.changeType.getValue());
      return 38;
   }

   @Override
   public int computeSize() {
      return 38;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 38) {
         return ValidationResult.error("Buffer too small: expected at least 38 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int v = buffer.getByte(offset + 37) & 255;
         return v >= 2 ? ValidationResult.error("Invalid ChangeVelocityType value for ChangeType") : ValidationResult.OK;
      }
   }

   public ApplyKnockback clone() {
      ApplyKnockback copy = new ApplyKnockback();
      copy.hitPosition = this.hitPosition != null ? this.hitPosition.clone() : null;
      copy.x = this.x;
      copy.y = this.y;
      copy.z = this.z;
      copy.changeType = this.changeType;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ApplyKnockback other)
            ? false
            : Objects.equals(this.hitPosition, other.hitPosition)
               && this.x == other.x
               && this.y == other.y
               && this.z == other.z
               && Objects.equals(this.changeType, other.changeType);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.hitPosition, this.x, this.y, this.z, this.changeType);
   }
}
