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

public class WieldingInteraction extends ChargingInteraction {
   public static final int NULLABLE_BIT_FIELD_SIZE = 2;
   public static final int FIXED_BLOCK_SIZE = 58;
   public static final int VARIABLE_FIELD_COUNT = 8;
   public static final int VARIABLE_BLOCK_START = 90;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public DamageEffects blockedEffects;
   public boolean hasModifiers;
   @Nullable
   public AngledWielding angledWielding;

   public WieldingInteraction() {
   }

   public WieldingInteraction(
      @Nonnull WaitForDataFrom waitForDataFrom,
      @Nullable InteractionEffects effects,
      float horizontalSpeedMultiplier,
      float runTime,
      boolean cancelOnItemChange,
      @Nullable Map<GameMode, InteractionSettings> settings,
      @Nullable InteractionRules rules,
      @Nullable int[] tags,
      @Nullable InteractionCameraSettings camera,
      int failed,
      boolean allowIndefiniteHold,
      boolean displayProgress,
      boolean cancelOnOtherClick,
      boolean failOnDamage,
      float mouseSensitivityAdjustmentTarget,
      float mouseSensitivityAdjustmentDuration,
      @Nullable Map<Float, Integer> chargedNext,
      @Nullable Map<InteractionType, Integer> forks,
      @Nullable ChargingDelay chargingDelay,
      @Nullable DamageEffects blockedEffects,
      boolean hasModifiers,
      @Nullable AngledWielding angledWielding
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
      this.failed = failed;
      this.allowIndefiniteHold = allowIndefiniteHold;
      this.displayProgress = displayProgress;
      this.cancelOnOtherClick = cancelOnOtherClick;
      this.failOnDamage = failOnDamage;
      this.mouseSensitivityAdjustmentTarget = mouseSensitivityAdjustmentTarget;
      this.mouseSensitivityAdjustmentDuration = mouseSensitivityAdjustmentDuration;
      this.chargedNext = chargedNext;
      this.forks = forks;
      this.chargingDelay = chargingDelay;
      this.blockedEffects = blockedEffects;
      this.hasModifiers = hasModifiers;
      this.angledWielding = angledWielding;
   }

   public WieldingInteraction(@Nonnull WieldingInteraction other) {
      this.waitForDataFrom = other.waitForDataFrom;
      this.effects = other.effects;
      this.horizontalSpeedMultiplier = other.horizontalSpeedMultiplier;
      this.runTime = other.runTime;
      this.cancelOnItemChange = other.cancelOnItemChange;
      this.settings = other.settings;
      this.rules = other.rules;
      this.tags = other.tags;
      this.camera = other.camera;
      this.failed = other.failed;
      this.allowIndefiniteHold = other.allowIndefiniteHold;
      this.displayProgress = other.displayProgress;
      this.cancelOnOtherClick = other.cancelOnOtherClick;
      this.failOnDamage = other.failOnDamage;
      this.mouseSensitivityAdjustmentTarget = other.mouseSensitivityAdjustmentTarget;
      this.mouseSensitivityAdjustmentDuration = other.mouseSensitivityAdjustmentDuration;
      this.chargedNext = other.chargedNext;
      this.forks = other.forks;
      this.chargingDelay = other.chargingDelay;
      this.blockedEffects = other.blockedEffects;
      this.hasModifiers = other.hasModifiers;
      this.angledWielding = other.angledWielding;
   }

