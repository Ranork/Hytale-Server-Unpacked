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

public class CustomHud implements Packet, ToClientPacket {
   public static final int PACKET_ID = 217;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 6;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 14;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public String hudId = "";
   public int zOrder;
   public boolean clear;
   @Nullable
   public CustomUICommand[] commands;

   @Override
   public int getId() {
      return 217;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public CustomHud() {
   }

   public CustomHud(@Nonnull String hudId, int zOrder, boolean clear, @Nullable CustomUICommand[] commands) {
      this.hudId = hudId;
      this.zOrder = zOrder;
      this.clear = clear;
      this.commands = commands;
   }

   public CustomHud(@Nonnull CustomHud other) {
      this.hudId = other.hudId;
      this.zOrder = other.zOrder;
      this.clear = other.clear;
      this.commands = other.commands;
   }

   @Nonnull
   public static CustomHud deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 14) {
         throw ProtocolException.bufferTooSmall("CustomHud", 14, buf.readableBytes() - offset);
      } else {
         CustomHud obj = new CustomHud();
         byte nullBits = buf.getByte(offset);
         obj.zOrder = buf.getIntLE(offset + 1);
         obj.clear = buf.getByte(offset + 5) != 0;
         int varPosBase0 = buf.getIntLE(offset + 6);
         if (varPosBase0 >= 0 && varPosBase0 <= buf.writerIndex() - offset - 14) {
            int varPos0 = offset + 14 + varPosBase0;
            int hudIdLen = VarInt.peek(buf, varPos0);
            if (hudIdLen < 0) {
               throw ProtocolException.invalidVarInt("HudId");
            } else {
               int hudIdVarIntLen = VarInt.size(hudIdLen);
               if (hudIdLen > 4096000) {
                  throw ProtocolException.stringTooLong("HudId", hudIdLen, 4096000);
               } else if (varPos0 + hudIdVarIntLen + hudIdLen > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("HudId", varPos0 + hudIdVarIntLen + hudIdLen, buf.readableBytes());
               } else {
                  obj.hudId = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
                  if ((nullBits & 1) != 0) {
                     varPosBase0 = buf.getIntLE(offset + 10);
                     if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 14) {
                        throw ProtocolException.invalidOffset("Commands", varPosBase0, buf.readableBytes());
                     }

                     varPos0 = offset + 14 + varPosBase0;
                     hudIdLen = VarInt.peek(buf, varPos0);
                     if (hudIdLen < 0) {
                        throw ProtocolException.invalidVarInt("Commands");
                     }

                     hudIdVarIntLen = VarInt.size(hudIdLen);
                     if (hudIdLen > 4096000) {
                        throw ProtocolException.arrayTooLong("Commands", hudIdLen, 4096000);
                     }

                     if (varPos0 + hudIdVarIntLen + hudIdLen * 2L > buf.readableBytes()) {
                        throw ProtocolException.bufferTooSmall("Commands", varPos0 + hudIdVarIntLen + hudIdLen * 2, buf.readableBytes());
                     }

                     obj.commands = new CustomUICommand[hudIdLen];
                     int elemPos = varPos0 + hudIdVarIntLen;

                     for (int i = 0; i < hudIdLen; i++) {
                        obj.commands[i] = CustomUICommand.deserialize(buf, elemPos);
                        elemPos += CustomUICommand.computeBytesConsumed(buf, elemPos);
                     }
                  }

                  return obj;
               }
            }
         } else {
            throw ProtocolException.invalidOffset("HudId", varPosBase0, buf.readableBytes());
         }
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 14;
      int fieldOffset0 = buf.getIntLE(offset + 6);
      if (fieldOffset0 >= 0 && fieldOffset0 <= buf.writerIndex() - offset - 14) {
         int pos0 = offset + 14 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }

         if ((nullBits & 1) != 0) {
            fieldOffset0 = buf.getIntLE(offset + 10);
            if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 14) {
               throw ProtocolException.invalidOffset("Commands", fieldOffset0, maxEnd);
            }

            pos0 = offset + 14 + fieldOffset0;
            sl = VarInt.peek(buf, pos0);
            pos0 += VarInt.size(sl);

            for (int i = 0; i < sl; i++) {
               pos0 += CustomUICommand.computeBytesConsumed(buf, pos0);
            }

