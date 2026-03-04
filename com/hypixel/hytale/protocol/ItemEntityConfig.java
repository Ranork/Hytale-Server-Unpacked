package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemEntityConfig {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 5;
   public static final int MAX_SIZE = 16384010;
   @Nullable
   public String particleSystemId;
   @Nullable
   public Color particleColor;
   public boolean showItemParticles;

   public ItemEntityConfig() {
   }

   public ItemEntityConfig(@Nullable String particleSystemId, @Nullable Color particleColor, boolean showItemParticles) {
      this.particleSystemId = particleSystemId;
      this.particleColor = particleColor;
      this.showItemParticles = showItemParticles;
   }

   public ItemEntityConfig(@Nonnull ItemEntityConfig other) {
      this.particleSystemId = other.particleSystemId;
      this.particleColor = other.particleColor;
      this.showItemParticles = other.showItemParticles;
   }

   @Nonnull
   public static ItemEntityConfig deserialize(@Nonnull ByteBuf buf, int offset) {
      ItemEntityConfig obj = new ItemEntityConfig();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         obj.particleColor = Color.deserialize(buf, offset + 1);
      }

      obj.showItemParticles = buf.getByte(offset + 4) != 0;
      int pos = offset + 5;
      if ((nullBits & 2) != 0) {
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
      int pos = offset + 5;
      if ((nullBits & 2) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos) + sl;
      }

      return pos - offset;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.particleColor != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.particleSystemId != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      if (this.particleColor != null) {
         this.particleColor.serialize(buf);
      } else {
         buf.writeZero(3);
      }

      buf.writeByte(this.showItemParticles ? 1 : 0);
      if (this.particleSystemId != null) {
         PacketIO.writeVarString(buf, this.particleSystemId, 4096000);
      }
   }

   public int computeSize() {
      int size = 5;
      if (this.particleSystemId != null) {
         size += PacketIO.stringSize(this.particleSystemId);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 5) {
         return ValidationResult.error("Buffer too small: expected at least 5 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 5;
         if ((nullBits & 2) != 0) {
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

   public ItemEntityConfig clone() {
      ItemEntityConfig copy = new ItemEntityConfig();
      copy.particleSystemId = this.particleSystemId;
      copy.particleColor = this.particleColor != null ? this.particleColor.clone() : null;
      copy.showItemParticles = this.showItemParticles;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ItemEntityConfig other)
            ? false
            : Objects.equals(this.particleSystemId, other.particleSystemId)
               && Objects.equals(this.particleColor, other.particleColor)
               && this.showItemParticles == other.showItemParticles;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.particleSystemId, this.particleColor, this.showItemParticles);
   }
}
