package com.hypixel.hytale.protocol.packets.player;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.SavedMovementStates;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SetMovementStates implements Packet, ToClientPacket {
   public static final int PACKET_ID = 102;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 2;
   public static final int MAX_SIZE = 2;
   @Nullable
   public SavedMovementStates movementStates;

   @Override
   public int getId() {
      return 102;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public SetMovementStates() {
   }

   public SetMovementStates(@Nullable SavedMovementStates movementStates) {
      this.movementStates = movementStates;
   }

   public SetMovementStates(@Nonnull SetMovementStates other) {
      this.movementStates = other.movementStates;
   }

   @Nonnull
   public static SetMovementStates deserialize(@Nonnull ByteBuf buf, int offset) {
      SetMovementStates obj = new SetMovementStates();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         obj.movementStates = SavedMovementStates.deserialize(buf, offset + 1);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 2;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.movementStates != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.movementStates != null) {
         this.movementStates.serialize(buf);
      } else {
         buf.writeZero(1);
      }
   }

   @Override
   public int computeSize() {
      return 2;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 2 ? ValidationResult.error("Buffer too small: expected at least 2 bytes") : ValidationResult.OK;
   }

   public SetMovementStates clone() {
      SetMovementStates copy = new SetMovementStates();
      copy.movementStates = this.movementStates != null ? this.movementStates.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof SetMovementStates other ? Objects.equals(this.movementStates, other.movementStates) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.movementStates);
   }
}
