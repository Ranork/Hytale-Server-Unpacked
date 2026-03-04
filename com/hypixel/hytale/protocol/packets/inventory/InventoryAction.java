package com.hypixel.hytale.protocol.packets.inventory;

import com.hypixel.hytale.protocol.InventoryActionType;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class InventoryAction implements Packet, ToServerPacket {
   public static final int PACKET_ID = 179;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 6;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 6;
   public static final int MAX_SIZE = 6;
   public int inventorySectionId;
   @Nonnull
   public InventoryActionType inventoryActionType = InventoryActionType.TakeAll;
   public byte actionData;

   @Override
   public int getId() {
      return 179;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public InventoryAction() {
   }

   public InventoryAction(int inventorySectionId, @Nonnull InventoryActionType inventoryActionType, byte actionData) {
      this.inventorySectionId = inventorySectionId;
      this.inventoryActionType = inventoryActionType;
      this.actionData = actionData;
   }

   public InventoryAction(@Nonnull InventoryAction other) {
      this.inventorySectionId = other.inventorySectionId;
      this.inventoryActionType = other.inventoryActionType;
      this.actionData = other.actionData;
   }

   @Nonnull
   public static InventoryAction deserialize(@Nonnull ByteBuf buf, int offset) {
      InventoryAction obj = new InventoryAction();
      obj.inventorySectionId = buf.getIntLE(offset + 0);
      obj.inventoryActionType = InventoryActionType.fromValue(buf.getByte(offset + 4));
      obj.actionData = buf.getByte(offset + 5);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 6;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.inventorySectionId);
      buf.writeByte(this.inventoryActionType.getValue());
      buf.writeByte(this.actionData);
   }

   @Override
   public int computeSize() {
      return 6;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 6 ? ValidationResult.error("Buffer too small: expected at least 6 bytes") : ValidationResult.OK;
   }

   public InventoryAction clone() {
      InventoryAction copy = new InventoryAction();
      copy.inventorySectionId = this.inventorySectionId;
      copy.inventoryActionType = this.inventoryActionType;
      copy.actionData = this.actionData;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof InventoryAction other)
            ? false
            : this.inventorySectionId == other.inventorySectionId
               && Objects.equals(this.inventoryActionType, other.inventoryActionType)
               && this.actionData == other.actionData;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.inventorySectionId, this.inventoryActionType, this.actionData);
   }
}
