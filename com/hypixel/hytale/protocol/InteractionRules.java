package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InteractionRules {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 17;
   public static final int VARIABLE_FIELD_COUNT = 4;
   public static final int VARIABLE_BLOCK_START = 33;
   public static final int MAX_SIZE = 16384053;
   @Nullable
   public InteractionType[] blockedBy;
   @Nullable
   public InteractionType[] blocking;
   @Nullable
   public InteractionType[] interruptedBy;
   @Nullable
   public InteractionType[] interrupting;
   public int blockedByBypassIndex;
   public int blockingBypassIndex;
   public int interruptedByBypassIndex;
   public int interruptingBypassIndex;

   public InteractionRules() {
   }

   public InteractionRules(
      @Nullable InteractionType[] blockedBy,
      @Nullable InteractionType[] blocking,
      @Nullable InteractionType[] interruptedBy,
      @Nullable InteractionType[] interrupting,
      int blockedByBypassIndex,
      int blockingBypassIndex,
      int interruptedByBypassIndex,
      int interruptingBypassIndex
   ) {
      this.blockedBy = blockedBy;
      this.blocking = blocking;
      this.interruptedBy = interruptedBy;
      this.interrupting = interrupting;
      this.blockedByBypassIndex = blockedByBypassIndex;
      this.blockingBypassIndex = blockingBypassIndex;
      this.interruptedByBypassIndex = interruptedByBypassIndex;
      this.interruptingBypassIndex = interruptingBypassIndex;
   }

   public InteractionRules(@Nonnull InteractionRules other) {
      this.blockedBy = other.blockedBy;
      this.blocking = other.blocking;
      this.interruptedBy = other.interruptedBy;
      this.interrupting = other.interrupting;
      this.blockedByBypassIndex = other.blockedByBypassIndex;
      this.blockingBypassIndex = other.blockingBypassIndex;
      this.interruptedByBypassIndex = other.interruptedByBypassIndex;
      this.interruptingBypassIndex = other.interruptingBypassIndex;
   }

   @Nonnull
   public static InteractionRules deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 33) {
         throw ProtocolException.bufferTooSmall("InteractionRules", 33, buf.readableBytes() - offset);
      } else {
         InteractionRules obj = new InteractionRules();
         byte nullBits = buf.getByte(offset);
         obj.blockedByBypassIndex = buf.getIntLE(offset + 1);
         obj.blockingBypassIndex = buf.getIntLE(offset + 5);
         obj.interruptedByBypassIndex = buf.getIntLE(offset + 9);
         obj.interruptingBypassIndex = buf.getIntLE(offset + 13);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 17);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 33) {
               throw ProtocolException.invalidOffset("BlockedBy", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 33 + varPosBase0;
            int blockedByCount = VarInt.peek(buf, varPos0);
            if (blockedByCount < 0) {
               throw ProtocolException.invalidVarInt("BlockedBy");
            }

            int varIntLen = VarInt.size(blockedByCount);
            if (blockedByCount > 4096000) {
               throw ProtocolException.arrayTooLong("BlockedBy", blockedByCount, 4096000);
            }

            if (varPos0 + varIntLen + blockedByCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("BlockedBy", varPos0 + varIntLen + blockedByCount * 1, buf.readableBytes());
            }

            obj.blockedBy = new InteractionType[blockedByCount];
            int elemPos = varPos0 + varIntLen;

            for (int i = 0; i < blockedByCount; i++) {
               obj.blockedBy[i] = InteractionType.fromValue(buf.getByte(elemPos));
               elemPos++;
            }
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 21);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 33) {
               throw ProtocolException.invalidOffset("Blocking", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 33 + varPosBase1;
            int blockingCount = VarInt.peek(buf, varPos1);
            if (blockingCount < 0) {
               throw ProtocolException.invalidVarInt("Blocking");
            }

            int varIntLenx = VarInt.size(blockingCount);
            if (blockingCount > 4096000) {
               throw ProtocolException.arrayTooLong("Blocking", blockingCount, 4096000);
            }

            if (varPos1 + varIntLenx + blockingCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Blocking", varPos1 + varIntLenx + blockingCount * 1, buf.readableBytes());
            }

            obj.blocking = new InteractionType[blockingCount];
            int elemPos = varPos1 + varIntLenx;

            for (int i = 0; i < blockingCount; i++) {
               obj.blocking[i] = InteractionType.fromValue(buf.getByte(elemPos));
               elemPos++;
            }
         }

         if ((nullBits & 4) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 25);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 33) {
               throw ProtocolException.invalidOffset("InterruptedBy", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 33 + varPosBase2;
            int interruptedByCount = VarInt.peek(buf, varPos2);
            if (interruptedByCount < 0) {
               throw ProtocolException.invalidVarInt("InterruptedBy");
            }

            int varIntLenxx = VarInt.size(interruptedByCount);
            if (interruptedByCount > 4096000) {
               throw ProtocolException.arrayTooLong("InterruptedBy", interruptedByCount, 4096000);
            }

            if (varPos2 + varIntLenxx + interruptedByCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("InterruptedBy", varPos2 + varIntLenxx + interruptedByCount * 1, buf.readableBytes());
            }

            obj.interruptedBy = new InteractionType[interruptedByCount];
            int elemPos = varPos2 + varIntLenxx;

            for (int i = 0; i < interruptedByCount; i++) {
               obj.interruptedBy[i] = InteractionType.fromValue(buf.getByte(elemPos));
               elemPos++;
            }
         }

         if ((nullBits & 8) != 0) {
            int varPosBase3 = buf.getIntLE(offset + 29);
            if (varPosBase3 < 0 || varPosBase3 > buf.writerIndex() - offset - 33) {
               throw ProtocolException.invalidOffset("Interrupting", varPosBase3, buf.readableBytes());
            }

            int varPos3 = offset + 33 + varPosBase3;
            int interruptingCount = VarInt.peek(buf, varPos3);
            if (interruptingCount < 0) {
               throw ProtocolException.invalidVarInt("Interrupting");
            }

            int varIntLenxxx = VarInt.size(interruptingCount);
            if (interruptingCount > 4096000) {
               throw ProtocolException.arrayTooLong("Interrupting", interruptingCount, 4096000);
            }

            if (varPos3 + varIntLenxxx + interruptingCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Interrupting", varPos3 + varIntLenxxx + interruptingCount * 1, buf.readableBytes());
            }

            obj.interrupting = new InteractionType[interruptingCount];
            int elemPos = varPos3 + varIntLenxxx;

            for (int i = 0; i < interruptingCount; i++) {
               obj.interrupting[i] = InteractionType.fromValue(buf.getByte(elemPos));
               elemPos++;
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 33;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 17);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 33) {
            throw ProtocolException.invalidOffset("BlockedBy", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 33 + fieldOffset0;
         int arrLen = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(arrLen) + arrLen * 1;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 21);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 33) {
            throw ProtocolException.invalidOffset("Blocking", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 33 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(arrLen) + arrLen * 1;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 25);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 33) {
            throw ProtocolException.invalidOffset("InterruptedBy", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 33 + fieldOffset2;
         int arrLen = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(arrLen) + arrLen * 1;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 29);
         if (fieldOffset3 < 0 || fieldOffset3 > buf.writerIndex() - offset - 33) {
            throw ProtocolException.invalidOffset("Interrupting", fieldOffset3, maxEnd);
         }

         int pos3 = offset + 33 + fieldOffset3;
         int arrLen = VarInt.peek(buf, pos3);
         pos3 += VarInt.size(arrLen) + arrLen * 1;
         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 33L;
   }

   @Nullable
   public static InteractionType[] getBlockedBy(MemorySegment mem) {
      return getBlockedBy(mem, 0);
   }

   @Nullable
   public static InteractionType[] getBlockedBy(MemorySegment mem, int offset) {
      if (!hasBlockedBy(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 17, 33, "BlockedBy");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("BlockedBy", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("BlockedBy", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("BlockedBy", off + lenOffset + len * 1, (int)mem.byteSize());
            } else {
               off += lenOffset;
               InteractionType[] data = new InteractionType[len];

               for (int i = 0; i < len; i++) {
                  data[i] = InteractionType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(off + i * 1)));
               }

               return data;
            }
         }
      }
   }

   @Nullable
   public static InteractionType[] getBlocking(MemorySegment mem) {
      return getBlocking(mem, 0);
   }

   @Nullable
   public static InteractionType[] getBlocking(MemorySegment mem, int offset) {
      if (!hasBlocking(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 21, 33, "Blocking");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Blocking", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Blocking", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Blocking", off + lenOffset + len * 1, (int)mem.byteSize());
            } else {
               off += lenOffset;
               InteractionType[] data = new InteractionType[len];

               for (int i = 0; i < len; i++) {
                  data[i] = InteractionType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(off + i * 1)));
               }

               return data;
            }
         }
      }
   }

   @Nullable
   public static InteractionType[] getInterruptedBy(MemorySegment mem) {
      return getInterruptedBy(mem, 0);
   }

   @Nullable
   public static InteractionType[] getInterruptedBy(MemorySegment mem, int offset) {
      if (!hasInterruptedBy(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 25, 33, "InterruptedBy");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("InterruptedBy", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("InterruptedBy", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("InterruptedBy", off + lenOffset + len * 1, (int)mem.byteSize());
            } else {
               off += lenOffset;
               InteractionType[] data = new InteractionType[len];

               for (int i = 0; i < len; i++) {
                  data[i] = InteractionType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(off + i * 1)));
               }

               return data;
            }
         }
      }
   }

   @Nullable
   public static InteractionType[] getInterrupting(MemorySegment mem) {
      return getInterrupting(mem, 0);
   }

   @Nullable
   public static InteractionType[] getInterrupting(MemorySegment mem, int offset) {
      if (!hasInterrupting(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 29, 33, "Interrupting");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Interrupting", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Interrupting", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Interrupting", off + lenOffset + len * 1, (int)mem.byteSize());
            } else {
               off += lenOffset;
               InteractionType[] data = new InteractionType[len];

               for (int i = 0; i < len; i++) {
                  data[i] = InteractionType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(off + i * 1)));
               }

               return data;
            }
         }
      }
   }

   public static int getBlockedByBypassIndex(MemorySegment mem) {
      return getBlockedByBypassIndex(mem, 0);
   }

   public static int getBlockedByBypassIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   public static int getBlockingBypassIndex(MemorySegment mem) {
      return getBlockingBypassIndex(mem, 0);
   }

   public static int getBlockingBypassIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 5));
   }

   public static int getInterruptedByBypassIndex(MemorySegment mem) {
      return getInterruptedByBypassIndex(mem, 0);
   }

   public static int getInterruptedByBypassIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 9));
   }

   public static int getInterruptingBypassIndex(MemorySegment mem) {
      return getInterruptingBypassIndex(mem, 0);
   }

   public static int getInterruptingBypassIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 13));
   }

   public static boolean hasBlockedBy(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasBlocking(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasInterruptedBy(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasInterrupting(MemorySegment mem, int offset) {
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

   public static InteractionRules toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static InteractionRules toObject(MemorySegment mem, int offset) {
      if (offset + 33 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("InteractionRules", offset + 33, (int)mem.byteSize());
      } else {
         InteractionType[] blockedBy = null;
         if (hasBlockedBy(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 17, 33, "BlockedBy");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("BlockedBy", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("BlockedBy", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("BlockedBy", off + lenOffset + len * 1, (int)mem.byteSize());
            }

            off += lenOffset;
            blockedBy = new InteractionType[len];

            for (int i = 0; i < len; i++) {
               blockedBy[i] = InteractionType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(off + i * 1)));
            }
         }

         InteractionType[] blocking = null;
         if (hasBlocking(mem, offset)) {
            int offx = offset + getValidatedOffset(mem, offset, 21, 33, "Blocking");
            long packedx = VarInt.getWithLength(mem, offx);
            int lenx = (int)packedx;
            if (lenx < 0) {
               throw ProtocolException.negativeLength("Blocking", lenx);
            }

            if (lenx > 4096000) {
               throw ProtocolException.arrayTooLong("Blocking", lenx, 4096000);
            }

            int lenOffset = (int)(packedx >>> 32);
            if (offx + lenOffset + lenx * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Blocking", offx + lenOffset + lenx * 1, (int)mem.byteSize());
            }

            offx += lenOffset;
            blocking = new InteractionType[lenx];

            for (int i = 0; i < lenx; i++) {
               blocking[i] = InteractionType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offx + i * 1)));
            }
         }

         InteractionType[] interruptedBy = null;
         if (hasInterruptedBy(mem, offset)) {
            int offxx = offset + getValidatedOffset(mem, offset, 25, 33, "InterruptedBy");
            long packedxx = VarInt.getWithLength(mem, offxx);
            int lenxx = (int)packedxx;
            if (lenxx < 0) {
               throw ProtocolException.negativeLength("InterruptedBy", lenxx);
            }

            if (lenxx > 4096000) {
               throw ProtocolException.arrayTooLong("InterruptedBy", lenxx, 4096000);
            }

            int lenOffset = (int)(packedxx >>> 32);
            if (offxx + lenOffset + lenxx * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("InterruptedBy", offxx + lenOffset + lenxx * 1, (int)mem.byteSize());
            }

            offxx += lenOffset;
            interruptedBy = new InteractionType[lenxx];

            for (int i = 0; i < lenxx; i++) {
               interruptedBy[i] = InteractionType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offxx + i * 1)));
            }
         }

         InteractionType[] interrupting = null;
         if (hasInterrupting(mem, offset)) {
            int offxxx = offset + getValidatedOffset(mem, offset, 29, 33, "Interrupting");
            long packedxxx = VarInt.getWithLength(mem, offxxx);
            int lenxxx = (int)packedxxx;
            if (lenxxx < 0) {
               throw ProtocolException.negativeLength("Interrupting", lenxxx);
            }

            if (lenxxx > 4096000) {
               throw ProtocolException.arrayTooLong("Interrupting", lenxxx, 4096000);
            }

            int lenOffset = (int)(packedxxx >>> 32);
            if (offxxx + lenOffset + lenxxx * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Interrupting", offxxx + lenOffset + lenxxx * 1, (int)mem.byteSize());
            }

            offxxx += lenOffset;
            interrupting = new InteractionType[lenxxx];

            for (int i = 0; i < lenxxx; i++) {
               interrupting[i] = InteractionType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offxxx + i * 1)));
            }
         }

         return new InteractionRules(
            blockedBy,
            blocking,
            interruptedBy,
            interrupting,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 5)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 9)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 13))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.blockedBy != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.blocking != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.interruptedBy != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.interrupting != null) {
         nullBits = (byte)(nullBits | 8);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.blockedByBypassIndex);
      buf.writeIntLE(this.blockingBypassIndex);
      buf.writeIntLE(this.interruptedByBypassIndex);
      buf.writeIntLE(this.interruptingBypassIndex);
      int blockedByOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int blockingOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int interruptedByOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int interruptingOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.blockedBy != null) {
         buf.setIntLE(blockedByOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.blockedBy.length > 4096000) {
            throw ProtocolException.arrayTooLong("BlockedBy", this.blockedBy.length, 4096000);
         }

         VarInt.write(buf, this.blockedBy.length);

         for (InteractionType item : this.blockedBy) {
            buf.writeByte(item.getValue());
         }
      } else {
         buf.setIntLE(blockedByOffsetSlot, -1);
      }

      if (this.blocking != null) {
         buf.setIntLE(blockingOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.blocking.length > 4096000) {
            throw ProtocolException.arrayTooLong("Blocking", this.blocking.length, 4096000);
         }

         VarInt.write(buf, this.blocking.length);

         for (InteractionType item : this.blocking) {
            buf.writeByte(item.getValue());
         }
      } else {
         buf.setIntLE(blockingOffsetSlot, -1);
      }

      if (this.interruptedBy != null) {
         buf.setIntLE(interruptedByOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.interruptedBy.length > 4096000) {
            throw ProtocolException.arrayTooLong("InterruptedBy", this.interruptedBy.length, 4096000);
         }

         VarInt.write(buf, this.interruptedBy.length);

         for (InteractionType item : this.interruptedBy) {
            buf.writeByte(item.getValue());
         }
      } else {
         buf.setIntLE(interruptedByOffsetSlot, -1);
      }

      if (this.interrupting != null) {
         buf.setIntLE(interruptingOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.interrupting.length > 4096000) {
            throw ProtocolException.arrayTooLong("Interrupting", this.interrupting.length, 4096000);
         }

         VarInt.write(buf, this.interrupting.length);

         for (InteractionType item : this.interrupting) {
            buf.writeByte(item.getValue());
         }
      } else {
         buf.setIntLE(interruptingOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.blockedBy != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.blocking != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.interruptedBy != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.interrupting != null) {
         nullBits = (byte)(nullBits | 8);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.blockedByBypassIndex);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 5), this.blockingBypassIndex);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 9), this.interruptedByBypassIndex);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 13), this.interruptingBypassIndex);
      int varOffset = offset + 33;
      if (this.blockedBy != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 17), varOffset - offset - 33);
         if (this.blockedBy.length > 4096000) {
            throw ProtocolException.arrayTooLong("BlockedBy", this.blockedBy.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.blockedBy.length);

         for (int i = 0; i < this.blockedBy.length; i++) {
            mem.set(PacketIO.PROTO_BYTE, (long)(varOffset + i * 1), (byte)this.blockedBy[i].getValue());
         }

         varOffset += this.blockedBy.length * 1;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 17), -1);
      }

      if (this.blocking != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 21), varOffset - offset - 33);
         if (this.blocking.length > 4096000) {
            throw ProtocolException.arrayTooLong("Blocking", this.blocking.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.blocking.length);

         for (int i = 0; i < this.blocking.length; i++) {
            mem.set(PacketIO.PROTO_BYTE, (long)(varOffset + i * 1), (byte)this.blocking[i].getValue());
         }

         varOffset += this.blocking.length * 1;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 21), -1);
      }

      if (this.interruptedBy != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 25), varOffset - offset - 33);
         if (this.interruptedBy.length > 4096000) {
            throw ProtocolException.arrayTooLong("InterruptedBy", this.interruptedBy.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.interruptedBy.length);

         for (int i = 0; i < this.interruptedBy.length; i++) {
            mem.set(PacketIO.PROTO_BYTE, (long)(varOffset + i * 1), (byte)this.interruptedBy[i].getValue());
         }

         varOffset += this.interruptedBy.length * 1;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 25), -1);
      }

      if (this.interrupting != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 29), varOffset - offset - 33);
         if (this.interrupting.length > 4096000) {
            throw ProtocolException.arrayTooLong("Interrupting", this.interrupting.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.interrupting.length);

         for (int i = 0; i < this.interrupting.length; i++) {
            mem.set(PacketIO.PROTO_BYTE, (long)(varOffset + i * 1), (byte)this.interrupting[i].getValue());
         }

         varOffset += this.interrupting.length * 1;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 29), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 33;
      if (this.blockedBy != null) {
         size += VarInt.size(this.blockedBy.length) + this.blockedBy.length * 1;
      }

      if (this.blocking != null) {
         size += VarInt.size(this.blocking.length) + this.blocking.length * 1;
      }

      if (this.interruptedBy != null) {
         size += VarInt.size(this.interruptedBy.length) + this.interruptedBy.length * 1;
      }

      if (this.interrupting != null) {
         size += VarInt.size(this.interrupting.length) + this.interrupting.length * 1;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 33) {
         return ValidationResult.error("Buffer too small: expected at least 33 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int blockedByOffset = buffer.getIntLE(offset + 17);
            if (blockedByOffset < 0 || blockedByOffset > buffer.writerIndex() - offset - 33) {
               return ValidationResult.error("Invalid offset for BlockedBy");
            }

            int pos = offset + 33 + blockedByOffset;
            int blockedByCount = VarInt.peek(buffer, pos);
            if (blockedByCount < 0) {
               return ValidationResult.error("Invalid array count for BlockedBy");
            }

            if (blockedByCount > 4096000) {
               return ValidationResult.error("BlockedBy exceeds max length 4096000");
            }

            pos += VarInt.size(blockedByCount);
            if (pos + blockedByCount * 1L > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading BlockedBy");
            }

            for (int i = 0; i < blockedByCount; i++) {
               int v = buffer.getByte(pos) & 255;
               if (v >= 25) {
                  return ValidationResult.error("Invalid InteractionType value for BlockedBy[i]");
               }

               pos++;
            }
         }

         if ((nullBits & 2) != 0) {
            int blockingOffset = buffer.getIntLE(offset + 21);
            if (blockingOffset < 0 || blockingOffset > buffer.writerIndex() - offset - 33) {
               return ValidationResult.error("Invalid offset for Blocking");
            }

            int posx = offset + 33 + blockingOffset;
            int blockingCount = VarInt.peek(buffer, posx);
            if (blockingCount < 0) {
               return ValidationResult.error("Invalid array count for Blocking");
            }

            if (blockingCount > 4096000) {
               return ValidationResult.error("Blocking exceeds max length 4096000");
            }

            posx += VarInt.size(blockingCount);
            if (posx + blockingCount * 1L > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Blocking");
            }

            for (int i = 0; i < blockingCount; i++) {
               int v = buffer.getByte(posx) & 255;
               if (v >= 25) {
                  return ValidationResult.error("Invalid InteractionType value for Blocking[i]");
               }

               posx++;
            }
         }

         if ((nullBits & 4) != 0) {
            int interruptedByOffset = buffer.getIntLE(offset + 25);
            if (interruptedByOffset < 0 || interruptedByOffset > buffer.writerIndex() - offset - 33) {
               return ValidationResult.error("Invalid offset for InterruptedBy");
            }

            int posxx = offset + 33 + interruptedByOffset;
            int interruptedByCount = VarInt.peek(buffer, posxx);
            if (interruptedByCount < 0) {
               return ValidationResult.error("Invalid array count for InterruptedBy");
            }

            if (interruptedByCount > 4096000) {
               return ValidationResult.error("InterruptedBy exceeds max length 4096000");
            }

            posxx += VarInt.size(interruptedByCount);
            if (posxx + interruptedByCount * 1L > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading InterruptedBy");
            }

            for (int i = 0; i < interruptedByCount; i++) {
               int v = buffer.getByte(posxx) & 255;
               if (v >= 25) {
                  return ValidationResult.error("Invalid InteractionType value for InterruptedBy[i]");
               }

               posxx++;
            }
         }

         if ((nullBits & 8) != 0) {
            int interruptingOffset = buffer.getIntLE(offset + 29);
            if (interruptingOffset < 0 || interruptingOffset > buffer.writerIndex() - offset - 33) {
               return ValidationResult.error("Invalid offset for Interrupting");
            }

            int posxxx = offset + 33 + interruptingOffset;
            int interruptingCount = VarInt.peek(buffer, posxxx);
            if (interruptingCount < 0) {
               return ValidationResult.error("Invalid array count for Interrupting");
            }

            if (interruptingCount > 4096000) {
               return ValidationResult.error("Interrupting exceeds max length 4096000");
            }

            posxxx += VarInt.size(interruptingCount);
            if (posxxx + interruptingCount * 1L > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Interrupting");
            }

            for (int i = 0; i < interruptingCount; i++) {
               int v = buffer.getByte(posxxx) & 255;
               if (v >= 25) {
                  return ValidationResult.error("Invalid InteractionType value for Interrupting[i]");
               }

               posxxx++;
            }
         }

         return ValidationResult.OK;
      }
   }

   public InteractionRules clone() {
      InteractionRules copy = new InteractionRules();
      copy.blockedBy = this.blockedBy != null ? Arrays.copyOf(this.blockedBy, this.blockedBy.length) : null;
      copy.blocking = this.blocking != null ? Arrays.copyOf(this.blocking, this.blocking.length) : null;
      copy.interruptedBy = this.interruptedBy != null ? Arrays.copyOf(this.interruptedBy, this.interruptedBy.length) : null;
      copy.interrupting = this.interrupting != null ? Arrays.copyOf(this.interrupting, this.interrupting.length) : null;
      copy.blockedByBypassIndex = this.blockedByBypassIndex;
      copy.blockingBypassIndex = this.blockingBypassIndex;
      copy.interruptedByBypassIndex = this.interruptedByBypassIndex;
      copy.interruptingBypassIndex = this.interruptingBypassIndex;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof InteractionRules other)
            ? false
            : Arrays.equals((Object[])this.blockedBy, (Object[])other.blockedBy)
               && Arrays.equals((Object[])this.blocking, (Object[])other.blocking)
               && Arrays.equals((Object[])this.interruptedBy, (Object[])other.interruptedBy)
               && Arrays.equals((Object[])this.interrupting, (Object[])other.interrupting)
               && this.blockedByBypassIndex == other.blockedByBypassIndex
               && this.blockingBypassIndex == other.blockingBypassIndex
               && this.interruptedByBypassIndex == other.interruptedByBypassIndex
               && this.interruptingBypassIndex == other.interruptingBypassIndex;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Arrays.hashCode((Object[])this.blockedBy);
      result = 31 * result + Arrays.hashCode((Object[])this.blocking);
      result = 31 * result + Arrays.hashCode((Object[])this.interruptedBy);
      result = 31 * result + Arrays.hashCode((Object[])this.interrupting);
      result = 31 * result + Integer.hashCode(this.blockedByBypassIndex);
      result = 31 * result + Integer.hashCode(this.blockingBypassIndex);
      result = 31 * result + Integer.hashCode(this.interruptedByBypassIndex);
      return 31 * result + Integer.hashCode(this.interruptingBypassIndex);
   }
}
