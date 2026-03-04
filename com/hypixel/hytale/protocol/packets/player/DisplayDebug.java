package com.hypixel.hytale.protocol.packets.player;

import com.hypixel.hytale.protocol.DebugShape;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.Vector3f;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DisplayDebug implements Packet, ToClientPacket {
   public static final int PACKET_ID = 114;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 23;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 31;
   public static final int MAX_SIZE = 32768041;
   @Nonnull
   public DebugShape shape = DebugShape.Sphere;
   @Nullable
   public float[] matrix;
   @Nullable
   public Vector3f color;
   public float time;
   public boolean fade;
   @Nullable
   public float[] frustumProjection;
   public float opacity;

   @Override
   public int getId() {
      return 114;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public DisplayDebug() {
   }

   public DisplayDebug(
      @Nonnull DebugShape shape,
      @Nullable float[] matrix,
      @Nullable Vector3f color,
      float time,
      boolean fade,
      @Nullable float[] frustumProjection,
      float opacity
   ) {
      this.shape = shape;
      this.matrix = matrix;
      this.color = color;
      this.time = time;
      this.fade = fade;
      this.frustumProjection = frustumProjection;
      this.opacity = opacity;
   }

   public DisplayDebug(@Nonnull DisplayDebug other) {
      this.shape = other.shape;
      this.matrix = other.matrix;
      this.color = other.color;
      this.time = other.time;
      this.fade = other.fade;
      this.frustumProjection = other.frustumProjection;
      this.opacity = other.opacity;
   }

   @Nonnull
   public static DisplayDebug deserialize(@Nonnull ByteBuf buf, int offset) {
      DisplayDebug obj = new DisplayDebug();
      byte nullBits = buf.getByte(offset);
      obj.shape = DebugShape.fromValue(buf.getByte(offset + 1));
      if ((nullBits & 1) != 0) {
         obj.color = Vector3f.deserialize(buf, offset + 2);
      }

      obj.time = buf.getFloatLE(offset + 14);
      obj.fade = buf.getByte(offset + 18) != 0;
      obj.opacity = buf.getFloatLE(offset + 19);
      if ((nullBits & 2) != 0) {
         int varPos0 = offset + 31 + buf.getIntLE(offset + 23);
         int matrixCount = VarInt.peek(buf, varPos0);
         if (matrixCount < 0) {
            throw ProtocolException.negativeLength("Matrix", matrixCount);
         }

         if (matrixCount > 4096000) {
            throw ProtocolException.arrayTooLong("Matrix", matrixCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos0);
         if (varPos0 + varIntLen + matrixCount * 4L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Matrix", varPos0 + varIntLen + matrixCount * 4, buf.readableBytes());
         }

         obj.matrix = new float[matrixCount];

         for (int i = 0; i < matrixCount; i++) {
            obj.matrix[i] = buf.getFloatLE(varPos0 + varIntLen + i * 4);
         }
      }

      if ((nullBits & 4) != 0) {
         int varPos1 = offset + 31 + buf.getIntLE(offset + 27);
         int frustumProjectionCount = VarInt.peek(buf, varPos1);
         if (frustumProjectionCount < 0) {
            throw ProtocolException.negativeLength("FrustumProjection", frustumProjectionCount);
         }

         if (frustumProjectionCount > 4096000) {
            throw ProtocolException.arrayTooLong("FrustumProjection", frustumProjectionCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos1);
         if (varPos1 + varIntLen + frustumProjectionCount * 4L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("FrustumProjection", varPos1 + varIntLen + frustumProjectionCount * 4, buf.readableBytes());
         }

         obj.frustumProjection = new float[frustumProjectionCount];

         for (int i = 0; i < frustumProjectionCount; i++) {
            obj.frustumProjection[i] = buf.getFloatLE(varPos1 + varIntLen + i * 4);
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 31;
      if ((nullBits & 2) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 23);
         int pos0 = offset + 31 + fieldOffset0;
         int arrLen = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + arrLen * 4;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 27);
         int pos1 = offset + 31 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1) + arrLen * 4;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.color != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.matrix != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.frustumProjection != null) {
         nullBits = (byte)(nullBits | 4);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.shape.getValue());
      if (this.color != null) {
         this.color.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      buf.writeFloatLE(this.time);
      buf.writeByte(this.fade ? 1 : 0);
      buf.writeFloatLE(this.opacity);
      int matrixOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int frustumProjectionOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.matrix != null) {
         buf.setIntLE(matrixOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.matrix.length > 4096000) {
            throw ProtocolException.arrayTooLong("Matrix", this.matrix.length, 4096000);
         }

         VarInt.write(buf, this.matrix.length);

         for (float item : this.matrix) {
            buf.writeFloatLE(item);
         }
      } else {
         buf.setIntLE(matrixOffsetSlot, -1);
      }

      if (this.frustumProjection != null) {
         buf.setIntLE(frustumProjectionOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.frustumProjection.length > 4096000) {
            throw ProtocolException.arrayTooLong("FrustumProjection", this.frustumProjection.length, 4096000);
         }

         VarInt.write(buf, this.frustumProjection.length);

         for (float item : this.frustumProjection) {
            buf.writeFloatLE(item);
         }
      } else {
         buf.setIntLE(frustumProjectionOffsetSlot, -1);
      }
   }

   @Override
   public int computeSize() {
      int size = 31;
      if (this.matrix != null) {
         size += VarInt.size(this.matrix.length) + this.matrix.length * 4;
      }

      if (this.frustumProjection != null) {
         size += VarInt.size(this.frustumProjection.length) + this.frustumProjection.length * 4;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 31) {
         return ValidationResult.error("Buffer too small: expected at least 31 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 2) != 0) {
            int matrixOffset = buffer.getIntLE(offset + 23);
            if (matrixOffset < 0) {
               return ValidationResult.error("Invalid offset for Matrix");
            }

            int pos = offset + 31 + matrixOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Matrix");
            }

            int matrixCount = VarInt.peek(buffer, pos);
            if (matrixCount < 0) {
               return ValidationResult.error("Invalid array count for Matrix");
            }

            if (matrixCount > 4096000) {
               return ValidationResult.error("Matrix exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += matrixCount * 4;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Matrix");
            }
         }

         if ((nullBits & 4) != 0) {
            int frustumProjectionOffset = buffer.getIntLE(offset + 27);
            if (frustumProjectionOffset < 0) {
               return ValidationResult.error("Invalid offset for FrustumProjection");
            }

            int posx = offset + 31 + frustumProjectionOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for FrustumProjection");
            }

            int frustumProjectionCount = VarInt.peek(buffer, posx);
            if (frustumProjectionCount < 0) {
               return ValidationResult.error("Invalid array count for FrustumProjection");
            }

            if (frustumProjectionCount > 4096000) {
               return ValidationResult.error("FrustumProjection exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
            posx += frustumProjectionCount * 4;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading FrustumProjection");
            }
         }

         return ValidationResult.OK;
      }
   }

   public DisplayDebug clone() {
      DisplayDebug copy = new DisplayDebug();
      copy.shape = this.shape;
      copy.matrix = this.matrix != null ? Arrays.copyOf(this.matrix, this.matrix.length) : null;
      copy.color = this.color != null ? this.color.clone() : null;
      copy.time = this.time;
      copy.fade = this.fade;
      copy.frustumProjection = this.frustumProjection != null ? Arrays.copyOf(this.frustumProjection, this.frustumProjection.length) : null;
      copy.opacity = this.opacity;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof DisplayDebug other)
            ? false
            : Objects.equals(this.shape, other.shape)
               && Arrays.equals(this.matrix, other.matrix)
               && Objects.equals(this.color, other.color)
               && this.time == other.time
               && this.fade == other.fade
               && Arrays.equals(this.frustumProjection, other.frustumProjection)
               && this.opacity == other.opacity;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.shape);
      result = 31 * result + Arrays.hashCode(this.matrix);
      result = 31 * result + Objects.hashCode(this.color);
      result = 31 * result + Float.hashCode(this.time);
      result = 31 * result + Boolean.hashCode(this.fade);
      result = 31 * result + Arrays.hashCode(this.frustumProjection);
      return 31 * result + Float.hashCode(this.opacity);
   }
}
