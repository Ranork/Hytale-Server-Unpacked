package com.hypixel.hytale.protocol.packets.world;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
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
      SleepMultiplayer obj = new SleepMultiplayer();
      byte nullBits = buf.getByte(offset);
      obj.sleepersCount = buf.getIntLE(offset + 1);
      obj.awakeCount = buf.getIntLE(offset + 5);
      int pos = offset + 9;
      if ((nullBits & 1) != 0) {
         int awakeSampleCount = VarInt.peek(buf, pos);
         if (awakeSampleCount < 0) {
            throw ProtocolException.negativeLength("AwakeSample", awakeSampleCount);
         }

         if (awakeSampleCount > 4096000) {
            throw ProtocolException.arrayTooLong("AwakeSample", awakeSampleCount, 4096000);
         }

         int awakeSampleVarLen = VarInt.size(awakeSampleCount);
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

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 9;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos) + arrLen * 16;
      }

      return pos - offset;
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

            pos += VarInt.length(buffer, pos);
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
