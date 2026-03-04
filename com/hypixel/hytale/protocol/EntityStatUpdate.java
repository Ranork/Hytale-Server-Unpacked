package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
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
      EntityStatUpdate obj = new EntityStatUpdate();
      byte nullBits = buf.getByte(offset);
      obj.op = EntityStatOp.fromValue(buf.getByte(offset + 1));
      obj.predictable = buf.getByte(offset + 2) != 0;
      obj.value = buf.getFloatLE(offset + 3);
      if ((nullBits & 1) != 0) {
         obj.modifier = Modifier.deserialize(buf, offset + 7);
      }

      if ((nullBits & 2) != 0) {
         int varPos0 = offset + 21 + buf.getIntLE(offset + 13);
         int modifiersCount = VarInt.peek(buf, varPos0);
         if (modifiersCount < 0) {
            throw ProtocolException.negativeLength("Modifiers", modifiersCount);
         }

         if (modifiersCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Modifiers", modifiersCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos0);
         obj.modifiers = new HashMap<>(modifiersCount);
         int dictPos = varPos0 + varIntLen;

         for (int i = 0; i < modifiersCount; i++) {
            int keyLen = VarInt.peek(buf, dictPos);
            if (keyLen < 0) {
               throw ProtocolException.negativeLength("key", keyLen);
            }

            if (keyLen > 4096000) {
               throw ProtocolException.stringTooLong("key", keyLen, 4096000);
            }

            int keyVarLen = VarInt.length(buf, dictPos);
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
         int varPos1 = offset + 21 + buf.getIntLE(offset + 17);
         int modifierKeyLen = VarInt.peek(buf, varPos1);
         if (modifierKeyLen < 0) {
            throw ProtocolException.negativeLength("ModifierKey", modifierKeyLen);
         }

         if (modifierKeyLen > 4096000) {
            throw ProtocolException.stringTooLong("ModifierKey", modifierKeyLen, 4096000);
         }

         obj.modifierKey = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 21;
      if ((nullBits & 2) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 13);
         int pos0 = offset + 21 + fieldOffset0;
         int dictLen = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0);

         for (int i = 0; i < dictLen; i++) {
            int sl = VarInt.peek(buf, pos0);
            pos0 += VarInt.length(buf, pos0) + sl;
            pos0 += Modifier.computeBytesConsumed(buf, pos0);
         }

         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 17);
         int pos1 = offset + 21 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
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
         if ((nullBits & 2) != 0) {
            int modifiersOffset = buffer.getIntLE(offset + 13);
            if (modifiersOffset < 0) {
               return ValidationResult.error("Invalid offset for Modifiers");
            }

            int pos = offset + 21 + modifiersOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Modifiers");
            }

            int modifiersCount = VarInt.peek(buffer, pos);
            if (modifiersCount < 0) {
               return ValidationResult.error("Invalid dictionary count for Modifiers");
            }

            if (modifiersCount > 4096000) {
               return ValidationResult.error("Modifiers exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for (int i = 0; i < modifiersCount; i++) {
               int keyLen = VarInt.peek(buffer, pos);
               if (keyLen < 0) {
                  return ValidationResult.error("Invalid string length for key");
               }

               if (keyLen > 4096000) {
                  return ValidationResult.error("key exceeds max length 4096000");
               }

               pos += VarInt.length(buffer, pos);
               pos += keyLen;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               pos += 6;
            }
         }

         if ((nullBits & 4) != 0) {
            int modifierKeyOffset = buffer.getIntLE(offset + 17);
            if (modifierKeyOffset < 0) {
               return ValidationResult.error("Invalid offset for ModifierKey");
            }

            int posx = offset + 21 + modifierKeyOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ModifierKey");
            }

            int modifierKeyLen = VarInt.peek(buffer, posx);
            if (modifierKeyLen < 0) {
               return ValidationResult.error("Invalid string length for ModifierKey");
            }

            if (modifierKeyLen > 4096000) {
               return ValidationResult.error("ModifierKey exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
            posx += modifierKeyLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading ModifierKey");
            }
         }

         return ValidationResult.OK;
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
