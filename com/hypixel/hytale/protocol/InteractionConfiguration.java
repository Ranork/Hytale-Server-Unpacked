package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InteractionConfiguration {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 4;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 12;
   public static final int MAX_SIZE = 1677721600;
   public boolean displayOutlines = true;
   public boolean debugOutlines;
   @Nullable
   public Map<GameMode, Float> useDistance;
   public boolean allEntities;
   @Nullable
   public Map<InteractionType, InteractionPriority> priorities;

   public InteractionConfiguration() {
   }

   public InteractionConfiguration(
      boolean displayOutlines,
      boolean debugOutlines,
      @Nullable Map<GameMode, Float> useDistance,
      boolean allEntities,
      @Nullable Map<InteractionType, InteractionPriority> priorities
   ) {
      this.displayOutlines = displayOutlines;
      this.debugOutlines = debugOutlines;
      this.useDistance = useDistance;
      this.allEntities = allEntities;
      this.priorities = priorities;
   }

   public InteractionConfiguration(@Nonnull InteractionConfiguration other) {
      this.displayOutlines = other.displayOutlines;
      this.debugOutlines = other.debugOutlines;
      this.useDistance = other.useDistance;
      this.allEntities = other.allEntities;
      this.priorities = other.priorities;
   }

   @Nonnull
   public static InteractionConfiguration deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 12) {
         throw ProtocolException.bufferTooSmall("InteractionConfiguration", 12, buf.readableBytes() - offset);
      } else {
         InteractionConfiguration obj = new InteractionConfiguration();
         byte nullBits = buf.getByte(offset);
         obj.displayOutlines = buf.getByte(offset + 1) != 0;
         obj.debugOutlines = buf.getByte(offset + 2) != 0;
         obj.allEntities = buf.getByte(offset + 3) != 0;
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 4);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 12) {
               throw ProtocolException.invalidOffset("UseDistance", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 12 + varPosBase0;
            int useDistanceCount = VarInt.peek(buf, varPos0);
            if (useDistanceCount < 0) {
               throw ProtocolException.invalidVarInt("UseDistance");
            }

            int varIntLen = VarInt.size(useDistanceCount);
            if (useDistanceCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("UseDistance", useDistanceCount, 4096000);
            }

            obj.useDistance = new HashMap<>(useDistanceCount);
            int dictPos = varPos0 + varIntLen;

            for (int i = 0; i < useDistanceCount; i++) {
               GameMode key = GameMode.fromValue(buf.getByte(dictPos));
               float val = buf.getFloatLE(++dictPos);
               dictPos += 4;
               if (obj.useDistance.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("useDistance", key);
               }
            }
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 8);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 12) {
               throw ProtocolException.invalidOffset("Priorities", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 12 + varPosBase1;
            int prioritiesCount = VarInt.peek(buf, varPos1);
            if (prioritiesCount < 0) {
               throw ProtocolException.invalidVarInt("Priorities");
            }

            int varIntLen = VarInt.size(prioritiesCount);
            if (prioritiesCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Priorities", prioritiesCount, 4096000);
            }

            obj.priorities = new HashMap<>(prioritiesCount);
            int dictPos = varPos1 + varIntLen;

            for (int ix = 0; ix < prioritiesCount; ix++) {
               InteractionType key = InteractionType.fromValue(buf.getByte(dictPos));
               InteractionPriority val = InteractionPriority.deserialize(buf, ++dictPos);
               dictPos += InteractionPriority.computeBytesConsumed(buf, dictPos);
               if (obj.priorities.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("priorities", key);
               }
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 12;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 4);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 12) {
            throw ProtocolException.invalidOffset("UseDistance", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 12 + fieldOffset0;
         int dictLen = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos0 = ++pos0 + 4;
         }

         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 8);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 12) {
            throw ProtocolException.invalidOffset("Priorities", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 12 + fieldOffset1;
         int dictLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos1 = ++pos1 + InteractionPriority.computeBytesConsumed(buf, pos1);
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 12L;
   }

   public static boolean getDisplayOutlines(MemorySegment mem) {
      return getDisplayOutlines(mem, 0);
   }

   public static boolean getDisplayOutlines(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1));
   }

   public static boolean getDebugOutlines(MemorySegment mem) {
      return getDebugOutlines(mem, 0);
   }

   public static boolean getDebugOutlines(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 2));
   }

   @Nullable
   public static Map<GameMode, Float> getUseDistance(MemorySegment mem) {
      return getUseDistance(mem, 0);
   }

   @Nullable
   public static Map<GameMode, Float> getUseDistance(MemorySegment mem, int offset) {
      if (!hasUseDistance(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 4, 12, "UseDistance");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("UseDistance", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("UseDistance", len, 4096000);
         } else {
            Map<GameMode, Float> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               GameMode key = GameMode.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)off));
               float value = mem.get(PacketIO.PROTO_FLOAT, (long)(++off));
               off += 4;
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("UseDistance", key);
               }
            }

            return data;
         }
      }
   }

   public static boolean getAllEntities(MemorySegment mem) {
      return getAllEntities(mem, 0);
   }

   public static boolean getAllEntities(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 3));
   }

   @Nullable
   public static Map<InteractionType, InteractionPriority> getPriorities(MemorySegment mem) {
      return getPriorities(mem, 0);
   }

   @Nullable
   public static Map<InteractionType, InteractionPriority> getPriorities(MemorySegment mem, int offset) {
      if (!hasPriorities(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 8, 12, "Priorities");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Priorities", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Priorities", len, 4096000);
         } else {
            Map<InteractionType, InteractionPriority> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               InteractionType key = InteractionType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)off));
               InteractionPriority value = InteractionPriority.toObject(mem, ++off);
               off += value.computeSize();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Priorities", key);
               }
            }

            return data;
         }
      }
   }

   public static boolean hasUseDistance(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasPriorities(MemorySegment mem, int offset) {
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

   public static InteractionConfiguration toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static InteractionConfiguration toObject(MemorySegment mem, int offset) {
      if (offset + 12 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("InteractionConfiguration", offset + 12, (int)mem.byteSize());
      } else {
         Map<GameMode, Float> useDistance = null;
         if (hasUseDistance(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 4, 12, "UseDistance");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("UseDistance", len);
            }

            if (len > 4096000) {
               throw ProtocolException.dictionaryTooLarge("UseDistance", len, 4096000);
            }

            useDistance = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               GameMode key = GameMode.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)off));
               float value = mem.get(PacketIO.PROTO_FLOAT, (long)(++off));
               off += 4;
               if (useDistance.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("UseDistance", key);
               }
            }
         }

         Map<InteractionType, InteractionPriority> priorities = null;
         if (hasPriorities(mem, offset)) {
            int offx = offset + getValidatedOffset(mem, offset, 8, 12, "Priorities");
            long packedx = VarInt.getWithLength(mem, offx);
            int lenx = (int)packedx;
            if (lenx < 0) {
               throw ProtocolException.negativeLength("Priorities", lenx);
            }

            if (lenx > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Priorities", lenx, 4096000);
            }

            priorities = new HashMap<>(lenx);
            offx += (int)(packedx >>> 32);

            for (int ix = 0; ix < lenx; ix++) {
               InteractionType key = InteractionType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)offx));
               InteractionPriority value = InteractionPriority.toObject(mem, ++offx);
               offx += value.computeSize();
               if (priorities.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Priorities", key);
               }
            }
         }

         return new InteractionConfiguration(
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 2)),
            useDistance,
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 3)),
            priorities
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.useDistance != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.priorities != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.displayOutlines ? 1 : 0);
      buf.writeByte(this.debugOutlines ? 1 : 0);
      buf.writeByte(this.allEntities ? 1 : 0);
      int useDistanceOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int prioritiesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.useDistance != null) {
         buf.setIntLE(useDistanceOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.useDistance.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("UseDistance", this.useDistance.size(), 4096000);
         }

         VarInt.write(buf, this.useDistance.size());

         for (Entry<GameMode, Float> e : this.useDistance.entrySet()) {
            buf.writeByte(e.getKey().getValue());
            buf.writeFloatLE(e.getValue());
         }
      } else {
         buf.setIntLE(useDistanceOffsetSlot, -1);
      }

      if (this.priorities != null) {
         buf.setIntLE(prioritiesOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.priorities.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Priorities", this.priorities.size(), 4096000);
         }

         VarInt.write(buf, this.priorities.size());

         for (Entry<InteractionType, InteractionPriority> e : this.priorities.entrySet()) {
            buf.writeByte(e.getKey().getValue());
            e.getValue().serialize(buf);
         }
      } else {
         buf.setIntLE(prioritiesOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.useDistance != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.priorities != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BOOL, offset + 1, this.displayOutlines);
      mem.set(PacketIO.PROTO_BOOL, offset + 2, this.debugOutlines);
      mem.set(PacketIO.PROTO_BOOL, offset + 3, this.allEntities);
      int varOffset = offset + 12;
      if (this.useDistance != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 4), varOffset - offset - 12);
         if (this.useDistance.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("UseDistance", this.useDistance.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.useDistance.size());

         for (Entry<GameMode, Float> e : this.useDistance.entrySet()) {
            mem.set(PacketIO.PROTO_BYTE, (long)varOffset, (byte)e.getKey().getValue());
            mem.set(PacketIO.PROTO_FLOAT, (long)(++varOffset), e.getValue());
            varOffset += 4;
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 4), -1);
      }

      if (this.priorities != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 8), varOffset - offset - 12);
         if (this.priorities.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Priorities", this.priorities.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.priorities.size());

         for (Entry<InteractionType, InteractionPriority> e : this.priorities.entrySet()) {
            mem.set(PacketIO.PROTO_BYTE, (long)varOffset, (byte)e.getKey().getValue());
            varOffset = ++varOffset + e.getValue().serialize(mem, varOffset);
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 8), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 12;
      if (this.useDistance != null) {
         size += VarInt.size(this.useDistance.size()) + this.useDistance.size() * 5;
      }

      if (this.priorities != null) {
         int prioritiesSize = 0;

         for (Entry<InteractionType, InteractionPriority> kvp : this.priorities.entrySet()) {
            prioritiesSize += 1 + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.priorities.size()) + prioritiesSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 12) {
         return ValidationResult.error("Buffer too small: expected at least 12 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int useDistanceOffset = buffer.getIntLE(offset + 4);
            if (useDistanceOffset < 0 || useDistanceOffset > buffer.writerIndex() - offset - 12) {
               return ValidationResult.error("Invalid offset for UseDistance");
            }

            int pos = offset + 12 + useDistanceOffset;
            int useDistanceCount = VarInt.peek(buffer, pos);
            if (useDistanceCount < 0) {
               return ValidationResult.error("Invalid dictionary count for UseDistance");
            }

            if (useDistanceCount > 4096000) {
               return ValidationResult.error("UseDistance exceeds max length 4096000");
            }

            pos += VarInt.size(useDistanceCount);

            for (int i = 0; i < useDistanceCount; i++) {
               int v = buffer.getByte(pos) & 255;
               if (v >= 2) {
                  return ValidationResult.error("Invalid GameMode value for key");
               }

               pos = ++pos + 4;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading value");
               }
            }
         }

         if ((nullBits & 2) != 0) {
            int prioritiesOffset = buffer.getIntLE(offset + 8);
            if (prioritiesOffset < 0 || prioritiesOffset > buffer.writerIndex() - offset - 12) {
               return ValidationResult.error("Invalid offset for Priorities");
            }

            int posx = offset + 12 + prioritiesOffset;
            int prioritiesCount = VarInt.peek(buffer, posx);
            if (prioritiesCount < 0) {
               return ValidationResult.error("Invalid dictionary count for Priorities");
            }

            if (prioritiesCount > 4096000) {
               return ValidationResult.error("Priorities exceeds max length 4096000");
            }

            posx += VarInt.size(prioritiesCount);

            for (int i = 0; i < prioritiesCount; i++) {
               int vx = buffer.getByte(posx) & 255;
               if (vx >= 25) {
                  return ValidationResult.error("Invalid InteractionType value for key");
               }

               posx = ++posx + InteractionPriority.computeBytesConsumed(buffer, posx);
            }
         }

         return ValidationResult.OK;
      }
   }

   public InteractionConfiguration clone() {
      InteractionConfiguration copy = new InteractionConfiguration();
      copy.displayOutlines = this.displayOutlines;
      copy.debugOutlines = this.debugOutlines;
      copy.useDistance = this.useDistance != null ? new HashMap<>(this.useDistance) : null;
      copy.allEntities = this.allEntities;
      if (this.priorities != null) {
         Map<InteractionType, InteractionPriority> m = new HashMap<>();

         for (Entry<InteractionType, InteractionPriority> e : this.priorities.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.priorities = m;
      }

      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof InteractionConfiguration other)
            ? false
            : this.displayOutlines == other.displayOutlines
               && this.debugOutlines == other.debugOutlines
               && Objects.equals(this.useDistance, other.useDistance)
               && this.allEntities == other.allEntities
               && Objects.equals(this.priorities, other.priorities);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.displayOutlines, this.debugOutlines, this.useDistance, this.allEntities, this.priorities);
   }
}
