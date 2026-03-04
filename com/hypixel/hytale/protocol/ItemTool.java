package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemTool {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 5;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public ItemToolSpec[] specs;
   public float speed;

   public ItemTool() {
   }

   public ItemTool(@Nullable ItemToolSpec[] specs, float speed) {
      this.specs = specs;
      this.speed = speed;
   }

   public ItemTool(@Nonnull ItemTool other) {
      this.specs = other.specs;
      this.speed = other.speed;
   }

   @Nonnull
   public static ItemTool deserialize(@Nonnull ByteBuf buf, int offset) {
      ItemTool obj = new ItemTool();
      byte nullBits = buf.getByte(offset);
      obj.speed = buf.getFloatLE(offset + 1);
      int pos = offset + 5;
      if ((nullBits & 1) != 0) {
         int specsCount = VarInt.peek(buf, pos);
         if (specsCount < 0) {
            throw ProtocolException.negativeLength("Specs", specsCount);
         }

         if (specsCount > 4096000) {
            throw ProtocolException.arrayTooLong("Specs", specsCount, 4096000);
         }

         int specsVarLen = VarInt.size(specsCount);
         if (pos + specsVarLen + specsCount * 9L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Specs", pos + specsVarLen + specsCount * 9, buf.readableBytes());
         }

         pos += specsVarLen;
         obj.specs = new ItemToolSpec[specsCount];

         for (int i = 0; i < specsCount; i++) {
            obj.specs[i] = ItemToolSpec.deserialize(buf, pos);
            pos += ItemToolSpec.computeBytesConsumed(buf, pos);
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 5;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos);

         for (int i = 0; i < arrLen; i++) {
            pos += ItemToolSpec.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.specs != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeFloatLE(this.speed);
      if (this.specs != null) {
         if (this.specs.length > 4096000) {
            throw ProtocolException.arrayTooLong("Specs", this.specs.length, 4096000);
         }

         VarInt.write(buf, this.specs.length);

         for (ItemToolSpec item : this.specs) {
            item.serialize(buf);
         }
      }
   }

   public int computeSize() {
      int size = 5;
      if (this.specs != null) {
         int specsSize = 0;

         for (ItemToolSpec elem : this.specs) {
            specsSize += elem.computeSize();
         }

         size += VarInt.size(this.specs.length) + specsSize;
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
            int specsCount = VarInt.peek(buffer, pos);
            if (specsCount < 0) {
               return ValidationResult.error("Invalid array count for Specs");
            }

            if (specsCount > 4096000) {
               return ValidationResult.error("Specs exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for (int i = 0; i < specsCount; i++) {
               ValidationResult structResult = ItemToolSpec.validateStructure(buffer, pos);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid ItemToolSpec in Specs[" + i + "]: " + structResult.error());
               }

               pos += ItemToolSpec.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
      }
   }

   public ItemTool clone() {
      ItemTool copy = new ItemTool();
      copy.specs = this.specs != null ? Arrays.stream(this.specs).map(e -> e.clone()).toArray(ItemToolSpec[]::new) : null;
      copy.speed = this.speed;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ItemTool other) ? false : Arrays.equals((Object[])this.specs, (Object[])other.specs) && this.speed == other.speed;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Arrays.hashCode((Object[])this.specs);
      return 31 * result + Float.hashCode(this.speed);
   }
}
