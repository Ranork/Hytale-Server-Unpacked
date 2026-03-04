package com.hypixel.hytale.protocol.packets.world;

import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UpdateBlockDamage implements Packet, ToClientPacket {
   public static final int PACKET_ID = 144;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 21;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 21;
   public static final int MAX_SIZE = 21;
   @Nullable
   public BlockPosition blockPosition;
   public float damage;
   public float delta;

   @Override
   public int getId() {
      return 144;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Chunks;
   }

   public UpdateBlockDamage() {
   }

   public UpdateBlockDamage(@Nullable BlockPosition blockPosition, float damage, float delta) {
      this.blockPosition = blockPosition;
      this.damage = damage;
      this.delta = delta;
   }

   public UpdateBlockDamage(@Nonnull UpdateBlockDamage other) {
      this.blockPosition = other.blockPosition;
      this.damage = other.damage;
      this.delta = other.delta;
   }

   @Nonnull
   public static UpdateBlockDamage deserialize(@Nonnull ByteBuf buf, int offset) {
      UpdateBlockDamage obj = new UpdateBlockDamage();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         obj.blockPosition = BlockPosition.deserialize(buf, offset + 1);
      }

      obj.damage = buf.getFloatLE(offset + 13);
      obj.delta = buf.getFloatLE(offset + 17);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 21;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.blockPosition != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.blockPosition != null) {
         this.blockPosition.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      buf.writeFloatLE(this.damage);
      buf.writeFloatLE(this.delta);
   }

   @Override
   public int computeSize() {
      return 21;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 21 ? ValidationResult.error("Buffer too small: expected at least 21 bytes") : ValidationResult.OK;
   }

   public UpdateBlockDamage clone() {
      UpdateBlockDamage copy = new UpdateBlockDamage();
      copy.blockPosition = this.blockPosition != null ? this.blockPosition.clone() : null;
      copy.damage = this.damage;
      copy.delta = this.delta;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateBlockDamage other)
            ? false
            : Objects.equals(this.blockPosition, other.blockPosition) && this.damage == other.damage && this.delta == other.delta;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.blockPosition, this.damage, this.delta);
   }
}
