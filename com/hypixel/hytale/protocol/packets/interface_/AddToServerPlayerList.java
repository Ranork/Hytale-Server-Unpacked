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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AddToServerPlayerList implements Packet, ToClientPacket {
   public static final int PACKET_ID = 224;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public ServerPlayerListPlayer[] players;

   @Override
   public int getId() {
      return 224;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public AddToServerPlayerList() {
   }

   public AddToServerPlayerList(@Nullable ServerPlayerListPlayer[] players) {
      this.players = players;
   }

   public AddToServerPlayerList(@Nonnull AddToServerPlayerList other) {
      this.players = other.players;
   }

   @Nonnull
   public static AddToServerPlayerList deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 1) {
         throw ProtocolException.bufferTooSmall("AddToServerPlayerList", 1, buf.readableBytes() - offset);
      } else {
         AddToServerPlayerList obj = new AddToServerPlayerList();
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

            if (pos + playersVarLen + playersCount * 37L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Players", pos + playersVarLen + playersCount * 37, buf.readableBytes());
            }

            pos += playersVarLen;
            obj.players = new ServerPlayerListPlayer[playersCount];

            for (int i = 0; i < playersCount; i++) {
               obj.players[i] = ServerPlayerListPlayer.deserialize(buf, pos);
               pos += ServerPlayerListPlayer.computeBytesConsumed(buf, pos);
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos += ServerPlayerListPlayer.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 1L;
   }

   @Nullable
   public static ServerPlayerListPlayer[] getPlayers(MemorySegment mem) {
      return getPlayers(mem, 0);
   }

   @Nullable
   public static ServerPlayerListPlayer[] getPlayers(MemorySegment mem, int offset) {
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
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Players", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               ServerPlayerListPlayer[] data = new ServerPlayerListPlayer[len];

               for (int i = 0; i < len; i++) {
                  data[i] = ServerPlayerListPlayer.toObject(mem, off);
                  off += data[i].computeSize();
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

   public static AddToServerPlayerList toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AddToServerPlayerList toObject(MemorySegment mem, int offset) {
      if (offset + 1 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AddToServerPlayerList", offset + 1, (int)mem.byteSize());
      } else {
         ServerPlayerListPlayer[] players = null;
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
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Players", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            players = new ServerPlayerListPlayer[len];

            for (int i = 0; i < len; i++) {
               players[i] = ServerPlayerListPlayer.toObject(mem, off);
               off += players[i].computeSize();
            }
         }

         return new AddToServerPlayerList(players);
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

         for (ServerPlayerListPlayer item : this.players) {
            item.serialize(buf);
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
         int playersValueOffset = 0;

         for (int i = 0; i < this.players.length; i++) {
            playersValueOffset += this.players[i].serialize(mem, varOffset + playersValueOffset);
         }

         varOffset += playersValueOffset;
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 1;
      if (this.players != null) {
         int playersSize = 0;

         for (ServerPlayerListPlayer elem : this.players) {
            playersSize += elem.computeSize();
         }

         size += VarInt.size(this.players.length) + playersSize;
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

            for (int i = 0; i < playersCount; i++) {
               ValidationResult structResult = ServerPlayerListPlayer.validateStructure(buffer, pos);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid ServerPlayerListPlayer in Players[" + i + "]: " + structResult.error());
               }

               pos += ServerPlayerListPlayer.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
      }
   }

   public AddToServerPlayerList clone() {
      AddToServerPlayerList copy = new AddToServerPlayerList();
      copy.players = this.players != null ? Arrays.stream(this.players).map(e -> e.clone()).toArray(ServerPlayerListPlayer[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof AddToServerPlayerList other ? Arrays.equals((Object[])this.players, (Object[])other.players) : false;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      return 31 * result + Arrays.hashCode((Object[])this.players);
   }
}
