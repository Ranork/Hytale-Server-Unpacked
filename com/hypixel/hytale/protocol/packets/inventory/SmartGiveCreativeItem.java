package com.hypixel.hytale.protocol.packets.inventory;

import com.hypixel.hytale.protocol.ItemQuantity;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.SmartMoveType;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class SmartGiveCreativeItem implements Packet, ToServerPacket {
   public static final int PACKET_ID = 173;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 16384011;
   @Nonnull
   public ItemQuantity item = new ItemQuantity();
   @Nonnull
   public SmartMoveType moveType = SmartMoveType.EquipOrMergeStack;

   @Override
   public int getId() {
      return 173;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public SmartGiveCreativeItem() {
   }

   public SmartGiveCreativeItem(@Nonnull ItemQuantity item, @Nonnull SmartMoveType moveType) {
      this.item = item;
      this.moveType = moveType;
   }

   public SmartGiveCreativeItem(@Nonnull SmartGiveCreativeItem other) {
      this.item = other.item;
      this.moveType = other.moveType;
   }

   @Nonnull
   public static SmartGiveCreativeItem deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 1) {
         throw ProtocolException.bufferTooSmall("SmartGiveCreativeItem", 1, buf.readableBytes() - offset);
      } else {
         SmartGiveCreativeItem obj = new SmartGiveCreativeItem();
         obj.moveType = SmartMoveType.fromValue(buf.getByte(offset + 0));
         int pos = offset + 1;
         obj.item = ItemQuantity.deserialize(buf, pos);
         pos += ItemQuantity.computeBytesConsumed(buf, pos);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      int pos = offset + 1;
      pos += ItemQuantity.computeBytesConsumed(buf, pos);
      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 1L;
   }

   public static ItemQuantity getItem(MemorySegment mem) {
      return getItem(mem, 0);
   }

   public static ItemQuantity getItem(MemorySegment mem, int offset) {
      return ItemQuantity.toObject(mem, offset + 1);
   }

   public static SmartMoveType getMoveType(MemorySegment mem) {
      return getMoveType(mem, 0);
   }

   public static SmartMoveType getMoveType(MemorySegment mem, int offset) {
      return SmartMoveType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0)));
   }

   public static SmartGiveCreativeItem toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static SmartGiveCreativeItem toObject(MemorySegment mem, int offset) {
      if (offset + 1 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("SmartGiveCreativeItem", offset + 1, (int)mem.byteSize());
      } else {
         return new SmartGiveCreativeItem(ItemQuantity.toObject(mem, offset + 1), SmartMoveType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0))));
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.moveType.getValue());
      this.item.serialize(buf);
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), (byte)this.moveType.getValue());
      int varOffset = offset + 1;
      varOffset += this.item.serialize(mem, varOffset);
      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 1;
      return size + this.item.computeSize();
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 1) {
         return ValidationResult.error("Buffer too small: expected at least 1 bytes");
      } else {
         int v = buffer.getByte(offset + 0) & 255;
         if (v >= 3) {
            return ValidationResult.error("Invalid SmartMoveType value for MoveType");
         } else {
            v = offset + 1;
            ValidationResult itemResult = ItemQuantity.validateStructure(buffer, v);
            if (!itemResult.isValid()) {
               return ValidationResult.error("Invalid Item: " + itemResult.error());
            } else {
               v += ItemQuantity.computeBytesConsumed(buffer, v);
               return ValidationResult.OK;
            }
         }
      }
   }

   public SmartGiveCreativeItem clone() {
      SmartGiveCreativeItem copy = new SmartGiveCreativeItem();
      copy.item = this.item.clone();
      copy.moveType = this.moveType;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof SmartGiveCreativeItem other) ? false : Objects.equals(this.item, other.item) && Objects.equals(this.moveType, other.moveType);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.item, this.moveType);
   }
}
