package com.hypixel.hytale.protocol;

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

public class AudioState {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 23;
   public static final int VARIABLE_FIELD_COUNT = 3;
   public static final int VARIABLE_BLOCK_START = 35;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public String id;
   @Nonnull
   public AudioStateAuthority authority = AudioStateAuthority.Server;
   @Nullable
   public String[] values;
   public int defaultValueIndex;
   @Nonnull
   public SyncPoint defaultSyncTo = SyncPoint.Immediate;
   @Nullable
   public StateTransition defaultTransition;
   @Nullable
   public StateTransition[] transitions;
   public boolean revertWhenInactive;

   public AudioState() {
   }

   public AudioState(
      @Nullable String id,
      @Nonnull AudioStateAuthority authority,
      @Nullable String[] values,
      int defaultValueIndex,
      @Nonnull SyncPoint defaultSyncTo,
      @Nullable StateTransition defaultTransition,
      @Nullable StateTransition[] transitions,
      boolean revertWhenInactive
   ) {
      this.id = id;
      this.authority = authority;
      this.values = values;
      this.defaultValueIndex = defaultValueIndex;
      this.defaultSyncTo = defaultSyncTo;
      this.defaultTransition = defaultTransition;
      this.transitions = transitions;
      this.revertWhenInactive = revertWhenInactive;
   }

   public AudioState(@Nonnull AudioState other) {
      this.id = other.id;
      this.authority = other.authority;
      this.values = other.values;
      this.defaultValueIndex = other.defaultValueIndex;
      this.defaultSyncTo = other.defaultSyncTo;
      this.defaultTransition = other.defaultTransition;
      this.transitions = other.transitions;
      this.revertWhenInactive = other.revertWhenInactive;
   }

