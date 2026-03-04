package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
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
      BlockIdMatcher obj = new BlockIdMatcher();
      byte nullBits = buf.getByte(offset);
      obj.tagIndex = buf.getIntLE(offset + 1);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 13 + buf.getIntLE(offset + 5);
         int idLen = VarInt.peek(buf, varPos0);
         if (idLen < 0) {
            throw ProtocolException.negativeLength("Id", idLen);
         }

         if (idLen > 4096000) {
            throw ProtocolException.stringTooLong("Id", idLen, 4096000);
         }

         obj.id = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 13 + buf.getIntLE(offset + 9);
         int stateLen = VarInt.peek(buf, varPos1);
         if (stateLen < 0) {
            throw ProtocolException.negativeLength("State", stateLen);
         }

         if (stateLen > 4096000) {
            throw ProtocolException.stringTooLong("State", stateLen, 4096000);
         }

         obj.state = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 13;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 5);
         int pos0 = offset + 13 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 9);
         int pos1 = offset + 13 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
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
            if (idOffset < 0) {
               return ValidationResult.error("Invalid offset for Id");
            }

            int pos = offset + 13 + idOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Id");
            }

            int idLen = VarInt.peek(buffer, pos);
            if (idLen < 0) {
               return ValidationResult.error("Invalid string length for Id");
            }

            if (idLen > 4096000) {
               return ValidationResult.error("Id exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += idLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Id");
            }
         }

         if ((nullBits & 2) != 0) {
            int stateOffset = buffer.getIntLE(offset + 9);
            if (stateOffset < 0) {
               return ValidationResult.error("Invalid offset for State");
            }

            int posx = offset + 13 + stateOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for State");
            }

            int stateLen = VarInt.peek(buffer, posx);
            if (stateLen < 0) {
               return ValidationResult.error("Invalid string length for State");
            }

            if (stateLen > 4096000) {
               return ValidationResult.error("State exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
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
