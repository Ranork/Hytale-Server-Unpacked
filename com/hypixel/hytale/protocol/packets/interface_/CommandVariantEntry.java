package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CommandVariantEntry {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 5;
   public static final int VARIABLE_BLOCK_START = 21;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public String pattern;
   @Nullable
   public CommandArgInfo[] requiredArgs;
   @Nullable
   public String usageText;
   @Nullable
   public CommandSuggestionOverride[] suggestionOverrides;
   @Nullable
   public String description;

   public CommandVariantEntry() {
   }

   public CommandVariantEntry(
      @Nullable String pattern,
      @Nullable CommandArgInfo[] requiredArgs,
      @Nullable String usageText,
      @Nullable CommandSuggestionOverride[] suggestionOverrides,
      @Nullable String description
   ) {
      this.pattern = pattern;
      this.requiredArgs = requiredArgs;
      this.usageText = usageText;
      this.suggestionOverrides = suggestionOverrides;
      this.description = description;
   }

   public CommandVariantEntry(@Nonnull CommandVariantEntry other) {
      this.pattern = other.pattern;
      this.requiredArgs = other.requiredArgs;
      this.usageText = other.usageText;
      this.suggestionOverrides = other.suggestionOverrides;
      this.description = other.description;
   }

   @Nonnull
   public static CommandVariantEntry deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 21) {
         throw ProtocolException.bufferTooSmall("CommandVariantEntry", 21, buf.readableBytes() - offset);
      } else {
         CommandVariantEntry obj = new CommandVariantEntry();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 1);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 21) {
               throw ProtocolException.invalidOffset("Pattern", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 21 + varPosBase0;
            int patternLen = VarInt.peek(buf, varPos0);
            if (patternLen < 0) {
               throw ProtocolException.invalidVarInt("Pattern");
            }

            int patternVarIntLen = VarInt.size(patternLen);
            if (patternLen > 4096000) {
               throw ProtocolException.stringTooLong("Pattern", patternLen, 4096000);
            }

            if (varPos0 + patternVarIntLen + patternLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Pattern", varPos0 + patternVarIntLen + patternLen, buf.readableBytes());
            }

            obj.pattern = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 5);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 21) {
               throw ProtocolException.invalidOffset("RequiredArgs", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 21 + varPosBase1;
            int requiredArgsCount = VarInt.peek(buf, varPos1);
            if (requiredArgsCount < 0) {
               throw ProtocolException.invalidVarInt("RequiredArgs");
            }

            int varIntLen = VarInt.size(requiredArgsCount);
            if (requiredArgsCount > 4096000) {
               throw ProtocolException.arrayTooLong("RequiredArgs", requiredArgsCount, 4096000);
            }

            if (varPos1 + varIntLen + requiredArgsCount * 5L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("RequiredArgs", varPos1 + varIntLen + requiredArgsCount * 5, buf.readableBytes());
            }

            obj.requiredArgs = new CommandArgInfo[requiredArgsCount];
            int elemPos = varPos1 + varIntLen;

            for (int i = 0; i < requiredArgsCount; i++) {
               obj.requiredArgs[i] = CommandArgInfo.deserialize(buf, elemPos);
               elemPos += CommandArgInfo.computeBytesConsumed(buf, elemPos);
            }
         }

         if ((nullBits & 4) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 9);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 21) {
               throw ProtocolException.invalidOffset("UsageText", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 21 + varPosBase2;
            int usageTextLen = VarInt.peek(buf, varPos2);
            if (usageTextLen < 0) {
               throw ProtocolException.invalidVarInt("UsageText");
            }

            int usageTextVarIntLen = VarInt.size(usageTextLen);
            if (usageTextLen > 4096000) {
               throw ProtocolException.stringTooLong("UsageText", usageTextLen, 4096000);
            }

            if (varPos2 + usageTextVarIntLen + usageTextLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("UsageText", varPos2 + usageTextVarIntLen + usageTextLen, buf.readableBytes());
            }

            obj.usageText = PacketIO.readVarString(buf, varPos2, PacketIO.UTF8);
         }

         if ((nullBits & 8) != 0) {
            int varPosBase3 = buf.getIntLE(offset + 13);
            if (varPosBase3 < 0 || varPosBase3 > buf.writerIndex() - offset - 21) {
               throw ProtocolException.invalidOffset("SuggestionOverrides", varPosBase3, buf.readableBytes());
            }

            int varPos3 = offset + 21 + varPosBase3;
            int suggestionOverridesCount = VarInt.peek(buf, varPos3);
            if (suggestionOverridesCount < 0) {
               throw ProtocolException.invalidVarInt("SuggestionOverrides");
            }

            int varIntLenx = VarInt.size(suggestionOverridesCount);
            if (suggestionOverridesCount > 4096000) {
               throw ProtocolException.arrayTooLong("SuggestionOverrides", suggestionOverridesCount, 4096000);
            }

            if (varPos3 + varIntLenx + suggestionOverridesCount * 9L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("SuggestionOverrides", varPos3 + varIntLenx + suggestionOverridesCount * 9, buf.readableBytes());
            }

            obj.suggestionOverrides = new CommandSuggestionOverride[suggestionOverridesCount];
            int elemPos = varPos3 + varIntLenx;

            for (int i = 0; i < suggestionOverridesCount; i++) {
               obj.suggestionOverrides[i] = CommandSuggestionOverride.deserialize(buf, elemPos);
               elemPos += CommandSuggestionOverride.computeBytesConsumed(buf, elemPos);
            }
         }

         if ((nullBits & 16) != 0) {
            int varPosBase4 = buf.getIntLE(offset + 17);
            if (varPosBase4 < 0 || varPosBase4 > buf.writerIndex() - offset - 21) {
               throw ProtocolException.invalidOffset("Description", varPosBase4, buf.readableBytes());
            }

            int varPos4 = offset + 21 + varPosBase4;
            int descriptionLen = VarInt.peek(buf, varPos4);
            if (descriptionLen < 0) {
               throw ProtocolException.invalidVarInt("Description");
            }

            int descriptionVarIntLen = VarInt.size(descriptionLen);
            if (descriptionLen > 4096000) {
               throw ProtocolException.stringTooLong("Description", descriptionLen, 4096000);
            }

            if (varPos4 + descriptionVarIntLen + descriptionLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Description", varPos4 + descriptionVarIntLen + descriptionLen, buf.readableBytes());
            }

            obj.description = PacketIO.readVarString(buf, varPos4, PacketIO.UTF8);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 21;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 21) {
            throw ProtocolException.invalidOffset("Pattern", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 21 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 21) {
            throw ProtocolException.invalidOffset("RequiredArgs", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 21 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos1 += CommandArgInfo.computeBytesConsumed(buf, pos1);
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 9);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 21) {
            throw ProtocolException.invalidOffset("UsageText", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 21 + fieldOffset2;
         int sl = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(sl) + sl;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 13);
         if (fieldOffset3 < 0 || fieldOffset3 > buf.writerIndex() - offset - 21) {
            throw ProtocolException.invalidOffset("SuggestionOverrides", fieldOffset3, maxEnd);
         }

         int pos3 = offset + 21 + fieldOffset3;
         int arrLen = VarInt.peek(buf, pos3);
         pos3 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos3 += CommandSuggestionOverride.computeBytesConsumed(buf, pos3);
         }

         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      if ((nullBits & 16) != 0) {
         int fieldOffset4 = buf.getIntLE(offset + 17);
         if (fieldOffset4 < 0 || fieldOffset4 > buf.writerIndex() - offset - 21) {
            throw ProtocolException.invalidOffset("Description", fieldOffset4, maxEnd);
         }

         int pos4 = offset + 21 + fieldOffset4;
         int sl = VarInt.peek(buf, pos4);
         pos4 += VarInt.size(sl) + sl;
         if (pos4 - offset > maxEnd) {
            maxEnd = pos4 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 21L;
   }

   @Nullable
   public static String getPattern(MemorySegment mem) {
      return getPattern(mem, 0);
   }

   @Nullable
   public static String getPattern(MemorySegment mem, int offset) {
      return hasPattern(mem, offset)
         ? PacketIO.readVarString("Pattern", mem, offset + getValidatedOffset(mem, offset, 1, 21, "Pattern"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static CommandArgInfo[] getRequiredArgs(MemorySegment mem) {
      return getRequiredArgs(mem, 0);
   }

   @Nullable
   public static CommandArgInfo[] getRequiredArgs(MemorySegment mem, int offset) {
      if (!hasRequiredArgs(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 5, 21, "RequiredArgs");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("RequiredArgs", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("RequiredArgs", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("RequiredArgs", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               CommandArgInfo[] data = new CommandArgInfo[len];

               for (int i = 0; i < len; i++) {
                  data[i] = CommandArgInfo.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   @Nullable
   public static String getUsageText(MemorySegment mem) {
      return getUsageText(mem, 0);
   }

   @Nullable
   public static String getUsageText(MemorySegment mem, int offset) {
      return hasUsageText(mem, offset)
         ? PacketIO.readVarString("UsageText", mem, offset + getValidatedOffset(mem, offset, 9, 21, "UsageText"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static CommandSuggestionOverride[] getSuggestionOverrides(MemorySegment mem) {
      return getSuggestionOverrides(mem, 0);
   }

   @Nullable
   public static CommandSuggestionOverride[] getSuggestionOverrides(MemorySegment mem, int offset) {
      if (!hasSuggestionOverrides(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 13, 21, "SuggestionOverrides");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("SuggestionOverrides", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("SuggestionOverrides", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("SuggestionOverrides", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               CommandSuggestionOverride[] data = new CommandSuggestionOverride[len];

               for (int i = 0; i < len; i++) {
                  data[i] = CommandSuggestionOverride.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
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
         ? PacketIO.readVarString("Description", mem, offset + getValidatedOffset(mem, offset, 17, 21, "Description"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static boolean hasPattern(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasRequiredArgs(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasUsageText(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasSuggestionOverrides(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   public static boolean hasDescription(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 16) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static CommandVariantEntry toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static CommandVariantEntry toObject(MemorySegment mem, int offset) {
      if (offset + 21 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("CommandVariantEntry", offset + 21, (int)mem.byteSize());
      } else {
         CommandArgInfo[] requiredArgs = null;
         if (hasRequiredArgs(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 5, 21, "RequiredArgs");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("RequiredArgs", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("RequiredArgs", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("RequiredArgs", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            requiredArgs = new CommandArgInfo[len];

            for (int i = 0; i < len; i++) {
               requiredArgs[i] = CommandArgInfo.toObject(mem, off);
               off += requiredArgs[i].computeSize();
            }
         }

         CommandSuggestionOverride[] suggestionOverrides = null;
         if (hasSuggestionOverrides(mem, offset)) {
            int offx = offset + getValidatedOffset(mem, offset, 13, 21, "SuggestionOverrides");
            long packedx = VarInt.getWithLength(mem, offx);
            int lenx = (int)packedx;
            if (lenx < 0) {
               throw ProtocolException.negativeLength("SuggestionOverrides", lenx);
            }

            if (lenx > 4096000) {
               throw ProtocolException.arrayTooLong("SuggestionOverrides", lenx, 4096000);
            }

            int lenOffset = (int)(packedx >>> 32);
            if (offx + lenOffset + lenx > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("SuggestionOverrides", offx + lenOffset + lenx, (int)mem.byteSize());
            }

            offx += lenOffset;
            suggestionOverrides = new CommandSuggestionOverride[lenx];

            for (int i = 0; i < lenx; i++) {
               suggestionOverrides[i] = CommandSuggestionOverride.toObject(mem, offx);
               offx += suggestionOverrides[i].computeSize();
            }
         }

         return new CommandVariantEntry(
            hasPattern(mem, offset)
               ? PacketIO.readVarString("Pattern", mem, offset + getValidatedOffset(mem, offset, 1, 21, "Pattern"), 4096000, PacketIO.UTF8)
               : null,
            requiredArgs,
            hasUsageText(mem, offset)
               ? PacketIO.readVarString("UsageText", mem, offset + getValidatedOffset(mem, offset, 9, 21, "UsageText"), 4096000, PacketIO.UTF8)
               : null,
            suggestionOverrides,
            hasDescription(mem, offset)
               ? PacketIO.readVarString("Description", mem, offset + getValidatedOffset(mem, offset, 17, 21, "Description"), 4096000, PacketIO.UTF8)
               : null
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.pattern != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.requiredArgs != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.usageText != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.suggestionOverrides != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.description != null) {
         nullBits = (byte)(nullBits | 16);
      }

      buf.writeByte(nullBits);
      int patternOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int requiredArgsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int usageTextOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int suggestionOverridesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int descriptionOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.pattern != null) {
         buf.setIntLE(patternOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.pattern, 4096000);
      } else {
         buf.setIntLE(patternOffsetSlot, -1);
      }

      if (this.requiredArgs != null) {
         buf.setIntLE(requiredArgsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.requiredArgs.length > 4096000) {
            throw ProtocolException.arrayTooLong("RequiredArgs", this.requiredArgs.length, 4096000);
         }

         VarInt.write(buf, this.requiredArgs.length);

         for (CommandArgInfo item : this.requiredArgs) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(requiredArgsOffsetSlot, -1);
      }

      if (this.usageText != null) {
         buf.setIntLE(usageTextOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.usageText, 4096000);
      } else {
         buf.setIntLE(usageTextOffsetSlot, -1);
      }

      if (this.suggestionOverrides != null) {
         buf.setIntLE(suggestionOverridesOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.suggestionOverrides.length > 4096000) {
            throw ProtocolException.arrayTooLong("SuggestionOverrides", this.suggestionOverrides.length, 4096000);
         }

         VarInt.write(buf, this.suggestionOverrides.length);

         for (CommandSuggestionOverride item : this.suggestionOverrides) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(suggestionOverridesOffsetSlot, -1);
      }

      if (this.description != null) {
         buf.setIntLE(descriptionOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.description, 4096000);
      } else {
         buf.setIntLE(descriptionOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.pattern != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.requiredArgs != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.usageText != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.suggestionOverrides != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.description != null) {
         nullBits = (byte)(nullBits | 16);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 21;
      if (this.pattern != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), varOffset - offset - 21);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.pattern, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), -1);
      }

      if (this.requiredArgs != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), varOffset - offset - 21);
         if (this.requiredArgs.length > 4096000) {
            throw ProtocolException.arrayTooLong("RequiredArgs", this.requiredArgs.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.requiredArgs.length);
         int requiredArgsValueOffset = 0;

         for (int i = 0; i < this.requiredArgs.length; i++) {
            requiredArgsValueOffset += this.requiredArgs[i].serialize(mem, varOffset + requiredArgsValueOffset);
         }

         varOffset += requiredArgsValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), -1);
      }

      if (this.usageText != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), varOffset - offset - 21);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.usageText, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), -1);
      }

      if (this.suggestionOverrides != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 13), varOffset - offset - 21);
         if (this.suggestionOverrides.length > 4096000) {
            throw ProtocolException.arrayTooLong("SuggestionOverrides", this.suggestionOverrides.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.suggestionOverrides.length);
         int suggestionOverridesValueOffset = 0;

         for (int i = 0; i < this.suggestionOverrides.length; i++) {
            suggestionOverridesValueOffset += this.suggestionOverrides[i].serialize(mem, varOffset + suggestionOverridesValueOffset);
         }

         varOffset += suggestionOverridesValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 13), -1);
      }

      if (this.description != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 17), varOffset - offset - 21);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.description, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 17), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 21;
      if (this.pattern != null) {
         size += PacketIO.stringSize(this.pattern);
      }

      if (this.requiredArgs != null) {
         int requiredArgsSize = 0;

         for (CommandArgInfo elem : this.requiredArgs) {
            requiredArgsSize += elem.computeSize();
         }

         size += VarInt.size(this.requiredArgs.length) + requiredArgsSize;
      }

      if (this.usageText != null) {
         size += PacketIO.stringSize(this.usageText);
      }

      if (this.suggestionOverrides != null) {
         int suggestionOverridesSize = 0;

         for (CommandSuggestionOverride elem : this.suggestionOverrides) {
            suggestionOverridesSize += elem.computeSize();
         }

         size += VarInt.size(this.suggestionOverrides.length) + suggestionOverridesSize;
      }

      if (this.description != null) {
         size += PacketIO.stringSize(this.description);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 21) {
         return ValidationResult.error("Buffer too small: expected at least 21 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int patternOffset = buffer.getIntLE(offset + 1);
            if (patternOffset < 0 || patternOffset > buffer.writerIndex() - offset - 21) {
               return ValidationResult.error("Invalid offset for Pattern");
            }

            int pos = offset + 21 + patternOffset;
            int patternLen = VarInt.peek(buffer, pos);
            if (patternLen < 0) {
               return ValidationResult.error("Invalid string length for Pattern");
            }

            if (patternLen > 4096000) {
               return ValidationResult.error("Pattern exceeds max length 4096000");
            }

            pos += VarInt.size(patternLen);
            pos += patternLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Pattern");
            }
         }

         if ((nullBits & 2) != 0) {
            int requiredArgsOffset = buffer.getIntLE(offset + 5);
            if (requiredArgsOffset < 0 || requiredArgsOffset > buffer.writerIndex() - offset - 21) {
               return ValidationResult.error("Invalid offset for RequiredArgs");
            }

            int posx = offset + 21 + requiredArgsOffset;
            int requiredArgsCount = VarInt.peek(buffer, posx);
            if (requiredArgsCount < 0) {
               return ValidationResult.error("Invalid array count for RequiredArgs");
            }

            if (requiredArgsCount > 4096000) {
               return ValidationResult.error("RequiredArgs exceeds max length 4096000");
            }

            posx += VarInt.size(requiredArgsCount);

            for (int i = 0; i < requiredArgsCount; i++) {
               ValidationResult structResult = CommandArgInfo.validateStructure(buffer, posx);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid CommandArgInfo in RequiredArgs[" + i + "]: " + structResult.error());
               }

               posx += CommandArgInfo.computeBytesConsumed(buffer, posx);
            }
         }

         if ((nullBits & 4) != 0) {
            int usageTextOffset = buffer.getIntLE(offset + 9);
            if (usageTextOffset < 0 || usageTextOffset > buffer.writerIndex() - offset - 21) {
               return ValidationResult.error("Invalid offset for UsageText");
            }

            int posxx = offset + 21 + usageTextOffset;
            int usageTextLen = VarInt.peek(buffer, posxx);
            if (usageTextLen < 0) {
               return ValidationResult.error("Invalid string length for UsageText");
            }

            if (usageTextLen > 4096000) {
               return ValidationResult.error("UsageText exceeds max length 4096000");
            }

            posxx += VarInt.size(usageTextLen);
            posxx += usageTextLen;
            if (posxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading UsageText");
            }
         }

         if ((nullBits & 8) != 0) {
            int suggestionOverridesOffset = buffer.getIntLE(offset + 13);
            if (suggestionOverridesOffset < 0 || suggestionOverridesOffset > buffer.writerIndex() - offset - 21) {
               return ValidationResult.error("Invalid offset for SuggestionOverrides");
            }

            int posxxx = offset + 21 + suggestionOverridesOffset;
            int suggestionOverridesCount = VarInt.peek(buffer, posxxx);
            if (suggestionOverridesCount < 0) {
               return ValidationResult.error("Invalid array count for SuggestionOverrides");
            }

            if (suggestionOverridesCount > 4096000) {
               return ValidationResult.error("SuggestionOverrides exceeds max length 4096000");
            }

            posxxx += VarInt.size(suggestionOverridesCount);

            for (int i = 0; i < suggestionOverridesCount; i++) {
               ValidationResult structResult = CommandSuggestionOverride.validateStructure(buffer, posxxx);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid CommandSuggestionOverride in SuggestionOverrides[" + i + "]: " + structResult.error());
               }

               posxxx += CommandSuggestionOverride.computeBytesConsumed(buffer, posxxx);
            }
         }

         if ((nullBits & 16) != 0) {
            int descriptionOffset = buffer.getIntLE(offset + 17);
            if (descriptionOffset < 0 || descriptionOffset > buffer.writerIndex() - offset - 21) {
               return ValidationResult.error("Invalid offset for Description");
            }

            int posxxxx = offset + 21 + descriptionOffset;
            int descriptionLen = VarInt.peek(buffer, posxxxx);
            if (descriptionLen < 0) {
               return ValidationResult.error("Invalid string length for Description");
            }

            if (descriptionLen > 4096000) {
               return ValidationResult.error("Description exceeds max length 4096000");
            }

            posxxxx += VarInt.size(descriptionLen);
            posxxxx += descriptionLen;
            if (posxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Description");
            }
         }

         return ValidationResult.OK;
      }
   }

   public CommandVariantEntry clone() {
      CommandVariantEntry copy = new CommandVariantEntry();
      copy.pattern = this.pattern;
      copy.requiredArgs = this.requiredArgs != null ? Arrays.stream(this.requiredArgs).map(e -> e.clone()).toArray(CommandArgInfo[]::new) : null;
      copy.usageText = this.usageText;
      copy.suggestionOverrides = this.suggestionOverrides != null
         ? Arrays.stream(this.suggestionOverrides).map(e -> e.clone()).toArray(CommandSuggestionOverride[]::new)
         : null;
      copy.description = this.description;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof CommandVariantEntry other)
            ? false
            : Objects.equals(this.pattern, other.pattern)
               && Arrays.equals((Object[])this.requiredArgs, (Object[])other.requiredArgs)
               && Objects.equals(this.usageText, other.usageText)
               && Arrays.equals((Object[])this.suggestionOverrides, (Object[])other.suggestionOverrides)
               && Objects.equals(this.description, other.description);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.pattern);
      result = 31 * result + Arrays.hashCode((Object[])this.requiredArgs);
      result = 31 * result + Objects.hashCode(this.usageText);
      result = 31 * result + Arrays.hashCode((Object[])this.suggestionOverrides);
      return 31 * result + Objects.hashCode(this.description);
   }
}
