package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ReverbEffect {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 54;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 54;
   public static final int MAX_SIZE = 16384059;
   @Nullable
   public String id;
   public float dryGain;
   public float modalDensity;
   public float diffusion;
   public float gain;
   public float highFrequencyGain;
   public float decayTime;
   public float highFrequencyDecayRatio;
   public float reflectionGain;
   public float reflectionDelay;
   public float lateReverbGain;
   public float lateReverbDelay;
   public float roomRolloffFactor;
   public float airAbsorptionHighFrequencyGain;
   public boolean limitDecayHighFrequency;

   public ReverbEffect() {
   }

   public ReverbEffect(
      @Nullable String id,
      float dryGain,
      float modalDensity,
      float diffusion,
      float gain,
      float highFrequencyGain,
      float decayTime,
      float highFrequencyDecayRatio,
      float reflectionGain,
      float reflectionDelay,
      float lateReverbGain,
      float lateReverbDelay,
      float roomRolloffFactor,
      float airAbsorptionHighFrequencyGain,
      boolean limitDecayHighFrequency
   ) {
      this.id = id;
      this.dryGain = dryGain;
      this.modalDensity = modalDensity;
      this.diffusion = diffusion;
      this.gain = gain;
      this.highFrequencyGain = highFrequencyGain;
      this.decayTime = decayTime;
      this.highFrequencyDecayRatio = highFrequencyDecayRatio;
      this.reflectionGain = reflectionGain;
      this.reflectionDelay = reflectionDelay;
      this.lateReverbGain = lateReverbGain;
      this.lateReverbDelay = lateReverbDelay;
      this.roomRolloffFactor = roomRolloffFactor;
      this.airAbsorptionHighFrequencyGain = airAbsorptionHighFrequencyGain;
      this.limitDecayHighFrequency = limitDecayHighFrequency;
   }

   public ReverbEffect(@Nonnull ReverbEffect other) {
      this.id = other.id;
      this.dryGain = other.dryGain;
      this.modalDensity = other.modalDensity;
      this.diffusion = other.diffusion;
      this.gain = other.gain;
      this.highFrequencyGain = other.highFrequencyGain;
      this.decayTime = other.decayTime;
      this.highFrequencyDecayRatio = other.highFrequencyDecayRatio;
      this.reflectionGain = other.reflectionGain;
      this.reflectionDelay = other.reflectionDelay;
      this.lateReverbGain = other.lateReverbGain;
      this.lateReverbDelay = other.lateReverbDelay;
      this.roomRolloffFactor = other.roomRolloffFactor;
      this.airAbsorptionHighFrequencyGain = other.airAbsorptionHighFrequencyGain;
      this.limitDecayHighFrequency = other.limitDecayHighFrequency;
   }

   @Nonnull
   public static ReverbEffect deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 54) {
         throw ProtocolException.bufferTooSmall("ReverbEffect", 54, buf.readableBytes() - offset);
      } else {
         ReverbEffect obj = new ReverbEffect();
         byte nullBits = buf.getByte(offset);
         obj.dryGain = buf.getFloatLE(offset + 1);
         obj.modalDensity = buf.getFloatLE(offset + 5);
         obj.diffusion = buf.getFloatLE(offset + 9);
         obj.gain = buf.getFloatLE(offset + 13);
         obj.highFrequencyGain = buf.getFloatLE(offset + 17);
         obj.decayTime = buf.getFloatLE(offset + 21);
         obj.highFrequencyDecayRatio = buf.getFloatLE(offset + 25);
         obj.reflectionGain = buf.getFloatLE(offset + 29);
         obj.reflectionDelay = buf.getFloatLE(offset + 33);
         obj.lateReverbGain = buf.getFloatLE(offset + 37);
         obj.lateReverbDelay = buf.getFloatLE(offset + 41);
         obj.roomRolloffFactor = buf.getFloatLE(offset + 45);
         obj.airAbsorptionHighFrequencyGain = buf.getFloatLE(offset + 49);
         obj.limitDecayHighFrequency = buf.getByte(offset + 53) != 0;
         int pos = offset + 54;
         if ((nullBits & 1) != 0) {
            int idLen = VarInt.peek(buf, pos);
            if (idLen < 0) {
               throw ProtocolException.invalidVarInt("Id");
            }

            int idVarLen = VarInt.size(idLen);
            if (idLen > 4096000) {
               throw ProtocolException.stringTooLong("Id", idLen, 4096000);
            }

            if (pos + idVarLen + idLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Id", pos + idVarLen + idLen, buf.readableBytes());
            }

            obj.id = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
            pos += idVarLen + idLen;
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 54;
      if ((nullBits & 1) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.size(sl) + sl;
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 54L;
   }

   @Nullable
   public static String getId(MemorySegment mem) {
      return getId(mem, 0);
   }

   @Nullable
   public static String getId(MemorySegment mem, int offset) {
      return hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + 54, 4096000, PacketIO.UTF8) : null;
   }

   public static float getDryGain(MemorySegment mem) {
      return getDryGain(mem, 0);
   }

   public static float getDryGain(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1));
   }

   public static float getModalDensity(MemorySegment mem) {
      return getModalDensity(mem, 0);
   }

   public static float getModalDensity(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 5));
   }

   public static float getDiffusion(MemorySegment mem) {
      return getDiffusion(mem, 0);
   }

   public static float getDiffusion(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 9));
   }

   public static float getGain(MemorySegment mem) {
      return getGain(mem, 0);
   }

   public static float getGain(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 13));
   }

   public static float getHighFrequencyGain(MemorySegment mem) {
      return getHighFrequencyGain(mem, 0);
   }

   public static float getHighFrequencyGain(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 17));
   }

   public static float getDecayTime(MemorySegment mem) {
      return getDecayTime(mem, 0);
   }

   public static float getDecayTime(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 21));
   }

   public static float getHighFrequencyDecayRatio(MemorySegment mem) {
      return getHighFrequencyDecayRatio(mem, 0);
   }

   public static float getHighFrequencyDecayRatio(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 25));
   }

   public static float getReflectionGain(MemorySegment mem) {
      return getReflectionGain(mem, 0);
   }

   public static float getReflectionGain(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 29));
   }

   public static float getReflectionDelay(MemorySegment mem) {
      return getReflectionDelay(mem, 0);
   }

   public static float getReflectionDelay(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 33));
   }

   public static float getLateReverbGain(MemorySegment mem) {
      return getLateReverbGain(mem, 0);
   }

   public static float getLateReverbGain(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 37));
   }

   public static float getLateReverbDelay(MemorySegment mem) {
      return getLateReverbDelay(mem, 0);
   }

   public static float getLateReverbDelay(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 41));
   }

   public static float getRoomRolloffFactor(MemorySegment mem) {
      return getRoomRolloffFactor(mem, 0);
   }

   public static float getRoomRolloffFactor(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 45));
   }

   public static float getAirAbsorptionHighFrequencyGain(MemorySegment mem) {
      return getAirAbsorptionHighFrequencyGain(mem, 0);
   }

   public static float getAirAbsorptionHighFrequencyGain(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 49));
   }

   public static boolean getLimitDecayHighFrequency(MemorySegment mem) {
      return getLimitDecayHighFrequency(mem, 0);
   }

   public static boolean getLimitDecayHighFrequency(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 53));
   }

   public static boolean hasId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static ReverbEffect toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ReverbEffect toObject(MemorySegment mem, int offset) {
      if (offset + 54 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ReverbEffect", offset + 54, (int)mem.byteSize());
      } else {
         return new ReverbEffect(
            hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + 54, 4096000, PacketIO.UTF8) : null,
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 5)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 9)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 13)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 17)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 21)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 25)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 29)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 33)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 37)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 41)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 45)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 49)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 53))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.id != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeFloatLE(this.dryGain);
      buf.writeFloatLE(this.modalDensity);
      buf.writeFloatLE(this.diffusion);
      buf.writeFloatLE(this.gain);
      buf.writeFloatLE(this.highFrequencyGain);
      buf.writeFloatLE(this.decayTime);
      buf.writeFloatLE(this.highFrequencyDecayRatio);
      buf.writeFloatLE(this.reflectionGain);
      buf.writeFloatLE(this.reflectionDelay);
      buf.writeFloatLE(this.lateReverbGain);
      buf.writeFloatLE(this.lateReverbDelay);
      buf.writeFloatLE(this.roomRolloffFactor);
      buf.writeFloatLE(this.airAbsorptionHighFrequencyGain);
      buf.writeByte(this.limitDecayHighFrequency ? 1 : 0);
      if (this.id != null) {
         PacketIO.writeVarString(buf, this.id, 4096000);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.id != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 1), this.dryGain);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 5), this.modalDensity);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 9), this.diffusion);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 13), this.gain);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 17), this.highFrequencyGain);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 21), this.decayTime);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 25), this.highFrequencyDecayRatio);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 29), this.reflectionGain);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 33), this.reflectionDelay);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 37), this.lateReverbGain);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 41), this.lateReverbDelay);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 45), this.roomRolloffFactor);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 49), this.airAbsorptionHighFrequencyGain);
      mem.set(PacketIO.PROTO_BOOL, offset + 53, this.limitDecayHighFrequency);
      int varOffset = offset + 54;
      if (this.id != null) {
         varOffset += PacketIO.writeVarString(mem, varOffset, this.id, 4096000);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 54;
      if (this.id != null) {
         size += PacketIO.stringSize(this.id);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 54) {
         return ValidationResult.error("Buffer too small: expected at least 54 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 54;
         if ((nullBits & 1) != 0) {
            int idLen = VarInt.peek(buffer, pos);
            if (idLen < 0) {
               return ValidationResult.error("Invalid string length for Id");
            }

            if (idLen > 4096000) {
               return ValidationResult.error("Id exceeds max length 4096000");
            }

            pos += VarInt.size(idLen);
            pos += idLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Id");
            }
         }

         return ValidationResult.OK;
      }
   }

   public ReverbEffect clone() {
      ReverbEffect copy = new ReverbEffect();
      copy.id = this.id;
      copy.dryGain = this.dryGain;
      copy.modalDensity = this.modalDensity;
      copy.diffusion = this.diffusion;
      copy.gain = this.gain;
      copy.highFrequencyGain = this.highFrequencyGain;
      copy.decayTime = this.decayTime;
      copy.highFrequencyDecayRatio = this.highFrequencyDecayRatio;
      copy.reflectionGain = this.reflectionGain;
      copy.reflectionDelay = this.reflectionDelay;
      copy.lateReverbGain = this.lateReverbGain;
      copy.lateReverbDelay = this.lateReverbDelay;
      copy.roomRolloffFactor = this.roomRolloffFactor;
      copy.airAbsorptionHighFrequencyGain = this.airAbsorptionHighFrequencyGain;
      copy.limitDecayHighFrequency = this.limitDecayHighFrequency;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ReverbEffect other)
            ? false
            : Objects.equals(this.id, other.id)
               && this.dryGain == other.dryGain
               && this.modalDensity == other.modalDensity
               && this.diffusion == other.diffusion
               && this.gain == other.gain
               && this.highFrequencyGain == other.highFrequencyGain
               && this.decayTime == other.decayTime
               && this.highFrequencyDecayRatio == other.highFrequencyDecayRatio
               && this.reflectionGain == other.reflectionGain
               && this.reflectionDelay == other.reflectionDelay
               && this.lateReverbGain == other.lateReverbGain
               && this.lateReverbDelay == other.lateReverbDelay
               && this.roomRolloffFactor == other.roomRolloffFactor
               && this.airAbsorptionHighFrequencyGain == other.airAbsorptionHighFrequencyGain
               && this.limitDecayHighFrequency == other.limitDecayHighFrequency;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(
         this.id,
         this.dryGain,
         this.modalDensity,
         this.diffusion,
         this.gain,
         this.highFrequencyGain,
         this.decayTime,
         this.highFrequencyDecayRatio,
         this.reflectionGain,
         this.reflectionDelay,
         this.lateReverbGain,
         this.lateReverbDelay,
         this.roomRolloffFactor,
         this.airAbsorptionHighFrequencyGain,
         this.limitDecayHighFrequency
      );
   }
}
