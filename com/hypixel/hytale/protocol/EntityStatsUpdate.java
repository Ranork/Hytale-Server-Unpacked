package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

public class EntityStatsUpdate extends ComponentUpdate {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 0;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 0;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public Map<Integer, EntityStatUpdate[]> entityStatUpdates = new HashMap<>();

   public EntityStatsUpdate() {
   }

   public EntityStatsUpdate(@Nonnull Map<Integer, EntityStatUpdate[]> entityStatUpdates) {
      this.entityStatUpdates = entityStatUpdates;
   }

   public EntityStatsUpdate(@Nonnull EntityStatsUpdate other) {
      this.entityStatUpdates = other.entityStatUpdates;
   }

   @Nonnull
   public static EntityStatsUpdate deserialize(@Nonnull ByteBuf buf, int offset) {
      EntityStatsUpdate obj = new EntityStatsUpdate();
      int pos = offset + 0;
      int entityStatUpdatesCount = VarInt.peek(buf, pos);
      if (entityStatUpdatesCount < 0) {
         throw ProtocolException.invalidVarInt("EntityStatUpdates");
      } else {
         int entityStatUpdatesVarLen = VarInt.size(entityStatUpdatesCount);
         if (entityStatUpdatesCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("EntityStatUpdates", entityStatUpdatesCount, 4096000);
         } else {
            pos += entityStatUpdatesVarLen;
            obj.entityStatUpdates = new HashMap<>(entityStatUpdatesCount);

            for (int i = 0; i < entityStatUpdatesCount; i++) {
               int key = buf.getIntLE(pos);
               pos += 4;
               int valLen = VarInt.peek(buf, pos);
               if (valLen < 0) {
                  throw ProtocolException.invalidVarInt("val");
               }

               int valVarLen = VarInt.size(valLen);
               if (valLen > 64) {
                  throw ProtocolException.arrayTooLong("val", valLen, 64);
               }

               if (pos + valVarLen + valLen * 13L > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("val", pos + valVarLen + valLen * 13, buf.readableBytes());
               }

               pos += valVarLen;
               EntityStatUpdate[] val = new EntityStatUpdate[valLen];

               for (int valIdx = 0; valIdx < valLen; valIdx++) {
                  val[valIdx] = EntityStatUpdate.deserialize(buf, pos);
                  pos += EntityStatUpdate.computeBytesConsumed(buf, pos);
               }

               if (obj.entityStatUpdates.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("entityStatUpdates", key);
               }
            }

            return obj;
         }
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      int pos = offset + 0;
      int dictLen = VarInt.peek(buf, pos);
      pos += VarInt.size(dictLen);

      for (int i = 0; i < dictLen; i++) {
         pos += 4;
         int al = VarInt.peek(buf, pos);
         pos += VarInt.size(al);

         for (int j = 0; j < al; j++) {
            pos += EntityStatUpdate.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 0L;
   }

   public static Map<Integer, EntityStatUpdate[]> getEntityStatUpdates(MemorySegment mem) {
      return getEntityStatUpdates(mem, 0);
   }

   public static Map<Integer, EntityStatUpdate[]> getEntityStatUpdates(MemorySegment mem, int offset) {
      int off = offset + 0;
      long packed = VarInt.getWithLength(mem, off);
      int len = (int)packed;
      if (len < 0) {
         throw ProtocolException.negativeLength("EntityStatUpdates", len);
      } else if (len > 4096000) {
         throw ProtocolException.dictionaryTooLarge("EntityStatUpdates", len, 4096000);
      } else {
         Map<Integer, EntityStatUpdate[]> data = new HashMap<>(len);
         off += (int)(packed >>> 32);

         for (int i = 0; i < len; i++) {
            int key = mem.get(PacketIO.PROTO_INT, (long)off);
            off += 4;
            long valuePacked = VarInt.getWithLength(mem, off);
            int valueLen = (int)valuePacked;
            int valueVarLen = (int)(valuePacked >>> 32);
            if (valueLen < 0) {
               throw ProtocolException.negativeLength("value", valueLen);
            }

            if (valueLen > 64) {
               throw ProtocolException.arrayTooLong("value", valueLen, 64);
            }

            if (off + valueVarLen + valueLen * 13L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("value", off + valueVarLen + valueLen * 13, (int)mem.byteSize());
            }

            off += valueVarLen;
            EntityStatUpdate[] value = new EntityStatUpdate[valueLen];

            for (int valueIdx = 0; valueIdx < valueLen; valueIdx++) {
               value[valueIdx] = EntityStatUpdate.toObject(mem, off);
               off += value[valueIdx].computeSize();
            }

            if (data.put(key, value) != null) {
               throw ProtocolException.duplicateKey("EntityStatUpdates", key);
            }
         }

         return data;
      }
   }

   public static EntityStatsUpdate toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static EntityStatsUpdate toObject(MemorySegment mem, int offset) {
      if (offset + 0 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("EntityStatsUpdate", offset + 0, (int)mem.byteSize());
      } else {
         int off = offset + 0;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("EntityStatUpdates", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("EntityStatUpdates", len, 4096000);
         } else {
            Map<Integer, EntityStatUpdate[]> entityStatUpdates = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               long valuePacked = VarInt.getWithLength(mem, off);
               int valueLen = (int)valuePacked;
               int valueVarLen = (int)(valuePacked >>> 32);
               if (valueLen < 0) {
                  throw ProtocolException.negativeLength("value", valueLen);
               }

               if (valueLen > 64) {
                  throw ProtocolException.arrayTooLong("value", valueLen, 64);
               }

               if (off + valueVarLen + valueLen * 13L > mem.byteSize()) {
                  throw ProtocolException.bufferTooSmall("value", off + valueVarLen + valueLen * 13, (int)mem.byteSize());
               }

               off += valueVarLen;
               EntityStatUpdate[] value = new EntityStatUpdate[valueLen];

               for (int valueIdx = 0; valueIdx < valueLen; valueIdx++) {
                  value[valueIdx] = EntityStatUpdate.toObject(mem, off);
                  off += value[valueIdx].computeSize();
               }

               if (entityStatUpdates.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("EntityStatUpdates", key);
               }
            }

            return new EntityStatsUpdate(entityStatUpdates);
         }
      }
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      if (this.entityStatUpdates.size() > 4096000) {
         throw ProtocolException.dictionaryTooLarge("EntityStatUpdates", this.entityStatUpdates.size(), 4096000);
      } else {
         VarInt.write(buf, this.entityStatUpdates.size());

         for (Entry<Integer, EntityStatUpdate[]> e : this.entityStatUpdates.entrySet()) {
            buf.writeIntLE(e.getKey());
            VarInt.write(buf, e.getValue().length);

            for (EntityStatUpdate arrItem : e.getValue()) {
               arrItem.serialize(buf);
            }
         }

         return buf.writerIndex() - startPos;
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      int varOffset = offset + 0;
      if (this.entityStatUpdates.size() > 4096000) {
         throw ProtocolException.dictionaryTooLarge("EntityStatUpdates", this.entityStatUpdates.size(), 4096000);
      } else {
         varOffset += VarInt.set(mem, varOffset, this.entityStatUpdates.size());

         for (Entry<Integer, EntityStatUpdate[]> e : this.entityStatUpdates.entrySet()) {
            mem.set(PacketIO.PROTO_INT, (long)varOffset, e.getKey());
            varOffset += 4;
            varOffset += VarInt.set(mem, varOffset, e.getValue().length);

            for (EntityStatUpdate arrItem : e.getValue()) {
               varOffset += arrItem.serialize(mem, varOffset);
            }
         }

         return varOffset - offset;
      }
   }

   @Override
   public int computeSize() {
      int size = 0;
      int entityStatUpdatesSize = 0;

      for (Entry<Integer, EntityStatUpdate[]> kvp : this.entityStatUpdates.entrySet()) {
         entityStatUpdatesSize += 4 + VarInt.size(kvp.getValue().length) + Arrays.stream(kvp.getValue()).mapToInt(inner -> inner.computeSize()).sum();
      }

      return size + VarInt.size(this.entityStatUpdates.size()) + entityStatUpdatesSize;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 0) {
         return ValidationResult.error("Buffer too small: expected at least 0 bytes");
      } else {
         int pos = offset + 0;
         int entityStatUpdatesCount = VarInt.peek(buffer, pos);
         if (entityStatUpdatesCount < 0) {
            return ValidationResult.error("Invalid dictionary count for EntityStatUpdates");
         } else if (entityStatUpdatesCount > 4096000) {
            return ValidationResult.error("EntityStatUpdates exceeds max length 4096000");
         } else {
            pos += VarInt.size(entityStatUpdatesCount);

            for (int i = 0; i < entityStatUpdatesCount; i++) {
               pos += 4;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               int valueArrCount = VarInt.peek(buffer, pos);
               if (valueArrCount < 0) {
                  return ValidationResult.error("Invalid array count for value");
               }

               pos += VarInt.size(valueArrCount);

               for (int valueArrIdx = 0; valueArrIdx < valueArrCount; valueArrIdx++) {
                  pos += EntityStatUpdate.computeBytesConsumed(buffer, pos);
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public EntityStatsUpdate clone() {
      EntityStatsUpdate copy = new EntityStatsUpdate();
      Map<Integer, EntityStatUpdate[]> m = new HashMap<>();

      for (Entry<Integer, EntityStatUpdate[]> e : this.entityStatUpdates.entrySet()) {
         m.put(e.getKey(), Arrays.stream(e.getValue()).map(x -> x.clone()).toArray(EntityStatUpdate[]::new));
      }

      copy.entityStatUpdates = m;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof EntityStatsUpdate other ? Objects.equals(this.entityStatUpdates, other.entityStatUpdates) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.entityStatUpdates);
   }
}
