package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AmbienceFXAmbientBed {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 6;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 6;
   public static final int MAX_SIZE = 16384011;
   @Nullable
   public String track;
   public float volume;
   @Nonnull
   public AmbienceTransitionSpeed transitionSpeed = AmbienceTransitionSpeed.Default;

   public AmbienceFXAmbientBed() {
   }

   public AmbienceFXAmbientBed(@Nullable String track, float volume, @Nonnull AmbienceTransitionSpeed transitionSpeed) {
      this.track = track;
      this.volume = volume;
      this.transitionSpeed = transitionSpeed;
   }

   public AmbienceFXAmbientBed(@Nonnull AmbienceFXAmbientBed other) {
      this.track = other.track;
      this.volume = other.volume;
      this.transitionSpeed = other.transitionSpeed;
   }

   @Nonnull
   public static AmbienceFXAmbientBed deserialize(@Nonnull ByteBuf buf, int offset) {
      AmbienceFXAmbientBed obj = new AmbienceFXAmbientBed();
      byte nullBits = buf.getByte(offset);
      obj.volume = buf.getFloatLE(offset + 1);
      obj.transitionSpeed = AmbienceTransitionSpeed.fromValue(buf.getByte(offset + 5));
      int pos = offset + 6;
      if ((nullBits & 1) != 0) {
         int trackLen = VarInt.peek(buf, pos);
         if (trackLen < 0) {
            throw ProtocolException.negativeLength("Track", trackLen);
         }

         if (trackLen > 4096000) {
            throw ProtocolException.stringTooLong("Track", trackLen, 4096000);
         }

         int trackVarLen = VarInt.length(buf, pos);
         obj.track = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
         pos += trackVarLen + trackLen;
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 6;
      if ((nullBits & 1) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos) + sl;
      }

      return pos - offset;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.track != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeFloatLE(this.volume);
      buf.writeByte(this.transitionSpeed.getValue());
      if (this.track != null) {
         PacketIO.writeVarString(buf, this.track, 4096000);
      }
   }

   public int computeSize() {
      int size = 6;
      if (this.track != null) {
         size += PacketIO.stringSize(this.track);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 6) {
         return ValidationResult.error("Buffer too small: expected at least 6 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 6;
         if ((nullBits & 1) != 0) {
            int trackLen = VarInt.peek(buffer, pos);
            if (trackLen < 0) {
               return ValidationResult.error("Invalid string length for Track");
            }

            if (trackLen > 4096000) {
               return ValidationResult.error("Track exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += trackLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Track");
            }
         }

         return ValidationResult.OK;
      }
   }

   public AmbienceFXAmbientBed clone() {
      AmbienceFXAmbientBed copy = new AmbienceFXAmbientBed();
      copy.track = this.track;
      copy.volume = this.volume;
      copy.transitionSpeed = this.transitionSpeed;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AmbienceFXAmbientBed other)
            ? false
            : Objects.equals(this.track, other.track) && this.volume == other.volume && Objects.equals(this.transitionSpeed, other.transitionSpeed);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.track, this.volume, this.transitionSpeed);
   }
}
