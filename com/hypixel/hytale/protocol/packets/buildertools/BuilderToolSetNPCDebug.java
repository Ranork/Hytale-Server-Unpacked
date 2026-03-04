package com.hypixel.hytale.protocol.packets.buildertools;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class BuilderToolSetNPCDebug implements Packet, ToServerPacket {
   public static final int PACKET_ID = 423;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 5;
   public static final int MAX_SIZE = 5;
   public int entityId;
   public boolean enabled;

   @Override
   public int getId() {
      return 423;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public BuilderToolSetNPCDebug() {
   }

   public BuilderToolSetNPCDebug(int entityId, boolean enabled) {
      this.entityId = entityId;
      this.enabled = enabled;
   }

   public BuilderToolSetNPCDebug(@Nonnull BuilderToolSetNPCDebug other) {
      this.entityId = other.entityId;
      this.enabled = other.enabled;
   }

   @Nonnull
   public static BuilderToolSetNPCDebug deserialize(@Nonnull ByteBuf buf, int offset) {
      BuilderToolSetNPCDebug obj = new BuilderToolSetNPCDebug();
      obj.entityId = buf.getIntLE(offset + 0);
      obj.enabled = buf.getByte(offset + 4) != 0;
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 5;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.entityId);
      buf.writeByte(this.enabled ? 1 : 0);
   }

   @Override
   public int computeSize() {
      return 5;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 5 ? ValidationResult.error("Buffer too small: expected at least 5 bytes") : ValidationResult.OK;
   }

   public BuilderToolSetNPCDebug clone() {
      BuilderToolSetNPCDebug copy = new BuilderToolSetNPCDebug();
      copy.entityId = this.entityId;
      copy.enabled = this.enabled;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BuilderToolSetNPCDebug other) ? false : this.entityId == other.entityId && this.enabled == other.enabled;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.entityId, this.enabled);
   }
}
