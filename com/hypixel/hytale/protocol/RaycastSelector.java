package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RaycastSelector extends Selector {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 23;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 23;
   public static final int MAX_SIZE = 23;
   @Nullable
   public Vector3f offset;
   public int distance;
   public int blockTagIndex = Integer.MIN_VALUE;
   public boolean ignoreFluids;
   public boolean ignoreEmptyCollisionMaterial;

   public RaycastSelector() {
   }

   public RaycastSelector(@Nullable Vector3f offset, int distance, int blockTagIndex, boolean ignoreFluids, boolean ignoreEmptyCollisionMaterial) {
      this.offset = offset;
      this.distance = distance;
      this.blockTagIndex = blockTagIndex;
      this.ignoreFluids = ignoreFluids;
      this.ignoreEmptyCollisionMaterial = ignoreEmptyCollisionMaterial;
   }

   public RaycastSelector(@Nonnull RaycastSelector other) {
      this.offset = other.offset;
      this.distance = other.distance;
      this.blockTagIndex = other.blockTagIndex;
      this.ignoreFluids = other.ignoreFluids;
      this.ignoreEmptyCollisionMaterial = other.ignoreEmptyCollisionMaterial;
   }

   @Nonnull
   public static RaycastSelector deserialize(@Nonnull ByteBuf buf, int offset) {
      RaycastSelector obj = new RaycastSelector();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         obj.offset = Vector3f.deserialize(buf, offset + 1);
      }

      obj.distance = buf.getIntLE(offset + 13);
      obj.blockTagIndex = buf.getIntLE(offset + 17);
      obj.ignoreFluids = buf.getByte(offset + 21) != 0;
      obj.ignoreEmptyCollisionMaterial = buf.getByte(offset + 22) != 0;
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 23;
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.offset != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.offset != null) {
         this.offset.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      buf.writeIntLE(this.distance);
      buf.writeIntLE(this.blockTagIndex);
      buf.writeByte(this.ignoreFluids ? 1 : 0);
      buf.writeByte(this.ignoreEmptyCollisionMaterial ? 1 : 0);
      return buf.writerIndex() - startPos;
   }

   @Override
   public int computeSize() {
      return 23;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 23 ? ValidationResult.error("Buffer too small: expected at least 23 bytes") : ValidationResult.OK;
   }

   public RaycastSelector clone() {
      RaycastSelector copy = new RaycastSelector();
      copy.offset = this.offset != null ? this.offset.clone() : null;
      copy.distance = this.distance;
      copy.blockTagIndex = this.blockTagIndex;
      copy.ignoreFluids = this.ignoreFluids;
      copy.ignoreEmptyCollisionMaterial = this.ignoreEmptyCollisionMaterial;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof RaycastSelector other)
            ? false
            : Objects.equals(this.offset, other.offset)
               && this.distance == other.distance
               && this.blockTagIndex == other.blockTagIndex
               && this.ignoreFluids == other.ignoreFluids
               && this.ignoreEmptyCollisionMaterial == other.ignoreEmptyCollisionMaterial;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.offset, this.distance, this.blockTagIndex, this.ignoreFluids, this.ignoreEmptyCollisionMaterial);
   }
}
