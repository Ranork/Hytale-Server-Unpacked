package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class ItemUpdate extends ComponentUpdate {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 4;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 4;
   public static final int MAX_SIZE = 32768044;
   @Nonnull
   public ItemWithAllMetadata item = new ItemWithAllMetadata();
   public float entityScale;

   public ItemUpdate() {
   }

   public ItemUpdate(@Nonnull ItemWithAllMetadata item, float entityScale) {
      this.item = item;
      this.entityScale = entityScale;
   }

   public ItemUpdate(@Nonnull ItemUpdate other) {
      this.item = other.item;
      this.entityScale = other.entityScale;
   }

   @Nonnull
   public static ItemUpdate deserialize(@Nonnull ByteBuf buf, int offset) {
      ItemUpdate obj = new ItemUpdate();
      obj.entityScale = buf.getFloatLE(offset + 0);
      int pos = offset + 4;
      obj.item = ItemWithAllMetadata.deserialize(buf, pos);
      pos += ItemWithAllMetadata.computeBytesConsumed(buf, pos);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      int pos = offset + 4;
      pos += ItemWithAllMetadata.computeBytesConsumed(buf, pos);
      return pos - offset;
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      buf.writeFloatLE(this.entityScale);
      this.item.serialize(buf);
      return buf.writerIndex() - startPos;
   }

   @Override
   public int computeSize() {
      int size = 4;
      return size + this.item.computeSize();
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 4) {
         return ValidationResult.error("Buffer too small: expected at least 4 bytes");
      } else {
         int pos = offset + 4;
         ValidationResult itemResult = ItemWithAllMetadata.validateStructure(buffer, pos);
         if (!itemResult.isValid()) {
            return ValidationResult.error("Invalid Item: " + itemResult.error());
         } else {
            pos += ItemWithAllMetadata.computeBytesConsumed(buffer, pos);
            return ValidationResult.OK;
         }
      }
   }

   public ItemUpdate clone() {
      ItemUpdate copy = new ItemUpdate();
      copy.item = this.item.clone();
      copy.entityScale = this.entityScale;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ItemUpdate other) ? false : Objects.equals(this.item, other.item) && this.entityScale == other.entityScale;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.item, this.entityScale);
   }
}
