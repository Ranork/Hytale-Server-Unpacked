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

public class RootInteraction {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 6;
   public static final int VARIABLE_FIELD_COUNT = 6;
   public static final int VARIABLE_BLOCK_START = 30;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public String id;
   @Nullable
   public int[] interactions;
   @Nullable
   public InteractionCooldown cooldown;
   @Nullable
   public Map<GameMode, RootInteractionSettings> settings;
   @Nullable
   public InteractionRules rules;
   @Nullable
   public int[] tags;
   public float clickQueuingTimeout;
   public boolean requireNewClick;

   public RootInteraction() {
   }

   public RootInteraction(
      @Nullable String id,
      @Nullable int[] interactions,
      @Nullable InteractionCooldown cooldown,
      @Nullable Map<GameMode, RootInteractionSettings> settings,
      @Nullable InteractionRules rules,
      @Nullable int[] tags,
      float clickQueuingTimeout,
      boolean requireNewClick
   ) {
      this.id = id;
      this.interactions = interactions;
      this.cooldown = cooldown;
      this.settings = settings;
      this.rules = rules;
      this.tags = tags;
      this.clickQueuingTimeout = clickQueuingTimeout;
      this.requireNewClick = requireNewClick;
   }

   public RootInteraction(@Nonnull RootInteraction other) {
      this.id = other.id;
      this.interactions = other.interactions;
      this.cooldown = other.cooldown;
      this.settings = other.settings;
      this.rules = other.rules;
      this.tags = other.tags;
      this.clickQueuingTimeout = other.clickQueuingTimeout;
      this.requireNewClick = other.requireNewClick;
   }

