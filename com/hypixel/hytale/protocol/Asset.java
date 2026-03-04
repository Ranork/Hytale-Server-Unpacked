package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
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
      Asset obj = new Asset();
      obj.hash = PacketIO.readFixedAsciiString(buf, offset + 0, 64);
      int pos = offset + 64;
      int nameLen = VarInt.peek(buf, pos);
      if (nameLen < 0) {
         throw ProtocolException.negativeLength("Name", nameLen);
      } else if (nameLen > 512) {
         throw ProtocolException.stringTooLong("Name", nameLen, 512);
      } else {
         int nameVarLen = VarInt.length(buf, pos);
         obj.name = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
         pos += nameVarLen + nameLen;
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      int pos = offset + 64;
      int sl = VarInt.peek(buf, pos);
      pos += VarInt.length(buf, pos) + sl;
      return pos - offset;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      PacketIO.writeFixedAsciiString(buf, this.hash, 64);
      PacketIO.writeVarString(buf, this.name, 512);
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
            pos += VarInt.length(buffer, pos);
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
