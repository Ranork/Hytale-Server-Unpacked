package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
   public Vector3f positionOffset;
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
      @Nullable Vector3f positionOffset,
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
      ModelParticle obj = new ModelParticle();
      byte nullBits = buf.getByte(offset);
      obj.scale = buf.getFloatLE(offset + 1);
      if ((nullBits & 1) != 0) {
         obj.color = Color.deserialize(buf, offset + 5);
      }

      obj.targetEntityPart = EntityPart.fromValue(buf.getByte(offset + 8));
      if ((nullBits & 2) != 0) {
         obj.positionOffset = Vector3f.deserialize(buf, offset + 9);
      }

      if ((nullBits & 4) != 0) {
         obj.rotationOffset = Direction.deserialize(buf, offset + 21);
      }

      obj.detachedFromModel = buf.getByte(offset + 33) != 0;
      if ((nullBits & 8) != 0) {
         int varPos0 = offset + 42 + buf.getIntLE(offset + 34);
         int systemIdLen = VarInt.peek(buf, varPos0);
         if (systemIdLen < 0) {
            throw ProtocolException.negativeLength("SystemId", systemIdLen);
         }

         if (systemIdLen > 4096000) {
            throw ProtocolException.stringTooLong("SystemId", systemIdLen, 4096000);
         }

         obj.systemId = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 16) != 0) {
         int varPos1 = offset + 42 + buf.getIntLE(offset + 38);
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
      int maxEnd = 42;
      if ((nullBits & 8) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 34);
         int pos0 = offset + 42 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 16) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 38);
         int pos1 = offset + 42 + fieldOffset1;
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
         this.positionOffset.serialize(buf);
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
         if ((nullBits & 8) != 0) {
            int systemIdOffset = buffer.getIntLE(offset + 34);
            if (systemIdOffset < 0) {
               return ValidationResult.error("Invalid offset for SystemId");
            }

            int pos = offset + 42 + systemIdOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for SystemId");
            }

            int systemIdLen = VarInt.peek(buffer, pos);
            if (systemIdLen < 0) {
               return ValidationResult.error("Invalid string length for SystemId");
            }

            if (systemIdLen > 4096000) {
               return ValidationResult.error("SystemId exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += systemIdLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading SystemId");
            }
         }

         if ((nullBits & 16) != 0) {
            int targetNodeNameOffset = buffer.getIntLE(offset + 38);
            if (targetNodeNameOffset < 0) {
               return ValidationResult.error("Invalid offset for TargetNodeName");
            }

            int posx = offset + 42 + targetNodeNameOffset;
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

   public ModelParticle clone() {
      ModelParticle copy = new ModelParticle();
      copy.systemId = this.systemId;
      copy.scale = this.scale;
      copy.color = this.color != null ? this.color.clone() : null;
      copy.targetEntityPart = this.targetEntityPart;
      copy.targetNodeName = this.targetNodeName;
      copy.positionOffset = this.positionOffset != null ? this.positionOffset.clone() : null;
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
