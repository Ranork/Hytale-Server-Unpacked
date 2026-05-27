package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class Asset {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 64;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 64;
   public static final int MAX_SIZE = 2117;
   @Nonnull
   public String hash = "";
   @Nonnull
   public String name = "";

   public Asset() {
   }

   public Asset(@Nonnull String hash, @Nonnull String name) {
      this.hash = hash;
      this.name = name;
   }

   public Asset(@Nonnull Asset other) {
      this.hash = other.hash;
      this.name = other.name;
   }

   @Nonnull
   public static Asset deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 64) {
         throw ProtocolException.bufferTooSmall("Asset", 64, buf.readableBytes() - offset);
      } else {
         Asset obj = new Asset();
         obj.hash = PacketIO.readFixedAsciiString(buf, offset + 0, 64);
         int pos = offset + 64;
         int nameLen = VarInt.peek(buf, pos);
         if (nameLen < 0) {
            throw ProtocolException.invalidVarInt("Name");
         } else {
            int nameVarLen = VarInt.size(nameLen);
            if (nameLen > 512) {
               throw ProtocolException.stringTooLong("Name", nameLen, 512);
            } else if (pos + nameVarLen + nameLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Name", pos + nameVarLen + nameLen, buf.readableBytes());
            } else {
               obj.name = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
               pos += nameVarLen + nameLen;
               return obj;
            }
         }
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      int pos = offset + 64;
      int sl = VarInt.peek(buf, pos);
      pos += VarInt.size(sl) + sl;
      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 64L;
   }

   public static String getHash(MemorySegment mem) {
      return getHash(mem, 0);
   }

   public static String getHash(MemorySegment mem, int offset) {
      return PacketIO.readFixedAsciiString(mem, offset + 0, 64);
   }

   public static String getName(MemorySegment mem) {
      return getName(mem, 0);
   }

   public static String getName(MemorySegment mem, int offset) {
      return PacketIO.readVarString("Name", mem, offset + 64, 512, PacketIO.UTF8);
   }

   public static Asset toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static Asset toObject(MemorySegment mem, int offset) {
      if (offset + 64 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("Asset", offset + 64, (int)mem.byteSize());
      } else {
         return new Asset(PacketIO.readFixedAsciiString(mem, offset + 0, 64), PacketIO.readVarString("Name", mem, offset + 64, 512, PacketIO.UTF8));
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      PacketIO.writeFixedAsciiString(buf, this.hash, 64);
      PacketIO.writeVarString(buf, this.name, 512);
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      PacketIO.writeFixedAsciiString(mem, offset + 0, this.hash, 64);
      int varOffset = offset + 64;
      varOffset += PacketIO.writeVarString(mem, varOffset, this.name, 512);
      return varOffset - offset;
   }

   public int computeSize() {
      int size = 64;
      return size + PacketIO.stringSize(this.name);
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 64) {
         return ValidationResult.error("Buffer too small: expected at least 64 bytes");
      } else {
         int pos = offset + 64;
         int nameLen = VarInt.peek(buffer, pos);
         if (nameLen < 0) {
            return ValidationResult.error("Invalid string length for Name");
         } else if (nameLen > 512) {
            return ValidationResult.error("Name exceeds max length 512");
         } else {
            pos += VarInt.size(nameLen);
            pos += nameLen;
            return pos > buffer.writerIndex() ? ValidationResult.error("Buffer overflow reading Name") : ValidationResult.OK;
         }
      }
   }

   public Asset clone() {
      Asset copy = new Asset();
      copy.hash = this.hash;
      copy.name = this.name;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof Asset other) ? false : Objects.equals(this.hash, other.hash) && Objects.equals(this.name, other.name);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.hash, this.name);
   }
}
