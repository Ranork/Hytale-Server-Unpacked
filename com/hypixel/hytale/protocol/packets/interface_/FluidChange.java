package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class FluidChange {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 17;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 17;
   public static final int MAX_SIZE = 17;
   public int x;
   public int y;
   public int z;
   public int fluidId;
   public byte fluidLevel;

   public FluidChange() {
   }

   public FluidChange(int x, int y, int z, int fluidId, byte fluidLevel) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.fluidId = fluidId;
      this.fluidLevel = fluidLevel;
   }

   public FluidChange(@Nonnull FluidChange other) {
      this.x = other.x;
      this.y = other.y;
      this.z = other.z;
      this.fluidId = other.fluidId;
      this.fluidLevel = other.fluidLevel;
   }

   @Nonnull
   public static FluidChange deserialize(@Nonnull ByteBuf buf, int offset) {
      FluidChange obj = new FluidChange();
      obj.x = buf.getIntLE(offset + 0);
      obj.y = buf.getIntLE(offset + 4);
      obj.z = buf.getIntLE(offset + 8);
      obj.fluidId = buf.getIntLE(offset + 12);
      obj.fluidLevel = buf.getByte(offset + 16);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 17;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.x);
      buf.writeIntLE(this.y);
      buf.writeIntLE(this.z);
      buf.writeIntLE(this.fluidId);
      buf.writeByte(this.fluidLevel);
   }

   public int computeSize() {
      return 17;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 17 ? ValidationResult.error("Buffer too small: expected at least 17 bytes") : ValidationResult.OK;
   }

   public FluidChange clone() {
      FluidChange copy = new FluidChange();
      copy.x = this.x;
      copy.y = this.y;
      copy.z = this.z;
      copy.fluidId = this.fluidId;
      copy.fluidLevel = this.fluidLevel;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof FluidChange other)
            ? false
            : this.x == other.x && this.y == other.y && this.z == other.z && this.fluidId == other.fluidId && this.fluidLevel == other.fluidLevel;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.x, this.y, this.z, this.fluidId, this.fluidLevel);
   }
}