   @Nonnull
   public static AudioState deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 35) {
         throw ProtocolException.bufferTooSmall("AudioState", 35, buf.readableBytes() - offset);
      } else {
         AudioState obj = new AudioState();
         byte nullBits = buf.getByte(offset);
         obj.authority = AudioStateAuthority.fromValue(buf.getByte(offset + 1));
         obj.defaultValueIndex = buf.getIntLE(offset + 2);
         obj.defaultSyncTo = SyncPoint.fromValue(buf.getByte(offset + 6));
         if ((nullBits & 1) != 0) {
            obj.defaultTransition = StateTransition.deserialize(buf, offset + 7);
         }

         obj.revertWhenInactive = buf.getByte(offset + 22) != 0;
         if ((nullBits & 2) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 23);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 35) {
               throw ProtocolException.invalidOffset("Id", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 35 + varPosBase0;
            int idLen = VarInt.peek(buf, varPos0);
            if (idLen < 0) {
               throw ProtocolException.invalidVarInt("Id");
            }

            int idVarIntLen = VarInt.size(idLen);
            if (idLen > 4096000) {
               throw ProtocolException.stringTooLong("Id", idLen, 4096000);
            }

            if (varPos0 + idVarIntLen + idLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Id", varPos0 + idVarIntLen + idLen, buf.readableBytes());
            }

            obj.id = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 4) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 27);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 35) {
               throw ProtocolException.invalidOffset("Values", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 35 + varPosBase1;
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

         if ((nullBits & 8) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 31);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 35) {
               throw ProtocolException.invalidOffset("Transitions", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 35 + varPosBase2;
            int transitionsCount = VarInt.peek(buf, varPos2);
            if (transitionsCount < 0) {
               throw ProtocolException.invalidVarInt("Transitions");
            }

            int varIntLenx = VarInt.size(transitionsCount);
            if (transitionsCount > 4096000) {
               throw ProtocolException.arrayTooLong("Transitions", transitionsCount, 4096000);
            }

            if (varPos2 + varIntLenx + transitionsCount * 15L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Transitions", varPos2 + varIntLenx + transitionsCount * 15, buf.readableBytes());
            }

            obj.transitions = new StateTransition[transitionsCount];
            int elemPos = varPos2 + varIntLenx;

            for (int i = 0; i < transitionsCount; i++) {
               obj.transitions[i] = StateTransition.deserialize(buf, elemPos);
               elemPos += StateTransition.computeBytesConsumed(buf, elemPos);
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 35;
      if ((nullBits & 2) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 23);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 35) {
            throw ProtocolException.invalidOffset("Id", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 35 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 27);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 35) {
            throw ProtocolException.invalidOffset("Values", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 35 + fieldOffset1;
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

      if ((nullBits & 8) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 31);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 35) {
            throw ProtocolException.invalidOffset("Transitions", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 35 + fieldOffset2;
         int arrLen = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos2 += StateTransition.computeBytesConsumed(buf, pos2);
         }

         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 35L;
   }

   @Nullable
   public static String getId(MemorySegment mem) {
      return getId(mem, 0);
   }

   @Nullable
   public static String getId(MemorySegment mem, int offset) {
      return hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 23, 35, "Id"), 4096000, PacketIO.UTF8) : null;
   }

   public static AudioStateAuthority getAuthority(MemorySegment mem) {
      return getAuthority(mem, 0);
   }

   public static AudioStateAuthority getAuthority(MemorySegment mem, int offset) {
      return AudioStateAuthority.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1)));
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
         int off = offset + getValidatedOffset(mem, offset, 27, 35, "Values");
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

   public static int getDefaultValueIndex(MemorySegment mem) {
      return getDefaultValueIndex(mem, 0);
   }

   public static int getDefaultValueIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 2));
   }

   public static SyncPoint getDefaultSyncTo(MemorySegment mem) {
      return getDefaultSyncTo(mem, 0);
   }

   public static SyncPoint getDefaultSyncTo(MemorySegment mem, int offset) {
      return SyncPoint.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 6)));
   }

   @Nullable
   public static StateTransition getDefaultTransition(MemorySegment mem) {
      return getDefaultTransition(mem, 0);
   }

   @Nullable
   public static StateTransition getDefaultTransition(MemorySegment mem, int offset) {
      return hasDefaultTransition(mem, offset) ? StateTransition.toObject(mem, offset + 7) : null;
   }

   @Nullable
   public static StateTransition[] getTransitions(MemorySegment mem) {
      return getTransitions(mem, 0);
   }

   @Nullable
   public static StateTransition[] getTransitions(MemorySegment mem, int offset) {
      if (!hasTransitions(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 31, 35, "Transitions");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Transitions", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Transitions", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 15L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Transitions", off + lenOffset + len * 15, (int)mem.byteSize());
            } else {
               off += lenOffset;
               StateTransition[] data = new StateTransition[len];

               for (int i = 0; i < len; i++) {
                  data[i] = StateTransition.toObject(mem, off + i * 15);
               }

               return data;
            }
         }
      }
   }

   public static boolean getRevertWhenInactive(MemorySegment mem) {
      return getRevertWhenInactive(mem, 0);
   }

   public static boolean getRevertWhenInactive(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 22));
   }

   public static boolean hasDefaultTransition(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasValues(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasTransitions(MemorySegment mem, int offset) {
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

   public static AudioState toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AudioState toObject(MemorySegment mem, int offset) {
      if (offset + 35 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AudioState", offset + 35, (int)mem.byteSize());
      } else {
         String[] values = null;
         if (hasValues(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 27, 35, "Values");
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

         StateTransition[] transitions = null;
         if (hasTransitions(mem, offset)) {
            int offx = offset + getValidatedOffset(mem, offset, 31, 35, "Transitions");
            long packedx = VarInt.getWithLength(mem, offx);
            int lenx = (int)packedx;
            if (lenx < 0) {
               throw ProtocolException.negativeLength("Transitions", lenx);
            }

            if (lenx > 4096000) {
               throw ProtocolException.arrayTooLong("Transitions", lenx, 4096000);
            }

            int lenOffset = (int)(packedx >>> 32);
            if (offx + lenOffset + lenx * 15L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Transitions", offx + lenOffset + lenx * 15, (int)mem.byteSize());
            }

            offx += lenOffset;
            transitions = new StateTransition[lenx];

            for (int i = 0; i < lenx; i++) {
               transitions[i] = StateTransition.toObject(mem, offx + i * 15);
            }
         }

         return new AudioState(
            hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 23, 35, "Id"), 4096000, PacketIO.UTF8) : null,
            AudioStateAuthority.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))),
            values,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 2)),
            SyncPoint.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 6))),
            hasDefaultTransition(mem, offset) ? StateTransition.toObject(mem, offset + 7) : null,
            transitions,
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 22))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.defaultTransition != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.id != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.values != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.transitions != null) {
         nullBits = (byte)(nullBits | 8);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.authority.getValue());
      buf.writeIntLE(this.defaultValueIndex);
      buf.writeByte(this.defaultSyncTo.getValue());
      if (this.defaultTransition != null) {
         this.defaultTransition.serialize(buf);
      } else {
         buf.writeZero(15);
      }

      buf.writeByte(this.revertWhenInactive ? 1 : 0);
      int idOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int valuesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int transitionsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.id != null) {
         buf.setIntLE(idOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.id, 4096000);
      } else {
         buf.setIntLE(idOffsetSlot, -1);
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

      if (this.transitions != null) {
         buf.setIntLE(transitionsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.transitions.length > 4096000) {
            throw ProtocolException.arrayTooLong("Transitions", this.transitions.length, 4096000);
         }

         VarInt.write(buf, this.transitions.length);

         for (StateTransition item : this.transitions) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(transitionsOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.defaultTransition != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.id != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.values != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.transitions != null) {
         nullBits = (byte)(nullBits | 8);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.authority.getValue());
      mem.set(PacketIO.PROTO_INT, (long)(offset + 2), this.defaultValueIndex);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 6), (byte)this.defaultSyncTo.getValue());
      if (this.defaultTransition != null) {
         this.defaultTransition.serialize(mem, offset + 7);
      } else {
         mem.asSlice(offset + 7, 15L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_BOOL, offset + 22, this.revertWhenInactive);
      int varOffset = offset + 35;
      if (this.id != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 23), varOffset - offset - 35);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.id, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 23), -1);
      }

      if (this.values != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 27), varOffset - offset - 35);
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
         mem.set(PacketIO.PROTO_INT, (long)(offset + 27), -1);
      }

      if (this.transitions != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 31), varOffset - offset - 35);
         if (this.transitions.length > 4096000) {
            throw ProtocolException.arrayTooLong("Transitions", this.transitions.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.transitions.length);
         int transitionsValueOffset = 0;

         for (int i = 0; i < this.transitions.length; i++) {
            transitionsValueOffset += this.transitions[i].serialize(mem, varOffset + transitionsValueOffset);
         }

         varOffset += transitionsValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 31), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 35;
      if (this.id != null) {
         size += PacketIO.stringSize(this.id);
      }

      if (this.values != null) {
         int valuesSize = 0;

         for (String elem : this.values) {
            valuesSize += PacketIO.stringSize(elem);
         }

         size += VarInt.size(this.values.length) + valuesSize;
      }

      if (this.transitions != null) {
         size += VarInt.size(this.transitions.length) + this.transitions.length * 15;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 35) {
         return ValidationResult.error("Buffer too small: expected at least 35 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int v = buffer.getByte(offset + 1) & 255;
         if (v >= 2) {
            return ValidationResult.error("Invalid AudioStateAuthority value for Authority");
         } else {
            v = buffer.getByte(offset + 6) & 255;
            if (v >= 4) {
               return ValidationResult.error("Invalid SyncPoint value for DefaultSyncTo");
            } else {
               if ((nullBits & 2) != 0) {
                  v = buffer.getIntLE(offset + 23);
                  if (v < 0 || v > buffer.writerIndex() - offset - 35) {
                     return ValidationResult.error("Invalid offset for Id");
                  }

                  int pos = offset + 35 + v;
                  int idLen = VarInt.peek(buffer, pos);
                  if (idLen < 0) {
                     return ValidationResult.error("Invalid string length for Id");
                  }

                  if (idLen > 4096000) {
                     return ValidationResult.error("Id exceeds max length 4096000");
                  }

                  pos += VarInt.size(idLen);
                  pos += idLen;
                  if (pos > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading Id");
                  }
               }

               if ((nullBits & 4) != 0) {
                  v = buffer.getIntLE(offset + 27);
                  if (v < 0 || v > buffer.writerIndex() - offset - 35) {
                     return ValidationResult.error("Invalid offset for Values");
                  }

                  int posx = offset + 35 + v;
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

               if ((nullBits & 8) != 0) {
                  v = buffer.getIntLE(offset + 31);
                  if (v < 0 || v > buffer.writerIndex() - offset - 35) {
                     return ValidationResult.error("Invalid offset for Transitions");
                  }

                  int posxx = offset + 35 + v;
                  int transitionsCount = VarInt.peek(buffer, posxx);
                  if (transitionsCount < 0) {
                     return ValidationResult.error("Invalid array count for Transitions");
                  }

                  if (transitionsCount > 4096000) {
                     return ValidationResult.error("Transitions exceeds max length 4096000");
                  }

                  posxx += VarInt.size(transitionsCount);
                  posxx += transitionsCount * 15;
                  if (posxx > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading Transitions");
                  }
               }

               return ValidationResult.OK;
            }
         }
      }
   }

   public AudioState clone() {
      AudioState copy = new AudioState();
      copy.id = this.id;
      copy.authority = this.authority;
      copy.values = this.values != null ? Arrays.copyOf(this.values, this.values.length) : null;
      copy.defaultValueIndex = this.defaultValueIndex;
      copy.defaultSyncTo = this.defaultSyncTo;
      copy.defaultTransition = this.defaultTransition != null ? this.defaultTransition.clone() : null;
      copy.transitions = this.transitions != null ? Arrays.stream(this.transitions).map(e -> e.clone()).toArray(StateTransition[]::new) : null;
      copy.revertWhenInactive = this.revertWhenInactive;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AudioState other)
            ? false
            : Objects.equals(this.id, other.id)
               && Objects.equals(this.authority, other.authority)
               && Arrays.equals((Object[])this.values, (Object[])other.values)
               && this.defaultValueIndex == other.defaultValueIndex
               && Objects.equals(this.defaultSyncTo, other.defaultSyncTo)
               && Objects.equals(this.defaultTransition, other.defaultTransition)
               && Arrays.equals((Object[])this.transitions, (Object[])other.transitions)
               && this.revertWhenInactive == other.revertWhenInactive;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.id);
      result = 31 * result + Objects.hashCode(this.authority);
      result = 31 * result + Arrays.hashCode((Object[])this.values);
      result = 31 * result + Integer.hashCode(this.defaultValueIndex);
      result = 31 * result + Objects.hashCode(this.defaultSyncTo);
      result = 31 * result + Objects.hashCode(this.defaultTransition);
      result = 31 * result + Arrays.hashCode((Object[])this.transitions);
      return 31 * result + Boolean.hashCode(this.revertWhenInactive);
   }
}
