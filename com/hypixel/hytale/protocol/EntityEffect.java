package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
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

public class EntityEffect {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 25;
   public static final int VARIABLE_FIELD_COUNT = 6;
   public static final int VARIABLE_BLOCK_START = 49;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public String id;
   @Nullable
   public String name;
   @Nullable
   public ApplicationEffects applicationEffects;
   public int worldRemovalSoundEventIndex;
   public int localRemovalSoundEventIndex;
   @Nullable
   public ModelOverride modelOverride;
   public float duration;
   public boolean infinite;
   public boolean debuff;
   @Nullable
   public String statusEffectIcon;
   @Nonnull
   public OverlapBehavior overlapBehavior = OverlapBehavior.Extend;
   public double damageCalculatorCooldown;
   @Nullable
   public Map<Integer, Float> statModifiers;
   @Nonnull
   public ValueType valueType = ValueType.Percent;

   public EntityEffect() {
   }

   public EntityEffect(
      @Nullable String id,
      @Nullable String name,
      @Nullable ApplicationEffects applicationEffects,
      int worldRemovalSoundEventIndex,
      int localRemovalSoundEventIndex,
      @Nullable ModelOverride modelOverride,
      float duration,
      boolean infinite,
      boolean debuff,
      @Nullable String statusEffectIcon,
      @Nonnull OverlapBehavior overlapBehavior,
      double damageCalculatorCooldown,
      @Nullable Map<Integer, Float> statModifiers,
      @Nonnull ValueType valueType
   ) {
      this.id = id;
      this.name = name;
      this.applicationEffects = applicationEffects;
      this.worldRemovalSoundEventIndex = worldRemovalSoundEventIndex;
      this.localRemovalSoundEventIndex = localRemovalSoundEventIndex;
      this.modelOverride = modelOverride;
      this.duration = duration;
      this.infinite = infinite;
      this.debuff = debuff;
      this.statusEffectIcon = statusEffectIcon;
      this.overlapBehavior = overlapBehavior;
      this.damageCalculatorCooldown = damageCalculatorCooldown;
      this.statModifiers = statModifiers;
      this.valueType = valueType;
   }

   public EntityEffect(@Nonnull EntityEffect other) {
      this.id = other.id;
      this.name = other.name;
      this.applicationEffects = other.applicationEffects;
      this.worldRemovalSoundEventIndex = other.worldRemovalSoundEventIndex;
      this.localRemovalSoundEventIndex = other.localRemovalSoundEventIndex;
      this.modelOverride = other.modelOverride;
      this.duration = other.duration;
      this.infinite = other.infinite;
      this.debuff = other.debuff;
      this.statusEffectIcon = other.statusEffectIcon;
      this.overlapBehavior = other.overlapBehavior;
      this.damageCalculatorCooldown = other.damageCalculatorCooldown;
      this.statModifiers = other.statModifiers;
      this.valueType = other.valueType;
   }

   @Nonnull
   public static EntityEffect deserialize(@Nonnull ByteBuf buf, int offset) {
      EntityEffect obj = new EntityEffect();
      byte nullBits = buf.getByte(offset);
      obj.worldRemovalSoundEventIndex = buf.getIntLE(offset + 1);
      obj.localRemovalSoundEventIndex = buf.getIntLE(offset + 5);
      obj.duration = buf.getFloatLE(offset + 9);
      obj.infinite = buf.getByte(offset + 13) != 0;
      obj.debuff = buf.getByte(offset + 14) != 0;
      obj.overlapBehavior = OverlapBehavior.fromValue(buf.getByte(offset + 15));
      obj.damageCalculatorCooldown = buf.getDoubleLE(offset + 16);
      obj.valueType = ValueType.fromValue(buf.getByte(offset + 24));
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 49 + buf.getIntLE(offset + 25);
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
         int varPos1 = offset + 49 + buf.getIntLE(offset + 29);
         int nameLen = VarInt.peek(buf, varPos1);
         if (nameLen < 0) {
            throw ProtocolException.negativeLength("Name", nameLen);
         }

         if (nameLen > 4096000) {
            throw ProtocolException.stringTooLong("Name", nameLen, 4096000);
         }

         obj.name = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
      }

