package com.hypixel.hytale.protocol.packets.buildertools;

import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderToolStackArea implements Packet, ToServerPacket {
   public static final int PACKET_ID = 404;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 41;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 41;
   public static final int MAX_SIZE = 41;
   @Nullable
   public BlockPosition selectionMin;
   @Nullable
   public BlockPosition selectionMax;
   public int xNormal;
   public int yNormal;
   public int zNormal;
   public int numStacks;

   @Override
   public int getId() {
      return 404;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public BuilderToolStackArea() {
   }

   public BuilderToolStackArea(@Nullable BlockPosition selectionMin, @Nullable BlockPosition selectionMax, int xNormal, int yNormal, int zNormal, int numStacks) {
      this.selectionMin = selectionMin;
      this.selectionMax = selectionMax;
      this.xNormal = xNormal;
      this.yNormal = yNormal;
      this.zNormal = zNormal;
      this.numStacks = numStacks;
   }

   public BuilderToolStackArea(@Nonnull BuilderToolStackArea other) {
      this.selectionMin = other.selectionMin;
      this.selectionMax = other.selectionMax;
      this.xNormal = other.xNormal;
      this.yNormal = other.yNormal;
      this.zNormal = other.zNormal;
      this.numStacks = other.numStacks;
   }

   @Nonnull
   public static BuilderToolStackArea deserialize(@Nonnull ByteBuf buf, int offset) {
      BuilderToolStackArea obj = new BuilderToolStackArea();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         obj.selectionMin = BlockPosition.deserialize(buf, offset + 1);
      }

      if ((nullBits & 2) != 0) {
         obj.selectionMax = BlockPosition.deserialize(buf, offset + 13);
      }

      obj.xNormal = buf.getIntLE(offset + 25);
      obj.yNormal = buf.getIntLE(offset + 29);
      obj.zNormal = buf.getIntLE(offset + 33);
      obj.numStacks = buf.getIntLE(offset + 37);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 41;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.selectionMin != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.selectionMax != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      if (this.selectionMin != null) {
         this.selectionMin.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      if (this.selectionMax != null) {
         this.selectionMax.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      buf.writeIntLE(this.xNormal);
      buf.writeIntLE(this.yNormal);
      buf.writeIntLE(this.zNormal);
      buf.writeIntLE(this.numStacks);
   }

   @Override
   public int computeSize() {
      return 41;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 41 ? ValidationResult.error("Buffer too small: expected at least 41 bytes") : ValidationResult.OK;
   }

   public BuilderToolStackArea clone() {
      BuilderToolStackArea copy = new BuilderToolStackArea();
      copy.selectionMin = this.selectionMin != null ? this.selectionMin.clone() : null;
      copy.selectionMax = this.selectionMax != null ? this.selectionMax.clone() : null;
      copy.xNormal = this.xNormal;
      copy.yNormal = this.yNormal;
      copy.zNormal = this.zNormal;
      copy.numStacks = this.numStacks;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BuilderToolStackArea other)
            ? false
            : Objects.equals(this.selectionMin, other.selectionMin)
               && Objects.equals(this.selectionMax, other.selectionMax)
               && this.xNormal == other.xNormal
               && this.yNormal == other.yNormal
               && this.zNormal == other.zNormal
               && this.numStacks == other.numStacks;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.selectionMin, this.selectionMax, this.xNormal, this.yNormal, this.zNormal, this.numStacks);
   }
}
