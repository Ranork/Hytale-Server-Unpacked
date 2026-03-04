package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SoundEventLayer {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 42;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 42;
   public static final int MAX_SIZE = 1677721600;
   public float volume;
   public float startDelay;
   public boolean looping;
   public int probability;
   public float probabilityRerollDelay;
   public int roundRobinHistorySize;
   @Nullable
   public SoundEventLayerRandomSettings randomSettings;
   @Nullable
   public String[] files;

   public SoundEventLayer() {
   }

   public SoundEventLayer(
      float volume,
      float startDelay,
      boolean looping,
      int probability,
      float probabilityRerollDelay,
      int roundRobinHistorySize,
      @Nullable SoundEventLayerRandomSettings randomSettings,
      @Nullable String[] files
   ) {
      this.volume = volume;
      this.startDelay = startDelay;
      this.looping = looping;
      this.probability = probability;
      this.probabilityRerollDelay = probabilityRerollDelay;
      this.roundRobinHistorySize = roundRobinHistorySize;
      this.randomSettings = randomSettings;
      this.files = files;
   }

   public SoundEventLayer(@Nonnull SoundEventLayer other) {
      this.volume = other.volume;
      this.startDelay = other.startDelay;
      this.looping = other.looping;
      this.probability = other.probability;
      this.probabilityRerollDelay = other.probabilityRerollDelay;
      this.roundRobinHistorySize = other.roundRobinHistorySize;
      this.randomSettings = other.randomSettings;
      this.files = other.files;
   }

   @Nonnull
   public static SoundEventLayer deserialize(@Nonnull ByteBuf buf, int offset) {
      SoundEventLayer obj = new SoundEventLayer();
      byte nullBits = buf.getByte(offset);
      obj.volume = buf.getFloatLE(offset + 1);
      obj.startDelay = buf.getFloatLE(offset + 5);
      obj.looping = buf.getByte(offset + 9) != 0;
      obj.probability = buf.getIntLE(offset + 10);
      obj.probabilityRerollDelay = buf.getFloatLE(offset + 14);
      obj.roundRobinHistorySize = buf.getIntLE(offset + 18);
      if ((nullBits & 1) != 0) {
         obj.randomSettings = SoundEventLayerRandomSettings.deserialize(buf, offset + 22);
      }

      int pos = offset + 42;
      if ((nullBits & 2) != 0) {
         int filesCount = VarInt.peek(buf, pos);
         if (filesCount < 0) {
            throw ProtocolException.negativeLength("Files", filesCount);
         }

         if (filesCount > 4096000) {
            throw ProtocolException.arrayTooLong("Files", filesCount, 4096000);
         }

         int filesVarLen = VarInt.size(filesCount);
         if (pos + filesVarLen + filesCount * 1L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Files", pos + filesVarLen + filesCount * 1, buf.readableBytes());
         }

         pos += filesVarLen;
         obj.files = new String[filesCount];

         for (int i = 0; i < filesCount; i++) {
            int strLen = VarInt.peek(buf, pos);
            if (strLen < 0) {
               throw ProtocolException.negativeLength("files[" + i + "]", strLen);
            }

            if (strLen > 4096000) {
               throw ProtocolException.stringTooLong("files[" + i + "]", strLen, 4096000);
            }

            int strVarLen = VarInt.length(buf, pos);
            obj.files[i] = PacketIO.readVarString(buf, pos);
            pos += strVarLen + strLen;
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 42;
      if ((nullBits & 2) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos);

         for (int i = 0; i < arrLen; i++) {
            int sl = VarInt.peek(buf, pos);
            pos += VarInt.length(buf, pos) + sl;
         }
      }

      return pos - offset;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.randomSettings != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.files != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeFloatLE(this.volume);
      buf.writeFloatLE(this.startDelay);
      buf.writeByte(this.looping ? 1 : 0);
      buf.writeIntLE(this.probability);
      buf.writeFloatLE(this.probabilityRerollDelay);
      buf.writeIntLE(this.roundRobinHistorySize);
      if (this.randomSettings != null) {
         this.randomSettings.serialize(buf);
      } else {
         buf.writeZero(20);
      }

      if (this.files != null) {
         if (this.files.length > 4096000) {
            throw ProtocolException.arrayTooLong("Files", this.files.length, 4096000);
         }

         VarInt.write(buf, this.files.length);

         for (String item : this.files) {
            PacketIO.writeVarString(buf, item, 4096000);
         }
      }
   }

   public int computeSize() {
      int size = 42;
      if (this.files != null) {
         int filesSize = 0;

         for (String elem : this.files) {
            filesSize += PacketIO.stringSize(elem);
         }

         size += VarInt.size(this.files.length) + filesSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 42) {
         return ValidationResult.error("Buffer too small: expected at least 42 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 42;
         if ((nullBits & 2) != 0) {
            int filesCount = VarInt.peek(buffer, pos);
            if (filesCount < 0) {
               return ValidationResult.error("Invalid array count for Files");
            }

            if (filesCount > 4096000) {
               return ValidationResult.error("Files exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for (int i = 0; i < filesCount; i++) {
               int strLen = VarInt.peek(buffer, pos);
               if (strLen < 0) {
                  return ValidationResult.error("Invalid string length in Files");
               }

               pos += VarInt.length(buffer, pos);
               pos += strLen;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading string in Files");
               }
            }
         }

         return ValidationResult.OK;
      }
   }

   public SoundEventLayer clone() {
      SoundEventLayer copy = new SoundEventLayer();
      copy.volume = this.volume;
      copy.startDelay = this.startDelay;
      copy.looping = this.looping;
      copy.probability = this.probability;
      copy.probabilityRerollDelay = this.probabilityRerollDelay;
      copy.roundRobinHistorySize = this.roundRobinHistorySize;
      copy.randomSettings = this.randomSettings != null ? this.randomSettings.clone() : null;
      copy.files = this.files != null ? Arrays.copyOf(this.files, this.files.length) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof SoundEventLayer other)
            ? false
            : this.volume == other.volume
               && this.startDelay == other.startDelay
               && this.looping == other.looping
               && this.probability == other.probability
               && this.probabilityRerollDelay == other.probabilityRerollDelay
               && this.roundRobinHistorySize == other.roundRobinHistorySize
               && Objects.equals(this.randomSettings, other.randomSettings)
               && Arrays.equals((Object[])this.files, (Object[])other.files);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Float.hashCode(this.volume);
      result = 31 * result + Float.hashCode(this.startDelay);
      result = 31 * result + Boolean.hashCode(this.looping);
      result = 31 * result + Integer.hashCode(this.probability);
      result = 31 * result + Float.hashCode(this.probabilityRerollDelay);
      result = 31 * result + Integer.hashCode(this.roundRobinHistorySize);
      result = 31 * result + Objects.hashCode(this.randomSettings);
      return 31 * result + Arrays.hashCode((Object[])this.files);
   }
}
