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

public class SpawnDeployableFromRaycastInteraction extends SimpleInteraction {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 23;
   public static final int VARIABLE_FIELD_COUNT = 7;
   public static final int VARIABLE_BLOCK_START = 51;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public DeployableConfig deployableConfig;
   public float maxDistance;
   @Nullable
   public Map<Integer, Float> costs;

   public SpawnDeployableFromRaycastInteraction() {
   }

   public SpawnDeployableFromRaycastInteraction(
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
      @Nullable DeployableConfig deployableConfig,
      float maxDistance,
      @Nullable Map<Integer, Float> costs
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
      this.deployableConfig = deployableConfig;
      this.maxDistance = maxDistance;
      this.costs = costs;
   }

   public SpawnDeployableFromRaycastInteraction(@Nonnull SpawnDeployableFromRaycastInteraction other) {
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
      this.deployableConfig = other.deployableConfig;
      this.maxDistance = other.maxDistance;
      this.costs = other.costs;
   }

   @Nonnull
   public static SpawnDeployableFromRaycastInteraction deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 51) {
         throw ProtocolException.bufferTooSmall("SpawnDeployableFromRaycastInteraction", 51, buf.readableBytes() - offset);
      } else {
         SpawnDeployableFromRaycastInteraction obj = new SpawnDeployableFromRaycastInteraction();
         byte nullBits = buf.getByte(offset);
         obj.waitForDataFrom = WaitForDataFrom.fromValue(buf.getByte(offset + 1));
         obj.horizontalSpeedMultiplier = buf.getFloatLE(offset + 2);
         obj.runTime = buf.getFloatLE(offset + 6);
         obj.cancelOnItemChange = buf.getByte(offset + 10) != 0;
         obj.next = buf.getIntLE(offset + 11);
         obj.failed = buf.getIntLE(offset + 15);
         obj.maxDistance = buf.getFloatLE(offset + 19);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 23);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 51) {
               throw ProtocolException.invalidOffset("Effects", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 51 + varPosBase0;
            obj.effects = InteractionEffects.deserialize(buf, varPos0);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 27);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 51) {
               throw ProtocolException.invalidOffset("Settings", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 51 + varPosBase1;
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

         if ((nullBits & 4) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 31);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 51) {
               throw ProtocolException.invalidOffset("Rules", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 51 + varPosBase2;
            obj.rules = InteractionRules.deserialize(buf, varPos2);
         }

         if ((nullBits & 8) != 0) {
            int varPosBase3 = buf.getIntLE(offset + 35);
            if (varPosBase3 < 0 || varPosBase3 > buf.writerIndex() - offset - 51) {
               throw ProtocolException.invalidOffset("Tags", varPosBase3, buf.readableBytes());
            }

            int varPos3 = offset + 51 + varPosBase3;
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

         if ((nullBits & 16) != 0) {
            int varPosBase4 = buf.getIntLE(offset + 39);
            if (varPosBase4 < 0 || varPosBase4 > buf.writerIndex() - offset - 51) {
               throw ProtocolException.invalidOffset("Camera", varPosBase4, buf.readableBytes());
            }

            int varPos4 = offset + 51 + varPosBase4;
            obj.camera = InteractionCameraSettings.deserialize(buf, varPos4);
         }

         if ((nullBits & 32) != 0) {
            int varPosBase5 = buf.getIntLE(offset + 43);
            if (varPosBase5 < 0 || varPosBase5 > buf.writerIndex() - offset - 51) {
               throw ProtocolException.invalidOffset("DeployableConfig", varPosBase5, buf.readableBytes());
            }

            int varPos5 = offset + 51 + varPosBase5;
            obj.deployableConfig = DeployableConfig.deserialize(buf, varPos5);
         }

         if ((nullBits & 64) != 0) {
            int varPosBase6 = buf.getIntLE(offset + 47);
            if (varPosBase6 < 0 || varPosBase6 > buf.writerIndex() - offset - 51) {
               throw ProtocolException.invalidOffset("Costs", varPosBase6, buf.readableBytes());
            }

            int varPos6 = offset + 51 + varPosBase6;
            int costsCount = VarInt.peek(buf, varPos6);
            if (costsCount < 0) {
               throw ProtocolException.invalidVarInt("Costs");
            }

            int varIntLenx = VarInt.size(costsCount);
            if (costsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Costs", costsCount, 4096000);
            }

            obj.costs = new HashMap<>(costsCount);
            int dictPos = varPos6 + varIntLenx;

            for (int ix = 0; ix < costsCount; ix++) {
               int key = buf.getIntLE(dictPos);
               dictPos += 4;
               float val = buf.getFloatLE(dictPos);
               dictPos += 4;
               if (obj.costs.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("costs", key);
               }
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 51;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 23);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 51) {
            throw ProtocolException.invalidOffset("Effects", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 51 + fieldOffset0;
         pos0 += InteractionEffects.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 27);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 51) {
            throw ProtocolException.invalidOffset("Settings", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 51 + fieldOffset1;
         int dictLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos1 = ++pos1 + InteractionSettings.computeBytesConsumed(buf, pos1);
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 31);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 51) {
            throw ProtocolException.invalidOffset("Rules", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 51 + fieldOffset2;
         pos2 += InteractionRules.computeBytesConsumed(buf, pos2);
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 35);
         if (fieldOffset3 < 0 || fieldOffset3 > buf.writerIndex() - offset - 51) {
            throw ProtocolException.invalidOffset("Tags", fieldOffset3, maxEnd);
         }

         int pos3 = offset + 51 + fieldOffset3;
         int arrLen = VarInt.peek(buf, pos3);
         pos3 += VarInt.size(arrLen) + arrLen * 4;
         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      if ((nullBits & 16) != 0) {
         int fieldOffset4 = buf.getIntLE(offset + 39);
         if (fieldOffset4 < 0 || fieldOffset4 > buf.writerIndex() - offset - 51) {
            throw ProtocolException.invalidOffset("Camera", fieldOffset4, maxEnd);
         }

         int pos4 = offset + 51 + fieldOffset4;
         pos4 += InteractionCameraSettings.computeBytesConsumed(buf, pos4);
         if (pos4 - offset > maxEnd) {
            maxEnd = pos4 - offset;
         }
      }

      if ((nullBits & 32) != 0) {
         int fieldOffset5 = buf.getIntLE(offset + 43);
         if (fieldOffset5 < 0 || fieldOffset5 > buf.writerIndex() - offset - 51) {
            throw ProtocolException.invalidOffset("DeployableConfig", fieldOffset5, maxEnd);
         }

         int pos5 = offset + 51 + fieldOffset5;
         pos5 += DeployableConfig.computeBytesConsumed(buf, pos5);
         if (pos5 - offset > maxEnd) {
            maxEnd = pos5 - offset;
         }
      }

      if ((nullBits & 64) != 0) {
         int fieldOffset6 = buf.getIntLE(offset + 47);
         if (fieldOffset6 < 0 || fieldOffset6 > buf.writerIndex() - offset - 51) {
            throw ProtocolException.invalidOffset("Costs", fieldOffset6, maxEnd);
         }

         int pos6 = offset + 51 + fieldOffset6;
         int dictLen = VarInt.peek(buf, pos6);
         pos6 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos6 += 4;
            pos6 += 4;
         }

         if (pos6 - offset > maxEnd) {
            maxEnd = pos6 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 51L;
   }

   public static WaitForDataFrom getWaitForDataFrom(MemorySegment mem) {
      return getWaitForDataFrom(mem, 0);
   }

   public static WaitForDataFrom getWaitForDataFrom(MemorySegment mem, int offset) {
      return WaitForDataFrom.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1)));
   }

   @Nullable
   public static InteractionEffects getEffects(MemorySegment mem) {
      return getEffects(mem, 0);
   }

   @Nullable
   public static InteractionEffects getEffects(MemorySegment mem, int offset) {
      return hasEffects(mem, offset) ? InteractionEffects.toObject(mem, offset + getValidatedOffset(mem, offset, 23, 51, "Effects")) : null;
   }

   public static float getHorizontalSpeedMultiplier(MemorySegment mem) {
      return getHorizontalSpeedMultiplier(mem, 0);
   }

   public static float getHorizontalSpeedMultiplier(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 2));
   }

   public static float getRunTime(MemorySegment mem) {
      return getRunTime(mem, 0);
   }

   public static float getRunTime(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 6));
   }

   public static boolean getCancelOnItemChange(MemorySegment mem) {
      return getCancelOnItemChange(mem, 0);
   }

   public static boolean getCancelOnItemChange(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 10));
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
         int off = offset + getValidatedOffset(mem, offset, 27, 51, "Settings");
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
      return hasRules(mem, offset) ? InteractionRules.toObject(mem, offset + getValidatedOffset(mem, offset, 31, 51, "Rules")) : null;
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
         int off = offset + getValidatedOffset(mem, offset, 35, 51, "Tags");
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
      return hasCamera(mem, offset) ? InteractionCameraSettings.toObject(mem, offset + getValidatedOffset(mem, offset, 39, 51, "Camera")) : null;
   }

   public static int getNext(MemorySegment mem) {
      return getNext(mem, 0);
   }

   public static int getNext(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 11));
   }

   public static int getFailed(MemorySegment mem) {
      return getFailed(mem, 0);
   }

   public static int getFailed(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 15));
   }

   @Nullable
   public static DeployableConfig getDeployableConfig(MemorySegment mem) {
      return getDeployableConfig(mem, 0);
   }

   @Nullable
   public static DeployableConfig getDeployableConfig(MemorySegment mem, int offset) {
      return hasDeployableConfig(mem, offset) ? DeployableConfig.toObject(mem, offset + getValidatedOffset(mem, offset, 43, 51, "DeployableConfig")) : null;
   }

   public static float getMaxDistance(MemorySegment mem) {
      return getMaxDistance(mem, 0);
   }

   public static float getMaxDistance(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 19));
   }

   @Nullable
   public static Map<Integer, Float> getCosts(MemorySegment mem) {
      return getCosts(mem, 0);
   }

   @Nullable
   public static Map<Integer, Float> getCosts(MemorySegment mem, int offset) {
      if (!hasCosts(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 47, 51, "Costs");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Costs", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Costs", len, 4096000);
         } else {
            Map<Integer, Float> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               float value = mem.get(PacketIO.PROTO_FLOAT, (long)off);
               off += 4;
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Costs", key);
               }
            }

            return data;
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

   public static boolean hasDeployableConfig(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 32) != 0;
   }

   public static boolean hasCosts(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 64) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static SpawnDeployableFromRaycastInteraction toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static SpawnDeployableFromRaycastInteraction toObject(MemorySegment mem, int offset) {
      if (offset + 51 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("SpawnDeployableFromRaycastInteraction", offset + 51, (int)mem.byteSize());
      } else {
         Map<GameMode, InteractionSettings> settings = null;
         if (hasSettings(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 27, 51, "Settings");
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
            int offx = offset + getValidatedOffset(mem, offset, 35, 51, "Tags");
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

         Map<Integer, Float> costs = null;
         if (hasCosts(mem, offset)) {
            int offxx = offset + getValidatedOffset(mem, offset, 47, 51, "Costs");
            long packedxx = VarInt.getWithLength(mem, offxx);
            int lenxx = (int)packedxx;
            if (lenxx < 0) {
               throw ProtocolException.negativeLength("Costs", lenxx);
            }

            if (lenxx > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Costs", lenxx, 4096000);
            }

            costs = new HashMap<>(lenxx);
            offxx += (int)(packedxx >>> 32);

            for (int ix = 0; ix < lenxx; ix++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)offxx);
               offxx += 4;
               float value = mem.get(PacketIO.PROTO_FLOAT, (long)offxx);
               offxx += 4;
               if (costs.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Costs", key);
               }
            }
         }

         return new SpawnDeployableFromRaycastInteraction(
            WaitForDataFrom.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))),
            hasEffects(mem, offset) ? InteractionEffects.toObject(mem, offset + getValidatedOffset(mem, offset, 23, 51, "Effects")) : null,
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 2)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 6)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 10)),
            settings,
            hasRules(mem, offset) ? InteractionRules.toObject(mem, offset + getValidatedOffset(mem, offset, 31, 51, "Rules")) : null,
            tags,
            hasCamera(mem, offset) ? InteractionCameraSettings.toObject(mem, offset + getValidatedOffset(mem, offset, 39, 51, "Camera")) : null,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 11)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 15)),
            hasDeployableConfig(mem, offset) ? DeployableConfig.toObject(mem, offset + getValidatedOffset(mem, offset, 43, 51, "DeployableConfig")) : null,
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 19)),
            costs
         );
      }
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

      if (this.deployableConfig != null) {
         nullBits = (byte)(nullBits | 32);
      }

      if (this.costs != null) {
         nullBits = (byte)(nullBits | 64);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.waitForDataFrom.getValue());
      buf.writeFloatLE(this.horizontalSpeedMultiplier);
      buf.writeFloatLE(this.runTime);
      buf.writeByte(this.cancelOnItemChange ? 1 : 0);
      buf.writeIntLE(this.next);
      buf.writeIntLE(this.failed);
      buf.writeFloatLE(this.maxDistance);
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
      int deployableConfigOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int costsOffsetSlot = buf.writerIndex();
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

      if (this.deployableConfig != null) {
         buf.setIntLE(deployableConfigOffsetSlot, buf.writerIndex() - varBlockStart);
         this.deployableConfig.serialize(buf);
      } else {
         buf.setIntLE(deployableConfigOffsetSlot, -1);
      }

      if (this.costs != null) {
         buf.setIntLE(costsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.costs.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Costs", this.costs.size(), 4096000);
         }

         VarInt.write(buf, this.costs.size());

         for (Entry<Integer, Float> e : this.costs.entrySet()) {
            buf.writeIntLE(e.getKey());
            buf.writeFloatLE(e.getValue());
         }
      } else {
         buf.setIntLE(costsOffsetSlot, -1);
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

      if (this.deployableConfig != null) {
         nullBits = (byte)(nullBits | 32);
      }

      if (this.costs != null) {
         nullBits = (byte)(nullBits | 64);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.waitForDataFrom.getValue());
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 2), this.horizontalSpeedMultiplier);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 6), this.runTime);
      mem.set(PacketIO.PROTO_BOOL, offset + 10, this.cancelOnItemChange);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 11), this.next);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 15), this.failed);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 19), this.maxDistance);
      int varOffset = offset + 51;
      if (this.effects != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 23), varOffset - offset - 51);
         varOffset += this.effects.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 23), -1);
      }

      if (this.settings != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 27), varOffset - offset - 51);
         if (this.settings.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Settings", this.settings.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.settings.size());

         for (Entry<GameMode, InteractionSettings> e : this.settings.entrySet()) {
            mem.set(PacketIO.PROTO_BYTE, (long)varOffset, (byte)e.getKey().getValue());
            varOffset = ++varOffset + e.getValue().serialize(mem, varOffset);
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 27), -1);
      }

      if (this.rules != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 31), varOffset - offset - 51);
         varOffset += this.rules.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 31), -1);
      }

      if (this.tags != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 35), varOffset - offset - 51);
         if (this.tags.length > 4096000) {
            throw ProtocolException.arrayTooLong("Tags", this.tags.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.tags.length);
         MemorySegment.copy(this.tags, 0, mem, PacketIO.PROTO_INT, varOffset, this.tags.length);
         varOffset += this.tags.length * 4;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 35), -1);
      }

      if (this.camera != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 39), varOffset - offset - 51);
         varOffset += this.camera.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 39), -1);
      }

      if (this.deployableConfig != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 43), varOffset - offset - 51);
         varOffset += this.deployableConfig.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 43), -1);
      }

      if (this.costs != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 47), varOffset - offset - 51);
         if (this.costs.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Costs", this.costs.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.costs.size());

         for (Entry<Integer, Float> e : this.costs.entrySet()) {
            mem.set(PacketIO.PROTO_INT, (long)varOffset, e.getKey());
            varOffset += 4;
            mem.set(PacketIO.PROTO_FLOAT, (long)varOffset, e.getValue());
            varOffset += 4;
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 47), -1);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 51;
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

      if (this.deployableConfig != null) {
         size += this.deployableConfig.computeSize();
      }

      if (this.costs != null) {
         size += VarInt.size(this.costs.size()) + this.costs.size() * 8;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 51) {
         return ValidationResult.error("Buffer too small: expected at least 51 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int v = buffer.getByte(offset + 1) & 255;
         if (v >= 3) {
            return ValidationResult.error("Invalid WaitForDataFrom value for WaitForDataFrom");
         } else {
            if ((nullBits & 1) != 0) {
               v = buffer.getIntLE(offset + 23);
               if (v < 0 || v > buffer.writerIndex() - offset - 51) {
                  return ValidationResult.error("Invalid offset for Effects");
               }

               int pos = offset + 51 + v;
               ValidationResult effectsResult = InteractionEffects.validateStructure(buffer, pos);
               if (!effectsResult.isValid()) {
                  return ValidationResult.error("Invalid Effects: " + effectsResult.error());
               }

               pos += InteractionEffects.computeBytesConsumed(buffer, pos);
            }

            if ((nullBits & 2) != 0) {
               v = buffer.getIntLE(offset + 27);
               if (v < 0 || v > buffer.writerIndex() - offset - 51) {
                  return ValidationResult.error("Invalid offset for Settings");
               }

               int pos = offset + 51 + v;
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

            if ((nullBits & 4) != 0) {
               v = buffer.getIntLE(offset + 31);
               if (v < 0 || v > buffer.writerIndex() - offset - 51) {
                  return ValidationResult.error("Invalid offset for Rules");
               }

               int posx = offset + 51 + v;
               ValidationResult rulesResult = InteractionRules.validateStructure(buffer, posx);
               if (!rulesResult.isValid()) {
                  return ValidationResult.error("Invalid Rules: " + rulesResult.error());
               }

               posx += InteractionRules.computeBytesConsumed(buffer, posx);
            }

            if ((nullBits & 8) != 0) {
               v = buffer.getIntLE(offset + 35);
               if (v < 0 || v > buffer.writerIndex() - offset - 51) {
                  return ValidationResult.error("Invalid offset for Tags");
               }

               int posx = offset + 51 + v;
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

            if ((nullBits & 16) != 0) {
               v = buffer.getIntLE(offset + 39);
               if (v < 0 || v > buffer.writerIndex() - offset - 51) {
                  return ValidationResult.error("Invalid offset for Camera");
               }

               int posxx = offset + 51 + v;
               ValidationResult cameraResult = InteractionCameraSettings.validateStructure(buffer, posxx);
               if (!cameraResult.isValid()) {
                  return ValidationResult.error("Invalid Camera: " + cameraResult.error());
               }

               posxx += InteractionCameraSettings.computeBytesConsumed(buffer, posxx);
            }

            if ((nullBits & 32) != 0) {
               v = buffer.getIntLE(offset + 43);
               if (v < 0 || v > buffer.writerIndex() - offset - 51) {
                  return ValidationResult.error("Invalid offset for DeployableConfig");
               }

               int posxx = offset + 51 + v;
               ValidationResult deployableConfigResult = DeployableConfig.validateStructure(buffer, posxx);
               if (!deployableConfigResult.isValid()) {
                  return ValidationResult.error("Invalid DeployableConfig: " + deployableConfigResult.error());
               }

               posxx += DeployableConfig.computeBytesConsumed(buffer, posxx);
            }

            if ((nullBits & 64) != 0) {
               v = buffer.getIntLE(offset + 47);
               if (v < 0 || v > buffer.writerIndex() - offset - 51) {
                  return ValidationResult.error("Invalid offset for Costs");
               }

               int posxx = offset + 51 + v;
               int costsCount = VarInt.peek(buffer, posxx);
               if (costsCount < 0) {
                  return ValidationResult.error("Invalid dictionary count for Costs");
               }

               if (costsCount > 4096000) {
                  return ValidationResult.error("Costs exceeds max length 4096000");
               }

               posxx += VarInt.size(costsCount);

               for (int i = 0; i < costsCount; i++) {
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

            return ValidationResult.OK;
         }
      }
   }

   public SpawnDeployableFromRaycastInteraction clone() {
      SpawnDeployableFromRaycastInteraction copy = new SpawnDeployableFromRaycastInteraction();
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
      copy.deployableConfig = this.deployableConfig != null ? this.deployableConfig.clone() : null;
      copy.maxDistance = this.maxDistance;
      copy.costs = this.costs != null ? new HashMap<>(this.costs) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof SpawnDeployableFromRaycastInteraction other)
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
               && Objects.equals(this.deployableConfig, other.deployableConfig)
               && this.maxDistance == other.maxDistance
               && Objects.equals(this.costs, other.costs);
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
      result = 31 * result + Objects.hashCode(this.deployableConfig);
      result = 31 * result + Float.hashCode(this.maxDistance);
      return 31 * result + Objects.hashCode(this.costs);
   }
}
