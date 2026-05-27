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

public class UpdateSunSettings implements Packet, ToClientPacket {
   public static final int PACKET_ID = 360;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 8;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 8;
   public static final int MAX_SIZE = 8;
   public float heightPercentage;
   public float angleRadians;

   @Override
   public int getId() {
      return 360;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateSunSettings() {
   }

   public UpdateSunSettings(float heightPercentage, float angleRadians) {
      this.heightPercentage = heightPercentage;
      this.angleRadians = angleRadians;
   }

   public UpdateSunSettings(@Nonnull UpdateSunSettings other) {
      this.heightPercentage = other.heightPercentage;
      this.angleRadians = other.angleRadians;
   }

   @Nonnull
   public static UpdateSunSettings deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 8) {
         throw ProtocolException.bufferTooSmall("UpdateSunSettings", 8, buf.readableBytes() - offset);
      } else {
         UpdateSunSettings obj = new UpdateSunSettings();
         obj.heightPercentage = buf.getFloatLE(offset + 0);
         obj.angleRadians = buf.getFloatLE(offset + 4);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 8;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 8L;
   }

   public static float getHeightPercentage(MemorySegment mem) {
      return getHeightPercentage(mem, 0);
   }

   public static float getHeightPercentage(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 0));
   }

   public static float getAngleRadians(MemorySegment mem) {
      return getAngleRadians(mem, 0);
   }

   public static float getAngleRadians(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 4));
   }

   public static UpdateSunSettings toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UpdateSunSettings toObject(MemorySegment mem, int offset) {
      if (offset + 8 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UpdateSunSettings", offset + 8, (int)mem.byteSize());
      } else {
         return new UpdateSunSettings(mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 0)), mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 4)));
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeFloatLE(this.heightPercentage);
      buf.writeFloatLE(this.angleRadians);
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 0), this.heightPercentage);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 4), this.angleRadians);
      return 8;
   }

   @Override
   public int computeSize() {
      return 8;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 8 ? ValidationResult.error("Buffer too small: expected at least 8 bytes") : ValidationResult.OK;
   }

   public UpdateSunSettings clone() {
      UpdateSunSettings copy = new UpdateSunSettings();
      copy.heightPercentage = this.heightPercentage;
      copy.angleRadians = this.angleRadians;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateSunSettings other) ? false : this.heightPercentage == other.heightPercentage && this.angleRadians == other.angleRadians;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.heightPercentage, this.angleRadians);
   }
}
