package com.hypixel.hytale.protocol.packets.worldmap;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
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
      if (buf.readableBytes() - offset < 18) {
         throw ProtocolException.bufferTooSmall("MapImage", 18, buf.readableBytes() - offset);
      } else {
         MapImage obj = new MapImage();
         byte nullBits = buf.getByte(offset);
         obj.width = buf.getIntLE(offset + 1);
         obj.height = buf.getIntLE(offset + 5);
         obj.bitsPerIndex = buf.getByte(offset + 9);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 10);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 18) {
               throw ProtocolException.invalidOffset("Palette", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 18 + varPosBase0;
            int paletteCount = VarInt.peek(buf, varPos0);
            if (paletteCount < 0) {
               throw ProtocolException.invalidVarInt("Palette");
            }

            int varIntLen = VarInt.size(paletteCount);
            if (paletteCount > 4096000) {
               throw ProtocolException.arrayTooLong("Palette", paletteCount, 4096000);
            }

            if (varPos0 + varIntLen + paletteCount * 4L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Palette", varPos0 + varIntLen + paletteCount * 4, buf.readableBytes());
            }

            obj.palette = new int[paletteCount];

            for (int i = 0; i < paletteCount; i++) {
               obj.palette[i] = buf.getIntLE(varPos0 + varIntLen + i * 4);
            }
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 14);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 18) {
               throw ProtocolException.invalidOffset("PackedIndices", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 18 + varPosBase1;
            int packedIndicesCount = VarInt.peek(buf, varPos1);
            if (packedIndicesCount < 0) {
               throw ProtocolException.invalidVarInt("PackedIndices");
            }

            int varIntLenx = VarInt.size(packedIndicesCount);
            if (packedIndicesCount > 4096000) {
               throw ProtocolException.arrayTooLong("PackedIndices", packedIndicesCount, 4096000);
            }

            if (varPos1 + varIntLenx + packedIndicesCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("PackedIndices", varPos1 + varIntLenx + packedIndicesCount * 1, buf.readableBytes());
            }

            obj.packedIndices = new byte[packedIndicesCount];

            for (int i = 0; i < packedIndicesCount; i++) {
               obj.packedIndices[i] = buf.getByte(varPos1 + varIntLenx + i * 1);
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 18;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 10);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 18) {
            throw ProtocolException.invalidOffset("Palette", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 18 + fieldOffset0;
         int arrLen = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(arrLen) + arrLen * 4;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 14);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 18) {
            throw ProtocolException.invalidOffset("PackedIndices", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 18 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(arrLen) + arrLen * 1;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 18L;
   }

   public static int getWidth(MemorySegment mem) {
      return getWidth(mem, 0);
   }

   public static int getWidth(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   public static int getHeight(MemorySegment mem) {
      return getHeight(mem, 0);
   }

   public static int getHeight(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 5));
   }

   @Nullable
   public static int[] getPalette(MemorySegment mem) {
      return getPalette(mem, 0);
   }

   @Nullable
   public static int[] getPalette(MemorySegment mem, int offset) {
      if (!hasPalette(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 10, 18, "Palette");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Palette", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Palette", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 4L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Palette", off + lenOffset + len * 4, (int)mem.byteSize());
            } else {
               off += lenOffset;
               int[] data = new int[len];
               MemorySegment.copy(mem, PacketIO.PROTO_INT, off, data, 0, len);
               return data;
            }
         }
      }
   }

   public static byte getBitsPerIndex(MemorySegment mem) {
      return getBitsPerIndex(mem, 0);
   }

   public static byte getBitsPerIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BYTE, (long)(offset + 9));
   }

   @Nullable
   public static byte[] getPackedIndices(MemorySegment mem) {
      return getPackedIndices(mem, 0);
   }

   @Nullable
   public static byte[] getPackedIndices(MemorySegment mem, int offset) {
      if (!hasPackedIndices(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 14, 18, "PackedIndices");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("PackedIndices", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("PackedIndices", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("PackedIndices", off + lenOffset + len * 1, (int)mem.byteSize());
            } else {
               off += lenOffset;
               byte[] data = new byte[len];
               MemorySegment.copy(mem, PacketIO.PROTO_BYTE, off, data, 0, len);
               return data;
            }
         }
      }
   }

   public static boolean hasPalette(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasPackedIndices(MemorySegment mem, int offset) {
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

   public static MapImage toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static MapImage toObject(MemorySegment mem, int offset) {
      if (offset + 18 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("MapImage", offset + 18, (int)mem.byteSize());
      } else {
         int[] palette = null;
         if (hasPalette(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 10, 18, "Palette");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Palette", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Palette", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 4L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Palette", off + lenOffset + len * 4, (int)mem.byteSize());
            }

            off += lenOffset;
            palette = new int[len];
            MemorySegment.copy(mem, PacketIO.PROTO_INT, off, palette, 0, len);
         }

         byte[] packedIndices = null;
         if (hasPackedIndices(mem, offset)) {
            int offx = offset + getValidatedOffset(mem, offset, 14, 18, "PackedIndices");
            long packedx = VarInt.getWithLength(mem, offx);
            int lenx = (int)packedx;
            if (lenx < 0) {
               throw ProtocolException.negativeLength("PackedIndices", lenx);
            }

            if (lenx > 4096000) {
               throw ProtocolException.arrayTooLong("PackedIndices", lenx, 4096000);
            }

            int lenOffset = (int)(packedx >>> 32);
            if (offx + lenOffset + lenx * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("PackedIndices", offx + lenOffset + lenx * 1, (int)mem.byteSize());
            }

            offx += lenOffset;
            packedIndices = new byte[lenx];
            MemorySegment.copy(mem, PacketIO.PROTO_BYTE, offx, packedIndices, 0, lenx);
         }

         return new MapImage(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 5)),
            palette,
            mem.get(PacketIO.PROTO_BYTE, (long)(offset + 9)),
            packedIndices
         );
      }
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

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.palette != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.packedIndices != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.width);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 5), this.height);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 9), this.bitsPerIndex);
      int varOffset = offset + 18;
      if (this.palette != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 10), varOffset - offset - 18);
         if (this.palette.length > 4096000) {
            throw ProtocolException.arrayTooLong("Palette", this.palette.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.palette.length);
         MemorySegment.copy(this.palette, 0, mem, PacketIO.PROTO_INT, varOffset, this.palette.length);
         varOffset += this.palette.length * 4;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 10), -1);
      }

      if (this.packedIndices != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 14), varOffset - offset - 18);
         if (this.packedIndices.length > 4096000) {
            throw ProtocolException.arrayTooLong("PackedIndices", this.packedIndices.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.packedIndices.length);
         MemorySegment.copy(this.packedIndices, 0, mem, PacketIO.PROTO_BYTE, varOffset, this.packedIndices.length);
         varOffset += this.packedIndices.length * 1;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 14), -1);
      }

      return varOffset - offset;
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
            if (paletteOffset < 0 || paletteOffset > buffer.writerIndex() - offset - 18) {
               return ValidationResult.error("Invalid offset for Palette");
            }

            int pos = offset + 18 + paletteOffset;
            int paletteCount = VarInt.peek(buffer, pos);
            if (paletteCount < 0) {
               return ValidationResult.error("Invalid array count for Palette");
            }

            if (paletteCount > 4096000) {
               return ValidationResult.error("Palette exceeds max length 4096000");
            }

            pos += VarInt.size(paletteCount);
            pos += paletteCount * 4;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Palette");
            }
         }

         if ((nullBits & 2) != 0) {
            int packedIndicesOffset = buffer.getIntLE(offset + 14);
            if (packedIndicesOffset < 0 || packedIndicesOffset > buffer.writerIndex() - offset - 18) {
               return ValidationResult.error("Invalid offset for PackedIndices");
            }

            int posx = offset + 18 + packedIndicesOffset;
            int packedIndicesCount = VarInt.peek(buffer, posx);
            if (packedIndicesCount < 0) {
               return ValidationResult.error("Invalid array count for PackedIndices");
            }

            if (packedIndicesCount > 4096000) {
               return ValidationResult.error("PackedIndices exceeds max length 4096000");
            }

            posx += VarInt.size(packedIndicesCount);
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
