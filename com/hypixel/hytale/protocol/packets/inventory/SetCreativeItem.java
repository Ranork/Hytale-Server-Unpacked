package com.hypixel.hytale.protocol.packets.inventory;

import com.hypixel.hytale.protocol.ItemQuantity;
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

public class SetCreativeItem implements Packet, ToServerPacket {
   public static final int PACKET_ID = 171;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 9;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 16384019;
   public int inventorySectionId;
   public int slotId;
   @Nonnull
   public ItemQuantity item = new ItemQuantity();
   public boolean override;

   @Override
   public int getId() {
      return 171;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public SetCreativeItem() {
   }

   public SetCreativeItem(int inventorySectionId, int slotId, @Nonnull ItemQuantity item, boolean override) {
      this.inventorySectionId = inventorySectionId;
      this.slotId = slotId;
      this.item = item;
      this.override = override;
   }

   public SetCreativeItem(@Nonnull SetCreativeItem other) {
      this.inventorySectionId = other.inventorySectionId;
      this.slotId = other.slotId;
      this.item = other.item;
      this.override = other.override;
   }

   @Nonnull
   public static SetCreativeItem deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 9) {
         throw ProtocolException.bufferTooSmall("SetCreativeItem", 9, buf.readableBytes() - offset);
      } else {
         SetCreativeItem obj = new SetCreativeItem();
         obj.inventorySectionId = buf.getIntLE(offset + 0);
         obj.slotId = buf.getIntLE(offset + 4);
         obj.override = buf.getByte(offset + 8) != 0;
         int pos = offset + 9;
         obj.item = ItemQuantity.deserialize(buf, pos);
         pos += ItemQuantity.computeBytesConsumed(buf, pos);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      int pos = offset + 9;
      pos += ItemQuantity.computeBytesConsumed(buf, pos);
      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 9L;
   }

   public static int getInventorySectionId(MemorySegment mem) {
      return getInventorySectionId(mem, 0);
   }

   public static int getInventorySectionId(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 0));
   }

   public static int getSlotId(MemorySegment mem) {
      return getSlotId(mem, 0);
   }

   public static int getSlotId(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 4));
   }

   public static ItemQuantity getItem(MemorySegment mem) {
      return getItem(mem, 0);
   }

   public static ItemQuantity getItem(MemorySegment mem, int offset) {
      return ItemQuantity.toObject(mem, offset + 9);
   }

   public static boolean getOverride(MemorySegment mem) {
      return getOverride(mem, 0);
   }

   public static boolean getOverride(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 8));
   }

   public static SetCreativeItem toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static SetCreativeItem toObject(MemorySegment mem, int offset) {
      if (offset + 9 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("SetCreativeItem", offset + 9, (int)mem.byteSize());
      } else {
         return new SetCreativeItem(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 0)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 4)),
            ItemQuantity.toObject(mem, offset + 9),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 8))
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.inventorySectionId);
      buf.writeIntLE(this.slotId);
      buf.writeByte(this.override ? 1 : 0);
      this.item.serialize(buf);
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_INT, (long)(offset + 0), this.inventorySectionId);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 4), this.slotId);
      mem.set(PacketIO.PROTO_BOOL, offset + 8, this.override);
      int varOffset = offset + 9;
      varOffset += this.item.serialize(mem, varOffset);
      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 9;
      return size + this.item.computeSize();
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 9) {
         return ValidationResult.error("Buffer too small: expected at least 9 bytes");
      } else {
         int pos = offset + 9;
         ValidationResult itemResult = ItemQuantity.validateStructure(buffer, pos);
         if (!itemResult.isValid()) {
            return ValidationResult.error("Invalid Item: " + itemResult.error());
         } else {
            pos += ItemQuantity.computeBytesConsumed(buffer, pos);
            return ValidationResult.OK;
         }
      }
   }

   public SetCreativeItem clone() {
      SetCreativeItem copy = new SetCreativeItem();
      copy.inventorySectionId = this.inventorySectionId;
      copy.slotId = this.slotId;
      copy.item = this.item.clone();
      copy.override = this.override;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof SetCreativeItem other)
            ? false
            : this.inventorySectionId == other.inventorySectionId
               && this.slotId == other.slotId
               && Objects.equals(this.item, other.item)
               && this.override == other.override;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.inventorySectionId, this.slotId, this.item, this.override);
   }
}
