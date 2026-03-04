package com.hypixel.hytale.protocol.packets.inventory;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class SetActiveSlot implements Packet, ToServerPacket, ToClientPacket {
   public static final int PACKET_ID = 177;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 8;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 8;
   public static final int MAX_SIZE = 8;
   public int inventorySectionId;
   public int activeSlot;

   @Override
   public int getId() {
      return 177;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public SetActiveSlot() {
   }

   public SetActiveSlot(int inventorySectionId, int activeSlot) {
      this.inventorySectionId = inventorySectionId;
      this.activeSlot = activeSlot;
   }

   public SetActiveSlot(@Nonnull SetActiveSlot other) {
      this.inventorySectionId = other.inventorySectionId;
      this.activeSlot = other.activeSlot;
   }

   @Nonnull
   public static SetActiveSlot deserialize(@Nonnull ByteBuf buf, int offset) {
      SetActiveSlot obj = new SetActiveSlot();
      obj.inventorySectionId = buf.getIntLE(offset + 0);
      obj.activeSlot = buf.getIntLE(offset + 4);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 8;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.inventorySectionId);
      buf.writeIntLE(this.activeSlot);
   }

   @Override
   public int computeSize() {
      return 8;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 8 ? ValidationResult.error("Buffer too small: expected at least 8 bytes") : ValidationResult.OK;
   }

   public SetActiveSlot clone() {
      SetActiveSlot copy = new SetActiveSlot();
      copy.inventorySectionId = this.inventorySectionId;
      copy.activeSlot = this.activeSlot;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof SetActiveSlot other) ? false : this.inventorySectionId == other.inventorySectionId && this.activeSlot == other.activeSlot;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.inventorySectionId, this.activeSlot);
   }
}
