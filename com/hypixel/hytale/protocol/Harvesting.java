package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Harvesting {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 32768019;
   @Nullable
   public String itemId;
   @Nullable
   public String dropListId;

   public Harvesting() {
   }

   public Harvesting(@Nullable String itemId, @Nullable String dropListId) {
      this.itemId = itemId;
      this.dropListId = dropListId;
   }

   public Harvesting(@Nonnull Harvesting other) {
      this.itemId = other.itemId;
      this.dropListId = other.dropListId;
   }

   @Nonnull
   public static Harvesting deserialize(@Nonnull ByteBuf buf, int offset) {
      Harvesting obj = new Harvesting();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 9 + buf.getIntLE(offset + 1);
         int itemIdLen = VarInt.peek(buf, varPos0);
         if (itemIdLen < 0) {
            throw ProtocolException.negativeLength("ItemId", itemIdLen);
         }

         if (itemIdLen > 4096000) {
            throw ProtocolException.stringTooLong("ItemId", itemIdLen, 4096000);
         }

         obj.itemId = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 9 + buf.getIntLE(offset + 5);
         int dropListIdLen = VarInt.peek(buf, varPos1);
         if (dropListIdLen < 0) {
            throw ProtocolException.negativeLength("DropListId", dropListIdLen);
         }

         if (dropListIdLen > 4096000) {
            throw ProtocolException.stringTooLong("DropListId", dropListIdLen, 4096000);
         }

         obj.dropListId = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 9;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         int pos0 = offset + 9 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         int pos1 = offset + 9 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.itemId != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.dropListId != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      int itemIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int dropListIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.itemId != null) {
         buf.setIntLE(itemIdOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.itemId, 4096000);
      } else {
         buf.setIntLE(itemIdOffsetSlot, -1);
      }

      if (this.dropListId != null) {
         buf.setIntLE(dropListIdOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.dropListId, 4096000);
      } else {
         buf.setIntLE(dropListIdOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 9;
      if (this.itemId != null) {
         size += PacketIO.stringSize(this.itemId);
      }

      if (this.dropListId != null) {
         size += PacketIO.stringSize(this.dropListId);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 9) {
         return ValidationResult.error("Buffer too small: expected at least 9 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int itemIdOffset = buffer.getIntLE(offset + 1);
            if (itemIdOffset < 0) {
               return ValidationResult.error("Invalid offset for ItemId");
            }

            int pos = offset + 9 + itemIdOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ItemId");
            }

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

         if ((nullBits & 2) != 0) {
            int dropListIdOffset = buffer.getIntLE(offset + 5);
            if (dropListIdOffset < 0) {
               return ValidationResult.error("Invalid offset for DropListId");
            }

            int posx = offset + 9 + dropListIdOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for DropListId");
            }

            int dropListIdLen = VarInt.peek(buffer, posx);
            if (dropListIdLen < 0) {
               return ValidationResult.error("Invalid string length for DropListId");
            }

            if (dropListIdLen > 4096000) {
               return ValidationResult.error("DropListId exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
            posx += dropListIdLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading DropListId");
            }
         }

         return ValidationResult.OK;
      }
   }

   public Harvesting clone() {
      Harvesting copy = new Harvesting();
      copy.itemId = this.itemId;
      copy.dropListId = this.dropListId;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof Harvesting other) ? false : Objects.equals(this.itemId, other.itemId) && Objects.equals(this.dropListId, other.dropListId);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.itemId, this.dropListId);
   }
}
