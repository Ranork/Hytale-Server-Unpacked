package com.hypixel.hytale.protocol.packets.buildertools;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderToolGMaskPreset implements Packet, ToClientPacket {
   public static final int PACKET_ID = 430;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 10;
   public static final int MAX_SIZE = 32768020;
   public boolean isSave;
   @Nullable
   public String name;
   @Nullable
   public String maskData;

   @Override
   public int getId() {
      return 430;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public BuilderToolGMaskPreset() {
   }

   public BuilderToolGMaskPreset(boolean isSave, @Nullable String name, @Nullable String maskData) {
      this.isSave = isSave;
      this.name = name;
      this.maskData = maskData;
   }

   public BuilderToolGMaskPreset(@Nonnull BuilderToolGMaskPreset other) {
      this.isSave = other.isSave;
      this.name = other.name;
      this.maskData = other.maskData;
   }

   @Nonnull
   public static BuilderToolGMaskPreset deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 10) {
         throw ProtocolException.bufferTooSmall("BuilderToolGMaskPreset", 10, buf.readableBytes() - offset);
      } else {
         BuilderToolGMaskPreset obj = new BuilderToolGMaskPreset();
         byte nullBits = buf.getByte(offset);
         obj.isSave = buf.getByte(offset + 1) != 0;
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 2);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 10) {
               throw ProtocolException.invalidOffset("Name", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 10 + varPosBase0;
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

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 6);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 10) {
               throw ProtocolException.invalidOffset("MaskData", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 10 + varPosBase1;
            int maskDataLen = VarInt.peek(buf, varPos1);
            if (maskDataLen < 0) {
               throw ProtocolException.invalidVarInt("MaskData");
            }

            int maskDataVarIntLen = VarInt.size(maskDataLen);
            if (maskDataLen > 4096000) {
               throw ProtocolException.stringTooLong("MaskData", maskDataLen, 4096000);
            }

            if (varPos1 + maskDataVarIntLen + maskDataLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("MaskData", varPos1 + maskDataVarIntLen + maskDataLen, buf.readableBytes());
            }

            obj.maskData = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 10;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 2);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 10) {
            throw ProtocolException.invalidOffset("Name", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 10 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 6);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 10) {
            throw ProtocolException.invalidOffset("MaskData", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 10 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(sl) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 10L;
   }

   public static boolean getIsSave(MemorySegment mem) {
      return getIsSave(mem, 0);
   }

   public static boolean getIsSave(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1));
   }

   @Nullable
   public static String getName(MemorySegment mem) {
      return getName(mem, 0);
   }

   @Nullable
   public static String getName(MemorySegment mem, int offset) {
      return hasName(mem, offset) ? PacketIO.readVarString("Name", mem, offset + getValidatedOffset(mem, offset, 2, 10, "Name"), 4096000, PacketIO.UTF8) : null;
   }

   @Nullable
   public static String getMaskData(MemorySegment mem) {
      return getMaskData(mem, 0);
   }

   @Nullable
   public static String getMaskData(MemorySegment mem, int offset) {
      return hasMaskData(mem, offset)
         ? PacketIO.readVarString("MaskData", mem, offset + getValidatedOffset(mem, offset, 6, 10, "MaskData"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static boolean hasName(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasMaskData(MemorySegment mem, int offset) {
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

   public static BuilderToolGMaskPreset toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static BuilderToolGMaskPreset toObject(MemorySegment mem, int offset) {
      if (offset + 10 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("BuilderToolGMaskPreset", offset + 10, (int)mem.byteSize());
      } else {
         return new BuilderToolGMaskPreset(
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1)),
            hasName(mem, offset) ? PacketIO.readVarString("Name", mem, offset + getValidatedOffset(mem, offset, 2, 10, "Name"), 4096000, PacketIO.UTF8) : null,
            hasMaskData(mem, offset)
               ? PacketIO.readVarString("MaskData", mem, offset + getValidatedOffset(mem, offset, 6, 10, "MaskData"), 4096000, PacketIO.UTF8)
               : null
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.name != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.maskData != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.isSave ? 1 : 0);
      int nameOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int maskDataOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.name != null) {
         buf.setIntLE(nameOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.name, 4096000);
      } else {
         buf.setIntLE(nameOffsetSlot, -1);
      }

      if (this.maskData != null) {
         buf.setIntLE(maskDataOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.maskData, 4096000);
      } else {
         buf.setIntLE(maskDataOffsetSlot, -1);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.name != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.maskData != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BOOL, offset + 1, this.isSave);
      int varOffset = offset + 10;
      if (this.name != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 2), varOffset - offset - 10);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.name, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 2), -1);
      }

      if (this.maskData != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 6), varOffset - offset - 10);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.maskData, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 6), -1);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 10;
      if (this.name != null) {
         size += PacketIO.stringSize(this.name);
      }

      if (this.maskData != null) {
         size += PacketIO.stringSize(this.maskData);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 10) {
         return ValidationResult.error("Buffer too small: expected at least 10 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int nameOffset = buffer.getIntLE(offset + 2);
            if (nameOffset < 0 || nameOffset > buffer.writerIndex() - offset - 10) {
               return ValidationResult.error("Invalid offset for Name");
            }

            int pos = offset + 10 + nameOffset;
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

         if ((nullBits & 2) != 0) {
            int maskDataOffset = buffer.getIntLE(offset + 6);
            if (maskDataOffset < 0 || maskDataOffset > buffer.writerIndex() - offset - 10) {
               return ValidationResult.error("Invalid offset for MaskData");
            }

            int posx = offset + 10 + maskDataOffset;
            int maskDataLen = VarInt.peek(buffer, posx);
            if (maskDataLen < 0) {
               return ValidationResult.error("Invalid string length for MaskData");
            }

            if (maskDataLen > 4096000) {
               return ValidationResult.error("MaskData exceeds max length 4096000");
            }

            posx += VarInt.size(maskDataLen);
            posx += maskDataLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading MaskData");
            }
         }

         return ValidationResult.OK;
      }
   }

   public BuilderToolGMaskPreset clone() {
      BuilderToolGMaskPreset copy = new BuilderToolGMaskPreset();
      copy.isSave = this.isSave;
      copy.name = this.name;
      copy.maskData = this.maskData;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BuilderToolGMaskPreset other)
            ? false
            : this.isSave == other.isSave && Objects.equals(this.name, other.name) && Objects.equals(this.maskData, other.maskData);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.isSave, this.name, this.maskData);
   }
}
