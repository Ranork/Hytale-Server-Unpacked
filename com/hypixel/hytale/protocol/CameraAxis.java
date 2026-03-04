package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
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
      CameraAxis obj = new CameraAxis();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         obj.angleRange = Rangef.deserialize(buf, offset + 1);
      }

      int pos = offset + 9;
      if ((nullBits & 2) != 0) {
         int targetNodesCount = VarInt.peek(buf, pos);
         if (targetNodesCount < 0) {
            throw ProtocolException.negativeLength("TargetNodes", targetNodesCount);
         }

         if (targetNodesCount > 4096000) {
            throw ProtocolException.arrayTooLong("TargetNodes", targetNodesCount, 4096000);
         }

         int targetNodesVarLen = VarInt.size(targetNodesCount);
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

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 9;
      if ((nullBits & 2) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos) + arrLen * 1;
      }

      return pos - offset;
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

            pos += VarInt.length(buffer, pos);
            pos += targetNodesCount * 1;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading TargetNodes");
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
