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
      if (buf.readableBytes() - offset < 60) {
         throw ProtocolException.bufferTooSmall("DamageEntityInteraction", 60, buf.readableBytes() - offset);
      } else {
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
            int varPosBase0 = buf.getIntLE(offset + 24);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 60) {
               throw ProtocolException.invalidOffset("Effects", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 60 + varPosBase0;
            obj.effects = InteractionEffects.deserialize(buf, varPos0);
         }

         if ((nullBits[0] & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 28);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 60) {
               throw ProtocolException.invalidOffset("Settings", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 60 + varPosBase1;
            int settingsCount = VarInt.peek(buf, varPos1);
            if (settingsCount < 0) {
               throw ProtocolException.invalidVarInt("Settings");
            }

            int varIntLen = VarInt.size(settingsCount);
            if (settingsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Settings", settingsCount, 4096000);
            }

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
            int varPosBase2 = buf.getIntLE(offset + 32);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 60) {
               throw ProtocolException.invalidOffset("Rules", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 60 + varPosBase2;
            obj.rules = InteractionRules.deserialize(buf, varPos2);
         }

         if ((nullBits[0] & 8) != 0) {
            int varPosBase3 = buf.getIntLE(offset + 36);
            if (varPosBase3 < 0 || varPosBase3 > buf.writerIndex() - offset - 60) {
               throw ProtocolException.invalidOffset("Tags", varPosBase3, buf.readableBytes());
            }

            int varPos3 = offset + 60 + varPosBase3;
            int tagsCount = VarInt.peek(buf, varPos3);
            if (tagsCount < 0) {
               throw ProtocolException.invalidVarInt("Tags");
            }

            int varIntLen = VarInt.size(tagsCount);
            if (tagsCount > 4096000) {
               throw ProtocolException.arrayTooLong("Tags", tagsCount, 4096000);
            }

            if (varPos3 + varIntLen + tagsCount * 4L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Tags", varPos3 + varIntLen + tagsCount * 4, buf.readableBytes());
            }

            obj.tags = new int[tagsCount];

            for (int ix = 0; ix < tagsCount; ix++) {
               obj.tags[ix] = buf.getIntLE(varPos3 + varIntLen + ix * 4);
            }
         }

         if ((nullBits[0] & 16) != 0) {
            int varPosBase4 = buf.getIntLE(offset + 40);
            if (varPosBase4 < 0 || varPosBase4 > buf.writerIndex() - offset - 60) {
               throw ProtocolException.invalidOffset("Camera", varPosBase4, buf.readableBytes());
            }

            int varPos4 = offset + 60 + varPosBase4;
            obj.camera = InteractionCameraSettings.deserialize(buf, varPos4);
         }

         if ((nullBits[0] & 32) != 0) {
            int varPosBase5 = buf.getIntLE(offset + 44);
            if (varPosBase5 < 0 || varPosBase5 > buf.writerIndex() - offset - 60) {
               throw ProtocolException.invalidOffset("DamageEffects", varPosBase5, buf.readableBytes());
            }

            int varPos5 = offset + 60 + varPosBase5;
            obj.damageEffects = DamageEffects.deserialize(buf, varPos5);
         }

         if ((nullBits[0] & 64) != 0) {
            int varPosBase6 = buf.getIntLE(offset + 48);
            if (varPosBase6 < 0 || varPosBase6 > buf.writerIndex() - offset - 60) {
               throw ProtocolException.invalidOffset("AngledDamage", varPosBase6, buf.readableBytes());
            }

            int varPos6 = offset + 60 + varPosBase6;
            int angledDamageCount = VarInt.peek(buf, varPos6);
            if (angledDamageCount < 0) {
               throw ProtocolException.invalidVarInt("AngledDamage");
            }

            int varIntLenx = VarInt.size(angledDamageCount);
            if (angledDamageCount > 4096000) {
               throw ProtocolException.arrayTooLong("AngledDamage", angledDamageCount, 4096000);
            }

            if (varPos6 + varIntLenx + angledDamageCount * 21L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("AngledDamage", varPos6 + varIntLenx + angledDamageCount * 21, buf.readableBytes());
            }

            obj.angledDamage = new AngledDamage[angledDamageCount];
            int elemPos = varPos6 + varIntLenx;

            for (int ix = 0; ix < angledDamageCount; ix++) {
               obj.angledDamage[ix] = AngledDamage.deserialize(buf, elemPos);
               elemPos += AngledDamage.computeBytesConsumed(buf, elemPos);
            }
         }

         if ((nullBits[0] & 128) != 0) {
            int varPosBase7 = buf.getIntLE(offset + 52);
            if (varPosBase7 < 0 || varPosBase7 > buf.writerIndex() - offset - 60) {
               throw ProtocolException.invalidOffset("TargetedDamage", varPosBase7, buf.readableBytes());
            }

            int varPos7 = offset + 60 + varPosBase7;
            int targetedDamageCount = VarInt.peek(buf, varPos7);
            if (targetedDamageCount < 0) {
               throw ProtocolException.invalidVarInt("TargetedDamage");
            }

            int varIntLenxx = VarInt.size(targetedDamageCount);
            if (targetedDamageCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("TargetedDamage", targetedDamageCount, 4096000);
            }

            obj.targetedDamage = new HashMap<>(targetedDamageCount);
            int dictPos = varPos7 + varIntLenxx;

            for (int ix = 0; ix < targetedDamageCount; ix++) {
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
            int varPosBase8 = buf.getIntLE(offset + 56);
            if (varPosBase8 < 0 || varPosBase8 > buf.writerIndex() - offset - 60) {
               throw ProtocolException.invalidOffset("EntityStatsOnHit", varPosBase8, buf.readableBytes());
            }

            int varPos8 = offset + 60 + varPosBase8;
            int entityStatsOnHitCount = VarInt.peek(buf, varPos8);
            if (entityStatsOnHitCount < 0) {
               throw ProtocolException.invalidVarInt("EntityStatsOnHit");
            }

            int varIntLenxx = VarInt.size(entityStatsOnHitCount);
            if (entityStatsOnHitCount > 4096000) {
               throw ProtocolException.arrayTooLong("EntityStatsOnHit", entityStatsOnHitCount, 4096000);
            }

            if (varPos8 + varIntLenxx + entityStatsOnHitCount * 13L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("EntityStatsOnHit", varPos8 + varIntLenxx + entityStatsOnHitCount * 13, buf.readableBytes());
            }

            obj.entityStatsOnHit = new EntityStatOnHit[entityStatsOnHitCount];
            int elemPos = varPos8 + varIntLenxx;

            for (int ix = 0; ix < entityStatsOnHitCount; ix++) {
               obj.entityStatsOnHit[ix] = EntityStatOnHit.deserialize(buf, elemPos);
               elemPos += EntityStatOnHit.computeBytesConsumed(buf, elemPos);
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte[] nullBits = PacketIO.readBytes(buf, offset, 2);
      int maxEnd = 60;
      if ((nullBits[0] & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 24);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 60) {
            throw ProtocolException.invalidOffset("Effects", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 60 + fieldOffset0;
         pos0 += InteractionEffects.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits[0] & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 28);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 60) {
            throw ProtocolException.invalidOffset("Settings", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 60 + fieldOffset1;
         int dictLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos1 = ++pos1 + InteractionSettings.computeBytesConsumed(buf, pos1);
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits[0] & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 32);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 60) {
            throw ProtocolException.invalidOffset("Rules", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 60 + fieldOffset2;
         pos2 += InteractionRules.computeBytesConsumed(buf, pos2);
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits[0] & 8) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 36);
         if (fieldOffset3 < 0 || fieldOffset3 > buf.writerIndex() - offset - 60) {
            throw ProtocolException.invalidOffset("Tags", fieldOffset3, maxEnd);
         }

         int pos3 = offset + 60 + fieldOffset3;
         int arrLen = VarInt.peek(buf, pos3);
         pos3 += VarInt.size(arrLen) + arrLen * 4;
         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      if ((nullBits[0] & 16) != 0) {
         int fieldOffset4 = buf.getIntLE(offset + 40);
         if (fieldOffset4 < 0 || fieldOffset4 > buf.writerIndex() - offset - 60) {
            throw ProtocolException.invalidOffset("Camera", fieldOffset4, maxEnd);
         }

         int pos4 = offset + 60 + fieldOffset4;
         pos4 += InteractionCameraSettings.computeBytesConsumed(buf, pos4);
         if (pos4 - offset > maxEnd) {
            maxEnd = pos4 - offset;
         }
      }

      if ((nullBits[0] & 32) != 0) {
         int fieldOffset5 = buf.getIntLE(offset + 44);
         if (fieldOffset5 < 0 || fieldOffset5 > buf.writerIndex() - offset - 60) {
            throw ProtocolException.invalidOffset("DamageEffects", fieldOffset5, maxEnd);
         }

         int pos5 = offset + 60 + fieldOffset5;
         pos5 += DamageEffects.computeBytesConsumed(buf, pos5);
         if (pos5 - offset > maxEnd) {
            maxEnd = pos5 - offset;
         }
      }

      if ((nullBits[0] & 64) != 0) {
         int fieldOffset6 = buf.getIntLE(offset + 48);
         if (fieldOffset6 < 0 || fieldOffset6 > buf.writerIndex() - offset - 60) {
            throw ProtocolException.invalidOffset("AngledDamage", fieldOffset6, maxEnd);
         }

         int pos6 = offset + 60 + fieldOffset6;
         int arrLen = VarInt.peek(buf, pos6);
         pos6 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos6 += AngledDamage.computeBytesConsumed(buf, pos6);
         }

         if (pos6 - offset > maxEnd) {
            maxEnd = pos6 - offset;
         }
      }

      if ((nullBits[0] & 128) != 0) {
         int fieldOffset7 = buf.getIntLE(offset + 52);
         if (fieldOffset7 < 0 || fieldOffset7 > buf.writerIndex() - offset - 60) {
            throw ProtocolException.invalidOffset("TargetedDamage", fieldOffset7, maxEnd);
         }

         int pos7 = offset + 60 + fieldOffset7;
         int dictLen = VarInt.peek(buf, pos7);
         pos7 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            int sl = VarInt.peek(buf, pos7);
            pos7 += VarInt.size(sl) + sl;
            pos7 += TargetedDamage.computeBytesConsumed(buf, pos7);
         }

         if (pos7 - offset > maxEnd) {
            maxEnd = pos7 - offset;
         }
      }

      if ((nullBits[1] & 1) != 0) {
         int fieldOffset8 = buf.getIntLE(offset + 56);
         if (fieldOffset8 < 0 || fieldOffset8 > buf.writerIndex() - offset - 60) {
            throw ProtocolException.invalidOffset("EntityStatsOnHit", fieldOffset8, maxEnd);
         }

         int pos8 = offset + 60 + fieldOffset8;
         int arrLen = VarInt.peek(buf, pos8);
         pos8 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos8 += EntityStatOnHit.computeBytesConsumed(buf, pos8);
         }

         if (pos8 - offset > maxEnd) {
            maxEnd = pos8 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 60L;
   }

   public static WaitForDataFrom getWaitForDataFrom(MemorySegment mem) {
      return getWaitForDataFrom(mem, 0);
   }

   public static WaitForDataFrom getWaitForDataFrom(MemorySegment mem, int offset) {
      return WaitForDataFrom.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2)));
   }

   @Nullable
   public static InteractionEffects getEffects(MemorySegment mem) {
      return getEffects(mem, 0);
   }

   @Nullable
   public static InteractionEffects getEffects(MemorySegment mem, int offset) {
      return hasEffects(mem, offset) ? InteractionEffects.toObject(mem, offset + getValidatedOffset(mem, offset, 24, 60, "Effects")) : null;
   }

   public static float getHorizontalSpeedMultiplier(MemorySegment mem) {
      return getHorizontalSpeedMultiplier(mem, 0);
   }

   public static float getHorizontalSpeedMultiplier(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 3));
   }

   public static float getRunTime(MemorySegment mem) {
      return getRunTime(mem, 0);
   }

   public static float getRunTime(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 7));
   }

   public static boolean getCancelOnItemChange(MemorySegment mem) {
      return getCancelOnItemChange(mem, 0);
   }

   public static boolean getCancelOnItemChange(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 11));
   }

   @Nullable
   public static Map<GameMode, InteractionSettings> getSettings(MemorySegment mem) {
      return getSettings(mem, 0);
   }

   @Nullable
   public static Map<GameMode, InteractionSettings> getSettings(MemorySegment mem, int offset) {
      if (!hasSettings(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 28, 60, "Settings");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Settings", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Settings", len, 4096000);
         } else {
            Map<GameMode, InteractionSettings> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               GameMode key = GameMode.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)off));
               InteractionSettings value = InteractionSettings.toObject(mem, ++off);
               off += value.computeSize();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Settings", key);
               }
            }

            return data;
         }
      }
   }

   @Nullable
   public static InteractionRules getRules(MemorySegment mem) {
      return getRules(mem, 0);
   }

   @Nullable
   public static InteractionRules getRules(MemorySegment mem, int offset) {
      return hasRules(mem, offset) ? InteractionRules.toObject(mem, offset + getValidatedOffset(mem, offset, 32, 60, "Rules")) : null;
   }

   @Nullable
   public static int[] getTags(MemorySegment mem) {
      return getTags(mem, 0);
   }

   @Nullable
   public static int[] getTags(MemorySegment mem, int offset) {
      if (!hasTags(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 36, 60, "Tags");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Tags", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Tags", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 4L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Tags", off + lenOffset + len * 4, (int)mem.byteSize());
            } else {
               off += lenOffset;
               int[] data = new int[len];
               MemorySegment.copy(mem, PacketIO.PROTO_INT, off, data, 0, len);
               return data;
            }
         }
      }
   }

   @Nullable
   public static InteractionCameraSettings getCamera(MemorySegment mem) {
      return getCamera(mem, 0);
   }

   @Nullable
   public static InteractionCameraSettings getCamera(MemorySegment mem, int offset) {
      return hasCamera(mem, offset) ? InteractionCameraSettings.toObject(mem, offset + getValidatedOffset(mem, offset, 40, 60, "Camera")) : null;
   }

   public static int getNext(MemorySegment mem) {
      return getNext(mem, 0);
   }

   public static int getNext(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 12));
   }

   public static int getFailed(MemorySegment mem) {
      return getFailed(mem, 0);
   }

   public static int getFailed(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 16));
   }

   public static int getBlocked(MemorySegment mem) {
      return getBlocked(mem, 0);
   }

   public static int getBlocked(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 20));
   }

   @Nullable
   public static DamageEffects getDamageEffects(MemorySegment mem) {
      return getDamageEffects(mem, 0);
   }

   @Nullable
   public static DamageEffects getDamageEffects(MemorySegment mem, int offset) {
      return hasDamageEffects(mem, offset) ? DamageEffects.toObject(mem, offset + getValidatedOffset(mem, offset, 44, 60, "DamageEffects")) : null;
   }

   @Nullable
   public static AngledDamage[] getAngledDamage(MemorySegment mem) {
      return getAngledDamage(mem, 0);
   }

   @Nullable
   public static AngledDamage[] getAngledDamage(MemorySegment mem, int offset) {
      if (!hasAngledDamage(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 48, 60, "AngledDamage");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("AngledDamage", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("AngledDamage", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("AngledDamage", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               AngledDamage[] data = new AngledDamage[len];

               for (int i = 0; i < len; i++) {
                  data[i] = AngledDamage.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   @Nullable
   public static Map<String, TargetedDamage> getTargetedDamage(MemorySegment mem) {
      return getTargetedDamage(mem, 0);
   }

   @Nullable
   public static Map<String, TargetedDamage> getTargetedDamage(MemorySegment mem, int offset) {
      if (!hasTargetedDamage(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 52, 60, "TargetedDamage");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("TargetedDamage", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("TargetedDamage", len, 4096000);
         } else {
            Map<String, TargetedDamage> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               long keyPacked = VarInt.getWithLength(mem, off);
               int nkey = (int)keyPacked + (int)(keyPacked >>> 32);
               String key = PacketIO.readVarString("key", mem, off, 16384000, PacketIO.UTF8);
               off += nkey;
               TargetedDamage value = TargetedDamage.toObject(mem, off);
               off += value.computeSize();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("TargetedDamage", key);
               }
            }

            return data;
         }
      }
   }

   @Nullable
   public static EntityStatOnHit[] getEntityStatsOnHit(MemorySegment mem) {
      return getEntityStatsOnHit(mem, 0);
   }

   @Nullable
   public static EntityStatOnHit[] getEntityStatsOnHit(MemorySegment mem, int offset) {
      if (!hasEntityStatsOnHit(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 56, 60, "EntityStatsOnHit");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("EntityStatsOnHit", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("EntityStatsOnHit", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("EntityStatsOnHit", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               EntityStatOnHit[] data = new EntityStatOnHit[len];

               for (int i = 0; i < len; i++) {
                  data[i] = EntityStatOnHit.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   public static boolean hasEffects(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasSettings(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasRules(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasTags(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   public static boolean hasCamera(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 16) != 0;
   }

   public static boolean hasDamageEffects(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 32) != 0;
   }

   public static boolean hasAngledDamage(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 64) != 0;
   }

   public static boolean hasTargetedDamage(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 128) != 0;
   }

   public static boolean hasEntityStatsOnHit(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 1) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static DamageEntityInteraction toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static DamageEntityInteraction toObject(MemorySegment mem, int offset) {
      if (offset + 60 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("DamageEntityInteraction", offset + 60, (int)mem.byteSize());
      } else {
         Map<GameMode, InteractionSettings> settings = null;
         if (hasSettings(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 28, 60, "Settings");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Settings", len);
            }

            if (len > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Settings", len, 4096000);
            }

            settings = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               GameMode key = GameMode.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)off));
               InteractionSettings value = InteractionSettings.toObject(mem, ++off);
               off += value.computeSize();
               if (settings.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Settings", key);
               }
            }
         }

         int[] tags = null;
         if (hasTags(mem, offset)) {
            int offx = offset + getValidatedOffset(mem, offset, 36, 60, "Tags");
            long packedx = VarInt.getWithLength(mem, offx);
            int lenx = (int)packedx;
            if (lenx < 0) {
               throw ProtocolException.negativeLength("Tags", lenx);
            }

            if (lenx > 4096000) {
               throw ProtocolException.arrayTooLong("Tags", lenx, 4096000);
            }

            int lenOffset = (int)(packedx >>> 32);
            if (offx + lenOffset + lenx * 4L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Tags", offx + lenOffset + lenx * 4, (int)mem.byteSize());
            }

            offx += lenOffset;
            tags = new int[lenx];
            MemorySegment.copy(mem, PacketIO.PROTO_INT, offx, tags, 0, lenx);
         }

         AngledDamage[] angledDamage = null;
         if (hasAngledDamage(mem, offset)) {
            int offxx = offset + getValidatedOffset(mem, offset, 48, 60, "AngledDamage");
            long packedxx = VarInt.getWithLength(mem, offxx);
            int lenxx = (int)packedxx;
            if (lenxx < 0) {
               throw ProtocolException.negativeLength("AngledDamage", lenxx);
            }

            if (lenxx > 4096000) {
               throw ProtocolException.arrayTooLong("AngledDamage", lenxx, 4096000);
            }

            int lenOffset = (int)(packedxx >>> 32);
            if (offxx + lenOffset + lenxx > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("AngledDamage", offxx + lenOffset + lenxx, (int)mem.byteSize());
            }

            offxx += lenOffset;
            angledDamage = new AngledDamage[lenxx];

            for (int ix = 0; ix < lenxx; ix++) {
               angledDamage[ix] = AngledDamage.toObject(mem, offxx);
               offxx += angledDamage[ix].computeSize();
            }
         }

         Map<String, TargetedDamage> targetedDamage = null;
         if (hasTargetedDamage(mem, offset)) {
            int offxxx = offset + getValidatedOffset(mem, offset, 52, 60, "TargetedDamage");
            long packedxxx = VarInt.getWithLength(mem, offxxx);
            int lenxxx = (int)packedxxx;
            if (lenxxx < 0) {
               throw ProtocolException.negativeLength("TargetedDamage", lenxxx);
            }

            if (lenxxx > 4096000) {
               throw ProtocolException.dictionaryTooLarge("TargetedDamage", lenxxx, 4096000);
            }

            targetedDamage = new HashMap<>(lenxxx);
            offxxx += (int)(packedxxx >>> 32);

            for (int ix = 0; ix < lenxxx; ix++) {
               long keyPacked = VarInt.getWithLength(mem, offxxx);
               int nkey = (int)keyPacked + (int)(keyPacked >>> 32);
               String key = PacketIO.readVarString("key", mem, offxxx, 16384000, PacketIO.UTF8);
               offxxx += nkey;
               TargetedDamage value = TargetedDamage.toObject(mem, offxxx);
               offxxx += value.computeSize();
               if (targetedDamage.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("TargetedDamage", key);
               }
            }
         }

         EntityStatOnHit[] entityStatsOnHit = null;
         if (hasEntityStatsOnHit(mem, offset)) {
            int offxxxx = offset + getValidatedOffset(mem, offset, 56, 60, "EntityStatsOnHit");
            long packedxxxx = VarInt.getWithLength(mem, offxxxx);
            int lenxxxx = (int)packedxxxx;
            if (lenxxxx < 0) {
               throw ProtocolException.negativeLength("EntityStatsOnHit", lenxxxx);
            }

            if (lenxxxx > 4096000) {
               throw ProtocolException.arrayTooLong("EntityStatsOnHit", lenxxxx, 4096000);
            }

            int lenOffset = (int)(packedxxxx >>> 32);
            if (offxxxx + lenOffset + lenxxxx > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("EntityStatsOnHit", offxxxx + lenOffset + lenxxxx, (int)mem.byteSize());
            }

            offxxxx += lenOffset;
            entityStatsOnHit = new EntityStatOnHit[lenxxxx];

            for (int ixx = 0; ixx < lenxxxx; ixx++) {
               entityStatsOnHit[ixx] = EntityStatOnHit.toObject(mem, offxxxx);
               offxxxx += entityStatsOnHit[ixx].computeSize();
            }
         }

         return new DamageEntityInteraction(
            WaitForDataFrom.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2))),
            hasEffects(mem, offset) ? InteractionEffects.toObject(mem, offset + getValidatedOffset(mem, offset, 24, 60, "Effects")) : null,
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 3)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 7)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 11)),
            settings,
            hasRules(mem, offset) ? InteractionRules.toObject(mem, offset + getValidatedOffset(mem, offset, 32, 60, "Rules")) : null,
            tags,
            hasCamera(mem, offset) ? InteractionCameraSettings.toObject(mem, offset + getValidatedOffset(mem, offset, 40, 60, "Camera")) : null,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 12)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 16)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 20)),
            hasDamageEffects(mem, offset) ? DamageEffects.toObject(mem, offset + getValidatedOffset(mem, offset, 44, 60, "DamageEffects")) : null,
            angledDamage,
            targetedDamage,
            entityStatsOnHit
         );
      }
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
   public int serialize(@Nonnull MemorySegment mem, int offset) {
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

      if (this.damageEffects != null) {
         nullBits = (byte)(nullBits | 32);
      }

      if (this.angledDamage != null) {
         nullBits = (byte)(nullBits | 64);
      }

      if (this.targetedDamage != null) {
         nullBits = (byte)(nullBits | 128);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      nullBits = 0;
      if (this.entityStatsOnHit != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 2), (byte)this.waitForDataFrom.getValue());
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 3), this.horizontalSpeedMultiplier);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 7), this.runTime);
      mem.set(PacketIO.PROTO_BOOL, offset + 11, this.cancelOnItemChange);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 12), this.next);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 16), this.failed);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 20), this.blocked);
      int varOffset = offset + 60;
      if (this.effects != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 24), varOffset - offset - 60);
         varOffset += this.effects.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 24), -1);
      }

      if (this.settings != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 28), varOffset - offset - 60);
         if (this.settings.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Settings", this.settings.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.settings.size());

         for (Entry<GameMode, InteractionSettings> e : this.settings.entrySet()) {
            mem.set(PacketIO.PROTO_BYTE, (long)varOffset, (byte)e.getKey().getValue());
            varOffset = ++varOffset + e.getValue().serialize(mem, varOffset);
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 28), -1);
      }

      if (this.rules != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 32), varOffset - offset - 60);
         varOffset += this.rules.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 32), -1);
      }

      if (this.tags != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 36), varOffset - offset - 60);
         if (this.tags.length > 4096000) {
            throw ProtocolException.arrayTooLong("Tags", this.tags.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.tags.length);
         MemorySegment.copy(this.tags, 0, mem, PacketIO.PROTO_INT, varOffset, this.tags.length);
         varOffset += this.tags.length * 4;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 36), -1);
      }

      if (this.camera != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 40), varOffset - offset - 60);
         varOffset += this.camera.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 40), -1);
      }

      if (this.damageEffects != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 44), varOffset - offset - 60);
         varOffset += this.damageEffects.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 44), -1);
      }

      if (this.angledDamage != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 48), varOffset - offset - 60);
         if (this.angledDamage.length > 4096000) {
            throw ProtocolException.arrayTooLong("AngledDamage", this.angledDamage.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.angledDamage.length);
         int angledDamageValueOffset = 0;

         for (int i = 0; i < this.angledDamage.length; i++) {
            angledDamageValueOffset += this.angledDamage[i].serialize(mem, varOffset + angledDamageValueOffset);
         }

         varOffset += angledDamageValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 48), -1);
      }

      if (this.targetedDamage != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 52), varOffset - offset - 60);
         if (this.targetedDamage.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("TargetedDamage", this.targetedDamage.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.targetedDamage.size());

         for (Entry<String, TargetedDamage> e : this.targetedDamage.entrySet()) {
            varOffset += PacketIO.writeVarString(mem, varOffset, e.getKey(), 16384000);
            varOffset += e.getValue().serialize(mem, varOffset);
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 52), -1);
      }

      if (this.entityStatsOnHit != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 56), varOffset - offset - 60);
         if (this.entityStatsOnHit.length > 4096000) {
            throw ProtocolException.arrayTooLong("EntityStatsOnHit", this.entityStatsOnHit.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.entityStatsOnHit.length);
         int entityStatsOnHitValueOffset = 0;

         for (int i = 0; i < this.entityStatsOnHit.length; i++) {
            entityStatsOnHitValueOffset += this.entityStatsOnHit[i].serialize(mem, varOffset + entityStatsOnHitValueOffset);
         }

         varOffset += entityStatsOnHitValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 56), -1);
      }

      return varOffset - offset;
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
         int v = buffer.getByte(offset + 2) & 255;
         if (v >= 3) {
            return ValidationResult.error("Invalid WaitForDataFrom value for WaitForDataFrom");
         } else {
            if ((nullBits[0] & 1) != 0) {
               v = buffer.getIntLE(offset + 24);
               if (v < 0 || v > buffer.writerIndex() - offset - 60) {
                  return ValidationResult.error("Invalid offset for Effects");
               }

               int pos = offset + 60 + v;
               ValidationResult effectsResult = InteractionEffects.validateStructure(buffer, pos);
               if (!effectsResult.isValid()) {
                  return ValidationResult.error("Invalid Effects: " + effectsResult.error());
               }

               pos += InteractionEffects.computeBytesConsumed(buffer, pos);
            }

            if ((nullBits[0] & 2) != 0) {
               v = buffer.getIntLE(offset + 28);
               if (v < 0 || v > buffer.writerIndex() - offset - 60) {
                  return ValidationResult.error("Invalid offset for Settings");
               }

               int pos = offset + 60 + v;
               int settingsCount = VarInt.peek(buffer, pos);
               if (settingsCount < 0) {
                  return ValidationResult.error("Invalid dictionary count for Settings");
               }

               if (settingsCount > 4096000) {
                  return ValidationResult.error("Settings exceeds max length 4096000");
               }

               pos += VarInt.size(settingsCount);

               for (int i = 0; i < settingsCount; i++) {
                  int vx = buffer.getByte(pos) & 255;
                  if (vx >= 2) {
                     return ValidationResult.error("Invalid GameMode value for key");
                  }

                  pos++;
                  pos++;
               }
            }

            if ((nullBits[0] & 4) != 0) {
               v = buffer.getIntLE(offset + 32);
               if (v < 0 || v > buffer.writerIndex() - offset - 60) {
                  return ValidationResult.error("Invalid offset for Rules");
               }

               int posx = offset + 60 + v;
               ValidationResult rulesResult = InteractionRules.validateStructure(buffer, posx);
               if (!rulesResult.isValid()) {
                  return ValidationResult.error("Invalid Rules: " + rulesResult.error());
               }

               posx += InteractionRules.computeBytesConsumed(buffer, posx);
            }

            if ((nullBits[0] & 8) != 0) {
               v = buffer.getIntLE(offset + 36);
               if (v < 0 || v > buffer.writerIndex() - offset - 60) {
                  return ValidationResult.error("Invalid offset for Tags");
               }

               int posx = offset + 60 + v;
               int tagsCount = VarInt.peek(buffer, posx);
               if (tagsCount < 0) {
                  return ValidationResult.error("Invalid array count for Tags");
               }

               if (tagsCount > 4096000) {
                  return ValidationResult.error("Tags exceeds max length 4096000");
               }

               posx += VarInt.size(tagsCount);
               posx += tagsCount * 4;
               if (posx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading Tags");
               }
            }

            if ((nullBits[0] & 16) != 0) {
               v = buffer.getIntLE(offset + 40);
               if (v < 0 || v > buffer.writerIndex() - offset - 60) {
                  return ValidationResult.error("Invalid offset for Camera");
               }

               int posxx = offset + 60 + v;
               ValidationResult cameraResult = InteractionCameraSettings.validateStructure(buffer, posxx);
               if (!cameraResult.isValid()) {
                  return ValidationResult.error("Invalid Camera: " + cameraResult.error());
               }

               posxx += InteractionCameraSettings.computeBytesConsumed(buffer, posxx);
            }

            if ((nullBits[0] & 32) != 0) {
               v = buffer.getIntLE(offset + 44);
               if (v < 0 || v > buffer.writerIndex() - offset - 60) {
                  return ValidationResult.error("Invalid offset for DamageEffects");
               }

               int posxx = offset + 60 + v;
               ValidationResult damageEffectsResult = DamageEffects.validateStructure(buffer, posxx);
               if (!damageEffectsResult.isValid()) {
                  return ValidationResult.error("Invalid DamageEffects: " + damageEffectsResult.error());
               }

               posxx += DamageEffects.computeBytesConsumed(buffer, posxx);
            }

            if ((nullBits[0] & 64) != 0) {
               v = buffer.getIntLE(offset + 48);
               if (v < 0 || v > buffer.writerIndex() - offset - 60) {
                  return ValidationResult.error("Invalid offset for AngledDamage");
               }

               int posxx = offset + 60 + v;
               int angledDamageCount = VarInt.peek(buffer, posxx);
               if (angledDamageCount < 0) {
                  return ValidationResult.error("Invalid array count for AngledDamage");
               }

               if (angledDamageCount > 4096000) {
                  return ValidationResult.error("AngledDamage exceeds max length 4096000");
               }

               posxx += VarInt.size(angledDamageCount);

               for (int i = 0; i < angledDamageCount; i++) {
                  ValidationResult structResult = AngledDamage.validateStructure(buffer, posxx);
                  if (!structResult.isValid()) {
                     return ValidationResult.error("Invalid AngledDamage in AngledDamage[" + i + "]: " + structResult.error());
                  }

                  posxx += AngledDamage.computeBytesConsumed(buffer, posxx);
               }
            }

            if ((nullBits[0] & 128) != 0) {
               v = buffer.getIntLE(offset + 52);
               if (v < 0 || v > buffer.writerIndex() - offset - 60) {
                  return ValidationResult.error("Invalid offset for TargetedDamage");
               }

               int posxxx = offset + 60 + v;
               int targetedDamageCount = VarInt.peek(buffer, posxxx);
               if (targetedDamageCount < 0) {
                  return ValidationResult.error("Invalid dictionary count for TargetedDamage");
               }

               if (targetedDamageCount > 4096000) {
                  return ValidationResult.error("TargetedDamage exceeds max length 4096000");
               }

               posxxx += VarInt.size(targetedDamageCount);

               for (int i = 0; i < targetedDamageCount; i++) {
                  int keyLen = VarInt.peek(buffer, posxxx);
                  if (keyLen < 0) {
                     return ValidationResult.error("Invalid string length for key");
                  }

                  if (keyLen > 4096000) {
                     return ValidationResult.error("key exceeds max length 4096000");
                  }

                  posxxx += VarInt.size(keyLen);
                  posxxx += keyLen;
                  if (posxxx > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading key");
                  }

                  posxxx += TargetedDamage.computeBytesConsumed(buffer, posxxx);
               }
            }

            if ((nullBits[1] & 1) != 0) {
               v = buffer.getIntLE(offset + 56);
               if (v < 0 || v > buffer.writerIndex() - offset - 60) {
                  return ValidationResult.error("Invalid offset for EntityStatsOnHit");
               }

               int posxxxx = offset + 60 + v;
               int entityStatsOnHitCount = VarInt.peek(buffer, posxxxx);
               if (entityStatsOnHitCount < 0) {
                  return ValidationResult.error("Invalid array count for EntityStatsOnHit");
               }

               if (entityStatsOnHitCount > 4096000) {
                  return ValidationResult.error("EntityStatsOnHit exceeds max length 4096000");
               }

               posxxxx += VarInt.size(entityStatsOnHitCount);

               for (int i = 0; i < entityStatsOnHitCount; i++) {
                  ValidationResult structResult = EntityStatOnHit.validateStructure(buffer, posxxxx);
                  if (!structResult.isValid()) {
                     return ValidationResult.error("Invalid EntityStatOnHit in EntityStatsOnHit[" + i + "]: " + structResult.error());
                  }

                  posxxxx += EntityStatOnHit.computeBytesConsumed(buffer, posxxxx);
               }
            }

            return ValidationResult.OK;
         }
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
