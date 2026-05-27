package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockGathering {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 3;
   public static final int VARIABLE_BLOCK_START = 13;
   public static final int MAX_SIZE = 114688092;
   @Nullable
   public BlockBreaking breaking;
   @Nullable
   public Harvesting harvest;
   @Nullable
   public SoftBlock soft;

   public BlockGathering() {
   }

   public BlockGathering(@Nullable BlockBreaking breaking, @Nullable Harvesting harvest, @Nullable SoftBlock soft) {
      this.breaking = breaking;
      this.harvest = harvest;
      this.soft = soft;
   }

   public BlockGathering(@Nonnull BlockGathering other) {
      this.breaking = other.breaking;
      this.harvest = other.harvest;
      this.soft = other.soft;
   }

   @Nonnull
   public static BlockGathering deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 13) {
         throw ProtocolException.bufferTooSmall("BlockGathering", 13, buf.readableBytes() - offset);
      } else {
         BlockGathering obj = new BlockGathering();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 1);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 13) {
               throw ProtocolException.invalidOffset("Breaking", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 13 + varPosBase0;
            obj.breaking = BlockBreaking.deserialize(buf, varPos0);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 5);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 13) {
               throw ProtocolException.invalidOffset("Harvest", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 13 + varPosBase1;
            obj.harvest = Harvesting.deserialize(buf, varPos1);
         }

         if ((nullBits & 4) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 9);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 13) {
               throw ProtocolException.invalidOffset("Soft", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 13 + varPosBase2;
            obj.soft = SoftBlock.deserialize(buf, varPos2);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 13;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 13) {
            throw ProtocolException.invalidOffset("Breaking", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 13 + fieldOffset0;
         pos0 += BlockBreaking.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 13) {
            throw ProtocolException.invalidOffset("Harvest", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 13 + fieldOffset1;
         pos1 += Harvesting.computeBytesConsumed(buf, pos1);
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 9);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 13) {
            throw ProtocolException.invalidOffset("Soft", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 13 + fieldOffset2;
         pos2 += SoftBlock.computeBytesConsumed(buf, pos2);
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 13L;
   }

   @Nullable
   public static BlockBreaking getBreaking(MemorySegment mem) {
      return getBreaking(mem, 0);
   }

   @Nullable
   public static BlockBreaking getBreaking(MemorySegment mem, int offset) {
      return hasBreaking(mem, offset) ? BlockBreaking.toObject(mem, offset + getValidatedOffset(mem, offset, 1, 13, "Breaking")) : null;
   }

   @Nullable
   public static Harvesting getHarvest(MemorySegment mem) {
      return getHarvest(mem, 0);
   }

   @Nullable
   public static Harvesting getHarvest(MemorySegment mem, int offset) {
      return hasHarvest(mem, offset) ? Harvesting.toObject(mem, offset + getValidatedOffset(mem, offset, 5, 13, "Harvest")) : null;
   }

   @Nullable
   public static SoftBlock getSoft(MemorySegment mem) {
      return getSoft(mem, 0);
   }

   @Nullable
   public static SoftBlock getSoft(MemorySegment mem, int offset) {
      return hasSoft(mem, offset) ? SoftBlock.toObject(mem, offset + getValidatedOffset(mem, offset, 9, 13, "Soft")) : null;
   }

   public static boolean hasBreaking(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasHarvest(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasSoft(MemorySegment mem, int offset) {
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

   public static BlockGathering toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static BlockGathering toObject(MemorySegment mem, int offset) {
      if (offset + 13 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("BlockGathering", offset + 13, (int)mem.byteSize());
      } else {
         return new BlockGathering(
            hasBreaking(mem, offset) ? BlockBreaking.toObject(mem, offset + getValidatedOffset(mem, offset, 1, 13, "Breaking")) : null,
            hasHarvest(mem, offset) ? Harvesting.toObject(mem, offset + getValidatedOffset(mem, offset, 5, 13, "Harvest")) : null,
            hasSoft(mem, offset) ? SoftBlock.toObject(mem, offset + getValidatedOffset(mem, offset, 9, 13, "Soft")) : null
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.breaking != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.harvest != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.soft != null) {
         nullBits = (byte)(nullBits | 4);
      }

      buf.writeByte(nullBits);
      int breakingOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int harvestOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int softOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.breaking != null) {
         buf.setIntLE(breakingOffsetSlot, buf.writerIndex() - varBlockStart);
         this.breaking.serialize(buf);
      } else {
         buf.setIntLE(breakingOffsetSlot, -1);
      }

      if (this.harvest != null) {
         buf.setIntLE(harvestOffsetSlot, buf.writerIndex() - varBlockStart);
         this.harvest.serialize(buf);
      } else {
         buf.setIntLE(harvestOffsetSlot, -1);
      }

      if (this.soft != null) {
         buf.setIntLE(softOffsetSlot, buf.writerIndex() - varBlockStart);
         this.soft.serialize(buf);
      } else {
         buf.setIntLE(softOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.breaking != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.harvest != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.soft != null) {
         nullBits = (byte)(nullBits | 4);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 13;
      if (this.breaking != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), varOffset - offset - 13);
         varOffset += this.breaking.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), -1);
      }

      if (this.harvest != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), varOffset - offset - 13);
         varOffset += this.harvest.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), -1);
      }

      if (this.soft != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), varOffset - offset - 13);
         varOffset += this.soft.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 13;
      if (this.breaking != null) {
         size += this.breaking.computeSize();
      }

      if (this.harvest != null) {
         size += this.harvest.computeSize();
      }

      if (this.soft != null) {
         size += this.soft.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 13) {
         return ValidationResult.error("Buffer too small: expected at least 13 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int breakingOffset = buffer.getIntLE(offset + 1);
            if (breakingOffset < 0 || breakingOffset > buffer.writerIndex() - offset - 13) {
               return ValidationResult.error("Invalid offset for Breaking");
            }

            int pos = offset + 13 + breakingOffset;
            ValidationResult breakingResult = BlockBreaking.validateStructure(buffer, pos);
            if (!breakingResult.isValid()) {
               return ValidationResult.error("Invalid Breaking: " + breakingResult.error());
            }

            pos += BlockBreaking.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits & 2) != 0) {
            int harvestOffset = buffer.getIntLE(offset + 5);
            if (harvestOffset < 0 || harvestOffset > buffer.writerIndex() - offset - 13) {
               return ValidationResult.error("Invalid offset for Harvest");
            }

            int pos = offset + 13 + harvestOffset;
            ValidationResult harvestResult = Harvesting.validateStructure(buffer, pos);
            if (!harvestResult.isValid()) {
               return ValidationResult.error("Invalid Harvest: " + harvestResult.error());
            }

            pos += Harvesting.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits & 4) != 0) {
            int softOffset = buffer.getIntLE(offset + 9);
            if (softOffset < 0 || softOffset > buffer.writerIndex() - offset - 13) {
               return ValidationResult.error("Invalid offset for Soft");
            }

            int pos = offset + 13 + softOffset;
            ValidationResult softResult = SoftBlock.validateStructure(buffer, pos);
            if (!softResult.isValid()) {
               return ValidationResult.error("Invalid Soft: " + softResult.error());
            }

            pos += SoftBlock.computeBytesConsumed(buffer, pos);
         }

         return ValidationResult.OK;
      }
   }

   public BlockGathering clone() {
      BlockGathering copy = new BlockGathering();
      copy.breaking = this.breaking != null ? this.breaking.clone() : null;
      copy.harvest = this.harvest != null ? this.harvest.clone() : null;
      copy.soft = this.soft != null ? this.soft.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BlockGathering other)
            ? false
            : Objects.equals(this.breaking, other.breaking) && Objects.equals(this.harvest, other.harvest) && Objects.equals(this.soft, other.soft);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.breaking, this.harvest, this.soft);
   }
}
