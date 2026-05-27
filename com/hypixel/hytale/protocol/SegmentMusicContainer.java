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

public class SegmentMusicContainer extends MusicContainer {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 87;
   public static final int VARIABLE_FIELD_COUNT = 3;
   public static final int VARIABLE_BLOCK_START = 99;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public LayerPlacement[] layers;
   @Nullable
   public BarBeatDuration entryMarker;
   @Nullable
   public BarBeatDuration exitMarker;

   public SegmentMusicContainer() {
   }

   public SegmentMusicContainer(
      float volume,
      int loopCount,
      float weight,
      @Nullable Rangef silenceAfter,
      @Nullable Rangef exitSilence,
      float fadeInDuration,
      float fadeOutDuration,
      @Nonnull MusicTransitionType transitionType,
      float transitionDuration,
      boolean playToCompletion,
      float resumeMemoryDuration,
      @Nullable String nameTranslationKey,
      int audioCategoryIndex,
      @Nullable TempoSettings tempo,
      @Nullable StateBinding[] stateBindings,
      @Nullable LayerPlacement[] layers,
      @Nullable BarBeatDuration entryMarker,
      @Nullable BarBeatDuration exitMarker
   ) {
      this.volume = volume;
      this.loopCount = loopCount;
      this.weight = weight;
      this.silenceAfter = silenceAfter;
      this.exitSilence = exitSilence;
      this.fadeInDuration = fadeInDuration;
      this.fadeOutDuration = fadeOutDuration;
      this.transitionType = transitionType;
      this.transitionDuration = transitionDuration;
      this.playToCompletion = playToCompletion;
      this.resumeMemoryDuration = resumeMemoryDuration;
      this.nameTranslationKey = nameTranslationKey;
      this.audioCategoryIndex = audioCategoryIndex;
      this.tempo = tempo;
      this.stateBindings = stateBindings;
      this.layers = layers;
      this.entryMarker = entryMarker;
      this.exitMarker = exitMarker;
   }

   public SegmentMusicContainer(@Nonnull SegmentMusicContainer other) {
      this.volume = other.volume;
      this.loopCount = other.loopCount;
      this.weight = other.weight;
      this.silenceAfter = other.silenceAfter;
      this.exitSilence = other.exitSilence;
      this.fadeInDuration = other.fadeInDuration;
      this.fadeOutDuration = other.fadeOutDuration;
      this.transitionType = other.transitionType;
      this.transitionDuration = other.transitionDuration;
      this.playToCompletion = other.playToCompletion;
      this.resumeMemoryDuration = other.resumeMemoryDuration;
      this.nameTranslationKey = other.nameTranslationKey;
      this.audioCategoryIndex = other.audioCategoryIndex;
      this.tempo = other.tempo;
      this.stateBindings = other.stateBindings;
      this.layers = other.layers;
      this.entryMarker = other.entryMarker;
      this.exitMarker = other.exitMarker;
   }

