package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RootInteractionSettings {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 2;
   public static final int MAX_SIZE = 32768028;
   public boolean allowSkipChainOnClick;
   @Nullable
   public InteractionCooldown cooldown;

   public RootInteractionSettings() {
   }

   public RootInteractionSettings(boolean allowSkipChainOnClick, @Nullable InteractionCooldown cooldown) {
      this.allowSkipChainOnClick = allowSkipChainOnClick;
      this.cooldown = cooldown;
   }

   public RootInteractionSettings(@Nonnull RootInteractionSettings other) {
      this.allowSkipChainOnClick = other.allowSkipChainOnClick;
      this.cooldown = other.cooldown;
   }

   @Nonnull
   public static RootInteractionSettings deserialize(@Nonnull ByteBuf buf, int offset) {
      RootInteractionSettings obj = new RootInteractionSettings();
      byte nullBits = buf.getByte(offset);
      obj.allowSkipChainOnClick = buf.getByte(offset + 1) != 0;
      int pos = offset + 2;
      if ((nullBits & 1) != 0) {
         obj.cooldown = InteractionCooldown.deserialize(buf, pos);
         pos += InteractionCooldown.computeBytesConsumed(buf, pos);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 2;
      if ((nullBits & 1) != 0) {
         pos += InteractionCooldown.computeBytesConsumed(buf, pos);
      }

      return pos - offset;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.cooldown != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.allowSkipChainOnClick ? 1 : 0);
      if (this.cooldown != null) {
         this.cooldown.serialize(buf);
      }
   }

   public int computeSize() {
      int size = 2;
      if (this.cooldown != null) {
         size += this.cooldown.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 2) {
         return ValidationResult.error("Buffer too small: expected at least 2 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 2;
         if ((nullBits & 1) != 0) {
            ValidationResult cooldownResult = InteractionCooldown.validateStructure(buffer, pos);
            if (!cooldownResult.isValid()) {
               return ValidationResult.error("Invalid Cooldown: " + cooldownResult.error());
            }

            pos += InteractionCooldown.computeBytesConsumed(buffer, pos);
         }

         return ValidationResult.OK;
      }
   }

   public RootInteractionSettings clone() {
      RootInteractionSettings copy = new RootInteractionSettings();
      copy.allowSkipChainOnClick = this.allowSkipChainOnClick;
      copy.cooldown = this.cooldown != null ? this.cooldown.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof RootInteractionSettings other)
            ? false
            : this.allowSkipChainOnClick == other.allowSkipChainOnClick && Objects.equals(this.cooldown, other.cooldown);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.allowSkipChainOnClick, this.cooldown);
   }
}
