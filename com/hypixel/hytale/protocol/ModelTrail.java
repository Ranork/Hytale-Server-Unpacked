package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3fc;

public class ModelTrail {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 27;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 35;
   public static final int MAX_SIZE = 32768045;
   @Nullable
   public String trailId;
   @Nonnull
   public EntityPart targetEntityPart = EntityPart.Self;
   @Nullable
   public String targetNodeName;
   @Nullable
   public Vector3fc positionOffset;
   @Nullable
   public Direction rotationOffset;
   public boolean fixedRotation;

   public ModelTrail() {
   }

   public ModelTrail(
      @Nullable String trailId,
      @Nonnull EntityPart targetEntityPart,
      @Nullable String targetNodeName,
      @Nullable Vector3fc positionOffset,
      @Nullable Direction rotationOffset,
      boolean fixedRotation
   ) {
      this.trailId = trailId;
      this.targetEntityPart = targetEntityPart;
      this.targetNodeName = targetNodeName;
      this.positionOffset = positionOffset;
      this.rotationOffset = rotationOffset;
      this.fixedRotation = fixedRotation;
   }

   public ModelTrail(@Nonnull ModelTrail other) {
      this.trailId = other.trailId;
      this.targetEntityPart = other.targetEntityPart;
      this.targetNodeName = other.targetNodeName;
      this.positionOffset = other.positionOffset;
      this.rotationOffset = other.rotationOffset;
      this.fixedRotation = other.fixedRotation;
   }

