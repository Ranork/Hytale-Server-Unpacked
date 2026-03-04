package com.hypixel.hytale.protocol.packets.world;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class SetBlockCmd {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 9;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 9;
   public short index;
   public int blockId;
   public short filler;
   public byte rotation;

   public SetBlockCmd() {
   }

   public SetBlockCmd(short index, int blockId, short filler, byte rotation) {
      this.index = index;
      this.blockId = blockId;
      this.filler = filler;
      this.rotation = rotation;
   }

   public SetBlockCmd(@Nonnull SetBlockCmd other) {
      this.index = other.index;
      this.blockId = other.blockId;
      this.filler = other.filler;
      this.rotation = other.rotation;
   }

   @Nonnull
   public static SetBlockCmd deserialize(@Nonnull ByteBuf buf, int offset) {
      SetBlockCmd obj = new SetBlockCmd();
      obj.index = buf.getShortLE(offset + 0);
      obj.blockId = buf.getIntLE(offset + 2);
      obj.filler = buf.getShortLE(offset + 6);
      obj.rotation = buf.getByte(offset + 8);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 9;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeShortLE(this.index);
      buf.writeIntLE(this.blockId);
      buf.writeShortLE(this.filler);
      buf.writeByte(this.rotation);
   }

   public int computeSize() {
      return 9;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 9 ? ValidationResult.error("Buffer too small: expected at least 9 bytes") : ValidationResult.OK;
   }

   public SetBlockCmd clone() {
      SetBlockCmd copy = new SetBlockCmd();
      copy.index = this.index;
      copy.blockId = this.blockId;
      copy.filler = this.filler;
      copy.rotation = this.rotation;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof SetBlockCmd other)
            ? false
            : this.index == other.index && this.blockId == other.blockId && this.filler == other.filler && this.rotation == other.rotation;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.index, this.blockId, this.filler, this.rotation);
   }
}
