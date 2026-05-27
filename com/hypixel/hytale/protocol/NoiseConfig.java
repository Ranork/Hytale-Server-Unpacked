package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NoiseConfig {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 23;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 23;
   public static final int MAX_SIZE = 23;
   public int seed;
   @Nonnull
   public NoiseType type = NoiseType.Sin;
   public float frequency;
   public float amplitude;
   @Nullable
   public ClampConfig clamp;

   public NoiseConfig() {
   }

   public NoiseConfig(int seed, @Nonnull NoiseType type, float frequency, float amplitude, @Nullable ClampConfig clamp) {
      this.seed = seed;
      this.type = type;
      this.frequency = frequency;
      this.amplitude = amplitude;
      this.clamp = clamp;
   }

   public NoiseConfig(@Nonnull NoiseConfig other) {
      this.seed = other.seed;
      this.type = other.type;
      this.frequency = other.frequency;
      this.amplitude = other.amplitude;
      this.clamp = other.clamp;
   }

   @Nonnull
   public static NoiseConfig deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 23) {
         throw ProtocolException.bufferTooSmall("NoiseConfig", 23, buf.readableBytes() - offset);
      } else {
         NoiseConfig obj = new NoiseConfig();
         byte nullBits = buf.getByte(offset);
         obj.seed = buf.getIntLE(offset + 1);
         obj.type = NoiseType.fromValue(buf.getByte(offset + 5));
         obj.frequency = buf.getFloatLE(offset + 6);
         obj.amplitude = buf.getFloatLE(offset + 10);
         if ((nullBits & 1) != 0) {
            obj.clamp = ClampConfig.deserialize(buf, offset + 14);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 23;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 23L;
   }

   public static int getSeed(MemorySegment mem) {
      return getSeed(mem, 0);
   }

   public static int getSeed(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   public static NoiseType getType(MemorySegment mem) {
      return getType(mem, 0);
   }

   public static NoiseType getType(MemorySegment mem, int offset) {
      return NoiseType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 5)));
   }

   public static float getFrequency(MemorySegment mem) {
      return getFrequency(mem, 0);
   }

   public static float getFrequency(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 6));
   }

   public static float getAmplitude(MemorySegment mem) {
      return getAmplitude(mem, 0);
   }

   public static float getAmplitude(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 10));
   }

   @Nullable
   public static ClampConfig getClamp(MemorySegment mem) {
      return getClamp(mem, 0);
   }

   @Nullable
   public static ClampConfig getClamp(MemorySegment mem, int offset) {
      return hasClamp(mem, offset) ? ClampConfig.toObject(mem, offset + 14) : null;
   }

   public static boolean hasClamp(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static NoiseConfig toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static NoiseConfig toObject(MemorySegment mem, int offset) {
      if (offset + 23 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("NoiseConfig", offset + 23, (int)mem.byteSize());
      } else {
         return new NoiseConfig(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            NoiseType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 5))),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 6)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 10)),
            hasClamp(mem, offset) ? ClampConfig.toObject(mem, offset + 14) : null
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.clamp != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.seed);
      buf.writeByte(this.type.getValue());
      buf.writeFloatLE(this.frequency);
      buf.writeFloatLE(this.amplitude);
      if (this.clamp != null) {
         this.clamp.serialize(buf);
      } else {
         buf.writeZero(9);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.clamp != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.seed);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 5), (byte)this.type.getValue());
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 6), this.frequency);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 10), this.amplitude);
      if (this.clamp != null) {
         this.clamp.serialize(mem, offset + 14);
      } else {
         mem.asSlice(offset + 14, 9L).fill((byte)0);
      }

      return 23;
   }

   public int computeSize() {
      return 23;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 23) {
         return ValidationResult.error("Buffer too small: expected at least 23 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int v = buffer.getByte(offset + 5) & 255;
         return v >= 6 ? ValidationResult.error("Invalid NoiseType value for Type") : ValidationResult.OK;
      }
   }

   public NoiseConfig clone() {
      NoiseConfig copy = new NoiseConfig();
      copy.seed = this.seed;
      copy.type = this.type;
      copy.frequency = this.frequency;
      copy.amplitude = this.amplitude;
      copy.clamp = this.clamp != null ? this.clamp.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof NoiseConfig other)
            ? false
            : this.seed == other.seed
               && Objects.equals(this.type, other.type)
               && this.frequency == other.frequency
               && this.amplitude == other.amplitude
               && Objects.equals(this.clamp, other.clamp);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.seed, this.type, this.frequency, this.amplitude, this.clamp);
   }
}
