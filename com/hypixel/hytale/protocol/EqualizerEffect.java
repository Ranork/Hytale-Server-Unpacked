package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EqualizerEffect {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 41;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 41;
   public static final int MAX_SIZE = 16384046;
   @Nullable
   public String id;
   public float lowGain;
   public float lowCutOff;
   public float lowMidGain;
   public float lowMidCenter;
   public float lowMidWidth;
   public float highMidGain;
   public float highMidCenter;
   public float highMidWidth;
   public float highGain;
   public float highCutOff;

   public EqualizerEffect() {
   }

   public EqualizerEffect(
      @Nullable String id,
      float lowGain,
      float lowCutOff,
      float lowMidGain,
      float lowMidCenter,
      float lowMidWidth,
      float highMidGain,
      float highMidCenter,
      float highMidWidth,
      float highGain,
      float highCutOff
   ) {
      this.id = id;
      this.lowGain = lowGain;
      this.lowCutOff = lowCutOff;
      this.lowMidGain = lowMidGain;
      this.lowMidCenter = lowMidCenter;
      this.lowMidWidth = lowMidWidth;
      this.highMidGain = highMidGain;
      this.highMidCenter = highMidCenter;
      this.highMidWidth = highMidWidth;
      this.highGain = highGain;
      this.highCutOff = highCutOff;
   }

   public EqualizerEffect(@Nonnull EqualizerEffect other) {
      this.id = other.id;
      this.lowGain = other.lowGain;
      this.lowCutOff = other.lowCutOff;
      this.lowMidGain = other.lowMidGain;
      this.lowMidCenter = other.lowMidCenter;
      this.lowMidWidth = other.lowMidWidth;
      this.highMidGain = other.highMidGain;
      this.highMidCenter = other.highMidCenter;
      this.highMidWidth = other.highMidWidth;
      this.highGain = other.highGain;
      this.highCutOff = other.highCutOff;
   }

   @Nonnull
   public static EqualizerEffect deserialize(@Nonnull ByteBuf buf, int offset) {
      EqualizerEffect obj = new EqualizerEffect();
      byte nullBits = buf.getByte(offset);
      obj.lowGain = buf.getFloatLE(offset + 1);
      obj.lowCutOff = buf.getFloatLE(offset + 5);
      obj.lowMidGain = buf.getFloatLE(offset + 9);
      obj.lowMidCenter = buf.getFloatLE(offset + 13);
      obj.lowMidWidth = buf.getFloatLE(offset + 17);
      obj.highMidGain = buf.getFloatLE(offset + 21);
      obj.highMidCenter = buf.getFloatLE(offset + 25);
      obj.highMidWidth = buf.getFloatLE(offset + 29);
      obj.highGain = buf.getFloatLE(offset + 33);
      obj.highCutOff = buf.getFloatLE(offset + 37);
      int pos = offset + 41;
      if ((nullBits & 1) != 0) {
         int idLen = VarInt.peek(buf, pos);
         if (idLen < 0) {
            throw ProtocolException.negativeLength("Id", idLen);
         }

         if (idLen > 4096000) {
            throw ProtocolException.stringTooLong("Id", idLen, 4096000);
         }

         int idVarLen = VarInt.length(buf, pos);
         obj.id = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
         pos += idVarLen + idLen;
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 41;
      if ((nullBits & 1) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos) + sl;
      }

      return pos - offset;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.id != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeFloatLE(this.lowGain);
      buf.writeFloatLE(this.lowCutOff);
      buf.writeFloatLE(this.lowMidGain);
      buf.writeFloatLE(this.lowMidCenter);
      buf.writeFloatLE(this.lowMidWidth);
      buf.writeFloatLE(this.highMidGain);
      buf.writeFloatLE(this.highMidCenter);
      buf.writeFloatLE(this.highMidWidth);
      buf.writeFloatLE(this.highGain);
      buf.writeFloatLE(this.highCutOff);
      if (this.id != null) {
         PacketIO.writeVarString(buf, this.id, 4096000);
      }
   }

   public int computeSize() {
      int size = 41;
      if (this.id != null) {
         size += PacketIO.stringSize(this.id);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 41) {
         return ValidationResult.error("Buffer too small: expected at least 41 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 41;
         if ((nullBits & 1) != 0) {
            int idLen = VarInt.peek(buffer, pos);
            if (idLen < 0) {
               return ValidationResult.error("Invalid string length for Id");
            }

            if (idLen > 4096000) {
               return ValidationResult.error("Id exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += idLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Id");
            }
         }

         return ValidationResult.OK;
      }
   }

   public EqualizerEffect clone() {
      EqualizerEffect copy = new EqualizerEffect();
      copy.id = this.id;
      copy.lowGain = this.lowGain;
      copy.lowCutOff = this.lowCutOff;
      copy.lowMidGain = this.lowMidGain;
      copy.lowMidCenter = this.lowMidCenter;
      copy.lowMidWidth = this.lowMidWidth;
      copy.highMidGain = this.highMidGain;
      copy.highMidCenter = this.highMidCenter;
      copy.highMidWidth = this.highMidWidth;
      copy.highGain = this.highGain;
      copy.highCutOff = this.highCutOff;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof EqualizerEffect other)
            ? false
            : Objects.equals(this.id, other.id)
               && this.lowGain == other.lowGain
               && this.lowCutOff == other.lowCutOff
               && this.lowMidGain == other.lowMidGain
               && this.lowMidCenter == other.lowMidCenter
               && this.lowMidWidth == other.lowMidWidth
               && this.highMidGain == other.highMidGain
               && this.highMidCenter == other.highMidCenter
               && this.highMidWidth == other.highMidWidth
               && this.highGain == other.highGain
               && this.highCutOff == other.highCutOff;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(
         this.id,
         this.lowGain,
         this.lowCutOff,
         this.lowMidGain,
         this.lowMidCenter,
         this.lowMidWidth,
         this.highMidGain,
         this.highMidCenter,
         this.highMidWidth,
         this.highGain,
         this.highCutOff
      );
   }
}
