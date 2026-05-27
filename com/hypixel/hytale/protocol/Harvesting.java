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
      if (buf.readableBytes() - offset < 9) {
         throw ProtocolException.bufferTooSmall("Harvesting", 9, buf.readableBytes() - offset);
      } else {
         Harvesting obj = new Harvesting();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 1);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 9) {
               throw ProtocolException.invalidOffset("ItemId", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 9 + varPosBase0;
            int itemIdLen = VarInt.peek(buf, varPos0);
            if (itemIdLen < 0) {
               throw ProtocolException.invalidVarInt("ItemId");
            }

            int itemIdVarIntLen = VarInt.size(itemIdLen);
            if (itemIdLen > 4096000) {
               throw ProtocolException.stringTooLong("ItemId", itemIdLen, 4096000);
            }

            if (varPos0 + itemIdVarIntLen + itemIdLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("ItemId", varPos0 + itemIdVarIntLen + itemIdLen, buf.readableBytes());
            }

            obj.itemId = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 5);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 9) {
               throw ProtocolException.invalidOffset("DropListId", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 9 + varPosBase1;
            int dropListIdLen = VarInt.peek(buf, varPos1);
            if (dropListIdLen < 0) {
               throw ProtocolException.invalidVarInt("DropListId");
            }

            int dropListIdVarIntLen = VarInt.size(dropListIdLen);
            if (dropListIdLen > 4096000) {
               throw ProtocolException.stringTooLong("DropListId", dropListIdLen, 4096000);
            }

            if (varPos1 + dropListIdVarIntLen + dropListIdLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("DropListId", varPos1 + dropListIdVarIntLen + dropListIdLen, buf.readableBytes());
            }

            obj.dropListId = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 9;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 9) {
            throw ProtocolException.invalidOffset("ItemId", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 9 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 9) {
            throw ProtocolException.invalidOffset("DropListId", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 9 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(sl) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 9L;
   }

   @Nullable
   public static String getItemId(MemorySegment mem) {
      return getItemId(mem, 0);
   }

   @Nullable
   public static String getItemId(MemorySegment mem, int offset) {
      return hasItemId(mem, offset)
         ? PacketIO.readVarString("ItemId", mem, offset + getValidatedOffset(mem, offset, 1, 9, "ItemId"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static String getDropListId(MemorySegment mem) {
      return getDropListId(mem, 0);
   }

   @Nullable
   public static String getDropListId(MemorySegment mem, int offset) {
      return hasDropListId(mem, offset)
         ? PacketIO.readVarString("DropListId", mem, offset + getValidatedOffset(mem, offset, 5, 9, "DropListId"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static boolean hasItemId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasDropListId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static Harvesting toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static Harvesting toObject(MemorySegment mem, int offset) {
      if (offset + 9 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("Harvesting", offset + 9, (int)mem.byteSize());
      } else {
         return new Harvesting(
            hasItemId(mem, offset)
               ? PacketIO.readVarString("ItemId", mem, offset + getValidatedOffset(mem, offset, 1, 9, "ItemId"), 4096000, PacketIO.UTF8)
               : null,
            hasDropListId(mem, offset)
               ? PacketIO.readVarString("DropListId", mem, offset + getValidatedOffset(mem, offset, 5, 9, "DropListId"), 4096000, PacketIO.UTF8)
               : null
         );
      }
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

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.itemId != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.dropListId != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 9;
      if (this.itemId != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), varOffset - offset - 9);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.itemId, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), -1);
      }

      if (this.dropListId != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), varOffset - offset - 9);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.dropListId, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), -1);
      }

      return varOffset - offset;
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
            if (itemIdOffset < 0 || itemIdOffset > buffer.writerIndex() - offset - 9) {
               return ValidationResult.error("Invalid offset for ItemId");
            }

            int pos = offset + 9 + itemIdOffset;
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

         if ((nullBits & 2) != 0) {
            int dropListIdOffset = buffer.getIntLE(offset + 5);
            if (dropListIdOffset < 0 || dropListIdOffset > buffer.writerIndex() - offset - 9) {
               return ValidationResult.error("Invalid offset for DropListId");
            }

            int posx = offset + 9 + dropListIdOffset;
            int dropListIdLen = VarInt.peek(buffer, posx);
            if (dropListIdLen < 0) {
               return ValidationResult.error("Invalid string length for DropListId");
            }

            if (dropListIdLen > 4096000) {
               return ValidationResult.error("DropListId exceeds max length 4096000");
            }

            posx += VarInt.size(dropListIdLen);
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
