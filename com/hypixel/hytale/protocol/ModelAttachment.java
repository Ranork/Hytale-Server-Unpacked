package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ModelAttachment {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 4;
   public static final int VARIABLE_BLOCK_START = 17;
   public static final int MAX_SIZE = 65536037;
   @Nullable
   public String model;
   @Nullable
   public String texture;
   @Nullable
   public String gradientSet;
   @Nullable
   public String gradientId;

   public ModelAttachment() {
   }

   public ModelAttachment(@Nullable String model, @Nullable String texture, @Nullable String gradientSet, @Nullable String gradientId) {
      this.model = model;
      this.texture = texture;
      this.gradientSet = gradientSet;
      this.gradientId = gradientId;
   }

   public ModelAttachment(@Nonnull ModelAttachment other) {
      this.model = other.model;
      this.texture = other.texture;
      this.gradientSet = other.gradientSet;
      this.gradientId = other.gradientId;
   }

   @Nonnull
   public static ModelAttachment deserialize(@Nonnull ByteBuf buf, int offset) {
      ModelAttachment obj = new ModelAttachment();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 17 + buf.getIntLE(offset + 1);
         int modelLen = VarInt.peek(buf, varPos0);
         if (modelLen < 0) {
            throw ProtocolException.negativeLength("Model", modelLen);
         }

         if (modelLen > 4096000) {
            throw ProtocolException.stringTooLong("Model", modelLen, 4096000);
         }

         obj.model = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 17 + buf.getIntLE(offset + 5);
         int textureLen = VarInt.peek(buf, varPos1);
         if (textureLen < 0) {
            throw ProtocolException.negativeLength("Texture", textureLen);
         }

         if (textureLen > 4096000) {
            throw ProtocolException.stringTooLong("Texture", textureLen, 4096000);
         }

         obj.texture = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
      }

      if ((nullBits & 4) != 0) {
         int varPos2 = offset + 17 + buf.getIntLE(offset + 9);
         int gradientSetLen = VarInt.peek(buf, varPos2);
         if (gradientSetLen < 0) {
            throw ProtocolException.negativeLength("GradientSet", gradientSetLen);
         }

         if (gradientSetLen > 4096000) {
            throw ProtocolException.stringTooLong("GradientSet", gradientSetLen, 4096000);
         }

         obj.gradientSet = PacketIO.readVarString(buf, varPos2, PacketIO.UTF8);
      }

      if ((nullBits & 8) != 0) {
         int varPos3 = offset + 17 + buf.getIntLE(offset + 13);
         int gradientIdLen = VarInt.peek(buf, varPos3);
         if (gradientIdLen < 0) {
            throw ProtocolException.negativeLength("GradientId", gradientIdLen);
         }

         if (gradientIdLen > 4096000) {
            throw ProtocolException.stringTooLong("GradientId", gradientIdLen, 4096000);
         }

         obj.gradientId = PacketIO.readVarString(buf, varPos3, PacketIO.UTF8);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 17;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         int pos0 = offset + 17 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         int pos1 = offset + 17 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 9);
         int pos2 = offset + 17 + fieldOffset2;
         int sl = VarInt.peek(buf, pos2);
         pos2 += VarInt.length(buf, pos2) + sl;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 13);
         int pos3 = offset + 17 + fieldOffset3;
         int sl = VarInt.peek(buf, pos3);
         pos3 += VarInt.length(buf, pos3) + sl;
         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      return maxEnd;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.model != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.texture != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.gradientSet != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.gradientId != null) {
         nullBits = (byte)(nullBits | 8);
      }

      buf.writeByte(nullBits);
      int modelOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int textureOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int gradientSetOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int gradientIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.model != null) {
         buf.setIntLE(modelOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.model, 4096000);
      } else {
         buf.setIntLE(modelOffsetSlot, -1);
      }

      if (this.texture != null) {
         buf.setIntLE(textureOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.texture, 4096000);
      } else {
         buf.setIntLE(textureOffsetSlot, -1);
      }

      if (this.gradientSet != null) {
         buf.setIntLE(gradientSetOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.gradientSet, 4096000);
      } else {
         buf.setIntLE(gradientSetOffsetSlot, -1);
      }

      if (this.gradientId != null) {
         buf.setIntLE(gradientIdOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.gradientId, 4096000);
      } else {
         buf.setIntLE(gradientIdOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 17;
      if (this.model != null) {
         size += PacketIO.stringSize(this.model);
      }

      if (this.texture != null) {
         size += PacketIO.stringSize(this.texture);
      }

      if (this.gradientSet != null) {
         size += PacketIO.stringSize(this.gradientSet);
      }

      if (this.gradientId != null) {
         size += PacketIO.stringSize(this.gradientId);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 17) {
         return ValidationResult.error("Buffer too small: expected at least 17 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int modelOffset = buffer.getIntLE(offset + 1);
            if (modelOffset < 0) {
               return ValidationResult.error("Invalid offset for Model");
            }

            int pos = offset + 17 + modelOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Model");
            }

            int modelLen = VarInt.peek(buffer, pos);
            if (modelLen < 0) {
               return ValidationResult.error("Invalid string length for Model");
            }

            if (modelLen > 4096000) {
               return ValidationResult.error("Model exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += modelLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Model");
            }
         }

         if ((nullBits & 2) != 0) {
            int textureOffset = buffer.getIntLE(offset + 5);
            if (textureOffset < 0) {
               return ValidationResult.error("Invalid offset for Texture");
            }

            int posx = offset + 17 + textureOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Texture");
            }

            int textureLen = VarInt.peek(buffer, posx);
            if (textureLen < 0) {
               return ValidationResult.error("Invalid string length for Texture");
            }

            if (textureLen > 4096000) {
               return ValidationResult.error("Texture exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
            posx += textureLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Texture");
            }
         }

         if ((nullBits & 4) != 0) {
            int gradientSetOffset = buffer.getIntLE(offset + 9);
            if (gradientSetOffset < 0) {
               return ValidationResult.error("Invalid offset for GradientSet");
            }

            int posxx = offset + 17 + gradientSetOffset;
            if (posxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for GradientSet");
            }

            int gradientSetLen = VarInt.peek(buffer, posxx);
            if (gradientSetLen < 0) {
               return ValidationResult.error("Invalid string length for GradientSet");
            }

            if (gradientSetLen > 4096000) {
               return ValidationResult.error("GradientSet exceeds max length 4096000");
            }

            posxx += VarInt.length(buffer, posxx);
            posxx += gradientSetLen;
            if (posxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading GradientSet");
            }
         }

         if ((nullBits & 8) != 0) {
            int gradientIdOffset = buffer.getIntLE(offset + 13);
            if (gradientIdOffset < 0) {
               return ValidationResult.error("Invalid offset for GradientId");
            }

            int posxxx = offset + 17 + gradientIdOffset;
            if (posxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for GradientId");
            }

            int gradientIdLen = VarInt.peek(buffer, posxxx);
            if (gradientIdLen < 0) {
               return ValidationResult.error("Invalid string length for GradientId");
            }

            if (gradientIdLen > 4096000) {
               return ValidationResult.error("GradientId exceeds max length 4096000");
            }

            posxxx += VarInt.length(buffer, posxxx);
            posxxx += gradientIdLen;
            if (posxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading GradientId");
            }
         }

         return ValidationResult.OK;
      }
   }

   public ModelAttachment clone() {
      ModelAttachment copy = new ModelAttachment();
      copy.model = this.model;
      copy.texture = this.texture;
      copy.gradientSet = this.gradientSet;
      copy.gradientId = this.gradientId;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ModelAttachment other)
            ? false
            : Objects.equals(this.model, other.model)
               && Objects.equals(this.texture, other.texture)
               && Objects.equals(this.gradientSet, other.gradientSet)
               && Objects.equals(this.gradientId, other.gradientId);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.model, this.texture, this.gradientSet, this.gradientId);
   }
}
