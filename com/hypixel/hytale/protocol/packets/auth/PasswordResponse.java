package com.hypixel.hytale.protocol.packets.auth;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
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
      PasswordResponse obj = new PasswordResponse();
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int hashCount = VarInt.peek(buf, pos);
         if (hashCount < 0) {
            throw ProtocolException.negativeLength("Hash", hashCount);
         }

         if (hashCount > 64) {
            throw ProtocolException.arrayTooLong("Hash", hashCount, 64);
         }

         int hashVarLen = VarInt.size(hashCount);
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

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos) + arrLen * 1;
      }

      return pos - offset;
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

            pos += VarInt.length(buffer, pos);
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