      if ((nullBits & 4) != 0) {
         int varPos2 = offset + 49 + buf.getIntLE(offset + 33);
         obj.applicationEffects = ApplicationEffects.deserialize(buf, varPos2);
      }

      if ((nullBits & 8) != 0) {
         int varPos3 = offset + 49 + buf.getIntLE(offset + 37);
         obj.modelOverride = ModelOverride.deserialize(buf, varPos3);
      }

      if ((nullBits & 16) != 0) {
         int varPos4 = offset + 49 + buf.getIntLE(offset + 41);
         int statusEffectIconLen = VarInt.peek(buf, varPos4);
         if (statusEffectIconLen < 0) {
            throw ProtocolException.negativeLength("StatusEffectIcon", statusEffectIconLen);
         }

         if (statusEffectIconLen > 4096000) {
            throw ProtocolException.stringTooLong("StatusEffectIcon", statusEffectIconLen, 4096000);
         }

         obj.statusEffectIcon = PacketIO.readVarString(buf, varPos4, PacketIO.UTF8);
      }

      if ((nullBits & 32) != 0) {
         int varPos5 = offset + 49 + buf.getIntLE(offset + 45);
         int statModifiersCount = VarInt.peek(buf, varPos5);
         if (statModifiersCount < 0) {
            throw ProtocolException.negativeLength("StatModifiers", statModifiersCount);
         }

         if (statModifiersCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("StatModifiers", statModifiersCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos5);
         obj.statModifiers = new HashMap<>(statModifiersCount);
         int dictPos = varPos5 + varIntLen;

         for (int i = 0; i < statModifiersCount; i++) {
            int key = buf.getIntLE(dictPos);
            dictPos += 4;
            float val = buf.getFloatLE(dictPos);
            dictPos += 4;
            if (obj.statModifiers.put(key, val) != null) {
               throw ProtocolException.duplicateKey("statModifiers", key);
            }
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 49;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 25);
         int pos0 = offset + 49 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 29);
         int pos1 = offset + 49 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 33);
         int pos2 = offset + 49 + fieldOffset2;
         pos2 += ApplicationEffects.computeBytesConsumed(buf, pos2);
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 37);
         int pos3 = offset + 49 + fieldOffset3;
         pos3 += ModelOverride.computeBytesConsumed(buf, pos3);
         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      if ((nullBits & 16) != 0) {
         int fieldOffset4 = buf.getIntLE(offset + 41);
         int pos4 = offset + 49 + fieldOffset4;
         int sl = VarInt.peek(buf, pos4);
         pos4 += VarInt.length(buf, pos4) + sl;
         if (pos4 - offset > maxEnd) {
            maxEnd = pos4 - offset;
         }
      }

      if ((nullBits & 32) != 0) {
         int fieldOffset5 = buf.getIntLE(offset + 45);
         int pos5 = offset + 49 + fieldOffset5;
         int dictLen = VarInt.peek(buf, pos5);
         pos5 += VarInt.length(buf, pos5);

         for (int i = 0; i < dictLen; i++) {
            pos5 += 4;
            pos5 += 4;
         }

         if (pos5 - offset > maxEnd) {
            maxEnd = pos5 - offset;
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

      if (this.name != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.applicationEffects != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.modelOverride != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.statusEffectIcon != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.statModifiers != null) {
         nullBits = (byte)(nullBits | 32);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.worldRemovalSoundEventIndex);
      buf.writeIntLE(this.localRemovalSoundEventIndex);
      buf.writeFloatLE(this.duration);
      buf.writeByte(this.infinite ? 1 : 0);
      buf.writeByte(this.debuff ? 1 : 0);
      buf.writeByte(this.overlapBehavior.getValue());
      buf.writeDoubleLE(this.damageCalculatorCooldown);
      buf.writeByte(this.valueType.getValue());
      int idOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int nameOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int applicationEffectsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int modelOverrideOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int statusEffectIconOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int statModifiersOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.id != null) {
         buf.setIntLE(idOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.id, 4096000);
      } else {
         buf.setIntLE(idOffsetSlot, -1);
      }

      if (this.name != null) {
         buf.setIntLE(nameOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.name, 4096000);
      } else {
         buf.setIntLE(nameOffsetSlot, -1);
      }

      if (this.applicationEffects != null) {
         buf.setIntLE(applicationEffectsOffsetSlot, buf.writerIndex() - varBlockStart);
         this.applicationEffects.serialize(buf);
      } else {
         buf.setIntLE(applicationEffectsOffsetSlot, -1);
      }

      if (this.modelOverride != null) {
         buf.setIntLE(modelOverrideOffsetSlot, buf.writerIndex() - varBlockStart);
         this.modelOverride.serialize(buf);
      } else {
         buf.setIntLE(modelOverrideOffsetSlot, -1);
      }

      if (this.statusEffectIcon != null) {
         buf.setIntLE(statusEffectIconOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.statusEffectIcon, 4096000);
      } else {
         buf.setIntLE(statusEffectIconOffsetSlot, -1);
      }

      if (this.statModifiers != null) {
         buf.setIntLE(statModifiersOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.statModifiers.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("StatModifiers", this.statModifiers.size(), 4096000);
         }

         VarInt.write(buf, this.statModifiers.size());

         for (Entry<Integer, Float> e : this.statModifiers.entrySet()) {
            buf.writeIntLE(e.getKey());
            buf.writeFloatLE(e.getValue());
         }
      } else {
         buf.setIntLE(statModifiersOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 49;
      if (this.id != null) {
         size += PacketIO.stringSize(this.id);
      }

      if (this.name != null) {
         size += PacketIO.stringSize(this.name);
      }

      if (this.applicationEffects != null) {
         size += this.applicationEffects.computeSize();
      }

      if (this.modelOverride != null) {
         size += this.modelOverride.computeSize();
      }

      if (this.statusEffectIcon != null) {
         size += PacketIO.stringSize(this.statusEffectIcon);
      }

      if (this.statModifiers != null) {
         size += VarInt.size(this.statModifiers.size()) + this.statModifiers.size() * 8;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 49) {
         return ValidationResult.error("Buffer too small: expected at least 49 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int idOffset = buffer.getIntLE(offset + 25);
            if (idOffset < 0) {
               return ValidationResult.error("Invalid offset for Id");
            }

            int pos = offset + 49 + idOffset;
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
            int nameOffset = buffer.getIntLE(offset + 29);
            if (nameOffset < 0) {
               return ValidationResult.error("Invalid offset for Name");
            }

            int posx = offset + 49 + nameOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Name");
            }

            int nameLen = VarInt.peek(buffer, posx);
            if (nameLen < 0) {
               return ValidationResult.error("Invalid string length for Name");
            }

            if (nameLen > 4096000) {
               return ValidationResult.error("Name exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
            posx += nameLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Name");
            }
         }

         if ((nullBits & 4) != 0) {
            int applicationEffectsOffset = buffer.getIntLE(offset + 33);
            if (applicationEffectsOffset < 0) {
               return ValidationResult.error("Invalid offset for ApplicationEffects");
            }

            int posxx = offset + 49 + applicationEffectsOffset;
            if (posxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ApplicationEffects");
            }

            ValidationResult applicationEffectsResult = ApplicationEffects.validateStructure(buffer, posxx);
            if (!applicationEffectsResult.isValid()) {
               return ValidationResult.error("Invalid ApplicationEffects: " + applicationEffectsResult.error());
            }

            posxx += ApplicationEffects.computeBytesConsumed(buffer, posxx);
         }

         if ((nullBits & 8) != 0) {
            int modelOverrideOffset = buffer.getIntLE(offset + 37);
            if (modelOverrideOffset < 0) {
               return ValidationResult.error("Invalid offset for ModelOverride");
            }

            int posxxx = offset + 49 + modelOverrideOffset;
            if (posxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ModelOverride");
            }

            ValidationResult modelOverrideResult = ModelOverride.validateStructure(buffer, posxxx);
            if (!modelOverrideResult.isValid()) {
               return ValidationResult.error("Invalid ModelOverride: " + modelOverrideResult.error());
            }

            posxxx += ModelOverride.computeBytesConsumed(buffer, posxxx);
         }

         if ((nullBits & 16) != 0) {
            int statusEffectIconOffset = buffer.getIntLE(offset + 41);
            if (statusEffectIconOffset < 0) {
               return ValidationResult.error("Invalid offset for StatusEffectIcon");
            }

            int posxxxx = offset + 49 + statusEffectIconOffset;
            if (posxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for StatusEffectIcon");
            }

            int statusEffectIconLen = VarInt.peek(buffer, posxxxx);
            if (statusEffectIconLen < 0) {
               return ValidationResult.error("Invalid string length for StatusEffectIcon");
            }

            if (statusEffectIconLen > 4096000) {
               return ValidationResult.error("StatusEffectIcon exceeds max length 4096000");
            }

            posxxxx += VarInt.length(buffer, posxxxx);
            posxxxx += statusEffectIconLen;
            if (posxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading StatusEffectIcon");
            }
         }

         if ((nullBits & 32) != 0) {
            int statModifiersOffset = buffer.getIntLE(offset + 45);
            if (statModifiersOffset < 0) {
               return ValidationResult.error("Invalid offset for StatModifiers");
            }

            int posxxxxx = offset + 49 + statModifiersOffset;
            if (posxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for StatModifiers");
            }

            int statModifiersCount = VarInt.peek(buffer, posxxxxx);
            if (statModifiersCount < 0) {
               return ValidationResult.error("Invalid dictionary count for StatModifiers");
            }

            if (statModifiersCount > 4096000) {
               return ValidationResult.error("StatModifiers exceeds max length 4096000");
            }

            posxxxxx += VarInt.length(buffer, posxxxxx);

            for (int i = 0; i < statModifiersCount; i++) {
               posxxxxx += 4;
               if (posxxxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               posxxxxx += 4;
               if (posxxxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading value");
               }
            }
         }

         return ValidationResult.OK;
      }
   }

   public EntityEffect clone() {
      EntityEffect copy = new EntityEffect();
      copy.id = this.id;
      copy.name = this.name;
      copy.applicationEffects = this.applicationEffects != null ? this.applicationEffects.clone() : null;
      copy.worldRemovalSoundEventIndex = this.worldRemovalSoundEventIndex;
      copy.localRemovalSoundEventIndex = this.localRemovalSoundEventIndex;
      copy.modelOverride = this.modelOverride != null ? this.modelOverride.clone() : null;
      copy.duration = this.duration;
      copy.infinite = this.infinite;
      copy.debuff = this.debuff;
      copy.statusEffectIcon = this.statusEffectIcon;
      copy.overlapBehavior = this.overlapBehavior;
      copy.damageCalculatorCooldown = this.damageCalculatorCooldown;
      copy.statModifiers = this.statModifiers != null ? new HashMap<>(this.statModifiers) : null;
      copy.valueType = this.valueType;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof EntityEffect other)
            ? false
            : Objects.equals(this.id, other.id)
               && Objects.equals(this.name, other.name)
               && Objects.equals(this.applicationEffects, other.applicationEffects)
               && this.worldRemovalSoundEventIndex == other.worldRemovalSoundEventIndex
               && this.localRemovalSoundEventIndex == other.localRemovalSoundEventIndex
               && Objects.equals(this.modelOverride, other.modelOverride)
               && this.duration == other.duration
               && this.infinite == other.infinite
               && this.debuff == other.debuff
               && Objects.equals(this.statusEffectIcon, other.statusEffectIcon)
               && Objects.equals(this.overlapBehavior, other.overlapBehavior)
               && this.damageCalculatorCooldown == other.damageCalculatorCooldown
               && Objects.equals(this.statModifiers, other.statModifiers)
               && Objects.equals(this.valueType, other.valueType);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(
         this.id,
         this.name,
         this.applicationEffects,
         this.worldRemovalSoundEventIndex,
         this.localRemovalSoundEventIndex,
         this.modelOverride,
         this.duration,
         this.infinite,
         this.debuff,
         this.statusEffectIcon,
         this.overlapBehavior,
         this.damageCalculatorCooldown,
         this.statModifiers,
         this.valueType
      );
   }
}
