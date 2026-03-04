package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
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
      RemoveFromServerPlayerList obj = new RemoveFromServerPlayerList();
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int playersCount = VarInt.peek(buf, pos);
         if (playersCount < 0) {
            throw ProtocolException.negativeLength("Players", playersCount);
         }

         if (playersCount > 4096000) {
            throw ProtocolException.arrayTooLong("Players", playersCount, 4096000);
         }

         int playersVarLen = VarInt.size(playersCount);
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

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos) + arrLen * 16;
      }

      return pos - offset;
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

            pos += VarInt.length(buffer, pos);
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
