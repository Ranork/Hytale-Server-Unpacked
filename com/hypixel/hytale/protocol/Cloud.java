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

public class Cloud {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 3;
   public static final int VARIABLE_BLOCK_START = 13;
   public static final int MAX_SIZE = 81920028;
   @Nullable
   public String texture;
   @Nullable
   public Map<Float, Float> speeds;
   @Nullable
   public Map<Float, ColorAlpha> colors;

   public Cloud() {
   }

   public Cloud(@Nullable String texture, @Nullable Map<Float, Float> speeds, @Nullable Map<Float, ColorAlpha> colors) {
      this.texture = texture;
      this.speeds = speeds;
      this.colors = colors;
   }

   public Cloud(@Nonnull Cloud other) {
      this.texture = other.texture;
      this.speeds = other.speeds;
      this.colors = other.colors;
   }

   @Nonnull
   public static Cloud deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 13) {
         throw ProtocolException.bufferTooSmall("Cloud", 13, buf.readableBytes() - offset);
      } else {
         Cloud obj = new Cloud();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 1);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 13) {
               throw ProtocolException.invalidOffset("Texture", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 13 + varPosBase0;
            int textureLen = VarInt.peek(buf, varPos0);
            if (textureLen < 0) {
               throw ProtocolException.invalidVarInt("Texture");
            }

            int textureVarIntLen = VarInt.size(textureLen);
            if (textureLen > 4096000) {
               throw ProtocolException.stringTooLong("Texture", textureLen, 4096000);
            }

            if (varPos0 + textureVarIntLen + textureLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Texture", varPos0 + textureVarIntLen + textureLen, buf.readableBytes());
            }

            obj.texture = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 5);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 13) {
               throw ProtocolException.invalidOffset("Speeds", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 13 + varPosBase1;
            int speedsCount = VarInt.peek(buf, varPos1);
            if (speedsCount < 0) {
               throw ProtocolException.invalidVarInt("Speeds");
            }

            int varIntLen = VarInt.size(speedsCount);
            if (speedsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Speeds", speedsCount, 4096000);
            }

            obj.speeds = new HashMap<>(speedsCount);
            int dictPos = varPos1 + varIntLen;

            for (int i = 0; i < speedsCount; i++) {
               float key = buf.getFloatLE(dictPos);
               dictPos += 4;
               float val = buf.getFloatLE(dictPos);
               dictPos += 4;
               if (obj.speeds.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("speeds", key);
               }
            }
         }

         if ((nullBits & 4) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 9);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 13) {
               throw ProtocolException.invalidOffset("Colors", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 13 + varPosBase2;
            int colorsCount = VarInt.peek(buf, varPos2);
            if (colorsCount < 0) {
               throw ProtocolException.invalidVarInt("Colors");
            }

            int varIntLen = VarInt.size(colorsCount);
            if (colorsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Colors", colorsCount, 4096000);
            }

            obj.colors = new HashMap<>(colorsCount);
            int dictPos = varPos2 + varIntLen;

            for (int ix = 0; ix < colorsCount; ix++) {
               float key = buf.getFloatLE(dictPos);
               dictPos += 4;
               ColorAlpha val = ColorAlpha.deserialize(buf, dictPos);
               dictPos += ColorAlpha.computeBytesConsumed(buf, dictPos);
               if (obj.colors.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("colors", key);
               }
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 13;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 13) {
            throw ProtocolException.invalidOffset("Texture", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 13 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 13) {
            throw ProtocolException.invalidOffset("Speeds", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 13 + fieldOffset1;
         int dictLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos1 += 4;
            pos1 += 4;
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 9);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 13) {
            throw ProtocolException.invalidOffset("Colors", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 13 + fieldOffset2;
         int dictLen = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos2 += 4;
            pos2 += ColorAlpha.computeBytesConsumed(buf, pos2);
         }

         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 13L;
   }

   @Nullable
   public static String getTexture(MemorySegment mem) {
      return getTexture(mem, 0);
   }

   @Nullable
   public static String getTexture(MemorySegment mem, int offset) {
      return hasTexture(mem, offset)
         ? PacketIO.readVarString("Texture", mem, offset + getValidatedOffset(mem, offset, 1, 13, "Texture"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static Map<Float, Float> getSpeeds(MemorySegment mem) {
      return getSpeeds(mem, 0);
   }

   @Nullable
   public static Map<Float, Float> getSpeeds(MemorySegment mem, int offset) {
      if (!hasSpeeds(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 5, 13, "Speeds");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Speeds", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Speeds", len, 4096000);
         } else {
            Map<Float, Float> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               float key = mem.get(PacketIO.PROTO_FLOAT, (long)off);
               off += 4;
               float value = mem.get(PacketIO.PROTO_FLOAT, (long)off);
               off += 4;
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Speeds", key);
               }
            }

            return data;
         }
      }
   }

   @Nullable
   public static Map<Float, ColorAlpha> getColors(MemorySegment mem) {
      return getColors(mem, 0);
   }

   @Nullable
   public static Map<Float, ColorAlpha> getColors(MemorySegment mem, int offset) {
      if (!hasColors(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 9, 13, "Colors");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Colors", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Colors", len, 4096000);
         } else {
            Map<Float, ColorAlpha> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               float key = mem.get(PacketIO.PROTO_FLOAT, (long)off);
               off += 4;
               ColorAlpha value = ColorAlpha.toObject(mem, off);
               off += value.computeSize();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Colors", key);
               }
            }

            return data;
         }
      }
   }

   public static boolean hasTexture(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasSpeeds(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasColors(MemorySegment mem, int offset) {
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

   public static Cloud toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static Cloud toObject(MemorySegment mem, int offset) {
      if (offset + 13 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("Cloud", offset + 13, (int)mem.byteSize());
      } else {
         Map<Float, Float> speeds = null;
         if (hasSpeeds(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 5, 13, "Speeds");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Speeds", len);
            }

            if (len > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Speeds", len, 4096000);
            }

            speeds = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               float key = mem.get(PacketIO.PROTO_FLOAT, (long)off);
               off += 4;
               float value = mem.get(PacketIO.PROTO_FLOAT, (long)off);
               off += 4;
               if (speeds.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Speeds", key);
               }
            }
         }

         Map<Float, ColorAlpha> colors = null;
         if (hasColors(mem, offset)) {
            int offx = offset + getValidatedOffset(mem, offset, 9, 13, "Colors");
            long packedx = VarInt.getWithLength(mem, offx);
            int lenx = (int)packedx;
            if (lenx < 0) {
               throw ProtocolException.negativeLength("Colors", lenx);
            }

            if (lenx > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Colors", lenx, 4096000);
            }

            colors = new HashMap<>(lenx);
            offx += (int)(packedx >>> 32);

            for (int ix = 0; ix < lenx; ix++) {
               float key = mem.get(PacketIO.PROTO_FLOAT, (long)offx);
               offx += 4;
               ColorAlpha value = ColorAlpha.toObject(mem, offx);
               offx += value.computeSize();
               if (colors.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Colors", key);
               }
            }
         }

         return new Cloud(
            hasTexture(mem, offset)
               ? PacketIO.readVarString("Texture", mem, offset + getValidatedOffset(mem, offset, 1, 13, "Texture"), 4096000, PacketIO.UTF8)
               : null,
            speeds,
            colors
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.texture != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.speeds != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.colors != null) {
         nullBits = (byte)(nullBits | 4);
      }

      buf.writeByte(nullBits);
      int textureOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int speedsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int colorsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.texture != null) {
         buf.setIntLE(textureOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.texture, 4096000);
      } else {
         buf.setIntLE(textureOffsetSlot, -1);
      }

      if (this.speeds != null) {
         buf.setIntLE(speedsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.speeds.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Speeds", this.speeds.size(), 4096000);
         }

         VarInt.write(buf, this.speeds.size());

         for (Entry<Float, Float> e : this.speeds.entrySet()) {
            buf.writeFloatLE(e.getKey());
            buf.writeFloatLE(e.getValue());
         }
      } else {
         buf.setIntLE(speedsOffsetSlot, -1);
      }

      if (this.colors != null) {
         buf.setIntLE(colorsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.colors.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Colors", this.colors.size(), 4096000);
         }

         VarInt.write(buf, this.colors.size());

         for (Entry<Float, ColorAlpha> e : this.colors.entrySet()) {
            buf.writeFloatLE(e.getKey());
            e.getValue().serialize(buf);
         }
      } else {
         buf.setIntLE(colorsOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.texture != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.speeds != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.colors != null) {
         nullBits = (byte)(nullBits | 4);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 13;
      if (this.texture != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), varOffset - offset - 13);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.texture, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), -1);
      }

      if (this.speeds != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), varOffset - offset - 13);
         if (this.speeds.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Speeds", this.speeds.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.speeds.size());

         for (Entry<Float, Float> e : this.speeds.entrySet()) {
            mem.set(PacketIO.PROTO_FLOAT, (long)varOffset, e.getKey());
            varOffset += 4;
            mem.set(PacketIO.PROTO_FLOAT, (long)varOffset, e.getValue());
            varOffset += 4;
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), -1);
      }

      if (this.colors != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), varOffset - offset - 13);
         if (this.colors.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Colors", this.colors.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.colors.size());

         for (Entry<Float, ColorAlpha> e : this.colors.entrySet()) {
            mem.set(PacketIO.PROTO_FLOAT, (long)varOffset, e.getKey());
            varOffset += 4;
            varOffset += e.getValue().serialize(mem, varOffset);
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 13;
      if (this.texture != null) {
         size += PacketIO.stringSize(this.texture);
      }

      if (this.speeds != null) {
         size += VarInt.size(this.speeds.size()) + this.speeds.size() * 8;
      }

      if (this.colors != null) {
         size += VarInt.size(this.colors.size()) + this.colors.size() * 8;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 13) {
         return ValidationResult.error("Buffer too small: expected at least 13 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int textureOffset = buffer.getIntLE(offset + 1);
            if (textureOffset < 0 || textureOffset > buffer.writerIndex() - offset - 13) {
               return ValidationResult.error("Invalid offset for Texture");
            }

            int pos = offset + 13 + textureOffset;
            int textureLen = VarInt.peek(buffer, pos);
            if (textureLen < 0) {
               return ValidationResult.error("Invalid string length for Texture");
            }

            if (textureLen > 4096000) {
               return ValidationResult.error("Texture exceeds max length 4096000");
            }

            pos += VarInt.size(textureLen);
            pos += textureLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Texture");
            }
         }

         if ((nullBits & 2) != 0) {
            int speedsOffset = buffer.getIntLE(offset + 5);
            if (speedsOffset < 0 || speedsOffset > buffer.writerIndex() - offset - 13) {
               return ValidationResult.error("Invalid offset for Speeds");
            }

            int posx = offset + 13 + speedsOffset;
            int speedsCount = VarInt.peek(buffer, posx);
            if (speedsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for Speeds");
            }

            if (speedsCount > 4096000) {
               return ValidationResult.error("Speeds exceeds max length 4096000");
            }

            posx += VarInt.size(speedsCount);

            for (int i = 0; i < speedsCount; i++) {
               posx += 4;
               if (posx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               posx += 4;
               if (posx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading value");
               }
            }
         }

         if ((nullBits & 4) != 0) {
            int colorsOffset = buffer.getIntLE(offset + 9);
            if (colorsOffset < 0 || colorsOffset > buffer.writerIndex() - offset - 13) {
               return ValidationResult.error("Invalid offset for Colors");
            }

            int posxx = offset + 13 + colorsOffset;
            int colorsCount = VarInt.peek(buffer, posxx);
            if (colorsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for Colors");
            }

            if (colorsCount > 4096000) {
               return ValidationResult.error("Colors exceeds max length 4096000");
            }

            posxx += VarInt.size(colorsCount);

            for (int i = 0; i < colorsCount; i++) {
               posxx += 4;
               if (posxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               posxx += 4;
            }
         }

         return ValidationResult.OK;
      }
   }

   public Cloud clone() {
      Cloud copy = new Cloud();
      copy.texture = this.texture;
      copy.speeds = this.speeds != null ? new HashMap<>(this.speeds) : null;
      if (this.colors != null) {
         Map<Float, ColorAlpha> m = new HashMap<>();

         for (Entry<Float, ColorAlpha> e : this.colors.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.colors = m;
      }

      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof Cloud other)
            ? false
            : Objects.equals(this.texture, other.texture) && Objects.equals(this.speeds, other.speeds) && Objects.equals(this.colors, other.colors);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.texture, this.speeds, this.colors);
   }
}
