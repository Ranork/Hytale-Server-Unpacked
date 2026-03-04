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

public class ModifyInventoryInteraction extends SimpleInteraction {
   public static final int NULLABLE_BIT_FIELD_SIZE = 2;
   public static final int FIXED_BLOCK_SIZE = 33;
   public static final int VARIABLE_FIELD_COUNT = 8;
   public static final int VARIABLE_BLOCK_START = 65;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public GameMode requiredGameMode;
   @Nullable
   public ItemWithAllMetadata itemToRemove;
   public int adjustHeldItemQuantity;
   @Nullable
   public ItemWithAllMetadata itemToAdd;
   @Nullable
   public String brokenItem;
   public double adjustHeldItemDurability;

   public ModifyInventoryInteraction() {
   }

   public ModifyInventoryInteraction(
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
      @Nullable GameMode requiredGameMode,
      @Nullable ItemWithAllMetadata itemToRemove,
      int adjustHeldItemQuantity,
      @Nullable ItemWithAllMetadata itemToAdd,
      @Nullable String brokenItem,
      double adjustHeldItemDurability
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
      this.requiredGameMode = requiredGameMode;
      this.itemToRemove = itemToRemove;
      this.adjustHeldItemQuantity = adjustHeldItemQuantity;
      this.itemToAdd = itemToAdd;
      this.brokenItem = brokenItem;
      this.adjustHeldItemDurability = adjustHeldItemDurability;
   }

   public ModifyInventoryInteraction(@Nonnull ModifyInventoryInteraction other) {
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
      this.requiredGameMode = other.requiredGameMode;
      this.itemToRemove = other.itemToRemove;
      this.adjustHeldItemQuantity = other.adjustHeldItemQuantity;
      this.itemToAdd = other.itemToAdd;
      this.brokenItem = other.brokenItem;
      this.adjustHeldItemDurability = other.adjustHeldItemDurability;
   }

