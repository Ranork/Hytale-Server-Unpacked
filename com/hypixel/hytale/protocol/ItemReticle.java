package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemReticle {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 6;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 6;
   public static final int MAX_SIZE = 1677721600;
   public boolean hideBase;
   @Nullable
   public String[] parts;
   public float duration;

   public ItemReticle() {
   }

   public ItemReticle(boolean hideBase, @Nullable String[] parts, float duration) {
      this.hideBase = hideBase;
      this.parts = parts;
      this.duration = duration;
   }

   public ItemReticle(@Nonnull ItemReticle other) {
      this.hideBase = other.hideBase;
      this.parts = other.parts;
      this.duration = other.duration;
   }

   @Nonnull
   public static ItemReticle deserialize(@Nonnull ByteBuf buf, int offset) {
      ItemReticle obj = new ItemReticle();
      byte nullBits = buf.getByte(offset);
      obj.hideBase = buf.getByte(offset + 1) != 0;
      obj.duration = buf.getFloatLE(offset + 2);
      int pos = offset + 6;
      if ((nullBits & 1) != 0) {
         int partsCount = VarInt.peek(buf, pos);
         if (partsCount < 0) {
            throw ProtocolException.negativeLength("Parts", partsCount);
         }

         if (partsCount > 4096000) {
            throw ProtocolException.arrayTooLong("Parts", partsCount, 4096000);
         }

         int partsVarLen = VarInt.size(partsCount);
         if (pos + partsVarLen + partsCount * 1L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Parts", pos + partsVarLen + partsCount * 1, buf.readableBytes());
         }

         pos += partsVarLen;
         obj.parts = new String[partsCount];

         for (int i = 0; i < partsCount; i++) {
            int strLen = VarInt.peek(buf, pos);
            if (strLen < 0) {
               throw ProtocolException.negativeLength("parts[" + i + "]", strLen);
            }

            if (strLen > 4096000) {
               throw ProtocolException.stringTooLong("parts[" + i + "]", strLen, 4096000);
            }

            int strVarLen = VarInt.length(buf, pos);
            obj.parts[i] = PacketIO.readVarString(buf, pos);
            pos += strVarLen + strLen;
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 6;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos);

         for (int i = 0; i < arrLen; i++) {
            int sl = VarInt.peek(buf, pos);
            pos += VarInt.length(buf, pos) + sl;
         }
      }

      return pos - offset;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.parts != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.hideBase ? 1 : 0);
      buf.writeFloatLE(this.duration);
      if (this.parts != null) {
         if (this.parts.length > 4096000) {
            throw ProtocolException.arrayTooLong("Parts", this.parts.length, 4096000);
         }

         VarInt.write(buf, this.parts.length);

         for (String item : this.parts) {
            PacketIO.writeVarString(buf, item, 4096000);
         }
      }
   }

   public int computeSize() {
      int size = 6;
      if (this.parts != null) {
         int partsSize = 0;

         for (String elem : this.parts) {
            partsSize += PacketIO.stringSize(elem);
         }

         size += VarInt.size(this.parts.length) + partsSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 6) {
         return ValidationResult.error("Buffer too small: expected at least 6 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 6;
         if ((nullBits & 1) != 0) {
            int partsCount = VarInt.peek(buffer, pos);
            if (partsCount < 0) {
               return ValidationResult.error("Invalid array count for Parts");
            }

            if (partsCount > 4096000) {
               return ValidationResult.error("Parts exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for (int i = 0; i < partsCount; i++) {
               int strLen = VarInt.peek(buffer, pos);
               if (strLen < 0) {
                  return ValidationResult.error("Invalid string length in Parts");
               }

               pos += VarInt.length(buffer, pos);
               pos += strLen;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading string in Parts");
               }
            }
         }

         return ValidationResult.OK;
      }
   }

   public ItemReticle clone() {
      ItemReticle copy = new ItemReticle();
      copy.hideBase = this.hideBase;
      copy.parts = this.parts != null ? Arrays.copyOf(this.parts, this.parts.length) : null;
      copy.duration = this.duration;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ItemReticle other)
            ? false
            : this.hideBase == other.hideBase && Arrays.equals((Object[])this.parts, (Object[])other.parts) && this.duration == other.duration;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Boolean.hashCode(this.hideBase);
      result = 31 * result + Arrays.hashCode((Object[])this.parts);
      return 31 * result + Float.hashCode(this.duration);
   }
}
