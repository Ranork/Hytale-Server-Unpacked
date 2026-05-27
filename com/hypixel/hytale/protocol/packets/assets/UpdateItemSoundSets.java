package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.ItemSoundSet;
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

public class UpdateItemSoundSets implements Packet, ToClientPacket {
   public static final int PACKET_ID = 43;
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
   public Map<Integer, ItemSoundSet> itemSoundSets;

   @Override
   public int getId() {
      return 43;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateItemSoundSets() {
   }

   public UpdateItemSoundSets(@Nonnull UpdateType type, int maxId, @Nullable Map<Integer, ItemSoundSet> itemSoundSets) {
      this.type = type;
      this.maxId = maxId;
      this.itemSoundSets = itemSoundSets;
   }

   public UpdateItemSoundSets(@Nonnull UpdateItemSoundSets other) {
      this.type = other.type;
      this.maxId = other.maxId;
      this.itemSoundSets = other.itemSoundSets;
   }

   @Nonnull
   public static UpdateItemSoundSets deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 6) {
         throw ProtocolException.bufferTooSmall("UpdateItemSoundSets", 6, buf.readableBytes() - offset);
      } else {
         UpdateItemSoundSets obj = new UpdateItemSoundSets();
         byte nullBits = buf.getByte(offset);
         obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
         obj.maxId = buf.getIntLE(offset + 2);
         int pos = offset + 6;
         if ((nullBits & 1) != 0) {
            int itemSoundSetsCount = VarInt.peek(buf, pos);
            if (itemSoundSetsCount < 0) {
               throw ProtocolException.invalidVarInt("ItemSoundSets");
            }

            int itemSoundSetsVarLen = VarInt.size(itemSoundSetsCount);
            if (itemSoundSetsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("ItemSoundSets", itemSoundSetsCount, 4096000);
            }

            pos += itemSoundSetsVarLen;
            obj.itemSoundSets = new HashMap<>(itemSoundSetsCount);

            for (int i = 0; i < itemSoundSetsCount; i++) {
               int key = buf.getIntLE(pos);
               pos += 4;
               ItemSoundSet val = ItemSoundSet.deserialize(buf, pos);
               pos += ItemSoundSet.computeBytesConsumed(buf, pos);
               if (obj.itemSoundSets.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("itemSoundSets", key);
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
            pos += ItemSoundSet.computeBytesConsumed(buf, pos);
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
   public static Map<Integer, ItemSoundSet> getItemSoundSets(MemorySegment mem) {
      return getItemSoundSets(mem, 0);
   }

   @Nullable
   public static Map<Integer, ItemSoundSet> getItemSoundSets(MemorySegment mem, int offset) {
      if (!hasItemSoundSets(mem, offset)) {
         return null;
      } else {
         int off = offset + 6;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("ItemSoundSets", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ItemSoundSets", len, 4096000);
         } else {
            Map<Integer, ItemSoundSet> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               ItemSoundSet value = ItemSoundSet.toObject(mem, off);
               off += value.computeSize();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("ItemSoundSets", key);
               }
            }

            return data;
         }
      }
   }

   public static boolean hasItemSoundSets(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static UpdateItemSoundSets toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UpdateItemSoundSets toObject(MemorySegment mem, int offset) {
      if (offset + 6 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UpdateItemSoundSets", offset + 6, (int)mem.byteSize());
      } else {
         Map<Integer, ItemSoundSet> itemSoundSets = null;
         if (hasItemSoundSets(mem, offset)) {
            int off = offset + 6;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("ItemSoundSets", len);
            }

            if (len > 4096000) {
               throw ProtocolException.dictionaryTooLarge("ItemSoundSets", len, 4096000);
            }

            itemSoundSets = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               ItemSoundSet value = ItemSoundSet.toObject(mem, off);
               off += value.computeSize();
               if (itemSoundSets.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("ItemSoundSets", key);
               }
            }
         }

         return new UpdateItemSoundSets(
            UpdateType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))), mem.get(PacketIO.PROTO_INT, (long)(offset + 2)), itemSoundSets
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.itemSoundSets != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      buf.writeIntLE(this.maxId);
      if (this.itemSoundSets != null) {
         if (this.itemSoundSets.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ItemSoundSets", this.itemSoundSets.size(), 4096000);
         }

         VarInt.write(buf, this.itemSoundSets.size());

         for (Entry<Integer, ItemSoundSet> e : this.itemSoundSets.entrySet()) {
            buf.writeIntLE(e.getKey());
            e.getValue().serialize(buf);
         }
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.itemSoundSets != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.type.getValue());
      mem.set(PacketIO.PROTO_INT, (long)(offset + 2), this.maxId);
      int varOffset = offset + 6;
      if (this.itemSoundSets != null) {
         if (this.itemSoundSets.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ItemSoundSets", this.itemSoundSets.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.itemSoundSets.size());

         for (Entry<Integer, ItemSoundSet> e : this.itemSoundSets.entrySet()) {
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
      if (this.itemSoundSets != null) {
         int itemSoundSetsSize = 0;

         for (Entry<Integer, ItemSoundSet> kvp : this.itemSoundSets.entrySet()) {
            itemSoundSetsSize += 4 + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.itemSoundSets.size()) + itemSoundSetsSize;
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
               int itemSoundSetsCount = VarInt.peek(buffer, v);
               if (itemSoundSetsCount < 0) {
                  return ValidationResult.error("Invalid dictionary count for ItemSoundSets");
               }

               if (itemSoundSetsCount > 4096000) {
                  return ValidationResult.error("ItemSoundSets exceeds max length 4096000");
               }

               v += VarInt.size(itemSoundSetsCount);

               for (int i = 0; i < itemSoundSetsCount; i++) {
                  v += 4;
                  if (v > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading key");
                  }

                  v += ItemSoundSet.computeBytesConsumed(buffer, v);
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public UpdateItemSoundSets clone() {
      UpdateItemSoundSets copy = new UpdateItemSoundSets();
      copy.type = this.type;
      copy.maxId = this.maxId;
      if (this.itemSoundSets != null) {
         Map<Integer, ItemSoundSet> m = new HashMap<>();

         for (Entry<Integer, ItemSoundSet> e : this.itemSoundSets.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.itemSoundSets = m;
      }

      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateItemSoundSets other)
            ? false
            : Objects.equals(this.type, other.type) && this.maxId == other.maxId && Objects.equals(this.itemSoundSets, other.itemSoundSets);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.maxId, this.itemSoundSets);
   }
}
