package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.ItemQuality;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
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

public class UpdateItemQualities implements Packet, ToClientPacket {
   public static final int PACKET_ID = 55;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 6;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 6;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public UpdateType type = UpdateType.Init;
   public int maxId;
   @Nullable
   public Map<Integer, ItemQuality> itemQualities;

   @Override
   public int getId() {
      return 55;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateItemQualities() {
   }

   public UpdateItemQualities(@Nonnull UpdateType type, int maxId, @Nullable Map<Integer, ItemQuality> itemQualities) {
      this.type = type;
      this.maxId = maxId;
      this.itemQualities = itemQualities;
   }

   public UpdateItemQualities(@Nonnull UpdateItemQualities other) {
      this.type = other.type;
      this.maxId = other.maxId;
      this.itemQualities = other.itemQualities;
   }

   @Nonnull
   public static UpdateItemQualities deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 6) {
         throw ProtocolException.bufferTooSmall("UpdateItemQualities", 6, buf.readableBytes() - offset);
      } else {
         UpdateItemQualities obj = new UpdateItemQualities();
         byte nullBits = buf.getByte(offset);
         obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
         obj.maxId = buf.getIntLE(offset + 2);
         int pos = offset + 6;
         if ((nullBits & 1) != 0) {
            int itemQualitiesCount = VarInt.peek(buf, pos);
            if (itemQualitiesCount < 0) {
               throw ProtocolException.invalidVarInt("ItemQualities");
            }

            int itemQualitiesVarLen = VarInt.size(itemQualitiesCount);
            if (itemQualitiesCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("ItemQualities", itemQualitiesCount, 4096000);
            }

            pos += itemQualitiesVarLen;
            obj.itemQualities = new HashMap<>(itemQualitiesCount);

            for (int i = 0; i < itemQualitiesCount; i++) {
               int key = buf.getIntLE(pos);
               pos += 4;
               ItemQuality val = ItemQuality.deserialize(buf, pos);
               pos += ItemQuality.computeBytesConsumed(buf, pos);
               if (obj.itemQualities.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("itemQualities", key);
               }
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 6;
      if ((nullBits & 1) != 0) {
         int dictLen = VarInt.peek(buf, pos);
         pos += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos += 4;
            pos += ItemQuality.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 6L;
   }

   public static UpdateType getType(MemorySegment mem) {
      return getType(mem, 0);
   }

   public static UpdateType getType(MemorySegment mem, int offset) {
      return UpdateType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1)));
   }

   public static int getMaxId(MemorySegment mem) {
      return getMaxId(mem, 0);
   }

   public static int getMaxId(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 2));
   }

   @Nullable
   public static Map<Integer, ItemQuality> getItemQualities(MemorySegment mem) {
      return getItemQualities(mem, 0);
   }

   @Nullable
   public static Map<Integer, ItemQuality> getItemQualities(MemorySegment mem, int offset) {
      if (!hasItemQualities(mem, offset)) {
         return null;
      } else {
         int off = offset + 6;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("ItemQualities", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ItemQualities", len, 4096000);
         } else {
            Map<Integer, ItemQuality> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               ItemQuality value = ItemQuality.toObject(mem, off);
               off += value.computeSize();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("ItemQualities", key);
               }
            }

            return data;
         }
      }
   }

   public static boolean hasItemQualities(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static UpdateItemQualities toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UpdateItemQualities toObject(MemorySegment mem, int offset) {
      if (offset + 6 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UpdateItemQualities", offset + 6, (int)mem.byteSize());
      } else {
         Map<Integer, ItemQuality> itemQualities = null;
         if (hasItemQualities(mem, offset)) {
            int off = offset + 6;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("ItemQualities", len);
            }

            if (len > 4096000) {
               throw ProtocolException.dictionaryTooLarge("ItemQualities", len, 4096000);
            }

            itemQualities = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               ItemQuality value = ItemQuality.toObject(mem, off);
               off += value.computeSize();
               if (itemQualities.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("ItemQualities", key);
               }
            }
         }

         return new UpdateItemQualities(
            UpdateType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))), mem.get(PacketIO.PROTO_INT, (long)(offset + 2)), itemQualities
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.itemQualities != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      buf.writeIntLE(this.maxId);
      if (this.itemQualities != null) {
         if (this.itemQualities.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ItemQualities", this.itemQualities.size(), 4096000);
         }

         VarInt.write(buf, this.itemQualities.size());

         for (Entry<Integer, ItemQuality> e : this.itemQualities.entrySet()) {
            buf.writeIntLE(e.getKey());
            e.getValue().serialize(buf);
         }
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.itemQualities != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.type.getValue());
      mem.set(PacketIO.PROTO_INT, (long)(offset + 2), this.maxId);
      int varOffset = offset + 6;
      if (this.itemQualities != null) {
         if (this.itemQualities.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ItemQualities", this.itemQualities.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.itemQualities.size());

         for (Entry<Integer, ItemQuality> e : this.itemQualities.entrySet()) {
            mem.set(PacketIO.PROTO_INT, (long)varOffset, e.getKey());
            varOffset += 4;
            varOffset += e.getValue().serialize(mem, varOffset);
         }
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 6;
      if (this.itemQualities != null) {
         int itemQualitiesSize = 0;

         for (Entry<Integer, ItemQuality> kvp : this.itemQualities.entrySet()) {
            itemQualitiesSize += 4 + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.itemQualities.size()) + itemQualitiesSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 6) {
         return ValidationResult.error("Buffer too small: expected at least 6 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int v = buffer.getByte(offset + 1) & 255;
         if (v >= 3) {
            return ValidationResult.error("Invalid UpdateType value for Type");
         } else {
            v = offset + 6;
            if ((nullBits & 1) != 0) {
               int itemQualitiesCount = VarInt.peek(buffer, v);
               if (itemQualitiesCount < 0) {
                  return ValidationResult.error("Invalid dictionary count for ItemQualities");
               }

               if (itemQualitiesCount > 4096000) {
                  return ValidationResult.error("ItemQualities exceeds max length 4096000");
               }

               v += VarInt.size(itemQualitiesCount);

               for (int i = 0; i < itemQualitiesCount; i++) {
                  v += 4;
                  if (v > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading key");
                  }

                  v += ItemQuality.computeBytesConsumed(buffer, v);
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public UpdateItemQualities clone() {
      UpdateItemQualities copy = new UpdateItemQualities();
      copy.type = this.type;
      copy.maxId = this.maxId;
      if (this.itemQualities != null) {
         Map<Integer, ItemQuality> m = new HashMap<>();

         for (Entry<Integer, ItemQuality> e : this.itemQualities.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.itemQualities = m;
      }

      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateItemQualities other)
            ? false
            : Objects.equals(this.type, other.type) && this.maxId == other.maxId && Objects.equals(this.itemQualities, other.itemQualities);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.maxId, this.itemQualities);
   }
}
