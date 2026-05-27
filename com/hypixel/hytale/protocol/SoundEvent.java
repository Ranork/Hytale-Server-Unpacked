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

public class SoundEvent {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 38;
   public static final int VARIABLE_FIELD_COUNT = 3;
   public static final int VARIABLE_BLOCK_START = 50;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public String id;
   public float volume;
   public float pitch;
   public float musicDuckingVolume;
   public float ambientDuckingVolume;
   public int maxInstance;
   public boolean preventSoundInterruption;
   public float startAttenuationDistance;
   public float maxDistance;
   public float spatialBlend;
   @Nullable
   public SoundEventLayer[] layers;
   public int audioCategory;
   @Nullable
   public StateBinding[] stateBindings;

   public SoundEvent() {
   }

   public SoundEvent(
      @Nullable String id,
      float volume,
      float pitch,
      float musicDuckingVolume,
      float ambientDuckingVolume,
      int maxInstance,
      boolean preventSoundInterruption,
      float startAttenuationDistance,
      float maxDistance,
      float spatialBlend,
      @Nullable SoundEventLayer[] layers,
      int audioCategory,
      @Nullable StateBinding[] stateBindings
   ) {
      this.id = id;
      this.volume = volume;
      this.pitch = pitch;
      this.musicDuckingVolume = musicDuckingVolume;
      this.ambientDuckingVolume = ambientDuckingVolume;
      this.maxInstance = maxInstance;
      this.preventSoundInterruption = preventSoundInterruption;
      this.startAttenuationDistance = startAttenuationDistance;
      this.maxDistance = maxDistance;
      this.spatialBlend = spatialBlend;
      this.layers = layers;
      this.audioCategory = audioCategory;
      this.stateBindings = stateBindings;
   }

   public SoundEvent(@Nonnull SoundEvent other) {
      this.id = other.id;
      this.volume = other.volume;
      this.pitch = other.pitch;
      this.musicDuckingVolume = other.musicDuckingVolume;
      this.ambientDuckingVolume = other.ambientDuckingVolume;
      this.maxInstance = other.maxInstance;
      this.preventSoundInterruption = other.preventSoundInterruption;
      this.startAttenuationDistance = other.startAttenuationDistance;
      this.maxDistance = other.maxDistance;
      this.spatialBlend = other.spatialBlend;
      this.layers = other.layers;
      this.audioCategory = other.audioCategory;
      this.stateBindings = other.stateBindings;
   }

