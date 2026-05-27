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

public class BlockTextures {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 6;
   public static final int VARIABLE_BLOCK_START = 29;
   public static final int MAX_SIZE = 98304059;
   @Nullable
   public String top;
   @Nullable
   public String bottom;
   @Nullable
   public String front;
   @Nullable
   public String back;
   @Nullable
   public String left;
   @Nullable
   public String right;
   public float weight;

   public BlockTextures() {
   }

   public BlockTextures(
      @Nullable String top, @Nullable String bottom, @Nullable String front, @Nullable String back, @Nullable String left, @Nullable String right, float weight
   ) {
      this.top = top;
      this.bottom = bottom;
      this.front = front;
      this.back = back;
      this.left = left;
      this.right = right;
      this.weight = weight;
   }

   public BlockTextures(@Nonnull BlockTextures other) {
      this.top = other.top;
      this.bottom = other.bottom;
      this.front = other.front;
      this.back = other.back;
      this.left = other.left;
      this.right = other.right;
      this.weight = other.weight;
   }

   @Nonnull
   public static BlockTextures deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 29) {
         throw ProtocolException.bufferTooSmall("BlockTextures", 29, buf.readableBytes() - offset);
      } else {
         BlockTextures obj = new BlockTextures();
         byte nullBits = buf.getByte(offset);
         obj.weight = buf.getFloatLE(offset + 1);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 5);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 29) {
               throw ProtocolException.invalidOffset("Top", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 29 + varPosBase0;
            int topLen = VarInt.peek(buf, varPos0);
            if (topLen < 0) {
               throw ProtocolException.invalidVarInt("Top");
            }

            int topVarIntLen = VarInt.size(topLen);
            if (topLen > 4096000) {
               throw ProtocolException.stringTooLong("Top", topLen, 4096000);
            }

            if (varPos0 + topVarIntLen + topLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Top", varPos0 + topVarIntLen + topLen, buf.readableBytes());
            }

            obj.top = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 9);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 29) {
               throw ProtocolException.invalidOffset("Bottom", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 29 + varPosBase1;
            int bottomLen = VarInt.peek(buf, varPos1);
            if (bottomLen < 0) {
               throw ProtocolException.invalidVarInt("Bottom");
            }

            int bottomVarIntLen = VarInt.size(bottomLen);
            if (bottomLen > 4096000) {
               throw ProtocolException.stringTooLong("Bottom", bottomLen, 4096000);
            }

            if (varPos1 + bottomVarIntLen + bottomLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Bottom", varPos1 + bottomVarIntLen + bottomLen, buf.readableBytes());
            }

            obj.bottom = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
         }

         if ((nullBits & 4) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 13);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 29) {
               throw ProtocolException.invalidOffset("Front", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 29 + varPosBase2;
            int frontLen = VarInt.peek(buf, varPos2);
            if (frontLen < 0) {
               throw ProtocolException.invalidVarInt("Front");
            }

            int frontVarIntLen = VarInt.size(frontLen);
            if (frontLen > 4096000) {
               throw ProtocolException.stringTooLong("Front", frontLen, 4096000);
            }

            if (varPos2 + frontVarIntLen + frontLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Front", varPos2 + frontVarIntLen + frontLen, buf.readableBytes());
            }

            obj.front = PacketIO.readVarString(buf, varPos2, PacketIO.UTF8);
         }

         if ((nullBits & 8) != 0) {
            int varPosBase3 = buf.getIntLE(offset + 17);
            if (varPosBase3 < 0 || varPosBase3 > buf.writerIndex() - offset - 29) {
               throw ProtocolException.invalidOffset("Back", varPosBase3, buf.readableBytes());
            }

            int varPos3 = offset + 29 + varPosBase3;
            int backLen = VarInt.peek(buf, varPos3);
            if (backLen < 0) {
               throw ProtocolException.invalidVarInt("Back");
            }

            int backVarIntLen = VarInt.size(backLen);
            if (backLen > 4096000) {
               throw ProtocolException.stringTooLong("Back", backLen, 4096000);
            }

            if (varPos3 + backVarIntLen + backLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Back", varPos3 + backVarIntLen + backLen, buf.readableBytes());
            }

            obj.back = PacketIO.readVarString(buf, varPos3, PacketIO.UTF8);
         }

         if ((nullBits & 16) != 0) {
            int varPosBase4 = buf.getIntLE(offset + 21);
            if (varPosBase4 < 0 || varPosBase4 > buf.writerIndex() - offset - 29) {
               throw ProtocolException.invalidOffset("Left", varPosBase4, buf.readableBytes());
            }

            int varPos4 = offset + 29 + varPosBase4;
            int leftLen = VarInt.peek(buf, varPos4);
            if (leftLen < 0) {
               throw ProtocolException.invalidVarInt("Left");
            }

            int leftVarIntLen = VarInt.size(leftLen);
            if (leftLen > 4096000) {
               throw ProtocolException.stringTooLong("Left", leftLen, 4096000);
            }

            if (varPos4 + leftVarIntLen + leftLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Left", varPos4 + leftVarIntLen + leftLen, buf.readableBytes());
            }

            obj.left = PacketIO.readVarString(buf, varPos4, PacketIO.UTF8);
         }

         if ((nullBits & 32) != 0) {
            int varPosBase5 = buf.getIntLE(offset + 25);
            if (varPosBase5 < 0 || varPosBase5 > buf.writerIndex() - offset - 29) {
               throw ProtocolException.invalidOffset("Right", varPosBase5, buf.readableBytes());
            }

            int varPos5 = offset + 29 + varPosBase5;
            int rightLen = VarInt.peek(buf, varPos5);
            if (rightLen < 0) {
               throw ProtocolException.invalidVarInt("Right");
            }

            int rightVarIntLen = VarInt.size(rightLen);
            if (rightLen > 4096000) {
               throw ProtocolException.stringTooLong("Right", rightLen, 4096000);
            }

            if (varPos5 + rightVarIntLen + rightLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Right", varPos5 + rightVarIntLen + rightLen, buf.readableBytes());
            }

            obj.right = PacketIO.readVarString(buf, varPos5, PacketIO.UTF8);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 29;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 5);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 29) {
            throw ProtocolException.invalidOffset("Top", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 29 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 9);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 29) {
            throw ProtocolException.invalidOffset("Bottom", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 29 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(sl) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 13);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 29) {
            throw ProtocolException.invalidOffset("Front", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 29 + fieldOffset2;
         int sl = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(sl) + sl;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 17);
         if (fieldOffset3 < 0 || fieldOffset3 > buf.writerIndex() - offset - 29) {
            throw ProtocolException.invalidOffset("Back", fieldOffset3, maxEnd);
         }

         int pos3 = offset + 29 + fieldOffset3;
         int sl = VarInt.peek(buf, pos3);
         pos3 += VarInt.size(sl) + sl;
         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      if ((nullBits & 16) != 0) {
         int fieldOffset4 = buf.getIntLE(offset + 21);
         if (fieldOffset4 < 0 || fieldOffset4 > buf.writerIndex() - offset - 29) {
            throw ProtocolException.invalidOffset("Left", fieldOffset4, maxEnd);
         }

         int pos4 = offset + 29 + fieldOffset4;
         int sl = VarInt.peek(buf, pos4);
         pos4 += VarInt.size(sl) + sl;
         if (pos4 - offset > maxEnd) {
            maxEnd = pos4 - offset;
         }
      }

      if ((nullBits & 32) != 0) {
         int fieldOffset5 = buf.getIntLE(offset + 25);
         if (fieldOffset5 < 0 || fieldOffset5 > buf.writerIndex() - offset - 29) {
            throw ProtocolException.invalidOffset("Right", fieldOffset5, maxEnd);
         }

         int pos5 = offset + 29 + fieldOffset5;
         int sl = VarInt.peek(buf, pos5);
         pos5 += VarInt.size(sl) + sl;
         if (pos5 - offset > maxEnd) {
            maxEnd = pos5 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 29L;
   }

   @Nullable
   public static String getTop(MemorySegment mem) {
      return getTop(mem, 0);
   }

   @Nullable
   public static String getTop(MemorySegment mem, int offset) {
      return hasTop(mem, offset) ? PacketIO.readVarString("Top", mem, offset + getValidatedOffset(mem, offset, 5, 29, "Top"), 4096000, PacketIO.UTF8) : null;
   }

   @Nullable
   public static String getBottom(MemorySegment mem) {
      return getBottom(mem, 0);
   }

   @Nullable
   public static String getBottom(MemorySegment mem, int offset) {
      return hasBottom(mem, offset)
         ? PacketIO.readVarString("Bottom", mem, offset + getValidatedOffset(mem, offset, 9, 29, "Bottom"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static String getFront(MemorySegment mem) {
      return getFront(mem, 0);
   }

   @Nullable
   public static String getFront(MemorySegment mem, int offset) {
      return hasFront(mem, offset)
         ? PacketIO.readVarString("Front", mem, offset + getValidatedOffset(mem, offset, 13, 29, "Front"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static String getBack(MemorySegment mem) {
      return getBack(mem, 0);
   }

   @Nullable
   public static String getBack(MemorySegment mem, int offset) {
      return hasBack(mem, offset)
         ? PacketIO.readVarString("Back", mem, offset + getValidatedOffset(mem, offset, 17, 29, "Back"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static String getLeft(MemorySegment mem) {
      return getLeft(mem, 0);
   }

   @Nullable
   public static String getLeft(MemorySegment mem, int offset) {
      return hasLeft(mem, offset)
         ? PacketIO.readVarString("Left", mem, offset + getValidatedOffset(mem, offset, 21, 29, "Left"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static String getRight(MemorySegment mem) {
      return getRight(mem, 0);
   }

   @Nullable
   public static String getRight(MemorySegment mem, int offset) {
      return hasRight(mem, offset)
         ? PacketIO.readVarString("Right", mem, offset + getValidatedOffset(mem, offset, 25, 29, "Right"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static float getWeight(MemorySegment mem) {
      return getWeight(mem, 0);
   }

   public static float getWeight(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1));
   }

   public static boolean hasTop(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasBottom(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasFront(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasBack(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   public static boolean hasLeft(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 16) != 0;
   }

   public static boolean hasRight(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 32) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static BlockTextures toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static BlockTextures toObject(MemorySegment mem, int offset) {
      if (offset + 29 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("BlockTextures", offset + 29, (int)mem.byteSize());
      } else {
         return new BlockTextures(
            hasTop(mem, offset) ? PacketIO.readVarString("Top", mem, offset + getValidatedOffset(mem, offset, 5, 29, "Top"), 4096000, PacketIO.UTF8) : null,
            hasBottom(mem, offset)
               ? PacketIO.readVarString("Bottom", mem, offset + getValidatedOffset(mem, offset, 9, 29, "Bottom"), 4096000, PacketIO.UTF8)
               : null,
            hasFront(mem, offset)
               ? PacketIO.readVarString("Front", mem, offset + getValidatedOffset(mem, offset, 13, 29, "Front"), 4096000, PacketIO.UTF8)
               : null,
            hasBack(mem, offset) ? PacketIO.readVarString("Back", mem, offset + getValidatedOffset(mem, offset, 17, 29, "Back"), 4096000, PacketIO.UTF8) : null,
            hasLeft(mem, offset) ? PacketIO.readVarString("Left", mem, offset + getValidatedOffset(mem, offset, 21, 29, "Left"), 4096000, PacketIO.UTF8) : null,
            hasRight(mem, offset)
               ? PacketIO.readVarString("Right", mem, offset + getValidatedOffset(mem, offset, 25, 29, "Right"), 4096000, PacketIO.UTF8)
               : null,
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.top != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.bottom != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.front != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.back != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.left != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.right != null) {
         nullBits = (byte)(nullBits | 32);
      }

      buf.writeByte(nullBits);
      buf.writeFloatLE(this.weight);
      int topOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int bottomOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int frontOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int backOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int leftOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int rightOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.top != null) {
         buf.setIntLE(topOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.top, 4096000);
      } else {
         buf.setIntLE(topOffsetSlot, -1);
      }

      if (this.bottom != null) {
         buf.setIntLE(bottomOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.bottom, 4096000);
      } else {
         buf.setIntLE(bottomOffsetSlot, -1);
      }

      if (this.front != null) {
         buf.setIntLE(frontOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.front, 4096000);
      } else {
         buf.setIntLE(frontOffsetSlot, -1);
      }

      if (this.back != null) {
         buf.setIntLE(backOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.back, 4096000);
      } else {
         buf.setIntLE(backOffsetSlot, -1);
      }

      if (this.left != null) {
         buf.setIntLE(leftOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.left, 4096000);
      } else {
         buf.setIntLE(leftOffsetSlot, -1);
      }

      if (this.right != null) {
         buf.setIntLE(rightOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.right, 4096000);
      } else {
         buf.setIntLE(rightOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.top != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.bottom != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.front != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.back != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.left != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.right != null) {
         nullBits = (byte)(nullBits | 32);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 1), this.weight);
      int varOffset = offset + 29;
      if (this.top != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), varOffset - offset - 29);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.top, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), -1);
      }

      if (this.bottom != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), varOffset - offset - 29);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.bottom, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), -1);
      }

      if (this.front != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 13), varOffset - offset - 29);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.front, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 13), -1);
      }

      if (this.back != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 17), varOffset - offset - 29);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.back, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 17), -1);
      }

      if (this.left != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 21), varOffset - offset - 29);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.left, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 21), -1);
      }

      if (this.right != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 25), varOffset - offset - 29);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.right, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 25), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 29;
      if (this.top != null) {
         size += PacketIO.stringSize(this.top);
      }

      if (this.bottom != null) {
         size += PacketIO.stringSize(this.bottom);
      }

      if (this.front != null) {
         size += PacketIO.stringSize(this.front);
      }

      if (this.back != null) {
         size += PacketIO.stringSize(this.back);
      }

      if (this.left != null) {
         size += PacketIO.stringSize(this.left);
      }

      if (this.right != null) {
         size += PacketIO.stringSize(this.right);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 29) {
         return ValidationResult.error("Buffer too small: expected at least 29 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int topOffset = buffer.getIntLE(offset + 5);
            if (topOffset < 0 || topOffset > buffer.writerIndex() - offset - 29) {
               return ValidationResult.error("Invalid offset for Top");
            }

            int pos = offset + 29 + topOffset;
            int topLen = VarInt.peek(buffer, pos);
            if (topLen < 0) {
               return ValidationResult.error("Invalid string length for Top");
            }

            if (topLen > 4096000) {
               return ValidationResult.error("Top exceeds max length 4096000");
            }

            pos += VarInt.size(topLen);
            pos += topLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Top");
            }
         }

         if ((nullBits & 2) != 0) {
            int bottomOffset = buffer.getIntLE(offset + 9);
            if (bottomOffset < 0 || bottomOffset > buffer.writerIndex() - offset - 29) {
               return ValidationResult.error("Invalid offset for Bottom");
            }

            int posx = offset + 29 + bottomOffset;
            int bottomLen = VarInt.peek(buffer, posx);
            if (bottomLen < 0) {
               return ValidationResult.error("Invalid string length for Bottom");
            }

            if (bottomLen > 4096000) {
               return ValidationResult.error("Bottom exceeds max length 4096000");
            }

            posx += VarInt.size(bottomLen);
            posx += bottomLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Bottom");
            }
         }

         if ((nullBits & 4) != 0) {
            int frontOffset = buffer.getIntLE(offset + 13);
            if (frontOffset < 0 || frontOffset > buffer.writerIndex() - offset - 29) {
               return ValidationResult.error("Invalid offset for Front");
            }

            int posxx = offset + 29 + frontOffset;
            int frontLen = VarInt.peek(buffer, posxx);
            if (frontLen < 0) {
               return ValidationResult.error("Invalid string length for Front");
            }

            if (frontLen > 4096000) {
               return ValidationResult.error("Front exceeds max length 4096000");
            }

            posxx += VarInt.size(frontLen);
            posxx += frontLen;
            if (posxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Front");
            }
         }

         if ((nullBits & 8) != 0) {
            int backOffset = buffer.getIntLE(offset + 17);
            if (backOffset < 0 || backOffset > buffer.writerIndex() - offset - 29) {
               return ValidationResult.error("Invalid offset for Back");
            }

            int posxxx = offset + 29 + backOffset;
            int backLen = VarInt.peek(buffer, posxxx);
            if (backLen < 0) {
               return ValidationResult.error("Invalid string length for Back");
            }

            if (backLen > 4096000) {
               return ValidationResult.error("Back exceeds max length 4096000");
            }

            posxxx += VarInt.size(backLen);
            posxxx += backLen;
            if (posxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Back");
            }
         }

         if ((nullBits & 16) != 0) {
            int leftOffset = buffer.getIntLE(offset + 21);
            if (leftOffset < 0 || leftOffset > buffer.writerIndex() - offset - 29) {
               return ValidationResult.error("Invalid offset for Left");
            }

            int posxxxx = offset + 29 + leftOffset;
            int leftLen = VarInt.peek(buffer, posxxxx);
            if (leftLen < 0) {
               return ValidationResult.error("Invalid string length for Left");
            }

            if (leftLen > 4096000) {
               return ValidationResult.error("Left exceeds max length 4096000");
            }

            posxxxx += VarInt.size(leftLen);
            posxxxx += leftLen;
            if (posxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Left");
            }
         }

         if ((nullBits & 32) != 0) {
            int rightOffset = buffer.getIntLE(offset + 25);
            if (rightOffset < 0 || rightOffset > buffer.writerIndex() - offset - 29) {
               return ValidationResult.error("Invalid offset for Right");
            }

            int posxxxxx = offset + 29 + rightOffset;
            int rightLen = VarInt.peek(buffer, posxxxxx);
            if (rightLen < 0) {
               return ValidationResult.error("Invalid string length for Right");
            }

            if (rightLen > 4096000) {
               return ValidationResult.error("Right exceeds max length 4096000");
            }

            posxxxxx += VarInt.size(rightLen);
            posxxxxx += rightLen;
            if (posxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Right");
            }
         }

         return ValidationResult.OK;
      }
   }

   public BlockTextures clone() {
      BlockTextures copy = new BlockTextures();
      copy.top = this.top;
      copy.bottom = this.bottom;
      copy.front = this.front;
      copy.back = this.back;
      copy.left = this.left;
      copy.right = this.right;
      copy.weight = this.weight;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BlockTextures other)
            ? false
            : Objects.equals(this.top, other.top)
               && Objects.equals(this.bottom, other.bottom)
               && Objects.equals(this.front, other.front)
               && Objects.equals(this.back, other.back)
               && Objects.equals(this.left, other.left)
               && Objects.equals(this.right, other.right)
               && this.weight == other.weight;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.top, this.bottom, this.front, this.back, this.left, this.right, this.weight);
   }
}
