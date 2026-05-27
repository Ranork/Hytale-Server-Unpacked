package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockIdMatcher {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 13;
   public static final int MAX_SIZE = 32768023;
   @Nullable
   public String id;
   @Nullable
   public String state;
   public int tagIndex;

   public BlockIdMatcher() {
   }

   public BlockIdMatcher(@Nullable String id, @Nullable String state, int tagIndex) {
      this.id = id;
      this.state = state;
      this.tagIndex = tagIndex;
   }

   public BlockIdMatcher(@Nonnull BlockIdMatcher other) {
      this.id = other.id;
      this.state = other.state;
      this.tagIndex = other.tagIndex;
   }

   @Nonnull
   public static BlockIdMatcher deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 13) {
         throw ProtocolException.bufferTooSmall("BlockIdMatcher", 13, buf.readableBytes() - offset);
      } else {
         BlockIdMatcher obj = new BlockIdMatcher();
         byte nullBits = buf.getByte(offset);
         obj.tagIndex = buf.getIntLE(offset + 1);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 5);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 13) {
               throw ProtocolException.invalidOffset("Id", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 13 + varPosBase0;
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
            int varPosBase1 = buf.getIntLE(offset + 9);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 13) {
               throw ProtocolException.invalidOffset("State", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 13 + varPosBase1;
            int stateLen = VarInt.peek(buf, varPos1);
            if (stateLen < 0) {
               throw ProtocolException.invalidVarInt("State");
            }

            int stateVarIntLen = VarInt.size(stateLen);
            if (stateLen > 4096000) {
               throw ProtocolException.stringTooLong("State", stateLen, 4096000);
            }

            if (varPos1 + stateVarIntLen + stateLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("State", varPos1 + stateVarIntLen + stateLen, buf.readableBytes());
            }

            obj.state = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 13;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 5);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 13) {
            throw ProtocolException.invalidOffset("Id", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 13 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 9);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 13) {
            throw ProtocolException.invalidOffset("State", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 13 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(sl) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 13L;
   }

   @Nullable
   public static String getId(MemorySegment mem) {
      return getId(mem, 0);
   }

   @Nullable
   public static String getId(MemorySegment mem, int offset) {
      return hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 5, 13, "Id"), 4096000, PacketIO.UTF8) : null;
   }

   @Nullable
   public static String getState(MemorySegment mem) {
      return getState(mem, 0);
   }

   @Nullable
   public static String getState(MemorySegment mem, int offset) {
      return hasState(mem, offset)
         ? PacketIO.readVarString("State", mem, offset + getValidatedOffset(mem, offset, 9, 13, "State"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static int getTagIndex(MemorySegment mem) {
      return getTagIndex(mem, 0);
   }

   public static int getTagIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   public static boolean hasId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasState(MemorySegment mem, int offset) {
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

   public static BlockIdMatcher toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static BlockIdMatcher toObject(MemorySegment mem, int offset) {
      if (offset + 13 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("BlockIdMatcher", offset + 13, (int)mem.byteSize());
      } else {
         return new BlockIdMatcher(
            hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 5, 13, "Id"), 4096000, PacketIO.UTF8) : null,
            hasState(mem, offset)
               ? PacketIO.readVarString("State", mem, offset + getValidatedOffset(mem, offset, 9, 13, "State"), 4096000, PacketIO.UTF8)
               : null,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.id != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.state != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.tagIndex);
      int idOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int stateOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.id != null) {
         buf.setIntLE(idOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.id, 4096000);
      } else {
         buf.setIntLE(idOffsetSlot, -1);
      }

      if (this.state != null) {
         buf.setIntLE(stateOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.state, 4096000);
      } else {
         buf.setIntLE(stateOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.id != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.state != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.tagIndex);
      int varOffset = offset + 13;
      if (this.id != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), varOffset - offset - 13);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.id, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), -1);
      }

      if (this.state != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), varOffset - offset - 13);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.state, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 13;
      if (this.id != null) {
         size += PacketIO.stringSize(this.id);
      }

      if (this.state != null) {
         size += PacketIO.stringSize(this.state);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 13) {
         return ValidationResult.error("Buffer too small: expected at least 13 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int idOffset = buffer.getIntLE(offset + 5);
            if (idOffset < 0 || idOffset > buffer.writerIndex() - offset - 13) {
               return ValidationResult.error("Invalid offset for Id");
            }

            int pos = offset + 13 + idOffset;
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
            int stateOffset = buffer.getIntLE(offset + 9);
            if (stateOffset < 0 || stateOffset > buffer.writerIndex() - offset - 13) {
               return ValidationResult.error("Invalid offset for State");
            }

            int posx = offset + 13 + stateOffset;
            int stateLen = VarInt.peek(buffer, posx);
            if (stateLen < 0) {
               return ValidationResult.error("Invalid string length for State");
            }

            if (stateLen > 4096000) {
               return ValidationResult.error("State exceeds max length 4096000");
            }

            posx += VarInt.size(stateLen);
            posx += stateLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading State");
            }
         }

         return ValidationResult.OK;
      }
   }

   public BlockIdMatcher clone() {
      BlockIdMatcher copy = new BlockIdMatcher();
      copy.id = this.id;
      copy.state = this.state;
      copy.tagIndex = this.tagIndex;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BlockIdMatcher other)
            ? false
            : Objects.equals(this.id, other.id) && Objects.equals(this.state, other.state) && this.tagIndex == other.tagIndex;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.id, this.state, this.tagIndex);
   }
}
