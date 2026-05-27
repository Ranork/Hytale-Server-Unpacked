package com.hypixel.hytale.protocol.packets.inventory;

import com.hypixel.hytale.protocol.InventoryActionType;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
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
      if (buf.readableBytes() - offset < 6) {
         throw ProtocolException.bufferTooSmall("InventoryAction", 6, buf.readableBytes() - offset);
      } else {
         InventoryAction obj = new InventoryAction();
         obj.inventorySectionId = buf.getIntLE(offset + 0);
         obj.inventoryActionType = InventoryActionType.fromValue(buf.getByte(offset + 4));
         obj.actionData = buf.getByte(offset + 5);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 6;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 6L;
   }

   public static int getInventorySectionId(MemorySegment mem) {
      return getInventorySectionId(mem, 0);
   }

   public static int getInventorySectionId(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 0));
   }

   public static InventoryActionType getInventoryActionType(MemorySegment mem) {
      return getInventoryActionType(mem, 0);
   }

   public static InventoryActionType getInventoryActionType(MemorySegment mem, int offset) {
      return InventoryActionType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 4)));
   }

   public static byte getActionData(MemorySegment mem) {
      return getActionData(mem, 0);
   }

   public static byte getActionData(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BYTE, (long)(offset + 5));
   }

   public static InventoryAction toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static InventoryAction toObject(MemorySegment mem, int offset) {
      if (offset + 6 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("InventoryAction", offset + 6, (int)mem.byteSize());
      } else {
         return new InventoryAction(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 0)),
            InventoryActionType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 4))),
            mem.get(PacketIO.PROTO_BYTE, (long)(offset + 5))
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.inventorySectionId);
      buf.writeByte(this.inventoryActionType.getValue());
      buf.writeByte(this.actionData);
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_INT, (long)(offset + 0), this.inventorySectionId);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 4), (byte)this.inventoryActionType.getValue());
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 5), this.actionData);
      return 6;
   }

   @Override
   public int computeSize() {
      return 6;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 6) {
         return ValidationResult.error("Buffer too small: expected at least 6 bytes");
      } else {
         int v = buffer.getByte(offset + 4) & 255;
         return v >= 4 ? ValidationResult.error("Invalid InventoryActionType value for InventoryActionType") : ValidationResult.OK;
      }
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
