package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class UICommandDataValue extends UIDataValue {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 0;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 0;
   public static final int MAX_SIZE = 16384005;
   @Nonnull
   public String commandId = "";

   public UICommandDataValue() {
   }

   public UICommandDataValue(@Nonnull String commandId) {
      this.commandId = commandId;
   }

   public UICommandDataValue(@Nonnull UICommandDataValue other) {
      this.commandId = other.commandId;
   }

   @Nonnull
   public static UICommandDataValue deserialize(@Nonnull ByteBuf buf, int offset) {
      UICommandDataValue obj = new UICommandDataValue();
      int pos = offset + 0;
      int commandIdLen = VarInt.peek(buf, pos);
      if (commandIdLen < 0) {
         throw ProtocolException.invalidVarInt("CommandId");
      } else {
         int commandIdVarLen = VarInt.size(commandIdLen);
         if (commandIdLen > 4096000) {
            throw ProtocolException.stringTooLong("CommandId", commandIdLen, 4096000);
         } else if (pos + commandIdVarLen + commandIdLen > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("CommandId", pos + commandIdVarLen + commandIdLen, buf.readableBytes());
         } else {
            obj.commandId = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
            pos += commandIdVarLen + commandIdLen;
            return obj;
         }
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      int pos = offset + 0;
      int sl = VarInt.peek(buf, pos);
      pos += VarInt.size(sl) + sl;
      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 0L;
   }

   public static String getCommandId(MemorySegment mem) {
      return getCommandId(mem, 0);
   }

   public static String getCommandId(MemorySegment mem, int offset) {
      return PacketIO.readVarString("CommandId", mem, offset + 0, 4096000, PacketIO.UTF8);
   }

   public static UICommandDataValue toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UICommandDataValue toObject(MemorySegment mem, int offset) {
      if (offset + 0 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UICommandDataValue", offset + 0, (int)mem.byteSize());
      } else {
         return new UICommandDataValue(PacketIO.readVarString("CommandId", mem, offset + 0, 4096000, PacketIO.UTF8));
      }
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      PacketIO.writeVarString(buf, this.commandId, 4096000);
      return buf.writerIndex() - startPos;
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      int varOffset = offset + 0;
      varOffset += PacketIO.writeVarString(mem, varOffset, this.commandId, 4096000);
      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 0;
      return size + PacketIO.stringSize(this.commandId);
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 0) {
         return ValidationResult.error("Buffer too small: expected at least 0 bytes");
      } else {
         int pos = offset + 0;
         int commandIdLen = VarInt.peek(buffer, pos);
         if (commandIdLen < 0) {
            return ValidationResult.error("Invalid string length for CommandId");
         } else if (commandIdLen > 4096000) {
            return ValidationResult.error("CommandId exceeds max length 4096000");
         } else {
            pos += VarInt.size(commandIdLen);
            pos += commandIdLen;
            return pos > buffer.writerIndex() ? ValidationResult.error("Buffer overflow reading CommandId") : ValidationResult.OK;
         }
      }
   }

   public UICommandDataValue clone() {
      UICommandDataValue copy = new UICommandDataValue();
      copy.commandId = this.commandId;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof UICommandDataValue other ? Objects.equals(this.commandId, other.commandId) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.commandId);
   }
}
