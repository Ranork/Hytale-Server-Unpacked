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

public class RoofConnectedBlockRuleSet {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 9;
   public static final int VARIABLE_FIELD_COUNT = 3;
   public static final int VARIABLE_BLOCK_START = 21;
   public static final int MAX_SIZE = 49152078;
   @Nullable
   public StairConnectedBlockRuleSet regular;
   @Nullable
   public StairConnectedBlockRuleSet hollow;
   public int topperBlockId;
   public int width;
   @Nullable
   public String materialName;

   public RoofConnectedBlockRuleSet() {
   }

   public RoofConnectedBlockRuleSet(
      @Nullable StairConnectedBlockRuleSet regular, @Nullable StairConnectedBlockRuleSet hollow, int topperBlockId, int width, @Nullable String materialName
   ) {
      this.regular = regular;
      this.hollow = hollow;
      this.topperBlockId = topperBlockId;
      this.width = width;
      this.materialName = materialName;
   }

   public RoofConnectedBlockRuleSet(@Nonnull RoofConnectedBlockRuleSet other) {
      this.regular = other.regular;
      this.hollow = other.hollow;
      this.topperBlockId = other.topperBlockId;
      this.width = other.width;
      this.materialName = other.materialName;
   }

   @Nonnull
   public static RoofConnectedBlockRuleSet deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 21) {
         throw ProtocolException.bufferTooSmall("RoofConnectedBlockRuleSet", 21, buf.readableBytes() - offset);
      } else {
         RoofConnectedBlockRuleSet obj = new RoofConnectedBlockRuleSet();
         byte nullBits = buf.getByte(offset);
         obj.topperBlockId = buf.getIntLE(offset + 1);
         obj.width = buf.getIntLE(offset + 5);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 9);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 21) {
               throw ProtocolException.invalidOffset("Regular", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 21 + varPosBase0;
            obj.regular = StairConnectedBlockRuleSet.deserialize(buf, varPos0);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 13);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 21) {
               throw ProtocolException.invalidOffset("Hollow", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 21 + varPosBase1;
            obj.hollow = StairConnectedBlockRuleSet.deserialize(buf, varPos1);
         }

         if ((nullBits & 4) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 17);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 21) {
               throw ProtocolException.invalidOffset("MaterialName", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 21 + varPosBase2;
            int materialNameLen = VarInt.peek(buf, varPos2);
            if (materialNameLen < 0) {
               throw ProtocolException.invalidVarInt("MaterialName");
            }

            int materialNameVarIntLen = VarInt.size(materialNameLen);
            if (materialNameLen > 4096000) {
               throw ProtocolException.stringTooLong("MaterialName", materialNameLen, 4096000);
            }

            if (varPos2 + materialNameVarIntLen + materialNameLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("MaterialName", varPos2 + materialNameVarIntLen + materialNameLen, buf.readableBytes());
            }

            obj.materialName = PacketIO.readVarString(buf, varPos2, PacketIO.UTF8);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 21;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 9);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 21) {
            throw ProtocolException.invalidOffset("Regular", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 21 + fieldOffset0;
         pos0 += StairConnectedBlockRuleSet.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 13);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 21) {
            throw ProtocolException.invalidOffset("Hollow", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 21 + fieldOffset1;
         pos1 += StairConnectedBlockRuleSet.computeBytesConsumed(buf, pos1);
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 17);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 21) {
            throw ProtocolException.invalidOffset("MaterialName", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 21 + fieldOffset2;
         int sl = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(sl) + sl;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 21L;
   }

   @Nullable
   public static StairConnectedBlockRuleSet getRegular(MemorySegment mem) {
      return getRegular(mem, 0);
   }

   @Nullable
   public static StairConnectedBlockRuleSet getRegular(MemorySegment mem, int offset) {
      return hasRegular(mem, offset) ? StairConnectedBlockRuleSet.toObject(mem, offset + getValidatedOffset(mem, offset, 9, 21, "Regular")) : null;
   }

   @Nullable
   public static StairConnectedBlockRuleSet getHollow(MemorySegment mem) {
      return getHollow(mem, 0);
   }

   @Nullable
   public static StairConnectedBlockRuleSet getHollow(MemorySegment mem, int offset) {
      return hasHollow(mem, offset) ? StairConnectedBlockRuleSet.toObject(mem, offset + getValidatedOffset(mem, offset, 13, 21, "Hollow")) : null;
   }

   public static int getTopperBlockId(MemorySegment mem) {
      return getTopperBlockId(mem, 0);
   }

   public static int getTopperBlockId(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   public static int getWidth(MemorySegment mem) {
      return getWidth(mem, 0);
   }

   public static int getWidth(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 5));
   }

   @Nullable
   public static String getMaterialName(MemorySegment mem) {
      return getMaterialName(mem, 0);
   }

   @Nullable
   public static String getMaterialName(MemorySegment mem, int offset) {
      return hasMaterialName(mem, offset)
         ? PacketIO.readVarString("MaterialName", mem, offset + getValidatedOffset(mem, offset, 17, 21, "MaterialName"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static boolean hasRegular(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasHollow(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasMaterialName(MemorySegment mem, int offset) {
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

   public static RoofConnectedBlockRuleSet toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static RoofConnectedBlockRuleSet toObject(MemorySegment mem, int offset) {
      if (offset + 21 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("RoofConnectedBlockRuleSet", offset + 21, (int)mem.byteSize());
      } else {
         return new RoofConnectedBlockRuleSet(
            hasRegular(mem, offset) ? StairConnectedBlockRuleSet.toObject(mem, offset + getValidatedOffset(mem, offset, 9, 21, "Regular")) : null,
            hasHollow(mem, offset) ? StairConnectedBlockRuleSet.toObject(mem, offset + getValidatedOffset(mem, offset, 13, 21, "Hollow")) : null,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 5)),
            hasMaterialName(mem, offset)
               ? PacketIO.readVarString("MaterialName", mem, offset + getValidatedOffset(mem, offset, 17, 21, "MaterialName"), 4096000, PacketIO.UTF8)
               : null
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.regular != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.hollow != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.materialName != null) {
         nullBits = (byte)(nullBits | 4);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.topperBlockId);
      buf.writeIntLE(this.width);
      int regularOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int hollowOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int materialNameOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.regular != null) {
         buf.setIntLE(regularOffsetSlot, buf.writerIndex() - varBlockStart);
         this.regular.serialize(buf);
      } else {
         buf.setIntLE(regularOffsetSlot, -1);
      }

      if (this.hollow != null) {
         buf.setIntLE(hollowOffsetSlot, buf.writerIndex() - varBlockStart);
         this.hollow.serialize(buf);
      } else {
         buf.setIntLE(hollowOffsetSlot, -1);
      }

      if (this.materialName != null) {
         buf.setIntLE(materialNameOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.materialName, 4096000);
      } else {
         buf.setIntLE(materialNameOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.regular != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.hollow != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.materialName != null) {
         nullBits = (byte)(nullBits | 4);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.topperBlockId);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 5), this.width);
      int varOffset = offset + 21;
      if (this.regular != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), varOffset - offset - 21);
         varOffset += this.regular.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), -1);
      }

      if (this.hollow != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 13), varOffset - offset - 21);
         varOffset += this.hollow.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 13), -1);
      }

      if (this.materialName != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 17), varOffset - offset - 21);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.materialName, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 17), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 21;
      if (this.regular != null) {
         size += this.regular.computeSize();
      }

      if (this.hollow != null) {
         size += this.hollow.computeSize();
      }

      if (this.materialName != null) {
         size += PacketIO.stringSize(this.materialName);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 21) {
         return ValidationResult.error("Buffer too small: expected at least 21 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int regularOffset = buffer.getIntLE(offset + 9);
            if (regularOffset < 0 || regularOffset > buffer.writerIndex() - offset - 21) {
               return ValidationResult.error("Invalid offset for Regular");
            }

            int pos = offset + 21 + regularOffset;
            ValidationResult regularResult = StairConnectedBlockRuleSet.validateStructure(buffer, pos);
            if (!regularResult.isValid()) {
               return ValidationResult.error("Invalid Regular: " + regularResult.error());
            }

            pos += StairConnectedBlockRuleSet.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits & 2) != 0) {
            int hollowOffset = buffer.getIntLE(offset + 13);
            if (hollowOffset < 0 || hollowOffset > buffer.writerIndex() - offset - 21) {
               return ValidationResult.error("Invalid offset for Hollow");
            }

            int pos = offset + 21 + hollowOffset;
            ValidationResult hollowResult = StairConnectedBlockRuleSet.validateStructure(buffer, pos);
            if (!hollowResult.isValid()) {
               return ValidationResult.error("Invalid Hollow: " + hollowResult.error());
            }

            pos += StairConnectedBlockRuleSet.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits & 4) != 0) {
            int materialNameOffset = buffer.getIntLE(offset + 17);
            if (materialNameOffset < 0 || materialNameOffset > buffer.writerIndex() - offset - 21) {
               return ValidationResult.error("Invalid offset for MaterialName");
            }

            int pos = offset + 21 + materialNameOffset;
            int materialNameLen = VarInt.peek(buffer, pos);
            if (materialNameLen < 0) {
               return ValidationResult.error("Invalid string length for MaterialName");
            }

            if (materialNameLen > 4096000) {
               return ValidationResult.error("MaterialName exceeds max length 4096000");
            }

            pos += VarInt.size(materialNameLen);
            pos += materialNameLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading MaterialName");
            }
         }

         return ValidationResult.OK;
      }
   }

   public RoofConnectedBlockRuleSet clone() {
      RoofConnectedBlockRuleSet copy = new RoofConnectedBlockRuleSet();
      copy.regular = this.regular != null ? this.regular.clone() : null;
      copy.hollow = this.hollow != null ? this.hollow.clone() : null;
      copy.topperBlockId = this.topperBlockId;
      copy.width = this.width;
      copy.materialName = this.materialName;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof RoofConnectedBlockRuleSet other)
            ? false
            : Objects.equals(this.regular, other.regular)
               && Objects.equals(this.hollow, other.hollow)
               && this.topperBlockId == other.topperBlockId
               && this.width == other.width
               && Objects.equals(this.materialName, other.materialName);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.regular, this.hollow, this.topperBlockId, this.width, this.materialName);
   }
}
