package com.hypixel.hytale.protocol.packets.world;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UpdateSleepState implements Packet, ToClientPacket {
   public static final int PACKET_ID = 157;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 36;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 36;
   public static final int MAX_SIZE = 65536050;
   public boolean grayFade;
   public boolean sleepUi;
   @Nullable
   public SleepClock clock;
   @Nullable
   public SleepMultiplayer multiplayer;

   @Override
   public int getId() {
      return 157;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateSleepState() {
   }

   public UpdateSleepState(boolean grayFade, boolean sleepUi, @Nullable SleepClock clock, @Nullable SleepMultiplayer multiplayer) {
      this.grayFade = grayFade;
      this.sleepUi = sleepUi;
      this.clock = clock;
      this.multiplayer = multiplayer;
   }

   public UpdateSleepState(@Nonnull UpdateSleepState other) {
      this.grayFade = other.grayFade;
      this.sleepUi = other.sleepUi;
      this.clock = other.clock;
      this.multiplayer = other.multiplayer;
   }

   @Nonnull
   public static UpdateSleepState deserialize(@Nonnull ByteBuf buf, int offset) {
      UpdateSleepState obj = new UpdateSleepState();
      byte nullBits = buf.getByte(offset);
      obj.grayFade = buf.getByte(offset + 1) != 0;
      obj.sleepUi = buf.getByte(offset + 2) != 0;
      if ((nullBits & 1) != 0) {
         obj.clock = SleepClock.deserialize(buf, offset + 3);
      }

      int pos = offset + 36;
      if ((nullBits & 2) != 0) {
         obj.multiplayer = SleepMultiplayer.deserialize(buf, pos);
         pos += SleepMultiplayer.computeBytesConsumed(buf, pos);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 36;
      if ((nullBits & 2) != 0) {
         pos += SleepMultiplayer.computeBytesConsumed(buf, pos);
      }

      return pos - offset;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.clock != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.multiplayer != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.grayFade ? 1 : 0);
      buf.writeByte(this.sleepUi ? 1 : 0);
      if (this.clock != null) {
         this.clock.serialize(buf);
      } else {
         buf.writeZero(33);
      }

      if (this.multiplayer != null) {
         this.multiplayer.serialize(buf);
      }
   }

   @Override
   public int computeSize() {
      int size = 36;
      if (this.multiplayer != null) {
         size += this.multiplayer.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 36) {
         return ValidationResult.error("Buffer too small: expected at least 36 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 36;
         if ((nullBits & 2) != 0) {
            ValidationResult multiplayerResult = SleepMultiplayer.validateStructure(buffer, pos);
            if (!multiplayerResult.isValid()) {
               return ValidationResult.error("Invalid Multiplayer: " + multiplayerResult.error());
            }

            pos += SleepMultiplayer.computeBytesConsumed(buffer, pos);
         }

         return ValidationResult.OK;
      }
   }

   public UpdateSleepState clone() {
      UpdateSleepState copy = new UpdateSleepState();
      copy.grayFade = this.grayFade;
      copy.sleepUi = this.sleepUi;
      copy.clock = this.clock != null ? this.clock.clone() : null;
      copy.multiplayer = this.multiplayer != null ? this.multiplayer.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateSleepState other)
            ? false
            : this.grayFade == other.grayFade
               && this.sleepUi == other.sleepUi
               && Objects.equals(this.clock, other.clock)
               && Objects.equals(this.multiplayer, other.multiplayer);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.grayFade, this.sleepUi, this.clock, this.multiplayer);
   }
}
