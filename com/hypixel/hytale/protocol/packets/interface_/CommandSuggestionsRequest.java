package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CommandSuggestionsRequest implements Packet, ToServerPacket {
   public static final int PACKET_ID = 236;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 9;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 16384014;
   @Nullable
   public String command;
   public int cursorPosition;
   public int selectedVariant;

   @Override
   public int getId() {
      return 236;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public CommandSuggestionsRequest() {
   }

   public CommandSuggestionsRequest(@Nullable String command, int cursorPosition, int selectedVariant) {
      this.command = command;
      this.cursorPosition = cursorPosition;
      this.selectedVariant = selectedVariant;
   }

   public CommandSuggestionsRequest(@Nonnull CommandSuggestionsRequest other) {
      this.command = other.command;
      this.cursorPosition = other.cursorPosition;
      this.selectedVariant = other.selectedVariant;
   }

   @Nonnull
   public static CommandSuggestionsRequest deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 9) {
         throw ProtocolException.bufferTooSmall("CommandSuggestionsRequest", 9, buf.readableBytes() - offset);
      } else {
         CommandSuggestionsRequest obj = new CommandSuggestionsRequest();
         byte nullBits = buf.getByte(offset);
         obj.cursorPosition = buf.getIntLE(offset + 1);
         obj.selectedVariant = buf.getIntLE(offset + 5);
         int pos = offset + 9;
         if ((nullBits & 1) != 0) {
            int commandLen = VarInt.peek(buf, pos);
            if (commandLen < 0) {
               throw ProtocolException.invalidVarInt("Command");
            }

            int commandVarLen = VarInt.size(commandLen);
            if (commandLen > 4096000) {
               throw ProtocolException.stringTooLong("Command", commandLen, 4096000);
            }

            if (pos + commandVarLen + commandLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Command", pos + commandVarLen + commandLen, buf.readableBytes());
            }

            obj.command = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
            pos += commandVarLen + commandLen;
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 9;
      if ((nullBits & 1) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.size(sl) + sl;
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 9L;
   }

   @Nullable
   public static String getCommand(MemorySegment mem) {
      return getCommand(mem, 0);
   }

   @Nullable
   public static String getCommand(MemorySegment mem, int offset) {
      return hasCommand(mem, offset) ? PacketIO.readVarString("Command", mem, offset + 9, 4096000, PacketIO.UTF8) : null;
   }

   public static int getCursorPosition(MemorySegment mem) {
      return getCursorPosition(mem, 0);
   }

   public static int getCursorPosition(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   public static int getSelectedVariant(MemorySegment mem) {
      return getSelectedVariant(mem, 0);
   }

   public static int getSelectedVariant(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 5));
   }

   public static boolean hasCommand(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static CommandSuggestionsRequest toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static CommandSuggestionsRequest toObject(MemorySegment mem, int offset) {
      if (offset + 9 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("CommandSuggestionsRequest", offset + 9, (int)mem.byteSize());
      } else {
         return new CommandSuggestionsRequest(
            hasCommand(mem, offset) ? PacketIO.readVarString("Command", mem, offset + 9, 4096000, PacketIO.UTF8) : null,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 5))
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.command != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.cursorPosition);
      buf.writeIntLE(this.selectedVariant);
      if (this.command != null) {
         PacketIO.writeVarString(buf, this.command, 4096000);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.command != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.cursorPosition);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 5), this.selectedVariant);
      int varOffset = offset + 9;
      if (this.command != null) {
         varOffset += PacketIO.writeVarString(mem, varOffset, this.command, 4096000);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 9;
      if (this.command != null) {
         size += PacketIO.stringSize(this.command);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 9) {
         return ValidationResult.error("Buffer too small: expected at least 9 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 9;
         if ((nullBits & 1) != 0) {
            int commandLen = VarInt.peek(buffer, pos);
            if (commandLen < 0) {
               return ValidationResult.error("Invalid string length for Command");
            }

            if (commandLen > 4096000) {
               return ValidationResult.error("Command exceeds max length 4096000");
            }

            pos += VarInt.size(commandLen);
            pos += commandLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Command");
            }
         }

         return ValidationResult.OK;
      }
   }

   public CommandSuggestionsRequest clone() {
      CommandSuggestionsRequest copy = new CommandSuggestionsRequest();
      copy.command = this.command;
      copy.cursorPosition = this.cursorPosition;
      copy.selectedVariant = this.selectedVariant;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof CommandSuggestionsRequest other)
            ? false
            : Objects.equals(this.command, other.command) && this.cursorPosition == other.cursorPosition && this.selectedVariant == other.selectedVariant;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.command, this.cursorPosition, this.selectedVariant);
   }
}
