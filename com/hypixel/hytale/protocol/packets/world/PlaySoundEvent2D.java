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

public class PlaySoundEvent2D implements Packet, ToClientPacket {
   public static final int PACKET_ID = 154;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 13;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 13;
   public static final int MAX_SIZE = 13;
   public int soundEventIndex;
   @Nonnull
   public SoundCategory category = SoundCategory.Music;
   public float volumeModifier;
   public float pitchModifier;

   @Override
   public int getId() {
      return 154;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public PlaySoundEvent2D() {
   }

   public PlaySoundEvent2D(int soundEventIndex, @Nonnull SoundCategory category, float volumeModifier, float pitchModifier) {
      this.soundEventIndex = soundEventIndex;
      this.category = category;
      this.volumeModifier = volumeModifier;
      this.pitchModifier = pitchModifier;
   }

   public PlaySoundEvent2D(@Nonnull PlaySoundEvent2D other) {
      this.soundEventIndex = other.soundEventIndex;
      this.category = other.category;
      this.volumeModifier = other.volumeModifier;
      this.pitchModifier = other.pitchModifier;
   }

   @Nonnull
   public static PlaySoundEvent2D deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 13) {
         throw ProtocolException.bufferTooSmall("PlaySoundEvent2D", 13, buf.readableBytes() - offset);
      } else {
         PlaySoundEvent2D obj = new PlaySoundEvent2D();
         obj.soundEventIndex = buf.getIntLE(offset + 0);
         obj.category = SoundCategory.fromValue(buf.getByte(offset + 4));
         obj.volumeModifier = buf.getFloatLE(offset + 5);
         obj.pitchModifier = buf.getFloatLE(offset + 9);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 13;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 13L;
   }

   public static int getSoundEventIndex(MemorySegment mem) {
      return getSoundEventIndex(mem, 0);
   }

   public static int getSoundEventIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 0));
   }

   public static SoundCategory getCategory(MemorySegment mem) {
      return getCategory(mem, 0);
   }

   public static SoundCategory getCategory(MemorySegment mem, int offset) {
      return SoundCategory.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 4)));
   }

   public static float getVolumeModifier(MemorySegment mem) {
      return getVolumeModifier(mem, 0);
   }

   public static float getVolumeModifier(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 5));
   }

   public static float getPitchModifier(MemorySegment mem) {
      return getPitchModifier(mem, 0);
   }

   public static float getPitchModifier(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 9));
   }

   public static PlaySoundEvent2D toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static PlaySoundEvent2D toObject(MemorySegment mem, int offset) {
      if (offset + 13 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("PlaySoundEvent2D", offset + 13, (int)mem.byteSize());
      } else {
         return new PlaySoundEvent2D(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 0)),
            SoundCategory.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 4))),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 5)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 9))
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.soundEventIndex);
      buf.writeByte(this.category.getValue());
      buf.writeFloatLE(this.volumeModifier);
      buf.writeFloatLE(this.pitchModifier);
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_INT, (long)(offset + 0), this.soundEventIndex);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 4), (byte)this.category.getValue());
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 5), this.volumeModifier);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 9), this.pitchModifier);
      return 13;
   }

   @Override
   public int computeSize() {
      return 13;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 13) {
         return ValidationResult.error("Buffer too small: expected at least 13 bytes");
      } else {
         int v = buffer.getByte(offset + 4) & 255;
         return v >= 5 ? ValidationResult.error("Invalid SoundCategory value for Category") : ValidationResult.OK;
      }
   }

   public PlaySoundEvent2D clone() {
      PlaySoundEvent2D copy = new PlaySoundEvent2D();
      copy.soundEventIndex = this.soundEventIndex;
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
         return !(obj instanceof PlaySoundEvent2D other)
            ? false
            : this.soundEventIndex == other.soundEventIndex
               && Objects.equals(this.category, other.category)
               && this.volumeModifier == other.volumeModifier
               && this.pitchModifier == other.pitchModifier;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.soundEventIndex, this.category, this.volumeModifier, this.pitchModifier);
   }
}
