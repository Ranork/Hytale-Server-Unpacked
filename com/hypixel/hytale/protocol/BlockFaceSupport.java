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

public class BlockFaceSupport {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 65536019;
   @Nullable
   public String faceType;
   @Nullable
   public Vector3i[] filler;

   public BlockFaceSupport() {
   }

   public BlockFaceSupport(@Nullable String faceType, @Nullable Vector3i[] filler) {
      this.faceType = faceType;
      this.filler = filler;
   }

   public BlockFaceSupport(@Nonnull BlockFaceSupport other) {
      this.faceType = other.faceType;
      this.filler = other.filler;
   }

   @Nonnull
   public static BlockFaceSupport deserialize(@Nonnull ByteBuf buf, int offset) {
      BlockFaceSupport obj = new BlockFaceSupport();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 9 + buf.getIntLE(offset + 1);
         int faceTypeLen = VarInt.peek(buf, varPos0);
         if (faceTypeLen < 0) {
            throw ProtocolException.negativeLength("FaceType", faceTypeLen);
         }

         if (faceTypeLen > 4096000) {
            throw ProtocolException.stringTooLong("FaceType", faceTypeLen, 4096000);
         }

         obj.faceType = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 9 + buf.getIntLE(offset + 5);
         int fillerCount = VarInt.peek(buf, varPos1);
         if (fillerCount < 0) {
            throw ProtocolException.negativeLength("Filler", fillerCount);
         }

         if (fillerCount > 4096000) {
            throw ProtocolException.arrayTooLong("Filler", fillerCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos1);
         if (varPos1 + varIntLen + fillerCount * 12L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Filler", varPos1 + varIntLen + fillerCount * 12, buf.readableBytes());
         }

         obj.filler = new Vector3i[fillerCount];
         int elemPos = varPos1 + varIntLen;

         for (int i = 0; i < fillerCount; i++) {
            obj.filler[i] = Vector3i.deserialize(buf, elemPos);
            elemPos += Vector3i.computeBytesConsumed(buf, elemPos);
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
         pos1 += VarInt.length(buf, pos1);

         for (int i = 0; i < arrLen; i++) {
            pos1 += Vector3i.computeBytesConsumed(buf, pos1);
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.faceType != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.filler != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      int faceTypeOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int fillerOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.faceType != null) {
         buf.setIntLE(faceTypeOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.faceType, 4096000);
      } else {
         buf.setIntLE(faceTypeOffsetSlot, -1);
      }

      if (this.filler != null) {
         buf.setIntLE(fillerOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.filler.length > 4096000) {
            throw ProtocolException.arrayTooLong("Filler", this.filler.length, 4096000);
         }

         VarInt.write(buf, this.filler.length);

         for (Vector3i item : this.filler) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(fillerOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 9;
      if (this.faceType != null) {
         size += PacketIO.stringSize(this.faceType);
      }

      if (this.filler != null) {
         size += VarInt.size(this.filler.length) + this.filler.length * 12;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 9) {
         return ValidationResult.error("Buffer too small: expected at least 9 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int faceTypeOffset = buffer.getIntLE(offset + 1);
            if (faceTypeOffset < 0) {
               return ValidationResult.error("Invalid offset for FaceType");
            }

            int pos = offset + 9 + faceTypeOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for FaceType");
            }

            int faceTypeLen = VarInt.peek(buffer, pos);
            if (faceTypeLen < 0) {
               return ValidationResult.error("Invalid string length for FaceType");
            }

            if (faceTypeLen > 4096000) {
               return ValidationResult.error("FaceType exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += faceTypeLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading FaceType");
            }
         }

         if ((nullBits & 2) != 0) {
            int fillerOffset = buffer.getIntLE(offset + 5);
            if (fillerOffset < 0) {
               return ValidationResult.error("Invalid offset for Filler");
            }

            int posx = offset + 9 + fillerOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Filler");
            }

            int fillerCount = VarInt.peek(buffer, posx);
            if (fillerCount < 0) {
               return ValidationResult.error("Invalid array count for Filler");
            }

            if (fillerCount > 4096000) {
               return ValidationResult.error("Filler exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
            posx += fillerCount * 12;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Filler");
            }
         }

         return ValidationResult.OK;
      }
   }

   public BlockFaceSupport clone() {
      BlockFaceSupport copy = new BlockFaceSupport();
      copy.faceType = this.faceType;
      copy.filler = this.filler != null ? Arrays.stream(this.filler).map(e -> e.clone()).toArray(Vector3i[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BlockFaceSupport other)
            ? false
            : Objects.equals(this.faceType, other.faceType) && Arrays.equals((Object[])this.filler, (Object[])other.filler);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.faceType);
      return 31 * result + Arrays.hashCode((Object[])this.filler);
   }
}
