package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AmbienceFXBlockSoundSet {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 13;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 13;
   public static final int MAX_SIZE = 13;
   public int blockSoundSetIndex;
   @Nullable
   public Rangef percent;

   public AmbienceFXBlockSoundSet() {
   }

   public AmbienceFXBlockSoundSet(int blockSoundSetIndex, @Nullable Rangef percent) {
      this.blockSoundSetIndex = blockSoundSetIndex;
      this.percent = percent;
   }

   public AmbienceFXBlockSoundSet(@Nonnull AmbienceFXBlockSoundSet other) {
      this.blockSoundSetIndex = other.blockSoundSetIndex;
      this.percent = other.percent;
   }

   @Nonnull
   public static AmbienceFXBlockSoundSet deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 13) {
         throw ProtocolException.bufferTooSmall("AmbienceFXBlockSoundSet", 13, buf.readableBytes() - offset);
      } else {
         AmbienceFXBlockSoundSet obj = new AmbienceFXBlockSoundSet();
         byte nullBits = buf.getByte(offset);
         obj.blockSoundSetIndex = buf.getIntLE(offset + 1);
         if ((nullBits & 1) != 0) {
            obj.percent = Rangef.deserialize(buf, offset + 5);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 13;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 13L;
   }

   public static int getBlockSoundSetIndex(MemorySegment mem) {
      return getBlockSoundSetIndex(mem, 0);
   }

   public static int getBlockSoundSetIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   @Nullable
   public static Rangef getPercent(MemorySegment mem) {
      return getPercent(mem, 0);
   }

   @Nullable
   public static Rangef getPercent(MemorySegment mem, int offset) {
      return hasPercent(mem, offset) ? Rangef.toObject(mem, offset + 5) : null;
   }

   public static boolean hasPercent(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static AmbienceFXBlockSoundSet toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AmbienceFXBlockSoundSet toObject(MemorySegment mem, int offset) {
      if (offset + 13 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AmbienceFXBlockSoundSet", offset + 13, (int)mem.byteSize());
      } else {
         return new AmbienceFXBlockSoundSet(mem.get(PacketIO.PROTO_INT, (long)(offset + 1)), hasPercent(mem, offset) ? Rangef.toObject(mem, offset + 5) : null);
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.percent != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.blockSoundSetIndex);
      if (this.percent != null) {
         this.percent.serialize(buf);
      } else {
         buf.writeZero(8);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.percent != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.blockSoundSetIndex);
      if (this.percent != null) {
         this.percent.serialize(mem, offset + 5);
      } else {
         mem.asSlice(offset + 5, 8L).fill((byte)0);
      }

      return 13;
   }

   public int computeSize() {
      return 13;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 13) {
         return ValidationResult.error("Buffer too small: expected at least 13 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         return ValidationResult.OK;
      }
   }

   public AmbienceFXBlockSoundSet clone() {
      AmbienceFXBlockSoundSet copy = new AmbienceFXBlockSoundSet();
      copy.blockSoundSetIndex = this.blockSoundSetIndex;
      copy.percent = this.percent != null ? this.percent.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AmbienceFXBlockSoundSet other)
            ? false
            : this.blockSoundSetIndex == other.blockSoundSetIndex && Objects.equals(this.percent, other.percent);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.blockSoundSetIndex, this.percent);
   }
}