   @Nonnull
   public static SegmentMusicContainer deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 99) {
         throw ProtocolException.bufferTooSmall("SegmentMusicContainer", 99, buf.readableBytes() - offset);
      } else {
         SegmentMusicContainer obj = new SegmentMusicContainer();
         byte nullBits = buf.getByte(offset);
         obj.volume = buf.getFloatLE(offset + 1);
         obj.loopCount = buf.getIntLE(offset + 5);
         obj.weight = buf.getFloatLE(offset + 9);
         if ((nullBits & 1) != 0) {
            obj.silenceAfter = Rangef.deserialize(buf, offset + 13);
         }

         if ((nullBits & 2) != 0) {
            obj.exitSilence = Rangef.deserialize(buf, offset + 21);
         }

         obj.fadeInDuration = buf.getFloatLE(offset + 29);
         obj.fadeOutDuration = buf.getFloatLE(offset + 33);
         obj.transitionType = MusicTransitionType.fromValue(buf.getByte(offset + 37));
         obj.transitionDuration = buf.getFloatLE(offset + 38);
         obj.playToCompletion = buf.getByte(offset + 42) != 0;
         obj.resumeMemoryDuration = buf.getFloatLE(offset + 43);
         obj.audioCategoryIndex = buf.getIntLE(offset + 47);
         if ((nullBits & 4) != 0) {
            obj.tempo = TempoSettings.deserialize(buf, offset + 51);
         }

         if ((nullBits & 8) != 0) {
            obj.entryMarker = BarBeatDuration.deserialize(buf, offset + 63);
         }

         if ((nullBits & 16) != 0) {
            obj.exitMarker = BarBeatDuration.deserialize(buf, offset + 75);
         }

         if ((nullBits & 32) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 87);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 99) {
               throw ProtocolException.invalidOffset("NameTranslationKey", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 99 + varPosBase0;
            int nameTranslationKeyLen = VarInt.peek(buf, varPos0);
            if (nameTranslationKeyLen < 0) {
               throw ProtocolException.invalidVarInt("NameTranslationKey");
            }

            int nameTranslationKeyVarIntLen = VarInt.size(nameTranslationKeyLen);
            if (nameTranslationKeyLen > 4096000) {
               throw ProtocolException.stringTooLong("NameTranslationKey", nameTranslationKeyLen, 4096000);
            }

            if (varPos0 + nameTranslationKeyVarIntLen + nameTranslationKeyLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("NameTranslationKey", varPos0 + nameTranslationKeyVarIntLen + nameTranslationKeyLen, buf.readableBytes());
            }

            obj.nameTranslationKey = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 64) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 91);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 99) {
               throw ProtocolException.invalidOffset("StateBindings", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 99 + varPosBase1;
            int stateBindingsCount = VarInt.peek(buf, varPos1);
            if (stateBindingsCount < 0) {
               throw ProtocolException.invalidVarInt("StateBindings");
            }

            int varIntLen = VarInt.size(stateBindingsCount);
            if (stateBindingsCount > 4096000) {
               throw ProtocolException.arrayTooLong("StateBindings", stateBindingsCount, 4096000);
            }

            if (varPos1 + varIntLen + stateBindingsCount * 5L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("StateBindings", varPos1 + varIntLen + stateBindingsCount * 5, buf.readableBytes());
            }

            obj.stateBindings = new StateBinding[stateBindingsCount];
            int elemPos = varPos1 + varIntLen;

            for (int i = 0; i < stateBindingsCount; i++) {
               obj.stateBindings[i] = StateBinding.deserialize(buf, elemPos);
               elemPos += StateBinding.computeBytesConsumed(buf, elemPos);
            }
         }

         if ((nullBits & 128) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 95);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 99) {
               throw ProtocolException.invalidOffset("Layers", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 99 + varPosBase2;
            int layersCount = VarInt.peek(buf, varPos2);
            if (layersCount < 0) {
               throw ProtocolException.invalidVarInt("Layers");
            }

            int varIntLenx = VarInt.size(layersCount);
            if (layersCount > 4096000) {
               throw ProtocolException.arrayTooLong("Layers", layersCount, 4096000);
            }

            if (varPos2 + varIntLenx + layersCount * 17L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Layers", varPos2 + varIntLenx + layersCount * 17, buf.readableBytes());
            }

            obj.layers = new LayerPlacement[layersCount];
            int elemPos = varPos2 + varIntLenx;

            for (int i = 0; i < layersCount; i++) {
               obj.layers[i] = LayerPlacement.deserialize(buf, elemPos);
               elemPos += LayerPlacement.computeBytesConsumed(buf, elemPos);
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 99;
      if ((nullBits & 32) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 87);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 99) {
            throw ProtocolException.invalidOffset("NameTranslationKey", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 99 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 64) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 91);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 99) {
            throw ProtocolException.invalidOffset("StateBindings", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 99 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos1 += StateBinding.computeBytesConsumed(buf, pos1);
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 128) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 95);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 99) {
            throw ProtocolException.invalidOffset("Layers", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 99 + fieldOffset2;
         int arrLen = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos2 += LayerPlacement.computeBytesConsumed(buf, pos2);
         }

         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 99L;
   }

   public static float getVolume(MemorySegment mem) {
      return getVolume(mem, 0);
   }

   public static float getVolume(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1));
   }

   public static int getLoopCount(MemorySegment mem) {
      return getLoopCount(mem, 0);
   }

   public static int getLoopCount(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 5));
   }

   public static float getWeight(MemorySegment mem) {
      return getWeight(mem, 0);
   }

   public static float getWeight(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 9));
   }

   @Nullable
   public static Rangef getSilenceAfter(MemorySegment mem) {
      return getSilenceAfter(mem, 0);
   }

   @Nullable
   public static Rangef getSilenceAfter(MemorySegment mem, int offset) {
      return hasSilenceAfter(mem, offset) ? Rangef.toObject(mem, offset + 13) : null;
   }

   @Nullable
   public static Rangef getExitSilence(MemorySegment mem) {
      return getExitSilence(mem, 0);
   }

   @Nullable
   public static Rangef getExitSilence(MemorySegment mem, int offset) {
      return hasExitSilence(mem, offset) ? Rangef.toObject(mem, offset + 21) : null;
   }

   public static float getFadeInDuration(MemorySegment mem) {
      return getFadeInDuration(mem, 0);
   }

   public static float getFadeInDuration(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 29));
   }

   public static float getFadeOutDuration(MemorySegment mem) {
      return getFadeOutDuration(mem, 0);
   }

   public static float getFadeOutDuration(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 33));
   }

   public static MusicTransitionType getTransitionType(MemorySegment mem) {
      return getTransitionType(mem, 0);
   }

   public static MusicTransitionType getTransitionType(MemorySegment mem, int offset) {
      return MusicTransitionType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 37)));
   }

   public static float getTransitionDuration(MemorySegment mem) {
      return getTransitionDuration(mem, 0);
   }

   public static float getTransitionDuration(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 38));
   }

   public static boolean getPlayToCompletion(MemorySegment mem) {
      return getPlayToCompletion(mem, 0);
   }

   public static boolean getPlayToCompletion(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 42));
   }

   public static float getResumeMemoryDuration(MemorySegment mem) {
      return getResumeMemoryDuration(mem, 0);
   }

   public static float getResumeMemoryDuration(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 43));
   }

   @Nullable
   public static String getNameTranslationKey(MemorySegment mem) {
      return getNameTranslationKey(mem, 0);
   }

   @Nullable
   public static String getNameTranslationKey(MemorySegment mem, int offset) {
      return hasNameTranslationKey(mem, offset)
         ? PacketIO.readVarString("NameTranslationKey", mem, offset + getValidatedOffset(mem, offset, 87, 99, "NameTranslationKey"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static int getAudioCategoryIndex(MemorySegment mem) {
      return getAudioCategoryIndex(mem, 0);
   }

   public static int getAudioCategoryIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 47));
   }

   @Nullable
   public static TempoSettings getTempo(MemorySegment mem) {
      return getTempo(mem, 0);
   }

   @Nullable
   public static TempoSettings getTempo(MemorySegment mem, int offset) {
      return hasTempo(mem, offset) ? TempoSettings.toObject(mem, offset + 51) : null;
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
         int off = offset + getValidatedOffset(mem, offset, 91, 99, "StateBindings");
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

   @Nullable
   public static LayerPlacement[] getLayers(MemorySegment mem) {
      return getLayers(mem, 0);
   }

   @Nullable
   public static LayerPlacement[] getLayers(MemorySegment mem, int offset) {
      if (!hasLayers(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 95, 99, "Layers");
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
               LayerPlacement[] data = new LayerPlacement[len];

               for (int i = 0; i < len; i++) {
                  data[i] = LayerPlacement.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   @Nullable
   public static BarBeatDuration getEntryMarker(MemorySegment mem) {
      return getEntryMarker(mem, 0);
   }

   @Nullable
   public static BarBeatDuration getEntryMarker(MemorySegment mem, int offset) {
      return hasEntryMarker(mem, offset) ? BarBeatDuration.toObject(mem, offset + 63) : null;
   }

   @Nullable
   public static BarBeatDuration getExitMarker(MemorySegment mem) {
      return getExitMarker(mem, 0);
   }

   @Nullable
   public static BarBeatDuration getExitMarker(MemorySegment mem, int offset) {
      return hasExitMarker(mem, offset) ? BarBeatDuration.toObject(mem, offset + 75) : null;
   }

   public static boolean hasSilenceAfter(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasExitSilence(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasTempo(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasEntryMarker(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   public static boolean hasExitMarker(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 16) != 0;
   }

   public static boolean hasNameTranslationKey(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 32) != 0;
   }

   public static boolean hasStateBindings(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 64) != 0;
   }

   public static boolean hasLayers(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 128) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static SegmentMusicContainer toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static SegmentMusicContainer toObject(MemorySegment mem, int offset) {
      if (offset + 99 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("SegmentMusicContainer", offset + 99, (int)mem.byteSize());
      } else {
         StateBinding[] stateBindings = null;
         if (hasStateBindings(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 91, 99, "StateBindings");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("StateBindings", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("StateBindings", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("StateBindings", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            stateBindings = new StateBinding[len];

            for (int i = 0; i < len; i++) {
               stateBindings[i] = StateBinding.toObject(mem, off);
               off += stateBindings[i].computeSize();
            }
         }

         LayerPlacement[] layers = null;
         if (hasLayers(mem, offset)) {
            int offx = offset + getValidatedOffset(mem, offset, 95, 99, "Layers");
            long packedx = VarInt.getWithLength(mem, offx);
            int lenx = (int)packedx;
            if (lenx < 0) {
               throw ProtocolException.negativeLength("Layers", lenx);
            }

            if (lenx > 4096000) {
               throw ProtocolException.arrayTooLong("Layers", lenx, 4096000);
            }

            int lenOffset = (int)(packedx >>> 32);
            if (offx + lenOffset + lenx > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Layers", offx + lenOffset + lenx, (int)mem.byteSize());
            }

            offx += lenOffset;
            layers = new LayerPlacement[lenx];

            for (int i = 0; i < lenx; i++) {
               layers[i] = LayerPlacement.toObject(mem, offx);
               offx += layers[i].computeSize();
            }
         }

         return new SegmentMusicContainer(
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 5)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 9)),
            hasSilenceAfter(mem, offset) ? Rangef.toObject(mem, offset + 13) : null,
            hasExitSilence(mem, offset) ? Rangef.toObject(mem, offset + 21) : null,
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 29)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 33)),
            MusicTransitionType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 37))),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 38)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 42)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 43)),
            hasNameTranslationKey(mem, offset)
               ? PacketIO.readVarString(
                  "NameTranslationKey", mem, offset + getValidatedOffset(mem, offset, 87, 99, "NameTranslationKey"), 4096000, PacketIO.UTF8
               )
               : null,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 47)),
            hasTempo(mem, offset) ? TempoSettings.toObject(mem, offset + 51) : null,
            stateBindings,
            layers,
            hasEntryMarker(mem, offset) ? BarBeatDuration.toObject(mem, offset + 63) : null,
            hasExitMarker(mem, offset) ? BarBeatDuration.toObject(mem, offset + 75) : null
         );
      }
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.silenceAfter != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.exitSilence != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.tempo != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.entryMarker != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.exitMarker != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.nameTranslationKey != null) {
         nullBits = (byte)(nullBits | 32);
      }

      if (this.stateBindings != null) {
         nullBits = (byte)(nullBits | 64);
      }

      if (this.layers != null) {
         nullBits = (byte)(nullBits | 128);
      }

      buf.writeByte(nullBits);
      buf.writeFloatLE(this.volume);
      buf.writeIntLE(this.loopCount);
      buf.writeFloatLE(this.weight);
      if (this.silenceAfter != null) {
         this.silenceAfter.serialize(buf);
      } else {
         buf.writeZero(8);
      }

      if (this.exitSilence != null) {
         this.exitSilence.serialize(buf);
      } else {
         buf.writeZero(8);
      }

      buf.writeFloatLE(this.fadeInDuration);
      buf.writeFloatLE(this.fadeOutDuration);
      buf.writeByte(this.transitionType.getValue());
      buf.writeFloatLE(this.transitionDuration);
      buf.writeByte(this.playToCompletion ? 1 : 0);
      buf.writeFloatLE(this.resumeMemoryDuration);
      buf.writeIntLE(this.audioCategoryIndex);
      if (this.tempo != null) {
         this.tempo.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      if (this.entryMarker != null) {
         this.entryMarker.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      if (this.exitMarker != null) {
         this.exitMarker.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      int nameTranslationKeyOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int stateBindingsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int layersOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.nameTranslationKey != null) {
         buf.setIntLE(nameTranslationKeyOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.nameTranslationKey, 4096000);
      } else {
         buf.setIntLE(nameTranslationKeyOffsetSlot, -1);
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

      if (this.layers != null) {
         buf.setIntLE(layersOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.layers.length > 4096000) {
            throw ProtocolException.arrayTooLong("Layers", this.layers.length, 4096000);
         }

         VarInt.write(buf, this.layers.length);

         for (LayerPlacement item : this.layers) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(layersOffsetSlot, -1);
      }

      return buf.writerIndex() - startPos;
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.silenceAfter != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.exitSilence != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.tempo != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.entryMarker != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.exitMarker != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.nameTranslationKey != null) {
         nullBits = (byte)(nullBits | 32);
      }

      if (this.stateBindings != null) {
         nullBits = (byte)(nullBits | 64);
      }

      if (this.layers != null) {
         nullBits = (byte)(nullBits | 128);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 1), this.volume);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 5), this.loopCount);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 9), this.weight);
      if (this.silenceAfter != null) {
         this.silenceAfter.serialize(mem, offset + 13);
      } else {
         mem.asSlice(offset + 13, 8L).fill((byte)0);
      }

      if (this.exitSilence != null) {
         this.exitSilence.serialize(mem, offset + 21);
      } else {
         mem.asSlice(offset + 21, 8L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 29), this.fadeInDuration);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 33), this.fadeOutDuration);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 37), (byte)this.transitionType.getValue());
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 38), this.transitionDuration);
      mem.set(PacketIO.PROTO_BOOL, offset + 42, this.playToCompletion);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 43), this.resumeMemoryDuration);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 47), this.audioCategoryIndex);
      if (this.tempo != null) {
         this.tempo.serialize(mem, offset + 51);
      } else {
         mem.asSlice(offset + 51, 12L).fill((byte)0);
      }

      if (this.entryMarker != null) {
         this.entryMarker.serialize(mem, offset + 63);
      } else {
         mem.asSlice(offset + 63, 12L).fill((byte)0);
      }

      if (this.exitMarker != null) {
         this.exitMarker.serialize(mem, offset + 75);
      } else {
         mem.asSlice(offset + 75, 12L).fill((byte)0);
      }

      int varOffset = offset + 99;
      if (this.nameTranslationKey != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 87), varOffset - offset - 99);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.nameTranslationKey, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 87), -1);
      }

      if (this.stateBindings != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 91), varOffset - offset - 99);
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
         mem.set(PacketIO.PROTO_INT, (long)(offset + 91), -1);
      }

      if (this.layers != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 95), varOffset - offset - 99);
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
         mem.set(PacketIO.PROTO_INT, (long)(offset + 95), -1);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 99;
      if (this.nameTranslationKey != null) {
         size += PacketIO.stringSize(this.nameTranslationKey);
      }

      if (this.stateBindings != null) {
         int stateBindingsSize = 0;

         for (StateBinding elem : this.stateBindings) {
            stateBindingsSize += elem.computeSize();
         }

         size += VarInt.size(this.stateBindings.length) + stateBindingsSize;
      }

      if (this.layers != null) {
         int layersSize = 0;

         for (LayerPlacement elem : this.layers) {
            layersSize += elem.computeSize();
         }

         size += VarInt.size(this.layers.length) + layersSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 99) {
         return ValidationResult.error("Buffer too small: expected at least 99 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int v = buffer.getByte(offset + 37) & 255;
         if (v >= 3) {
            return ValidationResult.error("Invalid MusicTransitionType value for TransitionType");
         } else {
            if ((nullBits & 32) != 0) {
               v = buffer.getIntLE(offset + 87);
               if (v < 0 || v > buffer.writerIndex() - offset - 99) {
                  return ValidationResult.error("Invalid offset for NameTranslationKey");
               }

               int pos = offset + 99 + v;
               int nameTranslationKeyLen = VarInt.peek(buffer, pos);
               if (nameTranslationKeyLen < 0) {
                  return ValidationResult.error("Invalid string length for NameTranslationKey");
               }

               if (nameTranslationKeyLen > 4096000) {
                  return ValidationResult.error("NameTranslationKey exceeds max length 4096000");
               }

               pos += VarInt.size(nameTranslationKeyLen);
               pos += nameTranslationKeyLen;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading NameTranslationKey");
               }
            }

            if ((nullBits & 64) != 0) {
               v = buffer.getIntLE(offset + 91);
               if (v < 0 || v > buffer.writerIndex() - offset - 99) {
                  return ValidationResult.error("Invalid offset for StateBindings");
               }

               int posx = offset + 99 + v;
               int stateBindingsCount = VarInt.peek(buffer, posx);
               if (stateBindingsCount < 0) {
                  return ValidationResult.error("Invalid array count for StateBindings");
               }

               if (stateBindingsCount > 4096000) {
                  return ValidationResult.error("StateBindings exceeds max length 4096000");
               }

               posx += VarInt.size(stateBindingsCount);

               for (int i = 0; i < stateBindingsCount; i++) {
                  ValidationResult structResult = StateBinding.validateStructure(buffer, posx);
                  if (!structResult.isValid()) {
                     return ValidationResult.error("Invalid StateBinding in StateBindings[" + i + "]: " + structResult.error());
                  }

                  posx += StateBinding.computeBytesConsumed(buffer, posx);
               }
            }

            if ((nullBits & 128) != 0) {
               v = buffer.getIntLE(offset + 95);
               if (v < 0 || v > buffer.writerIndex() - offset - 99) {
                  return ValidationResult.error("Invalid offset for Layers");
               }

               int posxx = offset + 99 + v;
               int layersCount = VarInt.peek(buffer, posxx);
               if (layersCount < 0) {
                  return ValidationResult.error("Invalid array count for Layers");
               }

               if (layersCount > 4096000) {
                  return ValidationResult.error("Layers exceeds max length 4096000");
               }

               posxx += VarInt.size(layersCount);

               for (int i = 0; i < layersCount; i++) {
                  ValidationResult structResult = LayerPlacement.validateStructure(buffer, posxx);
                  if (!structResult.isValid()) {
                     return ValidationResult.error("Invalid LayerPlacement in Layers[" + i + "]: " + structResult.error());
                  }

                  posxx += LayerPlacement.computeBytesConsumed(buffer, posxx);
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public SegmentMusicContainer clone() {
      SegmentMusicContainer copy = new SegmentMusicContainer();
      copy.volume = this.volume;
      copy.loopCount = this.loopCount;
      copy.weight = this.weight;
      copy.silenceAfter = this.silenceAfter != null ? this.silenceAfter.clone() : null;
      copy.exitSilence = this.exitSilence != null ? this.exitSilence.clone() : null;
      copy.fadeInDuration = this.fadeInDuration;
      copy.fadeOutDuration = this.fadeOutDuration;
      copy.transitionType = this.transitionType;
      copy.transitionDuration = this.transitionDuration;
      copy.playToCompletion = this.playToCompletion;
      copy.resumeMemoryDuration = this.resumeMemoryDuration;
      copy.nameTranslationKey = this.nameTranslationKey;
      copy.audioCategoryIndex = this.audioCategoryIndex;
      copy.tempo = this.tempo != null ? this.tempo.clone() : null;
      copy.stateBindings = this.stateBindings != null ? Arrays.stream(this.stateBindings).map(e -> e.clone()).toArray(StateBinding[]::new) : null;
      copy.layers = this.layers != null ? Arrays.stream(this.layers).map(e -> e.clone()).toArray(LayerPlacement[]::new) : null;
      copy.entryMarker = this.entryMarker != null ? this.entryMarker.clone() : null;
      copy.exitMarker = this.exitMarker != null ? this.exitMarker.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof SegmentMusicContainer other)
            ? false
            : this.volume == other.volume
               && this.loopCount == other.loopCount
               && this.weight == other.weight
               && Objects.equals(this.silenceAfter, other.silenceAfter)
               && Objects.equals(this.exitSilence, other.exitSilence)
               && this.fadeInDuration == other.fadeInDuration
               && this.fadeOutDuration == other.fadeOutDuration
               && Objects.equals(this.transitionType, other.transitionType)
               && this.transitionDuration == other.transitionDuration
               && this.playToCompletion == other.playToCompletion
               && this.resumeMemoryDuration == other.resumeMemoryDuration
               && Objects.equals(this.nameTranslationKey, other.nameTranslationKey)
               && this.audioCategoryIndex == other.audioCategoryIndex
               && Objects.equals(this.tempo, other.tempo)
               && Arrays.equals((Object[])this.stateBindings, (Object[])other.stateBindings)
               && Arrays.equals((Object[])this.layers, (Object[])other.layers)
               && Objects.equals(this.entryMarker, other.entryMarker)
               && Objects.equals(this.exitMarker, other.exitMarker);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Float.hashCode(this.volume);
      result = 31 * result + Integer.hashCode(this.loopCount);
      result = 31 * result + Float.hashCode(this.weight);
      result = 31 * result + Objects.hashCode(this.silenceAfter);
      result = 31 * result + Objects.hashCode(this.exitSilence);
      result = 31 * result + Float.hashCode(this.fadeInDuration);
      result = 31 * result + Float.hashCode(this.fadeOutDuration);
      result = 31 * result + Objects.hashCode(this.transitionType);
      result = 31 * result + Float.hashCode(this.transitionDuration);
      result = 31 * result + Boolean.hashCode(this.playToCompletion);
      result = 31 * result + Float.hashCode(this.resumeMemoryDuration);
      result = 31 * result + Objects.hashCode(this.nameTranslationKey);
      result = 31 * result + Integer.hashCode(this.audioCategoryIndex);
      result = 31 * result + Objects.hashCode(this.tempo);
      result = 31 * result + Arrays.hashCode((Object[])this.stateBindings);
      result = 31 * result + Arrays.hashCode((Object[])this.layers);
      result = 31 * result + Objects.hashCode(this.entryMarker);
      return 31 * result + Objects.hashCode(this.exitMarker);
   }
}
