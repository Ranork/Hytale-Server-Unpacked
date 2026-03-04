package com.hypixel.hytale.protocol.packets.world;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class SetPaused implements Packet, ToServerPacket {
   public static final int PACKET_ID = 158;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 1;
   public boolean paused;

   @Override
   public int getId() {
      return 158;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public SetPaused() {
   }

   public SetPaused(boolean paused) {
      this.paused = paused;
   }

   public SetPaused(@Nonnull SetPaused other) {
      this.paused = other.paused;
   }

   @Nonnull
   public static SetPaused deserialize(@Nonnull ByteBuf buf, int offset) {
      SetPaused obj = new SetPaused();
      obj.paused = buf.getByte(offset + 0) != 0;
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 1;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.paused ? 1 : 0);
   }

   @Override
   public int computeSize() {
      return 1;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 1 ? ValidationResult.error("Buffer too small: expected at least 1 bytes") : ValidationResult.OK;
   }

   public SetPaused clone() {
      SetPaused copy = new SetPaused();
      copy.paused = this.paused;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof SetPaused other ? this.paused == other.paused : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.paused);
   }
}
