package com.hypixel.hytale.protocol.packets.buildertools;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderToolArg {
   public static final int NULLABLE_BIT_FIELD_SIZE = 2;
   public static final int FIXED_BLOCK_SIZE = 33;
   public static final int VARIABLE_FIELD_COUNT = 5;
   public static final int VARIABLE_BLOCK_START = 53;
   public static final int MAX_SIZE = 1677721600;
   public boolean required;
   @Nullable
   public String id;
   @Nonnull
   public BuilderToolArgType argType = BuilderToolArgType.Bool;
   @Nullable
   public BuilderToolBoolArg boolArg;
   @Nullable
   public BuilderToolFloatArg floatArg;
   @Nullable
   public BuilderToolIntArg intArg;
   @Nullable
   public BuilderToolStringArg stringArg;
   @Nullable
   public BuilderToolBlockArg blockArg;
   @Nullable
   public BuilderToolMaskArg maskArg;
   @Nullable
   public BuilderToolBrushShapeArg brushShapeArg;
   @Nullable
   public BuilderToolBrushOriginArg brushOriginArg;
   @Nullable
   public BuilderToolBrushAxisArg brushAxisArg;
   @Nullable
   public BuilderToolRotationArg rotationArg;
   @Nullable
   public BuilderToolOptionArg optionArg;

   public BuilderToolArg() {
   }

   public BuilderToolArg(
      boolean required,
      @Nullable String id,
      @Nonnull BuilderToolArgType argType,
      @Nullable BuilderToolBoolArg boolArg,
      @Nullable BuilderToolFloatArg floatArg,
      @Nullable BuilderToolIntArg intArg,
      @Nullable BuilderToolStringArg stringArg,
      @Nullable BuilderToolBlockArg blockArg,
      @Nullable BuilderToolMaskArg maskArg,
      @Nullable BuilderToolBrushShapeArg brushShapeArg,
      @Nullable BuilderToolBrushOriginArg brushOriginArg,
      @Nullable BuilderToolBrushAxisArg brushAxisArg,
      @Nullable BuilderToolRotationArg rotationArg,
      @Nullable BuilderToolOptionArg optionArg
   ) {
      this.required = required;
      this.id = id;
      this.argType = argType;
      this.boolArg = boolArg;
      this.floatArg = floatArg;
      this.intArg = intArg;
      this.stringArg = stringArg;
      this.blockArg = blockArg;
      this.maskArg = maskArg;
      this.brushShapeArg = brushShapeArg;
      this.brushOriginArg = brushOriginArg;
      this.brushAxisArg = brushAxisArg;
      this.rotationArg = rotationArg;
      this.optionArg = optionArg;
   }

   public BuilderToolArg(@Nonnull BuilderToolArg other) {
      this.required = other.required;
      this.id = other.id;
      this.argType = other.argType;
      this.boolArg = other.boolArg;
      this.floatArg = other.floatArg;
      this.intArg = other.intArg;
      this.stringArg = other.stringArg;
      this.blockArg = other.blockArg;
      this.maskArg = other.maskArg;
      this.brushShapeArg = other.brushShapeArg;
      this.brushOriginArg = other.brushOriginArg;
      this.brushAxisArg = other.brushAxisArg;
      this.rotationArg = other.rotationArg;
      this.optionArg = other.optionArg;
   }

   @Nonnull
   public static BuilderToolArg deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 53) {
         throw ProtocolException.bufferTooSmall("BuilderToolArg", 53, buf.readableBytes() - offset);
      } else {
         BuilderToolArg obj = new BuilderToolArg();
         byte[] nullBits = PacketIO.readBytes(buf, offset, 2);
         obj.required = buf.getByte(offset + 2) != 0;
         obj.argType = BuilderToolArgType.fromValue(buf.getByte(offset + 3));
         if ((nullBits[0] & 1) != 0) {
            obj.boolArg = BuilderToolBoolArg.deserialize(buf, offset + 4);
         }

         if ((nullBits[0] & 2) != 0) {
            obj.floatArg = BuilderToolFloatArg.deserialize(buf, offset + 5);
         }

         if ((nullBits[0] & 4) != 0) {
            obj.intArg = BuilderToolIntArg.deserialize(buf, offset + 17);
         }

         if ((nullBits[0] & 8) != 0) {
            obj.brushShapeArg = BuilderToolBrushShapeArg.deserialize(buf, offset + 29);
         }

         if ((nullBits[0] & 16) != 0) {
            obj.brushOriginArg = BuilderToolBrushOriginArg.deserialize(buf, offset + 30);
         }

         if ((nullBits[0] & 32) != 0) {
            obj.brushAxisArg = BuilderToolBrushAxisArg.deserialize(buf, offset + 31);
         }

         if ((nullBits[0] & 64) != 0) {
            obj.rotationArg = BuilderToolRotationArg.deserialize(buf, offset + 32);
         }

         if ((nullBits[0] & 128) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 33);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 53) {
               throw ProtocolException.invalidOffset("Id", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 53 + varPosBase0;
            int idLen = VarInt.peek(buf, varPos0);
            if (idLen < 0) {
               throw ProtocolException.invalidVarInt("Id");
            }

            int idVarIntLen = VarInt.size(idLen);
            if (idLen > 4096000) {
               throw ProtocolException.stringTooLong("Id", idLen, 4096000);
            }

            if (varPos0 + idVarIntLen + idLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Id", varPos0 + idVarIntLen + idLen, buf.readableBytes());
            }

            obj.id = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits[1] & 1) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 37);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 53) {
               throw ProtocolException.invalidOffset("StringArg", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 53 + varPosBase1;
            obj.stringArg = BuilderToolStringArg.deserialize(buf, varPos1);
         }

         if ((nullBits[1] & 2) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 41);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 53) {
               throw ProtocolException.invalidOffset("BlockArg", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 53 + varPosBase2;
            obj.blockArg = BuilderToolBlockArg.deserialize(buf, varPos2);
         }

         if ((nullBits[1] & 4) != 0) {
            int varPosBase3 = buf.getIntLE(offset + 45);
            if (varPosBase3 < 0 || varPosBase3 > buf.writerIndex() - offset - 53) {
               throw ProtocolException.invalidOffset("MaskArg", varPosBase3, buf.readableBytes());
            }

            int varPos3 = offset + 53 + varPosBase3;
            obj.maskArg = BuilderToolMaskArg.deserialize(buf, varPos3);
         }

         if ((nullBits[1] & 8) != 0) {
            int varPosBase4 = buf.getIntLE(offset + 49);
            if (varPosBase4 < 0 || varPosBase4 > buf.writerIndex() - offset - 53) {
               throw ProtocolException.invalidOffset("OptionArg", varPosBase4, buf.readableBytes());
            }

            int varPos4 = offset + 53 + varPosBase4;
            obj.optionArg = BuilderToolOptionArg.deserialize(buf, varPos4);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte[] nullBits = PacketIO.readBytes(buf, offset, 2);
      int maxEnd = 53;
      if ((nullBits[0] & 128) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 33);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 53) {
            throw ProtocolException.invalidOffset("Id", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 53 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits[1] & 1) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 37);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 53) {
            throw ProtocolException.invalidOffset("StringArg", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 53 + fieldOffset1;
         pos1 += BuilderToolStringArg.computeBytesConsumed(buf, pos1);
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits[1] & 2) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 41);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 53) {
            throw ProtocolException.invalidOffset("BlockArg", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 53 + fieldOffset2;
         pos2 += BuilderToolBlockArg.computeBytesConsumed(buf, pos2);
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits[1] & 4) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 45);
         if (fieldOffset3 < 0 || fieldOffset3 > buf.writerIndex() - offset - 53) {
            throw ProtocolException.invalidOffset("MaskArg", fieldOffset3, maxEnd);
         }

         int pos3 = offset + 53 + fieldOffset3;
         pos3 += BuilderToolMaskArg.computeBytesConsumed(buf, pos3);
         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      if ((nullBits[1] & 8) != 0) {
         int fieldOffset4 = buf.getIntLE(offset + 49);
         if (fieldOffset4 < 0 || fieldOffset4 > buf.writerIndex() - offset - 53) {
            throw ProtocolException.invalidOffset("OptionArg", fieldOffset4, maxEnd);
         }

         int pos4 = offset + 53 + fieldOffset4;
         pos4 += BuilderToolOptionArg.computeBytesConsumed(buf, pos4);
         if (pos4 - offset > maxEnd) {
            maxEnd = pos4 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 53L;
   }

   public static boolean getRequired(MemorySegment mem) {
      return getRequired(mem, 0);
   }

   public static boolean getRequired(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 2));
   }

   @Nullable
   public static String getId(MemorySegment mem) {
      return getId(mem, 0);
   }

   @Nullable
   public static String getId(MemorySegment mem, int offset) {
      return hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 33, 53, "Id"), 4096000, PacketIO.UTF8) : null;
   }

   public static BuilderToolArgType getArgType(MemorySegment mem) {
      return getArgType(mem, 0);
   }

   public static BuilderToolArgType getArgType(MemorySegment mem, int offset) {
      return BuilderToolArgType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 3)));
   }

   @Nullable
   public static BuilderToolBoolArg getBoolArg(MemorySegment mem) {
      return getBoolArg(mem, 0);
   }

   @Nullable
   public static BuilderToolBoolArg getBoolArg(MemorySegment mem, int offset) {
      return hasBoolArg(mem, offset) ? BuilderToolBoolArg.toObject(mem, offset + 4) : null;
   }

   @Nullable
   public static BuilderToolFloatArg getFloatArg(MemorySegment mem) {
      return getFloatArg(mem, 0);
   }

   @Nullable
   public static BuilderToolFloatArg getFloatArg(MemorySegment mem, int offset) {
      return hasFloatArg(mem, offset) ? BuilderToolFloatArg.toObject(mem, offset + 5) : null;
   }

   @Nullable
   public static BuilderToolIntArg getIntArg(MemorySegment mem) {
      return getIntArg(mem, 0);
   }

   @Nullable
   public static BuilderToolIntArg getIntArg(MemorySegment mem, int offset) {
      return hasIntArg(mem, offset) ? BuilderToolIntArg.toObject(mem, offset + 17) : null;
   }

   @Nullable
   public static BuilderToolStringArg getStringArg(MemorySegment mem) {
      return getStringArg(mem, 0);
   }

   @Nullable
   public static BuilderToolStringArg getStringArg(MemorySegment mem, int offset) {
      return hasStringArg(mem, offset) ? BuilderToolStringArg.toObject(mem, offset + getValidatedOffset(mem, offset, 37, 53, "StringArg")) : null;
   }

   @Nullable
   public static BuilderToolBlockArg getBlockArg(MemorySegment mem) {
      return getBlockArg(mem, 0);
   }

   @Nullable
   public static BuilderToolBlockArg getBlockArg(MemorySegment mem, int offset) {
      return hasBlockArg(mem, offset) ? BuilderToolBlockArg.toObject(mem, offset + getValidatedOffset(mem, offset, 41, 53, "BlockArg")) : null;
   }

   @Nullable
   public static BuilderToolMaskArg getMaskArg(MemorySegment mem) {
      return getMaskArg(mem, 0);
   }

   @Nullable
   public static BuilderToolMaskArg getMaskArg(MemorySegment mem, int offset) {
      return hasMaskArg(mem, offset) ? BuilderToolMaskArg.toObject(mem, offset + getValidatedOffset(mem, offset, 45, 53, "MaskArg")) : null;
   }

   @Nullable
   public static BuilderToolBrushShapeArg getBrushShapeArg(MemorySegment mem) {
      return getBrushShapeArg(mem, 0);
   }

   @Nullable
   public static BuilderToolBrushShapeArg getBrushShapeArg(MemorySegment mem, int offset) {
      return hasBrushShapeArg(mem, offset) ? BuilderToolBrushShapeArg.toObject(mem, offset + 29) : null;
   }

   @Nullable
   public static BuilderToolBrushOriginArg getBrushOriginArg(MemorySegment mem) {
      return getBrushOriginArg(mem, 0);
   }

   @Nullable
   public static BuilderToolBrushOriginArg getBrushOriginArg(MemorySegment mem, int offset) {
      return hasBrushOriginArg(mem, offset) ? BuilderToolBrushOriginArg.toObject(mem, offset + 30) : null;
   }

   @Nullable
   public static BuilderToolBrushAxisArg getBrushAxisArg(MemorySegment mem) {
      return getBrushAxisArg(mem, 0);
   }

   @Nullable
   public static BuilderToolBrushAxisArg getBrushAxisArg(MemorySegment mem, int offset) {
      return hasBrushAxisArg(mem, offset) ? BuilderToolBrushAxisArg.toObject(mem, offset + 31) : null;
   }

   @Nullable
   public static BuilderToolRotationArg getRotationArg(MemorySegment mem) {
      return getRotationArg(mem, 0);
   }

   @Nullable
   public static BuilderToolRotationArg getRotationArg(MemorySegment mem, int offset) {
      return hasRotationArg(mem, offset) ? BuilderToolRotationArg.toObject(mem, offset + 32) : null;
   }

   @Nullable
   public static BuilderToolOptionArg getOptionArg(MemorySegment mem) {
      return getOptionArg(mem, 0);
   }

   @Nullable
   public static BuilderToolOptionArg getOptionArg(MemorySegment mem, int offset) {
      return hasOptionArg(mem, offset) ? BuilderToolOptionArg.toObject(mem, offset + getValidatedOffset(mem, offset, 49, 53, "OptionArg")) : null;
   }

   public static boolean hasBoolArg(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasFloatArg(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasIntArg(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasBrushShapeArg(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   public static boolean hasBrushOriginArg(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 16) != 0;
   }

   public static boolean hasBrushAxisArg(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 32) != 0;
   }

   public static boolean hasRotationArg(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 64) != 0;
   }

   public static boolean hasId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 128) != 0;
   }

   public static boolean hasStringArg(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 1) != 0;
   }

   public static boolean hasBlockArg(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 2) != 0;
   }

   public static boolean hasMaskArg(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 4) != 0;
   }

   public static boolean hasOptionArg(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
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

   public static BuilderToolArg toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static BuilderToolArg toObject(MemorySegment mem, int offset) {
      if (offset + 53 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("BuilderToolArg", offset + 53, (int)mem.byteSize());
      } else {
         return new BuilderToolArg(
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 2)),
            hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 33, 53, "Id"), 4096000, PacketIO.UTF8) : null,
            BuilderToolArgType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 3))),
            hasBoolArg(mem, offset) ? BuilderToolBoolArg.toObject(mem, offset + 4) : null,
            hasFloatArg(mem, offset) ? BuilderToolFloatArg.toObject(mem, offset + 5) : null,
            hasIntArg(mem, offset) ? BuilderToolIntArg.toObject(mem, offset + 17) : null,
            hasStringArg(mem, offset) ? BuilderToolStringArg.toObject(mem, offset + getValidatedOffset(mem, offset, 37, 53, "StringArg")) : null,
            hasBlockArg(mem, offset) ? BuilderToolBlockArg.toObject(mem, offset + getValidatedOffset(mem, offset, 41, 53, "BlockArg")) : null,
            hasMaskArg(mem, offset) ? BuilderToolMaskArg.toObject(mem, offset + getValidatedOffset(mem, offset, 45, 53, "MaskArg")) : null,
            hasBrushShapeArg(mem, offset) ? BuilderToolBrushShapeArg.toObject(mem, offset + 29) : null,
            hasBrushOriginArg(mem, offset) ? BuilderToolBrushOriginArg.toObject(mem, offset + 30) : null,
            hasBrushAxisArg(mem, offset) ? BuilderToolBrushAxisArg.toObject(mem, offset + 31) : null,
            hasRotationArg(mem, offset) ? BuilderToolRotationArg.toObject(mem, offset + 32) : null,
            hasOptionArg(mem, offset) ? BuilderToolOptionArg.toObject(mem, offset + getValidatedOffset(mem, offset, 49, 53, "OptionArg")) : null
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte[] nullBits = new byte[2];
      if (this.boolArg != null) {
         nullBits[0] = (byte)(nullBits[0] | 1);
      }

      if (this.floatArg != null) {
         nullBits[0] = (byte)(nullBits[0] | 2);
      }

      if (this.intArg != null) {
         nullBits[0] = (byte)(nullBits[0] | 4);
      }

      if (this.brushShapeArg != null) {
         nullBits[0] = (byte)(nullBits[0] | 8);
      }

      if (this.brushOriginArg != null) {
         nullBits[0] = (byte)(nullBits[0] | 16);
      }

      if (this.brushAxisArg != null) {
         nullBits[0] = (byte)(nullBits[0] | 32);
      }

      if (this.rotationArg != null) {
         nullBits[0] = (byte)(nullBits[0] | 64);
      }

      if (this.id != null) {
         nullBits[0] = (byte)(nullBits[0] | 128);
      }

      if (this.stringArg != null) {
         nullBits[1] = (byte)(nullBits[1] | 1);
      }

      if (this.blockArg != null) {
         nullBits[1] = (byte)(nullBits[1] | 2);
      }

      if (this.maskArg != null) {
         nullBits[1] = (byte)(nullBits[1] | 4);
      }

      if (this.optionArg != null) {
         nullBits[1] = (byte)(nullBits[1] | 8);
      }

      buf.writeBytes(nullBits);
      buf.writeByte(this.required ? 1 : 0);
      buf.writeByte(this.argType.getValue());
      if (this.boolArg != null) {
         this.boolArg.serialize(buf);
      } else {
         buf.writeZero(1);
      }

      if (this.floatArg != null) {
         this.floatArg.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      if (this.intArg != null) {
         this.intArg.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      if (this.brushShapeArg != null) {
         this.brushShapeArg.serialize(buf);
      } else {
         buf.writeZero(1);
      }

      if (this.brushOriginArg != null) {
         this.brushOriginArg.serialize(buf);
      } else {
         buf.writeZero(1);
      }

      if (this.brushAxisArg != null) {
         this.brushAxisArg.serialize(buf);
      } else {
         buf.writeZero(1);
      }

      if (this.rotationArg != null) {
         this.rotationArg.serialize(buf);
      } else {
         buf.writeZero(1);
      }

      int idOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int stringArgOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int blockArgOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int maskArgOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int optionArgOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.id != null) {
         buf.setIntLE(idOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.id, 4096000);
      } else {
         buf.setIntLE(idOffsetSlot, -1);
      }

      if (this.stringArg != null) {
         buf.setIntLE(stringArgOffsetSlot, buf.writerIndex() - varBlockStart);
         this.stringArg.serialize(buf);
      } else {
         buf.setIntLE(stringArgOffsetSlot, -1);
      }

      if (this.blockArg != null) {
         buf.setIntLE(blockArgOffsetSlot, buf.writerIndex() - varBlockStart);
         this.blockArg.serialize(buf);
      } else {
         buf.setIntLE(blockArgOffsetSlot, -1);
      }

      if (this.maskArg != null) {
         buf.setIntLE(maskArgOffsetSlot, buf.writerIndex() - varBlockStart);
         this.maskArg.serialize(buf);
      } else {
         buf.setIntLE(maskArgOffsetSlot, -1);
      }

      if (this.optionArg != null) {
         buf.setIntLE(optionArgOffsetSlot, buf.writerIndex() - varBlockStart);
         this.optionArg.serialize(buf);
      } else {
         buf.setIntLE(optionArgOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.boolArg != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.floatArg != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.intArg != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.brushShapeArg != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.brushOriginArg != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.brushAxisArg != null) {
         nullBits = (byte)(nullBits | 32);
      }

      if (this.rotationArg != null) {
         nullBits = (byte)(nullBits | 64);
      }

      if (this.id != null) {
         nullBits = (byte)(nullBits | 128);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      nullBits = 0;
      if (this.stringArg != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.blockArg != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.maskArg != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.optionArg != null) {
         nullBits = (byte)(nullBits | 8);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), nullBits);
      mem.set(PacketIO.PROTO_BOOL, offset + 2, this.required);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 3), (byte)this.argType.getValue());
      if (this.boolArg != null) {
         this.boolArg.serialize(mem, offset + 4);
      } else {
         mem.asSlice(offset + 4, 1L).fill((byte)0);
      }

      if (this.floatArg != null) {
         this.floatArg.serialize(mem, offset + 5);
      } else {
         mem.asSlice(offset + 5, 12L).fill((byte)0);
      }

      if (this.intArg != null) {
         this.intArg.serialize(mem, offset + 17);
      } else {
         mem.asSlice(offset + 17, 12L).fill((byte)0);
      }

      if (this.brushShapeArg != null) {
         this.brushShapeArg.serialize(mem, offset + 29);
      } else {
         mem.asSlice(offset + 29, 1L).fill((byte)0);
      }

      if (this.brushOriginArg != null) {
         this.brushOriginArg.serialize(mem, offset + 30);
      } else {
         mem.asSlice(offset + 30, 1L).fill((byte)0);
      }

      if (this.brushAxisArg != null) {
         this.brushAxisArg.serialize(mem, offset + 31);
      } else {
         mem.asSlice(offset + 31, 1L).fill((byte)0);
      }

      if (this.rotationArg != null) {
         this.rotationArg.serialize(mem, offset + 32);
      } else {
         mem.asSlice(offset + 32, 1L).fill((byte)0);
      }

      int varOffset = offset + 53;
      if (this.id != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 33), varOffset - offset - 53);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.id, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 33), -1);
      }

      if (this.stringArg != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 37), varOffset - offset - 53);
         varOffset += this.stringArg.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 37), -1);
      }

      if (this.blockArg != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 41), varOffset - offset - 53);
         varOffset += this.blockArg.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 41), -1);
      }

      if (this.maskArg != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 45), varOffset - offset - 53);
         varOffset += this.maskArg.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 45), -1);
      }

      if (this.optionArg != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 49), varOffset - offset - 53);
         varOffset += this.optionArg.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 49), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 53;
      if (this.id != null) {
         size += PacketIO.stringSize(this.id);
      }

      if (this.stringArg != null) {
         size += this.stringArg.computeSize();
      }

      if (this.blockArg != null) {
         size += this.blockArg.computeSize();
      }

      if (this.maskArg != null) {
         size += this.maskArg.computeSize();
      }

      if (this.optionArg != null) {
         size += this.optionArg.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 53) {
         return ValidationResult.error("Buffer too small: expected at least 53 bytes");
      } else {
         byte[] nullBits = PacketIO.readBytes(buffer, offset, 2);
         int v = buffer.getByte(offset + 3) & 255;
         if (v >= 11) {
            return ValidationResult.error("Invalid BuilderToolArgType value for ArgType");
         } else {
            if ((nullBits[0] & 128) != 0) {
               v = buffer.getIntLE(offset + 33);
               if (v < 0 || v > buffer.writerIndex() - offset - 53) {
                  return ValidationResult.error("Invalid offset for Id");
               }

               int pos = offset + 53 + v;
               int idLen = VarInt.peek(buffer, pos);
               if (idLen < 0) {
                  return ValidationResult.error("Invalid string length for Id");
               }

               if (idLen > 4096000) {
                  return ValidationResult.error("Id exceeds max length 4096000");
               }

               pos += VarInt.size(idLen);
               pos += idLen;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading Id");
               }
            }

            if ((nullBits[1] & 1) != 0) {
               v = buffer.getIntLE(offset + 37);
               if (v < 0 || v > buffer.writerIndex() - offset - 53) {
                  return ValidationResult.error("Invalid offset for StringArg");
               }

               int posx = offset + 53 + v;
               ValidationResult stringArgResult = BuilderToolStringArg.validateStructure(buffer, posx);
               if (!stringArgResult.isValid()) {
                  return ValidationResult.error("Invalid StringArg: " + stringArgResult.error());
               }

               posx += BuilderToolStringArg.computeBytesConsumed(buffer, posx);
            }

            if ((nullBits[1] & 2) != 0) {
               v = buffer.getIntLE(offset + 41);
               if (v < 0 || v > buffer.writerIndex() - offset - 53) {
                  return ValidationResult.error("Invalid offset for BlockArg");
               }

               int posx = offset + 53 + v;
               ValidationResult blockArgResult = BuilderToolBlockArg.validateStructure(buffer, posx);
               if (!blockArgResult.isValid()) {
                  return ValidationResult.error("Invalid BlockArg: " + blockArgResult.error());
               }

               posx += BuilderToolBlockArg.computeBytesConsumed(buffer, posx);
            }

            if ((nullBits[1] & 4) != 0) {
               v = buffer.getIntLE(offset + 45);
               if (v < 0 || v > buffer.writerIndex() - offset - 53) {
                  return ValidationResult.error("Invalid offset for MaskArg");
               }

               int posx = offset + 53 + v;
               ValidationResult maskArgResult = BuilderToolMaskArg.validateStructure(buffer, posx);
               if (!maskArgResult.isValid()) {
                  return ValidationResult.error("Invalid MaskArg: " + maskArgResult.error());
               }

               posx += BuilderToolMaskArg.computeBytesConsumed(buffer, posx);
            }

            if ((nullBits[1] & 8) != 0) {
               v = buffer.getIntLE(offset + 49);
               if (v < 0 || v > buffer.writerIndex() - offset - 53) {
                  return ValidationResult.error("Invalid offset for OptionArg");
               }

               int posx = offset + 53 + v;
               ValidationResult optionArgResult = BuilderToolOptionArg.validateStructure(buffer, posx);
               if (!optionArgResult.isValid()) {
                  return ValidationResult.error("Invalid OptionArg: " + optionArgResult.error());
               }

               posx += BuilderToolOptionArg.computeBytesConsumed(buffer, posx);
            }

            return ValidationResult.OK;
         }
      }
   }

   public BuilderToolArg clone() {
      BuilderToolArg copy = new BuilderToolArg();
      copy.required = this.required;
      copy.id = this.id;
      copy.argType = this.argType;
      copy.boolArg = this.boolArg != null ? this.boolArg.clone() : null;
      copy.floatArg = this.floatArg != null ? this.floatArg.clone() : null;
      copy.intArg = this.intArg != null ? this.intArg.clone() : null;
      copy.stringArg = this.stringArg != null ? this.stringArg.clone() : null;
      copy.blockArg = this.blockArg != null ? this.blockArg.clone() : null;
      copy.maskArg = this.maskArg != null ? this.maskArg.clone() : null;
      copy.brushShapeArg = this.brushShapeArg != null ? this.brushShapeArg.clone() : null;
      copy.brushOriginArg = this.brushOriginArg != null ? this.brushOriginArg.clone() : null;
      copy.brushAxisArg = this.brushAxisArg != null ? this.brushAxisArg.clone() : null;
      copy.rotationArg = this.rotationArg != null ? this.rotationArg.clone() : null;
      copy.optionArg = this.optionArg != null ? this.optionArg.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BuilderToolArg other)
            ? false
            : this.required == other.required
               && Objects.equals(this.id, other.id)
               && Objects.equals(this.argType, other.argType)
               && Objects.equals(this.boolArg, other.boolArg)
               && Objects.equals(this.floatArg, other.floatArg)
               && Objects.equals(this.intArg, other.intArg)
               && Objects.equals(this.stringArg, other.stringArg)
               && Objects.equals(this.blockArg, other.blockArg)
               && Objects.equals(this.maskArg, other.maskArg)
               && Objects.equals(this.brushShapeArg, other.brushShapeArg)
               && Objects.equals(this.brushOriginArg, other.brushOriginArg)
               && Objects.equals(this.brushAxisArg, other.brushAxisArg)
               && Objects.equals(this.rotationArg, other.rotationArg)
               && Objects.equals(this.optionArg, other.optionArg);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(
         this.required,
         this.id,
         this.argType,
         this.boolArg,
         this.floatArg,
         this.intArg,
         this.stringArg,
         this.blockArg,
         this.maskArg,
         this.brushShapeArg,
         this.brushOriginArg,
         this.brushAxisArg,
         this.rotationArg,
         this.optionArg
      );
   }
}
