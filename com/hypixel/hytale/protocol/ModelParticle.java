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

public class ModelParticle {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 34;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 42;
   public static final int MAX_SIZE = 32768052;
   @Nullable
   public String systemId;
   public float scale;
   @Nullable
   public Color color;
   @Nonnull
   public EntityPart targetEntityPart = EntityPart.Self;
   @Nullable
   public String targetNodeName;
   @Nullable
   public Vector3fc positionOffset;
   @Nullable
   public Direction rotationOffset;
   public boolean detachedFromModel;

   public ModelParticle() {
   }

   public ModelParticle(
      @Nullable String systemId,
      float scale,
      @Nullable Color color,
      @Nonnull EntityPart targetEntityPart,
      @Nullable String targetNodeName,
      @Nullable Vector3fc positionOffset,
      @Nullable Direction rotationOffset,
      boolean detachedFromModel
   ) {
      this.systemId = systemId;
      this.scale = scale;
      this.color = color;
      this.targetEntityPart = targetEntityPart;
      this.targetNodeName = targetNodeName;
      this.positionOffset = positionOffset;
      this.rotationOffset = rotationOffset;
      this.detachedFromModel = detachedFromModel;
   }

   public ModelParticle(@Nonnull ModelParticle other) {
      this.systemId = other.systemId;
      this.scale = other.scale;
      this.color = other.color;
      this.targetEntityPart = other.targetEntityPart;
      this.targetNodeName = other.targetNodeName;
      this.positionOffset = other.positionOffset;
      this.rotationOffset = other.rotationOffset;
      this.detachedFromModel = other.detachedFromModel;
   }

