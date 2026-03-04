package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AmbienceFXMusic {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 5;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public String[] tracks;
   public float volume;

   public AmbienceFXMusic() {
   }

   public AmbienceFXMusic(@Nullable String[] tracks, float volume) {
      this.tracks = tracks;
      this.volume = volume;
   }

   public AmbienceFXMusic(@Nonnull AmbienceFXMusic other) {
      this.tracks = other.tracks;
      this.volume = other.volume;
   }

   @Nonnull
   public static AmbienceFXMusic deserialize(@Nonnull ByteBuf buf, int offset) {
      AmbienceFXMusic obj = new AmbienceFXMusic();
      byte nullBits = buf.getByte(offset);
      obj.volume = buf.getFloatLE(offset + 1);
      int pos = offset + 5;
      if ((nullBits & 1) != 0) {
         int tracksCount = VarInt.peek(buf, pos);
         if (tracksCount < 0) {
            throw ProtocolException.negativeLength("Tracks", tracksCount);
         }

         if (tracksCount > 4096000) {
            throw ProtocolException.arrayTooLong("Tracks", tracksCount, 4096000);
         }

         int tracksVarLen = VarInt.size(tracksCount);
         if (pos + tracksVarLen + tracksCount * 1L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Tracks", pos + tracksVarLen + tracksCount * 1, buf.readableBytes());
         }

         pos += tracksVarLen;
         obj.tracks = new String[tracksCount];

         for (int i = 0; i < tracksCount; i++) {
            int strLen = VarInt.peek(buf, pos);
            if (strLen < 0) {
               throw ProtocolException.negativeLength("tracks[" + i + "]", strLen);
            }

            if (strLen > 4096000) {
               throw ProtocolException.stringTooLong("tracks[" + i + "]", strLen, 4096000);
            }

            int strVarLen = VarInt.length(buf, pos);
            obj.tracks[i] = PacketIO.readVarString(buf, pos);
            pos += strVarLen + strLen;
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 5;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos);

         for (int i = 0; i < arrLen; i++) {
            int sl = VarInt.peek(buf, pos);
            pos += VarInt.length(buf, pos) + sl;
         }
      }

      return pos - offset;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.tracks != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeFloatLE(this.volume);
      if (this.tracks != null) {
         if (this.tracks.length > 4096000) {
            throw ProtocolException.arrayTooLong("Tracks", this.tracks.length, 4096000);
         }

         VarInt.write(buf, this.tracks.length);

         for (String item : this.tracks) {
            PacketIO.writeVarString(buf, item, 4096000);
         }
      }
   }

   public int computeSize() {
      int size = 5;
      if (this.tracks != null) {
         int tracksSize = 0;

         for (String elem : this.tracks) {
            tracksSize += PacketIO.stringSize(elem);
         }

         size += VarInt.size(this.tracks.length) + tracksSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 5) {
         return ValidationResult.error("Buffer too small: expected at least 5 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 5;
         if ((nullBits & 1) != 0) {
            int tracksCount = VarInt.peek(buffer, pos);
            if (tracksCount < 0) {
               return ValidationResult.error("Invalid array count for Tracks");
            }

            if (tracksCount > 4096000) {
               return ValidationResult.error("Tracks exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for (int i = 0; i < tracksCount; i++) {
               int strLen = VarInt.peek(buffer, pos);
               if (strLen < 0) {
                  return ValidationResult.error("Invalid string length in Tracks");
               }

               pos += VarInt.length(buffer, pos);
               pos += strLen;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading string in Tracks");
               }
            }
         }

         return ValidationResult.OK;
      }
   }

   public AmbienceFXMusic clone() {
      AmbienceFXMusic copy = new AmbienceFXMusic();
      copy.tracks = this.tracks != null ? Arrays.copyOf(this.tracks, this.tracks.length) : null;
      copy.volume = this.volume;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AmbienceFXMusic other) ? false : Arrays.equals((Object[])this.tracks, (Object[])other.tracks) && this.volume == other.volume;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Arrays.hashCode((Object[])this.tracks);
      return 31 * result + Float.hashCode(this.volume);
   }
}
