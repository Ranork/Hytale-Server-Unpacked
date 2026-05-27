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
      if (buf.readableBytes() - offset < 65) {
         throw ProtocolException.bufferTooSmall("ModifyInventoryInteraction", 65, buf.readableBytes() - offset);
      } else {
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
            int varPosBase0 = buf.getIntLE(offset + 33);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 65) {
               throw ProtocolException.invalidOffset("Effects", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 65 + varPosBase0;
            obj.effects = InteractionEffects.deserialize(buf, varPos0);
         }

         if ((nullBits[0] & 4) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 37);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 65) {
               throw ProtocolException.invalidOffset("Settings", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 65 + varPosBase1;
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

         if ((nullBits[0] & 8) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 41);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 65) {
               throw ProtocolException.invalidOffset("Rules", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 65 + varPosBase2;
            obj.rules = InteractionRules.deserialize(buf, varPos2);
         }

         if ((nullBits[0] & 16) != 0) {
            int varPosBase3 = buf.getIntLE(offset + 45);
            if (varPosBase3 < 0 || varPosBase3 > buf.writerIndex() - offset - 65) {
               throw ProtocolException.invalidOffset("Tags", varPosBase3, buf.readableBytes());
            }

            int varPos3 = offset + 65 + varPosBase3;
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

         if ((nullBits[0] & 32) != 0) {
            int varPosBase4 = buf.getIntLE(offset + 49);
            if (varPosBase4 < 0 || varPosBase4 > buf.writerIndex() - offset - 65) {
               throw ProtocolException.invalidOffset("Camera", varPosBase4, buf.readableBytes());
            }

            int varPos4 = offset + 65 + varPosBase4;
            obj.camera = InteractionCameraSettings.deserialize(buf, varPos4);
         }

         if ((nullBits[0] & 64) != 0) {
            int varPosBase5 = buf.getIntLE(offset + 53);
            if (varPosBase5 < 0 || varPosBase5 > buf.writerIndex() - offset - 65) {
               throw ProtocolException.invalidOffset("ItemToRemove", varPosBase5, buf.readableBytes());
            }

            int varPos5 = offset + 65 + varPosBase5;
            obj.itemToRemove = ItemWithAllMetadata.deserialize(buf, varPos5);
         }

         if ((nullBits[0] & 128) != 0) {
            int varPosBase6 = buf.getIntLE(offset + 57);
            if (varPosBase6 < 0 || varPosBase6 > buf.writerIndex() - offset - 65) {
               throw ProtocolException.invalidOffset("ItemToAdd", varPosBase6, buf.readableBytes());
            }

            int varPos6 = offset + 65 + varPosBase6;
            obj.itemToAdd = ItemWithAllMetadata.deserialize(buf, varPos6);
         }

         if ((nullBits[1] & 1) != 0) {
            int varPosBase7 = buf.getIntLE(offset + 61);
            if (varPosBase7 < 0 || varPosBase7 > buf.writerIndex() - offset - 65) {
               throw ProtocolException.invalidOffset("BrokenItem", varPosBase7, buf.readableBytes());
            }

            int varPos7 = offset + 65 + varPosBase7;
            int brokenItemLen = VarInt.peek(buf, varPos7);
            if (brokenItemLen < 0) {
               throw ProtocolException.invalidVarInt("BrokenItem");
            }

            int brokenItemVarIntLen = VarInt.size(brokenItemLen);
            if (brokenItemLen > 4096000) {
               throw ProtocolException.stringTooLong("BrokenItem", brokenItemLen, 4096000);
            }

            if (varPos7 + brokenItemVarIntLen + brokenItemLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("BrokenItem", varPos7 + brokenItemVarIntLen + brokenItemLen, buf.readableBytes());
            }

            obj.brokenItem = PacketIO.readVarString(buf, varPos7, PacketIO.UTF8);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte[] nullBits = PacketIO.readBytes(buf, offset, 2);
      int maxEnd = 65;
      if ((nullBits[0] & 2) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 33);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 65) {
            throw ProtocolException.invalidOffset("Effects", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 65 + fieldOffset0;
         pos0 += InteractionEffects.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits[0] & 4) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 37);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 65) {
            throw ProtocolException.invalidOffset("Settings", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 65 + fieldOffset1;
         int dictLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos1 = ++pos1 + InteractionSettings.computeBytesConsumed(buf, pos1);
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits[0] & 8) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 41);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 65) {
            throw ProtocolException.invalidOffset("Rules", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 65 + fieldOffset2;
         pos2 += InteractionRules.computeBytesConsumed(buf, pos2);
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits[0] & 16) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 45);
         if (fieldOffset3 < 0 || fieldOffset3 > buf.writerIndex() - offset - 65) {
            throw ProtocolException.invalidOffset("Tags", fieldOffset3, maxEnd);
         }

         int pos3 = offset + 65 + fieldOffset3;
         int arrLen = VarInt.peek(buf, pos3);
         pos3 += VarInt.size(arrLen) + arrLen * 4;
         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      if ((nullBits[0] & 32) != 0) {
         int fieldOffset4 = buf.getIntLE(offset + 49);
         if (fieldOffset4 < 0 || fieldOffset4 > buf.writerIndex() - offset - 65) {
            throw ProtocolException.invalidOffset("Camera", fieldOffset4, maxEnd);
         }

         int pos4 = offset + 65 + fieldOffset4;
         pos4 += InteractionCameraSettings.computeBytesConsumed(buf, pos4);
         if (pos4 - offset > maxEnd) {
            maxEnd = pos4 - offset;
         }
      }

      if ((nullBits[0] & 64) != 0) {
         int fieldOffset5 = buf.getIntLE(offset + 53);
         if (fieldOffset5 < 0 || fieldOffset5 > buf.writerIndex() - offset - 65) {
            throw ProtocolException.invalidOffset("ItemToRemove", fieldOffset5, maxEnd);
         }

         int pos5 = offset + 65 + fieldOffset5;
         pos5 += ItemWithAllMetadata.computeBytesConsumed(buf, pos5);
         if (pos5 - offset > maxEnd) {
            maxEnd = pos5 - offset;
         }
      }

      if ((nullBits[0] & 128) != 0) {
         int fieldOffset6 = buf.getIntLE(offset + 57);
         if (fieldOffset6 < 0 || fieldOffset6 > buf.writerIndex() - offset - 65) {
            throw ProtocolException.invalidOffset("ItemToAdd", fieldOffset6, maxEnd);
         }

         int pos6 = offset + 65 + fieldOffset6;
         pos6 += ItemWithAllMetadata.computeBytesConsumed(buf, pos6);
         if (pos6 - offset > maxEnd) {
            maxEnd = pos6 - offset;
         }
      }

      if ((nullBits[1] & 1) != 0) {
         int fieldOffset7 = buf.getIntLE(offset + 61);
         if (fieldOffset7 < 0 || fieldOffset7 > buf.writerIndex() - offset - 65) {
            throw ProtocolException.invalidOffset("BrokenItem", fieldOffset7, maxEnd);
         }

         int pos7 = offset + 65 + fieldOffset7;
         int sl = VarInt.peek(buf, pos7);
         pos7 += VarInt.size(sl) + sl;
         if (pos7 - offset > maxEnd) {
            maxEnd = pos7 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 65L;
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
      return hasEffects(mem, offset) ? InteractionEffects.toObject(mem, offset + getValidatedOffset(mem, offset, 33, 65, "Effects")) : null;
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
         int off = offset + getValidatedOffset(mem, offset, 37, 65, "Settings");
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
      return hasRules(mem, offset) ? InteractionRules.toObject(mem, offset + getValidatedOffset(mem, offset, 41, 65, "Rules")) : null;
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
         int off = offset + getValidatedOffset(mem, offset, 45, 65, "Tags");
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
      return hasCamera(mem, offset) ? InteractionCameraSettings.toObject(mem, offset + getValidatedOffset(mem, offset, 49, 65, "Camera")) : null;
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

   @Nullable
   public static GameMode getRequiredGameMode(MemorySegment mem) {
      return getRequiredGameMode(mem, 0);
   }

   @Nullable
   public static GameMode getRequiredGameMode(MemorySegment mem, int offset) {
      return hasRequiredGameMode(mem, offset) ? GameMode.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 20))) : null;
   }

   @Nullable
   public static ItemWithAllMetadata getItemToRemove(MemorySegment mem) {
      return getItemToRemove(mem, 0);
   }

   @Nullable
   public static ItemWithAllMetadata getItemToRemove(MemorySegment mem, int offset) {
      return hasItemToRemove(mem, offset) ? ItemWithAllMetadata.toObject(mem, offset + getValidatedOffset(mem, offset, 53, 65, "ItemToRemove")) : null;
   }

   public static int getAdjustHeldItemQuantity(MemorySegment mem) {
      return getAdjustHeldItemQuantity(mem, 0);
   }

   public static int getAdjustHeldItemQuantity(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 21));
   }

   @Nullable
   public static ItemWithAllMetadata getItemToAdd(MemorySegment mem) {
      return getItemToAdd(mem, 0);
   }

   @Nullable
   public static ItemWithAllMetadata getItemToAdd(MemorySegment mem, int offset) {
      return hasItemToAdd(mem, offset) ? ItemWithAllMetadata.toObject(mem, offset + getValidatedOffset(mem, offset, 57, 65, "ItemToAdd")) : null;
   }

   @Nullable
   public static String getBrokenItem(MemorySegment mem) {
      return getBrokenItem(mem, 0);
   }

   @Nullable
   public static String getBrokenItem(MemorySegment mem, int offset) {
      return hasBrokenItem(mem, offset)
         ? PacketIO.readVarString("BrokenItem", mem, offset + getValidatedOffset(mem, offset, 61, 65, "BrokenItem"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static double getAdjustHeldItemDurability(MemorySegment mem) {
      return getAdjustHeldItemDurability(mem, 0);
   }

   public static double getAdjustHeldItemDurability(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 25));
   }

   public static boolean hasRequiredGameMode(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasEffects(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasSettings(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasRules(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   public static boolean hasTags(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 16) != 0;
   }

   public static boolean hasCamera(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 32) != 0;
   }

   public static boolean hasItemToRemove(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 64) != 0;
   }

   public static boolean hasItemToAdd(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 128) != 0;
   }

   public static boolean hasBrokenItem(MemorySegment mem, int offset) {
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

   public static ModifyInventoryInteraction toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ModifyInventoryInteraction toObject(MemorySegment mem, int offset) {
      if (offset + 65 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ModifyInventoryInteraction", offset + 65, (int)mem.byteSize());
      } else {
         Map<GameMode, InteractionSettings> settings = null;
         if (hasSettings(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 37, 65, "Settings");
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
            int offx = offset + getValidatedOffset(mem, offset, 45, 65, "Tags");
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

         return new ModifyInventoryInteraction(
            WaitForDataFrom.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2))),
            hasEffects(mem, offset) ? InteractionEffects.toObject(mem, offset + getValidatedOffset(mem, offset, 33, 65, "Effects")) : null,
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 3)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 7)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 11)),
            settings,
            hasRules(mem, offset) ? InteractionRules.toObject(mem, offset + getValidatedOffset(mem, offset, 41, 65, "Rules")) : null,
            tags,
            hasCamera(mem, offset) ? InteractionCameraSettings.toObject(mem, offset + getValidatedOffset(mem, offset, 49, 65, "Camera")) : null,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 12)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 16)),
            hasRequiredGameMode(mem, offset) ? GameMode.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 20))) : null,
            hasItemToRemove(mem, offset) ? ItemWithAllMetadata.toObject(mem, offset + getValidatedOffset(mem, offset, 53, 65, "ItemToRemove")) : null,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 21)),
            hasItemToAdd(mem, offset) ? ItemWithAllMetadata.toObject(mem, offset + getValidatedOffset(mem, offset, 57, 65, "ItemToAdd")) : null,
            hasBrokenItem(mem, offset)
               ? PacketIO.readVarString("BrokenItem", mem, offset + getValidatedOffset(mem, offset, 61, 65, "BrokenItem"), 4096000, PacketIO.UTF8)
               : null,
            mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 25))
         );
      }
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
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.requiredGameMode != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.effects != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.settings != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.rules != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.tags != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.camera != null) {
         nullBits = (byte)(nullBits | 32);
      }

      if (this.itemToRemove != null) {
         nullBits = (byte)(nullBits | 64);
      }

      if (this.itemToAdd != null) {
         nullBits = (byte)(nullBits | 128);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      nullBits = 0;
      if (this.brokenItem != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 2), (byte)this.waitForDataFrom.getValue());
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 3), this.horizontalSpeedMultiplier);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 7), this.runTime);
      mem.set(PacketIO.PROTO_BOOL, offset + 11, this.cancelOnItemChange);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 12), this.next);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 16), this.failed);
      if (this.requiredGameMode != null) {
         mem.set(PacketIO.PROTO_BYTE, (long)(offset + 20), (byte)this.requiredGameMode.getValue());
      } else {
         mem.asSlice(offset + 20, 1L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_INT, (long)(offset + 21), this.adjustHeldItemQuantity);
      mem.set(PacketIO.PROTO_DOUBLE, (long)(offset + 25), this.adjustHeldItemDurability);
      int varOffset = offset + 65;
      if (this.effects != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 33), varOffset - offset - 65);
         varOffset += this.effects.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 33), -1);
      }

      if (this.settings != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 37), varOffset - offset - 65);
         if (this.settings.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Settings", this.settings.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.settings.size());

         for (Entry<GameMode, InteractionSettings> e : this.settings.entrySet()) {
            mem.set(PacketIO.PROTO_BYTE, (long)varOffset, (byte)e.getKey().getValue());
            varOffset = ++varOffset + e.getValue().serialize(mem, varOffset);
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 37), -1);
      }

      if (this.rules != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 41), varOffset - offset - 65);
         varOffset += this.rules.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 41), -1);
      }

      if (this.tags != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 45), varOffset - offset - 65);
         if (this.tags.length > 4096000) {
            throw ProtocolException.arrayTooLong("Tags", this.tags.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.tags.length);
         MemorySegment.copy(this.tags, 0, mem, PacketIO.PROTO_INT, varOffset, this.tags.length);
         varOffset += this.tags.length * 4;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 45), -1);
      }

      if (this.camera != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 49), varOffset - offset - 65);
         varOffset += this.camera.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 49), -1);
      }

      if (this.itemToRemove != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 53), varOffset - offset - 65);
         varOffset += this.itemToRemove.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 53), -1);
      }

      if (this.itemToAdd != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 57), varOffset - offset - 65);
         varOffset += this.itemToAdd.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 57), -1);
      }

      if (this.brokenItem != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 61), varOffset - offset - 65);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.brokenItem, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 61), -1);
      }

      return varOffset - offset;
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
         int v = buffer.getByte(offset + 2) & 255;
         if (v >= 3) {
            return ValidationResult.error("Invalid WaitForDataFrom value for WaitForDataFrom");
         } else {
            if ((nullBits[0] & 1) != 0) {
               v = buffer.getByte(offset + 20) & 255;
               if (v >= 2) {
                  return ValidationResult.error("Invalid GameMode value for RequiredGameMode");
               }
            }

            if ((nullBits[0] & 2) != 0) {
               v = buffer.getIntLE(offset + 33);
               if (v < 0 || v > buffer.writerIndex() - offset - 65) {
                  return ValidationResult.error("Invalid offset for Effects");
               }

               int pos = offset + 65 + v;
               ValidationResult effectsResult = InteractionEffects.validateStructure(buffer, pos);
               if (!effectsResult.isValid()) {
                  return ValidationResult.error("Invalid Effects: " + effectsResult.error());
               }

               pos += InteractionEffects.computeBytesConsumed(buffer, pos);
            }

            if ((nullBits[0] & 4) != 0) {
               v = buffer.getIntLE(offset + 37);
               if (v < 0 || v > buffer.writerIndex() - offset - 65) {
                  return ValidationResult.error("Invalid offset for Settings");
               }

               int pos = offset + 65 + v;
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

            if ((nullBits[0] & 8) != 0) {
               v = buffer.getIntLE(offset + 41);
               if (v < 0 || v > buffer.writerIndex() - offset - 65) {
                  return ValidationResult.error("Invalid offset for Rules");
               }

               int posx = offset + 65 + v;
               ValidationResult rulesResult = InteractionRules.validateStructure(buffer, posx);
               if (!rulesResult.isValid()) {
                  return ValidationResult.error("Invalid Rules: " + rulesResult.error());
               }

               posx += InteractionRules.computeBytesConsumed(buffer, posx);
            }

            if ((nullBits[0] & 16) != 0) {
               v = buffer.getIntLE(offset + 45);
               if (v < 0 || v > buffer.writerIndex() - offset - 65) {
                  return ValidationResult.error("Invalid offset for Tags");
               }

               int posx = offset + 65 + v;
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

            if ((nullBits[0] & 32) != 0) {
               v = buffer.getIntLE(offset + 49);
               if (v < 0 || v > buffer.writerIndex() - offset - 65) {
                  return ValidationResult.error("Invalid offset for Camera");
               }

               int posxx = offset + 65 + v;
               ValidationResult cameraResult = InteractionCameraSettings.validateStructure(buffer, posxx);
               if (!cameraResult.isValid()) {
                  return ValidationResult.error("Invalid Camera: " + cameraResult.error());
               }

               posxx += InteractionCameraSettings.computeBytesConsumed(buffer, posxx);
            }

            if ((nullBits[0] & 64) != 0) {
               v = buffer.getIntLE(offset + 53);
               if (v < 0 || v > buffer.writerIndex() - offset - 65) {
                  return ValidationResult.error("Invalid offset for ItemToRemove");
               }

               int posxx = offset + 65 + v;
               ValidationResult itemToRemoveResult = ItemWithAllMetadata.validateStructure(buffer, posxx);
               if (!itemToRemoveResult.isValid()) {
                  return ValidationResult.error("Invalid ItemToRemove: " + itemToRemoveResult.error());
               }

               posxx += ItemWithAllMetadata.computeBytesConsumed(buffer, posxx);
            }

            if ((nullBits[0] & 128) != 0) {
               v = buffer.getIntLE(offset + 57);
               if (v < 0 || v > buffer.writerIndex() - offset - 65) {
                  return ValidationResult.error("Invalid offset for ItemToAdd");
               }

               int posxx = offset + 65 + v;
               ValidationResult itemToAddResult = ItemWithAllMetadata.validateStructure(buffer, posxx);
               if (!itemToAddResult.isValid()) {
                  return ValidationResult.error("Invalid ItemToAdd: " + itemToAddResult.error());
               }

               posxx += ItemWithAllMetadata.computeBytesConsumed(buffer, posxx);
            }

            if ((nullBits[1] & 1) != 0) {
               v = buffer.getIntLE(offset + 61);
               if (v < 0 || v > buffer.writerIndex() - offset - 65) {
                  return ValidationResult.error("Invalid offset for BrokenItem");
               }

               int posxx = offset + 65 + v;
               int brokenItemLen = VarInt.peek(buffer, posxx);
               if (brokenItemLen < 0) {
                  return ValidationResult.error("Invalid string length for BrokenItem");
               }

               if (brokenItemLen > 4096000) {
                  return ValidationResult.error("BrokenItem exceeds max length 4096000");
               }

               posxx += VarInt.size(brokenItemLen);
               posxx += brokenItemLen;
               if (posxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading BrokenItem");
               }
            }

            return ValidationResult.OK;
         }
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
