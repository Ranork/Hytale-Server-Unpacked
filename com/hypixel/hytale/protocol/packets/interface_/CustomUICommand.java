package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
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
      if (buf.readableBytes() - offset < 14) {
         throw ProtocolException.bufferTooSmall("CustomUICommand", 14, buf.readableBytes() - offset);
      } else {
         CustomUICommand obj = new CustomUICommand();
         byte nullBits = buf.getByte(offset);
         obj.type = CustomUICommandType.fromValue(buf.getByte(offset + 1));
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 2);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 14) {
               throw ProtocolException.invalidOffset("Selector", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 14 + varPosBase0;
            int selectorLen = VarInt.peek(buf, varPos0);
            if (selectorLen < 0) {
               throw ProtocolException.invalidVarInt("Selector");
            }

            int selectorVarIntLen = VarInt.size(selectorLen);
            if (selectorLen > 4096000) {
               throw ProtocolException.stringTooLong("Selector", selectorLen, 4096000);
            }

            if (varPos0 + selectorVarIntLen + selectorLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Selector", varPos0 + selectorVarIntLen + selectorLen, buf.readableBytes());
            }

            obj.selector = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 6);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 14) {
               throw ProtocolException.invalidOffset("Data", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 14 + varPosBase1;
            int dataLen = VarInt.peek(buf, varPos1);
            if (dataLen < 0) {
               throw ProtocolException.invalidVarInt("Data");
            }

            int dataVarIntLen = VarInt.size(dataLen);
            if (dataLen > 4096000) {
               throw ProtocolException.stringTooLong("Data", dataLen, 4096000);
            }

            if (varPos1 + dataVarIntLen + dataLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Data", varPos1 + dataVarIntLen + dataLen, buf.readableBytes());
            }

            obj.data = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
         }

         if ((nullBits & 4) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 10);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 14) {
               throw ProtocolException.invalidOffset("Text", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 14 + varPosBase2;
            int textLen = VarInt.peek(buf, varPos2);
            if (textLen < 0) {
               throw ProtocolException.invalidVarInt("Text");
            }

            int textVarIntLen = VarInt.size(textLen);
            if (textLen > 4096000) {
               throw ProtocolException.stringTooLong("Text", textLen, 4096000);
            }

            if (varPos2 + textVarIntLen + textLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Text", varPos2 + textVarIntLen + textLen, buf.readableBytes());
            }

            obj.text = PacketIO.readVarString(buf, varPos2, PacketIO.UTF8);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 14;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 2);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 14) {
            throw ProtocolException.invalidOffset("Selector", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 14 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 6);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 14) {
            throw ProtocolException.invalidOffset("Data", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 14 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(sl) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 10);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 14) {
            throw ProtocolException.invalidOffset("Text", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 14 + fieldOffset2;
         int sl = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(sl) + sl;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 14L;
   }

   public static CustomUICommandType getType(MemorySegment mem) {
      return getType(mem, 0);
   }

   public static CustomUICommandType getType(MemorySegment mem, int offset) {
      return CustomUICommandType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1)));
   }

   @Nullable
   public static String getSelector(MemorySegment mem) {
      return getSelector(mem, 0);
   }

   @Nullable
   public static String getSelector(MemorySegment mem, int offset) {
      return hasSelector(mem, offset)
         ? PacketIO.readVarString("Selector", mem, offset + getValidatedOffset(mem, offset, 2, 14, "Selector"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static String getData(MemorySegment mem) {
      return getData(mem, 0);
   }

   @Nullable
   public static String getData(MemorySegment mem, int offset) {
      return hasData(mem, offset) ? PacketIO.readVarString("Data", mem, offset + getValidatedOffset(mem, offset, 6, 14, "Data"), 4096000, PacketIO.UTF8) : null;
   }

   @Nullable
   public static String getText(MemorySegment mem) {
      return getText(mem, 0);
   }

   @Nullable
   public static String getText(MemorySegment mem, int offset) {
      return hasText(mem, offset)
         ? PacketIO.readVarString("Text", mem, offset + getValidatedOffset(mem, offset, 10, 14, "Text"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static boolean hasSelector(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasData(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasText(MemorySegment mem, int offset) {
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

   public static CustomUICommand toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static CustomUICommand toObject(MemorySegment mem, int offset) {
      if (offset + 14 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("CustomUICommand", offset + 14, (int)mem.byteSize());
      } else {
         return new CustomUICommand(
            CustomUICommandType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))),
            hasSelector(mem, offset)
               ? PacketIO.readVarString("Selector", mem, offset + getValidatedOffset(mem, offset, 2, 14, "Selector"), 4096000, PacketIO.UTF8)
               : null,
            hasData(mem, offset) ? PacketIO.readVarString("Data", mem, offset + getValidatedOffset(mem, offset, 6, 14, "Data"), 4096000, PacketIO.UTF8) : null,
            hasText(mem, offset) ? PacketIO.readVarString("Text", mem, offset + getValidatedOffset(mem, offset, 10, 14, "Text"), 4096000, PacketIO.UTF8) : null
         );
      }
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

   public int serialize(@Nonnull MemorySegment mem, int offset) {
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

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.type.getValue());
      int varOffset = offset + 14;
      if (this.selector != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 2), varOffset - offset - 14);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.selector, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 2), -1);
      }

      if (this.data != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 6), varOffset - offset - 14);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.data, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 6), -1);
      }

      if (this.text != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 10), varOffset - offset - 14);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.text, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 10), -1);
      }

      return varOffset - offset;
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
         int v = buffer.getByte(offset + 1) & 255;
         if (v >= 7) {
            return ValidationResult.error("Invalid CustomUICommandType value for Type");
         } else {
            if ((nullBits & 1) != 0) {
               v = buffer.getIntLE(offset + 2);
               if (v < 0 || v > buffer.writerIndex() - offset - 14) {
                  return ValidationResult.error("Invalid offset for Selector");
               }

               int pos = offset + 14 + v;
               int selectorLen = VarInt.peek(buffer, pos);
               if (selectorLen < 0) {
                  return ValidationResult.error("Invalid string length for Selector");
               }

               if (selectorLen > 4096000) {
                  return ValidationResult.error("Selector exceeds max length 4096000");
               }

               pos += VarInt.size(selectorLen);
               pos += selectorLen;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading Selector");
               }
            }

            if ((nullBits & 2) != 0) {
               v = buffer.getIntLE(offset + 6);
               if (v < 0 || v > buffer.writerIndex() - offset - 14) {
                  return ValidationResult.error("Invalid offset for Data");
               }

               int posx = offset + 14 + v;
               int dataLen = VarInt.peek(buffer, posx);
               if (dataLen < 0) {
                  return ValidationResult.error("Invalid string length for Data");
               }

               if (dataLen > 4096000) {
                  return ValidationResult.error("Data exceeds max length 4096000");
               }

               posx += VarInt.size(dataLen);
               posx += dataLen;
               if (posx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading Data");
               }
            }

            if ((nullBits & 4) != 0) {
               v = buffer.getIntLE(offset + 10);
               if (v < 0 || v > buffer.writerIndex() - offset - 14) {
                  return ValidationResult.error("Invalid offset for Text");
               }

               int posxx = offset + 14 + v;
               int textLen = VarInt.peek(buffer, posxx);
               if (textLen < 0) {
                  return ValidationResult.error("Invalid string length for Text");
               }

               if (textLen > 4096000) {
                  return ValidationResult.error("Text exceeds max length 4096000");
               }

               posxx += VarInt.size(textLen);
               posxx += textLen;
               if (posxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading Text");
               }
            }

            return ValidationResult.OK;
         }
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
