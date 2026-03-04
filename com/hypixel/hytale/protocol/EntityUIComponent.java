package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityUIComponent {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 51;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 51;
   public static final int MAX_SIZE = 139264056;
   @Nonnull
   public EntityUIType type = EntityUIType.EntityStat;
   @Nullable
   public Vector2f hitboxOffset;
   public boolean unknown;
   public int entityStatIndex;
   @Nullable
   public RangeVector2f combatTextRandomPositionOffsetRange;
   public float combatTextViewportMargin;
   public float combatTextDuration;
   public float combatTextHitAngleModifierStrength;
   public float combatTextFontSize;
   @Nullable
   public Color combatTextColor;
   @Nullable
   public CombatTextEntityUIComponentAnimationEvent[] combatTextAnimationEvents;

   public EntityUIComponent() {
   }

   public EntityUIComponent(
      @Nonnull EntityUIType type,
      @Nullable Vector2f hitboxOffset,
      boolean unknown,
      int entityStatIndex,
      @Nullable RangeVector2f combatTextRandomPositionOffsetRange,
      float combatTextViewportMargin,
      float combatTextDuration,
      float combatTextHitAngleModifierStrength,
      float combatTextFontSize,
      @Nullable Color combatTextColor,
      @Nullable CombatTextEntityUIComponentAnimationEvent[] combatTextAnimationEvents
   ) {
      this.type = type;
      this.hitboxOffset = hitboxOffset;
      this.unknown = unknown;
      this.entityStatIndex = entityStatIndex;
      this.combatTextRandomPositionOffsetRange = combatTextRandomPositionOffsetRange;
      this.combatTextViewportMargin = combatTextViewportMargin;
      this.combatTextDuration = combatTextDuration;
      this.combatTextHitAngleModifierStrength = combatTextHitAngleModifierStrength;
      this.combatTextFontSize = combatTextFontSize;
      this.combatTextColor = combatTextColor;
      this.combatTextAnimationEvents = combatTextAnimationEvents;
   }

   public EntityUIComponent(@Nonnull EntityUIComponent other) {
      this.type = other.type;
      this.hitboxOffset = other.hitboxOffset;
      this.unknown = other.unknown;
      this.entityStatIndex = other.entityStatIndex;
      this.combatTextRandomPositionOffsetRange = other.combatTextRandomPositionOffsetRange;
      this.combatTextViewportMargin = other.combatTextViewportMargin;
      this.combatTextDuration = other.combatTextDuration;
      this.combatTextHitAngleModifierStrength = other.combatTextHitAngleModifierStrength;
      this.combatTextFontSize = other.combatTextFontSize;
      this.combatTextColor = other.combatTextColor;
      this.combatTextAnimationEvents = other.combatTextAnimationEvents;
   }

   @Nonnull
   public static EntityUIComponent deserialize(@Nonnull ByteBuf buf, int offset) {
      EntityUIComponent obj = new EntityUIComponent();
      byte nullBits = buf.getByte(offset);
      obj.type = EntityUIType.fromValue(buf.getByte(offset + 1));
      if ((nullBits & 1) != 0) {
         obj.hitboxOffset = Vector2f.deserialize(buf, offset + 2);
      }

      obj.unknown = buf.getByte(offset + 10) != 0;
      obj.entityStatIndex = buf.getIntLE(offset + 11);
      if ((nullBits & 2) != 0) {
         obj.combatTextRandomPositionOffsetRange = RangeVector2f.deserialize(buf, offset + 15);
      }

      obj.combatTextViewportMargin = buf.getFloatLE(offset + 32);
      obj.combatTextDuration = buf.getFloatLE(offset + 36);
      obj.combatTextHitAngleModifierStrength = buf.getFloatLE(offset + 40);
      obj.combatTextFontSize = buf.getFloatLE(offset + 44);
      if ((nullBits & 4) != 0) {
         obj.combatTextColor = Color.deserialize(buf, offset + 48);
      }

      int pos = offset + 51;
      if ((nullBits & 8) != 0) {
         int combatTextAnimationEventsCount = VarInt.peek(buf, pos);
         if (combatTextAnimationEventsCount < 0) {
            throw ProtocolException.negativeLength("CombatTextAnimationEvents", combatTextAnimationEventsCount);
         }

         if (combatTextAnimationEventsCount > 4096000) {
            throw ProtocolException.arrayTooLong("CombatTextAnimationEvents", combatTextAnimationEventsCount, 4096000);
         }

         int combatTextAnimationEventsVarLen = VarInt.size(combatTextAnimationEventsCount);
         if (pos + combatTextAnimationEventsVarLen + combatTextAnimationEventsCount * 34L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall(
               "CombatTextAnimationEvents", pos + combatTextAnimationEventsVarLen + combatTextAnimationEventsCount * 34, buf.readableBytes()
            );
         }

         pos += combatTextAnimationEventsVarLen;
         obj.combatTextAnimationEvents = new CombatTextEntityUIComponentAnimationEvent[combatTextAnimationEventsCount];

         for (int i = 0; i < combatTextAnimationEventsCount; i++) {
            obj.combatTextAnimationEvents[i] = CombatTextEntityUIComponentAnimationEvent.deserialize(buf, pos);
            pos += CombatTextEntityUIComponentAnimationEvent.computeBytesConsumed(buf, pos);
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 51;
      if ((nullBits & 8) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos);

         for (int i = 0; i < arrLen; i++) {
            pos += CombatTextEntityUIComponentAnimationEvent.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.hitboxOffset != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.combatTextRandomPositionOffsetRange != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.combatTextColor != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.combatTextAnimationEvents != null) {
         nullBits = (byte)(nullBits | 8);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      if (this.hitboxOffset != null) {
         this.hitboxOffset.serialize(buf);
      } else {
         buf.writeZero(8);
      }

      buf.writeByte(this.unknown ? 1 : 0);
      buf.writeIntLE(this.entityStatIndex);
      if (this.combatTextRandomPositionOffsetRange != null) {
         this.combatTextRandomPositionOffsetRange.serialize(buf);
      } else {
         buf.writeZero(17);
      }

      buf.writeFloatLE(this.combatTextViewportMargin);
      buf.writeFloatLE(this.combatTextDuration);
      buf.writeFloatLE(this.combatTextHitAngleModifierStrength);
      buf.writeFloatLE(this.combatTextFontSize);
      if (this.combatTextColor != null) {
         this.combatTextColor.serialize(buf);
      } else {
         buf.writeZero(3);
      }

      if (this.combatTextAnimationEvents != null) {
         if (this.combatTextAnimationEvents.length > 4096000) {
            throw ProtocolException.arrayTooLong("CombatTextAnimationEvents", this.combatTextAnimationEvents.length, 4096000);
         }

         VarInt.write(buf, this.combatTextAnimationEvents.length);

         for (CombatTextEntityUIComponentAnimationEvent item : this.combatTextAnimationEvents) {
            item.serialize(buf);
         }
      }
   }

   public int computeSize() {
      int size = 51;
      if (this.combatTextAnimationEvents != null) {
         size += VarInt.size(this.combatTextAnimationEvents.length) + this.combatTextAnimationEvents.length * 34;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 51) {
         return ValidationResult.error("Buffer too small: expected at least 51 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 51;
         if ((nullBits & 8) != 0) {
            int combatTextAnimationEventsCount = VarInt.peek(buffer, pos);
            if (combatTextAnimationEventsCount < 0) {
               return ValidationResult.error("Invalid array count for CombatTextAnimationEvents");
            }

            if (combatTextAnimationEventsCount > 4096000) {
               return ValidationResult.error("CombatTextAnimationEvents exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += combatTextAnimationEventsCount * 34;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading CombatTextAnimationEvents");
            }
         }

         return ValidationResult.OK;
      }
   }

   public EntityUIComponent clone() {
      EntityUIComponent copy = new EntityUIComponent();
      copy.type = this.type;
      copy.hitboxOffset = this.hitboxOffset != null ? this.hitboxOffset.clone() : null;
      copy.unknown = this.unknown;
      copy.entityStatIndex = this.entityStatIndex;
      copy.combatTextRandomPositionOffsetRange = this.combatTextRandomPositionOffsetRange != null ? this.combatTextRandomPositionOffsetRange.clone() : null;
      copy.combatTextViewportMargin = this.combatTextViewportMargin;
      copy.combatTextDuration = this.combatTextDuration;
      copy.combatTextHitAngleModifierStrength = this.combatTextHitAngleModifierStrength;
      copy.combatTextFontSize = this.combatTextFontSize;
      copy.combatTextColor = this.combatTextColor != null ? this.combatTextColor.clone() : null;
      copy.combatTextAnimationEvents = this.combatTextAnimationEvents != null
         ? Arrays.stream(this.combatTextAnimationEvents).map(e -> e.clone()).toArray(CombatTextEntityUIComponentAnimationEvent[]::new)
         : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof EntityUIComponent other)
            ? false
            : Objects.equals(this.type, other.type)
               && Objects.equals(this.hitboxOffset, other.hitboxOffset)
               && this.unknown == other.unknown
               && this.entityStatIndex == other.entityStatIndex
               && Objects.equals(this.combatTextRandomPositionOffsetRange, other.combatTextRandomPositionOffsetRange)
               && this.combatTextViewportMargin == other.combatTextViewportMargin
               && this.combatTextDuration == other.combatTextDuration
               && this.combatTextHitAngleModifierStrength == other.combatTextHitAngleModifierStrength
               && this.combatTextFontSize == other.combatTextFontSize
               && Objects.equals(this.combatTextColor, other.combatTextColor)
               && Arrays.equals((Object[])this.combatTextAnimationEvents, (Object[])other.combatTextAnimationEvents);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.type);
      result = 31 * result + Objects.hashCode(this.hitboxOffset);
      result = 31 * result + Boolean.hashCode(this.unknown);
      result = 31 * result + Integer.hashCode(this.entityStatIndex);
      result = 31 * result + Objects.hashCode(this.combatTextRandomPositionOffsetRange);
      result = 31 * result + Float.hashCode(this.combatTextViewportMargin);
      result = 31 * result + Float.hashCode(this.combatTextDuration);
      result = 31 * result + Float.hashCode(this.combatTextHitAngleModifierStrength);
      result = 31 * result + Float.hashCode(this.combatTextFontSize);
      result = 31 * result + Objects.hashCode(this.combatTextColor);
      return 31 * result + Arrays.hashCode((Object[])this.combatTextAnimationEvents);
   }
}
