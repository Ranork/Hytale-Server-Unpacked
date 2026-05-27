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

public class ArgValuesRequest implements Packet, ToServerPacket {
   public static final int PACKET_ID = 239;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 32768019;
   @Nullable
   public String argTypeId;
   @Nullable
   public String partial;

   @Override
   public int getId() {
      return 239;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public ArgValuesRequest() {
   }

   public ArgValuesRequest(@Nullable String argTypeId, @Nullable String partial) {
      this.argTypeId = argTypeId;
      this.partial = partial;
   }

   public ArgValuesRequest(@Nonnull ArgValuesRequest other) {
      this.argTypeId = other.argTypeId;
      this.partial = other.partial;
   }

   @Nonnull
   public static ArgValuesRequest deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 9) {
         throw ProtocolException.bufferTooSmall("ArgValuesRequest", 9, buf.readableBytes() - offset);
      } else {
         ArgValuesRequest obj = new ArgValuesRequest();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 1);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 9) {
               throw ProtocolException.invalidOffset("ArgTypeId", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 9 + varPosBase0;
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
            int varPosBase1 = buf.getIntLE(offset + 5);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 9) {
               throw ProtocolException.invalidOffset("Partial", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 9 + varPosBase1;
            int partialLen = VarInt.peek(buf, varPos1);
            if (partialLen < 0) {
               throw ProtocolException.invalidVarInt("Partial");
            }

            int partialVarIntLen = VarInt.size(partialLen);
            if (partialLen > 4096000) {
               throw ProtocolException.stringTooLong("Partial", partialLen, 4096000);
            }

            if (varPos1 + partialVarIntLen + partialLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Partial", varPos1 + partialVarIntLen + partialLen, buf.readableBytes());
            }

            obj.partial = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 9;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 9) {
            throw ProtocolException.invalidOffset("ArgTypeId", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 9 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 9) {
            throw ProtocolException.invalidOffset("Partial", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 9 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(sl) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 9L;
   }

   @Nullable
   public static String getArgTypeId(MemorySegment mem) {
      return getArgTypeId(mem, 0);
   }

   @Nullable
   public static String getArgTypeId(MemorySegment mem, int offset) {
      return hasArgTypeId(mem, offset)
         ? PacketIO.readVarString("ArgTypeId", mem, offset + getValidatedOffset(mem, offset, 1, 9, "ArgTypeId"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static String getPartial(MemorySegment mem) {
      return getPartial(mem, 0);
   }

   @Nullable
   public static String getPartial(MemorySegment mem, int offset) {
      return hasPartial(mem, offset)
         ? PacketIO.readVarString("Partial", mem, offset + getValidatedOffset(mem, offset, 5, 9, "Partial"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static boolean hasArgTypeId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasPartial(MemorySegment mem, int offset) {
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

   public static ArgValuesRequest toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ArgValuesRequest toObject(MemorySegment mem, int offset) {
      if (offset + 9 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ArgValuesRequest", offset + 9, (int)mem.byteSize());
      } else {
         return new ArgValuesRequest(
            hasArgTypeId(mem, offset)
               ? PacketIO.readVarString("ArgTypeId", mem, offset + getValidatedOffset(mem, offset, 1, 9, "ArgTypeId"), 4096000, PacketIO.UTF8)
               : null,
            hasPartial(mem, offset)
               ? PacketIO.readVarString("Partial", mem, offset + getValidatedOffset(mem, offset, 5, 9, "Partial"), 4096000, PacketIO.UTF8)
               : null
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

      if (this.partial != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      int argTypeIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int partialOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.argTypeId != null) {
         buf.setIntLE(argTypeIdOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.argTypeId, 4096000);
      } else {
         buf.setIntLE(argTypeIdOffsetSlot, -1);
      }

      if (this.partial != null) {
         buf.setIntLE(partialOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.partial, 4096000);
      } else {
         buf.setIntLE(partialOffsetSlot, -1);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.argTypeId != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.partial != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 9;
      if (this.argTypeId != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), varOffset - offset - 9);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.argTypeId, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), -1);
      }

      if (this.partial != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), varOffset - offset - 9);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.partial, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), -1);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 9;
      if (this.argTypeId != null) {
         size += PacketIO.stringSize(this.argTypeId);
      }

      if (this.partial != null) {
         size += PacketIO.stringSize(this.partial);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 9) {
         return ValidationResult.error("Buffer too small: expected at least 9 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int argTypeIdOffset = buffer.getIntLE(offset + 1);
            if (argTypeIdOffset < 0 || argTypeIdOffset > buffer.writerIndex() - offset - 9) {
               return ValidationResult.error("Invalid offset for ArgTypeId");
            }

            int pos = offset + 9 + argTypeIdOffset;
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
            int partialOffset = buffer.getIntLE(offset + 5);
            if (partialOffset < 0 || partialOffset > buffer.writerIndex() - offset - 9) {
               return ValidationResult.error("Invalid offset for Partial");
            }

            int posx = offset + 9 + partialOffset;
            int partialLen = VarInt.peek(buffer, posx);
            if (partialLen < 0) {
               return ValidationResult.error("Invalid string length for Partial");
            }

            if (partialLen > 4096000) {
               return ValidationResult.error("Partial exceeds max length 4096000");
            }

            posx += VarInt.size(partialLen);
            posx += partialLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Partial");
            }
         }

         return ValidationResult.OK;
      }
   }

   public ArgValuesRequest clone() {
      ArgValuesRequest copy = new ArgValuesRequest();
      copy.argTypeId = this.argTypeId;
      copy.partial = this.partial;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ArgValuesRequest other)
            ? false
            : Objects.equals(this.argTypeId, other.argTypeId) && Objects.equals(this.partial, other.partial);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.argTypeId, this.partial);
   }
}
