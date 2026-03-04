package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SoundEvent {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 34;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 42;
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
   @Nullable
   public SoundEventLayer[] layers;
   public int audioCategory;

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
      @Nullable SoundEventLayer[] layers,
      int audioCategory
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
      this.layers = layers;
      this.audioCategory = audioCategory;
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
      this.layers = other.layers;
      this.audioCategory = other.audioCategory;
   }

   @Nonnull
   public static SoundEvent deserialize(@Nonnull ByteBuf buf, int offset) {
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
      obj.audioCategory = buf.getIntLE(offset + 30);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 42 + buf.getIntLE(offset + 34);
         int idLen = VarInt.peek(buf, varPos0);
         if (idLen < 0) {
            throw ProtocolException.negativeLength("Id", idLen);
         }

         if (idLen > 4096000) {
            throw ProtocolException.stringTooLong("Id", idLen, 4096000);
         }

         obj.id = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 42 + buf.getIntLE(offset + 38);
         int layersCount = VarInt.peek(buf, varPos1);
         if (layersCount < 0) {
            throw ProtocolException.negativeLength("Layers", layersCount);
         }

         if (layersCount > 4096000) {
            throw ProtocolException.arrayTooLong("Layers", layersCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos1);
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

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 42;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 34);
         int pos0 = offset + 42 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 38);
         int pos1 = offset + 42 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1);

         for (int i = 0; i < arrLen; i++) {
            pos1 += SoundEventLayer.computeBytesConsumed(buf, pos1);
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
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

      buf.writeByte(nullBits);
      buf.writeFloatLE(this.volume);
      buf.writeFloatLE(this.pitch);
      buf.writeFloatLE(this.musicDuckingVolume);
      buf.writeFloatLE(this.ambientDuckingVolume);
      buf.writeIntLE(this.maxInstance);
      buf.writeByte(this.preventSoundInterruption ? 1 : 0);
      buf.writeFloatLE(this.startAttenuationDistance);
      buf.writeFloatLE(this.maxDistance);
      buf.writeIntLE(this.audioCategory);
      int idOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int layersOffsetSlot = buf.writerIndex();
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
   }

   public int computeSize() {
      int size = 42;
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

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 42) {
         return ValidationResult.error("Buffer too small: expected at least 42 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int idOffset = buffer.getIntLE(offset + 34);
            if (idOffset < 0) {
               return ValidationResult.error("Invalid offset for Id");
            }

            int pos = offset + 42 + idOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Id");
            }

            int idLen = VarInt.peek(buffer, pos);
            if (idLen < 0) {
               return ValidationResult.error("Invalid string length for Id");
            }

            if (idLen > 4096000) {
               return ValidationResult.error("Id exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += idLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Id");
            }
         }

         if ((nullBits & 2) != 0) {
            int layersOffset = buffer.getIntLE(offset + 38);
            if (layersOffset < 0) {
               return ValidationResult.error("Invalid offset for Layers");
            }

            int posx = offset + 42 + layersOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Layers");
            }

            int layersCount = VarInt.peek(buffer, posx);
            if (layersCount < 0) {
               return ValidationResult.error("Invalid array count for Layers");
            }

            if (layersCount > 4096000) {
               return ValidationResult.error("Layers exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);

            for (int i = 0; i < layersCount; i++) {
               ValidationResult structResult = SoundEventLayer.validateStructure(buffer, posx);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid SoundEventLayer in Layers[" + i + "]: " + structResult.error());
               }

               posx += SoundEventLayer.computeBytesConsumed(buffer, posx);
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
      copy.layers = this.layers != null ? Arrays.stream(this.layers).map(e -> e.clone()).toArray(SoundEventLayer[]::new) : null;
      copy.audioCategory = this.audioCategory;
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
               && Arrays.equals((Object[])this.layers, (Object[])other.layers)
               && this.audioCategory == other.audioCategory;
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
      result = 31 * result + Arrays.hashCode((Object[])this.layers);
      return 31 * result + Integer.hashCode(this.audioCategory);
   }
}
