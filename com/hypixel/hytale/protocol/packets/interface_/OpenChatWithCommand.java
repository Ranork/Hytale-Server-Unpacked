package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class OpenChatWithCommand implements Packet, ToClientPacket {
   public static final int PACKET_ID = 234;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 16384006;
   @Nullable
   public String command;

   @Override
   public int getId() {
      return 234;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public OpenChatWithCommand() {
   }

   public OpenChatWithCommand(@Nullable String command) {
      this.command = command;
   }

   public OpenChatWithCommand(@Nonnull OpenChatWithCommand other) {
      this.command = other.command;
   }

   @Nonnull
   public static OpenChatWithCommand deserialize(@Nonnull ByteBuf buf, int offset) {
      OpenChatWithCommand obj = new OpenChatWithCommand();
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int commandLen = VarInt.peek(buf, pos);
         if (commandLen < 0) {
            throw ProtocolException.negativeLength("Command", commandLen);
         }

         if (commandLen > 4096000) {
            throw ProtocolException.stringTooLong("Command", commandLen, 4096000);
         }

         int commandVarLen = VarInt.length(buf, pos);
         obj.command = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
         pos += commandVarLen + commandLen;
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
      if (this.command != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.command != null) {
         PacketIO.writeVarString(buf, this.command, 4096000);
      }
   }

   @Override
   public int computeSize() {
      int size = 1;
      if (this.command != null) {
         size += PacketIO.stringSize(this.command);
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
            int commandLen = VarInt.peek(buffer, pos);
            if (commandLen < 0) {
               return ValidationResult.error("Invalid string length for Command");
            }

            if (commandLen > 4096000) {
               return ValidationResult.error("Command exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += commandLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Command");
            }
         }

         return ValidationResult.OK;
      }
   }

   public OpenChatWithCommand clone() {
      OpenChatWithCommand copy = new OpenChatWithCommand();
      copy.command = this.command;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof OpenChatWithCommand other ? Objects.equals(this.command, other.command) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.command);
   }
}
