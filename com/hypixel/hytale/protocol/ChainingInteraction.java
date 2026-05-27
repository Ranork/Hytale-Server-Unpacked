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
      if (buf.readableBytes() - offset < 47) {
         throw ProtocolException.bufferTooSmall("ChainingInteraction", 47, buf.readableBytes() - offset);
      } else {
         ChainingInteraction obj = new ChainingInteraction();
         byte nullBits = buf.getByte(offset);
         obj.waitForDataFrom = WaitForDataFrom.fromValue(buf.getByte(offset + 1));
         obj.horizontalSpeedMultiplier = buf.getFloatLE(offset + 2);
         obj.runTime = buf.getFloatLE(offset + 6);
         obj.cancelOnItemChange = buf.getByte(offset + 10) != 0;
         obj.chainingAllowance = buf.getFloatLE(offset + 11);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 15);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 47) {
               throw ProtocolException.invalidOffset("Effects", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 47 + varPosBase0;
            obj.effects = InteractionEffects.deserialize(buf, varPos0);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 19);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 47) {
               throw ProtocolException.invalidOffset("Settings", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 47 + varPosBase1;
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
            int varPosBase2 = buf.getIntLE(offset + 23);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 47) {
               throw ProtocolException.invalidOffset("Rules", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 47 + varPosBase2;
            obj.rules = InteractionRules.deserialize(buf, varPos2);
         }

         if ((nullBits & 8) != 0) {
            int varPosBase3 = buf.getIntLE(offset + 27);
            if (varPosBase3 < 0 || varPosBase3 > buf.writerIndex() - offset - 47) {
               throw ProtocolException.invalidOffset("Tags", varPosBase3, buf.readableBytes());
            }

            int varPos3 = offset + 47 + varPosBase3;
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
            int varPosBase4 = buf.getIntLE(offset + 31);
            if (varPosBase4 < 0 || varPosBase4 > buf.writerIndex() - offset - 47) {
               throw ProtocolException.invalidOffset("Camera", varPosBase4, buf.readableBytes());
            }

            int varPos4 = offset + 47 + varPosBase4;
            obj.camera = InteractionCameraSettings.deserialize(buf, varPos4);
         }

         if ((nullBits & 32) != 0) {
            int varPosBase5 = buf.getIntLE(offset + 35);
            if (varPosBase5 < 0 || varPosBase5 > buf.writerIndex() - offset - 47) {
               throw ProtocolException.invalidOffset("ChainId", varPosBase5, buf.readableBytes());
            }

            int varPos5 = offset + 47 + varPosBase5;
            int chainIdLen = VarInt.peek(buf, varPos5);
            if (chainIdLen < 0) {
               throw ProtocolException.invalidVarInt("ChainId");
            }

            int chainIdVarIntLen = VarInt.size(chainIdLen);
            if (chainIdLen > 4096000) {
               throw ProtocolException.stringTooLong("ChainId", chainIdLen, 4096000);
            }

            if (varPos5 + chainIdVarIntLen + chainIdLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("ChainId", varPos5 + chainIdVarIntLen + chainIdLen, buf.readableBytes());
            }

            obj.chainId = PacketIO.readVarString(buf, varPos5, PacketIO.UTF8);
         }

         if ((nullBits & 64) != 0) {
            int varPosBase6 = buf.getIntLE(offset + 39);
            if (varPosBase6 < 0 || varPosBase6 > buf.writerIndex() - offset - 47) {
               throw ProtocolException.invalidOffset("ChainingNext", varPosBase6, buf.readableBytes());
            }

            int varPos6 = offset + 47 + varPosBase6;
            int chainingNextCount = VarInt.peek(buf, varPos6);
            if (chainingNextCount < 0) {
               throw ProtocolException.invalidVarInt("ChainingNext");
            }

            int varIntLenx = VarInt.size(chainingNextCount);
            if (chainingNextCount > 4096000) {
               throw ProtocolException.arrayTooLong("ChainingNext", chainingNextCount, 4096000);
            }

            if (varPos6 + varIntLenx + chainingNextCount * 4L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("ChainingNext", varPos6 + varIntLenx + chainingNextCount * 4, buf.readableBytes());
            }

            obj.chainingNext = new int[chainingNextCount];

            for (int ix = 0; ix < chainingNextCount; ix++) {
               obj.chainingNext[ix] = buf.getIntLE(varPos6 + varIntLenx + ix * 4);
            }
         }

         if ((nullBits & 128) != 0) {
            int varPosBase7 = buf.getIntLE(offset + 43);
            if (varPosBase7 < 0 || varPosBase7 > buf.writerIndex() - offset - 47) {
               throw ProtocolException.invalidOffset("Flags", varPosBase7, buf.readableBytes());
            }

            int varPos7 = offset + 47 + varPosBase7;
            int flagsCount = VarInt.peek(buf, varPos7);
            if (flagsCount < 0) {
               throw ProtocolException.invalidVarInt("Flags");
            }

            int varIntLenxx = VarInt.size(flagsCount);
            if (flagsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Flags", flagsCount, 4096000);
            }

            obj.flags = new HashMap<>(flagsCount);
            int dictPos = varPos7 + varIntLenxx;

            for (int ix = 0; ix < flagsCount; ix++) {
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
               int val = buf.getIntLE(dictPos);
               dictPos += 4;
               if (obj.flags.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("flags", key);
               }
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 47;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 15);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 47) {
            throw ProtocolException.invalidOffset("Effects", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 47 + fieldOffset0;
         pos0 += InteractionEffects.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 19);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 47) {
            throw ProtocolException.invalidOffset("Settings", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 47 + fieldOffset1;
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
         int fieldOffset2 = buf.getIntLE(offset + 23);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 47) {
            throw ProtocolException.invalidOffset("Rules", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 47 + fieldOffset2;
         pos2 += InteractionRules.computeBytesConsumed(buf, pos2);
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 27);
         if (fieldOffset3 < 0 || fieldOffset3 > buf.writerIndex() - offset - 47) {
            throw ProtocolException.invalidOffset("Tags", fieldOffset3, maxEnd);
         }

         int pos3 = offset + 47 + fieldOffset3;
         int arrLen = VarInt.peek(buf, pos3);
         pos3 += VarInt.size(arrLen) + arrLen * 4;
         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      if ((nullBits & 16) != 0) {
         int fieldOffset4 = buf.getIntLE(offset + 31);
         if (fieldOffset4 < 0 || fieldOffset4 > buf.writerIndex() - offset - 47) {
            throw ProtocolException.invalidOffset("Camera", fieldOffset4, maxEnd);
         }

         int pos4 = offset + 47 + fieldOffset4;
         pos4 += InteractionCameraSettings.computeBytesConsumed(buf, pos4);
         if (pos4 - offset > maxEnd) {
            maxEnd = pos4 - offset;
         }
      }

      if ((nullBits & 32) != 0) {
         int fieldOffset5 = buf.getIntLE(offset + 35);
         if (fieldOffset5 < 0 || fieldOffset5 > buf.writerIndex() - offset - 47) {
            throw ProtocolException.invalidOffset("ChainId", fieldOffset5, maxEnd);
         }

         int pos5 = offset + 47 + fieldOffset5;
         int sl = VarInt.peek(buf, pos5);
         pos5 += VarInt.size(sl) + sl;
         if (pos5 - offset > maxEnd) {
            maxEnd = pos5 - offset;
         }
      }

      if ((nullBits & 64) != 0) {
         int fieldOffset6 = buf.getIntLE(offset + 39);
         if (fieldOffset6 < 0 || fieldOffset6 > buf.writerIndex() - offset - 47) {
            throw ProtocolException.invalidOffset("ChainingNext", fieldOffset6, maxEnd);
         }

         int pos6 = offset + 47 + fieldOffset6;
         int arrLen = VarInt.peek(buf, pos6);
         pos6 += VarInt.size(arrLen) + arrLen * 4;
         if (pos6 - offset > maxEnd) {
            maxEnd = pos6 - offset;
         }
      }

      if ((nullBits & 128) != 0) {
         int fieldOffset7 = buf.getIntLE(offset + 43);
         if (fieldOffset7 < 0 || fieldOffset7 > buf.writerIndex() - offset - 47) {
            throw ProtocolException.invalidOffset("Flags", fieldOffset7, maxEnd);
         }

         int pos7 = offset + 47 + fieldOffset7;
         int dictLen = VarInt.peek(buf, pos7);
         pos7 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            int sl = VarInt.peek(buf, pos7);
            pos7 += VarInt.size(sl) + sl;
            pos7 += 4;
         }

         if (pos7 - offset > maxEnd) {
            maxEnd = pos7 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 47L;
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
      return hasEffects(mem, offset) ? InteractionEffects.toObject(mem, offset + getValidatedOffset(mem, offset, 15, 47, "Effects")) : null;
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
         int off = offset + getValidatedOffset(mem, offset, 19, 47, "Settings");
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
      return hasRules(mem, offset) ? InteractionRules.toObject(mem, offset + getValidatedOffset(mem, offset, 23, 47, "Rules")) : null;
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
         int off = offset + getValidatedOffset(mem, offset, 27, 47, "Tags");
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
      return hasCamera(mem, offset) ? InteractionCameraSettings.toObject(mem, offset + getValidatedOffset(mem, offset, 31, 47, "Camera")) : null;
   }

   @Nullable
   public static String getChainId(MemorySegment mem) {
      return getChainId(mem, 0);
   }

   @Nullable
   public static String getChainId(MemorySegment mem, int offset) {
      return hasChainId(mem, offset)
         ? PacketIO.readVarString("ChainId", mem, offset + getValidatedOffset(mem, offset, 35, 47, "ChainId"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static float getChainingAllowance(MemorySegment mem) {
      return getChainingAllowance(mem, 0);
   }

   public static float getChainingAllowance(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 11));
   }

   @Nullable
   public static int[] getChainingNext(MemorySegment mem) {
      return getChainingNext(mem, 0);
   }

   @Nullable
   public static int[] getChainingNext(MemorySegment mem, int offset) {
      if (!hasChainingNext(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 39, 47, "ChainingNext");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("ChainingNext", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("ChainingNext", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 4L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("ChainingNext", off + lenOffset + len * 4, (int)mem.byteSize());
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
   public static Map<String, Integer> getFlags(MemorySegment mem) {
      return getFlags(mem, 0);
   }

   @Nullable
   public static Map<String, Integer> getFlags(MemorySegment mem, int offset) {
      if (!hasFlags(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 43, 47, "Flags");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Flags", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Flags", len, 4096000);
         } else {
            Map<String, Integer> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               long keyPacked = VarInt.getWithLength(mem, off);
               int nkey = (int)keyPacked + (int)(keyPacked >>> 32);
               String key = PacketIO.readVarString("key", mem, off, 16384000, PacketIO.UTF8);
               off += nkey;
               int value = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Flags", key);
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

   public static boolean hasChainId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 32) != 0;
   }

   public static boolean hasChainingNext(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 64) != 0;
   }

   public static boolean hasFlags(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 128) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static ChainingInteraction toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ChainingInteraction toObject(MemorySegment mem, int offset) {
      if (offset + 47 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ChainingInteraction", offset + 47, (int)mem.byteSize());
      } else {
         Map<GameMode, InteractionSettings> settings = null;
         if (hasSettings(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 19, 47, "Settings");
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
            int offx = offset + getValidatedOffset(mem, offset, 27, 47, "Tags");
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

         int[] chainingNext = null;
         if (hasChainingNext(mem, offset)) {
            int offxx = offset + getValidatedOffset(mem, offset, 39, 47, "ChainingNext");
            long packedxx = VarInt.getWithLength(mem, offxx);
            int lenxx = (int)packedxx;
            if (lenxx < 0) {
               throw ProtocolException.negativeLength("ChainingNext", lenxx);
            }

            if (lenxx > 4096000) {
               throw ProtocolException.arrayTooLong("ChainingNext", lenxx, 4096000);
            }

            int lenOffset = (int)(packedxx >>> 32);
            if (offxx + lenOffset + lenxx * 4L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("ChainingNext", offxx + lenOffset + lenxx * 4, (int)mem.byteSize());
            }

            offxx += lenOffset;
            chainingNext = new int[lenxx];
            MemorySegment.copy(mem, PacketIO.PROTO_INT, offxx, chainingNext, 0, lenxx);
         }

         Map<String, Integer> flags = null;
         if (hasFlags(mem, offset)) {
            int offxxx = offset + getValidatedOffset(mem, offset, 43, 47, "Flags");
            long packedxxx = VarInt.getWithLength(mem, offxxx);
            int lenxxx = (int)packedxxx;
            if (lenxxx < 0) {
               throw ProtocolException.negativeLength("Flags", lenxxx);
            }

            if (lenxxx > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Flags", lenxxx, 4096000);
            }

            flags = new HashMap<>(lenxxx);
            offxxx += (int)(packedxxx >>> 32);

            for (int ix = 0; ix < lenxxx; ix++) {
               long keyPacked = VarInt.getWithLength(mem, offxxx);
               int nkey = (int)keyPacked + (int)(keyPacked >>> 32);
               String key = PacketIO.readVarString("key", mem, offxxx, 16384000, PacketIO.UTF8);
               offxxx += nkey;
               int value = mem.get(PacketIO.PROTO_INT, (long)offxxx);
               offxxx += 4;
               if (flags.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Flags", key);
               }
            }
         }

         return new ChainingInteraction(
            WaitForDataFrom.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))),
            hasEffects(mem, offset) ? InteractionEffects.toObject(mem, offset + getValidatedOffset(mem, offset, 15, 47, "Effects")) : null,
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 2)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 6)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 10)),
            settings,
            hasRules(mem, offset) ? InteractionRules.toObject(mem, offset + getValidatedOffset(mem, offset, 23, 47, "Rules")) : null,
            tags,
            hasCamera(mem, offset) ? InteractionCameraSettings.toObject(mem, offset + getValidatedOffset(mem, offset, 31, 47, "Camera")) : null,
            hasChainId(mem, offset)
               ? PacketIO.readVarString("ChainId", mem, offset + getValidatedOffset(mem, offset, 35, 47, "ChainId"), 4096000, PacketIO.UTF8)
               : null,
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 11)),
            chainingNext,
            flags
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

      if (this.chainId != null) {
         nullBits = (byte)(nullBits | 32);
      }

      if (this.chainingNext != null) {
         nullBits = (byte)(nullBits | 64);
      }

      if (this.flags != null) {
         nullBits = (byte)(nullBits | 128);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.waitForDataFrom.getValue());
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 2), this.horizontalSpeedMultiplier);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 6), this.runTime);
      mem.set(PacketIO.PROTO_BOOL, offset + 10, this.cancelOnItemChange);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 11), this.chainingAllowance);
      int varOffset = offset + 47;
      if (this.effects != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 15), varOffset - offset - 47);
         varOffset += this.effects.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 15), -1);
      }

      if (this.settings != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 19), varOffset - offset - 47);
         if (this.settings.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Settings", this.settings.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.settings.size());

         for (Entry<GameMode, InteractionSettings> e : this.settings.entrySet()) {
            mem.set(PacketIO.PROTO_BYTE, (long)varOffset, (byte)e.getKey().getValue());
            varOffset = ++varOffset + e.getValue().serialize(mem, varOffset);
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 19), -1);
      }

      if (this.rules != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 23), varOffset - offset - 47);
         varOffset += this.rules.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 23), -1);
      }

      if (this.tags != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 27), varOffset - offset - 47);
         if (this.tags.length > 4096000) {
            throw ProtocolException.arrayTooLong("Tags", this.tags.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.tags.length);
         MemorySegment.copy(this.tags, 0, mem, PacketIO.PROTO_INT, varOffset, this.tags.length);
         varOffset += this.tags.length * 4;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 27), -1);
      }

      if (this.camera != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 31), varOffset - offset - 47);
         varOffset += this.camera.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 31), -1);
      }

      if (this.chainId != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 35), varOffset - offset - 47);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.chainId, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 35), -1);
      }

      if (this.chainingNext != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 39), varOffset - offset - 47);
         if (this.chainingNext.length > 4096000) {
            throw ProtocolException.arrayTooLong("ChainingNext", this.chainingNext.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.chainingNext.length);
         MemorySegment.copy(this.chainingNext, 0, mem, PacketIO.PROTO_INT, varOffset, this.chainingNext.length);
         varOffset += this.chainingNext.length * 4;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 39), -1);
      }

      if (this.flags != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 43), varOffset - offset - 47);
         if (this.flags.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Flags", this.flags.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.flags.size());

         for (Entry<String, Integer> e : this.flags.entrySet()) {
            varOffset += PacketIO.writeVarString(mem, varOffset, e.getKey(), 16384000);
            mem.set(PacketIO.PROTO_INT, (long)varOffset, e.getValue());
            varOffset += 4;
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 43), -1);
      }

      return varOffset - offset;
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
         int v = buffer.getByte(offset + 1) & 255;
         if (v >= 3) {
            return ValidationResult.error("Invalid WaitForDataFrom value for WaitForDataFrom");
         } else {
            if ((nullBits & 1) != 0) {
               v = buffer.getIntLE(offset + 15);
               if (v < 0 || v > buffer.writerIndex() - offset - 47) {
                  return ValidationResult.error("Invalid offset for Effects");
               }

               int pos = offset + 47 + v;
               ValidationResult effectsResult = InteractionEffects.validateStructure(buffer, pos);
               if (!effectsResult.isValid()) {
                  return ValidationResult.error("Invalid Effects: " + effectsResult.error());
               }

               pos += InteractionEffects.computeBytesConsumed(buffer, pos);
            }

            if ((nullBits & 2) != 0) {
               v = buffer.getIntLE(offset + 19);
               if (v < 0 || v > buffer.writerIndex() - offset - 47) {
                  return ValidationResult.error("Invalid offset for Settings");
               }

               int pos = offset + 47 + v;
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
               v = buffer.getIntLE(offset + 23);
               if (v < 0 || v > buffer.writerIndex() - offset - 47) {
                  return ValidationResult.error("Invalid offset for Rules");
               }

               int posx = offset + 47 + v;
               ValidationResult rulesResult = InteractionRules.validateStructure(buffer, posx);
               if (!rulesResult.isValid()) {
                  return ValidationResult.error("Invalid Rules: " + rulesResult.error());
               }

               posx += InteractionRules.computeBytesConsumed(buffer, posx);
            }

            if ((nullBits & 8) != 0) {
               v = buffer.getIntLE(offset + 27);
               if (v < 0 || v > buffer.writerIndex() - offset - 47) {
                  return ValidationResult.error("Invalid offset for Tags");
               }

               int posx = offset + 47 + v;
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
               v = buffer.getIntLE(offset + 31);
               if (v < 0 || v > buffer.writerIndex() - offset - 47) {
                  return ValidationResult.error("Invalid offset for Camera");
               }

               int posxx = offset + 47 + v;
               ValidationResult cameraResult = InteractionCameraSettings.validateStructure(buffer, posxx);
               if (!cameraResult.isValid()) {
                  return ValidationResult.error("Invalid Camera: " + cameraResult.error());
               }

               posxx += InteractionCameraSettings.computeBytesConsumed(buffer, posxx);
            }

            if ((nullBits & 32) != 0) {
               v = buffer.getIntLE(offset + 35);
               if (v < 0 || v > buffer.writerIndex() - offset - 47) {
                  return ValidationResult.error("Invalid offset for ChainId");
               }

               int posxx = offset + 47 + v;
               int chainIdLen = VarInt.peek(buffer, posxx);
               if (chainIdLen < 0) {
                  return ValidationResult.error("Invalid string length for ChainId");
               }

               if (chainIdLen > 4096000) {
                  return ValidationResult.error("ChainId exceeds max length 4096000");
               }

               posxx += VarInt.size(chainIdLen);
               posxx += chainIdLen;
               if (posxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading ChainId");
               }
            }

            if ((nullBits & 64) != 0) {
               v = buffer.getIntLE(offset + 39);
               if (v < 0 || v > buffer.writerIndex() - offset - 47) {
                  return ValidationResult.error("Invalid offset for ChainingNext");
               }

               int posxxx = offset + 47 + v;
               int chainingNextCount = VarInt.peek(buffer, posxxx);
               if (chainingNextCount < 0) {
                  return ValidationResult.error("Invalid array count for ChainingNext");
               }

               if (chainingNextCount > 4096000) {
                  return ValidationResult.error("ChainingNext exceeds max length 4096000");
               }

               posxxx += VarInt.size(chainingNextCount);
               posxxx += chainingNextCount * 4;
               if (posxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading ChainingNext");
               }
            }

            if ((nullBits & 128) != 0) {
               v = buffer.getIntLE(offset + 43);
               if (v < 0 || v > buffer.writerIndex() - offset - 47) {
                  return ValidationResult.error("Invalid offset for Flags");
               }

               int posxxxx = offset + 47 + v;
               int flagsCount = VarInt.peek(buffer, posxxxx);
               if (flagsCount < 0) {
                  return ValidationResult.error("Invalid dictionary count for Flags");
               }

               if (flagsCount > 4096000) {
                  return ValidationResult.error("Flags exceeds max length 4096000");
               }

               posxxxx += VarInt.size(flagsCount);

               for (int i = 0; i < flagsCount; i++) {
                  int keyLen = VarInt.peek(buffer, posxxxx);
                  if (keyLen < 0) {
                     return ValidationResult.error("Invalid string length for key");
                  }

                  if (keyLen > 4096000) {
                     return ValidationResult.error("key exceeds max length 4096000");
                  }

                  posxxxx += VarInt.size(keyLen);
                  posxxxx += keyLen;
                  if (posxxxx > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading key");
                  }

                  posxxxx += 4;
                  if (posxxxx > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading value");
                  }
               }
            }

            return ValidationResult.OK;
         }
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
