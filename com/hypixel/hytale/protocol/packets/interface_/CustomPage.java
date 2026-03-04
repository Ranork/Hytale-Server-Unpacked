package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CustomPage implements Packet, ToClientPacket {
   public static final int PACKET_ID = 218;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 4;
   public static final int VARIABLE_FIELD_COUNT = 3;
   public static final int VARIABLE_BLOCK_START = 16;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public String key;
   public boolean isInitial;
   public boolean clear;
   @Nonnull
   public CustomPageLifetime lifetime = CustomPageLifetime.CantClose;
   @Nullable
   public CustomUICommand[] commands;
   @Nullable
   public CustomUIEventBinding[] eventBindings;

   @Override
   public int getId() {
      return 218;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public CustomPage() {
   }

   public CustomPage(
      @Nullable String key,
      boolean isInitial,
      boolean clear,
      @Nonnull CustomPageLifetime lifetime,
      @Nullable CustomUICommand[] commands,
      @Nullable CustomUIEventBinding[] eventBindings
   ) {
      this.key = key;
      this.isInitial = isInitial;
      this.clear = clear;
      this.lifetime = lifetime;
      this.commands = commands;
      this.eventBindings = eventBindings;
   }

   public CustomPage(@Nonnull CustomPage other) {
      this.key = other.key;
      this.isInitial = other.isInitial;
      this.clear = other.clear;
      this.lifetime = other.lifetime;
      this.commands = other.commands;
      this.eventBindings = other.eventBindings;
   }

   @Nonnull
   public static CustomPage deserialize(@Nonnull ByteBuf buf, int offset) {
      CustomPage obj = new CustomPage();
      byte nullBits = buf.getByte(offset);
      obj.isInitial = buf.getByte(offset + 1) != 0;
      obj.clear = buf.getByte(offset + 2) != 0;
      obj.lifetime = CustomPageLifetime.fromValue(buf.getByte(offset + 3));
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 16 + buf.getIntLE(offset + 4);
         int keyLen = VarInt.peek(buf, varPos0);
         if (keyLen < 0) {
            throw ProtocolException.negativeLength("Key", keyLen);
         }

         if (keyLen > 4096000) {
            throw ProtocolException.stringTooLong("Key", keyLen, 4096000);
         }

         obj.key = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 16 + buf.getIntLE(offset + 8);
         int commandsCount = VarInt.peek(buf, varPos1);
         if (commandsCount < 0) {
            throw ProtocolException.negativeLength("Commands", commandsCount);
         }

         if (commandsCount > 4096000) {
            throw ProtocolException.arrayTooLong("Commands", commandsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos1);
         if (varPos1 + varIntLen + commandsCount * 2L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Commands", varPos1 + varIntLen + commandsCount * 2, buf.readableBytes());
         }

         obj.commands = new CustomUICommand[commandsCount];
         int elemPos = varPos1 + varIntLen;

         for (int i = 0; i < commandsCount; i++) {
            obj.commands[i] = CustomUICommand.deserialize(buf, elemPos);
            elemPos += CustomUICommand.computeBytesConsumed(buf, elemPos);
         }
      }

      if ((nullBits & 4) != 0) {
         int varPos2 = offset + 16 + buf.getIntLE(offset + 12);
         int eventBindingsCount = VarInt.peek(buf, varPos2);
         if (eventBindingsCount < 0) {
            throw ProtocolException.negativeLength("EventBindings", eventBindingsCount);
         }

         if (eventBindingsCount > 4096000) {
            throw ProtocolException.arrayTooLong("EventBindings", eventBindingsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos2);
         if (varPos2 + varIntLen + eventBindingsCount * 3L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("EventBindings", varPos2 + varIntLen + eventBindingsCount * 3, buf.readableBytes());
         }

         obj.eventBindings = new CustomUIEventBinding[eventBindingsCount];
         int elemPos = varPos2 + varIntLen;

         for (int i = 0; i < eventBindingsCount; i++) {
            obj.eventBindings[i] = CustomUIEventBinding.deserialize(buf, elemPos);
            elemPos += CustomUIEventBinding.computeBytesConsumed(buf, elemPos);
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 16;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 4);
         int pos0 = offset + 16 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 8);
         int pos1 = offset + 16 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1);

         for (int i = 0; i < arrLen; i++) {
            pos1 += CustomUICommand.computeBytesConsumed(buf, pos1);
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 12);
         int pos2 = offset + 16 + fieldOffset2;
         int arrLen = VarInt.peek(buf, pos2);
         pos2 += VarInt.length(buf, pos2);

         for (int i = 0; i < arrLen; i++) {
            pos2 += CustomUIEventBinding.computeBytesConsumed(buf, pos2);
         }

         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      return maxEnd;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.key != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.commands != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.eventBindings != null) {
         nullBits = (byte)(nullBits | 4);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.isInitial ? 1 : 0);
      buf.writeByte(this.clear ? 1 : 0);
      buf.writeByte(this.lifetime.getValue());
      int keyOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int commandsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int eventBindingsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.key != null) {
         buf.setIntLE(keyOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.key, 4096000);
      } else {
         buf.setIntLE(keyOffsetSlot, -1);
      }

      if (this.commands != null) {
         buf.setIntLE(commandsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.commands.length > 4096000) {
            throw ProtocolException.arrayTooLong("Commands", this.commands.length, 4096000);
         }

         VarInt.write(buf, this.commands.length);

         for (CustomUICommand item : this.commands) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(commandsOffsetSlot, -1);
      }

      if (this.eventBindings != null) {
         buf.setIntLE(eventBindingsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.eventBindings.length > 4096000) {
            throw ProtocolException.arrayTooLong("EventBindings", this.eventBindings.length, 4096000);
         }

         VarInt.write(buf, this.eventBindings.length);

         for (CustomUIEventBinding item : this.eventBindings) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(eventBindingsOffsetSlot, -1);
      }
   }

   @Override
   public int computeSize() {
      int size = 16;
      if (this.key != null) {
         size += PacketIO.stringSize(this.key);
      }

      if (this.commands != null) {
         int commandsSize = 0;

         for (CustomUICommand elem : this.commands) {
            commandsSize += elem.computeSize();
         }

         size += VarInt.size(this.commands.length) + commandsSize;
      }

      if (this.eventBindings != null) {
         int eventBindingsSize = 0;

         for (CustomUIEventBinding elem : this.eventBindings) {
            eventBindingsSize += elem.computeSize();
         }

         size += VarInt.size(this.eventBindings.length) + eventBindingsSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 16) {
         return ValidationResult.error("Buffer too small: expected at least 16 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int keyOffset = buffer.getIntLE(offset + 4);
            if (keyOffset < 0) {
               return ValidationResult.error("Invalid offset for Key");
            }

            int pos = offset + 16 + keyOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Key");
            }

            int keyLen = VarInt.peek(buffer, pos);
            if (keyLen < 0) {
               return ValidationResult.error("Invalid string length for Key");
            }

            if (keyLen > 4096000) {
               return ValidationResult.error("Key exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += keyLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Key");
            }
         }

         if ((nullBits & 2) != 0) {
            int commandsOffset = buffer.getIntLE(offset + 8);
            if (commandsOffset < 0) {
               return ValidationResult.error("Invalid offset for Commands");
            }

            int posx = offset + 16 + commandsOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Commands");
            }

            int commandsCount = VarInt.peek(buffer, posx);
            if (commandsCount < 0) {
               return ValidationResult.error("Invalid array count for Commands");
            }

            if (commandsCount > 4096000) {
               return ValidationResult.error("Commands exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);

            for (int i = 0; i < commandsCount; i++) {
               ValidationResult structResult = CustomUICommand.validateStructure(buffer, posx);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid CustomUICommand in Commands[" + i + "]: " + structResult.error());
               }

               posx += CustomUICommand.computeBytesConsumed(buffer, posx);
            }
         }

         if ((nullBits & 4) != 0) {
            int eventBindingsOffset = buffer.getIntLE(offset + 12);
            if (eventBindingsOffset < 0) {
               return ValidationResult.error("Invalid offset for EventBindings");
            }

            int posxx = offset + 16 + eventBindingsOffset;
            if (posxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for EventBindings");
            }

            int eventBindingsCount = VarInt.peek(buffer, posxx);
            if (eventBindingsCount < 0) {
               return ValidationResult.error("Invalid array count for EventBindings");
            }

            if (eventBindingsCount > 4096000) {
               return ValidationResult.error("EventBindings exceeds max length 4096000");
            }

            posxx += VarInt.length(buffer, posxx);

            for (int i = 0; i < eventBindingsCount; i++) {
               ValidationResult structResult = CustomUIEventBinding.validateStructure(buffer, posxx);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid CustomUIEventBinding in EventBindings[" + i + "]: " + structResult.error());
               }

               posxx += CustomUIEventBinding.computeBytesConsumed(buffer, posxx);
            }
         }

         return ValidationResult.OK;
      }
   }

   public CustomPage clone() {
      CustomPage copy = new CustomPage();
      copy.key = this.key;
      copy.isInitial = this.isInitial;
      copy.clear = this.clear;
      copy.lifetime = this.lifetime;
      copy.commands = this.commands != null ? Arrays.stream(this.commands).map(e -> e.clone()).toArray(CustomUICommand[]::new) : null;
      copy.eventBindings = this.eventBindings != null ? Arrays.stream(this.eventBindings).map(e -> e.clone()).toArray(CustomUIEventBinding[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof CustomPage other)
            ? false
            : Objects.equals(this.key, other.key)
               && this.isInitial == other.isInitial
               && this.clear == other.clear
               && Objects.equals(this.lifetime, other.lifetime)
               && Arrays.equals((Object[])this.commands, (Object[])other.commands)
               && Arrays.equals((Object[])this.eventBindings, (Object[])other.eventBindings);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.key);
      result = 31 * result + Boolean.hashCode(this.isInitial);
      result = 31 * result + Boolean.hashCode(this.clear);
      result = 31 * result + Objects.hashCode(this.lifetime);
      result = 31 * result + Arrays.hashCode((Object[])this.commands);
      return 31 * result + Arrays.hashCode((Object[])this.eventBindings);
   }
}
