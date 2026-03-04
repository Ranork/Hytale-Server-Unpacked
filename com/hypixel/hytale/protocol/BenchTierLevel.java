package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BenchTierLevel {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 17;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 17;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public BenchUpgradeRequirement benchUpgradeRequirement;
   public double craftingTimeReductionModifier;
   public int extraInputSlot;
   public int extraOutputSlot;

   public BenchTierLevel() {
   }

   public BenchTierLevel(
      @Nullable BenchUpgradeRequirement benchUpgradeRequirement, double craftingTimeReductionModifier, int extraInputSlot, int extraOutputSlot
   ) {
      this.benchUpgradeRequirement = benchUpgradeRequirement;
      this.craftingTimeReductionModifier = craftingTimeReductionModifier;
      this.extraInputSlot = extraInputSlot;
      this.extraOutputSlot = extraOutputSlot;
   }

   public BenchTierLevel(@Nonnull BenchTierLevel other) {
      this.benchUpgradeRequirement = other.benchUpgradeRequirement;
      this.craftingTimeReductionModifier = other.craftingTimeReductionModifier;
      this.extraInputSlot = other.extraInputSlot;
      this.extraOutputSlot = other.extraOutputSlot;
   }

   @Nonnull
   public static BenchTierLevel deserialize(@Nonnull ByteBuf buf, int offset) {
      BenchTierLevel obj = new BenchTierLevel();
      byte nullBits = buf.getByte(offset);
      obj.craftingTimeReductionModifier = buf.getDoubleLE(offset + 1);
      obj.extraInputSlot = buf.getIntLE(offset + 9);
      obj.extraOutputSlot = buf.getIntLE(offset + 13);
      int pos = offset + 17;
      if ((nullBits & 1) != 0) {
         obj.benchUpgradeRequirement = BenchUpgradeRequirement.deserialize(buf, pos);
         pos += BenchUpgradeRequirement.computeBytesConsumed(buf, pos);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 17;
      if ((nullBits & 1) != 0) {
         pos += BenchUpgradeRequirement.computeBytesConsumed(buf, pos);
      }

      return pos - offset;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.benchUpgradeRequirement != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeDoubleLE(this.craftingTimeReductionModifier);
      buf.writeIntLE(this.extraInputSlot);
      buf.writeIntLE(this.extraOutputSlot);
      if (this.benchUpgradeRequirement != null) {
         this.benchUpgradeRequirement.serialize(buf);
      }
   }

   public int computeSize() {
      int size = 17;
      if (this.benchUpgradeRequirement != null) {
         size += this.benchUpgradeRequirement.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 17) {
         return ValidationResult.error("Buffer too small: expected at least 17 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 17;
         if ((nullBits & 1) != 0) {
            ValidationResult benchUpgradeRequirementResult = BenchUpgradeRequirement.validateStructure(buffer, pos);
            if (!benchUpgradeRequirementResult.isValid()) {
               return ValidationResult.error("Invalid BenchUpgradeRequirement: " + benchUpgradeRequirementResult.error());
            }

            pos += BenchUpgradeRequirement.computeBytesConsumed(buffer, pos);
         }

         return ValidationResult.OK;
      }
   }

   public BenchTierLevel clone() {
      BenchTierLevel copy = new BenchTierLevel();
      copy.benchUpgradeRequirement = this.benchUpgradeRequirement != null ? this.benchUpgradeRequirement.clone() : null;
      copy.craftingTimeReductionModifier = this.craftingTimeReductionModifier;
      copy.extraInputSlot = this.extraInputSlot;
      copy.extraOutputSlot = this.extraOutputSlot;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BenchTierLevel other)
            ? false
            : Objects.equals(this.benchUpgradeRequirement, other.benchUpgradeRequirement)
               && this.craftingTimeReductionModifier == other.craftingTimeReductionModifier
               && this.extraInputSlot == other.extraInputSlot
               && this.extraOutputSlot == other.extraOutputSlot;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.benchUpgradeRequirement, this.craftingTimeReductionModifier, this.extraInputSlot, this.extraOutputSlot);
   }
}
