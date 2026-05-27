package com.hypixel.hytale.protocol.packets.world;

import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.Position;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SpawnParticleSystem implements Packet, ToClientPacket {
   public static final int PACKET_ID = 152;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 48;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 48;
   public static final int MAX_SIZE = 16384053;
   @Nullable
   public String particleSystemId;
   @Nullable
   public Position position;
   @Nullable
   public Direction rotation;
   public float scale;
   @Nullable
   public Color color;
   public float maxDuration;

   @Override
   public int getId() {
      return 152;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public SpawnParticleSystem() {
   }

   public SpawnParticleSystem(
      @Nullable String particleSystemId, @Nullable Position position, @Nullable Direction rotation, float scale, @Nullable Color color, float maxDuration
   ) {
      this.particleSystemId = particleSystemId;
      this.position = position;
      this.rotation = rotation;
      this.scale = scale;
      this.color = color;
      this.maxDuration = maxDuration;
   }

   public SpawnParticleSystem(@Nonnull SpawnParticleSystem other) {
      this.particleSystemId = other.particleSystemId;
      this.position = other.position;
      this.rotation = other.rotation;
      this.scale = other.scale;
      this.color = other.color;
      this.maxDuration = other.maxDuration;
   }

   @Nonnull
   public static SpawnParticleSystem deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 48) {
         throw ProtocolException.bufferTooSmall("SpawnParticleSystem", 48, buf.readableBytes() - offset);
      } else {
         SpawnParticleSystem obj = new SpawnParticleSystem();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            obj.position = Position.deserialize(buf, offset + 1);
         }

         if ((nullBits & 2) != 0) {
            obj.rotation = Direction.deserialize(buf, offset + 25);
         }

         obj.scale = buf.getFloatLE(offset + 37);
         if ((nullBits & 4) != 0) {
            obj.color = Color.deserialize(buf, offset + 41);
         }

         obj.maxDuration = buf.getFloatLE(offset + 44);
         int pos = offset + 48;
         if ((nullBits & 8) != 0) {
            int particleSystemIdLen = VarInt.peek(buf, pos);
            if (particleSystemIdLen < 0) {
               throw ProtocolException.invalidVarInt("ParticleSystemId");
            }

            int particleSystemIdVarLen = VarInt.size(particleSystemIdLen);
            if (particleSystemIdLen > 4096000) {
               throw ProtocolException.stringTooLong("ParticleSystemId", particleSystemIdLen, 4096000);
            }

            if (pos + particleSystemIdVarLen + particleSystemIdLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("ParticleSystemId", pos + particleSystemIdVarLen + particleSystemIdLen, buf.readableBytes());
            }

            obj.particleSystemId = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
            pos += particleSystemIdVarLen + particleSystemIdLen;
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 48;
      if ((nullBits & 8) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.size(sl) + sl;
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 48L;
   }

   @Nullable
   public static String getParticleSystemId(MemorySegment mem) {
      return getParticleSystemId(mem, 0);
   }

   @Nullable
   public static String getParticleSystemId(MemorySegment mem, int offset) {
      return hasParticleSystemId(mem, offset) ? PacketIO.readVarString("ParticleSystemId", mem, offset + 48, 4096000, PacketIO.UTF8) : null;
   }

   @Nullable
   public static Position getPosition(MemorySegment mem) {
      return getPosition(mem, 0);
   }

   @Nullable
   public static Position getPosition(MemorySegment mem, int offset) {
      return hasPosition(mem, offset) ? Position.toObject(mem, offset + 1) : null;
   }

   @Nullable
   public static Direction getRotation(MemorySegment mem) {
      return getRotation(mem, 0);
   }

   @Nullable
   public static Direction getRotation(MemorySegment mem, int offset) {
      return hasRotation(mem, offset) ? Direction.toObject(mem, offset + 25) : null;
   }

   public static float getScale(MemorySegment mem) {
      return getScale(mem, 0);
   }

   public static float getScale(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 37));
   }

   @Nullable
   public static Color getColor(MemorySegment mem) {
      return getColor(mem, 0);
   }

   @Nullable
   public static Color getColor(MemorySegment mem, int offset) {
      return hasColor(mem, offset) ? Color.toObject(mem, offset + 41) : null;
   }

   public static float getMaxDuration(MemorySegment mem) {
      return getMaxDuration(mem, 0);
   }

   public static float getMaxDuration(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 44));
   }

   public static boolean hasPosition(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasRotation(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasColor(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasParticleSystemId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   public static SpawnParticleSystem toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static SpawnParticleSystem toObject(MemorySegment mem, int offset) {
      if (offset + 48 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("SpawnParticleSystem", offset + 48, (int)mem.byteSize());
      } else {
         return new SpawnParticleSystem(
            hasParticleSystemId(mem, offset) ? PacketIO.readVarString("ParticleSystemId", mem, offset + 48, 4096000, PacketIO.UTF8) : null,
            hasPosition(mem, offset) ? Position.toObject(mem, offset + 1) : null,
            hasRotation(mem, offset) ? Direction.toObject(mem, offset + 25) : null,
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 37)),
            hasColor(mem, offset) ? Color.toObject(mem, offset + 41) : null,
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 44))
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.position != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.rotation != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.color != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.particleSystemId != null) {
         nullBits = (byte)(nullBits | 8);
      }

      buf.writeByte(nullBits);
      if (this.position != null) {
         this.position.serialize(buf);
      } else {
         buf.writeZero(24);
      }

      if (this.rotation != null) {
         this.rotation.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      buf.writeFloatLE(this.scale);
      if (this.color != null) {
         this.color.serialize(buf);
      } else {
         buf.writeZero(3);
      }

      buf.writeFloatLE(this.maxDuration);
      if (this.particleSystemId != null) {
         PacketIO.writeVarString(buf, this.particleSystemId, 4096000);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.position != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.rotation != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.color != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.particleSystemId != null) {
         nullBits = (byte)(nullBits | 8);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      if (this.position != null) {
         this.position.serialize(mem, offset + 1);
      } else {
         mem.asSlice(offset + 1, 24L).fill((byte)0);
      }

      if (this.rotation != null) {
         this.rotation.serialize(mem, offset + 25);
      } else {
         mem.asSlice(offset + 25, 12L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 37), this.scale);
      if (this.color != null) {
         this.color.serialize(mem, offset + 41);
      } else {
         mem.asSlice(offset + 41, 3L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 44), this.maxDuration);
      int varOffset = offset + 48;
      if (this.particleSystemId != null) {
         varOffset += PacketIO.writeVarString(mem, varOffset, this.particleSystemId, 4096000);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 48;
      if (this.particleSystemId != null) {
         size += PacketIO.stringSize(this.particleSystemId);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 48) {
         return ValidationResult.error("Buffer too small: expected at least 48 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 48;
         if ((nullBits & 8) != 0) {
            int particleSystemIdLen = VarInt.peek(buffer, pos);
            if (particleSystemIdLen < 0) {
               return ValidationResult.error("Invalid string length for ParticleSystemId");
            }

            if (particleSystemIdLen > 4096000) {
               return ValidationResult.error("ParticleSystemId exceeds max length 4096000");
            }

            pos += VarInt.size(particleSystemIdLen);
            pos += particleSystemIdLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading ParticleSystemId");
            }
         }

         return ValidationResult.OK;
      }
   }

   public SpawnParticleSystem clone() {
      SpawnParticleSystem copy = new SpawnParticleSystem();
      copy.particleSystemId = this.particleSystemId;
      copy.position = this.position != null ? this.position.clone() : null;
      copy.rotation = this.rotation != null ? this.rotation.clone() : null;
      copy.scale = this.scale;
      copy.color = this.color != null ? this.color.clone() : null;
      copy.maxDuration = this.maxDuration;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof SpawnParticleSystem other)
            ? false
            : Objects.equals(this.particleSystemId, other.particleSystemId)
               && Objects.equals(this.position, other.position)
               && Objects.equals(this.rotation, other.rotation)
               && this.scale == other.scale
               && Objects.equals(this.color, other.color)
               && this.maxDuration == other.maxDuration;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.particleSystemId, this.position, this.rotation, this.scale, this.color, this.maxDuration);
   }
}
