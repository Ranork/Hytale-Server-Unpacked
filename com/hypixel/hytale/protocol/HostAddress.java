package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class HostAddress {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 2;
   public static final int MAX_SIZE = 1031;
   @Nonnull
   public String host = "";
   public short port;

   public HostAddress() {
   }

   public HostAddress(@Nonnull String host, short port) {
      this.host = host;
      this.port = port;
   }

   public HostAddress(@Nonnull HostAddress other) {
      this.host = other.host;
      this.port = other.port;
   }

   @Nonnull
   public static HostAddress deserialize(@Nonnull ByteBuf buf, int offset) {
      HostAddress obj = new HostAddress();
      obj.port = buf.getShortLE(offset + 0);
      int pos = offset + 2;
      int hostLen = VarInt.peek(buf, pos);
      if (hostLen < 0) {
         throw ProtocolException.negativeLength("Host", hostLen);
      } else if (hostLen > 256) {
         throw ProtocolException.stringTooLong("Host", hostLen, 256);
      } else {
         int hostVarLen = VarInt.length(buf, pos);
         obj.host = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
         pos += hostVarLen + hostLen;
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      int pos = offset + 2;
      int sl = VarInt.peek(buf, pos);
      pos += VarInt.length(buf, pos) + sl;
      return pos - offset;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeShortLE(this.port);
      PacketIO.writeVarString(buf, this.host, 256);
   }

   public int computeSize() {
      int size = 2;
      return size + PacketIO.stringSize(this.host);
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 2) {
         return ValidationResult.error("Buffer too small: expected at least 2 bytes");
      } else {
         int pos = offset + 2;
         int hostLen = VarInt.peek(buffer, pos);
         if (hostLen < 0) {
            return ValidationResult.error("Invalid string length for Host");
         } else if (hostLen > 256) {
            return ValidationResult.error("Host exceeds max length 256");
         } else {
            pos += VarInt.length(buffer, pos);
            pos += hostLen;
            return pos > buffer.writerIndex() ? ValidationResult.error("Buffer overflow reading Host") : ValidationResult.OK;
         }
      }
   }

   public HostAddress clone() {
      HostAddress copy = new HostAddress();
      copy.host = this.host;
      copy.port = this.port;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof HostAddress other) ? false : Objects.equals(this.host, other.host) && this.port == other.port;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.host, this.port);
   }
}
