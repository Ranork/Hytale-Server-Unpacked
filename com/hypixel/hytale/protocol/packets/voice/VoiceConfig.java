package com.hypixel.hytale.protocol.packets.voice;

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

public class VoiceConfig implements Packet, ToClientPacket {
   public static final int PACKET_ID = 452;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 17;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 17;
   public static final int MAX_SIZE = 17;
   public boolean voiceEnabled;
   @Nonnull
   public VoiceCodec codec = VoiceCodec.Opus;
   public int sampleRate;
   public byte channels;
   public float maxHearingDistance;
   public float referenceDistance;
   public boolean supportsVoiceStream;
   public byte maxPacketsPerSecond;

   @Override
   public int getId() {
      return 452;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public VoiceConfig() {
   }

   public VoiceConfig(
      boolean voiceEnabled,
      @Nonnull VoiceCodec codec,
      int sampleRate,
      byte channels,
      float maxHearingDistance,
      float referenceDistance,
      boolean supportsVoiceStream,
      byte maxPacketsPerSecond
   ) {
      this.voiceEnabled = voiceEnabled;
      this.codec = codec;
      this.sampleRate = sampleRate;
      this.channels = channels;
      this.maxHearingDistance = maxHearingDistance;
      this.referenceDistance = referenceDistance;
      this.supportsVoiceStream = supportsVoiceStream;
      this.maxPacketsPerSecond = maxPacketsPerSecond;
   }

   public VoiceConfig(@Nonnull VoiceConfig other) {
      this.voiceEnabled = other.voiceEnabled;
      this.codec = other.codec;
      this.sampleRate = other.sampleRate;
      this.channels = other.channels;
      this.maxHearingDistance = other.maxHearingDistance;
      this.referenceDistance = other.referenceDistance;
      this.supportsVoiceStream = other.supportsVoiceStream;
      this.maxPacketsPerSecond = other.maxPacketsPerSecond;
   }

   @Nonnull
   public static VoiceConfig deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 17) {
         throw ProtocolException.bufferTooSmall("VoiceConfig", 17, buf.readableBytes() - offset);
      } else {
         VoiceConfig obj = new VoiceConfig();
         obj.voiceEnabled = buf.getByte(offset + 0) != 0;
         obj.codec = VoiceCodec.fromValue(buf.getByte(offset + 1));
         obj.sampleRate = buf.getIntLE(offset + 2);
         obj.channels = buf.getByte(offset + 6);
         obj.maxHearingDistance = buf.getFloatLE(offset + 7);
         obj.referenceDistance = buf.getFloatLE(offset + 11);
         obj.supportsVoiceStream = buf.getByte(offset + 15) != 0;
         obj.maxPacketsPerSecond = buf.getByte(offset + 16);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 17;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 17L;
   }

   public static boolean getVoiceEnabled(MemorySegment mem) {
      return getVoiceEnabled(mem, 0);
   }

   public static boolean getVoiceEnabled(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 0));
   }

   public static VoiceCodec getCodec(MemorySegment mem) {
      return getCodec(mem, 0);
   }

   public static VoiceCodec getCodec(MemorySegment mem, int offset) {
      return VoiceCodec.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1)));
   }

   public static int getSampleRate(MemorySegment mem) {
      return getSampleRate(mem, 0);
   }

   public static int getSampleRate(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 2));
   }

   public static byte getChannels(MemorySegment mem) {
      return getChannels(mem, 0);
   }

   public static byte getChannels(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BYTE, (long)(offset + 6));
   }

   public static float getMaxHearingDistance(MemorySegment mem) {
      return getMaxHearingDistance(mem, 0);
   }

   public static float getMaxHearingDistance(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 7));
   }

   public static float getReferenceDistance(MemorySegment mem) {
      return getReferenceDistance(mem, 0);
   }

   public static float getReferenceDistance(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 11));
   }

   public static boolean getSupportsVoiceStream(MemorySegment mem) {
      return getSupportsVoiceStream(mem, 0);
   }

   public static boolean getSupportsVoiceStream(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 15));
   }

   public static byte getMaxPacketsPerSecond(MemorySegment mem) {
      return getMaxPacketsPerSecond(mem, 0);
   }

   public static byte getMaxPacketsPerSecond(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BYTE, (long)(offset + 16));
   }

   public static VoiceConfig toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static VoiceConfig toObject(MemorySegment mem, int offset) {
      if (offset + 17 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("VoiceConfig", offset + 17, (int)mem.byteSize());
      } else {
         return new VoiceConfig(
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 0)),
            VoiceCodec.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 2)),
            mem.get(PacketIO.PROTO_BYTE, (long)(offset + 6)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 7)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 11)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 15)),
            mem.get(PacketIO.PROTO_BYTE, (long)(offset + 16))
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.voiceEnabled ? 1 : 0);
      buf.writeByte(this.codec.getValue());
      buf.writeIntLE(this.sampleRate);
      buf.writeByte(this.channels);
      buf.writeFloatLE(this.maxHearingDistance);
      buf.writeFloatLE(this.referenceDistance);
      buf.writeByte(this.supportsVoiceStream ? 1 : 0);
      buf.writeByte(this.maxPacketsPerSecond);
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_BOOL, offset + 0, this.voiceEnabled);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.codec.getValue());
      mem.set(PacketIO.PROTO_INT, (long)(offset + 2), this.sampleRate);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 6), this.channels);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 7), this.maxHearingDistance);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 11), this.referenceDistance);
      mem.set(PacketIO.PROTO_BOOL, offset + 15, this.supportsVoiceStream);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 16), this.maxPacketsPerSecond);
      return 17;
   }

   @Override
   public int computeSize() {
      return 17;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 17) {
         return ValidationResult.error("Buffer too small: expected at least 17 bytes");
      } else {
         int v = buffer.getByte(offset + 1) & 255;
         return v >= 1 ? ValidationResult.error("Invalid VoiceCodec value for Codec") : ValidationResult.OK;
      }
   }

   public VoiceConfig clone() {
      VoiceConfig copy = new VoiceConfig();
      copy.voiceEnabled = this.voiceEnabled;
      copy.codec = this.codec;
      copy.sampleRate = this.sampleRate;
      copy.channels = this.channels;
      copy.maxHearingDistance = this.maxHearingDistance;
      copy.referenceDistance = this.referenceDistance;
      copy.supportsVoiceStream = this.supportsVoiceStream;
      copy.maxPacketsPerSecond = this.maxPacketsPerSecond;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof VoiceConfig other)
            ? false
            : this.voiceEnabled == other.voiceEnabled
               && Objects.equals(this.codec, other.codec)
               && this.sampleRate == other.sampleRate
               && this.channels == other.channels
               && this.maxHearingDistance == other.maxHearingDistance
               && this.referenceDistance == other.referenceDistance
               && this.supportsVoiceStream == other.supportsVoiceStream
               && this.maxPacketsPerSecond == other.maxPacketsPerSecond;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(
         this.voiceEnabled,
         this.codec,
         this.sampleRate,
         this.channels,
         this.maxHearingDistance,
         this.referenceDistance,
         this.supportsVoiceStream,
         this.maxPacketsPerSecond
      );
   }
}
