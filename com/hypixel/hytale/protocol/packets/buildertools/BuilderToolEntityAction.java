package com.hypixel.hytale.protocol.packets.buildertools;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class BuilderToolEntityAction implements Packet, ToServerPacket {
   public static final int PACKET_ID = 401;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 5;
   public static final int MAX_SIZE = 5;
   public int entityId;
   @Nonnull
   public EntityToolAction action = EntityToolAction.Remove;

   @Override
   public int getId() {
      return 401;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public BuilderToolEntityAction() {
   }

   public BuilderToolEntityAction(int entityId, @Nonnull EntityToolAction action) {
      this.entityId = entityId;
      this.action = action;
   }

   public BuilderToolEntityAction(@Nonnull BuilderToolEntityAction other) {
      this.entityId = other.entityId;
      this.action = other.action;
   }

   @Nonnull
   public static BuilderToolEntityAction deserialize(@Nonnull ByteBuf buf, int offset) {
      BuilderToolEntityAction obj = new BuilderToolEntityAction();
      obj.entityId = buf.getIntLE(offset + 0);
      obj.action = EntityToolAction.fromValue(buf.getByte(offset + 4));
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 5;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.entityId);
      buf.writeByte(this.action.getValue());
   }

   @Override
   public int computeSize() {
      return 5;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 5 ? ValidationResult.error("Buffer too small: expected at least 5 bytes") : ValidationResult.OK;
   }

   public BuilderToolEntityAction clone() {
      BuilderToolEntityAction copy = new BuilderToolEntityAction();
      copy.entityId = this.entityId;
      copy.action = this.action;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BuilderToolEntityAction other) ? false : this.entityId == other.entityId && Objects.equals(this.action, other.action);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.entityId, this.action);
   }
}