   @Nonnull
   public static ModifyInventoryInteraction deserialize(@Nonnull ByteBuf buf, int offset) {
      ModifyInventoryInteraction obj = new ModifyInventoryInteraction();
      byte[] nullBits = PacketIO.readBytes(buf, offset, 2);
      obj.waitForDataFrom = WaitForDataFrom.fromValue(buf.getByte(offset + 2));
      obj.horizontalSpeedMultiplier = buf.getFloatLE(offset + 3);
      obj.runTime = buf.getFloatLE(offset + 7);
      obj.cancelOnItemChange = buf.getByte(offset + 11) != 0;
      obj.next = buf.getIntLE(offset + 12);
      obj.failed = buf.getIntLE(offset + 16);
      if ((nullBits[0] & 1) != 0) {
         obj.requiredGameMode = GameMode.fromValue(buf.getByte(offset + 20));
      }

      obj.adjustHeldItemQuantity = buf.getIntLE(offset + 21);
      obj.adjustHeldItemDurability = buf.getDoubleLE(offset + 25);
      if ((nullBits[0] & 2) != 0) {
         int varPos0 = offset + 65 + buf.getIntLE(offset + 33);
         obj.effects = InteractionEffects.deserialize(buf, varPos0);
      }

      if ((nullBits[0] & 4) != 0) {
         int varPos1 = offset + 65 + buf.getIntLE(offset + 37);
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

      if ((nullBits[0] & 8) != 0) {
         int varPos2 = offset + 65 + buf.getIntLE(offset + 41);
         obj.rules = InteractionRules.deserialize(buf, varPos2);
      }

      if ((nullBits[0] & 16) != 0) {
         int varPos3 = offset + 65 + buf.getIntLE(offset + 45);
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

      if ((nullBits[0] & 32) != 0) {
         int varPos4 = offset + 65 + buf.getIntLE(offset + 49);
         obj.camera = InteractionCameraSettings.deserialize(buf, varPos4);
      }

      if ((nullBits[0] & 64) != 0) {
         int varPos5 = offset + 65 + buf.getIntLE(offset + 53);
         obj.itemToRemove = ItemWithAllMetadata.deserialize(buf, varPos5);
      }

      if ((nullBits[0] & 128) != 0) {
         int varPos6 = offset + 65 + buf.getIntLE(offset + 57);
         obj.itemToAdd = ItemWithAllMetadata.deserialize(buf, varPos6);
      }

      if ((nullBits[1] & 1) != 0) {
         int varPos7 = offset + 65 + buf.getIntLE(offset + 61);
         int brokenItemLen = VarInt.peek(buf, varPos7);
         if (brokenItemLen < 0) {
            throw ProtocolException.negativeLength("BrokenItem", brokenItemLen);
         }

         if (brokenItemLen > 4096000) {
            throw ProtocolException.stringTooLong("BrokenItem", brokenItemLen, 4096000);
         }

         obj.brokenItem = PacketIO.readVarString(buf, varPos7, PacketIO.UTF8);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte[] nullBits = PacketIO.readBytes(buf, offset, 2);
      int maxEnd = 65;
      if ((nullBits[0] & 2) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 33);
         int pos0 = offset + 65 + fieldOffset0;
         pos0 += InteractionEffects.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits[0] & 4) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 37);
         int pos1 = offset + 65 + fieldOffset1;
         int dictLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1);

         for (int i = 0; i < dictLen; i++) {
            pos1 = ++pos1 + InteractionSettings.computeBytesConsumed(buf, pos1);
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits[0] & 8) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 41);
         int pos2 = offset + 65 + fieldOffset2;
         pos2 += InteractionRules.computeBytesConsumed(buf, pos2);
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits[0] & 16) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 45);
         int pos3 = offset + 65 + fieldOffset3;
         int arrLen = VarInt.peek(buf, pos3);
         pos3 += VarInt.length(buf, pos3) + arrLen * 4;
         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      if ((nullBits[0] & 32) != 0) {
         int fieldOffset4 = buf.getIntLE(offset + 49);
         int pos4 = offset + 65 + fieldOffset4;
         pos4 += InteractionCameraSettings.computeBytesConsumed(buf, pos4);
         if (pos4 - offset > maxEnd) {
            maxEnd = pos4 - offset;
         }
      }

      if ((nullBits[0] & 64) != 0) {
         int fieldOffset5 = buf.getIntLE(offset + 53);
         int pos5 = offset + 65 + fieldOffset5;
         pos5 += ItemWithAllMetadata.computeBytesConsumed(buf, pos5);
         if (pos5 - offset > maxEnd) {
            maxEnd = pos5 - offset;
         }
      }

      if ((nullBits[0] & 128) != 0) {
         int fieldOffset6 = buf.getIntLE(offset + 57);
         int pos6 = offset + 65 + fieldOffset6;
         pos6 += ItemWithAllMetadata.computeBytesConsumed(buf, pos6);
         if (pos6 - offset > maxEnd) {
            maxEnd = pos6 - offset;
         }
      }

      if ((nullBits[1] & 1) != 0) {
         int fieldOffset7 = buf.getIntLE(offset + 61);
         int pos7 = offset + 65 + fieldOffset7;
         int sl = VarInt.peek(buf, pos7);
         pos7 += VarInt.length(buf, pos7) + sl;
         if (pos7 - offset > maxEnd) {
            maxEnd = pos7 - offset;
         }
      }

      return maxEnd;
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte[] nullBits = new byte[2];
      if (this.requiredGameMode != null) {
         nullBits[0] = (byte)(nullBits[0] | 1);
      }

      if (this.effects != null) {
         nullBits[0] = (byte)(nullBits[0] | 2);
      }

      if (this.settings != null) {
         nullBits[0] = (byte)(nullBits[0] | 4);
      }

      if (this.rules != null) {
         nullBits[0] = (byte)(nullBits[0] | 8);
      }

      if (this.tags != null) {
         nullBits[0] = (byte)(nullBits[0] | 16);
      }

      if (this.camera != null) {
         nullBits[0] = (byte)(nullBits[0] | 32);
      }

      if (this.itemToRemove != null) {
         nullBits[0] = (byte)(nullBits[0] | 64);
      }

      if (this.itemToAdd != null) {
         nullBits[0] = (byte)(nullBits[0] | 128);
      }

      if (this.brokenItem != null) {
         nullBits[1] = (byte)(nullBits[1] | 1);
      }

      buf.writeBytes(nullBits);
      buf.writeByte(this.waitForDataFrom.getValue());
      buf.writeFloatLE(this.horizontalSpeedMultiplier);
      buf.writeFloatLE(this.runTime);
      buf.writeByte(this.cancelOnItemChange ? 1 : 0);
      buf.writeIntLE(this.next);
      buf.writeIntLE(this.failed);
      if (this.requiredGameMode != null) {
         buf.writeByte(this.requiredGameMode.getValue());
      } else {
         buf.writeZero(1);
      }

      buf.writeIntLE(this.adjustHeldItemQuantity);
      buf.writeDoubleLE(this.adjustHeldItemDurability);
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
      int itemToRemoveOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int itemToAddOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int brokenItemOffsetSlot = buf.writerIndex();
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

      if (this.itemToRemove != null) {
         buf.setIntLE(itemToRemoveOffsetSlot, buf.writerIndex() - varBlockStart);
         this.itemToRemove.serialize(buf);
      } else {
         buf.setIntLE(itemToRemoveOffsetSlot, -1);
      }

      if (this.itemToAdd != null) {
         buf.setIntLE(itemToAddOffsetSlot, buf.writerIndex() - varBlockStart);
         this.itemToAdd.serialize(buf);
      } else {
         buf.setIntLE(itemToAddOffsetSlot, -1);
      }

      if (this.brokenItem != null) {
         buf.setIntLE(brokenItemOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.brokenItem, 4096000);
      } else {
         buf.setIntLE(brokenItemOffsetSlot, -1);
      }

      return buf.writerIndex() - startPos;
   }

   @Override
   public int computeSize() {
      int size = 65;
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

      if (this.itemToRemove != null) {
         size += this.itemToRemove.computeSize();
      }

      if (this.itemToAdd != null) {
         size += this.itemToAdd.computeSize();
      }

      if (this.brokenItem != null) {
         size += PacketIO.stringSize(this.brokenItem);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 65) {
         return ValidationResult.error("Buffer too small: expected at least 65 bytes");
      } else {
         byte[] nullBits = PacketIO.readBytes(buffer, offset, 2);
         if ((nullBits[0] & 2) != 0) {
            int effectsOffset = buffer.getIntLE(offset + 33);
            if (effectsOffset < 0) {
               return ValidationResult.error("Invalid offset for Effects");
            }

            int pos = offset + 65 + effectsOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Effects");
            }

            ValidationResult effectsResult = InteractionEffects.validateStructure(buffer, pos);
            if (!effectsResult.isValid()) {
               return ValidationResult.error("Invalid Effects: " + effectsResult.error());
            }

            pos += InteractionEffects.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits[0] & 4) != 0) {
            int settingsOffset = buffer.getIntLE(offset + 37);
            if (settingsOffset < 0) {
               return ValidationResult.error("Invalid offset for Settings");
            }

            int posx = offset + 65 + settingsOffset;
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

         if ((nullBits[0] & 8) != 0) {
            int rulesOffset = buffer.getIntLE(offset + 41);
            if (rulesOffset < 0) {
               return ValidationResult.error("Invalid offset for Rules");
            }

            int posxx = offset + 65 + rulesOffset;
            if (posxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Rules");
            }

            ValidationResult rulesResult = InteractionRules.validateStructure(buffer, posxx);
            if (!rulesResult.isValid()) {
               return ValidationResult.error("Invalid Rules: " + rulesResult.error());
            }

            posxx += InteractionRules.computeBytesConsumed(buffer, posxx);
         }

         if ((nullBits[0] & 16) != 0) {
            int tagsOffset = buffer.getIntLE(offset + 45);
            if (tagsOffset < 0) {
               return ValidationResult.error("Invalid offset for Tags");
            }

            int posxxx = offset + 65 + tagsOffset;
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

         if ((nullBits[0] & 32) != 0) {
            int cameraOffset = buffer.getIntLE(offset + 49);
            if (cameraOffset < 0) {
               return ValidationResult.error("Invalid offset for Camera");
            }

            int posxxxx = offset + 65 + cameraOffset;
            if (posxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Camera");
            }

            ValidationResult cameraResult = InteractionCameraSettings.validateStructure(buffer, posxxxx);
            if (!cameraResult.isValid()) {
               return ValidationResult.error("Invalid Camera: " + cameraResult.error());
            }

            posxxxx += InteractionCameraSettings.computeBytesConsumed(buffer, posxxxx);
         }

         if ((nullBits[0] & 64) != 0) {
            int itemToRemoveOffset = buffer.getIntLE(offset + 53);
            if (itemToRemoveOffset < 0) {
               return ValidationResult.error("Invalid offset for ItemToRemove");
            }

            int posxxxxx = offset + 65 + itemToRemoveOffset;
            if (posxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ItemToRemove");
            }

            ValidationResult itemToRemoveResult = ItemWithAllMetadata.validateStructure(buffer, posxxxxx);
            if (!itemToRemoveResult.isValid()) {
               return ValidationResult.error("Invalid ItemToRemove: " + itemToRemoveResult.error());
            }

            posxxxxx += ItemWithAllMetadata.computeBytesConsumed(buffer, posxxxxx);
         }

         if ((nullBits[0] & 128) != 0) {
            int itemToAddOffset = buffer.getIntLE(offset + 57);
            if (itemToAddOffset < 0) {
               return ValidationResult.error("Invalid offset for ItemToAdd");
            }

            int posxxxxxx = offset + 65 + itemToAddOffset;
            if (posxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ItemToAdd");
            }

            ValidationResult itemToAddResult = ItemWithAllMetadata.validateStructure(buffer, posxxxxxx);
            if (!itemToAddResult.isValid()) {
               return ValidationResult.error("Invalid ItemToAdd: " + itemToAddResult.error());
            }

            posxxxxxx += ItemWithAllMetadata.computeBytesConsumed(buffer, posxxxxxx);
         }

         if ((nullBits[1] & 1) != 0) {
            int brokenItemOffset = buffer.getIntLE(offset + 61);
            if (brokenItemOffset < 0) {
               return ValidationResult.error("Invalid offset for BrokenItem");
            }

            int posxxxxxxx = offset + 65 + brokenItemOffset;
            if (posxxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for BrokenItem");
            }

            int brokenItemLen = VarInt.peek(buffer, posxxxxxxx);
            if (brokenItemLen < 0) {
               return ValidationResult.error("Invalid string length for BrokenItem");
            }

            if (brokenItemLen > 4096000) {
               return ValidationResult.error("BrokenItem exceeds max length 4096000");
            }

            posxxxxxxx += VarInt.length(buffer, posxxxxxxx);
            posxxxxxxx += brokenItemLen;
            if (posxxxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading BrokenItem");
            }
         }

         return ValidationResult.OK;
      }
   }

   public ModifyInventoryInteraction clone() {
      ModifyInventoryInteraction copy = new ModifyInventoryInteraction();
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
      copy.requiredGameMode = this.requiredGameMode;
      copy.itemToRemove = this.itemToRemove != null ? this.itemToRemove.clone() : null;
      copy.adjustHeldItemQuantity = this.adjustHeldItemQuantity;
      copy.itemToAdd = this.itemToAdd != null ? this.itemToAdd.clone() : null;
      copy.brokenItem = this.brokenItem;
      copy.adjustHeldItemDurability = this.adjustHeldItemDurability;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ModifyInventoryInteraction other)
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
               && Objects.equals(this.requiredGameMode, other.requiredGameMode)
               && Objects.equals(this.itemToRemove, other.itemToRemove)
               && this.adjustHeldItemQuantity == other.adjustHeldItemQuantity
               && Objects.equals(this.itemToAdd, other.itemToAdd)
               && Objects.equals(this.brokenItem, other.brokenItem)
               && this.adjustHeldItemDurability == other.adjustHeldItemDurability;
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
      result = 31 * result + Objects.hashCode(this.requiredGameMode);
      result = 31 * result + Objects.hashCode(this.itemToRemove);
      result = 31 * result + Integer.hashCode(this.adjustHeldItemQuantity);
      result = 31 * result + Objects.hashCode(this.itemToAdd);
      result = 31 * result + Objects.hashCode(this.brokenItem);
      return 31 * result + Double.hashCode(this.adjustHeldItemDurability);
   }
}
