package com.hypixel.hytale.protocol.packets.auth;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PasswordResponse implements Packet, ToServerPacket {
   public static final int PACKET_ID = 15;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 70;
   @Nullable
   public byte[] hash;

   @Override
   public int getId() {
      return 15;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public PasswordResponse() {
   }

   public PasswordResponse(@Nullable byte[] hash) {
      this.hash = hash;
   }

   public PasswordResponse(@Nonnull PasswordResponse other) {
      this.hash = other.hash;
   }

   @Nonnull
   public static PasswordResponse deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 1) {
         throw ProtocolException.bufferTooSmall("PasswordResponse", 1, buf.readableBytes() - offset);
      } else {
         PasswordResponse obj = new PasswordResponse();
         byte nullBits = buf.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            int hashCount = VarInt.peek(buf, pos);
            if (hashCount < 0) {
               throw ProtocolException.invalidVarInt("Hash");
            }

            int hashVarLen = VarInt.size(hashCount);
            if (hashCount > 64) {
               throw ProtocolException.arrayTooLong("Hash", hashCount, 64);
            }

            if (pos + hashVarLen + hashCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Hash", pos + hashVarLen + hashCount * 1, buf.readableBytes());
            }

            pos += hashVarLen;
            obj.hash = new byte[hashCount];

            for (int i = 0; i < hashCount; i++) {
               obj.hash[i] = buf.getByte(pos + i * 1);
            }

            pos += hashCount * 1;
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.size(arrLen) + arrLen * 1;
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 1L;
   }

   @Nullable
   public static byte[] getHash(MemorySegment mem) {
      return getHash(mem, 0);
   }

   @Nullable
   public static byte[] getHash(MemorySegment mem, int offset) {
      if (!hasHash(mem, offset)) {
         return null;
      } else {
         int off = offset + 1;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Hash", len);
         } else if (len > 64) {
            throw ProtocolException.arrayTooLong("Hash", len, 64);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Hash", off + lenOffset + len * 1, (int)mem.byteSize());
            } else {
               off += lenOffset;
               byte[] data = new byte[len];
               MemorySegment.copy(mem, PacketIO.PROTO_BYTE, off, data, 0, len);
               return data;
            }
         }
      }
   }

   public static boolean hasHash(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static PasswordResponse toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static PasswordResponse toObject(MemorySegment mem, int offset) {
      if (offset + 1 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("PasswordResponse", offset + 1, (int)mem.byteSize());
      } else {
         byte[] hash = null;
         if (hasHash(mem, offset)) {
            int off = offset + 1;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Hash", len);
            }

            if (len > 64) {
               throw ProtocolException.arrayTooLong("Hash", len, 64);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Hash", off + lenOffset + len * 1, (int)mem.byteSize());
            }

            off += lenOffset;
            hash = new byte[len];
            MemorySegment.copy(mem, PacketIO.PROTO_BYTE, off, hash, 0, len);
         }

         return new PasswordResponse(hash);
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.hash != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.hash != null) {
         if (this.hash.length > 64) {
            throw ProtocolException.arrayTooLong("Hash", this.hash.length, 64);
         }

         VarInt.write(buf, this.hash.length);

         for (byte item : this.hash) {
            buf.writeByte(item);
         }
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.hash != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 1;
      if (this.hash != null) {
         if (this.hash.length > 64) {
            throw ProtocolException.arrayTooLong("Hash", this.hash.length, 64);
         }

         varOffset += VarInt.set(mem, varOffset, this.hash.length);
         MemorySegment.copy(this.hash, 0, mem, PacketIO.PROTO_BYTE, varOffset, this.hash.length);
         varOffset += this.hash.length * 1;
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 1;
      if (this.hash != null) {
         size += VarInt.size(this.hash.length) + this.hash.length * 1;
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
            int hashCount = VarInt.peek(buffer, pos);
            if (hashCount < 0) {
               return ValidationResult.error("Invalid array count for Hash");
            }

            if (hashCount > 64) {
               return ValidationResult.error("Hash exceeds max length 64");
            }

            pos += VarInt.size(hashCount);
            pos += hashCount * 1;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Hash");
            }
         }

         return ValidationResult.OK;
      }
   }

   public PasswordResponse clone() {
      PasswordResponse copy = new PasswordResponse();
      copy.hash = this.hash != null ? Arrays.copyOf(this.hash, this.hash.length) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof PasswordResponse other ? Arrays.equals(this.hash, other.hash) : false;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      return 31 * result + Arrays.hashCode(this.hash);
   }
}
