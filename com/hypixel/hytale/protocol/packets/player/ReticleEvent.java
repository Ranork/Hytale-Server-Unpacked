package com.hypixel.hytale.protocol.packets.player;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class ReticleEvent implements Packet, ToClientPacket {
   public static final int PACKET_ID = 113;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 4;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 4;
   public static final int MAX_SIZE = 4;
   public int eventIndex;

   @Override
   public int getId() {
      return 113;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public ReticleEvent() {
   }

   public ReticleEvent(int eventIndex) {
      this.eventIndex = eventIndex;
   }

   public ReticleEvent(@Nonnull ReticleEvent other) {
      this.eventIndex = other.eventIndex;
   }

   @Nonnull
   public static ReticleEvent deserialize(@Nonnull ByteBuf buf, int offset) {
      ReticleEvent obj = new ReticleEvent();
      obj.eventIndex = buf.getIntLE(offset + 0);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 4;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.eventIndex);
   }

   @Override
   public int computeSize() {
      return 4;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 4 ? ValidationResult.error("Buffer too small: expected at least 4 bytes") : ValidationResult.OK;
   }

   public ReticleEvent clone() {
      ReticleEvent copy = new ReticleEvent();
      copy.eventIndex = this.eventIndex;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof ReticleEvent other ? this.eventIndex == other.eventIndex : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.eventIndex);
   }
}
