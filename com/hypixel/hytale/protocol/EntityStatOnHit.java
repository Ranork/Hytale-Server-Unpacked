package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityStatOnHit {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 13;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 13;
   public static final int MAX_SIZE = 16384018;
   public int entityStatIndex;
   public float amount;
   @Nullable
   public float[] multipliersPerEntitiesHit;
   public float multiplierPerExtraEntityHit;

   public EntityStatOnHit() {
   }

   public EntityStatOnHit(int entityStatIndex, float amount, @Nullable float[] multipliersPerEntitiesHit, float multiplierPerExtraEntityHit) {
      this.entityStatIndex = entityStatIndex;
      this.amount = amount;
      this.multipliersPerEntitiesHit = multipliersPerEntitiesHit;
      this.multiplierPerExtraEntityHit = multiplierPerExtraEntityHit;
   }

   public EntityStatOnHit(@Nonnull EntityStatOnHit other) {
      this.entityStatIndex = other.entityStatIndex;
      this.amount = other.amount;
      this.multipliersPerEntitiesHit = other.multipliersPerEntitiesHit;
      this.multiplierPerExtraEntityHit = other.multiplierPerExtraEntityHit;
   }

   @Nonnull
   public static EntityStatOnHit deserialize(@Nonnull ByteBuf buf, int offset) {
      EntityStatOnHit obj = new EntityStatOnHit();
      byte nullBits = buf.getByte(offset);
      obj.entityStatIndex = buf.getIntLE(offset + 1);
      obj.amount = buf.getFloatLE(offset + 5);
      obj.multiplierPerExtraEntityHit = buf.getFloatLE(offset + 9);
      int pos = offset + 13;
      if ((nullBits & 1) != 0) {
         int multipliersPerEntitiesHitCount = VarInt.peek(buf, pos);
         if (multipliersPerEntitiesHitCount < 0) {
            throw ProtocolException.negativeLength("MultipliersPerEntitiesHit", multipliersPerEntitiesHitCount);
         }

         if (multipliersPerEntitiesHitCount > 4096000) {
            throw ProtocolException.arrayTooLong("MultipliersPerEntitiesHit", multipliersPerEntitiesHitCount, 4096000);
         }

         int multipliersPerEntitiesHitVarLen = VarInt.size(multipliersPerEntitiesHitCount);
         if (pos + multipliersPerEntitiesHitVarLen + multipliersPerEntitiesHitCount * 4L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall(
               "MultipliersPerEntitiesHit", pos + multipliersPerEntitiesHitVarLen + multipliersPerEntitiesHitCount * 4, buf.readableBytes()
            );
         }

         pos += multipliersPerEntitiesHitVarLen;
         obj.multipliersPerEntitiesHit = new float[multipliersPerEntitiesHitCount];

         for (int i = 0; i < multipliersPerEntitiesHitCount; i++) {
            obj.multipliersPerEntitiesHit[i] = buf.getFloatLE(pos + i * 4);
         }

         pos += multipliersPerEntitiesHitCount * 4;
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 13;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos) + arrLen * 4;
      }

      return pos - offset;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.multipliersPerEntitiesHit != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.entityStatIndex);
      buf.writeFloatLE(this.amount);
      buf.writeFloatLE(this.multiplierPerExtraEntityHit);
      if (this.multipliersPerEntitiesHit != null) {
         if (this.multipliersPerEntitiesHit.length > 4096000) {
            throw ProtocolException.arrayTooLong("MultipliersPerEntitiesHit", this.multipliersPerEntitiesHit.length, 4096000);
         }

         VarInt.write(buf, this.multipliersPerEntitiesHit.length);

         for (float item : this.multipliersPerEntitiesHit) {
            buf.writeFloatLE(item);
         }
      }
   }

   public int computeSize() {
      int size = 13;
      if (this.multipliersPerEntitiesHit != null) {
         size += VarInt.size(this.multipliersPerEntitiesHit.length) + this.multipliersPerEntitiesHit.length * 4;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 13) {
         return ValidationResult.error("Buffer too small: expected at least 13 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 13;
         if ((nullBits & 1) != 0) {
            int multipliersPerEntitiesHitCount = VarInt.peek(buffer, pos);
            if (multipliersPerEntitiesHitCount < 0) {
               return ValidationResult.error("Invalid array count for MultipliersPerEntitiesHit");
            }

            if (multipliersPerEntitiesHitCount > 4096000) {
               return ValidationResult.error("MultipliersPerEntitiesHit exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += multipliersPerEntitiesHitCount * 4;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading MultipliersPerEntitiesHit");
            }
         }

         return ValidationResult.OK;
      }
   }

   public EntityStatOnHit clone() {
      EntityStatOnHit copy = new EntityStatOnHit();
      copy.entityStatIndex = this.entityStatIndex;
      copy.amount = this.amount;
      copy.multipliersPerEntitiesHit = this.multipliersPerEntitiesHit != null
         ? Arrays.copyOf(this.multipliersPerEntitiesHit, this.multipliersPerEntitiesHit.length)
         : null;
      copy.multiplierPerExtraEntityHit = this.multiplierPerExtraEntityHit;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof EntityStatOnHit other)
            ? false
            : this.entityStatIndex == other.entityStatIndex
               && this.amount == other.amount
               && Arrays.equals(this.multipliersPerEntitiesHit, other.multipliersPerEntitiesHit)
               && this.multiplierPerExtraEntityHit == other.multiplierPerExtraEntityHit;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Integer.hashCode(this.entityStatIndex);
      result = 31 * result + Float.hashCode(this.amount);
      result = 31 * result + Arrays.hashCode(this.multipliersPerEntitiesHit);
      return 31 * result + Float.hashCode(this.multiplierPerExtraEntityHit);
   }
}
