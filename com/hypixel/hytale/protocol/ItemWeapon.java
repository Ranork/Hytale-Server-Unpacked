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

public class ItemWeapon {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 4;
   public static final int VARIABLE_BLOCK_START = 18;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public int[] entityStatsToClear;
   @Nullable
   public Map<Integer, Modifier[]> statModifiers;
   public boolean renderDualWielded;
   @Nullable
   public DamageData basicDamageBreakdown;
   @Nullable
   public DamageData ultimateDamageBreakdown;

   public ItemWeapon() {
   }

   public ItemWeapon(
      @Nullable int[] entityStatsToClear,
      @Nullable Map<Integer, Modifier[]> statModifiers,
      boolean renderDualWielded,
      @Nullable DamageData basicDamageBreakdown,
      @Nullable DamageData ultimateDamageBreakdown
   ) {
      this.entityStatsToClear = entityStatsToClear;
      this.statModifiers = statModifiers;
      this.renderDualWielded = renderDualWielded;
      this.basicDamageBreakdown = basicDamageBreakdown;
      this.ultimateDamageBreakdown = ultimateDamageBreakdown;
   }

   public ItemWeapon(@Nonnull ItemWeapon other) {
      this.entityStatsToClear = other.entityStatsToClear;
      this.statModifiers = other.statModifiers;
      this.renderDualWielded = other.renderDualWielded;
      this.basicDamageBreakdown = other.basicDamageBreakdown;
      this.ultimateDamageBreakdown = other.ultimateDamageBreakdown;
   }

