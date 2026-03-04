package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ConnectedBlockRuleSet {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 10;
   public static final int MAX_SIZE = 65536114;
   @Nonnull
   public ConnectedBlockRuleSetType type = ConnectedBlockRuleSetType.Stair;
   @Nullable
   public StairConnectedBlockRuleSet stair;
   @Nullable
   public RoofConnectedBlockRuleSet roof;

   public ConnectedBlockRuleSet() {
   }

   public ConnectedBlockRuleSet(@Nonnull ConnectedBlockRuleSetType type, @Nullable StairConnectedBlockRuleSet stair, @Nullable RoofConnectedBlockRuleSet roof) {
      this.type = type;
      this.stair = stair;
      this.roof = roof;
   }

   public ConnectedBlockRuleSet(@Nonnull ConnectedBlockRuleSet other) {
      this.type = other.type;
      this.stair = other.stair;
      this.roof = other.roof;
   }

   @Nonnull
   public static ConnectedBlockRuleSet deserialize(@Nonnull ByteBuf buf, int offset) {
      ConnectedBlockRuleSet obj = new ConnectedBlockRuleSet();
      byte nullBits = buf.getByte(offset);
      obj.type = ConnectedBlockRuleSetType.fromValue(buf.getByte(offset + 1));
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 10 + buf.getIntLE(offset + 2);
         obj.stair = StairConnectedBlockRuleSet.deserialize(buf, varPos0);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 10 + buf.getIntLE(offset + 6);
         obj.roof = RoofConnectedBlockRuleSet.deserialize(buf, varPos1);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 10;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 2);
         int pos0 = offset + 10 + fieldOffset0;
         pos0 += StairConnectedBlockRuleSet.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 6);
         int pos1 = offset + 10 + fieldOffset1;
         pos1 += RoofConnectedBlockRuleSet.computeBytesConsumed(buf, pos1);
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.stair != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.roof != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      int stairOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int roofOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.stair != null) {
         buf.setIntLE(stairOffsetSlot, buf.writerIndex() - varBlockStart);
         this.stair.serialize(buf);
      } else {
         buf.setIntLE(stairOffsetSlot, -1);
      }

      if (this.roof != null) {
         buf.setIntLE(roofOffsetSlot, buf.writerIndex() - varBlockStart);
         this.roof.serialize(buf);
      } else {
         buf.setIntLE(roofOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 10;
      if (this.stair != null) {
         size += this.stair.computeSize();
      }

      if (this.roof != null) {
         size += this.roof.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 10) {
         return ValidationResult.error("Buffer too small: expected at least 10 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int stairOffset = buffer.getIntLE(offset + 2);
            if (stairOffset < 0) {
               return ValidationResult.error("Invalid offset for Stair");
            }

            int pos = offset + 10 + stairOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Stair");
            }

            ValidationResult stairResult = StairConnectedBlockRuleSet.validateStructure(buffer, pos);
            if (!stairResult.isValid()) {
               return ValidationResult.error("Invalid Stair: " + stairResult.error());
            }

            pos += StairConnectedBlockRuleSet.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits & 2) != 0) {
            int roofOffset = buffer.getIntLE(offset + 6);
            if (roofOffset < 0) {
               return ValidationResult.error("Invalid offset for Roof");
            }

            int posx = offset + 10 + roofOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Roof");
            }

            ValidationResult roofResult = RoofConnectedBlockRuleSet.validateStructure(buffer, posx);
            if (!roofResult.isValid()) {
               return ValidationResult.error("Invalid Roof: " + roofResult.error());
            }

            posx += RoofConnectedBlockRuleSet.computeBytesConsumed(buffer, posx);
         }

         return ValidationResult.OK;
      }
   }

   public ConnectedBlockRuleSet clone() {
      ConnectedBlockRuleSet copy = new ConnectedBlockRuleSet();
      copy.type = this.type;
      copy.stair = this.stair != null ? this.stair.clone() : null;
      copy.roof = this.roof != null ? this.roof.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ConnectedBlockRuleSet other)
            ? false
            : Objects.equals(this.type, other.type) && Objects.equals(this.stair, other.stair) && Objects.equals(this.roof, other.roof);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.stair, this.roof);
   }
}
