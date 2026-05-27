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

public class LayerPlacement {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 17;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 25;
   public static final int MAX_SIZE = 1677721600;
   public int containerIndex;
   @Nullable
   public String name;
   @Nullable
   public BarBeatDuration clipStart;
   @Nullable
   public StateBinding[] stateBindings;

   public LayerPlacement() {
   }

   public LayerPlacement(int containerIndex, @Nullable String name, @Nullable BarBeatDuration clipStart, @Nullable StateBinding[] stateBindings) {
      this.containerIndex = containerIndex;
      this.name = name;
      this.clipStart = clipStart;
      this.stateBindings = stateBindings;
   }

   public LayerPlacement(@Nonnull LayerPlacement other) {
      this.containerIndex = other.containerIndex;
      this.name = other.name;
      this.clipStart = other.clipStart;
      this.stateBindings = other.stateBindings;
   }

   @Nonnull
   public static LayerPlacement deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 25) {
         throw ProtocolException.bufferTooSmall("LayerPlacement", 25, buf.readableBytes() - offset);
      } else {
         LayerPlacement obj = new LayerPlacement();
         byte nullBits = buf.getByte(offset);
         obj.containerIndex = buf.getIntLE(offset + 1);
         if ((nullBits & 1) != 0) {
            obj.clipStart = BarBeatDuration.deserialize(buf, offset + 5);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 17);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 25) {
               throw ProtocolException.invalidOffset("Name", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 25 + varPosBase0;
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

         if ((nullBits & 4) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 21);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 25) {
               throw ProtocolException.invalidOffset("StateBindings", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 25 + varPosBase1;
            int stateBindingsCount = VarInt.peek(buf, varPos1);
            if (stateBindingsCount < 0) {
               throw ProtocolException.invalidVarInt("StateBindings");
            }

            int varIntLen = VarInt.size(stateBindingsCount);
            if (stateBindingsCount > 4096000) {
               throw ProtocolException.arrayTooLong("StateBindings", stateBindingsCount, 4096000);
            }

            if (varPos1 + varIntLen + stateBindingsCount * 5L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("StateBindings", varPos1 + varIntLen + stateBindingsCount * 5, buf.readableBytes());
            }

            obj.stateBindings = new StateBinding[stateBindingsCount];
            int elemPos = varPos1 + varIntLen;

            for (int i = 0; i < stateBindingsCount; i++) {
               obj.stateBindings[i] = StateBinding.deserialize(buf, elemPos);
               elemPos += StateBinding.computeBytesConsumed(buf, elemPos);
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 25;
      if ((nullBits & 2) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 17);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 25) {
            throw ProtocolException.invalidOffset("Name", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 25 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 21);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 25) {
            throw ProtocolException.invalidOffset("StateBindings", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 25 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos1 += StateBinding.computeBytesConsumed(buf, pos1);
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 25L;
   }

   public static int getContainerIndex(MemorySegment mem) {
      return getContainerIndex(mem, 0);
   }

   public static int getContainerIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   @Nullable
   public static String getName(MemorySegment mem) {
      return getName(mem, 0);
   }

   @Nullable
   public static String getName(MemorySegment mem, int offset) {
      return hasName(mem, offset)
         ? PacketIO.readVarString("Name", mem, offset + getValidatedOffset(mem, offset, 17, 25, "Name"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static BarBeatDuration getClipStart(MemorySegment mem) {
      return getClipStart(mem, 0);
   }

   @Nullable
   public static BarBeatDuration getClipStart(MemorySegment mem, int offset) {
      return hasClipStart(mem, offset) ? BarBeatDuration.toObject(mem, offset + 5) : null;
   }

   @Nullable
   public static StateBinding[] getStateBindings(MemorySegment mem) {
      return getStateBindings(mem, 0);
   }

   @Nullable
   public static StateBinding[] getStateBindings(MemorySegment mem, int offset) {
      if (!hasStateBindings(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 21, 25, "StateBindings");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("StateBindings", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("StateBindings", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("StateBindings", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               StateBinding[] data = new StateBinding[len];

               for (int i = 0; i < len; i++) {
                  data[i] = StateBinding.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   public static boolean hasClipStart(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasName(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasStateBindings(MemorySegment mem, int offset) {
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

   public static LayerPlacement toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static LayerPlacement toObject(MemorySegment mem, int offset) {
      if (offset + 25 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("LayerPlacement", offset + 25, (int)mem.byteSize());
      } else {
         StateBinding[] stateBindings = null;
         if (hasStateBindings(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 21, 25, "StateBindings");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("StateBindings", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("StateBindings", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("StateBindings", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            stateBindings = new StateBinding[len];

            for (int i = 0; i < len; i++) {
               stateBindings[i] = StateBinding.toObject(mem, off);
               off += stateBindings[i].computeSize();
            }
         }

         return new LayerPlacement(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            hasName(mem, offset) ? PacketIO.readVarString("Name", mem, offset + getValidatedOffset(mem, offset, 17, 25, "Name"), 4096000, PacketIO.UTF8) : null,
            hasClipStart(mem, offset) ? BarBeatDuration.toObject(mem, offset + 5) : null,
            stateBindings
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.clipStart != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.name != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.stateBindings != null) {
         nullBits = (byte)(nullBits | 4);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.containerIndex);
      if (this.clipStart != null) {
         this.clipStart.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      int nameOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int stateBindingsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.name != null) {
         buf.setIntLE(nameOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.name, 4096000);
      } else {
         buf.setIntLE(nameOffsetSlot, -1);
      }

      if (this.stateBindings != null) {
         buf.setIntLE(stateBindingsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.stateBindings.length > 4096000) {
            throw ProtocolException.arrayTooLong("StateBindings", this.stateBindings.length, 4096000);
         }

         VarInt.write(buf, this.stateBindings.length);

         for (StateBinding item : this.stateBindings) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(stateBindingsOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.clipStart != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.name != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.stateBindings != null) {
         nullBits = (byte)(nullBits | 4);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.containerIndex);
      if (this.clipStart != null) {
         this.clipStart.serialize(mem, offset + 5);
      } else {
         mem.asSlice(offset + 5, 12L).fill((byte)0);
      }

      int varOffset = offset + 25;
      if (this.name != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 17), varOffset - offset - 25);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.name, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 17), -1);
      }

      if (this.stateBindings != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 21), varOffset - offset - 25);
         if (this.stateBindings.length > 4096000) {
            throw ProtocolException.arrayTooLong("StateBindings", this.stateBindings.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.stateBindings.length);
         int stateBindingsValueOffset = 0;

         for (int i = 0; i < this.stateBindings.length; i++) {
            stateBindingsValueOffset += this.stateBindings[i].serialize(mem, varOffset + stateBindingsValueOffset);
         }

         varOffset += stateBindingsValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 21), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 25;
      if (this.name != null) {
         size += PacketIO.stringSize(this.name);
      }

      if (this.stateBindings != null) {
         int stateBindingsSize = 0;

         for (StateBinding elem : this.stateBindings) {
            stateBindingsSize += elem.computeSize();
         }

         size += VarInt.size(this.stateBindings.length) + stateBindingsSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 25) {
         return ValidationResult.error("Buffer too small: expected at least 25 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 2) != 0) {
            int nameOffset = buffer.getIntLE(offset + 17);
            if (nameOffset < 0 || nameOffset > buffer.writerIndex() - offset - 25) {
               return ValidationResult.error("Invalid offset for Name");
            }

            int pos = offset + 25 + nameOffset;
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

         if ((nullBits & 4) != 0) {
            int stateBindingsOffset = buffer.getIntLE(offset + 21);
            if (stateBindingsOffset < 0 || stateBindingsOffset > buffer.writerIndex() - offset - 25) {
               return ValidationResult.error("Invalid offset for StateBindings");
            }

            int posx = offset + 25 + stateBindingsOffset;
            int stateBindingsCount = VarInt.peek(buffer, posx);
            if (stateBindingsCount < 0) {
               return ValidationResult.error("Invalid array count for StateBindings");
            }

            if (stateBindingsCount > 4096000) {
               return ValidationResult.error("StateBindings exceeds max length 4096000");
            }

            posx += VarInt.size(stateBindingsCount);

            for (int i = 0; i < stateBindingsCount; i++) {
               ValidationResult structResult = StateBinding.validateStructure(buffer, posx);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid StateBinding in StateBindings[" + i + "]: " + structResult.error());
               }

               posx += StateBinding.computeBytesConsumed(buffer, posx);
            }
         }

         return ValidationResult.OK;
      }
   }

   public LayerPlacement clone() {
      LayerPlacement copy = new LayerPlacement();
      copy.containerIndex = this.containerIndex;
      copy.name = this.name;
      copy.clipStart = this.clipStart != null ? this.clipStart.clone() : null;
      copy.stateBindings = this.stateBindings != null ? Arrays.stream(this.stateBindings).map(e -> e.clone()).toArray(StateBinding[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof LayerPlacement other)
            ? false
            : this.containerIndex == other.containerIndex
               && Objects.equals(this.name, other.name)
               && Objects.equals(this.clipStart, other.clipStart)
               && Arrays.equals((Object[])this.stateBindings, (Object[])other.stateBindings);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Integer.hashCode(this.containerIndex);
      result = 31 * result + Objects.hashCode(this.name);
      result = 31 * result + Objects.hashCode(this.clipStart);
      return 31 * result + Arrays.hashCode((Object[])this.stateBindings);
   }
}
