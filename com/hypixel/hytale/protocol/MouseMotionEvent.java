package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MouseMotionEvent {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 9;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 4096014;
   @Nullable
   public MouseButtonType[] mouseButtonType;
   @Nullable
   public Vector2i relativeMotion;

   public MouseMotionEvent() {
   }

   public MouseMotionEvent(@Nullable MouseButtonType[] mouseButtonType, @Nullable Vector2i relativeMotion) {
      this.mouseButtonType = mouseButtonType;
      this.relativeMotion = relativeMotion;
   }

   public MouseMotionEvent(@Nonnull MouseMotionEvent other) {
      this.mouseButtonType = other.mouseButtonType;
      this.relativeMotion = other.relativeMotion;
   }

   @Nonnull
   public static MouseMotionEvent deserialize(@Nonnull ByteBuf buf, int offset) {
      MouseMotionEvent obj = new MouseMotionEvent();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         obj.relativeMotion = Vector2i.deserialize(buf, offset + 1);
      }

      int pos = offset + 9;
      if ((nullBits & 2) != 0) {
         int mouseButtonTypeCount = VarInt.peek(buf, pos);
         if (mouseButtonTypeCount < 0) {
            throw ProtocolException.negativeLength("MouseButtonType", mouseButtonTypeCount);
         }

         if (mouseButtonTypeCount > 4096000) {
            throw ProtocolException.arrayTooLong("MouseButtonType", mouseButtonTypeCount, 4096000);
         }

         int mouseButtonTypeVarLen = VarInt.size(mouseButtonTypeCount);
         if (pos + mouseButtonTypeVarLen + mouseButtonTypeCount * 1L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("MouseButtonType", pos + mouseButtonTypeVarLen + mouseButtonTypeCount * 1, buf.readableBytes());
         }

         pos += mouseButtonTypeVarLen;
         obj.mouseButtonType = new MouseButtonType[mouseButtonTypeCount];

         for (int i = 0; i < mouseButtonTypeCount; i++) {
            obj.mouseButtonType[i] = MouseButtonType.fromValue(buf.getByte(pos));
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
      if (this.relativeMotion != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.mouseButtonType != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      if (this.relativeMotion != null) {
         this.relativeMotion.serialize(buf);
      } else {
         buf.writeZero(8);
      }

      if (this.mouseButtonType != null) {
         if (this.mouseButtonType.length > 4096000) {
            throw ProtocolException.arrayTooLong("MouseButtonType", this.mouseButtonType.length, 4096000);
         }

         VarInt.write(buf, this.mouseButtonType.length);

         for (MouseButtonType item : this.mouseButtonType) {
            buf.writeByte(item.getValue());
         }
      }
   }

   public int computeSize() {
      int size = 9;
      if (this.mouseButtonType != null) {
         size += VarInt.size(this.mouseButtonType.length) + this.mouseButtonType.length * 1;
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
            int mouseButtonTypeCount = VarInt.peek(buffer, pos);
            if (mouseButtonTypeCount < 0) {
               return ValidationResult.error("Invalid array count for MouseButtonType");
            }

            if (mouseButtonTypeCount > 4096000) {
               return ValidationResult.error("MouseButtonType exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += mouseButtonTypeCount * 1;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading MouseButtonType");
            }
         }

         return ValidationResult.OK;
      }
   }

   public MouseMotionEvent clone() {
      MouseMotionEvent copy = new MouseMotionEvent();
      copy.mouseButtonType = this.mouseButtonType != null ? Arrays.copyOf(this.mouseButtonType, this.mouseButtonType.length) : null;
      copy.relativeMotion = this.relativeMotion != null ? this.relativeMotion.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof MouseMotionEvent other)
            ? false
            : Arrays.equals((Object[])this.mouseButtonType, (Object[])other.mouseButtonType) && Objects.equals(this.relativeMotion, other.relativeMotion);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Arrays.hashCode((Object[])this.mouseButtonType);
      return 31 * result + Objects.hashCode(this.relativeMotion);
   }
}
