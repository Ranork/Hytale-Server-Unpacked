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

public class UpdatePostFxSettings implements Packet, ToClientPacket {
   public static final int PACKET_ID = 361;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 20;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 20;
   public static final int MAX_SIZE = 20;
   public float globalIntensity;
   public float power;
   public float sunshaftScale;
   public float sunIntensity;
   public float sunshaftIntensity;

   @Override
   public int getId() {
      return 361;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdatePostFxSettings() {
   }

   public UpdatePostFxSettings(float globalIntensity, float power, float sunshaftScale, float sunIntensity, float sunshaftIntensity) {
      this.globalIntensity = globalIntensity;
      this.power = power;
      this.sunshaftScale = sunshaftScale;
      this.sunIntensity = sunIntensity;
      this.sunshaftIntensity = sunshaftIntensity;
   }

   public UpdatePostFxSettings(@Nonnull UpdatePostFxSettings other) {
      this.globalIntensity = other.globalIntensity;
      this.power = other.power;
      this.sunshaftScale = other.sunshaftScale;
      this.sunIntensity = other.sunIntensity;
      this.sunshaftIntensity = other.sunshaftIntensity;
   }

   @Nonnull
   public static UpdatePostFxSettings deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 20) {
         throw ProtocolException.bufferTooSmall("UpdatePostFxSettings", 20, buf.readableBytes() - offset);
      } else {
         UpdatePostFxSettings obj = new UpdatePostFxSettings();
         obj.globalIntensity = buf.getFloatLE(offset + 0);
         obj.power = buf.getFloatLE(offset + 4);
         obj.sunshaftScale = buf.getFloatLE(offset + 8);
         obj.sunIntensity = buf.getFloatLE(offset + 12);
         obj.sunshaftIntensity = buf.getFloatLE(offset + 16);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 20;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 20L;
   }

   public static float getGlobalIntensity(MemorySegment mem) {
      return getGlobalIntensity(mem, 0);
   }

   public static float getGlobalIntensity(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 0));
   }

   public static float getPower(MemorySegment mem) {
      return getPower(mem, 0);
   }

   public static float getPower(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 4));
   }

   public static float getSunshaftScale(MemorySegment mem) {
      return getSunshaftScale(mem, 0);
   }

   public static float getSunshaftScale(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 8));
   }

   public static float getSunIntensity(MemorySegment mem) {
      return getSunIntensity(mem, 0);
   }

   public static float getSunIntensity(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 12));
   }

   public static float getSunshaftIntensity(MemorySegment mem) {
      return getSunshaftIntensity(mem, 0);
   }

   public static float getSunshaftIntensity(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 16));
   }

   public static UpdatePostFxSettings toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UpdatePostFxSettings toObject(MemorySegment mem, int offset) {
      if (offset + 20 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UpdatePostFxSettings", offset + 20, (int)mem.byteSize());
      } else {
         return new UpdatePostFxSettings(
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 0)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 4)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 8)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 12)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 16))
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeFloatLE(this.globalIntensity);
      buf.writeFloatLE(this.power);
      buf.writeFloatLE(this.sunshaftScale);
      buf.writeFloatLE(this.sunIntensity);
      buf.writeFloatLE(this.sunshaftIntensity);
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 0), this.globalIntensity);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 4), this.power);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 8), this.sunshaftScale);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 12), this.sunIntensity);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 16), this.sunshaftIntensity);
      return 20;
   }

   @Override
   public int computeSize() {
      return 20;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 20 ? ValidationResult.error("Buffer too small: expected at least 20 bytes") : ValidationResult.OK;
   }

   public UpdatePostFxSettings clone() {
      UpdatePostFxSettings copy = new UpdatePostFxSettings();
      copy.globalIntensity = this.globalIntensity;
      copy.power = this.power;
      copy.sunshaftScale = this.sunshaftScale;
      copy.sunIntensity = this.sunIntensity;
      copy.sunshaftIntensity = this.sunshaftIntensity;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdatePostFxSettings other)
            ? false
            : this.globalIntensity == other.globalIntensity
               && this.power == other.power
               && this.sunshaftScale == other.sunshaftScale
               && this.sunIntensity == other.sunIntensity
               && this.sunshaftIntensity == other.sunshaftIntensity;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.globalIntensity, this.power, this.sunshaftScale, this.sunIntensity, this.sunshaftIntensity);
   }
}
