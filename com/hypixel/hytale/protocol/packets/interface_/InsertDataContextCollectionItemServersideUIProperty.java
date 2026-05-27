package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class InsertDataContextCollectionItemServersideUIProperty extends ServersideUICommand {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 4;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 12;
   public static final int MAX_SIZE = 16385041;
   @Nonnull
   public String property = "";
   public int index;
   @Nonnull
   public UIDataValue value;

   public InsertDataContextCollectionItemServersideUIProperty() {
   }

   public InsertDataContextCollectionItemServersideUIProperty(@Nonnull String property, int index, @Nonnull UIDataValue value) {
      this.property = property;
      this.index = index;
      this.value = value;
   }

   public InsertDataContextCollectionItemServersideUIProperty(@Nonnull InsertDataContextCollectionItemServersideUIProperty other) {
      this.property = other.property;
      this.index = other.index;
      this.value = other.value;
   }

   @Nonnull
   public static InsertDataContextCollectionItemServersideUIProperty deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 12) {
         throw ProtocolException.bufferTooSmall("InsertDataContextCollectionItemServersideUIProperty", 12, buf.readableBytes() - offset);
      } else {
         InsertDataContextCollectionItemServersideUIProperty obj = new InsertDataContextCollectionItemServersideUIProperty();
         obj.index = buf.getIntLE(offset + 0);
         int varPosBase0 = buf.getIntLE(offset + 4);
         if (varPosBase0 >= 0 && varPosBase0 <= buf.writerIndex() - offset - 12) {
            int varPos0 = offset + 12 + varPosBase0;
            int propertyLen = VarInt.peek(buf, varPos0);
            if (propertyLen < 0) {
               throw ProtocolException.invalidVarInt("Property");
            } else {
               int propertyVarIntLen = VarInt.size(propertyLen);
               if (propertyLen > 4096000) {
                  throw ProtocolException.stringTooLong("Property", propertyLen, 4096000);
               } else if (varPos0 + propertyVarIntLen + propertyLen > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("Property", varPos0 + propertyVarIntLen + propertyLen, buf.readableBytes());
               } else {
                  obj.property = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
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
            throw ProtocolException.invalidOffset("Property", varPosBase0, buf.readableBytes());
         }
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      int maxEnd = 12;
      int fieldOffset0 = buf.getIntLE(offset + 4);
      if (fieldOffset0 >= 0 && fieldOffset0 <= buf.writerIndex() - offset - 12) {
         int pos0 = offset + 12 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
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
         throw ProtocolException.invalidOffset("Property", fieldOffset0, maxEnd);
      }
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 12L;
   }

   public static String getProperty(MemorySegment mem) {
      return getProperty(mem, 0);
   }

   public static String getProperty(MemorySegment mem, int offset) {
      return PacketIO.readVarString("Property", mem, offset + getValidatedOffset(mem, offset, 4, 12, "Property"), 4096000, PacketIO.UTF8);
   }

   public static int getIndex(MemorySegment mem) {
      return getIndex(mem, 0);
   }

   public static int getIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 0));
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

   public static InsertDataContextCollectionItemServersideUIProperty toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static InsertDataContextCollectionItemServersideUIProperty toObject(MemorySegment mem, int offset) {
      if (offset + 12 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("InsertDataContextCollectionItemServersideUIProperty", offset + 12, (int)mem.byteSize());
      } else {
         return new InsertDataContextCollectionItemServersideUIProperty(
            PacketIO.readVarString("Property", mem, offset + getValidatedOffset(mem, offset, 4, 12, "Property"), 4096000, PacketIO.UTF8),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 0)),
            UIDataValue.toObject(mem, offset + getValidatedOffset(mem, offset, 8, 12, "Value"))
         );
      }
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      buf.writeIntLE(this.index);
      int propertyOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int valueOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      buf.setIntLE(propertyOffsetSlot, buf.writerIndex() - varBlockStart);
      PacketIO.writeVarString(buf, this.property, 4096000);
      buf.setIntLE(valueOffsetSlot, buf.writerIndex() - varBlockStart);
      this.value.serializeWithTypeId(buf);
      return buf.writerIndex() - startPos;
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_INT, (long)(offset + 0), this.index);
      int varOffset = offset + 12;
      mem.set(PacketIO.PROTO_INT, (long)(offset + 4), varOffset - offset - 12);
      varOffset += PacketIO.writeVarString(mem, varOffset, this.property, 4096000);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 8), varOffset - offset - 12);
      varOffset += this.value.serializeWithTypeId(mem, varOffset);
      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 12;
      size += PacketIO.stringSize(this.property);
      return size + this.value.computeSizeWithTypeId();
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 12) {
         return ValidationResult.error("Buffer too small: expected at least 12 bytes");
      } else {
         int propertyOffset = buffer.getIntLE(offset + 4);
         if (propertyOffset >= 0 && propertyOffset <= buffer.writerIndex() - offset - 12) {
            int pos = offset + 12 + propertyOffset;
            int propertyLen = VarInt.peek(buffer, pos);
            if (propertyLen < 0) {
               return ValidationResult.error("Invalid string length for Property");
            } else if (propertyLen > 4096000) {
               return ValidationResult.error("Property exceeds max length 4096000");
            } else {
               pos += VarInt.size(propertyLen);
               pos += propertyLen;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading Property");
               } else {
                  propertyOffset = buffer.getIntLE(offset + 8);
                  if (propertyOffset >= 0 && propertyOffset <= buffer.writerIndex() - offset - 12) {
                     pos = offset + 12 + propertyOffset;
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
            return ValidationResult.error("Invalid offset for Property");
         }
      }
   }

   public InsertDataContextCollectionItemServersideUIProperty clone() {
      InsertDataContextCollectionItemServersideUIProperty copy = new InsertDataContextCollectionItemServersideUIProperty();
      copy.property = this.property;
      copy.index = this.index;
      copy.value = this.value;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof InsertDataContextCollectionItemServersideUIProperty other)
            ? false
            : Objects.equals(this.property, other.property) && this.index == other.index && Objects.equals(this.value, other.value);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.property, this.index, this.value);
   }
}
