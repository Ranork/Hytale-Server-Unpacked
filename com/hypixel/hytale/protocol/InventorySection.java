package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InventorySection {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 3;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 3;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public Map<Integer, ItemWithAllMetadata> items;
   public short capacity;

   public InventorySection() {
   }

   public InventorySection(@Nullable Map<Integer, ItemWithAllMetadata> items, short capacity) {
      this.items = items;
      this.capacity = capacity;
   }

   public InventorySection(@Nonnull InventorySection other) {
      this.items = other.items;
      this.capacity = other.capacity;
   }

   @Nonnull
   public static InventorySection deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 3) {
         throw ProtocolException.bufferTooSmall("InventorySection", 3, buf.readableBytes() - offset);
      } else {
         InventorySection obj = new InventorySection();
         byte nullBits = buf.getByte(offset);
         obj.capacity = buf.getShortLE(offset + 1);
         int pos = offset + 3;
         if ((nullBits & 1) != 0) {
            int itemsCount = VarInt.peek(buf, pos);
            if (itemsCount < 0) {
               throw ProtocolException.invalidVarInt("Items");
            }

            int itemsVarLen = VarInt.size(itemsCount);
            if (itemsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Items", itemsCount, 4096000);
            }

            pos += itemsVarLen;
            obj.items = new HashMap<>(itemsCount);

            for (int i = 0; i < itemsCount; i++) {
               int key = buf.getIntLE(pos);
               pos += 4;
               ItemWithAllMetadata val = ItemWithAllMetadata.deserialize(buf, pos);
               pos += ItemWithAllMetadata.computeBytesConsumed(buf, pos);
               if (obj.items.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("items", key);
               }
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 3;
      if ((nullBits & 1) != 0) {
         int dictLen = VarInt.peek(buf, pos);
         pos += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos += 4;
            pos += ItemWithAllMetadata.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 3L;
   }

   @Nullable
   public static Map<Integer, ItemWithAllMetadata> getItems(MemorySegment mem) {
      return getItems(mem, 0);
   }

   @Nullable
   public static Map<Integer, ItemWithAllMetadata> getItems(MemorySegment mem, int offset) {
      if (!hasItems(mem, offset)) {
         return null;
      } else {
         int off = offset + 3;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Items", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Items", len, 4096000);
         } else {
            Map<Integer, ItemWithAllMetadata> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               ItemWithAllMetadata value = ItemWithAllMetadata.toObject(mem, off);
               off += value.computeSize();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Items", key);
               }
            }

            return data;
         }
      }
   }

   public static short getCapacity(MemorySegment mem) {
      return getCapacity(mem, 0);
   }

   public static short getCapacity(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_SHORT, (long)(offset + 1));
   }

   public static boolean hasItems(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static InventorySection toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static InventorySection toObject(MemorySegment mem, int offset) {
      if (offset + 3 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("InventorySection", offset + 3, (int)mem.byteSize());
      } else {
         Map<Integer, ItemWithAllMetadata> items = null;
         if (hasItems(mem, offset)) {
            int off = offset + 3;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Items", len);
            }

            if (len > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Items", len, 4096000);
            }

            items = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               ItemWithAllMetadata value = ItemWithAllMetadata.toObject(mem, off);
               off += value.computeSize();
               if (items.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Items", key);
               }
            }
         }

         return new InventorySection(items, mem.get(PacketIO.PROTO_SHORT, (long)(offset + 1)));
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.items != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeShortLE(this.capacity);
      if (this.items != null) {
         if (this.items.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Items", this.items.size(), 4096000);
         }

         VarInt.write(buf, this.items.size());

         for (Entry<Integer, ItemWithAllMetadata> e : this.items.entrySet()) {
            buf.writeIntLE(e.getKey());
            e.getValue().serialize(buf);
         }
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.items != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_SHORT, (long)(offset + 1), this.capacity);
      int varOffset = offset + 3;
      if (this.items != null) {
         if (this.items.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Items", this.items.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.items.size());

         for (Entry<Integer, ItemWithAllMetadata> e : this.items.entrySet()) {
            mem.set(PacketIO.PROTO_INT, (long)varOffset, e.getKey());
            varOffset += 4;
            varOffset += e.getValue().serialize(mem, varOffset);
         }
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 3;
      if (this.items != null) {
         int itemsSize = 0;

         for (Entry<Integer, ItemWithAllMetadata> kvp : this.items.entrySet()) {
            itemsSize += 4 + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.items.size()) + itemsSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 3) {
         return ValidationResult.error("Buffer too small: expected at least 3 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 3;
         if ((nullBits & 1) != 0) {
            int itemsCount = VarInt.peek(buffer, pos);
            if (itemsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for Items");
            }

            if (itemsCount > 4096000) {
               return ValidationResult.error("Items exceeds max length 4096000");
            }

            pos += VarInt.size(itemsCount);

            for (int i = 0; i < itemsCount; i++) {
               pos += 4;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               pos += ItemWithAllMetadata.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
      }
   }

   public InventorySection clone() {
      InventorySection copy = new InventorySection();
      if (this.items != null) {
         Map<Integer, ItemWithAllMetadata> m = new HashMap<>();

         for (Entry<Integer, ItemWithAllMetadata> e : this.items.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.items = m;
      }

      copy.capacity = this.capacity;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof InventorySection other) ? false : Objects.equals(this.items, other.items) && this.capacity == other.capacity;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.items, this.capacity);
   }
}
