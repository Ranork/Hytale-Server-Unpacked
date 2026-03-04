package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FormattedMessage {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 6;
   public static final int VARIABLE_FIELD_COUNT = 7;
   public static final int VARIABLE_BLOCK_START = 34;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public String rawText;
   @Nullable
   public String messageId;
   @Nullable
   public FormattedMessage[] children;
   @Nullable
   public Map<String, ParamValue> params;
   @Nullable
   public Map<String, FormattedMessage> messageParams;
   @Nullable
   public String color;
   @Nonnull
   public MaybeBool bold = MaybeBool.Null;
   @Nonnull
   public MaybeBool italic = MaybeBool.Null;
   @Nonnull
   public MaybeBool monospace = MaybeBool.Null;
   @Nonnull
   public MaybeBool underlined = MaybeBool.Null;
   @Nullable
   public String link;
   public boolean markupEnabled;

   public FormattedMessage() {
   }

   public FormattedMessage(
      @Nullable String rawText,
      @Nullable String messageId,
      @Nullable FormattedMessage[] children,
      @Nullable Map<String, ParamValue> params,
      @Nullable Map<String, FormattedMessage> messageParams,
      @Nullable String color,
      @Nonnull MaybeBool bold,
      @Nonnull MaybeBool italic,
      @Nonnull MaybeBool monospace,
      @Nonnull MaybeBool underlined,
      @Nullable String link,
      boolean markupEnabled
   ) {
      this.rawText = rawText;
      this.messageId = messageId;
      this.children = children;
      this.params = params;
      this.messageParams = messageParams;
      this.color = color;
      this.bold = bold;
      this.italic = italic;
      this.monospace = monospace;
      this.underlined = underlined;
      this.link = link;
      this.markupEnabled = markupEnabled;
   }

   public FormattedMessage(@Nonnull FormattedMessage other) {
      this.rawText = other.rawText;
      this.messageId = other.messageId;
      this.children = other.children;
      this.params = other.params;
      this.messageParams = other.messageParams;
      this.color = other.color;
      this.bold = other.bold;
      this.italic = other.italic;
      this.monospace = other.monospace;
      this.underlined = other.underlined;
      this.link = other.link;
      this.markupEnabled = other.markupEnabled;
   }

   @Nonnull
   public static FormattedMessage deserialize(@Nonnull ByteBuf buf, int offset) {
      FormattedMessage obj = new FormattedMessage();
      byte nullBits = buf.getByte(offset);
      obj.bold = MaybeBool.fromValue(buf.getByte(offset + 1));
      obj.italic = MaybeBool.fromValue(buf.getByte(offset + 2));
      obj.monospace = MaybeBool.fromValue(buf.getByte(offset + 3));
      obj.underlined = MaybeBool.fromValue(buf.getByte(offset + 4));
      obj.markupEnabled = buf.getByte(offset + 5) != 0;
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 34 + buf.getIntLE(offset + 6);
         int rawTextLen = VarInt.peek(buf, varPos0);
         if (rawTextLen < 0) {
            throw ProtocolException.negativeLength("RawText", rawTextLen);
         }

         if (rawTextLen > 4096000) {
            throw ProtocolException.stringTooLong("RawText", rawTextLen, 4096000);
         }

         obj.rawText = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 34 + buf.getIntLE(offset + 10);
         int messageIdLen = VarInt.peek(buf, varPos1);
         if (messageIdLen < 0) {
            throw ProtocolException.negativeLength("MessageId", messageIdLen);
         }

         if (messageIdLen > 4096000) {
            throw ProtocolException.stringTooLong("MessageId", messageIdLen, 4096000);
         }

         obj.messageId = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
      }

      if ((nullBits & 4) != 0) {
         int varPos2 = offset + 34 + buf.getIntLE(offset + 14);
         int childrenCount = VarInt.peek(buf, varPos2);
         if (childrenCount < 0) {
            throw ProtocolException.negativeLength("Children", childrenCount);
         }

         if (childrenCount > 4096000) {
            throw ProtocolException.arrayTooLong("Children", childrenCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos2);
         if (varPos2 + varIntLen + childrenCount * 6L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Children", varPos2 + varIntLen + childrenCount * 6, buf.readableBytes());
         }

         obj.children = new FormattedMessage[childrenCount];
         int elemPos = varPos2 + varIntLen;

         for (int i = 0; i < childrenCount; i++) {
            obj.children[i] = deserialize(buf, elemPos);
            elemPos += computeBytesConsumed(buf, elemPos);
         }
      }

      if ((nullBits & 8) != 0) {
         int varPos3 = offset + 34 + buf.getIntLE(offset + 18);
         int paramsCount = VarInt.peek(buf, varPos3);
         if (paramsCount < 0) {
            throw ProtocolException.negativeLength("Params", paramsCount);
         }

         if (paramsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Params", paramsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos3);
         obj.params = new HashMap<>(paramsCount);
         int dictPos = varPos3 + varIntLen;

         for (int i = 0; i < paramsCount; i++) {
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
            ParamValue val = ParamValue.deserialize(buf, dictPos);
            dictPos += ParamValue.computeBytesConsumed(buf, dictPos);
            if (obj.params.put(key, val) != null) {
               throw ProtocolException.duplicateKey("params", key);
            }
         }
      }

      if ((nullBits & 16) != 0) {
         int varPos4 = offset + 34 + buf.getIntLE(offset + 22);
         int messageParamsCount = VarInt.peek(buf, varPos4);
         if (messageParamsCount < 0) {
            throw ProtocolException.negativeLength("MessageParams", messageParamsCount);
         }

         if (messageParamsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("MessageParams", messageParamsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos4);
         obj.messageParams = new HashMap<>(messageParamsCount);
         int dictPos = varPos4 + varIntLen;

         for (int i = 0; i < messageParamsCount; i++) {
            int keyLenx = VarInt.peek(buf, dictPos);
            if (keyLenx < 0) {
               throw ProtocolException.negativeLength("key", keyLenx);
            }

            if (keyLenx > 4096000) {
               throw ProtocolException.stringTooLong("key", keyLenx, 4096000);
            }

            int keyVarLen = VarInt.length(buf, dictPos);
            String key = PacketIO.readVarString(buf, dictPos);
            dictPos += keyVarLen + keyLenx;
            FormattedMessage val = deserialize(buf, dictPos);
            dictPos += computeBytesConsumed(buf, dictPos);
            if (obj.messageParams.put(key, val) != null) {
               throw ProtocolException.duplicateKey("messageParams", key);
            }
         }
      }

      if ((nullBits & 32) != 0) {
         int varPos5 = offset + 34 + buf.getIntLE(offset + 26);
         int colorLen = VarInt.peek(buf, varPos5);
         if (colorLen < 0) {
            throw ProtocolException.negativeLength("Color", colorLen);
         }

         if (colorLen > 4096000) {
            throw ProtocolException.stringTooLong("Color", colorLen, 4096000);
         }

         obj.color = PacketIO.readVarString(buf, varPos5, PacketIO.UTF8);
      }

      if ((nullBits & 64) != 0) {
         int varPos6 = offset + 34 + buf.getIntLE(offset + 30);
         int linkLen = VarInt.peek(buf, varPos6);
         if (linkLen < 0) {
            throw ProtocolException.negativeLength("Link", linkLen);
         }

         if (linkLen > 4096000) {
            throw ProtocolException.stringTooLong("Link", linkLen, 4096000);
         }

         obj.link = PacketIO.readVarString(buf, varPos6, PacketIO.UTF8);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 34;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 6);
         int pos0 = offset + 34 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 10);
         int pos1 = offset + 34 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 14);
         int pos2 = offset + 34 + fieldOffset2;
         int arrLen = VarInt.peek(buf, pos2);
         pos2 += VarInt.length(buf, pos2);

         for (int i = 0; i < arrLen; i++) {
            pos2 += computeBytesConsumed(buf, pos2);
         }

         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 18);
         int pos3 = offset + 34 + fieldOffset3;
         int dictLen = VarInt.peek(buf, pos3);
         pos3 += VarInt.length(buf, pos3);

         for (int i = 0; i < dictLen; i++) {
            int sl = VarInt.peek(buf, pos3);
            pos3 += VarInt.length(buf, pos3) + sl;
            pos3 += ParamValue.computeBytesConsumed(buf, pos3);
         }

         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      if ((nullBits & 16) != 0) {
         int fieldOffset4 = buf.getIntLE(offset + 22);
         int pos4 = offset + 34 + fieldOffset4;
         int dictLen = VarInt.peek(buf, pos4);
         pos4 += VarInt.length(buf, pos4);

         for (int i = 0; i < dictLen; i++) {
            int sl = VarInt.peek(buf, pos4);
            pos4 += VarInt.length(buf, pos4) + sl;
            pos4 += computeBytesConsumed(buf, pos4);
         }

         if (pos4 - offset > maxEnd) {
            maxEnd = pos4 - offset;
         }
      }

      if ((nullBits & 32) != 0) {
         int fieldOffset5 = buf.getIntLE(offset + 26);
         int pos5 = offset + 34 + fieldOffset5;
         int sl = VarInt.peek(buf, pos5);
         pos5 += VarInt.length(buf, pos5) + sl;
         if (pos5 - offset > maxEnd) {
            maxEnd = pos5 - offset;
         }
      }

      if ((nullBits & 64) != 0) {
         int fieldOffset6 = buf.getIntLE(offset + 30);
         int pos6 = offset + 34 + fieldOffset6;
         int sl = VarInt.peek(buf, pos6);
         pos6 += VarInt.length(buf, pos6) + sl;
         if (pos6 - offset > maxEnd) {
            maxEnd = pos6 - offset;
         }
      }

      return maxEnd;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.rawText != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.messageId != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.children != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.params != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.messageParams != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.color != null) {
         nullBits = (byte)(nullBits | 32);
      }

      if (this.link != null) {
         nullBits = (byte)(nullBits | 64);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.bold.getValue());
      buf.writeByte(this.italic.getValue());
      buf.writeByte(this.monospace.getValue());
      buf.writeByte(this.underlined.getValue());
      buf.writeByte(this.markupEnabled ? 1 : 0);
      int rawTextOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int messageIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int childrenOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int paramsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int messageParamsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int colorOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int linkOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.rawText != null) {
         buf.setIntLE(rawTextOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.rawText, 4096000);
      } else {
         buf.setIntLE(rawTextOffsetSlot, -1);
      }

      if (this.messageId != null) {
         buf.setIntLE(messageIdOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.messageId, 4096000);
      } else {
         buf.setIntLE(messageIdOffsetSlot, -1);
      }

      if (this.children != null) {
         buf.setIntLE(childrenOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.children.length > 4096000) {
            throw ProtocolException.arrayTooLong("Children", this.children.length, 4096000);
         }

         VarInt.write(buf, this.children.length);

         for (FormattedMessage item : this.children) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(childrenOffsetSlot, -1);
      }

      if (this.params != null) {
         buf.setIntLE(paramsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.params.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Params", this.params.size(), 4096000);
         }

         VarInt.write(buf, this.params.size());

         for (Entry<String, ParamValue> e : this.params.entrySet()) {
            PacketIO.writeVarString(buf, e.getKey(), 4096000);
            e.getValue().serializeWithTypeId(buf);
         }
      } else {
         buf.setIntLE(paramsOffsetSlot, -1);
      }

      if (this.messageParams != null) {
         buf.setIntLE(messageParamsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.messageParams.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("MessageParams", this.messageParams.size(), 4096000);
         }

         VarInt.write(buf, this.messageParams.size());

         for (Entry<String, FormattedMessage> e : this.messageParams.entrySet()) {
            PacketIO.writeVarString(buf, e.getKey(), 4096000);
            e.getValue().serialize(buf);
         }
      } else {
         buf.setIntLE(messageParamsOffsetSlot, -1);
      }

      if (this.color != null) {
         buf.setIntLE(colorOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.color, 4096000);
      } else {
         buf.setIntLE(colorOffsetSlot, -1);
      }

      if (this.link != null) {
         buf.setIntLE(linkOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.link, 4096000);
      } else {
         buf.setIntLE(linkOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 34;
      if (this.rawText != null) {
         size += PacketIO.stringSize(this.rawText);
      }

      if (this.messageId != null) {
         size += PacketIO.stringSize(this.messageId);
      }

      if (this.children != null) {
         int childrenSize = 0;

         for (FormattedMessage elem : this.children) {
            childrenSize += elem.computeSize();
         }

         size += VarInt.size(this.children.length) + childrenSize;
      }

      if (this.params != null) {
         int paramsSize = 0;

         for (Entry<String, ParamValue> kvp : this.params.entrySet()) {
            paramsSize += PacketIO.stringSize(kvp.getKey()) + kvp.getValue().computeSizeWithTypeId();
         }

         size += VarInt.size(this.params.size()) + paramsSize;
      }

      if (this.messageParams != null) {
         int messageParamsSize = 0;

         for (Entry<String, FormattedMessage> kvp : this.messageParams.entrySet()) {
            messageParamsSize += PacketIO.stringSize(kvp.getKey()) + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.messageParams.size()) + messageParamsSize;
      }

      if (this.color != null) {
         size += PacketIO.stringSize(this.color);
      }

      if (this.link != null) {
         size += PacketIO.stringSize(this.link);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 34) {
         return ValidationResult.error("Buffer too small: expected at least 34 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int rawTextOffset = buffer.getIntLE(offset + 6);
            if (rawTextOffset < 0) {
               return ValidationResult.error("Invalid offset for RawText");
            }

            int pos = offset + 34 + rawTextOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for RawText");
            }

            int rawTextLen = VarInt.peek(buffer, pos);
            if (rawTextLen < 0) {
               return ValidationResult.error("Invalid string length for RawText");
            }

            if (rawTextLen > 4096000) {
               return ValidationResult.error("RawText exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += rawTextLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading RawText");
            }
         }

         if ((nullBits & 2) != 0) {
            int messageIdOffset = buffer.getIntLE(offset + 10);
            if (messageIdOffset < 0) {
               return ValidationResult.error("Invalid offset for MessageId");
            }

            int posx = offset + 34 + messageIdOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for MessageId");
            }

            int messageIdLen = VarInt.peek(buffer, posx);
            if (messageIdLen < 0) {
               return ValidationResult.error("Invalid string length for MessageId");
            }

            if (messageIdLen > 4096000) {
               return ValidationResult.error("MessageId exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
            posx += messageIdLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading MessageId");
            }
         }

         if ((nullBits & 4) != 0) {
            int childrenOffset = buffer.getIntLE(offset + 14);
            if (childrenOffset < 0) {
               return ValidationResult.error("Invalid offset for Children");
            }

            int posxx = offset + 34 + childrenOffset;
            if (posxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Children");
            }

            int childrenCount = VarInt.peek(buffer, posxx);
            if (childrenCount < 0) {
               return ValidationResult.error("Invalid array count for Children");
            }

            if (childrenCount > 4096000) {
               return ValidationResult.error("Children exceeds max length 4096000");
            }

            posxx += VarInt.length(buffer, posxx);

            for (int i = 0; i < childrenCount; i++) {
               ValidationResult structResult = validateStructure(buffer, posxx);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid FormattedMessage in Children[" + i + "]: " + structResult.error());
               }

               posxx += computeBytesConsumed(buffer, posxx);
            }
         }

         if ((nullBits & 8) != 0) {
            int paramsOffset = buffer.getIntLE(offset + 18);
            if (paramsOffset < 0) {
               return ValidationResult.error("Invalid offset for Params");
            }

            int posxxx = offset + 34 + paramsOffset;
            if (posxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Params");
            }

            int paramsCount = VarInt.peek(buffer, posxxx);
            if (paramsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for Params");
            }

            if (paramsCount > 4096000) {
               return ValidationResult.error("Params exceeds max length 4096000");
            }

            posxxx += VarInt.length(buffer, posxxx);

            for (int i = 0; i < paramsCount; i++) {
               int keyLen = VarInt.peek(buffer, posxxx);
               if (keyLen < 0) {
                  return ValidationResult.error("Invalid string length for key");
               }

               if (keyLen > 4096000) {
                  return ValidationResult.error("key exceeds max length 4096000");
               }

               posxxx += VarInt.length(buffer, posxxx);
               posxxx += keyLen;
               if (posxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               posxxx += ParamValue.computeBytesConsumed(buffer, posxxx);
            }
         }

         if ((nullBits & 16) != 0) {
            int messageParamsOffset = buffer.getIntLE(offset + 22);
            if (messageParamsOffset < 0) {
               return ValidationResult.error("Invalid offset for MessageParams");
            }

            int posxxxx = offset + 34 + messageParamsOffset;
            if (posxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for MessageParams");
            }

            int messageParamsCount = VarInt.peek(buffer, posxxxx);
            if (messageParamsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for MessageParams");
            }

            if (messageParamsCount > 4096000) {
               return ValidationResult.error("MessageParams exceeds max length 4096000");
            }

            posxxxx += VarInt.length(buffer, posxxxx);

            for (int i = 0; i < messageParamsCount; i++) {
               int keyLenx = VarInt.peek(buffer, posxxxx);
               if (keyLenx < 0) {
                  return ValidationResult.error("Invalid string length for key");
               }

               if (keyLenx > 4096000) {
                  return ValidationResult.error("key exceeds max length 4096000");
               }

               posxxxx += VarInt.length(buffer, posxxxx);
               posxxxx += keyLenx;
               if (posxxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               posxxxx += computeBytesConsumed(buffer, posxxxx);
            }
         }

         if ((nullBits & 32) != 0) {
            int colorOffset = buffer.getIntLE(offset + 26);
            if (colorOffset < 0) {
               return ValidationResult.error("Invalid offset for Color");
            }

            int posxxxxx = offset + 34 + colorOffset;
            if (posxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Color");
            }

            int colorLen = VarInt.peek(buffer, posxxxxx);
            if (colorLen < 0) {
               return ValidationResult.error("Invalid string length for Color");
            }

            if (colorLen > 4096000) {
               return ValidationResult.error("Color exceeds max length 4096000");
            }

            posxxxxx += VarInt.length(buffer, posxxxxx);
            posxxxxx += colorLen;
            if (posxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Color");
            }
         }

         if ((nullBits & 64) != 0) {
            int linkOffset = buffer.getIntLE(offset + 30);
            if (linkOffset < 0) {
               return ValidationResult.error("Invalid offset for Link");
            }

            int posxxxxxx = offset + 34 + linkOffset;
            if (posxxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Link");
            }

            int linkLen = VarInt.peek(buffer, posxxxxxx);
            if (linkLen < 0) {
               return ValidationResult.error("Invalid string length for Link");
            }

            if (linkLen > 4096000) {
               return ValidationResult.error("Link exceeds max length 4096000");
            }

            posxxxxxx += VarInt.length(buffer, posxxxxxx);
            posxxxxxx += linkLen;
            if (posxxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Link");
            }
         }

         return ValidationResult.OK;
      }
   }

   public FormattedMessage clone() {
      FormattedMessage copy = new FormattedMessage();
      copy.rawText = this.rawText;
      copy.messageId = this.messageId;
      copy.children = this.children != null ? Arrays.stream(this.children).map(ex -> ex.clone()).toArray(FormattedMessage[]::new) : null;
      copy.params = this.params != null ? new HashMap<>(this.params) : null;
      if (this.messageParams != null) {
         Map<String, FormattedMessage> m = new HashMap<>();

         for (Entry<String, FormattedMessage> e : this.messageParams.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.messageParams = m;
      }

      copy.color = this.color;
      copy.bold = this.bold;
      copy.italic = this.italic;
      copy.monospace = this.monospace;
      copy.underlined = this.underlined;
      copy.link = this.link;
      copy.markupEnabled = this.markupEnabled;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof FormattedMessage other)
            ? false
            : Objects.equals(this.rawText, other.rawText)
               && Objects.equals(this.messageId, other.messageId)
               && Arrays.equals((Object[])this.children, (Object[])other.children)
               && Objects.equals(this.params, other.params)
               && Objects.equals(this.messageParams, other.messageParams)
               && Objects.equals(this.color, other.color)
               && Objects.equals(this.bold, other.bold)
               && Objects.equals(this.italic, other.italic)
               && Objects.equals(this.monospace, other.monospace)
               && Objects.equals(this.underlined, other.underlined)
               && Objects.equals(this.link, other.link)
               && this.markupEnabled == other.markupEnabled;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.rawText);
      result = 31 * result + Objects.hashCode(this.messageId);
      result = 31 * result + Arrays.hashCode((Object[])this.children);
      result = 31 * result + Objects.hashCode(this.params);
      result = 31 * result + Objects.hashCode(this.messageParams);
      result = 31 * result + Objects.hashCode(this.color);
      result = 31 * result + Objects.hashCode(this.bold);
      result = 31 * result + Objects.hashCode(this.italic);
      result = 31 * result + Objects.hashCode(this.monospace);
      result = 31 * result + Objects.hashCode(this.underlined);
      result = 31 * result + Objects.hashCode(this.link);
      return 31 * result + Boolean.hashCode(this.markupEnabled);
   }
}
