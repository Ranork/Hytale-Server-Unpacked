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
      if (buf.readableBytes() - offset < 1) {
         throw ProtocolException.bufferTooSmall("ConnectAccept", 1, buf.readableBytes() - offset);
      } else {
         ConnectAccept obj = new ConnectAccept();
         byte nullBits = buf.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            int passwordChallengeCount = VarInt.peek(buf, pos);
            if (passwordChallengeCount < 0) {
               throw ProtocolException.invalidVarInt("PasswordChallenge");
            }

            int passwordChallengeVarLen = VarInt.size(passwordChallengeCount);
            if (passwordChallengeCount > 64) {
               throw ProtocolException.arrayTooLong("PasswordChallenge", passwordChallengeCount, 64);
            }

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
   public static byte[] getPasswordChallenge(MemorySegment mem) {
      return getPasswordChallenge(mem, 0);
   }

   @Nullable
   public static byte[] getPasswordChallenge(MemorySegment mem, int offset) {
      if (!hasPasswordChallenge(mem, offset)) {
         return null;
      } else {
         int off = offset + 1;
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

   public static boolean hasPasswordChallenge(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static ConnectAccept toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ConnectAccept toObject(MemorySegment mem, int offset) {
      if (offset + 1 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ConnectAccept", offset + 1, (int)mem.byteSize());
      } else {
         byte[] passwordChallenge = null;
         if (hasPasswordChallenge(mem, offset)) {
            int off = offset + 1;
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

         return new ConnectAccept(passwordChallenge);
      }
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
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.passwordChallenge != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 1;
      if (this.passwordChallenge != null) {
         if (this.passwordChallenge.length > 64) {
            throw ProtocolException.arrayTooLong("PasswordChallenge", this.passwordChallenge.length, 64);
         }

         varOffset += VarInt.set(mem, varOffset, this.passwordChallenge.length);
         MemorySegment.copy(this.passwordChallenge, 0, mem, PacketIO.PROTO_BYTE, varOffset, this.passwordChallenge.length);
         varOffset += this.passwordChallenge.length * 1;
      }

      return varOffset - offset;
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

            pos += VarInt.size(passwordChallengeCount);
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
