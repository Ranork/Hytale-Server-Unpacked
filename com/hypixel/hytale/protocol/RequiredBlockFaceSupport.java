package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RequiredBlockFaceSupport {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 17;
   public static final int VARIABLE_FIELD_COUNT = 4;
   public static final int VARIABLE_BLOCK_START = 33;
   public static final int MAX_SIZE = 98304053;
   @Nullable
   public String faceType;
   @Nullable
   public String selfFaceType;
   @Nullable
   public String blockSetId;
   public int blockTypeId;
   public int tagIndex;
   public int fluidId;
   @Nonnull
   public SupportMatch support = SupportMatch.Ignored;
   @Nonnull
   public SupportMatch matchSelf = SupportMatch.Ignored;
   public boolean allowSupportPropagation;
   public boolean rotate;
   @Nullable
   public Vector3i[] filler;

   public RequiredBlockFaceSupport() {
   }

   public RequiredBlockFaceSupport(
      @Nullable String faceType,
      @Nullable String selfFaceType,
      @Nullable String blockSetId,
      int blockTypeId,
      int tagIndex,
      int fluidId,
      @Nonnull SupportMatch support,
      @Nonnull SupportMatch matchSelf,
      boolean allowSupportPropagation,
      boolean rotate,
      @Nullable Vector3i[] filler
   ) {
      this.faceType = faceType;
      this.selfFaceType = selfFaceType;
      this.blockSetId = blockSetId;
      this.blockTypeId = blockTypeId;
      this.tagIndex = tagIndex;
      this.fluidId = fluidId;
      this.support = support;
      this.matchSelf = matchSelf;
      this.allowSupportPropagation = allowSupportPropagation;
      this.rotate = rotate;
      this.filler = filler;
   }

   public RequiredBlockFaceSupport(@Nonnull RequiredBlockFaceSupport other) {
      this.faceType = other.faceType;
      this.selfFaceType = other.selfFaceType;
      this.blockSetId = other.blockSetId;
      this.blockTypeId = other.blockTypeId;
      this.tagIndex = other.tagIndex;
      this.fluidId = other.fluidId;
      this.support = other.support;
      this.matchSelf = other.matchSelf;
      this.allowSupportPropagation = other.allowSupportPropagation;
      this.rotate = other.rotate;
      this.filler = other.filler;
   }

   @Nonnull
   public static RequiredBlockFaceSupport deserialize(@Nonnull ByteBuf buf, int offset) {
      RequiredBlockFaceSupport obj = new RequiredBlockFaceSupport();
      byte nullBits = buf.getByte(offset);
      obj.blockTypeId = buf.getIntLE(offset + 1);
      obj.tagIndex = buf.getIntLE(offset + 5);
      obj.fluidId = buf.getIntLE(offset + 9);
      obj.support = SupportMatch.fromValue(buf.getByte(offset + 13));
      obj.matchSelf = SupportMatch.fromValue(buf.getByte(offset + 14));
      obj.allowSupportPropagation = buf.getByte(offset + 15) != 0;
      obj.rotate = buf.getByte(offset + 16) != 0;
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 33 + buf.getIntLE(offset + 17);
         int faceTypeLen = VarInt.peek(buf, varPos0);
         if (faceTypeLen < 0) {
            throw ProtocolException.negativeLength("FaceType", faceTypeLen);
         }

         if (faceTypeLen > 4096000) {
            throw ProtocolException.stringTooLong("FaceType", faceTypeLen, 4096000);
         }

         obj.faceType = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 33 + buf.getIntLE(offset + 21);
         int selfFaceTypeLen = VarInt.peek(buf, varPos1);
         if (selfFaceTypeLen < 0) {
            throw ProtocolException.negativeLength("SelfFaceType", selfFaceTypeLen);
         }

         if (selfFaceTypeLen > 4096000) {
            throw ProtocolException.stringTooLong("SelfFaceType", selfFaceTypeLen, 4096000);
         }

         obj.selfFaceType = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
      }

      if ((nullBits & 4) != 0) {
         int varPos2 = offset + 33 + buf.getIntLE(offset + 25);
         int blockSetIdLen = VarInt.peek(buf, varPos2);
         if (blockSetIdLen < 0) {
            throw ProtocolException.negativeLength("BlockSetId", blockSetIdLen);
         }

         if (blockSetIdLen > 4096000) {
            throw ProtocolException.stringTooLong("BlockSetId", blockSetIdLen, 4096000);
         }

         obj.blockSetId = PacketIO.readVarString(buf, varPos2, PacketIO.UTF8);
      }

      if ((nullBits & 8) != 0) {
         int varPos3 = offset + 33 + buf.getIntLE(offset + 29);
         int fillerCount = VarInt.peek(buf, varPos3);
         if (fillerCount < 0) {
            throw ProtocolException.negativeLength("Filler", fillerCount);
         }

         if (fillerCount > 4096000) {
            throw ProtocolException.arrayTooLong("Filler", fillerCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos3);
         if (varPos3 + varIntLen + fillerCount * 12L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Filler", varPos3 + varIntLen + fillerCount * 12, buf.readableBytes());
         }

         obj.filler = new Vector3i[fillerCount];
         int elemPos = varPos3 + varIntLen;

         for (int i = 0; i < fillerCount; i++) {
            obj.filler[i] = Vector3i.deserialize(buf, elemPos);
            elemPos += Vector3i.computeBytesConsumed(buf, elemPos);
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 33;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 17);
         int pos0 = offset + 33 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 21);
         int pos1 = offset + 33 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 25);
         int pos2 = offset + 33 + fieldOffset2;
         int sl = VarInt.peek(buf, pos2);
         pos2 += VarInt.length(buf, pos2) + sl;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 29);
         int pos3 = offset + 33 + fieldOffset3;
         int arrLen = VarInt.peek(buf, pos3);
         pos3 += VarInt.length(buf, pos3);

         for (int i = 0; i < arrLen; i++) {
            pos3 += Vector3i.computeBytesConsumed(buf, pos3);
         }

         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      return maxEnd;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.faceType != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.selfFaceType != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.blockSetId != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.filler != null) {
         nullBits = (byte)(nullBits | 8);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.blockTypeId);
      buf.writeIntLE(this.tagIndex);
      buf.writeIntLE(this.fluidId);
      buf.writeByte(this.support.getValue());
      buf.writeByte(this.matchSelf.getValue());
      buf.writeByte(this.allowSupportPropagation ? 1 : 0);
      buf.writeByte(this.rotate ? 1 : 0);
      int faceTypeOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int selfFaceTypeOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int blockSetIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int fillerOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.faceType != null) {
         buf.setIntLE(faceTypeOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.faceType, 4096000);
      } else {
         buf.setIntLE(faceTypeOffsetSlot, -1);
      }

      if (this.selfFaceType != null) {
         buf.setIntLE(selfFaceTypeOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.selfFaceType, 4096000);
      } else {
         buf.setIntLE(selfFaceTypeOffsetSlot, -1);
      }

      if (this.blockSetId != null) {
         buf.setIntLE(blockSetIdOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.blockSetId, 4096000);
      } else {
         buf.setIntLE(blockSetIdOffsetSlot, -1);
      }

      if (this.filler != null) {
         buf.setIntLE(fillerOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.filler.length > 4096000) {
            throw ProtocolException.arrayTooLong("Filler", this.filler.length, 4096000);
         }

         VarInt.write(buf, this.filler.length);

         for (Vector3i item : this.filler) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(fillerOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 33;
      if (this.faceType != null) {
         size += PacketIO.stringSize(this.faceType);
      }

      if (this.selfFaceType != null) {
         size += PacketIO.stringSize(this.selfFaceType);
      }

      if (this.blockSetId != null) {
         size += PacketIO.stringSize(this.blockSetId);
      }

      if (this.filler != null) {
         size += VarInt.size(this.filler.length) + this.filler.length * 12;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 33) {
         return ValidationResult.error("Buffer too small: expected at least 33 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int faceTypeOffset = buffer.getIntLE(offset + 17);
            if (faceTypeOffset < 0) {
               return ValidationResult.error("Invalid offset for FaceType");
            }

            int pos = offset + 33 + faceTypeOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for FaceType");
            }

            int faceTypeLen = VarInt.peek(buffer, pos);
            if (faceTypeLen < 0) {
               return ValidationResult.error("Invalid string length for FaceType");
            }

            if (faceTypeLen > 4096000) {
               return ValidationResult.error("FaceType exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += faceTypeLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading FaceType");
            }
         }

         if ((nullBits & 2) != 0) {
            int selfFaceTypeOffset = buffer.getIntLE(offset + 21);
            if (selfFaceTypeOffset < 0) {
               return ValidationResult.error("Invalid offset for SelfFaceType");
            }

            int posx = offset + 33 + selfFaceTypeOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for SelfFaceType");
            }

            int selfFaceTypeLen = VarInt.peek(buffer, posx);
            if (selfFaceTypeLen < 0) {
               return ValidationResult.error("Invalid string length for SelfFaceType");
            }

            if (selfFaceTypeLen > 4096000) {
               return ValidationResult.error("SelfFaceType exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
            posx += selfFaceTypeLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading SelfFaceType");
            }
         }

         if ((nullBits & 4) != 0) {
            int blockSetIdOffset = buffer.getIntLE(offset + 25);
            if (blockSetIdOffset < 0) {
               return ValidationResult.error("Invalid offset for BlockSetId");
            }

            int posxx = offset + 33 + blockSetIdOffset;
            if (posxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for BlockSetId");
            }

            int blockSetIdLen = VarInt.peek(buffer, posxx);
            if (blockSetIdLen < 0) {
               return ValidationResult.error("Invalid string length for BlockSetId");
            }

            if (blockSetIdLen > 4096000) {
               return ValidationResult.error("BlockSetId exceeds max length 4096000");
            }

            posxx += VarInt.length(buffer, posxx);
            posxx += blockSetIdLen;
            if (posxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading BlockSetId");
            }
         }

         if ((nullBits & 8) != 0) {
            int fillerOffset = buffer.getIntLE(offset + 29);
            if (fillerOffset < 0) {
               return ValidationResult.error("Invalid offset for Filler");
            }

            int posxxx = offset + 33 + fillerOffset;
            if (posxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Filler");
            }

            int fillerCount = VarInt.peek(buffer, posxxx);
            if (fillerCount < 0) {
               return ValidationResult.error("Invalid array count for Filler");
            }

            if (fillerCount > 4096000) {
               return ValidationResult.error("Filler exceeds max length 4096000");
            }

            posxxx += VarInt.length(buffer, posxxx);
            posxxx += fillerCount * 12;
            if (posxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Filler");
            }
         }

         return ValidationResult.OK;
      }
   }

   public RequiredBlockFaceSupport clone() {
      RequiredBlockFaceSupport copy = new RequiredBlockFaceSupport();
      copy.faceType = this.faceType;
      copy.selfFaceType = this.selfFaceType;
      copy.blockSetId = this.blockSetId;
      copy.blockTypeId = this.blockTypeId;
      copy.tagIndex = this.tagIndex;
      copy.fluidId = this.fluidId;
      copy.support = this.support;
      copy.matchSelf = this.matchSelf;
      copy.allowSupportPropagation = this.allowSupportPropagation;
      copy.rotate = this.rotate;
      copy.filler = this.filler != null ? Arrays.stream(this.filler).map(e -> e.clone()).toArray(Vector3i[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof RequiredBlockFaceSupport other)
            ? false
            : Objects.equals(this.faceType, other.faceType)
               && Objects.equals(this.selfFaceType, other.selfFaceType)
               && Objects.equals(this.blockSetId, other.blockSetId)
               && this.blockTypeId == other.blockTypeId
               && this.tagIndex == other.tagIndex
               && this.fluidId == other.fluidId
               && Objects.equals(this.support, other.support)
               && Objects.equals(this.matchSelf, other.matchSelf)
               && this.allowSupportPropagation == other.allowSupportPropagation
               && this.rotate == other.rotate
               && Arrays.equals((Object[])this.filler, (Object[])other.filler);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.faceType);
      result = 31 * result + Objects.hashCode(this.selfFaceType);
      result = 31 * result + Objects.hashCode(this.blockSetId);
      result = 31 * result + Integer.hashCode(this.blockTypeId);
      result = 31 * result + Integer.hashCode(this.tagIndex);
      result = 31 * result + Integer.hashCode(this.fluidId);
      result = 31 * result + Objects.hashCode(this.support);
      result = 31 * result + Objects.hashCode(this.matchSelf);
      result = 31 * result + Boolean.hashCode(this.allowSupportPropagation);
      result = 31 * result + Boolean.hashCode(this.rotate);
      return 31 * result + Arrays.hashCode((Object[])this.filler);
   }
}
