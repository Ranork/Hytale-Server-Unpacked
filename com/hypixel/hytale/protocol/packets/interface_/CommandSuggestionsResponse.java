package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
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

public class CommandSuggestionsResponse implements Packet, ToClientPacket {
   public static final int PACKET_ID = 237;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 17;
   public static final int VARIABLE_FIELD_COUNT = 8;
   public static final int VARIABLE_BLOCK_START = 49;
   public static final int MAX_SIZE = 1677721600;
   public int startPosition;
   public int length;
   @Nullable
   public String[] suggestions;
   @Nullable
   public boolean[] continuations;
   @Nullable
   public String usageText;
   public int currentArgStart;
   public int currentArgLength;
   @Nullable
   public String[] variantPatterns;
   @Nullable
   public String[] subcommands;
   @Nullable
   public String[] subcommandHints;
   @Nullable
   public String[] optionalArgs;
   @Nullable
   public String[] optionalArgHints;

   @Override
   public int getId() {
      return 237;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public CommandSuggestionsResponse() {
   }

   public CommandSuggestionsResponse(
      int startPosition,
      int length,
      @Nullable String[] suggestions,
      @Nullable boolean[] continuations,
      @Nullable String usageText,
      int currentArgStart,
      int currentArgLength,
      @Nullable String[] variantPatterns,
      @Nullable String[] subcommands,
      @Nullable String[] subcommandHints,
      @Nullable String[] optionalArgs,
      @Nullable String[] optionalArgHints
   ) {
      this.startPosition = startPosition;
      this.length = length;
      this.suggestions = suggestions;
      this.continuations = continuations;
      this.usageText = usageText;
      this.currentArgStart = currentArgStart;
      this.currentArgLength = currentArgLength;
      this.variantPatterns = variantPatterns;
      this.subcommands = subcommands;
      this.subcommandHints = subcommandHints;
      this.optionalArgs = optionalArgs;
      this.optionalArgHints = optionalArgHints;
   }

   public CommandSuggestionsResponse(@Nonnull CommandSuggestionsResponse other) {
      this.startPosition = other.startPosition;
      this.length = other.length;
      this.suggestions = other.suggestions;
      this.continuations = other.continuations;
      this.usageText = other.usageText;
      this.currentArgStart = other.currentArgStart;
      this.currentArgLength = other.currentArgLength;
      this.variantPatterns = other.variantPatterns;
      this.subcommands = other.subcommands;
      this.subcommandHints = other.subcommandHints;
      this.optionalArgs = other.optionalArgs;
      this.optionalArgHints = other.optionalArgHints;
   }

   @Nonnull
   public static CommandSuggestionsResponse deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 49) {
         throw ProtocolException.bufferTooSmall("CommandSuggestionsResponse", 49, buf.readableBytes() - offset);
      } else {
         CommandSuggestionsResponse obj = new CommandSuggestionsResponse();
         byte nullBits = buf.getByte(offset);
         obj.startPosition = buf.getIntLE(offset + 1);
         obj.length = buf.getIntLE(offset + 5);
         obj.currentArgStart = buf.getIntLE(offset + 9);
         obj.currentArgLength = buf.getIntLE(offset + 13);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 17);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 49) {
               throw ProtocolException.invalidOffset("Suggestions", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 49 + varPosBase0;
            int suggestionsCount = VarInt.peek(buf, varPos0);
            if (suggestionsCount < 0) {
               throw ProtocolException.invalidVarInt("Suggestions");
            }

            int varIntLen = VarInt.size(suggestionsCount);
            if (suggestionsCount > 4096000) {
               throw ProtocolException.arrayTooLong("Suggestions", suggestionsCount, 4096000);
            }

            if (varPos0 + varIntLen + suggestionsCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Suggestions", varPos0 + varIntLen + suggestionsCount * 1, buf.readableBytes());
            }

            obj.suggestions = new String[suggestionsCount];
            int elemPos = varPos0 + varIntLen;

            for (int i = 0; i < suggestionsCount; i++) {
               int strLen = VarInt.peek(buf, elemPos);
               if (strLen < 0) {
                  throw ProtocolException.invalidVarInt("suggestions[" + i + "]");
               }

               int strVarLen = VarInt.size(strLen);
               if (strLen > 4096000) {
                  throw ProtocolException.stringTooLong("suggestions[" + i + "]", strLen, 4096000);
               }

               if (elemPos + strVarLen + strLen > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("suggestions[" + i + "]", elemPos + strVarLen + strLen, buf.readableBytes());
               }

               obj.suggestions[i] = PacketIO.readVarString(buf, elemPos);
               elemPos += strVarLen + strLen;
            }
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 21);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 49) {
               throw ProtocolException.invalidOffset("Continuations", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 49 + varPosBase1;
            int continuationsCount = VarInt.peek(buf, varPos1);
            if (continuationsCount < 0) {
               throw ProtocolException.invalidVarInt("Continuations");
            }

            int varIntLenx = VarInt.size(continuationsCount);
            if (continuationsCount > 4096000) {
               throw ProtocolException.arrayTooLong("Continuations", continuationsCount, 4096000);
            }

            if (varPos1 + varIntLenx + continuationsCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Continuations", varPos1 + varIntLenx + continuationsCount * 1, buf.readableBytes());
            }

            obj.continuations = new boolean[continuationsCount];

            for (int i = 0; i < continuationsCount; i++) {
               obj.continuations[i] = buf.getByte(varPos1 + varIntLenx + i * 1) != 0;
            }
         }

         if ((nullBits & 4) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 25);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 49) {
               throw ProtocolException.invalidOffset("UsageText", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 49 + varPosBase2;
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
            int varPosBase3 = buf.getIntLE(offset + 29);
            if (varPosBase3 < 0 || varPosBase3 > buf.writerIndex() - offset - 49) {
               throw ProtocolException.invalidOffset("VariantPatterns", varPosBase3, buf.readableBytes());
            }

            int varPos3 = offset + 49 + varPosBase3;
            int variantPatternsCount = VarInt.peek(buf, varPos3);
            if (variantPatternsCount < 0) {
               throw ProtocolException.invalidVarInt("VariantPatterns");
            }

            int varIntLenxx = VarInt.size(variantPatternsCount);
            if (variantPatternsCount > 4096000) {
               throw ProtocolException.arrayTooLong("VariantPatterns", variantPatternsCount, 4096000);
            }

            if (varPos3 + varIntLenxx + variantPatternsCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("VariantPatterns", varPos3 + varIntLenxx + variantPatternsCount * 1, buf.readableBytes());
            }

            obj.variantPatterns = new String[variantPatternsCount];
            int elemPos = varPos3 + varIntLenxx;

            for (int i = 0; i < variantPatternsCount; i++) {
               int strLenx = VarInt.peek(buf, elemPos);
               if (strLenx < 0) {
                  throw ProtocolException.invalidVarInt("variantPatterns[" + i + "]");
               }

               int strVarLenx = VarInt.size(strLenx);
               if (strLenx > 4096000) {
                  throw ProtocolException.stringTooLong("variantPatterns[" + i + "]", strLenx, 4096000);
               }

               if (elemPos + strVarLenx + strLenx > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("variantPatterns[" + i + "]", elemPos + strVarLenx + strLenx, buf.readableBytes());
               }

               obj.variantPatterns[i] = PacketIO.readVarString(buf, elemPos);
               elemPos += strVarLenx + strLenx;
            }
         }

         if ((nullBits & 16) != 0) {
            int varPosBase4 = buf.getIntLE(offset + 33);
            if (varPosBase4 < 0 || varPosBase4 > buf.writerIndex() - offset - 49) {
               throw ProtocolException.invalidOffset("Subcommands", varPosBase4, buf.readableBytes());
            }

            int varPos4 = offset + 49 + varPosBase4;
            int subcommandsCount = VarInt.peek(buf, varPos4);
            if (subcommandsCount < 0) {
               throw ProtocolException.invalidVarInt("Subcommands");
            }

            int varIntLenxxx = VarInt.size(subcommandsCount);
            if (subcommandsCount > 4096000) {
               throw ProtocolException.arrayTooLong("Subcommands", subcommandsCount, 4096000);
            }

            if (varPos4 + varIntLenxxx + subcommandsCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Subcommands", varPos4 + varIntLenxxx + subcommandsCount * 1, buf.readableBytes());
            }

            obj.subcommands = new String[subcommandsCount];
            int elemPos = varPos4 + varIntLenxxx;

            for (int i = 0; i < subcommandsCount; i++) {
               int strLenxx = VarInt.peek(buf, elemPos);
               if (strLenxx < 0) {
                  throw ProtocolException.invalidVarInt("subcommands[" + i + "]");
               }

               int strVarLenxx = VarInt.size(strLenxx);
               if (strLenxx > 4096000) {
                  throw ProtocolException.stringTooLong("subcommands[" + i + "]", strLenxx, 4096000);
               }

               if (elemPos + strVarLenxx + strLenxx > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("subcommands[" + i + "]", elemPos + strVarLenxx + strLenxx, buf.readableBytes());
               }

               obj.subcommands[i] = PacketIO.readVarString(buf, elemPos);
               elemPos += strVarLenxx + strLenxx;
            }
         }

         if ((nullBits & 32) != 0) {
            int varPosBase5 = buf.getIntLE(offset + 37);
            if (varPosBase5 < 0 || varPosBase5 > buf.writerIndex() - offset - 49) {
               throw ProtocolException.invalidOffset("SubcommandHints", varPosBase5, buf.readableBytes());
            }

            int varPos5 = offset + 49 + varPosBase5;
            int subcommandHintsCount = VarInt.peek(buf, varPos5);
            if (subcommandHintsCount < 0) {
               throw ProtocolException.invalidVarInt("SubcommandHints");
            }

            int varIntLenxxxx = VarInt.size(subcommandHintsCount);
            if (subcommandHintsCount > 4096000) {
               throw ProtocolException.arrayTooLong("SubcommandHints", subcommandHintsCount, 4096000);
            }

            if (varPos5 + varIntLenxxxx + subcommandHintsCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("SubcommandHints", varPos5 + varIntLenxxxx + subcommandHintsCount * 1, buf.readableBytes());
            }

            obj.subcommandHints = new String[subcommandHintsCount];
            int elemPos = varPos5 + varIntLenxxxx;

            for (int i = 0; i < subcommandHintsCount; i++) {
               int strLenxxx = VarInt.peek(buf, elemPos);
               if (strLenxxx < 0) {
                  throw ProtocolException.invalidVarInt("subcommandHints[" + i + "]");
               }

               int strVarLenxxx = VarInt.size(strLenxxx);
               if (strLenxxx > 4096000) {
                  throw ProtocolException.stringTooLong("subcommandHints[" + i + "]", strLenxxx, 4096000);
               }

               if (elemPos + strVarLenxxx + strLenxxx > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("subcommandHints[" + i + "]", elemPos + strVarLenxxx + strLenxxx, buf.readableBytes());
               }

               obj.subcommandHints[i] = PacketIO.readVarString(buf, elemPos);
               elemPos += strVarLenxxx + strLenxxx;
            }
         }

         if ((nullBits & 64) != 0) {
            int varPosBase6 = buf.getIntLE(offset + 41);
            if (varPosBase6 < 0 || varPosBase6 > buf.writerIndex() - offset - 49) {
               throw ProtocolException.invalidOffset("OptionalArgs", varPosBase6, buf.readableBytes());
            }

            int varPos6 = offset + 49 + varPosBase6;
            int optionalArgsCount = VarInt.peek(buf, varPos6);
            if (optionalArgsCount < 0) {
               throw ProtocolException.invalidVarInt("OptionalArgs");
            }

            int varIntLenxxxxx = VarInt.size(optionalArgsCount);
            if (optionalArgsCount > 4096000) {
               throw ProtocolException.arrayTooLong("OptionalArgs", optionalArgsCount, 4096000);
            }

            if (varPos6 + varIntLenxxxxx + optionalArgsCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("OptionalArgs", varPos6 + varIntLenxxxxx + optionalArgsCount * 1, buf.readableBytes());
            }

            obj.optionalArgs = new String[optionalArgsCount];
            int elemPos = varPos6 + varIntLenxxxxx;

            for (int i = 0; i < optionalArgsCount; i++) {
               int strLenxxxx = VarInt.peek(buf, elemPos);
               if (strLenxxxx < 0) {
                  throw ProtocolException.invalidVarInt("optionalArgs[" + i + "]");
               }

               int strVarLenxxxx = VarInt.size(strLenxxxx);
               if (strLenxxxx > 4096000) {
                  throw ProtocolException.stringTooLong("optionalArgs[" + i + "]", strLenxxxx, 4096000);
               }

               if (elemPos + strVarLenxxxx + strLenxxxx > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("optionalArgs[" + i + "]", elemPos + strVarLenxxxx + strLenxxxx, buf.readableBytes());
               }

               obj.optionalArgs[i] = PacketIO.readVarString(buf, elemPos);
               elemPos += strVarLenxxxx + strLenxxxx;
            }
         }

         if ((nullBits & 128) != 0) {
            int varPosBase7 = buf.getIntLE(offset + 45);
            if (varPosBase7 < 0 || varPosBase7 > buf.writerIndex() - offset - 49) {
               throw ProtocolException.invalidOffset("OptionalArgHints", varPosBase7, buf.readableBytes());
            }

            int varPos7 = offset + 49 + varPosBase7;
            int optionalArgHintsCount = VarInt.peek(buf, varPos7);
            if (optionalArgHintsCount < 0) {
               throw ProtocolException.invalidVarInt("OptionalArgHints");
            }

            int varIntLenxxxxxx = VarInt.size(optionalArgHintsCount);
            if (optionalArgHintsCount > 4096000) {
               throw ProtocolException.arrayTooLong("OptionalArgHints", optionalArgHintsCount, 4096000);
            }

            if (varPos7 + varIntLenxxxxxx + optionalArgHintsCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("OptionalArgHints", varPos7 + varIntLenxxxxxx + optionalArgHintsCount * 1, buf.readableBytes());
            }

            obj.optionalArgHints = new String[optionalArgHintsCount];
            int elemPos = varPos7 + varIntLenxxxxxx;

            for (int i = 0; i < optionalArgHintsCount; i++) {
               int strLenxxxxx = VarInt.peek(buf, elemPos);
               if (strLenxxxxx < 0) {
                  throw ProtocolException.invalidVarInt("optionalArgHints[" + i + "]");
               }

               int strVarLenxxxxx = VarInt.size(strLenxxxxx);
               if (strLenxxxxx > 4096000) {
                  throw ProtocolException.stringTooLong("optionalArgHints[" + i + "]", strLenxxxxx, 4096000);
               }

               if (elemPos + strVarLenxxxxx + strLenxxxxx > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("optionalArgHints[" + i + "]", elemPos + strVarLenxxxxx + strLenxxxxx, buf.readableBytes());
               }

               obj.optionalArgHints[i] = PacketIO.readVarString(buf, elemPos);
               elemPos += strVarLenxxxxx + strLenxxxxx;
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 49;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 17);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 49) {
            throw ProtocolException.invalidOffset("Suggestions", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 49 + fieldOffset0;
         int arrLen = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            int sl = VarInt.peek(buf, pos0);
            pos0 += VarInt.size(sl) + sl;
         }

         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 21);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 49) {
            throw ProtocolException.invalidOffset("Continuations", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 49 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(arrLen) + arrLen * 1;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 25);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 49) {
            throw ProtocolException.invalidOffset("UsageText", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 49 + fieldOffset2;
         int sl = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(sl) + sl;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 29);
         if (fieldOffset3 < 0 || fieldOffset3 > buf.writerIndex() - offset - 49) {
            throw ProtocolException.invalidOffset("VariantPatterns", fieldOffset3, maxEnd);
         }

         int pos3 = offset + 49 + fieldOffset3;
         int arrLen = VarInt.peek(buf, pos3);
         pos3 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            int sl = VarInt.peek(buf, pos3);
            pos3 += VarInt.size(sl) + sl;
         }

         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      if ((nullBits & 16) != 0) {
         int fieldOffset4 = buf.getIntLE(offset + 33);
         if (fieldOffset4 < 0 || fieldOffset4 > buf.writerIndex() - offset - 49) {
            throw ProtocolException.invalidOffset("Subcommands", fieldOffset4, maxEnd);
         }

         int pos4 = offset + 49 + fieldOffset4;
         int arrLen = VarInt.peek(buf, pos4);
         pos4 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            int sl = VarInt.peek(buf, pos4);
            pos4 += VarInt.size(sl) + sl;
         }

         if (pos4 - offset > maxEnd) {
            maxEnd = pos4 - offset;
         }
      }

      if ((nullBits & 32) != 0) {
         int fieldOffset5 = buf.getIntLE(offset + 37);
         if (fieldOffset5 < 0 || fieldOffset5 > buf.writerIndex() - offset - 49) {
            throw ProtocolException.invalidOffset("SubcommandHints", fieldOffset5, maxEnd);
         }

         int pos5 = offset + 49 + fieldOffset5;
         int arrLen = VarInt.peek(buf, pos5);
         pos5 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            int sl = VarInt.peek(buf, pos5);
            pos5 += VarInt.size(sl) + sl;
         }

         if (pos5 - offset > maxEnd) {
            maxEnd = pos5 - offset;
         }
      }

      if ((nullBits & 64) != 0) {
         int fieldOffset6 = buf.getIntLE(offset + 41);
         if (fieldOffset6 < 0 || fieldOffset6 > buf.writerIndex() - offset - 49) {
            throw ProtocolException.invalidOffset("OptionalArgs", fieldOffset6, maxEnd);
         }

         int pos6 = offset + 49 + fieldOffset6;
         int arrLen = VarInt.peek(buf, pos6);
         pos6 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            int sl = VarInt.peek(buf, pos6);
            pos6 += VarInt.size(sl) + sl;
         }

         if (pos6 - offset > maxEnd) {
            maxEnd = pos6 - offset;
         }
      }

      if ((nullBits & 128) != 0) {
         int fieldOffset7 = buf.getIntLE(offset + 45);
         if (fieldOffset7 < 0 || fieldOffset7 > buf.writerIndex() - offset - 49) {
            throw ProtocolException.invalidOffset("OptionalArgHints", fieldOffset7, maxEnd);
         }

         int pos7 = offset + 49 + fieldOffset7;
         int arrLen = VarInt.peek(buf, pos7);
         pos7 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            int sl = VarInt.peek(buf, pos7);
            pos7 += VarInt.size(sl) + sl;
         }

         if (pos7 - offset > maxEnd) {
            maxEnd = pos7 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 49L;
   }

   public static int getStartPosition(MemorySegment mem) {
      return getStartPosition(mem, 0);
   }

   public static int getStartPosition(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   public static int getLength(MemorySegment mem) {
      return getLength(mem, 0);
   }

   public static int getLength(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 5));
   }

   @Nullable
   public static String[] getSuggestions(MemorySegment mem) {
      return getSuggestions(mem, 0);
   }

   @Nullable
   public static String[] getSuggestions(MemorySegment mem, int offset) {
      if (!hasSuggestions(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 17, 49, "Suggestions");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Suggestions", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Suggestions", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Suggestions", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               String[] data = new String[len];

               for (int i = 0; i < len; i++) {
                  long sp = VarInt.getWithLength(mem, off);
                  int n = (int)sp + (int)(sp >>> 32);
                  data[i] = PacketIO.readVarString("Suggestions", mem, off, 16384000, PacketIO.UTF8);
                  off += n;
               }

               return data;
            }
         }
      }
   }

   @Nullable
   public static boolean[] getContinuations(MemorySegment mem) {
      return getContinuations(mem, 0);
   }

   @Nullable
   public static boolean[] getContinuations(MemorySegment mem, int offset) {
      if (!hasContinuations(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 21, 49, "Continuations");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Continuations", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Continuations", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Continuations", off + lenOffset + len * 1, (int)mem.byteSize());
            } else {
               off += lenOffset;
               boolean[] data = new boolean[len];

               for (int i = 0; i < len; i++) {
                  data[i] = mem.get(PacketIO.PROTO_BOOL, (long)(off + i));
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
         ? PacketIO.readVarString("UsageText", mem, offset + getValidatedOffset(mem, offset, 25, 49, "UsageText"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static int getCurrentArgStart(MemorySegment mem) {
      return getCurrentArgStart(mem, 0);
   }

   public static int getCurrentArgStart(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 9));
   }

   public static int getCurrentArgLength(MemorySegment mem) {
      return getCurrentArgLength(mem, 0);
   }

   public static int getCurrentArgLength(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 13));
   }

   @Nullable
   public static String[] getVariantPatterns(MemorySegment mem) {
      return getVariantPatterns(mem, 0);
   }

   @Nullable
   public static String[] getVariantPatterns(MemorySegment mem, int offset) {
      if (!hasVariantPatterns(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 29, 49, "VariantPatterns");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("VariantPatterns", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("VariantPatterns", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("VariantPatterns", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               String[] data = new String[len];

               for (int i = 0; i < len; i++) {
                  long sp = VarInt.getWithLength(mem, off);
                  int n = (int)sp + (int)(sp >>> 32);
                  data[i] = PacketIO.readVarString("VariantPatterns", mem, off, 16384000, PacketIO.UTF8);
                  off += n;
               }

               return data;
            }
         }
      }
   }

   @Nullable
   public static String[] getSubcommands(MemorySegment mem) {
      return getSubcommands(mem, 0);
   }

   @Nullable
   public static String[] getSubcommands(MemorySegment mem, int offset) {
      if (!hasSubcommands(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 33, 49, "Subcommands");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Subcommands", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Subcommands", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Subcommands", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               String[] data = new String[len];

               for (int i = 0; i < len; i++) {
                  long sp = VarInt.getWithLength(mem, off);
                  int n = (int)sp + (int)(sp >>> 32);
                  data[i] = PacketIO.readVarString("Subcommands", mem, off, 16384000, PacketIO.UTF8);
                  off += n;
               }

               return data;
            }
         }
      }
   }

   @Nullable
   public static String[] getSubcommandHints(MemorySegment mem) {
      return getSubcommandHints(mem, 0);
   }

   @Nullable
   public static String[] getSubcommandHints(MemorySegment mem, int offset) {
      if (!hasSubcommandHints(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 37, 49, "SubcommandHints");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("SubcommandHints", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("SubcommandHints", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("SubcommandHints", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               String[] data = new String[len];

               for (int i = 0; i < len; i++) {
                  long sp = VarInt.getWithLength(mem, off);
                  int n = (int)sp + (int)(sp >>> 32);
                  data[i] = PacketIO.readVarString("SubcommandHints", mem, off, 16384000, PacketIO.UTF8);
                  off += n;
               }

               return data;
            }
         }
      }
   }

   @Nullable
   public static String[] getOptionalArgs(MemorySegment mem) {
      return getOptionalArgs(mem, 0);
   }

   @Nullable
   public static String[] getOptionalArgs(MemorySegment mem, int offset) {
      if (!hasOptionalArgs(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 41, 49, "OptionalArgs");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("OptionalArgs", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("OptionalArgs", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("OptionalArgs", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               String[] data = new String[len];

               for (int i = 0; i < len; i++) {
                  long sp = VarInt.getWithLength(mem, off);
                  int n = (int)sp + (int)(sp >>> 32);
                  data[i] = PacketIO.readVarString("OptionalArgs", mem, off, 16384000, PacketIO.UTF8);
                  off += n;
               }

               return data;
            }
         }
      }
   }

   @Nullable
   public static String[] getOptionalArgHints(MemorySegment mem) {
      return getOptionalArgHints(mem, 0);
   }

   @Nullable
   public static String[] getOptionalArgHints(MemorySegment mem, int offset) {
      if (!hasOptionalArgHints(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 45, 49, "OptionalArgHints");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("OptionalArgHints", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("OptionalArgHints", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("OptionalArgHints", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               String[] data = new String[len];

               for (int i = 0; i < len; i++) {
                  long sp = VarInt.getWithLength(mem, off);
                  int n = (int)sp + (int)(sp >>> 32);
                  data[i] = PacketIO.readVarString("OptionalArgHints", mem, off, 16384000, PacketIO.UTF8);
                  off += n;
               }

               return data;
            }
         }
      }
   }

   public static boolean hasSuggestions(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasContinuations(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasUsageText(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasVariantPatterns(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   public static boolean hasSubcommands(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 16) != 0;
   }

   public static boolean hasSubcommandHints(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 32) != 0;
   }

   public static boolean hasOptionalArgs(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 64) != 0;
   }

   public static boolean hasOptionalArgHints(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 128) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static CommandSuggestionsResponse toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static CommandSuggestionsResponse toObject(MemorySegment mem, int offset) {
      if (offset + 49 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("CommandSuggestionsResponse", offset + 49, (int)mem.byteSize());
      } else {
         String[] suggestions = null;
         if (hasSuggestions(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 17, 49, "Suggestions");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Suggestions", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Suggestions", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Suggestions", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            suggestions = new String[len];

            for (int i = 0; i < len; i++) {
               long sp = VarInt.getWithLength(mem, off);
               int n = (int)sp + (int)(sp >>> 32);
               suggestions[i] = PacketIO.readVarString("Suggestions", mem, off, 16384000, PacketIO.UTF8);
               off += n;
            }
         }

         boolean[] continuations = null;
         if (hasContinuations(mem, offset)) {
            int offx = offset + getValidatedOffset(mem, offset, 21, 49, "Continuations");
            long packedx = VarInt.getWithLength(mem, offx);
            int lenx = (int)packedx;
            if (lenx < 0) {
               throw ProtocolException.negativeLength("Continuations", lenx);
            }

            if (lenx > 4096000) {
               throw ProtocolException.arrayTooLong("Continuations", lenx, 4096000);
            }

            int lenOffset = (int)(packedx >>> 32);
            if (offx + lenOffset + lenx * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Continuations", offx + lenOffset + lenx * 1, (int)mem.byteSize());
            }

            offx += lenOffset;
            continuations = new boolean[lenx];

            for (int i = 0; i < lenx; i++) {
               continuations[i] = mem.get(PacketIO.PROTO_BOOL, (long)(offx + i));
            }
         }

         String[] variantPatterns = null;
         if (hasVariantPatterns(mem, offset)) {
            int offxx = offset + getValidatedOffset(mem, offset, 29, 49, "VariantPatterns");
            long packedxx = VarInt.getWithLength(mem, offxx);
            int lenxx = (int)packedxx;
            if (lenxx < 0) {
               throw ProtocolException.negativeLength("VariantPatterns", lenxx);
            }

            if (lenxx > 4096000) {
               throw ProtocolException.arrayTooLong("VariantPatterns", lenxx, 4096000);
            }

            int lenOffset = (int)(packedxx >>> 32);
            if (offxx + lenOffset + lenxx > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("VariantPatterns", offxx + lenOffset + lenxx, (int)mem.byteSize());
            }

            offxx += lenOffset;
            variantPatterns = new String[lenxx];

            for (int i = 0; i < lenxx; i++) {
               long sp = VarInt.getWithLength(mem, offxx);
               int n = (int)sp + (int)(sp >>> 32);
               variantPatterns[i] = PacketIO.readVarString("VariantPatterns", mem, offxx, 16384000, PacketIO.UTF8);
               offxx += n;
            }
         }

         String[] subcommands = null;
         if (hasSubcommands(mem, offset)) {
            int offxxx = offset + getValidatedOffset(mem, offset, 33, 49, "Subcommands");
            long packedxxx = VarInt.getWithLength(mem, offxxx);
            int lenxxx = (int)packedxxx;
            if (lenxxx < 0) {
               throw ProtocolException.negativeLength("Subcommands", lenxxx);
            }

            if (lenxxx > 4096000) {
               throw ProtocolException.arrayTooLong("Subcommands", lenxxx, 4096000);
            }

            int lenOffset = (int)(packedxxx >>> 32);
            if (offxxx + lenOffset + lenxxx > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Subcommands", offxxx + lenOffset + lenxxx, (int)mem.byteSize());
            }

            offxxx += lenOffset;
            subcommands = new String[lenxxx];

            for (int i = 0; i < lenxxx; i++) {
               long sp = VarInt.getWithLength(mem, offxxx);
               int n = (int)sp + (int)(sp >>> 32);
               subcommands[i] = PacketIO.readVarString("Subcommands", mem, offxxx, 16384000, PacketIO.UTF8);
               offxxx += n;
            }
         }

         String[] subcommandHints = null;
         if (hasSubcommandHints(mem, offset)) {
            int offxxxx = offset + getValidatedOffset(mem, offset, 37, 49, "SubcommandHints");
            long packedxxxx = VarInt.getWithLength(mem, offxxxx);
            int lenxxxx = (int)packedxxxx;
            if (lenxxxx < 0) {
               throw ProtocolException.negativeLength("SubcommandHints", lenxxxx);
            }

            if (lenxxxx > 4096000) {
               throw ProtocolException.arrayTooLong("SubcommandHints", lenxxxx, 4096000);
            }

            int lenOffset = (int)(packedxxxx >>> 32);
            if (offxxxx + lenOffset + lenxxxx > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("SubcommandHints", offxxxx + lenOffset + lenxxxx, (int)mem.byteSize());
            }

            offxxxx += lenOffset;
            subcommandHints = new String[lenxxxx];

            for (int i = 0; i < lenxxxx; i++) {
               long sp = VarInt.getWithLength(mem, offxxxx);
               int n = (int)sp + (int)(sp >>> 32);
               subcommandHints[i] = PacketIO.readVarString("SubcommandHints", mem, offxxxx, 16384000, PacketIO.UTF8);
               offxxxx += n;
            }
         }

         String[] optionalArgs = null;
         if (hasOptionalArgs(mem, offset)) {
            int offxxxxx = offset + getValidatedOffset(mem, offset, 41, 49, "OptionalArgs");
            long packedxxxxx = VarInt.getWithLength(mem, offxxxxx);
            int lenxxxxx = (int)packedxxxxx;
            if (lenxxxxx < 0) {
               throw ProtocolException.negativeLength("OptionalArgs", lenxxxxx);
            }

            if (lenxxxxx > 4096000) {
               throw ProtocolException.arrayTooLong("OptionalArgs", lenxxxxx, 4096000);
            }

            int lenOffset = (int)(packedxxxxx >>> 32);
            if (offxxxxx + lenOffset + lenxxxxx > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("OptionalArgs", offxxxxx + lenOffset + lenxxxxx, (int)mem.byteSize());
            }

            offxxxxx += lenOffset;
            optionalArgs = new String[lenxxxxx];

            for (int i = 0; i < lenxxxxx; i++) {
               long sp = VarInt.getWithLength(mem, offxxxxx);
               int n = (int)sp + (int)(sp >>> 32);
               optionalArgs[i] = PacketIO.readVarString("OptionalArgs", mem, offxxxxx, 16384000, PacketIO.UTF8);
               offxxxxx += n;
            }
         }

         String[] optionalArgHints = null;
         if (hasOptionalArgHints(mem, offset)) {
            int offxxxxxx = offset + getValidatedOffset(mem, offset, 45, 49, "OptionalArgHints");
            long packedxxxxxx = VarInt.getWithLength(mem, offxxxxxx);
            int lenxxxxxx = (int)packedxxxxxx;
            if (lenxxxxxx < 0) {
               throw ProtocolException.negativeLength("OptionalArgHints", lenxxxxxx);
            }

            if (lenxxxxxx > 4096000) {
               throw ProtocolException.arrayTooLong("OptionalArgHints", lenxxxxxx, 4096000);
            }

            int lenOffset = (int)(packedxxxxxx >>> 32);
            if (offxxxxxx + lenOffset + lenxxxxxx > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("OptionalArgHints", offxxxxxx + lenOffset + lenxxxxxx, (int)mem.byteSize());
            }

            offxxxxxx += lenOffset;
            optionalArgHints = new String[lenxxxxxx];

            for (int i = 0; i < lenxxxxxx; i++) {
               long sp = VarInt.getWithLength(mem, offxxxxxx);
               int n = (int)sp + (int)(sp >>> 32);
               optionalArgHints[i] = PacketIO.readVarString("OptionalArgHints", mem, offxxxxxx, 16384000, PacketIO.UTF8);
               offxxxxxx += n;
            }
         }

         return new CommandSuggestionsResponse(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 5)),
            suggestions,
            continuations,
            hasUsageText(mem, offset)
               ? PacketIO.readVarString("UsageText", mem, offset + getValidatedOffset(mem, offset, 25, 49, "UsageText"), 4096000, PacketIO.UTF8)
               : null,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 9)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 13)),
            variantPatterns,
            subcommands,
            subcommandHints,
            optionalArgs,
            optionalArgHints
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.suggestions != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.continuations != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.usageText != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.variantPatterns != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.subcommands != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.subcommandHints != null) {
         nullBits = (byte)(nullBits | 32);
      }

      if (this.optionalArgs != null) {
         nullBits = (byte)(nullBits | 64);
      }

      if (this.optionalArgHints != null) {
         nullBits = (byte)(nullBits | 128);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.startPosition);
      buf.writeIntLE(this.length);
      buf.writeIntLE(this.currentArgStart);
      buf.writeIntLE(this.currentArgLength);
      int suggestionsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int continuationsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int usageTextOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int variantPatternsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int subcommandsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int subcommandHintsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int optionalArgsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int optionalArgHintsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.suggestions != null) {
         buf.setIntLE(suggestionsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.suggestions.length > 4096000) {
            throw ProtocolException.arrayTooLong("Suggestions", this.suggestions.length, 4096000);
         }

         VarInt.write(buf, this.suggestions.length);

         for (String item : this.suggestions) {
            PacketIO.writeVarString(buf, item, 4096000);
         }
      } else {
         buf.setIntLE(suggestionsOffsetSlot, -1);
      }

      if (this.continuations != null) {
         buf.setIntLE(continuationsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.continuations.length > 4096000) {
            throw ProtocolException.arrayTooLong("Continuations", this.continuations.length, 4096000);
         }

         VarInt.write(buf, this.continuations.length);

         for (boolean item : this.continuations) {
            buf.writeByte(item ? 1 : 0);
         }
      } else {
         buf.setIntLE(continuationsOffsetSlot, -1);
      }

      if (this.usageText != null) {
         buf.setIntLE(usageTextOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.usageText, 4096000);
      } else {
         buf.setIntLE(usageTextOffsetSlot, -1);
      }

      if (this.variantPatterns != null) {
         buf.setIntLE(variantPatternsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.variantPatterns.length > 4096000) {
            throw ProtocolException.arrayTooLong("VariantPatterns", this.variantPatterns.length, 4096000);
         }

         VarInt.write(buf, this.variantPatterns.length);

         for (String item : this.variantPatterns) {
            PacketIO.writeVarString(buf, item, 4096000);
         }
      } else {
         buf.setIntLE(variantPatternsOffsetSlot, -1);
      }

      if (this.subcommands != null) {
         buf.setIntLE(subcommandsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.subcommands.length > 4096000) {
            throw ProtocolException.arrayTooLong("Subcommands", this.subcommands.length, 4096000);
         }

         VarInt.write(buf, this.subcommands.length);

         for (String item : this.subcommands) {
            PacketIO.writeVarString(buf, item, 4096000);
         }
      } else {
         buf.setIntLE(subcommandsOffsetSlot, -1);
      }

      if (this.subcommandHints != null) {
         buf.setIntLE(subcommandHintsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.subcommandHints.length > 4096000) {
            throw ProtocolException.arrayTooLong("SubcommandHints", this.subcommandHints.length, 4096000);
         }

         VarInt.write(buf, this.subcommandHints.length);

         for (String item : this.subcommandHints) {
            PacketIO.writeVarString(buf, item, 4096000);
         }
      } else {
         buf.setIntLE(subcommandHintsOffsetSlot, -1);
      }

      if (this.optionalArgs != null) {
         buf.setIntLE(optionalArgsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.optionalArgs.length > 4096000) {
            throw ProtocolException.arrayTooLong("OptionalArgs", this.optionalArgs.length, 4096000);
         }

         VarInt.write(buf, this.optionalArgs.length);

         for (String item : this.optionalArgs) {
            PacketIO.writeVarString(buf, item, 4096000);
         }
      } else {
         buf.setIntLE(optionalArgsOffsetSlot, -1);
      }

      if (this.optionalArgHints != null) {
         buf.setIntLE(optionalArgHintsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.optionalArgHints.length > 4096000) {
            throw ProtocolException.arrayTooLong("OptionalArgHints", this.optionalArgHints.length, 4096000);
         }

         VarInt.write(buf, this.optionalArgHints.length);

         for (String item : this.optionalArgHints) {
            PacketIO.writeVarString(buf, item, 4096000);
         }
      } else {
         buf.setIntLE(optionalArgHintsOffsetSlot, -1);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.suggestions != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.continuations != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.usageText != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.variantPatterns != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.subcommands != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.subcommandHints != null) {
         nullBits = (byte)(nullBits | 32);
      }

      if (this.optionalArgs != null) {
         nullBits = (byte)(nullBits | 64);
      }

      if (this.optionalArgHints != null) {
         nullBits = (byte)(nullBits | 128);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.startPosition);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 5), this.length);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 9), this.currentArgStart);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 13), this.currentArgLength);
      int varOffset = offset + 49;
      if (this.suggestions != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 17), varOffset - offset - 49);
         if (this.suggestions.length > 4096000) {
            throw ProtocolException.arrayTooLong("Suggestions", this.suggestions.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.suggestions.length);
         int suggestionsValueOffset = 0;

         for (int i = 0; i < this.suggestions.length; i++) {
            suggestionsValueOffset += PacketIO.writeVarString(mem, varOffset + suggestionsValueOffset, this.suggestions[i], 16384000);
         }

         varOffset += suggestionsValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 17), -1);
      }

      if (this.continuations != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 21), varOffset - offset - 49);
         if (this.continuations.length > 4096000) {
            throw ProtocolException.arrayTooLong("Continuations", this.continuations.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.continuations.length);

         for (int i = 0; i < this.continuations.length; i++) {
            mem.set(PacketIO.PROTO_BOOL, varOffset + i, this.continuations[i]);
         }

         varOffset += this.continuations.length;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 21), -1);
      }

      if (this.usageText != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 25), varOffset - offset - 49);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.usageText, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 25), -1);
      }

      if (this.variantPatterns != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 29), varOffset - offset - 49);
         if (this.variantPatterns.length > 4096000) {
            throw ProtocolException.arrayTooLong("VariantPatterns", this.variantPatterns.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.variantPatterns.length);
         int variantPatternsValueOffset = 0;

         for (int i = 0; i < this.variantPatterns.length; i++) {
            variantPatternsValueOffset += PacketIO.writeVarString(mem, varOffset + variantPatternsValueOffset, this.variantPatterns[i], 16384000);
         }

         varOffset += variantPatternsValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 29), -1);
      }

      if (this.subcommands != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 33), varOffset - offset - 49);
         if (this.subcommands.length > 4096000) {
            throw ProtocolException.arrayTooLong("Subcommands", this.subcommands.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.subcommands.length);
         int subcommandsValueOffset = 0;

         for (int i = 0; i < this.subcommands.length; i++) {
            subcommandsValueOffset += PacketIO.writeVarString(mem, varOffset + subcommandsValueOffset, this.subcommands[i], 16384000);
         }

         varOffset += subcommandsValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 33), -1);
      }

      if (this.subcommandHints != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 37), varOffset - offset - 49);
         if (this.subcommandHints.length > 4096000) {
            throw ProtocolException.arrayTooLong("SubcommandHints", this.subcommandHints.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.subcommandHints.length);
         int subcommandHintsValueOffset = 0;

         for (int i = 0; i < this.subcommandHints.length; i++) {
            subcommandHintsValueOffset += PacketIO.writeVarString(mem, varOffset + subcommandHintsValueOffset, this.subcommandHints[i], 16384000);
         }

         varOffset += subcommandHintsValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 37), -1);
      }

      if (this.optionalArgs != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 41), varOffset - offset - 49);
         if (this.optionalArgs.length > 4096000) {
            throw ProtocolException.arrayTooLong("OptionalArgs", this.optionalArgs.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.optionalArgs.length);
         int optionalArgsValueOffset = 0;

         for (int i = 0; i < this.optionalArgs.length; i++) {
            optionalArgsValueOffset += PacketIO.writeVarString(mem, varOffset + optionalArgsValueOffset, this.optionalArgs[i], 16384000);
         }

         varOffset += optionalArgsValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 41), -1);
      }

      if (this.optionalArgHints != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 45), varOffset - offset - 49);
         if (this.optionalArgHints.length > 4096000) {
            throw ProtocolException.arrayTooLong("OptionalArgHints", this.optionalArgHints.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.optionalArgHints.length);
         int optionalArgHintsValueOffset = 0;

         for (int i = 0; i < this.optionalArgHints.length; i++) {
            optionalArgHintsValueOffset += PacketIO.writeVarString(mem, varOffset + optionalArgHintsValueOffset, this.optionalArgHints[i], 16384000);
         }

         varOffset += optionalArgHintsValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 45), -1);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 49;
      if (this.suggestions != null) {
         int suggestionsSize = 0;

         for (String elem : this.suggestions) {
            suggestionsSize += PacketIO.stringSize(elem);
         }

         size += VarInt.size(this.suggestions.length) + suggestionsSize;
      }

      if (this.continuations != null) {
         size += VarInt.size(this.continuations.length) + this.continuations.length * 1;
      }

      if (this.usageText != null) {
         size += PacketIO.stringSize(this.usageText);
      }

      if (this.variantPatterns != null) {
         int variantPatternsSize = 0;

         for (String elem : this.variantPatterns) {
            variantPatternsSize += PacketIO.stringSize(elem);
         }

         size += VarInt.size(this.variantPatterns.length) + variantPatternsSize;
      }

      if (this.subcommands != null) {
         int subcommandsSize = 0;

         for (String elem : this.subcommands) {
            subcommandsSize += PacketIO.stringSize(elem);
         }

         size += VarInt.size(this.subcommands.length) + subcommandsSize;
      }

      if (this.subcommandHints != null) {
         int subcommandHintsSize = 0;

         for (String elem : this.subcommandHints) {
            subcommandHintsSize += PacketIO.stringSize(elem);
         }

         size += VarInt.size(this.subcommandHints.length) + subcommandHintsSize;
      }

      if (this.optionalArgs != null) {
         int optionalArgsSize = 0;

         for (String elem : this.optionalArgs) {
            optionalArgsSize += PacketIO.stringSize(elem);
         }

         size += VarInt.size(this.optionalArgs.length) + optionalArgsSize;
      }

      if (this.optionalArgHints != null) {
         int optionalArgHintsSize = 0;

         for (String elem : this.optionalArgHints) {
            optionalArgHintsSize += PacketIO.stringSize(elem);
         }

         size += VarInt.size(this.optionalArgHints.length) + optionalArgHintsSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 49) {
         return ValidationResult.error("Buffer too small: expected at least 49 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int suggestionsOffset = buffer.getIntLE(offset + 17);
            if (suggestionsOffset < 0 || suggestionsOffset > buffer.writerIndex() - offset - 49) {
               return ValidationResult.error("Invalid offset for Suggestions");
            }

            int pos = offset + 49 + suggestionsOffset;
            int suggestionsCount = VarInt.peek(buffer, pos);
            if (suggestionsCount < 0) {
               return ValidationResult.error("Invalid array count for Suggestions");
            }

            if (suggestionsCount > 4096000) {
               return ValidationResult.error("Suggestions exceeds max length 4096000");
            }

            pos += VarInt.size(suggestionsCount);

            for (int i = 0; i < suggestionsCount; i++) {
               int strLen = VarInt.peek(buffer, pos);
               if (strLen < 0) {
                  return ValidationResult.error("Invalid string length in Suggestions");
               }

               pos += VarInt.size(strLen);
               pos += strLen;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading string in Suggestions");
               }
            }
         }

         if ((nullBits & 2) != 0) {
            int continuationsOffset = buffer.getIntLE(offset + 21);
            if (continuationsOffset < 0 || continuationsOffset > buffer.writerIndex() - offset - 49) {
               return ValidationResult.error("Invalid offset for Continuations");
            }

            int posx = offset + 49 + continuationsOffset;
            int continuationsCount = VarInt.peek(buffer, posx);
            if (continuationsCount < 0) {
               return ValidationResult.error("Invalid array count for Continuations");
            }

            if (continuationsCount > 4096000) {
               return ValidationResult.error("Continuations exceeds max length 4096000");
            }

            posx += VarInt.size(continuationsCount);
            posx += continuationsCount * 1;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Continuations");
            }
         }

         if ((nullBits & 4) != 0) {
            int usageTextOffset = buffer.getIntLE(offset + 25);
            if (usageTextOffset < 0 || usageTextOffset > buffer.writerIndex() - offset - 49) {
               return ValidationResult.error("Invalid offset for UsageText");
            }

            int posxx = offset + 49 + usageTextOffset;
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
            int variantPatternsOffset = buffer.getIntLE(offset + 29);
            if (variantPatternsOffset < 0 || variantPatternsOffset > buffer.writerIndex() - offset - 49) {
               return ValidationResult.error("Invalid offset for VariantPatterns");
            }

            int posxxx = offset + 49 + variantPatternsOffset;
            int variantPatternsCount = VarInt.peek(buffer, posxxx);
            if (variantPatternsCount < 0) {
               return ValidationResult.error("Invalid array count for VariantPatterns");
            }

            if (variantPatternsCount > 4096000) {
               return ValidationResult.error("VariantPatterns exceeds max length 4096000");
            }

            posxxx += VarInt.size(variantPatternsCount);

            for (int i = 0; i < variantPatternsCount; i++) {
               int strLenx = VarInt.peek(buffer, posxxx);
               if (strLenx < 0) {
                  return ValidationResult.error("Invalid string length in VariantPatterns");
               }

               posxxx += VarInt.size(strLenx);
               posxxx += strLenx;
               if (posxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading string in VariantPatterns");
               }
            }
         }

         if ((nullBits & 16) != 0) {
            int subcommandsOffset = buffer.getIntLE(offset + 33);
            if (subcommandsOffset < 0 || subcommandsOffset > buffer.writerIndex() - offset - 49) {
               return ValidationResult.error("Invalid offset for Subcommands");
            }

            int posxxxx = offset + 49 + subcommandsOffset;
            int subcommandsCount = VarInt.peek(buffer, posxxxx);
            if (subcommandsCount < 0) {
               return ValidationResult.error("Invalid array count for Subcommands");
            }

            if (subcommandsCount > 4096000) {
               return ValidationResult.error("Subcommands exceeds max length 4096000");
            }

            posxxxx += VarInt.size(subcommandsCount);

            for (int i = 0; i < subcommandsCount; i++) {
               int strLenxx = VarInt.peek(buffer, posxxxx);
               if (strLenxx < 0) {
                  return ValidationResult.error("Invalid string length in Subcommands");
               }

               posxxxx += VarInt.size(strLenxx);
               posxxxx += strLenxx;
               if (posxxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading string in Subcommands");
               }
            }
         }

         if ((nullBits & 32) != 0) {
            int subcommandHintsOffset = buffer.getIntLE(offset + 37);
            if (subcommandHintsOffset < 0 || subcommandHintsOffset > buffer.writerIndex() - offset - 49) {
               return ValidationResult.error("Invalid offset for SubcommandHints");
            }

            int posxxxxx = offset + 49 + subcommandHintsOffset;
            int subcommandHintsCount = VarInt.peek(buffer, posxxxxx);
            if (subcommandHintsCount < 0) {
               return ValidationResult.error("Invalid array count for SubcommandHints");
            }

            if (subcommandHintsCount > 4096000) {
               return ValidationResult.error("SubcommandHints exceeds max length 4096000");
            }

            posxxxxx += VarInt.size(subcommandHintsCount);

            for (int i = 0; i < subcommandHintsCount; i++) {
               int strLenxxx = VarInt.peek(buffer, posxxxxx);
               if (strLenxxx < 0) {
                  return ValidationResult.error("Invalid string length in SubcommandHints");
               }

               posxxxxx += VarInt.size(strLenxxx);
               posxxxxx += strLenxxx;
               if (posxxxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading string in SubcommandHints");
               }
            }
         }

         if ((nullBits & 64) != 0) {
            int optionalArgsOffset = buffer.getIntLE(offset + 41);
            if (optionalArgsOffset < 0 || optionalArgsOffset > buffer.writerIndex() - offset - 49) {
               return ValidationResult.error("Invalid offset for OptionalArgs");
            }

            int posxxxxxx = offset + 49 + optionalArgsOffset;
            int optionalArgsCount = VarInt.peek(buffer, posxxxxxx);
            if (optionalArgsCount < 0) {
               return ValidationResult.error("Invalid array count for OptionalArgs");
            }

            if (optionalArgsCount > 4096000) {
               return ValidationResult.error("OptionalArgs exceeds max length 4096000");
            }

            posxxxxxx += VarInt.size(optionalArgsCount);

            for (int i = 0; i < optionalArgsCount; i++) {
               int strLenxxxx = VarInt.peek(buffer, posxxxxxx);
               if (strLenxxxx < 0) {
                  return ValidationResult.error("Invalid string length in OptionalArgs");
               }

               posxxxxxx += VarInt.size(strLenxxxx);
               posxxxxxx += strLenxxxx;
               if (posxxxxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading string in OptionalArgs");
               }
            }
         }

         if ((nullBits & 128) != 0) {
            int optionalArgHintsOffset = buffer.getIntLE(offset + 45);
            if (optionalArgHintsOffset < 0 || optionalArgHintsOffset > buffer.writerIndex() - offset - 49) {
               return ValidationResult.error("Invalid offset for OptionalArgHints");
            }

            int posxxxxxxx = offset + 49 + optionalArgHintsOffset;
            int optionalArgHintsCount = VarInt.peek(buffer, posxxxxxxx);
            if (optionalArgHintsCount < 0) {
               return ValidationResult.error("Invalid array count for OptionalArgHints");
            }

            if (optionalArgHintsCount > 4096000) {
               return ValidationResult.error("OptionalArgHints exceeds max length 4096000");
            }

            posxxxxxxx += VarInt.size(optionalArgHintsCount);

            for (int i = 0; i < optionalArgHintsCount; i++) {
               int strLenxxxxx = VarInt.peek(buffer, posxxxxxxx);
               if (strLenxxxxx < 0) {
                  return ValidationResult.error("Invalid string length in OptionalArgHints");
               }

               posxxxxxxx += VarInt.size(strLenxxxxx);
               posxxxxxxx += strLenxxxxx;
               if (posxxxxxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading string in OptionalArgHints");
               }
            }
         }

         return ValidationResult.OK;
      }
   }

   public CommandSuggestionsResponse clone() {
      CommandSuggestionsResponse copy = new CommandSuggestionsResponse();
      copy.startPosition = this.startPosition;
      copy.length = this.length;
      copy.suggestions = this.suggestions != null ? Arrays.copyOf(this.suggestions, this.suggestions.length) : null;
      copy.continuations = this.continuations != null ? Arrays.copyOf(this.continuations, this.continuations.length) : null;
      copy.usageText = this.usageText;
      copy.currentArgStart = this.currentArgStart;
      copy.currentArgLength = this.currentArgLength;
      copy.variantPatterns = this.variantPatterns != null ? Arrays.copyOf(this.variantPatterns, this.variantPatterns.length) : null;
      copy.subcommands = this.subcommands != null ? Arrays.copyOf(this.subcommands, this.subcommands.length) : null;
      copy.subcommandHints = this.subcommandHints != null ? Arrays.copyOf(this.subcommandHints, this.subcommandHints.length) : null;
      copy.optionalArgs = this.optionalArgs != null ? Arrays.copyOf(this.optionalArgs, this.optionalArgs.length) : null;
      copy.optionalArgHints = this.optionalArgHints != null ? Arrays.copyOf(this.optionalArgHints, this.optionalArgHints.length) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof CommandSuggestionsResponse other)
            ? false
            : this.startPosition == other.startPosition
               && this.length == other.length
               && Arrays.equals((Object[])this.suggestions, (Object[])other.suggestions)
               && Arrays.equals(this.continuations, other.continuations)
               && Objects.equals(this.usageText, other.usageText)
               && this.currentArgStart == other.currentArgStart
               && this.currentArgLength == other.currentArgLength
               && Arrays.equals((Object[])this.variantPatterns, (Object[])other.variantPatterns)
               && Arrays.equals((Object[])this.subcommands, (Object[])other.subcommands)
               && Arrays.equals((Object[])this.subcommandHints, (Object[])other.subcommandHints)
               && Arrays.equals((Object[])this.optionalArgs, (Object[])other.optionalArgs)
               && Arrays.equals((Object[])this.optionalArgHints, (Object[])other.optionalArgHints);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Integer.hashCode(this.startPosition);
      result = 31 * result + Integer.hashCode(this.length);
      result = 31 * result + Arrays.hashCode((Object[])this.suggestions);
      result = 31 * result + Arrays.hashCode(this.continuations);
      result = 31 * result + Objects.hashCode(this.usageText);
      result = 31 * result + Integer.hashCode(this.currentArgStart);
      result = 31 * result + Integer.hashCode(this.currentArgLength);
      result = 31 * result + Arrays.hashCode((Object[])this.variantPatterns);
      result = 31 * result + Arrays.hashCode((Object[])this.subcommands);
      result = 31 * result + Arrays.hashCode((Object[])this.subcommandHints);
      result = 31 * result + Arrays.hashCode((Object[])this.optionalArgs);
      return 31 * result + Arrays.hashCode((Object[])this.optionalArgHints);
   }
}
