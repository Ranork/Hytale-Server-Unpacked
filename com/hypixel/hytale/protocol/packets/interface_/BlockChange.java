package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class BlockChange {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 17;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 17;
   public static final int MAX_SIZE = 17;
   public int x;
   public int y;
   public int z;
   public int block;
   public byte rotation;

   public BlockChange() {
   }

   public BlockChange(int x, int y, int z, int block, byte rotation) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.block = block;
      this.rotation = rotation;
   }

   public BlockChange(@Nonnull BlockChange other) {
      this.x = other.x;
      this.y = other.y;
      this.z = other.z;
      this.block = other.block;
      this.rotation = other.rotation;
   }

   @Nonnull
   public static BlockChange deserialize(@Nonnull ByteBuf buf, int offset) {
      BlockChange obj = new BlockChange();
      obj.x = buf.getIntLE(offset + 0);
      obj.y = buf.getIntLE(offset + 4);
      obj.z = buf.getIntLE(offset + 8);
      obj.block = buf.getIntLE(offset + 12);
      obj.rotation = buf.getByte(offset + 16);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 17;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.x);
      buf.writeIntLE(this.y);
      buf.writeIntLE(this.z);
      buf.writeIntLE(this.block);
      buf.writeByte(this.rotation);
   }

   public int computeSize() {
      return 17;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 17 ? ValidationResult.error("Buffer too small: expected at least 17 bytes") : ValidationResult.OK;
   }

   public BlockChange clone() {
      BlockChange copy = new BlockChange();
      copy.x = this.x;
      copy.y = this.y;
      copy.z = this.z;
      copy.block = this.block;
      copy.rotation = this.rotation;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BlockChange other)
            ? false
            : this.x == other.x && this.y == other.y && this.z == other.z && this.block == other.block && this.rotation == other.rotation;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.x, this.y, this.z, this.block, this.rotation);
   }
}
