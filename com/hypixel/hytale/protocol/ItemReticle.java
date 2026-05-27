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
      if (buf.readableBytes() - offset < 6) {
         throw ProtocolException.bufferTooSmall("ItemReticle", 6, buf.readableBytes() - offset);
      } else {
         ItemReticle obj = new ItemReticle();
         byte nullBits = buf.getByte(offset);
         obj.hideBase = buf.getByte(offset + 1) != 0;
         obj.duration = buf.getFloatLE(offset + 2);
         int pos = offset + 6;
         if ((nullBits & 1) != 0) {
            int partsCount = VarInt.peek(buf, pos);
            if (partsCount < 0) {
               throw ProtocolException.invalidVarInt("Parts");
            }

            int partsVarLen = VarInt.size(partsCount);
            if (partsCount > 4096000) {
               throw ProtocolException.arrayTooLong("Parts", partsCount, 4096000);
            }

            if (pos + partsVarLen + partsCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Parts", pos + partsVarLen + partsCount * 1, buf.readableBytes());
            }

            pos += partsVarLen;
            obj.parts = new String[partsCount];

            for (int i = 0; i < partsCount; i++) {
               int strLen = VarInt.peek(buf, pos);
               if (strLen < 0) {
                  throw ProtocolException.invalidVarInt("parts[" + i + "]");
               }

               int strVarLen = VarInt.size(strLen);
               if (strLen > 4096000) {
                  throw ProtocolException.stringTooLong("parts[" + i + "]", strLen, 4096000);
               }

               if (pos + strVarLen + strLen > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("parts[" + i + "]", pos + strVarLen + strLen, buf.readableBytes());
               }

               obj.parts[i] = PacketIO.readVarString(buf, pos);
               pos += strVarLen + strLen;
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 6;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            int sl = VarInt.peek(buf, pos);
            pos += VarInt.size(sl) + sl;
         }
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 6L;
   }

   public static boolean getHideBase(MemorySegment mem) {
      return getHideBase(mem, 0);
   }

   public static boolean getHideBase(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1));
   }

   @Nullable
   public static String[] getParts(MemorySegment mem) {
      return getParts(mem, 0);
   }

   @Nullable
   public static String[] getParts(MemorySegment mem, int offset) {
      if (!hasParts(mem, offset)) {
         return null;
      } else {
         int off = offset + 6;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Parts", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Parts", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Parts", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               String[] data = new String[len];

               for (int i = 0; i < len; i++) {
                  long sp = VarInt.getWithLength(mem, off);
                  int n = (int)sp + (int)(sp >>> 32);
                  data[i] = PacketIO.readVarString("Parts", mem, off, 16384000, PacketIO.UTF8);
                  off += n;
               }

               return data;
            }
         }
      }
   }

   public static float getDuration(MemorySegment mem) {
      return getDuration(mem, 0);
   }

   public static float getDuration(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 2));
   }

   public static boolean hasParts(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static ItemReticle toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ItemReticle toObject(MemorySegment mem, int offset) {
      if (offset + 6 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ItemReticle", offset + 6, (int)mem.byteSize());
      } else {
         String[] parts = null;
         if (hasParts(mem, offset)) {
            int off = offset + 6;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Parts", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Parts", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Parts", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            parts = new String[len];

            for (int i = 0; i < len; i++) {
               long sp = VarInt.getWithLength(mem, off);
               int n = (int)sp + (int)(sp >>> 32);
               parts[i] = PacketIO.readVarString("Parts", mem, off, 16384000, PacketIO.UTF8);
               off += n;
            }
         }

         return new ItemReticle(mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1)), parts, mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 2)));
      }
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

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.parts != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BOOL, offset + 1, this.hideBase);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 2), this.duration);
      int varOffset = offset + 6;
      if (this.parts != null) {
         if (this.parts.length > 4096000) {
            throw ProtocolException.arrayTooLong("Parts", this.parts.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.parts.length);
         int partsValueOffset = 0;

         for (int i = 0; i < this.parts.length; i++) {
            partsValueOffset += PacketIO.writeVarString(mem, varOffset + partsValueOffset, this.parts[i], 16384000);
         }

         varOffset += partsValueOffset;
      }

      return varOffset - offset;
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

            pos += VarInt.size(partsCount);

            for (int i = 0; i < partsCount; i++) {
               int strLen = VarInt.peek(buffer, pos);
               if (strLen < 0) {
                  return ValidationResult.error("Invalid string length in Parts");
               }

               pos += VarInt.size(strLen);
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
