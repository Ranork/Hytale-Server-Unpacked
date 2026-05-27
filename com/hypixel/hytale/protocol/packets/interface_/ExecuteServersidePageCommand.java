package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ExecuteServersidePageCommand implements Packet, ToServerPacket {
   public static final int PACKET_ID = 1202;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 3;
   public static final int VARIABLE_BLOCK_START = 13;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public String pageId = "";
   @Nonnull
   public String commandId = "";
   @Nullable
   public UIDataValue param;

   @Override
   public int getId() {
      return 1202;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public ExecuteServersidePageCommand() {
   }

   public ExecuteServersidePageCommand(@Nonnull String pageId, @Nonnull String commandId, @Nullable UIDataValue param) {
      this.pageId = pageId;
      this.commandId = commandId;
      this.param = param;
   }

   public ExecuteServersidePageCommand(@Nonnull ExecuteServersidePageCommand other) {
      this.pageId = other.pageId;
      this.commandId = other.commandId;
      this.param = other.param;
   }

   @Nonnull
   public static ExecuteServersidePageCommand deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 13) {
         throw ProtocolException.bufferTooSmall("ExecuteServersidePageCommand", 13, buf.readableBytes() - offset);
      } else {
         ExecuteServersidePageCommand obj = new ExecuteServersidePageCommand();
         byte nullBits = buf.getByte(offset);
         int varPosBase0 = buf.getIntLE(offset + 1);
         if (varPosBase0 >= 0 && varPosBase0 <= buf.writerIndex() - offset - 13) {
            int varPos0 = offset + 13 + varPosBase0;
            int pageIdLen = VarInt.peek(buf, varPos0);
            if (pageIdLen < 0) {
               throw ProtocolException.invalidVarInt("PageId");
            } else {
               int pageIdVarIntLen = VarInt.size(pageIdLen);
               if (pageIdLen > 4096000) {
                  throw ProtocolException.stringTooLong("PageId", pageIdLen, 4096000);
               } else if (varPos0 + pageIdVarIntLen + pageIdLen > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("PageId", varPos0 + pageIdVarIntLen + pageIdLen, buf.readableBytes());
               } else {
                  obj.pageId = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
                  varPosBase0 = buf.getIntLE(offset + 5);
                  if (varPosBase0 >= 0 && varPosBase0 <= buf.writerIndex() - offset - 13) {
                     varPos0 = offset + 13 + varPosBase0;
                     pageIdLen = VarInt.peek(buf, varPos0);
                     if (pageIdLen < 0) {
                        throw ProtocolException.invalidVarInt("CommandId");
                     } else {
                        pageIdVarIntLen = VarInt.size(pageIdLen);
                        if (pageIdLen > 4096000) {
                           throw ProtocolException.stringTooLong("CommandId", pageIdLen, 4096000);
                        } else if (varPos0 + pageIdVarIntLen + pageIdLen > buf.readableBytes()) {
                           throw ProtocolException.bufferTooSmall("CommandId", varPos0 + pageIdVarIntLen + pageIdLen, buf.readableBytes());
                        } else {
                           obj.commandId = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
                           if ((nullBits & 1) != 0) {
                              varPosBase0 = buf.getIntLE(offset + 9);
                              if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 13) {
                                 throw ProtocolException.invalidOffset("Param", varPosBase0, buf.readableBytes());
                              }

                              varPos0 = offset + 13 + varPosBase0;
                              obj.param = UIDataValue.deserialize(buf, varPos0);
                           }

                           return obj;
                        }
                     }
                  } else {
                     throw ProtocolException.invalidOffset("CommandId", varPosBase0, buf.readableBytes());
                  }
               }
            }
         } else {
            throw ProtocolException.invalidOffset("PageId", varPosBase0, buf.readableBytes());
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
                  throw ProtocolException.invalidOffset("Param", fieldOffset0, maxEnd);
               }

               pos0 = offset + 13 + fieldOffset0;
               pos0 += UIDataValue.computeBytesConsumed(buf, pos0);
               if (pos0 - offset > maxEnd) {
                  maxEnd = pos0 - offset;
               }
            }

            return maxEnd;
         } else {
            throw ProtocolException.invalidOffset("CommandId", fieldOffset0, maxEnd);
         }
      } else {
         throw ProtocolException.invalidOffset("PageId", fieldOffset0, maxEnd);
      }
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 13L;
   }

   public static String getPageId(MemorySegment mem) {
      return getPageId(mem, 0);
   }

   public static String getPageId(MemorySegment mem, int offset) {
      return PacketIO.readVarString("PageId", mem, offset + getValidatedOffset(mem, offset, 1, 13, "PageId"), 4096000, PacketIO.UTF8);
   }

   public static String getCommandId(MemorySegment mem) {
      return getCommandId(mem, 0);
   }

   public static String getCommandId(MemorySegment mem, int offset) {
      return PacketIO.readVarString("CommandId", mem, offset + getValidatedOffset(mem, offset, 5, 13, "CommandId"), 4096000, PacketIO.UTF8);
   }

   @Nullable
   public static UIDataValue getParam(MemorySegment mem) {
      return getParam(mem, 0);
   }

   @Nullable
   public static UIDataValue getParam(MemorySegment mem, int offset) {
      return hasParam(mem, offset) ? UIDataValue.toObject(mem, offset + getValidatedOffset(mem, offset, 9, 13, "Param")) : null;
   }

   public static boolean hasParam(MemorySegment mem, int offset) {
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

   public static ExecuteServersidePageCommand toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ExecuteServersidePageCommand toObject(MemorySegment mem, int offset) {
      if (offset + 13 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ExecuteServersidePageCommand", offset + 13, (int)mem.byteSize());
      } else {
         return new ExecuteServersidePageCommand(
            PacketIO.readVarString("PageId", mem, offset + getValidatedOffset(mem, offset, 1, 13, "PageId"), 4096000, PacketIO.UTF8),
            PacketIO.readVarString("CommandId", mem, offset + getValidatedOffset(mem, offset, 5, 13, "CommandId"), 4096000, PacketIO.UTF8),
            hasParam(mem, offset) ? UIDataValue.toObject(mem, offset + getValidatedOffset(mem, offset, 9, 13, "Param")) : null
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.param != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      int pageIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int commandIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int paramOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      buf.setIntLE(pageIdOffsetSlot, buf.writerIndex() - varBlockStart);
      PacketIO.writeVarString(buf, this.pageId, 4096000);
      buf.setIntLE(commandIdOffsetSlot, buf.writerIndex() - varBlockStart);
      PacketIO.writeVarString(buf, this.commandId, 4096000);
      if (this.param != null) {
         buf.setIntLE(paramOffsetSlot, buf.writerIndex() - varBlockStart);
         this.param.serializeWithTypeId(buf);
      } else {
         buf.setIntLE(paramOffsetSlot, -1);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.param != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 13;
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), varOffset - offset - 13);
      varOffset += PacketIO.writeVarString(mem, varOffset, this.pageId, 4096000);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 5), varOffset - offset - 13);
      varOffset += PacketIO.writeVarString(mem, varOffset, this.commandId, 4096000);
      if (this.param != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), varOffset - offset - 13);
         varOffset += this.param.serializeWithTypeId(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), -1);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 13;
      size += PacketIO.stringSize(this.pageId);
      size += PacketIO.stringSize(this.commandId);
      if (this.param != null) {
         size += this.param.computeSizeWithTypeId();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 13) {
         return ValidationResult.error("Buffer too small: expected at least 13 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pageIdOffset = buffer.getIntLE(offset + 1);
         if (pageIdOffset >= 0 && pageIdOffset <= buffer.writerIndex() - offset - 13) {
            int pos = offset + 13 + pageIdOffset;
            int pageIdLen = VarInt.peek(buffer, pos);
            if (pageIdLen < 0) {
               return ValidationResult.error("Invalid string length for PageId");
            } else if (pageIdLen > 4096000) {
               return ValidationResult.error("PageId exceeds max length 4096000");
            } else {
               pos += VarInt.size(pageIdLen);
               pos += pageIdLen;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading PageId");
               } else {
                  pageIdOffset = buffer.getIntLE(offset + 5);
                  if (pageIdOffset >= 0 && pageIdOffset <= buffer.writerIndex() - offset - 13) {
                     pos = offset + 13 + pageIdOffset;
                     pageIdLen = VarInt.peek(buffer, pos);
                     if (pageIdLen < 0) {
                        return ValidationResult.error("Invalid string length for CommandId");
                     } else if (pageIdLen > 4096000) {
                        return ValidationResult.error("CommandId exceeds max length 4096000");
                     } else {
                        pos += VarInt.size(pageIdLen);
                        pos += pageIdLen;
                        if (pos > buffer.writerIndex()) {
                           return ValidationResult.error("Buffer overflow reading CommandId");
                        } else {
                           if ((nullBits & 1) != 0) {
                              pageIdOffset = buffer.getIntLE(offset + 9);
                              if (pageIdOffset < 0 || pageIdOffset > buffer.writerIndex() - offset - 13) {
                                 return ValidationResult.error("Invalid offset for Param");
                              }

                              pos = offset + 13 + pageIdOffset;
                              ValidationResult paramResult = UIDataValue.validateStructure(buffer, pos);
                              if (!paramResult.isValid()) {
                                 return ValidationResult.error("Invalid Param: " + paramResult.error());
                              }

                              pos += UIDataValue.computeBytesConsumed(buffer, pos);
                           }

                           return ValidationResult.OK;
                        }
                     }
                  } else {
                     return ValidationResult.error("Invalid offset for CommandId");
                  }
               }
            }
         } else {
            return ValidationResult.error("Invalid offset for PageId");
         }
      }
   }

   public ExecuteServersidePageCommand clone() {
      ExecuteServersidePageCommand copy = new ExecuteServersidePageCommand();
      copy.pageId = this.pageId;
      copy.commandId = this.commandId;
      copy.param = this.param;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ExecuteServersidePageCommand other)
            ? false
            : Objects.equals(this.pageId, other.pageId) && Objects.equals(this.commandId, other.commandId) && Objects.equals(this.param, other.param);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.pageId, this.commandId, this.param);
   }
}
