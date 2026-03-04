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
      Cloud obj = new Cloud();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 13 + buf.getIntLE(offset + 1);
         int textureLen = VarInt.peek(buf, varPos0);
         if (textureLen < 0) {
            throw ProtocolException.negativeLength("Texture", textureLen);
         }

         if (textureLen > 4096000) {
            throw ProtocolException.stringTooLong("Texture", textureLen, 4096000);
         }

         obj.texture = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 13 + buf.getIntLE(offset + 5);
         int speedsCount = VarInt.peek(buf, varPos1);
         if (speedsCount < 0) {
            throw ProtocolException.negativeLength("Speeds", speedsCount);
         }

         if (speedsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Speeds", speedsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos1);
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
         int varPos2 = offset + 13 + buf.getIntLE(offset + 9);
         int colorsCount = VarInt.peek(buf, varPos2);
         if (colorsCount < 0) {
            throw ProtocolException.negativeLength("Colors", colorsCount);
         }

         if (colorsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Colors", colorsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos2);
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

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 13;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         int pos0 = offset + 13 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         int pos1 = offset + 13 + fieldOffset1;
         int dictLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1);

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
         int pos2 = offset + 13 + fieldOffset2;
         int dictLen = VarInt.peek(buf, pos2);
         pos2 += VarInt.length(buf, pos2);

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
            if (textureOffset < 0) {
               return ValidationResult.error("Invalid offset for Texture");
            }

            int pos = offset + 13 + textureOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Texture");
            }

            int textureLen = VarInt.peek(buffer, pos);
            if (textureLen < 0) {
               return ValidationResult.error("Invalid string length for Texture");
            }

            if (textureLen > 4096000) {
               return ValidationResult.error("Texture exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += textureLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Texture");
            }
         }

         if ((nullBits & 2) != 0) {
            int speedsOffset = buffer.getIntLE(offset + 5);
            if (speedsOffset < 0) {
               return ValidationResult.error("Invalid offset for Speeds");
            }

            int posx = offset + 13 + speedsOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Speeds");
            }

            int speedsCount = VarInt.peek(buffer, posx);
            if (speedsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for Speeds");
            }

            if (speedsCount > 4096000) {
               return ValidationResult.error("Speeds exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);

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
            if (colorsOffset < 0) {
               return ValidationResult.error("Invalid offset for Colors");
            }

            int posxx = offset + 13 + colorsOffset;
            if (posxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Colors");
            }

            int colorsCount = VarInt.peek(buffer, posxx);
            if (colorsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for Colors");
            }

            if (colorsCount > 4096000) {
               return ValidationResult.error("Colors exceeds max length 4096000");
            }

            posxx += VarInt.length(buffer, posxx);

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
