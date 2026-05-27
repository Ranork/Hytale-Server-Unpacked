package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityEffect {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 25;
   public static final int VARIABLE_FIELD_COUNT = 6;
   public static final int VARIABLE_BLOCK_START = 49;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public String id;
   @Nullable
   public String name;
   @Nullable
   public ApplicationEffects applicationEffects;
   public int worldRemovalSoundEventIndex;
   public int localRemovalSoundEventIndex;
   @Nullable
   public ModelOverride modelOverride;
   public float duration;
   public boolean infinite;
   public boolean debuff;
   @Nullable
   public String statusEffectIcon;
   @Nonnull
   public OverlapBehavior overlapBehavior = OverlapBehavior.Extend;
   public double damageCalculatorCooldown;
   @Nullable
   public Map<Integer, Float> statModifiers;
   @Nonnull
   public ValueType valueType = ValueType.Percent;

   public EntityEffect() {
   }

   public EntityEffect(
      @Nullable String id,
      @Nullable String name,
      @Nullable ApplicationEffects applicationEffects,
      int worldRemovalSoundEventIndex,
      int localRemovalSoundEventIndex,
      @Nullable ModelOverride modelOverride,
      float duration,
      boolean infinite,
      boolean debuff,
      @Nullable String statusEffectIcon,
      @Nonnull OverlapBehavior overlapBehavior,
      double damageCalculatorCooldown,
      @Nullable Map<Integer, Float> statModifiers,
      @Nonnull ValueType valueType
   ) {
      this.id = id;
      this.name = name;
      this.applicationEffects = applicationEffects;
      this.worldRemovalSoundEventIndex = worldRemovalSoundEventIndex;
      this.localRemovalSoundEventIndex = localRemovalSoundEventIndex;
      this.modelOverride = modelOverride;
      this.duration = duration;
      this.infinite = infinite;
      this.debuff = debuff;
      this.statusEffectIcon = statusEffectIcon;
      this.overlapBehavior = overlapBehavior;
      this.damageCalculatorCooldown = damageCalculatorCooldown;
      this.statModifiers = statModifiers;
      this.valueType = valueType;
   }

   public EntityEffect(@Nonnull EntityEffect other) {
      this.id = other.id;
      this.name = other.name;
      this.applicationEffects = other.applicationEffects;
      this.worldRemovalSoundEventIndex = other.worldRemovalSoundEventIndex;
      this.localRemovalSoundEventIndex = other.localRemovalSoundEventIndex;
      this.modelOverride = other.modelOverride;
      this.duration = other.duration;
      this.infinite = other.infinite;
      this.debuff = other.debuff;
      this.statusEffectIcon = other.statusEffectIcon;
      this.overlapBehavior = other.overlapBehavior;
      this.damageCalculatorCooldown = other.damageCalculatorCooldown;
      this.statModifiers = other.statModifiers;
      this.valueType = other.valueType;
   }

   @Nonnull
   public static EntityEffect deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 49) {
         throw ProtocolException.bufferTooSmall("EntityEffect", 49, buf.readableBytes() - offset);
      } else {
         EntityEffect obj = new EntityEffect();
         byte nullBits = buf.getByte(offset);
         obj.worldRemovalSoundEventIndex = buf.getIntLE(offset + 1);
         obj.localRemovalSoundEventIndex = buf.getIntLE(offset + 5);
         obj.duration = buf.getFloatLE(offset + 9);
         obj.infinite = buf.getByte(offset + 13) != 0;
         obj.debuff = buf.getByte(offset + 14) != 0;
         obj.overlapBehavior = OverlapBehavior.fromValue(buf.getByte(offset + 15));
         obj.damageCalculatorCooldown = buf.getDoubleLE(offset + 16);
         obj.valueType = ValueType.fromValue(buf.getByte(offset + 24));
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 25);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 49) {
               throw ProtocolException.invalidOffset("Id", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 49 + varPosBase0;
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
            int varPosBase1 = buf.getIntLE(offset + 29);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 49) {
               throw ProtocolException.invalidOffset("Name", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 49 + varPosBase1;
            int nameLen = VarInt.peek(buf, varPos1);
            if (nameLen < 0) {
               throw ProtocolException.invalidVarInt("Name");
            }

            int nameVarIntLen = VarInt.size(nameLen);
            if (nameLen > 4096000) {
               throw ProtocolException.stringTooLong("Name", nameLen, 4096000);
            }

            if (varPos1 + nameVarIntLen + nameLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Name", varPos1 + nameVarIntLen + nameLen, buf.readableBytes());
            }

            obj.name = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
         }

         if ((nullBits & 4) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 33);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 49) {
               throw ProtocolException.invalidOffset("ApplicationEffects", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 49 + varPosBase2;
            obj.applicationEffects = ApplicationEffects.deserialize(buf, varPos2);
         }

         if ((nullBits & 8) != 0) {
            int varPosBase3 = buf.getIntLE(offset + 37);
            if (varPosBase3 < 0 || varPosBase3 > buf.writerIndex() - offset - 49) {
               throw ProtocolException.invalidOffset("ModelOverride", varPosBase3, buf.readableBytes());
            }

            int varPos3 = offset + 49 + varPosBase3;
            obj.modelOverride = ModelOverride.deserialize(buf, varPos3);
         }

         if ((nullBits & 16) != 0) {
            int varPosBase4 = buf.getIntLE(offset + 41);
            if (varPosBase4 < 0 || varPosBase4 > buf.writerIndex() - offset - 49) {
               throw ProtocolException.invalidOffset("StatusEffectIcon", varPosBase4, buf.readableBytes());
            }

            int varPos4 = offset + 49 + varPosBase4;
            int statusEffectIconLen = VarInt.peek(buf, varPos4);
            if (statusEffectIconLen < 0) {
               throw ProtocolException.invalidVarInt("StatusEffectIcon");
            }

            int statusEffectIconVarIntLen = VarInt.size(statusEffectIconLen);
            if (statusEffectIconLen > 4096000) {
               throw ProtocolException.stringTooLong("StatusEffectIcon", statusEffectIconLen, 4096000);
            }

            if (varPos4 + statusEffectIconVarIntLen + statusEffectIconLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("StatusEffectIcon", varPos4 + statusEffectIconVarIntLen + statusEffectIconLen, buf.readableBytes());
            }

            obj.statusEffectIcon = PacketIO.readVarString(buf, varPos4, PacketIO.UTF8);
         }

         if ((nullBits & 32) != 0) {
            int varPosBase5 = buf.getIntLE(offset + 45);
            if (varPosBase5 < 0 || varPosBase5 > buf.writerIndex() - offset - 49) {
               throw ProtocolException.invalidOffset("StatModifiers", varPosBase5, buf.readableBytes());
            }

            int varPos5 = offset + 49 + varPosBase5;
            int statModifiersCount = VarInt.peek(buf, varPos5);
            if (statModifiersCount < 0) {
               throw ProtocolException.invalidVarInt("StatModifiers");
            }

            int varIntLen = VarInt.size(statModifiersCount);
            if (statModifiersCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("StatModifiers", statModifiersCount, 4096000);
            }

            obj.statModifiers = new HashMap<>(statModifiersCount);
            int dictPos = varPos5 + varIntLen;

            for (int i = 0; i < statModifiersCount; i++) {
               int key = buf.getIntLE(dictPos);
               dictPos += 4;
               float val = buf.getFloatLE(dictPos);
               dictPos += 4;
               if (obj.statModifiers.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("statModifiers", key);
               }
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 49;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 25);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 49) {
            throw ProtocolException.invalidOffset("Id", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 49 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 29);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 49) {
            throw ProtocolException.invalidOffset("Name", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 49 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(sl) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 33);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 49) {
            throw ProtocolException.invalidOffset("ApplicationEffects", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 49 + fieldOffset2;
         pos2 += ApplicationEffects.computeBytesConsumed(buf, pos2);
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 37);
         if (fieldOffset3 < 0 || fieldOffset3 > buf.writerIndex() - offset - 49) {
            throw ProtocolException.invalidOffset("ModelOverride", fieldOffset3, maxEnd);
         }

         int pos3 = offset + 49 + fieldOffset3;
         pos3 += ModelOverride.computeBytesConsumed(buf, pos3);
         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      if ((nullBits & 16) != 0) {
         int fieldOffset4 = buf.getIntLE(offset + 41);
         if (fieldOffset4 < 0 || fieldOffset4 > buf.writerIndex() - offset - 49) {
            throw ProtocolException.invalidOffset("StatusEffectIcon", fieldOffset4, maxEnd);
         }

         int pos4 = offset + 49 + fieldOffset4;
         int sl = VarInt.peek(buf, pos4);
         pos4 += VarInt.size(sl) + sl;
         if (pos4 - offset > maxEnd) {
            maxEnd = pos4 - offset;
         }
      }

      if ((nullBits & 32) != 0) {
         int fieldOffset5 = buf.getIntLE(offset + 45);
         if (fieldOffset5 < 0 || fieldOffset5 > buf.writerIndex() - offset - 49) {
            throw ProtocolException.invalidOffset("StatModifiers", fieldOffset5, maxEnd);
         }

         int pos5 = offset + 49 + fieldOffset5;
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

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 49L;
   }

   @Nullable
   public static String getId(MemorySegment mem) {
      return getId(mem, 0);
   }

   @Nullable
   public static String getId(MemorySegment mem, int offset) {
      return hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 25, 49, "Id"), 4096000, PacketIO.UTF8) : null;
   }

   @Nullable
   public static String getName(MemorySegment mem) {
      return getName(mem, 0);
   }

   @Nullable
   public static String getName(MemorySegment mem, int offset) {
      return hasName(mem, offset)
         ? PacketIO.readVarString("Name", mem, offset + getValidatedOffset(mem, offset, 29, 49, "Name"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static ApplicationEffects getApplicationEffects(MemorySegment mem) {
      return getApplicationEffects(mem, 0);
   }

   @Nullable
   public static ApplicationEffects getApplicationEffects(MemorySegment mem, int offset) {
      return hasApplicationEffects(mem, offset)
         ? ApplicationEffects.toObject(mem, offset + getValidatedOffset(mem, offset, 33, 49, "ApplicationEffects"))
         : null;
   }

   public static int getWorldRemovalSoundEventIndex(MemorySegment mem) {
      return getWorldRemovalSoundEventIndex(mem, 0);
   }

   public static int getWorldRemovalSoundEventIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   public static int getLocalRemovalSoundEventIndex(MemorySegment mem) {
      return getLocalRemovalSoundEventIndex(mem, 0);
   }

   public static int getLocalRemovalSoundEventIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 5));
   }

   @Nullable
   public static ModelOverride getModelOverride(MemorySegment mem) {
      return getModelOverride(mem, 0);
   }

   @Nullable
   public static ModelOverride getModelOverride(MemorySegment mem, int offset) {
      return hasModelOverride(mem, offset) ? ModelOverride.toObject(mem, offset + getValidatedOffset(mem, offset, 37, 49, "ModelOverride")) : null;
   }

   public static float getDuration(MemorySegment mem) {
      return getDuration(mem, 0);
   }

   public static float getDuration(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 9));
   }

   public static boolean getInfinite(MemorySegment mem) {
      return getInfinite(mem, 0);
   }

   public static boolean getInfinite(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 13));
   }

   public static boolean getDebuff(MemorySegment mem) {
      return getDebuff(mem, 0);
   }

   public static boolean getDebuff(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 14));
   }

   @Nullable
   public static String getStatusEffectIcon(MemorySegment mem) {
      return getStatusEffectIcon(mem, 0);
   }

   @Nullable
   public static String getStatusEffectIcon(MemorySegment mem, int offset) {
      return hasStatusEffectIcon(mem, offset)
         ? PacketIO.readVarString("StatusEffectIcon", mem, offset + getValidatedOffset(mem, offset, 41, 49, "StatusEffectIcon"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static OverlapBehavior getOverlapBehavior(MemorySegment mem) {
      return getOverlapBehavior(mem, 0);
   }

   public static OverlapBehavior getOverlapBehavior(MemorySegment mem, int offset) {
      return OverlapBehavior.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 15)));
   }

   public static double getDamageCalculatorCooldown(MemorySegment mem) {
      return getDamageCalculatorCooldown(mem, 0);
   }

   public static double getDamageCalculatorCooldown(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 16));
   }

   @Nullable
   public static Map<Integer, Float> getStatModifiers(MemorySegment mem) {
      return getStatModifiers(mem, 0);
   }

   @Nullable
   public static Map<Integer, Float> getStatModifiers(MemorySegment mem, int offset) {
      if (!hasStatModifiers(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 45, 49, "StatModifiers");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("StatModifiers", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("StatModifiers", len, 4096000);
         } else {
            Map<Integer, Float> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               float value = mem.get(PacketIO.PROTO_FLOAT, (long)off);
               off += 4;
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("StatModifiers", key);
               }
            }

            return data;
         }
      }
   }

   public static ValueType getValueType(MemorySegment mem) {
      return getValueType(mem, 0);
   }

   public static ValueType getValueType(MemorySegment mem, int offset) {
      return ValueType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 24)));
   }

   public static boolean hasId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasName(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasApplicationEffects(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasModelOverride(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   public static boolean hasStatusEffectIcon(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 16) != 0;
   }

   public static boolean hasStatModifiers(MemorySegment mem, int offset) {
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

   public static EntityEffect toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static EntityEffect toObject(MemorySegment mem, int offset) {
      if (offset + 49 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("EntityEffect", offset + 49, (int)mem.byteSize());
      } else {
         Map<Integer, Float> statModifiers = null;
         if (hasStatModifiers(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 45, 49, "StatModifiers");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("StatModifiers", len);
            }

            if (len > 4096000) {
               throw ProtocolException.dictionaryTooLarge("StatModifiers", len, 4096000);
            }

            statModifiers = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               float value = mem.get(PacketIO.PROTO_FLOAT, (long)off);
               off += 4;
               if (statModifiers.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("StatModifiers", key);
               }
            }
         }

         return new EntityEffect(
            hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 25, 49, "Id"), 4096000, PacketIO.UTF8) : null,
            hasName(mem, offset) ? PacketIO.readVarString("Name", mem, offset + getValidatedOffset(mem, offset, 29, 49, "Name"), 4096000, PacketIO.UTF8) : null,
            hasApplicationEffects(mem, offset)
               ? ApplicationEffects.toObject(mem, offset + getValidatedOffset(mem, offset, 33, 49, "ApplicationEffects"))
               : null,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 5)),
            hasModelOverride(mem, offset) ? ModelOverride.toObject(mem, offset + getValidatedOffset(mem, offset, 37, 49, "ModelOverride")) : null,
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 9)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 13)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 14)),
            hasStatusEffectIcon(mem, offset)
               ? PacketIO.readVarString("StatusEffectIcon", mem, offset + getValidatedOffset(mem, offset, 41, 49, "StatusEffectIcon"), 4096000, PacketIO.UTF8)
               : null,
            OverlapBehavior.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 15))),
            mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 16)),
            statModifiers,
            ValueType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 24)))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.id != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.name != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.applicationEffects != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.modelOverride != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.statusEffectIcon != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.statModifiers != null) {
         nullBits = (byte)(nullBits | 32);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.worldRemovalSoundEventIndex);
      buf.writeIntLE(this.localRemovalSoundEventIndex);
      buf.writeFloatLE(this.duration);
      buf.writeByte(this.infinite ? 1 : 0);
      buf.writeByte(this.debuff ? 1 : 0);
      buf.writeByte(this.overlapBehavior.getValue());
      buf.writeDoubleLE(this.damageCalculatorCooldown);
      buf.writeByte(this.valueType.getValue());
      int idOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int nameOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int applicationEffectsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int modelOverrideOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int statusEffectIconOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int statModifiersOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.id != null) {
         buf.setIntLE(idOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.id, 4096000);
      } else {
         buf.setIntLE(idOffsetSlot, -1);
      }

      if (this.name != null) {
         buf.setIntLE(nameOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.name, 4096000);
      } else {
         buf.setIntLE(nameOffsetSlot, -1);
      }

      if (this.applicationEffects != null) {
         buf.setIntLE(applicationEffectsOffsetSlot, buf.writerIndex() - varBlockStart);
         this.applicationEffects.serialize(buf);
      } else {
         buf.setIntLE(applicationEffectsOffsetSlot, -1);
      }

      if (this.modelOverride != null) {
         buf.setIntLE(modelOverrideOffsetSlot, buf.writerIndex() - varBlockStart);
         this.modelOverride.serialize(buf);
      } else {
         buf.setIntLE(modelOverrideOffsetSlot, -1);
      }

      if (this.statusEffectIcon != null) {
         buf.setIntLE(statusEffectIconOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.statusEffectIcon, 4096000);
      } else {
         buf.setIntLE(statusEffectIconOffsetSlot, -1);
      }

      if (this.statModifiers != null) {
         buf.setIntLE(statModifiersOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.statModifiers.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("StatModifiers", this.statModifiers.size(), 4096000);
         }

         VarInt.write(buf, this.statModifiers.size());

         for (Entry<Integer, Float> e : this.statModifiers.entrySet()) {
            buf.writeIntLE(e.getKey());
            buf.writeFloatLE(e.getValue());
         }
      } else {
         buf.setIntLE(statModifiersOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.id != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.name != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.applicationEffects != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.modelOverride != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.statusEffectIcon != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.statModifiers != null) {
         nullBits = (byte)(nullBits | 32);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.worldRemovalSoundEventIndex);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 5), this.localRemovalSoundEventIndex);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 9), this.duration);
      mem.set(PacketIO.PROTO_BOOL, offset + 13, this.infinite);
      mem.set(PacketIO.PROTO_BOOL, offset + 14, this.debuff);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 15), (byte)this.overlapBehavior.getValue());
      mem.set(PacketIO.PROTO_DOUBLE, (long)(offset + 16), this.damageCalculatorCooldown);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 24), (byte)this.valueType.getValue());
      int varOffset = offset + 49;
      if (this.id != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 25), varOffset - offset - 49);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.id, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 25), -1);
      }

      if (this.name != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 29), varOffset - offset - 49);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.name, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 29), -1);
      }

      if (this.applicationEffects != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 33), varOffset - offset - 49);
         varOffset += this.applicationEffects.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 33), -1);
      }

      if (this.modelOverride != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 37), varOffset - offset - 49);
         varOffset += this.modelOverride.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 37), -1);
      }

      if (this.statusEffectIcon != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 41), varOffset - offset - 49);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.statusEffectIcon, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 41), -1);
      }

      if (this.statModifiers != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 45), varOffset - offset - 49);
         if (this.statModifiers.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("StatModifiers", this.statModifiers.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.statModifiers.size());

         for (Entry<Integer, Float> e : this.statModifiers.entrySet()) {
            mem.set(PacketIO.PROTO_INT, (long)varOffset, e.getKey());
            varOffset += 4;
            mem.set(PacketIO.PROTO_FLOAT, (long)varOffset, e.getValue());
            varOffset += 4;
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 45), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 49;
      if (this.id != null) {
         size += PacketIO.stringSize(this.id);
      }

      if (this.name != null) {
         size += PacketIO.stringSize(this.name);
      }

      if (this.applicationEffects != null) {
         size += this.applicationEffects.computeSize();
      }

      if (this.modelOverride != null) {
         size += this.modelOverride.computeSize();
      }

      if (this.statusEffectIcon != null) {
         size += PacketIO.stringSize(this.statusEffectIcon);
      }

      if (this.statModifiers != null) {
         size += VarInt.size(this.statModifiers.size()) + this.statModifiers.size() * 8;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 49) {
         return ValidationResult.error("Buffer too small: expected at least 49 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int v = buffer.getByte(offset + 15) & 255;
         if (v >= 3) {
            return ValidationResult.error("Invalid OverlapBehavior value for OverlapBehavior");
         } else {
            v = buffer.getByte(offset + 24) & 255;
            if (v >= 2) {
               return ValidationResult.error("Invalid ValueType value for ValueType");
            } else {
               if ((nullBits & 1) != 0) {
                  v = buffer.getIntLE(offset + 25);
                  if (v < 0 || v > buffer.writerIndex() - offset - 49) {
                     return ValidationResult.error("Invalid offset for Id");
                  }

                  int pos = offset + 49 + v;
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
                  v = buffer.getIntLE(offset + 29);
                  if (v < 0 || v > buffer.writerIndex() - offset - 49) {
                     return ValidationResult.error("Invalid offset for Name");
                  }

                  int posx = offset + 49 + v;
                  int nameLen = VarInt.peek(buffer, posx);
                  if (nameLen < 0) {
                     return ValidationResult.error("Invalid string length for Name");
                  }

                  if (nameLen > 4096000) {
                     return ValidationResult.error("Name exceeds max length 4096000");
                  }

                  posx += VarInt.size(nameLen);
                  posx += nameLen;
                  if (posx > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading Name");
                  }
               }

               if ((nullBits & 4) != 0) {
                  v = buffer.getIntLE(offset + 33);
                  if (v < 0 || v > buffer.writerIndex() - offset - 49) {
                     return ValidationResult.error("Invalid offset for ApplicationEffects");
                  }

                  int posxx = offset + 49 + v;
                  ValidationResult applicationEffectsResult = ApplicationEffects.validateStructure(buffer, posxx);
                  if (!applicationEffectsResult.isValid()) {
                     return ValidationResult.error("Invalid ApplicationEffects: " + applicationEffectsResult.error());
                  }

                  posxx += ApplicationEffects.computeBytesConsumed(buffer, posxx);
               }

               if ((nullBits & 8) != 0) {
                  v = buffer.getIntLE(offset + 37);
                  if (v < 0 || v > buffer.writerIndex() - offset - 49) {
                     return ValidationResult.error("Invalid offset for ModelOverride");
                  }

                  int posxx = offset + 49 + v;
                  ValidationResult modelOverrideResult = ModelOverride.validateStructure(buffer, posxx);
                  if (!modelOverrideResult.isValid()) {
                     return ValidationResult.error("Invalid ModelOverride: " + modelOverrideResult.error());
                  }

                  posxx += ModelOverride.computeBytesConsumed(buffer, posxx);
               }

               if ((nullBits & 16) != 0) {
                  v = buffer.getIntLE(offset + 41);
                  if (v < 0 || v > buffer.writerIndex() - offset - 49) {
                     return ValidationResult.error("Invalid offset for StatusEffectIcon");
                  }

                  int posxx = offset + 49 + v;
                  int statusEffectIconLen = VarInt.peek(buffer, posxx);
                  if (statusEffectIconLen < 0) {
                     return ValidationResult.error("Invalid string length for StatusEffectIcon");
                  }

                  if (statusEffectIconLen > 4096000) {
                     return ValidationResult.error("StatusEffectIcon exceeds max length 4096000");
                  }

                  posxx += VarInt.size(statusEffectIconLen);
                  posxx += statusEffectIconLen;
                  if (posxx > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading StatusEffectIcon");
                  }
               }

               if ((nullBits & 32) != 0) {
                  v = buffer.getIntLE(offset + 45);
                  if (v < 0 || v > buffer.writerIndex() - offset - 49) {
                     return ValidationResult.error("Invalid offset for StatModifiers");
                  }

                  int posxxx = offset + 49 + v;
                  int statModifiersCount = VarInt.peek(buffer, posxxx);
                  if (statModifiersCount < 0) {
                     return ValidationResult.error("Invalid dictionary count for StatModifiers");
                  }

                  if (statModifiersCount > 4096000) {
                     return ValidationResult.error("StatModifiers exceeds max length 4096000");
                  }

                  posxxx += VarInt.size(statModifiersCount);

                  for (int i = 0; i < statModifiersCount; i++) {
                     posxxx += 4;
                     if (posxxx > buffer.writerIndex()) {
                        return ValidationResult.error("Buffer overflow reading key");
                     }

                     posxxx += 4;
                     if (posxxx > buffer.writerIndex()) {
                        return ValidationResult.error("Buffer overflow reading value");
                     }
                  }
               }

               return ValidationResult.OK;
            }
         }
      }
   }

   public EntityEffect clone() {
      EntityEffect copy = new EntityEffect();
      copy.id = this.id;
      copy.name = this.name;
      copy.applicationEffects = this.applicationEffects != null ? this.applicationEffects.clone() : null;
      copy.worldRemovalSoundEventIndex = this.worldRemovalSoundEventIndex;
      copy.localRemovalSoundEventIndex = this.localRemovalSoundEventIndex;
      copy.modelOverride = this.modelOverride != null ? this.modelOverride.clone() : null;
      copy.duration = this.duration;
      copy.infinite = this.infinite;
      copy.debuff = this.debuff;
      copy.statusEffectIcon = this.statusEffectIcon;
      copy.overlapBehavior = this.overlapBehavior;
      copy.damageCalculatorCooldown = this.damageCalculatorCooldown;
      copy.statModifiers = this.statModifiers != null ? new HashMap<>(this.statModifiers) : null;
      copy.valueType = this.valueType;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof EntityEffect other)
            ? false
            : Objects.equals(this.id, other.id)
               && Objects.equals(this.name, other.name)
               && Objects.equals(this.applicationEffects, other.applicationEffects)
               && this.worldRemovalSoundEventIndex == other.worldRemovalSoundEventIndex
               && this.localRemovalSoundEventIndex == other.localRemovalSoundEventIndex
               && Objects.equals(this.modelOverride, other.modelOverride)
               && this.duration == other.duration
               && this.infinite == other.infinite
               && this.debuff == other.debuff
               && Objects.equals(this.statusEffectIcon, other.statusEffectIcon)
               && Objects.equals(this.overlapBehavior, other.overlapBehavior)
               && this.damageCalculatorCooldown == other.damageCalculatorCooldown
               && Objects.equals(this.statModifiers, other.statModifiers)
               && Objects.equals(this.valueType, other.valueType);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(
         this.id,
         this.name,
         this.applicationEffects,
         this.worldRemovalSoundEventIndex,
         this.localRemovalSoundEventIndex,
         this.modelOverride,
         this.duration,
         this.infinite,
         this.debuff,
         this.statusEffectIcon,
         this.overlapBehavior,
         this.damageCalculatorCooldown,
         this.statModifiers,
         this.valueType
      );
   }
}
