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

public class CommandTreeSync implements Packet, ToClientPacket {
   public static final int PACKET_ID = 238;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public CommandTreeEntry[] commands;

   @Override
   public int getId() {
      return 238;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public CommandTreeSync() {
   }

   public CommandTreeSync(@Nullable CommandTreeEntry[] commands) {
      this.commands = commands;
   }

   public CommandTreeSync(@Nonnull CommandTreeSync other) {
      this.commands = other.commands;
   }

   @Nonnull
   public static CommandTreeSync deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 1) {
         throw ProtocolException.bufferTooSmall("CommandTreeSync", 1, buf.readableBytes() - offset);
      } else {
         CommandTreeSync obj = new CommandTreeSync();
         byte nullBits = buf.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            int commandsCount = VarInt.peek(buf, pos);
            if (commandsCount < 0) {
               throw ProtocolException.invalidVarInt("Commands");
            }

            int commandsVarLen = VarInt.size(commandsCount);
            if (commandsCount > 4096000) {
               throw ProtocolException.arrayTooLong("Commands", commandsCount, 4096000);
            }

            if (pos + commandsVarLen + commandsCount * 2L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Commands", pos + commandsVarLen + commandsCount * 2, buf.readableBytes());
            }

            pos += commandsVarLen;
            obj.commands = new CommandTreeEntry[commandsCount];

            for (int i = 0; i < commandsCount; i++) {
               obj.commands[i] = CommandTreeEntry.deserialize(buf, pos);
               pos += CommandTreeEntry.computeBytesConsumed(buf, pos);
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
            pos += CommandTreeEntry.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 1L;
   }

   @Nullable
   public static CommandTreeEntry[] getCommands(MemorySegment mem) {
      return getCommands(mem, 0);
   }

   @Nullable
   public static CommandTreeEntry[] getCommands(MemorySegment mem, int offset) {
      if (!hasCommands(mem, offset)) {
         return null;
      } else {
         int off = offset + 1;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Commands", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Commands", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Commands", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               CommandTreeEntry[] data = new CommandTreeEntry[len];

               for (int i = 0; i < len; i++) {
                  data[i] = CommandTreeEntry.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   public static boolean hasCommands(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static CommandTreeSync toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static CommandTreeSync toObject(MemorySegment mem, int offset) {
      if (offset + 1 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("CommandTreeSync", offset + 1, (int)mem.byteSize());
      } else {
         CommandTreeEntry[] commands = null;
         if (hasCommands(mem, offset)) {
            int off = offset + 1;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Commands", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Commands", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Commands", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            commands = new CommandTreeEntry[len];

            for (int i = 0; i < len; i++) {
               commands[i] = CommandTreeEntry.toObject(mem, off);
               off += commands[i].computeSize();
            }
         }

         return new CommandTreeSync(commands);
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.commands != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.commands != null) {
         if (this.commands.length > 4096000) {
            throw ProtocolException.arrayTooLong("Commands", this.commands.length, 4096000);
         }

         VarInt.write(buf, this.commands.length);

         for (CommandTreeEntry item : this.commands) {
            item.serialize(buf);
         }
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.commands != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 1;
      if (this.commands != null) {
         if (this.commands.length > 4096000) {
            throw ProtocolException.arrayTooLong("Commands", this.commands.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.commands.length);
         int commandsValueOffset = 0;

         for (int i = 0; i < this.commands.length; i++) {
            commandsValueOffset += this.commands[i].serialize(mem, varOffset + commandsValueOffset);
         }

         varOffset += commandsValueOffset;
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 1;
      if (this.commands != null) {
         int commandsSize = 0;

         for (CommandTreeEntry elem : this.commands) {
            commandsSize += elem.computeSize();
         }

         size += VarInt.size(this.commands.length) + commandsSize;
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
            int commandsCount = VarInt.peek(buffer, pos);
            if (commandsCount < 0) {
               return ValidationResult.error("Invalid array count for Commands");
            }

            if (commandsCount > 4096000) {
               return ValidationResult.error("Commands exceeds max length 4096000");
            }

            pos += VarInt.size(commandsCount);

            for (int i = 0; i < commandsCount; i++) {
               ValidationResult structResult = CommandTreeEntry.validateStructure(buffer, pos);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid CommandTreeEntry in Commands[" + i + "]: " + structResult.error());
               }

               pos += CommandTreeEntry.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
      }
   }

   public CommandTreeSync clone() {
      CommandTreeSync copy = new CommandTreeSync();
      copy.commands = this.commands != null ? Arrays.stream(this.commands).map(e -> e.clone()).toArray(CommandTreeEntry[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof CommandTreeSync other ? Arrays.equals((Object[])this.commands, (Object[])other.commands) : false;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      return 31 * result + Arrays.hashCode((Object[])this.commands);
   }
}
