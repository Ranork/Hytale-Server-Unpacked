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

public class CancelChainInteraction extends SimpleInteraction {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 19;
   public static final int VARIABLE_FIELD_COUNT = 6;
   public static final int VARIABLE_BLOCK_START = 43;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public String chainId;

   public CancelChainInteraction() {
   }

   public CancelChainInteraction(
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
      @Nullable String chainId
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
      this.chainId = chainId;
   }

   public CancelChainInteraction(@Nonnull CancelChainInteraction other) {
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
      this.chainId = other.chainId;
   }

   @Nonnull
   public static CancelChainInteraction deserialize(@Nonnull ByteBuf buf, int offset) {
      CancelChainInteraction obj = new CancelChainInteraction();
      byte nullBits = buf.getByte(offset);
      obj.waitForDataFrom = WaitForDataFrom.fromValue(buf.getByte(offset + 1));
      obj.horizontalSpeedMultiplier = buf.getFloatLE(offset + 2);
      obj.runTime = buf.getFloatLE(offset + 6);
      obj.cancelOnItemChange = buf.getByte(offset + 10) != 0;
      obj.next = buf.getIntLE(offset + 11);
      obj.failed = buf.getIntLE(offset + 15);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 43 + buf.getIntLE(offset + 19);
         obj.effects = InteractionEffects.deserialize(buf, varPos0);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 43 + buf.getIntLE(offset + 23);
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
         int varPos2 = offset + 43 + buf.getIntLE(offset + 27);
         obj.rules = InteractionRules.deserialize(buf, varPos2);
      }

      if ((nullBits & 8) != 0) {
         int varPos3 = offset + 43 + buf.getIntLE(offset + 31);
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
         int varPos4 = offset + 43 + buf.getIntLE(offset + 35);
         obj.camera = InteractionCameraSettings.deserialize(buf, varPos4);
      }

      if ((nullBits & 32) != 0) {
         int varPos5 = offset + 43 + buf.getIntLE(offset + 39);
         int chainIdLen = VarInt.peek(buf, varPos5);
         if (chainIdLen < 0) {
            throw ProtocolException.negativeLength("ChainId", chainIdLen);
         }

         if (chainIdLen > 4096000) {
            throw ProtocolException.stringTooLong("ChainId", chainIdLen, 4096000);
         }

         obj.chainId = PacketIO.readVarString(buf, varPos5, PacketIO.UTF8);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 43;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 19);
         int pos0 = offset + 43 + fieldOffset0;
         pos0 += InteractionEffects.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 23);
         int pos1 = offset + 43 + fieldOffset1;
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
         int fieldOffset2 = buf.getIntLE(offset + 27);
         int pos2 = offset + 43 + fieldOffset2;
         pos2 += InteractionRules.computeBytesConsumed(buf, pos2);
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 31);
         int pos3 = offset + 43 + fieldOffset3;
         int arrLen = VarInt.peek(buf, pos3);
         pos3 += VarInt.length(buf, pos3) + arrLen * 4;
         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      if ((nullBits & 16) != 0) {
         int fieldOffset4 = buf.getIntLE(offset + 35);
         int pos4 = offset + 43 + fieldOffset4;
         pos4 += InteractionCameraSettings.computeBytesConsumed(buf, pos4);
         if (pos4 - offset > maxEnd) {
            maxEnd = pos4 - offset;
         }
      }

      if ((nullBits & 32) != 0) {
         int fieldOffset5 = buf.getIntLE(offset + 39);
         int pos5 = offset + 43 + fieldOffset5;
         int sl = VarInt.peek(buf, pos5);
         pos5 += VarInt.length(buf, pos5) + sl;
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

      if (this.chainId != null) {
         nullBits = (byte)(nullBits | 32);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.waitForDataFrom.getValue());
      buf.writeFloatLE(this.horizontalSpeedMultiplier);
      buf.writeFloatLE(this.runTime);
      buf.writeByte(this.cancelOnItemChange ? 1 : 0);
      buf.writeIntLE(this.next);
      buf.writeIntLE(this.failed);
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

      return buf.writerIndex() - startPos;
   }

   @Override
   public int computeSize() {
      int size = 43;
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

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 43) {
         return ValidationResult.error("Buffer too small: expected at least 43 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int effectsOffset = buffer.getIntLE(offset + 19);
            if (effectsOffset < 0) {
               return ValidationResult.error("Invalid offset for Effects");
            }

            int pos = offset + 43 + effectsOffset;
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
            int settingsOffset = buffer.getIntLE(offset + 23);
            if (settingsOffset < 0) {
               return ValidationResult.error("Invalid offset for Settings");
            }

            int posx = offset + 43 + settingsOffset;
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
            int rulesOffset = buffer.getIntLE(offset + 27);
            if (rulesOffset < 0) {
               return ValidationResult.error("Invalid offset for Rules");
            }

            int posxx = offset + 43 + rulesOffset;
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
            int tagsOffset = buffer.getIntLE(offset + 31);
            if (tagsOffset < 0) {
               return ValidationResult.error("Invalid offset for Tags");
            }

            int posxxx = offset + 43 + tagsOffset;
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
            int cameraOffset = buffer.getIntLE(offset + 35);
            if (cameraOffset < 0) {
               return ValidationResult.error("Invalid offset for Camera");
            }

            int posxxxx = offset + 43 + cameraOffset;
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
            int chainIdOffset = buffer.getIntLE(offset + 39);
            if (chainIdOffset < 0) {
               return ValidationResult.error("Invalid offset for ChainId");
            }

            int posxxxxx = offset + 43 + chainIdOffset;
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

         return ValidationResult.OK;
      }
   }

   public CancelChainInteraction clone() {
      CancelChainInteraction copy = new CancelChainInteraction();
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
      copy.chainId = this.chainId;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof CancelChainInteraction other)
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
               && Objects.equals(this.chainId, other.chainId);
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
      return 31 * result + Objects.hashCode(this.chainId);
   }
}
