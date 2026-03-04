package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WorldInteraction {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 20;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 20;
   public static final int MAX_SIZE = 20;
   public int entityId;
   @Nullable
   public BlockPosition blockPosition;
   @Nullable
   public BlockRotation blockRotation;

   public WorldInteraction() {
   }

   public WorldInteraction(int entityId, @Nullable BlockPosition blockPosition, @Nullable BlockRotation blockRotation) {
      this.entityId = entityId;
      this.blockPosition = blockPosition;
      this.blockRotation = blockRotation;
   }

   public WorldInteraction(@Nonnull WorldInteraction other) {
      this.entityId = other.entityId;
      this.blockPosition = other.blockPosition;
      this.blockRotation = other.blockRotation;
   }

   @Nonnull
   public static WorldInteraction deserialize(@Nonnull ByteBuf buf, int offset) {
      WorldInteraction obj = new WorldInteraction();
      byte nullBits = buf.getByte(offset);
      obj.entityId = buf.getIntLE(offset + 1);
      if ((nullBits & 1) != 0) {
         obj.blockPosition = BlockPosition.deserialize(buf, offset + 5);
      }

      if ((nullBits & 2) != 0) {
         obj.blockRotation = BlockRotation.deserialize(buf, offset + 17);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 20;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.blockPosition != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.blockRotation != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.entityId);
      if (this.blockPosition != null) {
         this.blockPosition.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      if (this.blockRotation != null) {
         this.blockRotation.serialize(buf);
      } else {
         buf.writeZero(3);
      }
   }

   public int computeSize() {
      return 20;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 20 ? ValidationResult.error("Buffer too small: expected at least 20 bytes") : ValidationResult.OK;
   }

   public WorldInteraction clone() {
      WorldInteraction copy = new WorldInteraction();
      copy.entityId = this.entityId;
      copy.blockPosition = this.blockPosition != null ? this.blockPosition.clone() : null;
      copy.blockRotation = this.blockRotation != null ? this.blockRotation.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof WorldInteraction other)
            ? false
            : this.entityId == other.entityId
               && Objects.equals(this.blockPosition, other.blockPosition)
               && Objects.equals(this.blockRotation, other.blockRotation);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.entityId, this.blockPosition, this.blockRotation);
   }
}
