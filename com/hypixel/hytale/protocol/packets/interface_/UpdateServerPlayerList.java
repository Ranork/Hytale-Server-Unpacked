package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UpdateServerPlayerList implements Packet, ToClientPacket {
   public static final int PACKET_ID = 226;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 131072006;
   @Nullable
   public ServerPlayerListUpdate[] players;

   @Override
   public int getId() {
      return 226;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateServerPlayerList() {
   }

   public UpdateServerPlayerList(@Nullable ServerPlayerListUpdate[] players) {
      this.players = players;
   }

   public UpdateServerPlayerList(@Nonnull UpdateServerPlayerList other) {
      this.players = other.players;
   }

   @Nonnull
   public static UpdateServerPlayerList deserialize(@Nonnull ByteBuf buf, int offset) {
      UpdateServerPlayerList obj = new UpdateServerPlayerList();
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
         if (pos + playersVarLen + playersCount * 32L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Players", pos + playersVarLen + playersCount * 32, buf.readableBytes());
         }

         pos += playersVarLen;
         obj.players = new ServerPlayerListUpdate[playersCount];

         for (int i = 0; i < playersCount; i++) {
            obj.players[i] = ServerPlayerListUpdate.deserialize(buf, pos);
            pos += ServerPlayerListUpdate.computeBytesConsumed(buf, pos);
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos);

         for (int i = 0; i < arrLen; i++) {
            pos += ServerPlayerListUpdate.computeBytesConsumed(buf, pos);
         }
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

         for (ServerPlayerListUpdate item : this.players) {
            item.serialize(buf);
         }
      }
   }

   @Override
   public int computeSize() {
      int size = 1;
      if (this.players != null) {
         size += VarInt.size(this.players.length) + this.players.length * 32;
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
            pos += playersCount * 32;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Players");
            }
         }

         return ValidationResult.OK;
      }
   }

   public UpdateServerPlayerList clone() {
      UpdateServerPlayerList copy = new UpdateServerPlayerList();
      copy.players = this.players != null ? Arrays.stream(this.players).map(e -> e.clone()).toArray(ServerPlayerListUpdate[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof UpdateServerPlayerList other ? Arrays.equals((Object[])this.players, (Object[])other.players) : false;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      return 31 * result + Arrays.hashCode((Object[])this.players);
   }
}
