package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.ObjectiveTask;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
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
      if (buf.readableBytes() - offset < 21) {
         throw ProtocolException.bufferTooSmall("UpdateObjectiveTask", 21, buf.readableBytes() - offset);
      } else {
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
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 21;
      if ((nullBits & 1) != 0) {
         pos += ObjectiveTask.computeBytesConsumed(buf, pos);
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 21L;
   }

   public static UUID getObjectiveUuid(MemorySegment mem) {
      return getObjectiveUuid(mem, 0);
   }

   public static UUID getObjectiveUuid(MemorySegment mem, int offset) {
      return PacketIO.readUUID(mem, offset + 1);
   }

   public static int getTaskIndex(MemorySegment mem) {
      return getTaskIndex(mem, 0);
   }

   public static int getTaskIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 17));
   }

   @Nullable
   public static ObjectiveTask getTask(MemorySegment mem) {
      return getTask(mem, 0);
   }

   @Nullable
   public static ObjectiveTask getTask(MemorySegment mem, int offset) {
      return hasTask(mem, offset) ? ObjectiveTask.toObject(mem, offset + 21) : null;
   }

   public static boolean hasTask(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static UpdateObjectiveTask toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UpdateObjectiveTask toObject(MemorySegment mem, int offset) {
      if (offset + 21 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UpdateObjectiveTask", offset + 21, (int)mem.byteSize());
      } else {
         return new UpdateObjectiveTask(
            PacketIO.readUUID(mem, offset + 1),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 17)),
            hasTask(mem, offset) ? ObjectiveTask.toObject(mem, offset + 21) : null
         );
      }
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
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.task != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      PacketIO.writeUUID(mem, offset + 1, this.objectiveUuid);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 17), this.taskIndex);
      int varOffset = offset + 21;
      if (this.task != null) {
         varOffset += this.task.serialize(mem, varOffset);
      }

      return varOffset - offset;
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
