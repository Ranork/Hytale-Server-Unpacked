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

public class AmbienceFX {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 22;
   public static final int VARIABLE_FIELD_COUNT = 6;
   public static final int VARIABLE_BLOCK_START = 46;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public String id;
   @Nullable
   public AmbienceFXConditions conditions;
   @Nullable
   public AmbienceFXSound[] sounds;
   public int musicContainerIndex;
   @Nullable
   public AmbienceFXAmbientBed ambientBed;
   @Nullable
   public AmbienceFXSoundEffect soundEffect;
   public int priority;
   @Nullable
   public int[] blockedAmbienceFxIndices;
   public int audioCategoryIndex;
   @Nullable
   public AmbienceStateWrite[] setStates;

   public AmbienceFX() {
   }

   public AmbienceFX(
      @Nullable String id,
      @Nullable AmbienceFXConditions conditions,
      @Nullable AmbienceFXSound[] sounds,
      int musicContainerIndex,
      @Nullable AmbienceFXAmbientBed ambientBed,
      @Nullable AmbienceFXSoundEffect soundEffect,
      int priority,
      @Nullable int[] blockedAmbienceFxIndices,
      int audioCategoryIndex,
      @Nullable AmbienceStateWrite[] setStates
   ) {
      this.id = id;
      this.conditions = conditions;
      this.sounds = sounds;
      this.musicContainerIndex = musicContainerIndex;
      this.ambientBed = ambientBed;
      this.soundEffect = soundEffect;
      this.priority = priority;
      this.blockedAmbienceFxIndices = blockedAmbienceFxIndices;
      this.audioCategoryIndex = audioCategoryIndex;
      this.setStates = setStates;
   }

   public AmbienceFX(@Nonnull AmbienceFX other) {
      this.id = other.id;
      this.conditions = other.conditions;
      this.sounds = other.sounds;
      this.musicContainerIndex = other.musicContainerIndex;
      this.ambientBed = other.ambientBed;
      this.soundEffect = other.soundEffect;
      this.priority = other.priority;
      this.blockedAmbienceFxIndices = other.blockedAmbienceFxIndices;
      this.audioCategoryIndex = other.audioCategoryIndex;
      this.setStates = other.setStates;
   }

