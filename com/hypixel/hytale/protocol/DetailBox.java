package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DetailBox {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 37;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 37;
   public static final int MAX_SIZE = 37;
   @Nullable
   public Vector3f offset;
   @Nullable
   public Hitbox box;

   public DetailBox() {
   }

   public DetailBox(@Nullable Vector3f offset, @Nullable Hitbox box) {
      this.offset = offset;
      this.box = box;
   }

   public DetailBox(@Nonnull DetailBox other) {
      this.offset = other.offset;
      this.box = other.box;
   }

   @Nonnull
   public static DetailBox deserialize(@Nonnull ByteBuf buf, int offset) {
      DetailBox obj = new DetailBox();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         obj.offset = Vector3f.deserialize(buf, offset + 1);
      }

      if ((nullBits & 2) != 0) {
         obj.box = Hitbox.deserialize(buf, offset + 13);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 37;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.offset != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.box != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      if (this.offset != null) {
         this.offset.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      if (this.box != null) {
         this.box.serialize(buf);
      } else {
         buf.writeZero(24);
      }
   }

   public int computeSize() {
      return 37;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 37 ? ValidationResult.error("Buffer too small: expected at least 37 bytes") : ValidationResult.OK;
   }

   public DetailBox clone() {
      DetailBox copy = new DetailBox();
      copy.offset = this.offset != null ? this.offset.clone() : null;
      copy.box = this.box != null ? this.box.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof DetailBox other) ? false : Objects.equals(this.offset, other.offset) && Objects.equals(this.box, other.box);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.offset, this.box);
   }
}
