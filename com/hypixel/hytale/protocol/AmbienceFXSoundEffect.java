package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class AmbienceFXSoundEffect {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 9;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 9;
   public int reverbEffectIndex;
   public int equalizerEffectIndex;
   public boolean isInstant;

   public AmbienceFXSoundEffect() {
   }

   public AmbienceFXSoundEffect(int reverbEffectIndex, int equalizerEffectIndex, boolean isInstant) {
      this.reverbEffectIndex = reverbEffectIndex;
      this.equalizerEffectIndex = equalizerEffectIndex;
      this.isInstant = isInstant;
   }

   public AmbienceFXSoundEffect(@Nonnull AmbienceFXSoundEffect other) {
      this.reverbEffectIndex = other.reverbEffectIndex;
      this.equalizerEffectIndex = other.equalizerEffectIndex;
      this.isInstant = other.isInstant;
   }

   @Nonnull
   public static AmbienceFXSoundEffect deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 9) {
         throw ProtocolException.bufferTooSmall("AmbienceFXSoundEffect", 9, buf.readableBytes() - offset);
      } else {
         AmbienceFXSoundEffect obj = new AmbienceFXSoundEffect();
         obj.reverbEffectIndex = buf.getIntLE(offset + 0);
         obj.equalizerEffectIndex = buf.getIntLE(offset + 4);
         obj.isInstant = buf.getByte(offset + 8) != 0;
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 9;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 9L;
   }

   public static int getReverbEffectIndex(MemorySegment mem) {
      return getReverbEffectIndex(mem, 0);
   }

   public static int getReverbEffectIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 0));
   }

   public static int getEqualizerEffectIndex(MemorySegment mem) {
      return getEqualizerEffectIndex(mem, 0);
   }

   public static int getEqualizerEffectIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 4));
   }

   public static boolean getIsInstant(MemorySegment mem) {
      return getIsInstant(mem, 0);
   }

   public static boolean getIsInstant(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 8));
   }

   public static AmbienceFXSoundEffect toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AmbienceFXSoundEffect toObject(MemorySegment mem, int offset) {
      if (offset + 9 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AmbienceFXSoundEffect", offset + 9, (int)mem.byteSize());
      } else {
         return new AmbienceFXSoundEffect(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 0)), mem.get(PacketIO.PROTO_INT, (long)(offset + 4)), mem.get(PacketIO.PROTO_BOOL, (long)(offset + 8))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.reverbEffectIndex);
      buf.writeIntLE(this.equalizerEffectIndex);
      buf.writeByte(this.isInstant ? 1 : 0);
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_INT, (long)(offset + 0), this.reverbEffectIndex);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 4), this.equalizerEffectIndex);
      mem.set(PacketIO.PROTO_BOOL, offset + 8, this.isInstant);
      return 9;
   }

   public int computeSize() {
      return 9;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 9 ? ValidationResult.error("Buffer too small: expected at least 9 bytes") : ValidationResult.OK;
   }

   public AmbienceFXSoundEffect clone() {
      AmbienceFXSoundEffect copy = new AmbienceFXSoundEffect();
      copy.reverbEffectIndex = this.reverbEffectIndex;
      copy.equalizerEffectIndex = this.equalizerEffectIndex;
      copy.isInstant = this.isInstant;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AmbienceFXSoundEffect other)
            ? false
            : this.reverbEffectIndex == other.reverbEffectIndex && this.equalizerEffectIndex == other.equalizerEffectIndex && this.isInstant == other.isInstant;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.reverbEffectIndex, this.equalizerEffectIndex, this.isInstant);
   }
}
