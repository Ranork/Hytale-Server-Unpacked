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

public class ItemTranslationProperties {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 4;
   public static final int VARIABLE_BLOCK_START = 17;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public String name;
   @Nullable
   public Map<String, FormattedMessage> nameArguments;
   @Nullable
   public String description;
   @Nullable
   public Map<String, FormattedMessage> descriptionArguments;

   public ItemTranslationProperties() {
   }

   public ItemTranslationProperties(
      @Nullable String name,
      @Nullable Map<String, FormattedMessage> nameArguments,
      @Nullable String description,
      @Nullable Map<String, FormattedMessage> descriptionArguments
   ) {
      this.name = name;
      this.nameArguments = nameArguments;
      this.description = description;
      this.descriptionArguments = descriptionArguments;
   }

   public ItemTranslationProperties(@Nonnull ItemTranslationProperties other) {
      this.name = other.name;
      this.nameArguments = other.nameArguments;
      this.description = other.description;
      this.descriptionArguments = other.descriptionArguments;
   }

   @Nonnull
   public static ItemTranslationProperties deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 17) {
         throw ProtocolException.bufferTooSmall("ItemTranslationProperties", 17, buf.readableBytes() - offset);
      } else {
         ItemTranslationProperties obj = new ItemTranslationProperties();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 1);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 17) {
               throw ProtocolException.invalidOffset("Name", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 17 + varPosBase0;
            int nameLen = VarInt.peek(buf, varPos0);
            if (nameLen < 0) {
               throw ProtocolException.invalidVarInt("Name");
            }

            int nameVarIntLen = VarInt.size(nameLen);
            if (nameLen > 4096000) {
               throw ProtocolException.stringTooLong("Name", nameLen, 4096000);
            }

            if (varPos0 + nameVarIntLen + nameLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Name", varPos0 + nameVarIntLen + nameLen, buf.readableBytes());
            }

            obj.name = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 5);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 17) {
               throw ProtocolException.invalidOffset("NameArguments", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 17 + varPosBase1;
            int nameArgumentsCount = VarInt.peek(buf, varPos1);
            if (nameArgumentsCount < 0) {
               throw ProtocolException.invalidVarInt("NameArguments");
            }

            int varIntLen = VarInt.size(nameArgumentsCount);
            if (nameArgumentsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("NameArguments", nameArgumentsCount, 4096000);
            }

            obj.nameArguments = new HashMap<>(nameArgumentsCount);
            int dictPos = varPos1 + varIntLen;

            for (int i = 0; i < nameArgumentsCount; i++) {
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
               FormattedMessage val = FormattedMessage.deserialize(buf, dictPos);
               dictPos += FormattedMessage.computeBytesConsumed(buf, dictPos);
               if (obj.nameArguments.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("nameArguments", key);
               }
            }
         }

         if ((nullBits & 4) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 9);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 17) {
               throw ProtocolException.invalidOffset("Description", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 17 + varPosBase2;
            int descriptionLen = VarInt.peek(buf, varPos2);
            if (descriptionLen < 0) {
               throw ProtocolException.invalidVarInt("Description");
            }

            int descriptionVarIntLen = VarInt.size(descriptionLen);
            if (descriptionLen > 4096000) {
               throw ProtocolException.stringTooLong("Description", descriptionLen, 4096000);
            }

            if (varPos2 + descriptionVarIntLen + descriptionLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Description", varPos2 + descriptionVarIntLen + descriptionLen, buf.readableBytes());
            }

            obj.description = PacketIO.readVarString(buf, varPos2, PacketIO.UTF8);
         }

         if ((nullBits & 8) != 0) {
            int varPosBase3 = buf.getIntLE(offset + 13);
            if (varPosBase3 < 0 || varPosBase3 > buf.writerIndex() - offset - 17) {
               throw ProtocolException.invalidOffset("DescriptionArguments", varPosBase3, buf.readableBytes());
            }

            int varPos3 = offset + 17 + varPosBase3;
            int descriptionArgumentsCount = VarInt.peek(buf, varPos3);
            if (descriptionArgumentsCount < 0) {
               throw ProtocolException.invalidVarInt("DescriptionArguments");
            }

            int varIntLen = VarInt.size(descriptionArgumentsCount);
            if (descriptionArgumentsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("DescriptionArguments", descriptionArgumentsCount, 4096000);
            }

            obj.descriptionArguments = new HashMap<>(descriptionArgumentsCount);
            int dictPos = varPos3 + varIntLen;

            for (int i = 0; i < descriptionArgumentsCount; i++) {
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
               FormattedMessage val = FormattedMessage.deserialize(buf, dictPos);
               dictPos += FormattedMessage.computeBytesConsumed(buf, dictPos);
               if (obj.descriptionArguments.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("descriptionArguments", key);
               }
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 17;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 17) {
            throw ProtocolException.invalidOffset("Name", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 17 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 17) {
            throw ProtocolException.invalidOffset("NameArguments", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 17 + fieldOffset1;
         int dictLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            int sl = VarInt.peek(buf, pos1);
            pos1 += VarInt.size(sl) + sl;
            pos1 += FormattedMessage.computeBytesConsumed(buf, pos1);
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 9);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 17) {
            throw ProtocolException.invalidOffset("Description", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 17 + fieldOffset2;
         int sl = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(sl) + sl;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 13);
         if (fieldOffset3 < 0 || fieldOffset3 > buf.writerIndex() - offset - 17) {
            throw ProtocolException.invalidOffset("DescriptionArguments", fieldOffset3, maxEnd);
         }

         int pos3 = offset + 17 + fieldOffset3;
         int dictLen = VarInt.peek(buf, pos3);
         pos3 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            int sl = VarInt.peek(buf, pos3);
            pos3 += VarInt.size(sl) + sl;
            pos3 += FormattedMessage.computeBytesConsumed(buf, pos3);
         }

         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 17L;
   }

   @Nullable
   public static String getName(MemorySegment mem) {
      return getName(mem, 0);
   }

   @Nullable
   public static String getName(MemorySegment mem, int offset) {
      return hasName(mem, offset) ? PacketIO.readVarString("Name", mem, offset + getValidatedOffset(mem, offset, 1, 17, "Name"), 4096000, PacketIO.UTF8) : null;
   }

   @Nullable
   public static Map<String, FormattedMessage> getNameArguments(MemorySegment mem) {
      return getNameArguments(mem, 0);
   }

   @Nullable
   public static Map<String, FormattedMessage> getNameArguments(MemorySegment mem, int offset) {
      if (!hasNameArguments(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 5, 17, "NameArguments");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("NameArguments", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("NameArguments", len, 4096000);
         } else {
            Map<String, FormattedMessage> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               long keyPacked = VarInt.getWithLength(mem, off);
               int nkey = (int)keyPacked + (int)(keyPacked >>> 32);
               String key = PacketIO.readVarString("key", mem, off, 16384000, PacketIO.UTF8);
               off += nkey;
               FormattedMessage value = FormattedMessage.toObject(mem, off);
               off += value.computeSize();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("NameArguments", key);
               }
            }

            return data;
         }
      }
   }

   @Nullable
   public static String getDescription(MemorySegment mem) {
      return getDescription(mem, 0);
   }

   @Nullable
   public static String getDescription(MemorySegment mem, int offset) {
      return hasDescription(mem, offset)
         ? PacketIO.readVarString("Description", mem, offset + getValidatedOffset(mem, offset, 9, 17, "Description"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static Map<String, FormattedMessage> getDescriptionArguments(MemorySegment mem) {
      return getDescriptionArguments(mem, 0);
   }

   @Nullable
   public static Map<String, FormattedMessage> getDescriptionArguments(MemorySegment mem, int offset) {
      if (!hasDescriptionArguments(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 13, 17, "DescriptionArguments");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("DescriptionArguments", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("DescriptionArguments", len, 4096000);
         } else {
            Map<String, FormattedMessage> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               long keyPacked = VarInt.getWithLength(mem, off);
               int nkey = (int)keyPacked + (int)(keyPacked >>> 32);
               String key = PacketIO.readVarString("key", mem, off, 16384000, PacketIO.UTF8);
               off += nkey;
               FormattedMessage value = FormattedMessage.toObject(mem, off);
               off += value.computeSize();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("DescriptionArguments", key);
               }
            }

            return data;
         }
      }
   }

   public static boolean hasName(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasNameArguments(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasDescription(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasDescriptionArguments(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
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

   public static ItemTranslationProperties toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ItemTranslationProperties toObject(MemorySegment mem, int offset) {
      if (offset + 17 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ItemTranslationProperties", offset + 17, (int)mem.byteSize());
      } else {
         Map<String, FormattedMessage> nameArguments = null;
         if (hasNameArguments(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 5, 17, "NameArguments");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("NameArguments", len);
            }

            if (len > 4096000) {
               throw ProtocolException.dictionaryTooLarge("NameArguments", len, 4096000);
            }

            nameArguments = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               long keyPacked = VarInt.getWithLength(mem, off);
               int nkey = (int)keyPacked + (int)(keyPacked >>> 32);
               String key = PacketIO.readVarString("key", mem, off, 16384000, PacketIO.UTF8);
               off += nkey;
               FormattedMessage value = FormattedMessage.toObject(mem, off);
               off += value.computeSize();
               if (nameArguments.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("NameArguments", key);
               }
            }
         }

         Map<String, FormattedMessage> descriptionArguments = null;
         if (hasDescriptionArguments(mem, offset)) {
            int offx = offset + getValidatedOffset(mem, offset, 13, 17, "DescriptionArguments");
            long packedx = VarInt.getWithLength(mem, offx);
            int lenx = (int)packedx;
            if (lenx < 0) {
               throw ProtocolException.negativeLength("DescriptionArguments", lenx);
            }

            if (lenx > 4096000) {
               throw ProtocolException.dictionaryTooLarge("DescriptionArguments", lenx, 4096000);
            }

            descriptionArguments = new HashMap<>(lenx);
            offx += (int)(packedx >>> 32);

            for (int ix = 0; ix < lenx; ix++) {
               long keyPacked = VarInt.getWithLength(mem, offx);
               int nkey = (int)keyPacked + (int)(keyPacked >>> 32);
               String key = PacketIO.readVarString("key", mem, offx, 16384000, PacketIO.UTF8);
               offx += nkey;
               FormattedMessage value = FormattedMessage.toObject(mem, offx);
               offx += value.computeSize();
               if (descriptionArguments.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("DescriptionArguments", key);
               }
            }
         }

         return new ItemTranslationProperties(
            hasName(mem, offset) ? PacketIO.readVarString("Name", mem, offset + getValidatedOffset(mem, offset, 1, 17, "Name"), 4096000, PacketIO.UTF8) : null,
            nameArguments,
            hasDescription(mem, offset)
               ? PacketIO.readVarString("Description", mem, offset + getValidatedOffset(mem, offset, 9, 17, "Description"), 4096000, PacketIO.UTF8)
               : null,
            descriptionArguments
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.name != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.nameArguments != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.description != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.descriptionArguments != null) {
         nullBits = (byte)(nullBits | 8);
      }

      buf.writeByte(nullBits);
      int nameOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int nameArgumentsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int descriptionOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int descriptionArgumentsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.name != null) {
         buf.setIntLE(nameOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.name, 4096000);
      } else {
         buf.setIntLE(nameOffsetSlot, -1);
      }

      if (this.nameArguments != null) {
         buf.setIntLE(nameArgumentsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.nameArguments.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("NameArguments", this.nameArguments.size(), 4096000);
         }

         VarInt.write(buf, this.nameArguments.size());

         for (Entry<String, FormattedMessage> e : this.nameArguments.entrySet()) {
            PacketIO.writeVarString(buf, e.getKey(), 4096000);
            e.getValue().serialize(buf);
         }
      } else {
         buf.setIntLE(nameArgumentsOffsetSlot, -1);
      }

      if (this.description != null) {
         buf.setIntLE(descriptionOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.description, 4096000);
      } else {
         buf.setIntLE(descriptionOffsetSlot, -1);
      }

      if (this.descriptionArguments != null) {
         buf.setIntLE(descriptionArgumentsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.descriptionArguments.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("DescriptionArguments", this.descriptionArguments.size(), 4096000);
         }

         VarInt.write(buf, this.descriptionArguments.size());

         for (Entry<String, FormattedMessage> e : this.descriptionArguments.entrySet()) {
            PacketIO.writeVarString(buf, e.getKey(), 4096000);
            e.getValue().serialize(buf);
         }
      } else {
         buf.setIntLE(descriptionArgumentsOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.name != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.nameArguments != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.description != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.descriptionArguments != null) {
         nullBits = (byte)(nullBits | 8);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 17;
      if (this.name != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), varOffset - offset - 17);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.name, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), -1);
      }

      if (this.nameArguments != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), varOffset - offset - 17);
         if (this.nameArguments.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("NameArguments", this.nameArguments.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.nameArguments.size());

         for (Entry<String, FormattedMessage> e : this.nameArguments.entrySet()) {
            varOffset += PacketIO.writeVarString(mem, varOffset, e.getKey(), 16384000);
            varOffset += e.getValue().serialize(mem, varOffset);
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), -1);
      }

      if (this.description != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), varOffset - offset - 17);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.description, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), -1);
      }

      if (this.descriptionArguments != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 13), varOffset - offset - 17);
         if (this.descriptionArguments.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("DescriptionArguments", this.descriptionArguments.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.descriptionArguments.size());

         for (Entry<String, FormattedMessage> e : this.descriptionArguments.entrySet()) {
            varOffset += PacketIO.writeVarString(mem, varOffset, e.getKey(), 16384000);
            varOffset += e.getValue().serialize(mem, varOffset);
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 13), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 17;
      if (this.name != null) {
         size += PacketIO.stringSize(this.name);
      }

      if (this.nameArguments != null) {
         int nameArgumentsSize = 0;

         for (Entry<String, FormattedMessage> kvp : this.nameArguments.entrySet()) {
            nameArgumentsSize += PacketIO.stringSize(kvp.getKey()) + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.nameArguments.size()) + nameArgumentsSize;
      }

      if (this.description != null) {
         size += PacketIO.stringSize(this.description);
      }

      if (this.descriptionArguments != null) {
         int descriptionArgumentsSize = 0;

         for (Entry<String, FormattedMessage> kvp : this.descriptionArguments.entrySet()) {
            descriptionArgumentsSize += PacketIO.stringSize(kvp.getKey()) + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.descriptionArguments.size()) + descriptionArgumentsSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 17) {
         return ValidationResult.error("Buffer too small: expected at least 17 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int nameOffset = buffer.getIntLE(offset + 1);
            if (nameOffset < 0 || nameOffset > buffer.writerIndex() - offset - 17) {
               return ValidationResult.error("Invalid offset for Name");
            }

            int pos = offset + 17 + nameOffset;
            int nameLen = VarInt.peek(buffer, pos);
            if (nameLen < 0) {
               return ValidationResult.error("Invalid string length for Name");
            }

            if (nameLen > 4096000) {
               return ValidationResult.error("Name exceeds max length 4096000");
            }

            pos += VarInt.size(nameLen);
            pos += nameLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Name");
            }
         }

         if ((nullBits & 2) != 0) {
            int nameArgumentsOffset = buffer.getIntLE(offset + 5);
            if (nameArgumentsOffset < 0 || nameArgumentsOffset > buffer.writerIndex() - offset - 17) {
               return ValidationResult.error("Invalid offset for NameArguments");
            }

            int posx = offset + 17 + nameArgumentsOffset;
            int nameArgumentsCount = VarInt.peek(buffer, posx);
            if (nameArgumentsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for NameArguments");
            }

            if (nameArgumentsCount > 4096000) {
               return ValidationResult.error("NameArguments exceeds max length 4096000");
            }

            posx += VarInt.size(nameArgumentsCount);

            for (int i = 0; i < nameArgumentsCount; i++) {
               int keyLen = VarInt.peek(buffer, posx);
               if (keyLen < 0) {
                  return ValidationResult.error("Invalid string length for key");
               }

               if (keyLen > 4096000) {
                  return ValidationResult.error("key exceeds max length 4096000");
               }

               posx += VarInt.size(keyLen);
               posx += keyLen;
               if (posx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               posx += FormattedMessage.computeBytesConsumed(buffer, posx);
            }
         }

         if ((nullBits & 4) != 0) {
            int descriptionOffset = buffer.getIntLE(offset + 9);
            if (descriptionOffset < 0 || descriptionOffset > buffer.writerIndex() - offset - 17) {
               return ValidationResult.error("Invalid offset for Description");
            }

            int posxx = offset + 17 + descriptionOffset;
            int descriptionLen = VarInt.peek(buffer, posxx);
            if (descriptionLen < 0) {
               return ValidationResult.error("Invalid string length for Description");
            }

            if (descriptionLen > 4096000) {
               return ValidationResult.error("Description exceeds max length 4096000");
            }

            posxx += VarInt.size(descriptionLen);
            posxx += descriptionLen;
            if (posxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Description");
            }
         }

         if ((nullBits & 8) != 0) {
            int descriptionArgumentsOffset = buffer.getIntLE(offset + 13);
            if (descriptionArgumentsOffset < 0 || descriptionArgumentsOffset > buffer.writerIndex() - offset - 17) {
               return ValidationResult.error("Invalid offset for DescriptionArguments");
            }

            int posxxx = offset + 17 + descriptionArgumentsOffset;
            int descriptionArgumentsCount = VarInt.peek(buffer, posxxx);
            if (descriptionArgumentsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for DescriptionArguments");
            }

            if (descriptionArgumentsCount > 4096000) {
               return ValidationResult.error("DescriptionArguments exceeds max length 4096000");
            }

            posxxx += VarInt.size(descriptionArgumentsCount);

            for (int i = 0; i < descriptionArgumentsCount; i++) {
               int keyLenx = VarInt.peek(buffer, posxxx);
               if (keyLenx < 0) {
                  return ValidationResult.error("Invalid string length for key");
               }

               if (keyLenx > 4096000) {
                  return ValidationResult.error("key exceeds max length 4096000");
               }

               posxxx += VarInt.size(keyLenx);
               posxxx += keyLenx;
               if (posxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               posxxx += FormattedMessage.computeBytesConsumed(buffer, posxxx);
            }
         }

         return ValidationResult.OK;
      }
   }

   public ItemTranslationProperties clone() {
      ItemTranslationProperties copy = new ItemTranslationProperties();
      copy.name = this.name;
      if (this.nameArguments != null) {
         Map<String, FormattedMessage> m = new HashMap<>();

         for (Entry<String, FormattedMessage> e : this.nameArguments.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.nameArguments = m;
      }

      copy.description = this.description;
      if (this.descriptionArguments != null) {
         Map<String, FormattedMessage> m = new HashMap<>();

         for (Entry<String, FormattedMessage> e : this.descriptionArguments.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.descriptionArguments = m;
      }

      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ItemTranslationProperties other)
            ? false
            : Objects.equals(this.name, other.name)
               && Objects.equals(this.nameArguments, other.nameArguments)
               && Objects.equals(this.description, other.description)
               && Objects.equals(this.descriptionArguments, other.descriptionArguments);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.name, this.nameArguments, this.description, this.descriptionArguments);
   }
}
