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

public class PasswordRejected implements Packet, ToClientPacket {
   public static final int PACKET_ID = 17;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 5;
   public static final int MAX_SIZE = 74;
   @Nullable
   public byte[] newChallenge;
   public int attemptsRemaining;

   @Override
   public int getId() {
      return 17;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public PasswordRejected() {
   }

   public PasswordRejected(@Nullable byte[] newChallenge, int attemptsRemaining) {
      this.newChallenge = newChallenge;
      this.attemptsRemaining = attemptsRemaining;
   }

   public PasswordRejected(@Nonnull PasswordRejected other) {
      this.newChallenge = other.newChallenge;
      this.attemptsRemaining = other.attemptsRemaining;
   }

   @Nonnull
   public static PasswordRejected deserialize(@Nonnull ByteBuf buf, int offset) {
      PasswordRejected obj = new PasswordRejected();
      byte nullBits = buf.getByte(offset);
      obj.attemptsRemaining = buf.getIntLE(offset + 1);
      int pos = offset + 5;
      if ((nullBits & 1) != 0) {
         int newChallengeCount = VarInt.peek(buf, pos);
         if (newChallengeCount < 0) {
            throw ProtocolException.negativeLength("NewChallenge", newChallengeCount);
         }

         if (newChallengeCount > 64) {
            throw ProtocolException.arrayTooLong("NewChallenge", newChallengeCount, 64);
         }

         int newChallengeVarLen = VarInt.size(newChallengeCount);
         if (pos + newChallengeVarLen + newChallengeCount * 1L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("NewChallenge", pos + newChallengeVarLen + newChallengeCount * 1, buf.readableBytes());
         }

         pos += newChallengeVarLen;
         obj.newChallenge = new byte[newChallengeCount];

         for (int i = 0; i < newChallengeCount; i++) {
            obj.newChallenge[i] = buf.getByte(pos + i * 1);
         }

         pos += newChallengeCount * 1;
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 5;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos) + arrLen * 1;
      }

      return pos - offset;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.newChallenge != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.attemptsRemaining);
      if (this.newChallenge != null) {
         if (this.newChallenge.length > 64) {
            throw ProtocolException.arrayTooLong("NewChallenge", this.newChallenge.length, 64);
         }

         VarInt.write(buf, this.newChallenge.length);

         for (byte item : this.newChallenge) {
            buf.writeByte(item);
         }
      }
   }

   @Override
   public int computeSize() {
      int size = 5;
      if (this.newChallenge != null) {
         size += VarInt.size(this.newChallenge.length) + this.newChallenge.length * 1;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 5) {
         return ValidationResult.error("Buffer too small: expected at least 5 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 5;
         if ((nullBits & 1) != 0) {
            int newChallengeCount = VarInt.peek(buffer, pos);
            if (newChallengeCount < 0) {
               return ValidationResult.error("Invalid array count for NewChallenge");
            }

            if (newChallengeCount > 64) {
               return ValidationResult.error("NewChallenge exceeds max length 64");
            }

            pos += VarInt.length(buffer, pos);
            pos += newChallengeCount * 1;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading NewChallenge");
            }
         }

         return ValidationResult.OK;
      }
   }

   public PasswordRejected clone() {
      PasswordRejected copy = new PasswordRejected();
      copy.newChallenge = this.newChallenge != null ? Arrays.copyOf(this.newChallenge, this.newChallenge.length) : null;
      copy.attemptsRemaining = this.attemptsRemaining;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof PasswordRejected other)
            ? false
            : Arrays.equals(this.newChallenge, other.newChallenge) && this.attemptsRemaining == other.attemptsRemaining;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Arrays.hashCode(this.newChallenge);
      return 31 * result + Integer.hashCode(this.attemptsRemaining);
   }
}
