package com.hypixel.hytale.protocol.packets.world;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.Position;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlaySoundEvent3D implements Packet, ToClientPacket {
   public static final int PACKET_ID = 155;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 38;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 38;
   public static final int MAX_SIZE = 38;
   public int soundEventIndex;
   @Nonnull
   public SoundCategory category = SoundCategory.Music;
   @Nullable
   public Position position;
   public float volumeModifier;
   public float pitchModifier;

   @Override
   public int getId() {
      return 155;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public PlaySoundEvent3D() {
   }

   public PlaySoundEvent3D(int soundEventIndex, @Nonnull SoundCategory category, @Nullable Position position, float volumeModifier, float pitchModifier) {
      this.soundEventIndex = soundEventIndex;
      this.category = category;
      this.position = position;
      this.volumeModifier = volumeModifier;
      this.pitchModifier = pitchModifier;
   }

   public PlaySoundEvent3D(@Nonnull PlaySoundEvent3D other) {
      this.soundEventIndex = other.soundEventIndex;
      this.category = other.category;
      this.position = other.position;
      this.volumeModifier = other.volumeModifier;
      this.pitchModifier = other.pitchModifier;
   }

   @Nonnull
   public static PlaySoundEvent3D deserialize(@Nonnull ByteBuf buf, int offset) {
      PlaySoundEvent3D obj = new PlaySoundEvent3D();
      byte nullBits = buf.getByte(offset);
      obj.soundEventIndex = buf.getIntLE(offset + 1);
      obj.category = SoundCategory.fromValue(buf.getByte(offset + 5));
      if ((nullBits & 1) != 0) {
         obj.position = Position.deserialize(buf, offset + 6);
      }

      obj.volumeModifier = buf.getFloatLE(offset + 30);
      obj.pitchModifier = buf.getFloatLE(offset + 34);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 38;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.position != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.soundEventIndex);
      buf.writeByte(this.category.getValue());
      if (this.position != null) {
         this.position.serialize(buf);
      } else {
         buf.writeZero(24);
      }

      buf.writeFloatLE(this.volumeModifier);
      buf.writeFloatLE(this.pitchModifier);
   }

   @Override
   public int computeSize() {
      return 38;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 38 ? ValidationResult.error("Buffer too small: expected at least 38 bytes") : ValidationResult.OK;
   }

   public PlaySoundEvent3D clone() {
      PlaySoundEvent3D copy = new PlaySoundEvent3D();
      copy.soundEventIndex = this.soundEventIndex;
      copy.category = this.category;
      copy.position = this.position != null ? this.position.clone() : null;
      copy.volumeModifier = this.volumeModifier;
      copy.pitchModifier = this.pitchModifier;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof PlaySoundEvent3D other)
            ? false
            : this.soundEventIndex == other.soundEventIndex
               && Objects.equals(this.category, other.category)
               && Objects.equals(this.position, other.position)
               && this.volumeModifier == other.volumeModifier
               && this.pitchModifier == other.pitchModifier;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.soundEventIndex, this.category, this.position, this.volumeModifier, this.pitchModifier);
   }
}
