package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.AudioState;
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

public class UpdateAudioStates implements Packet, ToClientPacket {
   public static final int PACKET_ID = 89;
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
   public Map<Integer, AudioState> audioStates;

   @Override
   public int getId() {
      return 89;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateAudioStates() {
   }

   public UpdateAudioStates(@Nonnull UpdateType type, int maxId, @Nullable Map<Integer, AudioState> audioStates) {
      this.type = type;
      this.maxId = maxId;
      this.audioStates = audioStates;
   }

   public UpdateAudioStates(@Nonnull UpdateAudioStates other) {
      this.type = other.type;
      this.maxId = other.maxId;
      this.audioStates = other.audioStates;
   }

   @Nonnull
   public static UpdateAudioStates deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 6) {
         throw ProtocolException.bufferTooSmall("UpdateAudioStates", 6, buf.readableBytes() - offset);
      } else {
         UpdateAudioStates obj = new UpdateAudioStates();
         byte nullBits = buf.getByte(offset);
         obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
         obj.maxId = buf.getIntLE(offset + 2);
         int pos = offset + 6;
         if ((nullBits & 1) != 0) {
            int audioStatesCount = VarInt.peek(buf, pos);
            if (audioStatesCount < 0) {
               throw ProtocolException.invalidVarInt("AudioStates");
            }

            int audioStatesVarLen = VarInt.size(audioStatesCount);
            if (audioStatesCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("AudioStates", audioStatesCount, 4096000);
            }

            pos += audioStatesVarLen;
            obj.audioStates = new HashMap<>(audioStatesCount);

            for (int i = 0; i < audioStatesCount; i++) {
               int key = buf.getIntLE(pos);
               pos += 4;
               AudioState val = AudioState.deserialize(buf, pos);
               pos += AudioState.computeBytesConsumed(buf, pos);
               if (obj.audioStates.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("audioStates", key);
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
            pos += AudioState.computeBytesConsumed(buf, pos);
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
   public static Map<Integer, AudioState> getAudioStates(MemorySegment mem) {
      return getAudioStates(mem, 0);
   }

   @Nullable
   public static Map<Integer, AudioState> getAudioStates(MemorySegment mem, int offset) {
      if (!hasAudioStates(mem, offset)) {
         return null;
      } else {
         int off = offset + 6;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("AudioStates", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("AudioStates", len, 4096000);
         } else {
            Map<Integer, AudioState> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               AudioState value = AudioState.toObject(mem, off);
               off += value.computeSize();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("AudioStates", key);
               }
            }

            return data;
         }
      }
   }

   public static boolean hasAudioStates(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static UpdateAudioStates toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UpdateAudioStates toObject(MemorySegment mem, int offset) {
      if (offset + 6 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UpdateAudioStates", offset + 6, (int)mem.byteSize());
      } else {
         Map<Integer, AudioState> audioStates = null;
         if (hasAudioStates(mem, offset)) {
            int off = offset + 6;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("AudioStates", len);
            }

            if (len > 4096000) {
               throw ProtocolException.dictionaryTooLarge("AudioStates", len, 4096000);
            }

            audioStates = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               AudioState value = AudioState.toObject(mem, off);
               off += value.computeSize();
               if (audioStates.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("AudioStates", key);
               }
            }
         }

         return new UpdateAudioStates(
            UpdateType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))), mem.get(PacketIO.PROTO_INT, (long)(offset + 2)), audioStates
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.audioStates != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      buf.writeIntLE(this.maxId);
      if (this.audioStates != null) {
         if (this.audioStates.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("AudioStates", this.audioStates.size(), 4096000);
         }

         VarInt.write(buf, this.audioStates.size());

         for (Entry<Integer, AudioState> e : this.audioStates.entrySet()) {
            buf.writeIntLE(e.getKey());
            e.getValue().serialize(buf);
         }
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.audioStates != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.type.getValue());
      mem.set(PacketIO.PROTO_INT, (long)(offset + 2), this.maxId);
      int varOffset = offset + 6;
      if (this.audioStates != null) {
         if (this.audioStates.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("AudioStates", this.audioStates.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.audioStates.size());

         for (Entry<Integer, AudioState> e : this.audioStates.entrySet()) {
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
      if (this.audioStates != null) {
         int audioStatesSize = 0;

         for (Entry<Integer, AudioState> kvp : this.audioStates.entrySet()) {
            audioStatesSize += 4 + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.audioStates.size()) + audioStatesSize;
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
               int audioStatesCount = VarInt.peek(buffer, v);
               if (audioStatesCount < 0) {
                  return ValidationResult.error("Invalid dictionary count for AudioStates");
               }

               if (audioStatesCount > 4096000) {
                  return ValidationResult.error("AudioStates exceeds max length 4096000");
               }

               v += VarInt.size(audioStatesCount);

               for (int i = 0; i < audioStatesCount; i++) {
                  v += 4;
                  if (v > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading key");
                  }

                  v += AudioState.computeBytesConsumed(buffer, v);
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public UpdateAudioStates clone() {
      UpdateAudioStates copy = new UpdateAudioStates();
      copy.type = this.type;
      copy.maxId = this.maxId;
      if (this.audioStates != null) {
         Map<Integer, AudioState> m = new HashMap<>();

         for (Entry<Integer, AudioState> e : this.audioStates.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.audioStates = m;
      }

      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateAudioStates other)
            ? false
            : Objects.equals(this.type, other.type) && this.maxId == other.maxId && Objects.equals(this.audioStates, other.audioStates);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.maxId, this.audioStates);
   }
}
