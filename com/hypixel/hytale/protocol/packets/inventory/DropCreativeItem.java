package com.hypixel.hytale.protocol.packets.inventory;

import com.hypixel.hytale.protocol.ItemQuantity;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class DropCreativeItem implements Packet, ToServerPacket {
   public static final int PACKET_ID = 172;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 0;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 0;
   public static final int MAX_SIZE = 16384010;
   @Nonnull
   public ItemQuantity item = new ItemQuantity();

   @Override
   public int getId() {
      return 172;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public DropCreativeItem() {
   }

   public DropCreativeItem(@Nonnull ItemQuantity item) {
      this.item = item;
   }

   public DropCreativeItem(@Nonnull DropCreativeItem other) {
      this.item = other.item;
   }

   @Nonnull
   public static DropCreativeItem deserialize(@Nonnull ByteBuf buf, int offset) {
      DropCreativeItem obj = new DropCreativeItem();
      int pos = offset + 0;
      obj.item = ItemQuantity.deserialize(buf, pos);
      pos += ItemQuantity.computeBytesConsumed(buf, pos);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      int pos = offset + 0;
      pos += ItemQuantity.computeBytesConsumed(buf, pos);
      return pos - offset;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      this.item.serialize(buf);
   }

   @Override
   public int computeSize() {
      int size = 0;
      return size + this.item.computeSize();
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 0) {
         return ValidationResult.error("Buffer too small: expected at least 0 bytes");
      } else {
         int pos = offset + 0;
         ValidationResult itemResult = ItemQuantity.validateStructure(buffer, pos);
         if (!itemResult.isValid()) {
            return ValidationResult.error("Invalid Item: " + itemResult.error());
         } else {
            pos += ItemQuantity.computeBytesConsumed(buffer, pos);
            return ValidationResult.OK;
         }
      }
   }

   public DropCreativeItem clone() {
      DropCreativeItem copy = new DropCreativeItem();
      copy.item = this.item.clone();
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof DropCreativeItem other ? Objects.equals(this.item, other.item) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.item);
   }
}
