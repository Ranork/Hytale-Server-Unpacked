package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class MusicContainer {
   public static final int MAX_SIZE = 1677721605;
   public float volume;
   public int loopCount;
   public float weight;
   @Nullable
   public Rangef silenceAfter;
   @Nullable
   public Rangef exitSilence;
   public float fadeInDuration;
   public float fadeOutDuration;
   @Nonnull
   public MusicTransitionType transitionType = MusicTransitionType.Crossfade;
   public float transitionDuration;
   public boolean playToCompletion;
   public float resumeMemoryDuration;
   @Nullable
   public String nameTranslationKey;
   public int audioCategoryIndex;
   @Nullable
   public TempoSettings tempo;
   @Nullable
   public StateBinding[] stateBindings;

   @Nonnull
   public static MusicContainer deserialize(@Nonnull ByteBuf buf, int offset) {
      int typeId = VarInt.peek(buf, offset);
      int typeIdLen = VarInt.size(typeId);

      return (MusicContainer)(switch (typeId) {
         case 0 -> SingleTrackMusicContainer.deserialize(buf, offset + typeIdLen);
         case 1 -> RandomMusicContainer.deserialize(buf, offset + typeIdLen);
         case 2 -> SequenceMusicContainer.deserialize(buf, offset + typeIdLen);
         case 3 -> HorizontalMusicContainer.deserialize(buf, offset + typeIdLen);
         case 4 -> SegmentMusicContainer.deserialize(buf, offset + typeIdLen);
         default -> throw ProtocolException.unknownPolymorphicType("MusicContainer", typeId);
      });
   }

   public static MusicContainer toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static MusicContainer toObject(MemorySegment mem, int offset) {
      int typeId = VarInt.get(mem, offset);
      int typeIdLen = VarInt.size(typeId);

      return (MusicContainer)(switch (typeId) {
         case 0 -> SingleTrackMusicContainer.toObject(mem, offset + typeIdLen);
         case 1 -> RandomMusicContainer.toObject(mem, offset + typeIdLen);
         case 2 -> SequenceMusicContainer.toObject(mem, offset + typeIdLen);
         case 3 -> HorizontalMusicContainer.toObject(mem, offset + typeIdLen);
         case 4 -> SegmentMusicContainer.toObject(mem, offset + typeIdLen);
         default -> throw ProtocolException.unknownPolymorphicType("MusicContainer", typeId);
      });
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      int typeId = VarInt.peek(buf, offset);
      int typeIdLen = VarInt.size(typeId);

      return typeIdLen + switch (typeId) {
         case 0 -> SingleTrackMusicContainer.computeBytesConsumed(buf, offset + typeIdLen);
         case 1 -> RandomMusicContainer.computeBytesConsumed(buf, offset + typeIdLen);
         case 2 -> SequenceMusicContainer.computeBytesConsumed(buf, offset + typeIdLen);
         case 3 -> HorizontalMusicContainer.computeBytesConsumed(buf, offset + typeIdLen);
         case 4 -> SegmentMusicContainer.computeBytesConsumed(buf, offset + typeIdLen);
         default -> throw ProtocolException.unknownPolymorphicType("MusicContainer", typeId);
      };
   }

   public int getTypeId() {
      if (this instanceof SingleTrackMusicContainer sub) {
         return 0;
      } else if (this instanceof RandomMusicContainer sub) {
         return 1;
      } else if (this instanceof SequenceMusicContainer sub) {
         return 2;
      } else if (this instanceof HorizontalMusicContainer sub) {
         return 3;
      } else if (this instanceof SegmentMusicContainer sub) {
         return 4;
      } else {
         throw new IllegalStateException("Unknown subtype: " + this.getClass().getName());
      }
   }

   public abstract int serialize(@Nonnull ByteBuf var1);

   public abstract int serialize(@Nonnull MemorySegment var1, int var2);

   public abstract int computeSize();

   public int serializeWithTypeId(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      VarInt.write(buf, this.getTypeId());
      this.serialize(buf);
      return buf.writerIndex() - startPos;
   }

   public int serializeWithTypeId(@Nonnull MemorySegment mem, int offset) {
      int len = VarInt.set(mem, offset, this.getTypeId());
      return len + this.serialize(mem, offset + len);
   }

   public int computeSizeWithTypeId() {
      return VarInt.size(this.getTypeId()) + this.computeSize();
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      int typeId = VarInt.peek(buffer, offset);
      int typeIdLen = VarInt.size(typeId);

      return switch (typeId) {
         case 0 -> SingleTrackMusicContainer.validateStructure(buffer, offset + typeIdLen);
         case 1 -> RandomMusicContainer.validateStructure(buffer, offset + typeIdLen);
         case 2 -> SequenceMusicContainer.validateStructure(buffer, offset + typeIdLen);
         case 3 -> HorizontalMusicContainer.validateStructure(buffer, offset + typeIdLen);
         case 4 -> SegmentMusicContainer.validateStructure(buffer, offset + typeIdLen);
         default -> ValidationResult.error("Unknown polymorphic type ID " + typeId + " for MusicContainer");
      };
   }
}
