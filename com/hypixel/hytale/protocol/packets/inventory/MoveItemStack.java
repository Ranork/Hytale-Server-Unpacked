package com.hypixel.hytale.protocol.packets.inventory;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class MoveItemStack implements Packet, ToServerPacket {
   public static final int PACKET_ID = 175;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 20;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 20;
   public static final int MAX_SIZE = 20;
   public int fromSectionId;
   public int fromSlotId;
   public int quantity;
   public int toSectionId;
   public int toSlotId;

   @Override
   public int getId() {
      return 175;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public MoveItemStack() {
   }

   public MoveItemStack(int fromSectionId, int fromSlotId, int quantity, int toSectionId, int toSlotId) {
      this.fromSectionId = fromSectionId;
      this.fromSlotId = fromSlotId;
      this.quantity = quantity;
      this.toSectionId = toSectionId;
      this.toSlotId = toSlotId;
   }

   public MoveItemStack(@Nonnull MoveItemStack other) {
      this.fromSectionId = other.fromSectionId;
      this.fromSlotId = other.fromSlotId;
      this.quantity = other.quantity;
      this.toSectionId = other.toSectionId;
      this.toSlotId = other.toSlotId;
   }

   @Nonnull
   public static MoveItemStack deserialize(@Nonnull ByteBuf buf, int offset) {
      MoveItemStack obj = new MoveItemStack();
      obj.fromSectionId = buf.getIntLE(offset + 0);
      obj.fromSlotId = buf.getIntLE(offset + 4);
      obj.quantity = buf.getIntLE(offset + 8);
      obj.toSectionId = buf.getIntLE(offset + 12);
      obj.toSlotId = buf.getIntLE(offset + 16);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 20;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.fromSectionId);
      buf.writeIntLE(this.fromSlotId);
      buf.writeIntLE(this.quantity);
      buf.writeIntLE(this.toSectionId);
      buf.writeIntLE(this.toSlotId);
   }

   @Override
   public int computeSize() {
      return 20;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 20 ? ValidationResult.error("Buffer too small: expected at least 20 bytes") : ValidationResult.OK;
   }

   public MoveItemStack clone() {
      MoveItemStack copy = new MoveItemStack();
      copy.fromSectionId = this.fromSectionId;
      copy.fromSlotId = this.fromSlotId;
      copy.quantity = this.quantity;
      copy.toSectionId = this.toSectionId;
      copy.toSlotId = this.toSlotId;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof MoveItemStack other)
            ? false
            : this.fromSectionId == other.fromSectionId
               && this.fromSlotId == other.fromSlotId
               && this.quantity == other.quantity
               && this.toSectionId == other.toSectionId
               && this.toSlotId == other.toSlotId;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.fromSectionId, this.fromSlotId, this.quantity, this.toSectionId, this.toSlotId);
   }
}
