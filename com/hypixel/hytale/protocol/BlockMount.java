package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockMount {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 30;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 30;
   public static final int MAX_SIZE = 30;
   @Nonnull
   public BlockMountType type = BlockMountType.Seat;
   @Nullable
   public Vector3f position;
   @Nullable
   public Vector3f orientation;
   public int blockTypeId;

   public BlockMount() {
   }

   public BlockMount(@Nonnull BlockMountType type, @Nullable Vector3f position, @Nullable Vector3f orientation, int blockTypeId) {
      this.type = type;
      this.position = position;
      this.orientation = orientation;
      this.blockTypeId = blockTypeId;
   }

   public BlockMount(@Nonnull BlockMount other) {
      this.type = other.type;
      this.position = other.position;
      this.orientation = other.orientation;
      this.blockTypeId = other.blockTypeId;
   }

   @Nonnull
   public static BlockMount deserialize(@Nonnull ByteBuf buf, int offset) {
      BlockMount obj = new BlockMount();
      byte nullBits = buf.getByte(offset);
      obj.type = BlockMountType.fromValue(buf.getByte(offset + 1));
      if ((nullBits & 1) != 0) {
         obj.position = Vector3f.deserialize(buf, offset + 2);
      }

      if ((nullBits & 2) != 0) {
         obj.orientation = Vector3f.deserialize(buf, offset + 14);
      }

      obj.blockTypeId = buf.getIntLE(offset + 26);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 30;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.position != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.orientation != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      if (this.position != null) {
         this.position.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      if (this.orientation != null) {
         this.orientation.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      buf.writeIntLE(this.blockTypeId);
   }

   public int computeSize() {
      return 30;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 30 ? ValidationResult.error("Buffer too small: expected at least 30 bytes") : ValidationResult.OK;
   }

   public BlockMount clone() {
      BlockMount copy = new BlockMount();
      copy.type = this.type;
      copy.position = this.position != null ? this.position.clone() : null;
      copy.orientation = this.orientation != null ? this.orientation.clone() : null;
      copy.blockTypeId = this.blockTypeId;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BlockMount other)
            ? false
            : Objects.equals(this.type, other.type)
               && Objects.equals(this.position, other.position)
               && Objects.equals(this.orientation, other.orientation)
               && this.blockTypeId == other.blockTypeId;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.position, this.orientation, this.blockTypeId);
   }
}
