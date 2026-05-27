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

public class ItemWithAllMetadata {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 22;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 30;
   public static final int MAX_SIZE = 32768040;
   @Nonnull
   public String itemId = "";
   public int quantity;
   public double durability;
   public double maxDurability;
   public boolean overrideDroppedItemAnimation;
   @Nullable
   public String metadata;

   public ItemWithAllMetadata() {
   }

   public ItemWithAllMetadata(
      @Nonnull String itemId, int quantity, double durability, double maxDurability, boolean overrideDroppedItemAnimation, @Nullable String metadata
   ) {
      this.itemId = itemId;
      this.quantity = quantity;
      this.durability = durability;
      this.maxDurability = maxDurability;
      this.overrideDroppedItemAnimation = overrideDroppedItemAnimation;
      this.metadata = metadata;
   }

   public ItemWithAllMetadata(@Nonnull ItemWithAllMetadata other) {
      this.itemId = other.itemId;
      this.quantity = other.quantity;
      this.durability = other.durability;
      this.maxDurability = other.maxDurability;
      this.overrideDroppedItemAnimation = other.overrideDroppedItemAnimation;
      this.metadata = other.metadata;
   }

   @Nonnull
   public static ItemWithAllMetadata deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 30) {
         throw ProtocolException.bufferTooSmall("ItemWithAllMetadata", 30, buf.readableBytes() - offset);
      } else {
         ItemWithAllMetadata obj = new ItemWithAllMetadata();
         byte nullBits = buf.getByte(offset);
         obj.quantity = buf.getIntLE(offset + 1);
         obj.durability = buf.getDoubleLE(offset + 5);
         obj.maxDurability = buf.getDoubleLE(offset + 13);
         obj.overrideDroppedItemAnimation = buf.getByte(offset + 21) != 0;
         int varPosBase0 = buf.getIntLE(offset + 22);
         if (varPosBase0 >= 0 && varPosBase0 <= buf.writerIndex() - offset - 30) {
            int varPos0 = offset + 30 + varPosBase0;
            int itemIdLen = VarInt.peek(buf, varPos0);
            if (itemIdLen < 0) {
               throw ProtocolException.invalidVarInt("ItemId");
            } else {
               int itemIdVarIntLen = VarInt.size(itemIdLen);
               if (itemIdLen > 4096000) {
                  throw ProtocolException.stringTooLong("ItemId", itemIdLen, 4096000);
               } else if (varPos0 + itemIdVarIntLen + itemIdLen > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("ItemId", varPos0 + itemIdVarIntLen + itemIdLen, buf.readableBytes());
               } else {
                  obj.itemId = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
                  if ((nullBits & 1) != 0) {
                     varPosBase0 = buf.getIntLE(offset + 26);
                     if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 30) {
                        throw ProtocolException.invalidOffset("Metadata", varPosBase0, buf.readableBytes());
                     }

                     varPos0 = offset + 30 + varPosBase0;
                     itemIdLen = VarInt.peek(buf, varPos0);
                     if (itemIdLen < 0) {
                        throw ProtocolException.invalidVarInt("Metadata");
                     }

                     itemIdVarIntLen = VarInt.size(itemIdLen);
                     if (itemIdLen > 4096000) {
                        throw ProtocolException.stringTooLong("Metadata", itemIdLen, 4096000);
                     }

                     if (varPos0 + itemIdVarIntLen + itemIdLen > buf.readableBytes()) {
                        throw ProtocolException.bufferTooSmall("Metadata", varPos0 + itemIdVarIntLen + itemIdLen, buf.readableBytes());
                     }

                     obj.metadata = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
                  }

                  return obj;
               }
            }
         } else {
            throw ProtocolException.invalidOffset("ItemId", varPosBase0, buf.readableBytes());
         }
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 30;
      int fieldOffset0 = buf.getIntLE(offset + 22);
      if (fieldOffset0 >= 0 && fieldOffset0 <= buf.writerIndex() - offset - 30) {
         int pos0 = offset + 30 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }

         if ((nullBits & 1) != 0) {
            fieldOffset0 = buf.getIntLE(offset + 26);
            if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 30) {
               throw ProtocolException.invalidOffset("Metadata", fieldOffset0, maxEnd);
            }

            pos0 = offset + 30 + fieldOffset0;
            sl = VarInt.peek(buf, pos0);
            pos0 += VarInt.size(sl) + sl;
            if (pos0 - offset > maxEnd) {
               maxEnd = pos0 - offset;
            }
         }

         return maxEnd;
      } else {
         throw ProtocolException.invalidOffset("ItemId", fieldOffset0, maxEnd);
      }
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 30L;
   }

   public static String getItemId(MemorySegment mem) {
      return getItemId(mem, 0);
   }

   public static String getItemId(MemorySegment mem, int offset) {
      return PacketIO.readVarString("ItemId", mem, offset + getValidatedOffset(mem, offset, 22, 30, "ItemId"), 4096000, PacketIO.UTF8);
   }

   public static int getQuantity(MemorySegment mem) {
      return getQuantity(mem, 0);
   }

   public static int getQuantity(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   public static double getDurability(MemorySegment mem) {
      return getDurability(mem, 0);
   }

   public static double getDurability(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 5));
   }

   public static double getMaxDurability(MemorySegment mem) {
      return getMaxDurability(mem, 0);
   }

   public static double getMaxDurability(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 13));
   }

   public static boolean getOverrideDroppedItemAnimation(MemorySegment mem) {
      return getOverrideDroppedItemAnimation(mem, 0);
   }

   public static boolean getOverrideDroppedItemAnimation(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 21));
   }

   @Nullable
   public static String getMetadata(MemorySegment mem) {
      return getMetadata(mem, 0);
   }

   @Nullable
   public static String getMetadata(MemorySegment mem, int offset) {
      return hasMetadata(mem, offset)
         ? PacketIO.readVarString("Metadata", mem, offset + getValidatedOffset(mem, offset, 26, 30, "Metadata"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static boolean hasMetadata(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static ItemWithAllMetadata toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ItemWithAllMetadata toObject(MemorySegment mem, int offset) {
      if (offset + 30 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ItemWithAllMetadata", offset + 30, (int)mem.byteSize());
      } else {
         return new ItemWithAllMetadata(
            PacketIO.readVarString("ItemId", mem, offset + getValidatedOffset(mem, offset, 22, 30, "ItemId"), 4096000, PacketIO.UTF8),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 5)),
            mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 13)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 21)),
            hasMetadata(mem, offset)
               ? PacketIO.readVarString("Metadata", mem, offset + getValidatedOffset(mem, offset, 26, 30, "Metadata"), 4096000, PacketIO.UTF8)
               : null
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.metadata != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.quantity);
      buf.writeDoubleLE(this.durability);
      buf.writeDoubleLE(this.maxDurability);
      buf.writeByte(this.overrideDroppedItemAnimation ? 1 : 0);
      int itemIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int metadataOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      buf.setIntLE(itemIdOffsetSlot, buf.writerIndex() - varBlockStart);
      PacketIO.writeVarString(buf, this.itemId, 4096000);
      if (this.metadata != null) {
         buf.setIntLE(metadataOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.metadata, 4096000);
      } else {
         buf.setIntLE(metadataOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.metadata != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.quantity);
      mem.set(PacketIO.PROTO_DOUBLE, (long)(offset + 5), this.durability);
      mem.set(PacketIO.PROTO_DOUBLE, (long)(offset + 13), this.maxDurability);
      mem.set(PacketIO.PROTO_BOOL, offset + 21, this.overrideDroppedItemAnimation);
      int varOffset = offset + 30;
      mem.set(PacketIO.PROTO_INT, (long)(offset + 22), varOffset - offset - 30);
      varOffset += PacketIO.writeVarString(mem, varOffset, this.itemId, 4096000);
      if (this.metadata != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 26), varOffset - offset - 30);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.metadata, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 26), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 30;
      size += PacketIO.stringSize(this.itemId);
      if (this.metadata != null) {
         size += PacketIO.stringSize(this.metadata);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 30) {
         return ValidationResult.error("Buffer too small: expected at least 30 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int itemIdOffset = buffer.getIntLE(offset + 22);
         if (itemIdOffset >= 0 && itemIdOffset <= buffer.writerIndex() - offset - 30) {
            int pos = offset + 30 + itemIdOffset;
            int itemIdLen = VarInt.peek(buffer, pos);
            if (itemIdLen < 0) {
               return ValidationResult.error("Invalid string length for ItemId");
            } else if (itemIdLen > 4096000) {
               return ValidationResult.error("ItemId exceeds max length 4096000");
            } else {
               pos += VarInt.size(itemIdLen);
               pos += itemIdLen;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading ItemId");
               } else {
                  if ((nullBits & 1) != 0) {
                     itemIdOffset = buffer.getIntLE(offset + 26);
                     if (itemIdOffset < 0 || itemIdOffset > buffer.writerIndex() - offset - 30) {
                        return ValidationResult.error("Invalid offset for Metadata");
                     }

                     pos = offset + 30 + itemIdOffset;
                     itemIdLen = VarInt.peek(buffer, pos);
                     if (itemIdLen < 0) {
                        return ValidationResult.error("Invalid string length for Metadata");
                     }

                     if (itemIdLen > 4096000) {
                        return ValidationResult.error("Metadata exceeds max length 4096000");
                     }

                     pos += VarInt.size(itemIdLen);
                     pos += itemIdLen;
                     if (pos > buffer.writerIndex()) {
                        return ValidationResult.error("Buffer overflow reading Metadata");
                     }
                  }

                  return ValidationResult.OK;
               }
            }
         } else {
            return ValidationResult.error("Invalid offset for ItemId");
         }
      }
   }

   public ItemWithAllMetadata clone() {
      ItemWithAllMetadata copy = new ItemWithAllMetadata();
      copy.itemId = this.itemId;
      copy.quantity = this.quantity;
      copy.durability = this.durability;
      copy.maxDurability = this.maxDurability;
      copy.overrideDroppedItemAnimation = this.overrideDroppedItemAnimation;
      copy.metadata = this.metadata;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ItemWithAllMetadata other)
            ? false
            : Objects.equals(this.itemId, other.itemId)
               && this.quantity == other.quantity
               && this.durability == other.durability
               && this.maxDurability == other.maxDurability
               && this.overrideDroppedItemAnimation == other.overrideDroppedItemAnimation
               && Objects.equals(this.metadata, other.metadata);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.itemId, this.quantity, this.durability, this.maxDurability, this.overrideDroppedItemAnimation, this.metadata);
   }
}
