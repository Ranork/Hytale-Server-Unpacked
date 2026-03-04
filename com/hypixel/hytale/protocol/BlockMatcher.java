package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockMatcher {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 3;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 3;
   public static final int MAX_SIZE = 32768026;
   @Nullable
   public BlockIdMatcher block;
   @Nonnull
   public BlockFace face = BlockFace.None;
   public boolean staticFace;

   public BlockMatcher() {
   }

   public BlockMatcher(@Nullable BlockIdMatcher block, @Nonnull BlockFace face, boolean staticFace) {
      this.block = block;
      this.face = face;
      this.staticFace = staticFace;
   }

   public BlockMatcher(@Nonnull BlockMatcher other) {
      this.block = other.block;
      this.face = other.face;
      this.staticFace = other.staticFace;
   }

   @Nonnull
   public static BlockMatcher deserialize(@Nonnull ByteBuf buf, int offset) {
      BlockMatcher obj = new BlockMatcher();
      byte nullBits = buf.getByte(offset);
      obj.face = BlockFace.fromValue(buf.getByte(offset + 1));
      obj.staticFace = buf.getByte(offset + 2) != 0;
      int pos = offset + 3;
      if ((nullBits & 1) != 0) {
         obj.block = BlockIdMatcher.deserialize(buf, pos);
         pos += BlockIdMatcher.computeBytesConsumed(buf, pos);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 3;
      if ((nullBits & 1) != 0) {
         pos += BlockIdMatcher.computeBytesConsumed(buf, pos);
      }

      return pos - offset;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.block != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.face.getValue());
      buf.writeByte(this.staticFace ? 1 : 0);
      if (this.block != null) {
         this.block.serialize(buf);
      }
   }

   public int computeSize() {
      int size = 3;
      if (this.block != null) {
         size += this.block.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 3) {
         return ValidationResult.error("Buffer too small: expected at least 3 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 3;
         if ((nullBits & 1) != 0) {
            ValidationResult blockResult = BlockIdMatcher.validateStructure(buffer, pos);
            if (!blockResult.isValid()) {
               return ValidationResult.error("Invalid Block: " + blockResult.error());
            }

            pos += BlockIdMatcher.computeBytesConsumed(buffer, pos);
         }

         return ValidationResult.OK;
      }
   }

   public BlockMatcher clone() {
      BlockMatcher copy = new BlockMatcher();
      copy.block = this.block != null ? this.block.clone() : null;
      copy.face = this.face;
      copy.staticFace = this.staticFace;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BlockMatcher other)
            ? false
            : Objects.equals(this.block, other.block) && Objects.equals(this.face, other.face) && this.staticFace == other.staticFace;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.block, this.face, this.staticFace);
   }
}
