package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemLibrary {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public ItemBase[] items;
   @Nullable
   public Map<Integer, String>[] blockMap;

   public ItemLibrary() {
   }

   public ItemLibrary(@Nullable ItemBase[] items, @Nullable Map<Integer, String>[] blockMap) {
      this.items = items;
      this.blockMap = blockMap;
   }

   public ItemLibrary(@Nonnull ItemLibrary other) {
      this.items = other.items;
      this.blockMap = other.blockMap;
   }

   @Nonnull
   public static ItemLibrary deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 9) {
         throw ProtocolException.bufferTooSmall("ItemLibrary", 9, buf.readableBytes() - offset);
      } else {
         ItemLibrary obj = new ItemLibrary();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 1);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 9) {
               throw ProtocolException.invalidOffset("Items", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 9 + varPosBase0;
            int itemsCount = VarInt.peek(buf, varPos0);
            if (itemsCount < 0) {
               throw ProtocolException.invalidVarInt("Items");
            }

            int varIntLen = VarInt.size(itemsCount);
            if (itemsCount > 4096000) {
               throw ProtocolException.arrayTooLong("Items", itemsCount, 4096000);
            }

            if (varPos0 + varIntLen + itemsCount * 148L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Items", varPos0 + varIntLen + itemsCount * 148, buf.readableBytes());
            }

            obj.items = new ItemBase[itemsCount];
            int elemPos = varPos0 + varIntLen;

            for (int i = 0; i < itemsCount; i++) {
               obj.items[i] = ItemBase.deserialize(buf, elemPos);
               elemPos += ItemBase.computeBytesConsumed(buf, elemPos);
            }
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 5);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 9) {
               throw ProtocolException.invalidOffset("BlockMap", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 9 + varPosBase1;
            int blockMapCount = VarInt.peek(buf, varPos1);
            if (blockMapCount < 0) {
               throw ProtocolException.invalidVarInt("BlockMap");
            }

            int varIntLenx = VarInt.size(blockMapCount);
            if (blockMapCount > 4096000) {
               throw ProtocolException.arrayTooLong("BlockMap", blockMapCount, 4096000);
            }

            Map<Integer, String>[] blockMapArr = new Map[blockMapCount];
            obj.blockMap = blockMapArr;
            int elemPos = varPos1 + varIntLenx;

            for (int i = 0; i < blockMapCount; i++) {
               int mapLen = VarInt.peek(buf, elemPos);
               if (mapLen < 0) {
                  throw ProtocolException.invalidVarInt("BlockMap[" + i + "]");
               }

               int mapVarLen = VarInt.size(mapLen);
               HashMap<Integer, String> map = new HashMap<>(mapLen);
               int mapPos = elemPos + mapVarLen;

               for (int j = 0; j < mapLen; j++) {
                  int key = buf.getIntLE(mapPos);
                  mapPos += 4;
                  int valLen = VarInt.peek(buf, mapPos);
                  if (valLen < 0) {
                     throw ProtocolException.invalidVarInt("val");
                  }

                  int valVarLen = VarInt.size(valLen);
                  if (valLen > 4096000) {
                     throw ProtocolException.stringTooLong("val", valLen, 4096000);
                  }

                  if (mapPos + valVarLen + valLen > buf.readableBytes()) {
                     throw ProtocolException.bufferTooSmall("val", mapPos + valVarLen + valLen, buf.readableBytes());
                  }

                  String val = PacketIO.readVarString(buf, mapPos);
                  mapPos += valVarLen + valLen;
                  if (map.put(key, val) != null) {
                     throw ProtocolException.duplicateKey("BlockMap[" + i + "]", key);
                  }
               }

               obj.blockMap[i] = map;
               elemPos = mapPos;
            }
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
            throw ProtocolException.invalidOffset("Items", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 9 + fieldOffset0;
         int arrLen = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos0 += ItemBase.computeBytesConsumed(buf, pos0);
         }

         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 9) {
            throw ProtocolException.invalidOffset("BlockMap", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 9 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            int dictLen = VarInt.peek(buf, pos1);
            pos1 += VarInt.size(dictLen);

            for (int j = 0; j < dictLen; j++) {
               pos1 += 4;
               int sl = VarInt.peek(buf, pos1);
               pos1 += VarInt.size(sl) + sl;
            }
         }

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
   public static ItemBase[] getItems(MemorySegment mem) {
      return getItems(mem, 0);
   }

   @Nullable
   public static ItemBase[] getItems(MemorySegment mem, int offset) {
      if (!hasItems(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 1, 9, "Items");
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
               ItemBase[] data = new ItemBase[len];

               for (int i = 0; i < len; i++) {
                  data[i] = ItemBase.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   @Nullable
   public static Map<Integer, String>[] getBlockMap(MemorySegment mem) {
      return getBlockMap(mem, 0);
   }

   @Nullable
   public static Map<Integer, String>[] getBlockMap(MemorySegment mem, int offset) {
      if (!hasBlockMap(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 5, 9, "BlockMap");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("BlockMap", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("BlockMap", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("BlockMap", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               Map<Integer, String>[] data = new Map[len];

               for (int i = 0; i < len; i++) {
                  long mapPacked = VarInt.getWithLength(mem, off);
                  int mapLen = (int)mapPacked;
                  if (mapLen < 0) {
                     throw ProtocolException.negativeLength("BlockMap", mapLen);
                  }

                  if (mapLen > 64) {
                     throw ProtocolException.dictionaryTooLarge("BlockMap", mapLen, 64);
                  }

                  HashMap<Integer, String> map = new HashMap<>(mapLen);
                  off += (int)(mapPacked >>> 32);

                  for (int j = 0; j < mapLen; j++) {
                     int key = mem.get(PacketIO.PROTO_INT, (long)off);
                     off += 4;
                     long valuePacked = VarInt.getWithLength(mem, off);
                     int nvalue = (int)valuePacked + (int)(valuePacked >>> 32);
                     String value = PacketIO.readVarString("value", mem, off, 16384000, PacketIO.UTF8);
                     off += nvalue;
                     if (map.put(key, value) != null) {
                        throw ProtocolException.duplicateKey("BlockMap", key);
                     }
                  }

                  data[i] = map;
               }

               return data;
            }
         }
      }
   }

   public static boolean hasItems(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasBlockMap(MemorySegment mem, int offset) {
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

   public static ItemLibrary toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ItemLibrary toObject(MemorySegment mem, int offset) {
      if (offset + 9 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ItemLibrary", offset + 9, (int)mem.byteSize());
      } else {
         ItemBase[] items = null;
         if (hasItems(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 1, 9, "Items");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Items", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Items", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Items", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            items = new ItemBase[len];

            for (int i = 0; i < len; i++) {
               items[i] = ItemBase.toObject(mem, off);
               off += items[i].computeSize();
            }
         }

         Map<Integer, String>[] blockMap = null;
         if (hasBlockMap(mem, offset)) {
            int offx = offset + getValidatedOffset(mem, offset, 5, 9, "BlockMap");
            long packedx = VarInt.getWithLength(mem, offx);
            int lenx = (int)packedx;
            if (lenx < 0) {
               throw ProtocolException.negativeLength("BlockMap", lenx);
            }

            if (lenx > 4096000) {
               throw ProtocolException.arrayTooLong("BlockMap", lenx, 4096000);
            }

            int lenOffset = (int)(packedx >>> 32);
            if (offx + lenOffset + lenx > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("BlockMap", offx + lenOffset + lenx, (int)mem.byteSize());
            }

            offx += lenOffset;
            blockMap = new Map[lenx];

            for (int i = 0; i < lenx; i++) {
               long mapPacked = VarInt.getWithLength(mem, offx);
               int mapLen = (int)mapPacked;
               if (mapLen < 0) {
                  throw ProtocolException.negativeLength("BlockMap", mapLen);
               }

               if (mapLen > 64) {
                  throw ProtocolException.dictionaryTooLarge("BlockMap", mapLen, 64);
               }

               HashMap<Integer, String> map = new HashMap<>(mapLen);
               offx += (int)(mapPacked >>> 32);

               for (int j = 0; j < mapLen; j++) {
                  int key = mem.get(PacketIO.PROTO_INT, (long)offx);
                  offx += 4;
                  long valuePacked = VarInt.getWithLength(mem, offx);
                  int nvalue = (int)valuePacked + (int)(valuePacked >>> 32);
                  String value = PacketIO.readVarString("value", mem, offx, 16384000, PacketIO.UTF8);
                  offx += nvalue;
                  if (map.put(key, value) != null) {
                     throw ProtocolException.duplicateKey("BlockMap", key);
                  }
               }

               blockMap[i] = map;
            }
         }

         return new ItemLibrary(items, blockMap);
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.items != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.blockMap != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      int itemsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int blockMapOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.items != null) {
         buf.setIntLE(itemsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.items.length > 4096000) {
            throw ProtocolException.arrayTooLong("Items", this.items.length, 4096000);
         }

         VarInt.write(buf, this.items.length);

         for (ItemBase item : this.items) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(itemsOffsetSlot, -1);
      }

      if (this.blockMap != null) {
         buf.setIntLE(blockMapOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.blockMap.length > 4096000) {
            throw ProtocolException.arrayTooLong("BlockMap", this.blockMap.length, 4096000);
         }

         VarInt.write(buf, this.blockMap.length);

         for (Map<Integer, String> item : this.blockMap) {
            VarInt.write(buf, item.size());

            for (Entry<Integer, String> entry : item.entrySet()) {
               buf.writeIntLE(entry.getKey());
               PacketIO.writeVarString(buf, entry.getValue(), 4096000);
            }
         }
      } else {
         buf.setIntLE(blockMapOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.items != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.blockMap != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 9;
      if (this.items != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), varOffset - offset - 9);
         if (this.items.length > 4096000) {
            throw ProtocolException.arrayTooLong("Items", this.items.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.items.length);
         int itemsValueOffset = 0;

         for (int i = 0; i < this.items.length; i++) {
            itemsValueOffset += this.items[i].serialize(mem, varOffset + itemsValueOffset);
         }

         varOffset += itemsValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), -1);
      }

      if (this.blockMap != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), varOffset - offset - 9);
         if (this.blockMap.length > 4096000) {
            throw ProtocolException.arrayTooLong("BlockMap", this.blockMap.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.blockMap.length);

         for (int i = 0; i < this.blockMap.length; i++) {
            varOffset += VarInt.set(mem, varOffset, this.blockMap[i].size());

            for (Entry<Integer, String> e : this.blockMap[i].entrySet()) {
               mem.set(PacketIO.PROTO_INT, (long)varOffset, e.getKey());
               varOffset += 4;
               varOffset += PacketIO.writeVarString(mem, varOffset, e.getValue(), 16384000);
            }
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 9;
      if (this.items != null) {
         int itemsSize = 0;

         for (ItemBase elem : this.items) {
            itemsSize += elem.computeSize();
         }

         size += VarInt.size(this.items.length) + itemsSize;
      }

      if (this.blockMap != null) {
         int blockMapSize = 0;

         for (Map<Integer, String> elem : this.blockMap) {
            blockMapSize += VarInt.size(elem.size()) + elem.entrySet().stream().mapToInt(kvpInner -> 4 + PacketIO.stringSize(kvpInner.getValue())).sum();
         }

         size += VarInt.size(this.blockMap.length) + blockMapSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 9) {
         return ValidationResult.error("Buffer too small: expected at least 9 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int itemsOffset = buffer.getIntLE(offset + 1);
            if (itemsOffset < 0 || itemsOffset > buffer.writerIndex() - offset - 9) {
               return ValidationResult.error("Invalid offset for Items");
            }

            int pos = offset + 9 + itemsOffset;
            int itemsCount = VarInt.peek(buffer, pos);
            if (itemsCount < 0) {
               return ValidationResult.error("Invalid array count for Items");
            }

            if (itemsCount > 4096000) {
               return ValidationResult.error("Items exceeds max length 4096000");
            }

            pos += VarInt.size(itemsCount);

            for (int i = 0; i < itemsCount; i++) {
               ValidationResult structResult = ItemBase.validateStructure(buffer, pos);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid ItemBase in Items[" + i + "]: " + structResult.error());
               }

               pos += ItemBase.computeBytesConsumed(buffer, pos);
            }
         }

         if ((nullBits & 2) != 0) {
            int blockMapOffset = buffer.getIntLE(offset + 5);
            if (blockMapOffset < 0 || blockMapOffset > buffer.writerIndex() - offset - 9) {
               return ValidationResult.error("Invalid offset for BlockMap");
            }

            int posx = offset + 9 + blockMapOffset;
            int blockMapCount = VarInt.peek(buffer, posx);
            if (blockMapCount < 0) {
               return ValidationResult.error("Invalid array count for BlockMap");
            }

            if (blockMapCount > 4096000) {
               return ValidationResult.error("BlockMap exceeds max length 4096000");
            }

            posx += VarInt.size(blockMapCount);

            for (int i = 0; i < blockMapCount; i++) {
               int blockMapDictLen = VarInt.peek(buffer, posx);
               if (blockMapDictLen < 0) {
                  return ValidationResult.error("Invalid dictionary count in BlockMap[" + i + "]");
               }

               posx += VarInt.size(blockMapDictLen);

               for (int j = 0; j < blockMapDictLen; j++) {
                  posx += 4;
                  if (posx > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading blockMapKey");
                  }

                  int blockMapValLen = VarInt.peek(buffer, posx);
                  if (blockMapValLen < 0) {
                     return ValidationResult.error("Invalid string length for blockMapVal");
                  }

                  if (blockMapValLen > 4096000) {
                     return ValidationResult.error("blockMapVal exceeds max length 4096000");
                  }

                  posx += VarInt.size(blockMapValLen);
                  posx += blockMapValLen;
                  if (posx > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading blockMapVal");
                  }
               }
            }
         }

         return ValidationResult.OK;
      }
   }

   public ItemLibrary clone() {
      ItemLibrary copy = new ItemLibrary();
      copy.items = this.items != null ? Arrays.stream(this.items).map(e -> e.clone()).toArray(ItemBase[]::new) : null;
      copy.blockMap = this.blockMap != null
         ? Arrays.stream(this.blockMap).map(d -> new HashMap<>((Map<? extends Integer, ? extends String>)d)).toArray(Map[]::new)
         : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ItemLibrary other)
            ? false
            : Arrays.equals((Object[])this.items, (Object[])other.items) && Arrays.equals((Object[])this.blockMap, (Object[])other.blockMap);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Arrays.hashCode((Object[])this.items);
      return 31 * result + Arrays.hashCode((Object[])this.blockMap);
   }
}
