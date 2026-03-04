package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.ObjectiveTask;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UpdateObjectiveTask implements Packet, ToClientPacket {
   public static final int PACKET_ID = 71;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 21;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 21;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public UUID objectiveUuid = new UUID(0L, 0L);
   public int taskIndex;
   @Nullable
   public ObjectiveTask task;

   @Override
   public int getId() {
      return 71;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateObjectiveTask() {
   }

   public UpdateObjectiveTask(@Nonnull UUID objectiveUuid, int taskIndex, @Nullable ObjectiveTask task) {
      this.objectiveUuid = objectiveUuid;
      this.taskIndex = taskIndex;
      this.task = task;
   }

   public UpdateObjectiveTask(@Nonnull UpdateObjectiveTask other) {
      this.objectiveUuid = other.objectiveUuid;
      this.taskIndex = other.taskIndex;
      this.task = other.task;
   }

   @Nonnull
   public static UpdateObjectiveTask deserialize(@Nonnull ByteBuf buf, int offset) {
      UpdateObjectiveTask obj = new UpdateObjectiveTask();
      byte nullBits = buf.getByte(offset);
      obj.objectiveUuid = PacketIO.readUUID(buf, offset + 1);
      obj.taskIndex = buf.getIntLE(offset + 17);
      int pos = offset + 21;
      if ((nullBits & 1) != 0) {
         obj.task = ObjectiveTask.deserialize(buf, pos);
         pos += ObjectiveTask.computeBytesConsumed(buf, pos);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 21;
      if ((nullBits & 1) != 0) {
         pos += ObjectiveTask.computeBytesConsumed(buf, pos);
      }

      return pos - offset;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.task != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      PacketIO.writeUUID(buf, this.objectiveUuid);
      buf.writeIntLE(this.taskIndex);
      if (this.task != null) {
         this.task.serialize(buf);
      }
   }

   @Override
   public int computeSize() {
      int size = 21;
      if (this.task != null) {
         size += this.task.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 21) {
         return ValidationResult.error("Buffer too small: expected at least 21 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 21;
         if ((nullBits & 1) != 0) {
            ValidationResult taskResult = ObjectiveTask.validateStructure(buffer, pos);
            if (!taskResult.isValid()) {
               return ValidationResult.error("Invalid Task: " + taskResult.error());
            }

            pos += ObjectiveTask.computeBytesConsumed(buffer, pos);
         }

         return ValidationResult.OK;
      }
   }

   public UpdateObjectiveTask clone() {
      UpdateObjectiveTask copy = new UpdateObjectiveTask();
      copy.objectiveUuid = this.objectiveUuid;
      copy.taskIndex = this.taskIndex;
      copy.task = this.task != null ? this.task.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateObjectiveTask other)
            ? false
            : Objects.equals(this.objectiveUuid, other.objectiveUuid) && this.taskIndex == other.taskIndex && Objects.equals(this.task, other.task);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.objectiveUuid, this.taskIndex, this.task);
   }
}
