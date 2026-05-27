package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.HitboxCollisionConfig;
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

public class UpdateHitboxCollisionConfig implements Packet, ToClientPacket {
   public static final int PACKET_ID = 74;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 6;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 6;
   public static final int MAX_SIZE = 36864011;
   @Nonnull
   public UpdateType type = UpdateType.Init;
   public int maxId;
   @Nullable
   public Map<Integer, HitboxCollisionConfig> hitboxCollisionConfigs;

   @Override
   public int getId() {
      return 74;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateHitboxCollisionConfig() {
   }

   public UpdateHitboxCollisionConfig(@Nonnull UpdateType type, int maxId, @Nullable Map<Integer, HitboxCollisionConfig> hitboxCollisionConfigs) {
      this.type = type;
      this.maxId = maxId;
      this.hitboxCollisionConfigs = hitboxCollisionConfigs;
   }

   public UpdateHitboxCollisionConfig(@Nonnull UpdateHitboxCollisionConfig other) {
      this.type = other.type;
      this.maxId = other.maxId;
      this.hitboxCollisionConfigs = other.hitboxCollisionConfigs;
   }

   @Nonnull
   public static UpdateHitboxCollisionConfig deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 6) {
         throw ProtocolException.bufferTooSmall("UpdateHitboxCollisionConfig", 6, buf.readableBytes() - offset);
      } else {
         UpdateHitboxCollisionConfig obj = new UpdateHitboxCollisionConfig();
         byte nullBits = buf.getByte(offset);
         obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
         obj.maxId = buf.getIntLE(offset + 2);
         int pos = offset + 6;
         if ((nullBits & 1) != 0) {
            int hitboxCollisionConfigsCount = VarInt.peek(buf, pos);
            if (hitboxCollisionConfigsCount < 0) {
               throw ProtocolException.invalidVarInt("HitboxCollisionConfigs");
            }

            int hitboxCollisionConfigsVarLen = VarInt.size(hitboxCollisionConfigsCount);
            if (hitboxCollisionConfigsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("HitboxCollisionConfigs", hitboxCollisionConfigsCount, 4096000);
            }

            pos += hitboxCollisionConfigsVarLen;
            obj.hitboxCollisionConfigs = new HashMap<>(hitboxCollisionConfigsCount);

            for (int i = 0; i < hitboxCollisionConfigsCount; i++) {
               int key = buf.getIntLE(pos);
               pos += 4;
               HitboxCollisionConfig val = HitboxCollisionConfig.deserialize(buf, pos);
               pos += HitboxCollisionConfig.computeBytesConsumed(buf, pos);
               if (obj.hitboxCollisionConfigs.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("hitboxCollisionConfigs", key);
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
            pos += HitboxCollisionConfig.computeBytesConsumed(buf, pos);
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
   public static Map<Integer, HitboxCollisionConfig> getHitboxCollisionConfigs(MemorySegment mem) {
      return getHitboxCollisionConfigs(mem, 0);
   }

   @Nullable
   public static Map<Integer, HitboxCollisionConfig> getHitboxCollisionConfigs(MemorySegment mem, int offset) {
      if (!hasHitboxCollisionConfigs(mem, offset)) {
         return null;
      } else {
         int off = offset + 6;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("HitboxCollisionConfigs", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("HitboxCollisionConfigs", len, 4096000);
         } else {
            Map<Integer, HitboxCollisionConfig> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               HitboxCollisionConfig value = HitboxCollisionConfig.toObject(mem, off);
               off += value.computeSize();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("HitboxCollisionConfigs", key);
               }
            }

            return data;
         }
      }
   }

   public static boolean hasHitboxCollisionConfigs(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static UpdateHitboxCollisionConfig toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UpdateHitboxCollisionConfig toObject(MemorySegment mem, int offset) {
      if (offset + 6 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UpdateHitboxCollisionConfig", offset + 6, (int)mem.byteSize());
      } else {
         Map<Integer, HitboxCollisionConfig> hitboxCollisionConfigs = null;
         if (hasHitboxCollisionConfigs(mem, offset)) {
            int off = offset + 6;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("HitboxCollisionConfigs", len);
            }

            if (len > 4096000) {
               throw ProtocolException.dictionaryTooLarge("HitboxCollisionConfigs", len, 4096000);
            }

            hitboxCollisionConfigs = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               HitboxCollisionConfig value = HitboxCollisionConfig.toObject(mem, off);
               off += value.computeSize();
               if (hitboxCollisionConfigs.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("HitboxCollisionConfigs", key);
               }
            }
         }

         return new UpdateHitboxCollisionConfig(
            UpdateType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))), mem.get(PacketIO.PROTO_INT, (long)(offset + 2)), hitboxCollisionConfigs
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.hitboxCollisionConfigs != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      buf.writeIntLE(this.maxId);
      if (this.hitboxCollisionConfigs != null) {
         if (this.hitboxCollisionConfigs.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("HitboxCollisionConfigs", this.hitboxCollisionConfigs.size(), 4096000);
         }

         VarInt.write(buf, this.hitboxCollisionConfigs.size());

         for (Entry<Integer, HitboxCollisionConfig> e : this.hitboxCollisionConfigs.entrySet()) {
            buf.writeIntLE(e.getKey());
            e.getValue().serialize(buf);
         }
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.hitboxCollisionConfigs != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.type.getValue());
      mem.set(PacketIO.PROTO_INT, (long)(offset + 2), this.maxId);
      int varOffset = offset + 6;
      if (this.hitboxCollisionConfigs != null) {
         if (this.hitboxCollisionConfigs.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("HitboxCollisionConfigs", this.hitboxCollisionConfigs.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.hitboxCollisionConfigs.size());

         for (Entry<Integer, HitboxCollisionConfig> e : this.hitboxCollisionConfigs.entrySet()) {
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
      if (this.hitboxCollisionConfigs != null) {
         size += VarInt.size(this.hitboxCollisionConfigs.size()) + this.hitboxCollisionConfigs.size() * 9;
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
               int hitboxCollisionConfigsCount = VarInt.peek(buffer, v);
               if (hitboxCollisionConfigsCount < 0) {
                  return ValidationResult.error("Invalid dictionary count for HitboxCollisionConfigs");
               }

               if (hitboxCollisionConfigsCount > 4096000) {
                  return ValidationResult.error("HitboxCollisionConfigs exceeds max length 4096000");
               }

               v += VarInt.size(hitboxCollisionConfigsCount);

               for (int i = 0; i < hitboxCollisionConfigsCount; i++) {
                  v += 4;
                  if (v > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading key");
                  }

                  v += 5;
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public UpdateHitboxCollisionConfig clone() {
      UpdateHitboxCollisionConfig copy = new UpdateHitboxCollisionConfig();
      copy.type = this.type;
      copy.maxId = this.maxId;
      if (this.hitboxCollisionConfigs != null) {
         Map<Integer, HitboxCollisionConfig> m = new HashMap<>();

         for (Entry<Integer, HitboxCollisionConfig> e : this.hitboxCollisionConfigs.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.hitboxCollisionConfigs = m;
      }

      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateHitboxCollisionConfig other)
            ? false
            : Objects.equals(this.type, other.type) && this.maxId == other.maxId && Objects.equals(this.hitboxCollisionConfigs, other.hitboxCollisionConfigs);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.maxId, this.hitboxCollisionConfigs);
   }
}
