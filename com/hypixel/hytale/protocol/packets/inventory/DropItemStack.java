package com.hypixel.hytale.protocol.packets.inventory;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class DropItemStack implements Packet, ToServerPacket {
   public static final int PACKET_ID = 174;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 12;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 12;
   public static final int MAX_SIZE = 12;
   public int inventorySectionId;
   public int slotId;
   public int quantity;

   @Override
   public int getId() {
      return 174;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public DropItemStack() {
   }

   public DropItemStack(int inventorySectionId, int slotId, int quantity) {
      this.inventorySectionId = inventorySectionId;
      this.slotId = slotId;
      this.quantity = quantity;
   }

   public DropItemStack(@Nonnull DropItemStack other) {
      this.inventorySectionId = other.inventorySectionId;
      this.slotId = other.slotId;
      this.quantity = other.quantity;
   }

   @Nonnull
   public static DropItemStack deserialize(@Nonnull ByteBuf buf, int offset) {
      DropItemStack obj = new DropItemStack();
      obj.inventorySectionId = buf.getIntLE(offset + 0);
      obj.slotId = buf.getIntLE(offset + 4);
      obj.quantity = buf.getIntLE(offset + 8);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 12;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.inventorySectionId);
      buf.writeIntLE(this.slotId);
      buf.writeIntLE(this.quantity);
   }

   @Override
   public int computeSize() {
      return 12;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 12 ? ValidationResult.error("Buffer too small: expected at least 12 bytes") : ValidationResult.OK;
   }

   public DropItemStack clone() {
      DropItemStack copy = new DropItemStack();
      copy.inventorySectionId = this.inventorySectionId;
      copy.slotId = this.slotId;
      copy.quantity = this.quantity;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof DropItemStack other)
            ? false
            : this.inventorySectionId == other.inventorySectionId && this.slotId == other.slotId && this.quantity == other.quantity;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.inventorySectionId, this.slotId, this.quantity);
   }
}
