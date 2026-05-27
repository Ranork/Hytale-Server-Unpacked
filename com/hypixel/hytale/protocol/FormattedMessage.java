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

public class FormattedMessage {
   public static final int NULLABLE_BIT_FIELD_SIZE = 2;
   public static final int FIXED_BLOCK_SIZE = 7;
   public static final int VARIABLE_FIELD_COUNT = 8;
   public static final int VARIABLE_BLOCK_START = 39;
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
   @Nullable
   public Boolean bold;
   @Nullable
   public Boolean italic;
   @Nullable
   public Boolean monospace;
   @Nullable
   public Boolean underlined;
   @Nullable
   public String link;
   public boolean markupEnabled;
   @Nullable
   public FormattedMessageImage image;

   public FormattedMessage() {
   }

   public FormattedMessage(
      @Nullable String rawText,
      @Nullable String messageId,
      @Nullable FormattedMessage[] children,
      @Nullable Map<String, ParamValue> params,
      @Nullable Map<String, FormattedMessage> messageParams,
      @Nullable String color,
      @Nullable Boolean bold,
      @Nullable Boolean italic,
      @Nullable Boolean monospace,
      @Nullable Boolean underlined,
      @Nullable String link,
      boolean markupEnabled,
      @Nullable FormattedMessageImage image
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
      this.image = image;
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
      this.image = other.image;
   }

