package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Bench {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public BenchTierLevel[] benchTierLevels;

   public Bench() {
   }

   public Bench(@Nullable BenchTierLevel[] benchTierLevels) {
      this.benchTierLevels = benchTierLevels;
   }

   public Bench(@Nonnull Bench other) {
      this.benchTierLevels = other.benchTierLevels;
   }

   @Nonnull
   public static Bench deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 1) {
         throw ProtocolException.bufferTooSmall("Bench", 1, buf.readableBytes() - offset);
      } else {
         Bench obj = new Bench();
         byte nullBits = buf.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            int benchTierLevelsCount = VarInt.peek(buf, pos);
            if (benchTierLevelsCount < 0) {
               throw ProtocolException.invalidVarInt("BenchTierLevels");
            }

            int benchTierLevelsVarLen = VarInt.size(benchTierLevelsCount);
            if (benchTierLevelsCount > 4096000) {
               throw ProtocolException.arrayTooLong("BenchTierLevels", benchTierLevelsCount, 4096000);
            }

            if (pos + benchTierLevelsVarLen + benchTierLevelsCount * 17L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("BenchTierLevels", pos + benchTierLevelsVarLen + benchTierLevelsCount * 17, buf.readableBytes());
            }

            pos += benchTierLevelsVarLen;
            obj.benchTierLevels = new BenchTierLevel[benchTierLevelsCount];

            for (int i = 0; i < benchTierLevelsCount; i++) {
               obj.benchTierLevels[i] = BenchTierLevel.deserialize(buf, pos);
               pos += BenchTierLevel.computeBytesConsumed(buf, pos);
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos += BenchTierLevel.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 1L;
   }

   @Nullable
   public static BenchTierLevel[] getBenchTierLevels(MemorySegment mem) {
      return getBenchTierLevels(mem, 0);
   }

   @Nullable
   public static BenchTierLevel[] getBenchTierLevels(MemorySegment mem, int offset) {
      if (!hasBenchTierLevels(mem, offset)) {
         return null;
      } else {
         int off = offset + 1;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("BenchTierLevels", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("BenchTierLevels", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("BenchTierLevels", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               BenchTierLevel[] data = new BenchTierLevel[len];

               for (int i = 0; i < len; i++) {
                  data[i] = BenchTierLevel.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   public static boolean hasBenchTierLevels(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static Bench toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static Bench toObject(MemorySegment mem, int offset) {
      if (offset + 1 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("Bench", offset + 1, (int)mem.byteSize());
      } else {
         BenchTierLevel[] benchTierLevels = null;
         if (hasBenchTierLevels(mem, offset)) {
            int off = offset + 1;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("BenchTierLevels", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("BenchTierLevels", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("BenchTierLevels", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            benchTierLevels = new BenchTierLevel[len];

            for (int i = 0; i < len; i++) {
               benchTierLevels[i] = BenchTierLevel.toObject(mem, off);
               off += benchTierLevels[i].computeSize();
            }
         }

         return new Bench(benchTierLevels);
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.benchTierLevels != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.benchTierLevels != null) {
         if (this.benchTierLevels.length > 4096000) {
            throw ProtocolException.arrayTooLong("BenchTierLevels", this.benchTierLevels.length, 4096000);
         }

         VarInt.write(buf, this.benchTierLevels.length);

         for (BenchTierLevel item : this.benchTierLevels) {
            item.serialize(buf);
         }
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.benchTierLevels != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 1;
      if (this.benchTierLevels != null) {
         if (this.benchTierLevels.length > 4096000) {
            throw ProtocolException.arrayTooLong("BenchTierLevels", this.benchTierLevels.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.benchTierLevels.length);
         int benchTierLevelsValueOffset = 0;

         for (int i = 0; i < this.benchTierLevels.length; i++) {
            benchTierLevelsValueOffset += this.benchTierLevels[i].serialize(mem, varOffset + benchTierLevelsValueOffset);
         }

         varOffset += benchTierLevelsValueOffset;
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 1;
      if (this.benchTierLevels != null) {
         int benchTierLevelsSize = 0;

         for (BenchTierLevel elem : this.benchTierLevels) {
            benchTierLevelsSize += elem.computeSize();
         }

         size += VarInt.size(this.benchTierLevels.length) + benchTierLevelsSize;
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
            int benchTierLevelsCount = VarInt.peek(buffer, pos);
            if (benchTierLevelsCount < 0) {
               return ValidationResult.error("Invalid array count for BenchTierLevels");
            }

            if (benchTierLevelsCount > 4096000) {
               return ValidationResult.error("BenchTierLevels exceeds max length 4096000");
            }

            pos += VarInt.size(benchTierLevelsCount);

            for (int i = 0; i < benchTierLevelsCount; i++) {
               ValidationResult structResult = BenchTierLevel.validateStructure(buffer, pos);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid BenchTierLevel in BenchTierLevels[" + i + "]: " + structResult.error());
               }

               pos += BenchTierLevel.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
      }
   }

   public Bench clone() {
      Bench copy = new Bench();
      copy.benchTierLevels = this.benchTierLevels != null ? Arrays.stream(this.benchTierLevels).map(e -> e.clone()).toArray(BenchTierLevel[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof Bench other ? Arrays.equals((Object[])this.benchTierLevels, (Object[])other.benchTierLevels) : false;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      return 31 * result + Arrays.hashCode((Object[])this.benchTierLevels);
   }
}
