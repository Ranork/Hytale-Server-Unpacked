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

public class InitServersideUICommand extends ServersideUICommand {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 3;
   public static final int VARIABLE_BLOCK_START = 13;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public String id = "";
   @Nonnull
   public String filePath = "";
   @Nullable
   public UIObjectDataValue dataContext;

   public InitServersideUICommand() {
   }

   public InitServersideUICommand(@Nonnull String id, @Nonnull String filePath, @Nullable UIObjectDataValue dataContext) {
      this.id = id;
      this.filePath = filePath;
      this.dataContext = dataContext;
   }

   public InitServersideUICommand(@Nonnull InitServersideUICommand other) {
      this.id = other.id;
      this.filePath = other.filePath;
      this.dataContext = other.dataContext;
   }

   @Nonnull
   public static InitServersideUICommand deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 13) {
         throw ProtocolException.bufferTooSmall("InitServersideUICommand", 13, buf.readableBytes() - offset);
      } else {
         InitServersideUICommand obj = new InitServersideUICommand();
         byte nullBits = buf.getByte(offset);
         int varPosBase0 = buf.getIntLE(offset + 1);
         if (varPosBase0 >= 0 && varPosBase0 <= buf.writerIndex() - offset - 13) {
            int varPos0 = offset + 13 + varPosBase0;
            int idLen = VarInt.peek(buf, varPos0);
            if (idLen < 0) {
               throw ProtocolException.invalidVarInt("Id");
            } else {
               int idVarIntLen = VarInt.size(idLen);
               if (idLen > 4096000) {
                  throw ProtocolException.stringTooLong("Id", idLen, 4096000);
               } else if (varPos0 + idVarIntLen + idLen > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("Id", varPos0 + idVarIntLen + idLen, buf.readableBytes());
               } else {
                  obj.id = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
                  varPosBase0 = buf.getIntLE(offset + 5);
                  if (varPosBase0 >= 0 && varPosBase0 <= buf.writerIndex() - offset - 13) {
                     varPos0 = offset + 13 + varPosBase0;
                     idLen = VarInt.peek(buf, varPos0);
                     if (idLen < 0) {
                        throw ProtocolException.invalidVarInt("FilePath");
                     } else {
                        idVarIntLen = VarInt.size(idLen);
                        if (idLen > 4096000) {
                           throw ProtocolException.stringTooLong("FilePath", idLen, 4096000);
                        } else if (varPos0 + idVarIntLen + idLen > buf.readableBytes()) {
                           throw ProtocolException.bufferTooSmall("FilePath", varPos0 + idVarIntLen + idLen, buf.readableBytes());
                        } else {
                           obj.filePath = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
                           if ((nullBits & 1) != 0) {
                              varPosBase0 = buf.getIntLE(offset + 9);
                              if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 13) {
                                 throw ProtocolException.invalidOffset("DataContext", varPosBase0, buf.readableBytes());
                              }

                              varPos0 = offset + 13 + varPosBase0;
                              obj.dataContext = UIObjectDataValue.deserialize(buf, varPos0);
                           }

                           return obj;
                        }
                     }
                  } else {
                     throw ProtocolException.invalidOffset("FilePath", varPosBase0, buf.readableBytes());
                  }
               }
            }
         } else {
            throw ProtocolException.invalidOffset("Id", varPosBase0, buf.readableBytes());
         }
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 13;
      int fieldOffset0 = buf.getIntLE(offset + 1);
      if (fieldOffset0 >= 0 && fieldOffset0 <= buf.writerIndex() - offset - 13) {
         int pos0 = offset + 13 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }

         fieldOffset0 = buf.getIntLE(offset + 5);
         if (fieldOffset0 >= 0 && fieldOffset0 <= buf.writerIndex() - offset - 13) {
            pos0 = offset + 13 + fieldOffset0;
            sl = VarInt.peek(buf, pos0);
            pos0 += VarInt.size(sl) + sl;
            if (pos0 - offset > maxEnd) {
               maxEnd = pos0 - offset;
            }

            if ((nullBits & 1) != 0) {
               fieldOffset0 = buf.getIntLE(offset + 9);
               if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 13) {
                  throw ProtocolException.invalidOffset("DataContext", fieldOffset0, maxEnd);
               }

               pos0 = offset + 13 + fieldOffset0;
               pos0 += UIObjectDataValue.computeBytesConsumed(buf, pos0);
               if (pos0 - offset > maxEnd) {
                  maxEnd = pos0 - offset;
               }
            }

            return maxEnd;
         } else {
            throw ProtocolException.invalidOffset("FilePath", fieldOffset0, maxEnd);
         }
      } else {
         throw ProtocolException.invalidOffset("Id", fieldOffset0, maxEnd);
      }
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 13L;
   }

   public static String getId(MemorySegment mem) {
      return getId(mem, 0);
   }

   public static String getId(MemorySegment mem, int offset) {
      return PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 1, 13, "Id"), 4096000, PacketIO.UTF8);
   }

   public static String getFilePath(MemorySegment mem) {
      return getFilePath(mem, 0);
   }

   public static String getFilePath(MemorySegment mem, int offset) {
      return PacketIO.readVarString("FilePath", mem, offset + getValidatedOffset(mem, offset, 5, 13, "FilePath"), 4096000, PacketIO.UTF8);
   }

   @Nullable
   public static UIObjectDataValue getDataContext(MemorySegment mem) {
      return getDataContext(mem, 0);
   }

   @Nullable
   public static UIObjectDataValue getDataContext(MemorySegment mem, int offset) {
      return hasDataContext(mem, offset) ? UIObjectDataValue.toObject(mem, offset + getValidatedOffset(mem, offset, 9, 13, "DataContext")) : null;
   }

   public static boolean hasDataContext(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static InitServersideUICommand toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static InitServersideUICommand toObject(MemorySegment mem, int offset) {
      if (offset + 13 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("InitServersideUICommand", offset + 13, (int)mem.byteSize());
      } else {
         return new InitServersideUICommand(
            PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 1, 13, "Id"), 4096000, PacketIO.UTF8),
            PacketIO.readVarString("FilePath", mem, offset + getValidatedOffset(mem, offset, 5, 13, "FilePath"), 4096000, PacketIO.UTF8),
            hasDataContext(mem, offset) ? UIObjectDataValue.toObject(mem, offset + getValidatedOffset(mem, offset, 9, 13, "DataContext")) : null
         );
      }
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.dataContext != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      int idOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int filePathOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int dataContextOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      buf.setIntLE(idOffsetSlot, buf.writerIndex() - varBlockStart);
      PacketIO.writeVarString(buf, this.id, 4096000);
      buf.setIntLE(filePathOffsetSlot, buf.writerIndex() - varBlockStart);
      PacketIO.writeVarString(buf, this.filePath, 4096000);
      if (this.dataContext != null) {
         buf.setIntLE(dataContextOffsetSlot, buf.writerIndex() - varBlockStart);
         this.dataContext.serialize(buf);
      } else {
         buf.setIntLE(dataContextOffsetSlot, -1);
      }

      return buf.writerIndex() - startPos;
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.dataContext != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 13;
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), varOffset - offset - 13);
      varOffset += PacketIO.writeVarString(mem, varOffset, this.id, 4096000);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 5), varOffset - offset - 13);
      varOffset += PacketIO.writeVarString(mem, varOffset, this.filePath, 4096000);
      if (this.dataContext != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), varOffset - offset - 13);
         varOffset += this.dataContext.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), -1);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 13;
      size += PacketIO.stringSize(this.id);
      size += PacketIO.stringSize(this.filePath);
      if (this.dataContext != null) {
         size += this.dataContext.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 13) {
         return ValidationResult.error("Buffer too small: expected at least 13 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int idOffset = buffer.getIntLE(offset + 1);
         if (idOffset >= 0 && idOffset <= buffer.writerIndex() - offset - 13) {
            int pos = offset + 13 + idOffset;
            int idLen = VarInt.peek(buffer, pos);
            if (idLen < 0) {
               return ValidationResult.error("Invalid string length for Id");
            } else if (idLen > 4096000) {
               return ValidationResult.error("Id exceeds max length 4096000");
            } else {
               pos += VarInt.size(idLen);
               pos += idLen;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading Id");
               } else {
                  idOffset = buffer.getIntLE(offset + 5);
                  if (idOffset >= 0 && idOffset <= buffer.writerIndex() - offset - 13) {
                     pos = offset + 13 + idOffset;
                     idLen = VarInt.peek(buffer, pos);
                     if (idLen < 0) {
                        return ValidationResult.error("Invalid string length for FilePath");
                     } else if (idLen > 4096000) {
                        return ValidationResult.error("FilePath exceeds max length 4096000");
                     } else {
                        pos += VarInt.size(idLen);
                        pos += idLen;
                        if (pos > buffer.writerIndex()) {
                           return ValidationResult.error("Buffer overflow reading FilePath");
                        } else {
                           if ((nullBits & 1) != 0) {
                              idOffset = buffer.getIntLE(offset + 9);
                              if (idOffset < 0 || idOffset > buffer.writerIndex() - offset - 13) {
                                 return ValidationResult.error("Invalid offset for DataContext");
                              }

                              pos = offset + 13 + idOffset;
                              ValidationResult dataContextResult = UIObjectDataValue.validateStructure(buffer, pos);
                              if (!dataContextResult.isValid()) {
                                 return ValidationResult.error("Invalid DataContext: " + dataContextResult.error());
                              }

                              pos += UIObjectDataValue.computeBytesConsumed(buffer, pos);
                           }

                           return ValidationResult.OK;
                        }
                     }
                  } else {
                     return ValidationResult.error("Invalid offset for FilePath");
                  }
               }
            }
         } else {
            return ValidationResult.error("Invalid offset for Id");
         }
      }
   }

   public InitServersideUICommand clone() {
      InitServersideUICommand copy = new InitServersideUICommand();
      copy.id = this.id;
      copy.filePath = this.filePath;
      copy.dataContext = this.dataContext != null ? this.dataContext.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof InitServersideUICommand other)
            ? false
            : Objects.equals(this.id, other.id) && Objects.equals(this.filePath, other.filePath) && Objects.equals(this.dataContext, other.dataContext);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.id, this.filePath, this.dataContext);
   }
}
