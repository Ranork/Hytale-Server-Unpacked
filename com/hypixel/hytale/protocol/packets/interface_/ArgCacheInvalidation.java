package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ArgCacheInvalidation implements Packet, ToClientPacket {
   public static final int PACKET_ID = 248;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public String[] argTypeIds;

   @Override
   public int getId() {
      return 248;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public ArgCacheInvalidation() {
   }

   public ArgCacheInvalidation(@Nullable String[] argTypeIds) {
      this.argTypeIds = argTypeIds;
   }

   public ArgCacheInvalidation(@Nonnull ArgCacheInvalidation other) {
      this.argTypeIds = other.argTypeIds;
   }

   @Nonnull
   public static ArgCacheInvalidation deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 1) {
         throw ProtocolException.bufferTooSmall("ArgCacheInvalidation", 1, buf.readableBytes() - offset);
      } else {
         ArgCacheInvalidation obj = new ArgCacheInvalidation();
         byte nullBits = buf.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            int argTypeIdsCount = VarInt.peek(buf, pos);
            if (argTypeIdsCount < 0) {
               throw ProtocolException.invalidVarInt("ArgTypeIds");
            }

            int argTypeIdsVarLen = VarInt.size(argTypeIdsCount);
            if (argTypeIdsCount > 4096000) {
               throw ProtocolException.arrayTooLong("ArgTypeIds", argTypeIdsCount, 4096000);
            }

            if (pos + argTypeIdsVarLen + argTypeIdsCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("ArgTypeIds", pos + argTypeIdsVarLen + argTypeIdsCount * 1, buf.readableBytes());
            }

            pos += argTypeIdsVarLen;
            obj.argTypeIds = new String[argTypeIdsCount];

            for (int i = 0; i < argTypeIdsCount; i++) {
               int strLen = VarInt.peek(buf, pos);
               if (strLen < 0) {
                  throw ProtocolException.invalidVarInt("argTypeIds[" + i + "]");
               }

               int strVarLen = VarInt.size(strLen);
               if (strLen > 4096000) {
                  throw ProtocolException.stringTooLong("argTypeIds[" + i + "]", strLen, 4096000);
               }

               if (pos + strVarLen + strLen > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("argTypeIds[" + i + "]", pos + strVarLen + strLen, buf.readableBytes());
               }

               obj.argTypeIds[i] = PacketIO.readVarString(buf, pos);
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
   public static String[] getArgTypeIds(MemorySegment mem) {
      return getArgTypeIds(mem, 0);
   }

   @Nullable
   public static String[] getArgTypeIds(MemorySegment mem, int offset) {
      if (!hasArgTypeIds(mem, offset)) {
         return null;
      } else {
         int off = offset + 1;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("ArgTypeIds", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("ArgTypeIds", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("ArgTypeIds", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               String[] data = new String[len];

               for (int i = 0; i < len; i++) {
                  long sp = VarInt.getWithLength(mem, off);
                  int n = (int)sp + (int)(sp >>> 32);
                  data[i] = PacketIO.readVarString("ArgTypeIds", mem, off, 16384000, PacketIO.UTF8);
                  off += n;
               }

               return data;
            }
         }
      }
   }

   public static boolean hasArgTypeIds(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static ArgCacheInvalidation toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ArgCacheInvalidation toObject(MemorySegment mem, int offset) {
      if (offset + 1 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ArgCacheInvalidation", offset + 1, (int)mem.byteSize());
      } else {
         String[] argTypeIds = null;
         if (hasArgTypeIds(mem, offset)) {
            int off = offset + 1;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("ArgTypeIds", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("ArgTypeIds", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("ArgTypeIds", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            argTypeIds = new String[len];

            for (int i = 0; i < len; i++) {
               long sp = VarInt.getWithLength(mem, off);
               int n = (int)sp + (int)(sp >>> 32);
               argTypeIds[i] = PacketIO.readVarString("ArgTypeIds", mem, off, 16384000, PacketIO.UTF8);
               off += n;
            }
         }

         return new ArgCacheInvalidation(argTypeIds);
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.argTypeIds != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.argTypeIds != null) {
         if (this.argTypeIds.length > 4096000) {
            throw ProtocolException.arrayTooLong("ArgTypeIds", this.argTypeIds.length, 4096000);
         }

         VarInt.write(buf, this.argTypeIds.length);

         for (String item : this.argTypeIds) {
            PacketIO.writeVarString(buf, item, 4096000);
         }
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.argTypeIds != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 1;
      if (this.argTypeIds != null) {
         if (this.argTypeIds.length > 4096000) {
            throw ProtocolException.arrayTooLong("ArgTypeIds", this.argTypeIds.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.argTypeIds.length);
         int argTypeIdsValueOffset = 0;

         for (int i = 0; i < this.argTypeIds.length; i++) {
            argTypeIdsValueOffset += PacketIO.writeVarString(mem, varOffset + argTypeIdsValueOffset, this.argTypeIds[i], 16384000);
         }

         varOffset += argTypeIdsValueOffset;
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 1;
      if (this.argTypeIds != null) {
         int argTypeIdsSize = 0;

         for (String elem : this.argTypeIds) {
            argTypeIdsSize += PacketIO.stringSize(elem);
         }

         size += VarInt.size(this.argTypeIds.length) + argTypeIdsSize;
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
            int argTypeIdsCount = VarInt.peek(buffer, pos);
            if (argTypeIdsCount < 0) {
               return ValidationResult.error("Invalid array count for ArgTypeIds");
            }

            if (argTypeIdsCount > 4096000) {
               return ValidationResult.error("ArgTypeIds exceeds max length 4096000");
            }

            pos += VarInt.size(argTypeIdsCount);

            for (int i = 0; i < argTypeIdsCount; i++) {
               int strLen = VarInt.peek(buffer, pos);
               if (strLen < 0) {
                  return ValidationResult.error("Invalid string length in ArgTypeIds");
               }

               pos += VarInt.size(strLen);
               pos += strLen;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading string in ArgTypeIds");
               }
            }
         }

         return ValidationResult.OK;
      }
   }

   public ArgCacheInvalidation clone() {
      ArgCacheInvalidation copy = new ArgCacheInvalidation();
      copy.argTypeIds = this.argTypeIds != null ? Arrays.copyOf(this.argTypeIds, this.argTypeIds.length) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof ArgCacheInvalidation other ? Arrays.equals((Object[])this.argTypeIds, (Object[])other.argTypeIds) : false;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      return 31 * result + Arrays.hashCode((Object[])this.argTypeIds);
   }
}
