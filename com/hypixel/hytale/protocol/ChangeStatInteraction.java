package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ChangeStatInteraction extends SimpleInteraction {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 22;
   public static final int VARIABLE_FIELD_COUNT = 6;
   public static final int VARIABLE_BLOCK_START = 46;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public InteractionTarget entityTarget = InteractionTarget.User;
   @Nonnull
   public ValueType valueType = ValueType.Percent;
   @Nullable
   public Map<Integer, Float> statModifiers;
   @Nonnull
   public ChangeStatBehaviour changeStatBehaviour = ChangeStatBehaviour.Add;

   public ChangeStatInteraction() {
   }

   public ChangeStatInteraction(
      @Nonnull WaitForDataFrom waitForDataFrom,
      @Nullable InteractionEffects effects,
      float horizontalSpeedMultiplier,
      float runTime,
      boolean cancelOnItemChange,
      @Nullable Map<GameMode, InteractionSettings> settings,
      @Nullable InteractionRules rules,
      @Nullable int[] tags,
      @Nullable InteractionCameraSettings camera,
      int next,
      int failed,
      @Nonnull InteractionTarget entityTarget,
      @Nonnull ValueType valueType,
      @Nullable Map<Integer, Float> statModifiers,
      @Nonnull ChangeStatBehaviour changeStatBehaviour
   ) {
      this.waitForDataFrom = waitForDataFrom;
      this.effects = effects;
      this.horizontalSpeedMultiplier = horizontalSpeedMultiplier;
      this.runTime = runTime;
      this.cancelOnItemChange = cancelOnItemChange;
      this.settings = settings;
      this.rules = rules;
      this.tags = tags;
      this.camera = camera;
      this.next = next;
      this.failed = failed;
      this.entityTarget = entityTarget;
      this.valueType = valueType;
      this.statModifiers = statModifiers;
      this.changeStatBehaviour = changeStatBehaviour;
   }

   public ChangeStatInteraction(@Nonnull ChangeStatInteraction other) {
      this.waitForDataFrom = other.waitForDataFrom;
      this.effects = other.effects;
      this.horizontalSpeedMultiplier = other.horizontalSpeedMultiplier;
      this.runTime = other.runTime;
      this.cancelOnItemChange = other.cancelOnItemChange;
      this.settings = other.settings;
      this.rules = other.rules;
      this.tags = other.tags;
      this.camera = other.camera;
      this.next = other.next;
      this.failed = other.failed;
      this.entityTarget = other.entityTarget;
      this.valueType = other.valueType;
      this.statModifiers = other.statModifiers;
      this.changeStatBehaviour = other.changeStatBehaviour;
   }

   @Nonnull
   public static ChangeStatInteraction deserialize(@Nonnull ByteBuf buf, int offset) {
      ChangeStatInteraction obj = new ChangeStatInteraction();
      byte nullBits = buf.getByte(offset);
      obj.waitForDataFrom = WaitForDataFrom.fromValue(buf.getByte(offset + 1));
      obj.horizontalSpeedMultiplier = buf.getFloatLE(offset + 2);
      obj.runTime = buf.getFloatLE(offset + 6);
      obj.cancelOnItemChange = buf.getByte(offset + 10) != 0;
      obj.next = buf.getIntLE(offset + 11);
      obj.failed = buf.getIntLE(offset + 15);
      obj.entityTarget = InteractionTarget.fromValue(buf.getByte(offset + 19));
      obj.valueType = ValueType.fromValue(buf.getByte(offset + 20));
      obj.changeStatBehaviour = ChangeStatBehaviour.fromValue(buf.getByte(offset + 21));
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 46 + buf.getIntLE(offset + 22);
         obj.effects = InteractionEffects.deserialize(buf, varPos0);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 46 + buf.getIntLE(offset + 26);
         int settingsCount = VarInt.peek(buf, varPos1);
         if (settingsCount < 0) {
            throw ProtocolException.negativeLength("Settings", settingsCount);
         }

         if (settingsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Settings", settingsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos1);
         obj.settings = new HashMap<>(settingsCount);
         int dictPos = varPos1 + varIntLen;

         for (int i = 0; i < settingsCount; i++) {
            GameMode key = GameMode.fromValue(buf.getByte(dictPos));
            InteractionSettings val = InteractionSettings.deserialize(buf, ++dictPos);
            dictPos += InteractionSettings.computeBytesConsumed(buf, dictPos);
            if (obj.settings.put(key, val) != null) {
               throw ProtocolException.duplicateKey("settings", key);
            }
         }
      }

      if ((nullBits & 4) != 0) {
         int varPos2 = offset + 46 + buf.getIntLE(offset + 30);
         obj.rules = InteractionRules.deserialize(buf, varPos2);
      }

      if ((nullBits & 8) != 0) {
         int varPos3 = offset + 46 + buf.getIntLE(offset + 34);
         int tagsCount = VarInt.peek(buf, varPos3);
         if (tagsCount < 0) {
            throw ProtocolException.negativeLength("Tags", tagsCount);
         }

         if (tagsCount > 4096000) {
            throw ProtocolException.arrayTooLong("Tags", tagsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos3);
         if (varPos3 + varIntLen + tagsCount * 4L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Tags", varPos3 + varIntLen + tagsCount * 4, buf.readableBytes());
         }

         obj.tags = new int[tagsCount];

         for (int ix = 0; ix < tagsCount; ix++) {
            obj.tags[ix] = buf.getIntLE(varPos3 + varIntLen + ix * 4);
         }
      }

      if ((nullBits & 16) != 0) {
         int varPos4 = offset + 46 + buf.getIntLE(offset + 38);
         obj.camera = InteractionCameraSettings.deserialize(buf, varPos4);
      }

      if ((nullBits & 32) != 0) {
         int varPos5 = offset + 46 + buf.getIntLE(offset + 42);
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

         for (int ix = 0; ix < statModifiersCount; ix++) {
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
      int maxEnd = 46;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 22);
         int pos0 = offset + 46 + fieldOffset0;
         pos0 += InteractionEffects.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 26);
         int pos1 = offset + 46 + fieldOffset1;
         int dictLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1);

         for (int i = 0; i < dictLen; i++) {
            pos1 = ++pos1 + InteractionSettings.computeBytesConsumed(buf, pos1);
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 30);
         int pos2 = offset + 46 + fieldOffset2;
         pos2 += InteractionRules.computeBytesConsumed(buf, pos2);
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 34);
         int pos3 = offset + 46 + fieldOffset3;
         int arrLen = VarInt.peek(buf, pos3);
         pos3 += VarInt.length(buf, pos3) + arrLen * 4;
         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      if ((nullBits & 16) != 0) {
         int fieldOffset4 = buf.getIntLE(offset + 38);
         int pos4 = offset + 46 + fieldOffset4;
         pos4 += InteractionCameraSettings.computeBytesConsumed(buf, pos4);
         if (pos4 - offset > maxEnd) {
            maxEnd = pos4 - offset;
         }
      }

      if ((nullBits & 32) != 0) {
         int fieldOffset5 = buf.getIntLE(offset + 42);
         int pos5 = offset + 46 + fieldOffset5;
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

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.effects != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.settings != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.rules != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.tags != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.camera != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.statModifiers != null) {
         nullBits = (byte)(nullBits | 32);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.waitForDataFrom.getValue());
      buf.writeFloatLE(this.horizontalSpeedMultiplier);
      buf.writeFloatLE(this.runTime);
      buf.writeByte(this.cancelOnItemChange ? 1 : 0);
      buf.writeIntLE(this.next);
      buf.writeIntLE(this.failed);
      buf.writeByte(this.entityTarget.getValue());
      buf.writeByte(this.valueType.getValue());
      buf.writeByte(this.changeStatBehaviour.getValue());
      int effectsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int settingsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int rulesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int tagsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int cameraOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int statModifiersOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.effects != null) {
         buf.setIntLE(effectsOffsetSlot, buf.writerIndex() - varBlockStart);
         this.effects.serialize(buf);
      } else {
         buf.setIntLE(effectsOffsetSlot, -1);
      }

      if (this.settings != null) {
         buf.setIntLE(settingsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.settings.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Settings", this.settings.size(), 4096000);
         }

         VarInt.write(buf, this.settings.size());

         for (Entry<GameMode, InteractionSettings> e : this.settings.entrySet()) {
            buf.writeByte(e.getKey().getValue());
            e.getValue().serialize(buf);
         }
      } else {
         buf.setIntLE(settingsOffsetSlot, -1);
      }

      if (this.rules != null) {
         buf.setIntLE(rulesOffsetSlot, buf.writerIndex() - varBlockStart);
         this.rules.serialize(buf);
      } else {
         buf.setIntLE(rulesOffsetSlot, -1);
      }

      if (this.tags != null) {
         buf.setIntLE(tagsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.tags.length > 4096000) {
            throw ProtocolException.arrayTooLong("Tags", this.tags.length, 4096000);
         }

         VarInt.write(buf, this.tags.length);

         for (int item : this.tags) {
            buf.writeIntLE(item);
         }
      } else {
         buf.setIntLE(tagsOffsetSlot, -1);
      }

      if (this.camera != null) {
         buf.setIntLE(cameraOffsetSlot, buf.writerIndex() - varBlockStart);
         this.camera.serialize(buf);
      } else {
         buf.setIntLE(cameraOffsetSlot, -1);
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

      return buf.writerIndex() - startPos;
   }

   @Override
   public int computeSize() {
      int size = 46;
      if (this.effects != null) {
         size += this.effects.computeSize();
      }

      if (this.settings != null) {
         size += VarInt.size(this.settings.size()) + this.settings.size() * 2;
      }

      if (this.rules != null) {
         size += this.rules.computeSize();
      }

      if (this.tags != null) {
         size += VarInt.size(this.tags.length) + this.tags.length * 4;
      }

      if (this.camera != null) {
         size += this.camera.computeSize();
      }

      if (this.statModifiers != null) {
         size += VarInt.size(this.statModifiers.size()) + this.statModifiers.size() * 8;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 46) {
         return ValidationResult.error("Buffer too small: expected at least 46 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int effectsOffset = buffer.getIntLE(offset + 22);
            if (effectsOffset < 0) {
               return ValidationResult.error("Invalid offset for Effects");
            }

            int pos = offset + 46 + effectsOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Effects");
            }

            ValidationResult effectsResult = InteractionEffects.validateStructure(buffer, pos);
            if (!effectsResult.isValid()) {
               return ValidationResult.error("Invalid Effects: " + effectsResult.error());
            }

            pos += InteractionEffects.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits & 2) != 0) {
            int settingsOffset = buffer.getIntLE(offset + 26);
            if (settingsOffset < 0) {
               return ValidationResult.error("Invalid offset for Settings");
            }

            int posx = offset + 46 + settingsOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Settings");
            }

            int settingsCount = VarInt.peek(buffer, posx);
            if (settingsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for Settings");
            }

            if (settingsCount > 4096000) {
               return ValidationResult.error("Settings exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);

            for (int i = 0; i < settingsCount; i++) {
               posx++;
               posx++;
            }
         }

         if ((nullBits & 4) != 0) {
            int rulesOffset = buffer.getIntLE(offset + 30);
            if (rulesOffset < 0) {
               return ValidationResult.error("Invalid offset for Rules");
            }

            int posxx = offset + 46 + rulesOffset;
            if (posxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Rules");
            }

            ValidationResult rulesResult = InteractionRules.validateStructure(buffer, posxx);
            if (!rulesResult.isValid()) {
               return ValidationResult.error("Invalid Rules: " + rulesResult.error());
            }

            posxx += InteractionRules.computeBytesConsumed(buffer, posxx);
         }

         if ((nullBits & 8) != 0) {
            int tagsOffset = buffer.getIntLE(offset + 34);
            if (tagsOffset < 0) {
               return ValidationResult.error("Invalid offset for Tags");
            }

            int posxxx = offset + 46 + tagsOffset;
            if (posxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Tags");
            }

            int tagsCount = VarInt.peek(buffer, posxxx);
            if (tagsCount < 0) {
               return ValidationResult.error("Invalid array count for Tags");
            }

            if (tagsCount > 4096000) {
               return ValidationResult.error("Tags exceeds max length 4096000");
            }

            posxxx += VarInt.length(buffer, posxxx);
            posxxx += tagsCount * 4;
            if (posxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Tags");
            }
         }

         if ((nullBits & 16) != 0) {
            int cameraOffset = buffer.getIntLE(offset + 38);
            if (cameraOffset < 0) {
               return ValidationResult.error("Invalid offset for Camera");
            }

            int posxxxx = offset + 46 + cameraOffset;
            if (posxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Camera");
            }

            ValidationResult cameraResult = InteractionCameraSettings.validateStructure(buffer, posxxxx);
            if (!cameraResult.isValid()) {
               return ValidationResult.error("Invalid Camera: " + cameraResult.error());
            }

            posxxxx += InteractionCameraSettings.computeBytesConsumed(buffer, posxxxx);
         }

         if ((nullBits & 32) != 0) {
            int statModifiersOffset = buffer.getIntLE(offset + 42);
            if (statModifiersOffset < 0) {
               return ValidationResult.error("Invalid offset for StatModifiers");
            }

            int posxxxxx = offset + 46 + statModifiersOffset;
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

   public ChangeStatInteraction clone() {
      ChangeStatInteraction copy = new ChangeStatInteraction();
      copy.waitForDataFrom = this.waitForDataFrom;
      copy.effects = this.effects != null ? this.effects.clone() : null;
      copy.horizontalSpeedMultiplier = this.horizontalSpeedMultiplier;
      copy.runTime = this.runTime;
      copy.cancelOnItemChange = this.cancelOnItemChange;
      if (this.settings != null) {
         Map<GameMode, InteractionSettings> m = new HashMap<>();

         for (Entry<GameMode, InteractionSettings> e : this.settings.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.settings = m;
      }

      copy.rules = this.rules != null ? this.rules.clone() : null;
      copy.tags = this.tags != null ? Arrays.copyOf(this.tags, this.tags.length) : null;
      copy.camera = this.camera != null ? this.camera.clone() : null;
      copy.next = this.next;
      copy.failed = this.failed;
      copy.entityTarget = this.entityTarget;
      copy.valueType = this.valueType;
      copy.statModifiers = this.statModifiers != null ? new HashMap<>(this.statModifiers) : null;
      copy.changeStatBehaviour = this.changeStatBehaviour;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ChangeStatInteraction other)
            ? false
            : Objects.equals(this.waitForDataFrom, other.waitForDataFrom)
               && Objects.equals(this.effects, other.effects)
               && this.horizontalSpeedMultiplier == other.horizontalSpeedMultiplier
               && this.runTime == other.runTime
               && this.cancelOnItemChange == other.cancelOnItemChange
               && Objects.equals(this.settings, other.settings)
               && Objects.equals(this.rules, other.rules)
               && Arrays.equals(this.tags, other.tags)
               && Objects.equals(this.camera, other.camera)
               && this.next == other.next
               && this.failed == other.failed
               && Objects.equals(this.entityTarget, other.entityTarget)
               && Objects.equals(this.valueType, other.valueType)
               && Objects.equals(this.statModifiers, other.statModifiers)
               && Objects.equals(this.changeStatBehaviour, other.changeStatBehaviour);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.waitForDataFrom);
      result = 31 * result + Objects.hashCode(this.effects);
      result = 31 * result + Float.hashCode(this.horizontalSpeedMultiplier);
      result = 31 * result + Float.hashCode(this.runTime);
      result = 31 * result + Boolean.hashCode(this.cancelOnItemChange);
      result = 31 * result + Objects.hashCode(this.settings);
      result = 31 * result + Objects.hashCode(this.rules);
      result = 31 * result + Arrays.hashCode(this.tags);
      result = 31 * result + Objects.hashCode(this.camera);
      result = 31 * result + Integer.hashCode(this.next);
      result = 31 * result + Integer.hashCode(this.failed);
      result = 31 * result + Objects.hashCode(this.entityTarget);
      result = 31 * result + Objects.hashCode(this.valueType);
      result = 31 * result + Objects.hashCode(this.statModifiers);
      return 31 * result + Objects.hashCode(this.changeStatBehaviour);
   }
}
