package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
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

public class DamageEntityInteraction extends Interaction {
   public static final int NULLABLE_BIT_FIELD_SIZE = 2;
   public static final int FIXED_BLOCK_SIZE = 24;
   public static final int VARIABLE_FIELD_COUNT = 9;
   public static final int VARIABLE_BLOCK_START = 60;
   public static final int MAX_SIZE = 1677721600;
   public int next = Integer.MIN_VALUE;
   public int failed = Integer.MIN_VALUE;
   public int blocked = Integer.MIN_VALUE;
   @Nullable
   public DamageEffects damageEffects;
   @Nullable
   public AngledDamage[] angledDamage;
   @Nullable
   public Map<String, TargetedDamage> targetedDamage;
   @Nullable
   public EntityStatOnHit[] entityStatsOnHit;

   public DamageEntityInteraction() {
   }

   public DamageEntityInteraction(
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
      int blocked,
      @Nullable DamageEffects damageEffects,
      @Nullable AngledDamage[] angledDamage,
      @Nullable Map<String, TargetedDamage> targetedDamage,
      @Nullable EntityStatOnHit[] entityStatsOnHit
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
      this.blocked = blocked;
      this.damageEffects = damageEffects;
      this.angledDamage = angledDamage;
      this.targetedDamage = targetedDamage;
      this.entityStatsOnHit = entityStatsOnHit;
   }

   public DamageEntityInteraction(@Nonnull DamageEntityInteraction other) {
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
      this.blocked = other.blocked;
      this.damageEffects = other.damageEffects;
      this.angledDamage = other.angledDamage;
      this.targetedDamage = other.targetedDamage;
      this.entityStatsOnHit = other.entityStatsOnHit;
   }

