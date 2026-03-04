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

public class CustomHud implements Packet, ToClientPacket {
   public static final int PACKET_ID = 217;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 2;
   public static final int MAX_SIZE = 1677721600;
   public boolean clear;
   @Nullable
   public CustomUICommand[] commands;

   @Override
   public int getId() {
      return 217;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public CustomHud() {
   }

   public CustomHud(boolean clear, @Nullable CustomUICommand[] commands) {
      this.clear = clear;
      this.commands = commands;
   }

   public CustomHud(@Nonnull CustomHud other) {
      this.clear = other.clear;
      this.commands = other.commands;
   }

   @Nonnull
   public static CustomHud deserialize(@Nonnull ByteBuf buf, int offset) {
      CustomHud obj = new CustomHud();
      byte nullBits = buf.getByte(offset);
      obj.clear = buf.getByte(offset + 1) != 0;
      int pos = offset + 2;
      if ((nullBits & 1) != 0) {
         int commandsCount = VarInt.peek(buf, pos);
         if (commandsCount < 0) {
            throw ProtocolException.negativeLength("Commands", commandsCount);
         }

         if (commandsCount > 4096000) {
            throw ProtocolException.arrayTooLong("Commands", commandsCount, 4096000);
         }

         int commandsVarLen = VarInt.size(commandsCount);
         if (pos + commandsVarLen + commandsCount * 2L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Commands", pos + commandsVarLen + commandsCount * 2, buf.readableBytes());
         }

         pos += commandsVarLen;
         obj.commands = new CustomUICommand[commandsCount];

         for (int i = 0; i < commandsCount; i++) {
            obj.commands[i] = CustomUICommand.deserialize(buf, pos);
            pos += CustomUICommand.computeBytesConsumed(buf, pos);
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 2;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos);

         for (int i = 0; i < arrLen; i++) {
            pos += CustomUICommand.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.commands != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.clear ? 1 : 0);
      if (this.commands != null) {
         if (this.commands.length > 4096000) {
            throw ProtocolException.arrayTooLong("Commands", this.commands.length, 4096000);
         }

         VarInt.write(buf, this.commands.length);

         for (CustomUICommand item : this.commands) {
            item.serialize(buf);
         }
      }
   }

   @Override
   public int computeSize() {
      int size = 2;
      if (this.commands != null) {
         int commandsSize = 0;

         for (CustomUICommand elem : this.commands) {
            commandsSize += elem.computeSize();
         }

         size += VarInt.size(this.commands.length) + commandsSize;
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
            int commandsCount = VarInt.peek(buffer, pos);
            if (commandsCount < 0) {
               return ValidationResult.error("Invalid array count for Commands");
            }

            if (commandsCount > 4096000) {
               return ValidationResult.error("Commands exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for (int i = 0; i < commandsCount; i++) {
               ValidationResult structResult = CustomUICommand.validateStructure(buffer, pos);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid CustomUICommand in Commands[" + i + "]: " + structResult.error());
               }

               pos += CustomUICommand.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
      }
   }

   public CustomHud clone() {
      CustomHud copy = new CustomHud();
      copy.clear = this.clear;
      copy.commands = this.commands != null ? Arrays.stream(this.commands).map(e -> e.clone()).toArray(CustomUICommand[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof CustomHud other) ? false : this.clear == other.clear && Arrays.equals((Object[])this.commands, (Object[])other.commands);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Boolean.hashCode(this.clear);
      return 31 * result + Arrays.hashCode((Object[])this.commands);
   }
}