            if (pos0 - offset > maxEnd) {
               maxEnd = pos0 - offset;
            }
         }

         return maxEnd;
      } else {
         throw ProtocolException.invalidOffset("HudId", fieldOffset0, maxEnd);
      }
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 14L;
   }

   public static String getHudId(MemorySegment mem) {
      return getHudId(mem, 0);
   }

   public static String getHudId(MemorySegment mem, int offset) {
      return PacketIO.readVarString("HudId", mem, offset + getValidatedOffset(mem, offset, 6, 14, "HudId"), 4096000, PacketIO.UTF8);
   }

   public static int getZOrder(MemorySegment mem) {
      return getZOrder(mem, 0);
   }

   public static int getZOrder(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   public static boolean getClear(MemorySegment mem) {
      return getClear(mem, 0);
   }

   public static boolean getClear(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 5));
   }

   @Nullable
   public static CustomUICommand[] getCommands(MemorySegment mem) {
      return getCommands(mem, 0);
   }

   @Nullable
   public static CustomUICommand[] getCommands(MemorySegment mem, int offset) {
      if (!hasCommands(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 10, 14, "Commands");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Commands", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Commands", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Commands", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               CustomUICommand[] data = new CustomUICommand[len];

               for (int i = 0; i < len; i++) {
                  data[i] = CustomUICommand.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   public static boolean hasCommands(MemorySegment mem, int offset) {
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

   public static CustomHud toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static CustomHud toObject(MemorySegment mem, int offset) {
      if (offset + 14 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("CustomHud", offset + 14, (int)mem.byteSize());
      } else {
         CustomUICommand[] commands = null;
         if (hasCommands(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 10, 14, "Commands");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Commands", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Commands", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Commands", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            commands = new CustomUICommand[len];

            for (int i = 0; i < len; i++) {
               commands[i] = CustomUICommand.toObject(mem, off);
               off += commands[i].computeSize();
            }
         }

         return new CustomHud(
            PacketIO.readVarString("HudId", mem, offset + getValidatedOffset(mem, offset, 6, 14, "HudId"), 4096000, PacketIO.UTF8),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 5)),
            commands
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.commands != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.zOrder);
      buf.writeByte(this.clear ? 1 : 0);
      int hudIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int commandsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      buf.setIntLE(hudIdOffsetSlot, buf.writerIndex() - varBlockStart);
      PacketIO.writeVarString(buf, this.hudId, 4096000);
      if (this.commands != null) {
         buf.setIntLE(commandsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.commands.length > 4096000) {
            throw ProtocolException.arrayTooLong("Commands", this.commands.length, 4096000);
         }

         VarInt.write(buf, this.commands.length);

         for (CustomUICommand item : this.commands) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(commandsOffsetSlot, -1);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.commands != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.zOrder);
      mem.set(PacketIO.PROTO_BOOL, offset + 5, this.clear);
      int varOffset = offset + 14;
      mem.set(PacketIO.PROTO_INT, (long)(offset + 6), varOffset - offset - 14);
      varOffset += PacketIO.writeVarString(mem, varOffset, this.hudId, 4096000);
      if (this.commands != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 10), varOffset - offset - 14);
         if (this.commands.length > 4096000) {
            throw ProtocolException.arrayTooLong("Commands", this.commands.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.commands.length);
         int commandsValueOffset = 0;

         for (int i = 0; i < this.commands.length; i++) {
            commandsValueOffset += this.commands[i].serialize(mem, varOffset + commandsValueOffset);
         }

         varOffset += commandsValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 10), -1);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 14;
      size += PacketIO.stringSize(this.hudId);
      if (this.commands != null) {
         int commandsSize = 0;

         for (CustomUICommand elem : this.commands) {
            commandsSize += elem.computeSize();
         }

         size += VarInt.size(this.commands.length) + commandsSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 14) {
         return ValidationResult.error("Buffer too small: expected at least 14 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int hudIdOffset = buffer.getIntLE(offset + 6);
         if (hudIdOffset >= 0 && hudIdOffset <= buffer.writerIndex() - offset - 14) {
            int pos = offset + 14 + hudIdOffset;
            int hudIdLen = VarInt.peek(buffer, pos);
            if (hudIdLen < 0) {
               return ValidationResult.error("Invalid string length for HudId");
            } else if (hudIdLen > 4096000) {
               return ValidationResult.error("HudId exceeds max length 4096000");
            } else {
               pos += VarInt.size(hudIdLen);
               pos += hudIdLen;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading HudId");
               } else {
                  if ((nullBits & 1) != 0) {
                     hudIdOffset = buffer.getIntLE(offset + 10);
                     if (hudIdOffset < 0 || hudIdOffset > buffer.writerIndex() - offset - 14) {
                        return ValidationResult.error("Invalid offset for Commands");
                     }

                     pos = offset + 14 + hudIdOffset;
                     hudIdLen = VarInt.peek(buffer, pos);
                     if (hudIdLen < 0) {
                        return ValidationResult.error("Invalid array count for Commands");
                     }

                     if (hudIdLen > 4096000) {
                        return ValidationResult.error("Commands exceeds max length 4096000");
                     }

                     pos += VarInt.size(hudIdLen);

                     for (int i = 0; i < hudIdLen; i++) {
                        ValidationResult structResult = CustomUICommand.validateStructure(buffer, pos);
                        if (!structResult.isValid()) {
                           return ValidationResult.error("Invalid CustomUICommand in Commands[" + i + "]: " + structResult.error());
                        }

                        pos += CustomUICommand.computeBytesConsumed(buffer, pos);
                     }
                  }

                  return ValidationResult.OK;
               }
            }
         } else {
            return ValidationResult.error("Invalid offset for HudId");
         }
      }
   }

   public CustomHud clone() {
      CustomHud copy = new CustomHud();
      copy.hudId = this.hudId;
      copy.zOrder = this.zOrder;
      copy.clear = this.clear;
      copy.commands = this.commands != null ? Arrays.stream(this.commands).map(e -> e.clone()).toArray(CustomUICommand[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof CustomHud other)
            ? false
            : Objects.equals(this.hudId, other.hudId)
               && this.zOrder == other.zOrder
               && this.clear == other.clear
               && Arrays.equals((Object[])this.commands, (Object[])other.commands);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.hudId);
      result = 31 * result + Integer.hashCode(this.zOrder);
      result = 31 * result + Boolean.hashCode(this.clear);
      return 31 * result + Arrays.hashCode((Object[])this.commands);
   }
}
