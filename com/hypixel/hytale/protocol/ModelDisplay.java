package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ModelDisplay {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 37;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 45;
   public static final int MAX_SIZE = 32768055;
   @Nullable
   public String node;
   @Nullable
   public String attachTo;
   @Nullable
   public Vector3f translation;
   @Nullable
   public Vector3f rotation;
   @Nullable
   public Vector3f scale;

   public ModelDisplay() {
   }

   public ModelDisplay(@Nullable String node, @Nullable String attachTo, @Nullable Vector3f translation, @Nullable Vector3f rotation, @Nullable Vector3f scale) {
      this.node = node;
      this.attachTo = attachTo;
      this.translation = translation;
      this.rotation = rotation;
      this.scale = scale;
   }

   public ModelDisplay(@Nonnull ModelDisplay other) {
      this.node = other.node;
      this.attachTo = other.attachTo;
      this.translation = other.translation;
      this.rotation = other.rotation;
      this.scale = other.scale;
   }

   @Nonnull
   public static ModelDisplay deserialize(@Nonnull ByteBuf buf, int offset) {
      ModelDisplay obj = new ModelDisplay();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         obj.translation = Vector3f.deserialize(buf, offset + 1);
      }

      if ((nullBits & 2) != 0) {
         obj.rotation = Vector3f.deserialize(buf, offset + 13);
      }

      if ((nullBits & 4) != 0) {
         obj.scale = Vector3f.deserialize(buf, offset + 25);
      }

      if ((nullBits & 8) != 0) {
         int varPos0 = offset + 45 + buf.getIntLE(offset + 37);
         int nodeLen = VarInt.peek(buf, varPos0);
         if (nodeLen < 0) {
            throw ProtocolException.negativeLength("Node", nodeLen);
         }

         if (nodeLen > 4096000) {
            throw ProtocolException.stringTooLong("Node", nodeLen, 4096000);
         }

         obj.node = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 16) != 0) {
         int varPos1 = offset + 45 + buf.getIntLE(offset + 41);
         int attachToLen = VarInt.peek(buf, varPos1);
         if (attachToLen < 0) {
            throw ProtocolException.negativeLength("AttachTo", attachToLen);
         }

         if (attachToLen > 4096000) {
            throw ProtocolException.stringTooLong("AttachTo", attachToLen, 4096000);
         }

         obj.attachTo = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 45;
      if ((nullBits & 8) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 37);
         int pos0 = offset + 45 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 16) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 41);
         int pos1 = offset + 45 + fieldOffset1;
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
      if (this.translation != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.rotation != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.scale != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.node != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.attachTo != null) {
         nullBits = (byte)(nullBits | 16);
      }

      buf.writeByte(nullBits);
      if (this.translation != null) {
         this.translation.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      if (this.rotation != null) {
         this.rotation.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      if (this.scale != null) {
         this.scale.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      int nodeOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int attachToOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.node != null) {
         buf.setIntLE(nodeOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.node, 4096000);
      } else {
         buf.setIntLE(nodeOffsetSlot, -1);
      }

      if (this.attachTo != null) {
         buf.setIntLE(attachToOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.attachTo, 4096000);
      } else {
         buf.setIntLE(attachToOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 45;
      if (this.node != null) {
         size += PacketIO.stringSize(this.node);
      }

      if (this.attachTo != null) {
         size += PacketIO.stringSize(this.attachTo);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 45) {
         return ValidationResult.error("Buffer too small: expected at least 45 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 8) != 0) {
            int nodeOffset = buffer.getIntLE(offset + 37);
            if (nodeOffset < 0) {
               return ValidationResult.error("Invalid offset for Node");
            }

            int pos = offset + 45 + nodeOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Node");
            }

            int nodeLen = VarInt.peek(buffer, pos);
            if (nodeLen < 0) {
               return ValidationResult.error("Invalid string length for Node");
            }

            if (nodeLen > 4096000) {
               return ValidationResult.error("Node exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += nodeLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Node");
            }
         }

         if ((nullBits & 16) != 0) {
            int attachToOffset = buffer.getIntLE(offset + 41);
            if (attachToOffset < 0) {
               return ValidationResult.error("Invalid offset for AttachTo");
            }

            int posx = offset + 45 + attachToOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for AttachTo");
            }

            int attachToLen = VarInt.peek(buffer, posx);
            if (attachToLen < 0) {
               return ValidationResult.error("Invalid string length for AttachTo");
            }

            if (attachToLen > 4096000) {
               return ValidationResult.error("AttachTo exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
            posx += attachToLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading AttachTo");
            }
         }

         return ValidationResult.OK;
      }
   }

   public ModelDisplay clone() {
      ModelDisplay copy = new ModelDisplay();
      copy.node = this.node;
      copy.attachTo = this.attachTo;
      copy.translation = this.translation != null ? this.translation.clone() : null;
      copy.rotation = this.rotation != null ? this.rotation.clone() : null;
      copy.scale = this.scale != null ? this.scale.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ModelDisplay other)
            ? false
            : Objects.equals(this.node, other.node)
               && Objects.equals(this.attachTo, other.attachTo)
               && Objects.equals(this.translation, other.translation)
               && Objects.equals(this.rotation, other.rotation)
               && Objects.equals(this.scale, other.scale);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.node, this.attachTo, this.translation, this.rotation, this.scale);
   }
}
