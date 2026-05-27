package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.MusicContainer;
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

public class UpdateMusicContainers implements Packet, ToClientPacket {
   public static final int PACKET_ID = 88;
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
   public Map<Integer, MusicContainer> musicContainers;

   @Override
   public int getId() {
      return 88;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateMusicContainers() {
   }

   public UpdateMusicContainers(@Nonnull UpdateType type, int maxId, @Nullable Map<Integer, MusicContainer> musicContainers) {
      this.type = type;
      this.maxId = maxId;
      this.musicContainers = musicContainers;
   }

   public UpdateMusicContainers(@Nonnull UpdateMusicContainers other) {
      this.type = other.type;
      this.maxId = other.maxId;
      this.musicContainers = other.musicContainers;
   }

   @Nonnull
   public static UpdateMusicContainers deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 6) {
         throw ProtocolException.bufferTooSmall("UpdateMusicContainers", 6, buf.readableBytes() - offset);
      } else {
         UpdateMusicContainers obj = new UpdateMusicContainers();
         byte nullBits = buf.getByte(offset);
         obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
         obj.maxId = buf.getIntLE(offset + 2);
         int pos = offset + 6;
         if ((nullBits & 1) != 0) {
            int musicContainersCount = VarInt.peek(buf, pos);
            if (musicContainersCount < 0) {
               throw ProtocolException.invalidVarInt("MusicContainers");
            }

            int musicContainersVarLen = VarInt.size(musicContainersCount);
            if (musicContainersCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("MusicContainers", musicContainersCount, 4096000);
            }

            pos += musicContainersVarLen;
            obj.musicContainers = new HashMap<>(musicContainersCount);

            for (int i = 0; i < musicContainersCount; i++) {
               int key = buf.getIntLE(pos);
               pos += 4;
               MusicContainer val = MusicContainer.deserialize(buf, pos);
               pos += MusicContainer.computeBytesConsumed(buf, pos);
               if (obj.musicContainers.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("musicContainers", key);
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
            pos += MusicContainer.computeBytesConsumed(buf, pos);
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
   public static Map<Integer, MusicContainer> getMusicContainers(MemorySegment mem) {
      return getMusicContainers(mem, 0);
   }

   @Nullable
   public static Map<Integer, MusicContainer> getMusicContainers(MemorySegment mem, int offset) {
      if (!hasMusicContainers(mem, offset)) {
         return null;
      } else {
         int off = offset + 6;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("MusicContainers", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("MusicContainers", len, 4096000);
         } else {
            Map<Integer, MusicContainer> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               MusicContainer value = MusicContainer.toObject(mem, off);
               off += value.computeSizeWithTypeId();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("MusicContainers", key);
               }
            }

            return data;
         }
      }
   }

   public static boolean hasMusicContainers(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static UpdateMusicContainers toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UpdateMusicContainers toObject(MemorySegment mem, int offset) {
      if (offset + 6 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UpdateMusicContainers", offset + 6, (int)mem.byteSize());
      } else {
         Map<Integer, MusicContainer> musicContainers = null;
         if (hasMusicContainers(mem, offset)) {
            int off = offset + 6;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("MusicContainers", len);
            }

            if (len > 4096000) {
               throw ProtocolException.dictionaryTooLarge("MusicContainers", len, 4096000);
            }

            musicContainers = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               MusicContainer value = MusicContainer.toObject(mem, off);
               off += value.computeSizeWithTypeId();
               if (musicContainers.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("MusicContainers", key);
               }
            }
         }

         return new UpdateMusicContainers(
            UpdateType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))), mem.get(PacketIO.PROTO_INT, (long)(offset + 2)), musicContainers
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.musicContainers != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      buf.writeIntLE(this.maxId);
      if (this.musicContainers != null) {
         if (this.musicContainers.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("MusicContainers", this.musicContainers.size(), 4096000);
         }

         VarInt.write(buf, this.musicContainers.size());

         for (Entry<Integer, MusicContainer> e : this.musicContainers.entrySet()) {
            buf.writeIntLE(e.getKey());
            e.getValue().serializeWithTypeId(buf);
         }
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.musicContainers != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.type.getValue());
      mem.set(PacketIO.PROTO_INT, (long)(offset + 2), this.maxId);
      int varOffset = offset + 6;
      if (this.musicContainers != null) {
         if (this.musicContainers.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("MusicContainers", this.musicContainers.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.musicContainers.size());

         for (Entry<Integer, MusicContainer> e : this.musicContainers.entrySet()) {
            mem.set(PacketIO.PROTO_INT, (long)varOffset, e.getKey());
            varOffset += 4;
            varOffset += e.getValue().serializeWithTypeId(mem, varOffset);
         }
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 6;
      if (this.musicContainers != null) {
         int musicContainersSize = 0;

         for (Entry<Integer, MusicContainer> kvp : this.musicContainers.entrySet()) {
            musicContainersSize += 4 + kvp.getValue().computeSizeWithTypeId();
         }

         size += VarInt.size(this.musicContainers.size()) + musicContainersSize;
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
               int musicContainersCount = VarInt.peek(buffer, v);
               if (musicContainersCount < 0) {
                  return ValidationResult.error("Invalid dictionary count for MusicContainers");
               }

               if (musicContainersCount > 4096000) {
                  return ValidationResult.error("MusicContainers exceeds max length 4096000");
               }

               v += VarInt.size(musicContainersCount);

               for (int i = 0; i < musicContainersCount; i++) {
                  v += 4;
                  if (v > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading key");
                  }

                  v += MusicContainer.computeBytesConsumed(buffer, v);
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public UpdateMusicContainers clone() {
      UpdateMusicContainers copy = new UpdateMusicContainers();
      copy.type = this.type;
      copy.maxId = this.maxId;
      copy.musicContainers = this.musicContainers != null ? new HashMap<>(this.musicContainers) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateMusicContainers other)
            ? false
            : Objects.equals(this.type, other.type) && this.maxId == other.maxId && Objects.equals(this.musicContainers, other.musicContainers);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.maxId, this.musicContainers);
   }
}
