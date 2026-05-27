package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;

public class UIListDataValue extends UIDataValue {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 0;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 8;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public String itemTypeName = "";
   @Nonnull
   public UIDataValue[] items = new UIDataValue[0];

   public UIListDataValue() {
   }

   public UIListDataValue(@Nonnull String itemTypeName, @Nonnull UIDataValue[] items) {
      this.itemTypeName = itemTypeName;
      this.items = items;
   }

   public UIListDataValue(@Nonnull UIListDataValue other) {
      this.itemTypeName = other.itemTypeName;
      this.items = other.items;
   }

   @Nonnull
   public static UIListDataValue deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 8) {
         throw ProtocolException.bufferTooSmall("UIListDataValue", 8, buf.readableBytes() - offset);
      } else {
         UIListDataValue obj = new UIListDataValue();
         int varPosBase0 = buf.getIntLE(offset + 0);
         if (varPosBase0 >= 0 && varPosBase0 <= buf.writerIndex() - offset - 8) {
            int varPos0 = offset + 8 + varPosBase0;
            int itemTypeNameLen = VarInt.peek(buf, varPos0);
            if (itemTypeNameLen < 0) {
               throw ProtocolException.invalidVarInt("ItemTypeName");
            } else {
               int itemTypeNameVarIntLen = VarInt.size(itemTypeNameLen);
               if (itemTypeNameLen > 4096000) {
                  throw ProtocolException.stringTooLong("ItemTypeName", itemTypeNameLen, 4096000);
               } else if (varPos0 + itemTypeNameVarIntLen + itemTypeNameLen > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("ItemTypeName", varPos0 + itemTypeNameVarIntLen + itemTypeNameLen, buf.readableBytes());
               } else {
                  obj.itemTypeName = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
                  varPosBase0 = buf.getIntLE(offset + 4);
                  if (varPosBase0 >= 0 && varPosBase0 <= buf.writerIndex() - offset - 8) {
                     varPos0 = offset + 8 + varPosBase0;
                     itemTypeNameLen = VarInt.peek(buf, varPos0);
                     if (itemTypeNameLen < 0) {
                        throw ProtocolException.invalidVarInt("Items");
                     } else {
                        itemTypeNameVarIntLen = VarInt.size(itemTypeNameLen);
                        if (itemTypeNameLen > 4096000) {
                           throw ProtocolException.arrayTooLong("Items", itemTypeNameLen, 4096000);
                        } else if (varPos0 + itemTypeNameVarIntLen + itemTypeNameLen * 1L > buf.readableBytes()) {
                           throw ProtocolException.bufferTooSmall("Items", varPos0 + itemTypeNameVarIntLen + itemTypeNameLen * 1, buf.readableBytes());
                        } else {
                           obj.items = new UIDataValue[itemTypeNameLen];
                           int elemPos = varPos0 + itemTypeNameVarIntLen;

                           for (int i = 0; i < itemTypeNameLen; i++) {
                              obj.items[i] = UIDataValue.deserialize(buf, elemPos);
                              elemPos += UIDataValue.computeBytesConsumed(buf, elemPos);
                           }

                           return obj;
                        }
                     }
                  } else {
                     throw ProtocolException.invalidOffset("Items", varPosBase0, buf.readableBytes());
                  }
               }
            }
         } else {
            throw ProtocolException.invalidOffset("ItemTypeName", varPosBase0, buf.readableBytes());
         }
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      int maxEnd = 8;
      int fieldOffset0 = buf.getIntLE(offset + 0);
      if (fieldOffset0 >= 0 && fieldOffset0 <= buf.writerIndex() - offset - 8) {
         int pos0 = offset + 8 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }

         fieldOffset0 = buf.getIntLE(offset + 4);
         if (fieldOffset0 >= 0 && fieldOffset0 <= buf.writerIndex() - offset - 8) {
            pos0 = offset + 8 + fieldOffset0;
            sl = VarInt.peek(buf, pos0);
            pos0 += VarInt.size(sl);

            for (int i = 0; i < sl; i++) {
               pos0 += UIDataValue.computeBytesConsumed(buf, pos0);
            }

            if (pos0 - offset > maxEnd) {
               maxEnd = pos0 - offset;
            }

            return maxEnd;
         } else {
            throw ProtocolException.invalidOffset("Items", fieldOffset0, maxEnd);
         }
      } else {
         throw ProtocolException.invalidOffset("ItemTypeName", fieldOffset0, maxEnd);
      }
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 8L;
   }

   public static String getItemTypeName(MemorySegment mem) {
      return getItemTypeName(mem, 0);
   }

   public static String getItemTypeName(MemorySegment mem, int offset) {
      return PacketIO.readVarString("ItemTypeName", mem, offset + getValidatedOffset(mem, offset, 0, 8, "ItemTypeName"), 4096000, PacketIO.UTF8);
   }

   public static UIDataValue[] getItems(MemorySegment mem) {
      return getItems(mem, 0);
   }

   public static UIDataValue[] getItems(MemorySegment mem, int offset) {
      int off = offset + getValidatedOffset(mem, offset, 4, 8, "Items");
      long packed = VarInt.getWithLength(mem, off);
      int len = (int)packed;
      if (len < 0) {
         throw ProtocolException.negativeLength("Items", len);
      } else if (len > 4096000) {
         throw ProtocolException.arrayTooLong("Items", len, 4096000);
      } else {
         int lenOffset = (int)(packed >>> 32);
         if (off + lenOffset + len > mem.byteSize()) {
            throw ProtocolException.bufferTooSmall("Items", off + lenOffset + len, (int)mem.byteSize());
         } else {
            off += lenOffset;
            UIDataValue[] data = new UIDataValue[len];

            for (int i = 0; i < len; i++) {
               data[i] = UIDataValue.toObject(mem, off);
               off += data[i].computeSizeWithTypeId();
            }

            return data;
         }
      }
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static UIListDataValue toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UIListDataValue toObject(MemorySegment mem, int offset) {
      if (offset + 8 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UIListDataValue", offset + 8, (int)mem.byteSize());
      } else {
         int off = offset + getValidatedOffset(mem, offset, 4, 8, "Items");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Items", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Items", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Items", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               UIDataValue[] items = new UIDataValue[len];

               for (int i = 0; i < len; i++) {
                  items[i] = UIDataValue.toObject(mem, off);
                  off += items[i].computeSizeWithTypeId();
               }

               return new UIListDataValue(
                  PacketIO.readVarString("ItemTypeName", mem, offset + getValidatedOffset(mem, offset, 0, 8, "ItemTypeName"), 4096000, PacketIO.UTF8), items
               );
            }
         }
      }
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      int itemTypeNameOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int itemsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      buf.setIntLE(itemTypeNameOffsetSlot, buf.writerIndex() - varBlockStart);
      PacketIO.writeVarString(buf, this.itemTypeName, 4096000);
      buf.setIntLE(itemsOffsetSlot, buf.writerIndex() - varBlockStart);
      if (this.items.length > 4096000) {
         throw ProtocolException.arrayTooLong("Items", this.items.length, 4096000);
      } else {
         VarInt.write(buf, this.items.length);

         for (UIDataValue item : this.items) {
            item.serializeWithTypeId(buf);
         }

         return buf.writerIndex() - startPos;
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      int varOffset = offset + 8;
      mem.set(PacketIO.PROTO_INT, (long)(offset + 0), varOffset - offset - 8);
      varOffset += PacketIO.writeVarString(mem, varOffset, this.itemTypeName, 4096000);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 4), varOffset - offset - 8);
      if (this.items.length > 4096000) {
         throw ProtocolException.arrayTooLong("Items", this.items.length, 4096000);
      } else {
         varOffset += VarInt.set(mem, varOffset, this.items.length);
         int itemsValueOffset = 0;

         for (int i = 0; i < this.items.length; i++) {
            itemsValueOffset += this.items[i].serializeWithTypeId(mem, varOffset + itemsValueOffset);
         }

         varOffset += itemsValueOffset;
         return varOffset - offset;
      }
   }

   @Override
   public int computeSize() {
      int size = 8;
      size += PacketIO.stringSize(this.itemTypeName);
      int itemsSize = 0;

      for (UIDataValue elem : this.items) {
         itemsSize += elem.computeSizeWithTypeId();
      }

      return size + VarInt.size(this.items.length) + itemsSize;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 8) {
         return ValidationResult.error("Buffer too small: expected at least 8 bytes");
      } else {
         int itemTypeNameOffset = buffer.getIntLE(offset + 0);
         if (itemTypeNameOffset >= 0 && itemTypeNameOffset <= buffer.writerIndex() - offset - 8) {
            int pos = offset + 8 + itemTypeNameOffset;
            int itemTypeNameLen = VarInt.peek(buffer, pos);
            if (itemTypeNameLen < 0) {
               return ValidationResult.error("Invalid string length for ItemTypeName");
            } else if (itemTypeNameLen > 4096000) {
               return ValidationResult.error("ItemTypeName exceeds max length 4096000");
            } else {
               pos += VarInt.size(itemTypeNameLen);
               pos += itemTypeNameLen;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading ItemTypeName");
               } else {
                  itemTypeNameOffset = buffer.getIntLE(offset + 4);
                  if (itemTypeNameOffset >= 0 && itemTypeNameOffset <= buffer.writerIndex() - offset - 8) {
                     pos = offset + 8 + itemTypeNameOffset;
                     itemTypeNameLen = VarInt.peek(buffer, pos);
                     if (itemTypeNameLen < 0) {
                        return ValidationResult.error("Invalid array count for Items");
                     } else if (itemTypeNameLen > 4096000) {
                        return ValidationResult.error("Items exceeds max length 4096000");
                     } else {
                        pos += VarInt.size(itemTypeNameLen);

                        for (int i = 0; i < itemTypeNameLen; i++) {
                           ValidationResult structResult = UIDataValue.validateStructure(buffer, pos);
                           if (!structResult.isValid()) {
                              return ValidationResult.error("Invalid UIDataValue in Items[" + i + "]: " + structResult.error());
                           }

                           pos += UIDataValue.computeBytesConsumed(buffer, pos);
                        }

                        return ValidationResult.OK;
                     }
                  } else {
                     return ValidationResult.error("Invalid offset for Items");
                  }
               }
            }
         } else {
            return ValidationResult.error("Invalid offset for ItemTypeName");
         }
      }
   }

   public UIListDataValue clone() {
      UIListDataValue copy = new UIListDataValue();
      copy.itemTypeName = this.itemTypeName;
      copy.items = Arrays.copyOf(this.items, this.items.length);
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UIListDataValue other)
            ? false
            : Objects.equals(this.itemTypeName, other.itemTypeName) && Arrays.equals((Object[])this.items, (Object[])other.items);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.itemTypeName);
      return 31 * result + Arrays.hashCode((Object[])this.items);
   }
}
