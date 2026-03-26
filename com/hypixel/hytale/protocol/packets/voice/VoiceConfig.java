package com.hypixel.hytale.protocol.packets.voice;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
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

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 17;
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
   public int computeSize() {
      return 17;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 17 ? ValidationResult.error("Buffer too small: expected at least 17 bytes") : ValidationResult.OK;
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
