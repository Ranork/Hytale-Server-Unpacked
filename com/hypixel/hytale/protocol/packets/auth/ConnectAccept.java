package com.hypixel.hytale.protocol.packets.auth;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ConnectAccept implements Packet, ToClientPacket {
   public static final int PACKET_ID = 14;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 70;
   @Nullable
   public byte[] passwordChallenge;

   @Override
   public int getId() {
      return 14;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public ConnectAccept() {
   }

   public ConnectAccept(@Nullable byte[] passwordChallenge) {
      this.passwordChallenge = passwordChallenge;
   }

   public ConnectAccept(@Nonnull ConnectAccept other) {
      this.passwordChallenge = other.passwordChallenge;
   }

   @Nonnull
   public static ConnectAccept deserialize(@Nonnull ByteBuf buf, int offset) {
      ConnectAccept obj = new ConnectAccept();
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int passwordChallengeCount = VarInt.peek(buf, pos);
         if (passwordChallengeCount < 0) {
            throw ProtocolException.negativeLength("PasswordChallenge", passwordChallengeCount);
         }

         if (passwordChallengeCount > 64) {
            throw ProtocolException.arrayTooLong("PasswordChallenge", passwordChallengeCount, 64);
         }

         int passwordChallengeVarLen = VarInt.size(passwordChallengeCount);
         if (pos + passwordChallengeVarLen + passwordChallengeCount * 1L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("PasswordChallenge", pos + passwordChallengeVarLen + passwordChallengeCount * 1, buf.readableBytes());
         }

         pos += passwordChallengeVarLen;
         obj.passwordChallenge = new byte[passwordChallengeCount];

         for (int i = 0; i < passwordChallengeCount; i++) {
            obj.passwordChallenge[i] = buf.getByte(pos + i * 1);
         }

         pos += passwordChallengeCount * 1;
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
      if (this.passwordChallenge != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.passwordChallenge != null) {
         if (this.passwordChallenge.length > 64) {
            throw ProtocolException.arrayTooLong("PasswordChallenge", this.passwordChallenge.length, 64);
         }

         VarInt.write(buf, this.passwordChallenge.length);

         for (byte item : this.passwordChallenge) {
            buf.writeByte(item);
         }
      }
   }

   @Override
   public int computeSize() {
      int size = 1;
      if (this.passwordChallenge != null) {
         size += VarInt.size(this.passwordChallenge.length) + this.passwordChallenge.length * 1;
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
            int passwordChallengeCount = VarInt.peek(buffer, pos);
            if (passwordChallengeCount < 0) {
               return ValidationResult.error("Invalid array count for PasswordChallenge");
            }

            if (passwordChallengeCount > 64) {
               return ValidationResult.error("PasswordChallenge exceeds max length 64");
            }

            pos += VarInt.length(buffer, pos);
            pos += passwordChallengeCount * 1;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading PasswordChallenge");
            }
         }

         return ValidationResult.OK;
      }
   }

   public ConnectAccept clone() {
      ConnectAccept copy = new ConnectAccept();
      copy.passwordChallenge = this.passwordChallenge != null ? Arrays.copyOf(this.passwordChallenge, this.passwordChallenge.length) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof ConnectAccept other ? Arrays.equals(this.passwordChallenge, other.passwordChallenge) : false;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      return 31 * result + Arrays.hashCode(this.passwordChallenge);
   }
}
