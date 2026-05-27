package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class OffsetNoise {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 3;
   public static final int VARIABLE_BLOCK_START = 13;
   public static final int MAX_SIZE = 282624028;
   @Nullable
   public NoiseConfig[] x;
   @Nullable
   public NoiseConfig[] y;
   @Nullable
   public NoiseConfig[] z;

   public OffsetNoise() {
   }

   public OffsetNoise(@Nullable NoiseConfig[] x, @Nullable NoiseConfig[] y, @Nullable NoiseConfig[] z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public OffsetNoise(@Nonnull OffsetNoise other) {
      this.x = other.x;
      this.y = other.y;
      this.z = other.z;
   }

   @Nonnull
   public static OffsetNoise deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 13) {
         throw ProtocolException.bufferTooSmall("OffsetNoise", 13, buf.readableBytes() - offset);
      } else {
         OffsetNoise obj = new OffsetNoise();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 1);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 13) {
               throw ProtocolException.invalidOffset("X", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 13 + varPosBase0;
            int xCount = VarInt.peek(buf, varPos0);
            if (xCount < 0) {
               throw ProtocolException.invalidVarInt("X");
            }

            int varIntLen = VarInt.size(xCount);
            if (xCount > 4096000) {
               throw ProtocolException.arrayTooLong("X", xCount, 4096000);
            }

            if (varPos0 + varIntLen + xCount * 23L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("X", varPos0 + varIntLen + xCount * 23, buf.readableBytes());
            }

            obj.x = new NoiseConfig[xCount];
            int elemPos = varPos0 + varIntLen;

            for (int i = 0; i < xCount; i++) {
               obj.x[i] = NoiseConfig.deserialize(buf, elemPos);
               elemPos += NoiseConfig.computeBytesConsumed(buf, elemPos);
            }
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 5);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 13) {
               throw ProtocolException.invalidOffset("Y", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 13 + varPosBase1;
            int yCount = VarInt.peek(buf, varPos1);
            if (yCount < 0) {
               throw ProtocolException.invalidVarInt("Y");
            }

            int varIntLenx = VarInt.size(yCount);
            if (yCount > 4096000) {
               throw ProtocolException.arrayTooLong("Y", yCount, 4096000);
            }

            if (varPos1 + varIntLenx + yCount * 23L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Y", varPos1 + varIntLenx + yCount * 23, buf.readableBytes());
            }

            obj.y = new NoiseConfig[yCount];
            int elemPos = varPos1 + varIntLenx;

            for (int i = 0; i < yCount; i++) {
               obj.y[i] = NoiseConfig.deserialize(buf, elemPos);
               elemPos += NoiseConfig.computeBytesConsumed(buf, elemPos);
            }
         }

         if ((nullBits & 4) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 9);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 13) {
               throw ProtocolException.invalidOffset("Z", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 13 + varPosBase2;
            int zCount = VarInt.peek(buf, varPos2);
            if (zCount < 0) {
               throw ProtocolException.invalidVarInt("Z");
            }

            int varIntLenxx = VarInt.size(zCount);
            if (zCount > 4096000) {
               throw ProtocolException.arrayTooLong("Z", zCount, 4096000);
            }

            if (varPos2 + varIntLenxx + zCount * 23L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Z", varPos2 + varIntLenxx + zCount * 23, buf.readableBytes());
            }

            obj.z = new NoiseConfig[zCount];
            int elemPos = varPos2 + varIntLenxx;

            for (int i = 0; i < zCount; i++) {
               obj.z[i] = NoiseConfig.deserialize(buf, elemPos);
               elemPos += NoiseConfig.computeBytesConsumed(buf, elemPos);
            }
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
            throw ProtocolException.invalidOffset("X", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 13 + fieldOffset0;
         int arrLen = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos0 += NoiseConfig.computeBytesConsumed(buf, pos0);
         }

         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 13) {
            throw ProtocolException.invalidOffset("Y", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 13 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos1 += NoiseConfig.computeBytesConsumed(buf, pos1);
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 9);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 13) {
            throw ProtocolException.invalidOffset("Z", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 13 + fieldOffset2;
         int arrLen = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos2 += NoiseConfig.computeBytesConsumed(buf, pos2);
         }

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
   public static NoiseConfig[] getX(MemorySegment mem) {
      return getX(mem, 0);
   }

   @Nullable
   public static NoiseConfig[] getX(MemorySegment mem, int offset) {
      if (!hasX(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 1, 13, "X");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("X", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("X", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 23L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("X", off + lenOffset + len * 23, (int)mem.byteSize());
            } else {
               off += lenOffset;
               NoiseConfig[] data = new NoiseConfig[len];

               for (int i = 0; i < len; i++) {
                  data[i] = NoiseConfig.toObject(mem, off + i * 23);
               }

               return data;
            }
         }
      }
   }

   @Nullable
   public static NoiseConfig[] getY(MemorySegment mem) {
      return getY(mem, 0);
   }

   @Nullable
   public static NoiseConfig[] getY(MemorySegment mem, int offset) {
      if (!hasY(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 5, 13, "Y");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Y", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Y", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 23L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Y", off + lenOffset + len * 23, (int)mem.byteSize());
            } else {
               off += lenOffset;
               NoiseConfig[] data = new NoiseConfig[len];

               for (int i = 0; i < len; i++) {
                  data[i] = NoiseConfig.toObject(mem, off + i * 23);
               }

               return data;
            }
         }
      }
   }

   @Nullable
   public static NoiseConfig[] getZ(MemorySegment mem) {
      return getZ(mem, 0);
   }

   @Nullable
   public static NoiseConfig[] getZ(MemorySegment mem, int offset) {
      if (!hasZ(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 9, 13, "Z");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Z", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Z", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 23L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Z", off + lenOffset + len * 23, (int)mem.byteSize());
            } else {
               off += lenOffset;
               NoiseConfig[] data = new NoiseConfig[len];

               for (int i = 0; i < len; i++) {
                  data[i] = NoiseConfig.toObject(mem, off + i * 23);
               }

               return data;
            }
         }
      }
   }

   public static boolean hasX(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasY(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasZ(MemorySegment mem, int offset) {
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

   public static OffsetNoise toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static OffsetNoise toObject(MemorySegment mem, int offset) {
      if (offset + 13 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("OffsetNoise", offset + 13, (int)mem.byteSize());
      } else {
         NoiseConfig[] x = null;
         if (hasX(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 1, 13, "X");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("X", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("X", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 23L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("X", off + lenOffset + len * 23, (int)mem.byteSize());
            }

            off += lenOffset;
            x = new NoiseConfig[len];

            for (int i = 0; i < len; i++) {
               x[i] = NoiseConfig.toObject(mem, off + i * 23);
            }
         }

         NoiseConfig[] y = null;
         if (hasY(mem, offset)) {
            int offx = offset + getValidatedOffset(mem, offset, 5, 13, "Y");
            long packedx = VarInt.getWithLength(mem, offx);
            int lenx = (int)packedx;
            if (lenx < 0) {
               throw ProtocolException.negativeLength("Y", lenx);
            }

            if (lenx > 4096000) {
               throw ProtocolException.arrayTooLong("Y", lenx, 4096000);
            }

            int lenOffset = (int)(packedx >>> 32);
            if (offx + lenOffset + lenx * 23L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Y", offx + lenOffset + lenx * 23, (int)mem.byteSize());
            }

            offx += lenOffset;
            y = new NoiseConfig[lenx];

            for (int i = 0; i < lenx; i++) {
               y[i] = NoiseConfig.toObject(mem, offx + i * 23);
            }
         }

         NoiseConfig[] z = null;
         if (hasZ(mem, offset)) {
            int offxx = offset + getValidatedOffset(mem, offset, 9, 13, "Z");
            long packedxx = VarInt.getWithLength(mem, offxx);
            int lenxx = (int)packedxx;
            if (lenxx < 0) {
               throw ProtocolException.negativeLength("Z", lenxx);
            }

            if (lenxx > 4096000) {
               throw ProtocolException.arrayTooLong("Z", lenxx, 4096000);
            }

            int lenOffset = (int)(packedxx >>> 32);
            if (offxx + lenOffset + lenxx * 23L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Z", offxx + lenOffset + lenxx * 23, (int)mem.byteSize());
            }

            offxx += lenOffset;
            z = new NoiseConfig[lenxx];

            for (int i = 0; i < lenxx; i++) {
               z[i] = NoiseConfig.toObject(mem, offxx + i * 23);
            }
         }

         return new OffsetNoise(x, y, z);
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.x != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.y != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.z != null) {
         nullBits = (byte)(nullBits | 4);
      }

      buf.writeByte(nullBits);
      int xOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int yOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int zOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.x != null) {
         buf.setIntLE(xOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.x.length > 4096000) {
            throw ProtocolException.arrayTooLong("X", this.x.length, 4096000);
         }

         VarInt.write(buf, this.x.length);

         for (NoiseConfig item : this.x) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(xOffsetSlot, -1);
      }

      if (this.y != null) {
         buf.setIntLE(yOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.y.length > 4096000) {
            throw ProtocolException.arrayTooLong("Y", this.y.length, 4096000);
         }

         VarInt.write(buf, this.y.length);

         for (NoiseConfig item : this.y) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(yOffsetSlot, -1);
      }

      if (this.z != null) {
         buf.setIntLE(zOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.z.length > 4096000) {
            throw ProtocolException.arrayTooLong("Z", this.z.length, 4096000);
         }

         VarInt.write(buf, this.z.length);

         for (NoiseConfig item : this.z) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(zOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.x != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.y != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.z != null) {
         nullBits = (byte)(nullBits | 4);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 13;
      if (this.x != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), varOffset - offset - 13);
         if (this.x.length > 4096000) {
            throw ProtocolException.arrayTooLong("X", this.x.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.x.length);
         int xValueOffset = 0;

         for (int i = 0; i < this.x.length; i++) {
            xValueOffset += this.x[i].serialize(mem, varOffset + xValueOffset);
         }

         varOffset += xValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), -1);
      }

      if (this.y != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), varOffset - offset - 13);
         if (this.y.length > 4096000) {
            throw ProtocolException.arrayTooLong("Y", this.y.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.y.length);
         int yValueOffset = 0;

         for (int i = 0; i < this.y.length; i++) {
            yValueOffset += this.y[i].serialize(mem, varOffset + yValueOffset);
         }

         varOffset += yValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), -1);
      }

      if (this.z != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), varOffset - offset - 13);
         if (this.z.length > 4096000) {
            throw ProtocolException.arrayTooLong("Z", this.z.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.z.length);
         int zValueOffset = 0;

         for (int i = 0; i < this.z.length; i++) {
            zValueOffset += this.z[i].serialize(mem, varOffset + zValueOffset);
         }

         varOffset += zValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 13;
      if (this.x != null) {
         size += VarInt.size(this.x.length) + this.x.length * 23;
      }

      if (this.y != null) {
         size += VarInt.size(this.y.length) + this.y.length * 23;
      }

      if (this.z != null) {
         size += VarInt.size(this.z.length) + this.z.length * 23;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 13) {
         return ValidationResult.error("Buffer too small: expected at least 13 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int xOffset = buffer.getIntLE(offset + 1);
            if (xOffset < 0 || xOffset > buffer.writerIndex() - offset - 13) {
               return ValidationResult.error("Invalid offset for X");
            }

            int pos = offset + 13 + xOffset;
            int xCount = VarInt.peek(buffer, pos);
            if (xCount < 0) {
               return ValidationResult.error("Invalid array count for X");
            }

            if (xCount > 4096000) {
               return ValidationResult.error("X exceeds max length 4096000");
            }

            pos += VarInt.size(xCount);
            pos += xCount * 23;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading X");
            }
         }

         if ((nullBits & 2) != 0) {
            int yOffset = buffer.getIntLE(offset + 5);
            if (yOffset < 0 || yOffset > buffer.writerIndex() - offset - 13) {
               return ValidationResult.error("Invalid offset for Y");
            }

            int posx = offset + 13 + yOffset;
            int yCount = VarInt.peek(buffer, posx);
            if (yCount < 0) {
               return ValidationResult.error("Invalid array count for Y");
            }

            if (yCount > 4096000) {
               return ValidationResult.error("Y exceeds max length 4096000");
            }

            posx += VarInt.size(yCount);
            posx += yCount * 23;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Y");
            }
         }

         if ((nullBits & 4) != 0) {
            int zOffset = buffer.getIntLE(offset + 9);
            if (zOffset < 0 || zOffset > buffer.writerIndex() - offset - 13) {
               return ValidationResult.error("Invalid offset for Z");
            }

            int posxx = offset + 13 + zOffset;
            int zCount = VarInt.peek(buffer, posxx);
            if (zCount < 0) {
               return ValidationResult.error("Invalid array count for Z");
            }

            if (zCount > 4096000) {
               return ValidationResult.error("Z exceeds max length 4096000");
            }

            posxx += VarInt.size(zCount);
            posxx += zCount * 23;
            if (posxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Z");
            }
         }

         return ValidationResult.OK;
      }
   }

   public OffsetNoise clone() {
      OffsetNoise copy = new OffsetNoise();
      copy.x = this.x != null ? Arrays.stream(this.x).map(e -> e.clone()).toArray(NoiseConfig[]::new) : null;
      copy.y = this.y != null ? Arrays.stream(this.y).map(e -> e.clone()).toArray(NoiseConfig[]::new) : null;
      copy.z = this.z != null ? Arrays.stream(this.z).map(e -> e.clone()).toArray(NoiseConfig[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof OffsetNoise other)
            ? false
            : Arrays.equals((Object[])this.x, (Object[])other.x)
               && Arrays.equals((Object[])this.y, (Object[])other.y)
               && Arrays.equals((Object[])this.z, (Object[])other.z);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Arrays.hashCode((Object[])this.x);
      result = 31 * result + Arrays.hashCode((Object[])this.y);
      return 31 * result + Arrays.hashCode((Object[])this.z);
   }
}
