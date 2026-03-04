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
   public static final int FIXED_BLOCK_SIZE = 9;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 16384014;
   public int width;
   public int height;
   @Nullable
   public int[] data;

   public MapImage() {
   }

   public MapImage(int width, int height, @Nullable int[] data) {
      this.width = width;
      this.height = height;
      this.data = data;
   }

   public MapImage(@Nonnull MapImage other) {
      this.width = other.width;
      this.height = other.height;
      this.data = other.data;
   }

   @Nonnull
   public static MapImage deserialize(@Nonnull ByteBuf buf, int offset) {
      MapImage obj = new MapImage();
      byte nullBits = buf.getByte(offset);
      obj.width = buf.getIntLE(offset + 1);
      obj.height = buf.getIntLE(offset + 5);
      int pos = offset + 9;
      if ((nullBits & 1) != 0) {
         int dataCount = VarInt.peek(buf, pos);
         if (dataCount < 0) {
            throw ProtocolException.negativeLength("Data", dataCount);
         }

         if (dataCount > 4096000) {
            throw ProtocolException.arrayTooLong("Data", dataCount, 4096000);
         }

         int dataVarLen = VarInt.size(dataCount);
         if (pos + dataVarLen + dataCount * 4L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Data", pos + dataVarLen + dataCount * 4, buf.readableBytes());
         }

         pos += dataVarLen;
         obj.data = new int[dataCount];

         for (int i = 0; i < dataCount; i++) {
            obj.data[i] = buf.getIntLE(pos + i * 4);
         }

         pos += dataCount * 4;
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 9;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos) + arrLen * 4;
      }

      return pos - offset;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.data != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.width);
      buf.writeIntLE(this.height);
      if (this.data != null) {
         if (this.data.length > 4096000) {
            throw ProtocolException.arrayTooLong("Data", this.data.length, 4096000);
         }

         VarInt.write(buf, this.data.length);

         for (int item : this.data) {
            buf.writeIntLE(item);
         }
      }
   }

   public int computeSize() {
      int size = 9;
      if (this.data != null) {
         size += VarInt.size(this.data.length) + this.data.length * 4;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 9) {
         return ValidationResult.error("Buffer too small: expected at least 9 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 9;
         if ((nullBits & 1) != 0) {
            int dataCount = VarInt.peek(buffer, pos);
            if (dataCount < 0) {
               return ValidationResult.error("Invalid array count for Data");
            }

            if (dataCount > 4096000) {
               return ValidationResult.error("Data exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += dataCount * 4;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Data");
            }
         }

         return ValidationResult.OK;
      }
   }

   public MapImage clone() {
      MapImage copy = new MapImage();
      copy.width = this.width;
      copy.height = this.height;
      copy.data = this.data != null ? Arrays.copyOf(this.data, this.data.length) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof MapImage other) ? false : this.width == other.width && this.height == other.height && Arrays.equals(this.data, other.data);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Integer.hashCode(this.width);
      result = 31 * result + Integer.hashCode(this.height);
      return 31 * result + Arrays.hashCode(this.data);
   }
}
