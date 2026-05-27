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

public class BlockFaceSupport {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 65536019;
   @Nullable
   public String faceType;
   @Nullable
   public Vector3i[] filler;

   public BlockFaceSupport() {
   }

   public BlockFaceSupport(@Nullable String faceType, @Nullable Vector3i[] filler) {
      this.faceType = faceType;
      this.filler = filler;
   }

   public BlockFaceSupport(@Nonnull BlockFaceSupport other) {
      this.faceType = other.faceType;
      this.filler = other.filler;
   }

   @Nonnull
   public static BlockFaceSupport deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 9) {
         throw ProtocolException.bufferTooSmall("BlockFaceSupport", 9, buf.readableBytes() - offset);
      } else {
         BlockFaceSupport obj = new BlockFaceSupport();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 1);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 9) {
               throw ProtocolException.invalidOffset("FaceType", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 9 + varPosBase0;
            int faceTypeLen = VarInt.peek(buf, varPos0);
            if (faceTypeLen < 0) {
               throw ProtocolException.invalidVarInt("FaceType");
            }

            int faceTypeVarIntLen = VarInt.size(faceTypeLen);
            if (faceTypeLen > 4096000) {
               throw ProtocolException.stringTooLong("FaceType", faceTypeLen, 4096000);
            }

            if (varPos0 + faceTypeVarIntLen + faceTypeLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("FaceType", varPos0 + faceTypeVarIntLen + faceTypeLen, buf.readableBytes());
            }

            obj.faceType = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 5);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 9) {
               throw ProtocolException.invalidOffset("Filler", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 9 + varPosBase1;
            int fillerCount = VarInt.peek(buf, varPos1);
            if (fillerCount < 0) {
               throw ProtocolException.invalidVarInt("Filler");
            }

            int varIntLen = VarInt.size(fillerCount);
            if (fillerCount > 4096000) {
               throw ProtocolException.arrayTooLong("Filler", fillerCount, 4096000);
            }

            if (varPos1 + varIntLen + fillerCount * 12L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Filler", varPos1 + varIntLen + fillerCount * 12, buf.readableBytes());
            }

            obj.filler = new Vector3i[fillerCount];
            int elemPos = varPos1 + varIntLen;

            for (int i = 0; i < fillerCount; i++) {
               obj.filler[i] = Vector3i.deserialize(buf, elemPos);
               elemPos += Vector3i.computeBytesConsumed(buf, elemPos);
            }
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
            throw ProtocolException.invalidOffset("FaceType", fieldOffset0, maxEnd);
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
            throw ProtocolException.invalidOffset("Filler", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 9 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos1 += Vector3i.computeBytesConsumed(buf, pos1);
         }

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
   public static String getFaceType(MemorySegment mem) {
      return getFaceType(mem, 0);
   }

   @Nullable
   public static String getFaceType(MemorySegment mem, int offset) {
      return hasFaceType(mem, offset)
         ? PacketIO.readVarString("FaceType", mem, offset + getValidatedOffset(mem, offset, 1, 9, "FaceType"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static Vector3i[] getFiller(MemorySegment mem) {
      return getFiller(mem, 0);
   }

   @Nullable
   public static Vector3i[] getFiller(MemorySegment mem, int offset) {
      if (!hasFiller(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 5, 9, "Filler");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Filler", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Filler", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 12L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Filler", off + lenOffset + len * 12, (int)mem.byteSize());
            } else {
               off += lenOffset;
               Vector3i[] data = new Vector3i[len];

               for (int i = 0; i < len; i++) {
                  data[i] = Vector3i.toObject(mem, off + i * 12);
               }

               return data;
            }
         }
      }
   }

   public static boolean hasFaceType(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasFiller(MemorySegment mem, int offset) {
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

   public static BlockFaceSupport toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static BlockFaceSupport toObject(MemorySegment mem, int offset) {
      if (offset + 9 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("BlockFaceSupport", offset + 9, (int)mem.byteSize());
      } else {
         Vector3i[] filler = null;
         if (hasFiller(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 5, 9, "Filler");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Filler", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Filler", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 12L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Filler", off + lenOffset + len * 12, (int)mem.byteSize());
            }

            off += lenOffset;
            filler = new Vector3i[len];

            for (int i = 0; i < len; i++) {
               filler[i] = Vector3i.toObject(mem, off + i * 12);
            }
         }

         return new BlockFaceSupport(
            hasFaceType(mem, offset)
               ? PacketIO.readVarString("FaceType", mem, offset + getValidatedOffset(mem, offset, 1, 9, "FaceType"), 4096000, PacketIO.UTF8)
               : null,
            filler
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.faceType != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.filler != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      int faceTypeOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int fillerOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.faceType != null) {
         buf.setIntLE(faceTypeOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.faceType, 4096000);
      } else {
         buf.setIntLE(faceTypeOffsetSlot, -1);
      }

      if (this.filler != null) {
         buf.setIntLE(fillerOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.filler.length > 4096000) {
            throw ProtocolException.arrayTooLong("Filler", this.filler.length, 4096000);
         }

         VarInt.write(buf, this.filler.length);

         for (Vector3i item : this.filler) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(fillerOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.faceType != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.filler != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 9;
      if (this.faceType != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), varOffset - offset - 9);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.faceType, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), -1);
      }

      if (this.filler != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), varOffset - offset - 9);
         if (this.filler.length > 4096000) {
            throw ProtocolException.arrayTooLong("Filler", this.filler.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.filler.length);
         int fillerValueOffset = 0;

         for (int i = 0; i < this.filler.length; i++) {
            fillerValueOffset += this.filler[i].serialize(mem, varOffset + fillerValueOffset);
         }

         varOffset += fillerValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 9;
      if (this.faceType != null) {
         size += PacketIO.stringSize(this.faceType);
      }

      if (this.filler != null) {
         size += VarInt.size(this.filler.length) + this.filler.length * 12;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 9) {
         return ValidationResult.error("Buffer too small: expected at least 9 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int faceTypeOffset = buffer.getIntLE(offset + 1);
            if (faceTypeOffset < 0 || faceTypeOffset > buffer.writerIndex() - offset - 9) {
               return ValidationResult.error("Invalid offset for FaceType");
            }

            int pos = offset + 9 + faceTypeOffset;
            int faceTypeLen = VarInt.peek(buffer, pos);
            if (faceTypeLen < 0) {
               return ValidationResult.error("Invalid string length for FaceType");
            }

            if (faceTypeLen > 4096000) {
               return ValidationResult.error("FaceType exceeds max length 4096000");
            }

            pos += VarInt.size(faceTypeLen);
            pos += faceTypeLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading FaceType");
            }
         }

         if ((nullBits & 2) != 0) {
            int fillerOffset = buffer.getIntLE(offset + 5);
            if (fillerOffset < 0 || fillerOffset > buffer.writerIndex() - offset - 9) {
               return ValidationResult.error("Invalid offset for Filler");
            }

            int posx = offset + 9 + fillerOffset;
            int fillerCount = VarInt.peek(buffer, posx);
            if (fillerCount < 0) {
               return ValidationResult.error("Invalid array count for Filler");
            }

            if (fillerCount > 4096000) {
               return ValidationResult.error("Filler exceeds max length 4096000");
            }

            posx += VarInt.size(fillerCount);
            posx += fillerCount * 12;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Filler");
            }
         }

         return ValidationResult.OK;
      }
   }

   public BlockFaceSupport clone() {
      BlockFaceSupport copy = new BlockFaceSupport();
      copy.faceType = this.faceType;
      copy.filler = this.filler != null ? Arrays.stream(this.filler).map(e -> e.clone()).toArray(Vector3i[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BlockFaceSupport other)
            ? false
            : Objects.equals(this.faceType, other.faceType) && Arrays.equals((Object[])this.filler, (Object[])other.filler);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.faceType);
      return 31 * result + Arrays.hashCode((Object[])this.filler);
   }
}
