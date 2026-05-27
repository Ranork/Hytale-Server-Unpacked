package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
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
      if (buf.readableBytes() - offset < 9) {
         throw ProtocolException.bufferTooSmall("ObjectiveTask", 9, buf.readableBytes() - offset);
      } else {
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
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 9;
      if ((nullBits & 1) != 0) {
         pos += FormattedMessage.computeBytesConsumed(buf, pos);
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 9L;
   }

   @Nullable
   public static FormattedMessage getTaskDescriptionKey(MemorySegment mem) {
      return getTaskDescriptionKey(mem, 0);
   }

   @Nullable
   public static FormattedMessage getTaskDescriptionKey(MemorySegment mem, int offset) {
      return hasTaskDescriptionKey(mem, offset) ? FormattedMessage.toObject(mem, offset + 9) : null;
   }

   public static int getCurrentCompletion(MemorySegment mem) {
      return getCurrentCompletion(mem, 0);
   }

   public static int getCurrentCompletion(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   public static int getCompletionNeeded(MemorySegment mem) {
      return getCompletionNeeded(mem, 0);
   }

   public static int getCompletionNeeded(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 5));
   }

   public static boolean hasTaskDescriptionKey(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static ObjectiveTask toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ObjectiveTask toObject(MemorySegment mem, int offset) {
      if (offset + 9 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ObjectiveTask", offset + 9, (int)mem.byteSize());
      } else {
         return new ObjectiveTask(
            hasTaskDescriptionKey(mem, offset) ? FormattedMessage.toObject(mem, offset + 9) : null,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 5))
         );
      }
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

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.taskDescriptionKey != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.currentCompletion);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 5), this.completionNeeded);
      int varOffset = offset + 9;
      if (this.taskDescriptionKey != null) {
         varOffset += this.taskDescriptionKey.serialize(mem, varOffset);
      }

      return varOffset - offset;
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
