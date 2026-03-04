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

public class ItemArmor {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 10;
   public static final int VARIABLE_FIELD_COUNT = 5;
   public static final int VARIABLE_BLOCK_START = 30;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public ItemArmorSlot armorSlot = ItemArmorSlot.Head;
   @Nullable
   public Cosmetic[] cosmeticsToHide;
   @Nullable
   public Map<Integer, Modifier[]> statModifiers;
   public double baseDamageResistance;
   @Nullable
   public Map<String, Modifier[]> damageResistance;
   @Nullable
   public Map<String, Modifier[]> damageEnhancement;
   @Nullable
   public Map<String, Modifier[]> damageClassEnhancement;

   public ItemArmor() {
   }

   public ItemArmor(
      @Nonnull ItemArmorSlot armorSlot,
      @Nullable Cosmetic[] cosmeticsToHide,
      @Nullable Map<Integer, Modifier[]> statModifiers,
      double baseDamageResistance,
      @Nullable Map<String, Modifier[]> damageResistance,
      @Nullable Map<String, Modifier[]> damageEnhancement,
      @Nullable Map<String, Modifier[]> damageClassEnhancement
   ) {
      this.armorSlot = armorSlot;
      this.cosmeticsToHide = cosmeticsToHide;
      this.statModifiers = statModifiers;
      this.baseDamageResistance = baseDamageResistance;
      this.damageResistance = damageResistance;
      this.damageEnhancement = damageEnhancement;
      this.damageClassEnhancement = damageClassEnhancement;
   }

   public ItemArmor(@Nonnull ItemArmor other) {
      this.armorSlot = other.armorSlot;
      this.cosmeticsToHide = other.cosmeticsToHide;
      this.statModifiers = other.statModifiers;
      this.baseDamageResistance = other.baseDamageResistance;
      this.damageResistance = other.damageResistance;
      this.damageEnhancement = other.damageEnhancement;
      this.damageClassEnhancement = other.damageClassEnhancement;
   }

