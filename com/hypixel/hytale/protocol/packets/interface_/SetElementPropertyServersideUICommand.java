package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class SetElementPropertyServersideUICommand extends ServersideUICommand {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 0;
   public static final int VARIABLE_FIELD_COUNT = 3;
   public static final int VARIABLE_BLOCK_START = 12;
   public static final int MAX_SIZE = 32769046;
   @Nonnull
   public String selector = "";
   @Nonnull
   public String propertyName = "";
   @Nonnull
   public UIDataValue value;

   public SetElementPropertyServersideUICommand() {
   }

   public SetElementPropertyServersideUICommand(@Nonnull String selector, @Nonnull String propertyName, @Nonnull UIDataValue value) {
      this.selector = selector;
      this.propertyName = propertyName;
      this.value = value;
   }

   public SetElementPropertyServersideUICommand(@Nonnull SetElementPropertyServersideUICommand other) {
      this.selector = other.selector;
      this.propertyName = other.propertyName;
      this.value = other.value;
   }

   @Nonnull
   public static SetElementPropertyServersideUICommand deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 12) {
         throw ProtocolException.bufferTooSmall("SetElementPropertyServersideUICommand", 12, buf.readableBytes() - offset);
      } else {
         SetElementPropertyServersideUICommand obj = new SetElementPropertyServersideUICommand();
         int varPosBase0 = buf.getIntLE(offset + 0);
         if (varPosBase0 >= 0 && varPosBase0 <= buf.writerIndex() - offset - 12) {
            int varPos0 = offset + 12 + varPosBase0;
            int selectorLen = VarInt.peek(buf, varPos0);
            if (selectorLen < 0) {
               throw ProtocolException.invalidVarInt("Selector");
            } else {
               int selectorVarIntLen = VarInt.size(selectorLen);
               if (selectorLen > 4096000) {
                  throw ProtocolException.stringTooLong("Selector", selectorLen, 4096000);
               } else if (varPos0 + selectorVarIntLen + selectorLen > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("Selector", varPos0 + selectorVarIntLen + selectorLen, buf.readableBytes());
               } else {
                  obj.selector = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
                  varPosBase0 = buf.getIntLE(offset + 4);
                  if (varPosBase0 >= 0 && varPosBase0 <= buf.writerIndex() - offset - 12) {
                     varPos0 = offset + 12 + varPosBase0;
                     selectorLen = VarInt.peek(buf, varPos0);
                     if (selectorLen < 0) {
                        throw ProtocolException.invalidVarInt("PropertyName");
                     } else {
                        selectorVarIntLen = VarInt.size(selectorLen);
                        if (selectorLen > 4096000) {
                           throw ProtocolException.stringTooLong("PropertyName", selectorLen, 4096000);
                        } else if (varPos0 + selectorVarIntLen + selectorLen > buf.readableBytes()) {
                           throw ProtocolException.bufferTooSmall("PropertyName", varPos0 + selectorVarIntLen + selectorLen, buf.readableBytes());
                        } else {
                           obj.propertyName = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
                           varPosBase0 = buf.getIntLE(offset + 8);
                           if (varPosBase0 >= 0 && varPosBase0 <= buf.writerIndex() - offset - 12) {
                              varPos0 = offset + 12 + varPosBase0;
                              obj.value = UIDataValue.deserialize(buf, varPos0);
                              return obj;
                           } else {
                              throw ProtocolException.invalidOffset("Value", varPosBase0, buf.readableBytes());
                           }
                        }
                     }
                  } else {
                     throw ProtocolException.invalidOffset("PropertyName", varPosBase0, buf.readableBytes());
                  }
               }
            }
         } else {
            throw ProtocolException.invalidOffset("Selector", varPosBase0, buf.readableBytes());
         }
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      int maxEnd = 12;
      int fieldOffset0 = buf.getIntLE(offset + 0);
      if (fieldOffset0 >= 0 && fieldOffset0 <= buf.writerIndex() - offset - 12) {
         int pos0 = offset + 12 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }

         fieldOffset0 = buf.getIntLE(offset + 4);
         if (fieldOffset0 >= 0 && fieldOffset0 <= buf.writerIndex() - offset - 12) {
            pos0 = offset + 12 + fieldOffset0;
            sl = VarInt.peek(buf, pos0);
            pos0 += VarInt.size(sl) + sl;
            if (pos0 - offset > maxEnd) {
               maxEnd = pos0 - offset;
            }

            fieldOffset0 = buf.getIntLE(offset + 8);
            if (fieldOffset0 >= 0 && fieldOffset0 <= buf.writerIndex() - offset - 12) {
               pos0 = offset + 12 + fieldOffset0;
               pos0 += UIDataValue.computeBytesConsumed(buf, pos0);
               if (pos0 - offset > maxEnd) {
                  maxEnd = pos0 - offset;
               }

               return maxEnd;
            } else {
               throw ProtocolException.invalidOffset("Value", fieldOffset0, maxEnd);
            }
         } else {
            throw ProtocolException.invalidOffset("PropertyName", fieldOffset0, maxEnd);
         }
      } else {
         throw ProtocolException.invalidOffset("Selector", fieldOffset0, maxEnd);
      }
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 12L;
   }

   public static String getSelector(MemorySegment mem) {
      return getSelector(mem, 0);
   }

   public static String getSelector(MemorySegment mem, int offset) {
      return PacketIO.readVarString("Selector", mem, offset + getValidatedOffset(mem, offset, 0, 12, "Selector"), 4096000, PacketIO.UTF8);
   }

   public static String getPropertyName(MemorySegment mem) {
      return getPropertyName(mem, 0);
   }

   public static String getPropertyName(MemorySegment mem, int offset) {
      return PacketIO.readVarString("PropertyName", mem, offset + getValidatedOffset(mem, offset, 4, 12, "PropertyName"), 4096000, PacketIO.UTF8);
   }

   public static UIDataValue getValue(MemorySegment mem) {
      return getValue(mem, 0);
   }

   public static UIDataValue getValue(MemorySegment mem, int offset) {
      return UIDataValue.toObject(mem, offset + getValidatedOffset(mem, offset, 8, 12, "Value"));
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static SetElementPropertyServersideUICommand toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static SetElementPropertyServersideUICommand toObject(MemorySegment mem, int offset) {
      if (offset + 12 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("SetElementPropertyServersideUICommand", offset + 12, (int)mem.byteSize());
      } else {
         return new SetElementPropertyServersideUICommand(
            PacketIO.readVarString("Selector", mem, offset + getValidatedOffset(mem, offset, 0, 12, "Selector"), 4096000, PacketIO.UTF8),
            PacketIO.readVarString("PropertyName", mem, offset + getValidatedOffset(mem, offset, 4, 12, "PropertyName"), 4096000, PacketIO.UTF8),
            UIDataValue.toObject(mem, offset + getValidatedOffset(mem, offset, 8, 12, "Value"))
         );
      }
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      int selectorOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int propertyNameOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int valueOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      buf.setIntLE(selectorOffsetSlot, buf.writerIndex() - varBlockStart);
      PacketIO.writeVarString(buf, this.selector, 4096000);
      buf.setIntLE(propertyNameOffsetSlot, buf.writerIndex() - varBlockStart);
      PacketIO.writeVarString(buf, this.propertyName, 4096000);
      buf.setIntLE(valueOffsetSlot, buf.writerIndex() - varBlockStart);
      this.value.serializeWithTypeId(buf);
      return buf.writerIndex() - startPos;
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      int varOffset = offset + 12;
      mem.set(PacketIO.PROTO_INT, (long)(offset + 0), varOffset - offset - 12);
      varOffset += PacketIO.writeVarString(mem, varOffset, this.selector, 4096000);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 4), varOffset - offset - 12);
      varOffset += PacketIO.writeVarString(mem, varOffset, this.propertyName, 4096000);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 8), varOffset - offset - 12);
      varOffset += this.value.serializeWithTypeId(mem, varOffset);
      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 12;
      size += PacketIO.stringSize(this.selector);
      size += PacketIO.stringSize(this.propertyName);
      return size + this.value.computeSizeWithTypeId();
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 12) {
         return ValidationResult.error("Buffer too small: expected at least 12 bytes");
      } else {
         int selectorOffset = buffer.getIntLE(offset + 0);
         if (selectorOffset >= 0 && selectorOffset <= buffer.writerIndex() - offset - 12) {
            int pos = offset + 12 + selectorOffset;
            int selectorLen = VarInt.peek(buffer, pos);
            if (selectorLen < 0) {
               return ValidationResult.error("Invalid string length for Selector");
            } else if (selectorLen > 4096000) {
               return ValidationResult.error("Selector exceeds max length 4096000");
            } else {
               pos += VarInt.size(selectorLen);
               pos += selectorLen;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading Selector");
               } else {
                  selectorOffset = buffer.getIntLE(offset + 4);
                  if (selectorOffset >= 0 && selectorOffset <= buffer.writerIndex() - offset - 12) {
                     pos = offset + 12 + selectorOffset;
                     selectorLen = VarInt.peek(buffer, pos);
                     if (selectorLen < 0) {
                        return ValidationResult.error("Invalid string length for PropertyName");
                     } else if (selectorLen > 4096000) {
                        return ValidationResult.error("PropertyName exceeds max length 4096000");
                     } else {
                        pos += VarInt.size(selectorLen);
                        pos += selectorLen;
                        if (pos > buffer.writerIndex()) {
                           return ValidationResult.error("Buffer overflow reading PropertyName");
                        } else {
                           selectorOffset = buffer.getIntLE(offset + 8);
                           if (selectorOffset >= 0 && selectorOffset <= buffer.writerIndex() - offset - 12) {
                              pos = offset + 12 + selectorOffset;
                              ValidationResult valueResult = UIDataValue.validateStructure(buffer, pos);
                              if (!valueResult.isValid()) {
                                 return ValidationResult.error("Invalid Value: " + valueResult.error());
                              } else {
                                 pos += UIDataValue.computeBytesConsumed(buffer, pos);
                                 return ValidationResult.OK;
                              }
                           } else {
                              return ValidationResult.error("Invalid offset for Value");
                           }
                        }
                     }
                  } else {
                     return ValidationResult.error("Invalid offset for PropertyName");
                  }
               }
            }
         } else {
            return ValidationResult.error("Invalid offset for Selector");
         }
      }
   }

   public SetElementPropertyServersideUICommand clone() {
      SetElementPropertyServersideUICommand copy = new SetElementPropertyServersideUICommand();
      copy.selector = this.selector;
      copy.propertyName = this.propertyName;
      copy.value = this.value;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof SetElementPropertyServersideUICommand other)
            ? false
            : Objects.equals(this.selector, other.selector) && Objects.equals(this.propertyName, other.propertyName) && Objects.equals(this.value, other.value);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.selector, this.propertyName, this.value);
   }
}
