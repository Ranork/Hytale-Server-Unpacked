package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nonnull;

public class UntrackObjective implements Packet, ToClientPacket {
   public static final int PACKET_ID = 70;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 16;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 16;
   public static final int MAX_SIZE = 16;
   @Nonnull
   public UUID objectiveUuid = new UUID(0L, 0L);

   @Override
   public int getId() {
      return 70;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UntrackObjective() {
   }

   public UntrackObjective(@Nonnull UUID objectiveUuid) {
      this.objectiveUuid = objectiveUuid;
   }

   public UntrackObjective(@Nonnull UntrackObjective other) {
      this.objectiveUuid = other.objectiveUuid;
   }

   @Nonnull
   public static UntrackObjective deserialize(@Nonnull ByteBuf buf, int offset) {
      UntrackObjective obj = new UntrackObjective();
      obj.objectiveUuid = PacketIO.readUUID(buf, offset + 0);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 16;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      PacketIO.writeUUID(buf, this.objectiveUuid);
   }

   @Override
   public int computeSize() {
      return 16;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 16 ? ValidationResult.error("Buffer too small: expected at least 16 bytes") : ValidationResult.OK;
   }

   public UntrackObjective clone() {
      UntrackObjective copy = new UntrackObjective();
      copy.objectiveUuid = this.objectiveUuid;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof UntrackObjective other ? Objects.equals(this.objectiveUuid, other.objectiveUuid) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.objectiveUuid);
   }
}
