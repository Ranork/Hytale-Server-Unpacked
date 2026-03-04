package com.hypixel.hytale.protocol.packets.interface_;

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

public class CustomPageEvent implements Packet, ToServerPacket {
   public static final int PACKET_ID = 219;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 2;
   public static final int MAX_SIZE = 16384007;
   @Nonnull
   public CustomPageEventType type = CustomPageEventType.Acknowledge;
   @Nullable
   public String data;

   @Override
   public int getId() {
      return 219;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public CustomPageEvent() {
   }

   public CustomPageEvent(@Nonnull CustomPageEventType type, @Nullable String data) {
      this.type = type;
      this.data = data;
   }

   public CustomPageEvent(@Nonnull CustomPageEvent other) {
      this.type = other.type;
      this.data = other.data;
   }

   @Nonnull
   public static CustomPageEvent deserialize(@Nonnull ByteBuf buf, int offset) {
      CustomPageEvent obj = new CustomPageEvent();
      byte nullBits = buf.getByte(offset);
      obj.type = CustomPageEventType.fromValue(buf.getByte(offset + 1));
      int pos = offset + 2;
      if ((nullBits & 1) != 0) {
         int dataLen = VarInt.peek(buf, pos);
         if (dataLen < 0) {
            throw ProtocolException.negativeLength("Data", dataLen);
         }

         if (dataLen > 4096000) {
            throw ProtocolException.stringTooLong("Data", dataLen, 4096000);
         }

         int dataVarLen = VarInt.length(buf, pos);
         obj.data = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
         pos += dataVarLen + dataLen;
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 2;
      if ((nullBits & 1) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos) + sl;
      }

      return pos - offset;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.data != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      if (this.data != null) {
         PacketIO.writeVarString(buf, this.data, 4096000);
      }
   }

   @Override
   public int computeSize() {
      int size = 2;
      if (this.data != null) {
         size += PacketIO.stringSize(this.data);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 2) {
         return ValidationResult.error("Buffer too small: expected at least 2 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 2;
         if ((nullBits & 1) != 0) {
            int dataLen = VarInt.peek(buffer, pos);
            if (dataLen < 0) {
               return ValidationResult.error("Invalid string length for Data");
            }

            if (dataLen > 4096000) {
               return ValidationResult.error("Data exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += dataLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Data");
            }
         }

         return ValidationResult.OK;
      }
   }

   public CustomPageEvent clone() {
      CustomPageEvent copy = new CustomPageEvent();
      copy.type = this.type;
      copy.data = this.data;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof CustomPageEvent other) ? false : Objects.equals(this.type, other.type) && Objects.equals(this.data, other.data);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.data);
   }
}
