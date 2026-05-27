package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.EqualizerEffect;
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

public class UpdateEqualizerEffects implements Packet, ToClientPacket {
   public static final int PACKET_ID = 82;
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
   public Map<Integer, EqualizerEffect> effects;

   @Override
   public int getId() {
      return 82;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateEqualizerEffects() {
   }

   public UpdateEqualizerEffects(@Nonnull UpdateType type, int maxId, @Nullable Map<Integer, EqualizerEffect> effects) {
      this.type = type;
      this.maxId = maxId;
      this.effects = effects;
   }

   public UpdateEqualizerEffects(@Nonnull UpdateEqualizerEffects other) {
      this.type = other.type;
      this.maxId = other.maxId;
      this.effects = other.effects;
   }

   @Nonnull
   public static UpdateEqualizerEffects deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 6) {
         throw ProtocolException.bufferTooSmall("UpdateEqualizerEffects", 6, buf.readableBytes() - offset);
      } else {
         UpdateEqualizerEffects obj = new UpdateEqualizerEffects();
         byte nullBits = buf.getByte(offset);
         obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
         obj.maxId = buf.getIntLE(offset + 2);
         int pos = offset + 6;
         if ((nullBits & 1) != 0) {
            int effectsCount = VarInt.peek(buf, pos);
            if (effectsCount < 0) {
               throw ProtocolException.invalidVarInt("Effects");
            }

            int effectsVarLen = VarInt.size(effectsCount);
            if (effectsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Effects", effectsCount, 4096000);
            }

            pos += effectsVarLen;
            obj.effects = new HashMap<>(effectsCount);

            for (int i = 0; i < effectsCount; i++) {
               int key = buf.getIntLE(pos);
               pos += 4;
               EqualizerEffect val = EqualizerEffect.deserialize(buf, pos);
               pos += EqualizerEffect.computeBytesConsumed(buf, pos);
               if (obj.effects.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("effects", key);
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
            pos += EqualizerEffect.computeBytesConsumed(buf, pos);
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
   public static Map<Integer, EqualizerEffect> getEffects(MemorySegment mem) {
      return getEffects(mem, 0);
   }

   @Nullable
   public static Map<Integer, EqualizerEffect> getEffects(MemorySegment mem, int offset) {
      if (!hasEffects(mem, offset)) {
         return null;
      } else {
         int off = offset + 6;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Effects", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Effects", len, 4096000);
         } else {
            Map<Integer, EqualizerEffect> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               EqualizerEffect value = EqualizerEffect.toObject(mem, off);
               off += value.computeSize();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Effects", key);
               }
            }

            return data;
         }
      }
   }

   public static boolean hasEffects(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static UpdateEqualizerEffects toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UpdateEqualizerEffects toObject(MemorySegment mem, int offset) {
      if (offset + 6 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UpdateEqualizerEffects", offset + 6, (int)mem.byteSize());
      } else {
         Map<Integer, EqualizerEffect> effects = null;
         if (hasEffects(mem, offset)) {
            int off = offset + 6;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Effects", len);
            }

            if (len > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Effects", len, 4096000);
            }

            effects = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               EqualizerEffect value = EqualizerEffect.toObject(mem, off);
               off += value.computeSize();
               if (effects.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Effects", key);
               }
            }
         }

         return new UpdateEqualizerEffects(
            UpdateType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))), mem.get(PacketIO.PROTO_INT, (long)(offset + 2)), effects
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.effects != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      buf.writeIntLE(this.maxId);
      if (this.effects != null) {
         if (this.effects.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Effects", this.effects.size(), 4096000);
         }

         VarInt.write(buf, this.effects.size());

         for (Entry<Integer, EqualizerEffect> e : this.effects.entrySet()) {
            buf.writeIntLE(e.getKey());
            e.getValue().serialize(buf);
         }
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.effects != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.type.getValue());
      mem.set(PacketIO.PROTO_INT, (long)(offset + 2), this.maxId);
      int varOffset = offset + 6;
      if (this.effects != null) {
         if (this.effects.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Effects", this.effects.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.effects.size());

         for (Entry<Integer, EqualizerEffect> e : this.effects.entrySet()) {
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
      if (this.effects != null) {
         int effectsSize = 0;

         for (Entry<Integer, EqualizerEffect> kvp : this.effects.entrySet()) {
            effectsSize += 4 + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.effects.size()) + effectsSize;
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
               int effectsCount = VarInt.peek(buffer, v);
               if (effectsCount < 0) {
                  return ValidationResult.error("Invalid dictionary count for Effects");
               }

               if (effectsCount > 4096000) {
                  return ValidationResult.error("Effects exceeds max length 4096000");
               }

               v += VarInt.size(effectsCount);

               for (int i = 0; i < effectsCount; i++) {
                  v += 4;
                  if (v > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading key");
                  }

                  v += EqualizerEffect.computeBytesConsumed(buffer, v);
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public UpdateEqualizerEffects clone() {
      UpdateEqualizerEffects copy = new UpdateEqualizerEffects();
      copy.type = this.type;
      copy.maxId = this.maxId;
      if (this.effects != null) {
         Map<Integer, EqualizerEffect> m = new HashMap<>();

         for (Entry<Integer, EqualizerEffect> e : this.effects.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.effects = m;
      }

      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateEqualizerEffects other)
            ? false
            : Objects.equals(this.type, other.type) && this.maxId == other.maxId && Objects.equals(this.effects, other.effects);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.maxId, this.effects);
   }
}