   @Nonnull
   public static ItemArmor deserialize(@Nonnull ByteBuf buf, int offset) {
      ItemArmor obj = new ItemArmor();
      byte nullBits = buf.getByte(offset);
      obj.armorSlot = ItemArmorSlot.fromValue(buf.getByte(offset + 1));
      obj.baseDamageResistance = buf.getDoubleLE(offset + 2);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 30 + buf.getIntLE(offset + 10);
         int cosmeticsToHideCount = VarInt.peek(buf, varPos0);
         if (cosmeticsToHideCount < 0) {
            throw ProtocolException.negativeLength("CosmeticsToHide", cosmeticsToHideCount);
         }

         if (cosmeticsToHideCount > 4096000) {
            throw ProtocolException.arrayTooLong("CosmeticsToHide", cosmeticsToHideCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos0);
         if (varPos0 + varIntLen + cosmeticsToHideCount * 1L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("CosmeticsToHide", varPos0 + varIntLen + cosmeticsToHideCount * 1, buf.readableBytes());
         }

         obj.cosmeticsToHide = new Cosmetic[cosmeticsToHideCount];
         int elemPos = varPos0 + varIntLen;

         for (int i = 0; i < cosmeticsToHideCount; i++) {
            obj.cosmeticsToHide[i] = Cosmetic.fromValue(buf.getByte(elemPos));
            elemPos++;
         }
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 30 + buf.getIntLE(offset + 14);
         int statModifiersCount = VarInt.peek(buf, varPos1);
         if (statModifiersCount < 0) {
            throw ProtocolException.negativeLength("StatModifiers", statModifiersCount);
         }

         if (statModifiersCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("StatModifiers", statModifiersCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos1);
         obj.statModifiers = new HashMap<>(statModifiersCount);
         int dictPos = varPos1 + varIntLen;

         for (int i = 0; i < statModifiersCount; i++) {
            int key = buf.getIntLE(dictPos);
            dictPos += 4;
            int valLen = VarInt.peek(buf, dictPos);
            if (valLen < 0) {
               throw ProtocolException.negativeLength("val", valLen);
            }

            if (valLen > 64) {
               throw ProtocolException.arrayTooLong("val", valLen, 64);
            }

            int valVarLen = VarInt.length(buf, dictPos);
            if (dictPos + valVarLen + valLen * 6L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("val", dictPos + valVarLen + valLen * 6, buf.readableBytes());
            }

            dictPos += valVarLen;
            Modifier[] val = new Modifier[valLen];

            for (int valIdx = 0; valIdx < valLen; valIdx++) {
               val[valIdx] = Modifier.deserialize(buf, dictPos);
               dictPos += Modifier.computeBytesConsumed(buf, dictPos);
            }

            if (obj.statModifiers.put(key, val) != null) {
               throw ProtocolException.duplicateKey("statModifiers", key);
            }
         }
      }

      if ((nullBits & 4) != 0) {
         int varPos2 = offset + 30 + buf.getIntLE(offset + 18);
         int damageResistanceCount = VarInt.peek(buf, varPos2);
         if (damageResistanceCount < 0) {
            throw ProtocolException.negativeLength("DamageResistance", damageResistanceCount);
         }

         if (damageResistanceCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("DamageResistance", damageResistanceCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos2);
         obj.damageResistance = new HashMap<>(damageResistanceCount);
         int dictPos = varPos2 + varIntLen;

         for (int i = 0; i < damageResistanceCount; i++) {
            int keyLen = VarInt.peek(buf, dictPos);
            if (keyLen < 0) {
               throw ProtocolException.negativeLength("key", keyLen);
            }

            if (keyLen > 4096000) {
               throw ProtocolException.stringTooLong("key", keyLen, 4096000);
            }

            int keyVarLen = VarInt.length(buf, dictPos);
            String keyx = PacketIO.readVarString(buf, dictPos);
            dictPos += keyVarLen + keyLen;
            int valLenx = VarInt.peek(buf, dictPos);
            if (valLenx < 0) {
               throw ProtocolException.negativeLength("val", valLenx);
            }

            if (valLenx > 64) {
               throw ProtocolException.arrayTooLong("val", valLenx, 64);
            }

            int valVarLenx = VarInt.length(buf, dictPos);
            if (dictPos + valVarLenx + valLenx * 6L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("val", dictPos + valVarLenx + valLenx * 6, buf.readableBytes());
            }

            dictPos += valVarLenx;
            Modifier[] val = new Modifier[valLenx];

            for (int valIdx = 0; valIdx < valLenx; valIdx++) {
               val[valIdx] = Modifier.deserialize(buf, dictPos);
               dictPos += Modifier.computeBytesConsumed(buf, dictPos);
            }

            if (obj.damageResistance.put(keyx, val) != null) {
               throw ProtocolException.duplicateKey("damageResistance", keyx);
            }
         }
      }

      if ((nullBits & 8) != 0) {
         int varPos3 = offset + 30 + buf.getIntLE(offset + 22);
         int damageEnhancementCount = VarInt.peek(buf, varPos3);
         if (damageEnhancementCount < 0) {
            throw ProtocolException.negativeLength("DamageEnhancement", damageEnhancementCount);
         }

         if (damageEnhancementCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("DamageEnhancement", damageEnhancementCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos3);
         obj.damageEnhancement = new HashMap<>(damageEnhancementCount);
         int dictPos = varPos3 + varIntLen;

         for (int i = 0; i < damageEnhancementCount; i++) {
            int keyLenx = VarInt.peek(buf, dictPos);
            if (keyLenx < 0) {
               throw ProtocolException.negativeLength("key", keyLenx);
            }

            if (keyLenx > 4096000) {
               throw ProtocolException.stringTooLong("key", keyLenx, 4096000);
            }

            int keyVarLenx = VarInt.length(buf, dictPos);
            String keyxx = PacketIO.readVarString(buf, dictPos);
            dictPos += keyVarLenx + keyLenx;
            int valLenxx = VarInt.peek(buf, dictPos);
            if (valLenxx < 0) {
               throw ProtocolException.negativeLength("val", valLenxx);
            }

            if (valLenxx > 64) {
               throw ProtocolException.arrayTooLong("val", valLenxx, 64);
            }

            int valVarLenxx = VarInt.length(buf, dictPos);
            if (dictPos + valVarLenxx + valLenxx * 6L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("val", dictPos + valVarLenxx + valLenxx * 6, buf.readableBytes());
            }

            dictPos += valVarLenxx;
            Modifier[] val = new Modifier[valLenxx];

            for (int valIdx = 0; valIdx < valLenxx; valIdx++) {
               val[valIdx] = Modifier.deserialize(buf, dictPos);
               dictPos += Modifier.computeBytesConsumed(buf, dictPos);
            }

            if (obj.damageEnhancement.put(keyxx, val) != null) {
               throw ProtocolException.duplicateKey("damageEnhancement", keyxx);
            }
         }
      }

      if ((nullBits & 16) != 0) {
         int varPos4 = offset + 30 + buf.getIntLE(offset + 26);
         int damageClassEnhancementCount = VarInt.peek(buf, varPos4);
         if (damageClassEnhancementCount < 0) {
            throw ProtocolException.negativeLength("DamageClassEnhancement", damageClassEnhancementCount);
         }

         if (damageClassEnhancementCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("DamageClassEnhancement", damageClassEnhancementCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos4);
         obj.damageClassEnhancement = new HashMap<>(damageClassEnhancementCount);
         int dictPos = varPos4 + varIntLen;

         for (int i = 0; i < damageClassEnhancementCount; i++) {
            int keyLenxx = VarInt.peek(buf, dictPos);
            if (keyLenxx < 0) {
               throw ProtocolException.negativeLength("key", keyLenxx);
            }

            if (keyLenxx > 4096000) {
               throw ProtocolException.stringTooLong("key", keyLenxx, 4096000);
            }

            int keyVarLenxx = VarInt.length(buf, dictPos);
            String keyxxx = PacketIO.readVarString(buf, dictPos);
            dictPos += keyVarLenxx + keyLenxx;
            int valLenxxx = VarInt.peek(buf, dictPos);
            if (valLenxxx < 0) {
               throw ProtocolException.negativeLength("val", valLenxxx);
            }

            if (valLenxxx > 64) {
               throw ProtocolException.arrayTooLong("val", valLenxxx, 64);
            }

            int valVarLenxxx = VarInt.length(buf, dictPos);
            if (dictPos + valVarLenxxx + valLenxxx * 6L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("val", dictPos + valVarLenxxx + valLenxxx * 6, buf.readableBytes());
            }

            dictPos += valVarLenxxx;
            Modifier[] val = new Modifier[valLenxxx];

            for (int valIdx = 0; valIdx < valLenxxx; valIdx++) {
               val[valIdx] = Modifier.deserialize(buf, dictPos);
               dictPos += Modifier.computeBytesConsumed(buf, dictPos);
            }

            if (obj.damageClassEnhancement.put(keyxxx, val) != null) {
               throw ProtocolException.duplicateKey("damageClassEnhancement", keyxxx);
            }
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 30;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 10);
         int pos0 = offset + 30 + fieldOffset0;
         int arrLen = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + arrLen * 1;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 14);
         int pos1 = offset + 30 + fieldOffset1;
         int dictLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1);

         for (int i = 0; i < dictLen; i++) {
            pos1 += 4;
            int al = VarInt.peek(buf, pos1);
            pos1 += VarInt.length(buf, pos1);

            for (int j = 0; j < al; j++) {
               pos1 += Modifier.computeBytesConsumed(buf, pos1);
            }
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 18);
         int pos2 = offset + 30 + fieldOffset2;
         int dictLen = VarInt.peek(buf, pos2);
         pos2 += VarInt.length(buf, pos2);

         for (int i = 0; i < dictLen; i++) {
            int sl = VarInt.peek(buf, pos2);
            pos2 += VarInt.length(buf, pos2) + sl;
            sl = VarInt.peek(buf, pos2);
            pos2 += VarInt.length(buf, pos2);

            for (int j = 0; j < sl; j++) {
               pos2 += Modifier.computeBytesConsumed(buf, pos2);
            }
         }

         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 22);
         int pos3 = offset + 30 + fieldOffset3;
         int dictLen = VarInt.peek(buf, pos3);
         pos3 += VarInt.length(buf, pos3);

         for (int i = 0; i < dictLen; i++) {
            int sl = VarInt.peek(buf, pos3);
            pos3 += VarInt.length(buf, pos3) + sl;
            sl = VarInt.peek(buf, pos3);
            pos3 += VarInt.length(buf, pos3);

            for (int j = 0; j < sl; j++) {
               pos3 += Modifier.computeBytesConsumed(buf, pos3);
            }
         }

         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      if ((nullBits & 16) != 0) {
         int fieldOffset4 = buf.getIntLE(offset + 26);
         int pos4 = offset + 30 + fieldOffset4;
         int dictLen = VarInt.peek(buf, pos4);
         pos4 += VarInt.length(buf, pos4);

         for (int i = 0; i < dictLen; i++) {
            int sl = VarInt.peek(buf, pos4);
            pos4 += VarInt.length(buf, pos4) + sl;
            sl = VarInt.peek(buf, pos4);
            pos4 += VarInt.length(buf, pos4);

            for (int j = 0; j < sl; j++) {
               pos4 += Modifier.computeBytesConsumed(buf, pos4);
            }
         }

         if (pos4 - offset > maxEnd) {
            maxEnd = pos4 - offset;
         }
      }

      return maxEnd;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.cosmeticsToHide != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.statModifiers != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.damageResistance != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.damageEnhancement != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.damageClassEnhancement != null) {
         nullBits = (byte)(nullBits | 16);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.armorSlot.getValue());
      buf.writeDoubleLE(this.baseDamageResistance);
      int cosmeticsToHideOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int statModifiersOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int damageResistanceOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int damageEnhancementOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int damageClassEnhancementOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.cosmeticsToHide != null) {
         buf.setIntLE(cosmeticsToHideOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.cosmeticsToHide.length > 4096000) {
            throw ProtocolException.arrayTooLong("CosmeticsToHide", this.cosmeticsToHide.length, 4096000);
         }

         VarInt.write(buf, this.cosmeticsToHide.length);

         for (Cosmetic item : this.cosmeticsToHide) {
            buf.writeByte(item.getValue());
         }
      } else {
         buf.setIntLE(cosmeticsToHideOffsetSlot, -1);
      }

      if (this.statModifiers != null) {
         buf.setIntLE(statModifiersOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.statModifiers.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("StatModifiers", this.statModifiers.size(), 4096000);
         }

         VarInt.write(buf, this.statModifiers.size());

         for (Entry<Integer, Modifier[]> e : this.statModifiers.entrySet()) {
            buf.writeIntLE(e.getKey());
            VarInt.write(buf, e.getValue().length);

            for (Modifier arrItem : e.getValue()) {
               arrItem.serialize(buf);
            }
         }
      } else {
         buf.setIntLE(statModifiersOffsetSlot, -1);
      }

      if (this.damageResistance != null) {
         buf.setIntLE(damageResistanceOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.damageResistance.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("DamageResistance", this.damageResistance.size(), 4096000);
         }

         VarInt.write(buf, this.damageResistance.size());

         for (Entry<String, Modifier[]> e : this.damageResistance.entrySet()) {
            PacketIO.writeVarString(buf, e.getKey(), 4096000);
            VarInt.write(buf, e.getValue().length);

            for (Modifier arrItem : e.getValue()) {
               arrItem.serialize(buf);
            }
         }
      } else {
         buf.setIntLE(damageResistanceOffsetSlot, -1);
      }

      if (this.damageEnhancement != null) {
         buf.setIntLE(damageEnhancementOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.damageEnhancement.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("DamageEnhancement", this.damageEnhancement.size(), 4096000);
         }

         VarInt.write(buf, this.damageEnhancement.size());

         for (Entry<String, Modifier[]> e : this.damageEnhancement.entrySet()) {
            PacketIO.writeVarString(buf, e.getKey(), 4096000);
            VarInt.write(buf, e.getValue().length);

            for (Modifier arrItem : e.getValue()) {
               arrItem.serialize(buf);
            }
         }
      } else {
         buf.setIntLE(damageEnhancementOffsetSlot, -1);
      }

      if (this.damageClassEnhancement != null) {
         buf.setIntLE(damageClassEnhancementOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.damageClassEnhancement.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("DamageClassEnhancement", this.damageClassEnhancement.size(), 4096000);
         }

         VarInt.write(buf, this.damageClassEnhancement.size());

         for (Entry<String, Modifier[]> e : this.damageClassEnhancement.entrySet()) {
            PacketIO.writeVarString(buf, e.getKey(), 4096000);
            VarInt.write(buf, e.getValue().length);

            for (Modifier arrItem : e.getValue()) {
               arrItem.serialize(buf);
            }
         }
      } else {
         buf.setIntLE(damageClassEnhancementOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 30;
      if (this.cosmeticsToHide != null) {
         size += VarInt.size(this.cosmeticsToHide.length) + this.cosmeticsToHide.length * 1;
      }

      if (this.statModifiers != null) {
         int statModifiersSize = 0;

         for (Entry<Integer, Modifier[]> kvp : this.statModifiers.entrySet()) {
            statModifiersSize += 4 + VarInt.size(kvp.getValue().length) + ((Modifier[])kvp.getValue()).length * 6;
         }

         size += VarInt.size(this.statModifiers.size()) + statModifiersSize;
      }

      if (this.damageResistance != null) {
         int damageResistanceSize = 0;

         for (Entry<String, Modifier[]> kvp : this.damageResistance.entrySet()) {
            damageResistanceSize += PacketIO.stringSize(kvp.getKey()) + VarInt.size(kvp.getValue().length) + ((Modifier[])kvp.getValue()).length * 6;
         }

         size += VarInt.size(this.damageResistance.size()) + damageResistanceSize;
      }

      if (this.damageEnhancement != null) {
         int damageEnhancementSize = 0;

         for (Entry<String, Modifier[]> kvp : this.damageEnhancement.entrySet()) {
            damageEnhancementSize += PacketIO.stringSize(kvp.getKey()) + VarInt.size(kvp.getValue().length) + ((Modifier[])kvp.getValue()).length * 6;
         }

         size += VarInt.size(this.damageEnhancement.size()) + damageEnhancementSize;
      }

      if (this.damageClassEnhancement != null) {
         int damageClassEnhancementSize = 0;

         for (Entry<String, Modifier[]> kvp : this.damageClassEnhancement.entrySet()) {
            damageClassEnhancementSize += PacketIO.stringSize(kvp.getKey()) + VarInt.size(kvp.getValue().length) + ((Modifier[])kvp.getValue()).length * 6;
         }

         size += VarInt.size(this.damageClassEnhancement.size()) + damageClassEnhancementSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 30) {
         return ValidationResult.error("Buffer too small: expected at least 30 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int cosmeticsToHideOffset = buffer.getIntLE(offset + 10);
            if (cosmeticsToHideOffset < 0) {
               return ValidationResult.error("Invalid offset for CosmeticsToHide");
            }

            int pos = offset + 30 + cosmeticsToHideOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for CosmeticsToHide");
            }

            int cosmeticsToHideCount = VarInt.peek(buffer, pos);
            if (cosmeticsToHideCount < 0) {
               return ValidationResult.error("Invalid array count for CosmeticsToHide");
            }

            if (cosmeticsToHideCount > 4096000) {
               return ValidationResult.error("CosmeticsToHide exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += cosmeticsToHideCount * 1;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading CosmeticsToHide");
            }
         }

         if ((nullBits & 2) != 0) {
            int statModifiersOffset = buffer.getIntLE(offset + 14);
            if (statModifiersOffset < 0) {
               return ValidationResult.error("Invalid offset for StatModifiers");
            }

            int posx = offset + 30 + statModifiersOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for StatModifiers");
            }

            int statModifiersCount = VarInt.peek(buffer, posx);
            if (statModifiersCount < 0) {
               return ValidationResult.error("Invalid dictionary count for StatModifiers");
            }

            if (statModifiersCount > 4096000) {
               return ValidationResult.error("StatModifiers exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);

            for (int i = 0; i < statModifiersCount; i++) {
               posx += 4;
               if (posx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               int valueArrCount = VarInt.peek(buffer, posx);
               if (valueArrCount < 0) {
                  return ValidationResult.error("Invalid array count for value");
               }

               posx += VarInt.length(buffer, posx);

               for (int valueArrIdx = 0; valueArrIdx < valueArrCount; valueArrIdx++) {
                  posx += 6;
               }
            }
         }

         if ((nullBits & 4) != 0) {
            int damageResistanceOffset = buffer.getIntLE(offset + 18);
            if (damageResistanceOffset < 0) {
               return ValidationResult.error("Invalid offset for DamageResistance");
            }

            int posxx = offset + 30 + damageResistanceOffset;
            if (posxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for DamageResistance");
            }

            int damageResistanceCount = VarInt.peek(buffer, posxx);
            if (damageResistanceCount < 0) {
               return ValidationResult.error("Invalid dictionary count for DamageResistance");
            }

            if (damageResistanceCount > 4096000) {
               return ValidationResult.error("DamageResistance exceeds max length 4096000");
            }

            posxx += VarInt.length(buffer, posxx);

            for (int i = 0; i < damageResistanceCount; i++) {
               int keyLen = VarInt.peek(buffer, posxx);
               if (keyLen < 0) {
                  return ValidationResult.error("Invalid string length for key");
               }

               if (keyLen > 4096000) {
                  return ValidationResult.error("key exceeds max length 4096000");
               }

               posxx += VarInt.length(buffer, posxx);
               posxx += keyLen;
               if (posxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               int valueArrCount = VarInt.peek(buffer, posxx);
               if (valueArrCount < 0) {
                  return ValidationResult.error("Invalid array count for value");
               }

               posxx += VarInt.length(buffer, posxx);

               for (int valueArrIdx = 0; valueArrIdx < valueArrCount; valueArrIdx++) {
                  posxx += 6;
               }
            }
         }

         if ((nullBits & 8) != 0) {
            int damageEnhancementOffset = buffer.getIntLE(offset + 22);
            if (damageEnhancementOffset < 0) {
               return ValidationResult.error("Invalid offset for DamageEnhancement");
            }

            int posxxx = offset + 30 + damageEnhancementOffset;
            if (posxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for DamageEnhancement");
            }

            int damageEnhancementCount = VarInt.peek(buffer, posxxx);
            if (damageEnhancementCount < 0) {
               return ValidationResult.error("Invalid dictionary count for DamageEnhancement");
            }

            if (damageEnhancementCount > 4096000) {
               return ValidationResult.error("DamageEnhancement exceeds max length 4096000");
            }

            posxxx += VarInt.length(buffer, posxxx);

            for (int i = 0; i < damageEnhancementCount; i++) {
               int keyLenx = VarInt.peek(buffer, posxxx);
               if (keyLenx < 0) {
                  return ValidationResult.error("Invalid string length for key");
               }

               if (keyLenx > 4096000) {
                  return ValidationResult.error("key exceeds max length 4096000");
               }

               posxxx += VarInt.length(buffer, posxxx);
               posxxx += keyLenx;
               if (posxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               int valueArrCount = VarInt.peek(buffer, posxxx);
               if (valueArrCount < 0) {
                  return ValidationResult.error("Invalid array count for value");
               }

               posxxx += VarInt.length(buffer, posxxx);

               for (int valueArrIdx = 0; valueArrIdx < valueArrCount; valueArrIdx++) {
                  posxxx += 6;
               }
            }
         }

         if ((nullBits & 16) != 0) {
            int damageClassEnhancementOffset = buffer.getIntLE(offset + 26);
            if (damageClassEnhancementOffset < 0) {
               return ValidationResult.error("Invalid offset for DamageClassEnhancement");
            }

            int posxxxx = offset + 30 + damageClassEnhancementOffset;
            if (posxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for DamageClassEnhancement");
            }

            int damageClassEnhancementCount = VarInt.peek(buffer, posxxxx);
            if (damageClassEnhancementCount < 0) {
               return ValidationResult.error("Invalid dictionary count for DamageClassEnhancement");
            }

            if (damageClassEnhancementCount > 4096000) {
               return ValidationResult.error("DamageClassEnhancement exceeds max length 4096000");
            }

            posxxxx += VarInt.length(buffer, posxxxx);

            for (int i = 0; i < damageClassEnhancementCount; i++) {
               int keyLenxx = VarInt.peek(buffer, posxxxx);
               if (keyLenxx < 0) {
                  return ValidationResult.error("Invalid string length for key");
               }

               if (keyLenxx > 4096000) {
                  return ValidationResult.error("key exceeds max length 4096000");
               }

               posxxxx += VarInt.length(buffer, posxxxx);
               posxxxx += keyLenxx;
               if (posxxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               int valueArrCount = VarInt.peek(buffer, posxxxx);
               if (valueArrCount < 0) {
                  return ValidationResult.error("Invalid array count for value");
               }

               posxxxx += VarInt.length(buffer, posxxxx);

               for (int valueArrIdx = 0; valueArrIdx < valueArrCount; valueArrIdx++) {
                  posxxxx += 6;
               }
            }
         }

         return ValidationResult.OK;
      }
   }

   public ItemArmor clone() {
      ItemArmor copy = new ItemArmor();
      copy.armorSlot = this.armorSlot;
      copy.cosmeticsToHide = this.cosmeticsToHide != null ? Arrays.copyOf(this.cosmeticsToHide, this.cosmeticsToHide.length) : null;
      if (this.statModifiers != null) {
         Map<Integer, Modifier[]> m = new HashMap<>();

         for (Entry<Integer, Modifier[]> e : this.statModifiers.entrySet()) {
            m.put(e.getKey(), Arrays.stream(e.getValue()).map(x -> x.clone()).toArray(Modifier[]::new));
         }

         copy.statModifiers = m;
      }

      copy.baseDamageResistance = this.baseDamageResistance;
      if (this.damageResistance != null) {
         Map<String, Modifier[]> m = new HashMap<>();

         for (Entry<String, Modifier[]> e : this.damageResistance.entrySet()) {
            m.put(e.getKey(), Arrays.stream(e.getValue()).map(x -> x.clone()).toArray(Modifier[]::new));
         }

         copy.damageResistance = m;
      }

      if (this.damageEnhancement != null) {
         Map<String, Modifier[]> m = new HashMap<>();

         for (Entry<String, Modifier[]> e : this.damageEnhancement.entrySet()) {
            m.put(e.getKey(), Arrays.stream(e.getValue()).map(x -> x.clone()).toArray(Modifier[]::new));
         }

         copy.damageEnhancement = m;
      }

      if (this.damageClassEnhancement != null) {
         Map<String, Modifier[]> m = new HashMap<>();

         for (Entry<String, Modifier[]> e : this.damageClassEnhancement.entrySet()) {
            m.put(e.getKey(), Arrays.stream(e.getValue()).map(x -> x.clone()).toArray(Modifier[]::new));
         }

         copy.damageClassEnhancement = m;
      }

      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ItemArmor other)
            ? false
            : Objects.equals(this.armorSlot, other.armorSlot)
               && Arrays.equals((Object[])this.cosmeticsToHide, (Object[])other.cosmeticsToHide)
               && Objects.equals(this.statModifiers, other.statModifiers)
               && this.baseDamageResistance == other.baseDamageResistance
               && Objects.equals(this.damageResistance, other.damageResistance)
               && Objects.equals(this.damageEnhancement, other.damageEnhancement)
               && Objects.equals(this.damageClassEnhancement, other.damageClassEnhancement);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.armorSlot);
      result = 31 * result + Arrays.hashCode((Object[])this.cosmeticsToHide);
      result = 31 * result + Objects.hashCode(this.statModifiers);
      result = 31 * result + Double.hashCode(this.baseDamageResistance);
      result = 31 * result + Objects.hashCode(this.damageResistance);
      result = 31 * result + Objects.hashCode(this.damageEnhancement);
      return 31 * result + Objects.hashCode(this.damageClassEnhancement);
   }
}
