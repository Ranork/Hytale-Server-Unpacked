package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AssetIconProperties {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 25;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 25;
   public static final int MAX_SIZE = 25;
   public float scale;
   @Nullable
   public Vector2f translation;
   @Nullable
   public Vector3f rotation;

   public AssetIconProperties() {
   }

   public AssetIconProperties(float scale, @Nullable Vector2f translation, @Nullable Vector3f rotation) {
      this.scale = scale;
      this.translation = translation;
      this.rotation = rotation;
   }

   public AssetIconProperties(@Nonnull AssetIconProperties other) {
      this.scale = other.scale;
      this.translation = other.translation;
      this.rotation = other.rotation;
   }

   @Nonnull
   public static AssetIconProperties deserialize(@Nonnull ByteBuf buf, int offset) {
      AssetIconProperties obj = new AssetIconProperties();
      byte nullBits = buf.getByte(offset);
      obj.scale = buf.getFloatLE(offset + 1);
      if ((nullBits & 1) != 0) {
         obj.translation = Vector2f.deserialize(buf, offset + 5);
      }

      if ((nullBits & 2) != 0) {
         obj.rotation = Vector3f.deserialize(buf, offset + 13);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 25;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.translation != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.rotation != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeFloatLE(this.scale);
      if (this.translation != null) {
         this.translation.serialize(buf);
      } else {
         buf.writeZero(8);
      }

      if (this.rotation != null) {
         this.rotation.serialize(buf);
      } else {
         buf.writeZero(12);
      }
   }

   public int computeSize() {
      return 25;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 25 ? ValidationResult.error("Buffer too small: expected at least 25 bytes") : ValidationResult.OK;
   }

   public AssetIconProperties clone() {
      AssetIconProperties copy = new AssetIconProperties();
      copy.scale = this.scale;
      copy.translation = this.translation != null ? this.translation.clone() : null;
      copy.rotation = this.rotation != null ? this.rotation.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AssetIconProperties other)
            ? false
            : this.scale == other.scale && Objects.equals(this.translation, other.translation) && Objects.equals(this.rotation, other.rotation);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.scale, this.translation, this.rotation);
   }
}
