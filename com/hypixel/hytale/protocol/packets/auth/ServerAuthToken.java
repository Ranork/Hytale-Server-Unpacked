package com.hypixel.hytale.protocol.packets.auth;

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
      if (buf.readableBytes() - offset < 9) {
         throw ProtocolException.bufferTooSmall("ServerAuthToken", 9, buf.readableBytes() - offset);
      } else {
         ServerAuthToken obj = new ServerAuthToken();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 1);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 9) {
               throw ProtocolException.invalidOffset("ServerAccessToken", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 9 + varPosBase0;
            int serverAccessTokenLen = VarInt.peek(buf, varPos0);
            if (serverAccessTokenLen < 0) {
               throw ProtocolException.invalidVarInt("ServerAccessToken");
            }

            int serverAccessTokenVarIntLen = VarInt.size(serverAccessTokenLen);
            if (serverAccessTokenLen > 8192) {
               throw ProtocolException.stringTooLong("ServerAccessToken", serverAccessTokenLen, 8192);
            }

            if (varPos0 + serverAccessTokenVarIntLen + serverAccessTokenLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("ServerAccessToken", varPos0 + serverAccessTokenVarIntLen + serverAccessTokenLen, buf.readableBytes());
            }

            obj.serverAccessToken = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 5);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 9) {
               throw ProtocolException.invalidOffset("PasswordChallenge", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 9 + varPosBase1;
            int passwordChallengeCount = VarInt.peek(buf, varPos1);
            if (passwordChallengeCount < 0) {
               throw ProtocolException.invalidVarInt("PasswordChallenge");
            }

            int varIntLen = VarInt.size(passwordChallengeCount);
            if (passwordChallengeCount > 64) {
               throw ProtocolException.arrayTooLong("PasswordChallenge", passwordChallengeCount, 64);
            }

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
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 9;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 9) {
            throw ProtocolException.invalidOffset("ServerAccessToken", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 9 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 9) {
            throw ProtocolException.invalidOffset("PasswordChallenge", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 9 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(arrLen) + arrLen * 1;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 9L;
   }

   @Nullable
   public static String getServerAccessToken(MemorySegment mem) {
      return getServerAccessToken(mem, 0);
   }

   @Nullable
   public static String getServerAccessToken(MemorySegment mem, int offset) {
      return hasServerAccessToken(mem, offset)
         ? PacketIO.readVarString("ServerAccessToken", mem, offset + getValidatedOffset(mem, offset, 1, 9, "ServerAccessToken"), 8192, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static byte[] getPasswordChallenge(MemorySegment mem) {
      return getPasswordChallenge(mem, 0);
   }

   @Nullable
   public static byte[] getPasswordChallenge(MemorySegment mem, int offset) {
      if (!hasPasswordChallenge(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 5, 9, "PasswordChallenge");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("PasswordChallenge", len);
         } else if (len > 64) {
            throw ProtocolException.arrayTooLong("PasswordChallenge", len, 64);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("PasswordChallenge", off + lenOffset + len * 1, (int)mem.byteSize());
            } else {
               off += lenOffset;
               byte[] data = new byte[len];
               MemorySegment.copy(mem, PacketIO.PROTO_BYTE, off, data, 0, len);
               return data;
            }
         }
      }
   }

   public static boolean hasServerAccessToken(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasPasswordChallenge(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static ServerAuthToken toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ServerAuthToken toObject(MemorySegment mem, int offset) {
      if (offset + 9 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ServerAuthToken", offset + 9, (int)mem.byteSize());
      } else {
         byte[] passwordChallenge = null;
         if (hasPasswordChallenge(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 5, 9, "PasswordChallenge");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("PasswordChallenge", len);
            }

            if (len > 64) {
               throw ProtocolException.arrayTooLong("PasswordChallenge", len, 64);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("PasswordChallenge", off + lenOffset + len * 1, (int)mem.byteSize());
            }

            off += lenOffset;
            passwordChallenge = new byte[len];
            MemorySegment.copy(mem, PacketIO.PROTO_BYTE, off, passwordChallenge, 0, len);
         }

         return new ServerAuthToken(
            hasServerAccessToken(mem, offset)
               ? PacketIO.readVarString("ServerAccessToken", mem, offset + getValidatedOffset(mem, offset, 1, 9, "ServerAccessToken"), 8192, PacketIO.UTF8)
               : null,
            passwordChallenge
         );
      }
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
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.serverAccessToken != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.passwordChallenge != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 9;
      if (this.serverAccessToken != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), varOffset - offset - 9);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.serverAccessToken, 8192);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), -1);
      }

      if (this.passwordChallenge != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), varOffset - offset - 9);
         if (this.passwordChallenge.length > 64) {
            throw ProtocolException.arrayTooLong("PasswordChallenge", this.passwordChallenge.length, 64);
         }

         varOffset += VarInt.set(mem, varOffset, this.passwordChallenge.length);
         MemorySegment.copy(this.passwordChallenge, 0, mem, PacketIO.PROTO_BYTE, varOffset, this.passwordChallenge.length);
         varOffset += this.passwordChallenge.length * 1;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), -1);
      }

      return varOffset - offset;
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
            if (serverAccessTokenOffset < 0 || serverAccessTokenOffset > buffer.writerIndex() - offset - 9) {
               return ValidationResult.error("Invalid offset for ServerAccessToken");
            }

            int pos = offset + 9 + serverAccessTokenOffset;
            int serverAccessTokenLen = VarInt.peek(buffer, pos);
            if (serverAccessTokenLen < 0) {
               return ValidationResult.error("Invalid string length for ServerAccessToken");
            }

            if (serverAccessTokenLen > 8192) {
               return ValidationResult.error("ServerAccessToken exceeds max length 8192");
            }

            pos += VarInt.size(serverAccessTokenLen);
            pos += serverAccessTokenLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading ServerAccessToken");
            }
         }

         if ((nullBits & 2) != 0) {
            int passwordChallengeOffset = buffer.getIntLE(offset + 5);
            if (passwordChallengeOffset < 0 || passwordChallengeOffset > buffer.writerIndex() - offset - 9) {
               return ValidationResult.error("Invalid offset for PasswordChallenge");
            }

            int posx = offset + 9 + passwordChallengeOffset;
            int passwordChallengeCount = VarInt.peek(buffer, posx);
            if (passwordChallengeCount < 0) {
               return ValidationResult.error("Invalid array count for PasswordChallenge");
            }

            if (passwordChallengeCount > 64) {
               return ValidationResult.error("PasswordChallenge exceeds max length 64");
            }

            posx += VarInt.size(passwordChallengeCount);
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
