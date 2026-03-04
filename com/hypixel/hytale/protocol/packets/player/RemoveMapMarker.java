package com.hypixel.hytale.protocol.packets.player;

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

public class RemoveMapMarker implements Packet, ToServerPacket {
   public static final int PACKET_ID = 119;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 16384006;
   @Nullable
   public String markerId;

   @Override
   public int getId() {
      return 119;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public RemoveMapMarker() {
   }

   public RemoveMapMarker(@Nullable String markerId) {
      this.markerId = markerId;
   }

   public RemoveMapMarker(@Nonnull RemoveMapMarker other) {
      this.markerId = other.markerId;
   }

   @Nonnull
   public static RemoveMapMarker deserialize(@Nonnull ByteBuf buf, int offset) {
      RemoveMapMarker obj = new RemoveMapMarker();
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int markerIdLen = VarInt.peek(buf, pos);
         if (markerIdLen < 0) {
            throw ProtocolException.negativeLength("MarkerId", markerIdLen);
         }

         if (markerIdLen > 4096000) {
            throw ProtocolException.stringTooLong("MarkerId", markerIdLen, 4096000);
         }

         int markerIdVarLen = VarInt.length(buf, pos);
         obj.markerId = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
         pos += markerIdVarLen + markerIdLen;
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
      if (this.markerId != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.markerId != null) {
         PacketIO.writeVarString(buf, this.markerId, 4096000);
      }
   }

   @Override
   public int computeSize() {
      int size = 1;
      if (this.markerId != null) {
         size += PacketIO.stringSize(this.markerId);
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
            int markerIdLen = VarInt.peek(buffer, pos);
            if (markerIdLen < 0) {
               return ValidationResult.error("Invalid string length for MarkerId");
            }

            if (markerIdLen > 4096000) {
               return ValidationResult.error("MarkerId exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += markerIdLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading MarkerId");
            }
         }

         return ValidationResult.OK;
      }
   }

   public RemoveMapMarker clone() {
      RemoveMapMarker copy = new RemoveMapMarker();
      copy.markerId = this.markerId;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof RemoveMapMarker other ? Objects.equals(this.markerId, other.markerId) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.markerId);
   }
}
