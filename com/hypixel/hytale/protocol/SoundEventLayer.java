package com.hypixel.hytale.protocol;

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
      if (buf.readableBytes() - offset < 42) {
         throw ProtocolException.bufferTooSmall("SoundEventLayer", 42, buf.readableBytes() - offset);
      } else {
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
               throw ProtocolException.invalidVarInt("Files");
            }

            int filesVarLen = VarInt.size(filesCount);
            if (filesCount > 4096000) {
               throw ProtocolException.arrayTooLong("Files", filesCount, 4096000);
            }

            if (pos + filesVarLen + filesCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Files", pos + filesVarLen + filesCount * 1, buf.readableBytes());
            }

            pos += filesVarLen;
            obj.files = new String[filesCount];

            for (int i = 0; i < filesCount; i++) {
               int strLen = VarInt.peek(buf, pos);
               if (strLen < 0) {
                  throw ProtocolException.invalidVarInt("files[" + i + "]");
               }

               int strVarLen = VarInt.size(strLen);
               if (strLen > 4096000) {
                  throw ProtocolException.stringTooLong("files[" + i + "]", strLen, 4096000);
               }

               if (pos + strVarLen + strLen > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("files[" + i + "]", pos + strVarLen + strLen, buf.readableBytes());
               }

               obj.files[i] = PacketIO.readVarString(buf, pos);
               pos += strVarLen + strLen;
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 42;
      if ((nullBits & 2) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            int sl = VarInt.peek(buf, pos);
            pos += VarInt.size(sl) + sl;
         }
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 42L;
   }

   public static float getVolume(MemorySegment mem) {
      return getVolume(mem, 0);
   }

   public static float getVolume(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1));
   }

   public static float getStartDelay(MemorySegment mem) {
      return getStartDelay(mem, 0);
   }

   public static float getStartDelay(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 5));
   }

   public static boolean getLooping(MemorySegment mem) {
      return getLooping(mem, 0);
   }

   public static boolean getLooping(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 9));
   }

   public static int getProbability(MemorySegment mem) {
      return getProbability(mem, 0);
   }

   public static int getProbability(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 10));
   }

   public static float getProbabilityRerollDelay(MemorySegment mem) {
      return getProbabilityRerollDelay(mem, 0);
   }

   public static float getProbabilityRerollDelay(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 14));
   }

   public static int getRoundRobinHistorySize(MemorySegment mem) {
      return getRoundRobinHistorySize(mem, 0);
   }

   public static int getRoundRobinHistorySize(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 18));
   }

   @Nullable
   public static SoundEventLayerRandomSettings getRandomSettings(MemorySegment mem) {
      return getRandomSettings(mem, 0);
   }

   @Nullable
   public static SoundEventLayerRandomSettings getRandomSettings(MemorySegment mem, int offset) {
      return hasRandomSettings(mem, offset) ? SoundEventLayerRandomSettings.toObject(mem, offset + 22) : null;
   }

   @Nullable
   public static String[] getFiles(MemorySegment mem) {
      return getFiles(mem, 0);
   }

   @Nullable
   public static String[] getFiles(MemorySegment mem, int offset) {
      if (!hasFiles(mem, offset)) {
         return null;
      } else {
         int off = offset + 42;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Files", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Files", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Files", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               String[] data = new String[len];

               for (int i = 0; i < len; i++) {
                  long sp = VarInt.getWithLength(mem, off);
                  int n = (int)sp + (int)(sp >>> 32);
                  data[i] = PacketIO.readVarString("Files", mem, off, 16384000, PacketIO.UTF8);
                  off += n;
               }

               return data;
            }
         }
      }
   }

   public static boolean hasRandomSettings(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasFiles(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static SoundEventLayer toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static SoundEventLayer toObject(MemorySegment mem, int offset) {
      if (offset + 42 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("SoundEventLayer", offset + 42, (int)mem.byteSize());
      } else {
         String[] files = null;
         if (hasFiles(mem, offset)) {
            int off = offset + 42;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Files", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Files", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Files", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            files = new String[len];

            for (int i = 0; i < len; i++) {
               long sp = VarInt.getWithLength(mem, off);
               int n = (int)sp + (int)(sp >>> 32);
               files[i] = PacketIO.readVarString("Files", mem, off, 16384000, PacketIO.UTF8);
               off += n;
            }
         }

         return new SoundEventLayer(
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 5)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 9)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 10)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 14)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 18)),
            hasRandomSettings(mem, offset) ? SoundEventLayerRandomSettings.toObject(mem, offset + 22) : null,
            files
         );
      }
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

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.randomSettings != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.files != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 1), this.volume);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 5), this.startDelay);
      mem.set(PacketIO.PROTO_BOOL, offset + 9, this.looping);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 10), this.probability);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 14), this.probabilityRerollDelay);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 18), this.roundRobinHistorySize);
      if (this.randomSettings != null) {
         this.randomSettings.serialize(mem, offset + 22);
      } else {
         mem.asSlice(offset + 22, 20L).fill((byte)0);
      }

      int varOffset = offset + 42;
      if (this.files != null) {
         if (this.files.length > 4096000) {
            throw ProtocolException.arrayTooLong("Files", this.files.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.files.length);
         int filesValueOffset = 0;

         for (int i = 0; i < this.files.length; i++) {
            filesValueOffset += PacketIO.writeVarString(mem, varOffset + filesValueOffset, this.files[i], 16384000);
         }

         varOffset += filesValueOffset;
      }

      return varOffset - offset;
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

            pos += VarInt.size(filesCount);

            for (int i = 0; i < filesCount; i++) {
               int strLen = VarInt.peek(buffer, pos);
               if (strLen < 0) {
                  return ValidationResult.error("Invalid string length in Files");
               }

               pos += VarInt.size(strLen);
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