   @Nonnull
   public static DamageEntityInteraction deserialize(@Nonnull ByteBuf buf, int offset) {
      DamageEntityInteraction obj = new DamageEntityInteraction();
      byte[] nullBits = PacketIO.readBytes(buf, offset, 2);
      obj.waitForDataFrom = WaitForDataFrom.fromValue(buf.getByte(offset + 2));
      obj.horizontalSpeedMultiplier = buf.getFloatLE(offset + 3);
      obj.runTime = buf.getFloatLE(offset + 7);
      obj.cancelOnItemChange = buf.getByte(offset + 11) != 0;
      obj.next = buf.getIntLE(offset + 12);
      obj.failed = buf.getIntLE(offset + 16);
      obj.blocked = buf.getIntLE(offset + 20);
      if ((nullBits[0] & 1) != 0) {
         int varPos0 = offset + 60 + buf.getIntLE(offset + 24);
         obj.effects = InteractionEffects.deserialize(buf, varPos0);
      }

      if ((nullBits[0] & 2) != 0) {
         int varPos1 = offset + 60 + buf.getIntLE(offset + 28);
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

      if ((nullBits[0] & 4) != 0) {
         int varPos2 = offset + 60 + buf.getIntLE(offset + 32);
         obj.rules = InteractionRules.deserialize(buf, varPos2);
      }

      if ((nullBits[0] & 8) != 0) {
         int varPos3 = offset + 60 + buf.getIntLE(offset + 36);
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

      if ((nullBits[0] & 16) != 0) {
         int varPos4 = offset + 60 + buf.getIntLE(offset + 40);
         obj.camera = InteractionCameraSettings.deserialize(buf, varPos4);
      }

      if ((nullBits[0] & 32) != 0) {
         int varPos5 = offset + 60 + buf.getIntLE(offset + 44);
         obj.damageEffects = DamageEffects.deserialize(buf, varPos5);
      }

      if ((nullBits[0] & 64) != 0) {
         int varPos6 = offset + 60 + buf.getIntLE(offset + 48);
         int angledDamageCount = VarInt.peek(buf, varPos6);
         if (angledDamageCount < 0) {
            throw ProtocolException.negativeLength("AngledDamage", angledDamageCount);
         }

         if (angledDamageCount > 4096000) {
            throw ProtocolException.arrayTooLong("AngledDamage", angledDamageCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos6);
         if (varPos6 + varIntLen + angledDamageCount * 21L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("AngledDamage", varPos6 + varIntLen + angledDamageCount * 21, buf.readableBytes());
         }

         obj.angledDamage = new AngledDamage[angledDamageCount];
         int elemPos = varPos6 + varIntLen;

         for (int ix = 0; ix < angledDamageCount; ix++) {
            obj.angledDamage[ix] = AngledDamage.deserialize(buf, elemPos);
            elemPos += AngledDamage.computeBytesConsumed(buf, elemPos);
         }
      }

      if ((nullBits[0] & 128) != 0) {
         int varPos7 = offset + 60 + buf.getIntLE(offset + 52);
         int targetedDamageCount = VarInt.peek(buf, varPos7);
         if (targetedDamageCount < 0) {
            throw ProtocolException.negativeLength("TargetedDamage", targetedDamageCount);
         }

         if (targetedDamageCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("TargetedDamage", targetedDamageCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos7);
         obj.targetedDamage = new HashMap<>(targetedDamageCount);
         int dictPos = varPos7 + varIntLen;

         for (int ix = 0; ix < targetedDamageCount; ix++) {
            int keyLen = VarInt.peek(buf, dictPos);
            if (keyLen < 0) {
               throw ProtocolException.negativeLength("key", keyLen);
            }

            if (keyLen > 4096000) {
               throw ProtocolException.stringTooLong("key", keyLen, 4096000);
            }

            int keyVarLen = VarInt.length(buf, dictPos);
            String key = PacketIO.readVarString(buf, dictPos);
            dictPos += keyVarLen + keyLen;
            TargetedDamage val = TargetedDamage.deserialize(buf, dictPos);
            dictPos += TargetedDamage.computeBytesConsumed(buf, dictPos);
            if (obj.targetedDamage.put(key, val) != null) {
               throw ProtocolException.duplicateKey("targetedDamage", key);
            }
         }
      }

      if ((nullBits[1] & 1) != 0) {
         int varPos8 = offset + 60 + buf.getIntLE(offset + 56);
         int entityStatsOnHitCount = VarInt.peek(buf, varPos8);
         if (entityStatsOnHitCount < 0) {
            throw ProtocolException.negativeLength("EntityStatsOnHit", entityStatsOnHitCount);
         }

         if (entityStatsOnHitCount > 4096000) {
            throw ProtocolException.arrayTooLong("EntityStatsOnHit", entityStatsOnHitCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos8);
         if (varPos8 + varIntLen + entityStatsOnHitCount * 13L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("EntityStatsOnHit", varPos8 + varIntLen + entityStatsOnHitCount * 13, buf.readableBytes());
         }

         obj.entityStatsOnHit = new EntityStatOnHit[entityStatsOnHitCount];
         int elemPos = varPos8 + varIntLen;

         for (int ix = 0; ix < entityStatsOnHitCount; ix++) {
            obj.entityStatsOnHit[ix] = EntityStatOnHit.deserialize(buf, elemPos);
            elemPos += EntityStatOnHit.computeBytesConsumed(buf, elemPos);
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte[] nullBits = PacketIO.readBytes(buf, offset, 2);
      int maxEnd = 60;
      if ((nullBits[0] & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 24);
         int pos0 = offset + 60 + fieldOffset0;
         pos0 += InteractionEffects.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits[0] & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 28);
         int pos1 = offset + 60 + fieldOffset1;
         int dictLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1);

         for (int i = 0; i < dictLen; i++) {
            pos1 = ++pos1 + InteractionSettings.computeBytesConsumed(buf, pos1);
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits[0] & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 32);
         int pos2 = offset + 60 + fieldOffset2;
         pos2 += InteractionRules.computeBytesConsumed(buf, pos2);
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits[0] & 8) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 36);
         int pos3 = offset + 60 + fieldOffset3;
         int arrLen = VarInt.peek(buf, pos3);
         pos3 += VarInt.length(buf, pos3) + arrLen * 4;
         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      if ((nullBits[0] & 16) != 0) {
         int fieldOffset4 = buf.getIntLE(offset + 40);
         int pos4 = offset + 60 + fieldOffset4;
         pos4 += InteractionCameraSettings.computeBytesConsumed(buf, pos4);
         if (pos4 - offset > maxEnd) {
            maxEnd = pos4 - offset;
         }
      }

      if ((nullBits[0] & 32) != 0) {
         int fieldOffset5 = buf.getIntLE(offset + 44);
         int pos5 = offset + 60 + fieldOffset5;
         pos5 += DamageEffects.computeBytesConsumed(buf, pos5);
         if (pos5 - offset > maxEnd) {
            maxEnd = pos5 - offset;
         }
      }

      if ((nullBits[0] & 64) != 0) {
         int fieldOffset6 = buf.getIntLE(offset + 48);
         int pos6 = offset + 60 + fieldOffset6;
         int arrLen = VarInt.peek(buf, pos6);
         pos6 += VarInt.length(buf, pos6);

         for (int i = 0; i < arrLen; i++) {
            pos6 += AngledDamage.computeBytesConsumed(buf, pos6);
         }

         if (pos6 - offset > maxEnd) {
            maxEnd = pos6 - offset;
         }
      }

      if ((nullBits[0] & 128) != 0) {
         int fieldOffset7 = buf.getIntLE(offset + 52);
         int pos7 = offset + 60 + fieldOffset7;
         int dictLen = VarInt.peek(buf, pos7);
         pos7 += VarInt.length(buf, pos7);

         for (int i = 0; i < dictLen; i++) {
            int sl = VarInt.peek(buf, pos7);
            pos7 += VarInt.length(buf, pos7) + sl;
            pos7 += TargetedDamage.computeBytesConsumed(buf, pos7);
         }

         if (pos7 - offset > maxEnd) {
            maxEnd = pos7 - offset;
         }
      }

      if ((nullBits[1] & 1) != 0) {
         int fieldOffset8 = buf.getIntLE(offset + 56);
         int pos8 = offset + 60 + fieldOffset8;
         int arrLen = VarInt.peek(buf, pos8);
         pos8 += VarInt.length(buf, pos8);

         for (int i = 0; i < arrLen; i++) {
            pos8 += EntityStatOnHit.computeBytesConsumed(buf, pos8);
         }

         if (pos8 - offset > maxEnd) {
            maxEnd = pos8 - offset;
         }
      }

      return maxEnd;
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte[] nullBits = new byte[2];
      if (this.effects != null) {
         nullBits[0] = (byte)(nullBits[0] | 1);
      }

      if (this.settings != null) {
         nullBits[0] = (byte)(nullBits[0] | 2);
      }

      if (this.rules != null) {
         nullBits[0] = (byte)(nullBits[0] | 4);
      }

      if (this.tags != null) {
         nullBits[0] = (byte)(nullBits[0] | 8);
      }

      if (this.camera != null) {
         nullBits[0] = (byte)(nullBits[0] | 16);
      }

      if (this.damageEffects != null) {
         nullBits[0] = (byte)(nullBits[0] | 32);
      }

      if (this.angledDamage != null) {
         nullBits[0] = (byte)(nullBits[0] | 64);
      }

      if (this.targetedDamage != null) {
         nullBits[0] = (byte)(nullBits[0] | 128);
      }

      if (this.entityStatsOnHit != null) {
         nullBits[1] = (byte)(nullBits[1] | 1);
      }

      buf.writeBytes(nullBits);
      buf.writeByte(this.waitForDataFrom.getValue());
      buf.writeFloatLE(this.horizontalSpeedMultiplier);
      buf.writeFloatLE(this.runTime);
      buf.writeByte(this.cancelOnItemChange ? 1 : 0);
      buf.writeIntLE(this.next);
      buf.writeIntLE(this.failed);
      buf.writeIntLE(this.blocked);
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
      int damageEffectsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int angledDamageOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int targetedDamageOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int entityStatsOnHitOffsetSlot = buf.writerIndex();
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

      if (this.damageEffects != null) {
         buf.setIntLE(damageEffectsOffsetSlot, buf.writerIndex() - varBlockStart);
         this.damageEffects.serialize(buf);
      } else {
         buf.setIntLE(damageEffectsOffsetSlot, -1);
      }

      if (this.angledDamage != null) {
         buf.setIntLE(angledDamageOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.angledDamage.length > 4096000) {
            throw ProtocolException.arrayTooLong("AngledDamage", this.angledDamage.length, 4096000);
         }

         VarInt.write(buf, this.angledDamage.length);

         for (AngledDamage item : this.angledDamage) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(angledDamageOffsetSlot, -1);
      }

      if (this.targetedDamage != null) {
         buf.setIntLE(targetedDamageOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.targetedDamage.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("TargetedDamage", this.targetedDamage.size(), 4096000);
         }

         VarInt.write(buf, this.targetedDamage.size());

         for (Entry<String, TargetedDamage> e : this.targetedDamage.entrySet()) {
            PacketIO.writeVarString(buf, e.getKey(), 4096000);
            e.getValue().serialize(buf);
         }
      } else {
         buf.setIntLE(targetedDamageOffsetSlot, -1);
      }

      if (this.entityStatsOnHit != null) {
         buf.setIntLE(entityStatsOnHitOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.entityStatsOnHit.length > 4096000) {
            throw ProtocolException.arrayTooLong("EntityStatsOnHit", this.entityStatsOnHit.length, 4096000);
         }

         VarInt.write(buf, this.entityStatsOnHit.length);

         for (EntityStatOnHit item : this.entityStatsOnHit) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(entityStatsOnHitOffsetSlot, -1);
      }

      return buf.writerIndex() - startPos;
   }

   @Override
   public int computeSize() {
      int size = 60;
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

      if (this.damageEffects != null) {
         size += this.damageEffects.computeSize();
      }

      if (this.angledDamage != null) {
         int angledDamageSize = 0;

         for (AngledDamage elem : this.angledDamage) {
            angledDamageSize += elem.computeSize();
         }

         size += VarInt.size(this.angledDamage.length) + angledDamageSize;
      }

      if (this.targetedDamage != null) {
         int targetedDamageSize = 0;

         for (Entry<String, TargetedDamage> kvp : this.targetedDamage.entrySet()) {
            targetedDamageSize += PacketIO.stringSize(kvp.getKey()) + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.targetedDamage.size()) + targetedDamageSize;
      }

      if (this.entityStatsOnHit != null) {
         int entityStatsOnHitSize = 0;

         for (EntityStatOnHit elem : this.entityStatsOnHit) {
            entityStatsOnHitSize += elem.computeSize();
         }

         size += VarInt.size(this.entityStatsOnHit.length) + entityStatsOnHitSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 60) {
         return ValidationResult.error("Buffer too small: expected at least 60 bytes");
      } else {
         byte[] nullBits = PacketIO.readBytes(buffer, offset, 2);
         if ((nullBits[0] & 1) != 0) {
            int effectsOffset = buffer.getIntLE(offset + 24);
            if (effectsOffset < 0) {
               return ValidationResult.error("Invalid offset for Effects");
            }

            int pos = offset + 60 + effectsOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Effects");
            }

            ValidationResult effectsResult = InteractionEffects.validateStructure(buffer, pos);
            if (!effectsResult.isValid()) {
               return ValidationResult.error("Invalid Effects: " + effectsResult.error());
            }

            pos += InteractionEffects.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits[0] & 2) != 0) {
            int settingsOffset = buffer.getIntLE(offset + 28);
            if (settingsOffset < 0) {
               return ValidationResult.error("Invalid offset for Settings");
            }

            int posx = offset + 60 + settingsOffset;
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

         if ((nullBits[0] & 4) != 0) {
            int rulesOffset = buffer.getIntLE(offset + 32);
            if (rulesOffset < 0) {
               return ValidationResult.error("Invalid offset for Rules");
            }

            int posxx = offset + 60 + rulesOffset;
            if (posxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Rules");
            }

            ValidationResult rulesResult = InteractionRules.validateStructure(buffer, posxx);
            if (!rulesResult.isValid()) {
               return ValidationResult.error("Invalid Rules: " + rulesResult.error());
            }

            posxx += InteractionRules.computeBytesConsumed(buffer, posxx);
         }

         if ((nullBits[0] & 8) != 0) {
            int tagsOffset = buffer.getIntLE(offset + 36);
            if (tagsOffset < 0) {
               return ValidationResult.error("Invalid offset for Tags");
            }

            int posxxx = offset + 60 + tagsOffset;
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

         if ((nullBits[0] & 16) != 0) {
            int cameraOffset = buffer.getIntLE(offset + 40);
            if (cameraOffset < 0) {
               return ValidationResult.error("Invalid offset for Camera");
            }

            int posxxxx = offset + 60 + cameraOffset;
            if (posxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Camera");
            }

            ValidationResult cameraResult = InteractionCameraSettings.validateStructure(buffer, posxxxx);
            if (!cameraResult.isValid()) {
               return ValidationResult.error("Invalid Camera: " + cameraResult.error());
            }

            posxxxx += InteractionCameraSettings.computeBytesConsumed(buffer, posxxxx);
         }

         if ((nullBits[0] & 32) != 0) {
            int damageEffectsOffset = buffer.getIntLE(offset + 44);
            if (damageEffectsOffset < 0) {
               return ValidationResult.error("Invalid offset for DamageEffects");
            }

            int posxxxxx = offset + 60 + damageEffectsOffset;
            if (posxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for DamageEffects");
            }

            ValidationResult damageEffectsResult = DamageEffects.validateStructure(buffer, posxxxxx);
            if (!damageEffectsResult.isValid()) {
               return ValidationResult.error("Invalid DamageEffects: " + damageEffectsResult.error());
            }

            posxxxxx += DamageEffects.computeBytesConsumed(buffer, posxxxxx);
         }

         if ((nullBits[0] & 64) != 0) {
            int angledDamageOffset = buffer.getIntLE(offset + 48);
            if (angledDamageOffset < 0) {
               return ValidationResult.error("Invalid offset for AngledDamage");
            }

            int posxxxxxx = offset + 60 + angledDamageOffset;
            if (posxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for AngledDamage");
            }

            int angledDamageCount = VarInt.peek(buffer, posxxxxxx);
            if (angledDamageCount < 0) {
               return ValidationResult.error("Invalid array count for AngledDamage");
            }

            if (angledDamageCount > 4096000) {
               return ValidationResult.error("AngledDamage exceeds max length 4096000");
            }

            posxxxxxx += VarInt.length(buffer, posxxxxxx);

            for (int i = 0; i < angledDamageCount; i++) {
               ValidationResult structResult = AngledDamage.validateStructure(buffer, posxxxxxx);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid AngledDamage in AngledDamage[" + i + "]: " + structResult.error());
               }

               posxxxxxx += AngledDamage.computeBytesConsumed(buffer, posxxxxxx);
            }
         }

         if ((nullBits[0] & 128) != 0) {
            int targetedDamageOffset = buffer.getIntLE(offset + 52);
            if (targetedDamageOffset < 0) {
               return ValidationResult.error("Invalid offset for TargetedDamage");
            }

            int posxxxxxxx = offset + 60 + targetedDamageOffset;
            if (posxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for TargetedDamage");
            }

            int targetedDamageCount = VarInt.peek(buffer, posxxxxxxx);
            if (targetedDamageCount < 0) {
               return ValidationResult.error("Invalid dictionary count for TargetedDamage");
            }

            if (targetedDamageCount > 4096000) {
               return ValidationResult.error("TargetedDamage exceeds max length 4096000");
            }

            posxxxxxxx += VarInt.length(buffer, posxxxxxxx);

            for (int i = 0; i < targetedDamageCount; i++) {
               int keyLen = VarInt.peek(buffer, posxxxxxxx);
               if (keyLen < 0) {
                  return ValidationResult.error("Invalid string length for key");
               }

               if (keyLen > 4096000) {
                  return ValidationResult.error("key exceeds max length 4096000");
               }

               posxxxxxxx += VarInt.length(buffer, posxxxxxxx);
               posxxxxxxx += keyLen;
               if (posxxxxxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               posxxxxxxx += TargetedDamage.computeBytesConsumed(buffer, posxxxxxxx);
            }
         }

         if ((nullBits[1] & 1) != 0) {
            int entityStatsOnHitOffset = buffer.getIntLE(offset + 56);
            if (entityStatsOnHitOffset < 0) {
               return ValidationResult.error("Invalid offset for EntityStatsOnHit");
            }

            int posxxxxxxxx = offset + 60 + entityStatsOnHitOffset;
            if (posxxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for EntityStatsOnHit");
            }

            int entityStatsOnHitCount = VarInt.peek(buffer, posxxxxxxxx);
            if (entityStatsOnHitCount < 0) {
               return ValidationResult.error("Invalid array count for EntityStatsOnHit");
            }

            if (entityStatsOnHitCount > 4096000) {
               return ValidationResult.error("EntityStatsOnHit exceeds max length 4096000");
            }

            posxxxxxxxx += VarInt.length(buffer, posxxxxxxxx);

            for (int i = 0; i < entityStatsOnHitCount; i++) {
               ValidationResult structResult = EntityStatOnHit.validateStructure(buffer, posxxxxxxxx);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid EntityStatOnHit in EntityStatsOnHit[" + i + "]: " + structResult.error());
               }

               posxxxxxxxx += EntityStatOnHit.computeBytesConsumed(buffer, posxxxxxxxx);
            }
         }

         return ValidationResult.OK;
      }
   }

   public DamageEntityInteraction clone() {
      DamageEntityInteraction copy = new DamageEntityInteraction();
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
      copy.blocked = this.blocked;
      copy.damageEffects = this.damageEffects != null ? this.damageEffects.clone() : null;
      copy.angledDamage = this.angledDamage != null ? Arrays.stream(this.angledDamage).map(ex -> ex.clone()).toArray(AngledDamage[]::new) : null;
      if (this.targetedDamage != null) {
         Map<String, TargetedDamage> m = new HashMap<>();

         for (Entry<String, TargetedDamage> e : this.targetedDamage.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.targetedDamage = m;
      }

      copy.entityStatsOnHit = this.entityStatsOnHit != null ? Arrays.stream(this.entityStatsOnHit).map(ex -> ex.clone()).toArray(EntityStatOnHit[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof DamageEntityInteraction other)
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
               && this.blocked == other.blocked
               && Objects.equals(this.damageEffects, other.damageEffects)
               && Arrays.equals((Object[])this.angledDamage, (Object[])other.angledDamage)
               && Objects.equals(this.targetedDamage, other.targetedDamage)
               && Arrays.equals((Object[])this.entityStatsOnHit, (Object[])other.entityStatsOnHit);
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
      result = 31 * result + Integer.hashCode(this.blocked);
      result = 31 * result + Objects.hashCode(this.damageEffects);
      result = 31 * result + Arrays.hashCode((Object[])this.angledDamage);
      result = 31 * result + Objects.hashCode(this.targetedDamage);
      return 31 * result + Arrays.hashCode((Object[])this.entityStatsOnHit);
   }
}
