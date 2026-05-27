package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
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
      if (buf.readableBytes() - offset < 17) {
         throw ProtocolException.bufferTooSmall("ModelAttachment", 17, buf.readableBytes() - offset);
      } else {
         ModelAttachment obj = new ModelAttachment();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 1);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 17) {
               throw ProtocolException.invalidOffset("Model", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 17 + varPosBase0;
            int modelLen = VarInt.peek(buf, varPos0);
            if (modelLen < 0) {
               throw ProtocolException.invalidVarInt("Model");
            }

            int modelVarIntLen = VarInt.size(modelLen);
            if (modelLen > 4096000) {
               throw ProtocolException.stringTooLong("Model", modelLen, 4096000);
            }

            if (varPos0 + modelVarIntLen + modelLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Model", varPos0 + modelVarIntLen + modelLen, buf.readableBytes());
            }

            obj.model = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 5);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 17) {
               throw ProtocolException.invalidOffset("Texture", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 17 + varPosBase1;
            int textureLen = VarInt.peek(buf, varPos1);
            if (textureLen < 0) {
               throw ProtocolException.invalidVarInt("Texture");
            }

            int textureVarIntLen = VarInt.size(textureLen);
            if (textureLen > 4096000) {
               throw ProtocolException.stringTooLong("Texture", textureLen, 4096000);
            }

            if (varPos1 + textureVarIntLen + textureLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Texture", varPos1 + textureVarIntLen + textureLen, buf.readableBytes());
            }

            obj.texture = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
         }

         if ((nullBits & 4) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 9);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 17) {
               throw ProtocolException.invalidOffset("GradientSet", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 17 + varPosBase2;
            int gradientSetLen = VarInt.peek(buf, varPos2);
            if (gradientSetLen < 0) {
               throw ProtocolException.invalidVarInt("GradientSet");
            }

            int gradientSetVarIntLen = VarInt.size(gradientSetLen);
            if (gradientSetLen > 4096000) {
               throw ProtocolException.stringTooLong("GradientSet", gradientSetLen, 4096000);
            }

            if (varPos2 + gradientSetVarIntLen + gradientSetLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("GradientSet", varPos2 + gradientSetVarIntLen + gradientSetLen, buf.readableBytes());
            }

            obj.gradientSet = PacketIO.readVarString(buf, varPos2, PacketIO.UTF8);
         }

         if ((nullBits & 8) != 0) {
            int varPosBase3 = buf.getIntLE(offset + 13);
            if (varPosBase3 < 0 || varPosBase3 > buf.writerIndex() - offset - 17) {
               throw ProtocolException.invalidOffset("GradientId", varPosBase3, buf.readableBytes());
            }

            int varPos3 = offset + 17 + varPosBase3;
            int gradientIdLen = VarInt.peek(buf, varPos3);
            if (gradientIdLen < 0) {
               throw ProtocolException.invalidVarInt("GradientId");
            }

            int gradientIdVarIntLen = VarInt.size(gradientIdLen);
            if (gradientIdLen > 4096000) {
               throw ProtocolException.stringTooLong("GradientId", gradientIdLen, 4096000);
            }

            if (varPos3 + gradientIdVarIntLen + gradientIdLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("GradientId", varPos3 + gradientIdVarIntLen + gradientIdLen, buf.readableBytes());
            }

            obj.gradientId = PacketIO.readVarString(buf, varPos3, PacketIO.UTF8);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 17;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 17) {
            throw ProtocolException.invalidOffset("Model", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 17 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 17) {
            throw ProtocolException.invalidOffset("Texture", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 17 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(sl) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 9);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 17) {
            throw ProtocolException.invalidOffset("GradientSet", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 17 + fieldOffset2;
         int sl = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(sl) + sl;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 13);
         if (fieldOffset3 < 0 || fieldOffset3 > buf.writerIndex() - offset - 17) {
            throw ProtocolException.invalidOffset("GradientId", fieldOffset3, maxEnd);
         }

         int pos3 = offset + 17 + fieldOffset3;
         int sl = VarInt.peek(buf, pos3);
         pos3 += VarInt.size(sl) + sl;
         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 17L;
   }

   @Nullable
   public static String getModel(MemorySegment mem) {
      return getModel(mem, 0);
   }

   @Nullable
   public static String getModel(MemorySegment mem, int offset) {
      return hasModel(mem, offset)
         ? PacketIO.readVarString("Model", mem, offset + getValidatedOffset(mem, offset, 1, 17, "Model"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static String getTexture(MemorySegment mem) {
      return getTexture(mem, 0);
   }

   @Nullable
   public static String getTexture(MemorySegment mem, int offset) {
      return hasTexture(mem, offset)
         ? PacketIO.readVarString("Texture", mem, offset + getValidatedOffset(mem, offset, 5, 17, "Texture"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static String getGradientSet(MemorySegment mem) {
      return getGradientSet(mem, 0);
   }

   @Nullable
   public static String getGradientSet(MemorySegment mem, int offset) {
      return hasGradientSet(mem, offset)
         ? PacketIO.readVarString("GradientSet", mem, offset + getValidatedOffset(mem, offset, 9, 17, "GradientSet"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static String getGradientId(MemorySegment mem) {
      return getGradientId(mem, 0);
   }

   @Nullable
   public static String getGradientId(MemorySegment mem, int offset) {
      return hasGradientId(mem, offset)
         ? PacketIO.readVarString("GradientId", mem, offset + getValidatedOffset(mem, offset, 13, 17, "GradientId"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static boolean hasModel(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasTexture(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasGradientSet(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasGradientId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static ModelAttachment toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ModelAttachment toObject(MemorySegment mem, int offset) {
      if (offset + 17 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ModelAttachment", offset + 17, (int)mem.byteSize());
      } else {
         return new ModelAttachment(
            hasModel(mem, offset)
               ? PacketIO.readVarString("Model", mem, offset + getValidatedOffset(mem, offset, 1, 17, "Model"), 4096000, PacketIO.UTF8)
               : null,
            hasTexture(mem, offset)
               ? PacketIO.readVarString("Texture", mem, offset + getValidatedOffset(mem, offset, 5, 17, "Texture"), 4096000, PacketIO.UTF8)
               : null,
            hasGradientSet(mem, offset)
               ? PacketIO.readVarString("GradientSet", mem, offset + getValidatedOffset(mem, offset, 9, 17, "GradientSet"), 4096000, PacketIO.UTF8)
               : null,
            hasGradientId(mem, offset)
               ? PacketIO.readVarString("GradientId", mem, offset + getValidatedOffset(mem, offset, 13, 17, "GradientId"), 4096000, PacketIO.UTF8)
               : null
         );
      }
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

   public int serialize(@Nonnull MemorySegment mem, int offset) {
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

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 17;
      if (this.model != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), varOffset - offset - 17);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.model, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), -1);
      }

      if (this.texture != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), varOffset - offset - 17);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.texture, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), -1);
      }

      if (this.gradientSet != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), varOffset - offset - 17);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.gradientSet, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), -1);
      }

      if (this.gradientId != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 13), varOffset - offset - 17);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.gradientId, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 13), -1);
      }

      return varOffset - offset;
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
            if (modelOffset < 0 || modelOffset > buffer.writerIndex() - offset - 17) {
               return ValidationResult.error("Invalid offset for Model");
            }

            int pos = offset + 17 + modelOffset;
            int modelLen = VarInt.peek(buffer, pos);
            if (modelLen < 0) {
               return ValidationResult.error("Invalid string length for Model");
            }

            if (modelLen > 4096000) {
               return ValidationResult.error("Model exceeds max length 4096000");
            }

            pos += VarInt.size(modelLen);
            pos += modelLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Model");
            }
         }

         if ((nullBits & 2) != 0) {
            int textureOffset = buffer.getIntLE(offset + 5);
            if (textureOffset < 0 || textureOffset > buffer.writerIndex() - offset - 17) {
               return ValidationResult.error("Invalid offset for Texture");
            }

            int posx = offset + 17 + textureOffset;
            int textureLen = VarInt.peek(buffer, posx);
            if (textureLen < 0) {
               return ValidationResult.error("Invalid string length for Texture");
            }

            if (textureLen > 4096000) {
               return ValidationResult.error("Texture exceeds max length 4096000");
            }

            posx += VarInt.size(textureLen);
            posx += textureLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Texture");
            }
         }

         if ((nullBits & 4) != 0) {
            int gradientSetOffset = buffer.getIntLE(offset + 9);
            if (gradientSetOffset < 0 || gradientSetOffset > buffer.writerIndex() - offset - 17) {
               return ValidationResult.error("Invalid offset for GradientSet");
            }

            int posxx = offset + 17 + gradientSetOffset;
            int gradientSetLen = VarInt.peek(buffer, posxx);
            if (gradientSetLen < 0) {
               return ValidationResult.error("Invalid string length for GradientSet");
            }

            if (gradientSetLen > 4096000) {
               return ValidationResult.error("GradientSet exceeds max length 4096000");
            }

            posxx += VarInt.size(gradientSetLen);
            posxx += gradientSetLen;
            if (posxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading GradientSet");
            }
         }

         if ((nullBits & 8) != 0) {
            int gradientIdOffset = buffer.getIntLE(offset + 13);
            if (gradientIdOffset < 0 || gradientIdOffset > buffer.writerIndex() - offset - 17) {
               return ValidationResult.error("Invalid offset for GradientId");
            }

            int posxxx = offset + 17 + gradientIdOffset;
            int gradientIdLen = VarInt.peek(buffer, posxxx);
            if (gradientIdLen < 0) {
               return ValidationResult.error("Invalid string length for GradientId");
            }

            if (gradientIdLen > 4096000) {
               return ValidationResult.error("GradientId exceeds max length 4096000");
            }

            posxxx += VarInt.size(gradientIdLen);
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
