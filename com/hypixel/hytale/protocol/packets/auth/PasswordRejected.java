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
      if (buf.readableBytes() - offset < 5) {
         throw ProtocolException.bufferTooSmall("PasswordRejected", 5, buf.readableBytes() - offset);
      } else {
         PasswordRejected obj = new PasswordRejected();
         byte nullBits = buf.getByte(offset);
         obj.attemptsRemaining = buf.getIntLE(offset + 1);
         int pos = offset + 5;
         if ((nullBits & 1) != 0) {
            int newChallengeCount = VarInt.peek(buf, pos);
            if (newChallengeCount < 0) {
               throw ProtocolException.invalidVarInt("NewChallenge");
            }

            int newChallengeVarLen = VarInt.size(newChallengeCount);
            if (newChallengeCount > 64) {
               throw ProtocolException.arrayTooLong("NewChallenge", newChallengeCount, 64);
            }

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
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 5;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.size(arrLen) + arrLen * 1;
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 5L;
   }

   @Nullable
   public static byte[] getNewChallenge(MemorySegment mem) {
      return getNewChallenge(mem, 0);
   }

   @Nullable
   public static byte[] getNewChallenge(MemorySegment mem, int offset) {
      if (!hasNewChallenge(mem, offset)) {
         return null;
      } else {
         int off = offset + 5;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("NewChallenge", len);
         } else if (len > 64) {
            throw ProtocolException.arrayTooLong("NewChallenge", len, 64);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("NewChallenge", off + lenOffset + len * 1, (int)mem.byteSize());
            } else {
               off += lenOffset;
               byte[] data = new byte[len];
               MemorySegment.copy(mem, PacketIO.PROTO_BYTE, off, data, 0, len);
               return data;
            }
         }
      }
   }

   public static int getAttemptsRemaining(MemorySegment mem) {
      return getAttemptsRemaining(mem, 0);
   }

   public static int getAttemptsRemaining(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   public static boolean hasNewChallenge(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static PasswordRejected toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static PasswordRejected toObject(MemorySegment mem, int offset) {
      if (offset + 5 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("PasswordRejected", offset + 5, (int)mem.byteSize());
      } else {
         byte[] newChallenge = null;
         if (hasNewChallenge(mem, offset)) {
            int off = offset + 5;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("NewChallenge", len);
            }

            if (len > 64) {
               throw ProtocolException.arrayTooLong("NewChallenge", len, 64);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("NewChallenge", off + lenOffset + len * 1, (int)mem.byteSize());
            }

            off += lenOffset;
            newChallenge = new byte[len];
            MemorySegment.copy(mem, PacketIO.PROTO_BYTE, off, newChallenge, 0, len);
         }

         return new PasswordRejected(newChallenge, mem.get(PacketIO.PROTO_INT, (long)(offset + 1)));
      }
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
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.newChallenge != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.attemptsRemaining);
      int varOffset = offset + 5;
      if (this.newChallenge != null) {
         if (this.newChallenge.length > 64) {
            throw ProtocolException.arrayTooLong("NewChallenge", this.newChallenge.length, 64);
         }

         varOffset += VarInt.set(mem, varOffset, this.newChallenge.length);
         MemorySegment.copy(this.newChallenge, 0, mem, PacketIO.PROTO_BYTE, varOffset, this.newChallenge.length);
         varOffset += this.newChallenge.length * 1;
      }

      return varOffset - offset;
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

            pos += VarInt.size(newChallengeCount);
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
