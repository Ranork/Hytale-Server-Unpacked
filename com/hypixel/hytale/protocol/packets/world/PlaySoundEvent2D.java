package com.hypixel.hytale.protocol.packets.world;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
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
      PlaySoundEvent2D obj = new PlaySoundEvent2D();
      obj.soundEventIndex = buf.getIntLE(offset + 0);
      obj.category = SoundCategory.fromValue(buf.getByte(offset + 4));
      obj.volumeModifier = buf.getFloatLE(offset + 5);
      obj.pitchModifier = buf.getFloatLE(offset + 9);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 13;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.soundEventIndex);
      buf.writeByte(this.category.getValue());
      buf.writeFloatLE(this.volumeModifier);
      buf.writeFloatLE(this.pitchModifier);
   }

   @Override
   public int computeSize() {
      return 13;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 13 ? ValidationResult.error("Buffer too small: expected at least 13 bytes") : ValidationResult.OK;
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
