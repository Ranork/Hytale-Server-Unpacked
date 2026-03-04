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
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SpawnParticleSystem implements Packet, ToClientPacket {
   public static final int PACKET_ID = 152;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 44;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 44;
   public static final int MAX_SIZE = 16384049;
   @Nullable
   public String particleSystemId;
   @Nullable
   public Position position;
   @Nullable
   public Direction rotation;
   public float scale;
   @Nullable
   public Color color;

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

   public SpawnParticleSystem(@Nullable String particleSystemId, @Nullable Position position, @Nullable Direction rotation, float scale, @Nullable Color color) {
      this.particleSystemId = particleSystemId;
      this.position = position;
      this.rotation = rotation;
      this.scale = scale;
      this.color = color;
   }

   public SpawnParticleSystem(@Nonnull SpawnParticleSystem other) {
      this.particleSystemId = other.particleSystemId;
      this.position = other.position;
      this.rotation = other.rotation;
      this.scale = other.scale;
      this.color = other.color;
   }

   @Nonnull
   public static SpawnParticleSystem deserialize(@Nonnull ByteBuf buf, int offset) {
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

      int pos = offset + 44;
      if ((nullBits & 8) != 0) {
         int particleSystemIdLen = VarInt.peek(buf, pos);
         if (particleSystemIdLen < 0) {
            throw ProtocolException.negativeLength("ParticleSystemId", particleSystemIdLen);
         }

         if (particleSystemIdLen > 4096000) {
            throw ProtocolException.stringTooLong("ParticleSystemId", particleSystemIdLen, 4096000);
         }

         int particleSystemIdVarLen = VarInt.length(buf, pos);
         obj.particleSystemId = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
         pos += particleSystemIdVarLen + particleSystemIdLen;
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 44;
      if ((nullBits & 8) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos) + sl;
      }

      return pos - offset;
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

      if (this.particleSystemId != null) {
         PacketIO.writeVarString(buf, this.particleSystemId, 4096000);
      }
   }

   @Override
   public int computeSize() {
      int size = 44;
      if (this.particleSystemId != null) {
         size += PacketIO.stringSize(this.particleSystemId);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 44) {
         return ValidationResult.error("Buffer too small: expected at least 44 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 44;
         if ((nullBits & 8) != 0) {
            int particleSystemIdLen = VarInt.peek(buffer, pos);
            if (particleSystemIdLen < 0) {
               return ValidationResult.error("Invalid string length for ParticleSystemId");
            }

            if (particleSystemIdLen > 4096000) {
               return ValidationResult.error("ParticleSystemId exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
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
               && Objects.equals(this.color, other.color);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.particleSystemId, this.position, this.rotation, this.scale, this.color);
   }
}