   @Nonnull
   public static RootInteraction deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 30) {
         throw ProtocolException.bufferTooSmall("RootInteraction", 30, buf.readableBytes() - offset);
      } else {
         RootInteraction obj = new RootInteraction();
         byte nullBits = buf.getByte(offset);
         obj.clickQueuingTimeout = buf.getFloatLE(offset + 1);
         obj.requireNewClick = buf.getByte(offset + 5) != 0;
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 6);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 30) {
               throw ProtocolException.invalidOffset("Id", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 30 + varPosBase0;
            int idLen = VarInt.peek(buf, varPos0);
            if (idLen < 0) {
               throw ProtocolException.invalidVarInt("Id");
            }

            int idVarIntLen = VarInt.size(idLen);
            if (idLen > 4096000) {
               throw ProtocolException.stringTooLong("Id", idLen, 4096000);
            }

            if (varPos0 + idVarIntLen + idLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Id", varPos0 + idVarIntLen + idLen, buf.readableBytes());
            }

            obj.id = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 10);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 30) {
               throw ProtocolException.invalidOffset("Interactions", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 30 + varPosBase1;
            int interactionsCount = VarInt.peek(buf, varPos1);
            if (interactionsCount < 0) {
               throw ProtocolException.invalidVarInt("Interactions");
            }

            int varIntLen = VarInt.size(interactionsCount);
            if (interactionsCount > 4096000) {
               throw ProtocolException.arrayTooLong("Interactions", interactionsCount, 4096000);
            }

            if (varPos1 + varIntLen + interactionsCount * 4L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Interactions", varPos1 + varIntLen + interactionsCount * 4, buf.readableBytes());
            }

            obj.interactions = new int[interactionsCount];

            for (int i = 0; i < interactionsCount; i++) {
               obj.interactions[i] = buf.getIntLE(varPos1 + varIntLen + i * 4);
            }
         }

         if ((nullBits & 4) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 14);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 30) {
               throw ProtocolException.invalidOffset("Cooldown", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 30 + varPosBase2;
            obj.cooldown = InteractionCooldown.deserialize(buf, varPos2);
         }

         if ((nullBits & 8) != 0) {
            int varPosBase3 = buf.getIntLE(offset + 18);
            if (varPosBase3 < 0 || varPosBase3 > buf.writerIndex() - offset - 30) {
               throw ProtocolException.invalidOffset("Settings", varPosBase3, buf.readableBytes());
            }

            int varPos3 = offset + 30 + varPosBase3;
            int settingsCount = VarInt.peek(buf, varPos3);
            if (settingsCount < 0) {
               throw ProtocolException.invalidVarInt("Settings");
            }

            int varIntLenx = VarInt.size(settingsCount);
            if (settingsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Settings", settingsCount, 4096000);
            }

            obj.settings = new HashMap<>(settingsCount);
            int dictPos = varPos3 + varIntLenx;

            for (int i = 0; i < settingsCount; i++) {
               GameMode key = GameMode.fromValue(buf.getByte(dictPos));
               RootInteractionSettings val = RootInteractionSettings.deserialize(buf, ++dictPos);
               dictPos += RootInteractionSettings.computeBytesConsumed(buf, dictPos);
               if (obj.settings.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("settings", key);
               }
            }
         }

         if ((nullBits & 16) != 0) {
            int varPosBase4 = buf.getIntLE(offset + 22);
            if (varPosBase4 < 0 || varPosBase4 > buf.writerIndex() - offset - 30) {
               throw ProtocolException.invalidOffset("Rules", varPosBase4, buf.readableBytes());
            }

            int varPos4 = offset + 30 + varPosBase4;
            obj.rules = InteractionRules.deserialize(buf, varPos4);
         }

         if ((nullBits & 32) != 0) {
            int varPosBase5 = buf.getIntLE(offset + 26);
            if (varPosBase5 < 0 || varPosBase5 > buf.writerIndex() - offset - 30) {
               throw ProtocolException.invalidOffset("Tags", varPosBase5, buf.readableBytes());
            }

            int varPos5 = offset + 30 + varPosBase5;
            int tagsCount = VarInt.peek(buf, varPos5);
            if (tagsCount < 0) {
               throw ProtocolException.invalidVarInt("Tags");
            }

            int varIntLenx = VarInt.size(tagsCount);
            if (tagsCount > 4096000) {
               throw ProtocolException.arrayTooLong("Tags", tagsCount, 4096000);
            }

            if (varPos5 + varIntLenx + tagsCount * 4L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Tags", varPos5 + varIntLenx + tagsCount * 4, buf.readableBytes());
            }

            obj.tags = new int[tagsCount];

            for (int ix = 0; ix < tagsCount; ix++) {
               obj.tags[ix] = buf.getIntLE(varPos5 + varIntLenx + ix * 4);
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 30;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 6);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 30) {
            throw ProtocolException.invalidOffset("Id", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 30 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 10);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 30) {
            throw ProtocolException.invalidOffset("Interactions", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 30 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(arrLen) + arrLen * 4;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 14);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 30) {
            throw ProtocolException.invalidOffset("Cooldown", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 30 + fieldOffset2;
         pos2 += InteractionCooldown.computeBytesConsumed(buf, pos2);
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 18);
         if (fieldOffset3 < 0 || fieldOffset3 > buf.writerIndex() - offset - 30) {
            throw ProtocolException.invalidOffset("Settings", fieldOffset3, maxEnd);
         }

         int pos3 = offset + 30 + fieldOffset3;
         int dictLen = VarInt.peek(buf, pos3);
         pos3 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos3 = ++pos3 + RootInteractionSettings.computeBytesConsumed(buf, pos3);
         }

         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      if ((nullBits & 16) != 0) {
         int fieldOffset4 = buf.getIntLE(offset + 22);
         if (fieldOffset4 < 0 || fieldOffset4 > buf.writerIndex() - offset - 30) {
            throw ProtocolException.invalidOffset("Rules", fieldOffset4, maxEnd);
         }

         int pos4 = offset + 30 + fieldOffset4;
         pos4 += InteractionRules.computeBytesConsumed(buf, pos4);
         if (pos4 - offset > maxEnd) {
            maxEnd = pos4 - offset;
         }
      }

      if ((nullBits & 32) != 0) {
         int fieldOffset5 = buf.getIntLE(offset + 26);
         if (fieldOffset5 < 0 || fieldOffset5 > buf.writerIndex() - offset - 30) {
            throw ProtocolException.invalidOffset("Tags", fieldOffset5, maxEnd);
         }

         int pos5 = offset + 30 + fieldOffset5;
         int arrLen = VarInt.peek(buf, pos5);
         pos5 += VarInt.size(arrLen) + arrLen * 4;
         if (pos5 - offset > maxEnd) {
            maxEnd = pos5 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 30L;
   }

   @Nullable
   public static String getId(MemorySegment mem) {
      return getId(mem, 0);
   }

   @Nullable
   public static String getId(MemorySegment mem, int offset) {
      return hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 6, 30, "Id"), 4096000, PacketIO.UTF8) : null;
   }

   @Nullable
   public static int[] getInteractions(MemorySegment mem) {
      return getInteractions(mem, 0);
   }

   @Nullable
   public static int[] getInteractions(MemorySegment mem, int offset) {
      if (!hasInteractions(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 10, 30, "Interactions");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Interactions", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Interactions", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 4L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Interactions", off + lenOffset + len * 4, (int)mem.byteSize());
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
   public static InteractionCooldown getCooldown(MemorySegment mem) {
      return getCooldown(mem, 0);
   }

   @Nullable
   public static InteractionCooldown getCooldown(MemorySegment mem, int offset) {
      return hasCooldown(mem, offset) ? InteractionCooldown.toObject(mem, offset + getValidatedOffset(mem, offset, 14, 30, "Cooldown")) : null;
   }

   @Nullable
   public static Map<GameMode, RootInteractionSettings> getSettings(MemorySegment mem) {
      return getSettings(mem, 0);
   }

   @Nullable
   public static Map<GameMode, RootInteractionSettings> getSettings(MemorySegment mem, int offset) {
      if (!hasSettings(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 18, 30, "Settings");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Settings", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Settings", len, 4096000);
         } else {
            Map<GameMode, RootInteractionSettings> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               GameMode key = GameMode.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)off));
               RootInteractionSettings value = RootInteractionSettings.toObject(mem, ++off);
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
      return hasRules(mem, offset) ? InteractionRules.toObject(mem, offset + getValidatedOffset(mem, offset, 22, 30, "Rules")) : null;
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
         int off = offset + getValidatedOffset(mem, offset, 26, 30, "Tags");
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

   public static float getClickQueuingTimeout(MemorySegment mem) {
      return getClickQueuingTimeout(mem, 0);
   }

   public static float getClickQueuingTimeout(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1));
   }

   public static boolean getRequireNewClick(MemorySegment mem) {
      return getRequireNewClick(mem, 0);
   }

   public static boolean getRequireNewClick(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 5));
   }

   public static boolean hasId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasInteractions(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasCooldown(MemorySegment mem, int offset) {
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

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static RootInteraction toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static RootInteraction toObject(MemorySegment mem, int offset) {
      if (offset + 30 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("RootInteraction", offset + 30, (int)mem.byteSize());
      } else {
         int[] interactions = null;
         if (hasInteractions(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 10, 30, "Interactions");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Interactions", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Interactions", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 4L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Interactions", off + lenOffset + len * 4, (int)mem.byteSize());
            }

            off += lenOffset;
            interactions = new int[len];
            MemorySegment.copy(mem, PacketIO.PROTO_INT, off, interactions, 0, len);
         }

         Map<GameMode, RootInteractionSettings> settings = null;
         if (hasSettings(mem, offset)) {
            int offx = offset + getValidatedOffset(mem, offset, 18, 30, "Settings");
            long packedx = VarInt.getWithLength(mem, offx);
            int lenx = (int)packedx;
            if (lenx < 0) {
               throw ProtocolException.negativeLength("Settings", lenx);
            }

            if (lenx > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Settings", lenx, 4096000);
            }

            settings = new HashMap<>(lenx);
            offx += (int)(packedx >>> 32);

            for (int i = 0; i < lenx; i++) {
               GameMode key = GameMode.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)offx));
               RootInteractionSettings value = RootInteractionSettings.toObject(mem, ++offx);
               offx += value.computeSize();
               if (settings.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Settings", key);
               }
            }
         }

         int[] tags = null;
         if (hasTags(mem, offset)) {
            int offxx = offset + getValidatedOffset(mem, offset, 26, 30, "Tags");
            long packedxx = VarInt.getWithLength(mem, offxx);
            int lenxx = (int)packedxx;
            if (lenxx < 0) {
               throw ProtocolException.negativeLength("Tags", lenxx);
            }

            if (lenxx > 4096000) {
               throw ProtocolException.arrayTooLong("Tags", lenxx, 4096000);
            }

            int lenOffset = (int)(packedxx >>> 32);
            if (offxx + lenOffset + lenxx * 4L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Tags", offxx + lenOffset + lenxx * 4, (int)mem.byteSize());
            }

            offxx += lenOffset;
            tags = new int[lenxx];
            MemorySegment.copy(mem, PacketIO.PROTO_INT, offxx, tags, 0, lenxx);
         }

         return new RootInteraction(
            hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 6, 30, "Id"), 4096000, PacketIO.UTF8) : null,
            interactions,
            hasCooldown(mem, offset) ? InteractionCooldown.toObject(mem, offset + getValidatedOffset(mem, offset, 14, 30, "Cooldown")) : null,
            settings,
            hasRules(mem, offset) ? InteractionRules.toObject(mem, offset + getValidatedOffset(mem, offset, 22, 30, "Rules")) : null,
            tags,
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 5))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.id != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.interactions != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.cooldown != null) {
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

      buf.writeByte(nullBits);
      buf.writeFloatLE(this.clickQueuingTimeout);
      buf.writeByte(this.requireNewClick ? 1 : 0);
      int idOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int interactionsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int cooldownOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int settingsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int rulesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int tagsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.id != null) {
         buf.setIntLE(idOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.id, 4096000);
      } else {
         buf.setIntLE(idOffsetSlot, -1);
      }

      if (this.interactions != null) {
         buf.setIntLE(interactionsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.interactions.length > 4096000) {
            throw ProtocolException.arrayTooLong("Interactions", this.interactions.length, 4096000);
         }

         VarInt.write(buf, this.interactions.length);

         for (int item : this.interactions) {
            buf.writeIntLE(item);
         }
      } else {
         buf.setIntLE(interactionsOffsetSlot, -1);
      }

      if (this.cooldown != null) {
         buf.setIntLE(cooldownOffsetSlot, buf.writerIndex() - varBlockStart);
         this.cooldown.serialize(buf);
      } else {
         buf.setIntLE(cooldownOffsetSlot, -1);
      }

      if (this.settings != null) {
         buf.setIntLE(settingsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.settings.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Settings", this.settings.size(), 4096000);
         }

         VarInt.write(buf, this.settings.size());

         for (Entry<GameMode, RootInteractionSettings> e : this.settings.entrySet()) {
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
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.id != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.interactions != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.cooldown != null) {
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

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 1), this.clickQueuingTimeout);
      mem.set(PacketIO.PROTO_BOOL, offset + 5, this.requireNewClick);
      int varOffset = offset + 30;
      if (this.id != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 6), varOffset - offset - 30);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.id, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 6), -1);
      }

      if (this.interactions != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 10), varOffset - offset - 30);
         if (this.interactions.length > 4096000) {
            throw ProtocolException.arrayTooLong("Interactions", this.interactions.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.interactions.length);
         MemorySegment.copy(this.interactions, 0, mem, PacketIO.PROTO_INT, varOffset, this.interactions.length);
         varOffset += this.interactions.length * 4;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 10), -1);
      }

      if (this.cooldown != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 14), varOffset - offset - 30);
         varOffset += this.cooldown.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 14), -1);
      }

      if (this.settings != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 18), varOffset - offset - 30);
         if (this.settings.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Settings", this.settings.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.settings.size());

         for (Entry<GameMode, RootInteractionSettings> e : this.settings.entrySet()) {
            mem.set(PacketIO.PROTO_BYTE, (long)varOffset, (byte)e.getKey().getValue());
            varOffset = ++varOffset + e.getValue().serialize(mem, varOffset);
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 18), -1);
      }

      if (this.rules != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 22), varOffset - offset - 30);
         varOffset += this.rules.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 22), -1);
      }

      if (this.tags != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 26), varOffset - offset - 30);
         if (this.tags.length > 4096000) {
            throw ProtocolException.arrayTooLong("Tags", this.tags.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.tags.length);
         MemorySegment.copy(this.tags, 0, mem, PacketIO.PROTO_INT, varOffset, this.tags.length);
         varOffset += this.tags.length * 4;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 26), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 30;
      if (this.id != null) {
         size += PacketIO.stringSize(this.id);
      }

      if (this.interactions != null) {
         size += VarInt.size(this.interactions.length) + this.interactions.length * 4;
      }

      if (this.cooldown != null) {
         size += this.cooldown.computeSize();
      }

      if (this.settings != null) {
         int settingsSize = 0;

         for (Entry<GameMode, RootInteractionSettings> kvp : this.settings.entrySet()) {
            settingsSize += 1 + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.settings.size()) + settingsSize;
      }

      if (this.rules != null) {
         size += this.rules.computeSize();
      }

      if (this.tags != null) {
         size += VarInt.size(this.tags.length) + this.tags.length * 4;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 30) {
         return ValidationResult.error("Buffer too small: expected at least 30 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int idOffset = buffer.getIntLE(offset + 6);
            if (idOffset < 0 || idOffset > buffer.writerIndex() - offset - 30) {
               return ValidationResult.error("Invalid offset for Id");
            }

            int pos = offset + 30 + idOffset;
            int idLen = VarInt.peek(buffer, pos);
            if (idLen < 0) {
               return ValidationResult.error("Invalid string length for Id");
            }

            if (idLen > 4096000) {
               return ValidationResult.error("Id exceeds max length 4096000");
            }

            pos += VarInt.size(idLen);
            pos += idLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Id");
            }
         }

         if ((nullBits & 2) != 0) {
            int interactionsOffset = buffer.getIntLE(offset + 10);
            if (interactionsOffset < 0 || interactionsOffset > buffer.writerIndex() - offset - 30) {
               return ValidationResult.error("Invalid offset for Interactions");
            }

            int posx = offset + 30 + interactionsOffset;
            int interactionsCount = VarInt.peek(buffer, posx);
            if (interactionsCount < 0) {
               return ValidationResult.error("Invalid array count for Interactions");
            }

            if (interactionsCount > 4096000) {
               return ValidationResult.error("Interactions exceeds max length 4096000");
            }

            posx += VarInt.size(interactionsCount);
            posx += interactionsCount * 4;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Interactions");
            }
         }

         if ((nullBits & 4) != 0) {
            int cooldownOffset = buffer.getIntLE(offset + 14);
            if (cooldownOffset < 0 || cooldownOffset > buffer.writerIndex() - offset - 30) {
               return ValidationResult.error("Invalid offset for Cooldown");
            }

            int posxx = offset + 30 + cooldownOffset;
            ValidationResult cooldownResult = InteractionCooldown.validateStructure(buffer, posxx);
            if (!cooldownResult.isValid()) {
               return ValidationResult.error("Invalid Cooldown: " + cooldownResult.error());
            }

            posxx += InteractionCooldown.computeBytesConsumed(buffer, posxx);
         }

         if ((nullBits & 8) != 0) {
            int settingsOffset = buffer.getIntLE(offset + 18);
            if (settingsOffset < 0 || settingsOffset > buffer.writerIndex() - offset - 30) {
               return ValidationResult.error("Invalid offset for Settings");
            }

            int posxx = offset + 30 + settingsOffset;
            int settingsCount = VarInt.peek(buffer, posxx);
            if (settingsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for Settings");
            }

            if (settingsCount > 4096000) {
               return ValidationResult.error("Settings exceeds max length 4096000");
            }

            posxx += VarInt.size(settingsCount);

            for (int i = 0; i < settingsCount; i++) {
               int v = buffer.getByte(posxx) & 255;
               if (v >= 2) {
                  return ValidationResult.error("Invalid GameMode value for key");
               }

               posxx = ++posxx + RootInteractionSettings.computeBytesConsumed(buffer, posxx);
            }
         }

         if ((nullBits & 16) != 0) {
            int rulesOffset = buffer.getIntLE(offset + 22);
            if (rulesOffset < 0 || rulesOffset > buffer.writerIndex() - offset - 30) {
               return ValidationResult.error("Invalid offset for Rules");
            }

            int posxxx = offset + 30 + rulesOffset;
            ValidationResult rulesResult = InteractionRules.validateStructure(buffer, posxxx);
            if (!rulesResult.isValid()) {
               return ValidationResult.error("Invalid Rules: " + rulesResult.error());
            }

            posxxx += InteractionRules.computeBytesConsumed(buffer, posxxx);
         }

         if ((nullBits & 32) != 0) {
            int tagsOffset = buffer.getIntLE(offset + 26);
            if (tagsOffset < 0 || tagsOffset > buffer.writerIndex() - offset - 30) {
               return ValidationResult.error("Invalid offset for Tags");
            }

            int posxxx = offset + 30 + tagsOffset;
            int tagsCount = VarInt.peek(buffer, posxxx);
            if (tagsCount < 0) {
               return ValidationResult.error("Invalid array count for Tags");
            }

            if (tagsCount > 4096000) {
               return ValidationResult.error("Tags exceeds max length 4096000");
            }

            posxxx += VarInt.size(tagsCount);
            posxxx += tagsCount * 4;
            if (posxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Tags");
            }
         }

         return ValidationResult.OK;
      }
   }

   public RootInteraction clone() {
      RootInteraction copy = new RootInteraction();
      copy.id = this.id;
      copy.interactions = this.interactions != null ? Arrays.copyOf(this.interactions, this.interactions.length) : null;
      copy.cooldown = this.cooldown != null ? this.cooldown.clone() : null;
      if (this.settings != null) {
         Map<GameMode, RootInteractionSettings> m = new HashMap<>();

         for (Entry<GameMode, RootInteractionSettings> e : this.settings.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.settings = m;
      }

      copy.rules = this.rules != null ? this.rules.clone() : null;
      copy.tags = this.tags != null ? Arrays.copyOf(this.tags, this.tags.length) : null;
      copy.clickQueuingTimeout = this.clickQueuingTimeout;
      copy.requireNewClick = this.requireNewClick;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof RootInteraction other)
            ? false
            : Objects.equals(this.id, other.id)
               && Arrays.equals(this.interactions, other.interactions)
               && Objects.equals(this.cooldown, other.cooldown)
               && Objects.equals(this.settings, other.settings)
               && Objects.equals(this.rules, other.rules)
               && Arrays.equals(this.tags, other.tags)
               && this.clickQueuingTimeout == other.clickQueuingTimeout
               && this.requireNewClick == other.requireNewClick;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.id);
      result = 31 * result + Arrays.hashCode(this.interactions);
      result = 31 * result + Objects.hashCode(this.cooldown);
      result = 31 * result + Objects.hashCode(this.settings);
      result = 31 * result + Objects.hashCode(this.rules);
      result = 31 * result + Arrays.hashCode(this.tags);
      result = 31 * result + Float.hashCode(this.clickQueuingTimeout);
      return 31 * result + Boolean.hashCode(this.requireNewClick);
   }
}
