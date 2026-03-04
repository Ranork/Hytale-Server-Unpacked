package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ObjectiveTask {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 9;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public FormattedMessage taskDescriptionKey;
   public int currentCompletion;
   public int completionNeeded;

   public ObjectiveTask() {
   }

   public ObjectiveTask(@Nullable FormattedMessage taskDescriptionKey, int currentCompletion, int completionNeeded) {
      this.taskDescriptionKey = taskDescriptionKey;
      this.currentCompletion = currentCompletion;
      this.completionNeeded = completionNeeded;
   }

   public ObjectiveTask(@Nonnull ObjectiveTask other) {
      this.taskDescriptionKey = other.taskDescriptionKey;
      this.currentCompletion = other.currentCompletion;
      this.completionNeeded = other.completionNeeded;
   }

   @Nonnull
   public static ObjectiveTask deserialize(@Nonnull ByteBuf buf, int offset) {
      ObjectiveTask obj = new ObjectiveTask();
      byte nullBits = buf.getByte(offset);
      obj.currentCompletion = buf.getIntLE(offset + 1);
      obj.completionNeeded = buf.getIntLE(offset + 5);
      int pos = offset + 9;
      if ((nullBits & 1) != 0) {
         obj.taskDescriptionKey = FormattedMessage.deserialize(buf, pos);
         pos += FormattedMessage.computeBytesConsumed(buf, pos);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 9;
      if ((nullBits & 1) != 0) {
         pos += FormattedMessage.computeBytesConsumed(buf, pos);
      }

      return pos - offset;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.taskDescriptionKey != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.currentCompletion);
      buf.writeIntLE(this.completionNeeded);
      if (this.taskDescriptionKey != null) {
         this.taskDescriptionKey.serialize(buf);
      }
   }

   public int computeSize() {
      int size = 9;
      if (this.taskDescriptionKey != null) {
         size += this.taskDescriptionKey.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 9) {
         return ValidationResult.error("Buffer too small: expected at least 9 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 9;
         if ((nullBits & 1) != 0) {
            ValidationResult taskDescriptionKeyResult = FormattedMessage.validateStructure(buffer, pos);
            if (!taskDescriptionKeyResult.isValid()) {
               return ValidationResult.error("Invalid TaskDescriptionKey: " + taskDescriptionKeyResult.error());
            }

            pos += FormattedMessage.computeBytesConsumed(buffer, pos);
         }

         return ValidationResult.OK;
      }
   }

   public ObjectiveTask clone() {
      ObjectiveTask copy = new ObjectiveTask();
      copy.taskDescriptionKey = this.taskDescriptionKey != null ? this.taskDescriptionKey.clone() : null;
      copy.currentCompletion = this.currentCompletion;
      copy.completionNeeded = this.completionNeeded;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ObjectiveTask other)
            ? false
            : Objects.equals(this.taskDescriptionKey, other.taskDescriptionKey)
               && this.currentCompletion == other.currentCompletion
               && this.completionNeeded == other.completionNeeded;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.taskDescriptionKey, this.currentCompletion, this.completionNeeded);
   }
}
