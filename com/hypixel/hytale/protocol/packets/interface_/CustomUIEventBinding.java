package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CustomUIEventBinding {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 3;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 11;
   public static final int MAX_SIZE = 32768021;
   @Nonnull
   public CustomUIEventBindingType type = CustomUIEventBindingType.Activating;
   @Nullable
   public String selector;
   @Nullable
   public String data;
   public boolean locksInterface;

   public CustomUIEventBinding() {
   }

   public CustomUIEventBinding(@Nonnull CustomUIEventBindingType type, @Nullable String selector, @Nullable String data, boolean locksInterface) {
      this.type = type;
      this.selector = selector;
      this.data = data;
      this.locksInterface = locksInterface;
   }

   public CustomUIEventBinding(@Nonnull CustomUIEventBinding other) {
      this.type = other.type;
      this.selector = other.selector;
      this.data = other.data;
      this.locksInterface = other.locksInterface;
   }

   @Nonnull
   public static CustomUIEventBinding deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 11) {
         throw ProtocolException.bufferTooSmall("CustomUIEventBinding", 11, buf.readableBytes() - offset);
      } else {
         CustomUIEventBinding obj = new CustomUIEventBinding();
         byte nullBits = buf.getByte(offset);
         obj.type = CustomUIEventBindingType.fromValue(buf.getByte(offset + 1));
         obj.locksInterface = buf.getByte(offset + 2) != 0;
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 3);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 11) {
               throw ProtocolException.invalidOffset("Selector", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 11 + varPosBase0;
            int selectorLen = VarInt.peek(buf, varPos0);
            if (selectorLen < 0) {
               throw ProtocolException.invalidVarInt("Selector");
            }

            int selectorVarIntLen = VarInt.size(selectorLen);
            if (selectorLen > 4096000) {
               throw ProtocolException.stringTooLong("Selector", selectorLen, 4096000);
            }

            if (varPos0 + selectorVarIntLen + selectorLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Selector", varPos0 + selectorVarIntLen + selectorLen, buf.readableBytes());
            }

            obj.selector = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 7);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 11) {
               throw ProtocolException.invalidOffset("Data", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 11 + varPosBase1;
            int dataLen = VarInt.peek(buf, varPos1);
            if (dataLen < 0) {
               throw ProtocolException.invalidVarInt("Data");
            }

            int dataVarIntLen = VarInt.size(dataLen);
            if (dataLen > 4096000) {
               throw ProtocolException.stringTooLong("Data", dataLen, 4096000);
            }

            if (varPos1 + dataVarIntLen + dataLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Data", varPos1 + dataVarIntLen + dataLen, buf.readableBytes());
            }

            obj.data = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 11;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 3);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 11) {
            throw ProtocolException.invalidOffset("Selector", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 11 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 7);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 11) {
            throw ProtocolException.invalidOffset("Data", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 11 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(sl) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 11L;
   }

   public static CustomUIEventBindingType getType(MemorySegment mem) {
      return getType(mem, 0);
   }

   public static CustomUIEventBindingType getType(MemorySegment mem, int offset) {
      return CustomUIEventBindingType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1)));
   }

   @Nullable
   public static String getSelector(MemorySegment mem) {
      return getSelector(mem, 0);
   }

   @Nullable
   public static String getSelector(MemorySegment mem, int offset) {
      return hasSelector(mem, offset)
         ? PacketIO.readVarString("Selector", mem, offset + getValidatedOffset(mem, offset, 3, 11, "Selector"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static String getData(MemorySegment mem) {
      return getData(mem, 0);
   }

   @Nullable
   public static String getData(MemorySegment mem, int offset) {
      return hasData(mem, offset) ? PacketIO.readVarString("Data", mem, offset + getValidatedOffset(mem, offset, 7, 11, "Data"), 4096000, PacketIO.UTF8) : null;
   }

   public static boolean getLocksInterface(MemorySegment mem) {
      return getLocksInterface(mem, 0);
   }

   public static boolean getLocksInterface(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 2));
   }

   public static boolean hasSelector(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasData(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static CustomUIEventBinding toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static CustomUIEventBinding toObject(MemorySegment mem, int offset) {
      if (offset + 11 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("CustomUIEventBinding", offset + 11, (int)mem.byteSize());
      } else {
         return new CustomUIEventBinding(
            CustomUIEventBindingType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))),
            hasSelector(mem, offset)
               ? PacketIO.readVarString("Selector", mem, offset + getValidatedOffset(mem, offset, 3, 11, "Selector"), 4096000, PacketIO.UTF8)
               : null,
            hasData(mem, offset) ? PacketIO.readVarString("Data", mem, offset + getValidatedOffset(mem, offset, 7, 11, "Data"), 4096000, PacketIO.UTF8) : null,
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 2))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.selector != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.data != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      buf.writeByte(this.locksInterface ? 1 : 0);
      int selectorOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int dataOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.selector != null) {
         buf.setIntLE(selectorOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.selector, 4096000);
      } else {
         buf.setIntLE(selectorOffsetSlot, -1);
      }

      if (this.data != null) {
         buf.setIntLE(dataOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.data, 4096000);
      } else {
         buf.setIntLE(dataOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.selector != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.data != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.type.getValue());
      mem.set(PacketIO.PROTO_BOOL, offset + 2, this.locksInterface);
      int varOffset = offset + 11;
      if (this.selector != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 3), varOffset - offset - 11);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.selector, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 3), -1);
      }

      if (this.data != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 7), varOffset - offset - 11);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.data, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 7), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 11;
      if (this.selector != null) {
         size += PacketIO.stringSize(this.selector);
      }

      if (this.data != null) {
         size += PacketIO.stringSize(this.data);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 11) {
         return ValidationResult.error("Buffer too small: expected at least 11 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int v = buffer.getByte(offset + 1) & 255;
         if (v >= 24) {
            return ValidationResult.error("Invalid CustomUIEventBindingType value for Type");
         } else {
            if ((nullBits & 1) != 0) {
               v = buffer.getIntLE(offset + 3);
               if (v < 0 || v > buffer.writerIndex() - offset - 11) {
                  return ValidationResult.error("Invalid offset for Selector");
               }

               int pos = offset + 11 + v;
               int selectorLen = VarInt.peek(buffer, pos);
               if (selectorLen < 0) {
                  return ValidationResult.error("Invalid string length for Selector");
               }

               if (selectorLen > 4096000) {
                  return ValidationResult.error("Selector exceeds max length 4096000");
               }

               pos += VarInt.size(selectorLen);
               pos += selectorLen;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading Selector");
               }
            }

            if ((nullBits & 2) != 0) {
               v = buffer.getIntLE(offset + 7);
               if (v < 0 || v > buffer.writerIndex() - offset - 11) {
                  return ValidationResult.error("Invalid offset for Data");
               }

               int posx = offset + 11 + v;
               int dataLen = VarInt.peek(buffer, posx);
               if (dataLen < 0) {
                  return ValidationResult.error("Invalid string length for Data");
               }

               if (dataLen > 4096000) {
                  return ValidationResult.error("Data exceeds max length 4096000");
               }

               posx += VarInt.size(dataLen);
               posx += dataLen;
               if (posx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading Data");
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public CustomUIEventBinding clone() {
      CustomUIEventBinding copy = new CustomUIEventBinding();
      copy.type = this.type;
      copy.selector = this.selector;
      copy.data = this.data;
      copy.locksInterface = this.locksInterface;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof CustomUIEventBinding other)
            ? false
            : Objects.equals(this.type, other.type)
               && Objects.equals(this.selector, other.selector)
               && Objects.equals(this.data, other.data)
               && this.locksInterface == other.locksInterface;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.selector, this.data, this.locksInterface);
   }
}
