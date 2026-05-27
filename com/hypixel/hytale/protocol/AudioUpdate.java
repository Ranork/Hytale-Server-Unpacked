package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class AudioUpdate extends ComponentUpdate {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 0;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 0;
   public static final int MAX_SIZE = 16384005;
   @Nonnull
   public int[] soundEventIds = new int[0];

   public AudioUpdate() {
   }

   public AudioUpdate(@Nonnull int[] soundEventIds) {
      this.soundEventIds = soundEventIds;
   }

   public AudioUpdate(@Nonnull AudioUpdate other) {
      this.soundEventIds = other.soundEventIds;
   }

   @Nonnull
   public static AudioUpdate deserialize(@Nonnull ByteBuf buf, int offset) {
      AudioUpdate obj = new AudioUpdate();
      int pos = offset + 0;
      int soundEventIdsCount = VarInt.peek(buf, pos);
      if (soundEventIdsCount < 0) {
         throw ProtocolException.invalidVarInt("SoundEventIds");
      } else {
         int soundEventIdsVarLen = VarInt.size(soundEventIdsCount);
         if (soundEventIdsCount > 4096000) {
            throw ProtocolException.arrayTooLong("SoundEventIds", soundEventIdsCount, 4096000);
         } else if (pos + soundEventIdsVarLen + soundEventIdsCount * 4L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("SoundEventIds", pos + soundEventIdsVarLen + soundEventIdsCount * 4, buf.readableBytes());
         } else {
            pos += soundEventIdsVarLen;
            obj.soundEventIds = new int[soundEventIdsCount];

            for (int i = 0; i < soundEventIdsCount; i++) {
               obj.soundEventIds[i] = buf.getIntLE(pos + i * 4);
            }

            pos += soundEventIdsCount * 4;
            return obj;
         }
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      int pos = offset + 0;
      int arrLen = VarInt.peek(buf, pos);
      pos += VarInt.size(arrLen) + arrLen * 4;
      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 0L;
   }

   public static int[] getSoundEventIds(MemorySegment mem) {
      return getSoundEventIds(mem, 0);
   }

   public static int[] getSoundEventIds(MemorySegment mem, int offset) {
      int off = offset + 0;
      long packed = VarInt.getWithLength(mem, off);
      int len = (int)packed;
      if (len < 0) {
         throw ProtocolException.negativeLength("SoundEventIds", len);
      } else if (len > 4096000) {
         throw ProtocolException.arrayTooLong("SoundEventIds", len, 4096000);
      } else {
         int lenOffset = (int)(packed >>> 32);
         if (off + lenOffset + len * 4L > mem.byteSize()) {
            throw ProtocolException.bufferTooSmall("SoundEventIds", off + lenOffset + len * 4, (int)mem.byteSize());
         } else {
            off += lenOffset;
            int[] data = new int[len];
            MemorySegment.copy(mem, PacketIO.PROTO_INT, off, data, 0, len);
            return data;
         }
      }
   }

   public static AudioUpdate toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AudioUpdate toObject(MemorySegment mem, int offset) {
      if (offset + 0 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AudioUpdate", offset + 0, (int)mem.byteSize());
      } else {
         int off = offset + 0;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("SoundEventIds", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("SoundEventIds", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 4L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("SoundEventIds", off + lenOffset + len * 4, (int)mem.byteSize());
            } else {
               off += lenOffset;
               int[] soundEventIds = new int[len];
               MemorySegment.copy(mem, PacketIO.PROTO_INT, off, soundEventIds, 0, len);
               return new AudioUpdate(soundEventIds);
            }
         }
      }
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      if (this.soundEventIds.length > 4096000) {
         throw ProtocolException.arrayTooLong("SoundEventIds", this.soundEventIds.length, 4096000);
      } else {
         VarInt.write(buf, this.soundEventIds.length);

         for (int item : this.soundEventIds) {
            buf.writeIntLE(item);
         }

         return buf.writerIndex() - startPos;
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      int varOffset = offset + 0;
      if (this.soundEventIds.length > 4096000) {
         throw ProtocolException.arrayTooLong("SoundEventIds", this.soundEventIds.length, 4096000);
      } else {
         varOffset += VarInt.set(mem, varOffset, this.soundEventIds.length);
         MemorySegment.copy(this.soundEventIds, 0, mem, PacketIO.PROTO_INT, varOffset, this.soundEventIds.length);
         varOffset += this.soundEventIds.length * 4;
         return varOffset - offset;
      }
   }

   @Override
   public int computeSize() {
      int size = 0;
      return size + VarInt.size(this.soundEventIds.length) + this.soundEventIds.length * 4;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 0) {
         return ValidationResult.error("Buffer too small: expected at least 0 bytes");
      } else {
         int pos = offset + 0;
         int soundEventIdsCount = VarInt.peek(buffer, pos);
         if (soundEventIdsCount < 0) {
            return ValidationResult.error("Invalid array count for SoundEventIds");
         } else if (soundEventIdsCount > 4096000) {
            return ValidationResult.error("SoundEventIds exceeds max length 4096000");
         } else {
            pos += VarInt.size(soundEventIdsCount);
            pos += soundEventIdsCount * 4;
            return pos > buffer.writerIndex() ? ValidationResult.error("Buffer overflow reading SoundEventIds") : ValidationResult.OK;
         }
      }
   }

   public AudioUpdate clone() {
      AudioUpdate copy = new AudioUpdate();
      copy.soundEventIds = Arrays.copyOf(this.soundEventIds, this.soundEventIds.length);
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof AudioUpdate other ? Arrays.equals(this.soundEventIds, other.soundEventIds) : false;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      return 31 * result + Arrays.hashCode(this.soundEventIds);
   }
}
