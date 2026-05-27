package com.hypixel.hytale.protocol;

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
      if (buf.readableBytes() - offset < 9) {
         throw ProtocolException.bufferTooSmall("BlockSet", 9, buf.readableBytes() - offset);
      } else {
         BlockSet obj = new BlockSet();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 1);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 9) {
               throw ProtocolException.invalidOffset("Name", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 9 + varPosBase0;
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
            int varPosBase1 = buf.getIntLE(offset + 5);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 9) {
               throw ProtocolException.invalidOffset("Blocks", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 9 + varPosBase1;
            int blocksCount = VarInt.peek(buf, varPos1);
            if (blocksCount < 0) {
               throw ProtocolException.invalidVarInt("Blocks");
            }

            int varIntLen = VarInt.size(blocksCount);
            if (blocksCount > 4096000) {
               throw ProtocolException.arrayTooLong("Blocks", blocksCount, 4096000);
            }

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
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 9;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 9) {
            throw ProtocolException.invalidOffset("Name", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 9 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 9) {
            throw ProtocolException.invalidOffset("Blocks", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 9 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(arrLen) + arrLen * 4;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 9L;
   }

   @Nullable
   public static String getName(MemorySegment mem) {
      return getName(mem, 0);
   }

   @Nullable
   public static String getName(MemorySegment mem, int offset) {
      return hasName(mem, offset) ? PacketIO.readVarString("Name", mem, offset + getValidatedOffset(mem, offset, 1, 9, "Name"), 4096000, PacketIO.UTF8) : null;
   }

   @Nullable
   public static int[] getBlocks(MemorySegment mem) {
      return getBlocks(mem, 0);
   }

   @Nullable
   public static int[] getBlocks(MemorySegment mem, int offset) {
      if (!hasBlocks(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 5, 9, "Blocks");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Blocks", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Blocks", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 4L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Blocks", off + lenOffset + len * 4, (int)mem.byteSize());
            } else {
               off += lenOffset;
               int[] data = new int[len];
               MemorySegment.copy(mem, PacketIO.PROTO_INT, off, data, 0, len);
               return data;
            }
         }
      }
   }

   public static boolean hasName(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasBlocks(MemorySegment mem, int offset) {
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

   public static BlockSet toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static BlockSet toObject(MemorySegment mem, int offset) {
      if (offset + 9 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("BlockSet", offset + 9, (int)mem.byteSize());
      } else {
         int[] blocks = null;
         if (hasBlocks(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 5, 9, "Blocks");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Blocks", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Blocks", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 4L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Blocks", off + lenOffset + len * 4, (int)mem.byteSize());
            }

            off += lenOffset;
            blocks = new int[len];
            MemorySegment.copy(mem, PacketIO.PROTO_INT, off, blocks, 0, len);
         }

         return new BlockSet(
            hasName(mem, offset) ? PacketIO.readVarString("Name", mem, offset + getValidatedOffset(mem, offset, 1, 9, "Name"), 4096000, PacketIO.UTF8) : null,
            blocks
         );
      }
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

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.name != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.blocks != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 9;
      if (this.name != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), varOffset - offset - 9);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.name, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), -1);
      }

      if (this.blocks != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), varOffset - offset - 9);
         if (this.blocks.length > 4096000) {
            throw ProtocolException.arrayTooLong("Blocks", this.blocks.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.blocks.length);
         MemorySegment.copy(this.blocks, 0, mem, PacketIO.PROTO_INT, varOffset, this.blocks.length);
         varOffset += this.blocks.length * 4;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), -1);
      }

      return varOffset - offset;
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
            if (nameOffset < 0 || nameOffset > buffer.writerIndex() - offset - 9) {
               return ValidationResult.error("Invalid offset for Name");
            }

            int pos = offset + 9 + nameOffset;
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
            int blocksOffset = buffer.getIntLE(offset + 5);
            if (blocksOffset < 0 || blocksOffset > buffer.writerIndex() - offset - 9) {
               return ValidationResult.error("Invalid offset for Blocks");
            }

            int posx = offset + 9 + blocksOffset;
            int blocksCount = VarInt.peek(buffer, posx);
            if (blocksCount < 0) {
               return ValidationResult.error("Invalid array count for Blocks");
            }

            if (blocksCount > 4096000) {
               return ValidationResult.error("Blocks exceeds max length 4096000");
            }

            posx += VarInt.size(blocksCount);
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
