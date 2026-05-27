package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Objective {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 17;
   public static final int VARIABLE_FIELD_COUNT = 4;
   public static final int VARIABLE_BLOCK_START = 33;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public UUID objectiveUuid = new UUID(0L, 0L);
   @Nullable
   public FormattedMessage objectiveTitleKey;
   @Nullable
   public FormattedMessage objectiveDescriptionKey;
   @Nullable
   public String objectiveLineId;
   @Nullable
   public ObjectiveTask[] tasks;

   public Objective() {
   }

   public Objective(
      @Nonnull UUID objectiveUuid,
      @Nullable FormattedMessage objectiveTitleKey,
      @Nullable FormattedMessage objectiveDescriptionKey,
      @Nullable String objectiveLineId,
      @Nullable ObjectiveTask[] tasks
   ) {
      this.objectiveUuid = objectiveUuid;
      this.objectiveTitleKey = objectiveTitleKey;
      this.objectiveDescriptionKey = objectiveDescriptionKey;
      this.objectiveLineId = objectiveLineId;
      this.tasks = tasks;
   }

   public Objective(@Nonnull Objective other) {
      this.objectiveUuid = other.objectiveUuid;
      this.objectiveTitleKey = other.objectiveTitleKey;
      this.objectiveDescriptionKey = other.objectiveDescriptionKey;
      this.objectiveLineId = other.objectiveLineId;
      this.tasks = other.tasks;
   }

   @Nonnull
   public static Objective deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 33) {
         throw ProtocolException.bufferTooSmall("Objective", 33, buf.readableBytes() - offset);
      } else {
         Objective obj = new Objective();
         byte nullBits = buf.getByte(offset);
         obj.objectiveUuid = PacketIO.readUUID(buf, offset + 1);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 17);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 33) {
               throw ProtocolException.invalidOffset("ObjectiveTitleKey", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 33 + varPosBase0;
            obj.objectiveTitleKey = FormattedMessage.deserialize(buf, varPos0);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 21);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 33) {
               throw ProtocolException.invalidOffset("ObjectiveDescriptionKey", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 33 + varPosBase1;
            obj.objectiveDescriptionKey = FormattedMessage.deserialize(buf, varPos1);
         }

         if ((nullBits & 4) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 25);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 33) {
               throw ProtocolException.invalidOffset("ObjectiveLineId", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 33 + varPosBase2;
            int objectiveLineIdLen = VarInt.peek(buf, varPos2);
            if (objectiveLineIdLen < 0) {
               throw ProtocolException.invalidVarInt("ObjectiveLineId");
            }

            int objectiveLineIdVarIntLen = VarInt.size(objectiveLineIdLen);
            if (objectiveLineIdLen > 4096000) {
               throw ProtocolException.stringTooLong("ObjectiveLineId", objectiveLineIdLen, 4096000);
            }

            if (varPos2 + objectiveLineIdVarIntLen + objectiveLineIdLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("ObjectiveLineId", varPos2 + objectiveLineIdVarIntLen + objectiveLineIdLen, buf.readableBytes());
            }

            obj.objectiveLineId = PacketIO.readVarString(buf, varPos2, PacketIO.UTF8);
         }

         if ((nullBits & 8) != 0) {
            int varPosBase3 = buf.getIntLE(offset + 29);
            if (varPosBase3 < 0 || varPosBase3 > buf.writerIndex() - offset - 33) {
               throw ProtocolException.invalidOffset("Tasks", varPosBase3, buf.readableBytes());
            }

            int varPos3 = offset + 33 + varPosBase3;
            int tasksCount = VarInt.peek(buf, varPos3);
            if (tasksCount < 0) {
               throw ProtocolException.invalidVarInt("Tasks");
            }

            int varIntLen = VarInt.size(tasksCount);
            if (tasksCount > 4096000) {
               throw ProtocolException.arrayTooLong("Tasks", tasksCount, 4096000);
            }

            if (varPos3 + varIntLen + tasksCount * 9L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Tasks", varPos3 + varIntLen + tasksCount * 9, buf.readableBytes());
            }

            obj.tasks = new ObjectiveTask[tasksCount];
            int elemPos = varPos3 + varIntLen;

            for (int i = 0; i < tasksCount; i++) {
               obj.tasks[i] = ObjectiveTask.deserialize(buf, elemPos);
               elemPos += ObjectiveTask.computeBytesConsumed(buf, elemPos);
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 33;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 17);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 33) {
            throw ProtocolException.invalidOffset("ObjectiveTitleKey", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 33 + fieldOffset0;
         pos0 += FormattedMessage.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 21);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 33) {
            throw ProtocolException.invalidOffset("ObjectiveDescriptionKey", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 33 + fieldOffset1;
         pos1 += FormattedMessage.computeBytesConsumed(buf, pos1);
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 25);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 33) {
            throw ProtocolException.invalidOffset("ObjectiveLineId", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 33 + fieldOffset2;
         int sl = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(sl) + sl;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 29);
         if (fieldOffset3 < 0 || fieldOffset3 > buf.writerIndex() - offset - 33) {
            throw ProtocolException.invalidOffset("Tasks", fieldOffset3, maxEnd);
         }

         int pos3 = offset + 33 + fieldOffset3;
         int arrLen = VarInt.peek(buf, pos3);
         pos3 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos3 += ObjectiveTask.computeBytesConsumed(buf, pos3);
         }

         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 33L;
   }

   public static UUID getObjectiveUuid(MemorySegment mem) {
      return getObjectiveUuid(mem, 0);
   }

   public static UUID getObjectiveUuid(MemorySegment mem, int offset) {
      return PacketIO.readUUID(mem, offset + 1);
   }

   @Nullable
   public static FormattedMessage getObjectiveTitleKey(MemorySegment mem) {
      return getObjectiveTitleKey(mem, 0);
   }

   @Nullable
   public static FormattedMessage getObjectiveTitleKey(MemorySegment mem, int offset) {
      return hasObjectiveTitleKey(mem, offset) ? FormattedMessage.toObject(mem, offset + getValidatedOffset(mem, offset, 17, 33, "ObjectiveTitleKey")) : null;
   }

   @Nullable
   public static FormattedMessage getObjectiveDescriptionKey(MemorySegment mem) {
      return getObjectiveDescriptionKey(mem, 0);
   }

   @Nullable
   public static FormattedMessage getObjectiveDescriptionKey(MemorySegment mem, int offset) {
      return hasObjectiveDescriptionKey(mem, offset)
         ? FormattedMessage.toObject(mem, offset + getValidatedOffset(mem, offset, 21, 33, "ObjectiveDescriptionKey"))
         : null;
   }

   @Nullable
   public static String getObjectiveLineId(MemorySegment mem) {
      return getObjectiveLineId(mem, 0);
   }

   @Nullable
   public static String getObjectiveLineId(MemorySegment mem, int offset) {
      return hasObjectiveLineId(mem, offset)
         ? PacketIO.readVarString("ObjectiveLineId", mem, offset + getValidatedOffset(mem, offset, 25, 33, "ObjectiveLineId"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static ObjectiveTask[] getTasks(MemorySegment mem) {
      return getTasks(mem, 0);
   }

   @Nullable
   public static ObjectiveTask[] getTasks(MemorySegment mem, int offset) {
      if (!hasTasks(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 29, 33, "Tasks");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Tasks", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Tasks", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Tasks", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               ObjectiveTask[] data = new ObjectiveTask[len];

               for (int i = 0; i < len; i++) {
                  data[i] = ObjectiveTask.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   public static boolean hasObjectiveTitleKey(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasObjectiveDescriptionKey(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasObjectiveLineId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasTasks(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static Objective toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static Objective toObject(MemorySegment mem, int offset) {
      if (offset + 33 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("Objective", offset + 33, (int)mem.byteSize());
      } else {
         ObjectiveTask[] tasks = null;
         if (hasTasks(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 29, 33, "Tasks");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Tasks", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Tasks", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Tasks", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            tasks = new ObjectiveTask[len];

            for (int i = 0; i < len; i++) {
               tasks[i] = ObjectiveTask.toObject(mem, off);
               off += tasks[i].computeSize();
            }
         }

         return new Objective(
            PacketIO.readUUID(mem, offset + 1),
            hasObjectiveTitleKey(mem, offset) ? FormattedMessage.toObject(mem, offset + getValidatedOffset(mem, offset, 17, 33, "ObjectiveTitleKey")) : null,
            hasObjectiveDescriptionKey(mem, offset)
               ? FormattedMessage.toObject(mem, offset + getValidatedOffset(mem, offset, 21, 33, "ObjectiveDescriptionKey"))
               : null,
            hasObjectiveLineId(mem, offset)
               ? PacketIO.readVarString("ObjectiveLineId", mem, offset + getValidatedOffset(mem, offset, 25, 33, "ObjectiveLineId"), 4096000, PacketIO.UTF8)
               : null,
            tasks
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.objectiveTitleKey != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.objectiveDescriptionKey != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.objectiveLineId != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.tasks != null) {
         nullBits = (byte)(nullBits | 8);
      }

      buf.writeByte(nullBits);
      PacketIO.writeUUID(buf, this.objectiveUuid);
      int objectiveTitleKeyOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int objectiveDescriptionKeyOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int objectiveLineIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int tasksOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.objectiveTitleKey != null) {
         buf.setIntLE(objectiveTitleKeyOffsetSlot, buf.writerIndex() - varBlockStart);
         this.objectiveTitleKey.serialize(buf);
      } else {
         buf.setIntLE(objectiveTitleKeyOffsetSlot, -1);
      }

      if (this.objectiveDescriptionKey != null) {
         buf.setIntLE(objectiveDescriptionKeyOffsetSlot, buf.writerIndex() - varBlockStart);
         this.objectiveDescriptionKey.serialize(buf);
      } else {
         buf.setIntLE(objectiveDescriptionKeyOffsetSlot, -1);
      }

      if (this.objectiveLineId != null) {
         buf.setIntLE(objectiveLineIdOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.objectiveLineId, 4096000);
      } else {
         buf.setIntLE(objectiveLineIdOffsetSlot, -1);
      }

      if (this.tasks != null) {
         buf.setIntLE(tasksOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.tasks.length > 4096000) {
            throw ProtocolException.arrayTooLong("Tasks", this.tasks.length, 4096000);
         }

         VarInt.write(buf, this.tasks.length);

         for (ObjectiveTask item : this.tasks) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(tasksOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.objectiveTitleKey != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.objectiveDescriptionKey != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.objectiveLineId != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.tasks != null) {
         nullBits = (byte)(nullBits | 8);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      PacketIO.writeUUID(mem, offset + 1, this.objectiveUuid);
      int varOffset = offset + 33;
      if (this.objectiveTitleKey != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 17), varOffset - offset - 33);
         varOffset += this.objectiveTitleKey.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 17), -1);
      }

      if (this.objectiveDescriptionKey != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 21), varOffset - offset - 33);
         varOffset += this.objectiveDescriptionKey.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 21), -1);
      }

      if (this.objectiveLineId != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 25), varOffset - offset - 33);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.objectiveLineId, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 25), -1);
      }

      if (this.tasks != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 29), varOffset - offset - 33);
         if (this.tasks.length > 4096000) {
            throw ProtocolException.arrayTooLong("Tasks", this.tasks.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.tasks.length);
         int tasksValueOffset = 0;

         for (int i = 0; i < this.tasks.length; i++) {
            tasksValueOffset += this.tasks[i].serialize(mem, varOffset + tasksValueOffset);
         }

         varOffset += tasksValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 29), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 33;
      if (this.objectiveTitleKey != null) {
         size += this.objectiveTitleKey.computeSize();
      }

      if (this.objectiveDescriptionKey != null) {
         size += this.objectiveDescriptionKey.computeSize();
      }

      if (this.objectiveLineId != null) {
         size += PacketIO.stringSize(this.objectiveLineId);
      }

      if (this.tasks != null) {
         int tasksSize = 0;

         for (ObjectiveTask elem : this.tasks) {
            tasksSize += elem.computeSize();
         }

         size += VarInt.size(this.tasks.length) + tasksSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 33) {
         return ValidationResult.error("Buffer too small: expected at least 33 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int objectiveTitleKeyOffset = buffer.getIntLE(offset + 17);
            if (objectiveTitleKeyOffset < 0 || objectiveTitleKeyOffset > buffer.writerIndex() - offset - 33) {
               return ValidationResult.error("Invalid offset for ObjectiveTitleKey");
            }

            int pos = offset + 33 + objectiveTitleKeyOffset;
            ValidationResult objectiveTitleKeyResult = FormattedMessage.validateStructure(buffer, pos);
            if (!objectiveTitleKeyResult.isValid()) {
               return ValidationResult.error("Invalid ObjectiveTitleKey: " + objectiveTitleKeyResult.error());
            }

            pos += FormattedMessage.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits & 2) != 0) {
            int objectiveDescriptionKeyOffset = buffer.getIntLE(offset + 21);
            if (objectiveDescriptionKeyOffset < 0 || objectiveDescriptionKeyOffset > buffer.writerIndex() - offset - 33) {
               return ValidationResult.error("Invalid offset for ObjectiveDescriptionKey");
            }

            int pos = offset + 33 + objectiveDescriptionKeyOffset;
            ValidationResult objectiveDescriptionKeyResult = FormattedMessage.validateStructure(buffer, pos);
            if (!objectiveDescriptionKeyResult.isValid()) {
               return ValidationResult.error("Invalid ObjectiveDescriptionKey: " + objectiveDescriptionKeyResult.error());
            }

            pos += FormattedMessage.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits & 4) != 0) {
            int objectiveLineIdOffset = buffer.getIntLE(offset + 25);
            if (objectiveLineIdOffset < 0 || objectiveLineIdOffset > buffer.writerIndex() - offset - 33) {
               return ValidationResult.error("Invalid offset for ObjectiveLineId");
            }

            int pos = offset + 33 + objectiveLineIdOffset;
            int objectiveLineIdLen = VarInt.peek(buffer, pos);
            if (objectiveLineIdLen < 0) {
               return ValidationResult.error("Invalid string length for ObjectiveLineId");
            }

            if (objectiveLineIdLen > 4096000) {
               return ValidationResult.error("ObjectiveLineId exceeds max length 4096000");
            }

            pos += VarInt.size(objectiveLineIdLen);
            pos += objectiveLineIdLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading ObjectiveLineId");
            }
         }

         if ((nullBits & 8) != 0) {
            int tasksOffset = buffer.getIntLE(offset + 29);
            if (tasksOffset < 0 || tasksOffset > buffer.writerIndex() - offset - 33) {
               return ValidationResult.error("Invalid offset for Tasks");
            }

            int posx = offset + 33 + tasksOffset;
            int tasksCount = VarInt.peek(buffer, posx);
            if (tasksCount < 0) {
               return ValidationResult.error("Invalid array count for Tasks");
            }

            if (tasksCount > 4096000) {
               return ValidationResult.error("Tasks exceeds max length 4096000");
            }

            posx += VarInt.size(tasksCount);

            for (int i = 0; i < tasksCount; i++) {
               ValidationResult structResult = ObjectiveTask.validateStructure(buffer, posx);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid ObjectiveTask in Tasks[" + i + "]: " + structResult.error());
               }

               posx += ObjectiveTask.computeBytesConsumed(buffer, posx);
            }
         }

         return ValidationResult.OK;
      }
   }

   public Objective clone() {
      Objective copy = new Objective();
      copy.objectiveUuid = this.objectiveUuid;
      copy.objectiveTitleKey = this.objectiveTitleKey != null ? this.objectiveTitleKey.clone() : null;
      copy.objectiveDescriptionKey = this.objectiveDescriptionKey != null ? this.objectiveDescriptionKey.clone() : null;
      copy.objectiveLineId = this.objectiveLineId;
      copy.tasks = this.tasks != null ? Arrays.stream(this.tasks).map(e -> e.clone()).toArray(ObjectiveTask[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof Objective other)
            ? false
            : Objects.equals(this.objectiveUuid, other.objectiveUuid)
               && Objects.equals(this.objectiveTitleKey, other.objectiveTitleKey)
               && Objects.equals(this.objectiveDescriptionKey, other.objectiveDescriptionKey)
               && Objects.equals(this.objectiveLineId, other.objectiveLineId)
               && Arrays.equals((Object[])this.tasks, (Object[])other.tasks);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.objectiveUuid);
      result = 31 * result + Objects.hashCode(this.objectiveTitleKey);
      result = 31 * result + Objects.hashCode(this.objectiveDescriptionKey);
      result = 31 * result + Objects.hashCode(this.objectiveLineId);
      return 31 * result + Arrays.hashCode((Object[])this.tasks);
   }
}
