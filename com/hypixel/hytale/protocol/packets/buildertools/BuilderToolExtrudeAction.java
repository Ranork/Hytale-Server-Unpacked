package com.hypixel.hytale.protocol.packets.buildertools;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class BuilderToolExtrudeAction implements Packet, ToServerPacket {
   public static final int PACKET_ID = 403;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 24;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 24;
   public static final int MAX_SIZE = 24;
   public int x;
   public int y;
   public int z;
   public int xNormal;
   public int yNormal;
   public int zNormal;

   @Override
   public int getId() {
      return 403;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public BuilderToolExtrudeAction() {
   }

   public BuilderToolExtrudeAction(int x, int y, int z, int xNormal, int yNormal, int zNormal) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.xNormal = xNormal;
      this.yNormal = yNormal;
      this.zNormal = zNormal;
   }

   public BuilderToolExtrudeAction(@Nonnull BuilderToolExtrudeAction other) {
      this.x = other.x;
      this.y = other.y;
      this.z = other.z;
      this.xNormal = other.xNormal;
      this.yNormal = other.yNormal;
      this.zNormal = other.zNormal;
   }

   @Nonnull
   public static BuilderToolExtrudeAction deserialize(@Nonnull ByteBuf buf, int offset) {
      BuilderToolExtrudeAction obj = new BuilderToolExtrudeAction();
      obj.x = buf.getIntLE(offset + 0);
      obj.y = buf.getIntLE(offset + 4);
      obj.z = buf.getIntLE(offset + 8);
      obj.xNormal = buf.getIntLE(offset + 12);
      obj.yNormal = buf.getIntLE(offset + 16);
      obj.zNormal = buf.getIntLE(offset + 20);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 24;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.x);
      buf.writeIntLE(this.y);
      buf.writeIntLE(this.z);
      buf.writeIntLE(this.xNormal);
      buf.writeIntLE(this.yNormal);
      buf.writeIntLE(this.zNormal);
   }

   @Override
   public int computeSize() {
      return 24;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 24 ? ValidationResult.error("Buffer too small: expected at least 24 bytes") : ValidationResult.OK;
   }

   public BuilderToolExtrudeAction clone() {
      BuilderToolExtrudeAction copy = new BuilderToolExtrudeAction();
      copy.x = this.x;
      copy.y = this.y;
      copy.z = this.z;
      copy.xNormal = this.xNormal;
      copy.yNormal = this.yNormal;
      copy.zNormal = this.zNormal;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BuilderToolExtrudeAction other)
            ? false
            : this.x == other.x
               && this.y == other.y
               && this.z == other.z
               && this.xNormal == other.xNormal
               && this.yNormal == other.yNormal
               && this.zNormal == other.zNormal;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.x, this.y, this.z, this.xNormal, this.yNormal, this.zNormal);
   }
}
