package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InteractionCooldown {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 8;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 16;
   public static final int MAX_SIZE = 32768026;
   @Nullable
   public String cooldownId;
   public float cooldown;
   public boolean clickBypass;
   @Nullable
   public float[] chargeTimes;
   public boolean skipCooldownReset;
   public boolean interruptRecharge;

   public InteractionCooldown() {
   }

   public InteractionCooldown(
      @Nullable String cooldownId, float cooldown, boolean clickBypass, @Nullable float[] chargeTimes, boolean skipCooldownReset, boolean interruptRecharge
   ) {
      this.cooldownId = cooldownId;
      this.cooldown = cooldown;
      this.clickBypass = clickBypass;
      this.chargeTimes = chargeTimes;
      this.skipCooldownReset = skipCooldownReset;
      this.interruptRecharge = interruptRecharge;
   }

   public InteractionCooldown(@Nonnull InteractionCooldown other) {
      this.cooldownId = other.cooldownId;
      this.cooldown = other.cooldown;
      this.clickBypass = other.clickBypass;
      this.chargeTimes = other.chargeTimes;
      this.skipCooldownReset = other.skipCooldownReset;
      this.interruptRecharge = other.interruptRecharge;
   }

   @Nonnull
   public static InteractionCooldown deserialize(@Nonnull ByteBuf buf, int offset) {
      InteractionCooldown obj = new InteractionCooldown();
      byte nullBits = buf.getByte(offset);
      obj.cooldown = buf.getFloatLE(offset + 1);
      obj.clickBypass = buf.getByte(offset + 5) != 0;
      obj.skipCooldownReset = buf.getByte(offset + 6) != 0;
      obj.interruptRecharge = buf.getByte(offset + 7) != 0;
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 16 + buf.getIntLE(offset + 8);
         int cooldownIdLen = VarInt.peek(buf, varPos0);
         if (cooldownIdLen < 0) {
            throw ProtocolException.negativeLength("CooldownId", cooldownIdLen);
         }

         if (cooldownIdLen > 4096000) {
            throw ProtocolException.stringTooLong("CooldownId", cooldownIdLen, 4096000);
         }

         obj.cooldownId = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 16 + buf.getIntLE(offset + 12);
         int chargeTimesCount = VarInt.peek(buf, varPos1);
         if (chargeTimesCount < 0) {
            throw ProtocolException.negativeLength("ChargeTimes", chargeTimesCount);
         }

         if (chargeTimesCount > 4096000) {
            throw ProtocolException.arrayTooLong("ChargeTimes", chargeTimesCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos1);
         if (varPos1 + varIntLen + chargeTimesCount * 4L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("ChargeTimes", varPos1 + varIntLen + chargeTimesCount * 4, buf.readableBytes());
         }

         obj.chargeTimes = new float[chargeTimesCount];

         for (int i = 0; i < chargeTimesCount; i++) {
            obj.chargeTimes[i] = buf.getFloatLE(varPos1 + varIntLen + i * 4);
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 16;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 8);
         int pos0 = offset + 16 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 12);
         int pos1 = offset + 16 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1) + arrLen * 4;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.cooldownId != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.chargeTimes != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeFloatLE(this.cooldown);
      buf.writeByte(this.clickBypass ? 1 : 0);
      buf.writeByte(this.skipCooldownReset ? 1 : 0);
      buf.writeByte(this.interruptRecharge ? 1 : 0);
      int cooldownIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int chargeTimesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.cooldownId != null) {
         buf.setIntLE(cooldownIdOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.cooldownId, 4096000);
      } else {
         buf.setIntLE(cooldownIdOffsetSlot, -1);
      }

      if (this.chargeTimes != null) {
         buf.setIntLE(chargeTimesOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.chargeTimes.length > 4096000) {
            throw ProtocolException.arrayTooLong("ChargeTimes", this.chargeTimes.length, 4096000);
         }

         VarInt.write(buf, this.chargeTimes.length);

         for (float item : this.chargeTimes) {
            buf.writeFloatLE(item);
         }
      } else {
         buf.setIntLE(chargeTimesOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 16;
      if (this.cooldownId != null) {
         size += PacketIO.stringSize(this.cooldownId);
      }

      if (this.chargeTimes != null) {
         size += VarInt.size(this.chargeTimes.length) + this.chargeTimes.length * 4;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 16) {
         return ValidationResult.error("Buffer too small: expected at least 16 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int cooldownIdOffset = buffer.getIntLE(offset + 8);
            if (cooldownIdOffset < 0) {
               return ValidationResult.error("Invalid offset for CooldownId");
            }

            int pos = offset + 16 + cooldownIdOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for CooldownId");
            }

            int cooldownIdLen = VarInt.peek(buffer, pos);
            if (cooldownIdLen < 0) {
               return ValidationResult.error("Invalid string length for CooldownId");
            }

            if (cooldownIdLen > 4096000) {
               return ValidationResult.error("CooldownId exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += cooldownIdLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading CooldownId");
            }
         }

         if ((nullBits & 2) != 0) {
            int chargeTimesOffset = buffer.getIntLE(offset + 12);
            if (chargeTimesOffset < 0) {
               return ValidationResult.error("Invalid offset for ChargeTimes");
            }

            int posx = offset + 16 + chargeTimesOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ChargeTimes");
            }

            int chargeTimesCount = VarInt.peek(buffer, posx);
            if (chargeTimesCount < 0) {
               return ValidationResult.error("Invalid array count for ChargeTimes");
            }

            if (chargeTimesCount > 4096000) {
               return ValidationResult.error("ChargeTimes exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
            posx += chargeTimesCount * 4;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading ChargeTimes");
            }
         }

         return ValidationResult.OK;
      }
   }

   public InteractionCooldown clone() {
      InteractionCooldown copy = new InteractionCooldown();
      copy.cooldownId = this.cooldownId;
      copy.cooldown = this.cooldown;
      copy.clickBypass = this.clickBypass;
      copy.chargeTimes = this.chargeTimes != null ? Arrays.copyOf(this.chargeTimes, this.chargeTimes.length) : null;
      copy.skipCooldownReset = this.skipCooldownReset;
      copy.interruptRecharge = this.interruptRecharge;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof InteractionCooldown other)
            ? false
            : Objects.equals(this.cooldownId, other.cooldownId)
               && this.cooldown == other.cooldown
               && this.clickBypass == other.clickBypass
               && Arrays.equals(this.chargeTimes, other.chargeTimes)
               && this.skipCooldownReset == other.skipCooldownReset
               && this.interruptRecharge == other.interruptRecharge;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.cooldownId);
      result = 31 * result + Float.hashCode(this.cooldown);
      result = 31 * result + Boolean.hashCode(this.clickBypass);
      result = 31 * result + Arrays.hashCode(this.chargeTimes);
      result = 31 * result + Boolean.hashCode(this.skipCooldownReset);
      return 31 * result + Boolean.hashCode(this.interruptRecharge);
   }
}
