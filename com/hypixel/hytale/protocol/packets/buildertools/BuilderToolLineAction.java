package com.hypixel.hytale.protocol.packets.buildertools;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class BuilderToolLineAction implements Packet, ToServerPacket {
   public static final int PACKET_ID = 414;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 24;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 24;
   public static final int MAX_SIZE = 24;
   public int xStart;
   public int yStart;
   public int zStart;
   public int xEnd;
   public int yEnd;
   public int zEnd;

   @Override
   public int getId() {
      return 414;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public BuilderToolLineAction() {
   }

   public BuilderToolLineAction(int xStart, int yStart, int zStart, int xEnd, int yEnd, int zEnd) {
      this.xStart = xStart;
      this.yStart = yStart;
      this.zStart = zStart;
      this.xEnd = xEnd;
      this.yEnd = yEnd;
      this.zEnd = zEnd;
   }

   public BuilderToolLineAction(@Nonnull BuilderToolLineAction other) {
      this.xStart = other.xStart;
      this.yStart = other.yStart;
      this.zStart = other.zStart;
      this.xEnd = other.xEnd;
      this.yEnd = other.yEnd;
      this.zEnd = other.zEnd;
   }

   @Nonnull
   public static BuilderToolLineAction deserialize(@Nonnull ByteBuf buf, int offset) {
      BuilderToolLineAction obj = new BuilderToolLineAction();
      obj.xStart = buf.getIntLE(offset + 0);
      obj.yStart = buf.getIntLE(offset + 4);
      obj.zStart = buf.getIntLE(offset + 8);
      obj.xEnd = buf.getIntLE(offset + 12);
      obj.yEnd = buf.getIntLE(offset + 16);
      obj.zEnd = buf.getIntLE(offset + 20);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 24;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.xStart);
      buf.writeIntLE(this.yStart);
      buf.writeIntLE(this.zStart);
      buf.writeIntLE(this.xEnd);
      buf.writeIntLE(this.yEnd);
      buf.writeIntLE(this.zEnd);
   }

   @Override
   public int computeSize() {
      return 24;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 24 ? ValidationResult.error("Buffer too small: expected at least 24 bytes") : ValidationResult.OK;
   }

   public BuilderToolLineAction clone() {
      BuilderToolLineAction copy = new BuilderToolLineAction();
      copy.xStart = this.xStart;
      copy.yStart = this.yStart;
      copy.zStart = this.zStart;
      copy.xEnd = this.xEnd;
      copy.yEnd = this.yEnd;
      copy.zEnd = this.zEnd;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BuilderToolLineAction other)
            ? false
            : this.xStart == other.xStart
               && this.yStart == other.yStart
               && this.zStart == other.zStart
               && this.xEnd == other.xEnd
               && this.yEnd == other.yEnd
               && this.zEnd == other.zEnd;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.xStart, this.yStart, this.zStart, this.xEnd, this.yEnd, this.zEnd);
   }
}
