package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemPullbackConfiguration {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 49;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 49;
   public static final int MAX_SIZE = 49;
   @Nullable
   public Vector3f leftOffsetOverride;
   @Nullable
   public Vector3f leftRotationOverride;
   @Nullable
   public Vector3f rightOffsetOverride;
   @Nullable
   public Vector3f rightRotationOverride;

   public ItemPullbackConfiguration() {
   }

   public ItemPullbackConfiguration(
      @Nullable Vector3f leftOffsetOverride,
      @Nullable Vector3f leftRotationOverride,
      @Nullable Vector3f rightOffsetOverride,
      @Nullable Vector3f rightRotationOverride
   ) {
      this.leftOffsetOverride = leftOffsetOverride;
      this.leftRotationOverride = leftRotationOverride;
      this.rightOffsetOverride = rightOffsetOverride;
      this.rightRotationOverride = rightRotationOverride;
   }

   public ItemPullbackConfiguration(@Nonnull ItemPullbackConfiguration other) {
      this.leftOffsetOverride = other.leftOffsetOverride;
      this.leftRotationOverride = other.leftRotationOverride;
      this.rightOffsetOverride = other.rightOffsetOverride;
      this.rightRotationOverride = other.rightRotationOverride;
   }

   @Nonnull
   public static ItemPullbackConfiguration deserialize(@Nonnull ByteBuf buf, int offset) {
      ItemPullbackConfiguration obj = new ItemPullbackConfiguration();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         obj.leftOffsetOverride = Vector3f.deserialize(buf, offset + 1);
      }

      if ((nullBits & 2) != 0) {
         obj.leftRotationOverride = Vector3f.deserialize(buf, offset + 13);
      }

      if ((nullBits & 4) != 0) {
         obj.rightOffsetOverride = Vector3f.deserialize(buf, offset + 25);
      }

      if ((nullBits & 8) != 0) {
         obj.rightRotationOverride = Vector3f.deserialize(buf, offset + 37);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 49;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.leftOffsetOverride != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.leftRotationOverride != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.rightOffsetOverride != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.rightRotationOverride != null) {
         nullBits = (byte)(nullBits | 8);
      }

      buf.writeByte(nullBits);
      if (this.leftOffsetOverride != null) {
         this.leftOffsetOverride.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      if (this.leftRotationOverride != null) {
         this.leftRotationOverride.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      if (this.rightOffsetOverride != null) {
         this.rightOffsetOverride.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      if (this.rightRotationOverride != null) {
         this.rightRotationOverride.serialize(buf);
      } else {
         buf.writeZero(12);
      }
   }

   public int computeSize() {
      return 49;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 49 ? ValidationResult.error("Buffer too small: expected at least 49 bytes") : ValidationResult.OK;
   }

   public ItemPullbackConfiguration clone() {
      ItemPullbackConfiguration copy = new ItemPullbackConfiguration();
      copy.leftOffsetOverride = this.leftOffsetOverride != null ? this.leftOffsetOverride.clone() : null;
      copy.leftRotationOverride = this.leftRotationOverride != null ? this.leftRotationOverride.clone() : null;
      copy.rightOffsetOverride = this.rightOffsetOverride != null ? this.rightOffsetOverride.clone() : null;
      copy.rightRotationOverride = this.rightRotationOverride != null ? this.rightRotationOverride.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ItemPullbackConfiguration other)
            ? false
            : Objects.equals(this.leftOffsetOverride, other.leftOffsetOverride)
               && Objects.equals(this.leftRotationOverride, other.leftRotationOverride)
               && Objects.equals(this.rightOffsetOverride, other.rightOffsetOverride)
               && Objects.equals(this.rightRotationOverride, other.rightRotationOverride);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.leftOffsetOverride, this.leftRotationOverride, this.rightOffsetOverride, this.rightRotationOverride);
   }
}