   @Nonnull
   public static SoundEvent deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 50) {
         throw ProtocolException.bufferTooSmall("SoundEvent", 50, buf.readableBytes() - offset);
      } else {
         SoundEvent obj = new SoundEvent();
         byte nullBits = buf.getByte(offset);
         obj.volume = buf.getFloatLE(offset + 1);
         obj.pitch = buf.getFloatLE(offset + 5);
         obj.musicDuckingVolume = buf.getFloatLE(offset + 9);
         obj.ambientDuckingVolume = buf.getFloatLE(offset + 13);
         obj.maxInstance = buf.getIntLE(offset + 17);
         obj.preventSoundInterruption = buf.getByte(offset + 21) != 0;
         obj.startAttenuationDistance = buf.getFloatLE(offset + 22);
         obj.maxDistance = buf.getFloatLE(offset + 26);
         obj.spatialBlend = buf.getFloatLE(offset + 30);
         obj.audioCategory = buf.getIntLE(offset + 34);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 38);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 50) {
               throw ProtocolException.invalidOffset("Id", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 50 + varPosBase0;
            int idLen = VarInt.peek(buf, varPos0);
            if (idLen < 0) {
               throw ProtocolException.invalidVarInt("Id");
            }

            int idVarIntLen = VarInt.size(idLen);
            if (idLen > 4096000) {
               throw ProtocolException.stringTooLong("Id", idLen, 4096000);
            }

            if (varPos0 + idVarIntLen + idLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Id", varPos0 + idVarIntLen + idLen, buf.readableBytes());
            }

            obj.id = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 42);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 50) {
               throw ProtocolException.invalidOffset("Layers", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 50 + varPosBase1;
            int layersCount = VarInt.peek(buf, varPos1);
            if (layersCount < 0) {
               throw ProtocolException.invalidVarInt("Layers");
            }

            int varIntLen = VarInt.size(layersCount);
            if (layersCount > 4096000) {
               throw ProtocolException.arrayTooLong("Layers", layersCount, 4096000);
            }

            if (varPos1 + varIntLen + layersCount * 42L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Layers", varPos1 + varIntLen + layersCount * 42, buf.readableBytes());
            }

            obj.layers = new SoundEventLayer[layersCount];
            int elemPos = varPos1 + varIntLen;

            for (int i = 0; i < layersCount; i++) {
               obj.layers[i] = SoundEventLayer.deserialize(buf, elemPos);
               elemPos += SoundEventLayer.computeBytesConsumed(buf, elemPos);
            }
         }

         if ((nullBits & 4) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 46);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 50) {
               throw ProtocolException.invalidOffset("StateBindings", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 50 + varPosBase2;
            int stateBindingsCount = VarInt.peek(buf, varPos2);
            if (stateBindingsCount < 0) {
               throw ProtocolException.invalidVarInt("StateBindings");
            }

            int varIntLenx = VarInt.size(stateBindingsCount);
            if (stateBindingsCount > 4096000) {
               throw ProtocolException.arrayTooLong("StateBindings", stateBindingsCount, 4096000);
            }

            if (varPos2 + varIntLenx + stateBindingsCount * 5L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("StateBindings", varPos2 + varIntLenx + stateBindingsCount * 5, buf.readableBytes());
            }

            obj.stateBindings = new StateBinding[stateBindingsCount];
            int elemPos = varPos2 + varIntLenx;

            for (int i = 0; i < stateBindingsCount; i++) {
               obj.stateBindings[i] = StateBinding.deserialize(buf, elemPos);
               elemPos += StateBinding.computeBytesConsumed(buf, elemPos);
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 50;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 38);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 50) {
            throw ProtocolException.invalidOffset("Id", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 50 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 42);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 50) {
            throw ProtocolException.invalidOffset("Layers", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 50 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos1 += SoundEventLayer.computeBytesConsumed(buf, pos1);
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 46);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 50) {
            throw ProtocolException.invalidOffset("StateBindings", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 50 + fieldOffset2;
         int arrLen = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos2 += StateBinding.computeBytesConsumed(buf, pos2);
         }

         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 50L;
   }

   @Nullable
   public static String getId(MemorySegment mem) {
      return getId(mem, 0);
   }

   @Nullable
   public static String getId(MemorySegment mem, int offset) {
      return hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 38, 50, "Id"), 4096000, PacketIO.UTF8) : null;
   }

   public static float getVolume(MemorySegment mem) {
      return getVolume(mem, 0);
   }

   public static float getVolume(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1));
   }

   public static float getPitch(MemorySegment mem) {
      return getPitch(mem, 0);
   }

   public static float getPitch(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 5));
   }

   public static float getMusicDuckingVolume(MemorySegment mem) {
      return getMusicDuckingVolume(mem, 0);
   }

   public static float getMusicDuckingVolume(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 9));
   }

   public static float getAmbientDuckingVolume(MemorySegment mem) {
      return getAmbientDuckingVolume(mem, 0);
   }

   public static float getAmbientDuckingVolume(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 13));
   }

   public static int getMaxInstance(MemorySegment mem) {
      return getMaxInstance(mem, 0);
   }

   public static int getMaxInstance(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 17));
   }

   public static boolean getPreventSoundInterruption(MemorySegment mem) {
      return getPreventSoundInterruption(mem, 0);
   }

   public static boolean getPreventSoundInterruption(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 21));
   }

   public static float getStartAttenuationDistance(MemorySegment mem) {
      return getStartAttenuationDistance(mem, 0);
   }

   public static float getStartAttenuationDistance(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 22));
   }

   public static float getMaxDistance(MemorySegment mem) {
      return getMaxDistance(mem, 0);
   }

   public static float getMaxDistance(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 26));
   }

   public static float getSpatialBlend(MemorySegment mem) {
      return getSpatialBlend(mem, 0);
   }

   public static float getSpatialBlend(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 30));
   }

   @Nullable
   public static SoundEventLayer[] getLayers(MemorySegment mem) {
      return getLayers(mem, 0);
   }

   @Nullable
   public static SoundEventLayer[] getLayers(MemorySegment mem, int offset) {
      if (!hasLayers(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 42, 50, "Layers");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Layers", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Layers", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Layers", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               SoundEventLayer[] data = new SoundEventLayer[len];

               for (int i = 0; i < len; i++) {
                  data[i] = SoundEventLayer.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   public static int getAudioCategory(MemorySegment mem) {
      return getAudioCategory(mem, 0);
   }

   public static int getAudioCategory(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 34));
   }

   @Nullable
   public static StateBinding[] getStateBindings(MemorySegment mem) {
      return getStateBindings(mem, 0);
   }

   @Nullable
   public static StateBinding[] getStateBindings(MemorySegment mem, int offset) {
      if (!hasStateBindings(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 46, 50, "StateBindings");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("StateBindings", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("StateBindings", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("StateBindings", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               StateBinding[] data = new StateBinding[len];

               for (int i = 0; i < len; i++) {
                  data[i] = StateBinding.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   public static boolean hasId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasLayers(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasStateBindings(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static SoundEvent toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static SoundEvent toObject(MemorySegment mem, int offset) {
      if (offset + 50 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("SoundEvent", offset + 50, (int)mem.byteSize());
      } else {
         SoundEventLayer[] layers = null;
         if (hasLayers(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 42, 50, "Layers");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Layers", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Layers", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Layers", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            layers = new SoundEventLayer[len];

            for (int i = 0; i < len; i++) {
               layers[i] = SoundEventLayer.toObject(mem, off);
               off += layers[i].computeSize();
            }
         }

         StateBinding[] stateBindings = null;
         if (hasStateBindings(mem, offset)) {
            int offx = offset + getValidatedOffset(mem, offset, 46, 50, "StateBindings");
            long packedx = VarInt.getWithLength(mem, offx);
            int lenx = (int)packedx;
            if (lenx < 0) {
               throw ProtocolException.negativeLength("StateBindings", lenx);
            }

            if (lenx > 4096000) {
               throw ProtocolException.arrayTooLong("StateBindings", lenx, 4096000);
            }

            int lenOffset = (int)(packedx >>> 32);
            if (offx + lenOffset + lenx > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("StateBindings", offx + lenOffset + lenx, (int)mem.byteSize());
            }

            offx += lenOffset;
            stateBindings = new StateBinding[lenx];

            for (int i = 0; i < lenx; i++) {
               stateBindings[i] = StateBinding.toObject(mem, offx);
               offx += stateBindings[i].computeSize();
            }
         }

         return new SoundEvent(
            hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 38, 50, "Id"), 4096000, PacketIO.UTF8) : null,
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 5)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 9)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 13)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 17)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 21)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 22)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 26)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 30)),
            layers,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 34)),
            stateBindings
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.id != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.layers != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.stateBindings != null) {
         nullBits = (byte)(nullBits | 4);
      }

      buf.writeByte(nullBits);
      buf.writeFloatLE(this.volume);
      buf.writeFloatLE(this.pitch);
      buf.writeFloatLE(this.musicDuckingVolume);
      buf.writeFloatLE(this.ambientDuckingVolume);
      buf.writeIntLE(this.maxInstance);
      buf.writeByte(this.preventSoundInterruption ? 1 : 0);
      buf.writeFloatLE(this.startAttenuationDistance);
      buf.writeFloatLE(this.maxDistance);
      buf.writeFloatLE(this.spatialBlend);
      buf.writeIntLE(this.audioCategory);
      int idOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int layersOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int stateBindingsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.id != null) {
         buf.setIntLE(idOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.id, 4096000);
      } else {
         buf.setIntLE(idOffsetSlot, -1);
      }

      if (this.layers != null) {
         buf.setIntLE(layersOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.layers.length > 4096000) {
            throw ProtocolException.arrayTooLong("Layers", this.layers.length, 4096000);
         }

         VarInt.write(buf, this.layers.length);

         for (SoundEventLayer item : this.layers) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(layersOffsetSlot, -1);
      }

      if (this.stateBindings != null) {
         buf.setIntLE(stateBindingsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.stateBindings.length > 4096000) {
            throw ProtocolException.arrayTooLong("StateBindings", this.stateBindings.length, 4096000);
         }

         VarInt.write(buf, this.stateBindings.length);

         for (StateBinding item : this.stateBindings) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(stateBindingsOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.id != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.layers != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.stateBindings != null) {
         nullBits = (byte)(nullBits | 4);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 1), this.volume);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 5), this.pitch);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 9), this.musicDuckingVolume);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 13), this.ambientDuckingVolume);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 17), this.maxInstance);
      mem.set(PacketIO.PROTO_BOOL, offset + 21, this.preventSoundInterruption);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 22), this.startAttenuationDistance);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 26), this.maxDistance);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 30), this.spatialBlend);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 34), this.audioCategory);
      int varOffset = offset + 50;
      if (this.id != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 38), varOffset - offset - 50);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.id, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 38), -1);
      }

      if (this.layers != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 42), varOffset - offset - 50);
         if (this.layers.length > 4096000) {
            throw ProtocolException.arrayTooLong("Layers", this.layers.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.layers.length);
         int layersValueOffset = 0;

         for (int i = 0; i < this.layers.length; i++) {
            layersValueOffset += this.layers[i].serialize(mem, varOffset + layersValueOffset);
         }

         varOffset += layersValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 42), -1);
      }

      if (this.stateBindings != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 46), varOffset - offset - 50);
         if (this.stateBindings.length > 4096000) {
            throw ProtocolException.arrayTooLong("StateBindings", this.stateBindings.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.stateBindings.length);
         int stateBindingsValueOffset = 0;

         for (int i = 0; i < this.stateBindings.length; i++) {
            stateBindingsValueOffset += this.stateBindings[i].serialize(mem, varOffset + stateBindingsValueOffset);
         }

         varOffset += stateBindingsValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 46), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 50;
      if (this.id != null) {
         size += PacketIO.stringSize(this.id);
      }

      if (this.layers != null) {
         int layersSize = 0;

         for (SoundEventLayer elem : this.layers) {
            layersSize += elem.computeSize();
         }

         size += VarInt.size(this.layers.length) + layersSize;
      }

      if (this.stateBindings != null) {
         int stateBindingsSize = 0;

         for (StateBinding elem : this.stateBindings) {
            stateBindingsSize += elem.computeSize();
         }

         size += VarInt.size(this.stateBindings.length) + stateBindingsSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 50) {
         return ValidationResult.error("Buffer too small: expected at least 50 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int idOffset = buffer.getIntLE(offset + 38);
            if (idOffset < 0 || idOffset > buffer.writerIndex() - offset - 50) {
               return ValidationResult.error("Invalid offset for Id");
            }

            int pos = offset + 50 + idOffset;
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

         if ((nullBits & 2) != 0) {
            int layersOffset = buffer.getIntLE(offset + 42);
            if (layersOffset < 0 || layersOffset > buffer.writerIndex() - offset - 50) {
               return ValidationResult.error("Invalid offset for Layers");
            }

            int posx = offset + 50 + layersOffset;
            int layersCount = VarInt.peek(buffer, posx);
            if (layersCount < 0) {
               return ValidationResult.error("Invalid array count for Layers");
            }

            if (layersCount > 4096000) {
               return ValidationResult.error("Layers exceeds max length 4096000");
            }

            posx += VarInt.size(layersCount);

            for (int i = 0; i < layersCount; i++) {
               ValidationResult structResult = SoundEventLayer.validateStructure(buffer, posx);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid SoundEventLayer in Layers[" + i + "]: " + structResult.error());
               }

               posx += SoundEventLayer.computeBytesConsumed(buffer, posx);
            }
         }

         if ((nullBits & 4) != 0) {
            int stateBindingsOffset = buffer.getIntLE(offset + 46);
            if (stateBindingsOffset < 0 || stateBindingsOffset > buffer.writerIndex() - offset - 50) {
               return ValidationResult.error("Invalid offset for StateBindings");
            }

            int posxx = offset + 50 + stateBindingsOffset;
            int stateBindingsCount = VarInt.peek(buffer, posxx);
            if (stateBindingsCount < 0) {
               return ValidationResult.error("Invalid array count for StateBindings");
            }

            if (stateBindingsCount > 4096000) {
               return ValidationResult.error("StateBindings exceeds max length 4096000");
            }

            posxx += VarInt.size(stateBindingsCount);

            for (int i = 0; i < stateBindingsCount; i++) {
               ValidationResult structResult = StateBinding.validateStructure(buffer, posxx);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid StateBinding in StateBindings[" + i + "]: " + structResult.error());
               }

               posxx += StateBinding.computeBytesConsumed(buffer, posxx);
            }
         }

         return ValidationResult.OK;
      }
   }

   public SoundEvent clone() {
      SoundEvent copy = new SoundEvent();
      copy.id = this.id;
      copy.volume = this.volume;
      copy.pitch = this.pitch;
      copy.musicDuckingVolume = this.musicDuckingVolume;
      copy.ambientDuckingVolume = this.ambientDuckingVolume;
      copy.maxInstance = this.maxInstance;
      copy.preventSoundInterruption = this.preventSoundInterruption;
      copy.startAttenuationDistance = this.startAttenuationDistance;
      copy.maxDistance = this.maxDistance;
      copy.spatialBlend = this.spatialBlend;
      copy.layers = this.layers != null ? Arrays.stream(this.layers).map(e -> e.clone()).toArray(SoundEventLayer[]::new) : null;
      copy.audioCategory = this.audioCategory;
      copy.stateBindings = this.stateBindings != null ? Arrays.stream(this.stateBindings).map(e -> e.clone()).toArray(StateBinding[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof SoundEvent other)
            ? false
            : Objects.equals(this.id, other.id)
               && this.volume == other.volume
               && this.pitch == other.pitch
               && this.musicDuckingVolume == other.musicDuckingVolume
               && this.ambientDuckingVolume == other.ambientDuckingVolume
               && this.maxInstance == other.maxInstance
               && this.preventSoundInterruption == other.preventSoundInterruption
               && this.startAttenuationDistance == other.startAttenuationDistance
               && this.maxDistance == other.maxDistance
               && this.spatialBlend == other.spatialBlend
               && Arrays.equals((Object[])this.layers, (Object[])other.layers)
               && this.audioCategory == other.audioCategory
               && Arrays.equals((Object[])this.stateBindings, (Object[])other.stateBindings);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.id);
      result = 31 * result + Float.hashCode(this.volume);
      result = 31 * result + Float.hashCode(this.pitch);
      result = 31 * result + Float.hashCode(this.musicDuckingVolume);
      result = 31 * result + Float.hashCode(this.ambientDuckingVolume);
      result = 31 * result + Integer.hashCode(this.maxInstance);
      result = 31 * result + Boolean.hashCode(this.preventSoundInterruption);
      result = 31 * result + Float.hashCode(this.startAttenuationDistance);
      result = 31 * result + Float.hashCode(this.maxDistance);
      result = 31 * result + Float.hashCode(this.spatialBlend);
      result = 31 * result + Arrays.hashCode((Object[])this.layers);
      result = 31 * result + Integer.hashCode(this.audioCategory);
      return 31 * result + Arrays.hashCode((Object[])this.stateBindings);
   }
}
