package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UpdateServerPlayerListPing implements Packet, ToClientPacket {
   public static final int PACKET_ID = 227;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 81920006;
   @Nullable
   public Map<UUID, Integer> players;

   @Override
   public int getId() {
      return 227;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateServerPlayerListPing() {
   }

   public UpdateServerPlayerListPing(@Nullable Map<UUID, Integer> players) {
      this.players = players;
   }

   public UpdateServerPlayerListPing(@Nonnull UpdateServerPlayerListPing other) {
      this.players = other.players;
   }

   @Nonnull
   public static UpdateServerPlayerListPing deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 1) {
         throw ProtocolException.bufferTooSmall("UpdateServerPlayerListPing", 1, buf.readableBytes() - offset);
      } else {
         UpdateServerPlayerListPing obj = new UpdateServerPlayerListPing();
         byte nullBits = buf.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            int playersCount = VarInt.peek(buf, pos);
            if (playersCount < 0) {
               throw ProtocolException.invalidVarInt("Players");
            }

            int playersVarLen = VarInt.size(playersCount);
            if (playersCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Players", playersCount, 4096000);
            }

            pos += playersVarLen;
            obj.players = new HashMap<>(playersCount);

            for (int i = 0; i < playersCount; i++) {
               UUID key = PacketIO.readUUID(buf, pos);
               pos += 16;
               int val = buf.getIntLE(pos);
               pos += 4;
               if (obj.players.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("players", key);
               }
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int dictLen = VarInt.peek(buf, pos);
         pos += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos += 16;
            pos += 4;
         }
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 1L;
   }

   @Nullable
   public static Map<UUID, Integer> getPlayers(MemorySegment mem) {
      return getPlayers(mem, 0);
   }

   @Nullable
   public static Map<UUID, Integer> getPlayers(MemorySegment mem, int offset) {
      if (!hasPlayers(mem, offset)) {
         return null;
      } else {
         int off = offset + 1;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Players", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Players", len, 4096000);
         } else {
            Map<UUID, Integer> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               UUID key = PacketIO.readUUID(mem, off);
               off += 16;
               int value = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Players", key);
               }
            }

            return data;
         }
      }
   }

   public static boolean hasPlayers(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static UpdateServerPlayerListPing toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UpdateServerPlayerListPing toObject(MemorySegment mem, int offset) {
      if (offset + 1 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UpdateServerPlayerListPing", offset + 1, (int)mem.byteSize());
      } else {
         Map<UUID, Integer> players = null;
         if (hasPlayers(mem, offset)) {
            int off = offset + 1;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Players", len);
            }

            if (len > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Players", len, 4096000);
            }

            players = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               UUID key = PacketIO.readUUID(mem, off);
               off += 16;
               int value = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               if (players.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Players", key);
               }
            }
         }

         return new UpdateServerPlayerListPing(players);
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.players != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.players != null) {
         if (this.players.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Players", this.players.size(), 4096000);
         }

         VarInt.write(buf, this.players.size());

         for (Entry<UUID, Integer> e : this.players.entrySet()) {
            PacketIO.writeUUID(buf, e.getKey());
            buf.writeIntLE(e.getValue());
         }
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.players != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 1;
      if (this.players != null) {
         if (this.players.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Players", this.players.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.players.size());

         for (Entry<UUID, Integer> e : this.players.entrySet()) {
            PacketIO.writeUUID(mem, varOffset, e.getKey());
            varOffset += 16;
            mem.set(PacketIO.PROTO_INT, (long)varOffset, e.getValue());
            varOffset += 4;
         }
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 1;
      if (this.players != null) {
         size += VarInt.size(this.players.size()) + this.players.size() * 20;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 1) {
         return ValidationResult.error("Buffer too small: expected at least 1 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            int playersCount = VarInt.peek(buffer, pos);
            if (playersCount < 0) {
               return ValidationResult.error("Invalid dictionary count for Players");
            }

            if (playersCount > 4096000) {
               return ValidationResult.error("Players exceeds max length 4096000");
            }

            pos += VarInt.size(playersCount);

            for (int i = 0; i < playersCount; i++) {
               pos += 16;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               pos += 4;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading value");
               }
            }
         }

         return ValidationResult.OK;
      }
   }

   public UpdateServerPlayerListPing clone() {
      UpdateServerPlayerListPing copy = new UpdateServerPlayerListPing();
      copy.players = this.players != null ? new HashMap<>(this.players) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof UpdateServerPlayerListPing other ? Objects.equals(this.players, other.players) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.players);
   }
}
