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
      if (buf.readableBytes() - offset < 16) {
         throw ProtocolException.bufferTooSmall("CustomPage", 16, buf.readableBytes() - offset);
      } else {
         CustomPage obj = new CustomPage();
         byte nullBits = buf.getByte(offset);
         obj.isInitial = buf.getByte(offset + 1) != 0;
         obj.clear = buf.getByte(offset + 2) != 0;
         obj.lifetime = CustomPageLifetime.fromValue(buf.getByte(offset + 3));
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 4);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 16) {
               throw ProtocolException.invalidOffset("Key", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 16 + varPosBase0;
            int keyLen = VarInt.peek(buf, varPos0);
            if (keyLen < 0) {
               throw ProtocolException.invalidVarInt("Key");
            }

            int keyVarIntLen = VarInt.size(keyLen);
            if (keyLen > 4096000) {
               throw ProtocolException.stringTooLong("Key", keyLen, 4096000);
            }

            if (varPos0 + keyVarIntLen + keyLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Key", varPos0 + keyVarIntLen + keyLen, buf.readableBytes());
            }

            obj.key = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 8);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 16) {
               throw ProtocolException.invalidOffset("Commands", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 16 + varPosBase1;
            int commandsCount = VarInt.peek(buf, varPos1);
            if (commandsCount < 0) {
               throw ProtocolException.invalidVarInt("Commands");
            }

            int varIntLen = VarInt.size(commandsCount);
            if (commandsCount > 4096000) {
               throw ProtocolException.arrayTooLong("Commands", commandsCount, 4096000);
            }

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
            int varPosBase2 = buf.getIntLE(offset + 12);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 16) {
               throw ProtocolException.invalidOffset("EventBindings", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 16 + varPosBase2;
            int eventBindingsCount = VarInt.peek(buf, varPos2);
            if (eventBindingsCount < 0) {
               throw ProtocolException.invalidVarInt("EventBindings");
            }

            int varIntLenx = VarInt.size(eventBindingsCount);
            if (eventBindingsCount > 4096000) {
               throw ProtocolException.arrayTooLong("EventBindings", eventBindingsCount, 4096000);
            }

            if (varPos2 + varIntLenx + eventBindingsCount * 3L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("EventBindings", varPos2 + varIntLenx + eventBindingsCount * 3, buf.readableBytes());
            }

            obj.eventBindings = new CustomUIEventBinding[eventBindingsCount];
            int elemPos = varPos2 + varIntLenx;

            for (int i = 0; i < eventBindingsCount; i++) {
               obj.eventBindings[i] = CustomUIEventBinding.deserialize(buf, elemPos);
               elemPos += CustomUIEventBinding.computeBytesConsumed(buf, elemPos);
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 16;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 4);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 16) {
            throw ProtocolException.invalidOffset("Key", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 16 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 8);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 16) {
            throw ProtocolException.invalidOffset("Commands", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 16 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos1 += CustomUICommand.computeBytesConsumed(buf, pos1);
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 12);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 16) {
            throw ProtocolException.invalidOffset("EventBindings", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 16 + fieldOffset2;
         int arrLen = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos2 += CustomUIEventBinding.computeBytesConsumed(buf, pos2);
         }

         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 16L;
   }

   @Nullable
   public static String getKey(MemorySegment mem) {
      return getKey(mem, 0);
   }

   @Nullable
   public static String getKey(MemorySegment mem, int offset) {
      return hasKey(mem, offset) ? PacketIO.readVarString("Key", mem, offset + getValidatedOffset(mem, offset, 4, 16, "Key"), 4096000, PacketIO.UTF8) : null;
   }

   public static boolean getIsInitial(MemorySegment mem) {
      return getIsInitial(mem, 0);
   }

   public static boolean getIsInitial(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1));
   }

   public static boolean getClear(MemorySegment mem) {
      return getClear(mem, 0);
   }

   public static boolean getClear(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 2));
   }

   public static CustomPageLifetime getLifetime(MemorySegment mem) {
      return getLifetime(mem, 0);
   }

   public static CustomPageLifetime getLifetime(MemorySegment mem, int offset) {
      return CustomPageLifetime.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 3)));
   }

   @Nullable
   public static CustomUICommand[] getCommands(MemorySegment mem) {
      return getCommands(mem, 0);
   }

   @Nullable
   public static CustomUICommand[] getCommands(MemorySegment mem, int offset) {
      if (!hasCommands(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 8, 16, "Commands");
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
               CustomUICommand[] data = new CustomUICommand[len];

               for (int i = 0; i < len; i++) {
                  data[i] = CustomUICommand.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   @Nullable
   public static CustomUIEventBinding[] getEventBindings(MemorySegment mem) {
      return getEventBindings(mem, 0);
   }

   @Nullable
   public static CustomUIEventBinding[] getEventBindings(MemorySegment mem, int offset) {
      if (!hasEventBindings(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 12, 16, "EventBindings");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("EventBindings", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("EventBindings", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("EventBindings", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               CustomUIEventBinding[] data = new CustomUIEventBinding[len];

               for (int i = 0; i < len; i++) {
                  data[i] = CustomUIEventBinding.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   public static boolean hasKey(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasCommands(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasEventBindings(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static CustomPage toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static CustomPage toObject(MemorySegment mem, int offset) {
      if (offset + 16 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("CustomPage", offset + 16, (int)mem.byteSize());
      } else {
         CustomUICommand[] commands = null;
         if (hasCommands(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 8, 16, "Commands");
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
            commands = new CustomUICommand[len];

            for (int i = 0; i < len; i++) {
               commands[i] = CustomUICommand.toObject(mem, off);
               off += commands[i].computeSize();
            }
         }

         CustomUIEventBinding[] eventBindings = null;
         if (hasEventBindings(mem, offset)) {
            int offx = offset + getValidatedOffset(mem, offset, 12, 16, "EventBindings");
            long packedx = VarInt.getWithLength(mem, offx);
            int lenx = (int)packedx;
            if (lenx < 0) {
               throw ProtocolException.negativeLength("EventBindings", lenx);
            }

            if (lenx > 4096000) {
               throw ProtocolException.arrayTooLong("EventBindings", lenx, 4096000);
            }

            int lenOffset = (int)(packedx >>> 32);
            if (offx + lenOffset + lenx > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("EventBindings", offx + lenOffset + lenx, (int)mem.byteSize());
            }

            offx += lenOffset;
            eventBindings = new CustomUIEventBinding[lenx];

            for (int i = 0; i < lenx; i++) {
               eventBindings[i] = CustomUIEventBinding.toObject(mem, offx);
               offx += eventBindings[i].computeSize();
            }
         }

         return new CustomPage(
            hasKey(mem, offset) ? PacketIO.readVarString("Key", mem, offset + getValidatedOffset(mem, offset, 4, 16, "Key"), 4096000, PacketIO.UTF8) : null,
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 2)),
            CustomPageLifetime.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 3))),
            commands,
            eventBindings
         );
      }
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
   public int serialize(@Nonnull MemorySegment mem, int offset) {
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

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BOOL, offset + 1, this.isInitial);
      mem.set(PacketIO.PROTO_BOOL, offset + 2, this.clear);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 3), (byte)this.lifetime.getValue());
      int varOffset = offset + 16;
      if (this.key != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 4), varOffset - offset - 16);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.key, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 4), -1);
      }

      if (this.commands != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 8), varOffset - offset - 16);
         if (this.commands.length > 4096000) {
            throw ProtocolException.arrayTooLong("Commands", this.commands.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.commands.length);
         int commandsValueOffset = 0;

         for (int i = 0; i < this.commands.length; i++) {
            commandsValueOffset += this.commands[i].serialize(mem, varOffset + commandsValueOffset);
         }

         varOffset += commandsValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 8), -1);
      }

      if (this.eventBindings != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 12), varOffset - offset - 16);
         if (this.eventBindings.length > 4096000) {
            throw ProtocolException.arrayTooLong("EventBindings", this.eventBindings.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.eventBindings.length);
         int eventBindingsValueOffset = 0;

         for (int i = 0; i < this.eventBindings.length; i++) {
            eventBindingsValueOffset += this.eventBindings[i].serialize(mem, varOffset + eventBindingsValueOffset);
         }

         varOffset += eventBindingsValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 12), -1);
      }

      return varOffset - offset;
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
         int v = buffer.getByte(offset + 3) & 255;
         if (v >= 3) {
            return ValidationResult.error("Invalid CustomPageLifetime value for Lifetime");
         } else {
            if ((nullBits & 1) != 0) {
               v = buffer.getIntLE(offset + 4);
               if (v < 0 || v > buffer.writerIndex() - offset - 16) {
                  return ValidationResult.error("Invalid offset for Key");
               }

               int pos = offset + 16 + v;
               int keyLen = VarInt.peek(buffer, pos);
               if (keyLen < 0) {
                  return ValidationResult.error("Invalid string length for Key");
               }

               if (keyLen > 4096000) {
                  return ValidationResult.error("Key exceeds max length 4096000");
               }

               pos += VarInt.size(keyLen);
               pos += keyLen;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading Key");
               }
            }

            if ((nullBits & 2) != 0) {
               v = buffer.getIntLE(offset + 8);
               if (v < 0 || v > buffer.writerIndex() - offset - 16) {
                  return ValidationResult.error("Invalid offset for Commands");
               }

               int posx = offset + 16 + v;
               int commandsCount = VarInt.peek(buffer, posx);
               if (commandsCount < 0) {
                  return ValidationResult.error("Invalid array count for Commands");
               }

               if (commandsCount > 4096000) {
                  return ValidationResult.error("Commands exceeds max length 4096000");
               }

               posx += VarInt.size(commandsCount);

               for (int i = 0; i < commandsCount; i++) {
                  ValidationResult structResult = CustomUICommand.validateStructure(buffer, posx);
                  if (!structResult.isValid()) {
                     return ValidationResult.error("Invalid CustomUICommand in Commands[" + i + "]: " + structResult.error());
                  }

                  posx += CustomUICommand.computeBytesConsumed(buffer, posx);
               }
            }

            if ((nullBits & 4) != 0) {
               v = buffer.getIntLE(offset + 12);
               if (v < 0 || v > buffer.writerIndex() - offset - 16) {
                  return ValidationResult.error("Invalid offset for EventBindings");
               }

               int posxx = offset + 16 + v;
               int eventBindingsCount = VarInt.peek(buffer, posxx);
               if (eventBindingsCount < 0) {
                  return ValidationResult.error("Invalid array count for EventBindings");
               }

               if (eventBindingsCount > 4096000) {
                  return ValidationResult.error("EventBindings exceeds max length 4096000");
               }

               posxx += VarInt.size(eventBindingsCount);

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
