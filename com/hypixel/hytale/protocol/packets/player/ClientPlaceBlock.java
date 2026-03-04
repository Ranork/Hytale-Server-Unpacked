package com.hypixel.hytale.protocol.packets.player;

import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.BlockRotation;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ClientPlaceBlock implements Packet, ToServerPacket {
   public static final int PACKET_ID = 117;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 20;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 20;
   public static final int MAX_SIZE = 20;
   @Nullable
   public BlockPosition position;
   @Nullable
   public BlockRotation rotation;
   public int placedBlockId;

   @Override
   public int getId() {
      return 117;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public ClientPlaceBlock() {
   }

   public ClientPlaceBlock(@Nullable BlockPosition position, @Nullable BlockRotation rotation, int placedBlockId) {
      this.position = position;
      this.rotation = rotation;
      this.placedBlockId = placedBlockId;
   }

   public ClientPlaceBlock(@Nonnull ClientPlaceBlock other) {
      this.position = other.position;
      this.rotation = other.rotation;
      this.placedBlockId = other.placedBlockId;
   }

   @Nonnull
   public static ClientPlaceBlock deserialize(@Nonnull ByteBuf buf, int offset) {
      ClientPlaceBlock obj = new ClientPlaceBlock();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         obj.position = BlockPosition.deserialize(buf, offset + 1);
      }

      if ((nullBits & 2) != 0) {
         obj.rotation = BlockRotation.deserialize(buf, offset + 13);
      }

      obj.placedBlockId = buf.getIntLE(offset + 16);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 20;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.position != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.rotation != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      if (this.position != null) {
         this.position.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      if (this.rotation != null) {
         this.rotation.serialize(buf);
      } else {
         buf.writeZero(3);
      }

      buf.writeIntLE(this.placedBlockId);
   }

   @Override
   public int computeSize() {
      return 20;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 20 ? ValidationResult.error("Buffer too small: expected at least 20 bytes") : ValidationResult.OK;
   }

   public ClientPlaceBlock clone() {
      ClientPlaceBlock copy = new ClientPlaceBlock();
      copy.position = this.position != null ? this.position.clone() : null;
      copy.rotation = this.rotation != null ? this.rotation.clone() : null;
      copy.placedBlockId = this.placedBlockId;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ClientPlaceBlock other)
            ? false
            : Objects.equals(this.position, other.position) && Objects.equals(this.rotation, other.rotation) && this.placedBlockId == other.placedBlockId;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.position, this.rotation, this.placedBlockId);
   }
}
