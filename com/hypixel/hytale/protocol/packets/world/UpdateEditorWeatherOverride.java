package com.hypixel.hytale.protocol.packets.world;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class UpdateEditorWeatherOverride implements Packet, ToClientPacket {
   public static final int PACKET_ID = 150;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 4;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 4;
   public static final int MAX_SIZE = 4;
   public int weatherIndex;

   @Override
   public int getId() {
      return 150;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateEditorWeatherOverride() {
   }

   public UpdateEditorWeatherOverride(int weatherIndex) {
      this.weatherIndex = weatherIndex;
   }

   public UpdateEditorWeatherOverride(@Nonnull UpdateEditorWeatherOverride other) {
      this.weatherIndex = other.weatherIndex;
   }

   @Nonnull
   public static UpdateEditorWeatherOverride deserialize(@Nonnull ByteBuf buf, int offset) {
      UpdateEditorWeatherOverride obj = new UpdateEditorWeatherOverride();
      obj.weatherIndex = buf.getIntLE(offset + 0);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 4;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.weatherIndex);
   }

   @Override
   public int computeSize() {
      return 4;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 4 ? ValidationResult.error("Buffer too small: expected at least 4 bytes") : ValidationResult.OK;
   }

   public UpdateEditorWeatherOverride clone() {
      UpdateEditorWeatherOverride copy = new UpdateEditorWeatherOverride();
      copy.weatherIndex = this.weatherIndex;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof UpdateEditorWeatherOverride other ? this.weatherIndex == other.weatherIndex : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.weatherIndex);
   }
}
