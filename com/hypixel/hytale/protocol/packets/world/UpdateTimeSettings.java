package com.hypixel.hytale.protocol.packets.world;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class UpdateTimeSettings implements Packet, ToClientPacket {
   public static final int PACKET_ID = 145;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 10;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 10;
   public static final int MAX_SIZE = 10;
   public int daytimeDurationSeconds;
   public int nighttimeDurationSeconds;
   public byte totalMoonPhases;
   public boolean timePaused;

   @Override
   public int getId() {
      return 145;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateTimeSettings() {
   }

   public UpdateTimeSettings(int daytimeDurationSeconds, int nighttimeDurationSeconds, byte totalMoonPhases, boolean timePaused) {
      this.daytimeDurationSeconds = daytimeDurationSeconds;
      this.nighttimeDurationSeconds = nighttimeDurationSeconds;
      this.totalMoonPhases = totalMoonPhases;
      this.timePaused = timePaused;
   }

   public UpdateTimeSettings(@Nonnull UpdateTimeSettings other) {
      this.daytimeDurationSeconds = other.daytimeDurationSeconds;
      this.nighttimeDurationSeconds = other.nighttimeDurationSeconds;
      this.totalMoonPhases = other.totalMoonPhases;
      this.timePaused = other.timePaused;
   }

   @Nonnull
   public static UpdateTimeSettings deserialize(@Nonnull ByteBuf buf, int offset) {
      UpdateTimeSettings obj = new UpdateTimeSettings();
      obj.daytimeDurationSeconds = buf.getIntLE(offset + 0);
      obj.nighttimeDurationSeconds = buf.getIntLE(offset + 4);
      obj.totalMoonPhases = buf.getByte(offset + 8);
      obj.timePaused = buf.getByte(offset + 9) != 0;
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 10;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.daytimeDurationSeconds);
      buf.writeIntLE(this.nighttimeDurationSeconds);
      buf.writeByte(this.totalMoonPhases);
      buf.writeByte(this.timePaused ? 1 : 0);
   }

   @Override
   public int computeSize() {
      return 10;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 10 ? ValidationResult.error("Buffer too small: expected at least 10 bytes") : ValidationResult.OK;
   }

   public UpdateTimeSettings clone() {
      UpdateTimeSettings copy = new UpdateTimeSettings();
      copy.daytimeDurationSeconds = this.daytimeDurationSeconds;
      copy.nighttimeDurationSeconds = this.nighttimeDurationSeconds;
      copy.totalMoonPhases = this.totalMoonPhases;
      copy.timePaused = this.timePaused;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateTimeSettings other)
            ? false
            : this.daytimeDurationSeconds == other.daytimeDurationSeconds
               && this.nighttimeDurationSeconds == other.nighttimeDurationSeconds
               && this.totalMoonPhases == other.totalMoonPhases
               && this.timePaused == other.timePaused;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.daytimeDurationSeconds, this.nighttimeDurationSeconds, this.totalMoonPhases, this.timePaused);
   }
}
