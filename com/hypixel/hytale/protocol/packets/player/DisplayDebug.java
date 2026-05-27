package com.hypixel.hytale.protocol.packets.player;

import com.hypixel.hytale.protocol.DebugShape;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
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
import org.joml.Vector3fc;

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
   @Nonnull
   public Vector3fc color = PacketIO.ZERO_VECTOR3;
   public float time;
   public byte flags;
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
      @Nonnull DebugShape shape, @Nullable float[] matrix, @Nonnull Vector3fc color, float time, byte flags, @Nullable float[] frustumProjection, float opacity
   ) {
      this.shape = shape;
      this.matrix = matrix;
      this.color = color;
      this.time = time;
      this.flags = flags;
      this.frustumProjection = frustumProjection;
      this.opacity = opacity;
   }

   public DisplayDebug(@Nonnull DisplayDebug other) {
      this.shape = other.shape;
      this.matrix = other.matrix;
      this.color = other.color;
      this.time = other.time;
      this.flags = other.flags;
      this.frustumProjection = other.frustumProjection;
      this.opacity = other.opacity;
   }

   @Nonnull
   public static DisplayDebug deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 31) {
         throw ProtocolException.bufferTooSmall("DisplayDebug", 31, buf.readableBytes() - offset);
      } else {
         DisplayDebug obj = new DisplayDebug();
         byte nullBits = buf.getByte(offset);
         obj.shape = DebugShape.fromValue(buf.getByte(offset + 1));
         obj.color = PacketIO.readVector3f(buf, offset + 2);
         obj.time = buf.getFloatLE(offset + 14);
         obj.flags = buf.getByte(offset + 18);
         obj.opacity = buf.getFloatLE(offset + 19);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 23);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 31) {
               throw ProtocolException.invalidOffset("Matrix", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 31 + varPosBase0;
            int matrixCount = VarInt.peek(buf, varPos0);
            if (matrixCount < 0) {
               throw ProtocolException.invalidVarInt("Matrix");
            }

            int varIntLen = VarInt.size(matrixCount);
            if (matrixCount > 4096000) {
               throw ProtocolException.arrayTooLong("Matrix", matrixCount, 4096000);
            }

            if (varPos0 + varIntLen + matrixCount * 4L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Matrix", varPos0 + varIntLen + matrixCount * 4, buf.readableBytes());
            }

            obj.matrix = new float[matrixCount];

            for (int i = 0; i < matrixCount; i++) {
               obj.matrix[i] = buf.getFloatLE(varPos0 + varIntLen + i * 4);
            }
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 27);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 31) {
               throw ProtocolException.invalidOffset("FrustumProjection", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 31 + varPosBase1;
            int frustumProjectionCount = VarInt.peek(buf, varPos1);
            if (frustumProjectionCount < 0) {
               throw ProtocolException.invalidVarInt("FrustumProjection");
            }

            int varIntLenx = VarInt.size(frustumProjectionCount);
            if (frustumProjectionCount > 4096000) {
               throw ProtocolException.arrayTooLong("FrustumProjection", frustumProjectionCount, 4096000);
            }

            if (varPos1 + varIntLenx + frustumProjectionCount * 4L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("FrustumProjection", varPos1 + varIntLenx + frustumProjectionCount * 4, buf.readableBytes());
            }

            obj.frustumProjection = new float[frustumProjectionCount];

            for (int i = 0; i < frustumProjectionCount; i++) {
               obj.frustumProjection[i] = buf.getFloatLE(varPos1 + varIntLenx + i * 4);
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 31;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 23);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 31) {
            throw ProtocolException.invalidOffset("Matrix", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 31 + fieldOffset0;
         int arrLen = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(arrLen) + arrLen * 4;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 27);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 31) {
            throw ProtocolException.invalidOffset("FrustumProjection", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 31 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(arrLen) + arrLen * 4;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 31L;
   }

   public static DebugShape getShape(MemorySegment mem) {
      return getShape(mem, 0);
   }

   public static DebugShape getShape(MemorySegment mem, int offset) {
      return DebugShape.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1)));
   }

   @Nullable
   public static float[] getMatrix(MemorySegment mem) {
      return getMatrix(mem, 0);
   }

   @Nullable
   public static float[] getMatrix(MemorySegment mem, int offset) {
      if (!hasMatrix(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 23, 31, "Matrix");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Matrix", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Matrix", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 4L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Matrix", off + lenOffset + len * 4, (int)mem.byteSize());
            } else {
               off += lenOffset;
               float[] data = new float[len];
               MemorySegment.copy(mem, PacketIO.PROTO_FLOAT, off, data, 0, len);
               return data;
            }
         }
      }
   }

   public static Vector3fc getColor(MemorySegment mem) {
      return getColor(mem, 0);
   }

   public static Vector3fc getColor(MemorySegment mem, int offset) {
      return PacketIO.readVector3f(mem, offset + 2);
   }

   public static float getTime(MemorySegment mem) {
      return getTime(mem, 0);
   }

   public static float getTime(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 14));
   }

   public static byte getFlags(MemorySegment mem) {
      return getFlags(mem, 0);
   }

   public static byte getFlags(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BYTE, (long)(offset + 18));
   }

   @Nullable
   public static float[] getFrustumProjection(MemorySegment mem) {
      return getFrustumProjection(mem, 0);
   }

   @Nullable
   public static float[] getFrustumProjection(MemorySegment mem, int offset) {
      if (!hasFrustumProjection(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 27, 31, "FrustumProjection");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("FrustumProjection", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("FrustumProjection", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 4L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("FrustumProjection", off + lenOffset + len * 4, (int)mem.byteSize());
            } else {
               off += lenOffset;
               float[] data = new float[len];
               MemorySegment.copy(mem, PacketIO.PROTO_FLOAT, off, data, 0, len);
               return data;
            }
         }
      }
   }

   public static float getOpacity(MemorySegment mem) {
      return getOpacity(mem, 0);
   }

   public static float getOpacity(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 19));
   }

   public static boolean hasMatrix(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasFrustumProjection(MemorySegment mem, int offset) {
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

   public static DisplayDebug toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static DisplayDebug toObject(MemorySegment mem, int offset) {
      if (offset + 31 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("DisplayDebug", offset + 31, (int)mem.byteSize());
      } else {
         float[] matrix = null;
         if (hasMatrix(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 23, 31, "Matrix");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Matrix", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Matrix", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 4L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Matrix", off + lenOffset + len * 4, (int)mem.byteSize());
            }

            off += lenOffset;
            matrix = new float[len];
            MemorySegment.copy(mem, PacketIO.PROTO_FLOAT, off, matrix, 0, len);
         }

         float[] frustumProjection = null;
         if (hasFrustumProjection(mem, offset)) {
            int offx = offset + getValidatedOffset(mem, offset, 27, 31, "FrustumProjection");
            long packedx = VarInt.getWithLength(mem, offx);
            int lenx = (int)packedx;
            if (lenx < 0) {
               throw ProtocolException.negativeLength("FrustumProjection", lenx);
            }

            if (lenx > 4096000) {
               throw ProtocolException.arrayTooLong("FrustumProjection", lenx, 4096000);
            }

            int lenOffset = (int)(packedx >>> 32);
            if (offx + lenOffset + lenx * 4L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("FrustumProjection", offx + lenOffset + lenx * 4, (int)mem.byteSize());
            }

            offx += lenOffset;
            frustumProjection = new float[lenx];
            MemorySegment.copy(mem, PacketIO.PROTO_FLOAT, offx, frustumProjection, 0, lenx);
         }

         return new DisplayDebug(
            DebugShape.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))),
            matrix,
            PacketIO.readVector3f(mem, offset + 2),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 14)),
            mem.get(PacketIO.PROTO_BYTE, (long)(offset + 18)),
            frustumProjection,
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 19))
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.matrix != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.frustumProjection != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.shape.getValue());
      PacketIO.writeVector3f(buf, this.color);
      buf.writeFloatLE(this.time);
      buf.writeByte(this.flags);
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
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.matrix != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.frustumProjection != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.shape.getValue());
      PacketIO.writeVector3f(mem, offset + 2, this.color);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 14), this.time);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 18), this.flags);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 19), this.opacity);
      int varOffset = offset + 31;
      if (this.matrix != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 23), varOffset - offset - 31);
         if (this.matrix.length > 4096000) {
            throw ProtocolException.arrayTooLong("Matrix", this.matrix.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.matrix.length);
         MemorySegment.copy(this.matrix, 0, mem, PacketIO.PROTO_FLOAT, varOffset, this.matrix.length);
         varOffset += this.matrix.length * 4;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 23), -1);
      }

      if (this.frustumProjection != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 27), varOffset - offset - 31);
         if (this.frustumProjection.length > 4096000) {
            throw ProtocolException.arrayTooLong("FrustumProjection", this.frustumProjection.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.frustumProjection.length);
         MemorySegment.copy(this.frustumProjection, 0, mem, PacketIO.PROTO_FLOAT, varOffset, this.frustumProjection.length);
         varOffset += this.frustumProjection.length * 4;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 27), -1);
      }

      return varOffset - offset;
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
         int v = buffer.getByte(offset + 1) & 255;
         if (v >= 7) {
            return ValidationResult.error("Invalid DebugShape value for Shape");
         } else {
            if ((nullBits & 1) != 0) {
               v = buffer.getIntLE(offset + 23);
               if (v < 0 || v > buffer.writerIndex() - offset - 31) {
                  return ValidationResult.error("Invalid offset for Matrix");
               }

               int pos = offset + 31 + v;
               int matrixCount = VarInt.peek(buffer, pos);
               if (matrixCount < 0) {
                  return ValidationResult.error("Invalid array count for Matrix");
               }

               if (matrixCount > 4096000) {
                  return ValidationResult.error("Matrix exceeds max length 4096000");
               }

               pos += VarInt.size(matrixCount);
               pos += matrixCount * 4;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading Matrix");
               }
            }

            if ((nullBits & 2) != 0) {
               v = buffer.getIntLE(offset + 27);
               if (v < 0 || v > buffer.writerIndex() - offset - 31) {
                  return ValidationResult.error("Invalid offset for FrustumProjection");
               }

               int posx = offset + 31 + v;
               int frustumProjectionCount = VarInt.peek(buffer, posx);
               if (frustumProjectionCount < 0) {
                  return ValidationResult.error("Invalid array count for FrustumProjection");
               }

               if (frustumProjectionCount > 4096000) {
                  return ValidationResult.error("FrustumProjection exceeds max length 4096000");
               }

               posx += VarInt.size(frustumProjectionCount);
               posx += frustumProjectionCount * 4;
               if (posx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading FrustumProjection");
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public DisplayDebug clone() {
      DisplayDebug copy = new DisplayDebug();
      copy.shape = this.shape;
      copy.matrix = this.matrix != null ? Arrays.copyOf(this.matrix, this.matrix.length) : null;
      copy.color = this.color;
      copy.time = this.time;
      copy.flags = this.flags;
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
               && this.flags == other.flags
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
      result = 31 * result + Byte.hashCode(this.flags);
      result = 31 * result + Arrays.hashCode(this.frustumProjection);
      return 31 * result + Float.hashCode(this.opacity);
   }
}
