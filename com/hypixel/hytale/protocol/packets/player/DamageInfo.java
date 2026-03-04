package com.hypixel.hytale.protocol.packets.player;

import com.hypixel.hytale.protocol.DamageCause;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.Vector3d;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DamageInfo implements Packet, ToClientPacket {
   public static final int PACKET_ID = 112;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 29;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 29;
   public static final int MAX_SIZE = 32768048;
   @Nullable
   public Vector3d damageSourcePosition;
   public float damageAmount;
   @Nullable
   public DamageCause damageCause;

   @Override
   public int getId() {
      return 112;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public DamageInfo() {
   }

   public DamageInfo(@Nullable Vector3d damageSourcePosition, float damageAmount, @Nullable DamageCause damageCause) {
      this.damageSourcePosition = damageSourcePosition;
      this.damageAmount = damageAmount;
      this.damageCause = damageCause;
   }

   public DamageInfo(@Nonnull DamageInfo other) {
      this.damageSourcePosition = other.damageSourcePosition;
      this.damageAmount = other.damageAmount;
      this.damageCause = other.damageCause;
   }

   @Nonnull
   public static DamageInfo deserialize(@Nonnull ByteBuf buf, int offset) {
      DamageInfo obj = new DamageInfo();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         obj.damageSourcePosition = Vector3d.deserialize(buf, offset + 1);
      }

      obj.damageAmount = buf.getFloatLE(offset + 25);
      int pos = offset + 29;
      if ((nullBits & 2) != 0) {
         obj.damageCause = DamageCause.deserialize(buf, pos);
         pos += DamageCause.computeBytesConsumed(buf, pos);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 29;
      if ((nullBits & 2) != 0) {
         pos += DamageCause.computeBytesConsumed(buf, pos);
      }

      return pos - offset;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.damageSourcePosition != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.damageCause != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      if (this.damageSourcePosition != null) {
         this.damageSourcePosition.serialize(buf);
      } else {
         buf.writeZero(24);
      }

      buf.writeFloatLE(this.damageAmount);
      if (this.damageCause != null) {
         this.damageCause.serialize(buf);
      }
   }

   @Override
   public int computeSize() {
      int size = 29;
      if (this.damageCause != null) {
         size += this.damageCause.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 29) {
         return ValidationResult.error("Buffer too small: expected at least 29 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 29;
         if ((nullBits & 2) != 0) {
            ValidationResult damageCauseResult = DamageCause.validateStructure(buffer, pos);
            if (!damageCauseResult.isValid()) {
               return ValidationResult.error("Invalid DamageCause: " + damageCauseResult.error());
            }

            pos += DamageCause.computeBytesConsumed(buffer, pos);
         }

         return ValidationResult.OK;
      }
   }

   public DamageInfo clone() {
      DamageInfo copy = new DamageInfo();
      copy.damageSourcePosition = this.damageSourcePosition != null ? this.damageSourcePosition.clone() : null;
      copy.damageAmount = this.damageAmount;
      copy.damageCause = this.damageCause != null ? this.damageCause.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof DamageInfo other)
            ? false
            : Objects.equals(this.damageSourcePosition, other.damageSourcePosition)
               && this.damageAmount == other.damageAmount
               && Objects.equals(this.damageCause, other.damageCause);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.damageSourcePosition, this.damageAmount, this.damageCause);
   }
}
