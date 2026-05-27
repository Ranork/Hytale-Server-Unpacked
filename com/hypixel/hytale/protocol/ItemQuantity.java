package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
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
      if (buf.readableBytes() - offset < 5) {
         throw ProtocolException.bufferTooSmall("ItemQuantity", 5, buf.readableBytes() - offset);
      } else {
         ItemQuantity obj = new ItemQuantity();
         byte nullBits = buf.getByte(offset);
         obj.quantity = buf.getIntLE(offset + 1);
         int pos = offset + 5;
         if ((nullBits & 1) != 0) {
            int itemIdLen = VarInt.peek(buf, pos);
            if (itemIdLen < 0) {
               throw ProtocolException.invalidVarInt("ItemId");
            }

            int itemIdVarLen = VarInt.size(itemIdLen);
            if (itemIdLen > 4096000) {
               throw ProtocolException.stringTooLong("ItemId", itemIdLen, 4096000);
            }

            if (pos + itemIdVarLen + itemIdLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("ItemId", pos + itemIdVarLen + itemIdLen, buf.readableBytes());
            }

            obj.itemId = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
            pos += itemIdVarLen + itemIdLen;
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 5;
      if ((nullBits & 1) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.size(sl) + sl;
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 5L;
   }

   @Nullable
   public static String getItemId(MemorySegment mem) {
      return getItemId(mem, 0);
   }

   @Nullable
   public static String getItemId(MemorySegment mem, int offset) {
      return hasItemId(mem, offset) ? PacketIO.readVarString("ItemId", mem, offset + 5, 4096000, PacketIO.UTF8) : null;
   }

   public static int getQuantity(MemorySegment mem) {
      return getQuantity(mem, 0);
   }

   public static int getQuantity(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   public static boolean hasItemId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static ItemQuantity toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ItemQuantity toObject(MemorySegment mem, int offset) {
      if (offset + 5 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ItemQuantity", offset + 5, (int)mem.byteSize());
      } else {
         return new ItemQuantity(
            hasItemId(mem, offset) ? PacketIO.readVarString("ItemId", mem, offset + 5, 4096000, PacketIO.UTF8) : null,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1))
         );
      }
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

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.itemId != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.quantity);
      int varOffset = offset + 5;
      if (this.itemId != null) {
         varOffset += PacketIO.writeVarString(mem, varOffset, this.itemId, 4096000);
      }

      return varOffset - offset;
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

            pos += VarInt.size(itemIdLen);
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
