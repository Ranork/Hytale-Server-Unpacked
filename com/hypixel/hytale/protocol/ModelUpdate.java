package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ModelUpdate extends ComponentUpdate {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 5;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public Model model;
   public float entityScale;

   public ModelUpdate() {
   }

   public ModelUpdate(@Nullable Model model, float entityScale) {
      this.model = model;
      this.entityScale = entityScale;
   }

   public ModelUpdate(@Nonnull ModelUpdate other) {
      this.model = other.model;
      this.entityScale = other.entityScale;
   }

   @Nonnull
   public static ModelUpdate deserialize(@Nonnull ByteBuf buf, int offset) {
      ModelUpdate obj = new ModelUpdate();
      byte nullBits = buf.getByte(offset);
      obj.entityScale = buf.getFloatLE(offset + 1);
      int pos = offset + 5;
      if ((nullBits & 1) != 0) {
         obj.model = Model.deserialize(buf, pos);
         pos += Model.computeBytesConsumed(buf, pos);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 5;
      if ((nullBits & 1) != 0) {
         pos += Model.computeBytesConsumed(buf, pos);
      }

      return pos - offset;
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.model != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeFloatLE(this.entityScale);
      if (this.model != null) {
         this.model.serialize(buf);
      }

      return buf.writerIndex() - startPos;
   }

   @Override
   public int computeSize() {
      int size = 5;
      if (this.model != null) {
         size += this.model.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 5) {
         return ValidationResult.error("Buffer too small: expected at least 5 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 5;
         if ((nullBits & 1) != 0) {
            ValidationResult modelResult = Model.validateStructure(buffer, pos);
            if (!modelResult.isValid()) {
               return ValidationResult.error("Invalid Model: " + modelResult.error());
            }

            pos += Model.computeBytesConsumed(buffer, pos);
         }

         return ValidationResult.OK;
      }
   }

   public ModelUpdate clone() {
      ModelUpdate copy = new ModelUpdate();
      copy.model = this.model != null ? this.model.clone() : null;
      copy.entityScale = this.entityScale;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ModelUpdate other) ? false : Objects.equals(this.model, other.model) && this.entityScale == other.entityScale;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.model, this.entityScale);
   }
}
