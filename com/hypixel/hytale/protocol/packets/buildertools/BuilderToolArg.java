package com.hypixel.hytale.protocol.packets.buildertools;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderToolArg {
   public static final int NULLABLE_BIT_FIELD_SIZE = 2;
   public static final int FIXED_BLOCK_SIZE = 33;
   public static final int VARIABLE_FIELD_COUNT = 5;
   public static final int VARIABLE_BLOCK_START = 53;
   public static final int MAX_SIZE = 1677721600;
   public boolean required;
   @Nullable
   public String id;
   @Nonnull
   public BuilderToolArgType argType = BuilderToolArgType.Bool;
   @Nullable
   public BuilderToolBoolArg boolArg;
   @Nullable
   public BuilderToolFloatArg floatArg;
   @Nullable
   public BuilderToolIntArg intArg;
   @Nullable
   public BuilderToolStringArg stringArg;
   @Nullable
   public BuilderToolBlockArg blockArg;
   @Nullable
   public BuilderToolMaskArg maskArg;
   @Nullable
   public BuilderToolBrushShapeArg brushShapeArg;
   @Nullable
   public BuilderToolBrushOriginArg brushOriginArg;
   @Nullable
   public BuilderToolBrushAxisArg brushAxisArg;
   @Nullable
   public BuilderToolRotationArg rotationArg;
   @Nullable
   public BuilderToolOptionArg optionArg;

   public BuilderToolArg() {
   }

   public BuilderToolArg(
      boolean required,
      @Nullable String id,
      @Nonnull BuilderToolArgType argType,
      @Nullable BuilderToolBoolArg boolArg,
      @Nullable BuilderToolFloatArg floatArg,
      @Nullable BuilderToolIntArg intArg,
      @Nullable BuilderToolStringArg stringArg,
      @Nullable BuilderToolBlockArg blockArg,
      @Nullable BuilderToolMaskArg maskArg,
      @Nullable BuilderToolBrushShapeArg brushShapeArg,
      @Nullable BuilderToolBrushOriginArg brushOriginArg,
      @Nullable BuilderToolBrushAxisArg brushAxisArg,
      @Nullable BuilderToolRotationArg rotationArg,
      @Nullable BuilderToolOptionArg optionArg
   ) {
      this.required = required;
      this.id = id;
      this.argType = argType;
      this.boolArg = boolArg;
      this.floatArg = floatArg;
      this.intArg = intArg;
      this.stringArg = stringArg;
      this.blockArg = blockArg;
      this.maskArg = maskArg;
      this.brushShapeArg = brushShapeArg;
      this.brushOriginArg = brushOriginArg;
      this.brushAxisArg = brushAxisArg;
      this.rotationArg = rotationArg;
      this.optionArg = optionArg;
   }

   public BuilderToolArg(@Nonnull BuilderToolArg other) {
      this.required = other.required;
      this.id = other.id;
      this.argType = other.argType;
      this.boolArg = other.boolArg;
      this.floatArg = other.floatArg;
      this.intArg = other.intArg;
      this.stringArg = other.stringArg;
      this.blockArg = other.blockArg;
      this.maskArg = other.maskArg;
      this.brushShapeArg = other.brushShapeArg;
      this.brushOriginArg = other.brushOriginArg;
      this.brushAxisArg = other.brushAxisArg;
      this.rotationArg = other.rotationArg;
      this.optionArg = other.optionArg;
   }

   @Nonnull
   public static BuilderToolArg deserialize(@Nonnull ByteBuf buf, int offset) {
      BuilderToolArg obj = new BuilderToolArg();
      byte[] nullBits = PacketIO.readBytes(buf, offset, 2);
      obj.required = buf.getByte(offset + 2) != 0;
      obj.argType = BuilderToolArgType.fromValue(buf.getByte(offset + 3));
      if ((nullBits[0] & 1) != 0) {
         obj.boolArg = BuilderToolBoolArg.deserialize(buf, offset + 4);
      }

      if ((nullBits[0] & 2) != 0) {
         obj.floatArg = BuilderToolFloatArg.deserialize(buf, offset + 5);
      }

      if ((nullBits[0] & 4) != 0) {
         obj.intArg = BuilderToolIntArg.deserialize(buf, offset + 17);
      }

      if ((nullBits[0] & 8) != 0) {
         obj.brushShapeArg = BuilderToolBrushShapeArg.deserialize(buf, offset + 29);
      }

      if ((nullBits[0] & 16) != 0) {
         obj.brushOriginArg = BuilderToolBrushOriginArg.deserialize(buf, offset + 30);
      }

      if ((nullBits[0] & 32) != 0) {
         obj.brushAxisArg = BuilderToolBrushAxisArg.deserialize(buf, offset + 31);
      }

      if ((nullBits[0] & 64) != 0) {
         obj.rotationArg = BuilderToolRotationArg.deserialize(buf, offset + 32);
      }

      if ((nullBits[0] & 128) != 0) {
         int varPos0 = offset + 53 + buf.getIntLE(offset + 33);
         int idLen = VarInt.peek(buf, varPos0);
         if (idLen < 0) {
            throw ProtocolException.negativeLength("Id", idLen);
         }

         if (idLen > 4096000) {
            throw ProtocolException.stringTooLong("Id", idLen, 4096000);
         }

         obj.id = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits[1] & 1) != 0) {
         int varPos1 = offset + 53 + buf.getIntLE(offset + 37);
         obj.stringArg = BuilderToolStringArg.deserialize(buf, varPos1);
      }

      if ((nullBits[1] & 2) != 0) {
         int varPos2 = offset + 53 + buf.getIntLE(offset + 41);
         obj.blockArg = BuilderToolBlockArg.deserialize(buf, varPos2);
      }

      if ((nullBits[1] & 4) != 0) {
         int varPos3 = offset + 53 + buf.getIntLE(offset + 45);
         obj.maskArg = BuilderToolMaskArg.deserialize(buf, varPos3);
      }

      if ((nullBits[1] & 8) != 0) {
         int varPos4 = offset + 53 + buf.getIntLE(offset + 49);
         obj.optionArg = BuilderToolOptionArg.deserialize(buf, varPos4);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte[] nullBits = PacketIO.readBytes(buf, offset, 2);
      int maxEnd = 53;
      if ((nullBits[0] & 128) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 33);
         int pos0 = offset + 53 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits[1] & 1) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 37);
         int pos1 = offset + 53 + fieldOffset1;
         pos1 += BuilderToolStringArg.computeBytesConsumed(buf, pos1);
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits[1] & 2) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 41);
         int pos2 = offset + 53 + fieldOffset2;
         pos2 += BuilderToolBlockArg.computeBytesConsumed(buf, pos2);
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits[1] & 4) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 45);
         int pos3 = offset + 53 + fieldOffset3;
         pos3 += BuilderToolMaskArg.computeBytesConsumed(buf, pos3);
         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      if ((nullBits[1] & 8) != 0) {
         int fieldOffset4 = buf.getIntLE(offset + 49);
         int pos4 = offset + 53 + fieldOffset4;
         pos4 += BuilderToolOptionArg.computeBytesConsumed(buf, pos4);
         if (pos4 - offset > maxEnd) {
            maxEnd = pos4 - offset;
         }
      }

      return maxEnd;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte[] nullBits = new byte[2];
      if (this.boolArg != null) {
         nullBits[0] = (byte)(nullBits[0] | 1);
      }

      if (this.floatArg != null) {
         nullBits[0] = (byte)(nullBits[0] | 2);
      }

      if (this.intArg != null) {
         nullBits[0] = (byte)(nullBits[0] | 4);
      }

      if (this.brushShapeArg != null) {
         nullBits[0] = (byte)(nullBits[0] | 8);
      }

      if (this.brushOriginArg != null) {
         nullBits[0] = (byte)(nullBits[0] | 16);
      }

      if (this.brushAxisArg != null) {
         nullBits[0] = (byte)(nullBits[0] | 32);
      }

      if (this.rotationArg != null) {
         nullBits[0] = (byte)(nullBits[0] | 64);
      }

      if (this.id != null) {
         nullBits[0] = (byte)(nullBits[0] | 128);
      }

      if (this.stringArg != null) {
         nullBits[1] = (byte)(nullBits[1] | 1);
      }

      if (this.blockArg != null) {
         nullBits[1] = (byte)(nullBits[1] | 2);
      }

      if (this.maskArg != null) {
         nullBits[1] = (byte)(nullBits[1] | 4);
      }

      if (this.optionArg != null) {
         nullBits[1] = (byte)(nullBits[1] | 8);
      }

      buf.writeBytes(nullBits);
      buf.writeByte(this.required ? 1 : 0);
      buf.writeByte(this.argType.getValue());
      if (this.boolArg != null) {
         this.boolArg.serialize(buf);
      } else {
         buf.writeZero(1);
      }

      if (this.floatArg != null) {
         this.floatArg.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      if (this.intArg != null) {
         this.intArg.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      if (this.brushShapeArg != null) {
         this.brushShapeArg.serialize(buf);
      } else {
         buf.writeZero(1);
      }

      if (this.brushOriginArg != null) {
         this.brushOriginArg.serialize(buf);
      } else {
         buf.writeZero(1);
      }

      if (this.brushAxisArg != null) {
         this.brushAxisArg.serialize(buf);
      } else {
         buf.writeZero(1);
      }

      if (this.rotationArg != null) {
         this.rotationArg.serialize(buf);
      } else {
         buf.writeZero(1);
      }

      int idOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int stringArgOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int blockArgOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int maskArgOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int optionArgOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.id != null) {
         buf.setIntLE(idOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.id, 4096000);
      } else {
         buf.setIntLE(idOffsetSlot, -1);
      }

      if (this.stringArg != null) {
         buf.setIntLE(stringArgOffsetSlot, buf.writerIndex() - varBlockStart);
         this.stringArg.serialize(buf);
      } else {
         buf.setIntLE(stringArgOffsetSlot, -1);
      }

      if (this.blockArg != null) {
         buf.setIntLE(blockArgOffsetSlot, buf.writerIndex() - varBlockStart);
         this.blockArg.serialize(buf);
      } else {
         buf.setIntLE(blockArgOffsetSlot, -1);
      }

      if (this.maskArg != null) {
         buf.setIntLE(maskArgOffsetSlot, buf.writerIndex() - varBlockStart);
         this.maskArg.serialize(buf);
      } else {
         buf.setIntLE(maskArgOffsetSlot, -1);
      }

      if (this.optionArg != null) {
         buf.setIntLE(optionArgOffsetSlot, buf.writerIndex() - varBlockStart);
         this.optionArg.serialize(buf);
      } else {
         buf.setIntLE(optionArgOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 53;
      if (this.id != null) {
         size += PacketIO.stringSize(this.id);
      }

      if (this.stringArg != null) {
         size += this.stringArg.computeSize();
      }

      if (this.blockArg != null) {
         size += this.blockArg.computeSize();
      }

      if (this.maskArg != null) {
         size += this.maskArg.computeSize();
      }

      if (this.optionArg != null) {
         size += this.optionArg.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 53) {
         return ValidationResult.error("Buffer too small: expected at least 53 bytes");
      } else {
         byte[] nullBits = PacketIO.readBytes(buffer, offset, 2);
         if ((nullBits[0] & 128) != 0) {
            int idOffset = buffer.getIntLE(offset + 33);
            if (idOffset < 0) {
               return ValidationResult.error("Invalid offset for Id");
            }

            int pos = offset + 53 + idOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Id");
            }

            int idLen = VarInt.peek(buffer, pos);
            if (idLen < 0) {
               return ValidationResult.error("Invalid string length for Id");
            }

            if (idLen > 4096000) {
               return ValidationResult.error("Id exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += idLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Id");
            }
         }

         if ((nullBits[1] & 1) != 0) {
            int stringArgOffset = buffer.getIntLE(offset + 37);
            if (stringArgOffset < 0) {
               return ValidationResult.error("Invalid offset for StringArg");
            }

            int posx = offset + 53 + stringArgOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for StringArg");
            }

            ValidationResult stringArgResult = BuilderToolStringArg.validateStructure(buffer, posx);
            if (!stringArgResult.isValid()) {
               return ValidationResult.error("Invalid StringArg: " + stringArgResult.error());
            }

            posx += BuilderToolStringArg.computeBytesConsumed(buffer, posx);
         }

         if ((nullBits[1] & 2) != 0) {
            int blockArgOffset = buffer.getIntLE(offset + 41);
            if (blockArgOffset < 0) {
               return ValidationResult.error("Invalid offset for BlockArg");
            }

            int posxx = offset + 53 + blockArgOffset;
            if (posxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for BlockArg");
            }

            ValidationResult blockArgResult = BuilderToolBlockArg.validateStructure(buffer, posxx);
            if (!blockArgResult.isValid()) {
               return ValidationResult.error("Invalid BlockArg: " + blockArgResult.error());
            }

            posxx += BuilderToolBlockArg.computeBytesConsumed(buffer, posxx);
         }

         if ((nullBits[1] & 4) != 0) {
            int maskArgOffset = buffer.getIntLE(offset + 45);
            if (maskArgOffset < 0) {
               return ValidationResult.error("Invalid offset for MaskArg");
            }

            int posxxx = offset + 53 + maskArgOffset;
            if (posxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for MaskArg");
            }

            ValidationResult maskArgResult = BuilderToolMaskArg.validateStructure(buffer, posxxx);
            if (!maskArgResult.isValid()) {
               return ValidationResult.error("Invalid MaskArg: " + maskArgResult.error());
            }

            posxxx += BuilderToolMaskArg.computeBytesConsumed(buffer, posxxx);
         }

         if ((nullBits[1] & 8) != 0) {
            int optionArgOffset = buffer.getIntLE(offset + 49);
            if (optionArgOffset < 0) {
               return ValidationResult.error("Invalid offset for OptionArg");
            }

            int posxxxx = offset + 53 + optionArgOffset;
            if (posxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for OptionArg");
            }

            ValidationResult optionArgResult = BuilderToolOptionArg.validateStructure(buffer, posxxxx);
            if (!optionArgResult.isValid()) {
               return ValidationResult.error("Invalid OptionArg: " + optionArgResult.error());
            }

            posxxxx += BuilderToolOptionArg.computeBytesConsumed(buffer, posxxxx);
         }

         return ValidationResult.OK;
      }
   }

   public BuilderToolArg clone() {
      BuilderToolArg copy = new BuilderToolArg();
      copy.required = this.required;
      copy.id = this.id;
      copy.argType = this.argType;
      copy.boolArg = this.boolArg != null ? this.boolArg.clone() : null;
      copy.floatArg = this.floatArg != null ? this.floatArg.clone() : null;
      copy.intArg = this.intArg != null ? this.intArg.clone() : null;
      copy.stringArg = this.stringArg != null ? this.stringArg.clone() : null;
      copy.blockArg = this.blockArg != null ? this.blockArg.clone() : null;
      copy.maskArg = this.maskArg != null ? this.maskArg.clone() : null;
      copy.brushShapeArg = this.brushShapeArg != null ? this.brushShapeArg.clone() : null;
      copy.brushOriginArg = this.brushOriginArg != null ? this.brushOriginArg.clone() : null;
      copy.brushAxisArg = this.brushAxisArg != null ? this.brushAxisArg.clone() : null;
      copy.rotationArg = this.rotationArg != null ? this.rotationArg.clone() : null;
      copy.optionArg = this.optionArg != null ? this.optionArg.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BuilderToolArg other)
            ? false
            : this.required == other.required
               && Objects.equals(this.id, other.id)
               && Objects.equals(this.argType, other.argType)
               && Objects.equals(this.boolArg, other.boolArg)
               && Objects.equals(this.floatArg, other.floatArg)
               && Objects.equals(this.intArg, other.intArg)
               && Objects.equals(this.stringArg, other.stringArg)
               && Objects.equals(this.blockArg, other.blockArg)
               && Objects.equals(this.maskArg, other.maskArg)
               && Objects.equals(this.brushShapeArg, other.brushShapeArg)
               && Objects.equals(this.brushOriginArg, other.brushOriginArg)
               && Objects.equals(this.brushAxisArg, other.brushAxisArg)
               && Objects.equals(this.rotationArg, other.rotationArg)
               && Objects.equals(this.optionArg, other.optionArg);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(
         this.required,
         this.id,
         this.argType,
         this.boolArg,
         this.floatArg,
         this.intArg,
         this.stringArg,
         this.blockArg,
         this.maskArg,
         this.brushShapeArg,
         this.brushOriginArg,
         this.brushAxisArg,
         this.rotationArg,
         this.optionArg
      );
   }
}
