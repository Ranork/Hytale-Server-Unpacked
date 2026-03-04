package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolState;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemBuilderToolData {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public String[] ui;
   @Nullable
   public BuilderToolState[] tools;

   public ItemBuilderToolData() {
   }

   public ItemBuilderToolData(@Nullable String[] ui, @Nullable BuilderToolState[] tools) {
      this.ui = ui;
      this.tools = tools;
   }

   public ItemBuilderToolData(@Nonnull ItemBuilderToolData other) {
      this.ui = other.ui;
      this.tools = other.tools;
   }

   @Nonnull
   public static ItemBuilderToolData deserialize(@Nonnull ByteBuf buf, int offset) {
      ItemBuilderToolData obj = new ItemBuilderToolData();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 9 + buf.getIntLE(offset + 1);
         int uiCount = VarInt.peek(buf, varPos0);
         if (uiCount < 0) {
            throw ProtocolException.negativeLength("Ui", uiCount);
         }

         if (uiCount > 4096000) {
            throw ProtocolException.arrayTooLong("Ui", uiCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos0);
         if (varPos0 + varIntLen + uiCount * 1L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Ui", varPos0 + varIntLen + uiCount * 1, buf.readableBytes());
         }

         obj.ui = new String[uiCount];
         int elemPos = varPos0 + varIntLen;

         for (int i = 0; i < uiCount; i++) {
            int strLen = VarInt.peek(buf, elemPos);
            if (strLen < 0) {
               throw ProtocolException.negativeLength("ui[" + i + "]", strLen);
            }

            if (strLen > 4096000) {
               throw ProtocolException.stringTooLong("ui[" + i + "]", strLen, 4096000);
            }

            int strVarLen = VarInt.length(buf, elemPos);
            obj.ui[i] = PacketIO.readVarString(buf, elemPos);
            elemPos += strVarLen + strLen;
         }
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 9 + buf.getIntLE(offset + 5);
         int toolsCount = VarInt.peek(buf, varPos1);
         if (toolsCount < 0) {
            throw ProtocolException.negativeLength("Tools", toolsCount);
         }

         if (toolsCount > 4096000) {
            throw ProtocolException.arrayTooLong("Tools", toolsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos1);
         if (varPos1 + varIntLen + toolsCount * 2L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Tools", varPos1 + varIntLen + toolsCount * 2, buf.readableBytes());
         }

         obj.tools = new BuilderToolState[toolsCount];
         int elemPos = varPos1 + varIntLen;

         for (int i = 0; i < toolsCount; i++) {
            obj.tools[i] = BuilderToolState.deserialize(buf, elemPos);
            elemPos += BuilderToolState.computeBytesConsumed(buf, elemPos);
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 9;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         int pos0 = offset + 9 + fieldOffset0;
         int arrLen = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0);

         for (int i = 0; i < arrLen; i++) {
            int sl = VarInt.peek(buf, pos0);
            pos0 += VarInt.length(buf, pos0) + sl;
         }

         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         int pos1 = offset + 9 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1);

         for (int i = 0; i < arrLen; i++) {
            pos1 += BuilderToolState.computeBytesConsumed(buf, pos1);
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.ui != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.tools != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      int uiOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int toolsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.ui != null) {
         buf.setIntLE(uiOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.ui.length > 4096000) {
            throw ProtocolException.arrayTooLong("Ui", this.ui.length, 4096000);
         }

         VarInt.write(buf, this.ui.length);

         for (String item : this.ui) {
            PacketIO.writeVarString(buf, item, 4096000);
         }
      } else {
         buf.setIntLE(uiOffsetSlot, -1);
      }

      if (this.tools != null) {
         buf.setIntLE(toolsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.tools.length > 4096000) {
            throw ProtocolException.arrayTooLong("Tools", this.tools.length, 4096000);
         }

         VarInt.write(buf, this.tools.length);

         for (BuilderToolState item : this.tools) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(toolsOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 9;
      if (this.ui != null) {
         int uiSize = 0;

         for (String elem : this.ui) {
            uiSize += PacketIO.stringSize(elem);
         }

         size += VarInt.size(this.ui.length) + uiSize;
      }

      if (this.tools != null) {
         int toolsSize = 0;

         for (BuilderToolState elem : this.tools) {
            toolsSize += elem.computeSize();
         }

         size += VarInt.size(this.tools.length) + toolsSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 9) {
         return ValidationResult.error("Buffer too small: expected at least 9 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int uiOffset = buffer.getIntLE(offset + 1);
            if (uiOffset < 0) {
               return ValidationResult.error("Invalid offset for Ui");
            }

            int pos = offset + 9 + uiOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Ui");
            }

            int uiCount = VarInt.peek(buffer, pos);
            if (uiCount < 0) {
               return ValidationResult.error("Invalid array count for Ui");
            }

            if (uiCount > 4096000) {
               return ValidationResult.error("Ui exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for (int i = 0; i < uiCount; i++) {
               int strLen = VarInt.peek(buffer, pos);
               if (strLen < 0) {
                  return ValidationResult.error("Invalid string length in Ui");
               }

               pos += VarInt.length(buffer, pos);
               pos += strLen;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading string in Ui");
               }
            }
         }

         if ((nullBits & 2) != 0) {
            int toolsOffset = buffer.getIntLE(offset + 5);
            if (toolsOffset < 0) {
               return ValidationResult.error("Invalid offset for Tools");
            }

            int posx = offset + 9 + toolsOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Tools");
            }

            int toolsCount = VarInt.peek(buffer, posx);
            if (toolsCount < 0) {
               return ValidationResult.error("Invalid array count for Tools");
            }

            if (toolsCount > 4096000) {
               return ValidationResult.error("Tools exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);

            for (int i = 0; i < toolsCount; i++) {
               ValidationResult structResult = BuilderToolState.validateStructure(buffer, posx);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid BuilderToolState in Tools[" + i + "]: " + structResult.error());
               }

               posx += BuilderToolState.computeBytesConsumed(buffer, posx);
            }
         }

         return ValidationResult.OK;
      }
   }

   public ItemBuilderToolData clone() {
      ItemBuilderToolData copy = new ItemBuilderToolData();
      copy.ui = this.ui != null ? Arrays.copyOf(this.ui, this.ui.length) : null;
      copy.tools = this.tools != null ? Arrays.stream(this.tools).map(e -> e.clone()).toArray(BuilderToolState[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ItemBuilderToolData other)
            ? false
            : Arrays.equals((Object[])this.ui, (Object[])other.ui) && Arrays.equals((Object[])this.tools, (Object[])other.tools);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Arrays.hashCode((Object[])this.ui);
      return 31 * result + Arrays.hashCode((Object[])this.tools);
   }
}
