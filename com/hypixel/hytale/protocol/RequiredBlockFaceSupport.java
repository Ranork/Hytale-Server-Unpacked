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

public class RequiredBlockFaceSupport {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 17;
   public static final int VARIABLE_FIELD_COUNT = 4;
   public static final int VARIABLE_BLOCK_START = 33;
   public static final int MAX_SIZE = 98304053;
   @Nullable
   public String faceType;
   @Nullable
   public String selfFaceType;
   @Nullable
   public String blockSetId;
   public int blockTypeId;
   public int tagIndex;
   public int fluidId;
   @Nonnull
   public SupportMatch support = SupportMatch.Ignored;
   @Nonnull
   public SupportMatch matchSelf = SupportMatch.Ignored;
   public boolean allowSupportPropagation;
   public boolean rotate;
   @Nullable
   public Vector3i[] filler;

   public RequiredBlockFaceSupport() {
   }

   public RequiredBlockFaceSupport(
      @Nullable String faceType,
      @Nullable String selfFaceType,
      @Nullable String blockSetId,
      int blockTypeId,
      int tagIndex,
      int fluidId,
      @Nonnull SupportMatch support,
      @Nonnull SupportMatch matchSelf,
      boolean allowSupportPropagation,
      boolean rotate,
      @Nullable Vector3i[] filler
   ) {
      this.faceType = faceType;
      this.selfFaceType = selfFaceType;
      this.blockSetId = blockSetId;
      this.blockTypeId = blockTypeId;
      this.tagIndex = tagIndex;
      this.fluidId = fluidId;
      this.support = support;
      this.matchSelf = matchSelf;
      this.allowSupportPropagation = allowSupportPropagation;
      this.rotate = rotate;
      this.filler = filler;
   }

   public RequiredBlockFaceSupport(@Nonnull RequiredBlockFaceSupport other) {
      this.faceType = other.faceType;
      this.selfFaceType = other.selfFaceType;
      this.blockSetId = other.blockSetId;
      this.blockTypeId = other.blockTypeId;
      this.tagIndex = other.tagIndex;
      this.fluidId = other.fluidId;
      this.support = other.support;
      this.matchSelf = other.matchSelf;
      this.allowSupportPropagation = other.allowSupportPropagation;
      this.rotate = other.rotate;
      this.filler = other.filler;
   }

