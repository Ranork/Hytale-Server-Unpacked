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

public class CameraAxis {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 9;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 4096014;
   @Nullable
   public Rangef angleRange;
   @Nullable
   public CameraNode[] targetNodes;

   public CameraAxis() {
   }

   public CameraAxis(@Nullable Rangef angleRange, @Nullable CameraNode[] targetNodes) {
      this.angleRange = angleRange;
      this.targetNodes = targetNodes;
   }

   public CameraAxis(@Nonnull CameraAxis other) {
      this.angleRange = other.angleRange;
      this.targetNodes = other.targetNodes;
   }

   @Nonnull
   public static CameraAxis deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 9) {
         throw ProtocolException.bufferTooSmall("CameraAxis", 9, buf.readableBytes() - offset);
      } else {
         CameraAxis obj = new CameraAxis();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            obj.angleRange = Rangef.deserialize(buf, offset + 1);
         }

         int pos = offset + 9;
         if ((nullBits & 2) != 0) {
            int targetNodesCount = VarInt.peek(buf, pos);
            if (targetNodesCount < 0) {
               throw ProtocolException.invalidVarInt("TargetNodes");
            }

            int targetNodesVarLen = VarInt.size(targetNodesCount);
            if (targetNodesCount > 4096000) {
               throw ProtocolException.arrayTooLong("TargetNodes", targetNodesCount, 4096000);
            }

            if (pos + targetNodesVarLen + targetNodesCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("TargetNodes", pos + targetNodesVarLen + targetNodesCount * 1, buf.readableBytes());
            }

            pos += targetNodesVarLen;
            obj.targetNodes = new CameraNode[targetNodesCount];

            for (int i = 0; i < targetNodesCount; i++) {
               obj.targetNodes[i] = CameraNode.fromValue(buf.getByte(pos));
               pos++;
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 9;
      if ((nullBits & 2) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.size(arrLen) + arrLen * 1;
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 9L;
   }

   @Nullable
   public static Rangef getAngleRange(MemorySegment mem) {
      return getAngleRange(mem, 0);
   }

   @Nullable
   public static Rangef getAngleRange(MemorySegment mem, int offset) {
      return hasAngleRange(mem, offset) ? Rangef.toObject(mem, offset + 1) : null;
   }

   @Nullable
   public static CameraNode[] getTargetNodes(MemorySegment mem) {
      return getTargetNodes(mem, 0);
   }

   @Nullable
   public static CameraNode[] getTargetNodes(MemorySegment mem, int offset) {
      if (!hasTargetNodes(mem, offset)) {
         return null;
      } else {
         int off = offset + 9;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("TargetNodes", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("TargetNodes", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("TargetNodes", off + lenOffset + len * 1, (int)mem.byteSize());
            } else {
               off += lenOffset;
               CameraNode[] data = new CameraNode[len];

               for (int i = 0; i < len; i++) {
                  data[i] = CameraNode.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(off + i * 1)));
               }

               return data;
            }
         }
      }
   }

   public static boolean hasAngleRange(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasTargetNodes(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static CameraAxis toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static CameraAxis toObject(MemorySegment mem, int offset) {
      if (offset + 9 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("CameraAxis", offset + 9, (int)mem.byteSize());
      } else {
         CameraNode[] targetNodes = null;
         if (hasTargetNodes(mem, offset)) {
            int off = offset + 9;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("TargetNodes", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("TargetNodes", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("TargetNodes", off + lenOffset + len * 1, (int)mem.byteSize());
            }

            off += lenOffset;
            targetNodes = new CameraNode[len];

            for (int i = 0; i < len; i++) {
               targetNodes[i] = CameraNode.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(off + i * 1)));
            }
         }

         return new CameraAxis(hasAngleRange(mem, offset) ? Rangef.toObject(mem, offset + 1) : null, targetNodes);
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.angleRange != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.targetNodes != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      if (this.angleRange != null) {
         this.angleRange.serialize(buf);
      } else {
         buf.writeZero(8);
      }

      if (this.targetNodes != null) {
         if (this.targetNodes.length > 4096000) {
            throw ProtocolException.arrayTooLong("TargetNodes", this.targetNodes.length, 4096000);
         }

         VarInt.write(buf, this.targetNodes.length);

         for (CameraNode item : this.targetNodes) {
            buf.writeByte(item.getValue());
         }
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.angleRange != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.targetNodes != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      if (this.angleRange != null) {
         this.angleRange.serialize(mem, offset + 1);
      } else {
         mem.asSlice(offset + 1, 8L).fill((byte)0);
      }

      int varOffset = offset + 9;
      if (this.targetNodes != null) {
         if (this.targetNodes.length > 4096000) {
            throw ProtocolException.arrayTooLong("TargetNodes", this.targetNodes.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.targetNodes.length);

         for (int i = 0; i < this.targetNodes.length; i++) {
            mem.set(PacketIO.PROTO_BYTE, (long)(varOffset + i * 1), (byte)this.targetNodes[i].getValue());
         }

         varOffset += this.targetNodes.length * 1;
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 9;
      if (this.targetNodes != null) {
         size += VarInt.size(this.targetNodes.length) + this.targetNodes.length * 1;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 9) {
         return ValidationResult.error("Buffer too small: expected at least 9 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 9;
         if ((nullBits & 2) != 0) {
            int targetNodesCount = VarInt.peek(buffer, pos);
            if (targetNodesCount < 0) {
               return ValidationResult.error("Invalid array count for TargetNodes");
            }

            if (targetNodesCount > 4096000) {
               return ValidationResult.error("TargetNodes exceeds max length 4096000");
            }

            pos += VarInt.size(targetNodesCount);
            if (pos + targetNodesCount * 1L > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading TargetNodes");
            }

            for (int i = 0; i < targetNodesCount; i++) {
               int v = buffer.getByte(pos) & 255;
               if (v >= 5) {
                  return ValidationResult.error("Invalid CameraNode value for TargetNodes[i]");
               }

               pos++;
            }
         }

         return ValidationResult.OK;
      }
   }

   public CameraAxis clone() {
      CameraAxis copy = new CameraAxis();
      copy.angleRange = this.angleRange != null ? this.angleRange.clone() : null;
      copy.targetNodes = this.targetNodes != null ? Arrays.copyOf(this.targetNodes, this.targetNodes.length) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof CameraAxis other)
            ? false
            : Objects.equals(this.angleRange, other.angleRange) && Arrays.equals((Object[])this.targetNodes, (Object[])other.targetNodes);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.angleRange);
      return 31 * result + Arrays.hashCode((Object[])this.targetNodes);
   }
}
