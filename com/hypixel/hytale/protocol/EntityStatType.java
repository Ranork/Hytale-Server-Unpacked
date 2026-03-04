package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityStatType {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 15;
   public static final int VARIABLE_FIELD_COUNT = 3;
   public static final int VARIABLE_BLOCK_START = 27;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public String id;
   public float value;
   public float min;
   public float max;
   @Nullable
   public EntityStatEffects minValueEffects;
   @Nullable
   public EntityStatEffects maxValueEffects;
   @Nonnull
   public EntityStatResetBehavior resetBehavior = EntityStatResetBehavior.InitialValue;
   public boolean hideFromTooltip;

   public EntityStatType() {
   }

   public EntityStatType(
      @Nullable String id,
      float value,
      float min,
      float max,
      @Nullable EntityStatEffects minValueEffects,
      @Nullable EntityStatEffects maxValueEffects,
      @Nonnull EntityStatResetBehavior resetBehavior,
      boolean hideFromTooltip
   ) {
      this.id = id;
      this.value = value;
      this.min = min;
      this.max = max;
      this.minValueEffects = minValueEffects;
      this.maxValueEffects = maxValueEffects;
      this.resetBehavior = resetBehavior;
      this.hideFromTooltip = hideFromTooltip;
   }

   public EntityStatType(@Nonnull EntityStatType other) {
      this.id = other.id;
      this.value = other.value;
      this.min = other.min;
      this.max = other.max;
      this.minValueEffects = other.minValueEffects;
      this.maxValueEffects = other.maxValueEffects;
      this.resetBehavior = other.resetBehavior;
      this.hideFromTooltip = other.hideFromTooltip;
   }

   @Nonnull
   public static EntityStatType deserialize(@Nonnull ByteBuf buf, int offset) {
      EntityStatType obj = new EntityStatType();
      byte nullBits = buf.getByte(offset);
      obj.value = buf.getFloatLE(offset + 1);
      obj.min = buf.getFloatLE(offset + 5);
      obj.max = buf.getFloatLE(offset + 9);
      obj.resetBehavior = EntityStatResetBehavior.fromValue(buf.getByte(offset + 13));
      obj.hideFromTooltip = buf.getByte(offset + 14) != 0;
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 27 + buf.getIntLE(offset + 15);
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
         int varPos1 = offset + 27 + buf.getIntLE(offset + 19);
         obj.minValueEffects = EntityStatEffects.deserialize(buf, varPos1);
      }

      if ((nullBits & 4) != 0) {
         int varPos2 = offset + 27 + buf.getIntLE(offset + 23);
         obj.maxValueEffects = EntityStatEffects.deserialize(buf, varPos2);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 27;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 15);
         int pos0 = offset + 27 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 19);
         int pos1 = offset + 27 + fieldOffset1;
         pos1 += EntityStatEffects.computeBytesConsumed(buf, pos1);
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 23);
         int pos2 = offset + 27 + fieldOffset2;
         pos2 += EntityStatEffects.computeBytesConsumed(buf, pos2);
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
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

      if (this.minValueEffects != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.maxValueEffects != null) {
         nullBits = (byte)(nullBits | 4);
      }

      buf.writeByte(nullBits);
      buf.writeFloatLE(this.value);
      buf.writeFloatLE(this.min);
      buf.writeFloatLE(this.max);
      buf.writeByte(this.resetBehavior.getValue());
      buf.writeByte(this.hideFromTooltip ? 1 : 0);
      int idOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int minValueEffectsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int maxValueEffectsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.id != null) {
         buf.setIntLE(idOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.id, 4096000);
      } else {
         buf.setIntLE(idOffsetSlot, -1);
      }

      if (this.minValueEffects != null) {
         buf.setIntLE(minValueEffectsOffsetSlot, buf.writerIndex() - varBlockStart);
         this.minValueEffects.serialize(buf);
      } else {
         buf.setIntLE(minValueEffectsOffsetSlot, -1);
      }

      if (this.maxValueEffects != null) {
         buf.setIntLE(maxValueEffectsOffsetSlot, buf.writerIndex() - varBlockStart);
         this.maxValueEffects.serialize(buf);
      } else {
         buf.setIntLE(maxValueEffectsOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 27;
      if (this.id != null) {
         size += PacketIO.stringSize(this.id);
      }

      if (this.minValueEffects != null) {
         size += this.minValueEffects.computeSize();
      }

      if (this.maxValueEffects != null) {
         size += this.maxValueEffects.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 27) {
         return ValidationResult.error("Buffer too small: expected at least 27 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int idOffset = buffer.getIntLE(offset + 15);
            if (idOffset < 0) {
               return ValidationResult.error("Invalid offset for Id");
            }

            int pos = offset + 27 + idOffset;
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
            int minValueEffectsOffset = buffer.getIntLE(offset + 19);
            if (minValueEffectsOffset < 0) {
               return ValidationResult.error("Invalid offset for MinValueEffects");
            }

            int posx = offset + 27 + minValueEffectsOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for MinValueEffects");
            }

            ValidationResult minValueEffectsResult = EntityStatEffects.validateStructure(buffer, posx);
            if (!minValueEffectsResult.isValid()) {
               return ValidationResult.error("Invalid MinValueEffects: " + minValueEffectsResult.error());
            }

            posx += EntityStatEffects.computeBytesConsumed(buffer, posx);
         }

         if ((nullBits & 4) != 0) {
            int maxValueEffectsOffset = buffer.getIntLE(offset + 23);
            if (maxValueEffectsOffset < 0) {
               return ValidationResult.error("Invalid offset for MaxValueEffects");
            }

            int posxx = offset + 27 + maxValueEffectsOffset;
            if (posxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for MaxValueEffects");
            }

            ValidationResult maxValueEffectsResult = EntityStatEffects.validateStructure(buffer, posxx);
            if (!maxValueEffectsResult.isValid()) {
               return ValidationResult.error("Invalid MaxValueEffects: " + maxValueEffectsResult.error());
            }

            posxx += EntityStatEffects.computeBytesConsumed(buffer, posxx);
         }

         return ValidationResult.OK;
      }
   }

   public EntityStatType clone() {
      EntityStatType copy = new EntityStatType();
      copy.id = this.id;
      copy.value = this.value;
      copy.min = this.min;
      copy.max = this.max;
      copy.minValueEffects = this.minValueEffects != null ? this.minValueEffects.clone() : null;
      copy.maxValueEffects = this.maxValueEffects != null ? this.maxValueEffects.clone() : null;
      copy.resetBehavior = this.resetBehavior;
      copy.hideFromTooltip = this.hideFromTooltip;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof EntityStatType other)
            ? false
            : Objects.equals(this.id, other.id)
               && this.value == other.value
               && this.min == other.min
               && this.max == other.max
               && Objects.equals(this.minValueEffects, other.minValueEffects)
               && Objects.equals(this.maxValueEffects, other.maxValueEffects)
               && Objects.equals(this.resetBehavior, other.resetBehavior)
               && this.hideFromTooltip == other.hideFromTooltip;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.id, this.value, this.min, this.max, this.minValueEffects, this.maxValueEffects, this.resetBehavior, this.hideFromTooltip);
   }
}
