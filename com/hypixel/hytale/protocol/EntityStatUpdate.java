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

public class EntityStatUpdate {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 13;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 21;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public EntityStatOp op = EntityStatOp.Init;
   public boolean predictable;
   public float value;
   @Nullable
   public Map<String, Modifier> modifiers;
   @Nullable
   public String modifierKey;
   @Nullable
   public Modifier modifier;

   public EntityStatUpdate() {
   }

   public EntityStatUpdate(
      @Nonnull EntityStatOp op,
      boolean predictable,
      float value,
      @Nullable Map<String, Modifier> modifiers,
      @Nullable String modifierKey,
      @Nullable Modifier modifier
   ) {
      this.op = op;
      this.predictable = predictable;
      this.value = value;
      this.modifiers = modifiers;
      this.modifierKey = modifierKey;
      this.modifier = modifier;
   }

   public EntityStatUpdate(@Nonnull EntityStatUpdate other) {
      this.op = other.op;
      this.predictable = other.predictable;
      this.value = other.value;
      this.modifiers = other.modifiers;
      this.modifierKey = other.modifierKey;
      this.modifier = other.modifier;
   }

   @Nonnull
   public static EntityStatUpdate deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 21) {
         throw ProtocolException.bufferTooSmall("EntityStatUpdate", 21, buf.readableBytes() - offset);
      } else {
         EntityStatUpdate obj = new EntityStatUpdate();
         byte nullBits = buf.getByte(offset);
         obj.op = EntityStatOp.fromValue(buf.getByte(offset + 1));
         obj.predictable = buf.getByte(offset + 2) != 0;
         obj.value = buf.getFloatLE(offset + 3);
         if ((nullBits & 1) != 0) {
            obj.modifier = Modifier.deserialize(buf, offset + 7);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 13);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 21) {
               throw ProtocolException.invalidOffset("Modifiers", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 21 + varPosBase0;
            int modifiersCount = VarInt.peek(buf, varPos0);
            if (modifiersCount < 0) {
               throw ProtocolException.invalidVarInt("Modifiers");
            }

            int varIntLen = VarInt.size(modifiersCount);
            if (modifiersCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Modifiers", modifiersCount, 4096000);
            }

            obj.modifiers = new HashMap<>(modifiersCount);
            int dictPos = varPos0 + varIntLen;

            for (int i = 0; i < modifiersCount; i++) {
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
               Modifier val = Modifier.deserialize(buf, dictPos);
               dictPos += Modifier.computeBytesConsumed(buf, dictPos);
               if (obj.modifiers.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("modifiers", key);
               }
            }
         }

         if ((nullBits & 4) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 17);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 21) {
               throw ProtocolException.invalidOffset("ModifierKey", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 21 + varPosBase1;
            int modifierKeyLen = VarInt.peek(buf, varPos1);
            if (modifierKeyLen < 0) {
               throw ProtocolException.invalidVarInt("ModifierKey");
            }

            int modifierKeyVarIntLen = VarInt.size(modifierKeyLen);
            if (modifierKeyLen > 4096000) {
               throw ProtocolException.stringTooLong("ModifierKey", modifierKeyLen, 4096000);
            }

            if (varPos1 + modifierKeyVarIntLen + modifierKeyLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("ModifierKey", varPos1 + modifierKeyVarIntLen + modifierKeyLen, buf.readableBytes());
            }

            obj.modifierKey = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 21;
      if ((nullBits & 2) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 13);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 21) {
            throw ProtocolException.invalidOffset("Modifiers", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 21 + fieldOffset0;
         int dictLen = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            int sl = VarInt.peek(buf, pos0);
            pos0 += VarInt.size(sl) + sl;
            pos0 += Modifier.computeBytesConsumed(buf, pos0);
         }

         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 17);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 21) {
            throw ProtocolException.invalidOffset("ModifierKey", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 21 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(sl) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 21L;
   }

   public static EntityStatOp getOp(MemorySegment mem) {
      return getOp(mem, 0);
   }

   public static EntityStatOp getOp(MemorySegment mem, int offset) {
      return EntityStatOp.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1)));
   }

   public static boolean getPredictable(MemorySegment mem) {
      return getPredictable(mem, 0);
   }

   public static boolean getPredictable(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 2));
   }

   public static float getValue(MemorySegment mem) {
      return getValue(mem, 0);
   }

   public static float getValue(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 3));
   }

   @Nullable
   public static Map<String, Modifier> getModifiers(MemorySegment mem) {
      return getModifiers(mem, 0);
   }

   @Nullable
   public static Map<String, Modifier> getModifiers(MemorySegment mem, int offset) {
      if (!hasModifiers(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 13, 21, "Modifiers");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Modifiers", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Modifiers", len, 4096000);
         } else {
            Map<String, Modifier> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               long keyPacked = VarInt.getWithLength(mem, off);
               int nkey = (int)keyPacked + (int)(keyPacked >>> 32);
               String key = PacketIO.readVarString("key", mem, off, 16384000, PacketIO.UTF8);
               off += nkey;
               Modifier value = Modifier.toObject(mem, off);
               off += value.computeSize();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Modifiers", key);
               }
            }

            return data;
         }
      }
   }

   @Nullable
   public static String getModifierKey(MemorySegment mem) {
      return getModifierKey(mem, 0);
   }

   @Nullable
   public static String getModifierKey(MemorySegment mem, int offset) {
      return hasModifierKey(mem, offset)
         ? PacketIO.readVarString("ModifierKey", mem, offset + getValidatedOffset(mem, offset, 17, 21, "ModifierKey"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static Modifier getModifier(MemorySegment mem) {
      return getModifier(mem, 0);
   }

   @Nullable
   public static Modifier getModifier(MemorySegment mem, int offset) {
      return hasModifier(mem, offset) ? Modifier.toObject(mem, offset + 7) : null;
   }

   public static boolean hasModifier(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasModifiers(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasModifierKey(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static EntityStatUpdate toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static EntityStatUpdate toObject(MemorySegment mem, int offset) {
      if (offset + 21 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("EntityStatUpdate", offset + 21, (int)mem.byteSize());
      } else {
         Map<String, Modifier> modifiers = null;
         if (hasModifiers(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 13, 21, "Modifiers");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Modifiers", len);
            }

            if (len > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Modifiers", len, 4096000);
            }

            modifiers = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               long keyPacked = VarInt.getWithLength(mem, off);
               int nkey = (int)keyPacked + (int)(keyPacked >>> 32);
               String key = PacketIO.readVarString("key", mem, off, 16384000, PacketIO.UTF8);
               off += nkey;
               Modifier value = Modifier.toObject(mem, off);
               off += value.computeSize();
               if (modifiers.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Modifiers", key);
               }
            }
         }

         return new EntityStatUpdate(
            EntityStatOp.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 2)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 3)),
            modifiers,
            hasModifierKey(mem, offset)
               ? PacketIO.readVarString("ModifierKey", mem, offset + getValidatedOffset(mem, offset, 17, 21, "ModifierKey"), 4096000, PacketIO.UTF8)
               : null,
            hasModifier(mem, offset) ? Modifier.toObject(mem, offset + 7) : null
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.modifier != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.modifiers != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.modifierKey != null) {
         nullBits = (byte)(nullBits | 4);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.op.getValue());
      buf.writeByte(this.predictable ? 1 : 0);
      buf.writeFloatLE(this.value);
      if (this.modifier != null) {
         this.modifier.serialize(buf);
      } else {
         buf.writeZero(6);
      }

      int modifiersOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int modifierKeyOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.modifiers != null) {
         buf.setIntLE(modifiersOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.modifiers.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Modifiers", this.modifiers.size(), 4096000);
         }

         VarInt.write(buf, this.modifiers.size());

         for (Entry<String, Modifier> e : this.modifiers.entrySet()) {
            PacketIO.writeVarString(buf, e.getKey(), 4096000);
            e.getValue().serialize(buf);
         }
      } else {
         buf.setIntLE(modifiersOffsetSlot, -1);
      }

      if (this.modifierKey != null) {
         buf.setIntLE(modifierKeyOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.modifierKey, 4096000);
      } else {
         buf.setIntLE(modifierKeyOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.modifier != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.modifiers != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.modifierKey != null) {
         nullBits = (byte)(nullBits | 4);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.op.getValue());
      mem.set(PacketIO.PROTO_BOOL, offset + 2, this.predictable);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 3), this.value);
      if (this.modifier != null) {
         this.modifier.serialize(mem, offset + 7);
      } else {
         mem.asSlice(offset + 7, 6L).fill((byte)0);
      }

      int varOffset = offset + 21;
      if (this.modifiers != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 13), varOffset - offset - 21);
         if (this.modifiers.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Modifiers", this.modifiers.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.modifiers.size());

         for (Entry<String, Modifier> e : this.modifiers.entrySet()) {
            varOffset += PacketIO.writeVarString(mem, varOffset, e.getKey(), 16384000);
            varOffset += e.getValue().serialize(mem, varOffset);
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 13), -1);
      }

      if (this.modifierKey != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 17), varOffset - offset - 21);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.modifierKey, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 17), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 21;
      if (this.modifiers != null) {
         int modifiersSize = 0;

         for (Entry<String, Modifier> kvp : this.modifiers.entrySet()) {
            modifiersSize += PacketIO.stringSize(kvp.getKey()) + 6;
         }

         size += VarInt.size(this.modifiers.size()) + modifiersSize;
      }

      if (this.modifierKey != null) {
         size += PacketIO.stringSize(this.modifierKey);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 21) {
         return ValidationResult.error("Buffer too small: expected at least 21 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int v = buffer.getByte(offset + 1) & 255;
         if (v >= 9) {
            return ValidationResult.error("Invalid EntityStatOp value for Op");
         } else {
            if ((nullBits & 2) != 0) {
               v = buffer.getIntLE(offset + 13);
               if (v < 0 || v > buffer.writerIndex() - offset - 21) {
                  return ValidationResult.error("Invalid offset for Modifiers");
               }

               int pos = offset + 21 + v;
               int modifiersCount = VarInt.peek(buffer, pos);
               if (modifiersCount < 0) {
                  return ValidationResult.error("Invalid dictionary count for Modifiers");
               }

               if (modifiersCount > 4096000) {
                  return ValidationResult.error("Modifiers exceeds max length 4096000");
               }

               pos += VarInt.size(modifiersCount);

               for (int i = 0; i < modifiersCount; i++) {
                  int keyLen = VarInt.peek(buffer, pos);
                  if (keyLen < 0) {
                     return ValidationResult.error("Invalid string length for key");
                  }

                  if (keyLen > 4096000) {
                     return ValidationResult.error("key exceeds max length 4096000");
                  }

                  pos += VarInt.size(keyLen);
                  pos += keyLen;
                  if (pos > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading key");
                  }

                  pos += 6;
               }
            }

            if ((nullBits & 4) != 0) {
               v = buffer.getIntLE(offset + 17);
               if (v < 0 || v > buffer.writerIndex() - offset - 21) {
                  return ValidationResult.error("Invalid offset for ModifierKey");
               }

               int posx = offset + 21 + v;
               int modifierKeyLen = VarInt.peek(buffer, posx);
               if (modifierKeyLen < 0) {
                  return ValidationResult.error("Invalid string length for ModifierKey");
               }

               if (modifierKeyLen > 4096000) {
                  return ValidationResult.error("ModifierKey exceeds max length 4096000");
               }

               posx += VarInt.size(modifierKeyLen);
               posx += modifierKeyLen;
               if (posx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading ModifierKey");
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public EntityStatUpdate clone() {
      EntityStatUpdate copy = new EntityStatUpdate();
      copy.op = this.op;
      copy.predictable = this.predictable;
      copy.value = this.value;
      if (this.modifiers != null) {
         Map<String, Modifier> m = new HashMap<>();

         for (Entry<String, Modifier> e : this.modifiers.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.modifiers = m;
      }

      copy.modifierKey = this.modifierKey;
      copy.modifier = this.modifier != null ? this.modifier.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof EntityStatUpdate other)
            ? false
            : Objects.equals(this.op, other.op)
               && this.predictable == other.predictable
               && this.value == other.value
               && Objects.equals(this.modifiers, other.modifiers)
               && Objects.equals(this.modifierKey, other.modifierKey)
               && Objects.equals(this.modifier, other.modifier);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.op, this.predictable, this.value, this.modifiers, this.modifierKey, this.modifier);
   }
}
