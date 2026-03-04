package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
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
      OffsetNoise obj = new OffsetNoise();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 13 + buf.getIntLE(offset + 1);
         int xCount = VarInt.peek(buf, varPos0);
         if (xCount < 0) {
            throw ProtocolException.negativeLength("X", xCount);
         }

         if (xCount > 4096000) {
            throw ProtocolException.arrayTooLong("X", xCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos0);
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
         int varPos1 = offset + 13 + buf.getIntLE(offset + 5);
         int yCount = VarInt.peek(buf, varPos1);
         if (yCount < 0) {
            throw ProtocolException.negativeLength("Y", yCount);
         }

         if (yCount > 4096000) {
            throw ProtocolException.arrayTooLong("Y", yCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos1);
         if (varPos1 + varIntLen + yCount * 23L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Y", varPos1 + varIntLen + yCount * 23, buf.readableBytes());
         }

         obj.y = new NoiseConfig[yCount];
         int elemPos = varPos1 + varIntLen;

         for (int i = 0; i < yCount; i++) {
            obj.y[i] = NoiseConfig.deserialize(buf, elemPos);
            elemPos += NoiseConfig.computeBytesConsumed(buf, elemPos);
         }
      }

      if ((nullBits & 4) != 0) {
         int varPos2 = offset + 13 + buf.getIntLE(offset + 9);
         int zCount = VarInt.peek(buf, varPos2);
         if (zCount < 0) {
            throw ProtocolException.negativeLength("Z", zCount);
         }

         if (zCount > 4096000) {
            throw ProtocolException.arrayTooLong("Z", zCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos2);
         if (varPos2 + varIntLen + zCount * 23L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Z", varPos2 + varIntLen + zCount * 23, buf.readableBytes());
         }

         obj.z = new NoiseConfig[zCount];
         int elemPos = varPos2 + varIntLen;

         for (int i = 0; i < zCount; i++) {
            obj.z[i] = NoiseConfig.deserialize(buf, elemPos);
            elemPos += NoiseConfig.computeBytesConsumed(buf, elemPos);
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 13;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         int pos0 = offset + 13 + fieldOffset0;
         int arrLen = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0);

         for (int i = 0; i < arrLen; i++) {
            pos0 += NoiseConfig.computeBytesConsumed(buf, pos0);
         }

         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         int pos1 = offset + 13 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1);

         for (int i = 0; i < arrLen; i++) {
            pos1 += NoiseConfig.computeBytesConsumed(buf, pos1);
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 9);
         int pos2 = offset + 13 + fieldOffset2;
         int arrLen = VarInt.peek(buf, pos2);
         pos2 += VarInt.length(buf, pos2);

         for (int i = 0; i < arrLen; i++) {
            pos2 += NoiseConfig.computeBytesConsumed(buf, pos2);
         }

         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      return maxEnd;
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
            if (xOffset < 0) {
               return ValidationResult.error("Invalid offset for X");
            }

            int pos = offset + 13 + xOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for X");
            }

            int xCount = VarInt.peek(buffer, pos);
            if (xCount < 0) {
               return ValidationResult.error("Invalid array count for X");
            }

            if (xCount > 4096000) {
               return ValidationResult.error("X exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += xCount * 23;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading X");
            }
         }

         if ((nullBits & 2) != 0) {
            int yOffset = buffer.getIntLE(offset + 5);
            if (yOffset < 0) {
               return ValidationResult.error("Invalid offset for Y");
            }

            int posx = offset + 13 + yOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Y");
            }

            int yCount = VarInt.peek(buffer, posx);
            if (yCount < 0) {
               return ValidationResult.error("Invalid array count for Y");
            }

            if (yCount > 4096000) {
               return ValidationResult.error("Y exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
            posx += yCount * 23;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Y");
            }
         }

         if ((nullBits & 4) != 0) {
            int zOffset = buffer.getIntLE(offset + 9);
            if (zOffset < 0) {
               return ValidationResult.error("Invalid offset for Z");
            }

            int posxx = offset + 13 + zOffset;
            if (posxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Z");
            }

            int zCount = VarInt.peek(buffer, posxx);
            if (zCount < 0) {
               return ValidationResult.error("Invalid array count for Z");
            }

            if (zCount > 4096000) {
               return ValidationResult.error("Z exceeds max length 4096000");
            }

            posxx += VarInt.length(buffer, posxx);
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
