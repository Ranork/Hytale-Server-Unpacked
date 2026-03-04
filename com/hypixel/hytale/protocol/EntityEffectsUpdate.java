package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class EntityEffectsUpdate extends ComponentUpdate {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 0;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 0;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public EntityEffectUpdate[] entityEffectUpdates = new EntityEffectUpdate[0];

   public EntityEffectsUpdate() {
   }

   public EntityEffectsUpdate(@Nonnull EntityEffectUpdate[] entityEffectUpdates) {
      this.entityEffectUpdates = entityEffectUpdates;
   }

   public EntityEffectsUpdate(@Nonnull EntityEffectsUpdate other) {
      this.entityEffectUpdates = other.entityEffectUpdates;
   }

   @Nonnull
   public static EntityEffectsUpdate deserialize(@Nonnull ByteBuf buf, int offset) {
      EntityEffectsUpdate obj = new EntityEffectsUpdate();
      int pos = offset + 0;
      int entityEffectUpdatesCount = VarInt.peek(buf, pos);
      if (entityEffectUpdatesCount < 0) {
         throw ProtocolException.negativeLength("EntityEffectUpdates", entityEffectUpdatesCount);
      } else if (entityEffectUpdatesCount > 4096000) {
         throw ProtocolException.arrayTooLong("EntityEffectUpdates", entityEffectUpdatesCount, 4096000);
      } else {
         int entityEffectUpdatesVarLen = VarInt.size(entityEffectUpdatesCount);
         if (pos + entityEffectUpdatesVarLen + entityEffectUpdatesCount * 12L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("EntityEffectUpdates", pos + entityEffectUpdatesVarLen + entityEffectUpdatesCount * 12, buf.readableBytes());
         } else {
            pos += entityEffectUpdatesVarLen;
            obj.entityEffectUpdates = new EntityEffectUpdate[entityEffectUpdatesCount];

            for (int i = 0; i < entityEffectUpdatesCount; i++) {
               obj.entityEffectUpdates[i] = EntityEffectUpdate.deserialize(buf, pos);
               pos += EntityEffectUpdate.computeBytesConsumed(buf, pos);
            }

            return obj;
         }
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      int pos = offset + 0;
      int arrLen = VarInt.peek(buf, pos);
      pos += VarInt.length(buf, pos);

      for (int i = 0; i < arrLen; i++) {
         pos += EntityEffectUpdate.computeBytesConsumed(buf, pos);
      }

      return pos - offset;
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      if (this.entityEffectUpdates.length > 4096000) {
         throw ProtocolException.arrayTooLong("EntityEffectUpdates", this.entityEffectUpdates.length, 4096000);
      } else {
         VarInt.write(buf, this.entityEffectUpdates.length);

         for (EntityEffectUpdate item : this.entityEffectUpdates) {
            item.serialize(buf);
         }

         return buf.writerIndex() - startPos;
      }
   }

   @Override
   public int computeSize() {
      int size = 0;
      int entityEffectUpdatesSize = 0;

      for (EntityEffectUpdate elem : this.entityEffectUpdates) {
         entityEffectUpdatesSize += elem.computeSize();
      }

      return size + VarInt.size(this.entityEffectUpdates.length) + entityEffectUpdatesSize;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 0) {
         return ValidationResult.error("Buffer too small: expected at least 0 bytes");
      } else {
         int pos = offset + 0;
         int entityEffectUpdatesCount = VarInt.peek(buffer, pos);
         if (entityEffectUpdatesCount < 0) {
            return ValidationResult.error("Invalid array count for EntityEffectUpdates");
         } else if (entityEffectUpdatesCount > 4096000) {
            return ValidationResult.error("EntityEffectUpdates exceeds max length 4096000");
         } else {
            pos += VarInt.length(buffer, pos);

            for (int i = 0; i < entityEffectUpdatesCount; i++) {
               ValidationResult structResult = EntityEffectUpdate.validateStructure(buffer, pos);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid EntityEffectUpdate in EntityEffectUpdates[" + i + "]: " + structResult.error());
               }

               pos += EntityEffectUpdate.computeBytesConsumed(buffer, pos);
            }

            return ValidationResult.OK;
         }
      }
   }

   public EntityEffectsUpdate clone() {
      EntityEffectsUpdate copy = new EntityEffectsUpdate();
      copy.entityEffectUpdates = Arrays.stream(this.entityEffectUpdates).map(e -> e.clone()).toArray(EntityEffectUpdate[]::new);
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof EntityEffectsUpdate other ? Arrays.equals((Object[])this.entityEffectUpdates, (Object[])other.entityEffectUpdates) : false;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      return 31 * result + Arrays.hashCode((Object[])this.entityEffectUpdates);
   }
}
