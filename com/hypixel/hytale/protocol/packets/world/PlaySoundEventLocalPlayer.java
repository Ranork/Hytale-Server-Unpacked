package com.hypixel.hytale.protocol.packets.world;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class PlaySoundEventLocalPlayer implements Packet, ToClientPacket {
   public static final int PACKET_ID = 362;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 17;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 17;
   public static final int MAX_SIZE = 17;
   public int localSoundEventIndex;
   public int worldSoundEventIndex;
   @Nonnull
   public SoundCategory category = SoundCategory.Music;
   public float volumeModifier;
   public float pitchModifier;

   @Override
   public int getId() {
      return 362;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public PlaySoundEventLocalPlayer() {
   }

   public PlaySoundEventLocalPlayer(
      int localSoundEventIndex, int worldSoundEventIndex, @Nonnull SoundCategory category, float volumeModifier, float pitchModifier
   ) {
      this.localSoundEventIndex = localSoundEventIndex;
      this.worldSoundEventIndex = worldSoundEventIndex;
      this.category = category;
      this.volumeModifier = volumeModifier;
      this.pitchModifier = pitchModifier;
   }

   public PlaySoundEventLocalPlayer(@Nonnull PlaySoundEventLocalPlayer other) {
      this.localSoundEventIndex = other.localSoundEventIndex;
      this.worldSoundEventIndex = other.worldSoundEventIndex;
      this.category = other.category;
      this.volumeModifier = other.volumeModifier;
      this.pitchModifier = other.pitchModifier;
   }

   @Nonnull
   public static PlaySoundEventLocalPlayer deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 17) {
         throw ProtocolException.bufferTooSmall("PlaySoundEventLocalPlayer", 17, buf.readableBytes() - offset);
      } else {
         PlaySoundEventLocalPlayer obj = new PlaySoundEventLocalPlayer();
         obj.localSoundEventIndex = buf.getIntLE(offset + 0);
         obj.worldSoundEventIndex = buf.getIntLE(offset + 4);
         obj.category = SoundCategory.fromValue(buf.getByte(offset + 8));
         obj.volumeModifier = buf.getFloatLE(offset + 9);
         obj.pitchModifier = buf.getFloatLE(offset + 13);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 17;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 17L;
   }

   public static int getLocalSoundEventIndex(MemorySegment mem) {
      return getLocalSoundEventIndex(mem, 0);
   }

   public static int getLocalSoundEventIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 0));
   }

   public static int getWorldSoundEventIndex(MemorySegment mem) {
      return getWorldSoundEventIndex(mem, 0);
   }

   public static int getWorldSoundEventIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 4));
   }

   public static SoundCategory getCategory(MemorySegment mem) {
      return getCategory(mem, 0);
   }

   public static SoundCategory getCategory(MemorySegment mem, int offset) {
      return SoundCategory.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 8)));
   }

   public static float getVolumeModifier(MemorySegment mem) {
      return getVolumeModifier(mem, 0);
   }

   public static float getVolumeModifier(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 9));
   }

   public static float getPitchModifier(MemorySegment mem) {
      return getPitchModifier(mem, 0);
   }

   public static float getPitchModifier(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 13));
   }

   public static PlaySoundEventLocalPlayer toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static PlaySoundEventLocalPlayer toObject(MemorySegment mem, int offset) {
      if (offset + 17 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("PlaySoundEventLocalPlayer", offset + 17, (int)mem.byteSize());
      } else {
         return new PlaySoundEventLocalPlayer(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 0)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 4)),
            SoundCategory.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 8))),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 9)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 13))
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.localSoundEventIndex);
      buf.writeIntLE(this.worldSoundEventIndex);
      buf.writeByte(this.category.getValue());
      buf.writeFloatLE(this.volumeModifier);
      buf.writeFloatLE(this.pitchModifier);
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_INT, (long)(offset + 0), this.localSoundEventIndex);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 4), this.worldSoundEventIndex);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 8), (byte)this.category.getValue());
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 9), this.volumeModifier);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 13), this.pitchModifier);
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
         int v = buffer.getByte(offset + 8) & 255;
         return v >= 5 ? ValidationResult.error("Invalid SoundCategory value for Category") : ValidationResult.OK;
      }
   }

   public PlaySoundEventLocalPlayer clone() {
      PlaySoundEventLocalPlayer copy = new PlaySoundEventLocalPlayer();
      copy.localSoundEventIndex = this.localSoundEventIndex;
      copy.worldSoundEventIndex = this.worldSoundEventIndex;
      copy.category = this.category;
      copy.volumeModifier = this.volumeModifier;
      copy.pitchModifier = this.pitchModifier;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof PlaySoundEventLocalPlayer other)
            ? false
            : this.localSoundEventIndex == other.localSoundEventIndex
               && this.worldSoundEventIndex == other.worldSoundEventIndex
               && Objects.equals(this.category, other.category)
               && this.volumeModifier == other.volumeModifier
               && this.pitchModifier == other.pitchModifier;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.localSoundEventIndex, this.worldSoundEventIndex, this.category, this.volumeModifier, this.pitchModifier);
   }
}
