package com.hypixel.hytale.protocol.packets.buildertools;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class BuilderToolSelectionUpdate implements Packet, ToServerPacket {
   public static final int PACKET_ID = 409;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 24;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 24;
   public static final int MAX_SIZE = 24;
   public int xMin;
   public int yMin;
   public int zMin;
   public int xMax;
   public int yMax;
   public int zMax;

   @Override
   public int getId() {
      return 409;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public BuilderToolSelectionUpdate() {
   }

   public BuilderToolSelectionUpdate(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax) {
      this.xMin = xMin;
      this.yMin = yMin;
      this.zMin = zMin;
      this.xMax = xMax;
      this.yMax = yMax;
      this.zMax = zMax;
   }

   public BuilderToolSelectionUpdate(@Nonnull BuilderToolSelectionUpdate other) {
      this.xMin = other.xMin;
      this.yMin = other.yMin;
      this.zMin = other.zMin;
      this.xMax = other.xMax;
      this.yMax = other.yMax;
      this.zMax = other.zMax;
   }

   @Nonnull
   public static BuilderToolSelectionUpdate deserialize(@Nonnull ByteBuf buf, int offset) {
      BuilderToolSelectionUpdate obj = new BuilderToolSelectionUpdate();
      obj.xMin = buf.getIntLE(offset + 0);
      obj.yMin = buf.getIntLE(offset + 4);
      obj.zMin = buf.getIntLE(offset + 8);
      obj.xMax = buf.getIntLE(offset + 12);
      obj.yMax = buf.getIntLE(offset + 16);
      obj.zMax = buf.getIntLE(offset + 20);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 24;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.xMin);
      buf.writeIntLE(this.yMin);
      buf.writeIntLE(this.zMin);
      buf.writeIntLE(this.xMax);
      buf.writeIntLE(this.yMax);
      buf.writeIntLE(this.zMax);
   }

   @Override
   public int computeSize() {
      return 24;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 24 ? ValidationResult.error("Buffer too small: expected at least 24 bytes") : ValidationResult.OK;
   }

   public BuilderToolSelectionUpdate clone() {
      BuilderToolSelectionUpdate copy = new BuilderToolSelectionUpdate();
      copy.xMin = this.xMin;
      copy.yMin = this.yMin;
      copy.zMin = this.zMin;
      copy.xMax = this.xMax;
      copy.yMax = this.yMax;
      copy.zMax = this.zMax;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BuilderToolSelectionUpdate other)
            ? false
            : this.xMin == other.xMin
               && this.yMin == other.yMin
               && this.zMin == other.zMin
               && this.xMax == other.xMax
               && this.yMax == other.yMax
               && this.zMax == other.zMax;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.xMin, this.yMin, this.zMin, this.xMax, this.yMax, this.zMax);
   }
}
