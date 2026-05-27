package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import com.hypixel.hytale.protocol.packets.camera.CameraShakeEffect;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InteractionEffects {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 32;
   public static final int VARIABLE_FIELD_COUNT = 5;
   public static final int VARIABLE_BLOCK_START = 52;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public ModelParticle[] particles;
   @Nullable
   public ModelParticle[] firstPersonParticles;
   public int worldSoundEventIndex;
   public int localSoundEventIndex;
   @Nullable
   public ModelTrail[] trails;
   public boolean waitForAnimationToFinish = true;
   @Nullable
   public String itemPlayerAnimationsId;
   @Nullable
   public String itemAnimationId;
   public boolean clearAnimationOnFinish;
   public boolean clearSoundEventOnFinish;
   @Nullable
   public CameraShakeEffect cameraShake;
   @Nullable
   public MovementEffects movementEffects;
   public float startDelay;

   public InteractionEffects() {
   }

   public InteractionEffects(
      @Nullable ModelParticle[] particles,
      @Nullable ModelParticle[] firstPersonParticles,
      int worldSoundEventIndex,
      int localSoundEventIndex,
      @Nullable ModelTrail[] trails,
      boolean waitForAnimationToFinish,
      @Nullable String itemPlayerAnimationsId,
      @Nullable String itemAnimationId,
      boolean clearAnimationOnFinish,
      boolean clearSoundEventOnFinish,
      @Nullable CameraShakeEffect cameraShake,
      @Nullable MovementEffects movementEffects,
      float startDelay
   ) {
      this.particles = particles;
      this.firstPersonParticles = firstPersonParticles;
      this.worldSoundEventIndex = worldSoundEventIndex;
      this.localSoundEventIndex = localSoundEventIndex;
      this.trails = trails;
      this.waitForAnimationToFinish = waitForAnimationToFinish;
      this.itemPlayerAnimationsId = itemPlayerAnimationsId;
      this.itemAnimationId = itemAnimationId;
      this.clearAnimationOnFinish = clearAnimationOnFinish;
      this.clearSoundEventOnFinish = clearSoundEventOnFinish;
      this.cameraShake = cameraShake;
      this.movementEffects = movementEffects;
      this.startDelay = startDelay;
   }

   public InteractionEffects(@Nonnull InteractionEffects other) {
      this.particles = other.particles;
      this.firstPersonParticles = other.firstPersonParticles;
      this.worldSoundEventIndex = other.worldSoundEventIndex;
      this.localSoundEventIndex = other.localSoundEventIndex;
      this.trails = other.trails;
      this.waitForAnimationToFinish = other.waitForAnimationToFinish;
      this.itemPlayerAnimationsId = other.itemPlayerAnimationsId;
      this.itemAnimationId = other.itemAnimationId;
      this.clearAnimationOnFinish = other.clearAnimationOnFinish;
      this.clearSoundEventOnFinish = other.clearSoundEventOnFinish;
      this.cameraShake = other.cameraShake;
      this.movementEffects = other.movementEffects;
      this.startDelay = other.startDelay;
   }

   @Nonnull
   public static InteractionEffects deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 52) {
         throw ProtocolException.bufferTooSmall("InteractionEffects", 52, buf.readableBytes() - offset);
      } else {
         InteractionEffects obj = new InteractionEffects();
         byte nullBits = buf.getByte(offset);
         obj.worldSoundEventIndex = buf.getIntLE(offset + 1);
         obj.localSoundEventIndex = buf.getIntLE(offset + 5);
         obj.waitForAnimationToFinish = buf.getByte(offset + 9) != 0;
         obj.clearAnimationOnFinish = buf.getByte(offset + 10) != 0;
         obj.clearSoundEventOnFinish = buf.getByte(offset + 11) != 0;
         if ((nullBits & 1) != 0) {
            obj.cameraShake = CameraShakeEffect.deserialize(buf, offset + 12);
         }

         if ((nullBits & 2) != 0) {
            obj.movementEffects = MovementEffects.deserialize(buf, offset + 21);
         }

         obj.startDelay = buf.getFloatLE(offset + 28);
         if ((nullBits & 4) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 32);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 52) {
               throw ProtocolException.invalidOffset("Particles", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 52 + varPosBase0;
            int particlesCount = VarInt.peek(buf, varPos0);
            if (particlesCount < 0) {
               throw ProtocolException.invalidVarInt("Particles");
            }

            int varIntLen = VarInt.size(particlesCount);
            if (particlesCount > 4096000) {
               throw ProtocolException.arrayTooLong("Particles", particlesCount, 4096000);
            }

            if (varPos0 + varIntLen + particlesCount * 34L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Particles", varPos0 + varIntLen + particlesCount * 34, buf.readableBytes());
            }

            obj.particles = new ModelParticle[particlesCount];
            int elemPos = varPos0 + varIntLen;

            for (int i = 0; i < particlesCount; i++) {
               obj.particles[i] = ModelParticle.deserialize(buf, elemPos);
               elemPos += ModelParticle.computeBytesConsumed(buf, elemPos);
            }
         }

         if ((nullBits & 8) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 36);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 52) {
               throw ProtocolException.invalidOffset("FirstPersonParticles", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 52 + varPosBase1;
            int firstPersonParticlesCount = VarInt.peek(buf, varPos1);
            if (firstPersonParticlesCount < 0) {
               throw ProtocolException.invalidVarInt("FirstPersonParticles");
            }

            int varIntLenx = VarInt.size(firstPersonParticlesCount);
            if (firstPersonParticlesCount > 4096000) {
               throw ProtocolException.arrayTooLong("FirstPersonParticles", firstPersonParticlesCount, 4096000);
            }

            if (varPos1 + varIntLenx + firstPersonParticlesCount * 34L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("FirstPersonParticles", varPos1 + varIntLenx + firstPersonParticlesCount * 34, buf.readableBytes());
            }

            obj.firstPersonParticles = new ModelParticle[firstPersonParticlesCount];
            int elemPos = varPos1 + varIntLenx;

            for (int i = 0; i < firstPersonParticlesCount; i++) {
               obj.firstPersonParticles[i] = ModelParticle.deserialize(buf, elemPos);
               elemPos += ModelParticle.computeBytesConsumed(buf, elemPos);
            }
         }

         if ((nullBits & 16) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 40);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 52) {
               throw ProtocolException.invalidOffset("Trails", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 52 + varPosBase2;
            int trailsCount = VarInt.peek(buf, varPos2);
            if (trailsCount < 0) {
               throw ProtocolException.invalidVarInt("Trails");
            }

            int varIntLenxx = VarInt.size(trailsCount);
            if (trailsCount > 4096000) {
               throw ProtocolException.arrayTooLong("Trails", trailsCount, 4096000);
            }

            if (varPos2 + varIntLenxx + trailsCount * 27L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Trails", varPos2 + varIntLenxx + trailsCount * 27, buf.readableBytes());
            }

            obj.trails = new ModelTrail[trailsCount];
            int elemPos = varPos2 + varIntLenxx;

            for (int i = 0; i < trailsCount; i++) {
               obj.trails[i] = ModelTrail.deserialize(buf, elemPos);
               elemPos += ModelTrail.computeBytesConsumed(buf, elemPos);
            }
         }

         if ((nullBits & 32) != 0) {
            int varPosBase3 = buf.getIntLE(offset + 44);
            if (varPosBase3 < 0 || varPosBase3 > buf.writerIndex() - offset - 52) {
               throw ProtocolException.invalidOffset("ItemPlayerAnimationsId", varPosBase3, buf.readableBytes());
            }

            int varPos3 = offset + 52 + varPosBase3;
            int itemPlayerAnimationsIdLen = VarInt.peek(buf, varPos3);
            if (itemPlayerAnimationsIdLen < 0) {
               throw ProtocolException.invalidVarInt("ItemPlayerAnimationsId");
            }

            int itemPlayerAnimationsIdVarIntLen = VarInt.size(itemPlayerAnimationsIdLen);
            if (itemPlayerAnimationsIdLen > 4096000) {
               throw ProtocolException.stringTooLong("ItemPlayerAnimationsId", itemPlayerAnimationsIdLen, 4096000);
            }

            if (varPos3 + itemPlayerAnimationsIdVarIntLen + itemPlayerAnimationsIdLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall(
                  "ItemPlayerAnimationsId", varPos3 + itemPlayerAnimationsIdVarIntLen + itemPlayerAnimationsIdLen, buf.readableBytes()
               );
            }

            obj.itemPlayerAnimationsId = PacketIO.readVarString(buf, varPos3, PacketIO.UTF8);
         }

         if ((nullBits & 64) != 0) {
            int varPosBase4 = buf.getIntLE(offset + 48);
            if (varPosBase4 < 0 || varPosBase4 > buf.writerIndex() - offset - 52) {
               throw ProtocolException.invalidOffset("ItemAnimationId", varPosBase4, buf.readableBytes());
            }

            int varPos4 = offset + 52 + varPosBase4;
            int itemAnimationIdLen = VarInt.peek(buf, varPos4);
            if (itemAnimationIdLen < 0) {
               throw ProtocolException.invalidVarInt("ItemAnimationId");
            }

            int itemAnimationIdVarIntLen = VarInt.size(itemAnimationIdLen);
            if (itemAnimationIdLen > 4096000) {
               throw ProtocolException.stringTooLong("ItemAnimationId", itemAnimationIdLen, 4096000);
            }

            if (varPos4 + itemAnimationIdVarIntLen + itemAnimationIdLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("ItemAnimationId", varPos4 + itemAnimationIdVarIntLen + itemAnimationIdLen, buf.readableBytes());
            }

            obj.itemAnimationId = PacketIO.readVarString(buf, varPos4, PacketIO.UTF8);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 52;
      if ((nullBits & 4) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 32);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 52) {
            throw ProtocolException.invalidOffset("Particles", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 52 + fieldOffset0;
         int arrLen = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos0 += ModelParticle.computeBytesConsumed(buf, pos0);
         }

         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 36);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 52) {
            throw ProtocolException.invalidOffset("FirstPersonParticles", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 52 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos1 += ModelParticle.computeBytesConsumed(buf, pos1);
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 16) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 40);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 52) {
            throw ProtocolException.invalidOffset("Trails", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 52 + fieldOffset2;
         int arrLen = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos2 += ModelTrail.computeBytesConsumed(buf, pos2);
         }

         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits & 32) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 44);
         if (fieldOffset3 < 0 || fieldOffset3 > buf.writerIndex() - offset - 52) {
            throw ProtocolException.invalidOffset("ItemPlayerAnimationsId", fieldOffset3, maxEnd);
         }

         int pos3 = offset + 52 + fieldOffset3;
         int sl = VarInt.peek(buf, pos3);
         pos3 += VarInt.size(sl) + sl;
         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      if ((nullBits & 64) != 0) {
         int fieldOffset4 = buf.getIntLE(offset + 48);
         if (fieldOffset4 < 0 || fieldOffset4 > buf.writerIndex() - offset - 52) {
            throw ProtocolException.invalidOffset("ItemAnimationId", fieldOffset4, maxEnd);
         }

         int pos4 = offset + 52 + fieldOffset4;
         int sl = VarInt.peek(buf, pos4);
         pos4 += VarInt.size(sl) + sl;
         if (pos4 - offset > maxEnd) {
            maxEnd = pos4 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 52L;
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
         int off = offset + getValidatedOffset(mem, offset, 32, 52, "Particles");
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

   @Nullable
   public static ModelParticle[] getFirstPersonParticles(MemorySegment mem) {
      return getFirstPersonParticles(mem, 0);
   }

   @Nullable
   public static ModelParticle[] getFirstPersonParticles(MemorySegment mem, int offset) {
      if (!hasFirstPersonParticles(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 36, 52, "FirstPersonParticles");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("FirstPersonParticles", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("FirstPersonParticles", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("FirstPersonParticles", off + lenOffset + len, (int)mem.byteSize());
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

   public static int getWorldSoundEventIndex(MemorySegment mem) {
      return getWorldSoundEventIndex(mem, 0);
   }

   public static int getWorldSoundEventIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   public static int getLocalSoundEventIndex(MemorySegment mem) {
      return getLocalSoundEventIndex(mem, 0);
   }

   public static int getLocalSoundEventIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 5));
   }

   @Nullable
   public static ModelTrail[] getTrails(MemorySegment mem) {
      return getTrails(mem, 0);
   }

   @Nullable
   public static ModelTrail[] getTrails(MemorySegment mem, int offset) {
      if (!hasTrails(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 40, 52, "Trails");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Trails", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Trails", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Trails", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               ModelTrail[] data = new ModelTrail[len];

               for (int i = 0; i < len; i++) {
                  data[i] = ModelTrail.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   public static boolean getWaitForAnimationToFinish(MemorySegment mem) {
      return getWaitForAnimationToFinish(mem, 0);
   }

   public static boolean getWaitForAnimationToFinish(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 9));
   }

   @Nullable
   public static String getItemPlayerAnimationsId(MemorySegment mem) {
      return getItemPlayerAnimationsId(mem, 0);
   }

   @Nullable
   public static String getItemPlayerAnimationsId(MemorySegment mem, int offset) {
      return hasItemPlayerAnimationsId(mem, offset)
         ? PacketIO.readVarString(
            "ItemPlayerAnimationsId", mem, offset + getValidatedOffset(mem, offset, 44, 52, "ItemPlayerAnimationsId"), 4096000, PacketIO.UTF8
         )
         : null;
   }

   @Nullable
   public static String getItemAnimationId(MemorySegment mem) {
      return getItemAnimationId(mem, 0);
   }

   @Nullable
   public static String getItemAnimationId(MemorySegment mem, int offset) {
      return hasItemAnimationId(mem, offset)
         ? PacketIO.readVarString("ItemAnimationId", mem, offset + getValidatedOffset(mem, offset, 48, 52, "ItemAnimationId"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static boolean getClearAnimationOnFinish(MemorySegment mem) {
      return getClearAnimationOnFinish(mem, 0);
   }

   public static boolean getClearAnimationOnFinish(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 10));
   }

   public static boolean getClearSoundEventOnFinish(MemorySegment mem) {
      return getClearSoundEventOnFinish(mem, 0);
   }

   public static boolean getClearSoundEventOnFinish(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 11));
   }

   @Nullable
   public static CameraShakeEffect getCameraShake(MemorySegment mem) {
      return getCameraShake(mem, 0);
   }

   @Nullable
   public static CameraShakeEffect getCameraShake(MemorySegment mem, int offset) {
      return hasCameraShake(mem, offset) ? CameraShakeEffect.toObject(mem, offset + 12) : null;
   }

   @Nullable
   public static MovementEffects getMovementEffects(MemorySegment mem) {
      return getMovementEffects(mem, 0);
   }

   @Nullable
   public static MovementEffects getMovementEffects(MemorySegment mem, int offset) {
      return hasMovementEffects(mem, offset) ? MovementEffects.toObject(mem, offset + 21) : null;
   }

   public static float getStartDelay(MemorySegment mem) {
      return getStartDelay(mem, 0);
   }

   public static float getStartDelay(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 28));
   }

   public static boolean hasCameraShake(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasMovementEffects(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasParticles(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasFirstPersonParticles(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   public static boolean hasTrails(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 16) != 0;
   }

   public static boolean hasItemPlayerAnimationsId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 32) != 0;
   }

   public static boolean hasItemAnimationId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 64) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static InteractionEffects toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static InteractionEffects toObject(MemorySegment mem, int offset) {
      if (offset + 52 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("InteractionEffects", offset + 52, (int)mem.byteSize());
      } else {
         ModelParticle[] particles = null;
         if (hasParticles(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 32, 52, "Particles");
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

         ModelParticle[] firstPersonParticles = null;
         if (hasFirstPersonParticles(mem, offset)) {
            int offx = offset + getValidatedOffset(mem, offset, 36, 52, "FirstPersonParticles");
            long packedx = VarInt.getWithLength(mem, offx);
            int lenx = (int)packedx;
            if (lenx < 0) {
               throw ProtocolException.negativeLength("FirstPersonParticles", lenx);
            }

            if (lenx > 4096000) {
               throw ProtocolException.arrayTooLong("FirstPersonParticles", lenx, 4096000);
            }

            int lenOffset = (int)(packedx >>> 32);
            if (offx + lenOffset + lenx > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("FirstPersonParticles", offx + lenOffset + lenx, (int)mem.byteSize());
            }

            offx += lenOffset;
            firstPersonParticles = new ModelParticle[lenx];

            for (int i = 0; i < lenx; i++) {
               firstPersonParticles[i] = ModelParticle.toObject(mem, offx);
               offx += firstPersonParticles[i].computeSize();
            }
         }

         ModelTrail[] trails = null;
         if (hasTrails(mem, offset)) {
            int offxx = offset + getValidatedOffset(mem, offset, 40, 52, "Trails");
            long packedxx = VarInt.getWithLength(mem, offxx);
            int lenxx = (int)packedxx;
            if (lenxx < 0) {
               throw ProtocolException.negativeLength("Trails", lenxx);
            }

            if (lenxx > 4096000) {
               throw ProtocolException.arrayTooLong("Trails", lenxx, 4096000);
            }

            int lenOffset = (int)(packedxx >>> 32);
            if (offxx + lenOffset + lenxx > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Trails", offxx + lenOffset + lenxx, (int)mem.byteSize());
            }

            offxx += lenOffset;
            trails = new ModelTrail[lenxx];

            for (int i = 0; i < lenxx; i++) {
               trails[i] = ModelTrail.toObject(mem, offxx);
               offxx += trails[i].computeSize();
            }
         }

         return new InteractionEffects(
            particles,
            firstPersonParticles,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 5)),
            trails,
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 9)),
            hasItemPlayerAnimationsId(mem, offset)
               ? PacketIO.readVarString(
                  "ItemPlayerAnimationsId", mem, offset + getValidatedOffset(mem, offset, 44, 52, "ItemPlayerAnimationsId"), 4096000, PacketIO.UTF8
               )
               : null,
            hasItemAnimationId(mem, offset)
               ? PacketIO.readVarString("ItemAnimationId", mem, offset + getValidatedOffset(mem, offset, 48, 52, "ItemAnimationId"), 4096000, PacketIO.UTF8)
               : null,
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 10)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 11)),
            hasCameraShake(mem, offset) ? CameraShakeEffect.toObject(mem, offset + 12) : null,
            hasMovementEffects(mem, offset) ? MovementEffects.toObject(mem, offset + 21) : null,
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 28))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.cameraShake != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.movementEffects != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.particles != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.firstPersonParticles != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.trails != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.itemPlayerAnimationsId != null) {
         nullBits = (byte)(nullBits | 32);
      }

      if (this.itemAnimationId != null) {
         nullBits = (byte)(nullBits | 64);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.worldSoundEventIndex);
      buf.writeIntLE(this.localSoundEventIndex);
      buf.writeByte(this.waitForAnimationToFinish ? 1 : 0);
      buf.writeByte(this.clearAnimationOnFinish ? 1 : 0);
      buf.writeByte(this.clearSoundEventOnFinish ? 1 : 0);
      if (this.cameraShake != null) {
         this.cameraShake.serialize(buf);
      } else {
         buf.writeZero(9);
      }

      if (this.movementEffects != null) {
         this.movementEffects.serialize(buf);
      } else {
         buf.writeZero(7);
      }

      buf.writeFloatLE(this.startDelay);
      int particlesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int firstPersonParticlesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int trailsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int itemPlayerAnimationsIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int itemAnimationIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.particles != null) {
         buf.setIntLE(particlesOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.particles.length > 4096000) {
            throw ProtocolException.arrayTooLong("Particles", this.particles.length, 4096000);
         }

         VarInt.write(buf, this.particles.length);

         for (ModelParticle item : this.particles) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(particlesOffsetSlot, -1);
      }

      if (this.firstPersonParticles != null) {
         buf.setIntLE(firstPersonParticlesOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.firstPersonParticles.length > 4096000) {
            throw ProtocolException.arrayTooLong("FirstPersonParticles", this.firstPersonParticles.length, 4096000);
         }

         VarInt.write(buf, this.firstPersonParticles.length);

         for (ModelParticle item : this.firstPersonParticles) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(firstPersonParticlesOffsetSlot, -1);
      }

      if (this.trails != null) {
         buf.setIntLE(trailsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.trails.length > 4096000) {
            throw ProtocolException.arrayTooLong("Trails", this.trails.length, 4096000);
         }

         VarInt.write(buf, this.trails.length);

         for (ModelTrail item : this.trails) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(trailsOffsetSlot, -1);
      }

      if (this.itemPlayerAnimationsId != null) {
         buf.setIntLE(itemPlayerAnimationsIdOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.itemPlayerAnimationsId, 4096000);
      } else {
         buf.setIntLE(itemPlayerAnimationsIdOffsetSlot, -1);
      }

      if (this.itemAnimationId != null) {
         buf.setIntLE(itemAnimationIdOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.itemAnimationId, 4096000);
      } else {
         buf.setIntLE(itemAnimationIdOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.cameraShake != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.movementEffects != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.particles != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.firstPersonParticles != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.trails != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.itemPlayerAnimationsId != null) {
         nullBits = (byte)(nullBits | 32);
      }

      if (this.itemAnimationId != null) {
         nullBits = (byte)(nullBits | 64);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.worldSoundEventIndex);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 5), this.localSoundEventIndex);
      mem.set(PacketIO.PROTO_BOOL, offset + 9, this.waitForAnimationToFinish);
      mem.set(PacketIO.PROTO_BOOL, offset + 10, this.clearAnimationOnFinish);
      mem.set(PacketIO.PROTO_BOOL, offset + 11, this.clearSoundEventOnFinish);
      if (this.cameraShake != null) {
         this.cameraShake.serialize(mem, offset + 12);
      } else {
         mem.asSlice(offset + 12, 9L).fill((byte)0);
      }

      if (this.movementEffects != null) {
         this.movementEffects.serialize(mem, offset + 21);
      } else {
         mem.asSlice(offset + 21, 7L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 28), this.startDelay);
      int varOffset = offset + 52;
      if (this.particles != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 32), varOffset - offset - 52);
         if (this.particles.length > 4096000) {
            throw ProtocolException.arrayTooLong("Particles", this.particles.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.particles.length);
         int particlesValueOffset = 0;

         for (int i = 0; i < this.particles.length; i++) {
            particlesValueOffset += this.particles[i].serialize(mem, varOffset + particlesValueOffset);
         }

         varOffset += particlesValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 32), -1);
      }

      if (this.firstPersonParticles != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 36), varOffset - offset - 52);
         if (this.firstPersonParticles.length > 4096000) {
            throw ProtocolException.arrayTooLong("FirstPersonParticles", this.firstPersonParticles.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.firstPersonParticles.length);
         int firstPersonParticlesValueOffset = 0;

         for (int i = 0; i < this.firstPersonParticles.length; i++) {
            firstPersonParticlesValueOffset += this.firstPersonParticles[i].serialize(mem, varOffset + firstPersonParticlesValueOffset);
         }

         varOffset += firstPersonParticlesValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 36), -1);
      }

      if (this.trails != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 40), varOffset - offset - 52);
         if (this.trails.length > 4096000) {
            throw ProtocolException.arrayTooLong("Trails", this.trails.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.trails.length);
         int trailsValueOffset = 0;

         for (int i = 0; i < this.trails.length; i++) {
            trailsValueOffset += this.trails[i].serialize(mem, varOffset + trailsValueOffset);
         }

         varOffset += trailsValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 40), -1);
      }

      if (this.itemPlayerAnimationsId != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 44), varOffset - offset - 52);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.itemPlayerAnimationsId, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 44), -1);
      }

      if (this.itemAnimationId != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 48), varOffset - offset - 52);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.itemAnimationId, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 48), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 52;
      if (this.particles != null) {
         int particlesSize = 0;

         for (ModelParticle elem : this.particles) {
            particlesSize += elem.computeSize();
         }

         size += VarInt.size(this.particles.length) + particlesSize;
      }

      if (this.firstPersonParticles != null) {
         int firstPersonParticlesSize = 0;

         for (ModelParticle elem : this.firstPersonParticles) {
            firstPersonParticlesSize += elem.computeSize();
         }

         size += VarInt.size(this.firstPersonParticles.length) + firstPersonParticlesSize;
      }

      if (this.trails != null) {
         int trailsSize = 0;

         for (ModelTrail elem : this.trails) {
            trailsSize += elem.computeSize();
         }

         size += VarInt.size(this.trails.length) + trailsSize;
      }

      if (this.itemPlayerAnimationsId != null) {
         size += PacketIO.stringSize(this.itemPlayerAnimationsId);
      }

      if (this.itemAnimationId != null) {
         size += PacketIO.stringSize(this.itemAnimationId);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 52) {
         return ValidationResult.error("Buffer too small: expected at least 52 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 4) != 0) {
            int particlesOffset = buffer.getIntLE(offset + 32);
            if (particlesOffset < 0 || particlesOffset > buffer.writerIndex() - offset - 52) {
               return ValidationResult.error("Invalid offset for Particles");
            }

            int pos = offset + 52 + particlesOffset;
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

         if ((nullBits & 8) != 0) {
            int firstPersonParticlesOffset = buffer.getIntLE(offset + 36);
            if (firstPersonParticlesOffset < 0 || firstPersonParticlesOffset > buffer.writerIndex() - offset - 52) {
               return ValidationResult.error("Invalid offset for FirstPersonParticles");
            }

            int posx = offset + 52 + firstPersonParticlesOffset;
            int firstPersonParticlesCount = VarInt.peek(buffer, posx);
            if (firstPersonParticlesCount < 0) {
               return ValidationResult.error("Invalid array count for FirstPersonParticles");
            }

            if (firstPersonParticlesCount > 4096000) {
               return ValidationResult.error("FirstPersonParticles exceeds max length 4096000");
            }

            posx += VarInt.size(firstPersonParticlesCount);

            for (int i = 0; i < firstPersonParticlesCount; i++) {
               ValidationResult structResult = ModelParticle.validateStructure(buffer, posx);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid ModelParticle in FirstPersonParticles[" + i + "]: " + structResult.error());
               }

               posx += ModelParticle.computeBytesConsumed(buffer, posx);
            }
         }

         if ((nullBits & 16) != 0) {
            int trailsOffset = buffer.getIntLE(offset + 40);
            if (trailsOffset < 0 || trailsOffset > buffer.writerIndex() - offset - 52) {
               return ValidationResult.error("Invalid offset for Trails");
            }

            int posxx = offset + 52 + trailsOffset;
            int trailsCount = VarInt.peek(buffer, posxx);
            if (trailsCount < 0) {
               return ValidationResult.error("Invalid array count for Trails");
            }

            if (trailsCount > 4096000) {
               return ValidationResult.error("Trails exceeds max length 4096000");
            }

            posxx += VarInt.size(trailsCount);

            for (int i = 0; i < trailsCount; i++) {
               ValidationResult structResult = ModelTrail.validateStructure(buffer, posxx);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid ModelTrail in Trails[" + i + "]: " + structResult.error());
               }

               posxx += ModelTrail.computeBytesConsumed(buffer, posxx);
            }
         }

         if ((nullBits & 32) != 0) {
            int itemPlayerAnimationsIdOffset = buffer.getIntLE(offset + 44);
            if (itemPlayerAnimationsIdOffset < 0 || itemPlayerAnimationsIdOffset > buffer.writerIndex() - offset - 52) {
               return ValidationResult.error("Invalid offset for ItemPlayerAnimationsId");
            }

            int posxxx = offset + 52 + itemPlayerAnimationsIdOffset;
            int itemPlayerAnimationsIdLen = VarInt.peek(buffer, posxxx);
            if (itemPlayerAnimationsIdLen < 0) {
               return ValidationResult.error("Invalid string length for ItemPlayerAnimationsId");
            }

            if (itemPlayerAnimationsIdLen > 4096000) {
               return ValidationResult.error("ItemPlayerAnimationsId exceeds max length 4096000");
            }

            posxxx += VarInt.size(itemPlayerAnimationsIdLen);
            posxxx += itemPlayerAnimationsIdLen;
            if (posxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading ItemPlayerAnimationsId");
            }
         }

         if ((nullBits & 64) != 0) {
            int itemAnimationIdOffset = buffer.getIntLE(offset + 48);
            if (itemAnimationIdOffset < 0 || itemAnimationIdOffset > buffer.writerIndex() - offset - 52) {
               return ValidationResult.error("Invalid offset for ItemAnimationId");
            }

            int posxxxx = offset + 52 + itemAnimationIdOffset;
            int itemAnimationIdLen = VarInt.peek(buffer, posxxxx);
            if (itemAnimationIdLen < 0) {
               return ValidationResult.error("Invalid string length for ItemAnimationId");
            }

            if (itemAnimationIdLen > 4096000) {
               return ValidationResult.error("ItemAnimationId exceeds max length 4096000");
            }

            posxxxx += VarInt.size(itemAnimationIdLen);
            posxxxx += itemAnimationIdLen;
            if (posxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading ItemAnimationId");
            }
         }

         return ValidationResult.OK;
      }
   }

   public InteractionEffects clone() {
      InteractionEffects copy = new InteractionEffects();
      copy.particles = this.particles != null ? Arrays.stream(this.particles).map(e -> e.clone()).toArray(ModelParticle[]::new) : null;
      copy.firstPersonParticles = this.firstPersonParticles != null
         ? Arrays.stream(this.firstPersonParticles).map(e -> e.clone()).toArray(ModelParticle[]::new)
         : null;
      copy.worldSoundEventIndex = this.worldSoundEventIndex;
      copy.localSoundEventIndex = this.localSoundEventIndex;
      copy.trails = this.trails != null ? Arrays.stream(this.trails).map(e -> e.clone()).toArray(ModelTrail[]::new) : null;
      copy.waitForAnimationToFinish = this.waitForAnimationToFinish;
      copy.itemPlayerAnimationsId = this.itemPlayerAnimationsId;
      copy.itemAnimationId = this.itemAnimationId;
      copy.clearAnimationOnFinish = this.clearAnimationOnFinish;
      copy.clearSoundEventOnFinish = this.clearSoundEventOnFinish;
      copy.cameraShake = this.cameraShake != null ? this.cameraShake.clone() : null;
      copy.movementEffects = this.movementEffects != null ? this.movementEffects.clone() : null;
      copy.startDelay = this.startDelay;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof InteractionEffects other)
            ? false
            : Arrays.equals((Object[])this.particles, (Object[])other.particles)
               && Arrays.equals((Object[])this.firstPersonParticles, (Object[])other.firstPersonParticles)
               && this.worldSoundEventIndex == other.worldSoundEventIndex
               && this.localSoundEventIndex == other.localSoundEventIndex
               && Arrays.equals((Object[])this.trails, (Object[])other.trails)
               && this.waitForAnimationToFinish == other.waitForAnimationToFinish
               && Objects.equals(this.itemPlayerAnimationsId, other.itemPlayerAnimationsId)
               && Objects.equals(this.itemAnimationId, other.itemAnimationId)
               && this.clearAnimationOnFinish == other.clearAnimationOnFinish
               && this.clearSoundEventOnFinish == other.clearSoundEventOnFinish
               && Objects.equals(this.cameraShake, other.cameraShake)
               && Objects.equals(this.movementEffects, other.movementEffects)
               && this.startDelay == other.startDelay;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Arrays.hashCode((Object[])this.particles);
      result = 31 * result + Arrays.hashCode((Object[])this.firstPersonParticles);
      result = 31 * result + Integer.hashCode(this.worldSoundEventIndex);
      result = 31 * result + Integer.hashCode(this.localSoundEventIndex);
      result = 31 * result + Arrays.hashCode((Object[])this.trails);
      result = 31 * result + Boolean.hashCode(this.waitForAnimationToFinish);
      result = 31 * result + Objects.hashCode(this.itemPlayerAnimationsId);
      result = 31 * result + Objects.hashCode(this.itemAnimationId);
      result = 31 * result + Boolean.hashCode(this.clearAnimationOnFinish);
      result = 31 * result + Boolean.hashCode(this.clearSoundEventOnFinish);
      result = 31 * result + Objects.hashCode(this.cameraShake);
      result = 31 * result + Objects.hashCode(this.movementEffects);
      return 31 * result + Float.hashCode(this.startDelay);
   }
}