   @Nonnull
   public static WieldingInteraction deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 90) {
         throw ProtocolException.bufferTooSmall("WieldingInteraction", 90, buf.readableBytes() - offset);
      } else {
         WieldingInteraction obj = new WieldingInteraction();
         byte[] nullBits = PacketIO.readBytes(buf, offset, 2);
         obj.waitForDataFrom = WaitForDataFrom.fromValue(buf.getByte(offset + 2));
         obj.horizontalSpeedMultiplier = buf.getFloatLE(offset + 3);
         obj.runTime = buf.getFloatLE(offset + 7);
         obj.cancelOnItemChange = buf.getByte(offset + 11) != 0;
         obj.failed = buf.getIntLE(offset + 12);
         obj.allowIndefiniteHold = buf.getByte(offset + 16) != 0;
         obj.displayProgress = buf.getByte(offset + 17) != 0;
         obj.cancelOnOtherClick = buf.getByte(offset + 18) != 0;
         obj.failOnDamage = buf.getByte(offset + 19) != 0;
         obj.mouseSensitivityAdjustmentTarget = buf.getFloatLE(offset + 20);
         obj.mouseSensitivityAdjustmentDuration = buf.getFloatLE(offset + 24);
         if ((nullBits[0] & 1) != 0) {
            obj.chargingDelay = ChargingDelay.deserialize(buf, offset + 28);
         }

         obj.hasModifiers = buf.getByte(offset + 48) != 0;
         if ((nullBits[0] & 2) != 0) {
            obj.angledWielding = AngledWielding.deserialize(buf, offset + 49);
         }

         if ((nullBits[0] & 4) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 58);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 90) {
               throw ProtocolException.invalidOffset("Effects", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 90 + varPosBase0;
            obj.effects = InteractionEffects.deserialize(buf, varPos0);
         }

         if ((nullBits[0] & 8) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 62);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 90) {
               throw ProtocolException.invalidOffset("Settings", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 90 + varPosBase1;
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

         if ((nullBits[0] & 16) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 66);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 90) {
               throw ProtocolException.invalidOffset("Rules", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 90 + varPosBase2;
            obj.rules = InteractionRules.deserialize(buf, varPos2);
         }

         if ((nullBits[0] & 32) != 0) {
            int varPosBase3 = buf.getIntLE(offset + 70);
            if (varPosBase3 < 0 || varPosBase3 > buf.writerIndex() - offset - 90) {
               throw ProtocolException.invalidOffset("Tags", varPosBase3, buf.readableBytes());
            }

            int varPos3 = offset + 90 + varPosBase3;
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

         if ((nullBits[0] & 64) != 0) {
            int varPosBase4 = buf.getIntLE(offset + 74);
            if (varPosBase4 < 0 || varPosBase4 > buf.writerIndex() - offset - 90) {
               throw ProtocolException.invalidOffset("Camera", varPosBase4, buf.readableBytes());
            }

            int varPos4 = offset + 90 + varPosBase4;
            obj.camera = InteractionCameraSettings.deserialize(buf, varPos4);
         }

         if ((nullBits[0] & 128) != 0) {
            int varPosBase5 = buf.getIntLE(offset + 78);
            if (varPosBase5 < 0 || varPosBase5 > buf.writerIndex() - offset - 90) {
               throw ProtocolException.invalidOffset("ChargedNext", varPosBase5, buf.readableBytes());
            }

            int varPos5 = offset + 90 + varPosBase5;
            int chargedNextCount = VarInt.peek(buf, varPos5);
            if (chargedNextCount < 0) {
               throw ProtocolException.invalidVarInt("ChargedNext");
            }

            int varIntLenx = VarInt.size(chargedNextCount);
            if (chargedNextCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("ChargedNext", chargedNextCount, 4096000);
            }

            obj.chargedNext = new HashMap<>(chargedNextCount);
            int dictPos = varPos5 + varIntLenx;

            for (int ix = 0; ix < chargedNextCount; ix++) {
               float key = buf.getFloatLE(dictPos);
               dictPos += 4;
               int val = buf.getIntLE(dictPos);
               dictPos += 4;
               if (obj.chargedNext.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("chargedNext", key);
               }
            }
         }

         if ((nullBits[1] & 1) != 0) {
            int varPosBase6 = buf.getIntLE(offset + 82);
            if (varPosBase6 < 0 || varPosBase6 > buf.writerIndex() - offset - 90) {
               throw ProtocolException.invalidOffset("Forks", varPosBase6, buf.readableBytes());
            }

            int varPos6 = offset + 90 + varPosBase6;
            int forksCount = VarInt.peek(buf, varPos6);
            if (forksCount < 0) {
               throw ProtocolException.invalidVarInt("Forks");
            }

            int varIntLenx = VarInt.size(forksCount);
            if (forksCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Forks", forksCount, 4096000);
            }

            obj.forks = new HashMap<>(forksCount);
            int dictPos = varPos6 + varIntLenx;

            for (int ixx = 0; ixx < forksCount; ixx++) {
               InteractionType key = InteractionType.fromValue(buf.getByte(dictPos));
               int val = buf.getIntLE(++dictPos);
               dictPos += 4;
               if (obj.forks.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("forks", key);
               }
            }
         }

         if ((nullBits[1] & 2) != 0) {
            int varPosBase7 = buf.getIntLE(offset + 86);
            if (varPosBase7 < 0 || varPosBase7 > buf.writerIndex() - offset - 90) {
               throw ProtocolException.invalidOffset("BlockedEffects", varPosBase7, buf.readableBytes());
            }

            int varPos7 = offset + 90 + varPosBase7;
            obj.blockedEffects = DamageEffects.deserialize(buf, varPos7);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte[] nullBits = PacketIO.readBytes(buf, offset, 2);
      int maxEnd = 90;
      if ((nullBits[0] & 4) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 58);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 90) {
            throw ProtocolException.invalidOffset("Effects", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 90 + fieldOffset0;
         pos0 += InteractionEffects.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits[0] & 8) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 62);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 90) {
            throw ProtocolException.invalidOffset("Settings", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 90 + fieldOffset1;
         int dictLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos1 = ++pos1 + InteractionSettings.computeBytesConsumed(buf, pos1);
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits[0] & 16) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 66);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 90) {
            throw ProtocolException.invalidOffset("Rules", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 90 + fieldOffset2;
         pos2 += InteractionRules.computeBytesConsumed(buf, pos2);
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits[0] & 32) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 70);
         if (fieldOffset3 < 0 || fieldOffset3 > buf.writerIndex() - offset - 90) {
            throw ProtocolException.invalidOffset("Tags", fieldOffset3, maxEnd);
         }

         int pos3 = offset + 90 + fieldOffset3;
         int arrLen = VarInt.peek(buf, pos3);
         pos3 += VarInt.size(arrLen) + arrLen * 4;
         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      if ((nullBits[0] & 64) != 0) {
         int fieldOffset4 = buf.getIntLE(offset + 74);
         if (fieldOffset4 < 0 || fieldOffset4 > buf.writerIndex() - offset - 90) {
            throw ProtocolException.invalidOffset("Camera", fieldOffset4, maxEnd);
         }

         int pos4 = offset + 90 + fieldOffset4;
         pos4 += InteractionCameraSettings.computeBytesConsumed(buf, pos4);
         if (pos4 - offset > maxEnd) {
            maxEnd = pos4 - offset;
         }
      }

      if ((nullBits[0] & 128) != 0) {
         int fieldOffset5 = buf.getIntLE(offset + 78);
         if (fieldOffset5 < 0 || fieldOffset5 > buf.writerIndex() - offset - 90) {
            throw ProtocolException.invalidOffset("ChargedNext", fieldOffset5, maxEnd);
         }

         int pos5 = offset + 90 + fieldOffset5;
         int dictLen = VarInt.peek(buf, pos5);
         pos5 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos5 += 4;
            pos5 += 4;
         }

         if (pos5 - offset > maxEnd) {
            maxEnd = pos5 - offset;
         }
      }

      if ((nullBits[1] & 1) != 0) {
         int fieldOffset6 = buf.getIntLE(offset + 82);
         if (fieldOffset6 < 0 || fieldOffset6 > buf.writerIndex() - offset - 90) {
            throw ProtocolException.invalidOffset("Forks", fieldOffset6, maxEnd);
         }

         int pos6 = offset + 90 + fieldOffset6;
         int dictLen = VarInt.peek(buf, pos6);
         pos6 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos6 = ++pos6 + 4;
         }

         if (pos6 - offset > maxEnd) {
            maxEnd = pos6 - offset;
         }
      }

      if ((nullBits[1] & 2) != 0) {
         int fieldOffset7 = buf.getIntLE(offset + 86);
         if (fieldOffset7 < 0 || fieldOffset7 > buf.writerIndex() - offset - 90) {
            throw ProtocolException.invalidOffset("BlockedEffects", fieldOffset7, maxEnd);
         }

         int pos7 = offset + 90 + fieldOffset7;
         pos7 += DamageEffects.computeBytesConsumed(buf, pos7);
         if (pos7 - offset > maxEnd) {
            maxEnd = pos7 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 90L;
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
      return hasEffects(mem, offset) ? InteractionEffects.toObject(mem, offset + getValidatedOffset(mem, offset, 58, 90, "Effects")) : null;
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
         int off = offset + getValidatedOffset(mem, offset, 62, 90, "Settings");
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
      return hasRules(mem, offset) ? InteractionRules.toObject(mem, offset + getValidatedOffset(mem, offset, 66, 90, "Rules")) : null;
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
         int off = offset + getValidatedOffset(mem, offset, 70, 90, "Tags");
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
      return hasCamera(mem, offset) ? InteractionCameraSettings.toObject(mem, offset + getValidatedOffset(mem, offset, 74, 90, "Camera")) : null;
   }

   public static int getFailed(MemorySegment mem) {
      return getFailed(mem, 0);
   }

   public static int getFailed(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 12));
   }

   public static boolean getAllowIndefiniteHold(MemorySegment mem) {
      return getAllowIndefiniteHold(mem, 0);
   }

   public static boolean getAllowIndefiniteHold(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 16));
   }

   public static boolean getDisplayProgress(MemorySegment mem) {
      return getDisplayProgress(mem, 0);
   }

   public static boolean getDisplayProgress(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 17));
   }

   public static boolean getCancelOnOtherClick(MemorySegment mem) {
      return getCancelOnOtherClick(mem, 0);
   }

   public static boolean getCancelOnOtherClick(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 18));
   }

   public static boolean getFailOnDamage(MemorySegment mem) {
      return getFailOnDamage(mem, 0);
   }

   public static boolean getFailOnDamage(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 19));
   }

   public static float getMouseSensitivityAdjustmentTarget(MemorySegment mem) {
      return getMouseSensitivityAdjustmentTarget(mem, 0);
   }

   public static float getMouseSensitivityAdjustmentTarget(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 20));
   }

   public static float getMouseSensitivityAdjustmentDuration(MemorySegment mem) {
      return getMouseSensitivityAdjustmentDuration(mem, 0);
   }

   public static float getMouseSensitivityAdjustmentDuration(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 24));
   }

   @Nullable
   public static Map<Float, Integer> getChargedNext(MemorySegment mem) {
      return getChargedNext(mem, 0);
   }

   @Nullable
   public static Map<Float, Integer> getChargedNext(MemorySegment mem, int offset) {
      if (!hasChargedNext(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 78, 90, "ChargedNext");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("ChargedNext", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ChargedNext", len, 4096000);
         } else {
            Map<Float, Integer> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               float key = mem.get(PacketIO.PROTO_FLOAT, (long)off);
               off += 4;
               int value = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("ChargedNext", key);
               }
            }

            return data;
         }
      }
   }

   @Nullable
   public static Map<InteractionType, Integer> getForks(MemorySegment mem) {
      return getForks(mem, 0);
   }

   @Nullable
   public static Map<InteractionType, Integer> getForks(MemorySegment mem, int offset) {
      if (!hasForks(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 82, 90, "Forks");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Forks", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Forks", len, 4096000);
         } else {
            Map<InteractionType, Integer> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               InteractionType key = InteractionType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)off));
               int value = mem.get(PacketIO.PROTO_INT, (long)(++off));
               off += 4;
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Forks", key);
               }
            }

            return data;
         }
      }
   }

   @Nullable
   public static ChargingDelay getChargingDelay(MemorySegment mem) {
      return getChargingDelay(mem, 0);
   }

   @Nullable
   public static ChargingDelay getChargingDelay(MemorySegment mem, int offset) {
      return hasChargingDelay(mem, offset) ? ChargingDelay.toObject(mem, offset + 28) : null;
   }

   @Nullable
   public static DamageEffects getBlockedEffects(MemorySegment mem) {
      return getBlockedEffects(mem, 0);
   }

   @Nullable
   public static DamageEffects getBlockedEffects(MemorySegment mem, int offset) {
      return hasBlockedEffects(mem, offset) ? DamageEffects.toObject(mem, offset + getValidatedOffset(mem, offset, 86, 90, "BlockedEffects")) : null;
   }

   public static boolean getHasModifiers(MemorySegment mem) {
      return getHasModifiers(mem, 0);
   }

   public static boolean getHasModifiers(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 48));
   }

   @Nullable
   public static AngledWielding getAngledWielding(MemorySegment mem) {
      return getAngledWielding(mem, 0);
   }

   @Nullable
   public static AngledWielding getAngledWielding(MemorySegment mem, int offset) {
      return hasAngledWielding(mem, offset) ? AngledWielding.toObject(mem, offset + 49) : null;
   }

   public static boolean hasChargingDelay(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasAngledWielding(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasEffects(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasSettings(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   public static boolean hasRules(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 16) != 0;
   }

   public static boolean hasTags(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 32) != 0;
   }

   public static boolean hasCamera(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 64) != 0;
   }

   public static boolean hasChargedNext(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 128) != 0;
   }

   public static boolean hasForks(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 1) != 0;
   }

   public static boolean hasBlockedEffects(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 2) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static WieldingInteraction toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static WieldingInteraction toObject(MemorySegment mem, int offset) {
      if (offset + 90 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("WieldingInteraction", offset + 90, (int)mem.byteSize());
      } else {
         Map<GameMode, InteractionSettings> settings = null;
         if (hasSettings(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 62, 90, "Settings");
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
            int offx = offset + getValidatedOffset(mem, offset, 70, 90, "Tags");
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

         Map<Float, Integer> chargedNext = null;
         if (hasChargedNext(mem, offset)) {
            int offxx = offset + getValidatedOffset(mem, offset, 78, 90, "ChargedNext");
            long packedxx = VarInt.getWithLength(mem, offxx);
            int lenxx = (int)packedxx;
            if (lenxx < 0) {
               throw ProtocolException.negativeLength("ChargedNext", lenxx);
            }

            if (lenxx > 4096000) {
               throw ProtocolException.dictionaryTooLarge("ChargedNext", lenxx, 4096000);
            }

            chargedNext = new HashMap<>(lenxx);
            offxx += (int)(packedxx >>> 32);

            for (int ix = 0; ix < lenxx; ix++) {
               float key = mem.get(PacketIO.PROTO_FLOAT, (long)offxx);
               offxx += 4;
               int value = mem.get(PacketIO.PROTO_INT, (long)offxx);
               offxx += 4;
               if (chargedNext.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("ChargedNext", key);
               }
            }
         }

         Map<InteractionType, Integer> forks = null;
         if (hasForks(mem, offset)) {
            int offxxx = offset + getValidatedOffset(mem, offset, 82, 90, "Forks");
            long packedxxx = VarInt.getWithLength(mem, offxxx);
            int lenxxx = (int)packedxxx;
            if (lenxxx < 0) {
               throw ProtocolException.negativeLength("Forks", lenxxx);
            }

            if (lenxxx > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Forks", lenxxx, 4096000);
            }

            forks = new HashMap<>(lenxxx);
            offxxx += (int)(packedxxx >>> 32);

            for (int ixx = 0; ixx < lenxxx; ixx++) {
               InteractionType key = InteractionType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)offxxx));
               int value = mem.get(PacketIO.PROTO_INT, (long)(++offxxx));
               offxxx += 4;
               if (forks.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Forks", key);
               }
            }
         }

         return new WieldingInteraction(
            WaitForDataFrom.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2))),
            hasEffects(mem, offset) ? InteractionEffects.toObject(mem, offset + getValidatedOffset(mem, offset, 58, 90, "Effects")) : null,
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 3)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 7)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 11)),
            settings,
            hasRules(mem, offset) ? InteractionRules.toObject(mem, offset + getValidatedOffset(mem, offset, 66, 90, "Rules")) : null,
            tags,
            hasCamera(mem, offset) ? InteractionCameraSettings.toObject(mem, offset + getValidatedOffset(mem, offset, 74, 90, "Camera")) : null,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 12)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 16)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 17)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 18)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 19)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 20)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 24)),
            chargedNext,
            forks,
            hasChargingDelay(mem, offset) ? ChargingDelay.toObject(mem, offset + 28) : null,
            hasBlockedEffects(mem, offset) ? DamageEffects.toObject(mem, offset + getValidatedOffset(mem, offset, 86, 90, "BlockedEffects")) : null,
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 48)),
            hasAngledWielding(mem, offset) ? AngledWielding.toObject(mem, offset + 49) : null
         );
      }
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte[] nullBits = new byte[2];
      if (this.chargingDelay != null) {
         nullBits[0] = (byte)(nullBits[0] | 1);
      }

      if (this.angledWielding != null) {
         nullBits[0] = (byte)(nullBits[0] | 2);
      }

      if (this.effects != null) {
         nullBits[0] = (byte)(nullBits[0] | 4);
      }

      if (this.settings != null) {
         nullBits[0] = (byte)(nullBits[0] | 8);
      }

      if (this.rules != null) {
         nullBits[0] = (byte)(nullBits[0] | 16);
      }

      if (this.tags != null) {
         nullBits[0] = (byte)(nullBits[0] | 32);
      }

      if (this.camera != null) {
         nullBits[0] = (byte)(nullBits[0] | 64);
      }

      if (this.chargedNext != null) {
         nullBits[0] = (byte)(nullBits[0] | 128);
      }

      if (this.forks != null) {
         nullBits[1] = (byte)(nullBits[1] | 1);
      }

      if (this.blockedEffects != null) {
         nullBits[1] = (byte)(nullBits[1] | 2);
      }

      buf.writeBytes(nullBits);
      buf.writeByte(this.waitForDataFrom.getValue());
      buf.writeFloatLE(this.horizontalSpeedMultiplier);
      buf.writeFloatLE(this.runTime);
      buf.writeByte(this.cancelOnItemChange ? 1 : 0);
      buf.writeIntLE(this.failed);
      buf.writeByte(this.allowIndefiniteHold ? 1 : 0);
      buf.writeByte(this.displayProgress ? 1 : 0);
      buf.writeByte(this.cancelOnOtherClick ? 1 : 0);
      buf.writeByte(this.failOnDamage ? 1 : 0);
      buf.writeFloatLE(this.mouseSensitivityAdjustmentTarget);
      buf.writeFloatLE(this.mouseSensitivityAdjustmentDuration);
      if (this.chargingDelay != null) {
         this.chargingDelay.serialize(buf);
      } else {
         buf.writeZero(20);
      }

      buf.writeByte(this.hasModifiers ? 1 : 0);
      if (this.angledWielding != null) {
         this.angledWielding.serialize(buf);
      } else {
         buf.writeZero(9);
      }

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
      int chargedNextOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int forksOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int blockedEffectsOffsetSlot = buf.writerIndex();
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

      if (this.chargedNext != null) {
         buf.setIntLE(chargedNextOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.chargedNext.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ChargedNext", this.chargedNext.size(), 4096000);
         }

         VarInt.write(buf, this.chargedNext.size());

         for (Entry<Float, Integer> e : this.chargedNext.entrySet()) {
            buf.writeFloatLE(e.getKey());
            buf.writeIntLE(e.getValue());
         }
      } else {
         buf.setIntLE(chargedNextOffsetSlot, -1);
      }

      if (this.forks != null) {
         buf.setIntLE(forksOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.forks.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Forks", this.forks.size(), 4096000);
         }

         VarInt.write(buf, this.forks.size());

         for (Entry<InteractionType, Integer> e : this.forks.entrySet()) {
            buf.writeByte(e.getKey().getValue());
            buf.writeIntLE(e.getValue());
         }
      } else {
         buf.setIntLE(forksOffsetSlot, -1);
      }

      if (this.blockedEffects != null) {
         buf.setIntLE(blockedEffectsOffsetSlot, buf.writerIndex() - varBlockStart);
         this.blockedEffects.serialize(buf);
      } else {
         buf.setIntLE(blockedEffectsOffsetSlot, -1);
      }

      return buf.writerIndex() - startPos;
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.chargingDelay != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.angledWielding != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.effects != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.settings != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.rules != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.tags != null) {
         nullBits = (byte)(nullBits | 32);
      }

      if (this.camera != null) {
         nullBits = (byte)(nullBits | 64);
      }

      if (this.chargedNext != null) {
         nullBits = (byte)(nullBits | 128);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      nullBits = 0;
      if (this.forks != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.blockedEffects != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 2), (byte)this.waitForDataFrom.getValue());
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 3), this.horizontalSpeedMultiplier);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 7), this.runTime);
      mem.set(PacketIO.PROTO_BOOL, offset + 11, this.cancelOnItemChange);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 12), this.failed);
      mem.set(PacketIO.PROTO_BOOL, offset + 16, this.allowIndefiniteHold);
      mem.set(PacketIO.PROTO_BOOL, offset + 17, this.displayProgress);
      mem.set(PacketIO.PROTO_BOOL, offset + 18, this.cancelOnOtherClick);
      mem.set(PacketIO.PROTO_BOOL, offset + 19, this.failOnDamage);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 20), this.mouseSensitivityAdjustmentTarget);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 24), this.mouseSensitivityAdjustmentDuration);
      if (this.chargingDelay != null) {
         this.chargingDelay.serialize(mem, offset + 28);
      } else {
         mem.asSlice(offset + 28, 20L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_BOOL, offset + 48, this.hasModifiers);
      if (this.angledWielding != null) {
         this.angledWielding.serialize(mem, offset + 49);
      } else {
         mem.asSlice(offset + 49, 9L).fill((byte)0);
      }

      int varOffset = offset + 90;
      if (this.effects != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 58), varOffset - offset - 90);
         varOffset += this.effects.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 58), -1);
      }

      if (this.settings != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 62), varOffset - offset - 90);
         if (this.settings.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Settings", this.settings.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.settings.size());

         for (Entry<GameMode, InteractionSettings> e : this.settings.entrySet()) {
            mem.set(PacketIO.PROTO_BYTE, (long)varOffset, (byte)e.getKey().getValue());
            varOffset = ++varOffset + e.getValue().serialize(mem, varOffset);
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 62), -1);
      }

      if (this.rules != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 66), varOffset - offset - 90);
         varOffset += this.rules.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 66), -1);
      }

      if (this.tags != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 70), varOffset - offset - 90);
         if (this.tags.length > 4096000) {
            throw ProtocolException.arrayTooLong("Tags", this.tags.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.tags.length);
         MemorySegment.copy(this.tags, 0, mem, PacketIO.PROTO_INT, varOffset, this.tags.length);
         varOffset += this.tags.length * 4;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 70), -1);
      }

      if (this.camera != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 74), varOffset - offset - 90);
         varOffset += this.camera.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 74), -1);
      }

      if (this.chargedNext != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 78), varOffset - offset - 90);
         if (this.chargedNext.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ChargedNext", this.chargedNext.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.chargedNext.size());

         for (Entry<Float, Integer> e : this.chargedNext.entrySet()) {
            mem.set(PacketIO.PROTO_FLOAT, (long)varOffset, e.getKey());
            varOffset += 4;
            mem.set(PacketIO.PROTO_INT, (long)varOffset, e.getValue());
            varOffset += 4;
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 78), -1);
      }

      if (this.forks != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 82), varOffset - offset - 90);
         if (this.forks.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Forks", this.forks.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.forks.size());

         for (Entry<InteractionType, Integer> e : this.forks.entrySet()) {
            mem.set(PacketIO.PROTO_BYTE, (long)varOffset, (byte)e.getKey().getValue());
            mem.set(PacketIO.PROTO_INT, (long)(++varOffset), e.getValue());
            varOffset += 4;
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 82), -1);
      }

      if (this.blockedEffects != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 86), varOffset - offset - 90);
         varOffset += this.blockedEffects.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 86), -1);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 90;
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

      if (this.chargedNext != null) {
         size += VarInt.size(this.chargedNext.size()) + this.chargedNext.size() * 8;
      }

      if (this.forks != null) {
         size += VarInt.size(this.forks.size()) + this.forks.size() * 5;
      }

      if (this.blockedEffects != null) {
         size += this.blockedEffects.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 90) {
         return ValidationResult.error("Buffer too small: expected at least 90 bytes");
      } else {
         byte[] nullBits = PacketIO.readBytes(buffer, offset, 2);
         int v = buffer.getByte(offset + 2) & 255;
         if (v >= 3) {
            return ValidationResult.error("Invalid WaitForDataFrom value for WaitForDataFrom");
         } else {
            if ((nullBits[0] & 4) != 0) {
               v = buffer.getIntLE(offset + 58);
               if (v < 0 || v > buffer.writerIndex() - offset - 90) {
                  return ValidationResult.error("Invalid offset for Effects");
               }

               int pos = offset + 90 + v;
               ValidationResult effectsResult = InteractionEffects.validateStructure(buffer, pos);
               if (!effectsResult.isValid()) {
                  return ValidationResult.error("Invalid Effects: " + effectsResult.error());
               }

               pos += InteractionEffects.computeBytesConsumed(buffer, pos);
            }

            if ((nullBits[0] & 8) != 0) {
               v = buffer.getIntLE(offset + 62);
               if (v < 0 || v > buffer.writerIndex() - offset - 90) {
                  return ValidationResult.error("Invalid offset for Settings");
               }

               int pos = offset + 90 + v;
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

            if ((nullBits[0] & 16) != 0) {
               v = buffer.getIntLE(offset + 66);
               if (v < 0 || v > buffer.writerIndex() - offset - 90) {
                  return ValidationResult.error("Invalid offset for Rules");
               }

               int posx = offset + 90 + v;
               ValidationResult rulesResult = InteractionRules.validateStructure(buffer, posx);
               if (!rulesResult.isValid()) {
                  return ValidationResult.error("Invalid Rules: " + rulesResult.error());
               }

               posx += InteractionRules.computeBytesConsumed(buffer, posx);
            }

            if ((nullBits[0] & 32) != 0) {
               v = buffer.getIntLE(offset + 70);
               if (v < 0 || v > buffer.writerIndex() - offset - 90) {
                  return ValidationResult.error("Invalid offset for Tags");
               }

               int posx = offset + 90 + v;
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

            if ((nullBits[0] & 64) != 0) {
               v = buffer.getIntLE(offset + 74);
               if (v < 0 || v > buffer.writerIndex() - offset - 90) {
                  return ValidationResult.error("Invalid offset for Camera");
               }

               int posxx = offset + 90 + v;
               ValidationResult cameraResult = InteractionCameraSettings.validateStructure(buffer, posxx);
               if (!cameraResult.isValid()) {
                  return ValidationResult.error("Invalid Camera: " + cameraResult.error());
               }

               posxx += InteractionCameraSettings.computeBytesConsumed(buffer, posxx);
            }

            if ((nullBits[0] & 128) != 0) {
               v = buffer.getIntLE(offset + 78);
               if (v < 0 || v > buffer.writerIndex() - offset - 90) {
                  return ValidationResult.error("Invalid offset for ChargedNext");
               }

               int posxx = offset + 90 + v;
               int chargedNextCount = VarInt.peek(buffer, posxx);
               if (chargedNextCount < 0) {
                  return ValidationResult.error("Invalid dictionary count for ChargedNext");
               }

               if (chargedNextCount > 4096000) {
                  return ValidationResult.error("ChargedNext exceeds max length 4096000");
               }

               posxx += VarInt.size(chargedNextCount);

               for (int i = 0; i < chargedNextCount; i++) {
                  posxx += 4;
                  if (posxx > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading key");
                  }

                  posxx += 4;
                  if (posxx > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading value");
                  }
               }
            }

            if ((nullBits[1] & 1) != 0) {
               v = buffer.getIntLE(offset + 82);
               if (v < 0 || v > buffer.writerIndex() - offset - 90) {
                  return ValidationResult.error("Invalid offset for Forks");
               }

               int posxxx = offset + 90 + v;
               int forksCount = VarInt.peek(buffer, posxxx);
               if (forksCount < 0) {
                  return ValidationResult.error("Invalid dictionary count for Forks");
               }

               if (forksCount > 4096000) {
                  return ValidationResult.error("Forks exceeds max length 4096000");
               }

               posxxx += VarInt.size(forksCount);

               for (int i = 0; i < forksCount; i++) {
                  int vx = buffer.getByte(posxxx) & 255;
                  if (vx >= 25) {
                     return ValidationResult.error("Invalid InteractionType value for key");
                  }

                  posxxx = ++posxxx + 4;
                  if (posxxx > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading value");
                  }
               }
            }

            if ((nullBits[1] & 2) != 0) {
               v = buffer.getIntLE(offset + 86);
               if (v < 0 || v > buffer.writerIndex() - offset - 90) {
                  return ValidationResult.error("Invalid offset for BlockedEffects");
               }

               int posxxxx = offset + 90 + v;
               ValidationResult blockedEffectsResult = DamageEffects.validateStructure(buffer, posxxxx);
               if (!blockedEffectsResult.isValid()) {
                  return ValidationResult.error("Invalid BlockedEffects: " + blockedEffectsResult.error());
               }

               posxxxx += DamageEffects.computeBytesConsumed(buffer, posxxxx);
            }

            return ValidationResult.OK;
         }
      }
   }

   public WieldingInteraction clone() {
      WieldingInteraction copy = new WieldingInteraction();
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
      copy.failed = this.failed;
      copy.allowIndefiniteHold = this.allowIndefiniteHold;
      copy.displayProgress = this.displayProgress;
      copy.cancelOnOtherClick = this.cancelOnOtherClick;
      copy.failOnDamage = this.failOnDamage;
      copy.mouseSensitivityAdjustmentTarget = this.mouseSensitivityAdjustmentTarget;
      copy.mouseSensitivityAdjustmentDuration = this.mouseSensitivityAdjustmentDuration;
      copy.chargedNext = this.chargedNext != null ? new HashMap<>(this.chargedNext) : null;
      copy.forks = this.forks != null ? new HashMap<>(this.forks) : null;
      copy.chargingDelay = this.chargingDelay != null ? this.chargingDelay.clone() : null;
      copy.blockedEffects = this.blockedEffects != null ? this.blockedEffects.clone() : null;
      copy.hasModifiers = this.hasModifiers;
      copy.angledWielding = this.angledWielding != null ? this.angledWielding.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof WieldingInteraction other)
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
               && this.failed == other.failed
               && this.allowIndefiniteHold == other.allowIndefiniteHold
               && this.displayProgress == other.displayProgress
               && this.cancelOnOtherClick == other.cancelOnOtherClick
               && this.failOnDamage == other.failOnDamage
               && this.mouseSensitivityAdjustmentTarget == other.mouseSensitivityAdjustmentTarget
               && this.mouseSensitivityAdjustmentDuration == other.mouseSensitivityAdjustmentDuration
               && Objects.equals(this.chargedNext, other.chargedNext)
               && Objects.equals(this.forks, other.forks)
               && Objects.equals(this.chargingDelay, other.chargingDelay)
               && Objects.equals(this.blockedEffects, other.blockedEffects)
               && this.hasModifiers == other.hasModifiers
               && Objects.equals(this.angledWielding, other.angledWielding);
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
      result = 31 * result + Integer.hashCode(this.failed);
      result = 31 * result + Boolean.hashCode(this.allowIndefiniteHold);
      result = 31 * result + Boolean.hashCode(this.displayProgress);
      result = 31 * result + Boolean.hashCode(this.cancelOnOtherClick);
      result = 31 * result + Boolean.hashCode(this.failOnDamage);
      result = 31 * result + Float.hashCode(this.mouseSensitivityAdjustmentTarget);
      result = 31 * result + Float.hashCode(this.mouseSensitivityAdjustmentDuration);
      result = 31 * result + Objects.hashCode(this.chargedNext);
      result = 31 * result + Objects.hashCode(this.forks);
      result = 31 * result + Objects.hashCode(this.chargingDelay);
      result = 31 * result + Objects.hashCode(this.blockedEffects);
      result = 31 * result + Boolean.hashCode(this.hasModifiers);
      return 31 * result + Objects.hashCode(this.angledWielding);
   }
}
