package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WorldParticle {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 32;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 32;
   public static final int MAX_SIZE = 16384037;
   @Nullable
   public String systemId;
   public float scale;
   @Nullable
   public Color color;
   @Nullable
   public Vector3f positionOffset;
   @Nullable
   public Direction rotationOffset;

   public WorldParticle() {
   }

   public WorldParticle(@Nullable String systemId, float scale, @Nullable Color color, @Nullable Vector3f positionOffset, @Nullable Direction rotationOffset) {
      this.systemId = systemId;
      this.scale = scale;
      this.color = color;
      this.positionOffset = positionOffset;
      this.rotationOffset = rotationOffset;
   }

   public WorldParticle(@Nonnull WorldParticle other) {
      this.systemId = other.systemId;
      this.scale = other.scale;
      this.color = other.color;
      this.positionOffset = other.positionOffset;
      this.rotationOffset = other.rotationOffset;
   }

   @Nonnull
   public static WorldParticle deserialize(@Nonnull ByteBuf buf, int offset) {
      WorldParticle obj = new WorldParticle();
      byte nullBits = buf.getByte(offset);
      obj.scale = buf.getFloatLE(offset + 1);
      if ((nullBits & 1) != 0) {
         obj.color = Color.deserialize(buf, offset + 5);
      }

      if ((nullBits & 2) != 0) {
         obj.positionOffset = Vector3f.deserialize(buf, offset + 8);
      }

      if ((nullBits & 4) != 0) {
         obj.rotationOffset = Direction.deserialize(buf, offset + 20);
      }

      int pos = offset + 32;
      if ((nullBits & 8) != 0) {
         int systemIdLen = VarInt.peek(buf, pos);
         if (systemIdLen < 0) {
            throw ProtocolException.negativeLength("SystemId", systemIdLen);
         }

         if (systemIdLen > 4096000) {
            throw ProtocolException.stringTooLong("SystemId", systemIdLen, 4096000);
         }

         int systemIdVarLen = VarInt.length(buf, pos);
         obj.systemId = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
         pos += systemIdVarLen + systemIdLen;
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 32;
      if ((nullBits & 8) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos) + sl;
      }

      return pos - offset;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.color != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.positionOffset != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.rotationOffset != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.systemId != null) {
         nullBits = (byte)(nullBits | 8);
      }

      buf.writeByte(nullBits);
      buf.writeFloatLE(this.scale);
      if (this.color != null) {
         this.color.serialize(buf);
      } else {
         buf.writeZero(3);
      }

      if (this.positionOffset != null) {
         this.positionOffset.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      if (this.rotationOffset != null) {
         this.rotationOffset.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      if (this.systemId != null) {
         PacketIO.writeVarString(buf, this.systemId, 4096000);
      }
   }

   public int computeSize() {
      int size = 32;
      if (this.systemId != null) {
         size += PacketIO.stringSize(this.systemId);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 32) {
         return ValidationResult.error("Buffer too small: expected at least 32 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 32;
         if ((nullBits & 8) != 0) {
            int systemIdLen = VarInt.peek(buffer, pos);
            if (systemIdLen < 0) {
               return ValidationResult.error("Invalid string length for SystemId");
            }

            if (systemIdLen > 4096000) {
               return ValidationResult.error("SystemId exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += systemIdLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading SystemId");
            }
         }

         return ValidationResult.OK;
      }
   }

   public WorldParticle clone() {
      WorldParticle copy = new WorldParticle();
      copy.systemId = this.systemId;
      copy.scale = this.scale;
      copy.color = this.color != null ? this.color.clone() : null;
      copy.positionOffset = this.positionOffset != null ? this.positionOffset.clone() : null;
      copy.rotationOffset = this.rotationOffset != null ? this.rotationOffset.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof WorldParticle other)
            ? false
            : Objects.equals(this.systemId, other.systemId)
               && this.scale == other.scale
               && Objects.equals(this.color, other.color)
               && Objects.equals(this.positionOffset, other.positionOffset)
               && Objects.equals(this.rotationOffset, other.rotationOffset);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.systemId, this.scale, this.color, this.positionOffset, this.rotationOffset);
   }
}
