package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.RepulsionConfig;
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

public class UpdateRepulsionConfig implements Packet, ToClientPacket {
   public static final int PACKET_ID = 75;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 6;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 6;
   public static final int MAX_SIZE = 65536011;
   @Nonnull
   public UpdateType type = UpdateType.Init;
   public int maxId;
   @Nullable
   public Map<Integer, RepulsionConfig> repulsionConfigs;

   @Override
   public int getId() {
      return 75;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateRepulsionConfig() {
   }

   public UpdateRepulsionConfig(@Nonnull UpdateType type, int maxId, @Nullable Map<Integer, RepulsionConfig> repulsionConfigs) {
      this.type = type;
      this.maxId = maxId;
      this.repulsionConfigs = repulsionConfigs;
   }

   public UpdateRepulsionConfig(@Nonnull UpdateRepulsionConfig other) {
      this.type = other.type;
      this.maxId = other.maxId;
      this.repulsionConfigs = other.repulsionConfigs;
   }

   @Nonnull
   public static UpdateRepulsionConfig deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 6) {
         throw ProtocolException.bufferTooSmall("UpdateRepulsionConfig", 6, buf.readableBytes() - offset);
      } else {
         UpdateRepulsionConfig obj = new UpdateRepulsionConfig();
         byte nullBits = buf.getByte(offset);
         obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
         obj.maxId = buf.getIntLE(offset + 2);
         int pos = offset + 6;
         if ((nullBits & 1) != 0) {
            int repulsionConfigsCount = VarInt.peek(buf, pos);
            if (repulsionConfigsCount < 0) {
               throw ProtocolException.invalidVarInt("RepulsionConfigs");
            }

            int repulsionConfigsVarLen = VarInt.size(repulsionConfigsCount);
            if (repulsionConfigsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("RepulsionConfigs", repulsionConfigsCount, 4096000);
            }

            pos += repulsionConfigsVarLen;
            obj.repulsionConfigs = new HashMap<>(repulsionConfigsCount);

            for (int i = 0; i < repulsionConfigsCount; i++) {
               int key = buf.getIntLE(pos);
               pos += 4;
               RepulsionConfig val = RepulsionConfig.deserialize(buf, pos);
               pos += RepulsionConfig.computeBytesConsumed(buf, pos);
               if (obj.repulsionConfigs.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("repulsionConfigs", key);
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
            pos += RepulsionConfig.computeBytesConsumed(buf, pos);
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
   public static Map<Integer, RepulsionConfig> getRepulsionConfigs(MemorySegment mem) {
      return getRepulsionConfigs(mem, 0);
   }

   @Nullable
   public static Map<Integer, RepulsionConfig> getRepulsionConfigs(MemorySegment mem, int offset) {
      if (!hasRepulsionConfigs(mem, offset)) {
         return null;
      } else {
         int off = offset + 6;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("RepulsionConfigs", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("RepulsionConfigs", len, 4096000);
         } else {
            Map<Integer, RepulsionConfig> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               RepulsionConfig value = RepulsionConfig.toObject(mem, off);
               off += value.computeSize();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("RepulsionConfigs", key);
               }
            }

            return data;
         }
      }
   }

   public static boolean hasRepulsionConfigs(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static UpdateRepulsionConfig toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UpdateRepulsionConfig toObject(MemorySegment mem, int offset) {
      if (offset + 6 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UpdateRepulsionConfig", offset + 6, (int)mem.byteSize());
      } else {
         Map<Integer, RepulsionConfig> repulsionConfigs = null;
         if (hasRepulsionConfigs(mem, offset)) {
            int off = offset + 6;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("RepulsionConfigs", len);
            }

            if (len > 4096000) {
               throw ProtocolException.dictionaryTooLarge("RepulsionConfigs", len, 4096000);
            }

            repulsionConfigs = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               RepulsionConfig value = RepulsionConfig.toObject(mem, off);
               off += value.computeSize();
               if (repulsionConfigs.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("RepulsionConfigs", key);
               }
            }
         }

         return new UpdateRepulsionConfig(
            UpdateType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))), mem.get(PacketIO.PROTO_INT, (long)(offset + 2)), repulsionConfigs
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.repulsionConfigs != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      buf.writeIntLE(this.maxId);
      if (this.repulsionConfigs != null) {
         if (this.repulsionConfigs.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("RepulsionConfigs", this.repulsionConfigs.size(), 4096000);
         }

         VarInt.write(buf, this.repulsionConfigs.size());

         for (Entry<Integer, RepulsionConfig> e : this.repulsionConfigs.entrySet()) {
            buf.writeIntLE(e.getKey());
            e.getValue().serialize(buf);
         }
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.repulsionConfigs != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.type.getValue());
      mem.set(PacketIO.PROTO_INT, (long)(offset + 2), this.maxId);
      int varOffset = offset + 6;
      if (this.repulsionConfigs != null) {
         if (this.repulsionConfigs.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("RepulsionConfigs", this.repulsionConfigs.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.repulsionConfigs.size());

         for (Entry<Integer, RepulsionConfig> e : this.repulsionConfigs.entrySet()) {
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
      if (this.repulsionConfigs != null) {
         size += VarInt.size(this.repulsionConfigs.size()) + this.repulsionConfigs.size() * 16;
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
               int repulsionConfigsCount = VarInt.peek(buffer, v);
               if (repulsionConfigsCount < 0) {
                  return ValidationResult.error("Invalid dictionary count for RepulsionConfigs");
               }

               if (repulsionConfigsCount > 4096000) {
                  return ValidationResult.error("RepulsionConfigs exceeds max length 4096000");
               }

               v += VarInt.size(repulsionConfigsCount);

               for (int i = 0; i < repulsionConfigsCount; i++) {
                  v += 4;
                  if (v > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading key");
                  }

                  v += 12;
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public UpdateRepulsionConfig clone() {
      UpdateRepulsionConfig copy = new UpdateRepulsionConfig();
      copy.type = this.type;
      copy.maxId = this.maxId;
      if (this.repulsionConfigs != null) {
         Map<Integer, RepulsionConfig> m = new HashMap<>();

         for (Entry<Integer, RepulsionConfig> e : this.repulsionConfigs.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.repulsionConfigs = m;
      }

      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateRepulsionConfig other)
            ? false
            : Objects.equals(this.type, other.type) && this.maxId == other.maxId && Objects.equals(this.repulsionConfigs, other.repulsionConfigs);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.maxId, this.repulsionConfigs);
   }
}
