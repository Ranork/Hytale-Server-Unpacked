package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
   public Vector3f positionOffset;
   @Nullable
   public Direction rotationOffset;
   public boolean fixedRotation;

   public ModelTrail() {
   }

   public ModelTrail(
      @Nullable String trailId,
      @Nonnull EntityPart targetEntityPart,
      @Nullable String targetNodeName,
      @Nullable Vector3f positionOffset,
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
      ModelTrail obj = new ModelTrail();
      byte nullBits = buf.getByte(offset);
      obj.targetEntityPart = EntityPart.fromValue(buf.getByte(offset + 1));
      if ((nullBits & 1) != 0) {
         obj.positionOffset = Vector3f.deserialize(buf, offset + 2);
      }

      if ((nullBits & 2) != 0) {
         obj.rotationOffset = Direction.deserialize(buf, offset + 14);
      }

      obj.fixedRotation = buf.getByte(offset + 26) != 0;
      if ((nullBits & 4) != 0) {
         int varPos0 = offset + 35 + buf.getIntLE(offset + 27);
         int trailIdLen = VarInt.peek(buf, varPos0);
         if (trailIdLen < 0) {
            throw ProtocolException.negativeLength("TrailId", trailIdLen);
         }

         if (trailIdLen > 4096000) {
            throw ProtocolException.stringTooLong("TrailId", trailIdLen, 4096000);
         }

         obj.trailId = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 8) != 0) {
         int varPos1 = offset + 35 + buf.getIntLE(offset + 31);
         int targetNodeNameLen = VarInt.peek(buf, varPos1);
         if (targetNodeNameLen < 0) {
            throw ProtocolException.negativeLength("TargetNodeName", targetNodeNameLen);
         }

         if (targetNodeNameLen > 4096000) {
            throw ProtocolException.stringTooLong("TargetNodeName", targetNodeNameLen, 4096000);
         }

         obj.targetNodeName = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 35;
      if ((nullBits & 4) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 27);
         int pos0 = offset + 35 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 31);
         int pos1 = offset + 35 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
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
         this.positionOffset.serialize(buf);
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
         if ((nullBits & 4) != 0) {
            int trailIdOffset = buffer.getIntLE(offset + 27);
            if (trailIdOffset < 0) {
               return ValidationResult.error("Invalid offset for TrailId");
            }

            int pos = offset + 35 + trailIdOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for TrailId");
            }

            int trailIdLen = VarInt.peek(buffer, pos);
            if (trailIdLen < 0) {
               return ValidationResult.error("Invalid string length for TrailId");
            }

            if (trailIdLen > 4096000) {
               return ValidationResult.error("TrailId exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += trailIdLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading TrailId");
            }
         }

         if ((nullBits & 8) != 0) {
            int targetNodeNameOffset = buffer.getIntLE(offset + 31);
            if (targetNodeNameOffset < 0) {
               return ValidationResult.error("Invalid offset for TargetNodeName");
            }

            int posx = offset + 35 + targetNodeNameOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for TargetNodeName");
            }

            int targetNodeNameLen = VarInt.peek(buffer, posx);
            if (targetNodeNameLen < 0) {
               return ValidationResult.error("Invalid string length for TargetNodeName");
            }

            if (targetNodeNameLen > 4096000) {
               return ValidationResult.error("TargetNodeName exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
            posx += targetNodeNameLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading TargetNodeName");
            }
         }

         return ValidationResult.OK;
      }
   }

   public ModelTrail clone() {
      ModelTrail copy = new ModelTrail();
      copy.trailId = this.trailId;
      copy.targetEntityPart = this.targetEntityPart;
      copy.targetNodeName = this.targetNodeName;
      copy.positionOffset = this.positionOffset != null ? this.positionOffset.clone() : null;
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
