package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UVMotion {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 19;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 19;
   public static final int MAX_SIZE = 16384024;
   @Nullable
   public String texture;
   public boolean addRandomUVOffset;
   public float speedX;
   public float speedY;
   public float scale;
   public float strength;
   @Nonnull
   public UVMotionCurveType strengthCurveType = UVMotionCurveType.Constant;

   public UVMotion() {
   }

   public UVMotion(
      @Nullable String texture,
      boolean addRandomUVOffset,
      float speedX,
      float speedY,
      float scale,
      float strength,
      @Nonnull UVMotionCurveType strengthCurveType
   ) {
      this.texture = texture;
      this.addRandomUVOffset = addRandomUVOffset;
      this.speedX = speedX;
      this.speedY = speedY;
      this.scale = scale;
      this.strength = strength;
      this.strengthCurveType = strengthCurveType;
   }

   public UVMotion(@Nonnull UVMotion other) {
      this.texture = other.texture;
      this.addRandomUVOffset = other.addRandomUVOffset;
      this.speedX = other.speedX;
      this.speedY = other.speedY;
      this.scale = other.scale;
      this.strength = other.strength;
      this.strengthCurveType = other.strengthCurveType;
   }

   @Nonnull
   public static UVMotion deserialize(@Nonnull ByteBuf buf, int offset) {
      UVMotion obj = new UVMotion();
      byte nullBits = buf.getByte(offset);
      obj.addRandomUVOffset = buf.getByte(offset + 1) != 0;
      obj.speedX = buf.getFloatLE(offset + 2);
      obj.speedY = buf.getFloatLE(offset + 6);
      obj.scale = buf.getFloatLE(offset + 10);
      obj.strength = buf.getFloatLE(offset + 14);
      obj.strengthCurveType = UVMotionCurveType.fromValue(buf.getByte(offset + 18));
      int pos = offset + 19;
      if ((nullBits & 1) != 0) {
         int textureLen = VarInt.peek(buf, pos);
         if (textureLen < 0) {
            throw ProtocolException.negativeLength("Texture", textureLen);
         }

         if (textureLen > 4096000) {
            throw ProtocolException.stringTooLong("Texture", textureLen, 4096000);
         }

         int textureVarLen = VarInt.length(buf, pos);
         obj.texture = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
         pos += textureVarLen + textureLen;
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 19;
      if ((nullBits & 1) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos) + sl;
      }

      return pos - offset;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.texture != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.addRandomUVOffset ? 1 : 0);
      buf.writeFloatLE(this.speedX);
      buf.writeFloatLE(this.speedY);
      buf.writeFloatLE(this.scale);
      buf.writeFloatLE(this.strength);
      buf.writeByte(this.strengthCurveType.getValue());
      if (this.texture != null) {
         PacketIO.writeVarString(buf, this.texture, 4096000);
      }
   }

   public int computeSize() {
      int size = 19;
      if (this.texture != null) {
         size += PacketIO.stringSize(this.texture);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 19) {
         return ValidationResult.error("Buffer too small: expected at least 19 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 19;
         if ((nullBits & 1) != 0) {
            int textureLen = VarInt.peek(buffer, pos);
            if (textureLen < 0) {
               return ValidationResult.error("Invalid string length for Texture");
            }

            if (textureLen > 4096000) {
               return ValidationResult.error("Texture exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += textureLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Texture");
            }
         }

         return ValidationResult.OK;
      }
   }

   public UVMotion clone() {
      UVMotion copy = new UVMotion();
      copy.texture = this.texture;
      copy.addRandomUVOffset = this.addRandomUVOffset;
      copy.speedX = this.speedX;
      copy.speedY = this.speedY;
      copy.scale = this.scale;
      copy.strength = this.strength;
      copy.strengthCurveType = this.strengthCurveType;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UVMotion other)
            ? false
            : Objects.equals(this.texture, other.texture)
               && this.addRandomUVOffset == other.addRandomUVOffset
               && this.speedX == other.speedX
               && this.speedY == other.speedY
               && this.scale == other.scale
               && this.strength == other.strength
               && Objects.equals(this.strengthCurveType, other.strengthCurveType);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.texture, this.addRandomUVOffset, this.speedX, this.speedY, this.scale, this.strength, this.strengthCurveType);
   }
}
