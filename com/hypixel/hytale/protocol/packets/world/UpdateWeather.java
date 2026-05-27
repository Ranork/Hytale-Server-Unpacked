package com.hypixel.hytale.protocol.packets.world;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class UpdateWeather implements Packet, ToClientPacket {
   public static final int PACKET_ID = 149;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 8;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 8;
   public static final int MAX_SIZE = 8;
   public int weatherIndex;
   public float transitionSeconds;

   @Override
   public int getId() {
      return 149;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateWeather() {
   }

   public UpdateWeather(int weatherIndex, float transitionSeconds) {
      this.weatherIndex = weatherIndex;
      this.transitionSeconds = transitionSeconds;
   }

   public UpdateWeather(@Nonnull UpdateWeather other) {
      this.weatherIndex = other.weatherIndex;
      this.transitionSeconds = other.transitionSeconds;
   }

   @Nonnull
   public static UpdateWeather deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 8) {
         throw ProtocolException.bufferTooSmall("UpdateWeather", 8, buf.readableBytes() - offset);
      } else {
         UpdateWeather obj = new UpdateWeather();
         obj.weatherIndex = buf.getIntLE(offset + 0);
         obj.transitionSeconds = buf.getFloatLE(offset + 4);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 8;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 8L;
   }

   public static int getWeatherIndex(MemorySegment mem) {
      return getWeatherIndex(mem, 0);
   }

   public static int getWeatherIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 0));
   }

   public static float getTransitionSeconds(MemorySegment mem) {
      return getTransitionSeconds(mem, 0);
   }

   public static float getTransitionSeconds(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 4));
   }

   public static UpdateWeather toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UpdateWeather toObject(MemorySegment mem, int offset) {
      if (offset + 8 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UpdateWeather", offset + 8, (int)mem.byteSize());
      } else {
         return new UpdateWeather(mem.get(PacketIO.PROTO_INT, (long)(offset + 0)), mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 4)));
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.weatherIndex);
      buf.writeFloatLE(this.transitionSeconds);
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_INT, (long)(offset + 0), this.weatherIndex);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 4), this.transitionSeconds);
      return 8;
   }

   @Override
   public int computeSize() {
      return 8;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 8 ? ValidationResult.error("Buffer too small: expected at least 8 bytes") : ValidationResult.OK;
   }

   public UpdateWeather clone() {
      UpdateWeather copy = new UpdateWeather();
      copy.weatherIndex = this.weatherIndex;
      copy.transitionSeconds = this.transitionSeconds;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateWeather other) ? false : this.weatherIndex == other.weatherIndex && this.transitionSeconds == other.transitionSeconds;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.weatherIndex, this.transitionSeconds);
   }
}
