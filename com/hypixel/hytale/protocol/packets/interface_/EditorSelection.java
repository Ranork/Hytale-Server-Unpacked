package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class EditorSelection {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 24;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 24;
   public static final int MAX_SIZE = 24;
   public int minX;
   public int minY;
   public int minZ;
   public int maxX;
   public int maxY;
   public int maxZ;

   public EditorSelection() {
   }

   public EditorSelection(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
      this.minX = minX;
      this.minY = minY;
      this.minZ = minZ;
      this.maxX = maxX;
      this.maxY = maxY;
      this.maxZ = maxZ;
   }

   public EditorSelection(@Nonnull EditorSelection other) {
      this.minX = other.minX;
      this.minY = other.minY;
      this.minZ = other.minZ;
      this.maxX = other.maxX;
      this.maxY = other.maxY;
      this.maxZ = other.maxZ;
   }

   @Nonnull
   public static EditorSelection deserialize(@Nonnull ByteBuf buf, int offset) {
      EditorSelection obj = new EditorSelection();
      obj.minX = buf.getIntLE(offset + 0);
      obj.minY = buf.getIntLE(offset + 4);
      obj.minZ = buf.getIntLE(offset + 8);
      obj.maxX = buf.getIntLE(offset + 12);
      obj.maxY = buf.getIntLE(offset + 16);
      obj.maxZ = buf.getIntLE(offset + 20);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 24;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.minX);
      buf.writeIntLE(this.minY);
      buf.writeIntLE(this.minZ);
      buf.writeIntLE(this.maxX);
      buf.writeIntLE(this.maxY);
      buf.writeIntLE(this.maxZ);
   }

   public int computeSize() {
      return 24;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 24 ? ValidationResult.error("Buffer too small: expected at least 24 bytes") : ValidationResult.OK;
   }

   public EditorSelection clone() {
      EditorSelection copy = new EditorSelection();
      copy.minX = this.minX;
      copy.minY = this.minY;
      copy.minZ = this.minZ;
      copy.maxX = this.maxX;
      copy.maxY = this.maxY;
      copy.maxZ = this.maxZ;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof EditorSelection other)
            ? false
            : this.minX == other.minX
               && this.minY == other.minY
               && this.minZ == other.minZ
               && this.maxX == other.maxX
               && this.maxY == other.maxY
               && this.maxZ == other.maxZ;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
   }
}
