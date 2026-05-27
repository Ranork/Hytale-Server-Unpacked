package com.hypixel.hytale.protocol.packets.world;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SleepMultiplayer {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 9;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 65536014;
   public int sleepersCount;
   public int awakeCount;
   @Nullable
   public UUID[] awakeSample;

   public SleepMultiplayer() {
   }

   public SleepMultiplayer(int sleepersCount, int awakeCount, @Nullable UUID[] awakeSample) {
      this.sleepersCount = sleepersCount;
      this.awakeCount = awakeCount;
      this.awakeSample = awakeSample;
   }

   public SleepMultiplayer(@Nonnull SleepMultiplayer other) {
      this.sleepersCount = other.sleepersCount;
      this.awakeCount = other.awakeCount;
      this.awakeSample = other.awakeSample;
   }

   @Nonnull
   public static SleepMultiplayer deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 9) {
         throw ProtocolException.bufferTooSmall("SleepMultiplayer", 9, buf.readableBytes() - offset);
      } else {
         SleepMultiplayer obj = new SleepMultiplayer();
         byte nullBits = buf.getByte(offset);
         obj.sleepersCount = buf.getIntLE(offset + 1);
         obj.awakeCount = buf.getIntLE(offset + 5);
         int pos = offset + 9;
         if ((nullBits & 1) != 0) {
            int awakeSampleCount = VarInt.peek(buf, pos);
            if (awakeSampleCount < 0) {
               throw ProtocolException.invalidVarInt("AwakeSample");
            }

            int awakeSampleVarLen = VarInt.size(awakeSampleCount);
            if (awakeSampleCount > 4096000) {
               throw ProtocolException.arrayTooLong("AwakeSample", awakeSampleCount, 4096000);
            }

            if (pos + awakeSampleVarLen + awakeSampleCount * 16L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("AwakeSample", pos + awakeSampleVarLen + awakeSampleCount * 16, buf.readableBytes());
            }

            pos += awakeSampleVarLen;
            obj.awakeSample = new UUID[awakeSampleCount];

            for (int i = 0; i < awakeSampleCount; i++) {
               obj.awakeSample[i] = PacketIO.readUUID(buf, pos + i * 16);
            }

            pos += awakeSampleCount * 16;
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 9;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.size(arrLen) + arrLen * 16;
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 9L;
   }

   public static int getSleepersCount(MemorySegment mem) {
      return getSleepersCount(mem, 0);
   }

   public static int getSleepersCount(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   public static int getAwakeCount(MemorySegment mem) {
      return getAwakeCount(mem, 0);
   }

   public static int getAwakeCount(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 5));
   }

   @Nullable
   public static UUID[] getAwakeSample(MemorySegment mem) {
      return getAwakeSample(mem, 0);
   }

   @Nullable
   public static UUID[] getAwakeSample(MemorySegment mem, int offset) {
      if (!hasAwakeSample(mem, offset)) {
         return null;
      } else {
         int off = offset + 9;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("AwakeSample", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("AwakeSample", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 16L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("AwakeSample", off + lenOffset + len * 16, (int)mem.byteSize());
            } else {
               off += lenOffset;
               UUID[] data = new UUID[len];

               for (int i = 0; i < len; i++) {
                  data[i] = PacketIO.readUUID(mem, off + i * 16);
               }

               return data;
            }
         }
      }
   }

   public static boolean hasAwakeSample(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static SleepMultiplayer toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static SleepMultiplayer toObject(MemorySegment mem, int offset) {
      if (offset + 9 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("SleepMultiplayer", offset + 9, (int)mem.byteSize());
      } else {
         UUID[] awakeSample = null;
         if (hasAwakeSample(mem, offset)) {
            int off = offset + 9;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("AwakeSample", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("AwakeSample", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 16L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("AwakeSample", off + lenOffset + len * 16, (int)mem.byteSize());
            }

            off += lenOffset;
            awakeSample = new UUID[len];

            for (int i = 0; i < len; i++) {
               awakeSample[i] = PacketIO.readUUID(mem, off + i * 16);
            }
         }

         return new SleepMultiplayer(mem.get(PacketIO.PROTO_INT, (long)(offset + 1)), mem.get(PacketIO.PROTO_INT, (long)(offset + 5)), awakeSample);
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.awakeSample != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.sleepersCount);
      buf.writeIntLE(this.awakeCount);
      if (this.awakeSample != null) {
         if (this.awakeSample.length > 4096000) {
            throw ProtocolException.arrayTooLong("AwakeSample", this.awakeSample.length, 4096000);
         }

         VarInt.write(buf, this.awakeSample.length);

         for (UUID item : this.awakeSample) {
            PacketIO.writeUUID(buf, item);
         }
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.awakeSample != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.sleepersCount);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 5), this.awakeCount);
      int varOffset = offset + 9;
      if (this.awakeSample != null) {
         if (this.awakeSample.length > 4096000) {
            throw ProtocolException.arrayTooLong("AwakeSample", this.awakeSample.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.awakeSample.length);

         for (int i = 0; i < this.awakeSample.length; i++) {
            PacketIO.writeUUID(mem, varOffset + i * 16, this.awakeSample[i]);
         }

         varOffset += this.awakeSample.length * 16;
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 9;
      if (this.awakeSample != null) {
         size += VarInt.size(this.awakeSample.length) + this.awakeSample.length * 16;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 9) {
         return ValidationResult.error("Buffer too small: expected at least 9 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 9;
         if ((nullBits & 1) != 0) {
            int awakeSampleCount = VarInt.peek(buffer, pos);
            if (awakeSampleCount < 0) {
               return ValidationResult.error("Invalid array count for AwakeSample");
            }

            if (awakeSampleCount > 4096000) {
               return ValidationResult.error("AwakeSample exceeds max length 4096000");
            }

            pos += VarInt.size(awakeSampleCount);
            pos += awakeSampleCount * 16;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading AwakeSample");
            }
         }

         return ValidationResult.OK;
      }
   }

   public SleepMultiplayer clone() {
      SleepMultiplayer copy = new SleepMultiplayer();
      copy.sleepersCount = this.sleepersCount;
      copy.awakeCount = this.awakeCount;
      copy.awakeSample = this.awakeSample != null ? Arrays.copyOf(this.awakeSample, this.awakeSample.length) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof SleepMultiplayer other)
            ? false
            : this.sleepersCount == other.sleepersCount
               && this.awakeCount == other.awakeCount
               && Arrays.equals((Object[])this.awakeSample, (Object[])other.awakeSample);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Integer.hashCode(this.sleepersCount);
      result = 31 * result + Integer.hashCode(this.awakeCount);
      return 31 * result + Arrays.hashCode((Object[])this.awakeSample);
   }
}
