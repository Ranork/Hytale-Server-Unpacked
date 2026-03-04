package com.hypixel.hytale.server.core.modules.blockhealth;

import com.hypixel.hytale.math.util.MathUtil;
import io.netty.buffer.ByteBuf;
import java.time.Instant;
import javax.annotation.Nonnull;

public class BlockHealth implements Cloneable {
   public static final BlockHealth NO_DAMAGE_INSTANCE = new BlockHealth(1.0F, Instant.MIN) {
      @Override
      public void setHealth(float health) {
         throw new UnsupportedOperationException("NO_DAMAGE_INSTANCE is immutable!");
      }

      @Override
      public void setLastDamageGameTime(Instant lastDamageGameTime) {
         throw new UnsupportedOperationException("NO_DAMAGE_INSTANCE is immutable!");
      }
   };
   private float health;
   private Instant lastDamageGameTime;

   public BlockHealth() {
      this(1.0F, Instant.MIN);
   }

   public BlockHealth(float health, Instant lastDamageGameTime) {
      this.health = health;
      this.lastDamageGameTime = lastDamageGameTime;
   }

   public float getHealth() {
      return this.health;
   }

   public void setHealth(float health) {
      this.health = health;
   }

   public Instant getLastDamageGameTime() {
      return this.lastDamageGameTime;
   }

   public void setLastDamageGameTime(Instant lastDamageGameTime) {
      this.lastDamageGameTime = lastDamageGameTime;
   }

   public boolean isDestroyed() {
      return MathUtil.closeToZero(this.health) || this.health < 0.0F;
   }

   public boolean isFullHealth() {
      return this.health >= 1.0;
   }

   public void deserialize(@Nonnull ByteBuf buf, byte version) {
      this.health = buf.readFloat();
      this.lastDamageGameTime = Instant.ofEpochMilli(buf.readLong());
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeFloat(this.health);
      buf.writeLong(this.lastDamageGameTime.toEpochMilli());
   }

   @Nonnull
   protected BlockHealth clone() {
      return new BlockHealth(this.health, this.lastDamageGameTime);
   }

   @Nonnull
   @Override
   public String toString() {
      return "BlockHealth{health=" + this.health + ", lastDamageGameTime=" + this.lastDamageGameTime + "}";
   }
}
