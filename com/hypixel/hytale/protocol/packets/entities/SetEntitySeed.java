package com.hypixel.hytale.protocol.packets.entities;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class SetEntitySeed implements Packet, ToClientPacket {
   public static final int PACKET_ID = 160;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 4;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 4;
   public static final int MAX_SIZE = 4;
   public int entitySeed;

   @Override
   public int getId() {
      return 160;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public SetEntitySeed() {
   }

   public SetEntitySeed(int entitySeed) {
      this.entitySeed = entitySeed;
   }

   public SetEntitySeed(@Nonnull SetEntitySeed other) {
      this.entitySeed = other.entitySeed;
   }

   @Nonnull
   public static SetEntitySeed deserialize(@Nonnull ByteBuf buf, int offset) {
      SetEntitySeed obj = new SetEntitySeed();
      obj.entitySeed = buf.getIntLE(offset + 0);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 4;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.entitySeed);
   }

   @Override
   public int computeSize() {
      return 4;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 4 ? ValidationResult.error("Buffer too small: expected at least 4 bytes") : ValidationResult.OK;
   }

   public SetEntitySeed clone() {
      SetEntitySeed copy = new SetEntitySeed();
      copy.entitySeed = this.entitySeed;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof SetEntitySeed other ? this.entitySeed == other.entitySeed : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.entitySeed);
   }
}
