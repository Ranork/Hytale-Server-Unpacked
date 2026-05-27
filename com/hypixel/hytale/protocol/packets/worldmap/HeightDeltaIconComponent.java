package com.hypixel.hytale.protocol.packets.worldmap;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HeightDeltaIconComponent extends MapMarkerComponent {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 9;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 17;
   public static final int MAX_SIZE = 32768027;
   public int upDelta;
   @Nullable
   public String upImage;
   public int downDelta;
   @Nullable
   public String downImage;

   public HeightDeltaIconComponent() {
   }

   public HeightDeltaIconComponent(int upDelta, @Nullable String upImage, int downDelta, @Nullable String downImage) {
      this.upDelta = upDelta;
      this.upImage = upImage;
      this.downDelta = downDelta;
      this.downImage = downImage;
   }

   public HeightDeltaIconComponent(@Nonnull HeightDeltaIconComponent other) {
      this.upDelta = other.upDelta;
      this.upImage = other.upImage;
      this.downDelta = other.downDelta;
      this.downImage = other.downImage;
   }

   @Nonnull
   public static HeightDeltaIconComponent deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 17) {
         throw ProtocolException.bufferTooSmall("HeightDeltaIconComponent", 17, buf.readableBytes() - offset);
      } else {
         HeightDeltaIconComponent obj = new HeightDeltaIconComponent();
         byte nullBits = buf.getByte(offset);
         obj.upDelta = buf.getIntLE(offset + 1);
         obj.downDelta = buf.getIntLE(offset + 5);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 9);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 17) {
               throw ProtocolException.invalidOffset("UpImage", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 17 + varPosBase0;
            int upImageLen = VarInt.peek(buf, varPos0);
            if (upImageLen < 0) {
               throw ProtocolException.invalidVarInt("UpImage");
            }

            int upImageVarIntLen = VarInt.size(upImageLen);
            if (upImageLen > 4096000) {
               throw ProtocolException.stringTooLong("UpImage", upImageLen, 4096000);
            }

            if (varPos0 + upImageVarIntLen + upImageLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("UpImage", varPos0 + upImageVarIntLen + upImageLen, buf.readableBytes());
            }

            obj.upImage = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 13);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 17) {
               throw ProtocolException.invalidOffset("DownImage", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 17 + varPosBase1;
            int downImageLen = VarInt.peek(buf, varPos1);
            if (downImageLen < 0) {
               throw ProtocolException.invalidVarInt("DownImage");
            }

            int downImageVarIntLen = VarInt.size(downImageLen);
            if (downImageLen > 4096000) {
               throw ProtocolException.stringTooLong("DownImage", downImageLen, 4096000);
            }

            if (varPos1 + downImageVarIntLen + downImageLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("DownImage", varPos1 + downImageVarIntLen + downImageLen, buf.readableBytes());
            }

            obj.downImage = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 17;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 9);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 17) {
            throw ProtocolException.invalidOffset("UpImage", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 17 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 13);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 17) {
            throw ProtocolException.invalidOffset("DownImage", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 17 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(sl) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 17L;
   }

   public static int getUpDelta(MemorySegment mem) {
      return getUpDelta(mem, 0);
   }

   public static int getUpDelta(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   @Nullable
   public static String getUpImage(MemorySegment mem) {
      return getUpImage(mem, 0);
   }

   @Nullable
   public static String getUpImage(MemorySegment mem, int offset) {
      return hasUpImage(mem, offset)
         ? PacketIO.readVarString("UpImage", mem, offset + getValidatedOffset(mem, offset, 9, 17, "UpImage"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static int getDownDelta(MemorySegment mem) {
      return getDownDelta(mem, 0);
   }

   public static int getDownDelta(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 5));
   }

   @Nullable
   public static String getDownImage(MemorySegment mem) {
      return getDownImage(mem, 0);
   }

   @Nullable
   public static String getDownImage(MemorySegment mem, int offset) {
      return hasDownImage(mem, offset)
         ? PacketIO.readVarString("DownImage", mem, offset + getValidatedOffset(mem, offset, 13, 17, "DownImage"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static boolean hasUpImage(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasDownImage(MemorySegment mem, int offset) {
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

   public static HeightDeltaIconComponent toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static HeightDeltaIconComponent toObject(MemorySegment mem, int offset) {
      if (offset + 17 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("HeightDeltaIconComponent", offset + 17, (int)mem.byteSize());
      } else {
         return new HeightDeltaIconComponent(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            hasUpImage(mem, offset)
               ? PacketIO.readVarString("UpImage", mem, offset + getValidatedOffset(mem, offset, 9, 17, "UpImage"), 4096000, PacketIO.UTF8)
               : null,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 5)),
            hasDownImage(mem, offset)
               ? PacketIO.readVarString("DownImage", mem, offset + getValidatedOffset(mem, offset, 13, 17, "DownImage"), 4096000, PacketIO.UTF8)
               : null
         );
      }
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.upImage != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.downImage != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.upDelta);
      buf.writeIntLE(this.downDelta);
      int upImageOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int downImageOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.upImage != null) {
         buf.setIntLE(upImageOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.upImage, 4096000);
      } else {
         buf.setIntLE(upImageOffsetSlot, -1);
      }

      if (this.downImage != null) {
         buf.setIntLE(downImageOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.downImage, 4096000);
      } else {
         buf.setIntLE(downImageOffsetSlot, -1);
      }

      return buf.writerIndex() - startPos;
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.upImage != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.downImage != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.upDelta);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 5), this.downDelta);
      int varOffset = offset + 17;
      if (this.upImage != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), varOffset - offset - 17);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.upImage, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), -1);
      }

      if (this.downImage != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 13), varOffset - offset - 17);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.downImage, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 13), -1);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 17;
      if (this.upImage != null) {
         size += PacketIO.stringSize(this.upImage);
      }

      if (this.downImage != null) {
         size += PacketIO.stringSize(this.downImage);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 17) {
         return ValidationResult.error("Buffer too small: expected at least 17 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int upImageOffset = buffer.getIntLE(offset + 9);
            if (upImageOffset < 0 || upImageOffset > buffer.writerIndex() - offset - 17) {
               return ValidationResult.error("Invalid offset for UpImage");
            }

            int pos = offset + 17 + upImageOffset;
            int upImageLen = VarInt.peek(buffer, pos);
            if (upImageLen < 0) {
               return ValidationResult.error("Invalid string length for UpImage");
            }

            if (upImageLen > 4096000) {
               return ValidationResult.error("UpImage exceeds max length 4096000");
            }

            pos += VarInt.size(upImageLen);
            pos += upImageLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading UpImage");
            }
         }

         if ((nullBits & 2) != 0) {
            int downImageOffset = buffer.getIntLE(offset + 13);
            if (downImageOffset < 0 || downImageOffset > buffer.writerIndex() - offset - 17) {
               return ValidationResult.error("Invalid offset for DownImage");
            }

            int posx = offset + 17 + downImageOffset;
            int downImageLen = VarInt.peek(buffer, posx);
            if (downImageLen < 0) {
               return ValidationResult.error("Invalid string length for DownImage");
            }

            if (downImageLen > 4096000) {
               return ValidationResult.error("DownImage exceeds max length 4096000");
            }

            posx += VarInt.size(downImageLen);
            posx += downImageLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading DownImage");
            }
         }

         return ValidationResult.OK;
      }
   }

   public HeightDeltaIconComponent clone() {
      HeightDeltaIconComponent copy = new HeightDeltaIconComponent();
      copy.upDelta = this.upDelta;
      copy.upImage = this.upImage;
      copy.downDelta = this.downDelta;
      copy.downImage = this.downImage;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof HeightDeltaIconComponent other)
            ? false
            : this.upDelta == other.upDelta
               && Objects.equals(this.upImage, other.upImage)
               && this.downDelta == other.downDelta
               && Objects.equals(this.downImage, other.downImage);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.upDelta, this.upImage, this.downDelta, this.downImage);
   }
}
