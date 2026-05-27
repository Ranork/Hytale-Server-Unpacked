package com.hypixel.hytale.protocol.packets.buildertools;

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

public class BuilderToolArgUpdate implements Packet, ToServerPacket {
   public static final int PACKET_ID = 400;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 13;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 21;
   public static final int MAX_SIZE = 32768031;
   public int token;
   public int section;
   public int slot;
   @Nullable
   public String id;
   @Nullable
   public String value;

   @Override
   public int getId() {
      return 400;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public BuilderToolArgUpdate() {
   }

   public BuilderToolArgUpdate(int token, int section, int slot, @Nullable String id, @Nullable String value) {
      this.token = token;
      this.section = section;
      this.slot = slot;
      this.id = id;
      this.value = value;
   }

   public BuilderToolArgUpdate(@Nonnull BuilderToolArgUpdate other) {
      this.token = other.token;
      this.section = other.section;
      this.slot = other.slot;
      this.id = other.id;
      this.value = other.value;
   }

   @Nonnull
   public static BuilderToolArgUpdate deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 21) {
         throw ProtocolException.bufferTooSmall("BuilderToolArgUpdate", 21, buf.readableBytes() - offset);
      } else {
         BuilderToolArgUpdate obj = new BuilderToolArgUpdate();
         byte nullBits = buf.getByte(offset);
         obj.token = buf.getIntLE(offset + 1);
         obj.section = buf.getIntLE(offset + 5);
         obj.slot = buf.getIntLE(offset + 9);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 13);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 21) {
               throw ProtocolException.invalidOffset("Id", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 21 + varPosBase0;
            int idLen = VarInt.peek(buf, varPos0);
            if (idLen < 0) {
               throw ProtocolException.invalidVarInt("Id");
            }

            int idVarIntLen = VarInt.size(idLen);
            if (idLen > 4096000) {
               throw ProtocolException.stringTooLong("Id", idLen, 4096000);
            }

            if (varPos0 + idVarIntLen + idLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Id", varPos0 + idVarIntLen + idLen, buf.readableBytes());
            }

            obj.id = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 17);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 21) {
               throw ProtocolException.invalidOffset("Value", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 21 + varPosBase1;
            int valueLen = VarInt.peek(buf, varPos1);
            if (valueLen < 0) {
               throw ProtocolException.invalidVarInt("Value");
            }

            int valueVarIntLen = VarInt.size(valueLen);
            if (valueLen > 4096000) {
               throw ProtocolException.stringTooLong("Value", valueLen, 4096000);
            }

            if (varPos1 + valueVarIntLen + valueLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Value", varPos1 + valueVarIntLen + valueLen, buf.readableBytes());
            }

            obj.value = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 21;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 13);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 21) {
            throw ProtocolException.invalidOffset("Id", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 21 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 17);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 21) {
            throw ProtocolException.invalidOffset("Value", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 21 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(sl) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 21L;
   }

   public static int getToken(MemorySegment mem) {
      return getToken(mem, 0);
   }

   public static int getToken(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   public static int getSection(MemorySegment mem) {
      return getSection(mem, 0);
   }

   public static int getSection(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 5));
   }

   public static int getSlot(MemorySegment mem) {
      return getSlot(mem, 0);
   }

   public static int getSlot(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 9));
   }

   @Nullable
   public static String getId(MemorySegment mem) {
      return getId(mem, 0);
   }

   @Nullable
   public static String getId(MemorySegment mem, int offset) {
      return hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 13, 21, "Id"), 4096000, PacketIO.UTF8) : null;
   }

   @Nullable
   public static String getValue(MemorySegment mem) {
      return getValue(mem, 0);
   }

   @Nullable
   public static String getValue(MemorySegment mem, int offset) {
      return hasValue(mem, offset)
         ? PacketIO.readVarString("Value", mem, offset + getValidatedOffset(mem, offset, 17, 21, "Value"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static boolean hasId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasValue(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static BuilderToolArgUpdate toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static BuilderToolArgUpdate toObject(MemorySegment mem, int offset) {
      if (offset + 21 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("BuilderToolArgUpdate", offset + 21, (int)mem.byteSize());
      } else {
         return new BuilderToolArgUpdate(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 5)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 9)),
            hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 13, 21, "Id"), 4096000, PacketIO.UTF8) : null,
            hasValue(mem, offset)
               ? PacketIO.readVarString("Value", mem, offset + getValidatedOffset(mem, offset, 17, 21, "Value"), 4096000, PacketIO.UTF8)
               : null
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.id != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.value != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.token);
      buf.writeIntLE(this.section);
      buf.writeIntLE(this.slot);
      int idOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int valueOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.id != null) {
         buf.setIntLE(idOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.id, 4096000);
      } else {
         buf.setIntLE(idOffsetSlot, -1);
      }

      if (this.value != null) {
         buf.setIntLE(valueOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.value, 4096000);
      } else {
         buf.setIntLE(valueOffsetSlot, -1);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.id != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.value != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.token);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 5), this.section);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 9), this.slot);
      int varOffset = offset + 21;
      if (this.id != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 13), varOffset - offset - 21);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.id, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 13), -1);
      }

      if (this.value != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 17), varOffset - offset - 21);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.value, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 17), -1);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 21;
      if (this.id != null) {
         size += PacketIO.stringSize(this.id);
      }

      if (this.value != null) {
         size += PacketIO.stringSize(this.value);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 21) {
         return ValidationResult.error("Buffer too small: expected at least 21 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int idOffset = buffer.getIntLE(offset + 13);
            if (idOffset < 0 || idOffset > buffer.writerIndex() - offset - 21) {
               return ValidationResult.error("Invalid offset for Id");
            }

            int pos = offset + 21 + idOffset;
            int idLen = VarInt.peek(buffer, pos);
            if (idLen < 0) {
               return ValidationResult.error("Invalid string length for Id");
            }

            if (idLen > 4096000) {
               return ValidationResult.error("Id exceeds max length 4096000");
            }

            pos += VarInt.size(idLen);
            pos += idLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Id");
            }
         }

         if ((nullBits & 2) != 0) {
            int valueOffset = buffer.getIntLE(offset + 17);
            if (valueOffset < 0 || valueOffset > buffer.writerIndex() - offset - 21) {
               return ValidationResult.error("Invalid offset for Value");
            }

            int posx = offset + 21 + valueOffset;
            int valueLen = VarInt.peek(buffer, posx);
            if (valueLen < 0) {
               return ValidationResult.error("Invalid string length for Value");
            }

            if (valueLen > 4096000) {
               return ValidationResult.error("Value exceeds max length 4096000");
            }

            posx += VarInt.size(valueLen);
            posx += valueLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Value");
            }
         }

         return ValidationResult.OK;
      }
   }

   public BuilderToolArgUpdate clone() {
      BuilderToolArgUpdate copy = new BuilderToolArgUpdate();
      copy.token = this.token;
      copy.section = this.section;
      copy.slot = this.slot;
      copy.id = this.id;
      copy.value = this.value;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BuilderToolArgUpdate other)
            ? false
            : this.token == other.token
               && this.section == other.section
               && this.slot == other.slot
               && Objects.equals(this.id, other.id)
               && Objects.equals(this.value, other.value);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.token, this.section, this.slot, this.id, this.value);
   }
}