   @Nonnull
   public static RequiredBlockFaceSupport deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 33) {
         throw ProtocolException.bufferTooSmall("RequiredBlockFaceSupport", 33, buf.readableBytes() - offset);
      } else {
         RequiredBlockFaceSupport obj = new RequiredBlockFaceSupport();
         byte nullBits = buf.getByte(offset);
         obj.blockTypeId = buf.getIntLE(offset + 1);
         obj.tagIndex = buf.getIntLE(offset + 5);
         obj.fluidId = buf.getIntLE(offset + 9);
         obj.support = SupportMatch.fromValue(buf.getByte(offset + 13));
         obj.matchSelf = SupportMatch.fromValue(buf.getByte(offset + 14));
         obj.allowSupportPropagation = buf.getByte(offset + 15) != 0;
         obj.rotate = buf.getByte(offset + 16) != 0;
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 17);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 33) {
               throw ProtocolException.invalidOffset("FaceType", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 33 + varPosBase0;
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
            int varPosBase1 = buf.getIntLE(offset + 21);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 33) {
               throw ProtocolException.invalidOffset("SelfFaceType", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 33 + varPosBase1;
            int selfFaceTypeLen = VarInt.peek(buf, varPos1);
            if (selfFaceTypeLen < 0) {
               throw ProtocolException.invalidVarInt("SelfFaceType");
            }

            int selfFaceTypeVarIntLen = VarInt.size(selfFaceTypeLen);
            if (selfFaceTypeLen > 4096000) {
               throw ProtocolException.stringTooLong("SelfFaceType", selfFaceTypeLen, 4096000);
            }

            if (varPos1 + selfFaceTypeVarIntLen + selfFaceTypeLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("SelfFaceType", varPos1 + selfFaceTypeVarIntLen + selfFaceTypeLen, buf.readableBytes());
            }

            obj.selfFaceType = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
         }

         if ((nullBits & 4) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 25);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 33) {
               throw ProtocolException.invalidOffset("BlockSetId", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 33 + varPosBase2;
            int blockSetIdLen = VarInt.peek(buf, varPos2);
            if (blockSetIdLen < 0) {
               throw ProtocolException.invalidVarInt("BlockSetId");
            }

            int blockSetIdVarIntLen = VarInt.size(blockSetIdLen);
            if (blockSetIdLen > 4096000) {
               throw ProtocolException.stringTooLong("BlockSetId", blockSetIdLen, 4096000);
            }

            if (varPos2 + blockSetIdVarIntLen + blockSetIdLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("BlockSetId", varPos2 + blockSetIdVarIntLen + blockSetIdLen, buf.readableBytes());
            }

            obj.blockSetId = PacketIO.readVarString(buf, varPos2, PacketIO.UTF8);
         }

         if ((nullBits & 8) != 0) {
            int varPosBase3 = buf.getIntLE(offset + 29);
            if (varPosBase3 < 0 || varPosBase3 > buf.writerIndex() - offset - 33) {
               throw ProtocolException.invalidOffset("Filler", varPosBase3, buf.readableBytes());
            }

            int varPos3 = offset + 33 + varPosBase3;
            int fillerCount = VarInt.peek(buf, varPos3);
            if (fillerCount < 0) {
               throw ProtocolException.invalidVarInt("Filler");
            }

            int varIntLen = VarInt.size(fillerCount);
            if (fillerCount > 4096000) {
               throw ProtocolException.arrayTooLong("Filler", fillerCount, 4096000);
            }

            if (varPos3 + varIntLen + fillerCount * 12L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Filler", varPos3 + varIntLen + fillerCount * 12, buf.readableBytes());
            }

            obj.filler = new Vector3i[fillerCount];
            int elemPos = varPos3 + varIntLen;

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
      int maxEnd = 33;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 17);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 33) {
            throw ProtocolException.invalidOffset("FaceType", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 33 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 21);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 33) {
            throw ProtocolException.invalidOffset("SelfFaceType", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 33 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(sl) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 25);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 33) {
            throw ProtocolException.invalidOffset("BlockSetId", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 33 + fieldOffset2;
         int sl = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(sl) + sl;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 29);
         if (fieldOffset3 < 0 || fieldOffset3 > buf.writerIndex() - offset - 33) {
            throw ProtocolException.invalidOffset("Filler", fieldOffset3, maxEnd);
         }

         int pos3 = offset + 33 + fieldOffset3;
         int arrLen = VarInt.peek(buf, pos3);
         pos3 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos3 += Vector3i.computeBytesConsumed(buf, pos3);
         }

         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 33L;
   }

   @Nullable
   public static String getFaceType(MemorySegment mem) {
      return getFaceType(mem, 0);
   }

   @Nullable
   public static String getFaceType(MemorySegment mem, int offset) {
      return hasFaceType(mem, offset)
         ? PacketIO.readVarString("FaceType", mem, offset + getValidatedOffset(mem, offset, 17, 33, "FaceType"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static String getSelfFaceType(MemorySegment mem) {
      return getSelfFaceType(mem, 0);
   }

   @Nullable
   public static String getSelfFaceType(MemorySegment mem, int offset) {
      return hasSelfFaceType(mem, offset)
         ? PacketIO.readVarString("SelfFaceType", mem, offset + getValidatedOffset(mem, offset, 21, 33, "SelfFaceType"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static String getBlockSetId(MemorySegment mem) {
      return getBlockSetId(mem, 0);
   }

   @Nullable
   public static String getBlockSetId(MemorySegment mem, int offset) {
      return hasBlockSetId(mem, offset)
         ? PacketIO.readVarString("BlockSetId", mem, offset + getValidatedOffset(mem, offset, 25, 33, "BlockSetId"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static int getBlockTypeId(MemorySegment mem) {
      return getBlockTypeId(mem, 0);
   }

   public static int getBlockTypeId(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   public static int getTagIndex(MemorySegment mem) {
      return getTagIndex(mem, 0);
   }

   public static int getTagIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 5));
   }

   public static int getFluidId(MemorySegment mem) {
      return getFluidId(mem, 0);
   }

   public static int getFluidId(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 9));
   }

   public static SupportMatch getSupport(MemorySegment mem) {
      return getSupport(mem, 0);
   }

   public static SupportMatch getSupport(MemorySegment mem, int offset) {
      return SupportMatch.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 13)));
   }

   public static SupportMatch getMatchSelf(MemorySegment mem) {
      return getMatchSelf(mem, 0);
   }

   public static SupportMatch getMatchSelf(MemorySegment mem, int offset) {
      return SupportMatch.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 14)));
   }

   public static boolean getAllowSupportPropagation(MemorySegment mem) {
      return getAllowSupportPropagation(mem, 0);
   }

   public static boolean getAllowSupportPropagation(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 15));
   }

   public static boolean getRotate(MemorySegment mem) {
      return getRotate(mem, 0);
   }

   public static boolean getRotate(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 16));
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
         int off = offset + getValidatedOffset(mem, offset, 29, 33, "Filler");
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

   public static boolean hasSelfFaceType(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasBlockSetId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasFiller(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static RequiredBlockFaceSupport toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static RequiredBlockFaceSupport toObject(MemorySegment mem, int offset) {
      if (offset + 33 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("RequiredBlockFaceSupport", offset + 33, (int)mem.byteSize());
      } else {
         Vector3i[] filler = null;
         if (hasFiller(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 29, 33, "Filler");
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

         return new RequiredBlockFaceSupport(
            hasFaceType(mem, offset)
               ? PacketIO.readVarString("FaceType", mem, offset + getValidatedOffset(mem, offset, 17, 33, "FaceType"), 4096000, PacketIO.UTF8)
               : null,
            hasSelfFaceType(mem, offset)
               ? PacketIO.readVarString("SelfFaceType", mem, offset + getValidatedOffset(mem, offset, 21, 33, "SelfFaceType"), 4096000, PacketIO.UTF8)
               : null,
            hasBlockSetId(mem, offset)
               ? PacketIO.readVarString("BlockSetId", mem, offset + getValidatedOffset(mem, offset, 25, 33, "BlockSetId"), 4096000, PacketIO.UTF8)
               : null,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 5)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 9)),
            SupportMatch.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 13))),
            SupportMatch.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 14))),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 15)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 16)),
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

      if (this.selfFaceType != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.blockSetId != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.filler != null) {
         nullBits = (byte)(nullBits | 8);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.blockTypeId);
      buf.writeIntLE(this.tagIndex);
      buf.writeIntLE(this.fluidId);
      buf.writeByte(this.support.getValue());
      buf.writeByte(this.matchSelf.getValue());
      buf.writeByte(this.allowSupportPropagation ? 1 : 0);
      buf.writeByte(this.rotate ? 1 : 0);
      int faceTypeOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int selfFaceTypeOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int blockSetIdOffsetSlot = buf.writerIndex();
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

      if (this.selfFaceType != null) {
         buf.setIntLE(selfFaceTypeOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.selfFaceType, 4096000);
      } else {
         buf.setIntLE(selfFaceTypeOffsetSlot, -1);
      }

      if (this.blockSetId != null) {
         buf.setIntLE(blockSetIdOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.blockSetId, 4096000);
      } else {
         buf.setIntLE(blockSetIdOffsetSlot, -1);
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

      if (this.selfFaceType != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.blockSetId != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.filler != null) {
         nullBits = (byte)(nullBits | 8);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.blockTypeId);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 5), this.tagIndex);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 9), this.fluidId);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 13), (byte)this.support.getValue());
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 14), (byte)this.matchSelf.getValue());
      mem.set(PacketIO.PROTO_BOOL, offset + 15, this.allowSupportPropagation);
      mem.set(PacketIO.PROTO_BOOL, offset + 16, this.rotate);
      int varOffset = offset + 33;
      if (this.faceType != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 17), varOffset - offset - 33);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.faceType, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 17), -1);
      }

      if (this.selfFaceType != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 21), varOffset - offset - 33);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.selfFaceType, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 21), -1);
      }

      if (this.blockSetId != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 25), varOffset - offset - 33);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.blockSetId, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 25), -1);
      }

      if (this.filler != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 29), varOffset - offset - 33);
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
         mem.set(PacketIO.PROTO_INT, (long)(offset + 29), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 33;
      if (this.faceType != null) {
         size += PacketIO.stringSize(this.faceType);
      }

      if (this.selfFaceType != null) {
         size += PacketIO.stringSize(this.selfFaceType);
      }

      if (this.blockSetId != null) {
         size += PacketIO.stringSize(this.blockSetId);
      }

      if (this.filler != null) {
         size += VarInt.size(this.filler.length) + this.filler.length * 12;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 33) {
         return ValidationResult.error("Buffer too small: expected at least 33 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int v = buffer.getByte(offset + 13) & 255;
         if (v >= 3) {
            return ValidationResult.error("Invalid SupportMatch value for Support");
         } else {
            v = buffer.getByte(offset + 14) & 255;
            if (v >= 3) {
               return ValidationResult.error("Invalid SupportMatch value for MatchSelf");
            } else {
               if ((nullBits & 1) != 0) {
                  v = buffer.getIntLE(offset + 17);
                  if (v < 0 || v > buffer.writerIndex() - offset - 33) {
                     return ValidationResult.error("Invalid offset for FaceType");
                  }

                  int pos = offset + 33 + v;
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
                  v = buffer.getIntLE(offset + 21);
                  if (v < 0 || v > buffer.writerIndex() - offset - 33) {
                     return ValidationResult.error("Invalid offset for SelfFaceType");
                  }

                  int posx = offset + 33 + v;
                  int selfFaceTypeLen = VarInt.peek(buffer, posx);
                  if (selfFaceTypeLen < 0) {
                     return ValidationResult.error("Invalid string length for SelfFaceType");
                  }

                  if (selfFaceTypeLen > 4096000) {
                     return ValidationResult.error("SelfFaceType exceeds max length 4096000");
                  }

                  posx += VarInt.size(selfFaceTypeLen);
                  posx += selfFaceTypeLen;
                  if (posx > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading SelfFaceType");
                  }
               }

               if ((nullBits & 4) != 0) {
                  v = buffer.getIntLE(offset + 25);
                  if (v < 0 || v > buffer.writerIndex() - offset - 33) {
                     return ValidationResult.error("Invalid offset for BlockSetId");
                  }

                  int posxx = offset + 33 + v;
                  int blockSetIdLen = VarInt.peek(buffer, posxx);
                  if (blockSetIdLen < 0) {
                     return ValidationResult.error("Invalid string length for BlockSetId");
                  }

                  if (blockSetIdLen > 4096000) {
                     return ValidationResult.error("BlockSetId exceeds max length 4096000");
                  }

                  posxx += VarInt.size(blockSetIdLen);
                  posxx += blockSetIdLen;
                  if (posxx > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading BlockSetId");
                  }
               }

               if ((nullBits & 8) != 0) {
                  v = buffer.getIntLE(offset + 29);
                  if (v < 0 || v > buffer.writerIndex() - offset - 33) {
                     return ValidationResult.error("Invalid offset for Filler");
                  }

                  int posxxx = offset + 33 + v;
                  int fillerCount = VarInt.peek(buffer, posxxx);
                  if (fillerCount < 0) {
                     return ValidationResult.error("Invalid array count for Filler");
                  }

                  if (fillerCount > 4096000) {
                     return ValidationResult.error("Filler exceeds max length 4096000");
                  }

                  posxxx += VarInt.size(fillerCount);
                  posxxx += fillerCount * 12;
                  if (posxxx > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading Filler");
                  }
               }

               return ValidationResult.OK;
            }
         }
      }
   }

   public RequiredBlockFaceSupport clone() {
      RequiredBlockFaceSupport copy = new RequiredBlockFaceSupport();
      copy.faceType = this.faceType;
      copy.selfFaceType = this.selfFaceType;
      copy.blockSetId = this.blockSetId;
      copy.blockTypeId = this.blockTypeId;
      copy.tagIndex = this.tagIndex;
      copy.fluidId = this.fluidId;
      copy.support = this.support;
      copy.matchSelf = this.matchSelf;
      copy.allowSupportPropagation = this.allowSupportPropagation;
      copy.rotate = this.rotate;
      copy.filler = this.filler != null ? Arrays.stream(this.filler).map(e -> e.clone()).toArray(Vector3i[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof RequiredBlockFaceSupport other)
            ? false
            : Objects.equals(this.faceType, other.faceType)
               && Objects.equals(this.selfFaceType, other.selfFaceType)
               && Objects.equals(this.blockSetId, other.blockSetId)
               && this.blockTypeId == other.blockTypeId
               && this.tagIndex == other.tagIndex
               && this.fluidId == other.fluidId
               && Objects.equals(this.support, other.support)
               && Objects.equals(this.matchSelf, other.matchSelf)
               && this.allowSupportPropagation == other.allowSupportPropagation
               && this.rotate == other.rotate
               && Arrays.equals((Object[])this.filler, (Object[])other.filler);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.faceType);
      result = 31 * result + Objects.hashCode(this.selfFaceType);
      result = 31 * result + Objects.hashCode(this.blockSetId);
      result = 31 * result + Integer.hashCode(this.blockTypeId);
      result = 31 * result + Integer.hashCode(this.tagIndex);
      result = 31 * result + Integer.hashCode(this.fluidId);
      result = 31 * result + Objects.hashCode(this.support);
      result = 31 * result + Objects.hashCode(this.matchSelf);
      result = 31 * result + Boolean.hashCode(this.allowSupportPropagation);
      result = 31 * result + Boolean.hashCode(this.rotate);
      return 31 * result + Arrays.hashCode((Object[])this.filler);
   }
}