   @Nonnull
   public static FormattedMessage deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 39) {
         throw ProtocolException.bufferTooSmall("FormattedMessage", 39, buf.readableBytes() - offset);
      } else {
         FormattedMessage obj = new FormattedMessage();
         byte[] nullBits = PacketIO.readBytes(buf, offset, 2);
         if ((nullBits[0] & 1) != 0) {
            obj.bold = buf.getByte(offset + 2) != 0;
         }

         if ((nullBits[0] & 2) != 0) {
            obj.italic = buf.getByte(offset + 3) != 0;
         }

         if ((nullBits[0] & 4) != 0) {
            obj.monospace = buf.getByte(offset + 4) != 0;
         }

         if ((nullBits[0] & 8) != 0) {
            obj.underlined = buf.getByte(offset + 5) != 0;
         }

         obj.markupEnabled = buf.getByte(offset + 6) != 0;
         if ((nullBits[0] & 16) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 7);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 39) {
               throw ProtocolException.invalidOffset("RawText", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 39 + varPosBase0;
            int rawTextLen = VarInt.peek(buf, varPos0);
            if (rawTextLen < 0) {
               throw ProtocolException.invalidVarInt("RawText");
            }

            int rawTextVarIntLen = VarInt.size(rawTextLen);
            if (rawTextLen > 4096000) {
               throw ProtocolException.stringTooLong("RawText", rawTextLen, 4096000);
            }

            if (varPos0 + rawTextVarIntLen + rawTextLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("RawText", varPos0 + rawTextVarIntLen + rawTextLen, buf.readableBytes());
            }

            obj.rawText = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits[0] & 32) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 11);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 39) {
               throw ProtocolException.invalidOffset("MessageId", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 39 + varPosBase1;
            int messageIdLen = VarInt.peek(buf, varPos1);
            if (messageIdLen < 0) {
               throw ProtocolException.invalidVarInt("MessageId");
            }

            int messageIdVarIntLen = VarInt.size(messageIdLen);
            if (messageIdLen > 4096000) {
               throw ProtocolException.stringTooLong("MessageId", messageIdLen, 4096000);
            }

            if (varPos1 + messageIdVarIntLen + messageIdLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("MessageId", varPos1 + messageIdVarIntLen + messageIdLen, buf.readableBytes());
            }

            obj.messageId = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
         }

         if ((nullBits[0] & 64) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 15);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 39) {
               throw ProtocolException.invalidOffset("Children", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 39 + varPosBase2;
            int childrenCount = VarInt.peek(buf, varPos2);
            if (childrenCount < 0) {
               throw ProtocolException.invalidVarInt("Children");
            }

            int varIntLen = VarInt.size(childrenCount);
            if (childrenCount > 4096000) {
               throw ProtocolException.arrayTooLong("Children", childrenCount, 4096000);
            }

            if (varPos2 + varIntLen + childrenCount * 7L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Children", varPos2 + varIntLen + childrenCount * 7, buf.readableBytes());
            }

            obj.children = new FormattedMessage[childrenCount];
            int elemPos = varPos2 + varIntLen;

            for (int i = 0; i < childrenCount; i++) {
               obj.children[i] = deserialize(buf, elemPos);
               elemPos += computeBytesConsumed(buf, elemPos);
            }
         }

         if ((nullBits[0] & 128) != 0) {
            int varPosBase3 = buf.getIntLE(offset + 19);
            if (varPosBase3 < 0 || varPosBase3 > buf.writerIndex() - offset - 39) {
               throw ProtocolException.invalidOffset("Params", varPosBase3, buf.readableBytes());
            }

            int varPos3 = offset + 39 + varPosBase3;
            int paramsCount = VarInt.peek(buf, varPos3);
            if (paramsCount < 0) {
               throw ProtocolException.invalidVarInt("Params");
            }

            int varIntLenx = VarInt.size(paramsCount);
            if (paramsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Params", paramsCount, 4096000);
            }

            obj.params = new HashMap<>(paramsCount);
            int dictPos = varPos3 + varIntLenx;

            for (int i = 0; i < paramsCount; i++) {
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
               ParamValue val = ParamValue.deserialize(buf, dictPos);
               dictPos += ParamValue.computeBytesConsumed(buf, dictPos);
               if (obj.params.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("params", key);
               }
            }
         }

         if ((nullBits[1] & 1) != 0) {
            int varPosBase4 = buf.getIntLE(offset + 23);
            if (varPosBase4 < 0 || varPosBase4 > buf.writerIndex() - offset - 39) {
               throw ProtocolException.invalidOffset("MessageParams", varPosBase4, buf.readableBytes());
            }

            int varPos4 = offset + 39 + varPosBase4;
            int messageParamsCount = VarInt.peek(buf, varPos4);
            if (messageParamsCount < 0) {
               throw ProtocolException.invalidVarInt("MessageParams");
            }

            int varIntLenx = VarInt.size(messageParamsCount);
            if (messageParamsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("MessageParams", messageParamsCount, 4096000);
            }

            obj.messageParams = new HashMap<>(messageParamsCount);
            int dictPos = varPos4 + varIntLenx;

            for (int i = 0; i < messageParamsCount; i++) {
               int keyLenx = VarInt.peek(buf, dictPos);
               if (keyLenx < 0) {
                  throw ProtocolException.invalidVarInt("key");
               }

               int keyVarLenx = VarInt.size(keyLenx);
               if (keyLenx > 4096000) {
                  throw ProtocolException.stringTooLong("key", keyLenx, 4096000);
               }

               if (dictPos + keyVarLenx + keyLenx > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("key", dictPos + keyVarLenx + keyLenx, buf.readableBytes());
               }

               String key = PacketIO.readVarString(buf, dictPos);
               dictPos += keyVarLenx + keyLenx;
               FormattedMessage val = deserialize(buf, dictPos);
               dictPos += computeBytesConsumed(buf, dictPos);
               if (obj.messageParams.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("messageParams", key);
               }
            }
         }

         if ((nullBits[1] & 2) != 0) {
            int varPosBase5 = buf.getIntLE(offset + 27);
            if (varPosBase5 < 0 || varPosBase5 > buf.writerIndex() - offset - 39) {
               throw ProtocolException.invalidOffset("Color", varPosBase5, buf.readableBytes());
            }

            int varPos5 = offset + 39 + varPosBase5;
            int colorLen = VarInt.peek(buf, varPos5);
            if (colorLen < 0) {
               throw ProtocolException.invalidVarInt("Color");
            }

            int colorVarIntLen = VarInt.size(colorLen);
            if (colorLen > 4096000) {
               throw ProtocolException.stringTooLong("Color", colorLen, 4096000);
            }

            if (varPos5 + colorVarIntLen + colorLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Color", varPos5 + colorVarIntLen + colorLen, buf.readableBytes());
            }

            obj.color = PacketIO.readVarString(buf, varPos5, PacketIO.UTF8);
         }

         if ((nullBits[1] & 4) != 0) {
            int varPosBase6 = buf.getIntLE(offset + 31);
            if (varPosBase6 < 0 || varPosBase6 > buf.writerIndex() - offset - 39) {
               throw ProtocolException.invalidOffset("Link", varPosBase6, buf.readableBytes());
            }

            int varPos6 = offset + 39 + varPosBase6;
            int linkLen = VarInt.peek(buf, varPos6);
            if (linkLen < 0) {
               throw ProtocolException.invalidVarInt("Link");
            }

            int linkVarIntLen = VarInt.size(linkLen);
            if (linkLen > 4096000) {
               throw ProtocolException.stringTooLong("Link", linkLen, 4096000);
            }

            if (varPos6 + linkVarIntLen + linkLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Link", varPos6 + linkVarIntLen + linkLen, buf.readableBytes());
            }

            obj.link = PacketIO.readVarString(buf, varPos6, PacketIO.UTF8);
         }

         if ((nullBits[1] & 8) != 0) {
            int varPosBase7 = buf.getIntLE(offset + 35);
            if (varPosBase7 < 0 || varPosBase7 > buf.writerIndex() - offset - 39) {
               throw ProtocolException.invalidOffset("Image", varPosBase7, buf.readableBytes());
            }

            int varPos7 = offset + 39 + varPosBase7;
            obj.image = FormattedMessageImage.deserialize(buf, varPos7);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte[] nullBits = PacketIO.readBytes(buf, offset, 2);
      int maxEnd = 39;
      if ((nullBits[0] & 16) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 7);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 39) {
            throw ProtocolException.invalidOffset("RawText", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 39 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits[0] & 32) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 11);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 39) {
            throw ProtocolException.invalidOffset("MessageId", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 39 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(sl) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits[0] & 64) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 15);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 39) {
            throw ProtocolException.invalidOffset("Children", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 39 + fieldOffset2;
         int arrLen = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos2 += computeBytesConsumed(buf, pos2);
         }

         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits[0] & 128) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 19);
         if (fieldOffset3 < 0 || fieldOffset3 > buf.writerIndex() - offset - 39) {
            throw ProtocolException.invalidOffset("Params", fieldOffset3, maxEnd);
         }

         int pos3 = offset + 39 + fieldOffset3;
         int dictLen = VarInt.peek(buf, pos3);
         pos3 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            int sl = VarInt.peek(buf, pos3);
            pos3 += VarInt.size(sl) + sl;
            pos3 += ParamValue.computeBytesConsumed(buf, pos3);
         }

         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      if ((nullBits[1] & 1) != 0) {
         int fieldOffset4 = buf.getIntLE(offset + 23);
         if (fieldOffset4 < 0 || fieldOffset4 > buf.writerIndex() - offset - 39) {
            throw ProtocolException.invalidOffset("MessageParams", fieldOffset4, maxEnd);
         }

         int pos4 = offset + 39 + fieldOffset4;
         int dictLen = VarInt.peek(buf, pos4);
         pos4 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            int sl = VarInt.peek(buf, pos4);
            pos4 += VarInt.size(sl) + sl;
            pos4 += computeBytesConsumed(buf, pos4);
         }

         if (pos4 - offset > maxEnd) {
            maxEnd = pos4 - offset;
         }
      }

      if ((nullBits[1] & 2) != 0) {
         int fieldOffset5 = buf.getIntLE(offset + 27);
         if (fieldOffset5 < 0 || fieldOffset5 > buf.writerIndex() - offset - 39) {
            throw ProtocolException.invalidOffset("Color", fieldOffset5, maxEnd);
         }

         int pos5 = offset + 39 + fieldOffset5;
         int sl = VarInt.peek(buf, pos5);
         pos5 += VarInt.size(sl) + sl;
         if (pos5 - offset > maxEnd) {
            maxEnd = pos5 - offset;
         }
      }

      if ((nullBits[1] & 4) != 0) {
         int fieldOffset6 = buf.getIntLE(offset + 31);
         if (fieldOffset6 < 0 || fieldOffset6 > buf.writerIndex() - offset - 39) {
            throw ProtocolException.invalidOffset("Link", fieldOffset6, maxEnd);
         }

         int pos6 = offset + 39 + fieldOffset6;
         int sl = VarInt.peek(buf, pos6);
         pos6 += VarInt.size(sl) + sl;
         if (pos6 - offset > maxEnd) {
            maxEnd = pos6 - offset;
         }
      }

      if ((nullBits[1] & 8) != 0) {
         int fieldOffset7 = buf.getIntLE(offset + 35);
         if (fieldOffset7 < 0 || fieldOffset7 > buf.writerIndex() - offset - 39) {
            throw ProtocolException.invalidOffset("Image", fieldOffset7, maxEnd);
         }

         int pos7 = offset + 39 + fieldOffset7;
         pos7 += FormattedMessageImage.computeBytesConsumed(buf, pos7);
         if (pos7 - offset > maxEnd) {
            maxEnd = pos7 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 39L;
   }

   @Nullable
   public static String getRawText(MemorySegment mem) {
      return getRawText(mem, 0);
   }

   @Nullable
   public static String getRawText(MemorySegment mem, int offset) {
      return hasRawText(mem, offset)
         ? PacketIO.readVarString("RawText", mem, offset + getValidatedOffset(mem, offset, 7, 39, "RawText"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static String getMessageId(MemorySegment mem) {
      return getMessageId(mem, 0);
   }

   @Nullable
   public static String getMessageId(MemorySegment mem, int offset) {
      return hasMessageId(mem, offset)
         ? PacketIO.readVarString("MessageId", mem, offset + getValidatedOffset(mem, offset, 11, 39, "MessageId"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static FormattedMessage[] getChildren(MemorySegment mem) {
      return getChildren(mem, 0);
   }

   @Nullable
   public static FormattedMessage[] getChildren(MemorySegment mem, int offset) {
      if (!hasChildren(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 15, 39, "Children");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Children", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Children", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Children", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               FormattedMessage[] data = new FormattedMessage[len];

               for (int i = 0; i < len; i++) {
                  data[i] = toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   @Nullable
   public static Map<String, ParamValue> getParams(MemorySegment mem) {
      return getParams(mem, 0);
   }

   @Nullable
   public static Map<String, ParamValue> getParams(MemorySegment mem, int offset) {
      if (!hasParams(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 19, 39, "Params");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Params", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Params", len, 4096000);
         } else {
            Map<String, ParamValue> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               long keyPacked = VarInt.getWithLength(mem, off);
               int nkey = (int)keyPacked + (int)(keyPacked >>> 32);
               String key = PacketIO.readVarString("key", mem, off, 16384000, PacketIO.UTF8);
               off += nkey;
               ParamValue value = ParamValue.toObject(mem, off);
               off += value.computeSizeWithTypeId();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Params", key);
               }
            }

            return data;
         }
      }
   }

   @Nullable
   public static Map<String, FormattedMessage> getMessageParams(MemorySegment mem) {
      return getMessageParams(mem, 0);
   }

   @Nullable
   public static Map<String, FormattedMessage> getMessageParams(MemorySegment mem, int offset) {
      if (!hasMessageParams(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 23, 39, "MessageParams");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("MessageParams", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("MessageParams", len, 4096000);
         } else {
            Map<String, FormattedMessage> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               long keyPacked = VarInt.getWithLength(mem, off);
               int nkey = (int)keyPacked + (int)(keyPacked >>> 32);
               String key = PacketIO.readVarString("key", mem, off, 16384000, PacketIO.UTF8);
               off += nkey;
               FormattedMessage value = toObject(mem, off);
               off += value.computeSize();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("MessageParams", key);
               }
            }

            return data;
         }
      }
   }

   @Nullable
   public static String getColor(MemorySegment mem) {
      return getColor(mem, 0);
   }

   @Nullable
   public static String getColor(MemorySegment mem, int offset) {
      return hasColor(mem, offset)
         ? PacketIO.readVarString("Color", mem, offset + getValidatedOffset(mem, offset, 27, 39, "Color"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static Boolean getBold(MemorySegment mem) {
      return getBold(mem, 0);
   }

   @Nullable
   public static Boolean getBold(MemorySegment mem, int offset) {
      return hasBold(mem, offset) ? mem.get(PacketIO.PROTO_BOOL, (long)(offset + 2)) : null;
   }

   @Nullable
   public static Boolean getItalic(MemorySegment mem) {
      return getItalic(mem, 0);
   }

   @Nullable
   public static Boolean getItalic(MemorySegment mem, int offset) {
      return hasItalic(mem, offset) ? mem.get(PacketIO.PROTO_BOOL, (long)(offset + 3)) : null;
   }

   @Nullable
   public static Boolean getMonospace(MemorySegment mem) {
      return getMonospace(mem, 0);
   }

   @Nullable
   public static Boolean getMonospace(MemorySegment mem, int offset) {
      return hasMonospace(mem, offset) ? mem.get(PacketIO.PROTO_BOOL, (long)(offset + 4)) : null;
   }

   @Nullable
   public static Boolean getUnderlined(MemorySegment mem) {
      return getUnderlined(mem, 0);
   }

   @Nullable
   public static Boolean getUnderlined(MemorySegment mem, int offset) {
      return hasUnderlined(mem, offset) ? mem.get(PacketIO.PROTO_BOOL, (long)(offset + 5)) : null;
   }

   @Nullable
   public static String getLink(MemorySegment mem) {
      return getLink(mem, 0);
   }

   @Nullable
   public static String getLink(MemorySegment mem, int offset) {
      return hasLink(mem, offset)
         ? PacketIO.readVarString("Link", mem, offset + getValidatedOffset(mem, offset, 31, 39, "Link"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static boolean getMarkupEnabled(MemorySegment mem) {
      return getMarkupEnabled(mem, 0);
   }

   public static boolean getMarkupEnabled(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 6));
   }

   @Nullable
   public static FormattedMessageImage getImage(MemorySegment mem) {
      return getImage(mem, 0);
   }

   @Nullable
   public static FormattedMessageImage getImage(MemorySegment mem, int offset) {
      return hasImage(mem, offset) ? FormattedMessageImage.toObject(mem, offset + getValidatedOffset(mem, offset, 35, 39, "Image")) : null;
   }

   public static boolean hasBold(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasItalic(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasMonospace(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasUnderlined(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   public static boolean hasRawText(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 16) != 0;
   }

   public static boolean hasMessageId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 32) != 0;
   }

   public static boolean hasChildren(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 64) != 0;
   }

   public static boolean hasParams(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 128) != 0;
   }

   public static boolean hasMessageParams(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 1) != 0;
   }

   public static boolean hasColor(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 2) != 0;
   }

   public static boolean hasLink(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 4) != 0;
   }

   public static boolean hasImage(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
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

   public static FormattedMessage toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static FormattedMessage toObject(MemorySegment mem, int offset) {
      if (offset + 39 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("FormattedMessage", offset + 39, (int)mem.byteSize());
      } else {
         FormattedMessage[] children = null;
         if (hasChildren(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 15, 39, "Children");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Children", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Children", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Children", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            children = new FormattedMessage[len];

            for (int i = 0; i < len; i++) {
               children[i] = toObject(mem, off);
               off += children[i].computeSize();
            }
         }

         Map<String, ParamValue> params = null;
         if (hasParams(mem, offset)) {
            int offx = offset + getValidatedOffset(mem, offset, 19, 39, "Params");
            long packedx = VarInt.getWithLength(mem, offx);
            int lenx = (int)packedx;
            if (lenx < 0) {
               throw ProtocolException.negativeLength("Params", lenx);
            }

            if (lenx > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Params", lenx, 4096000);
            }

            params = new HashMap<>(lenx);
            offx += (int)(packedx >>> 32);

            for (int i = 0; i < lenx; i++) {
               long keyPacked = VarInt.getWithLength(mem, offx);
               int nkey = (int)keyPacked + (int)(keyPacked >>> 32);
               String key = PacketIO.readVarString("key", mem, offx, 16384000, PacketIO.UTF8);
               offx += nkey;
               ParamValue value = ParamValue.toObject(mem, offx);
               offx += value.computeSizeWithTypeId();
               if (params.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Params", key);
               }
            }
         }

         Map<String, FormattedMessage> messageParams = null;
         if (hasMessageParams(mem, offset)) {
            int offxx = offset + getValidatedOffset(mem, offset, 23, 39, "MessageParams");
            long packedxx = VarInt.getWithLength(mem, offxx);
            int lenxx = (int)packedxx;
            if (lenxx < 0) {
               throw ProtocolException.negativeLength("MessageParams", lenxx);
            }

            if (lenxx > 4096000) {
               throw ProtocolException.dictionaryTooLarge("MessageParams", lenxx, 4096000);
            }

            messageParams = new HashMap<>(lenxx);
            offxx += (int)(packedxx >>> 32);

            for (int ix = 0; ix < lenxx; ix++) {
               long keyPacked = VarInt.getWithLength(mem, offxx);
               int nkey = (int)keyPacked + (int)(keyPacked >>> 32);
               String key = PacketIO.readVarString("key", mem, offxx, 16384000, PacketIO.UTF8);
               offxx += nkey;
               FormattedMessage value = toObject(mem, offxx);
               offxx += value.computeSize();
               if (messageParams.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("MessageParams", key);
               }
            }
         }

         return new FormattedMessage(
            hasRawText(mem, offset)
               ? PacketIO.readVarString("RawText", mem, offset + getValidatedOffset(mem, offset, 7, 39, "RawText"), 4096000, PacketIO.UTF8)
               : null,
            hasMessageId(mem, offset)
               ? PacketIO.readVarString("MessageId", mem, offset + getValidatedOffset(mem, offset, 11, 39, "MessageId"), 4096000, PacketIO.UTF8)
               : null,
            children,
            params,
            messageParams,
            hasColor(mem, offset)
               ? PacketIO.readVarString("Color", mem, offset + getValidatedOffset(mem, offset, 27, 39, "Color"), 4096000, PacketIO.UTF8)
               : null,
            hasBold(mem, offset) ? mem.get(PacketIO.PROTO_BOOL, (long)(offset + 2)) : null,
            hasItalic(mem, offset) ? mem.get(PacketIO.PROTO_BOOL, (long)(offset + 3)) : null,
            hasMonospace(mem, offset) ? mem.get(PacketIO.PROTO_BOOL, (long)(offset + 4)) : null,
            hasUnderlined(mem, offset) ? mem.get(PacketIO.PROTO_BOOL, (long)(offset + 5)) : null,
            hasLink(mem, offset) ? PacketIO.readVarString("Link", mem, offset + getValidatedOffset(mem, offset, 31, 39, "Link"), 4096000, PacketIO.UTF8) : null,
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 6)),
            hasImage(mem, offset) ? FormattedMessageImage.toObject(mem, offset + getValidatedOffset(mem, offset, 35, 39, "Image")) : null
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte[] nullBits = new byte[2];
      if (this.bold != null) {
         nullBits[0] = (byte)(nullBits[0] | 1);
      }

      if (this.italic != null) {
         nullBits[0] = (byte)(nullBits[0] | 2);
      }

      if (this.monospace != null) {
         nullBits[0] = (byte)(nullBits[0] | 4);
      }

      if (this.underlined != null) {
         nullBits[0] = (byte)(nullBits[0] | 8);
      }

      if (this.rawText != null) {
         nullBits[0] = (byte)(nullBits[0] | 16);
      }

      if (this.messageId != null) {
         nullBits[0] = (byte)(nullBits[0] | 32);
      }

      if (this.children != null) {
         nullBits[0] = (byte)(nullBits[0] | 64);
      }

      if (this.params != null) {
         nullBits[0] = (byte)(nullBits[0] | 128);
      }

      if (this.messageParams != null) {
         nullBits[1] = (byte)(nullBits[1] | 1);
      }

      if (this.color != null) {
         nullBits[1] = (byte)(nullBits[1] | 2);
      }

      if (this.link != null) {
         nullBits[1] = (byte)(nullBits[1] | 4);
      }

      if (this.image != null) {
         nullBits[1] = (byte)(nullBits[1] | 8);
      }

      buf.writeBytes(nullBits);
      if (this.bold != null) {
         buf.writeByte(this.bold ? 1 : 0);
      } else {
         buf.writeZero(1);
      }

      if (this.italic != null) {
         buf.writeByte(this.italic ? 1 : 0);
      } else {
         buf.writeZero(1);
      }

      if (this.monospace != null) {
         buf.writeByte(this.monospace ? 1 : 0);
      } else {
         buf.writeZero(1);
      }

      if (this.underlined != null) {
         buf.writeByte(this.underlined ? 1 : 0);
      } else {
         buf.writeZero(1);
      }

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
      int imageOffsetSlot = buf.writerIndex();
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

      if (this.image != null) {
         buf.setIntLE(imageOffsetSlot, buf.writerIndex() - varBlockStart);
         this.image.serialize(buf);
      } else {
         buf.setIntLE(imageOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.bold != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.italic != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.monospace != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.underlined != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.rawText != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.messageId != null) {
         nullBits = (byte)(nullBits | 32);
      }

      if (this.children != null) {
         nullBits = (byte)(nullBits | 64);
      }

      if (this.params != null) {
         nullBits = (byte)(nullBits | 128);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      nullBits = 0;
      if (this.messageParams != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.color != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.link != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.image != null) {
         nullBits = (byte)(nullBits | 8);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), nullBits);
      if (this.bold != null) {
         mem.set(PacketIO.PROTO_BOOL, offset + 2, this.bold);
      } else {
         mem.asSlice(offset + 2, 1L).fill((byte)0);
      }

      if (this.italic != null) {
         mem.set(PacketIO.PROTO_BOOL, offset + 3, this.italic);
      } else {
         mem.asSlice(offset + 3, 1L).fill((byte)0);
      }

      if (this.monospace != null) {
         mem.set(PacketIO.PROTO_BOOL, offset + 4, this.monospace);
      } else {
         mem.asSlice(offset + 4, 1L).fill((byte)0);
      }

      if (this.underlined != null) {
         mem.set(PacketIO.PROTO_BOOL, offset + 5, this.underlined);
      } else {
         mem.asSlice(offset + 5, 1L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_BOOL, offset + 6, this.markupEnabled);
      int varOffset = offset + 39;
      if (this.rawText != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 7), varOffset - offset - 39);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.rawText, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 7), -1);
      }

      if (this.messageId != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 11), varOffset - offset - 39);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.messageId, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 11), -1);
      }

      if (this.children != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 15), varOffset - offset - 39);
         if (this.children.length > 4096000) {
            throw ProtocolException.arrayTooLong("Children", this.children.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.children.length);
         int childrenValueOffset = 0;

         for (int i = 0; i < this.children.length; i++) {
            childrenValueOffset += this.children[i].serialize(mem, varOffset + childrenValueOffset);
         }

         varOffset += childrenValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 15), -1);
      }

      if (this.params != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 19), varOffset - offset - 39);
         if (this.params.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Params", this.params.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.params.size());

         for (Entry<String, ParamValue> e : this.params.entrySet()) {
            varOffset += PacketIO.writeVarString(mem, varOffset, e.getKey(), 16384000);
            varOffset += e.getValue().serializeWithTypeId(mem, varOffset);
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 19), -1);
      }

      if (this.messageParams != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 23), varOffset - offset - 39);
         if (this.messageParams.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("MessageParams", this.messageParams.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.messageParams.size());

         for (Entry<String, FormattedMessage> e : this.messageParams.entrySet()) {
            varOffset += PacketIO.writeVarString(mem, varOffset, e.getKey(), 16384000);
            varOffset += e.getValue().serialize(mem, varOffset);
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 23), -1);
      }

      if (this.color != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 27), varOffset - offset - 39);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.color, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 27), -1);
      }

      if (this.link != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 31), varOffset - offset - 39);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.link, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 31), -1);
      }

      if (this.image != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 35), varOffset - offset - 39);
         varOffset += this.image.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 35), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 39;
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

      if (this.image != null) {
         size += this.image.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 39) {
         return ValidationResult.error("Buffer too small: expected at least 39 bytes");
      } else {
         byte[] nullBits = PacketIO.readBytes(buffer, offset, 2);
         if ((nullBits[0] & 16) != 0) {
            int rawTextOffset = buffer.getIntLE(offset + 7);
            if (rawTextOffset < 0 || rawTextOffset > buffer.writerIndex() - offset - 39) {
               return ValidationResult.error("Invalid offset for RawText");
            }

            int pos = offset + 39 + rawTextOffset;
            int rawTextLen = VarInt.peek(buffer, pos);
            if (rawTextLen < 0) {
               return ValidationResult.error("Invalid string length for RawText");
            }

            if (rawTextLen > 4096000) {
               return ValidationResult.error("RawText exceeds max length 4096000");
            }

            pos += VarInt.size(rawTextLen);
            pos += rawTextLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading RawText");
            }
         }

         if ((nullBits[0] & 32) != 0) {
            int messageIdOffset = buffer.getIntLE(offset + 11);
            if (messageIdOffset < 0 || messageIdOffset > buffer.writerIndex() - offset - 39) {
               return ValidationResult.error("Invalid offset for MessageId");
            }

            int posx = offset + 39 + messageIdOffset;
            int messageIdLen = VarInt.peek(buffer, posx);
            if (messageIdLen < 0) {
               return ValidationResult.error("Invalid string length for MessageId");
            }

            if (messageIdLen > 4096000) {
               return ValidationResult.error("MessageId exceeds max length 4096000");
            }

            posx += VarInt.size(messageIdLen);
            posx += messageIdLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading MessageId");
            }
         }

         if ((nullBits[0] & 64) != 0) {
            int childrenOffset = buffer.getIntLE(offset + 15);
            if (childrenOffset < 0 || childrenOffset > buffer.writerIndex() - offset - 39) {
               return ValidationResult.error("Invalid offset for Children");
            }

            int posxx = offset + 39 + childrenOffset;
            int childrenCount = VarInt.peek(buffer, posxx);
            if (childrenCount < 0) {
               return ValidationResult.error("Invalid array count for Children");
            }

            if (childrenCount > 4096000) {
               return ValidationResult.error("Children exceeds max length 4096000");
            }

            posxx += VarInt.size(childrenCount);

            for (int i = 0; i < childrenCount; i++) {
               ValidationResult structResult = validateStructure(buffer, posxx);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid FormattedMessage in Children[" + i + "]: " + structResult.error());
               }

               posxx += computeBytesConsumed(buffer, posxx);
            }
         }

         if ((nullBits[0] & 128) != 0) {
            int paramsOffset = buffer.getIntLE(offset + 19);
            if (paramsOffset < 0 || paramsOffset > buffer.writerIndex() - offset - 39) {
               return ValidationResult.error("Invalid offset for Params");
            }

            int posxxx = offset + 39 + paramsOffset;
            int paramsCount = VarInt.peek(buffer, posxxx);
            if (paramsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for Params");
            }

            if (paramsCount > 4096000) {
               return ValidationResult.error("Params exceeds max length 4096000");
            }

            posxxx += VarInt.size(paramsCount);

            for (int i = 0; i < paramsCount; i++) {
               int keyLen = VarInt.peek(buffer, posxxx);
               if (keyLen < 0) {
                  return ValidationResult.error("Invalid string length for key");
               }

               if (keyLen > 4096000) {
                  return ValidationResult.error("key exceeds max length 4096000");
               }

               posxxx += VarInt.size(keyLen);
               posxxx += keyLen;
               if (posxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               posxxx += ParamValue.computeBytesConsumed(buffer, posxxx);
            }
         }

         if ((nullBits[1] & 1) != 0) {
            int messageParamsOffset = buffer.getIntLE(offset + 23);
            if (messageParamsOffset < 0 || messageParamsOffset > buffer.writerIndex() - offset - 39) {
               return ValidationResult.error("Invalid offset for MessageParams");
            }

            int posxxxx = offset + 39 + messageParamsOffset;
            int messageParamsCount = VarInt.peek(buffer, posxxxx);
            if (messageParamsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for MessageParams");
            }

            if (messageParamsCount > 4096000) {
               return ValidationResult.error("MessageParams exceeds max length 4096000");
            }

            posxxxx += VarInt.size(messageParamsCount);

            for (int i = 0; i < messageParamsCount; i++) {
               int keyLenx = VarInt.peek(buffer, posxxxx);
               if (keyLenx < 0) {
                  return ValidationResult.error("Invalid string length for key");
               }

               if (keyLenx > 4096000) {
                  return ValidationResult.error("key exceeds max length 4096000");
               }

               posxxxx += VarInt.size(keyLenx);
               posxxxx += keyLenx;
               if (posxxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               posxxxx += computeBytesConsumed(buffer, posxxxx);
            }
         }

         if ((nullBits[1] & 2) != 0) {
            int colorOffset = buffer.getIntLE(offset + 27);
            if (colorOffset < 0 || colorOffset > buffer.writerIndex() - offset - 39) {
               return ValidationResult.error("Invalid offset for Color");
            }

            int posxxxxx = offset + 39 + colorOffset;
            int colorLen = VarInt.peek(buffer, posxxxxx);
            if (colorLen < 0) {
               return ValidationResult.error("Invalid string length for Color");
            }

            if (colorLen > 4096000) {
               return ValidationResult.error("Color exceeds max length 4096000");
            }

            posxxxxx += VarInt.size(colorLen);
            posxxxxx += colorLen;
            if (posxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Color");
            }
         }

         if ((nullBits[1] & 4) != 0) {
            int linkOffset = buffer.getIntLE(offset + 31);
            if (linkOffset < 0 || linkOffset > buffer.writerIndex() - offset - 39) {
               return ValidationResult.error("Invalid offset for Link");
            }

            int posxxxxxx = offset + 39 + linkOffset;
            int linkLen = VarInt.peek(buffer, posxxxxxx);
            if (linkLen < 0) {
               return ValidationResult.error("Invalid string length for Link");
            }

            if (linkLen > 4096000) {
               return ValidationResult.error("Link exceeds max length 4096000");
            }

            posxxxxxx += VarInt.size(linkLen);
            posxxxxxx += linkLen;
            if (posxxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Link");
            }
         }

         if ((nullBits[1] & 8) != 0) {
            int imageOffset = buffer.getIntLE(offset + 35);
            if (imageOffset < 0 || imageOffset > buffer.writerIndex() - offset - 39) {
               return ValidationResult.error("Invalid offset for Image");
            }

            int posxxxxxxx = offset + 39 + imageOffset;
            ValidationResult imageResult = FormattedMessageImage.validateStructure(buffer, posxxxxxxx);
            if (!imageResult.isValid()) {
               return ValidationResult.error("Invalid Image: " + imageResult.error());
            }

            posxxxxxxx += FormattedMessageImage.computeBytesConsumed(buffer, posxxxxxxx);
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
      copy.image = this.image != null ? this.image.clone() : null;
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
               && this.markupEnabled == other.markupEnabled
               && Objects.equals(this.image, other.image);
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
      result = 31 * result + Boolean.hashCode(this.markupEnabled);
      return 31 * result + Objects.hashCode(this.image);
   }
}