   @Nonnull
   public static ModelTrail deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 35) {
         throw ProtocolException.bufferTooSmall("ModelTrail", 35, buf.readableBytes() - offset);
      } else {
         ModelTrail obj = new ModelTrail();
         byte nullBits = buf.getByte(offset);
         obj.targetEntityPart = EntityPart.fromValue(buf.getByte(offset + 1));
         if ((nullBits & 1) != 0) {
            obj.positionOffset = PacketIO.readVector3f(buf, offset + 2);
         }

         if ((nullBits & 2) != 0) {
            obj.rotationOffset = Direction.deserialize(buf, offset + 14);
         }

         obj.fixedRotation = buf.getByte(offset + 26) != 0;
         if ((nullBits & 4) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 27);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 35) {
               throw ProtocolException.invalidOffset("TrailId", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 35 + varPosBase0;
            int trailIdLen = VarInt.peek(buf, varPos0);
            if (trailIdLen < 0) {
               throw ProtocolException.invalidVarInt("TrailId");
            }

            int trailIdVarIntLen = VarInt.size(trailIdLen);
            if (trailIdLen > 4096000) {
               throw ProtocolException.stringTooLong("TrailId", trailIdLen, 4096000);
            }

            if (varPos0 + trailIdVarIntLen + trailIdLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("TrailId", varPos0 + trailIdVarIntLen + trailIdLen, buf.readableBytes());
            }

            obj.trailId = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 8) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 31);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 35) {
               throw ProtocolException.invalidOffset("TargetNodeName", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 35 + varPosBase1;
            int targetNodeNameLen = VarInt.peek(buf, varPos1);
            if (targetNodeNameLen < 0) {
               throw ProtocolException.invalidVarInt("TargetNodeName");
            }

            int targetNodeNameVarIntLen = VarInt.size(targetNodeNameLen);
            if (targetNodeNameLen > 4096000) {
               throw ProtocolException.stringTooLong("TargetNodeName", targetNodeNameLen, 4096000);
            }

            if (varPos1 + targetNodeNameVarIntLen + targetNodeNameLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("TargetNodeName", varPos1 + targetNodeNameVarIntLen + targetNodeNameLen, buf.readableBytes());
            }

            obj.targetNodeName = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 35;
      if ((nullBits & 4) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 27);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 35) {
            throw ProtocolException.invalidOffset("TrailId", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 35 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 31);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 35) {
            throw ProtocolException.invalidOffset("TargetNodeName", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 35 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(sl) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 35L;
   }

   @Nullable
   public static String getTrailId(MemorySegment mem) {
      return getTrailId(mem, 0);
   }

   @Nullable
   public static String getTrailId(MemorySegment mem, int offset) {
      return hasTrailId(mem, offset)
         ? PacketIO.readVarString("TrailId", mem, offset + getValidatedOffset(mem, offset, 27, 35, "TrailId"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static EntityPart getTargetEntityPart(MemorySegment mem) {
      return getTargetEntityPart(mem, 0);
   }

   public static EntityPart getTargetEntityPart(MemorySegment mem, int offset) {
      return EntityPart.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1)));
   }

   @Nullable
   public static String getTargetNodeName(MemorySegment mem) {
      return getTargetNodeName(mem, 0);
   }

   @Nullable
   public static String getTargetNodeName(MemorySegment mem, int offset) {
      return hasTargetNodeName(mem, offset)
         ? PacketIO.readVarString("TargetNodeName", mem, offset + getValidatedOffset(mem, offset, 31, 35, "TargetNodeName"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static Vector3fc getPositionOffset(MemorySegment mem) {
      return getPositionOffset(mem, 0);
   }

   @Nullable
   public static Vector3fc getPositionOffset(MemorySegment mem, int offset) {
      return hasPositionOffset(mem, offset) ? PacketIO.readVector3f(mem, offset + 2) : null;
   }

   @Nullable
   public static Direction getRotationOffset(MemorySegment mem) {
      return getRotationOffset(mem, 0);
   }

   @Nullable
   public static Direction getRotationOffset(MemorySegment mem, int offset) {
      return hasRotationOffset(mem, offset) ? Direction.toObject(mem, offset + 14) : null;
   }

   public static boolean getFixedRotation(MemorySegment mem) {
      return getFixedRotation(mem, 0);
   }

   public static boolean getFixedRotation(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 26));
   }

   public static boolean hasPositionOffset(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasRotationOffset(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasTrailId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasTargetNodeName(MemorySegment mem, int offset) {
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

   public static ModelTrail toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ModelTrail toObject(MemorySegment mem, int offset) {
      if (offset + 35 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ModelTrail", offset + 35, (int)mem.byteSize());
      } else {
         return new ModelTrail(
            hasTrailId(mem, offset)
               ? PacketIO.readVarString("TrailId", mem, offset + getValidatedOffset(mem, offset, 27, 35, "TrailId"), 4096000, PacketIO.UTF8)
               : null,
            EntityPart.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))),
            hasTargetNodeName(mem, offset)
               ? PacketIO.readVarString("TargetNodeName", mem, offset + getValidatedOffset(mem, offset, 31, 35, "TargetNodeName"), 4096000, PacketIO.UTF8)
               : null,
            hasPositionOffset(mem, offset) ? PacketIO.readVector3f(mem, offset + 2) : null,
            hasRotationOffset(mem, offset) ? Direction.toObject(mem, offset + 14) : null,
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 26))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.positionOffset != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.rotationOffset != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.trailId != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.targetNodeName != null) {
         nullBits = (byte)(nullBits | 8);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.targetEntityPart.getValue());
      if (this.positionOffset != null) {
         PacketIO.writeVector3f(buf, this.positionOffset);
      } else {
         buf.writeZero(12);
      }

      if (this.rotationOffset != null) {
         this.rotationOffset.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      buf.writeByte(this.fixedRotation ? 1 : 0);
      int trailIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int targetNodeNameOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.trailId != null) {
         buf.setIntLE(trailIdOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.trailId, 4096000);
      } else {
         buf.setIntLE(trailIdOffsetSlot, -1);
      }

      if (this.targetNodeName != null) {
         buf.setIntLE(targetNodeNameOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.targetNodeName, 4096000);
      } else {
         buf.setIntLE(targetNodeNameOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.positionOffset != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.rotationOffset != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.trailId != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.targetNodeName != null) {
         nullBits = (byte)(nullBits | 8);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.targetEntityPart.getValue());
      if (this.positionOffset != null) {
         PacketIO.writeVector3f(mem, offset + 2, this.positionOffset);
      } else {
         mem.asSlice(offset + 2, 12L).fill((byte)0);
      }

      if (this.rotationOffset != null) {
         this.rotationOffset.serialize(mem, offset + 14);
      } else {
         mem.asSlice(offset + 14, 12L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_BOOL, offset + 26, this.fixedRotation);
      int varOffset = offset + 35;
      if (this.trailId != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 27), varOffset - offset - 35);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.trailId, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 27), -1);
      }

      if (this.targetNodeName != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 31), varOffset - offset - 35);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.targetNodeName, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 31), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 35;
      if (this.trailId != null) {
         size += PacketIO.stringSize(this.trailId);
      }

      if (this.targetNodeName != null) {
         size += PacketIO.stringSize(this.targetNodeName);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 35) {
         return ValidationResult.error("Buffer too small: expected at least 35 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int v = buffer.getByte(offset + 1) & 255;
         if (v >= 4) {
            return ValidationResult.error("Invalid EntityPart value for TargetEntityPart");
         } else {
            if ((nullBits & 4) != 0) {
               v = buffer.getIntLE(offset + 27);
               if (v < 0 || v > buffer.writerIndex() - offset - 35) {
                  return ValidationResult.error("Invalid offset for TrailId");
               }

               int pos = offset + 35 + v;
               int trailIdLen = VarInt.peek(buffer, pos);
               if (trailIdLen < 0) {
                  return ValidationResult.error("Invalid string length for TrailId");
               }

               if (trailIdLen > 4096000) {
                  return ValidationResult.error("TrailId exceeds max length 4096000");
               }

               pos += VarInt.size(trailIdLen);
               pos += trailIdLen;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading TrailId");
               }
            }

            if ((nullBits & 8) != 0) {
               v = buffer.getIntLE(offset + 31);
               if (v < 0 || v > buffer.writerIndex() - offset - 35) {
                  return ValidationResult.error("Invalid offset for TargetNodeName");
               }

               int posx = offset + 35 + v;
               int targetNodeNameLen = VarInt.peek(buffer, posx);
               if (targetNodeNameLen < 0) {
                  return ValidationResult.error("Invalid string length for TargetNodeName");
               }

               if (targetNodeNameLen > 4096000) {
                  return ValidationResult.error("TargetNodeName exceeds max length 4096000");
               }

               posx += VarInt.size(targetNodeNameLen);
               posx += targetNodeNameLen;
               if (posx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading TargetNodeName");
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public ModelTrail clone() {
      ModelTrail copy = new ModelTrail();
      copy.trailId = this.trailId;
      copy.targetEntityPart = this.targetEntityPart;
      copy.targetNodeName = this.targetNodeName;
      copy.positionOffset = this.positionOffset;
      copy.rotationOffset = this.rotationOffset != null ? this.rotationOffset.clone() : null;
      copy.fixedRotation = this.fixedRotation;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ModelTrail other)
            ? false
            : Objects.equals(this.trailId, other.trailId)
               && Objects.equals(this.targetEntityPart, other.targetEntityPart)
               && Objects.equals(this.targetNodeName, other.targetNodeName)
               && Objects.equals(this.positionOffset, other.positionOffset)
               && Objects.equals(this.rotationOffset, other.rotationOffset)
               && this.fixedRotation == other.fixedRotation;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.trailId, this.targetEntityPart, this.targetNodeName, this.positionOffset, this.rotationOffset, this.fixedRotation);
   }
}
