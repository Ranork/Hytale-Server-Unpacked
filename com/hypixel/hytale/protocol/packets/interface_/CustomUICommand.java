package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CustomUICommand {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 3;
   public static final int VARIABLE_BLOCK_START = 14;
   public static final int MAX_SIZE = 49152029;
   @Nonnull
   public CustomUICommandType type = CustomUICommandType.Append;
   @Nullable
   public String selector;
   @Nullable
   public String data;
   @Nullable
   public String text;

   public CustomUICommand() {
   }

   public CustomUICommand(@Nonnull CustomUICommandType type, @Nullable String selector, @Nullable String data, @Nullable String text) {
      this.type = type;
      this.selector = selector;
      this.data = data;
      this.text = text;
   }

   public CustomUICommand(@Nonnull CustomUICommand other) {
      this.type = other.type;
      this.selector = other.selector;
      this.data = other.data;
      this.text = other.text;
   }

   @Nonnull
   public static CustomUICommand deserialize(@Nonnull ByteBuf buf, int offset) {
      CustomUICommand obj = new CustomUICommand();
      byte nullBits = buf.getByte(offset);
      obj.type = CustomUICommandType.fromValue(buf.getByte(offset + 1));
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 14 + buf.getIntLE(offset + 2);
         int selectorLen = VarInt.peek(buf, varPos0);
         if (selectorLen < 0) {
            throw ProtocolException.negativeLength("Selector", selectorLen);
         }

         if (selectorLen > 4096000) {
            throw ProtocolException.stringTooLong("Selector", selectorLen, 4096000);
         }

         obj.selector = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 14 + buf.getIntLE(offset + 6);
         int dataLen = VarInt.peek(buf, varPos1);
         if (dataLen < 0) {
            throw ProtocolException.negativeLength("Data", dataLen);
         }

         if (dataLen > 4096000) {
            throw ProtocolException.stringTooLong("Data", dataLen, 4096000);
         }

         obj.data = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
      }

      if ((nullBits & 4) != 0) {
         int varPos2 = offset + 14 + buf.getIntLE(offset + 10);
         int textLen = VarInt.peek(buf, varPos2);
         if (textLen < 0) {
            throw ProtocolException.negativeLength("Text", textLen);
         }

         if (textLen > 4096000) {
            throw ProtocolException.stringTooLong("Text", textLen, 4096000);
         }

         obj.text = PacketIO.readVarString(buf, varPos2, PacketIO.UTF8);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 14;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 2);
         int pos0 = offset + 14 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 6);
         int pos1 = offset + 14 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 10);
         int pos2 = offset + 14 + fieldOffset2;
         int sl = VarInt.peek(buf, pos2);
         pos2 += VarInt.length(buf, pos2) + sl;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      return maxEnd;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.selector != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.data != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.text != null) {
         nullBits = (byte)(nullBits | 4);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      int selectorOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int dataOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int textOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.selector != null) {
         buf.setIntLE(selectorOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.selector, 4096000);
      } else {
         buf.setIntLE(selectorOffsetSlot, -1);
      }

      if (this.data != null) {
         buf.setIntLE(dataOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.data, 4096000);
      } else {
         buf.setIntLE(dataOffsetSlot, -1);
      }

      if (this.text != null) {
         buf.setIntLE(textOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.text, 4096000);
      } else {
         buf.setIntLE(textOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 14;
      if (this.selector != null) {
         size += PacketIO.stringSize(this.selector);
      }

      if (this.data != null) {
         size += PacketIO.stringSize(this.data);
      }

      if (this.text != null) {
         size += PacketIO.stringSize(this.text);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 14) {
         return ValidationResult.error("Buffer too small: expected at least 14 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int selectorOffset = buffer.getIntLE(offset + 2);
            if (selectorOffset < 0) {
               return ValidationResult.error("Invalid offset for Selector");
            }

            int pos = offset + 14 + selectorOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Selector");
            }

            int selectorLen = VarInt.peek(buffer, pos);
            if (selectorLen < 0) {
               return ValidationResult.error("Invalid string length for Selector");
            }

            if (selectorLen > 4096000) {
               return ValidationResult.error("Selector exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += selectorLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Selector");
            }
         }

         if ((nullBits & 2) != 0) {
            int dataOffset = buffer.getIntLE(offset + 6);
            if (dataOffset < 0) {
               return ValidationResult.error("Invalid offset for Data");
            }

            int posx = offset + 14 + dataOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Data");
            }

            int dataLen = VarInt.peek(buffer, posx);
            if (dataLen < 0) {
               return ValidationResult.error("Invalid string length for Data");
            }

            if (dataLen > 4096000) {
               return ValidationResult.error("Data exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
            posx += dataLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Data");
            }
         }

         if ((nullBits & 4) != 0) {
            int textOffset = buffer.getIntLE(offset + 10);
            if (textOffset < 0) {
               return ValidationResult.error("Invalid offset for Text");
            }

            int posxx = offset + 14 + textOffset;
            if (posxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Text");
            }

            int textLen = VarInt.peek(buffer, posxx);
            if (textLen < 0) {
               return ValidationResult.error("Invalid string length for Text");
            }

            if (textLen > 4096000) {
               return ValidationResult.error("Text exceeds max length 4096000");
            }

            posxx += VarInt.length(buffer, posxx);
            posxx += textLen;
            if (posxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Text");
            }
         }

         return ValidationResult.OK;
      }
   }

   public CustomUICommand clone() {
      CustomUICommand copy = new CustomUICommand();
      copy.type = this.type;
      copy.selector = this.selector;
      copy.data = this.data;
      copy.text = this.text;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof CustomUICommand other)
            ? false
            : Objects.equals(this.type, other.type)
               && Objects.equals(this.selector, other.selector)
               && Objects.equals(this.data, other.data)
               && Objects.equals(this.text, other.text);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.selector, this.data, this.text);
   }
}
