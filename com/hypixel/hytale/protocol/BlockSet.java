package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockSet {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 32768019;
   @Nullable
   public String name;
   @Nullable
   public int[] blocks;

   public BlockSet() {
   }

   public BlockSet(@Nullable String name, @Nullable int[] blocks) {
      this.name = name;
      this.blocks = blocks;
   }

   public BlockSet(@Nonnull BlockSet other) {
      this.name = other.name;
      this.blocks = other.blocks;
   }

   @Nonnull
   public static BlockSet deserialize(@Nonnull ByteBuf buf, int offset) {
      BlockSet obj = new BlockSet();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 9 + buf.getIntLE(offset + 1);
         int nameLen = VarInt.peek(buf, varPos0);
         if (nameLen < 0) {
            throw ProtocolException.negativeLength("Name", nameLen);
         }

         if (nameLen > 4096000) {
            throw ProtocolException.stringTooLong("Name", nameLen, 4096000);
         }

         obj.name = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 9 + buf.getIntLE(offset + 5);
         int blocksCount = VarInt.peek(buf, varPos1);
         if (blocksCount < 0) {
            throw ProtocolException.negativeLength("Blocks", blocksCount);
         }

         if (blocksCount > 4096000) {
            throw ProtocolException.arrayTooLong("Blocks", blocksCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos1);
         if (varPos1 + varIntLen + blocksCount * 4L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Blocks", varPos1 + varIntLen + blocksCount * 4, buf.readableBytes());
         }

         obj.blocks = new int[blocksCount];

         for (int i = 0; i < blocksCount; i++) {
            obj.blocks[i] = buf.getIntLE(varPos1 + varIntLen + i * 4);
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 9;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         int pos0 = offset + 9 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         int pos1 = offset + 9 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1) + arrLen * 4;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.name != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.blocks != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      int nameOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int blocksOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.name != null) {
         buf.setIntLE(nameOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.name, 4096000);
      } else {
         buf.setIntLE(nameOffsetSlot, -1);
      }

      if (this.blocks != null) {
         buf.setIntLE(blocksOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.blocks.length > 4096000) {
            throw ProtocolException.arrayTooLong("Blocks", this.blocks.length, 4096000);
         }

         VarInt.write(buf, this.blocks.length);

         for (int item : this.blocks) {
            buf.writeIntLE(item);
         }
      } else {
         buf.setIntLE(blocksOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 9;
      if (this.name != null) {
         size += PacketIO.stringSize(this.name);
      }

      if (this.blocks != null) {
         size += VarInt.size(this.blocks.length) + this.blocks.length * 4;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 9) {
         return ValidationResult.error("Buffer too small: expected at least 9 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int nameOffset = buffer.getIntLE(offset + 1);
            if (nameOffset < 0) {
               return ValidationResult.error("Invalid offset for Name");
            }

            int pos = offset + 9 + nameOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Name");
            }

            int nameLen = VarInt.peek(buffer, pos);
            if (nameLen < 0) {
               return ValidationResult.error("Invalid string length for Name");
            }

            if (nameLen > 4096000) {
               return ValidationResult.error("Name exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += nameLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Name");
            }
         }

         if ((nullBits & 2) != 0) {
            int blocksOffset = buffer.getIntLE(offset + 5);
            if (blocksOffset < 0) {
               return ValidationResult.error("Invalid offset for Blocks");
            }

            int posx = offset + 9 + blocksOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Blocks");
            }

            int blocksCount = VarInt.peek(buffer, posx);
            if (blocksCount < 0) {
               return ValidationResult.error("Invalid array count for Blocks");
            }

            if (blocksCount > 4096000) {
               return ValidationResult.error("Blocks exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
            posx += blocksCount * 4;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Blocks");
            }
         }

         return ValidationResult.OK;
      }
   }

   public BlockSet clone() {
      BlockSet copy = new BlockSet();
      copy.name = this.name;
      copy.blocks = this.blocks != null ? Arrays.copyOf(this.blocks, this.blocks.length) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BlockSet other) ? false : Objects.equals(this.name, other.name) && Arrays.equals(this.blocks, other.blocks);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.name);
      return 31 * result + Arrays.hashCode(this.blocks);
   }
}
