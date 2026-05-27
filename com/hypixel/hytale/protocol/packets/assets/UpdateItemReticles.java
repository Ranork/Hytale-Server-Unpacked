package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.ItemReticleConfig;
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

public class UpdateItemReticles implements Packet, ToClientPacket {
   public static final int PACKET_ID = 57;
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
   public Map<Integer, ItemReticleConfig> itemReticleConfigs;

   @Override
   public int getId() {
      return 57;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateItemReticles() {
   }

   public UpdateItemReticles(@Nonnull UpdateType type, int maxId, @Nullable Map<Integer, ItemReticleConfig> itemReticleConfigs) {
      this.type = type;
      this.maxId = maxId;
      this.itemReticleConfigs = itemReticleConfigs;
   }

   public UpdateItemReticles(@Nonnull UpdateItemReticles other) {
      this.type = other.type;
      this.maxId = other.maxId;
      this.itemReticleConfigs = other.itemReticleConfigs;
   }

   @Nonnull
   public static UpdateItemReticles deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 6) {
         throw ProtocolException.bufferTooSmall("UpdateItemReticles", 6, buf.readableBytes() - offset);
      } else {
         UpdateItemReticles obj = new UpdateItemReticles();
         byte nullBits = buf.getByte(offset);
         obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
         obj.maxId = buf.getIntLE(offset + 2);
         int pos = offset + 6;
         if ((nullBits & 1) != 0) {
            int itemReticleConfigsCount = VarInt.peek(buf, pos);
            if (itemReticleConfigsCount < 0) {
               throw ProtocolException.invalidVarInt("ItemReticleConfigs");
            }

            int itemReticleConfigsVarLen = VarInt.size(itemReticleConfigsCount);
            if (itemReticleConfigsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("ItemReticleConfigs", itemReticleConfigsCount, 4096000);
            }

            pos += itemReticleConfigsVarLen;
            obj.itemReticleConfigs = new HashMap<>(itemReticleConfigsCount);

            for (int i = 0; i < itemReticleConfigsCount; i++) {
               int key = buf.getIntLE(pos);
               pos += 4;
               ItemReticleConfig val = ItemReticleConfig.deserialize(buf, pos);
               pos += ItemReticleConfig.computeBytesConsumed(buf, pos);
               if (obj.itemReticleConfigs.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("itemReticleConfigs", key);
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
            pos += ItemReticleConfig.computeBytesConsumed(buf, pos);
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
   public static Map<Integer, ItemReticleConfig> getItemReticleConfigs(MemorySegment mem) {
      return getItemReticleConfigs(mem, 0);
   }

   @Nullable
   public static Map<Integer, ItemReticleConfig> getItemReticleConfigs(MemorySegment mem, int offset) {
      if (!hasItemReticleConfigs(mem, offset)) {
         return null;
      } else {
         int off = offset + 6;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("ItemReticleConfigs", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ItemReticleConfigs", len, 4096000);
         } else {
            Map<Integer, ItemReticleConfig> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               ItemReticleConfig value = ItemReticleConfig.toObject(mem, off);
               off += value.computeSize();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("ItemReticleConfigs", key);
               }
            }

            return data;
         }
      }
   }

   public static boolean hasItemReticleConfigs(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static UpdateItemReticles toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UpdateItemReticles toObject(MemorySegment mem, int offset) {
      if (offset + 6 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UpdateItemReticles", offset + 6, (int)mem.byteSize());
      } else {
         Map<Integer, ItemReticleConfig> itemReticleConfigs = null;
         if (hasItemReticleConfigs(mem, offset)) {
            int off = offset + 6;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("ItemReticleConfigs", len);
            }

            if (len > 4096000) {
               throw ProtocolException.dictionaryTooLarge("ItemReticleConfigs", len, 4096000);
            }

            itemReticleConfigs = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               ItemReticleConfig value = ItemReticleConfig.toObject(mem, off);
               off += value.computeSize();
               if (itemReticleConfigs.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("ItemReticleConfigs", key);
               }
            }
         }

         return new UpdateItemReticles(
            UpdateType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))), mem.get(PacketIO.PROTO_INT, (long)(offset + 2)), itemReticleConfigs
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.itemReticleConfigs != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      buf.writeIntLE(this.maxId);
      if (this.itemReticleConfigs != null) {
         if (this.itemReticleConfigs.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ItemReticleConfigs", this.itemReticleConfigs.size(), 4096000);
         }

         VarInt.write(buf, this.itemReticleConfigs.size());

         for (Entry<Integer, ItemReticleConfig> e : this.itemReticleConfigs.entrySet()) {
            buf.writeIntLE(e.getKey());
            e.getValue().serialize(buf);
         }
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.itemReticleConfigs != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.type.getValue());
      mem.set(PacketIO.PROTO_INT, (long)(offset + 2), this.maxId);
      int varOffset = offset + 6;
      if (this.itemReticleConfigs != null) {
         if (this.itemReticleConfigs.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ItemReticleConfigs", this.itemReticleConfigs.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.itemReticleConfigs.size());

         for (Entry<Integer, ItemReticleConfig> e : this.itemReticleConfigs.entrySet()) {
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
      if (this.itemReticleConfigs != null) {
         int itemReticleConfigsSize = 0;

         for (Entry<Integer, ItemReticleConfig> kvp : this.itemReticleConfigs.entrySet()) {
            itemReticleConfigsSize += 4 + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.itemReticleConfigs.size()) + itemReticleConfigsSize;
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
               int itemReticleConfigsCount = VarInt.peek(buffer, v);
               if (itemReticleConfigsCount < 0) {
                  return ValidationResult.error("Invalid dictionary count for ItemReticleConfigs");
               }

               if (itemReticleConfigsCount > 4096000) {
                  return ValidationResult.error("ItemReticleConfigs exceeds max length 4096000");
               }

               v += VarInt.size(itemReticleConfigsCount);

               for (int i = 0; i < itemReticleConfigsCount; i++) {
                  v += 4;
                  if (v > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading key");
                  }

                  v += ItemReticleConfig.computeBytesConsumed(buffer, v);
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public UpdateItemReticles clone() {
      UpdateItemReticles copy = new UpdateItemReticles();
      copy.type = this.type;
      copy.maxId = this.maxId;
      if (this.itemReticleConfigs != null) {
         Map<Integer, ItemReticleConfig> m = new HashMap<>();

         for (Entry<Integer, ItemReticleConfig> e : this.itemReticleConfigs.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.itemReticleConfigs = m;
      }

      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateItemReticles other)
            ? false
            : Objects.equals(this.type, other.type) && this.maxId == other.maxId && Objects.equals(this.itemReticleConfigs, other.itemReticleConfigs);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.maxId, this.itemReticleConfigs);
   }
}
