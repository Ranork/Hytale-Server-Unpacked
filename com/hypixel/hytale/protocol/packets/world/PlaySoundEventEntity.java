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

public class PlaySoundEventEntity implements Packet, ToClientPacket {
   public static final int PACKET_ID = 156;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 16;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 16;
   public static final int MAX_SIZE = 16;
   public int soundEventIndex;
   public int networkId;
   public float volumeModifier;
   public float pitchModifier;

   @Override
   public int getId() {
      return 156;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public PlaySoundEventEntity() {
   }

   public PlaySoundEventEntity(int soundEventIndex, int networkId, float volumeModifier, float pitchModifier) {
      this.soundEventIndex = soundEventIndex;
      this.networkId = networkId;
      this.volumeModifier = volumeModifier;
      this.pitchModifier = pitchModifier;
   }

   public PlaySoundEventEntity(@Nonnull PlaySoundEventEntity other) {
      this.soundEventIndex = other.soundEventIndex;
      this.networkId = other.networkId;
      this.volumeModifier = other.volumeModifier;
      this.pitchModifier = other.pitchModifier;
   }

   @Nonnull
   public static PlaySoundEventEntity deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 16) {
         throw ProtocolException.bufferTooSmall("PlaySoundEventEntity", 16, buf.readableBytes() - offset);
      } else {
         PlaySoundEventEntity obj = new PlaySoundEventEntity();
         obj.soundEventIndex = buf.getIntLE(offset + 0);
         obj.networkId = buf.getIntLE(offset + 4);
         obj.volumeModifier = buf.getFloatLE(offset + 8);
         obj.pitchModifier = buf.getFloatLE(offset + 12);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 16;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 16L;
   }

   public static int getSoundEventIndex(MemorySegment mem) {
      return getSoundEventIndex(mem, 0);
   }

   public static int getSoundEventIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 0));
   }

   public static int getNetworkId(MemorySegment mem) {
      return getNetworkId(mem, 0);
   }

   public static int getNetworkId(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 4));
   }

   public static float getVolumeModifier(MemorySegment mem) {
      return getVolumeModifier(mem, 0);
   }

   public static float getVolumeModifier(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 8));
   }

   public static float getPitchModifier(MemorySegment mem) {
      return getPitchModifier(mem, 0);
   }

   public static float getPitchModifier(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 12));
   }

   public static PlaySoundEventEntity toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static PlaySoundEventEntity toObject(MemorySegment mem, int offset) {
      if (offset + 16 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("PlaySoundEventEntity", offset + 16, (int)mem.byteSize());
      } else {
         return new PlaySoundEventEntity(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 0)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 4)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 8)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 12))
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.soundEventIndex);
      buf.writeIntLE(this.networkId);
      buf.writeFloatLE(this.volumeModifier);
      buf.writeFloatLE(this.pitchModifier);
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_INT, (long)(offset + 0), this.soundEventIndex);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 4), this.networkId);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 8), this.volumeModifier);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 12), this.pitchModifier);
      return 16;
   }

   @Override
   public int computeSize() {
      return 16;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 16 ? ValidationResult.error("Buffer too small: expected at least 16 bytes") : ValidationResult.OK;
   }

   public PlaySoundEventEntity clone() {
      PlaySoundEventEntity copy = new PlaySoundEventEntity();
      copy.soundEventIndex = this.soundEventIndex;
      copy.networkId = this.networkId;
      copy.volumeModifier = this.volumeModifier;
      copy.pitchModifier = this.pitchModifier;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof PlaySoundEventEntity other)
            ? false
            : this.soundEventIndex == other.soundEventIndex
               && this.networkId == other.networkId
               && this.volumeModifier == other.volumeModifier
               && this.pitchModifier == other.pitchModifier;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.soundEventIndex, this.networkId, this.volumeModifier, this.pitchModifier);
   }
}
