package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemQuantity {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 5;
   public static final int MAX_SIZE = 16384010;
   @Nullable
   public String itemId;
   public int quantity;

   public ItemQuantity() {
   }

   public ItemQuantity(@Nullable String itemId, int quantity) {
      this.itemId = itemId;
      this.quantity = quantity;
   }

   public ItemQuantity(@Nonnull ItemQuantity other) {
      this.itemId = other.itemId;
      this.quantity = other.quantity;
   }

   @Nonnull
   public static ItemQuantity deserialize(@Nonnull ByteBuf buf, int offset) {
      ItemQuantity obj = new ItemQuantity();
      byte nullBits = buf.getByte(offset);
      obj.quantity = buf.getIntLE(offset + 1);
      int pos = offset + 5;
      if ((nullBits & 1) != 0) {
         int itemIdLen = VarInt.peek(buf, pos);
         if (itemIdLen < 0) {
            throw ProtocolException.negativeLength("ItemId", itemIdLen);
         }

         if (itemIdLen > 4096000) {
            throw ProtocolException.stringTooLong("ItemId", itemIdLen, 4096000);
         }

         int itemIdVarLen = VarInt.length(buf, pos);
         obj.itemId = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
         pos += itemIdVarLen + itemIdLen;
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 5;
      if ((nullBits & 1) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos) + sl;
      }

      return pos - offset;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.itemId != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.quantity);
      if (this.itemId != null) {
         PacketIO.writeVarString(buf, this.itemId, 4096000);
      }
   }

   public int computeSize() {
      int size = 5;
      if (this.itemId != null) {
         size += PacketIO.stringSize(this.itemId);
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
            int itemIdLen = VarInt.peek(buffer, pos);
            if (itemIdLen < 0) {
               return ValidationResult.error("Invalid string length for ItemId");
            }

            if (itemIdLen > 4096000) {
               return ValidationResult.error("ItemId exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += itemIdLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading ItemId");
            }
         }

         return ValidationResult.OK;
      }
   }

   public ItemQuantity clone() {
      ItemQuantity copy = new ItemQuantity();
      copy.itemId = this.itemId;
      copy.quantity = this.quantity;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ItemQuantity other) ? false : Objects.equals(this.itemId, other.itemId) && this.quantity == other.quantity;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.itemId, this.quantity);
   }
}
