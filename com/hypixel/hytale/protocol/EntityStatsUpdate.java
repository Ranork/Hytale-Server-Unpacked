package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
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
         throw ProtocolException.negativeLength("EntityStatUpdates", entityStatUpdatesCount);
      } else if (entityStatUpdatesCount > 4096000) {
         throw ProtocolException.dictionaryTooLarge("EntityStatUpdates", entityStatUpdatesCount, 4096000);
      } else {
         pos += VarInt.size(entityStatUpdatesCount);
         obj.entityStatUpdates = new HashMap<>(entityStatUpdatesCount);

         for (int i = 0; i < entityStatUpdatesCount; i++) {
            int key = buf.getIntLE(pos);
            pos += 4;
            int valLen = VarInt.peek(buf, pos);
            if (valLen < 0) {
               throw ProtocolException.negativeLength("val", valLen);
            }

            if (valLen > 64) {
               throw ProtocolException.arrayTooLong("val", valLen, 64);
            }

            int valVarLen = VarInt.length(buf, pos);
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

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      int pos = offset + 0;
      int dictLen = VarInt.peek(buf, pos);
      pos += VarInt.length(buf, pos);

      for (int i = 0; i < dictLen; i++) {
         pos += 4;
         int al = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos);

         for (int j = 0; j < al; j++) {
            pos += EntityStatUpdate.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
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
            pos += VarInt.length(buffer, pos);

            for (int i = 0; i < entityStatUpdatesCount; i++) {
               pos += 4;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               int valueArrCount = VarInt.peek(buffer, pos);
               if (valueArrCount < 0) {
                  return ValidationResult.error("Invalid array count for value");
               }

               pos += VarInt.length(buffer, pos);

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