   @Nonnull
   public static AmbienceFX deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 46) {
         throw ProtocolException.bufferTooSmall("AmbienceFX", 46, buf.readableBytes() - offset);
      } else {
         AmbienceFX obj = new AmbienceFX();
         byte nullBits = buf.getByte(offset);
         obj.musicContainerIndex = buf.getIntLE(offset + 1);
         if ((nullBits & 1) != 0) {
            obj.soundEffect = AmbienceFXSoundEffect.deserialize(buf, offset + 5);
         }

         obj.priority = buf.getIntLE(offset + 14);
         obj.audioCategoryIndex = buf.getIntLE(offset + 18);
         if ((nullBits & 2) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 22);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 46) {
               throw ProtocolException.invalidOffset("Id", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 46 + varPosBase0;
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

         if ((nullBits & 4) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 26);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 46) {
               throw ProtocolException.invalidOffset("Conditions", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 46 + varPosBase1;
            obj.conditions = AmbienceFXConditions.deserialize(buf, varPos1);
         }

         if ((nullBits & 8) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 30);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 46) {
               throw ProtocolException.invalidOffset("Sounds", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 46 + varPosBase2;
            int soundsCount = VarInt.peek(buf, varPos2);
            if (soundsCount < 0) {
               throw ProtocolException.invalidVarInt("Sounds");
            }

            int varIntLen = VarInt.size(soundsCount);
            if (soundsCount > 4096000) {
               throw ProtocolException.arrayTooLong("Sounds", soundsCount, 4096000);
            }

            if (varPos2 + varIntLen + soundsCount * 33L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Sounds", varPos2 + varIntLen + soundsCount * 33, buf.readableBytes());
            }

            obj.sounds = new AmbienceFXSound[soundsCount];
            int elemPos = varPos2 + varIntLen;

            for (int i = 0; i < soundsCount; i++) {
               obj.sounds[i] = AmbienceFXSound.deserialize(buf, elemPos);
               elemPos += AmbienceFXSound.computeBytesConsumed(buf, elemPos);
            }
         }

         if ((nullBits & 16) != 0) {
            int varPosBase3 = buf.getIntLE(offset + 34);
            if (varPosBase3 < 0 || varPosBase3 > buf.writerIndex() - offset - 46) {
               throw ProtocolException.invalidOffset("AmbientBed", varPosBase3, buf.readableBytes());
            }

            int varPos3 = offset + 46 + varPosBase3;
            obj.ambientBed = AmbienceFXAmbientBed.deserialize(buf, varPos3);
         }

         if ((nullBits & 32) != 0) {
            int varPosBase4 = buf.getIntLE(offset + 38);
            if (varPosBase4 < 0 || varPosBase4 > buf.writerIndex() - offset - 46) {
               throw ProtocolException.invalidOffset("BlockedAmbienceFxIndices", varPosBase4, buf.readableBytes());
            }

            int varPos4 = offset + 46 + varPosBase4;
            int blockedAmbienceFxIndicesCount = VarInt.peek(buf, varPos4);
            if (blockedAmbienceFxIndicesCount < 0) {
               throw ProtocolException.invalidVarInt("BlockedAmbienceFxIndices");
            }

            int varIntLenx = VarInt.size(blockedAmbienceFxIndicesCount);
            if (blockedAmbienceFxIndicesCount > 4096000) {
               throw ProtocolException.arrayTooLong("BlockedAmbienceFxIndices", blockedAmbienceFxIndicesCount, 4096000);
            }

            if (varPos4 + varIntLenx + blockedAmbienceFxIndicesCount * 4L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("BlockedAmbienceFxIndices", varPos4 + varIntLenx + blockedAmbienceFxIndicesCount * 4, buf.readableBytes());
            }

            obj.blockedAmbienceFxIndices = new int[blockedAmbienceFxIndicesCount];

            for (int i = 0; i < blockedAmbienceFxIndicesCount; i++) {
               obj.blockedAmbienceFxIndices[i] = buf.getIntLE(varPos4 + varIntLenx + i * 4);
            }
         }

         if ((nullBits & 64) != 0) {
            int varPosBase5 = buf.getIntLE(offset + 42);
            if (varPosBase5 < 0 || varPosBase5 > buf.writerIndex() - offset - 46) {
               throw ProtocolException.invalidOffset("SetStates", varPosBase5, buf.readableBytes());
            }

            int varPos5 = offset + 46 + varPosBase5;
            int setStatesCount = VarInt.peek(buf, varPos5);
            if (setStatesCount < 0) {
               throw ProtocolException.invalidVarInt("SetStates");
            }

            int varIntLenxx = VarInt.size(setStatesCount);
            if (setStatesCount > 4096000) {
               throw ProtocolException.arrayTooLong("SetStates", setStatesCount, 4096000);
            }

            if (varPos5 + varIntLenxx + setStatesCount * 24L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("SetStates", varPos5 + varIntLenxx + setStatesCount * 24, buf.readableBytes());
            }

            obj.setStates = new AmbienceStateWrite[setStatesCount];
            int elemPos = varPos5 + varIntLenxx;

            for (int i = 0; i < setStatesCount; i++) {
               obj.setStates[i] = AmbienceStateWrite.deserialize(buf, elemPos);
               elemPos += AmbienceStateWrite.computeBytesConsumed(buf, elemPos);
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 46;
      if ((nullBits & 2) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 22);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 46) {
            throw ProtocolException.invalidOffset("Id", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 46 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 26);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 46) {
            throw ProtocolException.invalidOffset("Conditions", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 46 + fieldOffset1;
         pos1 += AmbienceFXConditions.computeBytesConsumed(buf, pos1);
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 30);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 46) {
            throw ProtocolException.invalidOffset("Sounds", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 46 + fieldOffset2;
         int arrLen = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos2 += AmbienceFXSound.computeBytesConsumed(buf, pos2);
         }

         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits & 16) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 34);
         if (fieldOffset3 < 0 || fieldOffset3 > buf.writerIndex() - offset - 46) {
            throw ProtocolException.invalidOffset("AmbientBed", fieldOffset3, maxEnd);
         }

         int pos3 = offset + 46 + fieldOffset3;
         pos3 += AmbienceFXAmbientBed.computeBytesConsumed(buf, pos3);
         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      if ((nullBits & 32) != 0) {
         int fieldOffset4 = buf.getIntLE(offset + 38);
         if (fieldOffset4 < 0 || fieldOffset4 > buf.writerIndex() - offset - 46) {
            throw ProtocolException.invalidOffset("BlockedAmbienceFxIndices", fieldOffset4, maxEnd);
         }

         int pos4 = offset + 46 + fieldOffset4;
         int arrLen = VarInt.peek(buf, pos4);
         pos4 += VarInt.size(arrLen) + arrLen * 4;
         if (pos4 - offset > maxEnd) {
            maxEnd = pos4 - offset;
         }
      }

      if ((nullBits & 64) != 0) {
         int fieldOffset5 = buf.getIntLE(offset + 42);
         if (fieldOffset5 < 0 || fieldOffset5 > buf.writerIndex() - offset - 46) {
            throw ProtocolException.invalidOffset("SetStates", fieldOffset5, maxEnd);
         }

         int pos5 = offset + 46 + fieldOffset5;
         int arrLen = VarInt.peek(buf, pos5);
         pos5 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos5 += AmbienceStateWrite.computeBytesConsumed(buf, pos5);
         }

         if (pos5 - offset > maxEnd) {
            maxEnd = pos5 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 46L;
   }

   @Nullable
   public static String getId(MemorySegment mem) {
      return getId(mem, 0);
   }

   @Nullable
   public static String getId(MemorySegment mem, int offset) {
      return hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 22, 46, "Id"), 4096000, PacketIO.UTF8) : null;
   }

   @Nullable
   public static AmbienceFXConditions getConditions(MemorySegment mem) {
      return getConditions(mem, 0);
   }

   @Nullable
   public static AmbienceFXConditions getConditions(MemorySegment mem, int offset) {
      return hasConditions(mem, offset) ? AmbienceFXConditions.toObject(mem, offset + getValidatedOffset(mem, offset, 26, 46, "Conditions")) : null;
   }

   @Nullable
   public static AmbienceFXSound[] getSounds(MemorySegment mem) {
      return getSounds(mem, 0);
   }

   @Nullable
   public static AmbienceFXSound[] getSounds(MemorySegment mem, int offset) {
      if (!hasSounds(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 30, 46, "Sounds");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Sounds", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Sounds", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Sounds", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               AmbienceFXSound[] data = new AmbienceFXSound[len];

               for (int i = 0; i < len; i++) {
                  data[i] = AmbienceFXSound.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   public static int getMusicContainerIndex(MemorySegment mem) {
      return getMusicContainerIndex(mem, 0);
   }

   public static int getMusicContainerIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   @Nullable
   public static AmbienceFXAmbientBed getAmbientBed(MemorySegment mem) {
      return getAmbientBed(mem, 0);
   }

   @Nullable
   public static AmbienceFXAmbientBed getAmbientBed(MemorySegment mem, int offset) {
      return hasAmbientBed(mem, offset) ? AmbienceFXAmbientBed.toObject(mem, offset + getValidatedOffset(mem, offset, 34, 46, "AmbientBed")) : null;
   }

   @Nullable
   public static AmbienceFXSoundEffect getSoundEffect(MemorySegment mem) {
      return getSoundEffect(mem, 0);
   }

   @Nullable
   public static AmbienceFXSoundEffect getSoundEffect(MemorySegment mem, int offset) {
      return hasSoundEffect(mem, offset) ? AmbienceFXSoundEffect.toObject(mem, offset + 5) : null;
   }

   public static int getPriority(MemorySegment mem) {
      return getPriority(mem, 0);
   }

   public static int getPriority(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 14));
   }

   @Nullable
   public static int[] getBlockedAmbienceFxIndices(MemorySegment mem) {
      return getBlockedAmbienceFxIndices(mem, 0);
   }

   @Nullable
   public static int[] getBlockedAmbienceFxIndices(MemorySegment mem, int offset) {
      if (!hasBlockedAmbienceFxIndices(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 38, 46, "BlockedAmbienceFxIndices");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("BlockedAmbienceFxIndices", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("BlockedAmbienceFxIndices", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 4L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("BlockedAmbienceFxIndices", off + lenOffset + len * 4, (int)mem.byteSize());
            } else {
               off += lenOffset;
               int[] data = new int[len];
               MemorySegment.copy(mem, PacketIO.PROTO_INT, off, data, 0, len);
               return data;
            }
         }
      }
   }

   public static int getAudioCategoryIndex(MemorySegment mem) {
      return getAudioCategoryIndex(mem, 0);
   }

   public static int getAudioCategoryIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 18));
   }

   @Nullable
   public static AmbienceStateWrite[] getSetStates(MemorySegment mem) {
      return getSetStates(mem, 0);
   }

   @Nullable
   public static AmbienceStateWrite[] getSetStates(MemorySegment mem, int offset) {
      if (!hasSetStates(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 42, 46, "SetStates");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("SetStates", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("SetStates", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 24L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("SetStates", off + lenOffset + len * 24, (int)mem.byteSize());
            } else {
               off += lenOffset;
               AmbienceStateWrite[] data = new AmbienceStateWrite[len];

               for (int i = 0; i < len; i++) {
                  data[i] = AmbienceStateWrite.toObject(mem, off + i * 24);
               }

               return data;
            }
         }
      }
   }

   public static boolean hasSoundEffect(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasConditions(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasSounds(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   public static boolean hasAmbientBed(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 16) != 0;
   }

   public static boolean hasBlockedAmbienceFxIndices(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 32) != 0;
   }

   public static boolean hasSetStates(MemorySegment mem, int offset) {
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

   public static AmbienceFX toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AmbienceFX toObject(MemorySegment mem, int offset) {
      if (offset + 46 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AmbienceFX", offset + 46, (int)mem.byteSize());
      } else {
         AmbienceFXSound[] sounds = null;
         if (hasSounds(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 30, 46, "Sounds");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Sounds", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Sounds", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Sounds", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            sounds = new AmbienceFXSound[len];

            for (int i = 0; i < len; i++) {
               sounds[i] = AmbienceFXSound.toObject(mem, off);
               off += sounds[i].computeSize();
            }
         }

         int[] blockedAmbienceFxIndices = null;
         if (hasBlockedAmbienceFxIndices(mem, offset)) {
            int offx = offset + getValidatedOffset(mem, offset, 38, 46, "BlockedAmbienceFxIndices");
            long packedx = VarInt.getWithLength(mem, offx);
            int lenx = (int)packedx;
            if (lenx < 0) {
               throw ProtocolException.negativeLength("BlockedAmbienceFxIndices", lenx);
            }

            if (lenx > 4096000) {
               throw ProtocolException.arrayTooLong("BlockedAmbienceFxIndices", lenx, 4096000);
            }

            int lenOffset = (int)(packedx >>> 32);
            if (offx + lenOffset + lenx * 4L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("BlockedAmbienceFxIndices", offx + lenOffset + lenx * 4, (int)mem.byteSize());
            }

            offx += lenOffset;
            blockedAmbienceFxIndices = new int[lenx];
            MemorySegment.copy(mem, PacketIO.PROTO_INT, offx, blockedAmbienceFxIndices, 0, lenx);
         }

         AmbienceStateWrite[] setStates = null;
         if (hasSetStates(mem, offset)) {
            int offxx = offset + getValidatedOffset(mem, offset, 42, 46, "SetStates");
            long packedxx = VarInt.getWithLength(mem, offxx);
            int lenxx = (int)packedxx;
            if (lenxx < 0) {
               throw ProtocolException.negativeLength("SetStates", lenxx);
            }

            if (lenxx > 4096000) {
               throw ProtocolException.arrayTooLong("SetStates", lenxx, 4096000);
            }

            int lenOffset = (int)(packedxx >>> 32);
            if (offxx + lenOffset + lenxx * 24L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("SetStates", offxx + lenOffset + lenxx * 24, (int)mem.byteSize());
            }

            offxx += lenOffset;
            setStates = new AmbienceStateWrite[lenxx];

            for (int i = 0; i < lenxx; i++) {
               setStates[i] = AmbienceStateWrite.toObject(mem, offxx + i * 24);
            }
         }

         return new AmbienceFX(
            hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 22, 46, "Id"), 4096000, PacketIO.UTF8) : null,
            hasConditions(mem, offset) ? AmbienceFXConditions.toObject(mem, offset + getValidatedOffset(mem, offset, 26, 46, "Conditions")) : null,
            sounds,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            hasAmbientBed(mem, offset) ? AmbienceFXAmbientBed.toObject(mem, offset + getValidatedOffset(mem, offset, 34, 46, "AmbientBed")) : null,
            hasSoundEffect(mem, offset) ? AmbienceFXSoundEffect.toObject(mem, offset + 5) : null,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 14)),
            blockedAmbienceFxIndices,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 18)),
            setStates
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.soundEffect != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.id != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.conditions != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.sounds != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.ambientBed != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.blockedAmbienceFxIndices != null) {
         nullBits = (byte)(nullBits | 32);
      }

      if (this.setStates != null) {
         nullBits = (byte)(nullBits | 64);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.musicContainerIndex);
      if (this.soundEffect != null) {
         this.soundEffect.serialize(buf);
      } else {
         buf.writeZero(9);
      }

      buf.writeIntLE(this.priority);
      buf.writeIntLE(this.audioCategoryIndex);
      int idOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int conditionsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int soundsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int ambientBedOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int blockedAmbienceFxIndicesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int setStatesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.id != null) {
         buf.setIntLE(idOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.id, 4096000);
      } else {
         buf.setIntLE(idOffsetSlot, -1);
      }

      if (this.conditions != null) {
         buf.setIntLE(conditionsOffsetSlot, buf.writerIndex() - varBlockStart);
         this.conditions.serialize(buf);
      } else {
         buf.setIntLE(conditionsOffsetSlot, -1);
      }

      if (this.sounds != null) {
         buf.setIntLE(soundsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.sounds.length > 4096000) {
            throw ProtocolException.arrayTooLong("Sounds", this.sounds.length, 4096000);
         }

         VarInt.write(buf, this.sounds.length);

         for (AmbienceFXSound item : this.sounds) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(soundsOffsetSlot, -1);
      }

      if (this.ambientBed != null) {
         buf.setIntLE(ambientBedOffsetSlot, buf.writerIndex() - varBlockStart);
         this.ambientBed.serialize(buf);
      } else {
         buf.setIntLE(ambientBedOffsetSlot, -1);
      }

      if (this.blockedAmbienceFxIndices != null) {
         buf.setIntLE(blockedAmbienceFxIndicesOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.blockedAmbienceFxIndices.length > 4096000) {
            throw ProtocolException.arrayTooLong("BlockedAmbienceFxIndices", this.blockedAmbienceFxIndices.length, 4096000);
         }

         VarInt.write(buf, this.blockedAmbienceFxIndices.length);

         for (int item : this.blockedAmbienceFxIndices) {
            buf.writeIntLE(item);
         }
      } else {
         buf.setIntLE(blockedAmbienceFxIndicesOffsetSlot, -1);
      }

      if (this.setStates != null) {
         buf.setIntLE(setStatesOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.setStates.length > 4096000) {
            throw ProtocolException.arrayTooLong("SetStates", this.setStates.length, 4096000);
         }

         VarInt.write(buf, this.setStates.length);

         for (AmbienceStateWrite item : this.setStates) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(setStatesOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.soundEffect != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.id != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.conditions != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.sounds != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.ambientBed != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.blockedAmbienceFxIndices != null) {
         nullBits = (byte)(nullBits | 32);
      }

      if (this.setStates != null) {
         nullBits = (byte)(nullBits | 64);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.musicContainerIndex);
      if (this.soundEffect != null) {
         this.soundEffect.serialize(mem, offset + 5);
      } else {
         mem.asSlice(offset + 5, 9L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_INT, (long)(offset + 14), this.priority);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 18), this.audioCategoryIndex);
      int varOffset = offset + 46;
      if (this.id != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 22), varOffset - offset - 46);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.id, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 22), -1);
      }

      if (this.conditions != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 26), varOffset - offset - 46);
         varOffset += this.conditions.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 26), -1);
      }

      if (this.sounds != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 30), varOffset - offset - 46);
         if (this.sounds.length > 4096000) {
            throw ProtocolException.arrayTooLong("Sounds", this.sounds.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.sounds.length);
         int soundsValueOffset = 0;

         for (int i = 0; i < this.sounds.length; i++) {
            soundsValueOffset += this.sounds[i].serialize(mem, varOffset + soundsValueOffset);
         }

         varOffset += soundsValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 30), -1);
      }

      if (this.ambientBed != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 34), varOffset - offset - 46);
         varOffset += this.ambientBed.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 34), -1);
      }

      if (this.blockedAmbienceFxIndices != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 38), varOffset - offset - 46);
         if (this.blockedAmbienceFxIndices.length > 4096000) {
            throw ProtocolException.arrayTooLong("BlockedAmbienceFxIndices", this.blockedAmbienceFxIndices.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.blockedAmbienceFxIndices.length);
         MemorySegment.copy(this.blockedAmbienceFxIndices, 0, mem, PacketIO.PROTO_INT, varOffset, this.blockedAmbienceFxIndices.length);
         varOffset += this.blockedAmbienceFxIndices.length * 4;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 38), -1);
      }

      if (this.setStates != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 42), varOffset - offset - 46);
         if (this.setStates.length > 4096000) {
            throw ProtocolException.arrayTooLong("SetStates", this.setStates.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.setStates.length);
         int setStatesValueOffset = 0;

         for (int i = 0; i < this.setStates.length; i++) {
            setStatesValueOffset += this.setStates[i].serialize(mem, varOffset + setStatesValueOffset);
         }

         varOffset += setStatesValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 42), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 46;
      if (this.id != null) {
         size += PacketIO.stringSize(this.id);
      }

      if (this.conditions != null) {
         size += this.conditions.computeSize();
      }

      if (this.sounds != null) {
         int soundsSize = 0;

         for (AmbienceFXSound elem : this.sounds) {
            soundsSize += elem.computeSize();
         }

         size += VarInt.size(this.sounds.length) + soundsSize;
      }

      if (this.ambientBed != null) {
         size += this.ambientBed.computeSize();
      }

      if (this.blockedAmbienceFxIndices != null) {
         size += VarInt.size(this.blockedAmbienceFxIndices.length) + this.blockedAmbienceFxIndices.length * 4;
      }

      if (this.setStates != null) {
         size += VarInt.size(this.setStates.length) + this.setStates.length * 24;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 46) {
         return ValidationResult.error("Buffer too small: expected at least 46 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 2) != 0) {
            int idOffset = buffer.getIntLE(offset + 22);
            if (idOffset < 0 || idOffset > buffer.writerIndex() - offset - 46) {
               return ValidationResult.error("Invalid offset for Id");
            }

            int pos = offset + 46 + idOffset;
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

         if ((nullBits & 4) != 0) {
            int conditionsOffset = buffer.getIntLE(offset + 26);
            if (conditionsOffset < 0 || conditionsOffset > buffer.writerIndex() - offset - 46) {
               return ValidationResult.error("Invalid offset for Conditions");
            }

            int posx = offset + 46 + conditionsOffset;
            ValidationResult conditionsResult = AmbienceFXConditions.validateStructure(buffer, posx);
            if (!conditionsResult.isValid()) {
               return ValidationResult.error("Invalid Conditions: " + conditionsResult.error());
            }

            posx += AmbienceFXConditions.computeBytesConsumed(buffer, posx);
         }

         if ((nullBits & 8) != 0) {
            int soundsOffset = buffer.getIntLE(offset + 30);
            if (soundsOffset < 0 || soundsOffset > buffer.writerIndex() - offset - 46) {
               return ValidationResult.error("Invalid offset for Sounds");
            }

            int posx = offset + 46 + soundsOffset;
            int soundsCount = VarInt.peek(buffer, posx);
            if (soundsCount < 0) {
               return ValidationResult.error("Invalid array count for Sounds");
            }

            if (soundsCount > 4096000) {
               return ValidationResult.error("Sounds exceeds max length 4096000");
            }

            posx += VarInt.size(soundsCount);

            for (int i = 0; i < soundsCount; i++) {
               ValidationResult structResult = AmbienceFXSound.validateStructure(buffer, posx);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid AmbienceFXSound in Sounds[" + i + "]: " + structResult.error());
               }

               posx += AmbienceFXSound.computeBytesConsumed(buffer, posx);
            }
         }

         if ((nullBits & 16) != 0) {
            int ambientBedOffset = buffer.getIntLE(offset + 34);
            if (ambientBedOffset < 0 || ambientBedOffset > buffer.writerIndex() - offset - 46) {
               return ValidationResult.error("Invalid offset for AmbientBed");
            }

            int posxx = offset + 46 + ambientBedOffset;
            ValidationResult ambientBedResult = AmbienceFXAmbientBed.validateStructure(buffer, posxx);
            if (!ambientBedResult.isValid()) {
               return ValidationResult.error("Invalid AmbientBed: " + ambientBedResult.error());
            }

            posxx += AmbienceFXAmbientBed.computeBytesConsumed(buffer, posxx);
         }

         if ((nullBits & 32) != 0) {
            int blockedAmbienceFxIndicesOffset = buffer.getIntLE(offset + 38);
            if (blockedAmbienceFxIndicesOffset < 0 || blockedAmbienceFxIndicesOffset > buffer.writerIndex() - offset - 46) {
               return ValidationResult.error("Invalid offset for BlockedAmbienceFxIndices");
            }

            int posxx = offset + 46 + blockedAmbienceFxIndicesOffset;
            int blockedAmbienceFxIndicesCount = VarInt.peek(buffer, posxx);
            if (blockedAmbienceFxIndicesCount < 0) {
               return ValidationResult.error("Invalid array count for BlockedAmbienceFxIndices");
            }

            if (blockedAmbienceFxIndicesCount > 4096000) {
               return ValidationResult.error("BlockedAmbienceFxIndices exceeds max length 4096000");
            }

            posxx += VarInt.size(blockedAmbienceFxIndicesCount);
            posxx += blockedAmbienceFxIndicesCount * 4;
            if (posxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading BlockedAmbienceFxIndices");
            }
         }

         if ((nullBits & 64) != 0) {
            int setStatesOffset = buffer.getIntLE(offset + 42);
            if (setStatesOffset < 0 || setStatesOffset > buffer.writerIndex() - offset - 46) {
               return ValidationResult.error("Invalid offset for SetStates");
            }

            int posxxx = offset + 46 + setStatesOffset;
            int setStatesCount = VarInt.peek(buffer, posxxx);
            if (setStatesCount < 0) {
               return ValidationResult.error("Invalid array count for SetStates");
            }

            if (setStatesCount > 4096000) {
               return ValidationResult.error("SetStates exceeds max length 4096000");
            }

            posxxx += VarInt.size(setStatesCount);
            posxxx += setStatesCount * 24;
            if (posxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading SetStates");
            }
         }

         return ValidationResult.OK;
      }
   }

   public AmbienceFX clone() {
      AmbienceFX copy = new AmbienceFX();
      copy.id = this.id;
      copy.conditions = this.conditions != null ? this.conditions.clone() : null;
      copy.sounds = this.sounds != null ? Arrays.stream(this.sounds).map(e -> e.clone()).toArray(AmbienceFXSound[]::new) : null;
      copy.musicContainerIndex = this.musicContainerIndex;
      copy.ambientBed = this.ambientBed != null ? this.ambientBed.clone() : null;
      copy.soundEffect = this.soundEffect != null ? this.soundEffect.clone() : null;
      copy.priority = this.priority;
      copy.blockedAmbienceFxIndices = this.blockedAmbienceFxIndices != null
         ? Arrays.copyOf(this.blockedAmbienceFxIndices, this.blockedAmbienceFxIndices.length)
         : null;
      copy.audioCategoryIndex = this.audioCategoryIndex;
      copy.setStates = this.setStates != null ? Arrays.stream(this.setStates).map(e -> e.clone()).toArray(AmbienceStateWrite[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AmbienceFX other)
            ? false
            : Objects.equals(this.id, other.id)
               && Objects.equals(this.conditions, other.conditions)
               && Arrays.equals((Object[])this.sounds, (Object[])other.sounds)
               && this.musicContainerIndex == other.musicContainerIndex
               && Objects.equals(this.ambientBed, other.ambientBed)
               && Objects.equals(this.soundEffect, other.soundEffect)
               && this.priority == other.priority
               && Arrays.equals(this.blockedAmbienceFxIndices, other.blockedAmbienceFxIndices)
               && this.audioCategoryIndex == other.audioCategoryIndex
               && Arrays.equals((Object[])this.setStates, (Object[])other.setStates);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.id);
      result = 31 * result + Objects.hashCode(this.conditions);
      result = 31 * result + Arrays.hashCode((Object[])this.sounds);
      result = 31 * result + Integer.hashCode(this.musicContainerIndex);
      result = 31 * result + Objects.hashCode(this.ambientBed);
      result = 31 * result + Objects.hashCode(this.soundEffect);
      result = 31 * result + Integer.hashCode(this.priority);
      result = 31 * result + Arrays.hashCode(this.blockedAmbienceFxIndices);
      result = 31 * result + Integer.hashCode(this.audioCategoryIndex);
      return 31 * result + Arrays.hashCode((Object[])this.setStates);
   }
}
