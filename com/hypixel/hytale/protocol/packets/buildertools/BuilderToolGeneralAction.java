package com.hypixel.hytale.protocol.packets.buildertools;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class BuilderToolGeneralAction implements Packet, ToServerPacket {
   public static final int PACKET_ID = 412;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 1;
   @Nonnull
   public BuilderToolAction action = BuilderToolAction.SelectionPosition1;

   @Override
   public int getId() {
      return 412;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public BuilderToolGeneralAction() {
   }

   public BuilderToolGeneralAction(@Nonnull BuilderToolAction action) {
      this.action = action;
   }

   public BuilderToolGeneralAction(@Nonnull BuilderToolGeneralAction other) {
      this.action = other.action;
   }

   @Nonnull
   public static BuilderToolGeneralAction deserialize(@Nonnull ByteBuf buf, int offset) {
      BuilderToolGeneralAction obj = new BuilderToolGeneralAction();
      obj.action = BuilderToolAction.fromValue(buf.getByte(offset + 0));
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 1;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.action.getValue());
   }

   @Override
   public int computeSize() {
      return 1;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 1 ? ValidationResult.error("Buffer too small: expected at least 1 bytes") : ValidationResult.OK;
   }

   public BuilderToolGeneralAction clone() {
      BuilderToolGeneralAction copy = new BuilderToolGeneralAction();
      copy.action = this.action;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof BuilderToolGeneralAction other ? Objects.equals(this.action, other.action) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.action);
   }
}
