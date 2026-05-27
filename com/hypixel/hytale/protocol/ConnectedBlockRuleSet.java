package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ConnectedBlockRuleSet {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 10;
   public static final int MAX_SIZE = 65536114;
   @Nonnull
   public ConnectedBlockRuleSetType type = ConnectedBlockRuleSetType.Stair;
   @Nullable
   public StairConnectedBlockRuleSet stair;
   @Nullable
   public RoofConnectedBlockRuleSet roof;

   public ConnectedBlockRuleSet() {
   }

   public ConnectedBlockRuleSet(@Nonnull ConnectedBlockRuleSetType type, @Nullable StairConnectedBlockRuleSet stair, @Nullable RoofConnectedBlockRuleSet roof) {
      this.type = type;
      this.stair = stair;
      this.roof = roof;
   }

   public ConnectedBlockRuleSet(@Nonnull ConnectedBlockRuleSet other) {
      this.type = other.type;
      this.stair = other.stair;
      this.roof = other.roof;
   }

   @Nonnull
   public static ConnectedBlockRuleSet deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 10) {
         throw ProtocolException.bufferTooSmall("ConnectedBlockRuleSet", 10, buf.readableBytes() - offset);
      } else {
         ConnectedBlockRuleSet obj = new ConnectedBlockRuleSet();
         byte nullBits = buf.getByte(offset);
         obj.type = ConnectedBlockRuleSetType.fromValue(buf.getByte(offset + 1));
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 2);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 10) {
               throw ProtocolException.invalidOffset("Stair", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 10 + varPosBase0;
            obj.stair = StairConnectedBlockRuleSet.deserialize(buf, varPos0);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 6);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 10) {
               throw ProtocolException.invalidOffset("Roof", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 10 + varPosBase1;
            obj.roof = RoofConnectedBlockRuleSet.deserialize(buf, varPos1);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 10;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 2);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 10) {
            throw ProtocolException.invalidOffset("Stair", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 10 + fieldOffset0;
         pos0 += StairConnectedBlockRuleSet.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 6);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 10) {
            throw ProtocolException.invalidOffset("Roof", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 10 + fieldOffset1;
         pos1 += RoofConnectedBlockRuleSet.computeBytesConsumed(buf, pos1);
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 10L;
   }

   public static ConnectedBlockRuleSetType getType(MemorySegment mem) {
      return getType(mem, 0);
   }

   public static ConnectedBlockRuleSetType getType(MemorySegment mem, int offset) {
      return ConnectedBlockRuleSetType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1)));
   }

   @Nullable
   public static StairConnectedBlockRuleSet getStair(MemorySegment mem) {
      return getStair(mem, 0);
   }

   @Nullable
   public static StairConnectedBlockRuleSet getStair(MemorySegment mem, int offset) {
      return hasStair(mem, offset) ? StairConnectedBlockRuleSet.toObject(mem, offset + getValidatedOffset(mem, offset, 2, 10, "Stair")) : null;
   }

   @Nullable
   public static RoofConnectedBlockRuleSet getRoof(MemorySegment mem) {
      return getRoof(mem, 0);
   }

   @Nullable
   public static RoofConnectedBlockRuleSet getRoof(MemorySegment mem, int offset) {
      return hasRoof(mem, offset) ? RoofConnectedBlockRuleSet.toObject(mem, offset + getValidatedOffset(mem, offset, 6, 10, "Roof")) : null;
   }

   public static boolean hasStair(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasRoof(MemorySegment mem, int offset) {
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

   public static ConnectedBlockRuleSet toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ConnectedBlockRuleSet toObject(MemorySegment mem, int offset) {
      if (offset + 10 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ConnectedBlockRuleSet", offset + 10, (int)mem.byteSize());
      } else {
         return new ConnectedBlockRuleSet(
            ConnectedBlockRuleSetType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))),
            hasStair(mem, offset) ? StairConnectedBlockRuleSet.toObject(mem, offset + getValidatedOffset(mem, offset, 2, 10, "Stair")) : null,
            hasRoof(mem, offset) ? RoofConnectedBlockRuleSet.toObject(mem, offset + getValidatedOffset(mem, offset, 6, 10, "Roof")) : null
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.stair != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.roof != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      int stairOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int roofOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.stair != null) {
         buf.setIntLE(stairOffsetSlot, buf.writerIndex() - varBlockStart);
         this.stair.serialize(buf);
      } else {
         buf.setIntLE(stairOffsetSlot, -1);
      }

      if (this.roof != null) {
         buf.setIntLE(roofOffsetSlot, buf.writerIndex() - varBlockStart);
         this.roof.serialize(buf);
      } else {
         buf.setIntLE(roofOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.stair != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.roof != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.type.getValue());
      int varOffset = offset + 10;
      if (this.stair != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 2), varOffset - offset - 10);
         varOffset += this.stair.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 2), -1);
      }

      if (this.roof != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 6), varOffset - offset - 10);
         varOffset += this.roof.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 6), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 10;
      if (this.stair != null) {
         size += this.stair.computeSize();
      }

      if (this.roof != null) {
         size += this.roof.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 10) {
         return ValidationResult.error("Buffer too small: expected at least 10 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int v = buffer.getByte(offset + 1) & 255;
         if (v >= 2) {
            return ValidationResult.error("Invalid ConnectedBlockRuleSetType value for Type");
         } else {
            if ((nullBits & 1) != 0) {
               v = buffer.getIntLE(offset + 2);
               if (v < 0 || v > buffer.writerIndex() - offset - 10) {
                  return ValidationResult.error("Invalid offset for Stair");
               }

               int pos = offset + 10 + v;
               ValidationResult stairResult = StairConnectedBlockRuleSet.validateStructure(buffer, pos);
               if (!stairResult.isValid()) {
                  return ValidationResult.error("Invalid Stair: " + stairResult.error());
               }

               pos += StairConnectedBlockRuleSet.computeBytesConsumed(buffer, pos);
            }

            if ((nullBits & 2) != 0) {
               v = buffer.getIntLE(offset + 6);
               if (v < 0 || v > buffer.writerIndex() - offset - 10) {
                  return ValidationResult.error("Invalid offset for Roof");
               }

               int pos = offset + 10 + v;
               ValidationResult roofResult = RoofConnectedBlockRuleSet.validateStructure(buffer, pos);
               if (!roofResult.isValid()) {
                  return ValidationResult.error("Invalid Roof: " + roofResult.error());
               }

               pos += RoofConnectedBlockRuleSet.computeBytesConsumed(buffer, pos);
            }

            return ValidationResult.OK;
         }
      }
   }

   public ConnectedBlockRuleSet clone() {
      ConnectedBlockRuleSet copy = new ConnectedBlockRuleSet();
      copy.type = this.type;
      copy.stair = this.stair != null ? this.stair.clone() : null;
      copy.roof = this.roof != null ? this.roof.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ConnectedBlockRuleSet other)
            ? false
            : Objects.equals(this.type, other.type) && Objects.equals(this.stair, other.stair) && Objects.equals(this.roof, other.roof);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.stair, this.roof);
   }
}
