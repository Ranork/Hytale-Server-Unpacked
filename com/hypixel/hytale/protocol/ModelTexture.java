package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ModelTexture {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 5;
   public static final int MAX_SIZE = 16384010;
   @Nullable
   public String texture;
   public float weight;

   public ModelTexture() {
   }

   public ModelTexture(@Nullable String texture, float weight) {
      this.texture = texture;
      this.weight = weight;
   }

   public ModelTexture(@Nonnull ModelTexture other) {
      this.texture = other.texture;
      this.weight = other.weight;
   }

   @Nonnull
   public static ModelTexture deserialize(@Nonnull ByteBuf buf, int offset) {
      ModelTexture obj = new ModelTexture();
      byte nullBits = buf.getByte(offset);
      obj.weight = buf.getFloatLE(offset + 1);
      int pos = offset + 5;
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
      int pos = offset + 5;
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
      buf.writeFloatLE(this.weight);
      if (this.texture != null) {
         PacketIO.writeVarString(buf, this.texture, 4096000);
      }
   }

   public int computeSize() {
      int size = 5;
      if (this.texture != null) {
         size += PacketIO.stringSize(this.texture);
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

   public ModelTexture clone() {
      ModelTexture copy = new ModelTexture();
      copy.texture = this.texture;
      copy.weight = this.weight;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ModelTexture other) ? false : Objects.equals(this.texture, other.texture) && this.weight == other.weight;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.texture, this.weight);
   }
}
