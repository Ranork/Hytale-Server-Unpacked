package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ExtraResources {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public ItemQuantity[] resources;

   public ExtraResources() {
   }

   public ExtraResources(@Nullable ItemQuantity[] resources) {
      this.resources = resources;
   }

   public ExtraResources(@Nonnull ExtraResources other) {
      this.resources = other.resources;
   }

   @Nonnull
   public static ExtraResources deserialize(@Nonnull ByteBuf buf, int offset) {
      ExtraResources obj = new ExtraResources();
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int resourcesCount = VarInt.peek(buf, pos);
         if (resourcesCount < 0) {
            throw ProtocolException.negativeLength("Resources", resourcesCount);
         }

         if (resourcesCount > 4096000) {
            throw ProtocolException.arrayTooLong("Resources", resourcesCount, 4096000);
         }

         int resourcesVarLen = VarInt.size(resourcesCount);
         if (pos + resourcesVarLen + resourcesCount * 5L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Resources", pos + resourcesVarLen + resourcesCount * 5, buf.readableBytes());
         }

         pos += resourcesVarLen;
         obj.resources = new ItemQuantity[resourcesCount];

         for (int i = 0; i < resourcesCount; i++) {
            obj.resources[i] = ItemQuantity.deserialize(buf, pos);
            pos += ItemQuantity.computeBytesConsumed(buf, pos);
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos);

         for (int i = 0; i < arrLen; i++) {
            pos += ItemQuantity.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.resources != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.resources != null) {
         if (this.resources.length > 4096000) {
            throw ProtocolException.arrayTooLong("Resources", this.resources.length, 4096000);
         }

         VarInt.write(buf, this.resources.length);

         for (ItemQuantity item : this.resources) {
            item.serialize(buf);
         }
      }
   }

   public int computeSize() {
      int size = 1;
      if (this.resources != null) {
         int resourcesSize = 0;

         for (ItemQuantity elem : this.resources) {
            resourcesSize += elem.computeSize();
         }

         size += VarInt.size(this.resources.length) + resourcesSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 1) {
         return ValidationResult.error("Buffer too small: expected at least 1 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            int resourcesCount = VarInt.peek(buffer, pos);
            if (resourcesCount < 0) {
               return ValidationResult.error("Invalid array count for Resources");
            }

            if (resourcesCount > 4096000) {
               return ValidationResult.error("Resources exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for (int i = 0; i < resourcesCount; i++) {
               ValidationResult structResult = ItemQuantity.validateStructure(buffer, pos);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid ItemQuantity in Resources[" + i + "]: " + structResult.error());
               }

               pos += ItemQuantity.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
      }
   }

   public ExtraResources clone() {
      ExtraResources copy = new ExtraResources();
      copy.resources = this.resources != null ? Arrays.stream(this.resources).map(e -> e.clone()).toArray(ItemQuantity[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof ExtraResources other ? Arrays.equals((Object[])this.resources, (Object[])other.resources) : false;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      return 31 * result + Arrays.hashCode((Object[])this.resources);
   }
}
