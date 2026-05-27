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

public class TagPattern {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 6;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 14;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public TagPatternType type = TagPatternType.Equals;
   public int tagIndex;
   @Nullable
   public TagPattern[] operands;
   @Nullable
   public TagPattern not;

   public TagPattern() {
   }

   public TagPattern(@Nonnull TagPatternType type, int tagIndex, @Nullable TagPattern[] operands, @Nullable TagPattern not) {
      this.type = type;
      this.tagIndex = tagIndex;
      this.operands = operands;
      this.not = not;
   }

   public TagPattern(@Nonnull TagPattern other) {
      this.type = other.type;
      this.tagIndex = other.tagIndex;
      this.operands = other.operands;
      this.not = other.not;
   }

   @Nonnull
   public static TagPattern deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 14) {
         throw ProtocolException.bufferTooSmall("TagPattern", 14, buf.readableBytes() - offset);
      } else {
         TagPattern obj = new TagPattern();
         byte nullBits = buf.getByte(offset);
         obj.type = TagPatternType.fromValue(buf.getByte(offset + 1));
         obj.tagIndex = buf.getIntLE(offset + 2);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 6);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 14) {
               throw ProtocolException.invalidOffset("Operands", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 14 + varPosBase0;
            int operandsCount = VarInt.peek(buf, varPos0);
            if (operandsCount < 0) {
               throw ProtocolException.invalidVarInt("Operands");
            }

            int varIntLen = VarInt.size(operandsCount);
            if (operandsCount > 4096000) {
               throw ProtocolException.arrayTooLong("Operands", operandsCount, 4096000);
            }

            if (varPos0 + varIntLen + operandsCount * 6L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Operands", varPos0 + varIntLen + operandsCount * 6, buf.readableBytes());
            }

            obj.operands = new TagPattern[operandsCount];
            int elemPos = varPos0 + varIntLen;

            for (int i = 0; i < operandsCount; i++) {
               obj.operands[i] = deserialize(buf, elemPos);
               elemPos += computeBytesConsumed(buf, elemPos);
            }
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 10);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 14) {
               throw ProtocolException.invalidOffset("Not", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 14 + varPosBase1;
            obj.not = deserialize(buf, varPos1);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 14;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 6);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 14) {
            throw ProtocolException.invalidOffset("Operands", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 14 + fieldOffset0;
         int arrLen = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos0 += computeBytesConsumed(buf, pos0);
         }

         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 10);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 14) {
            throw ProtocolException.invalidOffset("Not", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 14 + fieldOffset1;
         pos1 += computeBytesConsumed(buf, pos1);
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 14L;
   }

   public static TagPatternType getType(MemorySegment mem) {
      return getType(mem, 0);
   }

   public static TagPatternType getType(MemorySegment mem, int offset) {
      return TagPatternType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1)));
   }

   public static int getTagIndex(MemorySegment mem) {
      return getTagIndex(mem, 0);
   }

   public static int getTagIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 2));
   }

   @Nullable
   public static TagPattern[] getOperands(MemorySegment mem) {
      return getOperands(mem, 0);
   }

   @Nullable
   public static TagPattern[] getOperands(MemorySegment mem, int offset) {
      if (!hasOperands(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 6, 14, "Operands");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Operands", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Operands", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Operands", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               TagPattern[] data = new TagPattern[len];

               for (int i = 0; i < len; i++) {
                  data[i] = toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   @Nullable
   public static TagPattern getNot(MemorySegment mem) {
      return getNot(mem, 0);
   }

   @Nullable
   public static TagPattern getNot(MemorySegment mem, int offset) {
      return hasNot(mem, offset) ? toObject(mem, offset + getValidatedOffset(mem, offset, 10, 14, "Not")) : null;
   }

   public static boolean hasOperands(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasNot(MemorySegment mem, int offset) {
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

   public static TagPattern toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static TagPattern toObject(MemorySegment mem, int offset) {
      if (offset + 14 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("TagPattern", offset + 14, (int)mem.byteSize());
      } else {
         TagPattern[] operands = null;
         if (hasOperands(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 6, 14, "Operands");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Operands", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Operands", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Operands", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            operands = new TagPattern[len];

            for (int i = 0; i < len; i++) {
               operands[i] = toObject(mem, off);
               off += operands[i].computeSize();
            }
         }

         return new TagPattern(
            TagPatternType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 2)),
            operands,
            hasNot(mem, offset) ? toObject(mem, offset + getValidatedOffset(mem, offset, 10, 14, "Not")) : null
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.operands != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.not != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      buf.writeIntLE(this.tagIndex);
      int operandsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int notOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.operands != null) {
         buf.setIntLE(operandsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.operands.length > 4096000) {
            throw ProtocolException.arrayTooLong("Operands", this.operands.length, 4096000);
         }

         VarInt.write(buf, this.operands.length);

         for (TagPattern item : this.operands) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(operandsOffsetSlot, -1);
      }

      if (this.not != null) {
         buf.setIntLE(notOffsetSlot, buf.writerIndex() - varBlockStart);
         this.not.serialize(buf);
      } else {
         buf.setIntLE(notOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.operands != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.not != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.type.getValue());
      mem.set(PacketIO.PROTO_INT, (long)(offset + 2), this.tagIndex);
      int varOffset = offset + 14;
      if (this.operands != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 6), varOffset - offset - 14);
         if (this.operands.length > 4096000) {
            throw ProtocolException.arrayTooLong("Operands", this.operands.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.operands.length);
         int operandsValueOffset = 0;

         for (int i = 0; i < this.operands.length; i++) {
            operandsValueOffset += this.operands[i].serialize(mem, varOffset + operandsValueOffset);
         }

         varOffset += operandsValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 6), -1);
      }

      if (this.not != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 10), varOffset - offset - 14);
         varOffset += this.not.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 10), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 14;
      if (this.operands != null) {
         int operandsSize = 0;

         for (TagPattern elem : this.operands) {
            operandsSize += elem.computeSize();
         }

         size += VarInt.size(this.operands.length) + operandsSize;
      }

      if (this.not != null) {
         size += this.not.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 14) {
         return ValidationResult.error("Buffer too small: expected at least 14 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int v = buffer.getByte(offset + 1) & 255;
         if (v >= 4) {
            return ValidationResult.error("Invalid TagPatternType value for Type");
         } else {
            if ((nullBits & 1) != 0) {
               v = buffer.getIntLE(offset + 6);
               if (v < 0 || v > buffer.writerIndex() - offset - 14) {
                  return ValidationResult.error("Invalid offset for Operands");
               }

               int pos = offset + 14 + v;
               int operandsCount = VarInt.peek(buffer, pos);
               if (operandsCount < 0) {
                  return ValidationResult.error("Invalid array count for Operands");
               }

               if (operandsCount > 4096000) {
                  return ValidationResult.error("Operands exceeds max length 4096000");
               }

               pos += VarInt.size(operandsCount);

               for (int i = 0; i < operandsCount; i++) {
                  ValidationResult structResult = validateStructure(buffer, pos);
                  if (!structResult.isValid()) {
                     return ValidationResult.error("Invalid TagPattern in Operands[" + i + "]: " + structResult.error());
                  }

                  pos += computeBytesConsumed(buffer, pos);
               }
            }

            if ((nullBits & 2) != 0) {
               v = buffer.getIntLE(offset + 10);
               if (v < 0 || v > buffer.writerIndex() - offset - 14) {
                  return ValidationResult.error("Invalid offset for Not");
               }

               int posx = offset + 14 + v;
               ValidationResult notResult = validateStructure(buffer, posx);
               if (!notResult.isValid()) {
                  return ValidationResult.error("Invalid Not: " + notResult.error());
               }

               posx += computeBytesConsumed(buffer, posx);
            }

            return ValidationResult.OK;
         }
      }
   }

   public TagPattern clone() {
      TagPattern copy = new TagPattern();
      copy.type = this.type;
      copy.tagIndex = this.tagIndex;
      copy.operands = this.operands != null ? Arrays.stream(this.operands).map(e -> e.clone()).toArray(TagPattern[]::new) : null;
      copy.not = this.not != null ? this.not.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof TagPattern other)
            ? false
            : Objects.equals(this.type, other.type)
               && this.tagIndex == other.tagIndex
               && Arrays.equals((Object[])this.operands, (Object[])other.operands)
               && Objects.equals(this.not, other.not);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.type);
      result = 31 * result + Integer.hashCode(this.tagIndex);
      result = 31 * result + Arrays.hashCode((Object[])this.operands);
      return 31 * result + Objects.hashCode(this.not);
   }
}
