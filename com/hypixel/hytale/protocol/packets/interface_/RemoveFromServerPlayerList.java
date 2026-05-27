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
import java.util.Arrays;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RemoveFromServerPlayerList implements Packet, ToClientPacket {
   public static final int PACKET_ID = 225;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 65536006;
   @Nullable
   public UUID[] players;

   @Override
   public int getId() {
      return 225;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public RemoveFromServerPlayerList() {
   }

   public RemoveFromServerPlayerList(@Nullable UUID[] players) {
      this.players = players;
   }

   public RemoveFromServerPlayerList(@Nonnull RemoveFromServerPlayerList other) {
      this.players = other.players;
   }

   @Nonnull
   public static RemoveFromServerPlayerList deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 1) {
         throw ProtocolException.bufferTooSmall("RemoveFromServerPlayerList", 1, buf.readableBytes() - offset);
      } else {
         RemoveFromServerPlayerList obj = new RemoveFromServerPlayerList();
         byte nullBits = buf.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            int playersCount = VarInt.peek(buf, pos);
            if (playersCount < 0) {
               throw ProtocolException.invalidVarInt("Players");
            }

            int playersVarLen = VarInt.size(playersCount);
            if (playersCount > 4096000) {
               throw ProtocolException.arrayTooLong("Players", playersCount, 4096000);
            }

            if (pos + playersVarLen + playersCount * 16L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Players", pos + playersVarLen + playersCount * 16, buf.readableBytes());
            }

            pos += playersVarLen;
            obj.players = new UUID[playersCount];

            for (int i = 0; i < playersCount; i++) {
               obj.players[i] = PacketIO.readUUID(buf, pos + i * 16);
            }

            pos += playersCount * 16;
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.size(arrLen) + arrLen * 16;
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 1L;
   }

   @Nullable
   public static UUID[] getPlayers(MemorySegment mem) {
      return getPlayers(mem, 0);
   }

   @Nullable
   public static UUID[] getPlayers(MemorySegment mem, int offset) {
      if (!hasPlayers(mem, offset)) {
         return null;
      } else {
         int off = offset + 1;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Players", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Players", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 16L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Players", off + lenOffset + len * 16, (int)mem.byteSize());
            } else {
               off += lenOffset;
               UUID[] data = new UUID[len];

               for (int i = 0; i < len; i++) {
                  data[i] = PacketIO.readUUID(mem, off + i * 16);
               }

               return data;
            }
         }
      }
   }

   public static boolean hasPlayers(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static RemoveFromServerPlayerList toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static RemoveFromServerPlayerList toObject(MemorySegment mem, int offset) {
      if (offset + 1 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("RemoveFromServerPlayerList", offset + 1, (int)mem.byteSize());
      } else {
         UUID[] players = null;
         if (hasPlayers(mem, offset)) {
            int off = offset + 1;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Players", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Players", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 16L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Players", off + lenOffset + len * 16, (int)mem.byteSize());
            }

            off += lenOffset;
            players = new UUID[len];

            for (int i = 0; i < len; i++) {
               players[i] = PacketIO.readUUID(mem, off + i * 16);
            }
         }

         return new RemoveFromServerPlayerList(players);
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
         if (this.players.length > 4096000) {
            throw ProtocolException.arrayTooLong("Players", this.players.length, 4096000);
         }

         VarInt.write(buf, this.players.length);

         for (UUID item : this.players) {
            PacketIO.writeUUID(buf, item);
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
         if (this.players.length > 4096000) {
            throw ProtocolException.arrayTooLong("Players", this.players.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.players.length);

         for (int i = 0; i < this.players.length; i++) {
            PacketIO.writeUUID(mem, varOffset + i * 16, this.players[i]);
         }

         varOffset += this.players.length * 16;
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 1;
      if (this.players != null) {
         size += VarInt.size(this.players.length) + this.players.length * 16;
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
               return ValidationResult.error("Invalid array count for Players");
            }

            if (playersCount > 4096000) {
               return ValidationResult.error("Players exceeds max length 4096000");
            }

            pos += VarInt.size(playersCount);
            pos += playersCount * 16;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Players");
            }
         }

         return ValidationResult.OK;
      }
   }

   public RemoveFromServerPlayerList clone() {
      RemoveFromServerPlayerList copy = new RemoveFromServerPlayerList();
      copy.players = this.players != null ? Arrays.copyOf(this.players, this.players.length) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof RemoveFromServerPlayerList other ? Arrays.equals((Object[])this.players, (Object[])other.players) : false;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      return 31 * result + Arrays.hashCode((Object[])this.players);
   }
}
