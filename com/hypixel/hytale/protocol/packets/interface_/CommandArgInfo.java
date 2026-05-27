package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CommandArgInfo {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 4;
   public static final int VARIABLE_BLOCK_START = 21;
   public static final int MAX_SIZE = 65536041;
   @Nullable
   public String name;
   @Nullable
   public String argTypeId;
   @Nullable
   public String argTypeName;
   public int valueCount;
   @Nullable
   public String description;

   public CommandArgInfo() {
   }

   public CommandArgInfo(@Nullable String name, @Nullable String argTypeId, @Nullable String argTypeName, int valueCount, @Nullable String description) {
      this.name = name;
      this.argTypeId = argTypeId;
      this.argTypeName = argTypeName;
      this.valueCount = valueCount;
      this.description = description;
   }

   public CommandArgInfo(@Nonnull CommandArgInfo other) {
      this.name = other.name;
      this.argTypeId = other.argTypeId;
      this.argTypeName = other.argTypeName;
      this.valueCount = other.valueCount;
      this.description = other.description;
   }

   @Nonnull
   public static CommandArgInfo deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 21) {
         throw ProtocolException.bufferTooSmall("CommandArgInfo", 21, buf.readableBytes() - offset);
      } else {
         CommandArgInfo obj = new CommandArgInfo();
         byte nullBits = buf.getByte(offset);
         obj.valueCount = buf.getIntLE(offset + 1);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 5);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 21) {
               throw ProtocolException.invalidOffset("Name", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 21 + varPosBase0;
            int nameLen = VarInt.peek(buf, varPos0);
            if (nameLen < 0) {
               throw ProtocolException.invalidVarInt("Name");
            }

            int nameVarIntLen = VarInt.size(nameLen);
            if (nameLen > 4096000) {
               throw ProtocolException.stringTooLong("Name", nameLen, 4096000);
            }

            if (varPos0 + nameVarIntLen + nameLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Name", varPos0 + nameVarIntLen + nameLen, buf.readableBytes());
            }

            obj.name = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 9);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 21) {
               throw ProtocolException.invalidOffset("ArgTypeId", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 21 + varPosBase1;
            int argTypeIdLen = VarInt.peek(buf, varPos1);
            if (argTypeIdLen < 0) {
               throw ProtocolException.invalidVarInt("ArgTypeId");
            }

            int argTypeIdVarIntLen = VarInt.size(argTypeIdLen);
            if (argTypeIdLen > 4096000) {
               throw ProtocolException.stringTooLong("ArgTypeId", argTypeIdLen, 4096000);
            }

            if (varPos1 + argTypeIdVarIntLen + argTypeIdLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("ArgTypeId", varPos1 + argTypeIdVarIntLen + argTypeIdLen, buf.readableBytes());
            }

            obj.argTypeId = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
         }

         if ((nullBits & 4) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 13);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 21) {
               throw ProtocolException.invalidOffset("ArgTypeName", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 21 + varPosBase2;
            int argTypeNameLen = VarInt.peek(buf, varPos2);
            if (argTypeNameLen < 0) {
               throw ProtocolException.invalidVarInt("ArgTypeName");
            }

            int argTypeNameVarIntLen = VarInt.size(argTypeNameLen);
            if (argTypeNameLen > 4096000) {
               throw ProtocolException.stringTooLong("ArgTypeName", argTypeNameLen, 4096000);
            }

            if (varPos2 + argTypeNameVarIntLen + argTypeNameLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("ArgTypeName", varPos2 + argTypeNameVarIntLen + argTypeNameLen, buf.readableBytes());
            }

            obj.argTypeName = PacketIO.readVarString(buf, varPos2, PacketIO.UTF8);
         }

         if ((nullBits & 8) != 0) {
            int varPosBase3 = buf.getIntLE(offset + 17);
            if (varPosBase3 < 0 || varPosBase3 > buf.writerIndex() - offset - 21) {
               throw ProtocolException.invalidOffset("Description", varPosBase3, buf.readableBytes());
            }

            int varPos3 = offset + 21 + varPosBase3;
            int descriptionLen = VarInt.peek(buf, varPos3);
            if (descriptionLen < 0) {
               throw ProtocolException.invalidVarInt("Description");
            }

            int descriptionVarIntLen = VarInt.size(descriptionLen);
            if (descriptionLen > 4096000) {
               throw ProtocolException.stringTooLong("Description", descriptionLen, 4096000);
            }

            if (varPos3 + descriptionVarIntLen + descriptionLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Description", varPos3 + descriptionVarIntLen + descriptionLen, buf.readableBytes());
            }

            obj.description = PacketIO.readVarString(buf, varPos3, PacketIO.UTF8);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 21;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 5);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 21) {
            throw ProtocolException.invalidOffset("Name", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 21 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 9);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 21) {
            throw ProtocolException.invalidOffset("ArgTypeId", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 21 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(sl) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 13);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 21) {
            throw ProtocolException.invalidOffset("ArgTypeName", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 21 + fieldOffset2;
         int sl = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(sl) + sl;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 17);
         if (fieldOffset3 < 0 || fieldOffset3 > buf.writerIndex() - offset - 21) {
            throw ProtocolException.invalidOffset("Description", fieldOffset3, maxEnd);
         }

         int pos3 = offset + 21 + fieldOffset3;
         int sl = VarInt.peek(buf, pos3);
         pos3 += VarInt.size(sl) + sl;
         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 21L;
   }

   @Nullable
   public static String getName(MemorySegment mem) {
      return getName(mem, 0);
   }

   @Nullable
   public static String getName(MemorySegment mem, int offset) {
      return hasName(mem, offset) ? PacketIO.readVarString("Name", mem, offset + getValidatedOffset(mem, offset, 5, 21, "Name"), 4096000, PacketIO.UTF8) : null;
   }

   @Nullable
   public static String getArgTypeId(MemorySegment mem) {
      return getArgTypeId(mem, 0);
   }

   @Nullable
   public static String getArgTypeId(MemorySegment mem, int offset) {
      return hasArgTypeId(mem, offset)
         ? PacketIO.readVarString("ArgTypeId", mem, offset + getValidatedOffset(mem, offset, 9, 21, "ArgTypeId"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static String getArgTypeName(MemorySegment mem) {
      return getArgTypeName(mem, 0);
   }

   @Nullable
   public static String getArgTypeName(MemorySegment mem, int offset) {
      return hasArgTypeName(mem, offset)
         ? PacketIO.readVarString("ArgTypeName", mem, offset + getValidatedOffset(mem, offset, 13, 21, "ArgTypeName"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static int getValueCount(MemorySegment mem) {
      return getValueCount(mem, 0);
   }

   public static int getValueCount(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   @Nullable
   public static String getDescription(MemorySegment mem) {
      return getDescription(mem, 0);
   }

   @Nullable
   public static String getDescription(MemorySegment mem, int offset) {
      return hasDescription(mem, offset)
         ? PacketIO.readVarString("Description", mem, offset + getValidatedOffset(mem, offset, 17, 21, "Description"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static boolean hasName(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasArgTypeId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasArgTypeName(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasDescription(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static CommandArgInfo toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static CommandArgInfo toObject(MemorySegment mem, int offset) {
      if (offset + 21 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("CommandArgInfo", offset + 21, (int)mem.byteSize());
      } else {
         return new CommandArgInfo(
            hasName(mem, offset) ? PacketIO.readVarString("Name", mem, offset + getValidatedOffset(mem, offset, 5, 21, "Name"), 4096000, PacketIO.UTF8) : null,
            hasArgTypeId(mem, offset)
               ? PacketIO.readVarString("ArgTypeId", mem, offset + getValidatedOffset(mem, offset, 9, 21, "ArgTypeId"), 4096000, PacketIO.UTF8)
               : null,
            hasArgTypeName(mem, offset)
               ? PacketIO.readVarString("ArgTypeName", mem, offset + getValidatedOffset(mem, offset, 13, 21, "ArgTypeName"), 4096000, PacketIO.UTF8)
               : null,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            hasDescription(mem, offset)
               ? PacketIO.readVarString("Description", mem, offset + getValidatedOffset(mem, offset, 17, 21, "Description"), 4096000, PacketIO.UTF8)
               : null
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.name != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.argTypeId != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.argTypeName != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.description != null) {
         nullBits = (byte)(nullBits | 8);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.valueCount);
      int nameOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int argTypeIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int argTypeNameOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int descriptionOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.name != null) {
         buf.setIntLE(nameOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.name, 4096000);
      } else {
         buf.setIntLE(nameOffsetSlot, -1);
      }

      if (this.argTypeId != null) {
         buf.setIntLE(argTypeIdOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.argTypeId, 4096000);
      } else {
         buf.setIntLE(argTypeIdOffsetSlot, -1);
      }

      if (this.argTypeName != null) {
         buf.setIntLE(argTypeNameOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.argTypeName, 4096000);
      } else {
         buf.setIntLE(argTypeNameOffsetSlot, -1);
      }

      if (this.description != null) {
         buf.setIntLE(descriptionOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.description, 4096000);
      } else {
         buf.setIntLE(descriptionOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.name != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.argTypeId != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.argTypeName != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.description != null) {
         nullBits = (byte)(nullBits | 8);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.valueCount);
      int varOffset = offset + 21;
      if (this.name != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), varOffset - offset - 21);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.name, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), -1);
      }

      if (this.argTypeId != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), varOffset - offset - 21);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.argTypeId, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), -1);
      }

      if (this.argTypeName != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 13), varOffset - offset - 21);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.argTypeName, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 13), -1);
      }

      if (this.description != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 17), varOffset - offset - 21);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.description, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 17), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 21;
      if (this.name != null) {
         size += PacketIO.stringSize(this.name);
      }

      if (this.argTypeId != null) {
         size += PacketIO.stringSize(this.argTypeId);
      }

      if (this.argTypeName != null) {
         size += PacketIO.stringSize(this.argTypeName);
      }

      if (this.description != null) {
         size += PacketIO.stringSize(this.description);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 21) {
         return ValidationResult.error("Buffer too small: expected at least 21 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int nameOffset = buffer.getIntLE(offset + 5);
            if (nameOffset < 0 || nameOffset > buffer.writerIndex() - offset - 21) {
               return ValidationResult.error("Invalid offset for Name");
            }

            int pos = offset + 21 + nameOffset;
            int nameLen = VarInt.peek(buffer, pos);
            if (nameLen < 0) {
               return ValidationResult.error("Invalid string length for Name");
            }

            if (nameLen > 4096000) {
               return ValidationResult.error("Name exceeds max length 4096000");
            }

            pos += VarInt.size(nameLen);
            pos += nameLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Name");
            }
         }

         if ((nullBits & 2) != 0) {
            int argTypeIdOffset = buffer.getIntLE(offset + 9);
            if (argTypeIdOffset < 0 || argTypeIdOffset > buffer.writerIndex() - offset - 21) {
               return ValidationResult.error("Invalid offset for ArgTypeId");
            }

            int posx = offset + 21 + argTypeIdOffset;
            int argTypeIdLen = VarInt.peek(buffer, posx);
            if (argTypeIdLen < 0) {
               return ValidationResult.error("Invalid string length for ArgTypeId");
            }

            if (argTypeIdLen > 4096000) {
               return ValidationResult.error("ArgTypeId exceeds max length 4096000");
            }

            posx += VarInt.size(argTypeIdLen);
            posx += argTypeIdLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading ArgTypeId");
            }
         }

         if ((nullBits & 4) != 0) {
            int argTypeNameOffset = buffer.getIntLE(offset + 13);
            if (argTypeNameOffset < 0 || argTypeNameOffset > buffer.writerIndex() - offset - 21) {
               return ValidationResult.error("Invalid offset for ArgTypeName");
            }

            int posxx = offset + 21 + argTypeNameOffset;
            int argTypeNameLen = VarInt.peek(buffer, posxx);
            if (argTypeNameLen < 0) {
               return ValidationResult.error("Invalid string length for ArgTypeName");
            }

            if (argTypeNameLen > 4096000) {
               return ValidationResult.error("ArgTypeName exceeds max length 4096000");
            }

            posxx += VarInt.size(argTypeNameLen);
            posxx += argTypeNameLen;
            if (posxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading ArgTypeName");
            }
         }

         if ((nullBits & 8) != 0) {
            int descriptionOffset = buffer.getIntLE(offset + 17);
            if (descriptionOffset < 0 || descriptionOffset > buffer.writerIndex() - offset - 21) {
               return ValidationResult.error("Invalid offset for Description");
            }

            int posxxx = offset + 21 + descriptionOffset;
            int descriptionLen = VarInt.peek(buffer, posxxx);
            if (descriptionLen < 0) {
               return ValidationResult.error("Invalid string length for Description");
            }

            if (descriptionLen > 4096000) {
               return ValidationResult.error("Description exceeds max length 4096000");
            }

            posxxx += VarInt.size(descriptionLen);
            posxxx += descriptionLen;
            if (posxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Description");
            }
         }

         return ValidationResult.OK;
      }
   }

   public CommandArgInfo clone() {
      CommandArgInfo copy = new CommandArgInfo();
      copy.name = this.name;
      copy.argTypeId = this.argTypeId;
      copy.argTypeName = this.argTypeName;
      copy.valueCount = this.valueCount;
      copy.description = this.description;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof CommandArgInfo other)
            ? false
            : Objects.equals(this.name, other.name)
               && Objects.equals(this.argTypeId, other.argTypeId)
               && Objects.equals(this.argTypeName, other.argTypeName)
               && this.valueCount == other.valueCount
               && Objects.equals(this.description, other.description);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.name, this.argTypeId, this.argTypeName, this.valueCount, this.description);
   }
}
