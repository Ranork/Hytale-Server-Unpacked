package com.hypixel.hytale.protocol.packets.worldmap;

import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MapImage {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 10;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 18;
   public static final int MAX_SIZE = 20480028;
   public int width;
   public int height;
   @Nullable
   public int[] palette;
   public byte bitsPerIndex;
   @Nullable
   public byte[] packedIndices;

   public MapImage() {
   }

   public MapImage(int width, int height, @Nullable int[] palette, byte bitsPerIndex, @Nullable byte[] packedIndices) {
      this.width = width;
      this.height = height;
      this.palette = palette;
      this.bitsPerIndex = bitsPerIndex;
      this.packedIndices = packedIndices;
   }

   public MapImage(@Nonnull MapImage other) {
      this.width = other.width;
      this.height = other.height;
      this.palette = other.palette;
      this.bitsPerIndex = other.bitsPerIndex;
      this.packedIndices = other.packedIndices;
   }

   @Nonnull
   public static MapImage deserialize(@Nonnull ByteBuf buf, int offset) {
      MapImage obj = new MapImage();
      byte nullBits = buf.getByte(offset);
      obj.width = buf.getIntLE(offset + 1);
      obj.height = buf.getIntLE(offset + 5);
      obj.bitsPerIndex = buf.getByte(offset + 9);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 18 + buf.getIntLE(offset + 10);
         int paletteCount = VarInt.peek(buf, varPos0);
         if (paletteCount < 0) {
            throw ProtocolException.negativeLength("Palette", paletteCount);
         }

         if (paletteCount > 4096000) {
            throw ProtocolException.arrayTooLong("Palette", paletteCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos0);
         if (varPos0 + varIntLen + paletteCount * 4L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Palette", varPos0 + varIntLen + paletteCount * 4, buf.readableBytes());
         }

         obj.palette = new int[paletteCount];

         for (int i = 0; i < paletteCount; i++) {
            obj.palette[i] = buf.getIntLE(varPos0 + varIntLen + i * 4);
         }
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 18 + buf.getIntLE(offset + 14);
         int packedIndicesCount = VarInt.peek(buf, varPos1);
         if (packedIndicesCount < 0) {
            throw ProtocolException.negativeLength("PackedIndices", packedIndicesCount);
         }

         if (packedIndicesCount > 4096000) {
            throw ProtocolException.arrayTooLong("PackedIndices", packedIndicesCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos1);
         if (varPos1 + varIntLen + packedIndicesCount * 1L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("PackedIndices", varPos1 + varIntLen + packedIndicesCount * 1, buf.readableBytes());
         }

         obj.packedIndices = new byte[packedIndicesCount];

         for (int i = 0; i < packedIndicesCount; i++) {
            obj.packedIndices[i] = buf.getByte(varPos1 + varIntLen + i * 1);
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 18;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 10);
         int pos0 = offset + 18 + fieldOffset0;
         int arrLen = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + arrLen * 4;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 14);
         int pos1 = offset + 18 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1) + arrLen * 1;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.palette != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.packedIndices != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.width);
      buf.writeIntLE(this.height);
      buf.writeByte(this.bitsPerIndex);
      int paletteOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int packedIndicesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.palette != null) {
         buf.setIntLE(paletteOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.palette.length > 4096000) {
            throw ProtocolException.arrayTooLong("Palette", this.palette.length, 4096000);
         }

         VarInt.write(buf, this.palette.length);

         for (int item : this.palette) {
            buf.writeIntLE(item);
         }
      } else {
         buf.setIntLE(paletteOffsetSlot, -1);
      }

      if (this.packedIndices != null) {
         buf.setIntLE(packedIndicesOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.packedIndices.length > 4096000) {
            throw ProtocolException.arrayTooLong("PackedIndices", this.packedIndices.length, 4096000);
         }

         VarInt.write(buf, this.packedIndices.length);

         for (byte item : this.packedIndices) {
            buf.writeByte(item);
         }
      } else {
         buf.setIntLE(packedIndicesOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 18;
      if (this.palette != null) {
         size += VarInt.size(this.palette.length) + this.palette.length * 4;
      }

      if (this.packedIndices != null) {
         size += VarInt.size(this.packedIndices.length) + this.packedIndices.length * 1;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 18) {
         return ValidationResult.error("Buffer too small: expected at least 18 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int paletteOffset = buffer.getIntLE(offset + 10);
            if (paletteOffset < 0) {
               return ValidationResult.error("Invalid offset for Palette");
            }

            int pos = offset + 18 + paletteOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Palette");
            }

            int paletteCount = VarInt.peek(buffer, pos);
            if (paletteCount < 0) {
               return ValidationResult.error("Invalid array count for Palette");
            }

            if (paletteCount > 4096000) {
               return ValidationResult.error("Palette exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += paletteCount * 4;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Palette");
            }
         }

         if ((nullBits & 2) != 0) {
            int packedIndicesOffset = buffer.getIntLE(offset + 14);
            if (packedIndicesOffset < 0) {
               return ValidationResult.error("Invalid offset for PackedIndices");
            }

            int posx = offset + 18 + packedIndicesOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for PackedIndices");
            }

            int packedIndicesCount = VarInt.peek(buffer, posx);
            if (packedIndicesCount < 0) {
               return ValidationResult.error("Invalid array count for PackedIndices");
            }

            if (packedIndicesCount > 4096000) {
               return ValidationResult.error("PackedIndices exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
            posx += packedIndicesCount * 1;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading PackedIndices");
            }
         }

         return ValidationResult.OK;
      }
   }

   public MapImage clone() {
      MapImage copy = new MapImage();
      copy.width = this.width;
      copy.height = this.height;
      copy.palette = this.palette != null ? Arrays.copyOf(this.palette, this.palette.length) : null;
      copy.bitsPerIndex = this.bitsPerIndex;
      copy.packedIndices = this.packedIndices != null ? Arrays.copyOf(this.packedIndices, this.packedIndices.length) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof MapImage other)
            ? false
            : this.width == other.width
               && this.height == other.height
               && Arrays.equals(this.palette, other.palette)
               && this.bitsPerIndex == other.bitsPerIndex
               && Arrays.equals(this.packedIndices, other.packedIndices);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Integer.hashCode(this.width);
      result = 31 * result + Integer.hashCode(this.height);
      result = 31 * result + Arrays.hashCode(this.palette);
      result = 31 * result + Byte.hashCode(this.bitsPerIndex);
      return 31 * result + Arrays.hashCode(this.packedIndices);
   }
}
