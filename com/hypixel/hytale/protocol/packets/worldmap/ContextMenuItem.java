package com.hypixel.hytale.protocol.packets.worldmap;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class ContextMenuItem {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 0;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 8;
   public static final int MAX_SIZE = 32768018;
   @Nonnull
   public String name = "";
   @Nonnull
   public String command = "";

   public ContextMenuItem() {
   }

   public ContextMenuItem(@Nonnull String name, @Nonnull String command) {
      this.name = name;
      this.command = command;
   }

   public ContextMenuItem(@Nonnull ContextMenuItem other) {
      this.name = other.name;
      this.command = other.command;
   }

   @Nonnull
   public static ContextMenuItem deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 8) {
         throw ProtocolException.bufferTooSmall("ContextMenuItem", 8, buf.readableBytes() - offset);
      } else {
         ContextMenuItem obj = new ContextMenuItem();
         int varPosBase0 = buf.getIntLE(offset + 0);
         if (varPosBase0 >= 0 && varPosBase0 <= buf.writerIndex() - offset - 8) {
            int varPos0 = offset + 8 + varPosBase0;
            int nameLen = VarInt.peek(buf, varPos0);
            if (nameLen < 0) {
               throw ProtocolException.invalidVarInt("Name");
            } else {
               int nameVarIntLen = VarInt.size(nameLen);
               if (nameLen > 4096000) {
                  throw ProtocolException.stringTooLong("Name", nameLen, 4096000);
               } else if (varPos0 + nameVarIntLen + nameLen > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("Name", varPos0 + nameVarIntLen + nameLen, buf.readableBytes());
               } else {
                  obj.name = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
                  varPosBase0 = buf.getIntLE(offset + 4);
                  if (varPosBase0 >= 0 && varPosBase0 <= buf.writerIndex() - offset - 8) {
                     varPos0 = offset + 8 + varPosBase0;
                     nameLen = VarInt.peek(buf, varPos0);
                     if (nameLen < 0) {
                        throw ProtocolException.invalidVarInt("Command");
                     } else {
                        nameVarIntLen = VarInt.size(nameLen);
                        if (nameLen > 4096000) {
                           throw ProtocolException.stringTooLong("Command", nameLen, 4096000);
                        } else if (varPos0 + nameVarIntLen + nameLen > buf.readableBytes()) {
                           throw ProtocolException.bufferTooSmall("Command", varPos0 + nameVarIntLen + nameLen, buf.readableBytes());
                        } else {
                           obj.command = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
                           return obj;
                        }
                     }
                  } else {
                     throw ProtocolException.invalidOffset("Command", varPosBase0, buf.readableBytes());
                  }
               }
            }
         } else {
            throw ProtocolException.invalidOffset("Name", varPosBase0, buf.readableBytes());
         }
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      int maxEnd = 8;
      int fieldOffset0 = buf.getIntLE(offset + 0);
      if (fieldOffset0 >= 0 && fieldOffset0 <= buf.writerIndex() - offset - 8) {
         int pos0 = offset + 8 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }

         fieldOffset0 = buf.getIntLE(offset + 4);
         if (fieldOffset0 >= 0 && fieldOffset0 <= buf.writerIndex() - offset - 8) {
            pos0 = offset + 8 + fieldOffset0;
            sl = VarInt.peek(buf, pos0);
            pos0 += VarInt.size(sl) + sl;
            if (pos0 - offset > maxEnd) {
               maxEnd = pos0 - offset;
            }

            return maxEnd;
         } else {
            throw ProtocolException.invalidOffset("Command", fieldOffset0, maxEnd);
         }
      } else {
         throw ProtocolException.invalidOffset("Name", fieldOffset0, maxEnd);
      }
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 8L;
   }

   public static String getName(MemorySegment mem) {
      return getName(mem, 0);
   }

   public static String getName(MemorySegment mem, int offset) {
      return PacketIO.readVarString("Name", mem, offset + getValidatedOffset(mem, offset, 0, 8, "Name"), 4096000, PacketIO.UTF8);
   }

   public static String getCommand(MemorySegment mem) {
      return getCommand(mem, 0);
   }

   public static String getCommand(MemorySegment mem, int offset) {
      return PacketIO.readVarString("Command", mem, offset + getValidatedOffset(mem, offset, 4, 8, "Command"), 4096000, PacketIO.UTF8);
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static ContextMenuItem toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ContextMenuItem toObject(MemorySegment mem, int offset) {
      if (offset + 8 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ContextMenuItem", offset + 8, (int)mem.byteSize());
      } else {
         return new ContextMenuItem(
            PacketIO.readVarString("Name", mem, offset + getValidatedOffset(mem, offset, 0, 8, "Name"), 4096000, PacketIO.UTF8),
            PacketIO.readVarString("Command", mem, offset + getValidatedOffset(mem, offset, 4, 8, "Command"), 4096000, PacketIO.UTF8)
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      int nameOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int commandOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      buf.setIntLE(nameOffsetSlot, buf.writerIndex() - varBlockStart);
      PacketIO.writeVarString(buf, this.name, 4096000);
      buf.setIntLE(commandOffsetSlot, buf.writerIndex() - varBlockStart);
      PacketIO.writeVarString(buf, this.command, 4096000);
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      int varOffset = offset + 8;
      mem.set(PacketIO.PROTO_INT, (long)(offset + 0), varOffset - offset - 8);
      varOffset += PacketIO.writeVarString(mem, varOffset, this.name, 4096000);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 4), varOffset - offset - 8);
      varOffset += PacketIO.writeVarString(mem, varOffset, this.command, 4096000);
      return varOffset - offset;
   }

   public int computeSize() {
      int size = 8;
      size += PacketIO.stringSize(this.name);
      return size + PacketIO.stringSize(this.command);
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 8) {
         return ValidationResult.error("Buffer too small: expected at least 8 bytes");
      } else {
         int nameOffset = buffer.getIntLE(offset + 0);
         if (nameOffset >= 0 && nameOffset <= buffer.writerIndex() - offset - 8) {
            int pos = offset + 8 + nameOffset;
            int nameLen = VarInt.peek(buffer, pos);
            if (nameLen < 0) {
               return ValidationResult.error("Invalid string length for Name");
            } else if (nameLen > 4096000) {
               return ValidationResult.error("Name exceeds max length 4096000");
            } else {
               pos += VarInt.size(nameLen);
               pos += nameLen;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading Name");
               } else {
                  nameOffset = buffer.getIntLE(offset + 4);
                  if (nameOffset >= 0 && nameOffset <= buffer.writerIndex() - offset - 8) {
                     pos = offset + 8 + nameOffset;
                     nameLen = VarInt.peek(buffer, pos);
                     if (nameLen < 0) {
                        return ValidationResult.error("Invalid string length for Command");
                     } else if (nameLen > 4096000) {
                        return ValidationResult.error("Command exceeds max length 4096000");
                     } else {
                        pos += VarInt.size(nameLen);
                        pos += nameLen;
                        return pos > buffer.writerIndex() ? ValidationResult.error("Buffer overflow reading Command") : ValidationResult.OK;
                     }
                  } else {
                     return ValidationResult.error("Invalid offset for Command");
                  }
               }
            }
         } else {
            return ValidationResult.error("Invalid offset for Name");
         }
      }
   }

   public ContextMenuItem clone() {
      ContextMenuItem copy = new ContextMenuItem();
      copy.name = this.name;
      copy.command = this.command;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ContextMenuItem other) ? false : Objects.equals(this.name, other.name) && Objects.equals(this.command, other.command);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.name, this.command);
   }
}
