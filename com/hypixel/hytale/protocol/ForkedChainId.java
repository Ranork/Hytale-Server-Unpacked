package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ForkedChainId {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 9;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 1033;
   public int entryIndex;
   public int subIndex;
   @Nullable
   public ForkedChainId forkedId;

   public ForkedChainId() {
   }

   public ForkedChainId(int entryIndex, int subIndex, @Nullable ForkedChainId forkedId) {
      this.entryIndex = entryIndex;
      this.subIndex = subIndex;
      this.forkedId = forkedId;
   }

   public ForkedChainId(@Nonnull ForkedChainId other) {
      this.entryIndex = other.entryIndex;
      this.subIndex = other.subIndex;
      this.forkedId = other.forkedId;
   }

   @Nonnull
   public static ForkedChainId deserialize(@Nonnull ByteBuf buf, int offset) {
      ForkedChainId obj = new ForkedChainId();
      byte nullBits = buf.getByte(offset);
      obj.entryIndex = buf.getIntLE(offset + 1);
      obj.subIndex = buf.getIntLE(offset + 5);
      int pos = offset + 9;
      if ((nullBits & 1) != 0) {
         obj.forkedId = deserialize(buf, pos);
         pos += computeBytesConsumed(buf, pos);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 9;
      if ((nullBits & 1) != 0) {
         pos += computeBytesConsumed(buf, pos);
      }

      return pos - offset;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.forkedId != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.entryIndex);
      buf.writeIntLE(this.subIndex);
      if (this.forkedId != null) {
         this.forkedId.serialize(buf);
      }
   }

   public int computeSize() {
      int size = 9;
      if (this.forkedId != null) {
         size += this.forkedId.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 9) {
         return ValidationResult.error("Buffer too small: expected at least 9 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 9;
         if ((nullBits & 1) != 0) {
            ValidationResult forkedIdResult = validateStructure(buffer, pos);
            if (!forkedIdResult.isValid()) {
               return ValidationResult.error("Invalid ForkedId: " + forkedIdResult.error());
            }

            pos += computeBytesConsumed(buffer, pos);
         }

         return ValidationResult.OK;
      }
   }

   public ForkedChainId clone() {
      ForkedChainId copy = new ForkedChainId();
      copy.entryIndex = this.entryIndex;
      copy.subIndex = this.subIndex;
      copy.forkedId = this.forkedId != null ? this.forkedId.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ForkedChainId other)
            ? false
            : this.entryIndex == other.entryIndex && this.subIndex == other.subIndex && Objects.equals(this.forkedId, other.forkedId);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.entryIndex, this.subIndex, this.forkedId);
   }
}
