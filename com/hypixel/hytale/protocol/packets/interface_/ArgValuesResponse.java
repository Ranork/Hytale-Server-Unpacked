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

public class ArgValuesResponse implements Packet, ToClientPacket {
   public static final int PACKET_ID = 247;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 3;
   public static final int VARIABLE_BLOCK_START = 14;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public String argTypeId;
   @Nullable
   public String[] values;
   @Nullable
   public boolean[] continuations;
   public boolean isComplete;

   @Override
   public int getId() {
      return 247;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public ArgValuesResponse() {
   }

   public ArgValuesResponse(@Nullable String argTypeId, @Nullable String[] values, @Nullable boolean[] continuations, boolean isComplete) {
      this.argTypeId = argTypeId;
      this.values = values;
      this.continuations = continuations;
      this.isComplete = isComplete;
   }

   public ArgValuesResponse(@Nonnull ArgValuesResponse other) {
      this.argTypeId = other.argTypeId;
      this.values = other.values;
      this.continuations = other.continuations;
      this.isComplete = other.isComplete;
   }

   @Nonnull
   public static ArgValuesResponse deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 14) {
         throw ProtocolException.bufferTooSmall("ArgValuesResponse", 14, buf.readableBytes() - offset);
      } else {
         ArgValuesResponse obj = new ArgValuesResponse();
         byte nullBits = buf.getByte(offset);
         obj.isComplete = buf.getByte(offset + 1) != 0;
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 2);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 14) {
               throw ProtocolException.invalidOffset("ArgTypeId", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 14 + varPosBase0;
            int argTypeIdLen = VarInt.peek(buf, varPos0);
            if (argTypeIdLen < 0) {
               throw ProtocolException.invalidVarInt("ArgTypeId");
            }

            int argTypeIdVarIntLen = VarInt.size(argTypeIdLen);
            if (argTypeIdLen > 4096000) {
               throw ProtocolException.stringTooLong("ArgTypeId", argTypeIdLen, 4096000);
            }

            if (varPos0 + argTypeIdVarIntLen + argTypeIdLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("ArgTypeId", varPos0 + argTypeIdVarIntLen + argTypeIdLen, buf.readableBytes());
            }

            obj.argTypeId = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 6);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 14) {
               throw ProtocolException.invalidOffset("Values", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 14 + varPosBase1;
            int valuesCount = VarInt.peek(buf, varPos1);
            if (valuesCount < 0) {
               throw ProtocolException.invalidVarInt("Values");
            }

            int varIntLen = VarInt.size(valuesCount);
            if (valuesCount > 4096000) {
               throw ProtocolException.arrayTooLong("Values", valuesCount, 4096000);
            }

            if (varPos1 + varIntLen + valuesCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Values", varPos1 + varIntLen + valuesCount * 1, buf.readableBytes());
            }

            obj.values = new String[valuesCount];
            int elemPos = varPos1 + varIntLen;

            for (int i = 0; i < valuesCount; i++) {
               int strLen = VarInt.peek(buf, elemPos);
               if (strLen < 0) {
                  throw ProtocolException.invalidVarInt("values[" + i + "]");
               }

               int strVarLen = VarInt.size(strLen);
               if (strLen > 4096000) {
                  throw ProtocolException.stringTooLong("values[" + i + "]", strLen, 4096000);
               }

               if (elemPos + strVarLen + strLen > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("values[" + i + "]", elemPos + strVarLen + strLen, buf.readableBytes());
               }

               obj.values[i] = PacketIO.readVarString(buf, elemPos);
               elemPos += strVarLen + strLen;
            }
         }

         if ((nullBits & 4) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 10);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 14) {
               throw ProtocolException.invalidOffset("Continuations", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 14 + varPosBase2;
            int continuationsCount = VarInt.peek(buf, varPos2);
            if (continuationsCount < 0) {
               throw ProtocolException.invalidVarInt("Continuations");
            }

            int varIntLenx = VarInt.size(continuationsCount);
            if (continuationsCount > 4096000) {
               throw ProtocolException.arrayTooLong("Continuations", continuationsCount, 4096000);
            }

            if (varPos2 + varIntLenx + continuationsCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Continuations", varPos2 + varIntLenx + continuationsCount * 1, buf.readableBytes());
            }

            obj.continuations = new boolean[continuationsCount];

            for (int i = 0; i < continuationsCount; i++) {
               obj.continuations[i] = buf.getByte(varPos2 + varIntLenx + i * 1) != 0;
            }
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
            throw ProtocolException.invalidOffset("ArgTypeId", fieldOffset0, maxEnd);
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
            throw ProtocolException.invalidOffset("Values", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 14 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            int sl = VarInt.peek(buf, pos1);
            pos1 += VarInt.size(sl) + sl;
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 10);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 14) {
            throw ProtocolException.invalidOffset("Continuations", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 14 + fieldOffset2;
         int arrLen = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(arrLen) + arrLen * 1;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 14L;
   }

   @Nullable
   public static String getArgTypeId(MemorySegment mem) {
      return getArgTypeId(mem, 0);
   }

   @Nullable
   public static String getArgTypeId(MemorySegment mem, int offset) {
      return hasArgTypeId(mem, offset)
         ? PacketIO.readVarString("ArgTypeId", mem, offset + getValidatedOffset(mem, offset, 2, 14, "ArgTypeId"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static String[] getValues(MemorySegment mem) {
      return getValues(mem, 0);
   }

   @Nullable
   public static String[] getValues(MemorySegment mem, int offset) {
      if (!hasValues(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 6, 14, "Values");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Values", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Values", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Values", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               String[] data = new String[len];

               for (int i = 0; i < len; i++) {
                  long sp = VarInt.getWithLength(mem, off);
                  int n = (int)sp + (int)(sp >>> 32);
                  data[i] = PacketIO.readVarString("Values", mem, off, 16384000, PacketIO.UTF8);
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
         int off = offset + getValidatedOffset(mem, offset, 10, 14, "Continuations");
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

   public static boolean getIsComplete(MemorySegment mem) {
      return getIsComplete(mem, 0);
   }

   public static boolean getIsComplete(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1));
   }

   public static boolean hasArgTypeId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasValues(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasContinuations(MemorySegment mem, int offset) {
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

   public static ArgValuesResponse toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ArgValuesResponse toObject(MemorySegment mem, int offset) {
      if (offset + 14 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ArgValuesResponse", offset + 14, (int)mem.byteSize());
      } else {
         String[] values = null;
         if (hasValues(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 6, 14, "Values");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Values", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Values", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Values", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            values = new String[len];

            for (int i = 0; i < len; i++) {
               long sp = VarInt.getWithLength(mem, off);
               int n = (int)sp + (int)(sp >>> 32);
               values[i] = PacketIO.readVarString("Values", mem, off, 16384000, PacketIO.UTF8);
               off += n;
            }
         }

         boolean[] continuations = null;
         if (hasContinuations(mem, offset)) {
            int offx = offset + getValidatedOffset(mem, offset, 10, 14, "Continuations");
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

         return new ArgValuesResponse(
            hasArgTypeId(mem, offset)
               ? PacketIO.readVarString("ArgTypeId", mem, offset + getValidatedOffset(mem, offset, 2, 14, "ArgTypeId"), 4096000, PacketIO.UTF8)
               : null,
            values,
            continuations,
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1))
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.argTypeId != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.values != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.continuations != null) {
         nullBits = (byte)(nullBits | 4);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.isComplete ? 1 : 0);
      int argTypeIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int valuesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int continuationsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.argTypeId != null) {
         buf.setIntLE(argTypeIdOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.argTypeId, 4096000);
      } else {
         buf.setIntLE(argTypeIdOffsetSlot, -1);
      }

      if (this.values != null) {
         buf.setIntLE(valuesOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.values.length > 4096000) {
            throw ProtocolException.arrayTooLong("Values", this.values.length, 4096000);
         }

         VarInt.write(buf, this.values.length);

         for (String item : this.values) {
            PacketIO.writeVarString(buf, item, 4096000);
         }
      } else {
         buf.setIntLE(valuesOffsetSlot, -1);
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
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.argTypeId != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.values != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.continuations != null) {
         nullBits = (byte)(nullBits | 4);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BOOL, offset + 1, this.isComplete);
      int varOffset = offset + 14;
      if (this.argTypeId != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 2), varOffset - offset - 14);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.argTypeId, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 2), -1);
      }

      if (this.values != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 6), varOffset - offset - 14);
         if (this.values.length > 4096000) {
            throw ProtocolException.arrayTooLong("Values", this.values.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.values.length);
         int valuesValueOffset = 0;

         for (int i = 0; i < this.values.length; i++) {
            valuesValueOffset += PacketIO.writeVarString(mem, varOffset + valuesValueOffset, this.values[i], 16384000);
         }

         varOffset += valuesValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 6), -1);
      }

      if (this.continuations != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 10), varOffset - offset - 14);
         if (this.continuations.length > 4096000) {
            throw ProtocolException.arrayTooLong("Continuations", this.continuations.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.continuations.length);

         for (int i = 0; i < this.continuations.length; i++) {
            mem.set(PacketIO.PROTO_BOOL, varOffset + i, this.continuations[i]);
         }

         varOffset += this.continuations.length;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 10), -1);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 14;
      if (this.argTypeId != null) {
         size += PacketIO.stringSize(this.argTypeId);
      }

      if (this.values != null) {
         int valuesSize = 0;

         for (String elem : this.values) {
            valuesSize += PacketIO.stringSize(elem);
         }

         size += VarInt.size(this.values.length) + valuesSize;
      }

      if (this.continuations != null) {
         size += VarInt.size(this.continuations.length) + this.continuations.length * 1;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 14) {
         return ValidationResult.error("Buffer too small: expected at least 14 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int argTypeIdOffset = buffer.getIntLE(offset + 2);
            if (argTypeIdOffset < 0 || argTypeIdOffset > buffer.writerIndex() - offset - 14) {
               return ValidationResult.error("Invalid offset for ArgTypeId");
            }

            int pos = offset + 14 + argTypeIdOffset;
            int argTypeIdLen = VarInt.peek(buffer, pos);
            if (argTypeIdLen < 0) {
               return ValidationResult.error("Invalid string length for ArgTypeId");
            }

            if (argTypeIdLen > 4096000) {
               return ValidationResult.error("ArgTypeId exceeds max length 4096000");
            }

            pos += VarInt.size(argTypeIdLen);
            pos += argTypeIdLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading ArgTypeId");
            }
         }

         if ((nullBits & 2) != 0) {
            int valuesOffset = buffer.getIntLE(offset + 6);
            if (valuesOffset < 0 || valuesOffset > buffer.writerIndex() - offset - 14) {
               return ValidationResult.error("Invalid offset for Values");
            }

            int posx = offset + 14 + valuesOffset;
            int valuesCount = VarInt.peek(buffer, posx);
            if (valuesCount < 0) {
               return ValidationResult.error("Invalid array count for Values");
            }

            if (valuesCount > 4096000) {
               return ValidationResult.error("Values exceeds max length 4096000");
            }

            posx += VarInt.size(valuesCount);

            for (int i = 0; i < valuesCount; i++) {
               int strLen = VarInt.peek(buffer, posx);
               if (strLen < 0) {
                  return ValidationResult.error("Invalid string length in Values");
               }

               posx += VarInt.size(strLen);
               posx += strLen;
               if (posx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading string in Values");
               }
            }
         }

         if ((nullBits & 4) != 0) {
            int continuationsOffset = buffer.getIntLE(offset + 10);
            if (continuationsOffset < 0 || continuationsOffset > buffer.writerIndex() - offset - 14) {
               return ValidationResult.error("Invalid offset for Continuations");
            }

            int posxx = offset + 14 + continuationsOffset;
            int continuationsCount = VarInt.peek(buffer, posxx);
            if (continuationsCount < 0) {
               return ValidationResult.error("Invalid array count for Continuations");
            }

            if (continuationsCount > 4096000) {
               return ValidationResult.error("Continuations exceeds max length 4096000");
            }

            posxx += VarInt.size(continuationsCount);
            posxx += continuationsCount * 1;
            if (posxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Continuations");
            }
         }

         return ValidationResult.OK;
      }
   }

   public ArgValuesResponse clone() {
      ArgValuesResponse copy = new ArgValuesResponse();
      copy.argTypeId = this.argTypeId;
      copy.values = this.values != null ? Arrays.copyOf(this.values, this.values.length) : null;
      copy.continuations = this.continuations != null ? Arrays.copyOf(this.continuations, this.continuations.length) : null;
      copy.isComplete = this.isComplete;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ArgValuesResponse other)
            ? false
            : Objects.equals(this.argTypeId, other.argTypeId)
               && Arrays.equals((Object[])this.values, (Object[])other.values)
               && Arrays.equals(this.continuations, other.continuations)
               && this.isComplete == other.isComplete;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.argTypeId);
      result = 31 * result + Arrays.hashCode((Object[])this.values);
      result = 31 * result + Arrays.hashCode(this.continuations);
      return 31 * result + Boolean.hashCode(this.isComplete);
   }
}
