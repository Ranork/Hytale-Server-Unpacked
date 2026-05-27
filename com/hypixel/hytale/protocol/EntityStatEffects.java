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

public class EntityStatEffects {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 6;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 6;
   public static final int MAX_SIZE = 1677721600;
   public boolean triggerAtZero;
   public int soundEventIndex;
   @Nullable
   public ModelParticle[] particles;

   public EntityStatEffects() {
   }

   public EntityStatEffects(boolean triggerAtZero, int soundEventIndex, @Nullable ModelParticle[] particles) {
      this.triggerAtZero = triggerAtZero;
      this.soundEventIndex = soundEventIndex;
      this.particles = particles;
   }

   public EntityStatEffects(@Nonnull EntityStatEffects other) {
      this.triggerAtZero = other.triggerAtZero;
      this.soundEventIndex = other.soundEventIndex;
      this.particles = other.particles;
   }

   @Nonnull
   public static EntityStatEffects deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 6) {
         throw ProtocolException.bufferTooSmall("EntityStatEffects", 6, buf.readableBytes() - offset);
      } else {
         EntityStatEffects obj = new EntityStatEffects();
         byte nullBits = buf.getByte(offset);
         obj.triggerAtZero = buf.getByte(offset + 1) != 0;
         obj.soundEventIndex = buf.getIntLE(offset + 2);
         int pos = offset + 6;
         if ((nullBits & 1) != 0) {
            int particlesCount = VarInt.peek(buf, pos);
            if (particlesCount < 0) {
               throw ProtocolException.invalidVarInt("Particles");
            }

            int particlesVarLen = VarInt.size(particlesCount);
            if (particlesCount > 4096000) {
               throw ProtocolException.arrayTooLong("Particles", particlesCount, 4096000);
            }

            if (pos + particlesVarLen + particlesCount * 34L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Particles", pos + particlesVarLen + particlesCount * 34, buf.readableBytes());
            }

            pos += particlesVarLen;
            obj.particles = new ModelParticle[particlesCount];

            for (int i = 0; i < particlesCount; i++) {
               obj.particles[i] = ModelParticle.deserialize(buf, pos);
               pos += ModelParticle.computeBytesConsumed(buf, pos);
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 6;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos += ModelParticle.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 6L;
   }

   public static boolean getTriggerAtZero(MemorySegment mem) {
      return getTriggerAtZero(mem, 0);
   }

   public static boolean getTriggerAtZero(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1));
   }

   public static int getSoundEventIndex(MemorySegment mem) {
      return getSoundEventIndex(mem, 0);
   }

   public static int getSoundEventIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 2));
   }

   @Nullable
   public static ModelParticle[] getParticles(MemorySegment mem) {
      return getParticles(mem, 0);
   }

   @Nullable
   public static ModelParticle[] getParticles(MemorySegment mem, int offset) {
      if (!hasParticles(mem, offset)) {
         return null;
      } else {
         int off = offset + 6;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Particles", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Particles", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Particles", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               ModelParticle[] data = new ModelParticle[len];

               for (int i = 0; i < len; i++) {
                  data[i] = ModelParticle.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   public static boolean hasParticles(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static EntityStatEffects toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static EntityStatEffects toObject(MemorySegment mem, int offset) {
      if (offset + 6 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("EntityStatEffects", offset + 6, (int)mem.byteSize());
      } else {
         ModelParticle[] particles = null;
         if (hasParticles(mem, offset)) {
            int off = offset + 6;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Particles", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Particles", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Particles", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            particles = new ModelParticle[len];

            for (int i = 0; i < len; i++) {
               particles[i] = ModelParticle.toObject(mem, off);
               off += particles[i].computeSize();
            }
         }

         return new EntityStatEffects(mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1)), mem.get(PacketIO.PROTO_INT, (long)(offset + 2)), particles);
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.particles != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.triggerAtZero ? 1 : 0);
      buf.writeIntLE(this.soundEventIndex);
      if (this.particles != null) {
         if (this.particles.length > 4096000) {
            throw ProtocolException.arrayTooLong("Particles", this.particles.length, 4096000);
         }

         VarInt.write(buf, this.particles.length);

         for (ModelParticle item : this.particles) {
            item.serialize(buf);
         }
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.particles != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BOOL, offset + 1, this.triggerAtZero);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 2), this.soundEventIndex);
      int varOffset = offset + 6;
      if (this.particles != null) {
         if (this.particles.length > 4096000) {
            throw ProtocolException.arrayTooLong("Particles", this.particles.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.particles.length);
         int particlesValueOffset = 0;

         for (int i = 0; i < this.particles.length; i++) {
            particlesValueOffset += this.particles[i].serialize(mem, varOffset + particlesValueOffset);
         }

         varOffset += particlesValueOffset;
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 6;
      if (this.particles != null) {
         int particlesSize = 0;

         for (ModelParticle elem : this.particles) {
            particlesSize += elem.computeSize();
         }

         size += VarInt.size(this.particles.length) + particlesSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 6) {
         return ValidationResult.error("Buffer too small: expected at least 6 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 6;
         if ((nullBits & 1) != 0) {
            int particlesCount = VarInt.peek(buffer, pos);
            if (particlesCount < 0) {
               return ValidationResult.error("Invalid array count for Particles");
            }

            if (particlesCount > 4096000) {
               return ValidationResult.error("Particles exceeds max length 4096000");
            }

            pos += VarInt.size(particlesCount);

            for (int i = 0; i < particlesCount; i++) {
               ValidationResult structResult = ModelParticle.validateStructure(buffer, pos);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid ModelParticle in Particles[" + i + "]: " + structResult.error());
               }

               pos += ModelParticle.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
      }
   }

   public EntityStatEffects clone() {
      EntityStatEffects copy = new EntityStatEffects();
      copy.triggerAtZero = this.triggerAtZero;
      copy.soundEventIndex = this.soundEventIndex;
      copy.particles = this.particles != null ? Arrays.stream(this.particles).map(e -> e.clone()).toArray(ModelParticle[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof EntityStatEffects other)
            ? false
            : this.triggerAtZero == other.triggerAtZero
               && this.soundEventIndex == other.soundEventIndex
               && Arrays.equals((Object[])this.particles, (Object[])other.particles);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Boolean.hashCode(this.triggerAtZero);
      result = 31 * result + Integer.hashCode(this.soundEventIndex);
      return 31 * result + Arrays.hashCode((Object[])this.particles);
   }
}
