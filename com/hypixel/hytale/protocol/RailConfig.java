package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RailConfig {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 102400006;
   @Nullable
   public RailPoint[] points;

   public RailConfig() {
   }

   public RailConfig(@Nullable RailPoint[] points) {
      this.points = points;
   }

   public RailConfig(@Nonnull RailConfig other) {
      this.points = other.points;
   }

   @Nonnull
   public static RailConfig deserialize(@Nonnull ByteBuf buf, int offset) {
      RailConfig obj = new RailConfig();
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int pointsCount = VarInt.peek(buf, pos);
         if (pointsCount < 0) {
            throw ProtocolException.negativeLength("Points", pointsCount);
         }

         if (pointsCount > 4096000) {
            throw ProtocolException.arrayTooLong("Points", pointsCount, 4096000);
         }

         int pointsVarLen = VarInt.size(pointsCount);
         if (pos + pointsVarLen + pointsCount * 25L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Points", pos + pointsVarLen + pointsCount * 25, buf.readableBytes());
         }

         pos += pointsVarLen;
         obj.points = new RailPoint[pointsCount];

         for (int i = 0; i < pointsCount; i++) {
            obj.points[i] = RailPoint.deserialize(buf, pos);
            pos += RailPoint.computeBytesConsumed(buf, pos);
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos);

         for (int i = 0; i < arrLen; i++) {
            pos += RailPoint.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.points != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.points != null) {
         if (this.points.length > 4096000) {
            throw ProtocolException.arrayTooLong("Points", this.points.length, 4096000);
         }

         VarInt.write(buf, this.points.length);

         for (RailPoint item : this.points) {
            item.serialize(buf);
         }
      }
   }

   public int computeSize() {
      int size = 1;
      if (this.points != null) {
         size += VarInt.size(this.points.length) + this.points.length * 25;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 1) {
         return ValidationResult.error("Buffer too small: expected at least 1 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            int pointsCount = VarInt.peek(buffer, pos);
            if (pointsCount < 0) {
               return ValidationResult.error("Invalid array count for Points");
            }

            if (pointsCount > 4096000) {
               return ValidationResult.error("Points exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += pointsCount * 25;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Points");
            }
         }

         return ValidationResult.OK;
      }
   }

   public RailConfig clone() {
      RailConfig copy = new RailConfig();
      copy.points = this.points != null ? Arrays.stream(this.points).map(e -> e.clone()).toArray(RailPoint[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof RailConfig other ? Arrays.equals((Object[])this.points, (Object[])other.points) : false;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      return 31 * result + Arrays.hashCode((Object[])this.points);
   }
}
