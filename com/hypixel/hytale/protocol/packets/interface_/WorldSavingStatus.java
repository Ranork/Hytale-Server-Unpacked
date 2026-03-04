package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class WorldSavingStatus implements Packet, ToClientPacket {
   public static final int PACKET_ID = 233;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 1;
   public boolean isWorldSaving;

   @Override
   public int getId() {
      return 233;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public WorldSavingStatus() {
   }

   public WorldSavingStatus(boolean isWorldSaving) {
      this.isWorldSaving = isWorldSaving;
   }

   public WorldSavingStatus(@Nonnull WorldSavingStatus other) {
      this.isWorldSaving = other.isWorldSaving;
   }

   @Nonnull
   public static WorldSavingStatus deserialize(@Nonnull ByteBuf buf, int offset) {
      WorldSavingStatus obj = new WorldSavingStatus();
      obj.isWorldSaving = buf.getByte(offset + 0) != 0;
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 1;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.isWorldSaving ? 1 : 0);
   }

   @Override
   public int computeSize() {
      return 1;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 1 ? ValidationResult.error("Buffer too small: expected at least 1 bytes") : ValidationResult.OK;
   }

   public WorldSavingStatus clone() {
      WorldSavingStatus copy = new WorldSavingStatus();
      copy.isWorldSaving = this.isWorldSaving;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof WorldSavingStatus other ? this.isWorldSaving == other.isWorldSaving : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.isWorldSaving);
   }
}
