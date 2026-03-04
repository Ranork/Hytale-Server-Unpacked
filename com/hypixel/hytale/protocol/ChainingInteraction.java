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

public class ChainingInteraction extends Interaction {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 15;
   public static final int VARIABLE_FIELD_COUNT = 8;
   public static final int VARIABLE_BLOCK_START = 47;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public String chainId;
   public float chainingAllowance;
   @Nullable
   public int[] chainingNext;
   @Nullable
   public Map<String, Integer> flags;

   public ChainingInteraction() {
   }

   public ChainingInteraction(
      @Nonnull WaitForDataFrom waitForDataFrom,
      @Nullable InteractionEffects effects,
      float horizontalSpeedMultiplier,
      float runTime,
      boolean cancelOnItemChange,
      @Nullable Map<GameMode, InteractionSettings> settings,
      @Nullable InteractionRules rules,
      @Nullable int[] tags,
      @Nullable InteractionCameraSettings camera,
      @Nullable String chainId,
      float chainingAllowance,
      @Nullable int[] chainingNext,
      @Nullable Map<String, Integer> flags
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
      this.chainId = chainId;
      this.chainingAllowance = chainingAllowance;
      this.chainingNext = chainingNext;
      this.flags = flags;
   }

   public ChainingInteraction(@Nonnull ChainingInteraction other) {
      this.waitForDataFrom = other.waitForDataFrom;
      this.effects = other.effects;
      this.horizontalSpeedMultiplier = other.horizontalSpeedMultiplier;
      this.runTime = other.runTime;
      this.cancelOnItemChange = other.cancelOnItemChange;
      this.settings = other.settings;
      this.rules = other.rules;
      this.tags = other.tags;
      this.camera = other.camera;
      this.chainId = other.chainId;
      this.chainingAllowance = other.chainingAllowance;
      this.chainingNext = other.chainingNext;
      this.flags = other.flags;
   }

   @Nonnull
   public static ChainingInteraction deserialize(@Nonnull ByteBuf buf, int offset) {
      ChainingInteraction obj = new ChainingInteraction();
      byte nullBits = buf.getByte(offset);
      obj.waitForDataFrom = WaitForDataFrom.fromValue(buf.getByte(offset + 1));
      obj.horizontalSpeedMultiplier = buf.getFloatLE(offset + 2);
      obj.runTime = buf.getFloatLE(offset + 6);
      obj.cancelOnItemChange = buf.getByte(offset + 10) != 0;
      obj.chainingAllowance = buf.getFloatLE(offset + 11);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 47 + buf.getIntLE(offset + 15);
         obj.effects = InteractionEffects.deserialize(buf, varPos0);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 47 + buf.getIntLE(offset + 19);
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
         int varPos2 = offset + 47 + buf.getIntLE(offset + 23);
         obj.rules = InteractionRules.deserialize(buf, varPos2);
      }

      if ((nullBits & 8) != 0) {
         int varPos3 = offset + 47 + buf.getIntLE(offset + 27);
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
         int varPos4 = offset + 47 + buf.getIntLE(offset + 31);
         obj.camera = InteractionCameraSettings.deserialize(buf, varPos4);
      }

      if ((nullBits & 32) != 0) {
         int varPos5 = offset + 47 + buf.getIntLE(offset + 35);
         int chainIdLen = VarInt.peek(buf, varPos5);
         if (chainIdLen < 0) {
            throw ProtocolException.negativeLength("ChainId", chainIdLen);
         }

         if (chainIdLen > 4096000) {
            throw ProtocolException.stringTooLong("ChainId", chainIdLen, 4096000);
         }

         obj.chainId = PacketIO.readVarString(buf, varPos5, PacketIO.UTF8);
      }

