package com.hypixel.hytale.protocol.packets.worldmap;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TeleportToWorldMapMarker implements Packet, ToServerPacket {
   public static final int PACKET_ID = 244;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 16384006;
   @Nullable
   public String id;

   @Override
   public int getId() {
      return 244;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public TeleportToWorldMapMarker() {
   }

   public TeleportToWorldMapMarker(@Nullable String id) {
      this.id = id;
   }

   public TeleportToWorldMapMarker(@Nonnull TeleportToWorldMapMarker other) {
      this.id = other.id;
   }

   @Nonnull
   public static TeleportToWorldMapMarker deserialize(@Nonnull ByteBuf buf, int offset) {
      TeleportToWorldMapMarker obj = new TeleportToWorldMapMarker();
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int idLen = VarInt.peek(buf, pos);
         if (idLen < 0) {
            throw ProtocolException.negativeLength("Id", idLen);
         }

         if (idLen > 4096000) {
            throw ProtocolException.stringTooLong("Id", idLen, 4096000);
         }

         int idVarLen = VarInt.length(buf, pos);
         obj.id = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
         pos += idVarLen + idLen;
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos) + sl;
      }

      return pos - offset;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.id != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.id != null) {
         PacketIO.writeVarString(buf, this.id, 4096000);
      }
   }

   @Override
   public int computeSize() {
      int size = 1;
      if (this.id != null) {
         size += PacketIO.stringSize(this.id);
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
            int idLen = VarInt.peek(buffer, pos);
            if (idLen < 0) {
               return ValidationResult.error("Invalid string length for Id");
            }

            if (idLen > 4096000) {
               return ValidationResult.error("Id exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += idLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Id");
            }
         }

         return ValidationResult.OK;
      }
   }

   public TeleportToWorldMapMarker clone() {
      TeleportToWorldMapMarker copy = new TeleportToWorldMapMarker();
      copy.id = this.id;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof TeleportToWorldMapMarker other ? Objects.equals(this.id, other.id) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.id);
   }
}
