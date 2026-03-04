package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockGathering {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 3;
   public static final int VARIABLE_BLOCK_START = 13;
   public static final int MAX_SIZE = 114688092;
   @Nullable
   public BlockBreaking breaking;
   @Nullable
   public Harvesting harvest;
   @Nullable
   public SoftBlock soft;

   public BlockGathering() {
   }

   public BlockGathering(@Nullable BlockBreaking breaking, @Nullable Harvesting harvest, @Nullable SoftBlock soft) {
      this.breaking = breaking;
      this.harvest = harvest;
      this.soft = soft;
   }

   public BlockGathering(@Nonnull BlockGathering other) {
      this.breaking = other.breaking;
      this.harvest = other.harvest;
      this.soft = other.soft;
   }

   @Nonnull
   public static BlockGathering deserialize(@Nonnull ByteBuf buf, int offset) {
      BlockGathering obj = new BlockGathering();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 13 + buf.getIntLE(offset + 1);
         obj.breaking = BlockBreaking.deserialize(buf, varPos0);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 13 + buf.getIntLE(offset + 5);
         obj.harvest = Harvesting.deserialize(buf, varPos1);
      }

      if ((nullBits & 4) != 0) {
         int varPos2 = offset + 13 + buf.getIntLE(offset + 9);
         obj.soft = SoftBlock.deserialize(buf, varPos2);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 13;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         int pos0 = offset + 13 + fieldOffset0;
         pos0 += BlockBreaking.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         int pos1 = offset + 13 + fieldOffset1;
         pos1 += Harvesting.computeBytesConsumed(buf, pos1);
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 9);
         int pos2 = offset + 13 + fieldOffset2;
         pos2 += SoftBlock.computeBytesConsumed(buf, pos2);
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      return maxEnd;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.breaking != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.harvest != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.soft != null) {
         nullBits = (byte)(nullBits | 4);
      }

      buf.writeByte(nullBits);
      int breakingOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int harvestOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int softOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.breaking != null) {
         buf.setIntLE(breakingOffsetSlot, buf.writerIndex() - varBlockStart);
         this.breaking.serialize(buf);
      } else {
         buf.setIntLE(breakingOffsetSlot, -1);
      }

      if (this.harvest != null) {
         buf.setIntLE(harvestOffsetSlot, buf.writerIndex() - varBlockStart);
         this.harvest.serialize(buf);
      } else {
         buf.setIntLE(harvestOffsetSlot, -1);
      }

      if (this.soft != null) {
         buf.setIntLE(softOffsetSlot, buf.writerIndex() - varBlockStart);
         this.soft.serialize(buf);
      } else {
         buf.setIntLE(softOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 13;
      if (this.breaking != null) {
         size += this.breaking.computeSize();
      }

      if (this.harvest != null) {
         size += this.harvest.computeSize();
      }

      if (this.soft != null) {
         size += this.soft.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 13) {
         return ValidationResult.error("Buffer too small: expected at least 13 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int breakingOffset = buffer.getIntLE(offset + 1);
            if (breakingOffset < 0) {
               return ValidationResult.error("Invalid offset for Breaking");
            }

            int pos = offset + 13 + breakingOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Breaking");
            }

            ValidationResult breakingResult = BlockBreaking.validateStructure(buffer, pos);
            if (!breakingResult.isValid()) {
               return ValidationResult.error("Invalid Breaking: " + breakingResult.error());
            }

            pos += BlockBreaking.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits & 2) != 0) {
            int harvestOffset = buffer.getIntLE(offset + 5);
            if (harvestOffset < 0) {
               return ValidationResult.error("Invalid offset for Harvest");
            }

            int posx = offset + 13 + harvestOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Harvest");
            }

            ValidationResult harvestResult = Harvesting.validateStructure(buffer, posx);
            if (!harvestResult.isValid()) {
               return ValidationResult.error("Invalid Harvest: " + harvestResult.error());
            }

            posx += Harvesting.computeBytesConsumed(buffer, posx);
         }

         if ((nullBits & 4) != 0) {
            int softOffset = buffer.getIntLE(offset + 9);
            if (softOffset < 0) {
               return ValidationResult.error("Invalid offset for Soft");
            }

            int posxx = offset + 13 + softOffset;
            if (posxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Soft");
            }

            ValidationResult softResult = SoftBlock.validateStructure(buffer, posxx);
            if (!softResult.isValid()) {
               return ValidationResult.error("Invalid Soft: " + softResult.error());
            }

            posxx += SoftBlock.computeBytesConsumed(buffer, posxx);
         }

         return ValidationResult.OK;
      }
   }

   public BlockGathering clone() {
      BlockGathering copy = new BlockGathering();
      copy.breaking = this.breaking != null ? this.breaking.clone() : null;
      copy.harvest = this.harvest != null ? this.harvest.clone() : null;
      copy.soft = this.soft != null ? this.soft.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BlockGathering other)
            ? false
            : Objects.equals(this.breaking, other.breaking) && Objects.equals(this.harvest, other.harvest) && Objects.equals(this.soft, other.soft);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.breaking, this.harvest, this.soft);
   }
}
