package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class ConditionalBlockSound {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 8;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 8;
   public static final int MAX_SIZE = 8;
   public int soundEventIndex;
   public int ambienceFXIndex;

   public ConditionalBlockSound() {
   }

   public ConditionalBlockSound(int soundEventIndex, int ambienceFXIndex) {
      this.soundEventIndex = soundEventIndex;
      this.ambienceFXIndex = ambienceFXIndex;
   }

   public ConditionalBlockSound(@Nonnull ConditionalBlockSound other) {
      this.soundEventIndex = other.soundEventIndex;
      this.ambienceFXIndex = other.ambienceFXIndex;
   }

   @Nonnull
   public static ConditionalBlockSound deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 8) {
         throw ProtocolException.bufferTooSmall("ConditionalBlockSound", 8, buf.readableBytes() - offset);
      } else {
         ConditionalBlockSound obj = new ConditionalBlockSound();
         obj.soundEventIndex = buf.getIntLE(offset + 0);
         obj.ambienceFXIndex = buf.getIntLE(offset + 4);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 8;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 8L;
   }

   public static int getSoundEventIndex(MemorySegment mem) {
      return getSoundEventIndex(mem, 0);
   }

   public static int getSoundEventIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 0));
   }

   public static int getAmbienceFXIndex(MemorySegment mem) {
      return getAmbienceFXIndex(mem, 0);
   }

   public static int getAmbienceFXIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 4));
   }

   public static ConditionalBlockSound toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ConditionalBlockSound toObject(MemorySegment mem, int offset) {
      if (offset + 8 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ConditionalBlockSound", offset + 8, (int)mem.byteSize());
      } else {
         return new ConditionalBlockSound(mem.get(PacketIO.PROTO_INT, (long)(offset + 0)), mem.get(PacketIO.PROTO_INT, (long)(offset + 4)));
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.soundEventIndex);
      buf.writeIntLE(this.ambienceFXIndex);
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_INT, (long)(offset + 0), this.soundEventIndex);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 4), this.ambienceFXIndex);
      return 8;
   }

   public int computeSize() {
      return 8;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 8 ? ValidationResult.error("Buffer too small: expected at least 8 bytes") : ValidationResult.OK;
   }

   public ConditionalBlockSound clone() {
      ConditionalBlockSound copy = new ConditionalBlockSound();
      copy.soundEventIndex = this.soundEventIndex;
      copy.ambienceFXIndex = this.ambienceFXIndex;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ConditionalBlockSound other)
            ? false
            : this.soundEventIndex == other.soundEventIndex && this.ambienceFXIndex == other.ambienceFXIndex;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.soundEventIndex, this.ambienceFXIndex);
   }
}
