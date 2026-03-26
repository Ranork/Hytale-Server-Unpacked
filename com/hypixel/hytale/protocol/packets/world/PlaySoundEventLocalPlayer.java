package com.hypixel.hytale.protocol.packets.world;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
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
      PlaySoundEventLocalPlayer obj = new PlaySoundEventLocalPlayer();
      obj.localSoundEventIndex = buf.getIntLE(offset + 0);
      obj.worldSoundEventIndex = buf.getIntLE(offset + 4);
      obj.category = SoundCategory.fromValue(buf.getByte(offset + 8));
      obj.volumeModifier = buf.getFloatLE(offset + 9);
      obj.pitchModifier = buf.getFloatLE(offset + 13);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 17;
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
   public int computeSize() {
      return 17;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 17 ? ValidationResult.error("Buffer too small: expected at least 17 bytes") : ValidationResult.OK;
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