   @Nonnull
   public static ModelParticle deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 42) {
         throw ProtocolException.bufferTooSmall("ModelParticle", 42, buf.readableBytes() - offset);
      } else {
         ModelParticle obj = new ModelParticle();
         byte nullBits = buf.getByte(offset);
         obj.scale = buf.getFloatLE(offset + 1);
         if ((nullBits & 1) != 0) {
            obj.color = Color.deserialize(buf, offset + 5);
         }

         obj.targetEntityPart = EntityPart.fromValue(buf.getByte(offset + 8));
         if ((nullBits & 2) != 0) {
            obj.positionOffset = PacketIO.readVector3f(buf, offset + 9);
         }

         if ((nullBits & 4) != 0) {
            obj.rotationOffset = Direction.deserialize(buf, offset + 21);
         }

         obj.detachedFromModel = buf.getByte(offset + 33) != 0;
         if ((nullBits & 8) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 34);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 42) {
               throw ProtocolException.invalidOffset("SystemId", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 42 + varPosBase0;
            int systemIdLen = VarInt.peek(buf, varPos0);
            if (systemIdLen < 0) {
               throw ProtocolException.invalidVarInt("SystemId");
            }

            int systemIdVarIntLen = VarInt.size(systemIdLen);
            if (systemIdLen > 4096000) {
               throw ProtocolException.stringTooLong("SystemId", systemIdLen, 4096000);
            }

            if (varPos0 + systemIdVarIntLen + systemIdLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("SystemId", varPos0 + systemIdVarIntLen + systemIdLen, buf.readableBytes());
            }

            obj.systemId = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 16) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 38);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 42) {
               throw ProtocolException.invalidOffset("TargetNodeName", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 42 + varPosBase1;
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
      int maxEnd = 42;
      if ((nullBits & 8) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 34);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 42) {
            throw ProtocolException.invalidOffset("SystemId", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 42 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 16) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 38);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 42) {
            throw ProtocolException.invalidOffset("TargetNodeName", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 42 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(sl) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 42L;
   }

   @Nullable
   public static String getSystemId(MemorySegment mem) {
      return getSystemId(mem, 0);
   }

   @Nullable
   public static String getSystemId(MemorySegment mem, int offset) {
      return hasSystemId(mem, offset)
         ? PacketIO.readVarString("SystemId", mem, offset + getValidatedOffset(mem, offset, 34, 42, "SystemId"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static float getScale(MemorySegment mem) {
      return getScale(mem, 0);
   }

   public static float getScale(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1));
   }

   @Nullable
   public static Color getColor(MemorySegment mem) {
      return getColor(mem, 0);
   }

   @Nullable
   public static Color getColor(MemorySegment mem, int offset) {
      return hasColor(mem, offset) ? Color.toObject(mem, offset + 5) : null;
   }

   public static EntityPart getTargetEntityPart(MemorySegment mem) {
      return getTargetEntityPart(mem, 0);
   }

   public static EntityPart getTargetEntityPart(MemorySegment mem, int offset) {
      return EntityPart.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 8)));
   }

   @Nullable
   public static String getTargetNodeName(MemorySegment mem) {
      return getTargetNodeName(mem, 0);
   }

   @Nullable
   public static String getTargetNodeName(MemorySegment mem, int offset) {
      return hasTargetNodeName(mem, offset)
         ? PacketIO.readVarString("TargetNodeName", mem, offset + getValidatedOffset(mem, offset, 38, 42, "TargetNodeName"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static Vector3fc getPositionOffset(MemorySegment mem) {
      return getPositionOffset(mem, 0);
   }

   @Nullable
   public static Vector3fc getPositionOffset(MemorySegment mem, int offset) {
      return hasPositionOffset(mem, offset) ? PacketIO.readVector3f(mem, offset + 9) : null;
   }

   @Nullable
   public static Direction getRotationOffset(MemorySegment mem) {
      return getRotationOffset(mem, 0);
   }

   @Nullable
   public static Direction getRotationOffset(MemorySegment mem, int offset) {
      return hasRotationOffset(mem, offset) ? Direction.toObject(mem, offset + 21) : null;
   }

   public static boolean getDetachedFromModel(MemorySegment mem) {
      return getDetachedFromModel(mem, 0);
   }

   public static boolean getDetachedFromModel(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 33));
   }

   public static boolean hasColor(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasPositionOffset(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasRotationOffset(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasSystemId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   public static boolean hasTargetNodeName(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 16) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static ModelParticle toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ModelParticle toObject(MemorySegment mem, int offset) {
      if (offset + 42 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ModelParticle", offset + 42, (int)mem.byteSize());
      } else {
         return new ModelParticle(
            hasSystemId(mem, offset)
               ? PacketIO.readVarString("SystemId", mem, offset + getValidatedOffset(mem, offset, 34, 42, "SystemId"), 4096000, PacketIO.UTF8)
               : null,
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1)),
            hasColor(mem, offset) ? Color.toObject(mem, offset + 5) : null,
            EntityPart.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 8))),
            hasTargetNodeName(mem, offset)
               ? PacketIO.readVarString("TargetNodeName", mem, offset + getValidatedOffset(mem, offset, 38, 42, "TargetNodeName"), 4096000, PacketIO.UTF8)
               : null,
            hasPositionOffset(mem, offset) ? PacketIO.readVector3f(mem, offset + 9) : null,
            hasRotationOffset(mem, offset) ? Direction.toObject(mem, offset + 21) : null,
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 33))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.color != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.positionOffset != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.rotationOffset != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.systemId != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.targetNodeName != null) {
         nullBits = (byte)(nullBits | 16);
      }

      buf.writeByte(nullBits);
      buf.writeFloatLE(this.scale);
      if (this.color != null) {
         this.color.serialize(buf);
      } else {
         buf.writeZero(3);
      }

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

      buf.writeByte(this.detachedFromModel ? 1 : 0);
      int systemIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int targetNodeNameOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.systemId != null) {
         buf.setIntLE(systemIdOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.systemId, 4096000);
      } else {
         buf.setIntLE(systemIdOffsetSlot, -1);
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
      if (this.color != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.positionOffset != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.rotationOffset != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.systemId != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.targetNodeName != null) {
         nullBits = (byte)(nullBits | 16);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 1), this.scale);
      if (this.color != null) {
         this.color.serialize(mem, offset + 5);
      } else {
         mem.asSlice(offset + 5, 3L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 8), (byte)this.targetEntityPart.getValue());
      if (this.positionOffset != null) {
         PacketIO.writeVector3f(mem, offset + 9, this.positionOffset);
      } else {
         mem.asSlice(offset + 9, 12L).fill((byte)0);
      }

      if (this.rotationOffset != null) {
         this.rotationOffset.serialize(mem, offset + 21);
      } else {
         mem.asSlice(offset + 21, 12L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_BOOL, offset + 33, this.detachedFromModel);
      int varOffset = offset + 42;
      if (this.systemId != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 34), varOffset - offset - 42);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.systemId, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 34), -1);
      }

      if (this.targetNodeName != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 38), varOffset - offset - 42);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.targetNodeName, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 38), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 42;
      if (this.systemId != null) {
         size += PacketIO.stringSize(this.systemId);
      }

      if (this.targetNodeName != null) {
         size += PacketIO.stringSize(this.targetNodeName);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 42) {
         return ValidationResult.error("Buffer too small: expected at least 42 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int v = buffer.getByte(offset + 8) & 255;
         if (v >= 4) {
            return ValidationResult.error("Invalid EntityPart value for TargetEntityPart");
         } else {
            if ((nullBits & 8) != 0) {
               v = buffer.getIntLE(offset + 34);
               if (v < 0 || v > buffer.writerIndex() - offset - 42) {
                  return ValidationResult.error("Invalid offset for SystemId");
               }

               int pos = offset + 42 + v;
               int systemIdLen = VarInt.peek(buffer, pos);
               if (systemIdLen < 0) {
                  return ValidationResult.error("Invalid string length for SystemId");
               }

               if (systemIdLen > 4096000) {
                  return ValidationResult.error("SystemId exceeds max length 4096000");
               }

               pos += VarInt.size(systemIdLen);
               pos += systemIdLen;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading SystemId");
               }
            }

            if ((nullBits & 16) != 0) {
               v = buffer.getIntLE(offset + 38);
               if (v < 0 || v > buffer.writerIndex() - offset - 42) {
                  return ValidationResult.error("Invalid offset for TargetNodeName");
               }

               int posx = offset + 42 + v;
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

   public ModelParticle clone() {
      ModelParticle copy = new ModelParticle();
      copy.systemId = this.systemId;
      copy.scale = this.scale;
      copy.color = this.color != null ? this.color.clone() : null;
      copy.targetEntityPart = this.targetEntityPart;
      copy.targetNodeName = this.targetNodeName;
      copy.positionOffset = this.positionOffset;
      copy.rotationOffset = this.rotationOffset != null ? this.rotationOffset.clone() : null;
      copy.detachedFromModel = this.detachedFromModel;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ModelParticle other)
            ? false
            : Objects.equals(this.systemId, other.systemId)
               && this.scale == other.scale
               && Objects.equals(this.color, other.color)
               && Objects.equals(this.targetEntityPart, other.targetEntityPart)
               && Objects.equals(this.targetNodeName, other.targetNodeName)
               && Objects.equals(this.positionOffset, other.positionOffset)
               && Objects.equals(this.rotationOffset, other.rotationOffset)
               && this.detachedFromModel == other.detachedFromModel;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(
         this.systemId, this.scale, this.color, this.targetEntityPart, this.targetNodeName, this.positionOffset, this.rotationOffset, this.detachedFromModel
      );
   }
}
