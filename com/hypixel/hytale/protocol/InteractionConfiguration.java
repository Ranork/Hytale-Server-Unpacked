package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InteractionConfiguration {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 4;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 12;
   public static final int MAX_SIZE = 1677721600;
   public boolean displayOutlines = true;
   public boolean debugOutlines;
   @Nullable
   public Map<GameMode, Float> useDistance;
   public boolean allEntities;
   @Nullable
   public Map<InteractionType, InteractionPriority> priorities;

   public InteractionConfiguration() {
   }

   public InteractionConfiguration(
      boolean displayOutlines,
      boolean debugOutlines,
      @Nullable Map<GameMode, Float> useDistance,
      boolean allEntities,
      @Nullable Map<InteractionType, InteractionPriority> priorities
   ) {
      this.displayOutlines = displayOutlines;
      this.debugOutlines = debugOutlines;
      this.useDistance = useDistance;
      this.allEntities = allEntities;
      this.priorities = priorities;
   }

   public InteractionConfiguration(@Nonnull InteractionConfiguration other) {
      this.displayOutlines = other.displayOutlines;
      this.debugOutlines = other.debugOutlines;
      this.useDistance = other.useDistance;
      this.allEntities = other.allEntities;
      this.priorities = other.priorities;
   }

   @Nonnull
   public static InteractionConfiguration deserialize(@Nonnull ByteBuf buf, int offset) {
      InteractionConfiguration obj = new InteractionConfiguration();
      byte nullBits = buf.getByte(offset);
      obj.displayOutlines = buf.getByte(offset + 1) != 0;
      obj.debugOutlines = buf.getByte(offset + 2) != 0;
      obj.allEntities = buf.getByte(offset + 3) != 0;
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 12 + buf.getIntLE(offset + 4);
         int useDistanceCount = VarInt.peek(buf, varPos0);
         if (useDistanceCount < 0) {
            throw ProtocolException.negativeLength("UseDistance", useDistanceCount);
         }

         if (useDistanceCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("UseDistance", useDistanceCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos0);
         obj.useDistance = new HashMap<>(useDistanceCount);
         int dictPos = varPos0 + varIntLen;

         for (int i = 0; i < useDistanceCount; i++) {
            GameMode key = GameMode.fromValue(buf.getByte(dictPos));
            float val = buf.getFloatLE(++dictPos);
            dictPos += 4;
            if (obj.useDistance.put(key, val) != null) {
               throw ProtocolException.duplicateKey("useDistance", key);
            }
         }
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 12 + buf.getIntLE(offset + 8);
         int prioritiesCount = VarInt.peek(buf, varPos1);
         if (prioritiesCount < 0) {
            throw ProtocolException.negativeLength("Priorities", prioritiesCount);
         }

         if (prioritiesCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Priorities", prioritiesCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos1);
         obj.priorities = new HashMap<>(prioritiesCount);
         int dictPos = varPos1 + varIntLen;

         for (int ix = 0; ix < prioritiesCount; ix++) {
            InteractionType key = InteractionType.fromValue(buf.getByte(dictPos));
            InteractionPriority val = InteractionPriority.deserialize(buf, ++dictPos);
            dictPos += InteractionPriority.computeBytesConsumed(buf, dictPos);
            if (obj.priorities.put(key, val) != null) {
               throw ProtocolException.duplicateKey("priorities", key);
            }
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 12;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 4);
         int pos0 = offset + 12 + fieldOffset0;
         int dictLen = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0);

         for (int i = 0; i < dictLen; i++) {
            pos0 = ++pos0 + 4;
         }

         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 8);
         int pos1 = offset + 12 + fieldOffset1;
         int dictLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1);

         for (int i = 0; i < dictLen; i++) {
            pos1 = ++pos1 + InteractionPriority.computeBytesConsumed(buf, pos1);
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
      if (this.useDistance != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.priorities != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.displayOutlines ? 1 : 0);
      buf.writeByte(this.debugOutlines ? 1 : 0);
      buf.writeByte(this.allEntities ? 1 : 0);
      int useDistanceOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int prioritiesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.useDistance != null) {
         buf.setIntLE(useDistanceOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.useDistance.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("UseDistance", this.useDistance.size(), 4096000);
         }

         VarInt.write(buf, this.useDistance.size());

         for (Entry<GameMode, Float> e : this.useDistance.entrySet()) {
            buf.writeByte(e.getKey().getValue());
            buf.writeFloatLE(e.getValue());
         }
      } else {
         buf.setIntLE(useDistanceOffsetSlot, -1);
      }

      if (this.priorities != null) {
         buf.setIntLE(prioritiesOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.priorities.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Priorities", this.priorities.size(), 4096000);
         }

         VarInt.write(buf, this.priorities.size());

         for (Entry<InteractionType, InteractionPriority> e : this.priorities.entrySet()) {
            buf.writeByte(e.getKey().getValue());
            e.getValue().serialize(buf);
         }
      } else {
         buf.setIntLE(prioritiesOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 12;
      if (this.useDistance != null) {
         size += VarInt.size(this.useDistance.size()) + this.useDistance.size() * 5;
      }

      if (this.priorities != null) {
         int prioritiesSize = 0;

         for (Entry<InteractionType, InteractionPriority> kvp : this.priorities.entrySet()) {
            prioritiesSize += 1 + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.priorities.size()) + prioritiesSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 12) {
         return ValidationResult.error("Buffer too small: expected at least 12 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int useDistanceOffset = buffer.getIntLE(offset + 4);
            if (useDistanceOffset < 0) {
               return ValidationResult.error("Invalid offset for UseDistance");
            }

            int pos = offset + 12 + useDistanceOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for UseDistance");
            }

            int useDistanceCount = VarInt.peek(buffer, pos);
            if (useDistanceCount < 0) {
               return ValidationResult.error("Invalid dictionary count for UseDistance");
            }

            if (useDistanceCount > 4096000) {
               return ValidationResult.error("UseDistance exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for (int i = 0; i < useDistanceCount; i++) {
               pos = ++pos + 4;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading value");
               }
            }
         }

         if ((nullBits & 2) != 0) {
            int prioritiesOffset = buffer.getIntLE(offset + 8);
            if (prioritiesOffset < 0) {
               return ValidationResult.error("Invalid offset for Priorities");
            }

            int posx = offset + 12 + prioritiesOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Priorities");
            }

            int prioritiesCount = VarInt.peek(buffer, posx);
            if (prioritiesCount < 0) {
               return ValidationResult.error("Invalid dictionary count for Priorities");
            }

            if (prioritiesCount > 4096000) {
               return ValidationResult.error("Priorities exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);

            for (int ix = 0; ix < prioritiesCount; ix++) {
               posx = ++posx + InteractionPriority.computeBytesConsumed(buffer, posx);
            }
         }

         return ValidationResult.OK;
      }
   }

   public InteractionConfiguration clone() {
      InteractionConfiguration copy = new InteractionConfiguration();
      copy.displayOutlines = this.displayOutlines;
      copy.debugOutlines = this.debugOutlines;
      copy.useDistance = this.useDistance != null ? new HashMap<>(this.useDistance) : null;
      copy.allEntities = this.allEntities;
      if (this.priorities != null) {
         Map<InteractionType, InteractionPriority> m = new HashMap<>();

         for (Entry<InteractionType, InteractionPriority> e : this.priorities.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.priorities = m;
      }

      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof InteractionConfiguration other)
            ? false
            : this.displayOutlines == other.displayOutlines
               && this.debugOutlines == other.debugOutlines
               && Objects.equals(this.useDistance, other.useDistance)
               && this.allEntities == other.allEntities
               && Objects.equals(this.priorities, other.priorities);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.displayOutlines, this.debugOutlines, this.useDistance, this.allEntities, this.priorities);
   }
}
