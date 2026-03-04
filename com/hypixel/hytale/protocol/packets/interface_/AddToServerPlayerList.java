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
      AddToServerPlayerList obj = new AddToServerPlayerList();
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

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos);

         for (int i = 0; i < arrLen; i++) {
            pos += ServerPlayerListPlayer.computeBytesConsumed(buf, pos);
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

         for (ServerPlayerListPlayer item : this.players) {
            item.serialize(buf);
         }
      }
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

            pos += VarInt.length(buffer, pos);

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