      if ((nullBits & 64) != 0) {
         int varPos6 = offset + 47 + buf.getIntLE(offset + 39);
         int chainingNextCount = VarInt.peek(buf, varPos6);
         if (chainingNextCount < 0) {
            throw ProtocolException.negativeLength("ChainingNext", chainingNextCount);
         }

         if (chainingNextCount > 4096000) {
            throw ProtocolException.arrayTooLong("ChainingNext", chainingNextCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos6);
         if (varPos6 + varIntLen + chainingNextCount * 4L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("ChainingNext", varPos6 + varIntLen + chainingNextCount * 4, buf.readableBytes());
         }

         obj.chainingNext = new int[chainingNextCount];

         for (int ix = 0; ix < chainingNextCount; ix++) {
            obj.chainingNext[ix] = buf.getIntLE(varPos6 + varIntLen + ix * 4);
         }
      }

      if ((nullBits & 128) != 0) {
         int varPos7 = offset + 47 + buf.getIntLE(offset + 43);
         int flagsCount = VarInt.peek(buf, varPos7);
         if (flagsCount < 0) {
            throw ProtocolException.negativeLength("Flags", flagsCount);
         }

         if (flagsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Flags", flagsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos7);
         obj.flags = new HashMap<>(flagsCount);
         int dictPos = varPos7 + varIntLen;

         for (int ix = 0; ix < flagsCount; ix++) {
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
            int val = buf.getIntLE(dictPos);
            dictPos += 4;
            if (obj.flags.put(key, val) != null) {
               throw ProtocolException.duplicateKey("flags", key);
            }
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 47;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 15);
         int pos0 = offset + 47 + fieldOffset0;
         pos0 += InteractionEffects.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 19);
         int pos1 = offset + 47 + fieldOffset1;
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
         int fieldOffset2 = buf.getIntLE(offset + 23);
         int pos2 = offset + 47 + fieldOffset2;
         pos2 += InteractionRules.computeBytesConsumed(buf, pos2);
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 27);
         int pos3 = offset + 47 + fieldOffset3;
         int arrLen = VarInt.peek(buf, pos3);
         pos3 += VarInt.length(buf, pos3) + arrLen * 4;
         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      if ((nullBits & 16) != 0) {
         int fieldOffset4 = buf.getIntLE(offset + 31);
         int pos4 = offset + 47 + fieldOffset4;
         pos4 += InteractionCameraSettings.computeBytesConsumed(buf, pos4);
         if (pos4 - offset > maxEnd) {
            maxEnd = pos4 - offset;
         }
      }

      if ((nullBits & 32) != 0) {
         int fieldOffset5 = buf.getIntLE(offset + 35);
         int pos5 = offset + 47 + fieldOffset5;
         int sl = VarInt.peek(buf, pos5);
         pos5 += VarInt.length(buf, pos5) + sl;
         if (pos5 - offset > maxEnd) {
            maxEnd = pos5 - offset;
         }
      }

      if ((nullBits & 64) != 0) {
         int fieldOffset6 = buf.getIntLE(offset + 39);
         int pos6 = offset + 47 + fieldOffset6;
         int arrLen = VarInt.peek(buf, pos6);
         pos6 += VarInt.length(buf, pos6) + arrLen * 4;
         if (pos6 - offset > maxEnd) {
            maxEnd = pos6 - offset;
         }
      }

      if ((nullBits & 128) != 0) {
         int fieldOffset7 = buf.getIntLE(offset + 43);
         int pos7 = offset + 47 + fieldOffset7;
         int dictLen = VarInt.peek(buf, pos7);
         pos7 += VarInt.length(buf, pos7);

         for (int i = 0; i < dictLen; i++) {
            int sl = VarInt.peek(buf, pos7);
            pos7 += VarInt.length(buf, pos7) + sl;
            pos7 += 4;
         }

         if (pos7 - offset > maxEnd) {
            maxEnd = pos7 - offset;
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

      if (this.chainId != null) {
         nullBits = (byte)(nullBits | 32);
      }

      if (this.chainingNext != null) {
         nullBits = (byte)(nullBits | 64);
      }

      if (this.flags != null) {
         nullBits = (byte)(nullBits | 128);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.waitForDataFrom.getValue());
      buf.writeFloatLE(this.horizontalSpeedMultiplier);
      buf.writeFloatLE(this.runTime);
      buf.writeByte(this.cancelOnItemChange ? 1 : 0);
      buf.writeFloatLE(this.chainingAllowance);
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
      int chainIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int chainingNextOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int flagsOffsetSlot = buf.writerIndex();
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

      if (this.chainId != null) {
         buf.setIntLE(chainIdOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.chainId, 4096000);
      } else {
         buf.setIntLE(chainIdOffsetSlot, -1);
      }

      if (this.chainingNext != null) {
         buf.setIntLE(chainingNextOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.chainingNext.length > 4096000) {
            throw ProtocolException.arrayTooLong("ChainingNext", this.chainingNext.length, 4096000);
         }

         VarInt.write(buf, this.chainingNext.length);

         for (int item : this.chainingNext) {
            buf.writeIntLE(item);
         }
      } else {
         buf.setIntLE(chainingNextOffsetSlot, -1);
      }

      if (this.flags != null) {
         buf.setIntLE(flagsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.flags.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Flags", this.flags.size(), 4096000);
         }

         VarInt.write(buf, this.flags.size());

         for (Entry<String, Integer> e : this.flags.entrySet()) {
            PacketIO.writeVarString(buf, e.getKey(), 4096000);
            buf.writeIntLE(e.getValue());
         }
      } else {
         buf.setIntLE(flagsOffsetSlot, -1);
      }

      return buf.writerIndex() - startPos;
   }

   @Override
   public int computeSize() {
      int size = 47;
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

      if (this.chainId != null) {
         size += PacketIO.stringSize(this.chainId);
      }

      if (this.chainingNext != null) {
         size += VarInt.size(this.chainingNext.length) + this.chainingNext.length * 4;
      }

      if (this.flags != null) {
         int flagsSize = 0;

         for (Entry<String, Integer> kvp : this.flags.entrySet()) {
            flagsSize += PacketIO.stringSize(kvp.getKey()) + 4;
         }

         size += VarInt.size(this.flags.size()) + flagsSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 47) {
         return ValidationResult.error("Buffer too small: expected at least 47 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int effectsOffset = buffer.getIntLE(offset + 15);
            if (effectsOffset < 0) {
               return ValidationResult.error("Invalid offset for Effects");
            }

            int pos = offset + 47 + effectsOffset;
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
            int settingsOffset = buffer.getIntLE(offset + 19);
            if (settingsOffset < 0) {
               return ValidationResult.error("Invalid offset for Settings");
            }

            int posx = offset + 47 + settingsOffset;
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
            int rulesOffset = buffer.getIntLE(offset + 23);
            if (rulesOffset < 0) {
               return ValidationResult.error("Invalid offset for Rules");
            }

            int posxx = offset + 47 + rulesOffset;
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
            int tagsOffset = buffer.getIntLE(offset + 27);
            if (tagsOffset < 0) {
               return ValidationResult.error("Invalid offset for Tags");
            }

            int posxxx = offset + 47 + tagsOffset;
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
            int cameraOffset = buffer.getIntLE(offset + 31);
            if (cameraOffset < 0) {
               return ValidationResult.error("Invalid offset for Camera");
            }

            int posxxxx = offset + 47 + cameraOffset;
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
            int chainIdOffset = buffer.getIntLE(offset + 35);
            if (chainIdOffset < 0) {
               return ValidationResult.error("Invalid offset for ChainId");
            }

            int posxxxxx = offset + 47 + chainIdOffset;
            if (posxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ChainId");
            }

            int chainIdLen = VarInt.peek(buffer, posxxxxx);
            if (chainIdLen < 0) {
               return ValidationResult.error("Invalid string length for ChainId");
            }

            if (chainIdLen > 4096000) {
               return ValidationResult.error("ChainId exceeds max length 4096000");
            }

            posxxxxx += VarInt.length(buffer, posxxxxx);
            posxxxxx += chainIdLen;
            if (posxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading ChainId");
            }
         }

         if ((nullBits & 64) != 0) {
            int chainingNextOffset = buffer.getIntLE(offset + 39);
            if (chainingNextOffset < 0) {
               return ValidationResult.error("Invalid offset for ChainingNext");
            }

            int posxxxxxx = offset + 47 + chainingNextOffset;
            if (posxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ChainingNext");
            }

            int chainingNextCount = VarInt.peek(buffer, posxxxxxx);
            if (chainingNextCount < 0) {
               return ValidationResult.error("Invalid array count for ChainingNext");
            }

            if (chainingNextCount > 4096000) {
               return ValidationResult.error("ChainingNext exceeds max length 4096000");
            }

            posxxxxxx += VarInt.length(buffer, posxxxxxx);
            posxxxxxx += chainingNextCount * 4;
            if (posxxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading ChainingNext");
            }
         }

         if ((nullBits & 128) != 0) {
            int flagsOffset = buffer.getIntLE(offset + 43);
            if (flagsOffset < 0) {
               return ValidationResult.error("Invalid offset for Flags");
            }

            int posxxxxxxx = offset + 47 + flagsOffset;
            if (posxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Flags");
            }

            int flagsCount = VarInt.peek(buffer, posxxxxxxx);
            if (flagsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for Flags");
            }

            if (flagsCount > 4096000) {
               return ValidationResult.error("Flags exceeds max length 4096000");
            }

            posxxxxxxx += VarInt.length(buffer, posxxxxxxx);

            for (int i = 0; i < flagsCount; i++) {
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

               posxxxxxxx += 4;
               if (posxxxxxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading value");
               }
            }
         }

         return ValidationResult.OK;
      }
   }

   public ChainingInteraction clone() {
      ChainingInteraction copy = new ChainingInteraction();
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
      copy.chainId = this.chainId;
      copy.chainingAllowance = this.chainingAllowance;
      copy.chainingNext = this.chainingNext != null ? Arrays.copyOf(this.chainingNext, this.chainingNext.length) : null;
      copy.flags = this.flags != null ? new HashMap<>(this.flags) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ChainingInteraction other)
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
               && Objects.equals(this.chainId, other.chainId)
               && this.chainingAllowance == other.chainingAllowance
               && Arrays.equals(this.chainingNext, other.chainingNext)
               && Objects.equals(this.flags, other.flags);
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
      result = 31 * result + Objects.hashCode(this.chainId);
      result = 31 * result + Float.hashCode(this.chainingAllowance);
      result = 31 * result + Arrays.hashCode(this.chainingNext);
      return 31 * result + Objects.hashCode(this.flags);
   }
}
