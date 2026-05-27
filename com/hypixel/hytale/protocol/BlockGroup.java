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

public class BlockGroup {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public String[] names;

   public BlockGroup() {
   }

   public BlockGroup(@Nullable String[] names) {
      this.names = names;
   }

   public BlockGroup(@Nonnull BlockGroup other) {
      this.names = other.names;
   }

   @Nonnull
   public static BlockGroup deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 1) {
         throw ProtocolException.bufferTooSmall("BlockGroup", 1, buf.readableBytes() - offset);
      } else {
         BlockGroup obj = new BlockGroup();
         byte nullBits = buf.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            int namesCount = VarInt.peek(buf, pos);
            if (namesCount < 0) {
               throw ProtocolException.invalidVarInt("Names");
            }

            int namesVarLen = VarInt.size(namesCount);
            if (namesCount > 4096000) {
               throw ProtocolException.arrayTooLong("Names", namesCount, 4096000);
            }

            if (pos + namesVarLen + namesCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Names", pos + namesVarLen + namesCount * 1, buf.readableBytes());
            }

            pos += namesVarLen;
            obj.names = new String[namesCount];

            for (int i = 0; i < namesCount; i++) {
               int strLen = VarInt.peek(buf, pos);
               if (strLen < 0) {
                  throw ProtocolException.invalidVarInt("names[" + i + "]");
               }

               int strVarLen = VarInt.size(strLen);
               if (strLen > 4096000) {
                  throw ProtocolException.stringTooLong("names[" + i + "]", strLen, 4096000);
               }

               if (pos + strVarLen + strLen > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("names[" + i + "]", pos + strVarLen + strLen, buf.readableBytes());
               }

               obj.names[i] = PacketIO.readVarString(buf, pos);
               pos += strVarLen + strLen;
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
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
      return mem.byteSize() < 1L;
   }

   @Nullable
   public static String[] getNames(MemorySegment mem) {
      return getNames(mem, 0);
   }

   @Nullable
   public static String[] getNames(MemorySegment mem, int offset) {
      if (!hasNames(mem, offset)) {
         return null;
      } else {
         int off = offset + 1;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Names", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Names", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Names", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               String[] data = new String[len];

               for (int i = 0; i < len; i++) {
                  long sp = VarInt.getWithLength(mem, off);
                  int n = (int)sp + (int)(sp >>> 32);
                  data[i] = PacketIO.readVarString("Names", mem, off, 16384000, PacketIO.UTF8);
                  off += n;
               }

               return data;
            }
         }
      }
   }

   public static boolean hasNames(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static BlockGroup toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static BlockGroup toObject(MemorySegment mem, int offset) {
      if (offset + 1 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("BlockGroup", offset + 1, (int)mem.byteSize());
      } else {
         String[] names = null;
         if (hasNames(mem, offset)) {
            int off = offset + 1;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Names", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Names", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Names", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            names = new String[len];

            for (int i = 0; i < len; i++) {
               long sp = VarInt.getWithLength(mem, off);
               int n = (int)sp + (int)(sp >>> 32);
               names[i] = PacketIO.readVarString("Names", mem, off, 16384000, PacketIO.UTF8);
               off += n;
            }
         }

         return new BlockGroup(names);
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.names != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.names != null) {
         if (this.names.length > 4096000) {
            throw ProtocolException.arrayTooLong("Names", this.names.length, 4096000);
         }

         VarInt.write(buf, this.names.length);

         for (String item : this.names) {
            PacketIO.writeVarString(buf, item, 4096000);
         }
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.names != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 1;
      if (this.names != null) {
         if (this.names.length > 4096000) {
            throw ProtocolException.arrayTooLong("Names", this.names.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.names.length);
         int namesValueOffset = 0;

         for (int i = 0; i < this.names.length; i++) {
            namesValueOffset += PacketIO.writeVarString(mem, varOffset + namesValueOffset, this.names[i], 16384000);
         }

         varOffset += namesValueOffset;
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 1;
      if (this.names != null) {
         int namesSize = 0;

         for (String elem : this.names) {
            namesSize += PacketIO.stringSize(elem);
         }

         size += VarInt.size(this.names.length) + namesSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 1) {
         return ValidationResult.error("Buffer too small: expected at least 1 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            int namesCount = VarInt.peek(buffer, pos);
            if (namesCount < 0) {
               return ValidationResult.error("Invalid array count for Names");
            }

            if (namesCount > 4096000) {
               return ValidationResult.error("Names exceeds max length 4096000");
            }

            pos += VarInt.size(namesCount);

            for (int i = 0; i < namesCount; i++) {
               int strLen = VarInt.peek(buffer, pos);
               if (strLen < 0) {
                  return ValidationResult.error("Invalid string length in Names");
               }

               pos += VarInt.size(strLen);
               pos += strLen;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading string in Names");
               }
            }
         }

         return ValidationResult.OK;
      }
   }

   public BlockGroup clone() {
      BlockGroup copy = new BlockGroup();
      copy.names = this.names != null ? Arrays.copyOf(this.names, this.names.length) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof BlockGroup other ? Arrays.equals((Object[])this.names, (Object[])other.names) : false;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      return 31 * result + Arrays.hashCode((Object[])this.names);
   }
}
