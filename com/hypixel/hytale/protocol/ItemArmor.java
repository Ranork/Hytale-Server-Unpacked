package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
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
   public Map<String, ResistanceModifier[]> damageResistance;
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
      @Nullable Map<String, ResistanceModifier[]> damageResistance,
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
      if (buf.readableBytes() - offset < 30) {
         throw ProtocolException.bufferTooSmall("ItemArmor", 30, buf.readableBytes() - offset);
      } else {
         ItemArmor obj = new ItemArmor();
         byte nullBits = buf.getByte(offset);
         obj.armorSlot = ItemArmorSlot.fromValue(buf.getByte(offset + 1));
         obj.baseDamageResistance = buf.getDoubleLE(offset + 2);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 10);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 30) {
               throw ProtocolException.invalidOffset("CosmeticsToHide", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 30 + varPosBase0;
            int cosmeticsToHideCount = VarInt.peek(buf, varPos0);
            if (cosmeticsToHideCount < 0) {
               throw ProtocolException.invalidVarInt("CosmeticsToHide");
            }

            int varIntLen = VarInt.size(cosmeticsToHideCount);
            if (cosmeticsToHideCount > 4096000) {
               throw ProtocolException.arrayTooLong("CosmeticsToHide", cosmeticsToHideCount, 4096000);
            }

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
            int varPosBase1 = buf.getIntLE(offset + 14);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 30) {
               throw ProtocolException.invalidOffset("StatModifiers", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 30 + varPosBase1;
            int statModifiersCount = VarInt.peek(buf, varPos1);
            if (statModifiersCount < 0) {
               throw ProtocolException.invalidVarInt("StatModifiers");
            }

            int varIntLenx = VarInt.size(statModifiersCount);
            if (statModifiersCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("StatModifiers", statModifiersCount, 4096000);
            }

            obj.statModifiers = new HashMap<>(statModifiersCount);
            int dictPos = varPos1 + varIntLenx;

            for (int i = 0; i < statModifiersCount; i++) {
               int key = buf.getIntLE(dictPos);
               dictPos += 4;
               int valLen = VarInt.peek(buf, dictPos);
               if (valLen < 0) {
                  throw ProtocolException.invalidVarInt("val");
               }

               int valVarLen = VarInt.size(valLen);
               if (valLen > 64) {
                  throw ProtocolException.arrayTooLong("val", valLen, 64);
               }

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
            int varPosBase2 = buf.getIntLE(offset + 18);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 30) {
               throw ProtocolException.invalidOffset("DamageResistance", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 30 + varPosBase2;
            int damageResistanceCount = VarInt.peek(buf, varPos2);
            if (damageResistanceCount < 0) {
               throw ProtocolException.invalidVarInt("DamageResistance");
            }

            int varIntLenx = VarInt.size(damageResistanceCount);
            if (damageResistanceCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("DamageResistance", damageResistanceCount, 4096000);
            }

            obj.damageResistance = new HashMap<>(damageResistanceCount);
            int dictPos = varPos2 + varIntLenx;

            for (int i = 0; i < damageResistanceCount; i++) {
               int keyLen = VarInt.peek(buf, dictPos);
               if (keyLen < 0) {
                  throw ProtocolException.invalidVarInt("key");
               }

               int keyVarLen = VarInt.size(keyLen);
               if (keyLen > 4096000) {
                  throw ProtocolException.stringTooLong("key", keyLen, 4096000);
               }

               if (dictPos + keyVarLen + keyLen > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("key", dictPos + keyVarLen + keyLen, buf.readableBytes());
               }

               String keyx = PacketIO.readVarString(buf, dictPos);
               dictPos += keyVarLen + keyLen;
               int valLenx = VarInt.peek(buf, dictPos);
               if (valLenx < 0) {
                  throw ProtocolException.invalidVarInt("val");
               }

               int valVarLenx = VarInt.size(valLenx);
               if (valLenx > 64) {
                  throw ProtocolException.arrayTooLong("val", valLenx, 64);
               }

               if (dictPos + valVarLenx + valLenx * 5L > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("val", dictPos + valVarLenx + valLenx * 5, buf.readableBytes());
               }

               dictPos += valVarLenx;
               ResistanceModifier[] val = new ResistanceModifier[valLenx];

               for (int valIdx = 0; valIdx < valLenx; valIdx++) {
                  val[valIdx] = ResistanceModifier.deserialize(buf, dictPos);
                  dictPos += ResistanceModifier.computeBytesConsumed(buf, dictPos);
               }

               if (obj.damageResistance.put(keyx, val) != null) {
                  throw ProtocolException.duplicateKey("damageResistance", keyx);
               }
            }
         }

         if ((nullBits & 8) != 0) {
            int varPosBase3 = buf.getIntLE(offset + 22);
            if (varPosBase3 < 0 || varPosBase3 > buf.writerIndex() - offset - 30) {
               throw ProtocolException.invalidOffset("DamageEnhancement", varPosBase3, buf.readableBytes());
            }

            int varPos3 = offset + 30 + varPosBase3;
            int damageEnhancementCount = VarInt.peek(buf, varPos3);
            if (damageEnhancementCount < 0) {
               throw ProtocolException.invalidVarInt("DamageEnhancement");
            }

            int varIntLenx = VarInt.size(damageEnhancementCount);
            if (damageEnhancementCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("DamageEnhancement", damageEnhancementCount, 4096000);
            }

            obj.damageEnhancement = new HashMap<>(damageEnhancementCount);
            int dictPos = varPos3 + varIntLenx;

            for (int i = 0; i < damageEnhancementCount; i++) {
               int keyLenx = VarInt.peek(buf, dictPos);
               if (keyLenx < 0) {
                  throw ProtocolException.invalidVarInt("key");
               }

               int keyVarLenx = VarInt.size(keyLenx);
               if (keyLenx > 4096000) {
                  throw ProtocolException.stringTooLong("key", keyLenx, 4096000);
               }

               if (dictPos + keyVarLenx + keyLenx > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("key", dictPos + keyVarLenx + keyLenx, buf.readableBytes());
               }

               String keyxx = PacketIO.readVarString(buf, dictPos);
               dictPos += keyVarLenx + keyLenx;
               int valLenxx = VarInt.peek(buf, dictPos);
               if (valLenxx < 0) {
                  throw ProtocolException.invalidVarInt("val");
               }

               int valVarLenxx = VarInt.size(valLenxx);
               if (valLenxx > 64) {
                  throw ProtocolException.arrayTooLong("val", valLenxx, 64);
               }

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
            int varPosBase4 = buf.getIntLE(offset + 26);
            if (varPosBase4 < 0 || varPosBase4 > buf.writerIndex() - offset - 30) {
               throw ProtocolException.invalidOffset("DamageClassEnhancement", varPosBase4, buf.readableBytes());
            }

            int varPos4 = offset + 30 + varPosBase4;
            int damageClassEnhancementCount = VarInt.peek(buf, varPos4);
            if (damageClassEnhancementCount < 0) {
               throw ProtocolException.invalidVarInt("DamageClassEnhancement");
            }

            int varIntLenx = VarInt.size(damageClassEnhancementCount);
            if (damageClassEnhancementCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("DamageClassEnhancement", damageClassEnhancementCount, 4096000);
            }

            obj.damageClassEnhancement = new HashMap<>(damageClassEnhancementCount);
            int dictPos = varPos4 + varIntLenx;

            for (int i = 0; i < damageClassEnhancementCount; i++) {
               int keyLenxx = VarInt.peek(buf, dictPos);
               if (keyLenxx < 0) {
                  throw ProtocolException.invalidVarInt("key");
               }

               int keyVarLenxx = VarInt.size(keyLenxx);
               if (keyLenxx > 4096000) {
                  throw ProtocolException.stringTooLong("key", keyLenxx, 4096000);
               }

               if (dictPos + keyVarLenxx + keyLenxx > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("key", dictPos + keyVarLenxx + keyLenxx, buf.readableBytes());
               }

               String keyxxx = PacketIO.readVarString(buf, dictPos);
               dictPos += keyVarLenxx + keyLenxx;
               int valLenxxx = VarInt.peek(buf, dictPos);
               if (valLenxxx < 0) {
                  throw ProtocolException.invalidVarInt("val");
               }

               int valVarLenxxx = VarInt.size(valLenxxx);
               if (valLenxxx > 64) {
                  throw ProtocolException.arrayTooLong("val", valLenxxx, 64);
               }

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
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 30;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 10);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 30) {
            throw ProtocolException.invalidOffset("CosmeticsToHide", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 30 + fieldOffset0;
         int arrLen = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(arrLen) + arrLen * 1;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 14);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 30) {
            throw ProtocolException.invalidOffset("StatModifiers", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 30 + fieldOffset1;
         int dictLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos1 += 4;
            int al = VarInt.peek(buf, pos1);
            pos1 += VarInt.size(al);

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
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 30) {
            throw ProtocolException.invalidOffset("DamageResistance", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 30 + fieldOffset2;
         int dictLen = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            int sl = VarInt.peek(buf, pos2);
            pos2 += VarInt.size(sl) + sl;
            sl = VarInt.peek(buf, pos2);
            pos2 += VarInt.size(sl);

            for (int j = 0; j < sl; j++) {
               pos2 += ResistanceModifier.computeBytesConsumed(buf, pos2);
            }
         }

         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 22);
         if (fieldOffset3 < 0 || fieldOffset3 > buf.writerIndex() - offset - 30) {
            throw ProtocolException.invalidOffset("DamageEnhancement", fieldOffset3, maxEnd);
         }

         int pos3 = offset + 30 + fieldOffset3;
         int dictLen = VarInt.peek(buf, pos3);
         pos3 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            int sl = VarInt.peek(buf, pos3);
            pos3 += VarInt.size(sl) + sl;
            sl = VarInt.peek(buf, pos3);
            pos3 += VarInt.size(sl);

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
         if (fieldOffset4 < 0 || fieldOffset4 > buf.writerIndex() - offset - 30) {
            throw ProtocolException.invalidOffset("DamageClassEnhancement", fieldOffset4, maxEnd);
         }

         int pos4 = offset + 30 + fieldOffset4;
         int dictLen = VarInt.peek(buf, pos4);
         pos4 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            int sl = VarInt.peek(buf, pos4);
            pos4 += VarInt.size(sl) + sl;
            sl = VarInt.peek(buf, pos4);
            pos4 += VarInt.size(sl);

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

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 30L;
   }

   public static ItemArmorSlot getArmorSlot(MemorySegment mem) {
      return getArmorSlot(mem, 0);
   }

   public static ItemArmorSlot getArmorSlot(MemorySegment mem, int offset) {
      return ItemArmorSlot.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1)));
   }

   @Nullable
   public static Cosmetic[] getCosmeticsToHide(MemorySegment mem) {
      return getCosmeticsToHide(mem, 0);
   }

   @Nullable
   public static Cosmetic[] getCosmeticsToHide(MemorySegment mem, int offset) {
      if (!hasCosmeticsToHide(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 10, 30, "CosmeticsToHide");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("CosmeticsToHide", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("CosmeticsToHide", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("CosmeticsToHide", off + lenOffset + len * 1, (int)mem.byteSize());
            } else {
               off += lenOffset;
               Cosmetic[] data = new Cosmetic[len];

               for (int i = 0; i < len; i++) {
                  data[i] = Cosmetic.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(off + i * 1)));
               }

               return data;
            }
         }
      }
   }

   @Nullable
   public static Map<Integer, Modifier[]> getStatModifiers(MemorySegment mem) {
      return getStatModifiers(mem, 0);
   }

   @Nullable
   public static Map<Integer, Modifier[]> getStatModifiers(MemorySegment mem, int offset) {
      if (!hasStatModifiers(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 14, 30, "StatModifiers");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("StatModifiers", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("StatModifiers", len, 4096000);
         } else {
            Map<Integer, Modifier[]> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               long valuePacked = VarInt.getWithLength(mem, off);
               int valueLen = (int)valuePacked;
               int valueVarLen = (int)(valuePacked >>> 32);
               if (valueLen < 0) {
                  throw ProtocolException.negativeLength("value", valueLen);
               }

               if (valueLen > 64) {
                  throw ProtocolException.arrayTooLong("value", valueLen, 64);
               }

               if (off + valueVarLen + valueLen * 6L > mem.byteSize()) {
                  throw ProtocolException.bufferTooSmall("value", off + valueVarLen + valueLen * 6, (int)mem.byteSize());
               }

               off += valueVarLen;
               Modifier[] value = new Modifier[valueLen];

               for (int valueIdx = 0; valueIdx < valueLen; valueIdx++) {
                  value[valueIdx] = Modifier.toObject(mem, off);
                  off += value[valueIdx].computeSize();
               }

               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("StatModifiers", key);
               }
            }

            return data;
         }
      }
   }

   public static double getBaseDamageResistance(MemorySegment mem) {
      return getBaseDamageResistance(mem, 0);
   }

   public static double getBaseDamageResistance(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 2));
   }

   @Nullable
   public static Map<String, ResistanceModifier[]> getDamageResistance(MemorySegment mem) {
      return getDamageResistance(mem, 0);
   }

   @Nullable
   public static Map<String, ResistanceModifier[]> getDamageResistance(MemorySegment mem, int offset) {
      if (!hasDamageResistance(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 18, 30, "DamageResistance");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("DamageResistance", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("DamageResistance", len, 4096000);
         } else {
            Map<String, ResistanceModifier[]> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               long keyPacked = VarInt.getWithLength(mem, off);
               int nkey = (int)keyPacked + (int)(keyPacked >>> 32);
               String key = PacketIO.readVarString("key", mem, off, 16384000, PacketIO.UTF8);
               off += nkey;
               long valuePacked = VarInt.getWithLength(mem, off);
               int valueLen = (int)valuePacked;
               int valueVarLen = (int)(valuePacked >>> 32);
               if (valueLen < 0) {
                  throw ProtocolException.negativeLength("value", valueLen);
               }

               if (valueLen > 64) {
                  throw ProtocolException.arrayTooLong("value", valueLen, 64);
               }

               if (off + valueVarLen + valueLen * 5L > mem.byteSize()) {
                  throw ProtocolException.bufferTooSmall("value", off + valueVarLen + valueLen * 5, (int)mem.byteSize());
               }

               off += valueVarLen;
               ResistanceModifier[] value = new ResistanceModifier[valueLen];

               for (int valueIdx = 0; valueIdx < valueLen; valueIdx++) {
                  value[valueIdx] = ResistanceModifier.toObject(mem, off);
                  off += value[valueIdx].computeSize();
               }

               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("DamageResistance", key);
               }
            }

            return data;
         }
      }
   }

   @Nullable
   public static Map<String, Modifier[]> getDamageEnhancement(MemorySegment mem) {
      return getDamageEnhancement(mem, 0);
   }

   @Nullable
   public static Map<String, Modifier[]> getDamageEnhancement(MemorySegment mem, int offset) {
      if (!hasDamageEnhancement(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 22, 30, "DamageEnhancement");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("DamageEnhancement", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("DamageEnhancement", len, 4096000);
         } else {
            Map<String, Modifier[]> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               long keyPacked = VarInt.getWithLength(mem, off);
               int nkey = (int)keyPacked + (int)(keyPacked >>> 32);
               String key = PacketIO.readVarString("key", mem, off, 16384000, PacketIO.UTF8);
               off += nkey;
               long valuePacked = VarInt.getWithLength(mem, off);
               int valueLen = (int)valuePacked;
               int valueVarLen = (int)(valuePacked >>> 32);
               if (valueLen < 0) {
                  throw ProtocolException.negativeLength("value", valueLen);
               }

               if (valueLen > 64) {
                  throw ProtocolException.arrayTooLong("value", valueLen, 64);
               }

               if (off + valueVarLen + valueLen * 6L > mem.byteSize()) {
                  throw ProtocolException.bufferTooSmall("value", off + valueVarLen + valueLen * 6, (int)mem.byteSize());
               }

               off += valueVarLen;
               Modifier[] value = new Modifier[valueLen];

               for (int valueIdx = 0; valueIdx < valueLen; valueIdx++) {
                  value[valueIdx] = Modifier.toObject(mem, off);
                  off += value[valueIdx].computeSize();
               }

               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("DamageEnhancement", key);
               }
            }

            return data;
         }
      }
   }

   @Nullable
   public static Map<String, Modifier[]> getDamageClassEnhancement(MemorySegment mem) {
      return getDamageClassEnhancement(mem, 0);
   }

   @Nullable
   public static Map<String, Modifier[]> getDamageClassEnhancement(MemorySegment mem, int offset) {
      if (!hasDamageClassEnhancement(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 26, 30, "DamageClassEnhancement");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("DamageClassEnhancement", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("DamageClassEnhancement", len, 4096000);
         } else {
            Map<String, Modifier[]> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               long keyPacked = VarInt.getWithLength(mem, off);
               int nkey = (int)keyPacked + (int)(keyPacked >>> 32);
               String key = PacketIO.readVarString("key", mem, off, 16384000, PacketIO.UTF8);
               off += nkey;
               long valuePacked = VarInt.getWithLength(mem, off);
               int valueLen = (int)valuePacked;
               int valueVarLen = (int)(valuePacked >>> 32);
               if (valueLen < 0) {
                  throw ProtocolException.negativeLength("value", valueLen);
               }

               if (valueLen > 64) {
                  throw ProtocolException.arrayTooLong("value", valueLen, 64);
               }

               if (off + valueVarLen + valueLen * 6L > mem.byteSize()) {
                  throw ProtocolException.bufferTooSmall("value", off + valueVarLen + valueLen * 6, (int)mem.byteSize());
               }

               off += valueVarLen;
               Modifier[] value = new Modifier[valueLen];

               for (int valueIdx = 0; valueIdx < valueLen; valueIdx++) {
                  value[valueIdx] = Modifier.toObject(mem, off);
                  off += value[valueIdx].computeSize();
               }

               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("DamageClassEnhancement", key);
               }
            }

            return data;
         }
      }
   }

   public static boolean hasCosmeticsToHide(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasStatModifiers(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasDamageResistance(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasDamageEnhancement(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   public static boolean hasDamageClassEnhancement(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 16) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static ItemArmor toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ItemArmor toObject(MemorySegment mem, int offset) {
      if (offset + 30 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ItemArmor", offset + 30, (int)mem.byteSize());
      } else {
         Cosmetic[] cosmeticsToHide = null;
         if (hasCosmeticsToHide(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 10, 30, "CosmeticsToHide");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("CosmeticsToHide", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("CosmeticsToHide", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("CosmeticsToHide", off + lenOffset + len * 1, (int)mem.byteSize());
            }

            off += lenOffset;
            cosmeticsToHide = new Cosmetic[len];

            for (int i = 0; i < len; i++) {
               cosmeticsToHide[i] = Cosmetic.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(off + i * 1)));
            }
         }

         Map<Integer, Modifier[]> statModifiers = null;
         if (hasStatModifiers(mem, offset)) {
            int offx = offset + getValidatedOffset(mem, offset, 14, 30, "StatModifiers");
            long packedx = VarInt.getWithLength(mem, offx);
            int lenx = (int)packedx;
            if (lenx < 0) {
               throw ProtocolException.negativeLength("StatModifiers", lenx);
            }

            if (lenx > 4096000) {
               throw ProtocolException.dictionaryTooLarge("StatModifiers", lenx, 4096000);
            }

            statModifiers = new HashMap<>(lenx);
            offx += (int)(packedx >>> 32);

            for (int i = 0; i < lenx; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)offx);
               offx += 4;
               long valuePacked = VarInt.getWithLength(mem, offx);
               int valueLen = (int)valuePacked;
               int valueVarLen = (int)(valuePacked >>> 32);
               if (valueLen < 0) {
                  throw ProtocolException.negativeLength("value", valueLen);
               }

               if (valueLen > 64) {
                  throw ProtocolException.arrayTooLong("value", valueLen, 64);
               }

               if (offx + valueVarLen + valueLen * 6L > mem.byteSize()) {
                  throw ProtocolException.bufferTooSmall("value", offx + valueVarLen + valueLen * 6, (int)mem.byteSize());
               }

               offx += valueVarLen;
               Modifier[] value = new Modifier[valueLen];

               for (int valueIdx = 0; valueIdx < valueLen; valueIdx++) {
                  value[valueIdx] = Modifier.toObject(mem, offx);
                  offx += value[valueIdx].computeSize();
               }

               if (statModifiers.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("StatModifiers", key);
               }
            }
         }

         Map<String, ResistanceModifier[]> damageResistance = null;
         if (hasDamageResistance(mem, offset)) {
            int offxx = offset + getValidatedOffset(mem, offset, 18, 30, "DamageResistance");
            long packedxx = VarInt.getWithLength(mem, offxx);
            int lenxx = (int)packedxx;
            if (lenxx < 0) {
               throw ProtocolException.negativeLength("DamageResistance", lenxx);
            }

            if (lenxx > 4096000) {
               throw ProtocolException.dictionaryTooLarge("DamageResistance", lenxx, 4096000);
            }

            damageResistance = new HashMap<>(lenxx);
            offxx += (int)(packedxx >>> 32);

            for (int i = 0; i < lenxx; i++) {
               long keyPacked = VarInt.getWithLength(mem, offxx);
               int nkey = (int)keyPacked + (int)(keyPacked >>> 32);
               String keyx = PacketIO.readVarString("key", mem, offxx, 16384000, PacketIO.UTF8);
               offxx += nkey;
               long valuePackedx = VarInt.getWithLength(mem, offxx);
               int valueLenx = (int)valuePackedx;
               int valueVarLenx = (int)(valuePackedx >>> 32);
               if (valueLenx < 0) {
                  throw ProtocolException.negativeLength("value", valueLenx);
               }

               if (valueLenx > 64) {
                  throw ProtocolException.arrayTooLong("value", valueLenx, 64);
               }

               if (offxx + valueVarLenx + valueLenx * 5L > mem.byteSize()) {
                  throw ProtocolException.bufferTooSmall("value", offxx + valueVarLenx + valueLenx * 5, (int)mem.byteSize());
               }

               offxx += valueVarLenx;
               ResistanceModifier[] value = new ResistanceModifier[valueLenx];

               for (int valueIdx = 0; valueIdx < valueLenx; valueIdx++) {
                  value[valueIdx] = ResistanceModifier.toObject(mem, offxx);
                  offxx += value[valueIdx].computeSize();
               }

               if (damageResistance.put(keyx, value) != null) {
                  throw ProtocolException.duplicateKey("DamageResistance", keyx);
               }
            }
         }

         Map<String, Modifier[]> damageEnhancement = null;
         if (hasDamageEnhancement(mem, offset)) {
            int offxxx = offset + getValidatedOffset(mem, offset, 22, 30, "DamageEnhancement");
            long packedxxx = VarInt.getWithLength(mem, offxxx);
            int lenxxx = (int)packedxxx;
            if (lenxxx < 0) {
               throw ProtocolException.negativeLength("DamageEnhancement", lenxxx);
            }

            if (lenxxx > 4096000) {
               throw ProtocolException.dictionaryTooLarge("DamageEnhancement", lenxxx, 4096000);
            }

            damageEnhancement = new HashMap<>(lenxxx);
            offxxx += (int)(packedxxx >>> 32);

            for (int i = 0; i < lenxxx; i++) {
               long keyPackedx = VarInt.getWithLength(mem, offxxx);
               int nkeyx = (int)keyPackedx + (int)(keyPackedx >>> 32);
               String keyxx = PacketIO.readVarString("key", mem, offxxx, 16384000, PacketIO.UTF8);
               offxxx += nkeyx;
               long valuePackedxx = VarInt.getWithLength(mem, offxxx);
               int valueLenxx = (int)valuePackedxx;
               int valueVarLenxx = (int)(valuePackedxx >>> 32);
               if (valueLenxx < 0) {
                  throw ProtocolException.negativeLength("value", valueLenxx);
               }

               if (valueLenxx > 64) {
                  throw ProtocolException.arrayTooLong("value", valueLenxx, 64);
               }

               if (offxxx + valueVarLenxx + valueLenxx * 6L > mem.byteSize()) {
                  throw ProtocolException.bufferTooSmall("value", offxxx + valueVarLenxx + valueLenxx * 6, (int)mem.byteSize());
               }

               offxxx += valueVarLenxx;
               Modifier[] value = new Modifier[valueLenxx];

               for (int valueIdx = 0; valueIdx < valueLenxx; valueIdx++) {
                  value[valueIdx] = Modifier.toObject(mem, offxxx);
                  offxxx += value[valueIdx].computeSize();
               }

               if (damageEnhancement.put(keyxx, value) != null) {
                  throw ProtocolException.duplicateKey("DamageEnhancement", keyxx);
               }
            }
         }

         Map<String, Modifier[]> damageClassEnhancement = null;
         if (hasDamageClassEnhancement(mem, offset)) {
            int offxxxx = offset + getValidatedOffset(mem, offset, 26, 30, "DamageClassEnhancement");
            long packedxxxx = VarInt.getWithLength(mem, offxxxx);
            int lenxxxx = (int)packedxxxx;
            if (lenxxxx < 0) {
               throw ProtocolException.negativeLength("DamageClassEnhancement", lenxxxx);
            }

            if (lenxxxx > 4096000) {
               throw ProtocolException.dictionaryTooLarge("DamageClassEnhancement", lenxxxx, 4096000);
            }

            damageClassEnhancement = new HashMap<>(lenxxxx);
            offxxxx += (int)(packedxxxx >>> 32);

            for (int i = 0; i < lenxxxx; i++) {
               long keyPackedxx = VarInt.getWithLength(mem, offxxxx);
               int nkeyxx = (int)keyPackedxx + (int)(keyPackedxx >>> 32);
               String keyxxx = PacketIO.readVarString("key", mem, offxxxx, 16384000, PacketIO.UTF8);
               offxxxx += nkeyxx;
               long valuePackedxxx = VarInt.getWithLength(mem, offxxxx);
               int valueLenxxx = (int)valuePackedxxx;
               int valueVarLenxxx = (int)(valuePackedxxx >>> 32);
               if (valueLenxxx < 0) {
                  throw ProtocolException.negativeLength("value", valueLenxxx);
               }

               if (valueLenxxx > 64) {
                  throw ProtocolException.arrayTooLong("value", valueLenxxx, 64);
               }

               if (offxxxx + valueVarLenxxx + valueLenxxx * 6L > mem.byteSize()) {
                  throw ProtocolException.bufferTooSmall("value", offxxxx + valueVarLenxxx + valueLenxxx * 6, (int)mem.byteSize());
               }

               offxxxx += valueVarLenxxx;
               Modifier[] value = new Modifier[valueLenxxx];

               for (int valueIdx = 0; valueIdx < valueLenxxx; valueIdx++) {
                  value[valueIdx] = Modifier.toObject(mem, offxxxx);
                  offxxxx += value[valueIdx].computeSize();
               }

               if (damageClassEnhancement.put(keyxxx, value) != null) {
                  throw ProtocolException.duplicateKey("DamageClassEnhancement", keyxxx);
               }
            }
         }

         return new ItemArmor(
            ItemArmorSlot.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))),
            cosmeticsToHide,
            statModifiers,
            mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 2)),
            damageResistance,
            damageEnhancement,
            damageClassEnhancement
         );
      }
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

         for (Entry<String, ResistanceModifier[]> e : this.damageResistance.entrySet()) {
            PacketIO.writeVarString(buf, e.getKey(), 4096000);
            VarInt.write(buf, e.getValue().length);

            for (ResistanceModifier arrItem : e.getValue()) {
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

   public int serialize(@Nonnull MemorySegment mem, int offset) {
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

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.armorSlot.getValue());
      mem.set(PacketIO.PROTO_DOUBLE, (long)(offset + 2), this.baseDamageResistance);
      int varOffset = offset + 30;
      if (this.cosmeticsToHide != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 10), varOffset - offset - 30);
         if (this.cosmeticsToHide.length > 4096000) {
            throw ProtocolException.arrayTooLong("CosmeticsToHide", this.cosmeticsToHide.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.cosmeticsToHide.length);

         for (int i = 0; i < this.cosmeticsToHide.length; i++) {
            mem.set(PacketIO.PROTO_BYTE, (long)(varOffset + i * 1), (byte)this.cosmeticsToHide[i].getValue());
         }

         varOffset += this.cosmeticsToHide.length * 1;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 10), -1);
      }

      if (this.statModifiers != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 14), varOffset - offset - 30);
         if (this.statModifiers.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("StatModifiers", this.statModifiers.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.statModifiers.size());

         for (Entry<Integer, Modifier[]> e : this.statModifiers.entrySet()) {
            mem.set(PacketIO.PROTO_INT, (long)varOffset, e.getKey());
            varOffset += 4;
            varOffset += VarInt.set(mem, varOffset, e.getValue().length);

            for (Modifier arrItem : e.getValue()) {
               varOffset += arrItem.serialize(mem, varOffset);
            }
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 14), -1);
      }

      if (this.damageResistance != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 18), varOffset - offset - 30);
         if (this.damageResistance.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("DamageResistance", this.damageResistance.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.damageResistance.size());

         for (Entry<String, ResistanceModifier[]> e : this.damageResistance.entrySet()) {
            varOffset += PacketIO.writeVarString(mem, varOffset, e.getKey(), 16384000);
            varOffset += VarInt.set(mem, varOffset, e.getValue().length);

            for (ResistanceModifier arrItem : e.getValue()) {
               varOffset += arrItem.serialize(mem, varOffset);
            }
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 18), -1);
      }

      if (this.damageEnhancement != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 22), varOffset - offset - 30);
         if (this.damageEnhancement.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("DamageEnhancement", this.damageEnhancement.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.damageEnhancement.size());

         for (Entry<String, Modifier[]> e : this.damageEnhancement.entrySet()) {
            varOffset += PacketIO.writeVarString(mem, varOffset, e.getKey(), 16384000);
            varOffset += VarInt.set(mem, varOffset, e.getValue().length);

            for (Modifier arrItem : e.getValue()) {
               varOffset += arrItem.serialize(mem, varOffset);
            }
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 22), -1);
      }

      if (this.damageClassEnhancement != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 26), varOffset - offset - 30);
         if (this.damageClassEnhancement.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("DamageClassEnhancement", this.damageClassEnhancement.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.damageClassEnhancement.size());

         for (Entry<String, Modifier[]> e : this.damageClassEnhancement.entrySet()) {
            varOffset += PacketIO.writeVarString(mem, varOffset, e.getKey(), 16384000);
            varOffset += VarInt.set(mem, varOffset, e.getValue().length);

            for (Modifier arrItem : e.getValue()) {
               varOffset += arrItem.serialize(mem, varOffset);
            }
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 26), -1);
      }

      return varOffset - offset;
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

         for (Entry<String, ResistanceModifier[]> kvp : this.damageResistance.entrySet()) {
            damageResistanceSize += PacketIO.stringSize(kvp.getKey()) + VarInt.size(kvp.getValue().length) + ((ResistanceModifier[])kvp.getValue()).length * 5;
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
         int v = buffer.getByte(offset + 1) & 255;
         if (v >= 4) {
            return ValidationResult.error("Invalid ItemArmorSlot value for ArmorSlot");
         } else {
            if ((nullBits & 1) != 0) {
               v = buffer.getIntLE(offset + 10);
               if (v < 0 || v > buffer.writerIndex() - offset - 30) {
                  return ValidationResult.error("Invalid offset for CosmeticsToHide");
               }

               int pos = offset + 30 + v;
               int cosmeticsToHideCount = VarInt.peek(buffer, pos);
               if (cosmeticsToHideCount < 0) {
                  return ValidationResult.error("Invalid array count for CosmeticsToHide");
               }

               if (cosmeticsToHideCount > 4096000) {
                  return ValidationResult.error("CosmeticsToHide exceeds max length 4096000");
               }

               pos += VarInt.size(cosmeticsToHideCount);
               if (pos + cosmeticsToHideCount * 1L > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading CosmeticsToHide");
               }

               for (int i = 0; i < cosmeticsToHideCount; i++) {
                  int vx = buffer.getByte(pos) & 255;
                  if (vx >= 13) {
                     return ValidationResult.error("Invalid Cosmetic value for CosmeticsToHide[i]");
                  }

                  pos++;
               }
            }

            if ((nullBits & 2) != 0) {
               v = buffer.getIntLE(offset + 14);
               if (v < 0 || v > buffer.writerIndex() - offset - 30) {
                  return ValidationResult.error("Invalid offset for StatModifiers");
               }

               int posx = offset + 30 + v;
               int statModifiersCount = VarInt.peek(buffer, posx);
               if (statModifiersCount < 0) {
                  return ValidationResult.error("Invalid dictionary count for StatModifiers");
               }

               if (statModifiersCount > 4096000) {
                  return ValidationResult.error("StatModifiers exceeds max length 4096000");
               }

               posx += VarInt.size(statModifiersCount);

               for (int i = 0; i < statModifiersCount; i++) {
                  posx += 4;
                  if (posx > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading key");
                  }

                  int valueArrCount = VarInt.peek(buffer, posx);
                  if (valueArrCount < 0) {
                     return ValidationResult.error("Invalid array count for value");
                  }

                  posx += VarInt.size(valueArrCount);

                  for (int valueArrIdx = 0; valueArrIdx < valueArrCount; valueArrIdx++) {
                     posx += 6;
                  }
               }
            }

            if ((nullBits & 4) != 0) {
               v = buffer.getIntLE(offset + 18);
               if (v < 0 || v > buffer.writerIndex() - offset - 30) {
                  return ValidationResult.error("Invalid offset for DamageResistance");
               }

               int posxx = offset + 30 + v;
               int damageResistanceCount = VarInt.peek(buffer, posxx);
               if (damageResistanceCount < 0) {
                  return ValidationResult.error("Invalid dictionary count for DamageResistance");
               }

               if (damageResistanceCount > 4096000) {
                  return ValidationResult.error("DamageResistance exceeds max length 4096000");
               }

               posxx += VarInt.size(damageResistanceCount);

               for (int i = 0; i < damageResistanceCount; i++) {
                  int keyLen = VarInt.peek(buffer, posxx);
                  if (keyLen < 0) {
                     return ValidationResult.error("Invalid string length for key");
                  }

                  if (keyLen > 4096000) {
                     return ValidationResult.error("key exceeds max length 4096000");
                  }

                  posxx += VarInt.size(keyLen);
                  posxx += keyLen;
                  if (posxx > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading key");
                  }

                  int valueArrCount = VarInt.peek(buffer, posxx);
                  if (valueArrCount < 0) {
                     return ValidationResult.error("Invalid array count for value");
                  }

                  posxx += VarInt.size(valueArrCount);

                  for (int valueArrIdx = 0; valueArrIdx < valueArrCount; valueArrIdx++) {
                     posxx += 5;
                  }
               }
            }

            if ((nullBits & 8) != 0) {
               v = buffer.getIntLE(offset + 22);
               if (v < 0 || v > buffer.writerIndex() - offset - 30) {
                  return ValidationResult.error("Invalid offset for DamageEnhancement");
               }

               int posxxx = offset + 30 + v;
               int damageEnhancementCount = VarInt.peek(buffer, posxxx);
               if (damageEnhancementCount < 0) {
                  return ValidationResult.error("Invalid dictionary count for DamageEnhancement");
               }

               if (damageEnhancementCount > 4096000) {
                  return ValidationResult.error("DamageEnhancement exceeds max length 4096000");
               }

               posxxx += VarInt.size(damageEnhancementCount);

               for (int i = 0; i < damageEnhancementCount; i++) {
                  int keyLenx = VarInt.peek(buffer, posxxx);
                  if (keyLenx < 0) {
                     return ValidationResult.error("Invalid string length for key");
                  }

                  if (keyLenx > 4096000) {
                     return ValidationResult.error("key exceeds max length 4096000");
                  }

                  posxxx += VarInt.size(keyLenx);
                  posxxx += keyLenx;
                  if (posxxx > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading key");
                  }

                  int valueArrCount = VarInt.peek(buffer, posxxx);
                  if (valueArrCount < 0) {
                     return ValidationResult.error("Invalid array count for value");
                  }

                  posxxx += VarInt.size(valueArrCount);

                  for (int valueArrIdx = 0; valueArrIdx < valueArrCount; valueArrIdx++) {
                     posxxx += 6;
                  }
               }
            }

            if ((nullBits & 16) != 0) {
               v = buffer.getIntLE(offset + 26);
               if (v < 0 || v > buffer.writerIndex() - offset - 30) {
                  return ValidationResult.error("Invalid offset for DamageClassEnhancement");
               }

               int posxxxx = offset + 30 + v;
               int damageClassEnhancementCount = VarInt.peek(buffer, posxxxx);
               if (damageClassEnhancementCount < 0) {
                  return ValidationResult.error("Invalid dictionary count for DamageClassEnhancement");
               }

               if (damageClassEnhancementCount > 4096000) {
                  return ValidationResult.error("DamageClassEnhancement exceeds max length 4096000");
               }

               posxxxx += VarInt.size(damageClassEnhancementCount);

               for (int i = 0; i < damageClassEnhancementCount; i++) {
                  int keyLenxx = VarInt.peek(buffer, posxxxx);
                  if (keyLenxx < 0) {
                     return ValidationResult.error("Invalid string length for key");
                  }

                  if (keyLenxx > 4096000) {
                     return ValidationResult.error("key exceeds max length 4096000");
                  }

                  posxxxx += VarInt.size(keyLenxx);
                  posxxxx += keyLenxx;
                  if (posxxxx > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading key");
                  }

                  int valueArrCount = VarInt.peek(buffer, posxxxx);
                  if (valueArrCount < 0) {
                     return ValidationResult.error("Invalid array count for value");
                  }

                  posxxxx += VarInt.size(valueArrCount);

                  for (int valueArrIdx = 0; valueArrIdx < valueArrCount; valueArrIdx++) {
                     posxxxx += 6;
                  }
               }
            }

            return ValidationResult.OK;
         }
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
         Map<String, ResistanceModifier[]> m = new HashMap<>();

         for (Entry<String, ResistanceModifier[]> e : this.damageResistance.entrySet()) {
            m.put(e.getKey(), Arrays.stream(e.getValue()).map(x -> x.clone()).toArray(ResistanceModifier[]::new));
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
