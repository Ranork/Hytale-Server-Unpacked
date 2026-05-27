package com.hypixel.hytale.protocol.packets.entities;

import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.VelocityConfig;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ChangeVelocity implements Packet, ToClientPacket {
   public static final int PACKET_ID = 163;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 35;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 35;
   public static final int MAX_SIZE = 35;
   public float x;
   public float y;
   public float z;
   @Nonnull
   public ChangeVelocityType changeType = ChangeVelocityType.Add;
   @Nullable
   public VelocityConfig config;

   @Override
   public int getId() {
      return 163;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public ChangeVelocity() {
   }

   public ChangeVelocity(float x, float y, float z, @Nonnull ChangeVelocityType changeType, @Nullable VelocityConfig config) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.changeType = changeType;
      this.config = config;
   }

   public ChangeVelocity(@Nonnull ChangeVelocity other) {
      this.x = other.x;
      this.y = other.y;
      this.z = other.z;
      this.changeType = other.changeType;
      this.config = other.config;
   }

   @Nonnull
   public static ChangeVelocity deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 35) {
         throw ProtocolException.bufferTooSmall("ChangeVelocity", 35, buf.readableBytes() - offset);
      } else {
         ChangeVelocity obj = new ChangeVelocity();
         byte nullBits = buf.getByte(offset);
         obj.x = buf.getFloatLE(offset + 1);
         obj.y = buf.getFloatLE(offset + 5);
         obj.z = buf.getFloatLE(offset + 9);
         obj.changeType = ChangeVelocityType.fromValue(buf.getByte(offset + 13));
         if ((nullBits & 1) != 0) {
            obj.config = VelocityConfig.deserialize(buf, offset + 14);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 35;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 35L;
   }

   public static float getX(MemorySegment mem) {
      return getX(mem, 0);
   }

   public static float getX(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1));
   }

   public static float getY(MemorySegment mem) {
      return getY(mem, 0);
   }

   public static float getY(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 5));
   }

   public static float getZ(MemorySegment mem) {
      return getZ(mem, 0);
   }

   public static float getZ(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 9));
   }

   public static ChangeVelocityType getChangeType(MemorySegment mem) {
      return getChangeType(mem, 0);
   }

   public static ChangeVelocityType getChangeType(MemorySegment mem, int offset) {
      return ChangeVelocityType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 13)));
   }

   @Nullable
   public static VelocityConfig getConfig(MemorySegment mem) {
      return getConfig(mem, 0);
   }

   @Nullable
   public static VelocityConfig getConfig(MemorySegment mem, int offset) {
      return hasConfig(mem, offset) ? VelocityConfig.toObject(mem, offset + 14) : null;
   }

   public static boolean hasConfig(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static ChangeVelocity toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ChangeVelocity toObject(MemorySegment mem, int offset) {
      if (offset + 35 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ChangeVelocity", offset + 35, (int)mem.byteSize());
      } else {
         return new ChangeVelocity(
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 5)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 9)),
            ChangeVelocityType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 13))),
            hasConfig(mem, offset) ? VelocityConfig.toObject(mem, offset + 14) : null
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.config != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeFloatLE(this.x);
      buf.writeFloatLE(this.y);
      buf.writeFloatLE(this.z);
      buf.writeByte(this.changeType.getValue());
      if (this.config != null) {
         this.config.serialize(buf);
      } else {
         buf.writeZero(21);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.config != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 1), this.x);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 5), this.y);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 9), this.z);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 13), (byte)this.changeType.getValue());
      if (this.config != null) {
         this.config.serialize(mem, offset + 14);
      } else {
         mem.asSlice(offset + 14, 21L).fill((byte)0);
      }

      return 35;
   }

   @Override
   public int computeSize() {
      return 35;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 35) {
         return ValidationResult.error("Buffer too small: expected at least 35 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int v = buffer.getByte(offset + 13) & 255;
         return v >= 2 ? ValidationResult.error("Invalid ChangeVelocityType value for ChangeType") : ValidationResult.OK;
      }
   }

   public ChangeVelocity clone() {
      ChangeVelocity copy = new ChangeVelocity();
      copy.x = this.x;
      copy.y = this.y;
      copy.z = this.z;
      copy.changeType = this.changeType;
      copy.config = this.config != null ? this.config.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ChangeVelocity other)
            ? false
            : this.x == other.x
               && this.y == other.y
               && this.z == other.z
               && Objects.equals(this.changeType, other.changeType)
               && Objects.equals(this.config, other.config);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.x, this.y, this.z, this.changeType, this.config);
   }
}
