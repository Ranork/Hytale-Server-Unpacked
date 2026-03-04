package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CombatTextEntityUIComponentAnimationEvent {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 34;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 34;
   public static final int MAX_SIZE = 34;
   @Nonnull
   public CombatTextEntityUIAnimationEventType type = CombatTextEntityUIAnimationEventType.Scale;
   public float startAt;
   public float endAt;
   public float startScale;
   public float endScale;
   @Nullable
   public Vector2f positionOffset;
   public float startOpacity;
   public float endOpacity;

   public CombatTextEntityUIComponentAnimationEvent() {
   }

   public CombatTextEntityUIComponentAnimationEvent(
      @Nonnull CombatTextEntityUIAnimationEventType type,
      float startAt,
      float endAt,
      float startScale,
      float endScale,
      @Nullable Vector2f positionOffset,
      float startOpacity,
      float endOpacity
   ) {
      this.type = type;
      this.startAt = startAt;
      this.endAt = endAt;
      this.startScale = startScale;
      this.endScale = endScale;
      this.positionOffset = positionOffset;
      this.startOpacity = startOpacity;
      this.endOpacity = endOpacity;
   }

   public CombatTextEntityUIComponentAnimationEvent(@Nonnull CombatTextEntityUIComponentAnimationEvent other) {
      this.type = other.type;
      this.startAt = other.startAt;
      this.endAt = other.endAt;
      this.startScale = other.startScale;
      this.endScale = other.endScale;
      this.positionOffset = other.positionOffset;
      this.startOpacity = other.startOpacity;
      this.endOpacity = other.endOpacity;
   }

   @Nonnull
   public static CombatTextEntityUIComponentAnimationEvent deserialize(@Nonnull ByteBuf buf, int offset) {
      CombatTextEntityUIComponentAnimationEvent obj = new CombatTextEntityUIComponentAnimationEvent();
      byte nullBits = buf.getByte(offset);
      obj.type = CombatTextEntityUIAnimationEventType.fromValue(buf.getByte(offset + 1));
      obj.startAt = buf.getFloatLE(offset + 2);
      obj.endAt = buf.getFloatLE(offset + 6);
      obj.startScale = buf.getFloatLE(offset + 10);
      obj.endScale = buf.getFloatLE(offset + 14);
      if ((nullBits & 1) != 0) {
         obj.positionOffset = Vector2f.deserialize(buf, offset + 18);
      }

      obj.startOpacity = buf.getFloatLE(offset + 26);
      obj.endOpacity = buf.getFloatLE(offset + 30);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 34;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.positionOffset != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      buf.writeFloatLE(this.startAt);
      buf.writeFloatLE(this.endAt);
      buf.writeFloatLE(this.startScale);
      buf.writeFloatLE(this.endScale);
      if (this.positionOffset != null) {
         this.positionOffset.serialize(buf);
      } else {
         buf.writeZero(8);
      }

      buf.writeFloatLE(this.startOpacity);
      buf.writeFloatLE(this.endOpacity);
   }

   public int computeSize() {
      return 34;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 34 ? ValidationResult.error("Buffer too small: expected at least 34 bytes") : ValidationResult.OK;
   }

   public CombatTextEntityUIComponentAnimationEvent clone() {
      CombatTextEntityUIComponentAnimationEvent copy = new CombatTextEntityUIComponentAnimationEvent();
      copy.type = this.type;
      copy.startAt = this.startAt;
      copy.endAt = this.endAt;
      copy.startScale = this.startScale;
      copy.endScale = this.endScale;
      copy.positionOffset = this.positionOffset != null ? this.positionOffset.clone() : null;
      copy.startOpacity = this.startOpacity;
      copy.endOpacity = this.endOpacity;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof CombatTextEntityUIComponentAnimationEvent other)
            ? false
            : Objects.equals(this.type, other.type)
               && this.startAt == other.startAt
               && this.endAt == other.endAt
               && this.startScale == other.startScale
               && this.endScale == other.endScale
               && Objects.equals(this.positionOffset, other.positionOffset)
               && this.startOpacity == other.startOpacity
               && this.endOpacity == other.endOpacity;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.startAt, this.endAt, this.startScale, this.endScale, this.positionOffset, this.startOpacity, this.endOpacity);
   }
}
