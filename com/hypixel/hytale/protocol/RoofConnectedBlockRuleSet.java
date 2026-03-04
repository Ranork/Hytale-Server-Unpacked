package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RoofConnectedBlockRuleSet {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 9;
   public static final int VARIABLE_FIELD_COUNT = 3;
   public static final int VARIABLE_BLOCK_START = 21;
   public static final int MAX_SIZE = 49152078;
   @Nullable
   public StairConnectedBlockRuleSet regular;
   @Nullable
   public StairConnectedBlockRuleSet hollow;
   public int topperBlockId;
   public int width;
   @Nullable
   public String materialName;

   public RoofConnectedBlockRuleSet() {
   }

   public RoofConnectedBlockRuleSet(
      @Nullable StairConnectedBlockRuleSet regular, @Nullable StairConnectedBlockRuleSet hollow, int topperBlockId, int width, @Nullable String materialName
   ) {
      this.regular = regular;
      this.hollow = hollow;
      this.topperBlockId = topperBlockId;
      this.width = width;
      this.materialName = materialName;
   }

   public RoofConnectedBlockRuleSet(@Nonnull RoofConnectedBlockRuleSet other) {
      this.regular = other.regular;
      this.hollow = other.hollow;
      this.topperBlockId = other.topperBlockId;
      this.width = other.width;
      this.materialName = other.materialName;
   }

   @Nonnull
   public static RoofConnectedBlockRuleSet deserialize(@Nonnull ByteBuf buf, int offset) {
      RoofConnectedBlockRuleSet obj = new RoofConnectedBlockRuleSet();
      byte nullBits = buf.getByte(offset);
      obj.topperBlockId = buf.getIntLE(offset + 1);
      obj.width = buf.getIntLE(offset + 5);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 21 + buf.getIntLE(offset + 9);
         obj.regular = StairConnectedBlockRuleSet.deserialize(buf, varPos0);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 21 + buf.getIntLE(offset + 13);
         obj.hollow = StairConnectedBlockRuleSet.deserialize(buf, varPos1);
      }

      if ((nullBits & 4) != 0) {
         int varPos2 = offset + 21 + buf.getIntLE(offset + 17);
         int materialNameLen = VarInt.peek(buf, varPos2);
         if (materialNameLen < 0) {
            throw ProtocolException.negativeLength("MaterialName", materialNameLen);
         }

         if (materialNameLen > 4096000) {
            throw ProtocolException.stringTooLong("MaterialName", materialNameLen, 4096000);
         }

         obj.materialName = PacketIO.readVarString(buf, varPos2, PacketIO.UTF8);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 21;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 9);
         int pos0 = offset + 21 + fieldOffset0;
         pos0 += StairConnectedBlockRuleSet.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 13);
         int pos1 = offset + 21 + fieldOffset1;
         pos1 += StairConnectedBlockRuleSet.computeBytesConsumed(buf, pos1);
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 17);
         int pos2 = offset + 21 + fieldOffset2;
         int sl = VarInt.peek(buf, pos2);
         pos2 += VarInt.length(buf, pos2) + sl;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      return maxEnd;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.regular != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.hollow != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.materialName != null) {
         nullBits = (byte)(nullBits | 4);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.topperBlockId);
      buf.writeIntLE(this.width);
      int regularOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int hollowOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int materialNameOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.regular != null) {
         buf.setIntLE(regularOffsetSlot, buf.writerIndex() - varBlockStart);
         this.regular.serialize(buf);
      } else {
         buf.setIntLE(regularOffsetSlot, -1);
      }

      if (this.hollow != null) {
         buf.setIntLE(hollowOffsetSlot, buf.writerIndex() - varBlockStart);
         this.hollow.serialize(buf);
      } else {
         buf.setIntLE(hollowOffsetSlot, -1);
      }

      if (this.materialName != null) {
         buf.setIntLE(materialNameOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.materialName, 4096000);
      } else {
         buf.setIntLE(materialNameOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 21;
      if (this.regular != null) {
         size += this.regular.computeSize();
      }

      if (this.hollow != null) {
         size += this.hollow.computeSize();
      }

      if (this.materialName != null) {
         size += PacketIO.stringSize(this.materialName);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 21) {
         return ValidationResult.error("Buffer too small: expected at least 21 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int regularOffset = buffer.getIntLE(offset + 9);
            if (regularOffset < 0) {
               return ValidationResult.error("Invalid offset for Regular");
            }

            int pos = offset + 21 + regularOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Regular");
            }

            ValidationResult regularResult = StairConnectedBlockRuleSet.validateStructure(buffer, pos);
            if (!regularResult.isValid()) {
               return ValidationResult.error("Invalid Regular: " + regularResult.error());
            }

            pos += StairConnectedBlockRuleSet.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits & 2) != 0) {
            int hollowOffset = buffer.getIntLE(offset + 13);
            if (hollowOffset < 0) {
               return ValidationResult.error("Invalid offset for Hollow");
            }

            int posx = offset + 21 + hollowOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Hollow");
            }

            ValidationResult hollowResult = StairConnectedBlockRuleSet.validateStructure(buffer, posx);
            if (!hollowResult.isValid()) {
               return ValidationResult.error("Invalid Hollow: " + hollowResult.error());
            }

            posx += StairConnectedBlockRuleSet.computeBytesConsumed(buffer, posx);
         }

         if ((nullBits & 4) != 0) {
            int materialNameOffset = buffer.getIntLE(offset + 17);
            if (materialNameOffset < 0) {
               return ValidationResult.error("Invalid offset for MaterialName");
            }

            int posxx = offset + 21 + materialNameOffset;
            if (posxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for MaterialName");
            }

            int materialNameLen = VarInt.peek(buffer, posxx);
            if (materialNameLen < 0) {
               return ValidationResult.error("Invalid string length for MaterialName");
            }

            if (materialNameLen > 4096000) {
               return ValidationResult.error("MaterialName exceeds max length 4096000");
            }

            posxx += VarInt.length(buffer, posxx);
            posxx += materialNameLen;
            if (posxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading MaterialName");
            }
         }

         return ValidationResult.OK;
      }
   }

   public RoofConnectedBlockRuleSet clone() {
      RoofConnectedBlockRuleSet copy = new RoofConnectedBlockRuleSet();
      copy.regular = this.regular != null ? this.regular.clone() : null;
      copy.hollow = this.hollow != null ? this.hollow.clone() : null;
      copy.topperBlockId = this.topperBlockId;
      copy.width = this.width;
      copy.materialName = this.materialName;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof RoofConnectedBlockRuleSet other)
            ? false
            : Objects.equals(this.regular, other.regular)
               && Objects.equals(this.hollow, other.hollow)
               && this.topperBlockId == other.topperBlockId
               && this.width == other.width
               && Objects.equals(this.materialName, other.materialName);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.regular, this.hollow, this.topperBlockId, this.width, this.materialName);
   }
}
