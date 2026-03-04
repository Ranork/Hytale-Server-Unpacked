package com.hypixel.hytale.protocol.packets.player;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;

public class ClearDebugShapes implements Packet, ToClientPacket {
   public static final int PACKET_ID = 115;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 0;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 0;
   public static final int MAX_SIZE = 0;

   @Override
   public int getId() {
      return 115;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   @Nonnull
   public static ClearDebugShapes deserialize(@Nonnull ByteBuf buf, int offset) {
      return new ClearDebugShapes();
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 0;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
   }

   @Override
   public int computeSize() {
      return 0;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 0 ? ValidationResult.error("Buffer too small: expected at least 0 bytes") : ValidationResult.OK;
   }

   public ClearDebugShapes clone() {
      return new ClearDebugShapes();
   }

   @Override
   public boolean equals(Object obj) {
      return this == obj ? true : obj instanceof ClearDebugShapes other;
   }

   @Override
   public int hashCode() {
      return 0;
   }
}