   @Nonnull
   public static ItemWeapon deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 18) {
         throw ProtocolException.bufferTooSmall("ItemWeapon", 18, buf.readableBytes() - offset);
      } else {
         ItemWeapon obj = new ItemWeapon();
         byte nullBits = buf.getByte(offset);
         obj.renderDualWielded = buf.getByte(offset + 1) != 0;
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 2);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 18) {
               throw ProtocolException.invalidOffset("EntityStatsToClear", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 18 + varPosBase0;
            int entityStatsToClearCount = VarInt.peek(buf, varPos0);
            if (entityStatsToClearCount < 0) {
               throw ProtocolException.invalidVarInt("EntityStatsToClear");
            }

            int varIntLen = VarInt.size(entityStatsToClearCount);
            if (entityStatsToClearCount > 4096000) {
               throw ProtocolException.arrayTooLong("EntityStatsToClear", entityStatsToClearCount, 4096000);
            }

            if (varPos0 + varIntLen + entityStatsToClearCount * 4L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("EntityStatsToClear", varPos0 + varIntLen + entityStatsToClearCount * 4, buf.readableBytes());
            }

            obj.entityStatsToClear = new int[entityStatsToClearCount];

            for (int i = 0; i < entityStatsToClearCount; i++) {
               obj.entityStatsToClear[i] = buf.getIntLE(varPos0 + varIntLen + i * 4);
            }
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 6);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 18) {
               throw ProtocolException.invalidOffset("StatModifiers", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 18 + varPosBase1;
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
            int varPosBase2 = buf.getIntLE(offset + 10);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 18) {
               throw ProtocolException.invalidOffset("BasicDamageBreakdown", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 18 + varPosBase2;
            obj.basicDamageBreakdown = DamageData.deserialize(buf, varPos2);
         }

         if ((nullBits & 8) != 0) {
            int varPosBase3 = buf.getIntLE(offset + 14);
            if (varPosBase3 < 0 || varPosBase3 > buf.writerIndex() - offset - 18) {
               throw ProtocolException.invalidOffset("UltimateDamageBreakdown", varPosBase3, buf.readableBytes());
            }

            int varPos3 = offset + 18 + varPosBase3;
            obj.ultimateDamageBreakdown = DamageData.deserialize(buf, varPos3);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 18;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 2);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 18) {
            throw ProtocolException.invalidOffset("EntityStatsToClear", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 18 + fieldOffset0;
         int arrLen = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(arrLen) + arrLen * 4;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 6);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 18) {
            throw ProtocolException.invalidOffset("StatModifiers", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 18 + fieldOffset1;
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
         int fieldOffset2 = buf.getIntLE(offset + 10);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 18) {
            throw ProtocolException.invalidOffset("BasicDamageBreakdown", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 18 + fieldOffset2;
         pos2 += DamageData.computeBytesConsumed(buf, pos2);
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 14);
         if (fieldOffset3 < 0 || fieldOffset3 > buf.writerIndex() - offset - 18) {
            throw ProtocolException.invalidOffset("UltimateDamageBreakdown", fieldOffset3, maxEnd);
         }

         int pos3 = offset + 18 + fieldOffset3;
         pos3 += DamageData.computeBytesConsumed(buf, pos3);
         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 18L;
   }

   @Nullable
   public static int[] getEntityStatsToClear(MemorySegment mem) {
      return getEntityStatsToClear(mem, 0);
   }

   @Nullable
   public static int[] getEntityStatsToClear(MemorySegment mem, int offset) {
      if (!hasEntityStatsToClear(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 2, 18, "EntityStatsToClear");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("EntityStatsToClear", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("EntityStatsToClear", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 4L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("EntityStatsToClear", off + lenOffset + len * 4, (int)mem.byteSize());
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
   public static Map<Integer, Modifier[]> getStatModifiers(MemorySegment mem) {
      return getStatModifiers(mem, 0);
   }

   @Nullable
   public static Map<Integer, Modifier[]> getStatModifiers(MemorySegment mem, int offset) {
      if (!hasStatModifiers(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 6, 18, "StatModifiers");
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

   public static boolean getRenderDualWielded(MemorySegment mem) {
      return getRenderDualWielded(mem, 0);
   }

   public static boolean getRenderDualWielded(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1));
   }

   @Nullable
   public static DamageData getBasicDamageBreakdown(MemorySegment mem) {
      return getBasicDamageBreakdown(mem, 0);
   }

   @Nullable
   public static DamageData getBasicDamageBreakdown(MemorySegment mem, int offset) {
      return hasBasicDamageBreakdown(mem, offset) ? DamageData.toObject(mem, offset + getValidatedOffset(mem, offset, 10, 18, "BasicDamageBreakdown")) : null;
   }

   @Nullable
   public static DamageData getUltimateDamageBreakdown(MemorySegment mem) {
      return getUltimateDamageBreakdown(mem, 0);
   }

   @Nullable
   public static DamageData getUltimateDamageBreakdown(MemorySegment mem, int offset) {
      return hasUltimateDamageBreakdown(mem, offset)
         ? DamageData.toObject(mem, offset + getValidatedOffset(mem, offset, 14, 18, "UltimateDamageBreakdown"))
         : null;
   }

   public static boolean hasEntityStatsToClear(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasStatModifiers(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasBasicDamageBreakdown(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasUltimateDamageBreakdown(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static ItemWeapon toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ItemWeapon toObject(MemorySegment mem, int offset) {
      if (offset + 18 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ItemWeapon", offset + 18, (int)mem.byteSize());
      } else {
         int[] entityStatsToClear = null;
         if (hasEntityStatsToClear(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 2, 18, "EntityStatsToClear");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("EntityStatsToClear", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("EntityStatsToClear", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 4L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("EntityStatsToClear", off + lenOffset + len * 4, (int)mem.byteSize());
            }

            off += lenOffset;
            entityStatsToClear = new int[len];
            MemorySegment.copy(mem, PacketIO.PROTO_INT, off, entityStatsToClear, 0, len);
         }

         Map<Integer, Modifier[]> statModifiers = null;
         if (hasStatModifiers(mem, offset)) {
            int offx = offset + getValidatedOffset(mem, offset, 6, 18, "StatModifiers");
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

         return new ItemWeapon(
            entityStatsToClear,
            statModifiers,
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1)),
            hasBasicDamageBreakdown(mem, offset) ? DamageData.toObject(mem, offset + getValidatedOffset(mem, offset, 10, 18, "BasicDamageBreakdown")) : null,
            hasUltimateDamageBreakdown(mem, offset)
               ? DamageData.toObject(mem, offset + getValidatedOffset(mem, offset, 14, 18, "UltimateDamageBreakdown"))
               : null
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.entityStatsToClear != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.statModifiers != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.basicDamageBreakdown != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.ultimateDamageBreakdown != null) {
         nullBits = (byte)(nullBits | 8);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.renderDualWielded ? 1 : 0);
      int entityStatsToClearOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int statModifiersOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int basicDamageBreakdownOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int ultimateDamageBreakdownOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.entityStatsToClear != null) {
         buf.setIntLE(entityStatsToClearOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.entityStatsToClear.length > 4096000) {
            throw ProtocolException.arrayTooLong("EntityStatsToClear", this.entityStatsToClear.length, 4096000);
         }

         VarInt.write(buf, this.entityStatsToClear.length);

         for (int item : this.entityStatsToClear) {
            buf.writeIntLE(item);
         }
      } else {
         buf.setIntLE(entityStatsToClearOffsetSlot, -1);
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

      if (this.basicDamageBreakdown != null) {
         buf.setIntLE(basicDamageBreakdownOffsetSlot, buf.writerIndex() - varBlockStart);
         this.basicDamageBreakdown.serialize(buf);
      } else {
         buf.setIntLE(basicDamageBreakdownOffsetSlot, -1);
      }

      if (this.ultimateDamageBreakdown != null) {
         buf.setIntLE(ultimateDamageBreakdownOffsetSlot, buf.writerIndex() - varBlockStart);
         this.ultimateDamageBreakdown.serialize(buf);
      } else {
         buf.setIntLE(ultimateDamageBreakdownOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.entityStatsToClear != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.statModifiers != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.basicDamageBreakdown != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.ultimateDamageBreakdown != null) {
         nullBits = (byte)(nullBits | 8);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BOOL, offset + 1, this.renderDualWielded);
      int varOffset = offset + 18;
      if (this.entityStatsToClear != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 2), varOffset - offset - 18);
         if (this.entityStatsToClear.length > 4096000) {
            throw ProtocolException.arrayTooLong("EntityStatsToClear", this.entityStatsToClear.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.entityStatsToClear.length);
         MemorySegment.copy(this.entityStatsToClear, 0, mem, PacketIO.PROTO_INT, varOffset, this.entityStatsToClear.length);
         varOffset += this.entityStatsToClear.length * 4;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 2), -1);
      }

      if (this.statModifiers != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 6), varOffset - offset - 18);
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
         mem.set(PacketIO.PROTO_INT, (long)(offset + 6), -1);
      }

      if (this.basicDamageBreakdown != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 10), varOffset - offset - 18);
         varOffset += this.basicDamageBreakdown.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 10), -1);
      }

      if (this.ultimateDamageBreakdown != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 14), varOffset - offset - 18);
         varOffset += this.ultimateDamageBreakdown.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 14), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 18;
      if (this.entityStatsToClear != null) {
         size += VarInt.size(this.entityStatsToClear.length) + this.entityStatsToClear.length * 4;
      }

      if (this.statModifiers != null) {
         int statModifiersSize = 0;

         for (Entry<Integer, Modifier[]> kvp : this.statModifiers.entrySet()) {
            statModifiersSize += 4 + VarInt.size(kvp.getValue().length) + ((Modifier[])kvp.getValue()).length * 6;
         }

         size += VarInt.size(this.statModifiers.size()) + statModifiersSize;
      }

      if (this.basicDamageBreakdown != null) {
         size += this.basicDamageBreakdown.computeSize();
      }

      if (this.ultimateDamageBreakdown != null) {
         size += this.ultimateDamageBreakdown.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 18) {
         return ValidationResult.error("Buffer too small: expected at least 18 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int entityStatsToClearOffset = buffer.getIntLE(offset + 2);
            if (entityStatsToClearOffset < 0 || entityStatsToClearOffset > buffer.writerIndex() - offset - 18) {
               return ValidationResult.error("Invalid offset for EntityStatsToClear");
            }

            int pos = offset + 18 + entityStatsToClearOffset;
            int entityStatsToClearCount = VarInt.peek(buffer, pos);
            if (entityStatsToClearCount < 0) {
               return ValidationResult.error("Invalid array count for EntityStatsToClear");
            }

            if (entityStatsToClearCount > 4096000) {
               return ValidationResult.error("EntityStatsToClear exceeds max length 4096000");
            }

            pos += VarInt.size(entityStatsToClearCount);
            pos += entityStatsToClearCount * 4;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading EntityStatsToClear");
            }
         }

         if ((nullBits & 2) != 0) {
            int statModifiersOffset = buffer.getIntLE(offset + 6);
            if (statModifiersOffset < 0 || statModifiersOffset > buffer.writerIndex() - offset - 18) {
               return ValidationResult.error("Invalid offset for StatModifiers");
            }

            int posx = offset + 18 + statModifiersOffset;
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
            int basicDamageBreakdownOffset = buffer.getIntLE(offset + 10);
            if (basicDamageBreakdownOffset < 0 || basicDamageBreakdownOffset > buffer.writerIndex() - offset - 18) {
               return ValidationResult.error("Invalid offset for BasicDamageBreakdown");
            }

            int posxx = offset + 18 + basicDamageBreakdownOffset;
            ValidationResult basicDamageBreakdownResult = DamageData.validateStructure(buffer, posxx);
            if (!basicDamageBreakdownResult.isValid()) {
               return ValidationResult.error("Invalid BasicDamageBreakdown: " + basicDamageBreakdownResult.error());
            }

            posxx += DamageData.computeBytesConsumed(buffer, posxx);
         }

         if ((nullBits & 8) != 0) {
            int ultimateDamageBreakdownOffset = buffer.getIntLE(offset + 14);
            if (ultimateDamageBreakdownOffset < 0 || ultimateDamageBreakdownOffset > buffer.writerIndex() - offset - 18) {
               return ValidationResult.error("Invalid offset for UltimateDamageBreakdown");
            }

            int posxx = offset + 18 + ultimateDamageBreakdownOffset;
            ValidationResult ultimateDamageBreakdownResult = DamageData.validateStructure(buffer, posxx);
            if (!ultimateDamageBreakdownResult.isValid()) {
               return ValidationResult.error("Invalid UltimateDamageBreakdown: " + ultimateDamageBreakdownResult.error());
            }

            posxx += DamageData.computeBytesConsumed(buffer, posxx);
         }

         return ValidationResult.OK;
      }
   }

   public ItemWeapon clone() {
      ItemWeapon copy = new ItemWeapon();
      copy.entityStatsToClear = this.entityStatsToClear != null ? Arrays.copyOf(this.entityStatsToClear, this.entityStatsToClear.length) : null;
      if (this.statModifiers != null) {
         Map<Integer, Modifier[]> m = new HashMap<>();

         for (Entry<Integer, Modifier[]> e : this.statModifiers.entrySet()) {
            m.put(e.getKey(), Arrays.stream(e.getValue()).map(x -> x.clone()).toArray(Modifier[]::new));
         }

         copy.statModifiers = m;
      }

      copy.renderDualWielded = this.renderDualWielded;
      copy.basicDamageBreakdown = this.basicDamageBreakdown != null ? this.basicDamageBreakdown.clone() : null;
      copy.ultimateDamageBreakdown = this.ultimateDamageBreakdown != null ? this.ultimateDamageBreakdown.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ItemWeapon other)
            ? false
            : Arrays.equals(this.entityStatsToClear, other.entityStatsToClear)
               && Objects.equals(this.statModifiers, other.statModifiers)
               && this.renderDualWielded == other.renderDualWielded
               && Objects.equals(this.basicDamageBreakdown, other.basicDamageBreakdown)
               && Objects.equals(this.ultimateDamageBreakdown, other.ultimateDamageBreakdown);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Arrays.hashCode(this.entityStatsToClear);
      result = 31 * result + Objects.hashCode(this.statModifiers);
      result = 31 * result + Boolean.hashCode(this.renderDualWielded);
      result = 31 * result + Objects.hashCode(this.basicDamageBreakdown);
      return 31 * result + Objects.hashCode(this.ultimateDamageBreakdown);
   }
}
