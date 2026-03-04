package com.hypixel.hytale.protocol.packets.world;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class SetFluidCmd {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 7;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 7;
   public static final int MAX_SIZE = 7;
   public short index;
   public int fluidId;
   public byte fluidLevel;

   public SetFluidCmd() {
   }

   public SetFluidCmd(short index, int fluidId, byte fluidLevel) {
      this.index = index;
      this.fluidId = fluidId;
      this.fluidLevel = fluidLevel;
   }

   public SetFluidCmd(@Nonnull SetFluidCmd other) {
      this.index = other.index;
      this.fluidId = other.fluidId;
      this.fluidLevel = other.fluidLevel;
   }

   @Nonnull
   public static SetFluidCmd deserialize(@Nonnull ByteBuf buf, int offset) {
      SetFluidCmd obj = new SetFluidCmd();
      obj.index = buf.getShortLE(offset + 0);
      obj.fluidId = buf.getIntLE(offset + 2);
      obj.fluidLevel = buf.getByte(offset + 6);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 7;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeShortLE(this.index);
      buf.writeIntLE(this.fluidId);
      buf.writeByte(this.fluidLevel);
   }

   public int computeSize() {
      return 7;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 7 ? ValidationResult.error("Buffer too small: expected at least 7 bytes") : ValidationResult.OK;
   }

   public SetFluidCmd clone() {
      SetFluidCmd copy = new SetFluidCmd();
      copy.index = this.index;
      copy.fluidId = this.fluidId;
      copy.fluidLevel = this.fluidLevel;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof SetFluidCmd other) ? false : this.index == other.index && this.fluidId == other.fluidId && this.fluidLevel == other.fluidLevel;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.index, this.fluidId, this.fluidLevel);
   }
}
