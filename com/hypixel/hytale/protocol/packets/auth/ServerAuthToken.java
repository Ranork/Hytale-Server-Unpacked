package com.hypixel.hytale.protocol.packets.auth;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ServerAuthToken implements Packet, ToClientPacket {
   public static final int PACKET_ID = 13;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 32851;
   @Nullable
   public String serverAccessToken;
   @Nullable
   public byte[] passwordChallenge;

   @Override
   public int getId() {
      return 13;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public ServerAuthToken() {
   }

   public ServerAuthToken(@Nullable String serverAccessToken, @Nullable byte[] passwordChallenge) {
      this.serverAccessToken = serverAccessToken;
      this.passwordChallenge = passwordChallenge;
   }

   public ServerAuthToken(@Nonnull ServerAuthToken other) {
      this.serverAccessToken = other.serverAccessToken;
      this.passwordChallenge = other.passwordChallenge;
   }

   @Nonnull
   public static ServerAuthToken deserialize(@Nonnull ByteBuf buf, int offset) {
      ServerAuthToken obj = new ServerAuthToken();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 9 + buf.getIntLE(offset + 1);
         int serverAccessTokenLen = VarInt.peek(buf, varPos0);
         if (serverAccessTokenLen < 0) {
            throw ProtocolException.negativeLength("ServerAccessToken", serverAccessTokenLen);
         }

         if (serverAccessTokenLen > 8192) {
            throw ProtocolException.stringTooLong("ServerAccessToken", serverAccessTokenLen, 8192);
         }

         obj.serverAccessToken = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 9 + buf.getIntLE(offset + 5);
         int passwordChallengeCount = VarInt.peek(buf, varPos1);
         if (passwordChallengeCount < 0) {
            throw ProtocolException.negativeLength("PasswordChallenge", passwordChallengeCount);
         }

         if (passwordChallengeCount > 64) {
            throw ProtocolException.arrayTooLong("PasswordChallenge", passwordChallengeCount, 64);
         }

         int varIntLen = VarInt.length(buf, varPos1);
         if (varPos1 + varIntLen + passwordChallengeCount * 1L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("PasswordChallenge", varPos1 + varIntLen + passwordChallengeCount * 1, buf.readableBytes());
         }

         obj.passwordChallenge = new byte[passwordChallengeCount];

         for (int i = 0; i < passwordChallengeCount; i++) {
            obj.passwordChallenge[i] = buf.getByte(varPos1 + varIntLen + i * 1);
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 9;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         int pos0 = offset + 9 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         int pos1 = offset + 9 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1) + arrLen * 1;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.serverAccessToken != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.passwordChallenge != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      int serverAccessTokenOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int passwordChallengeOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.serverAccessToken != null) {
         buf.setIntLE(serverAccessTokenOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.serverAccessToken, 8192);
      } else {
         buf.setIntLE(serverAccessTokenOffsetSlot, -1);
      }

      if (this.passwordChallenge != null) {
         buf.setIntLE(passwordChallengeOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.passwordChallenge.length > 64) {
            throw ProtocolException.arrayTooLong("PasswordChallenge", this.passwordChallenge.length, 64);
         }

         VarInt.write(buf, this.passwordChallenge.length);

         for (byte item : this.passwordChallenge) {
            buf.writeByte(item);
         }
      } else {
         buf.setIntLE(passwordChallengeOffsetSlot, -1);
      }
   }

   @Override
   public int computeSize() {
      int size = 9;
      if (this.serverAccessToken != null) {
         size += PacketIO.stringSize(this.serverAccessToken);
      }

      if (this.passwordChallenge != null) {
         size += VarInt.size(this.passwordChallenge.length) + this.passwordChallenge.length * 1;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 9) {
         return ValidationResult.error("Buffer too small: expected at least 9 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int serverAccessTokenOffset = buffer.getIntLE(offset + 1);
            if (serverAccessTokenOffset < 0) {
               return ValidationResult.error("Invalid offset for ServerAccessToken");
            }

            int pos = offset + 9 + serverAccessTokenOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ServerAccessToken");
            }

            int serverAccessTokenLen = VarInt.peek(buffer, pos);
            if (serverAccessTokenLen < 0) {
               return ValidationResult.error("Invalid string length for ServerAccessToken");
            }

            if (serverAccessTokenLen > 8192) {
               return ValidationResult.error("ServerAccessToken exceeds max length 8192");
            }

            pos += VarInt.length(buffer, pos);
            pos += serverAccessTokenLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading ServerAccessToken");
            }
         }

         if ((nullBits & 2) != 0) {
            int passwordChallengeOffset = buffer.getIntLE(offset + 5);
            if (passwordChallengeOffset < 0) {
               return ValidationResult.error("Invalid offset for PasswordChallenge");
            }

            int posx = offset + 9 + passwordChallengeOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for PasswordChallenge");
            }

            int passwordChallengeCount = VarInt.peek(buffer, posx);
            if (passwordChallengeCount < 0) {
               return ValidationResult.error("Invalid array count for PasswordChallenge");
            }

            if (passwordChallengeCount > 64) {
               return ValidationResult.error("PasswordChallenge exceeds max length 64");
            }

            posx += VarInt.length(buffer, posx);
            posx += passwordChallengeCount * 1;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading PasswordChallenge");
            }
         }

         return ValidationResult.OK;
      }
   }

   public ServerAuthToken clone() {
      ServerAuthToken copy = new ServerAuthToken();
      copy.serverAccessToken = this.serverAccessToken;
      copy.passwordChallenge = this.passwordChallenge != null ? Arrays.copyOf(this.passwordChallenge, this.passwordChallenge.length) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ServerAuthToken other)
            ? false
            : Objects.equals(this.serverAccessToken, other.serverAccessToken) && Arrays.equals(this.passwordChallenge, other.passwordChallenge);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.serverAccessToken);
      return 31 * result + Arrays.hashCode(this.passwordChallenge);
   }
}
